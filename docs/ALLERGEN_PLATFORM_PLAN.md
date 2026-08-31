# Allergen Platform Plan

## 1. 项目方向

当前项目由“综合花粉预报”升级为“个人过敏原预报平台”。

核心目标：

- 不再只显示笼统的花粉等级。
- 支持具体过敏原分类。
- 第一优先过敏原为蒿属（Artemisia / Mugwort）。
- 后续支持葎草、豚草、藜-苋科、禾本科、桦树、柏科等。
- 数据必须明确区分实测、官方预报和模型估算。
- 禁止将综合花粉数据伪装成物种级数据。

---

## 2. 核心原则

### 2.1 数据真实性优先

如果数据源只提供：

- 综合花粉
- 杂草花粉
- 花粉过敏指数

则不得自动解释为：

- 蒿属花粉浓度
- Artemisia 实测值

没有物种级数据时必须明确显示：

- 暂无独立监测数据
- 分类预报
- 模型估算

不得制造虚假的物种级精确度。

### 2.2 数据类型

平台统一将数据分为四类：

#### OBSERVATION

实际监测数据。

例如：

- 监测站直接采样
- 自动花粉监测设备
- 实验室识别后的实测结果

#### CURRENT

上游发布的当前期数据，但其生产方法无法确认是直接观测。

CURRENT 不能描述为实测、监测值或 OBSERVATION。例如 WeatherDT 当前综合
花粉风险指数属于 CURRENT；它保留当前风险信息，但不伪造直接测量语义。

#### FORECAST

官方或可信数据源提供的预测数据。

例如：

- 北京分类花粉预报
- 全国花粉风险预报

#### ESTIMATE

平台自身基于其他数据进行的估算。

例如：

- 附近城市插值
- 气象条件推算
- 历史花粉季模型
- 植被分布模型

### 2.3 分类精度

scope 支持：

- TOTAL
- CATEGORY
- FAMILY
- GENUS
- SPECIES

含义：

- TOTAL：综合花粉
- CATEGORY：大类，例如树木、禾本科、杂草
- FAMILY：科级
- GENUS：属级
- SPECIES：种级

蒿属统一定义为：

taxon_code: ARTEMISIA
taxon_name_cn: 蒿属
taxon_name_en: Artemisia
scope: GENUS

---

## 3. 数据来源架构

后端逐步改造成 Provider 模式。

PollenProvider
    |
    +-- WeatherDtProvider
    |
    +-- BeijingPollenProvider
    |
    +-- XianPollenProvider
    |
    +-- NationalPollenProvider
    |
    +-- Future Providers

每个 Provider 只负责获取和解析自己的原始数据。

统一流程：

External Data Source
        |
        v
PollenProvider
        |
        v
Normalizer
        |
        v
Unified Pollen Model
        |
        v
PostgreSQL
        |
        v
/api/v1
        |
        v
Web / Android App

Provider 之间必须相互独立。

新增或移除某个 Provider 时，不应破坏其他数据源。

---

## 4. 当前数据源

### 4.1 WeatherDT

当前项目的主要综合花粉来源。

用途：

- 全国主要城市综合花粉等级
- 当前已有 42 个重点城市目录
- 保留现有 Web 版基础能力

限制：

- 不提供蒿属等明确物种级数据
- 主要提供综合花粉风险
- 数据授权和长期使用方式需要进一步确认
- 不能作为蒿属真实浓度的数据来源

WeatherDT 数据只能归类为：

scope: TOTAL

不能自动归类为：

scope: GENUS
taxon_code: ARTEMISIA

---

### 4.2 Nearby Interpolation

当前项目已有基于附近城市的插值逻辑。

用途：

- 当某城市当天没有 WeatherDT 数据时提供辅助估算

当前逻辑：

- 仅使用 WeatherDT 当日数据
- 搜索 800 km 范围内邻近城市
- 至少需要 2 个有效邻居
- 使用反距离平方加权

这种数据必须标记为：

measurement_type: ESTIMATE
provider: nearby

不能与直接实测数据混淆。

如果无法可靠插值：

返回暂无数据

而不是强行生成结果。

---

### 4.3 北京花粉监测

北京是 Phase 1 重点接入地区。

计划接入：

- 北京 16 区花粉信息
- 实时站点监测数据
- 单站点 24 小时变化
- 总花粉预报
- 分类花粉预报

已确认存在的接口类型包括：

区域花粉数据
站点实时数据
24 小时历史数据
总量预报
分类花粉预报

重点验证分类接口中蒿属相关字段：

- plantCode
- plantName
- level
- min
- max
- description
- forecast time

正式接入前必须确认：

- 蒿属在上游接口中的实际名称
- 是否为“蒿属”
- 是否为“菊科”
- 是否有更具体的植物名称
- min/max 的实际含义
- 数据单位
- 风险等级含义

