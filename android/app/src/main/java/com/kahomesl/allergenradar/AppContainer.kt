package com.kahomesl.allergenradar

import android.content.Context
import androidx.room.Room
import com.kahomesl.allergenradar.data.AllergenDataRepository
import com.kahomesl.allergenradar.data.ApiClient
import com.kahomesl.allergenradar.data.DataStoreLocationPreference
import com.kahomesl.allergenradar.data.LocationPreference
import com.kahomesl.allergenradar.data.NetworkAllergenRepository
import com.kahomesl.allergenradar.data.OfflineFirstAllergenRepository
import com.kahomesl.allergenradar.data.local.AllergenRadarDatabase
import com.kahomesl.allergenradar.data.local.MIGRATION_1_2
import com.kahomesl.allergenradar.data.local.RoomAllergenCache
import com.kahomesl.allergenradar.notifications.DataStoreRiskAlertPreference
import com.kahomesl.allergenradar.notifications.RiskAlertPreference
import com.kahomesl.allergenradar.location.AndroidOneShotLocationClient
import com.kahomesl.allergenradar.location.OneShotLocationClient

class AppContainer(context: Context) {
    private val database = Room.databaseBuilder(
        context.applicationContext,
        AllergenRadarDatabase::class.java,
        "allergen-radar.db",
    ).addMigrations(MIGRATION_1_2).build()
    private val networkRepository = NetworkAllergenRepository(
        ApiClient.create(BuildConfig.API_BASE_URL, BuildConfig.DEBUG),
    )
    val repository: AllergenDataRepository = OfflineFirstAllergenRepository(networkRepository, RoomAllergenCache(database))
    val locationPreference: LocationPreference = DataStoreLocationPreference(context)
    val oneShotLocationClient: OneShotLocationClient = AndroidOneShotLocationClient(context)
    val riskAlertPreference: RiskAlertPreference = DataStoreRiskAlertPreference(context)
}
