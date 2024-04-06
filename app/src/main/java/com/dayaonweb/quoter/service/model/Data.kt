package com.dayaonweb.quoter.service.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Data(
    @Json(name = "_id")
    val id: String? = null,
    @Json(name = "quoteAuthor")
    val quoteAuthor: String? = null,
    @Json(name = "quoteGenre")
    val quoteGenre: String? = null,
    @Json(name = "quoteText")
    val quoteText: String? = null,
    @Json(name = "__v")
    val v: Int? = null
)