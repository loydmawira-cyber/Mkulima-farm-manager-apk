package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Immutable
data class MkulimaColors(
    val primary: Color,
    val primaryVariant: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiary: Color,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val cardBackground: Color,
    val cardBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val heroTop: Color,
    val heroBottom: Color,
    val heroText: Color,
    val heroSubtitleText: Color,
    val urgentCardTop: Color,
    val urgentCardBottom: Color,
    val statusSuccess: Color,
    val statusSuccessBg: Color,
    val statusPending: Color,
    val statusPendingBg: Color,
    val statusUrgent: Color,
    val statusUrgentBg: Color,
    val tagYieldBg: Color,
    val tagYieldText: Color,
    val tagLivestockBg: Color,
    val tagLivestockText: Color,
    val tagFinanceBg: Color,
    val tagFinanceText: Color,
    val tagHRBg: Color,
    val tagHRText: Color,
    val topBarContainer: Color,
    val bottomNavContainer: Color,
    val navSelectedIcon: Color,
    val navUnselectedIcon: Color,
    val navIndicator: Color,
    val chartLine: Color,
    val chartFillTop: Color,
    val chartFillBottom: Color,
    val isDark: Boolean
)

data class AppThemeOption(
    val id: String,
    val title: String,
    val subtitle: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val backgroundColor: Color,
    val isDark: Boolean = false
)

val AvailableAppThemes = listOf(
    AppThemeOption(
        id = "SYSTEM",
        title = "System Default",
        subtitle = "Matches device dark / light mode",
        primaryColor = EmeraldPrimary,
        secondaryColor = ForestGreenPrimary,
        backgroundColor = Color(0xFFF4F9F4)
    ),
    AppThemeOption(
        id = "EMERALD",
        title = "Emerald Farm",
        subtitle = "Lush agricultural forest & mint green",
        primaryColor = Color(0xFF15803D),
        secondaryColor = Color(0xFF166534),
        backgroundColor = Color(0xFFF4F9F4)
    ),
    AppThemeOption(
        id = "GOLDEN",
        title = "Golden Harvest",
        subtitle = "Warm golden amber & honey wheat",
        primaryColor = Color(0xFFB45309),
        secondaryColor = Color(0xFFD97706),
        backgroundColor = Color(0xFFFAF7F2)
    ),
    AppThemeOption(
        id = "TERRACOTTA",
        title = "Earthy Terracotta",
        subtitle = "Warm savanna clay, soil & rust",
        primaryColor = Color(0xFFC2410C),
        secondaryColor = Color(0xFFEA580C),
        backgroundColor = Color(0xFFFAF5F0)
    ),
    AppThemeOption(
        id = "OCEAN",
        title = "Ocean Azure",
        subtitle = "Crisp deep azure & cool cyan",
        primaryColor = Color(0xFF0284C7),
        secondaryColor = Color(0xFF0EA5E9),
        backgroundColor = Color(0xFFF0F7FF)
    ),
    AppThemeOption(
        id = "COFFEE",
        title = "Espresso Roast",
        subtitle = "Rich roasted coffee & mocha earth",
        primaryColor = Color(0xFF78350F),
        secondaryColor = Color(0xFF92400E),
        backgroundColor = Color(0xFFFDF9F5)
    ),
    AppThemeOption(
        id = "DARK",
        title = "Midnight Dark",
        subtitle = "OLED obsidian dark canvas & neon amber",
        primaryColor = Color(0xFFFBBF24),
        secondaryColor = Color(0xFF34D399),
        backgroundColor = Color(0xFF121417),
        isDark = true
    ),
    AppThemeOption(
        id = "LIGHT",
        title = "Classic Light",
        subtitle = "Clean bright standard light theme",
        primaryColor = Color(0xFF15803D),
        secondaryColor = Color(0xFFB45309),
        backgroundColor = Color(0xFFFFFFFF)
    )
)

