package com.kahomesl.allergenradar.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "location_cache")
data class LocationCacheEntity(
    @PrimaryKey
    val id: String,
    val nameCn: String,
    val scope: String,
    val latitude: Double?,
    val longitude: Double?,
    val cachedAt: Long,
)

@Entity(
    tableName = "observation_cache",
    primaryKeys = ["cacheKey", "observationId"],
    indices = [Index(value = ["cacheKey"])],
)
data class ObservationCacheEntity(
    val cacheKey: String,
    val observationId: String,
    val locationId: String,
    val taxonCode: String?,
    val taxonNameCn: String?,
    val taxonNameEn: String?,
    val scope: String,
    val measurementType: String,
    val value: Double?,
    val minValue: Double?,
    val maxValue: Double?,
    val unit: String,
    val riskLevel: Int?,
    val riskLabel: String?,
    val riskSeverity: String,
    val provider: String,
    val sourceName: String,
    val sourceUrl: String?,
    val confidence: Int,
    val observedAt: String?,
    val validFrom: String?,
    val validTo: String?,
    val retrievedAt: String,
    val cachedAt: Long,
)

@Entity(tableName = "query_cache_metadata")
data class QueryCacheMetadataEntity(
    @PrimaryKey
    val cacheKey: String,
    val locationId: String?,
    val responseKind: String,
    val taxonCode: String?,
    val measurementType: String?,
    val cachedAt: Long,
    val retrievedAt: String?,
    val providersWithErrorsJson: String?,
)
