package com.dayaonweb.quoter.analytics

import com.dayaonweb.quoter.BuildConfig
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.ktx.Firebase

object Analytics{

    private var analytics = Firebase.analytics

    init {
        analytics.setAnalyticsCollectionEnabled(!BuildConfig.DEBUG)
    }

    fun trackQuoteShare(quote: String, quoteId: String, quoteTags: List<String>) {
        analytics.logEvent(FirebaseAnalytics.Event.SHARE) {
            param("shared_quote", if (quote.length >= 145) quote.substring(0, 100) else quote)
            param("shared_quote_id", quoteId)
            param("shared_quote_tags", quoteTags.toString())
        }
    }

    fun trackQuoteCopy(quote: String, quoteId: String, quoteTags: List<String>) {
        analytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
            param("copied_quote", if (quote.length >= 145) quote.substring(0, 100) else quote)
            param("copied_quote_id", quoteId)
            param("copied_quote_tags", quoteTags.toString())
        }
    }

    fun trackAppReview(){
        analytics.logEvent(FirebaseAnalytics.Event.POST_SCORE){
            param("rated","true")
        }
    }
}