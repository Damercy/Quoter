package com.dayaonweb.quoter.di

import android.content.Context
import android.speech.tts.TextToSpeech
import com.dayaonweb.quoter.BuildConfig
import com.dayaonweb.quoter.constants.Constants
import com.dayaonweb.quoter.data.remote.QuoteService
import com.dayaonweb.quoter.data.remote.WikiService
import com.dayaonweb.quoter.domain.repository.Repository
import com.dayaonweb.quoter.domain.repository.RepositoryImpl
import com.dayaonweb.quoter.domain.tts.Quoter
import com.dayaonweb.quoter.domain.tts.Speaker
import com.dayaonweb.quoter.view.ui.browse.AllQuotesByTagViewModel
import com.dayaonweb.quoter.view.ui.browsetag.BrowseTagViewModel
import com.dayaonweb.quoter.view.ui.settings.AllSettingsViewModel
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

val appModule = module {
    single<Speaker> {
        Quoter(get())
    }
    single {
        TextToSpeech(
            get(),
            null,
            Constants.DEFAULT_ENGINE
        )
    }
    single<Repository> {
        RepositoryImpl(get(), get())
    }
    single<QuoteService> {
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.QUOTES_BASE_URL)
            .client(get())
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        retrofit.create(QuoteService::class.java)
    }
    single<WikiService> {
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.QUOTES_IMAGE_BASE_URL)
            .client(get())
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        retrofit.create(WikiService::class.java)
    }
    single<OkHttpClient> {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level =
            if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .cache(Cache(get<Context>().cacheDir, 10 * 1024 * 1024))
            .callTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
    }
    viewModelOf(::AllQuotesByTagViewModel)
    viewModelOf(::BrowseTagViewModel)
    viewModelOf(::AllSettingsViewModel)
}