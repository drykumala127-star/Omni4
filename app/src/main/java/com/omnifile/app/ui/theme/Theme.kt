package com.omnifile.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val VioletPrimary = Color(0xFF673DE6)
val BlueSecondary = Color(0xFF168BFF)
val EmeraldSuccess = Color(0xFF13A67A)
val RoseAccent = Color(0xFFE95A7B)
val AmberWarning = Color(0xFFEE9A2C)
val InkPrimary = Color(0xFF17143A)
val MutedSlate = Color(0xFF77758D)
val PageBackgroundLight = Color(0xFFF7F7FC)

enum class AppThemeMode {
    VIOLET,
    DARK,
    INDIGO
}

private val VioletColorScheme = lightColorScheme(
    primary = VioletPrimary,
    onPrimary = Color.White,
    secondary = BlueSecondary,
    onSecondary = Color.White,
    tertiary = EmeraldSuccess,
    background = PageBackgroundLight,
    onBackground = InkPrimary,
    surface = Color.White,
    onSurface = InkPrimary,
    surfaceVariant = Color(0xFFEDE7FF),
    onSurfaceVariant = VioletPrimary,
    outline = Color(0xFFE2E1EC)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF9675FF),
    onPrimary = Color(0xFF140738),
    secondary = Color(0xFF38BDF8),
    onSecondary = Color(0xFF032644),
    tertiary = Color(0xFF34D399),
    background = Color(0xFF090810),
    onBackground = Color(0xFFEBEBF5),
    surface = Color(0xFF141224),
    onSurface = Color(0xFFEBEBF5),
    surfaceVariant = Color(0xFF221E3D),
    onSurfaceVariant = Color(0xFFD4CAFD),
    outline = Color(0xFF302B52)
)

private val IndigoColorScheme = darkColorScheme(
    primary = Color(0xFF818CF8),
    onPrimary = Color(0xFF111827),
    secondary = Color(0xFF38BDF8),
    onSecondary = Color(0xFF082F49),
    tertiary = Color(0xFF34D399),
    background = Color(0xFF0B0F19),
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF111827),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF1F2937),
    onSurfaceVariant = Color(0xFFC7D2FE),
    outline = Color(0xFF374151)
)

@Composable
fun OmniFileTheme(
    themeMode: AppThemeMode = AppThemeMode.VIOLET,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        AppThemeMode.VIOLET -> VioletColorScheme
        AppThemeMode.DARK -> DarkColorScheme
        AppThemeMode.INDIGO -> IndigoColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
