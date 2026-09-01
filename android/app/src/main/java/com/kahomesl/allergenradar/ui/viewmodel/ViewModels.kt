package com.kahomesl.allergenradar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kahomesl.allergenradar.data.AllergenDataRepository
import com.kahomesl.allergenradar.data.ApiException
import com.kahomesl.allergenradar.data.LocationDto
import com.kahomesl.allergenradar.data.LocationPreference
import com.kahomesl.allergenradar.data.ObservationDto
import com.kahomesl.allergenradar.data.RepositoryDataSource
import com.kahomesl.allergenradar.data.isTemporaryDataFailure
import com.kahomesl.allergenradar.data.SyncRunDto
import com.kahomesl.allergenradar.domain.ARTEMISIA_TAXON
import com.kahomesl.allergenradar.domain.ArtemisiaPresentation
import com.kahomesl.allergenradar.domain.resolveArtemisiaPresentation
import com.kahomesl.allergenradar.domain.selectArtemisia
import com.kahomesl.allergenradar.domain.selectPrimaryTotal
import com.kahomesl.allergenradar.location.OneShotLocationClient
import com.kahomesl.allergenradar.location.OneShotLocationResult
import com.kahomesl.allergenradar.location.SupportedLocationMatch
import com.kahomesl.allergenradar.location.SupportedLocationMatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

internal fun userFacingDataError(error: Throwable): String = when (error) {
    is ApiException -> error.apiError?.message ?: "请求失败（HTTP ${error.statusCode}）"
    else -> "网络连接暂时不可用，请检查网络后重试。"
}

inline fun <reified T : ViewModel> viewModelFactory(crossinline create: () -> T): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = create() as VM
    }

data class HomeUiState(
    val locationName: String = "北京",
    val location: LocationDto? = null,
    val isLoading: Boolean = true,
    val total: ObservationDto? = null,
    val artemisia: ObservationDto? = null,
    val providersWithErrors: List<String> = emptyList(),
    val artemisiaError: Boolean = false,
    val totalSource: RepositoryDataSource? = null,
    val totalCachedAt: Long? = null,
    val artemisiaSource: RepositoryDataSource? = null,
    val artemisiaCachedAt: Long? = null,
    val artemisiaOfflineWithoutCache: Boolean = false,
    val artemisiaProvidersWithErrors: List<String> = emptyList(),
    val errorMessage: String? = null,
) {
    val artemisiaUsesCache: Boolean get() = artemisiaSource == RepositoryDataSource.CACHE
    val artemisiaPresentation: ArtemisiaPresentation get() = resolveArtemisiaPresentation(
        location = location,
        observation = artemisia,
        source = artemisiaSource,
        temporaryFailure = artemisiaOfflineWithoutCache,
        providersWithErrors = artemisiaProvidersWithErrors,
    )
}

class HomeViewModel(
    private val repository: AllergenDataRepository,
    private val preference: LocationPreference,
) : ViewModel() {
    private val mutableState = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = mutableState.asStateFlow()
    private var currentLocationId: String? = null

    init {
        viewModelScope.launch {
            preference.selectedLocationId.collectLatest { locationId ->
                currentLocationId = locationId
                load(locationId)
            }
        }
    }

    fun refresh() {
        currentLocationId?.let { locationId -> viewModelScope.launch { load(locationId) } }
    }

    private suspend fun load(locationId: String) {
        mutableState.value = mutableState.value.copy(isLoading = true, errorMessage = null)
        val totalResult = runCatching { repository.getLocationAllergens(locationId) }.getOrElse { error ->
            mutableState.value = mutableState.value.copy(isLoading = false, errorMessage = userFacingDataError(error))
            return
        }
        val taxonResult = runCatching { repository.getLocationTaxon(locationId, ARTEMISIA_TAXON) }
        val taxonResponse = taxonResult.getOrNull()?.data
        val taxonMetadata = taxonResult.getOrNull()
        mutableState.value = HomeUiState(
            locationName = totalResult.data.location.nameCn,
            location = totalResult.data.location,
            isLoading = false,
            total = selectPrimaryTotal(totalResult.data.observations),
            artemisia = taxonResponse?.observations?.let(::selectArtemisia),
            providersWithErrors = (totalResult.data.providersWithErrors + (taxonResponse?.providersWithErrors ?: emptyList())).distinct(),
            artemisiaError = taxonResult.isFailure,
            totalSource = totalResult.source,
            totalCachedAt = totalResult.cachedAt,
            artemisiaSource = taxonMetadata?.source,
            artemisiaCachedAt = taxonMetadata?.cachedAt,
            artemisiaOfflineWithoutCache = taxonResult.exceptionOrNull()?.isTemporaryDataFailure() == true,
            artemisiaProvidersWithErrors = taxonResponse?.providersWithErrors.orEmpty(),
        )
    }
}

