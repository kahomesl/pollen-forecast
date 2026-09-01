package com.kahomesl.allergenradar.ui.viewmodel

import com.kahomesl.allergenradar.data.AllergenDataRepository
import com.kahomesl.allergenradar.data.AllergenDto
import com.kahomesl.allergenradar.data.HistoryResponseDto
import com.kahomesl.allergenradar.data.LocationAllergenResponseDto
import com.kahomesl.allergenradar.data.LocationDto
import com.kahomesl.allergenradar.data.LocationPreference
import com.kahomesl.allergenradar.data.ObservationDto
import com.kahomesl.allergenradar.data.ObservationTimeDto
import com.kahomesl.allergenradar.data.ProviderDto
import com.kahomesl.allergenradar.data.RepositoryDataSource
import com.kahomesl.allergenradar.data.RepositoryResult
import com.kahomesl.allergenradar.data.RiskDto
import com.kahomesl.allergenradar.data.SourceDto
import com.kahomesl.allergenradar.data.SyncStatusResponseDto
import com.kahomesl.allergenradar.data.TaxonDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeOfflineCacheViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun onlineEmptyArtemisiaRemainsNetworkEmptyInsteadOfCache() = runTest {
        val viewModel = HomeViewModel(FakeRepository(taxon = network(emptyResponse())), FakePreference())

        assertNull(viewModel.state.value.artemisia)
        assertEquals(RepositoryDataSource.NETWORK, viewModel.state.value.artemisiaSource)
        assertFalse(viewModel.state.value.artemisiaUsesCache)
    }

    @Test
    fun cachedArtemisiaHasExplicitCacheMetadata() = runTest {
        val viewModel = HomeViewModel(FakeRepository(taxon = cache(artemisiaResponse())), FakePreference())

        assertEquals("ARTEMISIA", viewModel.state.value.artemisia?.taxon?.code)
        assertEquals(RepositoryDataSource.CACHE, viewModel.state.value.artemisiaSource)
        assertEquals(1_725_139_800_000L, viewModel.state.value.artemisiaCachedAt)
    }

    private class FakePreference : LocationPreference {
        override val selectedLocationId: Flow<String> = MutableStateFlow("cn-city-beijing")
        override suspend fun setSelectedLocationId(locationId: String) = Unit
    }

    private class FakeRepository(
        private val total: RepositoryResult<LocationAllergenResponseDto> = network(totalResponse()),
        private val taxon: RepositoryResult<LocationAllergenResponseDto> = network(emptyResponse()),
    ) : AllergenDataRepository {
        override suspend fun getAllergens() = emptyList<AllergenDto>()
        override suspend fun getProviders() = emptyList<ProviderDto>()
        override suspend fun getLocations() = network(emptyList<LocationDto>())
        override suspend fun getLocationAllergens(locationId: String) = total
        override suspend fun getLocationTaxon(locationId: String, taxon: String) = this.taxon
        override suspend fun getHistory(locationId: String, taxon: String?, measurementType: String?, limit: Int) =
            network(HistoryResponseDto(location()))
        override suspend fun getSyncStatus() = SyncStatusResponseDto()
    }

    private companion object {
        fun <T> network(data: T) = RepositoryResult(data, RepositoryDataSource.NETWORK)
        fun <T> cache(data: T) = RepositoryResult(data, RepositoryDataSource.CACHE, 1_725_139_800_000L)
        fun location() = LocationDto("cn-city-beijing", "北京", "CITY")
        fun emptyResponse() = LocationAllergenResponseDto(location())
        fun totalResponse() = LocationAllergenResponseDto(location(), listOf(observation("total", null, "TOTAL", "CURRENT")))
        fun artemisiaResponse() = LocationAllergenResponseDto(location(), listOf(observation("artemisia", TaxonDto("ARTEMISIA", "蒿属", "Artemisia"), "GENUS", "FORECAST")))
        fun observation(id: String, taxon: TaxonDto?, scope: String, type: String) = ObservationDto(
            id, "cn-city-beijing", taxon, scope, type, value = 2.0, unit = "index", risk = RiskDto(2, "中"),
            provider = "fixture", source = SourceDto("Fixture"), confidence = 4,
            time = ObservationTimeDto("2026-08-31T08:10:00.000Z"),
        )
    }
}
