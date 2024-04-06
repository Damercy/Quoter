package com.dayaonweb.quoter.service.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Pagination(
    @Json(name = "currentPage")
    val currentPage: Int? = null,
    @Json(name = "nextPage")
    val nextPage: Int? = null,
    @Json(name = "totalPages")
    val totalPages: Int? = null
)