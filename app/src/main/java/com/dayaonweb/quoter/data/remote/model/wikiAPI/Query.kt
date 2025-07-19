package com.dayaonweb.quoter.data.remote.model.wikiAPI


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Query(
    @Json(name = "normalized")
    val normalized: List<Normalized>?,
    @Json(name = "pages")
    val pages: Map<Int, PageContent>?
)