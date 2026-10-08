package com.dayaonweb.quoter.presentation.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.foundation.LocalIndication
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color

private val quoterTypography = Typography().let { t ->
    t.copy(displayLarge = t.displayLarge.copy(fontFamily = quoterFont), displayMedium = t.displayMedium.copy(fontFamily = quoterFont),
        displaySmall = t.displaySmall.copy(fontFamily = quoterFont), headlineLarge = t.headlineLarge.copy(fontFamily = quoterFont),
        headlineMedium = t.headlineMedium.copy(fontFamily = quoterFont), headlineSmall = t.headlineSmall.copy(fontFamily = quoterFont),
        titleLarge = t.titleLarge.copy(fontFamily = quoterFont), titleMedium = t.titleMedium.copy(fontFamily = quoterFont),
        titleSmall = t.titleSmall.copy(fontFamily = quoterFont), bodyLarge = t.bodyLarge.copy(fontFamily = quoterFont),
        bodyMedium = t.bodyMedium.copy(fontFamily = quoterFont), bodySmall = t.bodySmall.copy(fontFamily = quoterFont),
        labelLarge = t.labelLarge.copy(fontFamily = quoterFont), labelMedium = t.labelMedium.copy(fontFamily = quoterFont),
        labelSmall = t.labelSmall.copy(fontFamily = quoterFont))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun QuoterTheme(theme: String, content: @Composable () -> Unit) {
    val colors = palette(theme == "Dark" || theme == "System" && isSystemInDarkTheme())
    val motion = rememberMotionEnabled()
    MaterialTheme(colorScheme = colors.copy(primary = themeColor(colors.primary, motion), onPrimary = themeColor(colors.onPrimary, motion),
        secondary = themeColor(colors.secondary, motion), onSecondary = themeColor(colors.onSecondary, motion),
        surface = themeColor(colors.surface, motion), onSurface = themeColor(colors.onSurface, motion),
        onSurfaceVariant = themeColor(colors.onSurfaceVariant, motion), surfaceContainer = themeColor(colors.surfaceContainer, motion),
        background = themeColor(colors.background, motion), onBackground = themeColor(colors.onBackground, motion),
        outline = themeColor(colors.outline, motion), outlineVariant = themeColor(colors.outlineVariant, motion)),
        typography = quoterTypography) {
        CompositionLocalProvider(LocalRippleConfiguration provides null,
            LocalIndication provides QuietIndication, content = content)
    }
}

@Composable private fun themeColor(target: Color, motion: Boolean): Color {
    val color by animateColorAsState(target, tween(if (motion) 220 else 0), label = "Theme color")
    return color
}
