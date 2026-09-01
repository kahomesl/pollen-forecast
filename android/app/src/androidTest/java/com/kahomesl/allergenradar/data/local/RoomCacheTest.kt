package com.kahomesl.allergenradar.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.kahomesl.allergenradar.data.LocationAllergenResponseDto
import com.kahomesl.allergenradar.data.LocationDto
import com.kahomesl.allergenradar.data.ObservationDto
import com.kahomesl.allergenradar.data.ObservationTimeDto
import com.kahomesl.allergenradar.data.RiskDto
import com.kahomesl.allergenradar.data.SourceDto
import com.kahomesl.allergenradar.data.TaxonDto
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class RoomCacheTest {
    private lateinit var database: AllergenRadarDatabase
    private lateinit var cache: RoomAllergenCache

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AllergenRadarDatabase::class.java,
        ).allowMainThreadQueries().build()
        cache = RoomAllergenCache(database, clock = { 1_725_139_800_000L })
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun totalCurrentRoundTripKeepsCurrentObservation() = runTest {
        val response = LocationAllergenResponseDto(
            location = LocationDto("cn-city-beijing", "北京", "CITY"),
            observations = listOf(observation(id = "total-current", scope = "TOTAL", type = "CURRENT")),
        )

        cache.replaceCurrent(CacheKey.total("cn-city-beijing"), response)

        val cached = cache.readCurrent(CacheKey.total("cn-city-beijing"))!!
        assertEquals("北京", cached.data.location.nameCn)
        assertEquals("CURRENT", cached.data.observations.single().measurementType)
        assertEquals(1_725_139_800_000L, cached.cachedAt)
    }

    @Test
    fun artemisiaForecastRoundTripRetainsNullableValidityTimes() = runTest {
        val response = LocationAllergenResponseDto(
            location = LocationDto("cn-beijing-chaoyang", "朝阳区", "DISTRICT"),
            observations = listOf(
                observation(
                    id = "artemisia-forecast",
                    scope = "GENUS",
                    type = "FORECAST",
                    taxon = TaxonDto("ARTEMISIA", "蒿属", "Artemisia"),
                    validFrom = null,
                    validTo = null,
                ),
            ),
        )

        cache.replaceCurrent(CacheKey.taxon("cn-beijing-chaoyang", "ARTEMISIA"), response)

        val cached = cache.readCurrent(CacheKey.taxon("cn-beijing-chaoyang", "ARTEMISIA"))!!
        val observation = cached.data.observations.single()
        assertEquals("ARTEMISIA", observation.taxon?.code)
        assertEquals("FORECAST", observation.measurementType)
        assertNull(observation.time.validFrom)
        assertNull(observation.time.validTo)
    }

    private fun observation(
        id: String,
        scope: String,
        type: String,
        taxon: TaxonDto? = null,
        validFrom: String? = "2026-09-01T00:00:00.000Z",
        validTo: String? = "2026-09-02T00:00:00.000Z",
    ) = ObservationDto(
        id = id,
        locationId = if (taxon == null) "cn-city-beijing" else "cn-beijing-chaoyang",
        taxon = taxon,
        scope = scope,
        measurementType = type,
        value = 4.0,
        unit = "index",
        risk = RiskDto(2, "中"),
        provider = "fixture",
        source = SourceDto("Fixture", "https://example.invalid"),
        confidence = 4,
        time = ObservationTimeDto(
            retrievedAt = "2026-08-31T08:10:00.000Z",
            validFrom = validFrom,
            validTo = validTo,
        ),
    )
}
