package com.dayaonweb.quoter.analytics

import android.content.Context
import com.dayaonweb.quoter.BuildConfig
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent

object Analytics {

    private var analytics: FirebaseAnalytics? = null

    fun init(context: Context) {
        analytics = FirebaseAnalytics.getInstance(context.applicationContext)
        if (BuildConfig.DEBUG)
            analytics?.setAnalyticsCollectionEnabled(false)
    }

    fun setUserId(userId: String) = analytics?.setUserId(userId)

    fun trackQuoteShare(quote: String, quoteId: String, quoteTags: List<String>) {
        analytics?.logEvent(FirebaseAnalytics.Event.SHARE) {
            param("shared_quote", if (quote.length >= 145) quote.substring(0, 100) else quote)
            param("shared_quote_id", quoteId)
            param("shared_quote_tags", quoteTags.toString())
        }
    }

    fun trackQuoteCopy(quote: String, quoteId: String, quoteTags: List<String>) {
        analytics?.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
            param("copied_quote", if (quote.length >= 145) quote.substring(0, 100) else quote)
            param("copied_quote_id", quoteId)
            param("copied_quote_tags", quoteTags.toString())
        }
    }
}