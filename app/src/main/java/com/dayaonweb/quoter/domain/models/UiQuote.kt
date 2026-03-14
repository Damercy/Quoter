package com.dayaonweb.quoter.domain.models

import java.util.UUID

data class UiQuote(
    val id: String = UUID.randomUUID().toString(),
    val quote: String,
    val author: String,
    val tags: List<String>,
    val authorImage: String? = null,
    val quoteLength: Int = quote.length
)
