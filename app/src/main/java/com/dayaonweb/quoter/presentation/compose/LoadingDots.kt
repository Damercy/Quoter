package com.dayaonweb.quoter.presentation.compose

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

/** The original three-dot loading rhythm, drawn natively without Lottie. */
@Composable
internal fun LoadingDots() {
    val motion = rememberMotionEnabled()
    val phase = if (motion) {
        val transition = rememberInfiniteTransition(label = "Loading dots")
        val position by transition.animateFloat(0f, (2 * PI).toFloat(), infiniteRepeatable(tween(1000, easing = LinearEasing)), label = "Dot wave")
        position
    } else 0f
    val ink = MaterialTheme.colorScheme.onSurface
    Canvas(Modifier.size(60.dp, 24.dp).semantics { contentDescription = "Loading quotes" }) {
        repeat(3) { index ->
            val lift = if (motion) ((sin(phase - index * 0.8f) + 1f) / 2f) else 0f
            drawCircle(ink.copy(alpha = 0.4f + lift * 0.6f), 3.dp.toPx(),
                Offset(size.width / 2f + (index - 1) * 16.dp.toPx(), size.height / 2f - lift * 4.dp.toPx()))
        }
    }
}
