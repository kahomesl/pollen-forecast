package com.kahomesl.allergenradar.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.kahomesl.allergenradar.data.local.AllergenRadarDatabase
import com.kahomesl.allergenradar.data.local.CacheKey
import com.kahomesl.allergenradar.data.local.RoomAllergenCache
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class OfflineFirstAllergenRepositoryTest {
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
    fun onlineEmptyArtemisiaClearsPreviousArtemisiaCache() = runTest {
        cache.replaceCurrent(CacheKey.taxon("cn-city-beijing", "ARTEMISIA"), artemisiaResponse())
        val repository = OfflineFirstAllergenRepository(FakeNetwork(taxonResponse = emptyResponse()), cache)

        val result = repository.getLocationTaxon("cn-city-beijing", "ARTEMISIA")

        assertEquals(RepositoryDataSource.NETWORK, result.source)
        assertEquals(emptyList<ObservationDto>(), result.data.observations)
        assertNull(cache.readCurrent(CacheKey.taxon("cn-city-beijing", "ARTEMISIA"))!!.data.observations.singleOrNull())
    }

    @Test
    fun ioFailureReadsMatchingCachedTaxonResult() = runTest {
        cache.replaceCurrent(CacheKey.taxon("cn-city-beijing", "ARTEMISIA"), artemisiaResponse())
        val repository = OfflineFirstAllergenRepository(FakeNetwork(taxonFailure = IOException("offline")), cache)

        val result = repository.getLocationTaxon("cn-city-beijing", "ARTEMISIA")

        assertEquals(RepositoryDataSource.CACHE, result.source)
        assertEquals("ARTEMISIA", result.data.observations.single().taxon?.code)
        assertEquals(1_725_139_800_000L, result.cachedAt)
    }

    @Test
    fun contract4xxDoesNotReadCachedTaxonResult() = runTest {
        cache.replaceCurrent(CacheKey.taxon("cn-city-beijing", "ARTEMISIA"), artemisiaResponse())
        val repository = OfflineFirstAllergenRepository(
            FakeNetwork(taxonFailure = ApiException(404, ApiErrorDto("TAXON_NOT_FOUND", "Unknown taxon"))),
            cache,
        )

        try {
            repository.getLocationTaxon("cn-city-beijing", "ARTEMISIA")
            fail("Expected the 4xx contract error to propagate")
        } catch (error: ApiException) {
            assertEquals(404, error.statusCode)
        }
    }

    @Test
    fun queryKeysKeepTotalAndTaxonResultsSeparate() = runTest {
        val repository = OfflineFirstAllergenRepository(FakeNetwork(totalResponse = totalResponse(), taxonResponse = artemisiaResponse()), cache)

        repository.getLocationAllergens("cn-city-beijing")
        repository.getLocationTaxon("cn-city-beijing", "ARTEMISIA")

        assertEquals("TOTAL", cache.readCurrent(CacheKey.total("cn-city-beijing"))!!.data.observations.single().scope)
        assertEquals("ARTEMISIA", cache.readCurrent(CacheKey.taxon("cn-city-beijing", "ARTEMISIA"))!!.data.observations.single().taxon?.code)
    }

    @Test
    fun locationsFallBackToRoomOnlyForNetworkFailure() = runTest {
        val online = OfflineFirstAllergenRepository(FakeNetwork(locations = listOf(location("cn-city-beijing", "北京"))), cache)
        online.getLocations()
        val offline = OfflineFirstAllergenRepository(FakeNetwork(locationsFailure = IOException("offline")), cache)

        val result = offline.getLocations()

        assertEquals(RepositoryDataSource.CACHE, result.source)
        assertEquals("北京", result.data.single().nameCn)
    }

    private class FakeNetwork(
        private val totalResponse: LocationAllergenResponseDto = emptyResponse(),
        private val taxonResponse: LocationAllergenResponseDto = emptyResponse(),
        private val taxonFailure: Throwable? = null,
        private val locations: List<LocationDto> = emptyList(),
        private val locationsFailure: Throwable? = null,
    ) : AllergenRepository {
        override suspend fun getAllergens() = emptyList<AllergenDto>()
        override suspend fun getProviders() = emptyList<ProviderDto>()
        override suspend fun getLocations(): List<LocationDto> {
            locationsFailure?.let { throw it }
            return locations
        }
        override suspend fun getLocationAllergens(locationId: String) = totalResponse
        override suspend fun getLocationTaxon(locationId: String, taxon: String): LocationAllergenResponseDto {
            taxonFailure?.let { throw it }
            return taxonResponse
        }
        override suspend fun getHistory(locationId: String, taxon: String?, measurementType: String?, limit: Int) =
            HistoryResponseDto(location(locationId, "北京"))
        override suspend fun getSyncStatus() = SyncStatusResponseDto()
    }

    private companion object {
        fun location(id: String, name: String) = LocationDto(id, name, "CITY")

        fun emptyResponse() = LocationAllergenResponseDto(location("cn-city-beijing", "北京"))

        fun totalResponse() = LocationAllergenResponseDto(
            location("cn-city-beijing", "北京"),
            observations = listOf(observation("total", null, "TOTAL", "CURRENT")),
        )

        fun artemisiaResponse() = LocationAllergenResponseDto(
            location("cn-city-beijing", "北京"),
            observations = listOf(observation("artemisia", TaxonDto("ARTEMISIA", "蒿属", "Artemisia"), "GENUS", "FORECAST")),
        )

        fun observation(id: String, taxon: TaxonDto?, scope: String, measurementType: String) = ObservationDto(
            id = id,
            locationId = "cn-city-beijing",
            taxon = taxon,
            scope = scope,
            measurementType = measurementType,
            value = 2.0,
            unit = "index",
            risk = RiskDto(2, "中"),
            provider = "fixture",
            source = SourceDto("Fixture"),
            confidence = 4,
            time = ObservationTimeDto("2026-08-31T08:10:00.000Z"),
        )
    }
}
