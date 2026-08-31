package com.kahomesl.allergenradar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.kahomesl.allergenradar.ui.AllergenRadarApp
import com.kahomesl.allergenradar.ui.theme.AllergenRadarTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = AppContainer(applicationContext)
        setContent {
            AllergenRadarTheme {
                AllergenRadarApp(container)
            }
        }
    }
}
