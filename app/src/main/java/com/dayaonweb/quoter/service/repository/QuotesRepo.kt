package com.dayaonweb.quoter.service.repository

import android.util.Log
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
        Log.d(TAG, "getQuotes: Called!")
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

}