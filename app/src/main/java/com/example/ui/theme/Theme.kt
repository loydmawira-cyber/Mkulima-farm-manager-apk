package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = ForestGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = ForestGreenLight,
    onPrimaryContainer = ForestGreenOnContainer,
    secondary = ForestGreenDark,
    onSecondary = Color.White,
    secondaryContainer = TagYieldBg,
    onSecondaryContainer = TagYieldText,
    tertiary = TerracottaAccent,
    background = MkulimaBackground,
    onBackground = MkulimaTextPrimary,
    surface = MkulimaSurface,
    onSurface = MkulimaTextPrimary,
    surfaceVariant = MkulimaBackground,
    onSurfaceVariant = MkulimaTextSecondary,
    outline = MkulimaBorder,
    error = StatusUrgentRed
)

private val DarkColorScheme = darkColorScheme(
    primary = FarmGreenDarkPrimary,
    onPrimary = Color.Black,
    primaryContainer = FarmGreenDarkContainer,
    onPrimaryContainer = FarmGreenDarkPrimary,
    secondary = HarvestAmber,
    onSecondary = Color.Black,
    background = FarmGreenDarkSurface,
    onBackground = Color.White,
    surface = FarmGreenDarkContainer,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF26382B),
    onSurfaceVariant = Color(0xFFCBD5E1)
)

val MaterialTheme.terracottaColor: Color
    @Composable get() = TerracottaAccent

@Composable
fun MkulimaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our brand farm colors by default
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MkulimaTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}

