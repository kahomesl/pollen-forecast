package com.kahomesl.allergenradar.data

import kotlinx.serialization.json.Json
import retrofit2.Response
import java.io.IOException
import com.kahomesl.allergenradar.data.local.CachedValue
import com.kahomesl.allergenradar.data.local.CacheKey
import com.kahomesl.allergenradar.data.local.RoomAllergenCache

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

enum class RepositoryDataSource { NETWORK, CACHE }

data class RepositoryResult<T>(
    val data: T,
    val source: RepositoryDataSource,
    val cachedAt: Long? = null,
    val retrievedAt: String? = null,
)

interface AllergenDataRepository {
    suspend fun getAllergens(): List<AllergenDto>
    suspend fun getProviders(): List<ProviderDto>
    suspend fun getLocations(): RepositoryResult<List<LocationDto>>
    suspend fun getLocationAllergens(locationId: String): RepositoryResult<LocationAllergenResponseDto>
    suspend fun getLocationTaxon(locationId: String, taxon: String): RepositoryResult<LocationAllergenResponseDto>
    suspend fun getHistory(
        locationId: String,
        taxon: String? = null,
        measurementType: String? = null,
        limit: Int = 100,
    ): RepositoryResult<HistoryResponseDto>
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

class OfflineFirstAllergenRepository(
    private val network: AllergenRepository,
    private val cache: RoomAllergenCache,
) : AllergenDataRepository {
    override suspend fun getAllergens(): List<AllergenDto> = network.getAllergens()

    override suspend fun getProviders(): List<ProviderDto> = network.getProviders()

    override suspend fun getLocations(): RepositoryResult<List<LocationDto>> = loadWithCache(
        networkCall = network::getLocations,
        writeCache = cache::replaceLocations,
        readCache = cache::readLocations,
    )

    override suspend fun getLocationAllergens(locationId: String): RepositoryResult<LocationAllergenResponseDto> =
        loadWithCache(
            networkCall = { network.getLocationAllergens(locationId) },
            writeCache = { response -> cache.replaceCurrent(CacheKey.total(locationId), response) },
            readCache = { cache.readCurrent(CacheKey.total(locationId)) },
        )

    override suspend fun getLocationTaxon(
        locationId: String,
        taxon: String,
    ): RepositoryResult<LocationAllergenResponseDto> {
        val cacheKey = CacheKey.taxon(locationId, taxon)
        return loadWithCache(
            networkCall = { network.getLocationTaxon(locationId, taxon) },
            writeCache = { response -> cache.replaceCurrent(cacheKey, response) },
            readCache = { cache.readCurrent(cacheKey) },
        )
    }

    override suspend fun getHistory(
        locationId: String,
        taxon: String?,
        measurementType: String?,
        limit: Int,
    ): RepositoryResult<HistoryResponseDto> {
        val cacheKey = CacheKey.history(locationId, taxon, measurementType)
        return loadWithCache(
            networkCall = { network.getHistory(locationId, taxon, measurementType, limit) },
            writeCache = { response -> cache.replaceHistory(cacheKey, taxon, measurementType, response) },
            readCache = { cache.readHistory(cacheKey) },
        )
    }

    override suspend fun getSyncStatus(): SyncStatusResponseDto = network.getSyncStatus()

    private suspend fun <T> loadWithCache(
        networkCall: suspend () -> T,
        writeCache: suspend (T) -> Unit,
        readCache: suspend () -> CachedValue<T>?,
    ): RepositoryResult<T> = try {
        val data = networkCall()
        runCatching { writeCache(data) }
        RepositoryResult(data = data, source = RepositoryDataSource.NETWORK)
    } catch (error: Throwable) {
        if (!error.isCacheEligible()) throw error
        val cached = runCatching { readCache() }.getOrNull() ?: throw error
        RepositoryResult(
            data = cached.data,
            source = RepositoryDataSource.CACHE,
            cachedAt = cached.cachedAt,
            retrievedAt = cached.retrievedAt,
        )
    }
}

private fun Throwable.isCacheEligible(): Boolean =
    this is IOException || this is ApiException && statusCode in 500..599
