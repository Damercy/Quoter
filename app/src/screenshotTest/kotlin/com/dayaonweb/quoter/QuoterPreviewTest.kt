package com.dayaonweb.quoter

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.dayaonweb.quoter.domain.models.UiQuote
import com.dayaonweb.quoter.presentation.compose.QuoterPreviewContent
import com.dayaonweb.quoter.presentation.compose.QuoterState
import com.dayaonweb.quoter.presentation.compose.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier

@PreviewTest
@Preview(name = "Phone", widthDp = 360, heightDp = 800)
@Preview(name = "Landscape", widthDp = 800, heightDp = 360)
@Preview(name = "Unfolded", widthDp = 840, heightDp = 720)
@Preview(name = "Tablet", widthDp = 1200, heightDp = 800)
@Preview(name = "Desktop", widthDp = 1600, heightDp = 900)
@Preview(name = "Large text", widthDp = 360, heightDp = 800, fontScale = 2f)
@Composable
fun ReaderSizes() {
    QuoterPreviewContent(QuoterState(quotes = listOf(UiQuote("preview", "Stay hungry, stay foolish.", "Steve Jobs", listOf("inspiration")))))
}

@PreviewTest
@Preview(name = "Settings light", widthDp = 360, heightDp = 800)
@Preview(name = "Settings dark", widthDp = 360, heightDp = 800, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Settings large text", widthDp = 360, heightDp = 800, fontScale = 2f)
@Composable
fun SettingsSizes() {
    QuoterTheme("System") { Surface(Modifier.fillMaxSize()) {
        SettingsContent(AppSettings(), QuoterActions(), 0.6f, {}, {}, {}, "Reminders are off", {}, {})
    } }
}

@PreviewTest
@Preview(name = "Empty saved", widthDp = 360, heightDp = 600)
@Preview(name = "Empty saved dark", widthDp = 360, heightDp = 600, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Empty saved large text", widthDp = 360, heightDp = 600, fontScale = 2f)
@Composable
fun EmptySavedSizes() {
    QuoterTheme("System") { Surface(Modifier.fillMaxSize()) {
        Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
            QuietEmptyState("bookmark", "No saved quotes", "Save quotes to find them here.", "Explore quotes")
        }
    } }
}
