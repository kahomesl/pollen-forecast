# Production readiness audit

此文档记录当前实现与仍需由运营方处理的发布条件，不把未验证事项表述为已完成。

## 已实现的运行保护

- 服务启动先执行 PostgreSQL 表初始化及既有增量 schema 初始化；数据库连接池上限为 10，连接超时为 10 秒。
- `GET /health` 只执行轻量级数据库探针，返回 `status`、`timestamp` 与 `database`；不会调用抓取或同步。数据库不可用时返回 HTTP 503。
- 既有 `bun run sync:pollen` 保留为显式人工/外部调度同步入口。旧的服务启动全量抓取现在仅在 `POLLEN_STARTUP_SCRAPE_ENABLED=true` 时启用。
- `POLLEN_BACKGROUND_SYNC_ENABLED=true` 仍是独立的可选周期同步开关；默认关闭，间隔最小 15 分钟。
- Android Release API 地址和签名材料不会由源码默认值或版本库提供。

## 部署前仍需确认

- Fly.io 生产环境应设置 `DATABASE_URL` 与 `FLY_API_TOKEN`，并在部署后探测 `/health`。当前部署工作流只在 `main` 推送时运行。
- CORS 已改为显式 allowlist：staging/production 必须设置 HTTPS `ALLOWED_ORIGINS`，不接受 `*`。Android 客户端不依赖浏览器 CORS。
- staging/production 默认关闭外部花粉抓取及 WeatherDT/北京 Provider gate；旧 `/api/pollen*` 与按需抓取在关闭时只读取现存数据，不能刷新外部源。
- 公开 API 已提供请求 ID、受限 API 路径的进程内 429 限流与不含异常细节的统一错误边界。同步会记录 provider、状态、耗时、超时和行数，不写入原始 provider 错误内容。平台级 DDoS/WAF、日志保留与告警仍由运营方配置。
- WeatherDT 和北京花粉相关数据的长期抓取、再分发、商标及许可权利尚未由本项目验证。这是公开商业/大规模发布前的法律 blocker，不能以本文件替代授权。Phase G1 审计未找到可直接替代的全国 ARTEMISIA Provider；北京 `JKHS / 菊科蒿属` 是精确来源标签，但仍不构成缓存、离线、展示或通知的授权。详见 `ARTEMISIA_SOURCE_RESEARCH.md` 和 `ARTEMISIA_PROVIDER_SCORECARD.md`。
- 数据语义限制（CURRENT 非实测、TOTAL 非 Artemisia、北京 Artemisia severity 为 UNKNOWN）见 `DATA_SOURCE_NOTES.md` 与 `RISK_SEVERITY.md`；运营文案不得淡化这些限制。
- 应配置 PostgreSQL 备份、迁移演练、服务资源上限、错误告警和独立的上游可用性监测。它们目前不由仓库自动保证。

## 推荐发布门槛

1. `bun audit`、`bun test`、后端 TypeScript 检查与前端生产构建通过。
2. Android Debug 单元测试、lint、组装、两套 API 级别的仪器测试和 APK 签名验证通过。
3. 真实 HTTPS backend 的 `/health` 和 Android 只读 API smoke 通过。
4. 法务确认上游使用/再分发权限，部署方确认日志、备份、告警与故障响应责任人。
