package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Core Light Theme Tokens
val LightCanvas = Color(0xFFF6F8F5)
val LightSurface = Color(0xFFFFFFFF)
val LightSoftSurface = Color(0xFFEDF2EB)
val LightPrimaryDeepGreen = Color(0xFF163814)
val LightAccentLime = Color(0xFF8FD82B)
val LightSoftLime = Color(0xFFC7F084)
val LightTextBlack = Color(0xFF111811)
val LightMutedGray = Color(0xFF556353)

// Core Dark Theme Tokens
val DarkCanvas = Color(0xFF0C120C)
val DarkSurface = Color(0xFF131B14)
val DarkElevated = Color(0xFF1A261C)
val DarkPrimaryGreen = Color(0xFF9CE870)
val DarkAccentLime = Color(0xFFCCE844)
val DarkSoftLime = Color(0xFF78C946)
val DarkPrimaryText = Color(0xFFF4F7F2)
val DarkSecondaryText = Color(0xFFAAB8A7)

// Status / Severity Tokens
val SeverityLow = Color(0xFF43A047)
val SeverityLowContainerLight = Color(0xFFE8F5E9)
val SeverityLowContainerDark = Color(0xFF1B381D)

val SeverityMedium = Color(0xFFFB8C00)
val SeverityMediumContainerLight = Color(0xFFFFF3E0)
val SeverityMediumContainerDark = Color(0xFF3E280F)

val SeverityHigh = Color(0xFFE53935)
val SeverityHighContainerLight = Color(0xFFFFEBEE)
val SeverityHighContainerDark = Color(0xFF401614)

@Immutable
data class SurakshaColors(
    val canvas: Color,
    val surface: Color,
    val softSurface: Color,
    val primary: Color,
    val onPrimary: Color,
    val accentLime: Color,
    val softLime: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val border: Color,
    val severityLow: Color,
    val severityLowContainer: Color,
    val severityMedium: Color,
    val severityMediumContainer: Color,
    val severityHigh: Color,
    val severityHighContainer: Color,
    val backgroundGradient: List<Color>,
    val isDark: Boolean
) {
    fun getBackgroundBrush(): Brush = Brush.linearGradient(backgroundGradient)
}

val LightSurakshaColors = SurakshaColors(
    canvas = LightCanvas,
    surface = LightSurface,
    softSurface = LightSoftSurface,
    primary = LightPrimaryDeepGreen,
    onPrimary = Color.White,
    accentLime = LightAccentLime,
    softLime = LightSoftLime,
    textPrimary = LightTextBlack,
    textSecondary = LightMutedGray,
    border = Color(0xFFDDE5DC),
    severityLow = SeverityLow,
    severityLowContainer = SeverityLowContainerLight,
    severityMedium = SeverityMedium,
    severityMediumContainer = SeverityMediumContainerLight,
    severityHigh = SeverityHigh,
    severityHighContainer = SeverityHighContainerLight,
    backgroundGradient = listOf(Color(0xFFF6F8F5), Color(0xFFEDF5EA), Color(0xFFE4EEE2)),
    isDark = false
)

val DarkSurakshaColors = SurakshaColors(
    canvas = DarkCanvas,
    surface = DarkSurface,
    softSurface = DarkElevated,
    primary = DarkPrimaryGreen,
    onPrimary = Color(0xFF0C120C),
    accentLime = DarkAccentLime,
    softLime = DarkSoftLime,
    textPrimary = DarkPrimaryText,
    textSecondary = DarkSecondaryText,
    border = Color(0xFF233325),
    severityLow = SeverityLow,
    severityLowContainer = SeverityLowContainerDark,
    severityMedium = SeverityMedium,
    severityMediumContainer = SeverityMediumContainerDark,
    severityHigh = SeverityHigh,
    severityHighContainer = SeverityHighContainerDark,
    backgroundGradient = listOf(Color(0xFF0C120C), Color(0xFF111A12), Color(0xFF162217)),
    isDark = true
)

val LocalSurakshaColors = staticCompositionLocalOf { LightSurakshaColors }
