package com.dayaonweb.quoter.service.repository

import com.dayaonweb.quoter.service.model.RandomQuotesListingResponse
import com.dayaonweb.quoter.service.model.wikiAPI.WikiApiImageResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface QuoteService {

    @GET("quotes/random")
    suspend fun getQuotes(
        @Query("authors") authors:String? = null,
        @Query("tags") tags: String? = null,
        @Query("page") page: Int = 1,
        @Query("count") limit: Int = 50,
    ): RandomQuotesListingResponse


    @GET("tags")
    suspend fun getAllGenres(): List<String>

    // Only to be called from wikiApi
    @GET("api.php")
    suspend fun getAuthorImage(
        @Query("action") action: String = "query",
        @Query("prop") prop: String = "pageimages",
        @Query("format") format: String = "json",
        @Query("piprop") piProp: String = "thumbnail",
        @Query("titles") authorName: String,
        @Query("pilicense") license: String = "any",
        @Query("pithumbsize") thumbnailSize: Int = 500
    ): WikiApiImageResponse
}