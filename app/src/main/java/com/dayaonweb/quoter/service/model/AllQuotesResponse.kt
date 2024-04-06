package com.dayaonweb.quoter.service.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AllQuotesResponse(
    @Json(name = "data")
    val data: List<Data?>? = null,
    @Json(name = "message")
    val message: String? = null,
    @Json(name = "pagination")
    val pagination: Pagination? = null,
    @Json(name = "statusCode")
    val statusCode: Int? = null,
    @Json(name = "totalQuotes")
    val totalQuotes: Int? = null
)