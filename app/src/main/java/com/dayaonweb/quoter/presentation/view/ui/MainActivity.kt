package com.dayaonweb.quoter.presentation.view.ui

import android.os.Bundle
import android.content.Intent
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.dayaonweb.quoter.domain.analytics.Analytics
import com.dayaonweb.quoter.domain.broadcast.ReminderScheduler
import com.dayaonweb.quoter.presentation.compose.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import androidx.lifecycle.Lifecycle

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val model: QuoterViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        splash.setOnExitAnimationListener { it.remove() }
        Analytics.init(this)
        setContent {
            val state by model.state.collectAsStateWithLifecycle()
            if (state.quotes.isEmpty()) QuoterTheme(state.settings.theme) { Surface(Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { LoadingDots() }
            }
            } else QuoterScreen(state, remember(model) { QuoterActions(model::save, model::theme, model::reminder,
                model::time, model::imageNotification, model::rate, model::voice, model::glass, model::glassTransparency, model::reviewRequests) })
        }
        if (savedInstanceState == null) recordAppLaunch()
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.action == Intent.ACTION_MAIN && Intent.CATEGORY_LAUNCHER in intent.categories.orEmpty()) recordAppLaunch()
    }
    private fun recordAppLaunch() {
        lifecycleScope.launch {
            val review = ReviewPrompter(this@MainActivity)
            val due = review.claimLaunch()
            if (!due || com.dayaonweb.quoter.BuildConfig.DEBUG) return@launch
            model.state.first { it.quotes.isNotEmpty() }
            lifecycle.currentStateFlow.first { it.isAtLeast(Lifecycle.State.RESUMED) }
            review.request()
        }
    }
    override fun onResume() {
        super.onResume()
        lifecycleScope.launch { ReminderScheduler(this@MainActivity).reconcile() }
    }
}
