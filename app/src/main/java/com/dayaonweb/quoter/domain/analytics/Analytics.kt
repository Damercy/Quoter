package com.dayaonweb.quoter.domain.analytics

import android.content.Context
import com.dayaonweb.quoter.BuildConfig
import com.google.firebase.analytics.FirebaseAnalytics

object Analytics {

    private var analytics: FirebaseAnalytics? = null
    @Volatile internal var testSink: ((QuoteEvent) -> Unit)? = null

    fun init(context: Context) {
        if (!BuildConfig.USE_FIREBASE) return
        analytics = FirebaseAnalytics.getInstance(context.applicationContext)
        analytics?.setAnalyticsCollectionEnabled(!BuildConfig.DEBUG)
    }

    fun record(event: QuoteEvent) {
        if (BuildConfig.DEBUG) testSink?.invoke(event)
        else analytics?.logEvent(event.eventName, null)
    }
}
