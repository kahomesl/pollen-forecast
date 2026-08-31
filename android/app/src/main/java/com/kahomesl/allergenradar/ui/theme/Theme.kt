package com.kahomesl.allergenradar.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF236A5B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC5F1E0),
    onPrimaryContainer = Color(0xFF002018),
    secondary = Color(0xFF4B635B),
    secondaryContainer = Color(0xFFCDE9DD),
    tertiary = Color(0xFF466179),
    background = Color(0xFFF7F9F5),
    surface = Color(0xFFF7F9F5),
    surfaceVariant = Color(0xFFE1E8E1),
)

@Composable
fun AllergenRadarTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColors, content = content)
}
