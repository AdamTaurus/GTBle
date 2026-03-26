# content_sdk

`content_sdk` 是一个面向业务应用的 Android SDK，用来和 `Content Center` 进程通信。  
SDK 底层通过 AIDL 绑定 `com.goolton.content_center`，对外提供了配置同步、控制消息收发、文件列表查询、文件上传下载等能力。

## 适用场景

- 业务应用需要接入 Content Center，并接收来自中心服务的消息回调
- 业务应用需要把配置同步给手机端或从 Content Center 拉取配置
- 业务应用需要管理归属于自己包名的文件

## 能力概览

- 初始化 SDK，并监听 AIDL 连接状态
- 注册消息监听器，接收 JSON 字符串回调
- 发送自定义 `BleMsg`
- 拉取应用配置
- 查询文件列表
- 上传文件到 Content Center
- 请求下载文件并保存到本地
- 获取设备信息

## 接入前提

1. 设备中需要安装并运行 `Content Center`，包名固定为 `com.goolton.content_center`。
2. `content_sdk` 已经自动带上运行期所需的 `gson` 依赖；如果接入方也要直接用 `Gson` 解析业务回包，可以再显式添加自己的 `gson` 依赖。
3. 上传文件时，单个文件建议不超过 `1 MB`。`ContentCenterManager.addFile(context, file)` 会直接做大小校验。
4. 绑定需要系统权限，对应系统签名文件请联系商务获取。
5. Demo `app` 已声明 `android:sharedUserId="android.uid.system"`，用于以系统应用身份联调。
6. SDK 自带 Manifest，会自动合并以下配置：
   - `com.goolton.permission.ContentCenter`
   - `queries` 中的 `com.goolton.ContentCenter`
   - `${applicationId}.blefileprovider`

## 本地签名配置

仓库默认不会提交系统签名文件和密码信息：

- 根目录 `platform.jks` 已加入 Git 忽略
- 根目录 `signing.properties` 已加入 Git 忽略
- `app` 会优先从本地 `signing.properties` 读取签名信息，为 `debug` 和 `release` 构建自动签名

本地文件格式如下：

```properties
storeFile=platform.jks
storePassword=你的 storePassword
keyAlias=platform
keyPassword=你的 keyPassword
```

## Gradle 依赖

### 通过 GitHub + JitPack 直接依赖

仓库已经配置好 `maven-publish` 与 `jitpack.yml`，推送到 GitHub 并打版本 tag 后，可以直接使用：

1. 在宿主工程的 `settings.gradle.kts` 或根 `build.gradle` 中加入 JitPack 仓库：

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

2. 在宿主模块中加入依赖：

```kotlin
dependencies {
    implementation("com.github.AdamTaurus.GTBle:content_sdk:1.0.1")
}
```

其中 `1.0.1` 需要替换成实际推送到 GitHub 的 tag 名。

### 同仓库模块依赖

```kotlin
dependencies {
    implementation(project(":content_sdk"))
}
```

`content_sdk` 会自动带上运行期所需的 `gson` 依赖；如果宿主本身也要直接使用 `Gson` 解析业务回包，再额外自行引入即可。

## 快速开始

### 1. 初始化并注册监听

推荐在应用内统一管理 SDK 的初始化和释放，例如放在 `Application`、单例管理类或常驻组件中。  
原因是 `release()` 会解绑服务并清空全部监听器，如果每个页面都独立调用，容易互相影响。

