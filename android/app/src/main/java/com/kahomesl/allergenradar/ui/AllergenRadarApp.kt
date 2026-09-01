package com.kahomesl.allergenradar.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kahomesl.allergenradar.AppContainer
import com.kahomesl.allergenradar.BuildConfig
import com.kahomesl.allergenradar.data.LocationDto
import com.kahomesl.allergenradar.data.ObservationDto
import com.kahomesl.allergenradar.data.ObservationTimeDto
import com.kahomesl.allergenradar.data.RepositoryDataSource
import com.kahomesl.allergenradar.data.RiskDto
import com.kahomesl.allergenradar.data.RiskSeverityDto
import com.kahomesl.allergenradar.data.SourceDto
import com.kahomesl.allergenradar.ui.theme.AllergenRadarTheme
import com.kahomesl.allergenradar.ui.viewmodel.HistoryMeasurementFilter
import com.kahomesl.allergenradar.ui.viewmodel.HistoryTaxonFilter
import com.kahomesl.allergenradar.ui.viewmodel.HistoryUiState
import com.kahomesl.allergenradar.ui.viewmodel.HistoryViewModel
import com.kahomesl.allergenradar.ui.viewmodel.HomeUiState
import com.kahomesl.allergenradar.ui.viewmodel.HomeViewModel
import com.kahomesl.allergenradar.ui.viewmodel.LocationUiState
import com.kahomesl.allergenradar.ui.viewmodel.LocationViewModel
import com.kahomesl.allergenradar.ui.viewmodel.MyUiState
import com.kahomesl.allergenradar.ui.viewmodel.MyViewModel
import com.kahomesl.allergenradar.ui.viewmodel.viewModelFactory
import com.kahomesl.allergenradar.util.formatLocalTime
import com.kahomesl.allergenradar.util.formatCachedTime

private enum class AppScreen(val label: String) {
    HOME("首页"), LOCATION("位置"), HISTORY("历史"), MY("我的"), DATA_INFO("数据说明"),
}

@Composable
fun AllergenRadarApp(container: AppContainer) {
    val homeViewModel: HomeViewModel = viewModel(factory = viewModelFactory { HomeViewModel(container.repository, container.locationPreference) })
    val locationViewModel: LocationViewModel = viewModel(factory = viewModelFactory { LocationViewModel(container.repository, container.locationPreference) })
    val historyViewModel: HistoryViewModel = viewModel(factory = viewModelFactory { HistoryViewModel(container.repository, container.locationPreference) })
    val myViewModel: MyViewModel = viewModel(factory = viewModelFactory { MyViewModel(container.repository, container.locationPreference) })
    val homeState by homeViewModel.state.collectAsStateWithLifecycle()
    val locationState by locationViewModel.state.collectAsStateWithLifecycle()
    val historyState by historyViewModel.state.collectAsStateWithLifecycle()
    val myState by myViewModel.state.collectAsStateWithLifecycle()
    var screen by remember { mutableStateOf(AppScreen.HOME) }

    if (screen == AppScreen.DATA_INFO) {
        DataExplanationScreen(onBack = { screen = AppScreen.MY })
        return
    }

    Scaffold(
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                NavItem(AppScreen.HOME, Icons.Default.Home, screen) { screen = AppScreen.HOME }
                NavItem(AppScreen.LOCATION, Icons.Default.LocationOn, screen) { screen = AppScreen.LOCATION }
                NavItem(AppScreen.HISTORY, Icons.Default.History, screen) { screen = AppScreen.HISTORY }
                NavItem(AppScreen.MY, Icons.Default.AccountCircle, screen) { screen = AppScreen.MY }
                }
            }
        },
    ) { padding ->
        when (screen) {
            AppScreen.HOME -> HomeContent(homeState, homeViewModel::refresh, Modifier.padding(padding))
            AppScreen.LOCATION -> LocationContent(locationState, locationViewModel::refresh, locationViewModel::select, Modifier.padding(padding))
            AppScreen.HISTORY -> HistoryContent(
                historyState,
                historyViewModel::refresh,
                historyViewModel::setTaxon,
                historyViewModel::setMeasurement,
                Modifier.padding(padding),
            )
            AppScreen.MY -> MyContent(myState, myViewModel::refresh, { screen = AppScreen.DATA_INFO }, Modifier.padding(padding))
            AppScreen.DATA_INFO -> Unit
        }
    }
}

