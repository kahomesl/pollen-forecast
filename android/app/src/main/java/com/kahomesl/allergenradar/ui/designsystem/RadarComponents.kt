package com.kahomesl.allergenradar.ui.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kahomesl.allergenradar.data.RiskSeverityDto

@Composable
fun RadarCard(modifier: Modifier = Modifier, highlighted: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (highlighted) RadarShapes.hero else RadarShapes.card),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (highlighted) 2.dp else 1.dp),
    ) { Column(Modifier.padding(RadarSpacing.lg), verticalArrangement = Arrangement.spacedBy(RadarSpacing.sm), content = content) }
}

@Composable
fun PageTitle(title: String, subtitle: String? = null, action: @Composable (() -> Unit)? = null) {
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(RadarSpacing.xs)) {
            Text(text = title, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            subtitle?.let { Text(text = it, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        action?.invoke()
    }
}

@Composable
fun SectionHeader(title: String, supporting: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(RadarSpacing.xxs)) {
        Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        supporting?.let { Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
fun IconDisc(icon: ImageVector, description: String?, tint: Color = MaterialTheme.colorScheme.primary) {
    Surface(color = tint.copy(alpha = .12f), shape = RoundedCornerShape(50), modifier = Modifier.size(48.dp)) {
        Icon(imageVector = icon, contentDescription = description, modifier = Modifier.padding(12.dp), tint = tint)
    }
}

@Composable fun SourceBadge(label: String) = Badge(label, MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
@Composable fun DataTypeBadge(label: String) = Badge(label, MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
@Composable private fun Badge(label: String, color: Color, textColor: Color) {
    Surface(color = color, shape = RoundedCornerShape(50)) {
        Text(text = label, modifier = Modifier.padding(horizontal = RadarSpacing.sm, vertical = RadarSpacing.xxs), style = MaterialTheme.typography.labelMedium, color = textColor)
    }
}

@Composable
fun RiskBadge(severity: RiskSeverityDto, label: String) {
    val color = when (severity) { RiskSeverityDto.LOW -> RadarRiskColors.low; RiskSeverityDto.MODERATE -> RadarRiskColors.moderate; RiskSeverityDto.HIGH -> RadarRiskColors.high; RiskSeverityDto.VERY_HIGH -> RadarRiskColors.veryHigh; RiskSeverityDto.UNKNOWN -> RadarRiskColors.unknown }
    Surface(modifier = Modifier.semantics { contentDescription = "风险等级：$label" }, color = color.copy(alpha = .14f), shape = RoundedCornerShape(50)) {
        Text(text = label, modifier = Modifier.padding(horizontal = RadarSpacing.sm, vertical = RadarSpacing.xs), style = MaterialTheme.typography.labelLarge, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun RiskScale(severity: RiskSeverityDto) {
    if (severity == RiskSeverityDto.UNKNOWN) return
    val labels = listOf("低", "中等", "高", "很高")
    val selected = when (severity) { RiskSeverityDto.LOW -> 0; RiskSeverityDto.MODERATE -> 1; RiskSeverityDto.HIGH -> 2; RiskSeverityDto.VERY_HIGH -> 3; RiskSeverityDto.UNKNOWN -> -1 }
    val colors = listOf(RadarRiskColors.low, RadarRiskColors.moderate, RadarRiskColors.high, RadarRiskColors.veryHigh)
    Column(modifier = Modifier.semantics { contentDescription = "标准化风险刻度：${labels[selected]}" }, verticalArrangement = Arrangement.spacedBy(RadarSpacing.xs)) {
        Row(horizontalArrangement = Arrangement.spacedBy(RadarSpacing.xxs), modifier = Modifier.fillMaxWidth()) { colors.forEachIndexed { index, color -> Spacer(Modifier.weight(1f).height(6.dp).background(if (index <= selected) color else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50))) } }
        Row(Modifier.fillMaxWidth()) { labels.forEach { Text(text = it, modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    }
}

@Composable
fun InfoBanner(icon: ImageVector, title: String, body: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .56f), shape = RoundedCornerShape(RadarShapes.card)) {
        Row(Modifier.padding(RadarSpacing.md), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.width(RadarSpacing.sm))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(RadarSpacing.xxs)) {
                Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(text = body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun SettingRow(icon: ImageVector, title: String, supporting: String? = null, trailing: @Composable RowScope.() -> Unit = {}) {
    Row(Modifier.fillMaxWidth().padding(vertical = RadarSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.width(RadarSpacing.sm))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(RadarSpacing.xxs)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            supporting?.let { Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        trailing()
    }
}
