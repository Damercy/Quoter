package com.dayaonweb.quoter.presentation.compose

import android.app.ActivityManager
import android.content.Context
import android.database.ContentObserver
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import kotlin.math.PI
import kotlin.math.sin

internal val LocalGlassBackdrop = staticCompositionLocalOf<Backdrop?> { null }
internal data class GlassStyle(val enabled: Boolean = true, val transparency: Float = 0.6f)
internal val LocalGlassStyle = compositionLocalOf { GlassStyle() }
internal val LocalIconPressed = compositionLocalOf { false }
internal val LocalIconActivation = compositionLocalOf { 0 }

@Composable
internal fun rememberMotionEnabled(): Boolean {
    if (LocalInspectionMode.current) return false
    val resolver = LocalContext.current.contentResolver
    fun enabled() = Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
    var motion by remember { mutableStateOf(enabled()) }
    DisposableEffect(resolver) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) { motion = enabled() }
        }
        resolver.registerContentObserver(Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE), false, observer)
        onDispose { resolver.unregisterContentObserver(observer) }
    }
    return motion
}

/** Real backdrop blur/refraction where supported; a light surface on older/low-RAM devices. */
@Composable
internal fun Modifier.glassSurface(pressed: Boolean = false): Modifier {
    val style = LocalGlassStyle.current
    if (!style.enabled) return this
    val context = LocalContext.current
    val backdrop = LocalGlassBackdrop.current
    val paper = MaterialTheme.colorScheme.surface
    val ink = MaterialTheme.colorScheme.onSurface
    val dark = paper.red < 0.5f
    val opacity = 1f - style.transparency.coerceIn(0f,1f)
    val tint = MaterialTheme.colorScheme.surfaceContainer
    val shape = RoundedCornerShape(50)
    val preview = LocalInspectionMode.current
    val capable = remember(context, preview) {
        !preview && Build.VERSION.SDK_INT >= 31 && !context.getSystemService(ActivityManager::class.java).isLowRamDevice
    }
    val surface = if (capable && backdrop != null) drawBackdrop(backdrop, shape = { shape }, effects = {
        blur(4.dp.toPx())
        if (Build.VERSION.SDK_INT >= 33) lens(8.dp.toPx(), 12.dp.toPx(), chromaticAberration = false)
    }, onDrawSurface = {
        drawRect(tint.copy(alpha = opacity))
        drawRect(Brush.verticalGradient(listOf(Color.White.copy(alpha = (if (dark) 0.03f + opacity * 0.15f else 0.12f + opacity * 0.6f) + if (pressed) 0.06f else 0f), Color.Transparent)))
    }) else background(tint.copy(alpha = opacity), shape)
    return surface.border(0.75.dp, Brush.linearGradient(listOf(
        if (dark) Color.White.copy(alpha = 0.1f + opacity * 0.45f) else Color.White.copy(alpha = 0.35f + opacity * 0.65f),
        ink.copy(alpha = if (dark) 0.05f else 0.12f),
        Color.White.copy(alpha = if (dark) 0.12f else 0.8f))), shape)
}

@Composable
internal fun GlassIconButton(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
    content: @Composable () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    var activation by remember { mutableIntStateOf(0) }
    val motion = rememberMotionEnabled()
    val scale by animateFloatAsState(if (pressed && motion) 0.94f else 1f,
        spring(dampingRatio = 0.72f, stiffness = 850f), label = "Glass press")
    val haptic = rememberQuoterHaptic()
    var lastClick by remember { mutableLongStateOf(-1000L) }
    Box(modifier.size(48.dp).clickable(enabled = enabled, role = androidx.compose.ui.semantics.Role.Button,
            interactionSource = interaction, indication = null) {
            val now = android.os.SystemClock.uptimeMillis()
            if (now - lastClick >= 240) { lastClick = now; haptic(false); activation++; onClick() }
        }.padding(4.dp).graphicsLayer {
            // Auto clips the overflowing glass shadow when disabled alpha creates an offscreen layer.
            compositingStrategy = CompositingStrategy.ModulateAlpha
            clip = false
            scaleX = scale; scaleY = scale; alpha = if (enabled) 1f else 0.38f
        }
        .glassSurface(pressed), contentAlignment = androidx.compose.ui.Alignment.Center) {
        CompositionLocalProvider(LocalIconPressed provides pressed, LocalIconActivation provides activation, content = content)
    }
}

