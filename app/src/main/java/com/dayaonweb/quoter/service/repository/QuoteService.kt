package com.dayaonweb.quoter.service.repository

import com.dayaonweb.quoter.service.model.AuthorBySlugResponse
import com.dayaonweb.quoter.service.model.Quotes
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

    // https://en.wikipedia.org/w/api.php?action=query&prop=pageimages&format=json&piprop=original&titles={authorName}&pilicense=any ---> Get author image
}