package com.kahomesl.allergenradar.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kahomesl.allergenradar.AllergenRadarApplication
import com.kahomesl.allergenradar.data.AllergenDataRepository
import com.kahomesl.allergenradar.data.LocationPreference
import com.kahomesl.allergenradar.data.RepositoryDataSource
import com.kahomesl.allergenradar.data.isTemporaryDataFailure
import com.kahomesl.allergenradar.domain.ARTEMISIA_TAXON
import kotlinx.coroutines.flow.first

interface CurrentLocationRefreshTarget {
    suspend fun refreshSelectedLocation(): List<RepositoryDataSource>
}

class CurrentLocationRefresher(
    private val repository: AllergenDataRepository,
    private val preference: LocationPreference,
) : CurrentLocationRefreshTarget {
    override suspend fun refreshSelectedLocation(): List<RepositoryDataSource> {
        val locationId = preference.selectedLocationId.first()
        return listOf(
            repository.getLocationAllergens(locationId).source,
            repository.getLocationTaxon(locationId, ARTEMISIA_TAXON).source,
        )
    }
}

class AllergenRefreshWorker private constructor(
    appContext: Context,
    params: WorkerParameters,
    private val refreshTarget: CurrentLocationRefreshTarget,
) : CoroutineWorker(appContext, params) {
    constructor(appContext: Context, params: WorkerParameters) : this(
        appContext,
        params,
        refreshTargetForTest ?: (appContext.applicationContext as AllergenRadarApplication).refreshTarget,
    )

    override suspend fun doWork(): Result = try {
        val sources = refreshTarget.refreshSelectedLocation()
        if (sources.any { it == RepositoryDataSource.CACHE }) Result.retry() else Result.success()
    } catch (error: Throwable) {
        if (error.isTemporaryDataFailure()) Result.retry() else Result.failure()
    }

    companion object {
        @Volatile
        internal var refreshTargetForTest: CurrentLocationRefreshTarget? = null
    }
}
