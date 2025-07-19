package com.dayaonweb.quoter.data.remote


import com.dayaonweb.quoter.data.remote.model.AllTagsResponseItem
import com.dayaonweb.quoter.data.remote.model.QuotesResponseItem
import retrofit2.http.GET
import retrofit2.http.Query

interface QuoteService {

    @GET("quotes/random")
    suspend fun getQuotes(
        @Query("authors") authors: String? = null,
        @Query("tags") tags: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
    ): QuotesResponseItem?


    @GET("tags")
    suspend fun getAllGenres(): List<AllTagsResponseItem?>
}