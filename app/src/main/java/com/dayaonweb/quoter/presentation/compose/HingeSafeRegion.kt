package com.dayaonweb.quoter.presentation.compose

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.window.layout.FoldingFeature
import kotlin.math.roundToInt

internal data class PaneRect(val left: Int, val top: Int, val right: Int, val bottom: Int) {
    val width get() = (right-left).coerceAtLeast(0)
    val height get() = (bottom-top).coerceAtLeast(0)
}
internal data class WindowHinge(val bounds: PaneRect, val vertical: Boolean)

/** Choose the largest contiguous region; ties favor the leading/top pane. Supports multiple hinges. */
internal fun safePane(width: Int, height: Int, hinges: List<WindowHinge>): PaneRect {
    var regions = listOf(PaneRect(0,0,width,height))
    for (hinge in hinges) regions = regions.flatMap { pane ->
        val b = hinge.bounds
        if (hinge.vertical && b.left > pane.left && b.right < pane.right && b.bottom > pane.top && b.top < pane.bottom)
            listOf(pane.copy(right=b.left), pane.copy(left=b.right))
        else if (!hinge.vertical && b.top > pane.top && b.bottom < pane.bottom && b.right > pane.left && b.left < pane.right)
            listOf(pane.copy(bottom=b.top), pane.copy(top=b.bottom))
        else listOf(pane)
    }
    return regions.maxByOrNull { it.width.toLong()*it.height } ?: PaneRect(0,0,width,height)
}

@Composable
internal fun HingeSafeRegion(useWholeWindow: Boolean, features: List<FoldingFeature>, content: @Composable () -> Unit) {
    val density = LocalDensity.current
    var origin by remember { mutableStateOf(IntOffset.Zero) }
    BoxWithConstraints(Modifier.fillMaxSize().onGloballyPositioned {
        val position = it.positionInWindow()
        origin = IntOffset(position.x.roundToInt(),position.y.roundToInt())
    }) {
        val width = with(density) { maxWidth.roundToPx() }
        val height = with(density) { maxHeight.roundToPx() }
        val hinges = if (useWholeWindow) emptyList() else features.filter {
            it.isSeparating || it.occlusionType == FoldingFeature.OcclusionType.FULL
        }.map { feature ->
            val b = feature.bounds
            WindowHinge(PaneRect(b.left-origin.x,b.top-origin.y,b.right-origin.x,b.bottom-origin.y), feature.orientation == FoldingFeature.Orientation.VERTICAL)
        }
        val pane = safePane(width,height,hinges)
        Box(Modifier.absoluteOffset { IntOffset(pane.left,pane.top) }.size(
            with(density) { pane.width.toDp() }, with(density) { pane.height.toDp() })) { content() }
    }
}
