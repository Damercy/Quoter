package com.dayaonweb.quoter.service.repository

import com.dayaonweb.quoter.service.QuotesClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


object QuotesRepo {
    private val api = QuotesClient().api

    suspend fun getAllQuoteTags() = withContext(Dispatchers.IO){
        api.getAllGenres()
    }

    suspend fun getQuotesByTags(tags: List<String>, pageNo: Int) = withContext(Dispatchers.IO){
        println("xxx ${tags.joinToString()}")
        api.getQuotes(page = pageNo, tags = tags.joinToString())
    }

}