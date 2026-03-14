package com.dayaonweb.quoter.data.di

import android.content.Context
import com.dayaonweb.quoter.BuildConfig
import com.dayaonweb.quoter.data.local.DataStoreManager
import com.dayaonweb.quoter.data.remote.QuotesClient
import com.dayaonweb.quoter.data.repository.PreferencesRepo
import com.dayaonweb.quoter.data.repository.PreferencesRepoImpl
import com.dayaonweb.quoter.data.repository.QuotesRepo
import com.dayaonweb.quoter.data.repository.QuotesRepoImpl
import com.dayaonweb.quoter.domain.tts.QuoteSpeaker
import com.dayaonweb.quoter.domain.tts.QuoteSpeakerImpl
import com.dayaonweb.quoter.domain.tts.Quoter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class QuoteModule {

    @Provides
    @Singleton
    fun provideTTSEngine(
        @ApplicationContext context: Context,
    ): Quoter = Quoter(context, {})

    @Provides
    @Singleton
    fun provideQuoteSpeaker(
        quoter: Quoter
    ): QuoteSpeaker = QuoteSpeakerImpl(quoter)

    @Provides
    @Singleton
    fun provideQuotesRepo(
        remoteDataSource: QuotesClient
    ): QuotesRepo = QuotesRepoImpl(
        remoteDataSource = remoteDataSource
    )

    @Provides
    @Singleton
    fun providePreferencesRepo(
        @ApplicationContext context: Context,
        dataStoreManager: DataStoreManager
    ): PreferencesRepo = PreferencesRepoImpl(
        context,
        dataStoreManager
    )

    @Provides
    @Singleton
    fun provideLocalDataSource(): DataStoreManager {
        return DataStoreManager()
    }

    @Provides
    @Singleton
    fun provideRemoteDataSource(
        okHttpClient: OkHttpClient
    ): QuotesClient {
        return QuotesClient(okHttpClient)
    }

    @Provides
    @Singleton
    fun provideIoCoroutine(): CoroutineScope {
        return CoroutineScope(Dispatchers.IO + SupervisorJob())
    }

    @Provides
    @Singleton
    fun provideHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level =
            if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.HEADERS else HttpLoggingInterceptor.Level.NONE
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .callTimeout(7, TimeUnit.SECONDS)
            .build()
    }
}