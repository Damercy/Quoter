package com.dayaonweb.quoter.service.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RandomQuotesListingResponseItem(
    @Json(name = "author")
    val author: String? = null,
    @Json(name="id")
    val id: Int? = null,
    @Json(name = "length")
    val length: Int? = null,
    @Json(name = "quote")
    val quote: String? = null,
    @Json(name = "tags")
    val tags: List<String>? = null
)