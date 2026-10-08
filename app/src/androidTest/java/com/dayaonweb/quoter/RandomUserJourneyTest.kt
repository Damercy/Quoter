package com.dayaonweb.quoter

import android.content.Intent
import android.util.Log
import androidx.datastore.preferences.core.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.*
import com.dayaonweb.quoter.data.local.settingsDatastore
import com.dayaonweb.quoter.domain.broadcast.ReminderScheduler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

/** Reproducible mixed-use journeys against the real Activity, Room, preferences and system chooser.
 * No app-data clearing, external sharing, ratings or notification scheduling are performed.
 */
@RunWith(AndroidJUnit4::class)
class RandomUserJourneyTest {
    private val pkg = "com.dayaonweb.quoter"
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val launch = Intent().setClassName(pkg, "$pkg.presentation.view.ui.MainActivity")
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    private enum class Step { AUTHOR, EMPTY_SEARCH, SAVE_RETURN, CATEGORIES, THEME, GLASS, REMINDER_CANCEL, SHARE_CANCEL, ROTATE, BACKGROUND, SWIPE }

    @Test fun mixedJourneySeed42() = journey(42)
    @Test fun mixedJourneySeed9271() = journey(9271)
    @Test fun mixedJourneySeed108() = journey(108)

    private fun journey(seed: Int) {
        val before = runBlocking { context.settingsDatastore.data.first() }
        val random = Random(seed)
        try {
            // Prevent native review requests during test-only Activity relaunches; preserve the original preference.
            runBlocking { context.settingsDatastore.edit { it[booleanPreferencesKey("REVIEW_REQUESTS")] = false } }
            uiAutomator {
                startActivityIntent(launch)
                reader()
                val plan = Step.entries.shuffled(random) + List(4) { Step.entries.random(random) }
                plan.forEachIndexed { index, step ->
                    Log.i("QuoterJourney", "seed=$seed step=$index action=$step")
                    when (step) {
                        Step.AUTHOR -> {
                            desc("Browse quotes").click()
                            onElement { isEditable && packageName?.toString() == pkg }.apply { click(); setText("Einstein") }
                            hideKeyboard()
                            val result = onElement { textAsString() == "Albert Einstein" }
                            result.click()
                            reader()
                            onElement { textAsString().orEmpty().contains("EINSTEIN") }
                            label("Quotes").click()
                            reader()
                        }
                        Step.EMPTY_SEARCH -> {
                            desc("Browse quotes").click()
                            onElement { isEditable && packageName?.toString() == pkg }.apply { click(); setText("zz-no-such-quote-$seed") }
                            hideKeyboard()
                            onElement { textAsString().orEmpty().startsWith("No quotes found") }
                            desc("Clear search").click()
                            hideKeyboard()
                            device.pressBack()
                            reader()
                        }
                        Step.CATEGORIES -> {
                            desc("Browse quotes").click()
                            onElementOrNull(300) { contentDescription?.toString() == "Clear search" }?.click()
                            listOf("Action", "All", "Abundance", "All", "Action").shuffled(random).forEach { category ->
                                label(category).click()
                                desc("Search quotes or authors")
                                assertNull("Phone Browse has no redundant close control", onElementOrNull(0) { contentDescription?.toString() == "Close browse" })
                            }
                            label("All").click()
                            device.pressBack()
                            reader()
                        }
                        Step.SAVE_RETURN -> {
                            val original = onElement { textAsString().orEmpty().matches(Regex("\\d+ / \\d+")) }.text
                            val initiallySaved = onElementOrNull(300) { contentDescription?.toString() == "Unsave quote" } != null
                            if (!initiallySaved) desc("Save quote").click()
                            desc("Unsave quote")
                            label("Saved").click()
                            reader()
                            desc("Unsave quote")
                            label("Quotes").click()
                            reader()
                            onElement { textAsString() == original }
                            // Quotes retains its own position; restore the original saved state.
                            if (!initiallySaved) { desc("Unsave quote").click(); desc("Save quote") }
                        }
                        Step.THEME -> {
                            openSettings()
                            val theme = if (random.nextBoolean()) "Light" else "Dark"
                            label(theme).click()
                            onElement { textAsString() == theme && parent?.isChecked == true }
                            device.pressBack()
                            reader()
                        }
                        Step.GLASS -> {
                            openSettings()
                            val toggle = desc("Glass appearance")
                            val wasOn = toggle.parent.isChecked
                            toggle.click()
                            onElement { contentDescription?.toString() == "Glass appearance" && parent?.isChecked != wasOn }
                            // Save + restore by actual Activity recreation, not a test-state fake.
                            device.pressHome()
                            startActivityIntent(launch)
                            reader()
                            openSettings()
                            assertEquals(!wasOn, desc("Glass appearance").parent.isChecked)
                            device.pressBack()
                            reader()
                        }
                        Step.REMINDER_CANCEL -> {
                            openSettings()
                            onElement { isScrollable }.scrollToElement(Direction.DOWN) { textAsString() == "Time" }.click()
                            label("Reminder time")
                            assertTrue("Both sheet actions must be reachable", label("Cancel").visibleBounds.height() > 0 && label("Done").visibleBounds.height() > 0)
                            label("Cancel").click()
                            onElement { textAsString() == "Time" }
                            device.pressBack()
                            reader()
                        }
                        Step.SHARE_CANCEL -> {
                            desc("Share quote").click()
                            // Assert Android owns the chooser. Do not choose any recipient.
                            onElement(15000) { packageName?.toString() in setOf("android", "com.android.intentresolver") &&
                                (textAsString() == "Share quote" || textAsString().orEmpty().contains("Shared with Quoter")) }
                            device.pressBack()
                            reader()
                            assertNull("Chooser must not reopen on return", onElementOrNull(300) { packageName?.toString() == "com.android.intentresolver" })
                        }
                        Step.ROTATE -> {
                            val counter = onElement { textAsString().orEmpty().matches(Regex("\\d+ / \\d+")) }.text
                            device.setOrientationLeft()
                            reader()
                            onElement { textAsString() == counter }
                            device.setOrientationNatural()
                            reader()
                            onElement { textAsString() == counter }
                        }
                        Step.BACKGROUND -> {
                            val counter = onElement { textAsString().orEmpty().matches(Regex("\\d+ / \\d+")) }.text
                            device.pressHome()
                            startApp(pkg)
                            reader()
                            onElement { textAsString() == counter }
                        }
                        Step.SWIPE -> {
                            val counter = onElement { textAsString().orEmpty().matches(Regex("\\d+ / \\d+")) }.text.split(" / ").map(String::toInt)
                            val forward = counter[0] == 1 || (counter[0] < counter[1] && random.nextBoolean())
                            onElement { isScrollable }.swipe(if (forward) Direction.LEFT else Direction.RIGHT, 0.65f)
                            onElement { textAsString() == "${counter[0] + if (forward) 1 else -1} / ${counter[1]}" }
                            reader()
                        }
                    }
                    assertEquals("The journey must return to Quoter", pkg, device.currentPackageName)
                    assertNull("No duplicate Browse screen", onElementOrNull(0) { contentDescription?.toString() == "Search quotes or authors" })
                }
            }
        } catch (failure: Throwable) {
            val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            device.takeScreenshot(java.io.File(context.getExternalFilesDir(null), "journey-$seed-failure.png"))
            device.dumpWindowHierarchy(java.io.File(context.getExternalFilesDir(null), "journey-$seed-failure.xml"))
            throw failure
        } finally {
            UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).setOrientationNatural()
            UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).unfreezeRotation()
            runBlocking { context.settingsDatastore.updateData { before } }
            runBlocking { ReminderScheduler(context).reconcile() }
        }
    }

    private fun UiAutomatorTestScope.desc(value: String) = onElement { contentDescription?.toString() == value }
    private fun UiAutomatorTestScope.label(value: String) = onElement { textAsString() == value }
    private fun UiAutomatorTestScope.hideKeyboard() {
        val deadline = android.os.SystemClock.uptimeMillis() + 3000
        while (android.os.SystemClock.uptimeMillis() < deadline) {
            if (windows().any { it.type == android.view.accessibility.AccessibilityWindowInfo.TYPE_INPUT_METHOD }) {
                device.pressBack()
                val hiddenDeadline = android.os.SystemClock.uptimeMillis() + 3000
                while (windows().any { it.type == android.view.accessibility.AccessibilityWindowInfo.TYPE_INPUT_METHOD }
                    && android.os.SystemClock.uptimeMillis() < hiddenDeadline) android.os.SystemClock.sleep(50)
                return
            }
            android.os.SystemClock.sleep(50)
        }
    }
    private fun UiAutomatorTestScope.openSettings() {
        desc("Settings").click()
        label("APPEARANCE").waitForStable(requireStableScreenshot = false)
    }
    private fun UiAutomatorTestScope.reader() {
        desc("Browse quotes")
        desc("Settings")
        desc("Share quote")
        // Wait on accessibility stability, not screenshots: living slider waves deliberately keep drawing.
        label("Quotes").waitForStable(requireStableScreenshot = false)
    }
}
