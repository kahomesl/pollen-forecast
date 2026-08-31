package com.kahomesl.allergenradar.data

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiContractTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun parsesObservationTimeAndOptionalObservationFields() {
        val observation = json.decodeFromString<ObservationDto>(OBSERVATION_JSON)

        assertEquals("2026-08-31T08:10:00.000Z", observation.time.retrievedAt)
        assertEquals("ARTEMISIA", observation.taxon?.code)
        assertEquals(4.0, observation.value)
        assertEquals("高", observation.risk?.label)
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
        const val OBSERVATION_JSON = """
            {
              "id": "weatherdt:cn-city-beijing:2026-08-31:4:CURRENT",
              "locationId": "cn-city-beijing",
              "taxon": { "code": "ARTEMISIA", "nameCn": "蒿属", "nameEn": "Artemisia" },
              "scope": "TOTAL",
              "measurementType": "CURRENT",
              "value": 4,
              "risk": { "level": 4, "label": "高" },
              "provider": "weatherdt",
              "source": { "name": "WeatherDT" },
              "time": {
                "retrievedAt": "2026-08-31T08:10:00.000Z",
                "createdAt": "2026-08-31T08:10:00.000Z",
                "updatedAt": "2026-08-31T08:10:00.000Z"
              }
            }
        """
    }
}
