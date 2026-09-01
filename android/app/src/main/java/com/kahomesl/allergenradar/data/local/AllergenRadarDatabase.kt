package com.kahomesl.allergenradar.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [LocationCacheEntity::class, ObservationCacheEntity::class, QueryCacheMetadataEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AllergenRadarDatabase : RoomDatabase() {
    abstract fun cacheDao(): CacheDao
}
