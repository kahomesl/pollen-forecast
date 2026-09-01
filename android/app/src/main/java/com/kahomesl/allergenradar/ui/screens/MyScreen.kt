package com.kahomesl.allergenradar.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.kahomesl.allergenradar.BuildConfig
import com.kahomesl.allergenradar.data.RiskSeverityDto
import com.kahomesl.allergenradar.notifications.RiskAlertSettings
import com.kahomesl.allergenradar.ui.designsystem.*
import com.kahomesl.allergenradar.ui.viewmodel.MyUiState
import com.kahomesl.allergenradar.util.formatLocalTime

@Composable
fun PremiumMyScreen(
    state: MyUiState,
    onRefresh: () -> Unit,
    onOpenLocations: () -> Unit,
    onOpenDataInfo: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenAbout: () -> Unit,
    alertSettings: RiskAlertSettings,
    permissionDenied: Boolean,
    onOpenNotificationSettings: () -> Unit,
    onSettingsChange: (RiskAlertSettings) -> Unit,
    onAlertsEnabled: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = RadarSpacing.lg, vertical = RadarSpacing.xl), verticalArrangement = Arrangement.spacedBy(RadarSpacing.md)) {
        item { PageTitle("我的", "个人设置与应用信息", action = { IconButton(onClick = onRefresh) { Icon(Icons.Default.Refresh, "刷新状态") } }) }
        item { RadarCard { SettingRow(Icons.Default.LocationOn, "当前位置", state.locationName, onOpenLocations) { Icon(Icons.AutoMirrored.Filled.ArrowForward, "切换位置") } } }
        item {
            RadarCard(highlighted = true) {
                Row(verticalAlignment = Alignment.CenterVertically) { IconDisc(Icons.Default.Notifications, "风险提醒", MaterialTheme.colorScheme.error); Spacer(Modifier.width(RadarSpacing.sm)); Text("风险提醒", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold) }
                SettingRow(Icons.Default.Notifications, "开启风险提醒", "仅依据已标准化的真实网络数据") { Switch(checked = alertSettings.enabled, onCheckedChange = onAlertsEnabled) }
                if (permissionDenied) {
                    InfoBanner(Icons.Default.Info, "系统通知权限未开启", "请在系统设置中允许通知后再开启提醒。", Modifier.clickable(onClick = onOpenNotificationSettings))
                }
                if (alertSettings.enabled) {
                    HorizontalDivider()
                    SettingRow(Icons.Default.Notifications, "综合花粉提醒", "当前可基于标准化风险等级触发") { Switch(alertSettings.notifyTotal, { onSettingsChange(alertSettings.copy(notifyTotal = it)) }) }
                    SettingRow(Icons.Default.Notifications, "蒿属提醒", "仅在存在支持且风险已标准化的蒿属数据时触发") { Switch(alertSettings.notifyArtemisia, { onSettingsChange(alertSettings.copy(notifyArtemisia = it)) }) }
                    Text("最低提醒等级", style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(RadarSpacing.xs)) { listOf(RiskSeverityDto.MODERATE, RiskSeverityDto.HIGH, RiskSeverityDto.VERY_HIGH).forEach { severity -> FilterChip(alertSettings.minimumSeverity == severity, { onSettingsChange(alertSettings.copy(minimumSeverity = severity)) }, label = { Text(riskThresholdLabel(severity)) }) } }
                }
            }
        }
        item {
            RadarCard {
                SectionHeader("数据与同步")
                SettingRow(Icons.Default.Sync, "API 状态", when (state.apiAvailable) { true -> "连接正常"; false -> "暂时不可用"; null -> "正在检查" })
                SettingRow(Icons.Default.Sync, "最近同步", state.latestRun?.finishedAt?.let { formatLocalTime(it) } ?: "暂无同步记录")
                HorizontalDivider()
                SettingRow(Icons.Default.Info, "数据说明", "数据类型、风险等级与来源", onOpenDataInfo) { Icon(Icons.AutoMirrored.Filled.ArrowForward, "数据说明") }
            }
        }
        item {
            RadarCard {
                SectionHeader("隐私与关于")
                SettingRow(Icons.Default.PrivacyTip, "隐私说明", "定位、缓存与通知偏好", onOpenPrivacy) { Icon(Icons.AutoMirrored.Filled.ArrowForward, "隐私说明") }
                SettingRow(Icons.Default.Settings, "关于", "应用版本与数据来源", onOpenAbout) { Icon(Icons.AutoMirrored.Filled.ArrowForward, "关于") }
            }
        }
        item { Text("版本 ${BuildConfig.VERSION_NAME}（${BuildConfig.VERSION_CODE}）", modifier = Modifier.fillMaxWidth(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

private fun riskThresholdLabel(severity: RiskSeverityDto) = when (severity) { RiskSeverityDto.MODERATE -> "中等及以上"; RiskSeverityDto.HIGH -> "高风险及以上"; RiskSeverityDto.VERY_HIGH -> "很高风险"; else -> severity.name }
