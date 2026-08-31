package com.kahomesl.allergenradar.data

import kotlinx.serialization.Serializable

@Serializable
data class TaxonDto(
    val code: String,
    val nameCn: String,
    val nameEn: String,
)

@Serializable
data class AllergenDto(
    val code: String,
    val nameCn: String,
    val nameEn: String,
    val aliases: List<String> = emptyList(),
    val scope: String,
)

@Serializable
data class ProviderDto(
    val id: String,
    val name: String,
    val capabilities: List<String> = emptyList(),
    val supportedTaxa: List<String> = emptyList(),
)

@Serializable
data class LocationDto(
    val id: String,
    val nameCn: String,
    val scope: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
)

@Serializable
data class RiskDto(
    val level: Int? = null,
    val label: String? = null,
)

@Serializable
data class SourceDto(
    val name: String,
    val url: String? = null,
)

@Serializable
data class ObservationTimeDto(
    val retrievedAt: String,
    val observedAt: String? = null,
    val validFrom: String? = null,
    val validTo: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

@Serializable
data class ObservationDto(
    val id: String,
    val locationId: String,
    val taxon: TaxonDto? = null,
    val scope: String,
    val measurementType: String,
    val value: Double? = null,
    val minValue: Double? = null,
    val maxValue: Double? = null,
    val unit: String,
    val risk: RiskDto,
    val provider: String,
    val source: SourceDto,
    val confidence: Int,
    val time: ObservationTimeDto,
)

@Serializable
data class LocationAllergenResponseDto(
    val location: LocationDto,
    val observations: List<ObservationDto> = emptyList(),
    val providersWithErrors: List<String> = emptyList(),
)

@Serializable
data class HistoryResponseDto(
    val location: LocationDto,
    val observations: List<ObservationDto> = emptyList(),
)

@Serializable
data class SyncRunDto(
    val status: String,
    val startedAt: String,
    val finishedAt: String? = null,
    val trigger: String,
    val locationsAttempted: Int,
    val locationsSucceeded: Int,
    val locationsFailed: Int,
    val observationsReceived: Int,
    val observationsPersisted: Int,
)

@Serializable
data class SyncStatusResponseDto(
    val latestRun: SyncRunDto? = null,
)

@Serializable
data class ApiErrorDto(
    val code: String,
    val message: String,
)

@Serializable
data class ApiErrorEnvelopeDto(
    val error: ApiErrorDto,
)

enum class ProviderCapability {
    TOTAL_OBSERVATION,
    TOTAL_FORECAST,
    CATEGORY_FORECAST,
    GENUS_OBSERVATION,
    GENUS_FORECAST,
    HISTORY,
}

fun ProviderDto.capabilitySet(): Set<ProviderCapability> = capabilities.mapNotNull { value ->
    when (value) {
        "TOTAL_OBSERVATION", "TOTAL_CURRENT" -> ProviderCapability.TOTAL_OBSERVATION
        "TOTAL_FORECAST" -> ProviderCapability.TOTAL_FORECAST
        "CATEGORY_FORECAST" -> ProviderCapability.CATEGORY_FORECAST
        "GENUS_OBSERVATION" -> ProviderCapability.GENUS_OBSERVATION
        "GENUS_FORECAST" -> ProviderCapability.GENUS_FORECAST
        "HISTORY" -> ProviderCapability.HISTORY
        else -> null
    }
}.toSet()
