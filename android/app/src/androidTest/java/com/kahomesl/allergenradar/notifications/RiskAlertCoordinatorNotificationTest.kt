package com.kahomesl.allergenradar.notifications

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kahomesl.allergenradar.data.LocationAllergenResponseDto
import com.kahomesl.allergenradar.data.LocationDto
import com.kahomesl.allergenradar.data.ObservationDto
import com.kahomesl.allergenradar.data.ObservationTimeDto
import com.kahomesl.allergenradar.data.RepositoryDataSource
import com.kahomesl.allergenradar.data.RepositoryResult
import com.kahomesl.allergenradar.data.RiskDto
import com.kahomesl.allergenradar.data.RiskSeverityDto
import com.kahomesl.allergenradar.data.SourceDto
import com.kahomesl.allergenradar.data.TaxonDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RiskAlertCoordinatorNotificationTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val manager = context.getSystemService(NotificationManager::class.java)

    @Before
    fun clearNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(
                context.packageName,
                Manifest.permission.POST_NOTIFICATIONS,
            )
        }
        manager.cancelAll()
    }

    @After
    fun cleanUpNotifications() {
        manager.cancelAll()
    }

    @Test
    fun highTotalCurrentPublishesSystemNotificationWithCurrentIndexWording() = runBlocking {
        coordinator().evaluate(network(totalObservation(id = "total-current", severity = RiskSeverityDto.HIGH)), network())

        val notification = awaitNotifications(1).single().notification
        assertTrue(notification.extras.getCharSequence("android.title").toString().contains("综合花粉"))
        assertTrue(notification.extras.getCharSequence("android.text").toString().contains("当前指数"))
    }

    @Test
    fun highArtemisiaForecastPublishesSystemNotificationWithForecastWording() = runBlocking {
        coordinator().evaluate(network(), network(artemisiaObservation(id = "artemisia-forecast", severity = RiskSeverityDto.HIGH)))

        val notification = awaitNotifications(1).single().notification
        assertTrue(notification.extras.getCharSequence("android.title").toString().contains("蒿属"))
        assertTrue(notification.extras.getCharSequence("android.text").toString().contains("预报风险"))
    }

    @Test
    fun cacheEstimateUnknownAndEmptyArtemisiaNeverPublishNotifications() = runBlocking {
        val highTotal = totalObservation(id = "cache-high", severity = RiskSeverityDto.HIGH)
        coordinator().evaluate(cached(highTotal), network())
        coordinator().evaluate(network(totalObservation(id = "estimate-high", severity = RiskSeverityDto.HIGH, measurementType = "ESTIMATE")), network())
        coordinator().evaluate(network(totalObservation(id = "unknown", severity = RiskSeverityDto.UNKNOWN)), network())
        coordinator().evaluate(network(), network())

        assertTrue(manager.activeNotifications.isEmpty())
    }

    @Test
    fun fingerprintSuppressesDuplicatesAndSeverityEscalationPublishesAgain() = runBlocking {
        val preference = InMemoryRiskAlertPreference()
        val coordinator = RiskAlertCoordinator(context, preference)
        coordinator.evaluate(network(totalObservation(id = "escalation", severity = RiskSeverityDto.HIGH)), network())
        assertEquals(1, awaitNotifications(1).size)

        coordinator.evaluate(network(totalObservation(id = "escalation", severity = RiskSeverityDto.HIGH)), network())
        assertEquals(1, manager.activeNotifications.size)

        coordinator.evaluate(network(totalObservation(id = "escalation", severity = RiskSeverityDto.VERY_HIGH)), network())
        assertEquals(2, awaitNotifications(2).size)
    }

    @Test
    fun dataStorePersistsEnabledSettingsAndFingerprintAcrossNewInstances() = runBlocking {
        val preference = DataStoreRiskAlertPreference(context)
        preference.update(RiskAlertSettings(enabled = true))
        val expectedFingerprint = "beijing-chaoyang:TOTAL:CURRENT:persisted:HIGH"

        RiskAlertCoordinator(context, preference).evaluate(
            network(totalObservation(id = "persisted", severity = RiskSeverityDto.HIGH)),
            network(),
        )

        val reloaded = DataStoreRiskAlertPreference(context).settings.first()
        assertTrue(reloaded.enabled)
        assertEquals(expectedFingerprint, reloaded.lastTotalFingerprint)

        preference.update(RiskAlertSettings())
    }

    private fun coordinator(): RiskAlertCoordinator = RiskAlertCoordinator(context, InMemoryRiskAlertPreference())

    private fun network(vararg observations: ObservationDto): RepositoryResult<LocationAllergenResponseDto> =
        RepositoryResult(response(observations.toList()), RepositoryDataSource.NETWORK)

    private fun cached(vararg observations: ObservationDto): RepositoryResult<LocationAllergenResponseDto> =
        RepositoryResult(response(observations.toList()), RepositoryDataSource.CACHE)

    private fun response(observations: List<ObservationDto> = emptyList()) = LocationAllergenResponseDto(
        location = LocationDto(id = "beijing-chaoyang", nameCn = "朝阳", scope = "DISTRICT"),
        observations = observations,
    )

    private fun totalObservation(id: String, severity: RiskSeverityDto, measurementType: String = "CURRENT") = observation(
        id = id,
        scope = "TOTAL",
        measurementType = measurementType,
        severity = severity,
        taxon = null,
    )

    private fun artemisiaObservation(id: String, severity: RiskSeverityDto) = observation(
        id = id,
        scope = "GENUS",
        measurementType = "FORECAST",
        severity = severity,
        taxon = TaxonDto("ARTEMISIA", "蒿属", "Artemisia"),
    )

    private fun observation(
        id: String,
        scope: String,
        measurementType: String,
        severity: RiskSeverityDto,
        taxon: TaxonDto?,
    ) = ObservationDto(
        id = id,
        locationId = "beijing-chaoyang",
        taxon = taxon,
        scope = scope,
        measurementType = measurementType,
        unit = "INDEX",
        risk = RiskDto(level = 4, label = "高", severity = severity),
        provider = "fixture",
        source = SourceDto("fixture"),
        confidence = 100,
        time = ObservationTimeDto(retrievedAt = "2026-09-01T00:00:00Z"),
    )

    private fun awaitNotifications(expected: Int): Array<android.service.notification.StatusBarNotification> {
        repeat(20) {
            val current = manager.activeNotifications
            if (current.size >= expected) return current
            Thread.sleep(100)
        }
        return manager.activeNotifications
    }

    private class InMemoryRiskAlertPreference : RiskAlertPreference {
        private val state = MutableStateFlow(RiskAlertSettings(enabled = true))
        override val settings: Flow<RiskAlertSettings> = state
        override suspend fun update(settings: RiskAlertSettings) {
            state.value = settings
        }
    }
}
