package com.dayaonweb.quoter.service.repository

import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.dayaonweb.quoter.service.QuotesClient
import com.dayaonweb.quoter.service.model.Quote

private const val TAG = "QuotesRepo"

object QuotesRepo {
    private val api = QuotesClient().api

    fun getQuotes(): LiveData<PagingData<Quote>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                maxSize = 30,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                QuotesPagingSource(api)
            }
        ).liveData
    }

    suspend fun getAuthorBySlug(authorSlug: String, limit: Int?, page: Int?) =
        api.getAuthorBySlug(authorSlug, limit, page)

    suspend fun getAllQuoteTags() = api.getAllTags()


    suspend fun getAuthorImageResponse(authorName:String) = QuotesClient().wikiApi.getAuthorImage(authorName = authorName)
}