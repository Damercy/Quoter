package com.dayaonweb.quoter.data.remote.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.serialization.Serializable

@Serializable
@JsonClass(generateAdapter = true)
data class Author(
    @Json(name = "bio")
    val bio: String?,
    @Json(name = "description")
    val description: String?,
    @Json(name = "id")
    val id: String?,
    @Json(name = "link")
    val link: String?,
    @Json(name = "name")
    val name: String?,
    @Json(name = "slug")
    val slug: String?
)