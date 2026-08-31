package com.kahomesl.allergenradar.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.locationDataStore by preferencesDataStore(name = "location_preferences")

interface LocationPreference {
    val selectedLocationId: Flow<String>
    suspend fun setSelectedLocationId(locationId: String)
}

class DataStoreLocationPreference(context: Context) : LocationPreference {
    private val dataStore = context.applicationContext.locationDataStore

    override val selectedLocationId: Flow<String> = dataStore.data.map { preferences: Preferences ->
        preferences[SELECTED_LOCATION] ?: DEFAULT_LOCATION_ID
    }

    override suspend fun setSelectedLocationId(locationId: String) {
        dataStore.edit { preferences -> preferences[SELECTED_LOCATION] = locationId }
    }

    private companion object {
        val SELECTED_LOCATION = stringPreferencesKey("selected_location_id")
    }
}

const val DEFAULT_LOCATION_ID = "cn-city-beijing"
