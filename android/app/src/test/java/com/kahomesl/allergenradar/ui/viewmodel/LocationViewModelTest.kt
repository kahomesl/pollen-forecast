package com.kahomesl.allergenradar.ui.viewmodel

import com.kahomesl.allergenradar.data.AllergenDataRepository
import com.kahomesl.allergenradar.data.AllergenDto
import com.kahomesl.allergenradar.data.HistoryResponseDto
import com.kahomesl.allergenradar.data.LocationAllergenResponseDto
import com.kahomesl.allergenradar.data.LocationDto
import com.kahomesl.allergenradar.data.LocationPreference
import com.kahomesl.allergenradar.data.ProviderDto
import com.kahomesl.allergenradar.data.RepositoryDataSource
import com.kahomesl.allergenradar.data.RepositoryResult
import com.kahomesl.allergenradar.data.SyncStatusResponseDto
import com.kahomesl.allergenradar.location.DeviceCoordinates
import com.kahomesl.allergenradar.location.OneShotLocationClient
import com.kahomesl.allergenradar.location.OneShotLocationResult
import com.kahomesl.allergenradar.location.SupportedLocationMatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LocationViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun nearbyCandidateUsesCachedLocationsWithoutChangingTheSelectedLocation() = runTest {
        val preference = RecordingPreference()
        val viewModel = LocationViewModel(
            FakeLocationRepository(RepositoryDataSource.CACHE),
            preference,
            FakeOneShotLocationClient(DeviceCoordinates(39.9215, 116.4864)),
            SupportedLocationMatcher(),
        )

        viewModel.findNearbySupportedLocation()
        runCurrent()

        val candidate = requireNotNull(viewModel.state.value.nearbyCandidate)
        assertEquals("cn-beijing-chaoyang", candidate.match.location.id)
        assertEquals(RepositoryDataSource.CACHE, candidate.source)
        assertEquals("cn-city-beijing", preference.selectedLocationId.value)
    }

    @Test
    fun confirmingNearbyCandidatePersistsOnlyItsCanonicalLocationId() = runTest {
        val preference = RecordingPreference()
        val viewModel = LocationViewModel(
            FakeLocationRepository(RepositoryDataSource.NETWORK),
            preference,
            FakeOneShotLocationClient(DeviceCoordinates(34.3416, 108.9398)),
            SupportedLocationMatcher(),
        )

        viewModel.findNearbySupportedLocation()
        runCurrent()
        viewModel.confirmNearbyLocation()
        runCurrent()

        assertEquals("cn-city-xian", preference.selectedLocationId.value)
        assertTrue(viewModel.state.value.nearbyCandidate == null)
    }

    @Test
    fun unavailableOneShotLocationLeavesManualPickerAvailable() = runTest {
        val viewModel = LocationViewModel(
            FakeLocationRepository(RepositoryDataSource.NETWORK),
            RecordingPreference(),
            FakeOneShotLocationClient(null),
            SupportedLocationMatcher(),
        )

        viewModel.findNearbySupportedLocation()
        runCurrent()

        assertEquals("暂时无法获取当前位置，可继续手动选择位置。", viewModel.state.value.nearbyMessage)
        assertTrue(viewModel.state.value.locations.isNotEmpty())
    }

    private class RecordingPreference : LocationPreference {
        override val selectedLocationId = MutableStateFlow("cn-city-beijing")
        override suspend fun setSelectedLocationId(locationId: String) {
            selectedLocationId.value = locationId
        }
    }

    private class FakeOneShotLocationClient(private val coordinates: DeviceCoordinates?) : OneShotLocationClient {
        override suspend fun getCurrentLocation(): OneShotLocationResult = coordinates?.let(OneShotLocationResult::Available)
            ?: OneShotLocationResult.Unavailable
    }

    private class FakeLocationRepository(private val source: RepositoryDataSource) : AllergenDataRepository {
        override suspend fun getAllergens() = emptyList<AllergenDto>()
        override suspend fun getProviders() = emptyList<ProviderDto>()
        override suspend fun getLocations() = RepositoryResult(locations, source, cachedAt = 1_000L)
        override suspend fun getLocationAllergens(locationId: String) = RepositoryResult(LocationAllergenResponseDto(locations.first()), source)
        override suspend fun getLocationTaxon(locationId: String, taxon: String) = RepositoryResult(LocationAllergenResponseDto(locations.first()), source)
        override suspend fun getHistory(locationId: String, taxon: String?, measurementType: String?, limit: Int) = RepositoryResult(HistoryResponseDto(locations.first()), source)
        override suspend fun getSyncStatus() = SyncStatusResponseDto()
    }

    private companion object {
        val locations = listOf(
            LocationDto("cn-city-beijing", "北京", "CITY", 39.9042, 116.4074),
            LocationDto("cn-beijing-chaoyang", "朝阳区", "DISTRICT", 39.9215, 116.4864),
            LocationDto("cn-city-xian", "西安", "CITY", 34.3416, 108.9398),
        )
    }
}
