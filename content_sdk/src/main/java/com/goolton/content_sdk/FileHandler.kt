package com.goolton.content_sdk

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import java.io.IOException

object FileHandler {
    private val tag = this::class.simpleName

    /**
     * 扫描缓存目录 /cache/<pkg>/ 下的全部文件并封装成 File 列表
     */
    fun scanLocalFiles(context: Context, path: String): ArrayList<File> {
        val res = arrayListOf<File>()

        val cacheDir = context.cacheDir
            ?: throw IllegalStateException("BleService not ready")

        val dir = java.io.File(cacheDir, path)          // /data/user/0/…/cache/<pkg>
        if (!dir.exists() || !dir.isDirectory) return res   // 无文件

        dir.listFiles()?.forEach { f ->
            if (f.isFile) {
                res += File(
                    name = f.name,
                    size = f.length().toInt(),
                    id = f.name.hashCode(),
                    time = f.lastModified(),
                )
            }
        }
        return res
    }

    /**
     * 加载文件
     */
    fun locateFile(context: Context, pkg: String, id: Int): java.io.File? {
        val dir = java.io.File(context.cacheDir, pkg)
        if (!dir.exists()) return null
        return dir.listFiles()?.firstOrNull { it.isFile && it.name.hashCode() == id }
    }

    /**
     * 保存文件到对应包名下
     */
    fun saveFile(context: Context?, uri: Uri?, name: String,path: String): Boolean {
        return try {
            val inS = context!!.contentResolver.openInputStream(uri!!) ?: throw IOException()
            val bytes = inS.readBytes()
            inS.close()
            context.revokeUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)

            // 保存到 cache/<pkg>/
            val dir = java.io.File(context.cacheDir, path).apply { mkdirs() }
            val dst = java.io.File(dir, name)
            dst.outputStream().use { it.write(bytes) }
            Log.i(tag,"文件已保存：$name")
            true
        } catch (e: Exception) {
            Log.e(tag,"文件保存失败：${e.message}")
            false
        }
    }
}