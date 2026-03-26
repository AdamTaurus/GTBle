package com.goolton.content_sdk

import android.content.*
import android.net.Uri
import android.os.IBinder
import android.util.Log
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.android.m_ble.BleCallbackInterface
import com.android.m_ble.BleInterface
import com.google.gson.Gson
import java.util.concurrent.CopyOnWriteArrayList

/**
 * ContentCenter SDK 入口。
 *
 * 使用流程：
 * 1) ContentCenterManager.init(context)
 * 2) ContentCenterManager.addListener(callback)
 * 3) ContentCenterManager.sendCtrl(json)
 * 4) 在不需要时 ContentCenterManager.release(context)
 */
object ContentCenterManager {

    private const val TAG = "ContentCenterMgr"
    private const val ACTION_BIND = "com.goolton.ContentCenter"
    private const val SERVICE_PKG = "com.goolton.content_center"

    private val gson = Gson()

    // -------- 状态 --------
    @Volatile
    private var ble: BleInterface? = null
    private val listeners = CopyOnWriteArrayList<Callback>()

    /** SDK 回调接口 */
    interface Callback {
        fun onCtrlReceive(msg: String)        // 收到 Control JSON
    }

    private lateinit var onConnectListener: (onConnect: Boolean) -> Unit
    private var aidlConnect = false

    // -------- 公共 API --------
    fun init(ctx: Context, onConnectListener: (onConnect: Boolean) -> Unit = {}) {
        this.onConnectListener = onConnectListener
        if (ble != null) {
            if (aidlConnect) onConnectListener(true)
            return
        }
        currentPkg = ctx.packageName
        val intent = Intent(ACTION_BIND).setPackage(SERVICE_PKG)
        val result = ctx.bindService(intent, conn, Context.BIND_AUTO_CREATE)
        if (!result) Log.e(TAG, "服务未绑定")
    }

    fun release(ctx: Context) {
        try {
            ble?.unregisterReceive(ctx.packageName, stub)
        } catch (_: Exception) {
        }
        runCatching { ctx.unbindService(conn) }
        ble = null
        listeners.clear()
    }

    fun addListener(cb: Callback) = listeners.addIfAbsent(cb)
    fun removeListener(cb: Callback) = listeners.remove(cb)


    /** 发送 Control JSON 给内容中心*/
    fun sendCtrl(json: String): Boolean {
        Log.i(TAG, "发送数据给内容中心: $json")
        val svr = ble
        if (svr == null) {
            Log.w(TAG, "sendCtrl 失败，Service not ready; call init() first")
            return false
        }
        return try {
            svr.sendCtrl(currentPkg, json)
            true
        } catch (e: Exception) {
            Log.e(TAG, "sendCtrl 异常: ${e.message}")
            false
        }
    }

    /** 发送 Control JSON 给手机 */
    fun sendCtrl(msg: BleMsg): Boolean {
        return sendCtrl(gson.toJson(msg))
    }

    /**
     * 重新加载数据
     */
    fun reload(): Boolean {
       return sendCtrl(LOAD_CONFIG)
    }

    /**
     * 查看文件列表
     */
    fun viewFile(): Boolean {
        return sendCtrl(VIEW_FILE)
    }

    /**
     * 下载文件
     */
    fun downLoadFIle(file: File): Boolean {
        return sendCtrl(
            BleMsg(
                action = Action.DOWNLOAD_FILE,
                data = Payload(
                    file = file
                )
            )
        )
    }

    private const val AUTH = ".blefileprovider"

    /** 把 File 转 content://uri，自动处理私有目录 */
    private fun java.io.File.toShareUri(ctx: Context): Uri =
        FileProvider.getUriForFile(ctx, ctx.packageName + AUTH, this)

    /** 对外：上传 File（≤1 MB）到 Content-Center */
    fun addFile(ctx: Context, file: java.io.File) {
        require(file.length() <= 1_048_576) { "File too large" }
        val uri = file.toShareUri(ctx)
        // 临时授权给 Content-Center 主包
        val svrPkg = SERVICE_PKG         // Content-Center 包名
        ctx.grantUriPermission(
            svrPkg,
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        addFile(ctx, uri, file.name)
    }

    /** 上传通过 SAF 获得的 Uri */
    fun addFile(ctx: Context, uri: Uri, name: String) {
        val svrPkg = SERVICE_PKG
        ctx.grantUriPermission(svrPkg, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        sendCtrl(
            ADD_FILE.copy(
                pkg = ctx.packageName,
                data = Payload(
                    data = uri.toString(),
                    file = File(name = name, 0, 0, 0)
                )
            )
        )
    }

    // ----- 内部 -----
    private lateinit var currentPkg: String

    private val stub = object : BleCallbackInterface.Stub() {
        override fun onCtrlReceive(message: String) =
            listeners.forEach { it.onCtrlReceive(message) }
    }

    private val conn = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            ble = BleInterface.Stub.asInterface(binder)
            try {
                ble?.registerReceive(currentPkg, stub)
                Log.i(TAG, "AIDL connected & callback registered")
                aidlConnect = true
                onConnectListener(true)
            } catch (e: Exception) {
                Log.e(TAG, "register failed: ${e.message}")
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            ble = null
            Log.w(TAG, "AIDL disconnected")
            aidlConnect = false
            onConnectListener(false)
        }
    }
}
