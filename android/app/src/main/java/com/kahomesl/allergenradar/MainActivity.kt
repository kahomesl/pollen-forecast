package com.kahomesl.allergenradar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.kahomesl.allergenradar.ui.AllergenRadarApp
import com.kahomesl.allergenradar.ui.theme.AllergenRadarTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as AllergenRadarApplication).container
        setContent {
            AllergenRadarTheme {
                AllergenRadarApp(container)
            }
        }
    }
}
