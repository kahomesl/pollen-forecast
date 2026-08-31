package com.kahomesl.allergenradar

import android.content.Context
import com.kahomesl.allergenradar.data.AllergenRepository
import com.kahomesl.allergenradar.data.ApiClient
import com.kahomesl.allergenradar.data.DataStoreLocationPreference
import com.kahomesl.allergenradar.data.LocationPreference
import com.kahomesl.allergenradar.data.NetworkAllergenRepository

class AppContainer(context: Context) {
    val repository: AllergenRepository = NetworkAllergenRepository(
        ApiClient.create(BuildConfig.API_BASE_URL, BuildConfig.DEBUG),
    )
    val locationPreference: LocationPreference = DataStoreLocationPreference(context)
}
