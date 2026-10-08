package com.dayaonweb.quoter.presentation.compose

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material3.MaterialTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur

/** Platform feedback obeys the device's haptic setting, without a vibration permission. */
@Composable
internal fun rememberQuoterHaptic(): (Boolean) -> Unit {
    val view = LocalView.current
    val preview = LocalInspectionMode.current
    return remember(view, preview) { { selection ->
        if (!preview) view.performHapticFeedback(
            if (selection && Build.VERSION.SDK_INT >= 34) HapticFeedbackConstants.SEGMENT_FREQUENT_TICK
            else if (selection) HapticFeedbackConstants.CLOCK_TICK
            else HapticFeedbackConstants.CONTEXT_CLICK)
        Unit
    } }
}

internal val LocalChromeInset = compositionLocalOf { 0.dp }
internal val LocalChromeScrolled = compositionLocalOf<(Boolean) -> Unit> { {} }

@Composable
internal fun QuietTapSounds() {
    val view = LocalView.current
    DisposableEffect(view) {
        val sounds = view.isSoundEffectsEnabled
        view.isSoundEffectsEnabled = false
        onDispose { view.isSoundEffectsEnabled = sounds }
    }
}

@Composable
internal fun Modifier.chromeSurface(backdrop: Backdrop, scrolled: Boolean): Modifier {
    val paper = MaterialTheme.colorScheme.surface
    val glass = LocalGlassStyle.current.enabled
    val alpha by androidx.compose.animation.core.animateFloatAsState(
        if (glass && scrolled) 1f - LocalGlassStyle.current.transparency * 0.3f else 1f,
        androidx.compose.animation.core.tween(if (rememberMotionEnabled()) 180 else 0), label = "Scroll edge frost")
    return if (Build.VERSION.SDK_INT >= 31 && !LocalInspectionMode.current && glass && scrolled)
        drawBackdrop(backdrop, shape = { RectangleShape }, effects = { blur(12.dp.toPx()) }, shadow = null, highlight = null,
            onDrawSurface = { drawRect(paper.copy(alpha = alpha)) })
    else background(paper)
}

@Composable
internal fun CollectionLabel(label: String, selected: Boolean) {
    val opacity by androidx.compose.animation.core.animateFloatAsState(if (selected) 1f else 0f,
        androidx.compose.animation.core.tween(if (rememberMotionEnabled()) 180 else 0), label = "Collection selection")
    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        androidx.compose.material3.Text(label, fontFamily = quoterFont,
            fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal,
            fontSize = androidx.compose.ui.unit.TextUnit(18f, androidx.compose.ui.unit.TextUnitType.Sp),
            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(3.dp))
        Box(Modifier.size(18.dp, 2.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = opacity),
            androidx.compose.foundation.shape.RoundedCornerShape(50)))
    }
}