/** Material 3 retains drag, focus, keyboard and accessibility semantics; only its track is custom. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WavySlider(value: Float, onValueChange: (Float) -> Unit, onValueChangeFinished: () -> Unit,
    description: String, valueRange: ClosedFloatingPointRange<Float> = 0f..1f, steps: Int = 0, enabled: Boolean = true) {
    val interaction = remember { MutableInteractionSource() }
    val dragged by interaction.collectIsDraggedAsState()
    val motion = rememberMotionEnabled()
    val amplitude by animateFloatAsState(if (!motion) 0f else if (dragged) 5f else 2f,
        spring(dampingRatio = 0.62f, stiffness = 850f), label = "Slider wave")
    val lifecycle = androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycle
    val lifecycleState by lifecycle.currentStateFlow.collectAsState()
    val phase = remember { mutableFloatStateOf(0f) }
    val animateWave = motion && enabled && lifecycleState.isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)
    LaunchedEffect(animateWave, dragged) {
        if (!animateWave) { phase.floatValue = 0f; return@LaunchedEffect }
        var previous = 0L
        while (true) withInfiniteAnimationFrameNanos { now ->
            if (now - previous >= if (dragged) 16_000_000L else 33_000_000L) {
                phase.floatValue = (now % 2_400_000_000L) / 2_400_000_000f * (2 * PI).toFloat()
                previous = now
            }
        }
    }
    val active = MaterialTheme.colorScheme.onSurface
    val inactive = MaterialTheme.colorScheme.outlineVariant
    val haptic = rememberQuoterHaptic()
    val bins = if (steps > 0) steps + 1 else 20
    fun bin(v: Float) = kotlin.math.round((v - valueRange.start) / (valueRange.endInclusive - valueRange.start) * bins).toInt()
    var lastBin by remember { mutableIntStateOf(bin(value)) }
    LaunchedEffect(value) { lastBin = bin(value) }
    Slider(value = value, onValueChange = { next ->
        val nextBin = bin(next)
        if (nextBin != lastBin) { haptic(true); lastBin = nextBin }
        onValueChange(next)
    }, onValueChangeFinished = onValueChangeFinished,
        modifier = Modifier.semantics { contentDescription = description }, enabled = enabled,
        valueRange = valueRange, steps = steps, interactionSource = interaction,
        thumb = { Spacer(Modifier.size(8.dp, 32.dp).background(active.copy(alpha = if (enabled) 1f else 0.38f), RoundedCornerShape(50))) },
        track = { slider -> Canvas(Modifier.fillMaxWidth().height(24.dp)) {
            val fraction = ((slider.value - valueRange.start) / (valueRange.endInclusive - valueRange.start)).coerceIn(0f,1f)
            val end = size.width * fraction
            val center = size.height / 2f
            drawLine(inactive, Offset(0f,center), Offset(size.width,center), 4.dp.toPx(), StrokeCap.Round)
            val path = Path()
            val height = amplitude.dp.toPx()
            val wavelength = 24.dp.toPx()
            val samples = (end / 2.dp.toPx()).toInt().coerceAtLeast(1)
            for (step in 0..samples) {
                val x = end * step / samples
                val envelope = (x / 12.dp.toPx()).coerceIn(0f,1f) * ((end-x) / 12.dp.toPx()).coerceIn(0f,1f)
                val y = center + sin((x / wavelength * 2 * PI + phase.floatValue).toFloat()) * height * envelope
                if (step == 0) path.moveTo(x,y) else path.lineTo(x,y)
            }
            drawPath(path, active.copy(alpha = if (enabled) 1f else 0.38f), style = Stroke(4.dp.toPx(), cap = StrokeCap.Round))
        } })
}

@Composable
internal fun MicroTextButton(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, pressFeedback: Boolean = true,
    content: @Composable RowScope.() -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val motion = rememberMotionEnabled()
    val scale by animateFloatAsState(if (pressed && motion && pressFeedback) 0.96f else 1f,
        spring(dampingRatio = 0.72f, stiffness = 850f), label = "Text control press")
    val haptic = rememberQuoterHaptic()
    var lastClick by remember { mutableLongStateOf(-1000L) }
    TextButton({ val now = android.os.SystemClock.uptimeMillis()
        if (now - lastClick >= 240) { lastClick = now; haptic(false); onClick() }
    }, modifier.graphicsLayer { scaleX = scale; scaleY = scale }, enabled,
        interactionSource = interaction, content = content)
}
