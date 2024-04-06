package com.dayaonweb.quoter.service.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AllQuotesGenreResponse(
    @Json(name = "data")
    val `data`: List<String>? = listOf(),
    @Json(name = "message")
    val message: String? = "",
    @Json(name = "pagination")
    val pagination: Pagination? = Pagination(),
    @Json(name = "statusCode")
    val statusCode: Int? = 0,
    @Json(name = "totalQuotes")
    val totalQuotes: Any? = Any()
)