package com.dayaonweb.quoter.service

import com.dayaonweb.quoter.service.repository.QuoteService
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class QuotesClient {

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://api.quotable.io/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val api = retrofit.create(QuoteService::class.java)
}