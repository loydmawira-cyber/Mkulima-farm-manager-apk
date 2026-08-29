package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * High-contrast Smart Farm palette.
 * Dark mode uses deep blue-green surfaces instead of pure black so white and
 * yellow status text remains distinguishable without glare or muddy contrast.
 */
@Immutable
data class MkulimaColors(
    val primary: Color,
    val primaryVariant: Color,
    val primaryContainer: Color,
    val secondary: Color,
    val secondaryContainer: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val cardBackground: Color,
    val cardBorder: Color,
    val heroTop: Color,
    val heroBottom: Color
)

private val LightMkulimaColors = MkulimaColors(
    primary = Color(0xFF126B45),
    primaryVariant = Color(0xFF0B4D33),
    primaryContainer = Color(0xFFDDF5E7),
    secondary = Color(0xFF2F7D5B),
    secondaryContainer = Color(0xFFE2F2E8),
    textPrimary = Color(0xFF102A1F),
    textSecondary = Color(0xFF4B6358),
    cardBackground = Color(0xFFFFFFFF),
    cardBorder = Color(0xFFD7E3DC),
    heroTop = Color(0xFFE8F6EE),
    heroBottom = Color(0xFFF8FCF9)
)

private val DarkMkulimaColors = MkulimaColors(
    primary = Color(0xFF7DE2AA),
    primaryVariant = Color(0xFFB1F1C9),
    primaryContainer = Color(0xFF1E4E3A),
    secondary = Color(0xFF63D8C0),
    secondaryContainer = Color(0xFF164E63),
    textPrimary = Color(0xFFF7FAFC),
    textSecondary = Color(0xFFC3D2E3),
    cardBackground = Color(0xFF17263A),
    cardBorder = Color(0xFF3A506A),
    heroTop = Color(0xFF12352A),
    heroBottom = Color(0xFF0B1E17)
)

private val LightScheme = lightColorScheme(
    primary = LightMkulimaColors.primary,
    onPrimary = Color.White,
    primaryContainer = LightMkulimaColors.primaryContainer,
    onPrimaryContainer = LightMkulimaColors.textPrimary,
    secondary = LightMkulimaColors.secondary,
    onSecondary = Color.White,
    secondaryContainer = LightMkulimaColors.secondaryContainer,
    onSecondaryContainer = LightMkulimaColors.textPrimary,
    background = Color(0xFFF8FCF9),
    onBackground = LightMkulimaColors.textPrimary,
    surface = LightMkulimaColors.cardBackground,
    onSurface = LightMkulimaColors.textPrimary,
    surfaceVariant = Color(0xFFEAF2ED),
    onSurfaceVariant = LightMkulimaColors.textSecondary,
    outline = LightMkulimaColors.cardBorder,
    error = Color(0xFFB42318),
    onError = Color.White
)

private val DarkScheme = darkColorScheme(
    primary = DarkMkulimaColors.primary,
    onPrimary = Color(0xFF06351F),
    primaryContainer = DarkMkulimaColors.primaryContainer,
    onPrimaryContainer = Color(0xFFD7FBE3),
    secondary = DarkMkulimaColors.secondary,
    onSecondary = Color(0xFF06352D),
    secondaryContainer = DarkMkulimaColors.secondaryContainer,
    onSecondaryContainer = Color(0xFFD3F8FF),
    background = Color(0xFF0B1220),
    onBackground = DarkMkulimaColors.textPrimary,
    surface = DarkMkulimaColors.cardBackground,
    onSurface = DarkMkulimaColors.textPrimary,
    surfaceVariant = Color(0xFF223148),
    onSurfaceVariant = DarkMkulimaColors.textSecondary,
    outline = DarkMkulimaColors.cardBorder,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

private val LocalMkulimaColors = staticCompositionLocalOf { LightMkulimaColors }

/**
 * Screen-specific legacy tokens such as ForestGreenPrimary and TagYieldText
 * remain in the project’s existing Color.kt. Keeping them out of this file
 * prevents duplicate top-level declarations when the theme is replaced.
 */
val MaterialTheme.mkulimaColors: MkulimaColors
    @Composable get() = LocalMkulimaColors.current

@Composable
fun MkulimaTheme(
    themeMode: String = "CLASSIC",
    content: @Composable () -> Unit
) {
    val normalizedMode = themeMode.trim().uppercase()
    val useDark = when (normalizedMode) {
        "DARK" -> true
        "SYSTEM" -> isSystemInDarkTheme()
        else -> false
    }
    val colors = if (useDark) DarkMkulimaColors else LightMkulimaColors
    CompositionLocalProvider(LocalMkulimaColors provides colors) {
        MaterialTheme(
            colorScheme = if (useDark) DarkScheme else LightScheme,
            content = content
        )
    }
}
