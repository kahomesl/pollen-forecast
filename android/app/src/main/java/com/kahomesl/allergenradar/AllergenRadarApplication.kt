package com.kahomesl.allergenradar

import android.app.Application
import com.kahomesl.allergenradar.work.AllergenRefreshScheduler
import com.kahomesl.allergenradar.work.CurrentLocationRefresher

class AllergenRadarApplication : Application() {
    val container by lazy { AppContainer(this) }
    val refreshTarget by lazy { CurrentLocationRefresher(container.repository, container.locationPreference) }

    override fun onCreate() {
        super.onCreate()
        AllergenRefreshScheduler.schedule(this)
    }
}
