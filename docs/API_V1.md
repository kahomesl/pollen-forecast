# Allergen API v1 Contract

本文档冻结后端 `GET /api/v1` 第一版的 Android 可依赖契约。所有响应字段使用
camelCase；列表接口返回 JSON 数组，位置数据使用 canonical `locationId`。v1
只读，不提供未鉴权的同步写接口。

## Common observation shape

`PollenObservation` 在 API 中表示为：

```json
{
  "id": "weatherdt:cn-city-beijing:2026-08-31:4:CURRENT",
  "locationId": "cn-city-beijing",
  "taxon": { "code": "ARTEMISIA", "nameCn": "蒿属", "nameEn": "Artemisia" },
  "scope": "TOTAL",
  "measurementType": "CURRENT",
  "value": 4,
  "minValue": 12,
  "maxValue": 33,
  "unit": "level",
  "risk": { "level": 4, "label": "高" },
  "provider": "weatherdt",
  "source": { "name": "WeatherDT", "url": "https://graph.weatherdt.com/ty/pollen/v2/hfindex.html" },
  "confidence": 3,
  "time": {
    "observedAt": "2026-08-31T00:00:00.000Z",
    "validFrom": "2026-09-01T00:00:00.000Z",
    "validTo": "2026-09-02T00:00:00.000Z",
    "retrievedAt": "2026-08-31T08:10:00.000Z",
    "createdAt": "2026-08-31T08:10:00.000Z",
    "updatedAt": "2026-08-31T08:10:00.000Z"
  }
}
```

`taxon`、数值、`observedAt`、`validFrom` 和 `validTo` 依数据语义可省略；示例
中的字段不表示所有记录都会同时具备这些字段。`retrievedAt` 始终存在，等于
当前记录的 `updatedAt`，含义是平台最近一次从 Provider 获取并标准化这条记录
的时间。它不是 source observation time、forecast validity，也不是
`lastMeasuredAt`。`createdAt` 和 `updatedAt` 为兼容现有 v1 响应而保留。

`measurementType` 为 `OBSERVATION`、`CURRENT`、`FORECAST` 或 `ESTIMATE`；
`scope` 为 `TOTAL`、`CATEGORY`、`FAMILY`、`GENUS` 或 `SPECIES`。单位不能在
没有换算依据时直接比较。

## `GET /api/v1/allergens`

列出平台正式支持的过敏原。

- Method: `GET`
- Params/query: 无
- Success `200`:

```json
[
  {
    "code": "ARTEMISIA",
    "nameCn": "蒿属",
    "nameEn": "Artemisia",
    "aliases": ["Artemisia", "Mugwort", "蒿属", "菊科蒿属"],
    "scope": "GENUS"
  }
]
```

## `GET /api/v1/providers`

列出 Provider 元数据和能力声明。

- Method: `GET`
- Params/query: 无
- Success `200`:

```json
[
  {
    "id": "weatherdt",
    "name": "WeatherDT",
    "capabilities": ["TOTAL_CURRENT", "TOTAL_FORECAST", "HISTORY"],
    "supportedTaxa": []
  },
  {
    "id": "beijing-pollen",
    "name": "北京花粉监测",
    "capabilities": ["GENUS_FORECAST"],
    "supportedTaxa": ["ARTEMISIA"]
  }
]
```

## `GET /api/v1/locations`

列出可查询的 canonical 城市和北京区级位置。上游 WeatherDT city code 和北京
行政区 code 不在公开响应中。

- Method: `GET`
- Params/query: 无
- Success `200`:

```json
[
  {
    "id": "cn-city-beijing",
    "nameCn": "北京",
    "scope": "CITY",
    "latitude": 39.9042,
    "longitude": 116.4074
  },
  {
    "id": "cn-beijing-chaoyang",
    "nameCn": "朝阳区",
    "scope": "DISTRICT",
    "latitude": 39.9215,
    "longitude": 116.4864
  }
]
```

## `GET /api/v1/locations/:locationId/allergens`