```kotlin
import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.goolton.content_sdk.Action
import com.goolton.content_sdk.BleMsg
import com.goolton.content_sdk.ContentCenterManager

class ContentSdkClient(
    private val appContext: Context
) : ContentCenterManager.Callback {

    private val gson = Gson()

    fun start() {
        // 初始化 SDK，并监听 AIDL 连接状态
        ContentCenterManager.init(appContext) { connected ->
            Log.d("ContentSdkClient", "AIDL connected = $connected")
        }
        ContentCenterManager.addListener(this)
    }

    fun stop() {
        ContentCenterManager.removeListener(this)
        ContentCenterManager.release(appContext)
    }

    override fun onCtrlReceive(message: String) {
        // 回调内容是 JSON 字符串，建议第一时间反序列化
        val msg = gson.fromJson(message, BleMsg::class.java)
        when (msg.action) {
            Action.APP_DATA -> {
                Log.d("ContentSdkClient", "收到 APP_DATA: $message")
            }

            Action.VIEW_FILE -> {
                Log.d("ContentSdkClient", "收到文件列表: $message")
            }

            Action.DOWNLOAD_FILE -> {
                Log.d("ContentSdkClient", "收到下载文件回包: $message")
            }

            else -> {
                Log.d("ContentSdkClient", "收到其他消息: $message")
            }
        }
    }
}
```

### 2. 发送消息

SDK 提供两种发送方式：

- 直接发送 JSON 字符串：`ContentCenterManager.sendCtrl(json)`
- 发送结构化对象：`ContentCenterManager.sendCtrl(bleMsg)`

更推荐使用结构化对象，便于维护协议字段。

## 常用 API

| API | 说明 |
| --- | --- |
| `ContentCenterManager.init(context, onConnectListener)` | 初始化 SDK，并绑定 `Content Center` 服务 |
| `ContentCenterManager.release(context)` | 解绑服务并清空当前监听器 |
| `ContentCenterManager.addListener(callback)` | 添加消息监听 |
| `ContentCenterManager.removeListener(callback)` | 移除消息监听 |
| `ContentCenterManager.sendCtrl(json)` | 发送原始 JSON |
| `ContentCenterManager.sendCtrl(msg)` | 发送 `BleMsg` |
| `ContentCenterManager.reload()` | 拉取当前应用配置 |
| `ContentCenterManager.viewFile()` | 查询当前应用的文件列表 |
| `ContentCenterManager.downLoadFIle(file)` | 请求下载指定文件 |
| `ContentCenterManager.addFile(context, file)` | 上传本地 `File` |
| `ContentCenterManager.addFile(context, uri, name)` | 上传通过 SAF 等方式拿到的 `Uri` |

## 典型使用方式

### 1. 拉取当前应用配置

```kotlin
ContentCenterManager.reload()
```

收到回调后，通常会得到一个 `Action.APP_DATA` 消息，配置位于 `data.config`：

```kotlin
override fun onCtrlReceive(message: String) {
    val msg = Gson().fromJson(message, BleMsg::class.java)
    if (msg.action == Action.APP_DATA && msg.data?.config != null) {
        val config = msg.data.config
        // 在这里应用字体大小、颜色、速度等配置
    }
}
```

### 2. 主动上报应用配置

如果业务方希望把当前配置同步给 Content Center / 手机端，可以主动发送 `APP_DATA`：

```kotlin
import com.goolton.content_sdk.Payload
import com.goolton.content_sdk.StyleConfig

val msg = BleMsg(
    action = Action.APP_DATA,
    // APP_DATA 场景建议显式带上当前应用包名，便于服务端按应用存储配置
    pkg = context.packageName,
    data = Payload(
        config = StyleConfig(
            fontSize = 32,
            fontColor = "#FFFFFF",
            speed = 5
        )
    )
)

ContentCenterManager.sendCtrl(msg)
```

### 3. 查询文件列表

```kotlin
ContentCenterManager.viewFile()
```

回调中会收到 `Action.VIEW_FILE`，文件列表位于 `data.fileList`：

```kotlin
override fun onCtrlReceive(message: String) {
    val msg = Gson().fromJson(message, BleMsg::class.java)
    if (msg.action == Action.VIEW_FILE) {
        val files = msg.data?.fileList.orEmpty()
        files.forEach { file ->
            Log.d("ContentSdkClient", "name=${file.name}, id=${file.id}, size=${file.size}")
        }
    }
}
```

### 4. 上传文件

上传本地文件：

```kotlin
val localFile = java.io.File(filesDir, "demo.txt")
ContentCenterManager.addFile(context, localFile)
```

上传 `Uri`：

