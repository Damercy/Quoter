package com.dayaonweb.quoter.service.model.wikiAPI


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PageContent(
    @Json(name = "ns")
    val ns: Int?,
    @Json(name = "original")
    val original: Original?,
    @Json(name = "pageid")
    val pageId: Int?,
    @Json(name = "title")
    val title: String?
)