private val DefaultMkulimaColors = MkulimaColors(
    primary = EmeraldPrimary,
    primaryVariant = EmeraldDark,
    primaryContainer = EmeraldLight,
    onPrimaryContainer = EmeraldOnContainer,
    secondary = ForestGreenPrimary,
    secondaryContainer = TagYieldBg,
    onSecondaryContainer = TagYieldText,
    tertiary = TerracottaPrimary,
    background = EmeraldBackground,
    surface = EmeraldSurface,
    surfaceVariant = Color(0xFFE8F5E9),
    cardBackground = EmeraldSurface,
    cardBorder = EmeraldBorder,
    textPrimary = EmeraldTextPrimary,
    textSecondary = EmeraldTextSecondary,
    textMuted = Color(0xFF6B7280),
    heroTop = EmeraldHeroTop,
    heroBottom = EmeraldHeroBottom,
    heroText = Color(0xFF0D2813),
    heroSubtitleText = Color(0xFF1B4323),
    urgentCardTop = Color(0xFFC4592F),
    urgentCardBottom = Color(0xFF9E3F1E),
    statusSuccess = StatusSuccessGreen,
    statusSuccessBg = Color(0xFFDCFCE7),
    statusPending = StatusPendingAmber,
    statusPendingBg = Color(0xFFFEF3C7),
    statusUrgent = StatusUrgentRed,
    statusUrgentBg = Color(0xFFFEE2E2),
    tagYieldBg = TagYieldBg,
    tagYieldText = TagYieldText,
    tagLivestockBg = TagLivestockBg,
    tagLivestockText = TagLivestockText,
    tagFinanceBg = TagFinanceBg,
    tagFinanceText = TagFinanceText,
    tagHRBg = TagHRBg,
    tagHRText = TagHRText,
    topBarContainer = EmeraldSurface,
    bottomNavContainer = EmeraldSurface,
    navSelectedIcon = EmeraldPrimary,
    navUnselectedIcon = Color(0xFF64748B),
    navIndicator = Color(0xFFDCFCE7),
    chartLine = EmeraldPrimary,
    chartFillTop = EmeraldPrimary.copy(alpha = 0.35f),
    chartFillBottom = EmeraldPrimary.copy(alpha = 0.05f),
    isDark = false
)

val LocalMkulimaColors = staticCompositionLocalOf { DefaultMkulimaColors }

val MaterialTheme.mkulimaColors: MkulimaColors
    @Composable
    @ReadOnlyComposable
    get() = LocalMkulimaColors.current

val MaterialTheme.terracottaColor: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.mkulimaColors.tertiary

