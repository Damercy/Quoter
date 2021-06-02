package com.dayaonweb.quoter.service.model.wikiAPI


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WikiApiImageResponse(
    @Json(name = "batchcomplete")
    val batchComplete: String?,
    @Json(name = "query")
    val query: Query?
)