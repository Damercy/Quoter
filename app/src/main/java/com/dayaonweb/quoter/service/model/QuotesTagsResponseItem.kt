package com.dayaonweb.quoter.service.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class QuotesTagsResponseItem(
    @Json(name = "dateAdded")
    val dateAdded: String,
    @Json(name = "dateModified")
    val dateModified: String,
    @Json(name = "_id")
    val id: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "quoteCount")
    val quoteCount: Int,
    @Json(name = "__v")
    val v: Int? = null
)