fun resolveAppTheme(themeMode: String, isSystemDark: Boolean): Pair<ColorScheme, MkulimaColors> {
    val normalized = themeMode.trim().uppercase()
    val isDark = when (normalized) {
        "DARK", "MIDNIGHT", "MIDNIGHT_DARK", "BLACK" -> true
        "LIGHT", "EMERALD", "GOLDEN", "TERRACOTTA", "OCEAN", "COFFEE" -> false
        else -> isSystemDark
    }

    if (isDark) {
        val darkColorScheme = darkColorScheme(
            primary = FarmGreenDarkPrimary,
            onPrimary = Color(0xFF121417),
            primaryContainer = FarmGreenDarkContainer,
            onPrimaryContainer = Color(0xFFFDE68A),
            secondary = Color(0xFF34D399),
            onSecondary = Color(0xFF121417),
            secondaryContainer = Color(0xFF1E293B),
            onSecondaryContainer = Color(0xFF6EE7B7),
            tertiary = Color(0xFFFB7185),
            background = FarmGreenDarkBackground,
            onBackground = DarkTextPrimary,
            surface = FarmGreenDarkSurface,
            onSurface = DarkTextPrimary,
            surfaceVariant = FarmGreenDarkContainer,
            onSurfaceVariant = DarkTextSecondary,
            outline = DarkBorder,
            error = StatusUrgentRed
        )

        val darkMkulimaColors = MkulimaColors(
            primary = FarmGreenDarkPrimary,
            primaryVariant = Color(0xFFF59E0B),
            primaryContainer = FarmGreenDarkContainer,
            onPrimaryContainer = Color(0xFFFDE68A),
            secondary = Color(0xFF34D399),
            secondaryContainer = Color(0xFF1E293B),
            onSecondaryContainer = Color(0xFF6EE7B7),
            tertiary = Color(0xFFFB7185),
            background = FarmGreenDarkBackground,
            surface = FarmGreenDarkSurface,
            surfaceVariant = FarmGreenDarkContainer,
            cardBackground = FarmGreenDarkSurface,
            cardBorder = DarkBorder,
            textPrimary = DarkTextPrimary,
            textSecondary = DarkTextSecondary,
            textMuted = Color(0xFF64748B),
            heroTop = Color(0xFF282E37),
            heroBottom = Color(0xFF1A1F26),
            heroText = Color(0xFFF8FAFC),
            heroSubtitleText = Color(0xFFCBD5E1),
            urgentCardTop = Color(0xFF991B1B),
            urgentCardBottom = Color(0xFF7F1D1D),
            statusSuccess = Color(0xFF34D399),
            statusSuccessBg = Color(0xFF064E3B),
            statusPending = Color(0xFFFBBF24),
            statusPendingBg = Color(0xFF78350F),
            statusUrgent = Color(0xFFF87171),
            statusUrgentBg = Color(0xFF7F1D1D),
            tagYieldBg = Color(0xFF78350F),
            tagYieldText = Color(0xFFFDE68A),
            tagLivestockBg = Color(0xFF064E3B),
            tagLivestockText = Color(0xFF6EE7B7),
            tagFinanceBg = Color(0xFF7C2D12),
            tagFinanceText = Color(0xFFFFD8BF),
            tagHRBg = Color(0xFF7F1D1D),
            tagHRText = Color(0xFFFECACA),
            topBarContainer = FarmGreenDarkSurface,
            bottomNavContainer = FarmGreenDarkSurface,
            navSelectedIcon = FarmGreenDarkPrimary,
            navUnselectedIcon = Color(0xFF94A3B8),
            navIndicator = Color(0xFF2D2618),
            chartLine = FarmGreenDarkPrimary,
            chartFillTop = FarmGreenDarkPrimary.copy(alpha = 0.35f),
            chartFillBottom = FarmGreenDarkPrimary.copy(alpha = 0.05f),
            isDark = true
        )
        return Pair(darkColorScheme, darkMkulimaColors)
    }

    // Light Theme Palettes
    return when (normalized) {
        "GOLDEN", "GOLDEN_HARVEST" -> {
            val scheme = lightColorScheme(
                primary = ForestGreenPrimary,
                onPrimary = Color.White,
                primaryContainer = ForestGreenLight,
                onPrimaryContainer = ForestGreenOnContainer,
                secondary = ForestGreenDark,
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFFEF3C7),
                onSecondaryContainer = Color(0xFF78350F),
                tertiary = TerracottaPrimary,
                background = MkulimaBackground,
                onBackground = MkulimaTextPrimary,
                surface = MkulimaSurface,
                onSurface = MkulimaTextPrimary,
                surfaceVariant = Color(0xFFFFFBEB),
                onSurfaceVariant = MkulimaTextSecondary,
                outline = MkulimaBorder,
                error = StatusUrgentRed
            )
            val custom = MkulimaColors(
                primary = ForestGreenPrimary,
                primaryVariant = ForestGreenDark,
                primaryContainer = ForestGreenLight,
                onPrimaryContainer = ForestGreenOnContainer,
                secondary = ForestGreenDark,
                secondaryContainer = Color(0xFFFEF3C7),
                onSecondaryContainer = Color(0xFF78350F),
                tertiary = TerracottaPrimary,
                background = MkulimaBackground,
                surface = MkulimaSurface,
                surfaceVariant = Color(0xFFFFFBEB),
                cardBackground = MkulimaSurface,
                cardBorder = MkulimaBorder,
                textPrimary = MkulimaTextPrimary,
                textSecondary = MkulimaTextSecondary,
                textMuted = Color(0xFF78716C),
                heroTop = GoldenHeroTop,
                heroBottom = GoldenHeroBottom,
                heroText = Color(0xFF451A03),
                heroSubtitleText = Color(0xFF78350F),
                urgentCardTop = Color(0xFFC2410C),
                urgentCardBottom = Color(0xFF9A3412),
                statusSuccess = StatusSuccessGreen,
                statusSuccessBg = Color(0xFFDCFCE7),
                statusPending = StatusPendingAmber,
                statusPendingBg = Color(0xFFFEF3C7),
                statusUrgent = StatusUrgentRed,
                statusUrgentBg = Color(0xFFFEE2E2),
                tagYieldBg = TagYieldBg,
                tagYieldText = TagYieldText,
                tagLivestockBg = TagLivestockBg,
                tagLivestockText = TagLivestockText,
                tagFinanceBg = TagFinanceBg,
                tagFinanceText = TagFinanceText,
                tagHRBg = TagHRBg,
                tagHRText = TagHRText,
                topBarContainer = MkulimaSurface,
                bottomNavContainer = MkulimaSurface,
                navSelectedIcon = ForestGreenPrimary,
                navUnselectedIcon = Color(0xFF78716C),
                navIndicator = Color(0xFFFEF3C7),
                chartLine = ForestGreenPrimary,
                chartFillTop = ForestGreenPrimary.copy(alpha = 0.35f),
                chartFillBottom = ForestGreenPrimary.copy(alpha = 0.05f),
                isDark = false
            )
            Pair(scheme, custom)
        }

        "TERRACOTTA" -> {
            val scheme = lightColorScheme(
                primary = TerracottaPrimary,
                onPrimary = Color.White,
                primaryContainer = TerracottaLightBg,
                onPrimaryContainer = TerracottaOnContainer,
                secondary = TerracottaDark,
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFFFEDD5),
                onSecondaryContainer = Color(0xFF9A3412),
                tertiary = StatusPendingAmber,
                background = TerracottaBackground,
                onBackground = TerracottaTextPrimary,
                surface = TerracottaSurface,
                onSurface = TerracottaTextPrimary,
                surfaceVariant = Color(0xFFFFF7ED),
                onSurfaceVariant = TerracottaTextSecondary,
                outline = TerracottaBorder,
                error = StatusUrgentRed
            )
            val custom = MkulimaColors(
                primary = TerracottaPrimary,
                primaryVariant = TerracottaDark,
                primaryContainer = TerracottaLightBg,
                onPrimaryContainer = TerracottaOnContainer,
                secondary = TerracottaDark,
                secondaryContainer = Color(0xFFFFEDD5),
                onSecondaryContainer = Color(0xFF9A3412),
                tertiary = StatusPendingAmber,
                background = TerracottaBackground,
                surface = TerracottaSurface,
                surfaceVariant = Color(0xFFFFF7ED),
                cardBackground = TerracottaSurface,
                cardBorder = TerracottaBorder,
                textPrimary = TerracottaTextPrimary,
                textSecondary = TerracottaTextSecondary,
                textMuted = Color(0xFF7C6255),
                heroTop = TerracottaHeroTop,
                heroBottom = TerracottaHeroBottom,
                heroText = Color(0xFF431407),
                heroSubtitleText = Color(0xFF7C2D12),
                urgentCardTop = Color(0xFF9A3412),
                urgentCardBottom = Color(0xFF7C2D12),
                statusSuccess = StatusSuccessGreen,
                statusSuccessBg = Color(0xFFDCFCE7),
                statusPending = StatusPendingAmber,
                statusPendingBg = Color(0xFFFEF3C7),
                statusUrgent = StatusUrgentRed,
                statusUrgentBg = Color(0xFFFEE2E2),
                tagYieldBg = TagYieldBg,
                tagYieldText = TagYieldText,
                tagLivestockBg = TagLivestockBg,
                tagLivestockText = TagLivestockText,
                tagFinanceBg = TagFinanceBg,
                tagFinanceText = TagFinanceText,
                tagHRBg = TagHRBg,
                tagHRText = TagHRText,
                topBarContainer = TerracottaSurface,
                bottomNavContainer = TerracottaSurface,
                navSelectedIcon = TerracottaPrimary,
                navUnselectedIcon = Color(0xFF7C6255),
                navIndicator = Color(0xFFFFEDD5),
                chartLine = TerracottaPrimary,
                chartFillTop = TerracottaPrimary.copy(alpha = 0.35f),
                chartFillBottom = TerracottaPrimary.copy(alpha = 0.05f),
                isDark = false
            )
            Pair(scheme, custom)
        }

        "OCEAN" -> {
            val scheme = lightColorScheme(
                primary = OceanPrimary,
                onPrimary = Color.White,
                primaryContainer = OceanLight,
                onPrimaryContainer = OceanOnContainer,
                secondary = OceanDark,
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFE0F2FE),
                onSecondaryContainer = Color(0xFF0369A1),
                tertiary = Color(0xFF0D9488),
                background = OceanBackground,
                onBackground = OceanTextPrimary,
                surface = OceanSurface,
                onSurface = OceanTextPrimary,
                surfaceVariant = Color(0xFFF0F9FF),
                onSurfaceVariant = OceanTextSecondary,
                outline = OceanBorder,
                error = StatusUrgentRed
            )
            val custom = MkulimaColors(
                primary = OceanPrimary,
                primaryVariant = OceanDark,
                primaryContainer = OceanLight,
                onPrimaryContainer = OceanOnContainer,
                secondary = OceanDark,
                secondaryContainer = Color(0xFFE0F2FE),
                onSecondaryContainer = Color(0xFF0369A1),
                tertiary = Color(0xFF0D9488),
                background = OceanBackground,
                surface = OceanSurface,
                surfaceVariant = Color(0xFFF0F9FF),
                cardBackground = OceanSurface,
                cardBorder = OceanBorder,
                textPrimary = OceanTextPrimary,
                textSecondary = OceanTextSecondary,
                textMuted = Color(0xFF64748B),
                heroTop = OceanHeroTop,
                heroBottom = OceanHeroBottom,
                heroText = Color(0xFF082F49),
                heroSubtitleText = Color(0xFF0369A1),
                urgentCardTop = Color(0xFF0284C7),
                urgentCardBottom = Color(0xFF0369A1),
                statusSuccess = StatusSuccessGreen,
                statusSuccessBg = Color(0xFFDCFCE7),
                statusPending = StatusPendingAmber,
                statusPendingBg = Color(0xFFFEF3C7),
                statusUrgent = StatusUrgentRed,
                statusUrgentBg = Color(0xFFFEE2E2),
                tagYieldBg = Color(0xFFE0F2FE),
                tagYieldText = Color(0xFF0369A1),
                tagLivestockBg = TagLivestockBg,
                tagLivestockText = TagLivestockText,
                tagFinanceBg = TagFinanceBg,
                tagFinanceText = TagFinanceText,
                tagHRBg = TagHRBg,
                tagHRText = TagHRText,
                topBarContainer = OceanSurface,
                bottomNavContainer = OceanSurface,
                navSelectedIcon = OceanPrimary,
                navUnselectedIcon = Color(0xFF64748B),
                navIndicator = Color(0xFFE0F2FE),
                chartLine = OceanPrimary,
                chartFillTop = OceanPrimary.copy(alpha = 0.35f),
                chartFillBottom = OceanPrimary.copy(alpha = 0.05f),
                isDark = false
            )
            Pair(scheme, custom)
        }

        "COFFEE" -> {
            val scheme = lightColorScheme(
                primary = CoffeePrimary,
                onPrimary = Color.White,
                primaryContainer = CoffeeLight,
                onPrimaryContainer = CoffeeOnContainer,
                secondary = CoffeeDark,
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFFCE7D0),
                onSecondaryContainer = Color(0xFF451A03),
                tertiary = StatusPendingAmber,
                background = CoffeeBackground,
                onBackground = CoffeeTextPrimary,
                surface = CoffeeSurface,
                onSurface = CoffeeTextPrimary,
                surfaceVariant = Color(0xFFFDF4EB),
                onSurfaceVariant = CoffeeTextSecondary,
                outline = CoffeeBorder,
                error = StatusUrgentRed
            )
            val custom = MkulimaColors(
                primary = CoffeePrimary,
                primaryVariant = CoffeeDark,
                primaryContainer = CoffeeLight,
                onPrimaryContainer = CoffeeOnContainer,
                secondary = CoffeeDark,
                secondaryContainer = Color(0xFFFCE7D0),
                onSecondaryContainer = Color(0xFF451A03),
                tertiary = StatusPendingAmber,
                background = CoffeeBackground,
                surface = CoffeeSurface,
                surfaceVariant = Color(0xFFFDF4EB),
                cardBackground = CoffeeSurface,
                cardBorder = CoffeeBorder,
                textPrimary = CoffeeTextPrimary,
                textSecondary = CoffeeTextSecondary,
                textMuted = Color(0xFF6B584C),
                heroTop = CoffeeHeroTop,
                heroBottom = CoffeeHeroBottom,
                heroText = Color(0xFF231710),
                heroSubtitleText = Color(0xFF451A03),
                urgentCardTop = Color(0xFF78350F),
                urgentCardBottom = Color(0xFF451A03),
                statusSuccess = StatusSuccessGreen,
                statusSuccessBg = Color(0xFFDCFCE7),
                statusPending = StatusPendingAmber,
                statusPendingBg = Color(0xFFFEF3C7),
                statusUrgent = StatusUrgentRed,
                statusUrgentBg = Color(0xFFFEE2E2),
                tagYieldBg = TagYieldBg,
                tagYieldText = TagYieldText,
                tagLivestockBg = TagLivestockBg,
                tagLivestockText = TagLivestockText,
                tagFinanceBg = TagFinanceBg,
                tagFinanceText = TagFinanceText,
                tagHRBg = TagHRBg,
                tagHRText = TagHRText,
                topBarContainer = CoffeeSurface,
                bottomNavContainer = CoffeeSurface,
                navSelectedIcon = CoffeePrimary,
                navUnselectedIcon = Color(0xFF6B584C),
                navIndicator = Color(0xFFFCE7D0),
                chartLine = CoffeePrimary,
                chartFillTop = CoffeePrimary.copy(alpha = 0.35f),
                chartFillBottom = CoffeePrimary.copy(alpha = 0.05f),
                isDark = false
            )
            Pair(scheme, custom)
        }

        else -> {
            // Default: EMERALD / LIGHT
            val scheme = lightColorScheme(
                primary = EmeraldPrimary,
                onPrimary = Color.White,
                primaryContainer = EmeraldLight,
                onPrimaryContainer = EmeraldOnContainer,
                secondary = EmeraldDark,
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFDCFCE7),
                onSecondaryContainer = Color(0xFF14532D),
                tertiary = TerracottaPrimary,
                background = EmeraldBackground,
                onBackground = EmeraldTextPrimary,
                surface = EmeraldSurface,
                onSurface = EmeraldTextPrimary,
                surfaceVariant = Color(0xFFE8F5E9),
                onSurfaceVariant = EmeraldTextSecondary,
                outline = EmeraldBorder,
                error = StatusUrgentRed
            )
            Pair(scheme, DefaultMkulimaColors)
        }
    }
}

@Composable
fun MkulimaTheme(
    themeMode: String = "SYSTEM",
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val (colorScheme, mkulimaColors) = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            val isDark = if (themeMode.equals("DARK", ignoreCase = true)) true else if (themeMode.equals("LIGHT", ignoreCase = true)) false else isSystemDark
            val dynamicScheme = if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            val custom = if (isDark) resolveAppTheme("DARK", true).second else resolveAppTheme("EMERALD", false).second
            Pair(dynamicScheme, custom)
        }
        else -> resolveAppTheme(themeMode, isSystemDark)
    }

    CompositionLocalProvider(LocalMkulimaColors provides mkulimaColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

@Composable
fun MyApplicationTheme(
    themeMode: String = "SYSTEM",
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MkulimaTheme(themeMode = themeMode, dynamicColor = dynamicColor, content = content)
}



