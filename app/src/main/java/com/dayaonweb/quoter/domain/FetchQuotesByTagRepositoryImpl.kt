package com.dayaonweb.quoter.domain

import android.content.Context
import com.dayaonweb.quoter.data.local.DataStoreManager
import com.dayaonweb.quoter.service.model.RandomQuotesListingResponseItem
import com.dayaonweb.quoter.service.repository.QuotesRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Collections.emptyList

class FetchQuotesByTagRepositoryImpl(
    private val context: Context
) : FetchQuotesByTagRepository {

    override suspend fun fetchQuotesByTag(
        tag: String,
        pageNo: Int
    ): List<RandomQuotesListingResponseItem> = withContext(Dispatchers.IO) {
        val remoteQuotesOrEmpty = fetchQuotesFromRemote(tag, pageNo)
        remoteQuotesOrEmpty.ifEmpty { fetchQuotesFromLocal(tag, pageNo) }
    }

    private suspend fun fetchQuotesFromRemote(
        tag: String,
        pageNo: Int
    ): List<RandomQuotesListingResponseItem> = withContext(Dispatchers.IO) {
        try {
            val quotes = QuotesRepo.getQuotesByTags(listOf(tag), pageNo).filterNotNull()
            if (quotes.isNotEmpty())
                cacheQuotes(quotes)
            quotes
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun fetchQuotesFromLocal(
        tag: String,
        pageNo: Int
    ): List<RandomQuotesListingResponseItem> = withContext(Dispatchers.IO) {
        emptyList()
    }

    private suspend fun cacheQuotes(quotes: List<RandomQuotesListingResponseItem>) {

    }
}