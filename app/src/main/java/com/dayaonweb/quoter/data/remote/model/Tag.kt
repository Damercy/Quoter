package com.dayaonweb.quoter.data.remote.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.serialization.Serializable

@Serializable
@JsonClass(generateAdapter = true)
data class Tag(
    @Json(name = "id")
    val id: String?,
    @Json(name = "name")
    val name: String?
)