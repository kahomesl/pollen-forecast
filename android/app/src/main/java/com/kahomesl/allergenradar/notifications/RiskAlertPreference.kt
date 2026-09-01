package com.kahomesl.allergenradar.notifications

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kahomesl.allergenradar.data.RiskSeverityDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.riskAlertDataStore by preferencesDataStore(name = "risk_alert_preferences")

interface RiskAlertPreference {
    val settings: Flow<RiskAlertSettings>
    suspend fun update(settings: RiskAlertSettings)
}

class DataStoreRiskAlertPreference(context: Context) : RiskAlertPreference {
    private val dataStore = context.applicationContext.riskAlertDataStore
    override val settings: Flow<RiskAlertSettings> = dataStore.data.map { p -> RiskAlertSettings(
        enabled = p[ENABLED] ?: false, notifyTotal = p[NOTIFY_TOTAL] ?: true, notifyArtemisia = p[NOTIFY_ARTEMISIA] ?: true,
        minimumSeverity = RiskSeverityDto.fromWire(p[MINIMUM_SEVERITY]) .takeIf { it != RiskSeverityDto.UNKNOWN } ?: RiskSeverityDto.HIGH,
        lastTotalFingerprint = p[LAST_TOTAL], lastArtemisiaFingerprint = p[LAST_ARTEMISIA],
    ) }
    override suspend fun update(settings: RiskAlertSettings) { dataStore.edit { p ->
        p[ENABLED] = settings.enabled; p[NOTIFY_TOTAL] = settings.notifyTotal; p[NOTIFY_ARTEMISIA] = settings.notifyArtemisia
        p[MINIMUM_SEVERITY] = settings.minimumSeverity.name
        settings.lastTotalFingerprint?.let { p[LAST_TOTAL] = it } ?: p.remove(LAST_TOTAL)
        settings.lastArtemisiaFingerprint?.let { p[LAST_ARTEMISIA] = it } ?: p.remove(LAST_ARTEMISIA)
    } }
    private companion object {
        val ENABLED = booleanPreferencesKey("notification_enabled"); val NOTIFY_TOTAL = booleanPreferencesKey("notify_total"); val NOTIFY_ARTEMISIA = booleanPreferencesKey("notify_artemisia")
        val MINIMUM_SEVERITY = stringPreferencesKey("minimum_severity"); val LAST_TOTAL = stringPreferencesKey("last_total_fingerprint"); val LAST_ARTEMISIA = stringPreferencesKey("last_artemisia_fingerprint")
    }
}