禁止仅凭名称猜测数据语义。

---

### 4.4 西安花粉监测

当前已确认西安存在真实花粉监测。

重点关注：

- 北大街监测点
- 西安交通大学第二附属医院相关监测
- 春季花粉季
- 秋季花粉季
- 蒿属
- 葎草
- 其他主要致敏植物

当前问题：

- 尚未确认公开稳定 API
- 公开信息中经常只提供综合花粉浓度
- 暂未确认稳定的蒿属独立实时接口

因此：

西安总花粉浓度

绝不能直接转换为：

西安蒿属花粉浓度

在没有物种级数据前，只能展示：

综合花粉
主要致敏花粉类型
蒿属风险提示

并明确数据性质。

---

### 4.5 全国花粉数据

后续计划研究国家级花粉预报产品。

用途：

- 全国综合花粉背景
- 无地方监测城市的风险底图
- 全国地图展示

原则：

如果全国数据只提供综合花粉：

scope: TOTAL

则不得转换为蒿属独立数据。

---

## 5. 统一 Taxon 定义

建立统一的过敏原分类字典。

第一阶段必须支持：

ARTEMISIA

定义：

code: ARTEMISIA
name_cn: 蒿属
name_en: Artemisia
alias:
  - Mugwort
  - Artemisia
scope: GENUS

后续计划支持：

HUMULUS
RAGWEED
CHENOPOD_AMARANTH
GRASS
BIRCH
CYPRESS
OAK
PINE
ALDER
ELM
WILLOW
POPLAR

不同 Provider 的原始名称必须经过映射后统一到内部 taxon_code。

例如：

Mugwort
Artemisia
蒿属
菊科蒿属

在确认语义一致后，可以统一映射为：

ARTEMISIA

如果无法确认，则不得强制归一化。

---

## 6. 统一数据模型

计划新增统一模型：

PollenObservation

字段：

id

location_id
station_id

taxon_code
taxon_name_cn
taxon_name_en
scope

measurement_type

value
min_value
max_value
unit

risk_level
risk_label

provider
source_name
source_url

confidence

observed_at
valid_from
valid_to

created_at
updated_at

### measurement_type

取值：

OBSERVATION
CURRENT
FORECAST
ESTIMATE

### scope

取值：

TOTAL
CATEGORY
FAMILY
GENUS
SPECIES

### unit

必须显式保存。

可能包括：

grains/m3
grains/1000mm2
index
level
unknown

不同单位之间不得在没有明确换算依据时直接比较。

---

## 7. 数据可信度

统一使用 1～5 级可信度。

建议定义：

5 = 物种级直接实测
4 = 官方物种级或分类预报
3 = 官方综合花粉实测或高质量总量数据
2 = 平台模型估算
1 = 低可信度辅助数据

注意：

confidence 不是医学准确率。

它只描述：

当前这条数据相对于目标过敏原的直接程度和来源可靠性

客户端必须展示：

- 数据类型
- Provider
- 数据来源
- 可信度
- 更新时间

示例：

蒿属
很高

分类预报
北京花粉监测

可信度：4/5

或者：

蒿属风险
中等

模型估算
当前地区暂无蒿属直接监测

可信度：2/5

---

## 8. 蒿属优先策略

平台第一阶段重点支持：

ARTEMISIA
蒿属
Artemisia
Mugwort

所有新功能优先围绕蒿属验证。

包括：

- 分类数据
- 地区覆盖
- 预报
- 历史变化
- 地图
- 用户关注
- 后续通知

第一阶段不要求全国所有城市都有蒿属真实数据。

允许不同地区存在不同数据等级。

例如：

北京
蒿属分类预报
confidence: 4

西安
综合花粉实测 + 蒿属季节提示
confidence: 3

普通城市
蒿属模型风险
confidence: 2

---

## 9. Provider 接口设计

计划定义统一 Provider 接口。

概念结构：

PollenProvider

id
name
supportedLocations
supportedTaxa
capabilities

fetchCurrent()
fetchHistory()
fetchForecast()

capabilities 可包括：

TOTAL_OBSERVATION
TOTAL_CURRENT
TOTAL_FORECAST
CATEGORY_FORECAST
GENUS_CURRENT
GENUS_OBSERVATION
GENUS_FORECAST
HISTORY

每个 Provider 必须明确声明自己支持什么。

例如：

WeatherDtProvider

TOTAL_CURRENT
TOTAL_FORECAST
HISTORY

不能声明：

GENUS_OBSERVATION

除非数据源确实提供属级数据。

---

## 10. API V1

未来 Android App 使用新的版本化 API。

计划接口：

GET /api/v1/allergens

返回平台支持的过敏原。

例如：

ARTEMISIA
HUMULUS
RAGWEED

GET /api/v1/providers

返回当前数据源和能力。

