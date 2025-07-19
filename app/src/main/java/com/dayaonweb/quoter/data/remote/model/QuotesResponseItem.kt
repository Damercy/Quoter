package com.dayaonweb.quoter.data.remote.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.serialization.Serializable

@Serializable
@JsonClass(generateAdapter = true)
data class QuotesResponseItem(
    @Json(name = "quotes")
    val quotes: List<Quote?>?
)