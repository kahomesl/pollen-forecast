package com.kahomesl.allergenradar.domain

import com.kahomesl.allergenradar.data.ObservationDto
import com.kahomesl.allergenradar.data.ObservationTimeDto
import com.kahomesl.allergenradar.data.RiskDto
import com.kahomesl.allergenradar.data.SourceDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ObservationSemanticsTest {
    @Test
    fun currentIsPreferredOverForecastAndIsNotObservation() {
        val forecast = observation("FORECAST", value = 2.0)
        val current = observation("CURRENT", value = 4.0)

        assertEquals(current, selectPrimaryTotal(listOf(forecast, current)))
        assertEquals("CURRENT", selectPrimaryTotal(listOf(forecast, current))?.measurementType)
    }

    @Test
    fun artemisiaEmptyDoesNotFallBackToTotal() {
        assertNull(selectArtemisia(listOf(observation("CURRENT", value = 4.0))))
    }

    private fun observation(type: String, value: Double) = ObservationDto(
        id = type,
        locationId = "cn-city-beijing",
        scope = "TOTAL",
        measurementType = type,
        value = value,
        risk = RiskDto(level = value.toInt()),
        provider = "weatherdt",
        source = SourceDto("WeatherDT"),
        time = ObservationTimeDto("2026-08-31T08:10:00.000Z"),
    )
}
