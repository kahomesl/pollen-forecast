package com.kahomesl.allergenradar.data

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class RiskSeverityDtoTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun missingSeverityFromAnOlderServerDefaultsToUnknown() {
        val risk = json.decodeFromString<RiskDto>("""{"level":4,"label":"高"}""")

        assertEquals(RiskSeverityDto.UNKNOWN, risk.severity)
    }

    @Test
    fun unknownSeverityWireValueDoesNotBreakObservationDecoding() {
        val risk = json.decodeFromString<RiskDto>("""{"severity":"FUTURE_PROVIDER_VALUE"}""")

        assertEquals(RiskSeverityDto.UNKNOWN, risk.severity)
    }

    @Test
    fun knownSeverityWireValueUsesTheTypedEnum() {
        val risk = json.decodeFromString<RiskDto>("""{"severity":"HIGH"}""")

        assertEquals(RiskSeverityDto.HIGH, risk.severity)
    }
}
