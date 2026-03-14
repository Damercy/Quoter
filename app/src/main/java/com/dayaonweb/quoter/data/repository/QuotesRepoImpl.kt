package com.dayaonweb.quoter.data.repository

import com.dayaonweb.quoter.data.local.LOCAL_QUOTES_JSON
import com.dayaonweb.quoter.data.remote.QuotesClient
import com.dayaonweb.quoter.data.remote.model.RandomQuotesListingResponseItem
import com.dayaonweb.quoter.domain.models.UiQuote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import javax.inject.Inject

class QuotesRepoImpl @Inject constructor(
    private val remoteDataSource: QuotesClient
) : QuotesRepo {

    override fun getTags(): Flow<List<String>> = flow {
        try {
            val remoteResponse = remoteDataSource.api.getAllGenres()
            if (remoteResponse.isEmpty()) {
                emit(getLocalQuotes()
                    .sortedByDescending { it.tags.count() }
                    .flatMap { it.tags }
                    .distinct()
                )
            } else {
                emit(remoteResponse.distinct())
            }
        } catch (_: Exception) {
            emit(getLocalQuotes()
                .sortedByDescending { it.tags.count() }
                .flatMap { it.tags }
                .distinct()
            )
        }
    }

    override fun getQuotesByTags(tags: List<String>): Flow<List<UiQuote>> = flow {
        try {
            // Attempt to fetch from remote
            val remoteResponse =
                remoteDataSource.api.getQuotes(tags = tags.distinct().joinToString())
            // Map remote response here if necessary. Assuming local fallback for now:
            if (remoteResponse.isEmpty()) {
                emit(
                    getLocalQuotes().filter { it.tags.contains(tags.distinct().firstOrNull()) }
                )
            } else {
                emit(mapToDomainModel(remoteResponse = remoteResponse))
            }
        } catch (_: Exception) {
            emit(
                getLocalQuotes().filter { it.tags.contains(tags.distinct().firstOrNull()) }
            )
        }
    }

    private suspend fun getLocalQuotes(): List<UiQuote> = withContext(Dispatchers.Default) {
        val mockLocalJson = LOCAL_QUOTES_JSON
        val jsonArray = JSONArray(mockLocalJson)
        val quotes = mutableListOf<UiQuote>()
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val tagsArray = jsonObject.getJSONArray("tags")
            val tagsList = mutableListOf<String>()
            for (j in 0 until tagsArray.length()) {
                tagsList.add(tagsArray.getString(j))
            }
            quotes.add(
                UiQuote(
                    id = jsonObject.getString("id"),
                    quote = jsonObject.getString("quote"),
                    author = jsonObject.getString("author"),
                    tags = tagsList,
                    authorImage = if (jsonObject.isNull("authorImage")) null else jsonObject.getString(
                        "authorImage"
                    ),
                    quoteLength = jsonObject.getInt("quoteLength")
                )
            )
        }
        quotes
    }


    private suspend fun mapToDomainModel(remoteResponse: List<RandomQuotesListingResponseItem?>): List<UiQuote> =
        withContext(Dispatchers.Default) {
            remoteResponse.mapNotNull {
                UiQuote(
                    id = it?.id?.toString() ?: "",
                    quote = it?.quote ?: "",
                    author = it?.author ?: "",
                    tags = it?.tags ?: emptyList(),
                )
            }
        }
}