GET /api/v1/locations/:locationId/allergens

返回某地区当前可用过敏原数据。

GET /api/v1/locations/:locationId/allergens/:taxon

例如：

GET /api/v1/locations/beijing-chaoyang/allergens/artemisia

返回蒿属当前数据。

GET /api/v1/observations

用于查询观测记录。

GET /api/v1/allergens/artemisia/forecast

用于蒿属预报数据。

现有接口暂时保留：

/api/cities
/api/city-options
/api/my-city
/api/scrape-status
/api/pollen
/api/pollen/:city
/api/rating
/api/ratings/:city

Phase 1 不破坏当前 Web 前端。

---

## 11. Web 版策略

Phase 1 期间现有 Web UI 暂时保持。

目标：

- 原页面继续可用
- 原地图继续可用
- 原城市详情继续可用
- 原接口保持兼容

新过敏原能力优先从后端开始。

等统一数据层稳定后，再考虑 Web UI 升级。

---

## 12. Android App 产品方向

未来 Android App 不再只是现有网页的 WebView 包装。

采用原生 Android。

首页优先显示：

当前城市
我的过敏原
风险等级
趋势
数据来源
可信度
天气影响
综合花粉

示例：

西安 · 雁塔区

我的过敏原

蒿属 Artemisia

较高
4 / 5

风险正在升高

数据类型
模型预报

数据可信度
2 / 5

综合花粉
很高

---

## 13. 地图设计

地图支持按过敏原切换。

图层：

我的过敏原
蒿属
杂草
禾本科
树木
综合花粉

颜色只能表达当前选择图层的风险。

例如选择：

蒿属

地图上的红色表示：

蒿属风险高

不能表示综合花粉高。

---

## 14. 用户过敏原配置

未来支持用户配置关注过敏原。

例如：

主要过敏原
ARTEMISIA

其他关注
HUMULUS
RAGWEED
GRASS

App 首页优先显示主要过敏原。

第一版 Android App 可以只支持本地保存。

不需要登录。

---

## 15. Phase 1 开发范围

Phase 1 只做后端数据底座。

目标：

1. 保持现有 Web 功能正常。
2. 保持现有 `/api/pollen` 正常。
3. 建立 Provider 基础接口。
4. 将现有 WeatherDT 逻辑逐步抽象为 WeatherDtProvider。
5. 建立统一 Taxon 定义。
6. 加入 ARTEMISIA。
7. 建立统一 Observation 类型。
8. 建立 measurement_type。
9. 建立 scope。
10. 建立 confidence。
11. 建立 BeijingPollenProvider。
12. 接入北京分类花粉预报。
13. 验证北京上游蒿属名称和 plantCode。
14. 新增 `/api/v1`。
15. 添加后端测试。
16. 保证旧接口兼容。

---

## 16. Phase 1 不做

Phase 1 暂时不做：

- Android UI
- iOS
- 用户登录
- 用户注册
- 云同步
- FCM 推送
- 蒿属全国预测模型
- AI 医疗建议
- 药物推荐
- 用户症状诊断

---

## 17. 医疗内容原则

本项目是环境过敏原信息工具。

不是医疗诊断软件。

App 不应基于花粉等级直接给出具体药物服用建议。

例如避免直接写：

请服用某种抗过敏药物

更合适的表达是：

建议减少高风险时段户外活动。
外出时可采取适当防护。
如出现明显过敏症状，请按照医生建议处理。

---

## 18. 数据缺失原则

如果某地区没有对应数据：

应返回：

暂无数据

或：

暂无蒿属独立监测数据

不能：

- 用综合花粉冒充蒿属
- 用杂草花粉冒充蒿属
- 无限扩大插值距离
- 用单一远距离城市替代本地数据
- 为了保证地图“全有颜色”而制造数据

---

## 19. 时间处理原则

现有项目使用：

new Date().toISOString().split('T')[0]

这使用 UTC 日期。

对于中国地区可能在北京时间：

00:00 - 07:59

出现日期错误。

后续应统一建立中国时区日期工具。

目标时区：

Asia/Shanghai

所有中国花粉数据的：

- today
- observed_at
- forecast date
- cache date

必须明确时区。

---

## 20. 抓取调度原则

现有项目由客户端请求和服务启动触发抓取。

长期方向应调整为：

后台定时任务
        |
        v
Provider 更新数据
        |
        v
PostgreSQL
        |
        v
客户端只读取

客户端访问 API 不应成为主要抓取触发方式。

Phase 1 可以保持旧行为兼容。

后续再迁移。

---

## 21. 当前 fallback 链

当前综合花粉逻辑：

P0 WeatherDT
      |
      | 当天无数据
      v
P1 QWeather
      |
      | 无 Key 或无数据
      v
P2 Nearby interpolation
      |
      | 无可靠邻居
      v
No Data

