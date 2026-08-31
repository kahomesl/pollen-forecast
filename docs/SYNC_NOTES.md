# Pollen 同步说明

## 同步方式

`PollenSyncService` 直接使用 canonical locations 和已注册 Provider：

```text
canonical locations
  → provider.supportsLocation(locationId)
  → fetchCurrent / fetchForecast
  → PollenObservation
  → ObservationStore
  → pollen_observations
```

它不通过 HTTP 请求本地 API，也不依赖用户访问 API。现有 legacy `runScrape()` 与
`pollen_data` 并存，旧 `/api/pollen` 行为保持不变；同步服务不会把历史记录当作
当前无数据时的 fallback。

当前同步目标是 42 个 WeatherDT 城市和 16 个北京区级位置。Provider 的
`supportsLocation()` 是边界：北京区不会发送给 WeatherDT，普通城市不会发送给
BeijingPollenProvider。综合数据保持 `TOTAL`；北京分类上游若 `isValid=false`，
返回空结果且不写假 ARTEMISIA。

## CLI

执行一次同步：

```bash
bun run sync:pollen
```

CLI 只执行 `initDB → PollenSyncService`，不启动 HTTP server，也不启动 legacy
`runScrape()`。输出只包含状态、位置计数和 Observation 计数，不包含数据库连接串、
带 query 的上游 URL 或 stack trace。

## 并发、timeout 与 retry

- `POLLEN_SYNC_CONCURRENCY` 默认 3，合法范围实际限制为 1–10，超大值钳制为 10，
  非法、0 或负数回退为 3。
- `POLLEN_PROVIDER_TIMEOUT_MS` 默认 10 秒；有效值最低 1 秒，最高钳制为 60 秒。
- 单次请求由 Provider 使用 AbortSignal timeout；网络错误、timeout 和 HTTP 5xx
  只额外 retry 1 次，间隔 100ms。
- HTTP 4xx、正常空数据、北京 `isValid=false` 和解析后合法的 `[]` 不 retry。

## 运行状态与重叠保护

每次 CLI 或 scheduler 同步在 additive `pollen_sync_runs` 中记录一次运行，状态为
`RUNNING`、`SUCCESS`、`PARTIAL` 或 `FAILED`。只保存聚合计数、时间和 trigger；
Provider 错误只在执行结果中分类为 `TIMEOUT`、`NETWORK`、`HTTP` 或 `UNKNOWN`，不
保存原始异常文本。

本地 scheduler 只有 `POLLEN_BACKGROUND_SYNC_ENABLED=true` 才启用，默认关闭；默认
间隔 60 分钟，配置小于 15 分钟时钳制为 15 分钟。scheduler 第一次执行延迟到第一
个 interval，不做 startup full sync。共享的 scheduler 在上一轮仍运行时会跳过下一
轮，不并发启动第二轮全量同步。

这不能解决多副本部署时的跨进程重复：内存中的重叠保护只覆盖同一个进程。正式多
实例生产环境优先使用外部 cron/scheduler 调用一次性 sync job，而不是让每个 web
replica 都运行 interval；后续可增加数据库锁或专用任务队列。
