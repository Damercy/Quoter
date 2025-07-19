package com.dayaonweb.quoter.service

import com.dayaonweb.quoter.BuildConfig
import com.dayaonweb.quoter.service.repository.QuoteService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class QuotesClient {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.quotable.io")
        .client(getClient())
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    private val wikiRetrofit = Retrofit.Builder()
        .baseUrl("https://en.wikipedia.org/w/")
        .client(getClient())
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val api: QuoteService = retrofit.create(QuoteService::class.java)
    val wikiApi: QuoteService = wikiRetrofit.create(QuoteService::class.java)


    private fun getClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level =
            if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.HEADERS else HttpLoggingInterceptor.Level.NONE
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .callTimeout(7,TimeUnit.SECONDS)
            .build()
    }
}