package com.kahomesl.allergenradar.work

import androidx.test.core.app.ApplicationProvider
import androidx.work.BackoffPolicy
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.testing.TestListenableWorkerBuilder
import com.kahomesl.allergenradar.data.ApiErrorDto
import com.kahomesl.allergenradar.data.ApiException
import com.kahomesl.allergenradar.data.RepositoryDataSource
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AllergenRefreshWorkerTest {
    @Before
    fun setUp() {
        AllergenRefreshWorker.refreshTargetForTest = null
    }

    @After
    fun tearDown() {
        AllergenRefreshWorker.refreshTargetForTest = null
    }

    @Test
    fun periodicRequestUsesConnectedTwoHourUniqueConfiguration() {
        val request = AllergenRefreshScheduler.newRequest()

        assertEquals("allergen-background-refresh", AllergenRefreshScheduler.UNIQUE_WORK_NAME)
        assertEquals(NetworkType.CONNECTED, request.workSpec.constraints.requiredNetworkType)
        assertEquals(BackoffPolicy.EXPONENTIAL, request.workSpec.backoffPolicy)
        assertTrue(request.workSpec.backoffDelayDuration >= 10 * 60 * 1000L)
    }

    @Test
    fun emptyArtemisiaNetworkResultIsSuccessfulRefresh() = runTest {
        AllergenRefreshWorker.refreshTargetForTest = FakeRefreshTarget(
            listOf(RepositoryDataSource.NETWORK, RepositoryDataSource.NETWORK),
        )

        val result = worker().doWork()

        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun temporaryNetworkFailureRetries() = runTest {
        AllergenRefreshWorker.refreshTargetForTest = FakeRefreshTarget(error = IOException("offline"))

        val result = worker().doWork()

        assertEquals(ListenableWorker.Result.retry(), result)
    }

    @Test
    fun contract4xxFailsInsteadOfRetrying() = runTest {
        AllergenRefreshWorker.refreshTargetForTest = FakeRefreshTarget(
            error = ApiException(404, ApiErrorDto("LOCATION_NOT_FOUND", "Unknown location")),
        )

        val result = worker().doWork()

        assertEquals(ListenableWorker.Result.failure(), result)
    }

    private fun worker() = TestListenableWorkerBuilder<AllergenRefreshWorker>(
        ApplicationProvider.getApplicationContext(),
    ).build()

    private class FakeRefreshTarget(
        private val sources: List<RepositoryDataSource> = emptyList(),
        private val error: Throwable? = null,
    ) : CurrentLocationRefreshTarget {
        override suspend fun refreshSelectedLocation(): RefreshSnapshot {
            error?.let { throw it }
            return RefreshSnapshot(sources)
        }
    }
}
