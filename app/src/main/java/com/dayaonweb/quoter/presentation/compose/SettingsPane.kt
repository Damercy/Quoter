package com.dayaonweb.quoter.presentation.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.backdrops.*

/** Supporting Settings owns its scroll edge; the reader's controls stay anchored above both panes. */
@Composable
internal fun SettingsPane(wide: Boolean, onClose: () -> Unit, content: @Composable () -> Unit) {
    if (!wide) { content(); return }
    var scrolled by remember { mutableStateOf(false) }
    val source = rememberLayerBackdrop { val content = this; clipRect(bottom = 64.dp.toPx()) { content.drawContent() } }
    val backdrop = rememberCombinedBackdrop(requireNotNull(LocalGlassBackdrop.current), source)
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        Box(Modifier.fillMaxSize().then(if (scrolled && LocalGlassStyle.current.enabled) Modifier.layerBackdrop(source) else Modifier)) {
            CompositionLocalProvider(LocalChromeInset provides 48.dp, LocalChromeScrolled provides { scrolled = it }) { content() }
        }
        val alpha by androidx.compose.animation.core.animateFloatAsState(if (scrolled) 1f else 0f,
            androidx.compose.animation.core.tween(if (rememberMotionEnabled()) 160 else 0), label = "Pane title reveal")
        Row(Modifier.fillMaxWidth().chromeSurface(backdrop, scrolled).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Settings", Modifier.weight(1f).graphicsLayer { this.alpha = alpha }, fontFamily = quoterFont,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 16.sp)
            GlassIconButton(onClick = onClose) { LineIcon("close", "Close settings") }
        }
    }
}
