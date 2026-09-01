package com.kahomesl.allergenradar.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE observation_cache ADD COLUMN riskSeverity TEXT NOT NULL DEFAULT 'UNKNOWN'")
    }
}

@Database(
    entities = [LocationCacheEntity::class, ObservationCacheEntity::class, QueryCacheMetadataEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class AllergenRadarDatabase : RoomDatabase() {
    abstract fun cacheDao(): CacheDao
}
