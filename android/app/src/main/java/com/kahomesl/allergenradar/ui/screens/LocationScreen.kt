package com.kahomesl.allergenradar.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.kahomesl.allergenradar.data.LocationDto
import com.kahomesl.allergenradar.data.RepositoryDataSource
import com.kahomesl.allergenradar.ui.designsystem.*
import com.kahomesl.allergenradar.ui.viewmodel.LocationUiState
import com.kahomesl.allergenradar.util.formatCachedTime
import java.util.Locale

@Composable
fun PremiumLocationScreen(
    state: LocationUiState,
    onRefresh: () -> Unit,
    onSelect: (String) -> Unit,
    onUseCurrentLocation: () -> Unit,
    onConfirmNearbyLocation: () -> Unit,
    permissionDenied: Boolean,
    focusDistricts: Boolean,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    val visible = remember(state.locations, query) { state.locations.filter { it.nameCn.contains(query.trim(), true) } }
    val scopes = if (focusDistricts) listOf("DISTRICT", "CITY") else listOf("CITY", "DISTRICT")
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = RadarSpacing.lg, vertical = RadarSpacing.xl), verticalArrangement = Arrangement.spacedBy(RadarSpacing.md)) {
        item { PageTitle("位置", "选择位置以查看该地区的过敏原指数") }
        item {
            RadarCard(highlighted = true) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconDisc(Icons.Default.MyLocation, "使用当前位置")
                    Spacer(Modifier.width(RadarSpacing.sm))
                    Column(Modifier.weight(1f)) { Text("使用当前位置", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold); Text("仅在点按后请求粗略定位", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    IconButton(onClick = onUseCurrentLocation, enabled = !state.isFindingNearby) { Icon(Icons.AutoMirrored.Filled.ArrowForward, "使用当前位置") }
                }
                if (state.isFindingNearby) LinearProgressIndicator(Modifier.fillMaxWidth())
            }
        }
        item { OutlinedTextField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Search, null) }, label = { Text("搜索城市或区县") }, singleLine = true) }
        if (permissionDenied) item { InfoBanner(Icons.Default.Info, "定位权限未开启", "仍可手动选择位置。") }
        state.nearbyMessage?.let { item { InfoBanner(Icons.Default.Info, "附近位置", it) } }
        state.nearbyCandidate?.let { candidate -> item {
            RadarCard {
                Text("附近支持位置", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("${candidate.match.location.nameCn} · 约 ${String.format(Locale.ROOT, "%.1f", candidate.match.distanceKm)} km", style = MaterialTheme.typography.bodyLarge)
                Text("这是最近的支持位置，不代表行政区边界识别。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (candidate.source == RepositoryDataSource.CACHE) Text("基于离线缓存的位置列表匹配", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                Button(onClick = onConfirmNearbyLocation, modifier = Modifier.fillMaxWidth()) { Text("使用此位置") }
            }
        } }
        if (state.source == RepositoryDataSource.CACHE) item { InfoBanner(Icons.Default.Info, "离线缓存位置", "缓存于 ${formatCachedTime(state.cachedAt).orEmpty()}；定位成功不代表花粉数据在线。") }
        state.errorMessage?.let { message -> item { ErrorCard(message, onRefresh) } }
        if (state.isLoading && state.locations.isEmpty()) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        scopes.forEach { scope ->
            val locations = visible.filter { it.scope == scope }
            if (locations.isNotEmpty()) {
                item { SectionHeader(if (scope == "DISTRICT") "北京区县" else "全部支持城市") }
                items(locations, key = { it.id }) { location -> LocationRow(location, location.id == state.selectedLocationId) { onSelect(location.id) } }
            }
        }
    }
}

@Composable private fun LocationRow(location: LocationDto, selected: Boolean, onClick: () -> Unit) {
    RadarCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconDisc(Icons.Default.LocationOn, location.nameCn)
            Spacer(Modifier.width(RadarSpacing.sm))
            Column(Modifier.weight(1f)) { Text(location.nameCn, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold); Text(if (location.scope == "DISTRICT") "区县位置" else "城市位置", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            if (selected) SourceBadge("当前选择") else Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable private fun ErrorCard(message: String, retry: () -> Unit) { RadarCard { Text("位置列表暂时不可用", style = MaterialTheme.typography.titleMedium); Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant); OutlinedButton(onClick = retry) { Text("重试") } } }
