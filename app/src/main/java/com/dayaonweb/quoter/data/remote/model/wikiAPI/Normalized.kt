package com.dayaonweb.quoter.data.remote.model.wikiAPI


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Normalized(
    @Json(name = "from")
    val from: String?,
    @Json(name = "to")
    val to: String?
)