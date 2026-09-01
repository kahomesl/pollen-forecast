package com.kahomesl.allergenradar.ui.screens

import com.kahomesl.allergenradar.data.ObservationDto
import com.kahomesl.allergenradar.data.ObservationTimeDto
import com.kahomesl.allergenradar.data.RiskDto
import com.kahomesl.allergenradar.data.RiskSeverityDto
import com.kahomesl.allergenradar.data.SourceDto
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeProductCopyTest {
    @Test fun currentIsNotCalledObservation() = assertEquals("当前指数", displayMeasurementLabel("CURRENT"))
    @Test fun estimateIsExplicitlyPlatformEstimate() = assertEquals("平台估算", displayMeasurementLabel("ESTIMATE"))
    @Test fun observationIsOnlyMeasurementTypeCalledObservation() = assertEquals("实测", displayMeasurementLabel("OBSERVATION"))
    @Test fun unknownRiskDoesNotInventALowLabel() = assertEquals("—", riskHeadline(observation(RiskSeverityDto.UNKNOWN)))

    private fun observation(severity: RiskSeverityDto) = ObservationDto(
        id = "fixture", locationId = "fixture", scope = "GENUS", measurementType = "FORECAST", unit = "index",
        risk = RiskDto(level = 2, severity = severity), provider = "fixture", source = SourceDto("Fixture"), confidence = 4,
        time = ObservationTimeDto("2026-09-01T00:00:00Z"),
    )
}
