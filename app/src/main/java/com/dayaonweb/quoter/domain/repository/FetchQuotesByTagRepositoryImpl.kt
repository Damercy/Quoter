package com.dayaonweb.quoter.domain.repository

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.dayaonweb.quoter.constants.Constants
import com.dayaonweb.quoter.data.local.DataStoreManager
import com.dayaonweb.quoter.data.local.models.Preferences
import com.dayaonweb.quoter.data.remote.QuoteService
import com.dayaonweb.quoter.data.remote.model.RandomQuotesListingResponseItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Collections.emptyList
import java.util.Locale
import kotlin.collections.get

class FetchQuotesByTagRepositoryImpl(
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
            val genres = quotesService.getAllGenres().filterNotNull()
            if (genres.isNotEmpty()) {
                cacheTags(genres)
                genres
            } else
                fetchTagsFromLocal()
        } catch (e: Exception) {
            fetchTagsFromLocal()
        }
    }

    override suspend fun fetchQuotesByTag(
        tag: String,
        pageNo: Int
    ): List<RandomQuotesListingResponseItem> = withContext(Dispatchers.IO) {
        val remoteQuotesOrEmpty = fetchQuotesFromRemote(tag, pageNo)
        remoteQuotesOrEmpty.ifEmpty { fetchQuotesFromLocal(tag, pageNo) }
    }

    private suspend fun fetchQuotesFromRemote(
        tag: String,
        pageNo: Int
    ): List<RandomQuotesListingResponseItem> = withContext(Dispatchers.IO) {
        try {
            val quotes = quotesService.getQuotes(tags = tag, page = pageNo).filterNotNull()
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
    ): List<RandomQuotesListingResponseItem> = withContext(Dispatchers.IO) {
        // TODO: Use kotlinx serialization
        val localQuotesString = DataStoreManager.getStringValue(context, Constants.LOCAL_QUOTES, "")
        if (localQuotesString.isEmpty())
            return@withContext emptyList()
        localQuotesString
            .split(",")
            .map {
                RandomQuotesListingResponseItem()
            }
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
        quotes: List<RandomQuotesListingResponseItem>
    ) = withContext(
        Dispatchers.IO
    ) {
        DataStoreManager.saveValue(
            context,
            Constants.LOCAL_QUOTES,
            quotes.joinToString()
        )
    }
    private suspend fun cacheTags(tags: List<String>) = withContext(Dispatchers.IO) {
        DataStoreManager.saveValue(
            context,
            Constants.LOCAL_TAGS,
            tags.joinToString()
        )
    }
}