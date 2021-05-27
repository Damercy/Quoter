package com.dayaonweb.quoter.service.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Quote(
    @Json(name = "author")
    val author: String,
    @Json(name = "authorSlug")
    val authorSlug: String,
    @Json(name = "content")
    val content: String,
    @Json(name = "_id")
    val id: String,
    @Json(name = "length")
    val length: Int,
    @Json(name = "tags")
    val tags: List<String>
)