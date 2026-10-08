package com.dayaonweb.quoter

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.window.layout.FoldingFeature.Orientation
import androidx.window.layout.FoldingFeature.State
import androidx.window.testing.layout.FoldingFeature
import androidx.window.testing.layout.TestWindowLayoutInfo
import androidx.window.testing.layout.WindowLayoutInfoPublisherRule
import com.dayaonweb.quoter.domain.models.UiQuote
import com.dayaonweb.quoter.presentation.compose.*
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FoldableScreenTest {
    @get:Rule(order = 1) val compose = createAndroidComposeRule<ComponentActivity>()
    @get:Rule(order = 2) val window = WindowLayoutInfoPublisherRule()
    private val quotes = listOf(UiQuote("one", "Leave room for a little quiet.", "First Author", listOf("life")),
        UiQuote("two", "Keep learning and moving forward.", "Second Author", listOf("wisdom")))

    @Test fun browseStaysBesideReaderAndSelectionSurvivesClosing() {
        assumeTrue("Two-pane test requires an unfolded/tablet window", compose.activity.resources.configuration.screenWidthDp >= 720)
        compose.setContent { QuoterScreen(QuoterState(quotes)) }
        val hinge = FoldingFeature(compose.activity, state = State.HALF_OPENED, orientation = Orientation.VERTICAL, size = 20)
        window.overrideWindowLayoutInfo(TestWindowLayoutInfo(listOf(hinge)))
        compose.waitForIdle()
        val reader = compose.onNodeWithText("Leave room for a little quiet.").fetchSemanticsNode().boundsInWindow
        assertTrue("Reader text must not cross the hinge", reader.right <= hinge.bounds.left || reader.left >= hinge.bounds.right)
        compose.onNodeWithContentDescription("Browse quotes").performClick()
        compose.onNodeWithContentDescription("Close browse").assertIsDisplayed()
        compose.onNodeWithContentDescription("Search quotes or authors").performTextInput("Second Author")
        compose.onNode(hasText("Keep learning and moving forward.") and hasClickAction()).performClick()
        compose.onNodeWithText("SECOND AUTHOR").assertIsDisplayed()
        compose.onNodeWithContentDescription("Search quotes or authors").assertIsDisplayed()
        compose.onNodeWithContentDescription("Close browse").performClick()
        compose.onNodeWithContentDescription("Search quotes or authors").assertDoesNotExist()
        compose.onNodeWithText("SECOND AUTHOR").assertIsDisplayed()
    }

    @Test fun tabletopReaderAndReminderSheetAvoidSeparatingHinge() {
        assumeTrue("Posture test requires an unfolded/tablet window", compose.activity.resources.configuration.screenWidthDp >= 720)
        compose.setContent { QuoterScreen(QuoterState(quotes)) }
        val hinge = FoldingFeature(compose.activity, state = State.HALF_OPENED, orientation = Orientation.HORIZONTAL, size = 20)
        window.overrideWindowLayoutInfo(TestWindowLayoutInfo(listOf(hinge)))
        compose.waitForIdle()
        val reader = compose.onNodeWithText("Leave room for a little quiet.").fetchSemanticsNode().boundsInWindow
        assertTrue("Reader must occupy a contiguous side", reader.bottom <= hinge.bounds.top || reader.top >= hinge.bounds.bottom)
        compose.onNodeWithContentDescription("Settings").performClick()
        compose.onNodeWithText("9:00").performScrollTo().performClick()
        val sheet = compose.onNodeWithText("Reminder time").fetchSemanticsNode().boundsInWindow
        assertTrue("Sheet title must avoid the fold", sheet.bottom <= hinge.bounds.top || sheet.top >= hinge.bounds.bottom)
    }

    @Test fun settingsReplacesBrowseBesideReaderWithoutMovingToolbarOrStacking() {
        assumeTrue("Supporting pane needs an unfolded/tablet window", compose.activity.resources.configuration.screenWidthDp >= 720)
        compose.setContent { QuoterScreen(QuoterState(quotes)) }
        val hinge = FoldingFeature(compose.activity, state = State.HALF_OPENED, orientation = Orientation.VERTICAL, size = 20)
        window.overrideWindowLayoutInfo(TestWindowLayoutInfo(listOf(hinge)))
        compose.waitForIdle()
        compose.onNodeWithContentDescription("Browse quotes").performClick()
        compose.onNodeWithContentDescription("Close browse").assertIsDisplayed()
        val toolbar = compose.onNodeWithContentDescription("Settings").fetchSemanticsNode().boundsInWindow
        compose.onNodeWithContentDescription("Settings").performClick()
        compose.onNodeWithContentDescription("Close settings").assertIsDisplayed()
        compose.onNodeWithContentDescription("Search quotes or authors").assertDoesNotExist()
        compose.onNodeWithText("Leave room for a little quiet.").assertIsDisplayed()
        val settingsPosition = compose.onNodeWithContentDescription("Settings").fetchSemanticsNode().boundsInWindow
        assertTrue("Toolbar must stay anchored when changing panes", toolbar == settingsPosition)
        val setting = compose.onNodeWithText("Light").fetchSemanticsNode().boundsInWindow
        assertTrue("Settings controls must avoid the hinge", setting.right <= hinge.bounds.left || setting.left >= hinge.bounds.right)
        compose.onNodeWithContentDescription("Settings").performClick()
        compose.onNodeWithContentDescription("Close settings").performClick()
        compose.onNodeWithContentDescription("Close settings").assertDoesNotExist()
        compose.onNodeWithText("Leave room for a little quiet.").assertIsDisplayed()
    }
}
