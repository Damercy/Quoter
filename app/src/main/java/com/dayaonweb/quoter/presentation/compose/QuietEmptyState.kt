package com.dayaonweb.quoter.presentation.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Original vector artwork, kept decorative so the title is the accessible description. */
@Composable
internal fun QuietEmptyState(kind: String, title: String, description: String, action: String? = null,
    onAction: () -> Unit = {}) {
    val ink = MaterialTheme.colorScheme.onSurfaceVariant
    Column(Modifier.widthIn(max = 340.dp).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(Modifier.size(72.dp)) {
            val unit = size.width / 72f
            val stroke = Stroke(2.4f * unit, cap = StrokeCap.Round)
            when (kind) {
                "search" -> {
                    drawCircle(ink.copy(alpha = .06f), 23f * unit, Offset(29f * unit, 29f * unit))
                    drawCircle(ink.copy(alpha = .7f), 20f * unit, Offset(29f * unit, 29f * unit), style = stroke)
                    drawLine(ink.copy(alpha = .7f), Offset(44f * unit,44f * unit), Offset(61f * unit,61f * unit), stroke.width, StrokeCap.Round)
                }
                "speaker" -> {
                    val speaker = Path().apply {
                        moveTo(13f * unit,27f * unit); lineTo(25f * unit,27f * unit); lineTo(40f * unit,16f * unit)
                        lineTo(40f * unit,56f * unit); lineTo(25f * unit,45f * unit); lineTo(13f * unit,45f * unit); close()
                    }
                    drawPath(speaker, ink.copy(alpha = .06f)); drawPath(speaker, ink.copy(alpha = .7f), style = stroke)
                    drawArc(ink.copy(alpha = .5f), -55f, 110f, false, Offset(37f * unit,22f * unit), Size(22f * unit,28f * unit), style = stroke)
                }
                else -> {
                    val bookmark = Path().apply {
                        moveTo(23f * unit,12f * unit); lineTo(49f * unit,12f * unit)
                        quadraticTo(53f * unit,12f * unit,53f * unit,16f * unit); lineTo(53f * unit,61f * unit)
                        lineTo(36f * unit,49f * unit); lineTo(19f * unit,61f * unit); lineTo(19f * unit,16f * unit)
                        quadraticTo(19f * unit,12f * unit,23f * unit,12f * unit); close()
                    }
                    drawPath(bookmark, ink.copy(alpha = .06f)); drawPath(bookmark, ink.copy(alpha = .7f), style = stroke)
                    drawLine(ink.copy(alpha = .3f), Offset(27f * unit,24f * unit), Offset(45f * unit,24f * unit), stroke.width, StrokeCap.Round)
                }
            }
        }
        Text(title, Modifier.padding(top = 16.dp), fontFamily = quoterFont, fontWeight = FontWeight.Bold,
            fontSize = 19.sp, textAlign = TextAlign.Center)
        Text(description, Modifier.padding(top = 8.dp), fontFamily = quoterFont, fontSize = 14.sp,
            lineHeight = 20.sp, color = ink, textAlign = TextAlign.Center)
        if (action != null) MicroTextButton(onClick = onAction, modifier = Modifier.padding(top = 8.dp)) {
            Text(action, fontFamily = quoterFont, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}
