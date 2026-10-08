package com.dayaonweb.quoter.data.di

import android.content.Context
import androidx.room.Room
import com.dayaonweb.quoter.data.local.QuoteDatabase
import com.dayaonweb.quoter.data.repository.QuotesRepoImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object QuoteModule {
    @Provides @Singleton fun database(@ApplicationContext context: Context): QuoteDatabase =
        Room.databaseBuilder(context, QuoteDatabase::class.java, "quotes.db").build()
    @Provides @Singleton fun scope(): CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    @Provides @Singleton fun httpClient(): OkHttpClient = OkHttpClient.Builder().callTimeout(7, TimeUnit.SECONDS).build()
    @Provides @Singleton fun repository(@ApplicationContext context: Context, database: QuoteDatabase,
        client: OkHttpClient, scope: CoroutineScope): QuotesRepoImpl = QuotesRepoImpl(context, database, client, scope)
}
