package com.kahomesl.allergenradar.data

import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import retrofit2.Response

class RepositoryTest {
    @Test
    fun mapsStructured404ToApiException() = runTest {
        val repository = NetworkAllergenRepository(FakeApi())

        val exception = runCatching { repository.getLocationTaxon("unknown", "ARTEMISIA") }.exceptionOrNull()

        assertNotNull(exception)
        assertEquals(ApiException::class.java, exception!!::class.java)
        assertEquals(404, (exception as ApiException).statusCode)
        assertEquals("LOCATION_NOT_FOUND", exception.apiError?.code)
    }

    private class FakeApi : PollenApi {
        override suspend fun getAllergens() = Response.success(emptyList<AllergenDto>())
        override suspend fun getProviders() = Response.success(emptyList<ProviderDto>())
        override suspend fun getLocations() = Response.success(emptyList<LocationDto>())
        override suspend fun getLocationAllergens(locationId: String) = Response.success(
            LocationAllergenResponseDto(LocationDto(locationId, "北京", "CITY")),
        )

        override suspend fun getLocationTaxon(locationId: String, taxon: String) = Response.error<LocationAllergenResponseDto>(
            404,
            """{"error":{"code":"LOCATION_NOT_FOUND","message":"Unknown location"}}"""
                .toResponseBody("application/json".toMediaType()),
        )

        override suspend fun getHistory(
            locationId: String,
            taxon: String?,
            measurementType: String?,
            limit: Int?,
        ) = Response.success(HistoryResponseDto(LocationDto(locationId, "北京", "CITY")))

        override suspend fun getSyncStatus() = Response.success(SyncStatusResponseDto())
    }
}
