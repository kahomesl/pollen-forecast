package com.kahomesl.allergenradar.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Source
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.kahomesl.allergenradar.BuildConfig
import com.kahomesl.allergenradar.ui.designsystem.*

@Composable fun PremiumDataInfoScreen(onBack: () -> Unit) = InformationScaffold("数据说明", onBack) {
    item { InfoEditorialCard("综合花粉 ≠ 蒿属", "综合花粉是 TOTAL 范围数据，不能替代 Artemisia 的独立预报。") }
    item { InfoEditorialCard("当前指数 ≠ 已确认实测", "CURRENT 是数据源发布的当前期数据；其生产方法未确认是直接观测，因此平台不会将其标记为实测。") }
    item { InfoEditorialCard("ESTIMATE = 平台估算", "只有 ESTIMATE 才表示平台通过明确的估算或插值逻辑生成的数据。") }
    item { InfoEditorialCard("暂无数据 ≠ 低风险", "暂无数据表示信息不足，不能据此得出低风险或无花粉结论。") }
    item { InfoEditorialCard("风险等级", "LOW、MODERATE、HIGH、VERY_HIGH 是平台按 Provider 已确认语义标准化的展示等级；UNKNOWN 表示尚无法可靠标准化。") }
    item { SourceCard() }
    item { Disclaimer() }
}

@Composable fun PremiumPrivacyScreen(onBack: () -> Unit) = InformationScaffold("隐私说明", onBack) {
    item { InfoEditorialCard("无账户与健康资料", "应用不提供账号、登录、广告 SDK、分析跟踪或健康档案功能。") }
    item { InfoEditorialCard("按需粗略定位", "只有点按“使用当前位置”后才请求 ACCESS_COARSE_LOCATION。原始坐标仅在本机即时匹配附近支持位置，不上传、不记录、不持久化。") }
    item { InfoEditorialCard("本地保存", "仅保存确认后的 canonical selectedLocationId、Room 离线缓存和 DataStore 通知偏好。原始坐标不会写入本地数据。") }
    item { InfoEditorialCard("通知", "风险提醒默认关闭；开启后只在设备上评估选定位置的真实网络数据。缓存、估算、未知风险与空蒿属结果不会触发通知。") }
    item { Disclaimer() }
}

@Composable fun PremiumAboutScreen(onBack: () -> Unit) = InformationScaffold("关于", onBack) {
    item { RadarCard(highlighted = true) { Text("过敏原雷达", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold); Text("环境过敏原信息工具", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant); Text("版本 ${BuildConfig.VERSION_NAME}（${BuildConfig.VERSION_CODE}）", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    item { SourceCard() }
    item { InfoEditorialCard("数据边界", "综合花粉主要来自 WeatherDT。北京蒿属仅在上游提供有效独立结果时显示，不由综合花粉推算。公开或商业发布仍需完成上游数据授权与许可审查。") }
    item { Disclaimer() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun InformationScaffold(title: String, onBack: () -> Unit, content: LazyListScope.() -> Unit) {
    BackHandler(onBack = onBack)
    Scaffold(topBar = { TopAppBar(title = { Text(title, fontWeight = FontWeight.SemiBold) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") } }) }) { padding ->
        LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(horizontal = RadarSpacing.lg, vertical = RadarSpacing.xl), verticalArrangement = Arrangement.spacedBy(RadarSpacing.md), content = content)
    }
}

@Composable private fun InfoEditorialCard(title: String, body: String) { RadarCard { Row { IconDisc(Icons.Default.Info, null); Spacer(Modifier.width(RadarSpacing.sm)); Column { Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(RadarSpacing.xs)); Text(body, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) } } } }
@Composable private fun SourceCard() { RadarCard { Row { IconDisc(Icons.Default.Source, null); Spacer(Modifier.width(RadarSpacing.sm)); Column { Text("数据来源", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold); Text("WeatherDT 提供综合花粉当前期与预报数据；北京花粉监测仅在可用时提供蒿属独立分类预报。", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) } } } }
@Composable private fun Disclaimer() { InfoBanner(Icons.Default.PrivacyTip, "环境信息参考", "本应用提供环境信息参考，不提供医疗诊断或治疗建议。") }
