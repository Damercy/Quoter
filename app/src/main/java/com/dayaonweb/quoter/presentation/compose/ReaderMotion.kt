package com.dayaonweb.quoter.presentation.compose

import android.os.Build
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Constant font weight and one travelling marker keep both collections in the same place. */
@Composable
internal fun CollectionTabs(savedOnly: Boolean, onQuotes: () -> Unit, onSaved: () -> Unit, modifier: Modifier = Modifier) {
    val motion = rememberMotionEnabled()
    var quotesBounds by remember { mutableStateOf(Rect.Zero) }
    var savedBounds by remember { mutableStateOf(Rect.Zero) }
    val bounds = if (savedOnly) savedBounds else quotesBounds
    val x by animateFloatAsState(bounds.center.x, spring(if (motion) 0.78f else 1f, 650f), label = "Collection marker x")
    val y by animateFloatAsState(bounds.bottom - 3f, spring(if (motion) 0.9f else 1f, 650f), label = "Collection marker y")
    val ink = MaterialTheme.colorScheme.onSurface
    Box(modifier) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf("Quotes" to false, "Saved" to true).forEach { (label, isSaved) ->
                val selected = savedOnly == isSaved
                MicroTextButton(onClick = if (isSaved) onSaved else onQuotes,
                    modifier = Modifier.onGloballyPositioned { if (isSaved) savedBounds = it.boundsInParent() else quotesBounds = it.boundsInParent() }
                        .semantics { this.selected = selected }, pressFeedback = false) {
                    Text(label, fontFamily = quoterFont, fontWeight = FontWeight.Bold, fontSize = 16.sp,
                        color = if (selected) ink else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Canvas(Modifier.matchParentSize()) {
            if (bounds != Rect.Zero) {
                val centerX = if (motion) x else bounds.center.x
                val bottom = if (motion) y else bounds.bottom - 3f
                drawLine(ink, androidx.compose.ui.geometry.Offset(centerX - 9.dp.toPx(), bottom),
                    androidx.compose.ui.geometry.Offset(centerX + 9.dp.toPx(), bottom), 2.dp.toPx(), StrokeCap.Round)
            }
        }
    }
}

/** Blur is cached in a few levels; settled content has no offscreen blur layer. */
@Composable
internal fun rememberMotionBlur(): List<RenderEffect?> {
    val density = androidx.compose.ui.platform.LocalDensity.current.density
    return remember(density) { List(5) { index ->
        if (index == 0 || Build.VERSION.SDK_INT < 31) null else
            android.graphics.RenderEffect.createBlurEffect(index * 0.45f * density, index * 0.45f * density,
                android.graphics.Shader.TileMode.CLAMP).asComposeRenderEffect()
    } }
}

@Composable
internal fun QuoteCounter(current: Int, total: Int, modifier: Modifier = Modifier) {
    val motion = rememberMotionEnabled()
    val blur = rememberMotionBlur()
    val digits = total.toString().length.coerceAtLeast(1)
    val digitWidth = (8f * androidx.compose.ui.platform.LocalDensity.current.fontScale).dp
    val displayed = current.toString().padStart(digits)
    Row(modifier.clearAndSetSemantics { text = AnnotatedString("$current / $total") }, verticalAlignment = Alignment.CenterVertically) {
        displayed.forEachIndexed { index, digit ->
            AnimatedContent(digit, modifier = Modifier.width(digitWidth), transitionSpec = {
                if (!motion) EnterTransition.None.togetherWith(ExitTransition.None) else {
                    val direction = if (targetState > initialState) 1 else -1
                    (slideInVertically(spring(0.86f, 650f)) { it * direction } + fadeIn(tween(160)))
                        .togetherWith(slideOutVertically(spring(0.86f, 650f)) { -it * direction } + fadeOut(tween(120)))
                }.using(SizeTransform(clip = true))
            }, label = "Quote digit $index") { value ->
                val visibility = transition.animateFloat(label = "Digit clarity") { if (it == EnterExitState.Visible) 0f else 1f }
                Text(value.toString(), Modifier.graphicsLayer { renderEffect = blur[(visibility.value * 4).toInt().coerceIn(0, 4)] },
                    fontFamily = quoterFont, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text(" / $total", fontFamily = quoterFont, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
internal fun EmptyQuoteReader(saved: Boolean, onBrowse: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(top = LocalChromeInset.current, bottom = 72.dp), contentAlignment = Alignment.Center) {
        QuietEmptyState(if (saved) "bookmark" else "search", if (saved) "No saved quotes" else "No quotes found",
            if (saved) "Save quotes to find them here." else "Explore another word or topic.", "Explore quotes", onBrowse)
    }
    Box(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp), contentAlignment = Alignment.BottomEnd) {
        QuoteCounter(0, 0, Modifier.heightIn(min = 48.dp).padding(end = 8.dp))
    }
}
