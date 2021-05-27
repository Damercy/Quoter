package com.dayaonweb.quoter.service.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Quotes(
    @Json(name = "count")
    val count: Int,
    @Json(name = "lastItemIndex")
    val lastItemIndex: Int,
    @Json(name = "results")
    val results: List<Quote>,
    @Json(name = "totalCount")
    val totalCount: Int
)