package com.kahomesl.allergenradar.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.kahomesl.allergenradar.MainActivity
import com.kahomesl.allergenradar.R
import com.kahomesl.allergenradar.data.RepositoryResult
import com.kahomesl.allergenradar.data.LocationAllergenResponseDto
import kotlinx.coroutines.flow.first

class RiskAlertCoordinator(private val context: Context, private val preference: RiskAlertPreference) {
    suspend fun evaluate(total: RepositoryResult<LocationAllergenResponseDto>, artemisia: RepositoryResult<LocationAllergenResponseDto>) {
        var settings = preference.settings.first()
        listOf(total to total.data.observations, artemisia to artemisia.data.observations).forEach { (result, observations) ->
            observations.forEach { observation ->
                val decision = RiskAlertEvaluator(settings).evaluate(RiskAlertCandidate(result.data.location.nameCn, result.source, observation)) ?: return@forEach
                if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return@forEach
                publish(decision)
                settings = if (decision.target == RiskAlertTarget.TOTAL) settings.copy(lastTotalFingerprint = decision.fingerprint) else settings.copy(lastArtemisiaFingerprint = decision.fingerprint)
                preference.update(settings)
            }
        }
    }
    private fun publish(decision: RiskAlertDecision) {
        val manager = context.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) manager.createNotificationChannel(NotificationChannel(CHANNEL_ID, "花粉风险提醒", NotificationManager.IMPORTANCE_DEFAULT))
        val intent = Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pending = PendingIntent.getActivity(context, decision.fingerprint.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        manager.notify(decision.fingerprint.hashCode(), NotificationCompat.Builder(context, CHANNEL_ID).setSmallIcon(R.drawable.ic_allergen_radar).setContentTitle(decision.title).setContentText(decision.body).setContentIntent(pending).setAutoCancel(true).build())
    }
    companion object { const val CHANNEL_ID = "pollen_risk_alerts" }
}
