package com.kahomesl.allergenradar.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.kahomesl.allergenradar.data.ObservationDto
import com.kahomesl.allergenradar.data.RepositoryDataSource
import com.kahomesl.allergenradar.data.RiskSeverityDto
import com.kahomesl.allergenradar.domain.ArtemisiaPresentation
import com.kahomesl.allergenradar.ui.designsystem.*
import com.kahomesl.allergenradar.ui.viewmodel.HomeUiState
import com.kahomesl.allergenradar.util.formatCachedTime
import com.kahomesl.allergenradar.util.formatLocalTime

@Composable
fun PremiumHomeScreen(
    state: HomeUiState,
    onRefresh: () -> Unit,
    onOpenLocations: () -> Unit,
    onOpenDataInfo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = RadarSpacing.lg, vertical = RadarSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(RadarSpacing.md),
    ) {
        item {
            PageTitle(
                title = "过敏原雷达",
                subtitle = "当前位置 · ${state.locationName}",
                action = { IconButton(onClick = onRefresh, enabled = !state.isLoading) { Icon(Icons.Default.Refresh, "刷新数据") } },
            )
        }
        if (state.isLoading && state.total == null) item { HomeSkeleton() }
        state.errorMessage?.let { message -> item { HomeError(message, onRefresh) } }
        if (state.total != null || (!state.isLoading && state.errorMessage == null)) {
            item { TotalHero(state.total, state.totalSource, state.totalCachedAt) }
            item { ArtemisiaCard(state, onOpenLocations) }
            if (state.providersWithErrors.isNotEmpty()) item {
                InfoBanner(Icons.Default.Info, "部分数据源暂时不可用", "其余已获取的数据仍可正常查看。")
            }
            item {
                InfoBanner(
                    Icons.Default.Info,
                    "当前指数并非已确认的直接实测数据",
                    "了解数据类型、来源与风险等级的含义",
                    Modifier.clickable(onClick = onOpenDataInfo),
                )
            }
        }
    }
}

