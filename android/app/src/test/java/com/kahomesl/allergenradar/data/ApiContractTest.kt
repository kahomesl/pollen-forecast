package com.kahomesl.allergenradar.data

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiContractTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun parsesWeatherDtTotalCurrentWithoutTaxon() {
        val observation = json.decodeFromString<ObservationDto>(TOTAL_CURRENT_JSON)

        assertEquals("2026-08-31T08:10:00.000Z", observation.time.retrievedAt)
        assertNull(observation.taxon)
        assertEquals("TOTAL", observation.scope)
        assertEquals("CURRENT", observation.measurementType)
        assertEquals("weatherdt", observation.provider)
        assertEquals(4.0, observation.value)
        assertEquals("level", observation.unit)
        assertEquals(3, observation.confidence)
        assertEquals("高", observation.risk.label)
    }

    @Test
    fun parsesBeijingArtemisiaForecastFixture() {
        val observation = json.decodeFromString<ObservationDto>(ARTEMISIA_FORECAST_JSON)

        assertEquals("beijing-pollen", observation.provider)
        assertEquals("GENUS", observation.scope)
        assertEquals("FORECAST", observation.measurementType)
        assertEquals("ARTEMISIA", observation.taxon?.code)
        assertEquals("index", observation.unit)
        assertEquals(4, observation.confidence)
        assertEquals(2, observation.risk.level)
    }

    @Test
    fun providerCapabilitiesAreExpressedAsTypedValues() {
        val provider = ProviderDto(
            id = "beijing-pollen",
            name = "北京花粉监测",
            capabilities = listOf("GENUS_FORECAST", "HISTORY"),
            supportedTaxa = listOf("ARTEMISIA"),
        )

        assertEquals(
            setOf(ProviderCapability.GENUS_FORECAST, ProviderCapability.HISTORY),
            provider.capabilitySet(),
        )
        assertTrue(ProviderCapability.entries.none { it.name == "NOT_A_CAPABILITY" })
    }

    private companion object {
        const val TOTAL_CURRENT_JSON = """
            {
              "id": "weatherdt:cn-city-beijing:2026-08-31:4:CURRENT",
              "locationId": "cn-city-beijing",
              "scope": "TOTAL",
              "measurementType": "CURRENT",
              "value": 4,
              "unit": "level",
              "risk": { "level": 4, "label": "高" },
              "provider": "weatherdt",
              "source": { "name": "WeatherDT" },
              "confidence": 3,
              "time": {
                "retrievedAt": "2026-08-31T08:10:00.000Z",
                "createdAt": "2026-08-31T08:10:00.000Z",
                "updatedAt": "2026-08-31T08:10:00.000Z"
              }
            }
        """

        const val ARTEMISIA_FORECAST_JSON = """
            {
              "id": "beijing-pollen:cn-beijing-chaoyang:JKHS:202608300900",
              "locationId": "cn-beijing-chaoyang",
              "taxon": { "code": "ARTEMISIA", "nameCn": "蒿属", "nameEn": "Artemisia" },
              "scope": "GENUS",
              "measurementType": "FORECAST",
              "minValue": 12,
              "maxValue": 33,
              "unit": "index",
              "risk": { "level": 2 },
              "provider": "beijing-pollen",
              "source": { "name": "北京花粉监测" },
              "confidence": 4,
              "time": {
                "retrievedAt": "2026-08-30T01:30:00.000Z",
                "createdAt": "2026-08-30T01:30:00.000Z",
                "updatedAt": "2026-08-30T01:30:00.000Z"
              }
            }
        """
    }
}
