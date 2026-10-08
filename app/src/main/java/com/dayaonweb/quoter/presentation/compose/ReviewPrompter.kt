package com.dayaonweb.quoter.presentation.compose

import androidx.activity.ComponentActivity
import androidx.datastore.preferences.core.*
import androidx.lifecycle.Lifecycle
import com.dayaonweb.quoter.data.local.settingsDatastore
import com.google.android.play.core.review.ReviewManagerFactory

internal object ReviewLaunchPolicy {
    fun isDue(launches: Long) = launches >= 2 && launches and (launches - 1) == 0L
    fun increment(launches: Long) = launches.coerceIn(0, Long.MAX_VALUE - 1) + 1
}

internal class ReviewPrompter(private val activity: ComponentActivity) {
    suspend fun claimLaunch(): Boolean {
        var due = false
        activity.settingsDatastore.edit { values ->
            val launches = ReviewLaunchPolicy.increment(values[longPreferencesKey("APP_LAUNCH_COUNT")] ?: 0)
            values[longPreferencesKey("APP_LAUNCH_COUNT")] = launches
            if (values[booleanPreferencesKey("REVIEW_REQUESTS")] != false && ReviewLaunchPolicy.isDue(launches)) {
                // This records an attempt, never a submitted rating (Play does not expose that).
                due = true
            }
        }
        return due
    }
    fun request() {
        val manager = ReviewManagerFactory.create(activity)
        manager.requestReviewFlow().addOnCompleteListener(activity) { task ->
            if (task.isSuccessful && !activity.isFinishing && activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
                manager.launchReviewFlow(activity, task.result)
            // Failures/quota silently retain the normal app flow.
        }
    }
}
