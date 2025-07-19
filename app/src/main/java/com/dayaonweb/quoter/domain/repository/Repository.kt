package com.dayaonweb.quoter.domain.repository

import com.dayaonweb.quoter.data.local.models.Preferences
import com.dayaonweb.quoter.data.remote.model.RandomQuotesListingResponseItem

interface Repository {
    suspend fun fetchQuotesByTag(tag: String, pageNo: Int): List<RandomQuotesListingResponseItem>
    suspend fun fetchQuotesTag(): List<String>
    suspend fun getUserPreferences(): Preferences
}