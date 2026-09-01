package com.kahomesl.allergenradar.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.kahomesl.allergenradar.data.ObservationDto
import com.kahomesl.allergenradar.data.RepositoryDataSource
import com.kahomesl.allergenradar.data.RiskSeverityDto
import com.kahomesl.allergenradar.ui.designsystem.*
import com.kahomesl.allergenradar.ui.viewmodel.HistoryMeasurementFilter
import com.kahomesl.allergenradar.ui.viewmodel.HistoryTaxonFilter
import com.kahomesl.allergenradar.ui.viewmodel.HistoryUiState
import com.kahomesl.allergenradar.util.formatCachedTime
import com.kahomesl.allergenradar.util.formatLocalTime

@Composable
fun PremiumHistoryScreen(state: HistoryUiState, onRefresh: () -> Unit, onTaxonChange: (HistoryTaxonFilter) -> Unit, onMeasurementChange: (HistoryMeasurementFilter) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = RadarSpacing.lg, vertical = RadarSpacing.xl), verticalArrangement = Arrangement.spacedBy(RadarSpacing.md)) {
        item { PageTitle("历史", state.locationName, action = { IconButton(onClick = onRefresh) { Icon(Icons.Default.Refresh, "刷新历史") } }) }
        item { FilterGroup(HistoryTaxonFilter.entries.toList(), state.taxonFilter, { it.title }, onTaxonChange) }
        item { FilterGroup(HistoryMeasurementFilter.entries.toList(), state.measurementFilter, { it.title }, onMeasurementChange) }
        if (state.source == RepositoryDataSource.CACHE) item { InfoBanner(Icons.Default.Info, "离线历史缓存", "缓存于 ${formatCachedTime(state.cachedAt).orEmpty()}") }
        if (state.isLoading && state.observations.isEmpty()) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        state.errorMessage?.let { message -> item { RadarCard { Text("历史数据暂时不可用", style = MaterialTheme.typography.titleMedium); Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant); OutlinedButton(onClick = onRefresh) { Text("重试") } } } }
        if (!state.isLoading && state.errorMessage == null && state.observations.isEmpty()) item { RadarCard { Text("暂无符合筛选条件的历史数据", style = MaterialTheme.typography.titleMedium); Text("没有数据不代表低风险。", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        items(state.observations, key = { it.id }) { HistoryObservationRow(it, state.locationName, state.source) }
    }
}

@Composable private fun <T> FilterGroup(items: List<T>, selected: T, title: (T) -> String, onSelect: (T) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(RadarSpacing.xs)) { items(items) { item -> FilterChip(selected = item == selected, onClick = { onSelect(item) }, label = { Text(title(item)) }) } }
}

@Composable private fun HistoryObservationRow(observation: ObservationDto, locationName: String, source: RepositoryDataSource?) {
    RadarCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconDisc(if (observation.taxon == null) Icons.Default.History else Icons.Default.Info, null)
            Spacer(Modifier.width(RadarSpacing.sm))
            Column(Modifier.weight(1f)) { Text(observation.taxon?.nameCn ?: "综合花粉", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold); Text(displayMeasurementLabel(observation.measurementType), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            if (observation.risk.severity == RiskSeverityDto.UNKNOWN) DataTypeBadge("未标准化") else RiskBadge(observation.risk.severity, riskHeadline(observation))
        }
        Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(ButtonDefaults.IconSize), tint = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.width(RadarSpacing.xxs)); Text(locationName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        if (observation.risk.severity == RiskSeverityDto.UNKNOWN) Text("风险等级暂未标准化", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) else RiskScale(observation.risk.severity)
        Row(horizontalArrangement = Arrangement.spacedBy(RadarSpacing.xs)) { SourceBadge(observation.source.name); DataTypeBadge(displayMeasurementLabel(observation.measurementType)) }
        formatLocalTime(observation.time.retrievedAt)?.let { Text("更新于 $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        if (source == RepositoryDataSource.CACHE) Text("离线缓存", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
    }
}
