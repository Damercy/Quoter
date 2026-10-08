package com.dayaonweb.quoter

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dayaonweb.quoter.domain.models.UiQuote
import com.dayaonweb.quoter.presentation.compose.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.espresso.Espresso.pressBack
import androidx.compose.ui.semantics.SemanticsActions
import android.content.Intent
import android.content.IntentFilter
import androidx.test.platform.app.InstrumentationRegistry

@RunWith(AndroidJUnit4::class)
class QuoterScreenTest {
    @get:Rule val compose = createComposeRule()
    private val quotes = listOf(UiQuote("first", "A quiet moment helps us think.", "First Author", listOf("life")),
        UiQuote("second", "Keep learning and moving forward.", "Second Author", listOf("wisdom")))

    @Test fun appearancePreviewsAndAutomaticSurviveRestoration() {
        val state = mutableStateOf(QuoterState(quotes))
        val restoration = StateRestorationTester(compose)
        restoration.setContent { QuoterScreen(state.value, QuoterActions(theme = {
            state.value = state.value.copy(settings = state.value.settings.copy(theme = it))
        })) }
        compose.onNodeWithContentDescription("Settings").performClick()
        compose.onNodeWithText("Dark").performClick().assertIsSelected()
        compose.onNodeWithContentDescription("Automatic").assertIsOff()
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("Dark").assertIsSelected()
        compose.onNodeWithContentDescription("Automatic").performClick().assertIsOn()
        compose.onNodeWithText("Dark").assertIsNotSelected()
        compose.onNodeWithText("Light").performClick().assertIsSelected()
        compose.onNodeWithContentDescription("Automatic").assertIsOff()
    }

    @Test fun saveAndUnsaveUpdatesSavedCollection() {
        val state = mutableStateOf(QuoterState(quotes))
        compose.setContent { QuoterScreen(state.value, QuoterActions(save = { id, saved ->
            state.value = state.value.copy(saved = if (saved) state.value.saved + id else state.value.saved - id)
        })) }
        compose.onNodeWithContentDescription("Save quote").performTouchInput { click() }
        compose.onNodeWithText("Saved").performClick()
        compose.onRoot().printToLog("QuoterTest")
        compose.onNodeWithText("1 / 1").assertIsDisplayed()
        compose.onNodeWithContentDescription("Unsave quote").performTouchInput { click() }
        compose.onNodeWithText("Save quotes to find them here.").assertIsDisplayed()
    }

    @Test fun searchFindsAuthorAndSelectionScopesReader() {
        compose.setContent { QuoterScreen(QuoterState(quotes)) }
        compose.onNodeWithContentDescription("Browse quotes").performClick()
        compose.onNodeWithContentDescription("Search quotes or authors").performTextInput("Second Author")
        compose.onNode(hasText("Keep learning and moving forward.") and hasClickAction()).performClick()
        compose.onRoot().printToLog("QuoterTest")
        compose.onNodeWithText("SECOND AUTHOR").assertIsDisplayed()
        compose.onNodeWithText("1 / 1").assertIsDisplayed()
    }

