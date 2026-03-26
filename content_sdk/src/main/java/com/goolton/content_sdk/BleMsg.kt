package com.goolton.content_sdk

import com.google.gson.annotations.SerializedName

/**
 * 顶层消息
 * 只有当action为CMD 时，才读取CMD属性，否则忽略该属性
 */
data class BleMsg(
    val action: Action,               // key_event / open_app / text_input / app_data / cmd
    val pkg: String? = null,          // 仅 open_app / app_data 用到
    val data: Payload? = null,                 // 统一数据体
    val cmd: CMD = CMD.ACK
)

/** 可枚举也可直接 String；这里用 enum 方便 when(branch) */
enum class Action {
    @SerializedName("key_event")
    KEY_EVENT,

    @SerializedName("open_app")
    OPEN_APP,

    @SerializedName("text_input")
    TEXT_INPUT,

    @SerializedName("app_data")
    APP_DATA, //应用配置数据

    @SerializedName("load_app_data")
    LOAD_APP_DATA, //读取应用配置数据

    @SerializedName("add_file")
    ADD_FILE, //发送文件

    @SerializedName("delete_file")
    DELETE_FILE, //删除文件

    @SerializedName("download_file")
    DOWNLOAD_FILE, //下载文件

    @SerializedName("view_file")
    VIEW_FILE, //查看文件列表

    @SerializedName("rename_file")
    RENAME_FILE, //重命名文件

    @SerializedName("view_media")
    VIEW_MEDIA, //查看媒体列表

    @SerializedName("delete_media")
    DELETE_MEDIA, //删除媒体（按 MediaStore 管理的多媒体）

    @SerializedName("wifi_service_start")
    WIFI_SERVICE_START, //开启WIFI传输服务

    @SerializedName("wifi_service_stop")
    WIFI_SERVICE_STOP, //关闭WIFI传输服务

    @SerializedName("net_connect_config")
    NET_CONNECT_CONFIG, //发送WIFI连接状态

    @SerializedName("device_info")
    DEVICE_INFO, //获取设备信息

    @SerializedName("cmd")
    CMD, //SDK内部通讯协议相关，当为该类型时有SDK内部处理，不对外暴露
}

enum class CMD {
    @SerializedName("ack")
    ACK, //数据校验正常,本次通讯结束，如有数据未完成，可继续发送

    @SerializedName("reload")
    RELOAD, //重新发送数据或配置

    @SerializedName("error")
    ERROR, //通讯错误，检查通讯协议或参数是否正确

    @SerializedName("file_end")
    FILE_END, //文件发送完成
}

/** data 节点：所有字段可空，按场景取用即可 */
data class Payload(
    val data: String? = null,
    @SerializedName("key_code") val keyCode: Int? = null,
    val text: String? = null,
    val config: StyleConfig? = null,
    @SerializedName("navigation_data") val navigationData: Map<String, Any>? = null,
    val file: File? = null,
    val fileList: ArrayList<File>? = null
)

/**
 * 文件数据
 * @param name 文件名
 * @param size 大小
 * @param id 唯一id
 * @param time 创建时间
 */
data class File(
    val name: String,
    val size: Int,
    val id: Int,
    val time: Long,
    val type: String = "image",
    val size64: Long = size.toLong(),
    val duration: Long = 0,
)

data class StyleConfig(
    val fontSize: Int, //文字大小
    val fontColor: String, //文字颜色
    val speed: Int, // 提词器速度
    val hideText: Int = 0, // 0开启 ， 1 关闭 ，*关闭后，会议进行时AR端将不再显示文本。
    val following: Boolean = false, // 智能跟随
    val cameraRatio: String = "4_3", //3_4 三比四 9_16 9比16 默认四比三
    val watermarkStyle: Int = 0, //水印样式 0：关闭 1-3为样式
    val recordTime: Int = 1, //录制时长 1:1分钟 3:3分钟 10:10
    var topFileId: Int = -1, //语音备忘录置顶文件id
    val customConfig: Map<String, Any>? = null,//自定义配置
)

data class MediaRequest(
    val mediaType: String = "image", //媒体类型
    val page: Int, //页码
    val pageSize: Int = 100 //每页数量
)

/**
 * 网络配置
 */
data class NetConfig(
    val ip: String,
    val port: Int,
    val token: String,
    val exp: Long
)

/**
 * 网络帐号密码
 */
data class NetConnectConfig(
    val ssid: String,
    val pws: String,
    val connectStatus: Int = 0,// 0：无网络、1：连接网络但不可用、2：连接网络并可用（用于发送到手机的回调
)


/** 设备信息：序列号、电量、已安装应用包名列表 */
data class DeviceInfo(
    val battery: Int,           // 0..100
    val isCharging: Boolean = false,
    val serial: String,         // Build.getSerial()；拿不到时 "unknown"
    val apps: List<String>      // 包名列表（按字典序）
)

/**
 * 分页元信息
 */
data class PageMeta(
    val page: Int,
    val pageSize: Int,
    val total: Int,
    val totalPages: Int,
    val hasNext: Boolean,
    val mediaType: String
)
