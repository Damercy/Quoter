package com.dayaonweb.quoter.presentation.compose

import androidx.compose.runtime.*
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner

/** Layoutlib has no Activity dispatcher. Runtime continues to use ComponentActivity's owner. */
@Composable
internal fun QuoterPreviewContent(state: QuoterState) {
    val owner = remember { object : NavigationEventDispatcherOwner {
        override val navigationEventDispatcher = NavigationEventDispatcher()
    } }
    DisposableEffect(owner) { onDispose { owner.navigationEventDispatcher.dispose() } }
    CompositionLocalProvider(LocalNavigationEventDispatcherOwner provides owner) { QuoterScreen(state) }
}
