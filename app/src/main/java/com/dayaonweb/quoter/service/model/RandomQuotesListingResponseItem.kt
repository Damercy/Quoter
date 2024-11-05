package com.dayaonweb.quoter.service.model

data class RandomQuotesListingResponseItem(
    val author: String? = null,
    val id: Int? = null,
    val length: Int? = null,
    val quote: String? = null,
    val tags: List<String>? = null
)