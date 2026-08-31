package com.kahomesl.allergenradar.ui.viewmodel

import com.kahomesl.allergenradar.data.AllergenDto
import com.kahomesl.allergenradar.data.AllergenRepository
import com.kahomesl.allergenradar.data.LocationDto
import com.kahomesl.allergenradar.data.LocationAllergenResponseDto
import com.kahomesl.allergenradar.data.LocationPreference
import com.kahomesl.allergenradar.data.ObservationDto
import com.kahomesl.allergenradar.data.ObservationTimeDto
import com.kahomesl.allergenradar.data.ProviderDto
import com.kahomesl.allergenradar.data.SourceDto
import com.kahomesl.allergenradar.data.SyncStatusResponseDto
import com.kahomesl.allergenradar.data.HistoryResponseDto
import com.kahomesl.allergenradar.ui.measurementLabel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import java.io.IOException
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
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
    fun partialProviderFailureWithTotalDataRemainsSuccess() = runTest {
        val viewModel = HomeViewModel(FakeRepository(), FakePreference())

        assertFalse(viewModel.state.value.isLoading)
        assertEquals("CURRENT", viewModel.state.value.total?.measurementType)
        assertEquals(listOf("beijing-pollen"), viewModel.state.value.providersWithErrors)
        assertNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun artemisiaEmptyDoesNotUseTotalObservation() = runTest {
        val viewModel = HomeViewModel(FakeRepository(), FakePreference())

        assertNull(viewModel.state.value.artemisia)
        assertEquals("当前指数", measurementLabel("CURRENT"))
    }

    @Test
    fun totalRequestFailureUsesSafeNetworkMessage() = runTest {
        val viewModel = HomeViewModel(
            FakeRepository(totalFailure = IOException("CLEARTEXT communication to 10.0.2.2 not permitted")),
            FakePreference(),
        )

        assertEquals("网络连接暂时不可用，请检查网络后重试。", viewModel.state.value.errorMessage)
    }

    private class FakePreference : LocationPreference {
        override val selectedLocationId: Flow<String> = MutableStateFlow("cn-city-beijing")
        override suspend fun setSelectedLocationId(locationId: String) = Unit
    }

    private class FakeRepository(
        private val totalFailure: Throwable? = null,
    ) : AllergenRepository {
        override suspend fun getAllergens() = emptyList<AllergenDto>()
        override suspend fun getProviders() = emptyList<ProviderDto>()
        override suspend fun getLocations() = emptyList<LocationDto>()
        override suspend fun getLocationAllergens(locationId: String): LocationAllergenResponseDto {
            totalFailure?.let { throw it }
            return LocationAllergenResponseDto(
                location = LocationDto(locationId, "北京", "CITY"),
                observations = listOf(
                    ObservationDto(
                        id = "total-current",
                        locationId = locationId,
                        scope = "TOTAL",
                        measurementType = "CURRENT",
                        unit = "level",
                        risk = com.kahomesl.allergenradar.data.RiskDto(level = 4),
                        provider = "weatherdt",
                        source = SourceDto("WeatherDT"),
                        confidence = 3,
                        time = ObservationTimeDto("2026-08-31T08:10:00.000Z"),
                    ),
                ),
                providersWithErrors = listOf("beijing-pollen"),
            )
        }

        override suspend fun getLocationTaxon(locationId: String, taxon: String) = LocationAllergenResponseDto(
            location = LocationDto(locationId, "北京", "CITY"),
        )

        override suspend fun getHistory(locationId: String, taxon: String?, measurementType: String?, limit: Int) =
            HistoryResponseDto(LocationDto(locationId, "北京", "CITY"))

        override suspend fun getSyncStatus() = SyncStatusResponseDto()
    }
}