```kotlin
val fileUri: Uri = TODO()
ContentCenterManager.addFile(context, fileUri, "demo.txt")
```

上传成功后，通常会收到带 `ACK` 的回调；如果失败，则会收到 `ERROR`。

### 5. 下载文件并保存到本地缓存

先通过 `viewFile()` 拿到文件元数据，然后调用：

```kotlin
val targetFile = fileList.first()
ContentCenterManager.downLoadFIle(targetFile)
```

回调中会收到 `Action.DOWNLOAD_FILE`，其中：

- `data.file`：文件元信息
- `data.data`：`content://` 格式的文件 `Uri`

可以直接使用 SDK 自带的 `FileHandler` 保存到本地缓存目录：

```kotlin
import androidx.core.net.toUri
import com.goolton.content_sdk.FileHandler

override fun onCtrlReceive(message: String) {
    val msg = Gson().fromJson(message, BleMsg::class.java)
    if (msg.action == Action.DOWNLOAD_FILE) {
        val fileName = msg.data?.file?.name ?: return
        val uri = msg.data?.data?.toUri() ?: return

        // 保存到 app 的 cache/content_center/ 目录下
        FileHandler.saveFile(
            context = appContext,
            uri = uri,
            name = fileName,
            path = "content_center"
        )
    }
}
```

如果需要读取已经缓存的文件，也可以使用：

```kotlin
val localFiles = FileHandler.scanLocalFiles(appContext, "content_center")
val localFile = FileHandler.locateFile(
    context = appContext,
    pkg = "content_center",
    id = localFiles.first().id
)
```

## 高级消息示例

### 获取设备信息

```kotlin
ContentCenterManager.sendCtrl(
    BleMsg(action = Action.DEVICE_INFO)
)
```

回调中 `data.data` 是一个 JSON 字符串，可反序列化为 `DeviceInfo`：

```kotlin
import com.goolton.content_sdk.DeviceInfo

override fun onCtrlReceive(message: String) {
    val gson = Gson()
    val msg = gson.fromJson(message, BleMsg::class.java)
    if (msg.action == Action.DEVICE_INFO && !msg.data?.data.isNullOrEmpty()) {
        val info = gson.fromJson(msg.data?.data, DeviceInfo::class.java)
        Log.d("ContentSdkClient", "battery=${info.battery}, serial=${info.serial}")
    }
}
```

### 监听 BLE 生命周期广播

部分场景下，SDK 回调会收到 `Action.APP_DATA`，但 `data.config` 为空，而 `data.data` 是一段业务 JSON。  
其中一种常见情况是 BLE 生命周期广播，内容类似：

```json
{
  "type": "ble_lifecycle",
  "state": "ON",
  "addr": "",
  "ts": 1710000000000
}
```

因此处理 `APP_DATA` 时，建议同时兼容这两类数据：

- `data.config`：应用配置
- `data.data`：字符串形式的扩展业务数据

## 注意事项

1. `onConnectListener` 只表示 SDK 与 `Content Center` 的 AIDL 连接是否成功，不等同于蓝牙是否已连接手机。
2. `release()` 会清空所有通过 `addListener()` 注册的监听器，多个页面共用时建议统一管理生命周期。
3. `ContentCenterManager.downLoadFIle(...)` 的方法名中 `FIle` 大小写是现有 API 定义，调用时请以实际代码为准。
4. `FileHandler.saveFile(...)` 会把文件保存到宿主应用的 `cache/<path>/` 目录下，适合做临时缓存；如果需要长期保存，请自行转存。
5. 自定义发送 `APP_DATA` 时，建议显式设置 `pkg = context.packageName`，否则服务端可能无法按应用维度保存配置。

## 协议对象

SDK 已经内置以下常用数据结构，可直接复用：

- `BleMsg`
- `Action`
- `CMD`
- `Payload`
- `File`
- `StyleConfig`
- `DeviceInfo`
- `NetConfig`
- `NetConnectConfig`
- `MediaRequest`
- `PageMeta`

推荐统一使用这些对象构造请求和解析响应，避免手写 JSON 字段名出错。
