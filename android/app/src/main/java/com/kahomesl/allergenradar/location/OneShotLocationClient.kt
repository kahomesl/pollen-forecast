package com.kahomesl.allergenradar.location

import android.content.Context
import android.location.LocationManager
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.util.Consumer
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

data class DeviceCoordinates(val latitude: Double, val longitude: Double)

sealed interface OneShotLocationResult {
    data class Available(val coordinates: DeviceCoordinates) : OneShotLocationResult
    data object Unavailable : OneShotLocationResult
    data object TimedOut : OneShotLocationResult
}

interface OneShotLocationClient {
    suspend fun getCurrentLocation(): OneShotLocationResult
}

class AndroidOneShotLocationClient(context: Context) : OneShotLocationClient {
    private val appContext = context.applicationContext
    private val locationManager = appContext.getSystemService(LocationManager::class.java)

    override suspend fun getCurrentLocation(): OneShotLocationResult {
        val provider = preferredProvider() ?: return OneShotLocationResult.Unavailable
        if (!LocationManagerCompat.isLocationEnabled(locationManager)) {
            return OneShotLocationResult.Unavailable
        }
        return withTimeoutOrNull(TIMEOUT_MILLIS) {
            suspendCancellableCoroutine { continuation ->
                val cancellationSignal = CancellationSignal()
                continuation.invokeOnCancellation { cancellationSignal.cancel() }
                try {
                    LocationManagerCompat.getCurrentLocation(
                        locationManager,
                        provider,
                        cancellationSignal,
                        ContextCompat.getMainExecutor(appContext),
                        Consumer { location ->
                            if (continuation.isActive) {
                                continuation.resume(
                                    location?.let { DeviceCoordinates(it.latitude, it.longitude) }
                                        ?.let(OneShotLocationResult::Available)
                                        ?: OneShotLocationResult.Unavailable,
                                )
                            }
                        },
                    )
                } catch (_: SecurityException) {
                    if (continuation.isActive) continuation.resume(OneShotLocationResult.Unavailable)
                }
            }
        } ?: OneShotLocationResult.TimedOut
    }

    private fun preferredProvider(): String? = when {
        LocationManagerCompat.hasProvider(locationManager, FUSED_PROVIDER) -> FUSED_PROVIDER
        LocationManagerCompat.hasProvider(locationManager, LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
        else -> null
    }

    private companion object {
        const val FUSED_PROVIDER = "fused"
        const val TIMEOUT_MILLIS = 10_000L
    }
}
