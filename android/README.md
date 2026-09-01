# 过敏原雷达 Android 客户端

Phase A 是原生 Jetpack Compose 客户端，使用 Retrofit/OkHttp 访问 `docs/API_V1.md` 定义的只读 API。

## Phase C：离线缓存与后台刷新

客户端使用 Room 保存位置、最近成功的当前查询和历史查询。网络可用时 API 响应（包括空
`observations`）始终是权威结果；只有 I/O 或 5xx 时才会读取匹配的离线缓存，并在界面中明确
标识。WorkManager 每两小时仅刷新已选择的位置。详细语义见
[`docs/OFFLINE_CACHE.md`](docs/OFFLINE_CACHE.md)。

## Phase D：标准化风险与本地提醒

API v1 的 `risk.severity` 仅在 Provider 语义已确认时使用。风险提醒默认关闭，使用
DataStore 保存对象、阈值和去重状态；仅真实网络数据可以经既有 WorkManager 刷新触发。
详见 [`docs/NOTIFICATIONS.md`](docs/NOTIFICATIONS.md)。

## 构建环境

- Gradle 9.4.1（项目内 Gradle Wrapper）
- Android Gradle Plugin 9.2.0
- Kotlin 2.3.21
- compileSdk / targetSdk 37，minSdk 26
- JDK 17 或更高版本；本机已验证 JDK 25

默认 Debug API 地址为 Android 模拟器访问宿主机的 `http://10.0.2.2:8080/`。可在 `android/local.properties` 增加 `API_BASE_URL=https://.../`，或使用 `-PAPI_BASE_URL=https://.../` 覆盖。`local.properties` 不应提交。

Release 构建不会默认连接 localhost；发布前必须显式配置真实 HTTPS API 地址。

## 常用命令

在本目录执行：

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
```
