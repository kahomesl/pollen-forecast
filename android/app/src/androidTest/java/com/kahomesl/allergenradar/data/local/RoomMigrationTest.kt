package com.kahomesl.allergenradar.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RoomMigrationTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val databaseName = "migration-${UUID.randomUUID()}.db"

    @Before
    fun createVersionOneDatabase() {
        context.deleteDatabase(databaseName)
        context.openOrCreateDatabase(databaseName, Context.MODE_PRIVATE, null).use { database ->
            database.execSQL("""
                CREATE TABLE location_cache (
                    id TEXT NOT NULL PRIMARY KEY,
                    nameCn TEXT NOT NULL,
                    scope TEXT NOT NULL,
                    latitude REAL,
                    longitude REAL,
                    cachedAt INTEGER NOT NULL
                )
            """.trimIndent())
            database.execSQL("""
                CREATE TABLE observation_cache (
                    cacheKey TEXT NOT NULL,
                    observationId TEXT NOT NULL,
                    locationId TEXT NOT NULL,
                    taxonCode TEXT,
                    taxonNameCn TEXT,
                    taxonNameEn TEXT,
                    scope TEXT NOT NULL,
                    measurementType TEXT NOT NULL,
                    value REAL,
                    minValue REAL,
                    maxValue REAL,
                    unit TEXT NOT NULL,
                    riskLevel INTEGER,
                    riskLabel TEXT,
                    provider TEXT NOT NULL,
                    sourceName TEXT NOT NULL,
                    sourceUrl TEXT,
                    confidence INTEGER NOT NULL,
                    observedAt TEXT,
                    validFrom TEXT,
                    validTo TEXT,
                    retrievedAt TEXT NOT NULL,
                    cachedAt INTEGER NOT NULL,
                    PRIMARY KEY(cacheKey, observationId)
                )
            """.trimIndent())
            database.execSQL("CREATE INDEX index_observation_cache_cacheKey ON observation_cache(cacheKey)")
            database.execSQL("""
                CREATE TABLE query_cache_metadata (
                    cacheKey TEXT NOT NULL PRIMARY KEY,
                    locationId TEXT,
                    responseKind TEXT NOT NULL,
                    taxonCode TEXT,
                    measurementType TEXT,
                    cachedAt INTEGER NOT NULL,
                    retrievedAt TEXT,
                    providersWithErrorsJson TEXT
                )
            """.trimIndent())
            database.execSQL("""
                INSERT INTO observation_cache(
                    cacheKey, observationId, locationId, scope, measurementType, unit,
                    riskLevel, riskLabel, provider, sourceName, confidence, retrievedAt, cachedAt
                ) VALUES(
                    'current:cn-city-beijing:total', 'weatherdt:legacy', 'cn-city-beijing', 'TOTAL', 'CURRENT', 'level',
                    4, '高', 'weatherdt', 'WeatherDT', 3, '2026-09-01T00:00:00.000Z', 1725148800000
                )
            """.trimIndent())
            database.execSQL("PRAGMA user_version = 1")
        }
    }

    @After
    fun tearDown() {
        context.deleteDatabase(databaseName)
    }

    @Test
    fun migrationFromVersionOnePreservesCacheAndDefaultsSeverityToUnknown() = runTest {
        val database = Room.databaseBuilder(context, AllergenRadarDatabase::class.java, databaseName)
            .addMigrations(MIGRATION_1_2)
            .allowMainThreadQueries()
            .build()

        val observation = database.cacheDao().observations("current:cn-city-beijing:total").single()

        assertEquals("weatherdt:legacy", observation.observationId)
        assertEquals("高", observation.riskLabel)
        assertEquals("UNKNOWN", observation.riskSeverity)
        database.close()
    }
}
