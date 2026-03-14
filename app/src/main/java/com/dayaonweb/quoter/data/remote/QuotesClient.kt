package com.dayaonweb.quoter.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Inject

class QuotesClient @Inject constructor(
    httpClient: OkHttpClient
) {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://quoteslate.vercel.app/api/")
        .client(httpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    private val wikiRetrofit = Retrofit.Builder()
        .baseUrl("https://en.wikipedia.org/w/")
        .client(httpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val api: QuoteService = retrofit.create(QuoteService::class.java)
    val wikiApi: QuoteService = wikiRetrofit.create(QuoteService::class.java)


}