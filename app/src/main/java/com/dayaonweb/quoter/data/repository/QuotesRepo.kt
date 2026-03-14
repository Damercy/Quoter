package com.dayaonweb.quoter.data.repository

import com.dayaonweb.quoter.domain.models.UiQuote
import kotlinx.coroutines.flow.Flow

interface QuotesRepo {
    fun getTags(): Flow<List<String>>
    fun getQuotesByTags(tags: List<String>, pageNo: Int): Flow<List<UiQuote>>
}