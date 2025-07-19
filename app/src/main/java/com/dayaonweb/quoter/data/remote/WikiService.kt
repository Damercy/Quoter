package com.dayaonweb.quoter.data.remote

import com.dayaonweb.quoter.data.remote.model.wikiAPI.WikiApiImageResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WikiService {
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