@Composable
private fun RowScope.NavItem(screen: AppScreen, icon: androidx.compose.ui.graphics.vector.ImageVector, selected: AppScreen, onClick: () -> Unit) {
    val tint = if (selected == screen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = Modifier.weight(1f).clickable(onClick = onClick).padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Icon(icon, screen.label, tint = tint)
        Text(screen.label, color = tint, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun HomeContent(state: HomeUiState, onRefresh: () -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("过敏原雷达", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("当前位置 · ${state.locationName}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onRefresh) { Icon(Icons.Default.Refresh, "刷新数据") }
            }
        }
        if (state.isLoading) item { LoadingPanel("正在获取当前过敏原数据…") }
        state.errorMessage?.let { message -> item { ErrorPanel(message, onRefresh) } }
        if (!state.isLoading && state.errorMessage == null) {
            item {
                ObservationCard(
                    title = "综合花粉",
                    observation = state.total,
                    emptyText = "暂无综合花粉数据",
                    cacheLabel = state.totalSource.takeIf { it == RepositoryDataSource.CACHE }?.let { "当前显示离线缓存数据" },
                    cachedAt = state.totalCachedAt,
                )
            }
            item {
                ObservationCard(
                    title = "蒿属 Artemisia",
                    observation = state.artemisia,
                    emptyText = if (state.artemisiaOfflineWithoutCache) "离线且暂无缓存的蒿属数据" else "暂无蒿属独立数据",
                    highlight = true,
                    cacheLabel = state.artemisiaSource.takeIf { it == RepositoryDataSource.CACHE }?.let {
                        if (state.artemisia == null) "离线缓存的蒿属查询结果" else "离线缓存的蒿属数据"
                    },
                    cachedAt = state.artemisiaCachedAt,
                )
            }
            if (state.providersWithErrors.isNotEmpty()) {
                item {
                    AssistChip(
                        onClick = {},
                        label = { Text("部分数据源暂时不可用（${state.providersWithErrors.size}）") },
                        leadingIcon = { Icon(Icons.Default.Info, null, modifier = Modifier.size(18.dp)) },
                    )
                }
            }
            item { SemanticsNote() }
        }
    }
}

@Composable
private fun ObservationCard(
    title: String,
    observation: ObservationDto?,
    emptyText: String,
    highlight: Boolean = false,
    cacheLabel: String? = null,
    cachedAt: Long? = null,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (highlight) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        ),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            cacheLabel?.let { label ->
                Text(
                    "${label}${formatCachedTime(cachedAt)?.let { " · 缓存于 $it" }.orEmpty()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            if (observation == null) {
                Text(emptyText, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("没有数据不代表风险为零。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        riskLabel(observation),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = severityColor(observation.risk.severity),
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(measurementLabel(observation.measurementType), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                observation.value?.let { value -> Text("指数 ${formatNumber(value)} ${observation.unit.orEmpty()}") }
                rangeText(observation)?.let { Text(it) }
                ObservationTimeLines(observation)
                if (observation.risk.severity == RiskSeverityDto.UNKNOWN) {
                    Text("风险等级暂未标准化", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("来源：${observation.source.name}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ObservationTimeLines(observation: ObservationDto) {
    formatLocalTime(observation.time.retrievedAt)?.let { Text("数据更新于 $it", style = MaterialTheme.typography.bodySmall) }
    formatLocalTime(observation.time.observedAt)?.let { Text("观测时间：$it", style = MaterialTheme.typography.bodySmall) }
    val from = formatLocalTime(observation.time.validFrom)
    val to = formatLocalTime(observation.time.validTo)
    if (from != null || to != null) Text("预报有效期：${from ?: "—"} 至 ${to ?: "—"}", style = MaterialTheme.typography.bodySmall)
}

@Composable
private fun SemanticsNote() {
    Text(
        "“当前指数”不等同于实测；仅当数据类型为“实测”时才表示 OBSERVATION。",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
fun LocationContent(state: LocationUiState, onRefresh: () -> Unit, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    var query by remember { mutableStateOf("") }
    val visible = state.locations.filter { it.nameCn.contains(query, ignoreCase = true) }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("位置", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = onRefresh) { Icon(Icons.Default.Refresh, "刷新位置") }
            }
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("搜索城市或北京区县") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (state.isLoading) item { LoadingPanel("正在获取位置列表…") }
        state.errorMessage?.let { item { ErrorPanel(it, onRefresh) } }
        if (state.source == RepositoryDataSource.CACHE) item {
            OfflineCacheNotice("当前显示离线缓存位置", state.cachedAt)
        }
        groupedLocations(visible).forEach { (scope, locations) ->
            item { Text(if (scope == "CITY") "城市" else "北京区县", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 10.dp)) }
            items(locations, key = { it.id }) { location ->
                LocationRow(location, location.id == state.selectedLocationId) { onSelect(location.id) }
            }
        }
    }
}

private fun groupedLocations(locations: List<LocationDto>): List<Pair<String, List<LocationDto>>> = listOf("CITY", "DISTRICT")
    .map { scope -> scope to locations.filter { it.scope == scope } }
    .filter { it.second.isNotEmpty() }

@Composable
private fun LocationRow(location: LocationDto, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp),
    ) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Text(location.nameCn, modifier = Modifier.weight(1f))
            if (selected) Text("当前", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun HistoryContent(
    state: HistoryUiState,
    onRefresh: () -> Unit,
    onTaxonChange: (HistoryTaxonFilter) -> Unit,
    onMeasurementChange: (HistoryMeasurementFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("历史", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(state.locationName, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onRefresh) { Icon(Icons.Default.Refresh, "刷新历史") }
            }
        }
        item { FilterRow(HistoryTaxonFilter.entries.toList(), state.taxonFilter, { it.title }, onTaxonChange) }
        item { FilterRow(HistoryMeasurementFilter.entries.toList(), state.measurementFilter, { it.title }, onMeasurementChange) }
        if (state.source == RepositoryDataSource.CACHE) item { OfflineCacheNotice("离线历史缓存", state.cachedAt) }
        if (state.isLoading) item { LoadingPanel("正在获取历史数据…") }
        state.errorMessage?.let { item { ErrorPanel(it, onRefresh) } }
        if (!state.isLoading && state.errorMessage == null && state.observations.isEmpty()) item {
            EmptyPanel("暂无符合筛选条件的历史数据")
        }
        items(state.observations, key = { it.id }) { observation -> HistoryRow(observation) }
    }
}

@Composable
private fun <T> FilterRow(items: List<T>, selected: T, title: (T) -> String, onSelect: (T) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { item ->
            FilterChip(selected = item == selected, onClick = { onSelect(item) }, label = { Text(title(item)) })
        }
    }
}

@Composable
private fun HistoryRow(observation: ObservationDto) {
    Card(shape = RoundedCornerShape(14.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row {
                Text(observation.taxon?.nameCn ?: "综合花粉", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(riskLabel(observation), color = severityColor(observation.risk.severity))
            }
            Text(measurementLabel(observation.measurementType))
            formatLocalTime(observation.time.retrievedAt)?.let { Text("数据更新于 $it", style = MaterialTheme.typography.bodySmall) }
            Text("来源：${observation.source.name}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun MyContent(state: MyUiState, onRefresh: () -> Unit, onDataInfo: () -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("我的", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = onRefresh) { Icon(Icons.Default.Refresh, "刷新状态") }
            }
        }
        item { StatusCard("当前位置", state.locationName) }
        item { StatusCard("API 状态", when (state.apiAvailable) { true -> "连接正常"; false -> "暂时不可用"; null -> "正在检查" }) }
        item {
            val run = state.latestRun
            StatusCard(
                "最近同步",
                if (run == null) "暂无同步记录" else "${run.status} · ${run.locationsSucceeded}/${run.locationsAttempted} 个位置",
                detail = run?.finishedAt?.let { formatLocalTime(it)?.let { value -> "完成于 $value" } },
            )
        }
        item {
            ListItem(
                headlineContent = { Text("数据说明") },
                supportingContent = { Text("数据类型、时间字段和“无数据”的含义") },
                leadingContent = { Icon(Icons.Default.Info, null) },
                modifier = Modifier.clickable(onClick = onDataInfo),
            )
        }
        item { HorizontalDivider() }
        item { Text("版本 ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
private fun StatusCard(title: String, value: String, detail: String? = null) {
    Card(shape = RoundedCornerShape(14.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            detail?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun DataExplanationScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    Scaffold(topBar = {
        TopAppBar(title = { Text("数据说明") }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") }
        })
    }) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { ExplanationItem("综合花粉不等于蒿属", "综合花粉是 TOTAL 范围数据，不能替代 Artemisia 的独立预报。") }
            item { ExplanationItem("CURRENT 不等于实测", "CURRENT 表示当前指数；只有 OBSERVATION 才显示为“实测”。") }
            item { ExplanationItem("ESTIMATE 是平台估算", "ESTIMATE 为平台估算值，不应与实测或预报混为一谈。") }
            item { ExplanationItem("北京蒿属数据", "仅当上游提供有效的蒿属数据时才会显示，不会由综合花粉推算。") }
            item { ExplanationItem("没有数据不等于零", "暂无数据表示当前没有可用的独立结果，不代表不存在风险。") }
            item { ExplanationItem("时间含义", "“数据更新于”来自 retrievedAt；观测时间和预报有效期会在上游提供时单独显示。") }
        }
    }
}

@Composable
private fun ExplanationItem(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun LoadingPanel(label: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CircularProgressIndicator()
            Text(label)
        }
    }
}

@Composable
private fun EmptyPanel(label: String) {
    Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(14.dp)) {
        Text(label, modifier = Modifier.padding(20.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun OfflineCacheNotice(label: String, cachedAt: Long?) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = RoundedCornerShape(14.dp),
    ) {
        Text(
            "${label}${formatCachedTime(cachedAt)?.let { " · 缓存于 $it" }.orEmpty()}",
            modifier = Modifier.padding(14.dp),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun ErrorPanel(message: String, retry: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer), shape = RoundedCornerShape(14.dp)) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("暂时无法获取数据", fontWeight = FontWeight.Bold)
            Text(message, style = MaterialTheme.typography.bodySmall)
            Button(onClick = retry) { Text("重试") }
        }
    }
}

fun measurementLabel(type: String): String = when (type) {
    "CURRENT" -> "当前指数"
    "FORECAST" -> "预报"
    "OBSERVATION" -> "实测"
    "ESTIMATE" -> "估算"
    else -> type
}

fun riskLabel(observation: ObservationDto): String = observation.risk.label?.takeIf { it.isNotBlank() }
    ?: observation.risk.level?.let { "等级 $it" }
    ?: "暂无风险等级"

@Composable
private fun severityColor(severity: RiskSeverityDto) = when (severity) {
    RiskSeverityDto.VERY_HIGH, RiskSeverityDto.HIGH -> MaterialTheme.colorScheme.error
    RiskSeverityDto.MODERATE -> MaterialTheme.colorScheme.tertiary
    RiskSeverityDto.LOW -> MaterialTheme.colorScheme.primary
    RiskSeverityDto.UNKNOWN -> MaterialTheme.colorScheme.onSurfaceVariant
}

private fun rangeText(observation: ObservationDto): String? = when {
    observation.minValue != null && observation.maxValue != null -> "范围 ${formatNumber(observation.minValue)}–${formatNumber(observation.maxValue)} ${observation.unit.orEmpty()}"
    else -> null
}

private fun formatNumber(value: Double): String = if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()

private fun sampleObservation(type: String = "CURRENT", taxonName: String? = null) = ObservationDto(
    id = "preview-$type-$taxonName",
    locationId = "cn-city-beijing",
    taxon = taxonName?.let { com.kahomesl.allergenradar.data.TaxonDto("ARTEMISIA", "蒿属", "Artemisia") },
    scope = if (taxonName == null) "TOTAL" else "GENUS",
    measurementType = type,
    value = 4.0,
    unit = "level",
    risk = RiskDto(4, "高"),
    provider = "weatherdt",
    source = SourceDto("WeatherDT"),
    confidence = 3,
    time = ObservationTimeDto("2026-08-31T08:10:00.000Z"),
)

@Preview(showBackground = true)
@Composable
private fun HomeNormalPreview() = AllergenRadarTheme {
    HomeContent(HomeUiState(isLoading = false, total = sampleObservation(), artemisia = sampleObservation("FORECAST", "蒿属")), {})
}

@Preview(showBackground = true)
@Composable
private fun HomeArtemisiaEmptyPreview() = AllergenRadarTheme {
    HomeContent(HomeUiState(isLoading = false, total = sampleObservation()), {})
}

@Preview(showBackground = true)
@Composable
private fun HomeLoadingPreview() = AllergenRadarTheme { HomeContent(HomeUiState(isLoading = true), {}) }

@Preview(showBackground = true)
@Composable
private fun HomePartialProviderPreview() = AllergenRadarTheme {
    HomeContent(HomeUiState(isLoading = false, total = sampleObservation(), providersWithErrors = listOf("beijing-pollen")), {})
}

@Preview(showBackground = true)
@Composable
private fun HomeErrorPreview() = AllergenRadarTheme { HomeContent(HomeUiState(isLoading = false, errorMessage = "网络连接超时"), {}) }