data class LocationUiState(
    val isLoading: Boolean = true,
    val locations: List<LocationDto> = emptyList(),
    val selectedLocationId: String = "cn-city-beijing",
    val source: RepositoryDataSource? = null,
    val cachedAt: Long? = null,
    val errorMessage: String? = null,
    val nearbyCandidate: NearbyLocationCandidate? = null,
    val nearbyMessage: String? = null,
    val isFindingNearby: Boolean = false,
)

data class NearbyLocationCandidate(
    val match: SupportedLocationMatch,
    val source: RepositoryDataSource?,
)

class LocationViewModel(
    private val repository: AllergenDataRepository,
    private val preference: LocationPreference,
    private val oneShotLocationClient: OneShotLocationClient,
    private val matcher: SupportedLocationMatcher = SupportedLocationMatcher(),
) : ViewModel() {
    private val mutableState = MutableStateFlow(LocationUiState())
    val state: StateFlow<LocationUiState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            preference.selectedLocationId.collectLatest { id ->
                mutableState.value = mutableState.value.copy(selectedLocationId = id)
            }
        }
        refresh()
    }

    fun refresh() = viewModelScope.launch {
        mutableState.value = mutableState.value.copy(isLoading = true, errorMessage = null)
        runCatching { repository.getLocations() }
            .onSuccess { result -> mutableState.value = mutableState.value.copy(isLoading = false, locations = result.data, source = result.source, cachedAt = result.cachedAt) }
            .onFailure { error -> mutableState.value = mutableState.value.copy(isLoading = false, errorMessage = userFacingDataError(error)) }
    }

    fun select(locationId: String) = viewModelScope.launch {
        preference.setSelectedLocationId(locationId)
        mutableState.value = mutableState.value.copy(nearbyCandidate = null, nearbyMessage = null)
    }

    fun findNearbySupportedLocation() = viewModelScope.launch {
        val current = mutableState.value
        if (current.locations.isEmpty()) {
            mutableState.value = current.copy(nearbyMessage = "位置列表尚未准备好，可继续手动选择位置。")
            return@launch
        }
        mutableState.value = current.copy(isFindingNearby = true, nearbyCandidate = null, nearbyMessage = null)
        when (val result = oneShotLocationClient.getCurrentLocation()) {
            is OneShotLocationResult.Available -> {
                val match = matcher.match(result.coordinates.latitude, result.coordinates.longitude, current.locations)
                mutableState.value = current.copy(
                    isFindingNearby = false,
                    nearbyCandidate = match?.let { NearbyLocationCandidate(it, current.source) },
                    nearbyMessage = if (match == null) "当前位置附近暂无支持的花粉数据，可继续手动选择位置。" else null,
                )
            }
            OneShotLocationResult.TimedOut -> mutableState.value = current.copy(
                isFindingNearby = false,
                nearbyMessage = "获取当前位置超时，可继续手动选择位置。",
            )
            OneShotLocationResult.Unavailable -> mutableState.value = current.copy(
                isFindingNearby = false,
                nearbyMessage = "暂时无法获取当前位置，可继续手动选择位置。",
            )
        }
    }

    fun confirmNearbyLocation() = viewModelScope.launch {
        val candidate = mutableState.value.nearbyCandidate ?: return@launch
        preference.setSelectedLocationId(candidate.match.location.id)
        mutableState.value = mutableState.value.copy(
            nearbyCandidate = null,
            nearbyMessage = "已选择附近支持位置：${candidate.match.location.nameCn}",
        )
    }
}

