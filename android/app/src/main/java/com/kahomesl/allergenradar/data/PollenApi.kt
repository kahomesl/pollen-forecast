package com.kahomesl.allergenradar.data

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PollenApi {
    @GET("api/v1/allergens")
    suspend fun getAllergens(): Response<List<AllergenDto>>

    @GET("api/v1/providers")
    suspend fun getProviders(): Response<List<ProviderDto>>

    @GET("api/v1/locations")
    suspend fun getLocations(): Response<List<LocationDto>>

    @GET("api/v1/locations/{locationId}/allergens")
    suspend fun getLocationAllergens(
        @Path("locationId") locationId: String,
    ): Response<LocationAllergenResponseDto>

    @GET("api/v1/locations/{locationId}/allergens/{taxon}")
    suspend fun getLocationTaxon(
        @Path("locationId") locationId: String,
        @Path("taxon") taxon: String,
    ): Response<LocationAllergenResponseDto>

    @GET("api/v1/locations/{locationId}/history")
    suspend fun getHistory(
        @Path("locationId") locationId: String,
        @Query("taxon") taxon: String? = null,
        @Query("measurementType") measurementType: String? = null,
        @Query("limit") limit: Int? = null,
    ): Response<HistoryResponseDto>

    @GET("api/v1/sync/status")
    suspend fun getSyncStatus(): Response<SyncStatusResponseDto>
}
