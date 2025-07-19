package com.dayaonweb.quoter.domain.repository

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.dayaonweb.quoter.constants.Constants
import com.dayaonweb.quoter.data.local.DataStoreManager
import com.dayaonweb.quoter.data.local.models.Preferences
import com.dayaonweb.quoter.data.remote.QuoteService
import com.dayaonweb.quoter.data.remote.model.Quote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.util.Collections.emptyList
import java.util.Locale
import kotlin.time.Duration.Companion.hours

class RepositoryImpl(
    private val context: Context,
    private val quotesService: QuoteService
) : Repository {

    override suspend fun getUserPreferences(): Preferences = withContext(Dispatchers.IO) {
        val isImageStyled = DataStoreManager.getBooleanValue(
            context,
            Constants.IS_IMAGE_NOTIFICATION_STYLE,
            true
        )
        val isNotificationOn =
            DataStoreManager.getBooleanValue(context, Constants.IS_NOTIFICATION_ON, true)
        val selectedTtsLanguage =
            DataStoreManager.getStringValue(context, Constants.TTS_LANGUAGE, "en_IN")
                .split("_")
        val notifTime =
            DataStoreManager.getStringValue(context, Constants.NOTIFICATION_TIME, "9:00")
        val speechRate =
            DataStoreManager.getFloatValue(context, Constants.TTS_SPEECH_RATE, 1.0f)
        val isDarkMode =
            DataStoreManager.getBooleanValue(
                context,
                Constants.IS_DARK_MODE,
                AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
            )
        Preferences(
            isNotificationOn = isNotificationOn,
            isImageStyleNotification = isImageStyled,
            notificationTime = notifTime,
            ttsLanguage = Locale(selectedTtsLanguage[0], selectedTtsLanguage[1]),
            speechRate = speechRate,
            isDarkMode = isDarkMode
        )
    }

    override suspend fun fetchQuotesTag(): List<String> = withContext(Dispatchers.IO) {
        try {
            // Check local cache first, then remote.
            val lastFetchTimeInMs = DataStoreManager.getFloatValue(
                context,
                Constants.LOCAL_TAGS_LAST_FETCH_TIME_MS,
                0.0f
            )
            if (isTagsCacheExpired(lastFetchTimeInMs, true)) {
                val genres = quotesService
                    .getAllGenres()
                    .mapNotNull {
                        it?.name
                    }
                if (genres.isNotEmpty()) {
                    cacheTags(genres)
                    genres
                } else
                    fetchTagsFromLocal()
            } else {
                fetchTagsFromLocal()
            }
        } catch (e: Exception) {
            fetchTagsFromLocal()
        }
    }

    private suspend fun isTagsCacheExpired(lastFetchTimeInMs: Float, isTags: Boolean): Boolean =
        withContext(Dispatchers.IO) {
            if (lastFetchTimeInMs == 0.0f)
                return@withContext true
            val localTags = if (isTags) fetchTagsFromLocal() else fetchQuotesFromLocal("", 0)
            if (localTags.isEmpty())
                return@withContext true
            val lastFetchAt = lastFetchTimeInMs.toLong()
            val now = System.currentTimeMillis()
            val timeDiff = now - lastFetchAt
            return@withContext timeDiff > 8.hours.inWholeMilliseconds
        }

    override suspend fun fetchQuotesByTag(
        tag: String,
        pageNo: Int
    ): List<Quote> = withContext(Dispatchers.IO) {
        val quotesOrEmpty = fetchQuotesFromRemote(tag, pageNo)
        quotesOrEmpty.ifEmpty { fetchQuotesFromLocal(tag, pageNo) }
    }

    private suspend fun fetchQuotesFromRemote(
        tag: String,
        pageNo: Int
    ): List<Quote> = withContext(Dispatchers.IO) {
        try {
            val quotes = quotesService.getQuotes(tags = tag, page = pageNo)?.quotes?.filterNotNull()
                ?: emptyList()
            if (quotes.isNotEmpty())
                cacheQuotes(tag, pageNo, quotes)
            quotes
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun fetchQuotesFromLocal(
        tag: String,
        pageNo: Int
    ): List<Quote> = withContext(Dispatchers.IO) {
        val localQuotesString = DataStoreManager.getStringValue(context, Constants.LOCAL_QUOTES, "")
        if (localQuotesString.isEmpty())
            return@withContext emptyList()
        Json.decodeFromString<List<Quote>>(localQuotesString)
    }

    private suspend fun fetchTagsFromLocal(): List<String> = withContext(Dispatchers.IO) {
        val tagsOrEmpty = DataStoreManager.getStringValue(context, Constants.LOCAL_TAGS, "")
        if (tagsOrEmpty.isEmpty())
            return@withContext emptyList()
        tagsOrEmpty.split(",")
    }

    private suspend fun cacheQuotes(
        tag: String,
        pageNo: Int,
        quotes: List<Quote>
    ) = withContext(
        Dispatchers.IO
    ) {
        DataStoreManager.saveValue(
            context,
            Constants.LOCAL_QUOTES,
            Json.encodeToString(quotes)
        )
        DataStoreManager.saveValue(
            context,
            Constants.LOCAL_QUOTES_LAST_FETCH_TIME_MS,
            System.currentTimeMillis().toFloat()
        )
    }

    private suspend fun cacheTags(tags: List<String>) = withContext(Dispatchers.IO) {
        DataStoreManager.saveValue(
            context,
            Constants.LOCAL_TAGS,
            tags.joinToString()
        )
        DataStoreManager.saveValue(
            context,
            Constants.LOCAL_TAGS_LAST_FETCH_TIME_MS,
            System.currentTimeMillis().toFloat()
        )
    }
}