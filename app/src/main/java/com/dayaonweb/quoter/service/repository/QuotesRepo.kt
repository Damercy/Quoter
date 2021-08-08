package com.dayaonweb.quoter.service.repository

import com.dayaonweb.quoter.service.QuotesClient


object QuotesRepo {
    private val api = QuotesClient().api


    suspend fun getAllQuoteTags() = api.getAllTags()

    suspend fun getQuotesByTags(tags: List<String>, pageNo: Int) =
        api.getQuotes(tags = tags, page = pageNo)

}