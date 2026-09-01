package com.kahomesl.allergenradar.domain

import com.kahomesl.allergenradar.data.LocationDto
import com.kahomesl.allergenradar.data.RepositoryDataSource
import com.kahomesl.allergenradar.data.TaxonAvailabilityDto
import com.kahomesl.allergenradar.data.TaxonAvailabilityStatusDto
import org.junit.Assert.assertEquals
import org.junit.Test

class ArtemisiaAvailabilityTest {
    @Test
    fun unsupportedLocationStatesThatIndependentDataIsNotConnected() {
        val state = resolveArtemisiaPresentation(
            location = location(TaxonAvailabilityStatusDto.UNSUPPORTED),
            observation = null,
            source = RepositoryDataSource.NETWORK,
        )

        assertEquals(ArtemisiaPresentation.Unsupported, state)
    }

    @Test
    fun aggregateLocationRequiresAChildSelectionWithoutGuessingAChild() {
        val state = resolveArtemisiaPresentation(
            location = location(TaxonAvailabilityStatusDto.CHILD_LOCATION_REQUIRED),
            observation = null,
            source = RepositoryDataSource.NETWORK,
        )

        assertEquals(ArtemisiaPresentation.ChildLocationRequired("北京区县"), state)
    }

    @Test
    fun supportedOnlineEmptyResultIsValidEmptyRatherThanLowRisk() {
        val state = resolveArtemisiaPresentation(
            location = location(TaxonAvailabilityStatusDto.SUPPORTED),
            observation = null,
            source = RepositoryDataSource.NETWORK,
        )

        assertEquals(ArtemisiaPresentation.ValidEmpty, state)
    }

    @Test
    fun cachedEmptyResultRemainsCacheInsteadOfClaimingNoData() {
        val state = resolveArtemisiaPresentation(
            location = location(TaxonAvailabilityStatusDto.SUPPORTED),
            observation = null,
            source = RepositoryDataSource.CACHE,
        )

        assertEquals(ArtemisiaPresentation.CachedEmpty, state)
    }

    @Test
    fun temporaryFailureWithoutCacheIsOfflineWithoutCache() {
        val state = resolveArtemisiaPresentation(
            location = location(TaxonAvailabilityStatusDto.SUPPORTED),
            observation = null,
            source = null,
            temporaryFailure = true,
        )

        assertEquals(ArtemisiaPresentation.OfflineWithoutCache, state)
    }

    private fun location(status: TaxonAvailabilityStatusDto) = LocationDto(
        id = "cn-city-beijing",
        nameCn = "北京",
        scope = "CITY",
        taxonAvailability = listOf(
            TaxonAvailabilityDto(
                taxonCode = ARTEMISIA_TAXON,
                status = status,
                childLocationLabel = "北京区县",
            ),
        ),
    )
}
