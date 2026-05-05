package com.carflow.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class CarflowColors(
    val bg: Color,
    val bg1: Color,
    val bg2: Color,
    val line: Color,
    val line2: Color,
    val fg: Color,
    val fg2: Color,
    val fg3: Color,
    val accent: Color,
    val accentInk: Color,
    val danger: Color,
    val warn: Color,
    val ok: Color,
    val card: Color,
    val cardHi: Color,
)

val AcidDarkColors = CarflowColors(
    bg = BgDark,
    bg1 = Bg1Dark,
    bg2 = Bg2Dark,
    line = LineDark,
    line2 = Line2Dark,
    fg = FgDark,
    fg2 = Fg2Dark,
    fg3 = Fg3Dark,
    accent = AccentLime,
    accentInk = AccentInkDark,
    danger = Danger,
    warn = Warn,
    ok = Ok,
    card = CardDark,
    cardHi = CardHiDark,
)

val EditorialWarmColors = CarflowColors(
    bg = BgWarm,
    bg1 = Bg1Warm,
    bg2 = Bg2Warm,
    line = LineWarm,
    line2 = Line2Warm,
    fg = FgWarm,
    fg2 = Fg2Warm,
    fg3 = Fg3Warm,
    accent = AccentTerracotta,
    accentInk = AccentInkWarm,
    danger = Color(0xFFA32A12),
    warn = Color(0xFFB9791A),
    ok = Color(0xFF3D7A4A),
    card = CardWarm,
    cardHi = CardHiWarm,
)

val LocalCarflowColors = staticCompositionLocalOf { AcidDarkColors }

private val DarkScheme = darkColorScheme(
    primary = AccentLime,
    onPrimary = AccentInkDark,
    background = BgDark,
    surface = CardDark,
    onBackground = FgDark,
    onSurface = FgDark,
    error = Danger,
)

private val LightScheme = lightColorScheme(
    primary = AccentTerracotta,
    onPrimary = AccentInkWarm,
    background = BgWarm,
    surface = CardWarm,
    onBackground = FgWarm,
    onSurface = FgWarm,
    error = Color(0xFFA32A12),
)

@Composable
fun CarflowTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkScheme else LightScheme
    val carflowColors = if (darkTheme) AcidDarkColors else EditorialWarmColors

    CompositionLocalProvider(LocalCarflowColors provides carflowColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = CarflowTypography,
            content = content,
        )
    }
}

val carflowColors: CarflowColors
    @Composable get() = LocalCarflowColors.current
