# 过敏原雷达 Android 客户端

Phase A 是原生 Jetpack Compose 客户端，使用 Retrofit/OkHttp 访问 `docs/API_V1.md` 定义的只读 API。

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
