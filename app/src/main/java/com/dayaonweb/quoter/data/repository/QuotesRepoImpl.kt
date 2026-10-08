package com.dayaonweb.quoter.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.room.withTransaction
import com.dayaonweb.quoter.data.local.*
import com.dayaonweb.quoter.domain.models.UiQuote
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayOutputStream
import java.io.IOException

/** UI reads Room; network refresh never sits in the reading path. */
class QuotesRepoImpl(private val context: Context, private val database: QuoteDatabase,
    private val client: OkHttpClient, private val scope: CoroutineScope,
    private val settings: DataStore<Preferences> = context.settingsDatastore,
    private val endpoints: List<String> = ENDPOINTS,
    private val clock: () -> Long = System::currentTimeMillis,
    startAutomatically: Boolean = true) : QuotesRepo {
    private val dao = database.quotes()
    private val mutex = Mutex()
    private val mutableStatus = MutableStateFlow("Loading local quotes")
    val status = mutableStatus.asStateFlow()
    val allQuotes = dao.observeQuotes().map { rows -> rows.map(QuoteParser::domain) }
    val savedIds = dao.observeSaved().map { it.toSet() }
    init { if (startAutomatically) scope.launch { initialize(); refresh() } }

    suspend fun initialize() = mutex.withLock {
        val preferences = settings.data.first()
        if (preferences[stringPreferencesKey("QUOTE_BUNDLE_VERSION")] != BUNDLE_VERSION || dao.count() == 0) {
            val quotes = withContext(Dispatchers.IO) { context.assets.open("quotes.json").bufferedReader().use { QuoteParser.parse(it.readText()) } }
            require(quotes.isNotEmpty()) { "Bundled quote collection is empty" }
            database.withTransaction { dao.insertQuotes(quotes.map(QuoteParser::store)) }
            settings.edit { it[stringPreferencesKey("QUOTE_BUNDLE_VERSION")] = BUNDLE_VERSION }
        }
        mutableStatus.value = "Offline collection ready"
    }

    suspend fun refresh(force: Boolean = false) = mutex.withLock {
        val preferences = settings.data.first()
        val now = clock()
        val lastSuccess = preferences[longPreferencesKey("QUOTE_REFRESH_SUCCESS")] ?: 0L
        val nextAttempt = preferences[longPreferencesKey("QUOTE_REFRESH_AFTER")] ?: 0L
        if (!force && (now < nextAttempt || now - lastSuccess < DAY)) return@withLock
        var success = false
        for ((index, url) in endpoints.withIndex()) {
            currentCoroutineContext().ensureActive()
            val etagKey = stringPreferencesKey("QUOTE_ETAG_$index")
            try {
                val response = withContext(Dispatchers.IO) {
                    val request = Request.Builder().url(url).header("Accept", "application/json")
                    preferences[etagKey]?.let { request.header("If-None-Match", it) }
                    client.newCall(request.build()).execute().use { result ->
                        when {
                            result.code == 304 -> RefreshResult(null, result.header("ETag"))
                            result.code == 429 -> throw IOException("Quote refresh rate limited")
                            !result.isSuccessful -> throw IOException("Quote refresh HTTP ${result.code}")
                            else -> {
                                val body = result.body
                                if (body.contentLength() > MAX_BYTES) throw IOException("Quote response too large")
                                val output = ByteArrayOutputStream()
                                body.byteStream().use { input ->
                                    val buffer = ByteArray(8192)
                                    while (true) {
                                        val size = input.read(buffer); if (size < 0) break
                                        if (output.size() + size > MAX_BYTES) throw IOException("Quote response too large")
                                        output.write(buffer, 0, size)
                                    }
                                }
                                RefreshResult(output.toString("UTF-8"), result.header("ETag"))
                            }
                        }
                    }
                }
                currentCoroutineContext().ensureActive()
                response.json?.let { json ->
                    val incoming = withContext(Dispatchers.Default) { QuoteParser.parse(json) }
                    require(incoming.isNotEmpty()) { "Empty or invalid quote source" }
                    val existing = allQuotes.first().associateBy { it.id }
                    val merged = incoming.map { quote ->
                        val tags = (existing[quote.id]?.tags.orEmpty() + quote.tags).distinct()
                        quote.copy(tags = if (tags.size > 1) tags.filterNot { it == "general" } else tags)
                    }
                    database.withTransaction { dao.insertQuotes(merged.map(QuoteParser::store)) }
                }
                response.etag?.let { etag -> settings.edit { it[etagKey] = etag } }
                success = true
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { /* The persistent local collection remains authoritative. */ }
        }
        settings.edit {
            if (success) it[longPreferencesKey("QUOTE_REFRESH_SUCCESS")] = now
            it[longPreferencesKey("QUOTE_REFRESH_AFTER")] = now + if (success) DAY else HOUR
        }
        mutableStatus.value = if (success) "Collection up to date" else "Offline collection · refresh unavailable"
    }

    suspend fun setSaved(id: String, saved: Boolean) { if (saved) dao.save(SavedQuote(id)) else dao.unsave(id) }
    suspend fun randomQuote(): UiQuote { initialize(); return QuoteParser.domain(requireNotNull(dao.randomQuote())) }
    override fun getTags(): Flow<List<String>> = allQuotes.map { quotes -> quotes.flatMap { it.tags }.distinct().sorted() }
    override fun getQuotesByTags(tags: List<String>): Flow<List<UiQuote>> = allQuotes.map { quotes ->
        val wanted = tags.toSet(); if (wanted.isEmpty()) quotes else quotes.filter { quote -> quote.tags.any { it in wanted } }
    }
    private data class RefreshResult(val json: String?, val etag: String?)
    companion object {
        const val BUNDLE_VERSION = "2026-10-08-3079"
        private const val MAX_BYTES = 4 * 1024 * 1024
        private const val HOUR = 60 * 60 * 1000L
        private const val DAY = 24 * HOUR
        val ENDPOINTS = listOf(
            "https://raw.githubusercontent.com/Musheer360/QuoteSlate/main/data/quotes.json",
            "https://raw.githubusercontent.com/micheleriva/the-quotes-database/master/src/data/quotes.json")
    }
}
