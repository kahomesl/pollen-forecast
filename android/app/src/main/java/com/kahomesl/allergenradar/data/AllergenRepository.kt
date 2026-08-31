package com.kahomesl.allergenradar.data

import kotlinx.serialization.json.Json
import retrofit2.Response

class ApiException(
    val statusCode: Int,
    val apiError: ApiErrorDto?,
) : Exception(apiError?.message ?: "请求失败（HTTP $statusCode）")

interface AllergenRepository {
    suspend fun getAllergens(): List<AllergenDto>
    suspend fun getProviders(): List<ProviderDto>
    suspend fun getLocations(): List<LocationDto>
    suspend fun getLocationAllergens(locationId: String): LocationAllergenResponseDto
    suspend fun getLocationTaxon(locationId: String, taxon: String): LocationAllergenResponseDto
    suspend fun getHistory(
        locationId: String,
        taxon: String? = null,
        measurementType: String? = null,
        limit: Int = 100,
    ): HistoryResponseDto
    suspend fun getSyncStatus(): SyncStatusResponseDto
}

class NetworkAllergenRepository(
    private val api: PollenApi,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : AllergenRepository {
    override suspend fun getAllergens(): List<AllergenDto> = api.getAllergens().requireBody()

    override suspend fun getProviders(): List<ProviderDto> = api.getProviders().requireBody()

    override suspend fun getLocations(): List<LocationDto> = api.getLocations().requireBody()

    override suspend fun getLocationAllergens(locationId: String): LocationAllergenResponseDto =
        api.getLocationAllergens(locationId).requireBody()

    override suspend fun getLocationTaxon(locationId: String, taxon: String): LocationAllergenResponseDto =
        api.getLocationTaxon(locationId, taxon).requireBody()

    override suspend fun getHistory(
        locationId: String,
        taxon: String?,
        measurementType: String?,
        limit: Int,
    ): HistoryResponseDto = api.getHistory(locationId, taxon, measurementType, limit).requireBody()

    override suspend fun getSyncStatus(): SyncStatusResponseDto = api.getSyncStatus().requireBody()

    private fun <T> Response<T>.requireBody(): T {
        if (isSuccessful) return body() ?: throw ApiException(code(), null)
        val error = errorBody()?.string()?.let { raw ->
            runCatching { json.decodeFromString<ApiErrorEnvelopeDto>(raw).error }.getOrNull()
        }
        throw ApiException(code(), error)
    }
}
