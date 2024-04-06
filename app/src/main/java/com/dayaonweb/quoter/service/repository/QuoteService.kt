package com.dayaonweb.quoter.service.repository

import com.dayaonweb.quoter.service.model.AllQuotesGenreResponse
import com.dayaonweb.quoter.service.model.AllQuotesResponse
import com.dayaonweb.quoter.service.model.wikiAPI.WikiApiImageResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface QuoteService {

    @GET("quotes")
    suspend fun getQuotes(
        @Query("author") author: String? = null,
        @Query("genre") genre: String? = null,
        @Query("query") query: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
    ): AllQuotesResponse


    @GET("genres")
    suspend fun getAllGenres(): AllQuotesGenreResponse

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