package com.kahomesl.allergenradar.domain

import com.kahomesl.allergenradar.data.LocationDto
import com.kahomesl.allergenradar.data.ObservationDto
import com.kahomesl.allergenradar.data.RepositoryDataSource
import com.kahomesl.allergenradar.data.TaxonAvailabilityStatusDto

sealed interface ArtemisiaPresentation {
    data object Available : ArtemisiaPresentation
    data object Unsupported : ArtemisiaPresentation
    data class ChildLocationRequired(val label: String) : ArtemisiaPresentation
    data object ValidEmpty : ArtemisiaPresentation
    data object CachedEmpty : ArtemisiaPresentation
    data object OfflineWithoutCache : ArtemisiaPresentation
    data object ProviderTemporarilyUnavailable : ArtemisiaPresentation
    data object AvailabilityUnknown : ArtemisiaPresentation
}

fun resolveArtemisiaPresentation(
    location: LocationDto?,
    observation: ObservationDto?,
    source: RepositoryDataSource?,
    temporaryFailure: Boolean = false,
    providersWithErrors: List<String> = emptyList(),
): ArtemisiaPresentation {
    if (observation != null) return ArtemisiaPresentation.Available
    if (source == RepositoryDataSource.CACHE) return ArtemisiaPresentation.CachedEmpty
    if (temporaryFailure) return ArtemisiaPresentation.OfflineWithoutCache
    if (providersWithErrors.isNotEmpty()) return ArtemisiaPresentation.ProviderTemporarilyUnavailable

    return when (location?.taxonAvailability
        ?.firstOrNull { it.taxonCode.equals(ARTEMISIA_TAXON, ignoreCase = true) }
        ?.status) {
        TaxonAvailabilityStatusDto.UNSUPPORTED -> ArtemisiaPresentation.Unsupported
        TaxonAvailabilityStatusDto.CHILD_LOCATION_REQUIRED -> ArtemisiaPresentation.ChildLocationRequired(
            location.taxonAvailability.first { it.taxonCode.equals(ARTEMISIA_TAXON, ignoreCase = true) }
                .childLocationLabel ?: "下级位置",
        )
        TaxonAvailabilityStatusDto.SUPPORTED -> ArtemisiaPresentation.ValidEmpty
        else -> ArtemisiaPresentation.AvailabilityUnknown
    }
}
