package com.kahomesl.allergenradar.data.local

import com.kahomesl.allergenradar.data.LocationAllergenResponseDto
import com.kahomesl.allergenradar.data.LocationDto
import com.kahomesl.allergenradar.data.ObservationDto
import com.kahomesl.allergenradar.data.ObservationTimeDto
import com.kahomesl.allergenradar.data.RiskDto
import com.kahomesl.allergenradar.data.SourceDto
import com.kahomesl.allergenradar.data.TaxonDto
import kotlinx.serialization.json.Json

data class CachedValue<T>(
    val data: T,
    val cachedAt: Long,
    val retrievedAt: String?,
)

object CacheKey {
    const val LOCATIONS = "locations"

    fun total(locationId: String): String = "current:$locationId:total"

    fun taxon(locationId: String, taxonCode: String): String =
        "current:$locationId:taxon:${taxonCode.uppercase()}"

    fun history(locationId: String, taxonCode: String?, measurementType: String?): String =
        "history:$locationId:taxon:${taxonCode?.uppercase() ?: "ALL"}:measurement:${measurementType ?: "ALL"}"
}

class RoomAllergenCache(
    database: AllergenRadarDatabase,
    private val clock: () -> Long = System::currentTimeMillis,
    private val json: Json = Json,
) {
    private val dao = database.cacheDao()

    suspend fun replaceCurrent(cacheKey: String, response: LocationAllergenResponseDto) {
        val cachedAt = clock()
        dao.replaceQuery(
            location = response.location.toCacheEntity(cachedAt),
            metadata = QueryCacheMetadataEntity(
                cacheKey = cacheKey,
                locationId = response.location.id,
                responseKind = "CURRENT",
                taxonCode = cacheKey.substringAfter("taxon:", "").ifBlank { null },
                measurementType = null,
                cachedAt = cachedAt,
                retrievedAt = response.observations.maxOfOrNull { it.time.retrievedAt },
                providersWithErrorsJson = json.encodeToString(response.providersWithErrors),
            ),
            observations = response.observations.map { it.toCacheEntity(cacheKey, cachedAt) },
        )
    }

    suspend fun readCurrent(cacheKey: String): CachedValue<LocationAllergenResponseDto>? {
        val metadata = dao.metadata(cacheKey) ?: return null
        val locationId = metadata.locationId ?: return null
        val location = dao.location(locationId) ?: return null
        val providerErrors = metadata.providersWithErrorsJson?.let { raw ->
            runCatching { json.decodeFromString<List<String>>(raw) }.getOrDefault(emptyList())
        }.orEmpty()
        return CachedValue(
            data = LocationAllergenResponseDto(
                location = location.toDto(),
                observations = dao.observations(cacheKey).map { it.toDto() },
                providersWithErrors = providerErrors,
            ),
            cachedAt = metadata.cachedAt,
            retrievedAt = metadata.retrievedAt,
        )
    }
}

private fun LocationDto.toCacheEntity(cachedAt: Long) = LocationCacheEntity(
    id = id,
    nameCn = nameCn,
    scope = scope,
    latitude = latitude,
    longitude = longitude,
    cachedAt = cachedAt,
)

private fun LocationCacheEntity.toDto() = LocationDto(id, nameCn, scope, latitude, longitude)

private fun ObservationDto.toCacheEntity(cacheKey: String, cachedAt: Long) = ObservationCacheEntity(
    cacheKey = cacheKey,
    observationId = id,
    locationId = locationId,
    taxonCode = taxon?.code,
    taxonNameCn = taxon?.nameCn,
    taxonNameEn = taxon?.nameEn,
    scope = scope,
    measurementType = measurementType,
    value = value,
    minValue = minValue,
    maxValue = maxValue,
    unit = unit,
    riskLevel = risk.level,
    riskLabel = risk.label,
    provider = provider,
    sourceName = source.name,
    sourceUrl = source.url,
    confidence = confidence,
    observedAt = time.observedAt,
    validFrom = time.validFrom,
    validTo = time.validTo,
    retrievedAt = time.retrievedAt,
    cachedAt = cachedAt,
)

private fun ObservationCacheEntity.toDto() = ObservationDto(
    id = observationId,
    locationId = locationId,
    taxon = taxonCode?.let { TaxonDto(it, taxonNameCn.orEmpty(), taxonNameEn.orEmpty()) },
    scope = scope,
    measurementType = measurementType,
    value = value,
    minValue = minValue,
    maxValue = maxValue,
    unit = unit,
    risk = RiskDto(riskLevel, riskLabel),
    provider = provider,
    source = SourceDto(sourceName, sourceUrl),
    confidence = confidence,
    time = ObservationTimeDto(
        retrievedAt = retrievedAt,
        observedAt = observedAt,
        validFrom = validFrom,
        validTo = validTo,
    ),
)
