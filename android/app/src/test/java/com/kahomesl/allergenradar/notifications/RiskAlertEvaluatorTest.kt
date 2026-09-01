package com.kahomesl.allergenradar.notifications

import com.kahomesl.allergenradar.data.ObservationDto
import com.kahomesl.allergenradar.data.ObservationTimeDto
import com.kahomesl.allergenradar.data.RepositoryDataSource
import com.kahomesl.allergenradar.data.RiskDto
import com.kahomesl.allergenradar.data.RiskSeverityDto
import com.kahomesl.allergenradar.data.SourceDto
import com.kahomesl.allergenradar.data.TaxonDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RiskAlertEvaluatorTest {
    @Test fun defaultsAreOptInAndHighThreshold() = assertEquals(RiskAlertSettings(), RiskAlertSettings())

    @Test fun highCurrentTotalAtHighThresholdNotifies() {
        assertEquals("北京 · 综合花粉", evaluator().evaluate(networkTotal(RiskSeverityDto.HIGH))?.title)
    }

    @Test fun moderateBelowHighDoesNotNotify() = assertNull(evaluator().evaluate(networkTotal(RiskSeverityDto.MODERATE)))
    @Test fun unknownNeverNotifies() = assertNull(evaluator().evaluate(networkTotal(RiskSeverityDto.UNKNOWN)))
    @Test fun cacheNeverNotifies() = assertNull(evaluator().evaluate(RiskAlertCandidate("北京", RepositoryDataSource.CACHE, total(RiskSeverityDto.HIGH, "CURRENT"))))
    @Test fun estimateNeverNotifies() = assertNull(evaluator().evaluate(networkTotal(RiskSeverityDto.HIGH, "ESTIMATE")))

    @Test fun totalCannotTriggerArtemisiaOnlySetting() {
        assertNull(evaluator(RiskAlertSettings(enabled = true, notifyTotal = false, notifyArtemisia = true)).evaluate(networkTotal(RiskSeverityDto.HIGH)))
    }

    @Test fun artemisiaForecastNamesTheTargetAndForecast() {
        val decision = evaluator().evaluate(networkArtemisia(RiskSeverityDto.HIGH))
        assertEquals("朝阳区 · 蒿属", decision?.title)
        assertEquals("预报风险：高", decision?.body)
    }

    @Test fun fingerprintDedupesSameObservationButAllowsSeverityEscalation() {
        val first = evaluator().evaluate(networkTotal(RiskSeverityDto.HIGH))!!
        assertNull(evaluator(RiskAlertSettings(enabled = true, lastTotalFingerprint = first.fingerprint)).evaluate(networkTotal(RiskSeverityDto.HIGH)))
        assertEquals(RiskSeverityDto.VERY_HIGH, evaluator(RiskAlertSettings(enabled = true, lastTotalFingerprint = first.fingerprint)).evaluate(networkTotal(RiskSeverityDto.VERY_HIGH))?.severity)
    }

    private fun evaluator(settings: RiskAlertSettings = RiskAlertSettings(enabled = true)) = RiskAlertEvaluator(settings)
    private fun networkTotal(severity: RiskSeverityDto, type: String = "CURRENT") = RiskAlertCandidate("北京", RepositoryDataSource.NETWORK, total(severity, type))
    private fun networkArtemisia(severity: RiskSeverityDto) = RiskAlertCandidate("朝阳区", RepositoryDataSource.NETWORK, artemisia(severity))
    private fun total(severity: RiskSeverityDto, type: String) = ObservationDto("total-1", "cn-city-beijing", scope = "TOTAL", measurementType = type, unit = "level", risk = RiskDto(4, "高", severity), provider = "weatherdt", source = SourceDto("WeatherDT"), confidence = 3, time = ObservationTimeDto("2026-09-01T00:00:00Z"))
    private fun artemisia(severity: RiskSeverityDto) = ObservationDto("art-1", "cn-beijing-chaoyang", TaxonDto("ARTEMISIA", "蒿属", "Artemisia"), "GENUS", "FORECAST", unit = "index", risk = RiskDto(4, "高", severity), provider = "fixture", source = SourceDto("Fixture"), confidence = 4, time = ObservationTimeDto("2026-09-01T00:00:00Z"))
}
