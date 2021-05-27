package com.dayaonweb.quoter.service.repository

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dayaonweb.quoter.service.model.Quote
import retrofit2.HttpException
import java.io.IOException

private const val QUOTES_STARTING_INDEX = 0
private const val TAG = "QuotesPagingSource"

class QuotesPagingSource(
    private val quotesApi: QuoteService
) : PagingSource<Int, Quote>() {
    override fun getRefreshKey(state: PagingState<Int, Quote>): Int? {
        Log.d(TAG, "getRefreshKey: Called")
        return null
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Quote> {
        val position = params.key ?: QUOTES_STARTING_INDEX
        Log.d(TAG, "load: page=$position loadSize=${params.loadSize}")
        return try {
            val response =
                quotesApi.getQuotes(page = position, skip = position*20)
            val quotes = response.results
            Log.d(TAG, "load: quotes size=${quotes.size}")
            LoadResult.Page(
                data = quotes,
                nextKey = if (quotes.isEmpty()) null else position + 1,
                prevKey = if (position == QUOTES_STARTING_INDEX) null else position - 1
            )
        } catch (exception: IOException) {
            LoadResult.Error(exception)
        } catch (exception: HttpException) {
            LoadResult.Error(exception)
        }
    }
}