@Composable
private fun TotalHero(observation: ObservationDto?, source: RepositoryDataSource?, cachedAt: Long?) {
    RadarCard(highlighted = true) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconDisc(Icons.Default.LocalFlorist, "综合花粉", MaterialTheme.colorScheme.error)
            Spacer(Modifier.width(RadarSpacing.sm))
            Column {
                Text("综合花粉", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text("TOTAL", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (observation == null) {
            Text("当前暂无综合花粉数据", style = MaterialTheme.typography.titleMedium)
            Text("没有数据不代表低风险。", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(RadarSpacing.sm)) {
                Text(riskHeadline(observation), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                RiskBadge(observation.risk.severity, providerRiskDetail(observation))
            }
            Text(displayMeasurementLabel(observation.measurementType), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (observation.risk.severity == RiskSeverityDto.UNKNOWN) {
                Text("风险等级暂未标准化", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else RiskScale(observation.risk.severity)
            Row(horizontalArrangement = Arrangement.spacedBy(RadarSpacing.xs)) { SourceBadge(observation.source.name); DataTypeBadge(displayMeasurementLabel(observation.measurementType)) }
            formatLocalTime(observation.time.retrievedAt)?.let { Text("更新于 $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        if (source == RepositoryDataSource.CACHE) OfflineNotice(cachedAt)
    }
}

@Composable
private fun ArtemisiaCard(state: HomeUiState, onOpenLocations: () -> Unit) {
    RadarCard(highlighted = true) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconDisc(Icons.Default.Eco, "蒿属 Artemisia")
            Spacer(Modifier.width(RadarSpacing.sm))
            Column { Text("蒿属 Artemisia", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold); Text("独立属级数据", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        when (val presentation = state.artemisiaPresentation) {
            ArtemisiaPresentation.Available -> ArtemisiaObservation(state.artemisia!!, state.artemisiaSource, state.artemisiaCachedAt)
            ArtemisiaPresentation.Unsupported -> EmptyArtemisia("该地区暂未接入蒿属独立数据", "当前仅有综合花粉数据，不能据此判断蒿属风险。")
            is ArtemisiaPresentation.ChildLocationRequired -> {
                EmptyArtemisia("蒿属数据按${presentation.label}提供", "请选择具体区县查看蒿属预报。")
                OutlinedButton(onClick = onOpenLocations, modifier = Modifier.fillMaxWidth()) { Text("选择${presentation.label}") }
            }
            ArtemisiaPresentation.ValidEmpty -> EmptyArtemisia("当前时段暂无有效蒿属预报", "数据源当前未提供本时段有效结果。")
            ArtemisiaPresentation.CachedEmpty -> EmptyArtemisia("离线缓存的蒿属数据", "缓存结果可能不是当前时段的最新结果。")
            ArtemisiaPresentation.OfflineWithoutCache -> EmptyArtemisia("离线且暂无缓存的蒿属数据", "网络恢复后可再次刷新。")
            ArtemisiaPresentation.ProviderTemporarilyUnavailable -> EmptyArtemisia("蒿属数据源暂时不可用", "网络恢复后可再次刷新。")
            ArtemisiaPresentation.AvailabilityUnknown -> EmptyArtemisia("暂时无法确认蒿属数据状态", "请在网络恢复后刷新。")
        }
    }
}

@Composable
private fun ArtemisiaObservation(observation: ObservationDto, source: RepositoryDataSource?, cachedAt: Long?) {
    Text(if (observation.risk.severity == RiskSeverityDto.UNKNOWN) "风险等级暂未标准化" else riskHeadline(observation), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
    Text(displayMeasurementLabel(observation.measurementType), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    if (observation.risk.severity != RiskSeverityDto.UNKNOWN) RiskScale(observation.risk.severity)
    observationRange(observation)?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
    Row(horizontalArrangement = Arrangement.spacedBy(RadarSpacing.xs)) { SourceBadge(observation.source.name); DataTypeBadge(displayMeasurementLabel(observation.measurementType)) }
    formatLocalTime(observation.time.retrievedAt)?.let { Text("更新于 $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    if (source == RepositoryDataSource.CACHE) OfflineNotice(cachedAt)
}

@Composable private fun EmptyArtemisia(title: String, body: String) { Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold); Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
@Composable private fun OfflineNotice(cachedAt: Long?) { AssistChip(onClick = {}, label = { Text("离线缓存${formatCachedTime(cachedAt)?.let { " · 缓存于 $it" }.orEmpty()}") }, leadingIcon = { Icon(Icons.Default.CloudOff, null) }) }
@Composable private fun HomeSkeleton() { RadarCard { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()); Text("正在获取当前过敏原数据…", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
@Composable private fun HomeError(message: String, retry: () -> Unit) { RadarCard { Text("网络连接暂时不可用", style = MaterialTheme.typography.titleMedium); Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant); Button(onClick = retry) { Text("重试") } } }

fun displayMeasurementLabel(type: String): String = when (type) { "CURRENT" -> "当前指数"; "FORECAST" -> "预报"; "OBSERVATION" -> "实测"; "ESTIMATE" -> "平台估算"; else -> type }
fun riskHeadline(observation: ObservationDto): String = observation.risk.label?.takeIf { it.isNotBlank() } ?: if (observation.risk.severity == RiskSeverityDto.UNKNOWN) "—" else observation.risk.severity.name
fun providerRiskDetail(observation: ObservationDto): String = observation.risk.level?.let { "原始等级 $it" } ?: displayMeasurementLabel(observation.measurementType)
fun observationRange(observation: ObservationDto): String? = when { observation.minValue != null && observation.maxValue != null -> "范围 ${observation.minValue}–${observation.maxValue} ${observation.unit}"; observation.value != null -> "指数 ${observation.value} ${observation.unit}"; else -> null }
