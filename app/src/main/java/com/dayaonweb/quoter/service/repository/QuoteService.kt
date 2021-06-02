package com.dayaonweb.quoter.service.repository

import com.dayaonweb.quoter.service.model.AuthorBySlugResponse
import com.dayaonweb.quoter.service.model.Quotes
import com.dayaonweb.quoter.service.model.wikiAPI.WikiApiImageResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface QuoteService {

    @GET("quotes")
    suspend fun getQuotes(
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("skip") skip: Int? = null,
        @Query("maxLength") maxLength: Int? = null,
        @Query("minLength") minLength: Int? = null,
        @Query("tags") tags: List<String>? = null,
        @Query("author") author: String? = null,
        @Query("authorId") authorId: String? = null,
    ): Quotes

    @GET("authors")
    suspend fun getAuthorBySlug(
        @Query("slug") slug: String,
        @Query("limit") limit: Int? = null,
        @Query("page") page: Int? = null,
    ): AuthorBySlugResponse


    // Only to be called from wikiApi
    @GET("api.php")
    suspend fun getAuthorImage(
        @Query("action") action: String = "query",
        @Query("prop") prop: String = "pageimages",
        @Query("format") format: String = "json",
        @Query("piprop") piProp: String = "original",
        @Query("titles") authorName: String,
        @Query("pilicense") license: String = "any",
    ): WikiApiImageResponse
}