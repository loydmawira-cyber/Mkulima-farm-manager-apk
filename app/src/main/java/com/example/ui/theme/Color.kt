package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Single public source for the colors used by Smart Farm App screens and dialogs.
 */

private val ColorScheme.isLightScheme: Boolean
    @Composable
    get() = background.red > 0.5f && background.blue > 0.5f

@Composable
private inline fun <T> ColorScheme.byTheme(light: () -> T, dark: () -> T): T =
    if (isLightScheme) light() else dark()

/** Main application green, supplied by the active Material theme. */
val ForestGreenPrimary: Color
    @Composable
    get() = MaterialTheme.colorScheme.primary

/** Backward-compatible name used by older proof-upload screens. */
val FarmGreenPrimary: Color
    @Composable
    get() = ForestGreenPrimary

/** Backward-compatible light-green name used by older screens. */
val FarmGreenLight: Color
    @Composable
    get() = MaterialTheme.colorScheme.byTheme(
        light = { Color(0xFFDCFCE7) },
        dark = { Color(0xFFB1F1C9) },
    )

/** Backward-compatible dark-green name used by older screens. */
val FarmGreenDark: Color
    @Composable
    get() = ForestGreenDark

/** Dark/light gradient green used by the authentication and hero surfaces. */
val ForestGreenDark: Color
    @Composable
    get() = MaterialTheme.colorScheme.byTheme(
        light = { Color(0xFF0B4D33) },
        dark = { Color(0xFFB1F1C9) },
    )

/** Completed task/status color. */
val StatusCompleted: Color
    @Composable
    get() = MaterialTheme.colorScheme.byTheme(
        light = { Color(0xFF166534) },
        dark = { Color(0xFF86EFAC) },
    )

/** Urgent task/status color. */
val StatusUrgent: Color
    @Composable
    get() = MaterialTheme.colorScheme.byTheme(
        light = { Color(0xFFB42318) },
        dark = { Color(0xFFFFB4AB) },
    )

/** Backward-compatible urgent color alias. */
val StatusUrgentRed: Color
    @Composable
    get() = StatusUrgent

/** Amber harvest/yield accent. */
val HarvestAmber: Color
    @Composable
    get() = MaterialTheme.colorScheme.byTheme(
        light = { Color(0xFFC2410C) },
        dark = { Color(0xFFFFD166) },
    )

/** Light amber accent for secondary text and highlights. */
val HarvestAmberLight: Color
    @Composable
    get() = MaterialTheme.colorScheme.byTheme(
        light = { Color(0xFF92400E) },
        dark = { Color(0xFFFFE7A3) },
    )

/** Shared outline/separator color. */
val LineColor: Color
    @Composable
    get() = MaterialTheme.colorScheme.byTheme(
        light = { Color(0xFFCBD5E1) },
        dark = { Color(0xFF41536B) },
    )

/** Sage green surface and accent. */
val Sage: Color
    @Composable
    get() = MaterialTheme.colorScheme.byTheme(
        light = { Color(0xFFE8F5E9) },
        dark = { Color(0xFF1B3B2A) },
    )

/** Warm soil surface. */
val Soil: Color
    @Composable
    get() = MaterialTheme.colorScheme.byTheme(
        light = { Color(0xFFF4EEE6) },
        dark = { Color(0xFF352A20) },
    )

/** Softer soil surface. */
val SoilSoft: Color
    @Composable
    get() = MaterialTheme.colorScheme.byTheme(
        light = { Color(0xFFFAF7F2) },
        dark = { Color(0xFF211B17) },
    )

/** Terracotta accent. */
val Terracotta: Color
    @Composable
    get() = MaterialTheme.colorScheme.byTheme(
        light = { Color(0xFFC65D3A) },
        dark = { Color(0xFFFFB59F) },
    )

/** Finance tag background and text. */
val TagFinanceBg: Color
    @Composable
    get() = MaterialTheme.colorScheme.byTheme(
        light = { Color(0xFFDCFCE7) },
        dark = { Color(0xFF14532D) },
    )

val TagFinanceText: Color
    @Composable
    get() = MaterialTheme.colorScheme.byTheme(
        light = { Color(0xFF166534) },
        dark = { Color(0xFFBBF7D0) },
    )

/** Herb tag background and text. */
val TagHerbBg: Color
    @Composable
    get() = MaterialTheme.colorScheme.byTheme(
        light = { Color(0xFFECFCCB) },
        dark = { Color(0xFF365314) },
    )

val TagHerbText: Color
    @Composable
    get() = MaterialTheme.colorScheme.byTheme(
        light = { Color(0xFF3F6212) },
        dark = { Color(0xFFD9F99D) },
    )

/** Heat/reminder tag background and text. */
val TagHrBg: Color
    @Composable
    get() = MaterialTheme.colorScheme.byTheme(
        light = { Color(0xFFFFEDD5) },
        dark = { Color(0xFF7C2D12) },
    )

val TagHrText: Color
    @Composable
    get() = MaterialTheme.colorScheme.byTheme(
        light = { Color(0xFF9A3412) },
        dark = { Color(0xFFFFD7AA) },
    )

val TagHRBg: Color
    @Composable
    get() = TagHrBg

val TagHRText: Color
    @Composable
    get() = TagHrText

/** Livestock tag background and text. */
val TagLivestockBg: Color
    @Composable
    get() = MaterialTheme.colorScheme.byTheme(
        light = { Color(0xFFE0F2FE) },
        dark = { Color(0xFF164E63) },
    )

val TagLivestockText: Color
    @Composable
    get() = MaterialTheme.colorScheme.byTheme(
        light = { Color(0xFF075985) },
        dark = { Color(0xFFCFFAFE) },
    )

/** Yield tag background and text. */
val TagYieldBg: Color
    @Composable
    get() = MaterialTheme.colorScheme.byTheme(
        light = { Color(0xFFFEF3C7) },
        dark = { Color(0xFF5A4210) },
    )

val TagYieldText: Color
    @Composable
    get() = MaterialTheme.colorScheme.byTheme(
        light = { Color(0xFF92400E) },
        dark = { Color(0xFFFF