    @Test fun quotesAndSavedKeepIndependentPositionsAfterRestoration() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent { QuoterScreen(QuoterState(quotes, saved = setOf("first"))) }
        compose.onNodeWithContentDescription("Browse quotes").performClick()
        compose.onNodeWithContentDescription("Search quotes or authors").performTextInput("Second Author")
        compose.onNode(hasText("Keep learning and moving forward.") and hasClickAction()).performClick()
        compose.onNodeWithText("Quotes").performClick()
        compose.onNodeWithText("2 / 2").assertIsDisplayed()
        repeat(3) {
            compose.onNodeWithText("Saved").performClick()
            compose.onNodeWithText("FIRST AUTHOR").assertIsDisplayed()
            compose.onNodeWithText("Quotes").performClick()
            compose.onNodeWithText("SECOND AUTHOR").assertIsDisplayed()
            compose.onNodeWithText("2 / 2").assertIsDisplayed()
        }
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("SECOND AUTHOR").assertIsDisplayed()
        compose.onNodeWithText("2 / 2").assertIsDisplayed()
    }

    @Test fun settingsNavigationRestoresAndDirectShareDoesNotReplay() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent { QuoterScreen(QuoterState(quotes)) }
        compose.onNodeWithContentDescription("Settings").performClick()
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("Daily reminder").performScrollTo().assertIsDisplayed()
        compose.onNodeWithContentDescription("Back to quotes").performClick()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val chooser = instrumentation.addMonitor(IntentFilter(Intent.ACTION_CHOOSER), null, true)
        try {
            compose.onNodeWithContentDescription("Share quote").performClick()
            compose.waitUntil(10000) { chooser.hits == 1 }
            compose.onNodeWithText("Share quote").assertDoesNotExist()
            restoration.emulateSavedInstanceStateRestore()
            compose.waitForIdle()
            org.junit.Assert.assertEquals(1, chooser.hits)
            compose.onNodeWithText("A quiet moment helps us think.").assertIsDisplayed()
        } finally { instrumentation.removeMonitor(chooser) }
    }

    @Test fun repeatedSettingsAndBrowseDoNotAccumulateScreens() {
        compose.setContent { QuoterScreen(QuoterState(quotes)) }
        repeat(3) {
            compose.onNodeWithContentDescription("Settings").performClick()
            compose.onNodeWithText("Daily reminder").performScrollTo().assertIsDisplayed()
            pressBack()
            compose.onNodeWithText("A quiet moment helps us think.").assertIsDisplayed()
            compose.onNodeWithContentDescription("Browse quotes").performClick()
            pressBack()
            compose.onNodeWithContentDescription("Search quotes or authors").assertDoesNotExist()
            compose.onNodeWithText("A quiet moment helps us think.").assertIsDisplayed()
        }
    }

    @Test fun glassPreferenceDisablesEffectsAndRetainsTransparency() {
        val state = mutableStateOf(QuoterState(quotes))
        val restoration = StateRestorationTester(compose)
        restoration.setContent { QuoterScreen(state.value, QuoterActions(
            glass = { state.value = state.value.copy(settings = state.value.settings.copy(glassEnabled = it)) },
            glassTransparency = { state.value = state.value.copy(settings = state.value.settings.copy(glassTransparency = it)) })) }
        compose.onNodeWithContentDescription("Settings").performClick()
        compose.onNodeWithContentDescription("Glass transparency").performScrollTo().performSemanticsAction(SemanticsActions.SetProgress) { it(0.2f) }
        compose.onNodeWithText("20%").performScrollTo().assertIsDisplayed()
        compose.onNodeWithContentDescription("Glass appearance").performScrollTo().performClick().assertIsOff()
        compose.onNodeWithContentDescription("Glass transparency").assertIsNotEnabled()
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("20%").performScrollTo().assertIsDisplayed()
        compose.onNodeWithContentDescription("Glass appearance").assertIsOff()
        compose.onNodeWithContentDescription("Glass appearance").performScrollTo().performClick().assertIsOn()
        compose.onNodeWithContentDescription("Glass transparency").assertIsEnabled()
    }

    @Test fun collectionTabsHaveSelectionSemanticsAndQuotesClearsFilter() {
        compose.setContent { QuoterScreen(QuoterState(quotes)) }
        compose.onNodeWithText("Quotes").assertIsSelected()
        compose.onNodeWithText("Saved").assertIsNotSelected()
        val quotePill = compose.onNodeWithText("Quotes").fetchSemanticsNode().boundsInRoot
        val savedPill = compose.onNodeWithText("Saved").fetchSemanticsNode().boundsInRoot
        org.junit.Assert.assertTrue("Collection tabs must have space between them", savedPill.left > quotePill.right)
        compose.onNodeWithContentDescription("Browse quotes").performClick()
        compose.onNodeWithContentDescription("Search quotes or authors").performTextInput("Second Author")
        compose.onNode(hasText("Keep learning and moving forward.") and hasClickAction()).performClick()
        // Wide layouts retain Browse until explicitly closed.
        compose.onAllNodesWithContentDescription("Close browse").fetchSemanticsNodes().takeIf { it.isNotEmpty() }?.let {
            compose.onNodeWithContentDescription("Close browse").performClick()
        }
        compose.onNodeWithText("1 / 1").assertIsDisplayed()
        compose.onNodeWithText("Quotes").performClick()
        compose.onNodeWithText("2 / 2").assertIsDisplayed()
    }
}
