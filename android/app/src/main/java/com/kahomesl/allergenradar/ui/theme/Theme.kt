package com.kahomesl.allergenradar.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF146C58), onPrimary = Color.White,
    primaryContainer = Color(0xFFD9F0E8), onPrimaryContainer = Color(0xFF083D32),
    secondary = Color(0xFF52665F), onSecondary = Color.White,
    secondaryContainer = Color(0xFFE4EEE9), onSecondaryContainer = Color(0xFF263B34),
    tertiary = Color(0xFF906A20), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE9BE), onTertiaryContainer = Color(0xFF503A00),
    error = Color(0xFFB53B2D), onError = Color.White,
    errorContainer = Color(0xFFFFDAD4), onErrorContainer = Color(0xFF4B110B),
    background = Color(0xFFF9F9F5), onBackground = Color(0xFF17221E),
    surface = Color(0xFFFFFCF8), onSurface = Color(0xFF17221E),
    surfaceVariant = Color(0xFFE6ECE6), onSurfaceVariant = Color(0xFF56615B), outline = Color(0xFFC1CBC4),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8FD6C0), onPrimary = Color(0xFF00382C),
    primaryContainer = Color(0xFF0A5846), onPrimaryContainer = Color(0xFFD5F5E9),
    secondary = Color(0xFFB8CDC2), onSecondary = Color(0xFF23382F),
    secondaryContainer = Color(0xFF394E45), onSecondaryContainer = Color(0xFFD8EADF),
    tertiary = Color(0xFFF1C777), onTertiary = Color(0xFF432F00),
    tertiaryContainer = Color(0xFF614800), onTertiaryContainer = Color(0xFFFFEAC0),
    error = Color(0xFFFFB4A8), onError = Color(0xFF67150E),
    errorContainer = Color(0xFF8B291F), onErrorContainer = Color(0xFFFFDAD4),
    background = Color(0xFF111A16), onBackground = Color(0xFFE1E9E2),
    surface = Color(0xFF16211C), onSurface = Color(0xFFE1E9E2),
    surfaceVariant = Color(0xFF33423A), onSurfaceVariant = Color(0xFFBBC9C0), outline = Color(0xFF87958D),
)

@Composable
fun AllergenRadarTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors, content = content)
}
