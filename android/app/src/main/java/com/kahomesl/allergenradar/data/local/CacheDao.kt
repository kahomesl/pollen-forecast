package com.kahomesl.allergenradar.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface CacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLocation(location: LocationCacheEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLocations(locations: List<LocationCacheEntity>)

    @Query("SELECT * FROM location_cache WHERE id = :id")
    suspend fun location(id: String): LocationCacheEntity?

    @Query("SELECT * FROM location_cache ORDER BY scope, nameCn")
    suspend fun locations(): List<LocationCacheEntity>

    @Query("DELETE FROM location_cache")
    suspend fun clearLocations()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObservations(observations: List<ObservationCacheEntity>)

    @Query("SELECT * FROM observation_cache WHERE cacheKey = :cacheKey ORDER BY observationId")
    suspend fun observations(cacheKey: String): List<ObservationCacheEntity>

    @Query("DELETE FROM observation_cache WHERE cacheKey = :cacheKey")
    suspend fun clearObservations(cacheKey: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMetadata(metadata: QueryCacheMetadataEntity)

    @Query("SELECT * FROM query_cache_metadata WHERE cacheKey = :cacheKey")
    suspend fun metadata(cacheKey: String): QueryCacheMetadataEntity?

    @Transaction
    suspend fun replaceQuery(
        location: LocationCacheEntity,
        metadata: QueryCacheMetadataEntity,
        observations: List<ObservationCacheEntity>,
    ) {
        upsertLocation(location)
        clearObservations(metadata.cacheKey)
        if (observations.isNotEmpty()) insertObservations(observations)
        upsertMetadata(metadata)
    }

    @Transaction
    suspend fun replaceLocations(
        metadata: QueryCacheMetadataEntity,
        locations: List<LocationCacheEntity>,
    ) {
        clearLocations()
        if (locations.isNotEmpty()) upsertLocations(locations)
        upsertMetadata(metadata)
    }
}
