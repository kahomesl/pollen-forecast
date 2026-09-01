# Android beta 发布清单

当前 beta 版本为 `0.1.0-beta.1`（versionCode `2`）。R8 在本 beta 保持关闭：尚未为全部依赖完成 Release 收缩/反射验证，不得将此状态误称为生产优化。

## 构建前

- [ ] 在真实设备或模拟器验证首次启动不会请求定位权限。
- [ ] 验证仅由“使用当前位置”触发粗略定位权限；拒绝后手动选择仍可用。
- [ ] 验证通知默认关闭，且只由用户主动开启后请求通知权限。
- [ ] 验证离线缓存标识、空蒿属和风险提醒去重行为。
- [ ] 为 Release 设置明确的 HTTPS `API_BASE_URL`；禁止空值、HTTP、`localhost`、`10.0.2.2` 与 `example.invalid`。
- [ ] 在环境中设置且仅设置以下签名变量，不提交任何签名材料：
  - `ALLERGENRADAR_RELEASE_STORE_FILE`
  - `ALLERGENRADAR_RELEASE_STORE_PASSWORD`
  - `ALLERGENRADAR_RELEASE_KEY_ALIAS`
  - `ALLERGENRADAR_RELEASE_KEY_PASSWORD`

## 验证命令

```powershell
cd android
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
.\gradlew.bat assembleDebug
.\gradlew.bat connectedDebugAndroidTest
```

Release 构建必须在已配置上述 URL 和签名环境变量的受控环境执行。Debug 默认仅用于模拟器访问宿主机的 `http://10.0.2.2:8080/`。

## 交付前

- [ ] 使用 `apksigner verify --verbose app/build/outputs/apk/debug/app-debug.apk` 验证候选 APK。
- [ ] 在 Pixel 9 Pro XL API 35 与 API 36 执行仪器测试；托管 CI 不运行模拟器测试。
- [ ] 检查 `git diff --check`、工作区干净以及提交已推送至目标分支。
- [ ] 确认部署 `/health` 可返回数据库状态；不要通过启动时全量抓取作为健康检查。
