package com.dayaonweb.quoter

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.room.Room
import androidx.lifecycle.ViewModelStore
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.dayaonweb.quoter.data.local.QuoteDatabase
import com.dayaonweb.quoter.data.repository.QuotesRepoImpl
import com.dayaonweb.quoter.domain.analytics.Analytics
import com.dayaonweb.quoter.domain.analytics.QuoteEvent
import com.dayaonweb.quoter.domain.models.UiQuote
import com.dayaonweb.quoter.presentation.compose.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.util.Collections

@RunWith(AndroidJUnit4::class)
class AnalyticsJourneyTest {
    @get:Rule val compose = createComposeRule()
    private val events = Collections.synchronizedList(mutableListOf<QuoteEvent>())
    private val quote = UiQuote("analytics-fixture", "Keep a quiet moment.", "Example Author", listOf("life"))
    @Before fun capture() { Analytics.testSink = { events.add(it) } }
    @After fun stopCapture() { Analytics.testSink = null }

    @Test fun recompositionDoesNotInflateViewsAndShareRestorationDoesNotReplay() {
        val state = mutableStateOf(QuoterState(listOf(quote)))
        val restoration = StateRestorationTester(compose)
        restoration.setContent { QuoterScreen(state.value) }
        compose.waitForIdle()
        assertEquals(1, events.count { it == QuoteEvent.VIEW })
        compose.runOnIdle { state.value = state.value.copy(sourceStatus = "Offline collection ready") }
        compose.waitForIdle()
        assertEquals(1, events.count { it == QuoteEvent.VIEW })
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        // Block outbound sharing: opening a chooser is measurable; sending is not inferred.
        val chooser = instrumentation.addMonitor(IntentFilter(Intent.ACTION_CHOOSER), null, true)
        try {
            compose.onNodeWithContentDescription("Share quote").performClick()
            compose.waitUntil(10000) { chooser.hits == 1 && events.count { it == QuoteEvent.SHARE_SHEET } == 1 }
            restoration.emulateSavedInstanceStateRestore()
            compose.waitForIdle()
            assertEquals(1, chooser.hits)
            assertEquals(1, events.count { it == QuoteEvent.SHARE_SHEET })
        } finally { instrumentation.removeMonitor(chooser) }
    }

    @Test fun rapidRepeatedSaveRequestsCountOnlyPersistedChanges() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, QuoteDatabase::class.java).build()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val store = ViewModelStore()
        val repo = QuotesRepoImpl(context, database, OkHttpClient(), scope, startAutomatically = false)
        val model = QuoterViewModel(context, repo)
        try {
            compose.runOnIdle {
                store.put("analytics", model)
                repeat(5) { model.save(quote.id, true) }
            }
            compose.waitUntil(10000) { events.count { it == QuoteEvent.SAVE } == 1 }
            assertTrue(quote.id in repo.savedIds.first())
            compose.runOnIdle { repeat(5) { model.save(quote.id, false) } }
            compose.waitUntil(10000) { events.count { it == QuoteEvent.UNSAVE } == 1 }
            compose.runOnIdle { repeat(5) { model.save(quote.id, false) } }
            compose.waitForIdle()
            assertFalse(quote.id in repo.savedIds.first())
            assertEquals(1, events.count { it == QuoteEvent.SAVE })
            assertEquals(1, events.count { it == QuoteEvent.UNSAVE })
        } finally {
            compose.runOnIdle { store.clear() }
            scope.cancel()
            database.close()
        }
    }
}
