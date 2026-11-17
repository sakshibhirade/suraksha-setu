package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

private val LightM3Colors = lightColorScheme(
    primary = LightPrimaryDeepGreen,
    onPrimary = Color.White,
    primaryContainer = LightSoftLime,
    onPrimaryContainer = LightPrimaryDeepGreen,
    secondary = LightSoftLime,
    onSecondary = LightPrimaryDeepGreen,
    tertiary = LightAccentLime,
    onTertiary = LightPrimaryDeepGreen,
    background = LightCanvas,
    onBackground = LightTextBlack,
    surface = LightSurface,
    onSurface = LightTextBlack,
    surfaceVariant = LightSoftSurface,
    onSurfaceVariant = LightMutedGray,
    outline = Color(0xFFD0D0CA)
)

private val DarkM3Colors = darkColorScheme(
    primary = DarkPrimaryGreen,
    onPrimary = Color(0xFF0B1009),
    primaryContainer = Color(0xFF1B3012),
    onPrimaryContainer = DarkPrimaryGreen,
    secondary = DarkSoftLime,
    onSecondary = Color(0xFF0B1009),
    tertiary = DarkAccentLime,
    onTertiary = Color(0xFF0B1009),
    background = DarkCanvas,
    onBackground = DarkPrimaryText,
    surface = DarkSurface,
    onSurface = DarkPrimaryText,
    surfaceVariant = DarkElevated,
    onSurfaceVariant = DarkSecondaryText,
    outline = Color(0xFF263325)
)

object SurakshaTheme {
    val colors: SurakshaColors
        @Composable
        @ReadOnlyComposable
        get() = LocalSurakshaColors.current
}

@Composable
fun SurakshaSutraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val surakshaColors = if (darkTheme) DarkSurakshaColors else LightSurakshaColors
    val m3Colors = if (darkTheme) DarkM3Colors else LightM3Colors

    CompositionLocalProvider(LocalSurakshaColors provides surakshaColors) {
        MaterialTheme(
            colorScheme = m3Colors,
            typography = Typography,
            content = content
        )
    }
}
