package com.kahomesl.allergenradar.location

import com.kahomesl.allergenradar.data.LocationDto
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class SupportedLocationMatch(
    val location: LocationDto,
    val distanceKm: Double,
)

class SupportedLocationMatcher {
    fun match(latitude: Double, longitude: Double, locations: List<LocationDto>): SupportedLocationMatch? {
        if (!latitude.isValidLatitude() || !longitude.isValidLongitude()) return null
        val matches = locations.mapNotNull { location ->
            val locationLatitude = location.latitude ?: return@mapNotNull null
            val locationLongitude = location.longitude ?: return@mapNotNull null
            SupportedLocationMatch(location, haversineKm(latitude, longitude, locationLatitude, locationLongitude))
        }
        val closestCity = matches.filter { it.location.scope == "CITY" }.minByOrNull { it.distanceKm }
        val closestDistrict = matches.filter { it.location.scope == "DISTRICT" }.minByOrNull { it.distanceKm }
        val preferred = when {
            closestDistrict == null -> closestCity
            closestCity == null -> closestDistrict
            closestDistrict.distanceKm + DISTRICT_CLEAR_ADVANTAGE_KM <= closestCity.distanceKm -> closestDistrict
            else -> closestCity
        }
        return preferred?.takeIf { it.distanceKm <= MAXIMUM_DISTANCE_KM }
    }

    private fun haversineKm(latitudeA: Double, longitudeA: Double, latitudeB: Double, longitudeB: Double): Double {
        val latitudeDelta = Math.toRadians(latitudeB - latitudeA)
        val longitudeDelta = Math.toRadians(longitudeB - longitudeA)
        val haversine = sin(latitudeDelta / 2).pow(2) +
            cos(Math.toRadians(latitudeA)) * cos(Math.toRadians(latitudeB)) * sin(longitudeDelta / 2).pow(2)
        return EARTH_RADIUS_KM * 2 * asin(sqrt(haversine.coerceIn(0.0, 1.0)))
    }

    private fun Double.isValidLatitude(): Boolean = this in -90.0..90.0

    private fun Double.isValidLongitude(): Boolean = this in -180.0..180.0

    companion object {
        const val MAXIMUM_DISTANCE_KM = 150.0
        private const val DISTRICT_CLEAR_ADVANTAGE_KM = 5.0
        private const val EARTH_RADIUS_KM = 6371.0088
    }
}
