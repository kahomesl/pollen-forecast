package com.kahomesl.allergenradar.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kahomesl.allergenradar.AllergenRadarApplication
import com.kahomesl.allergenradar.data.AllergenDataRepository
import com.kahomesl.allergenradar.data.LocationPreference
import com.kahomesl.allergenradar.data.RepositoryDataSource
import com.kahomesl.allergenradar.data.RepositoryResult
import com.kahomesl.allergenradar.data.LocationAllergenResponseDto
import com.kahomesl.allergenradar.data.isTemporaryDataFailure
import com.kahomesl.allergenradar.notifications.RiskAlertCoordinator
import com.kahomesl.allergenradar.domain.ARTEMISIA_TAXON
import kotlinx.coroutines.flow.first

interface CurrentLocationRefreshTarget {
    suspend fun refreshSelectedLocation(): RefreshSnapshot
}
data class RefreshSnapshot(val sources: List<RepositoryDataSource>, val total: RepositoryResult<LocationAllergenResponseDto>? = null, val artemisia: RepositoryResult<LocationAllergenResponseDto>? = null)

class CurrentLocationRefresher(
    private val repository: AllergenDataRepository,
    private val preference: LocationPreference,
) : CurrentLocationRefreshTarget {
    override suspend fun refreshSelectedLocation(): RefreshSnapshot {
        val locationId = preference.selectedLocationId.first()
        val total = repository.getLocationAllergens(locationId); val artemisia = repository.getLocationTaxon(locationId, ARTEMISIA_TAXON)
        return RefreshSnapshot(listOf(total.source, artemisia.source), total, artemisia)
    }
}

class AllergenRefreshWorker private constructor(
    appContext: Context,
    params: WorkerParameters,
    private val refreshTarget: CurrentLocationRefreshTarget,
    private val coordinator: RiskAlertCoordinator? = null,
) : CoroutineWorker(appContext, params) {
    constructor(appContext: Context, params: WorkerParameters) : this(
        appContext,
        params,
        refreshTargetForTest ?: (appContext.applicationContext as AllergenRadarApplication).refreshTarget,
        (appContext.applicationContext as AllergenRadarApplication).riskAlertCoordinator,
    )

    override suspend fun doWork(): Result = try {
        val snapshot = refreshTarget.refreshSelectedLocation()
        if (snapshot.total != null && snapshot.artemisia != null) coordinator?.evaluate(snapshot.total, snapshot.artemisia)
        if (snapshot.sources.any { it == RepositoryDataSource.CACHE }) Result.retry() else Result.success()
    } catch (error: Throwable) {
        if (error.isTemporaryDataFailure()) Result.retry() else Result.failure()
    }

    companion object {
        @Volatile
        internal var refreshTargetForTest: CurrentLocationRefreshTarget? = null
    }
}
