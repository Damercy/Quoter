package com.dayaonweb.quoter.domain.repository

import com.dayaonweb.quoter.data.local.models.Preferences
import com.dayaonweb.quoter.data.remote.model.Quote

interface Repository {
    suspend fun fetchQuotesByTag(tag: String, pageNo: Int): List<Quote>
    suspend fun fetchQuotesTag(): List<String>
    suspend fun getUserPreferences(): Preferences
}