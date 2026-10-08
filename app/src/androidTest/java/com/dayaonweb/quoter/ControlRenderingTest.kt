package com.dayaonweb.quoter

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import android.graphics.Bitmap
import com.dayaonweb.quoter.presentation.compose.*
import com.kyant.backdrop.backdrops.rememberCanvasBackdrop
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ControlRenderingTest {
    @get:Rule val compose = createComposeRule()

    @Test fun plainIconAndTextPressesDoNotPaintRipple() {
        compose.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                QuoterTheme("Light") {
                    CompositionLocalProvider(LocalGlassStyle provides GlassStyle(enabled = false)) {
                        Surface(Modifier.size(220.dp).testTag("fixture")) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                GlassIconButton(onClick = {}) { LineIcon("share", "Share") }
                                MicroTextButton(onClick = {}, pressFeedback = false) { Text("Browse") }
                            }
                        }
                    }
                }
            }
        }
        listOf(compose.onNodeWithContentDescription("Share"), compose.onNodeWithText("Browse")).forEach { button ->
            val before = compose.onNodeWithTag("fixture").captureToImage().toPixelMap()
            button.performTouchInput { down(center) }
            compose.mainClock.advanceTimeBy(300)
            val held = compose.onNodeWithTag("fixture").captureToImage().toPixelMap()
            try {
                for (y in 0 until before.height) for (x in 0 until before.width) {
                    assertEquals("A held plain control must not paint a ripple at $x,$y", before[x,y], held[x,y])
                }
            } finally { button.performTouchInput { up() } }
        }
    }

    @Test fun disabledShareKeepsShadowOutsideItsDrawingBounds() {
        val enabled = mutableStateOf(true)
        compose.setContent {
            QuoterTheme("Light") {
                val paper = MaterialTheme.colorScheme.surface
                val backdrop = rememberCanvasBackdrop { drawRect(paper) }
                CompositionLocalProvider(LocalGlassBackdrop provides backdrop,
                    LocalGlassStyle provides GlassStyle(enabled = true, transparency = .75f)) {
                    Surface(Modifier.size(180.dp).testTag("fixture")) {
                        Box(contentAlignment = Alignment.Center) {
                            GlassIconButton(enabled = enabled.value, onClick = {}, modifier = Modifier.testTag("share")) { LineIcon("share", "Share") }
                        }
                    }
                }
            }
        }
        val control = compose.onNodeWithTag("share").fetchSemanticsNode().boundsInWindow
        val x = (control.left - 2).toInt()
        val top = control.center.y.toInt()
        fun shadowDarkness(name: String): Float {
            compose.waitForIdle()
            // Capture the real compositor: Compose's forced-redraw capture times out on this software renderer.
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            var frame: Bitmap? = null
            val centerX = control.center.x.toInt()
            val centerY = control.center.y.toInt()
            val halfIcon = (control.width * .18f).toInt()
            compose.waitUntil(10_000) {
                val candidate = checkNotNull(instrumentation.uiAutomation.takeScreenshot())
                val ink = (centerY-halfIcon..centerY+halfIcon).minOf { y ->
                    (centerX-halfIcon..centerX+halfIcon).minOf { x -> android.graphics.Color.red(candidate.getPixel(x,y)) / 255f }
                }
                // Accessibility can update before the compositor. Reject blank and previous-state frames.
                val ready = if (enabled.value) ink < .2f else ink in .45f.. .9f
                if (ready) frame = candidate else candidate.recycle()
                ready
            }
            val pixels = checkNotNull(frame)
            val root = compose.onNodeWithTag("fixture").fetchSemanticsNode().boundsInWindow
            val crop = Bitmap.createBitmap(pixels, root.left.toInt(), root.top.toInt(), root.width.toInt(), root.height.toInt())
            java.io.File(instrumentation.targetContext.getExternalFilesDir(null), "$name.png").outputStream().use {
                crop.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            return (top until top + 8).maxOf { 1f - android.graphics.Color.red(pixels.getPixel(x,it)) / 255f }
        }
        assertTrue("Fixture must have a visible glass shadow outside its touch bounds", shadowDarkness("share-shadow-enabled") > .002f)
        compose.runOnIdle { enabled.value = false }
        compose.onNodeWithTag("share").assertIsNotEnabled()
        assertTrue("Disabled alpha must retain the overflowing shadow", shadowDarkness("share-shadow-disabled") > .002f)
    }
}
