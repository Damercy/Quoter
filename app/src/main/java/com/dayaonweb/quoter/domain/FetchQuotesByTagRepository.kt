package com.dayaonweb.quoter.domain

import com.dayaonweb.quoter.service.model.RandomQuotesListingResponseItem

interface FetchQuotesByTagRepository {
    suspend fun fetchQuotesByTag(tag: String, pageNo: Int): List<RandomQuotesListingResponseItem>
}