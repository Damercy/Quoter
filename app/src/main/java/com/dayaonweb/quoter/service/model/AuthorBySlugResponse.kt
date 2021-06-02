package com.dayaonweb.quoter.service.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AuthorBySlugResponse(
    @Json(name = "count")
    val count: Int?,
    @Json(name = "lastItemIndex")
    val lastItemIndex: Int?,
    @Json(name = "page")
    val page: Int?,
    @Json(name = "results")
    val results: List<Result>?,
    @Json(name = "totalCount")
    val totalCount: Int?,
    @Json(name = "totalPages")
    val totalPages: Int?
)