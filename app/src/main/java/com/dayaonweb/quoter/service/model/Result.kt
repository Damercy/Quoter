package com.dayaonweb.quoter.service.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Result(
    @Json(name = "bio")
    val bio: String,
    @Json(name = "description")
    val description: String,
    @Json(name = "_id")
    val id: String,
    @Json(name = "link")
    val link: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "quoteCount")
    val quoteCount: Int,
    @Json(name = "slug")
    val slug: String
)