当前原则：

宁可 No Data
也不生成低可信度伪数据

---

## 22. 当前已发现并修复的问题

原逻辑存在：

最近 7 天任意一天抓到数据

就把城市认定为：

今天已有数据

导致部分城市当天缺失时不会进入 fallback。

现已修改为：

抓取完成后
查询数据库中今天实际存在的城市
再计算 missingCities

当前验证结果：

WeatherDT 当天数据：27 城
Nearby 补全：14 城
无可靠 fallback：1 城

无可靠 fallback 城市：

拉萨

当前 API：

/api/pollen

返回：

41 城

这是可接受结果。

不为了凑满 42 城强行生成拉萨数据。

---

## 23. Phase 1 验收标准

Phase 1 必须同时满足：

- 原 `/api/pollen` 可以继续使用。
- 当前 Web 前端不被破坏。
- Provider 之间互相独立。
- ARTEMISIA 有统一 taxon_code。
- 数据可以区分 TOTAL / CATEGORY / FAMILY / GENUS / SPECIES。
- 数据可以区分 OBSERVATION / CURRENT / FORECAST / ESTIMATE，且 CURRENT 不伪装为实测。
- API 返回 provider。
- API 返回 source。
- API 返回 confidence。
- 北京分类花粉数据可以转换为统一模型。
- 北京蒿属字段经过真实接口验证。
- 没有物种数据时不伪造物种数据。
- 时间使用明确时区。
- 单元测试通过。
- Bun build / test 通过。
- 原 Web 功能可运行。
- Git working tree clean。

---

## 24. 后续阶段

### Phase 2

北京蒿属完整数据链。

包括：

- 北京实时站点
- 分类预报
- 蒿属历史
- 蒿属详情 API

### Phase 3

扩展地区数据源。

重点：

- 西安
- 呼和浩特
- 鄂尔多斯
- 内蒙古其他重点地区

### Phase 4

全国蒿属风险模型。

可能使用：

- 气温
- 湿度
- 风速
- 风向
- 降水
- 植被分布
- 历史花粉季
- 附近真实监测站

必须与实测数据明确区分。

### Phase 5

Android 原生 App。

技术方向：

- Kotlin
- Jetpack Compose
- Material 3
- MVVM
- Repository
- Room
- Retrofit
- OkHttp
- Coroutines
- StateFlow

---

## 25. Android 第一版计划

第一版计划功能：

1. 自动定位。
2. 当前城市。
3. 我的主要过敏原。
4. 蒿属风险。
5. 数据来源。
6. 数据可信度。
7. 今日趋势。
8. 未来预报。
9. 综合花粉。
10. 城市搜索。
11. 收藏城市。
12. 本地缓存。
13. 地图。
14. 数据说明。

第一版暂时不要求：

- 登录
- 云账户
- 社交
- 复杂个人档案

---

## 26. 当前开发环境

当前 Windows 开发环境：

Project:
C:\Users\PC\Desktop\ai\Pollen Forecast

Git 分支：

feature/allergen-platform

远程：

origin:
kahomesl/pollen-forecast

upstream:
PeterChen1997/pollen-forecast

运行环境：

Node.js 24.20.0
Bun 1.4.0
PostgreSQL 18.6

本地数据库：

pollen_forecast

后端：

http://localhost:8080

---

## 27. 已完成提交

已完成基线修复：

daf4180
fix: detect missing cities from today's pollen data

代码格式修复：

5546d8f
style: fix scraper loop indentation

---

## 28. 当前开发原则

所有后续开发必须遵守：

> 数据真实性优先于数据完整性。

> 宁可显示暂无数据，也不制造虚假的物种级精确度。

> 综合花粉不能直接等同于蒿属花粉。

> 实测、预报和模型估算必须明确区分。

> 新架构不得破坏现有 Web 基线。

---

## 29. 下一步

下一步进入 Phase 1。

第一项任务：

建立统一 Taxon 定义和 Provider 基础接口。

暂时不直接修改现有 WeatherDT 业务逻辑。

先建立可独立测试的新架构骨架，再逐步迁移现有代码。

---

## 30. Observation 持久化原则

统一 Observation 使用 additive `pollen_observations` 表保存；它不替换既有
`pollen_data`、`scrape_log` 或 `pollen_ratings`。Provider 当前结果先返回给 API，
再尽力写入记录层；持久化失败不能让真实 Provider 数据消失，也不能泄露数据库细节。

`pollen_observations` 使用 observation `id` 作为 upsert identity，保存
OBSERVATION / CURRENT / FORECAST / ESTIMATE 及可空时间字段。当前接口不得把旧记录
作为无数据时的自动 fallback；历史记录只能由明确的 history 查询返回。

现阶段 schema 由 additive initialization 建立。生产环境需要在引入破坏性变更前
迁移到正式 migration system。