enum class HistoryTaxonFilter(val title: String, val taxon: String?) {
    TOTAL("综合花粉", null),
    ARTEMISIA("蒿属", ARTEMISIA_TAXON),
}

enum class HistoryMeasurementFilter(val title: String, val measurementType: String?) {
    ALL("全部", null),
    CURRENT("当前", "CURRENT"),
    FORECAST("预报", "FORECAST"),
    OBSERVATION("实测", "OBSERVATION"),
    ESTIMATE("估算", "ESTIMATE"),
}

data class HistoryUiState(
    val isLoading: Boolean = true,
    val locationName: String = "北京",
    val observations: List<ObservationDto> = emptyList(),
    val taxonFilter: HistoryTaxonFilter = HistoryTaxonFilter.TOTAL,
    val measurementFilter: HistoryMeasurementFilter = HistoryMeasurementFilter.ALL,
    val source: RepositoryDataSource? = null,
    val cachedAt: Long? = null,
    val errorMessage: String? = null,
)

class HistoryViewModel(
    private val repository: AllergenDataRepository,
    private val preference: LocationPreference,
) : ViewModel() {
    private val mutableState = MutableStateFlow(HistoryUiState())
    val state: StateFlow<HistoryUiState> = mutableState.asStateFlow()
    private var currentLocationId = "cn-city-beijing"

    init {
        viewModelScope.launch {
            preference.selectedLocationId.collectLatest { locationId ->
                currentLocationId = locationId
                load()
            }
        }
    }

    fun setTaxon(filter: HistoryTaxonFilter) {
        mutableState.value = mutableState.value.copy(taxonFilter = filter)
        refresh()
    }

    fun setMeasurement(filter: HistoryMeasurementFilter) {
        mutableState.value = mutableState.value.copy(measurementFilter = filter)
        refresh()
    }

    fun refresh() = viewModelScope.launch { load() }

    private suspend fun load() {
        val previous = mutableState.value
        mutableState.value = previous.copy(isLoading = true, errorMessage = null)
        runCatching {
            repository.getHistory(
                currentLocationId,
                taxon = previous.taxonFilter.taxon,
                measurementType = previous.measurementFilter.measurementType,
            )
        }.onSuccess { result ->
            val response = result.data
            mutableState.value = mutableState.value.copy(
                isLoading = false,
                locationName = response.location.nameCn,
                observations = if (previous.taxonFilter == HistoryTaxonFilter.TOTAL) {
                    response.observations.filter { it.scope == "TOTAL" }
                } else {
                    response.observations
                },
                source = result.source,
                cachedAt = result.cachedAt,
            )
        }.onFailure { error ->
            mutableState.value = mutableState.value.copy(isLoading = false, errorMessage = userFacingDataError(error))
        }
    }
}

data class MyUiState(
    val locationId: String = "cn-city-beijing",
    val locationName: String = "北京",
    val apiAvailable: Boolean? = null,
    val latestRun: SyncRunDto? = null,
)

class MyViewModel(
    private val repository: AllergenDataRepository,
    private val preference: LocationPreference,
) : ViewModel() {
    private val mutableState = MutableStateFlow(MyUiState())
    val state: StateFlow<MyUiState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            preference.selectedLocationId.collectLatest { id ->
                mutableState.value = mutableState.value.copy(locationId = id)
                refresh()
            }
        }
        refresh()
    }

    fun refresh() = viewModelScope.launch {
        val apiAvailable = runCatching { repository.getProviders() }.isSuccess
        val locations = runCatching { repository.getLocations().data }.getOrDefault(emptyList())
        val latestRun = runCatching { repository.getSyncStatus().latestRun }.getOrNull()
        mutableState.value = mutableState.value.copy(
            apiAvailable = apiAvailable,
            latestRun = latestRun,
            locationName = locations.firstOrNull { it.id == mutableState.value.locationId }?.nameCn ?: mutableState.value.locationId,
        )
    }
}
