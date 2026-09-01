package com.kahomesl.allergenradar.notifications

import com.kahomesl.allergenradar.data.ObservationDto
import com.kahomesl.allergenradar.data.RepositoryDataSource
import com.kahomesl.allergenradar.data.RiskSeverityDto

enum class RiskAlertTarget { TOTAL, ARTEMISIA }

data class RiskAlertSettings(
    val enabled: Boolean = false,
    val notifyTotal: Boolean = true,
    val notifyArtemisia: Boolean = true,
    val minimumSeverity: RiskSeverityDto = RiskSeverityDto.HIGH,
    val lastTotalFingerprint: String? = null,
    val lastArtemisiaFingerprint: String? = null,
)

data class RiskAlertCandidate(val locationName: String, val source: RepositoryDataSource, val observation: ObservationDto)
data class RiskAlertDecision(val target: RiskAlertTarget, val title: String, val body: String, val fingerprint: String, val severity: RiskSeverityDto)

class RiskAlertEvaluator(private val settings: RiskAlertSettings) {
    fun evaluate(candidate: RiskAlertCandidate): RiskAlertDecision? {
        val observation = candidate.observation
        val target = targetFor(observation) ?: return null
        if (!settings.enabled || candidate.source != RepositoryDataSource.NETWORK || observation.measurementType !in setOf("CURRENT", "FORECAST")) return null
        if ((target == RiskAlertTarget.TOTAL && !settings.notifyTotal) || (target == RiskAlertTarget.ARTEMISIA && !settings.notifyArtemisia)) return null
        if (observation.risk.severity == RiskSeverityDto.UNKNOWN || rank(observation.risk.severity) < rank(settings.minimumSeverity)) return null
        val fingerprint = "${observation.locationId}:$target:${observation.measurementType}:${observation.id}:${observation.risk.severity}"
        if (fingerprint == if (target == RiskAlertTarget.TOTAL) settings.lastTotalFingerprint else settings.lastArtemisiaFingerprint) return null
        val targetName = if (target == RiskAlertTarget.TOTAL) "综合花粉" else "蒿属"
        val typeLabel = if (observation.measurementType == "CURRENT") "当前指数" else "预报风险"
        val label = observation.risk.label?.takeIf { it.isNotBlank() } ?: severityLabel(observation.risk.severity)
        return RiskAlertDecision(target, "${candidate.locationName} · $targetName", "$typeLabel：$label", fingerprint, observation.risk.severity)
    }
}

private fun targetFor(observation: ObservationDto): RiskAlertTarget? = when {
    observation.scope == "TOTAL" && observation.taxon == null -> RiskAlertTarget.TOTAL
    observation.scope == "GENUS" && observation.taxon?.code == "ARTEMISIA" -> RiskAlertTarget.ARTEMISIA
    else -> null
}

private fun rank(severity: RiskSeverityDto): Int = when (severity) {
    RiskSeverityDto.UNKNOWN -> 0; RiskSeverityDto.LOW -> 1; RiskSeverityDto.MODERATE -> 2; RiskSeverityDto.HIGH -> 3; RiskSeverityDto.VERY_HIGH -> 4
}
private fun severityLabel(severity: RiskSeverityDto): String = when (severity) {
    RiskSeverityDto.LOW -> "低"; RiskSeverityDto.MODERATE -> "中"; RiskSeverityDto.HIGH -> "高"; RiskSeverityDto.VERY_HIGH -> "很高"; RiskSeverityDto.UNKNOWN -> "未知"
}
