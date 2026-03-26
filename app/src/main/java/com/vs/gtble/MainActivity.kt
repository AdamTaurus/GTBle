package com.vs.gtble

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.goolton.content_sdk.Action
import com.goolton.content_sdk.BleMsg
import com.goolton.content_sdk.ContentCenterManager
import com.goolton.content_sdk.DeviceInfo
import com.goolton.content_sdk.File
import com.goolton.content_sdk.FileHandler
import com.goolton.content_sdk.Payload
import com.goolton.content_sdk.StyleConfig
import com.vs.gtble.databinding.ActivityMainBinding
import java.io.File as LocalFile
import java.text.SimpleDateFormat
import java.util.ArrayDeque
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity(), ContentCenterManager.Callback {

    companion object {
        private const val DOWNLOAD_CACHE_DIR = "content_center_downloads"
        private const val MAX_LOG_SIZE = 40
    }

    private lateinit var binding: ActivityMainBinding
    private val gson = Gson()
    private val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private val logLines = ArrayDeque<String>()
    private val remoteFiles = mutableListOf<File>()

    private var hasRegisteredListener = false
    private var isSdkActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupActions()
        updateConnectionState(getString(R.string.demo_status_waiting), R.color.brand_orange, false)
        appendLog("页面已创建，准备初始化 ContentCenterManager。")
        connectSdk()
    }

    override fun onDestroy() {
        releaseSdk()
        super.onDestroy()
    }

    override fun onCtrlReceive(message: String) {
        runOnUiThread {
            handleSdkMessage(message)
        }
    }

    /**
     * 统一绑定按钮行为，避免各个 API 的调用入口分散。
     */
    private fun setupActions() = with(binding) {
        btnReconnect.setOnClickListener {
            connectSdk()
        }
        btnRelease.setOnClickListener {
            releaseSdk()
        }
        btnReload.setOnClickListener {
            ensureSdkReady {
                val success = ContentCenterManager.reload()
                appendLog("调用 reload()，结果=$success")
            }
        }
        btnPushConfig.setOnClickListener {
            ensureSdkReady {
                pushDemoConfig()
            }
        }
        btnViewFiles.setOnClickListener {
            ensureSdkReady {
                remoteFiles.clear()
                val success = ContentCenterManager.viewFile()
                appendLog("调用 viewFile()，结果=$success")
            }
        }
        btnDeviceInfo.setOnClickListener {
            ensureSdkReady {
                val success = ContentCenterManager.sendCtrl(BleMsg(action = Action.DEVICE_INFO))
                appendLog("请求设备信息，结果=$success")
            }
        }
        btnUploadFile.setOnClickListener {
            ensureSdkReady {
                uploadDemoFile()
            }
        }
        btnDownloadFirst.setOnClickListener {
            ensureSdkReady {
                downloadFirstRemoteFile()
            }
        }
    }

    /**
     * 统一处理 SDK 的初始化与回调注册，按钮和页面启动都能复用。
     */
    private fun connectSdk() {
        if (!hasRegisteredListener) {
            ContentCenterManager.addListener(this)
            hasRegisteredListener = true
        }
        updateConnectionState(getString(R.string.demo_status_connecting), R.color.brand_orange, false)
        ContentCenterManager.init(applicationContext) { connected ->
            runOnUiThread {
                isSdkActive = connected
                val label = if (connected) {
                    getString(R.string.demo_status_connected)
                } else {
                    getString(R.string.demo_status_disconnected)
                }
                val color = if (connected) R.color.success else R.color.danger
                updateConnectionState(label, color, connected)
                appendLog("AIDL 连接状态变更：connected=$connected")
            }
        }
        appendLog("已调用 ContentCenterManager.init()。")
    }

    /**
     * 页面退出或手动释放时统一清理，避免重复注册监听。
     */
    private fun releaseSdk() {
        if (hasRegisteredListener) {
            ContentCenterManager.removeListener(this)
            hasRegisteredListener = false
        }
        ContentCenterManager.release(applicationContext)
        isSdkActive = false
        remoteFiles.clear()
        updateConnectionState(getString(R.string.demo_status_disconnected), R.color.danger, false)
        appendLog("已调用 ContentCenterManager.release() 并清空页面内的连接状态。")
    }

    /**
     * 把 SDK 回包按 action 分发，顺手更新 demo 页面上的可见状态。
     */
    private fun handleSdkMessage(rawMessage: String) {
        appendLog("收到原始消息：$rawMessage")
        val message = try {
            gson.fromJson(rawMessage, BleMsg::class.java)
        } catch (error: JsonSyntaxException) {
            appendLog("消息解析失败：${error.message}")
            return
        }

        when (message.action) {
            Action.APP_DATA -> handleAppData(message)
            Action.VIEW_FILE -> handleRemoteFileList(message.data?.fileList.orEmpty())
            Action.DOWNLOAD_FILE -> handleDownloadFile(message)
            Action.DEVICE_INFO -> handleDeviceInfo(message.data?.data)
            else -> {
                appendLog("暂未额外处理的 action：${message.action}")
            }
        }
    }

    private fun handleAppData(message: BleMsg) {
        val config = message.data?.config
        if (config != null) {
            appendLog(
                "收到配置：fontSize=${config.fontSize}, color=${config.fontColor}, " +
                    "speed=${config.speed}, following=${config.following}"
            )
            return
        }
        appendLog("收到 APP_DATA 扩展数据：${message.data?.data ?: "空"}")
    }

    private fun handleRemoteFileList(files: List<File>) {
        remoteFiles.clear()
        remoteFiles.addAll(files)
        if (files.isEmpty()) {
            appendLog("Content Center 当前没有可下载文件。")
            return
        }
        val summary = files.take(5).joinToString(separator = " | ") { file ->
            "${file.name}(id=${file.id}, size=${file.size64})"
        }
        val tailHint = if (files.size > 5) " | 其余 ${files.size - 5} 个已省略" else ""
        appendLog("远端文件列表已更新，共 ${files.size} 个：$summary$tailHint")
    }

    private fun handleDownloadFile(message: BleMsg) {
        val fileMeta = message.data?.file
        val uriText = message.data?.data
        if (fileMeta == null || uriText.isNullOrBlank()) {
            appendLog("下载回包缺少文件信息或 Uri，无法保存。")
            return
        }
        val saved = FileHandler.saveFile(
            context = applicationContext,
            uri = uriText.toUri(),
            name = fileMeta.name,
            path = DOWNLOAD_CACHE_DIR
        )
        val localCount = FileHandler.scanLocalFiles(applicationContext, DOWNLOAD_CACHE_DIR).size
        appendLog("下载文件 ${fileMeta.name} 完成，保存结果=$saved，本地缓存数=$localCount")
    }

    private fun handleDeviceInfo(rawData: String?) {
        if (rawData.isNullOrBlank()) {
            appendLog("设备信息为空。")
            return
        }
        val info = try {
            gson.fromJson(rawData, DeviceInfo::class.java)
        } catch (error: JsonSyntaxException) {
            appendLog("设备信息解析失败：${error.message}")
            return
        }
        appendLog(
            "设备信息：battery=${info.battery}, charging=${info.isCharging}, " +
                "serial=${info.serial}, apps=${info.apps.size}"
        )
    }

    /**
     * 构造一份明确的 APP_DATA 示例，便于联调时观察协议字段。
     */
    private fun pushDemoConfig() {
        val message = BleMsg(
            action = Action.APP_DATA,
            pkg = packageName,
            data = Payload(
                data = gson.toJson(
                    mapOf(
                        "type" to "demo_sync",
                        "source" to "app_module",
                        "ts" to System.currentTimeMillis()
                    )
                ),
                config = StyleConfig(
                    fontSize = 30,
                    fontColor = "#FF914D",
                    speed = 4,
                    following = true,
                    watermarkStyle = 1,
                    recordTime = 3
                )
            )
        )
        val success = ContentCenterManager.sendCtrl(message)
        appendLog("发送 APP_DATA 示例配置，结果=$success")
    }

    private fun uploadDemoFile() {
        val dir = LocalFile(filesDir, "demo_uploads").apply { mkdirs() }
        val file = LocalFile(dir, "content-sdk-demo-${System.currentTimeMillis()}.txt")
        file.writeText(
            buildString {
                appendLine("Content SDK Demo Upload")
                appendLine("pkg=$packageName")
                appendLine("time=${Date()}")
                appendLine("message=这是一份由 app 模块自动生成的演示文件。")
            }
        )
        ContentCenterManager.addFile(applicationContext, file)
        appendLog("已创建并上传示例文件：${file.name}")
    }

    private fun downloadFirstRemoteFile() {
        val targetFile = remoteFiles.firstOrNull()
        if (targetFile == null) {
            Toast.makeText(this, "请先点击“查看远端文件”获取列表", Toast.LENGTH_SHORT).show()
            appendLog("下载被跳过：尚未拿到远端文件列表。")
            return
        }
        val success = ContentCenterManager.downLoadFIle(targetFile)
        appendLog("请求下载远端文件 ${targetFile.name}，结果=$success")
    }

    private fun ensureSdkReady(action: () -> Unit) {
        if (!isSdkActive) {
            Toast.makeText(this, "Content Center 尚未连接，请先等待初始化完成", Toast.LENGTH_SHORT).show()
            appendLog("当前 AIDL 未连接，操作已拦截。")
            return
        }
        action()
    }

    private fun updateConnectionState(label: String, colorRes: Int, connected: Boolean) {
        binding.tvConnectionState.text = label
        binding.tvConnectionState.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 999f
            setColor(ContextCompat.getColor(this@MainActivity, colorRes))
        }
        binding.tvConnectionState.setTextColor(ContextCompat.getColor(this, R.color.white))
        binding.btnRelease.alpha = if (connected) 1f else 0.7f
        binding.btnRelease.visibility = View.VISIBLE
    }

    /**
     * 限制日志条数，保证演示页面长时间联调也不会无限膨胀。
     */
    private fun appendLog(message: String) {
        if (logLines.size >= MAX_LOG_SIZE) {
            logLines.removeFirst()
        }
        val timestamp = timeFormatter.format(Date())
        logLines.addLast("[$timestamp] $message")
        binding.tvLogs.text = logLines.joinToString(separator = "\n\n")
        binding.logScroll.post {
            binding.logScroll.fullScroll(View.FOCUS_DOWN)
        }
    }
}