获取位置当前可用的标准化观察/当前值和预报。接口直接查询 Provider；成功响应
会尽力持久化，但 DB 持久化失败不改变 Provider 响应。

- Method: `GET`
- Path param: `locationId`，例如 `cn-city-beijing`
- Query: 无
- Success `200`:

```json
{
  "location": { "id": "cn-city-beijing", "nameCn": "北京", "scope": "CITY" },
  "observations": [],
  "providersWithErrors": []
}
```

Provider 部分失败仍返回 `200`，`providersWithErrors` 只包含 Provider id；不会
返回原始异常消息。未知位置错误 `404`：

```json
{ "error": { "code": "LOCATION_NOT_FOUND", "message": "Unknown location" } }
```

## `GET /api/v1/locations/:locationId/allergens/:taxon`

获取指定位置和过敏原的数据，例如北京朝阳区的 Artemisia。

- Method: `GET`
- Path params: `locationId`、`taxon`；taxon code 不区分大小写
- Query: 无
- Success `200`:

```json
{
  "location": { "id": "cn-beijing-chaoyang", "nameCn": "朝阳区", "scope": "DISTRICT" },
  "observations": [
    {
      "id": "beijing-pollen:cn-beijing-chaoyang:JKHS:202608300900",
      "locationId": "cn-beijing-chaoyang",
      "taxon": { "code": "ARTEMISIA", "nameCn": "蒿属", "nameEn": "Artemisia" },
      "scope": "GENUS",
      "measurementType": "FORECAST",
      "minValue": 12,
      "maxValue": 33,
      "unit": "index",
      "risk": { "level": 2 },
      "provider": "beijing-pollen",
      "source": { "name": "北京花粉监测" },
      "confidence": 4,
      "time": {
        "retrievedAt": "2026-08-30T01:30:00.000Z",
        "createdAt": "2026-08-30T01:30:00.000Z",
        "updatedAt": "2026-08-30T01:30:00.000Z"
      }
    }
  ],
  "providersWithErrors": []
}
```

未知位置返回 `LOCATION_NOT_FOUND`；未知 taxon 返回 `TAXON_NOT_FOUND`，格式同
上面的结构化错误。

## `GET /api/v1/locations/:locationId/history`

从 `pollen_observations` 读取明确请求的历史记录，不作为当前数据 fallback。

- Method: `GET`
- Path param: `locationId`
- Query: `taxon` 可选；`measurementType` 可选；`limit` 可选，默认 100，范围 1–500
- Success `200`:

```json
{
  "location": { "id": "cn-city-beijing", "nameCn": "北京", "scope": "CITY" },
  "observations": []
}
```

错误：未知位置 `LOCATION_NOT_FOUND`（404）、未知 taxon `TAXON_NOT_FOUND`（404）、
非法 measurement type `INVALID_MEASUREMENT_TYPE`（400）、非法 limit
`INVALID_LIMIT`（400）、存储层未配置 `HISTORY_UNAVAILABLE`（503）。错误格式为：

```json
{ "error": { "code": "INVALID_LIMIT", "message": "limit must be an integer from 1 to 500" } }
```

## `GET /api/v1/sync/status`

返回最近一次后台/CLI 同步的公开聚合状态，不返回异常消息、stack、数据库连接串或
Provider 请求细节。

- Method: `GET`
- Params/query: 无
- Success `200`（从未运行时）：

```json
{ "latestRun": null }
```

- Success `200`（已有运行记录）：

```json
{
  "latestRun": {
    "status": "PARTIAL",
    "startedAt": "2026-08-31T00:00:00.000Z",
    "finishedAt": "2026-08-31T00:00:05.000Z",
    "trigger": "SCHEDULED",
    "locationsAttempted": 58,
    "locationsSucceeded": 57,
    "locationsFailed": 1,
    "observationsReceived": 41,
    "observationsPersisted": 41
  }
}
```

`status` 为 `RUNNING`、`SUCCESS`、`PARTIAL` 或 `FAILED`；`trigger` 为
`MANUAL`、`SCHEDULED` 或 `STARTUP`。本轮不开放 `POST /api/v1/sync`。
