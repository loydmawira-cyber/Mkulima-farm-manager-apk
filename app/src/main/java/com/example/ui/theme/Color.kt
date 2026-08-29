package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Single source of truth for screen-level colors used throughout Smart Farm App.
 * Replace the existing Color.kt or Colors.kt contents with this file; do not keep
 * a second file defining the same top-level names.
 */

val ForestGreenPrimary: Color
    @Composable get() = MaterialTheme.colorScheme.primary

val StatusUrgentRed: Color
    @Composable get() = MaterialTheme.colorScheme.error

val TagLivestockBg: Color
    @Composable get() = if (MaterialTheme.colorScheme.isLightScheme()) {
        Color(0xFFE0F2FE)
    } else {
        Color(0xFF164E63)
    }

val TagLivestockText: Color
    @Composable get() = if (MaterialTheme.colorScheme.isLightScheme()) {
        Color(0xFF075985)
    } else {
        Color(0xFFCFFAFE)
    }

val TagYieldBg: Color
    @Composable get() = if (MaterialTheme.colorScheme.isLightScheme()) {
        Color(0xFFFEF3C7)
    } else {
        Color(0xFF5A4210)
    }

val TagYieldText: Color
    @Composable get() = if (MaterialTheme.colorScheme.isLightScheme()) {
        Color(0xFF92400E)
    } else {
        Color(0xFFFFE7A3)
    }

private fun androidx.compose.material3.ColorScheme.isLightScheme(): Boolean =
    background.red > 0.5f && background.blue > 0.5f

