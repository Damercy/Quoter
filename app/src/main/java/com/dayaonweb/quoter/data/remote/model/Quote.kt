package com.dayaonweb.quoter.data.remote.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.serialization.Serializable

@Serializable
@JsonClass(generateAdapter = true)
data class Quote(
    @Json(name = "author")
    val author: Author?,
    @Json(name = "content")
    val content: String?,
    @Json(name = "id")
    val id: String?,
    @Json(name = "tags")
    val tags: List<Tag?>?
)