package com.dayaonweb.quoter.domain.analytics

/** Aggregate actions only: quote contents and reader preferences stay on device. */
enum class QuoteEvent(val eventName: String) {
    VIEW("quote_view"),
    SAVE("quote_save"),
    UNSAVE("quote_unsave"),
    LISTEN_START("quote_listen_start"),
    LISTEN_COMPLETE("quote_listen_complete"),
    SHARE_SHEET("quote_share_sheet_open"),
    REMINDER_ENABLED("reminder_enabled"),
    REMINDER_DISABLED("reminder_disabled"),
    REMINDER_TIME_CHANGED("reminder_time_changed"),
    REVIEW_REQUEST("review_request")
}
