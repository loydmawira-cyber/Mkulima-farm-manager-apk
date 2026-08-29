package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Single source of truth for screen-level colors used throughout Smart Farm App.
 * Replace the existing Color.kt or Colors.kt contents with this complete file.
 * Do not keep another file in com.example.ui.theme with the same top-level names.
 */

private val ColorScheme.isLightScheme: Boolean
    @Composable
    get() = background.red > 0.5f && background.blue > 0.5f

/** Primary application green. It follows the active Material theme. */
val ForestGreenPrimary: Color
    @Composable
    get() = MaterialTheme.colorScheme.primary

/** Darker green used by the authentication and hero gradients. */
val ForestGreenDark: Color
    @Composable
    get() = if (MaterialTheme.colorScheme.isLightScheme) {
        Color(0xFF0B4D33)
    } else {
        Color(0xFFB1F1C9)
    }

/** Completed/success status color with readable contrast in both themes. */
val StatusCompleted: Color
    @Composable
    get() = if (MaterialTheme.colorScheme.isLightScheme) {
        Color(0xFF166534)
    } else {
        Color(0xFF86EFAC)
    }

/** Urgent/error status color with readable contrast in both themes. */
val StatusUrgentRed: Color
    @Composable
    get() = if (MaterialTheme.colorScheme.isLightScheme) {
        Color(0xFFB42318)
    } else {
        Color(0xFFFFB4AB)
    }

/** Livestock tag background. */
val TagLivestockBg: Color
    @Composable
    get() = if (MaterialTheme.colorScheme.isLightScheme) {
        Color(0xFFE0F2FE)
    } else {
        Color(0xFF164E63)
    }

/** Livestock tag text. */
val TagLivestockText: Color
    @Composable
    get() = if (MaterialTheme.colorScheme.isLightScheme) {
        Color(0xFF075985)
    } else {
        Color(0xFFCFFAFE)
    }

/** Yield tag background. */
val TagYieldBg: Color
    @Composable
    get() = if (MaterialTheme.colorScheme.isLightScheme) {
        Color(0xFFFEF3C7)
    } else {
        Color(0xFF5A4210)
    }

/** Yield tag text. */
val TagYieldText: Color
    @Composable
    get() = if (MaterialTheme.colorScheme.isLightScheme) {
        Color(0xFF92400E)
    } else {
        Color(0xFFFFE7A3)
    }
