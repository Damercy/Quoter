package com.dayaonweb.quoter

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dayaonweb.quoter.data.local.QuoteDatabase
import com.dayaonweb.quoter.data.local.QuoteParser
import com.dayaonweb.quoter.data.repository.QuotesRepoImpl
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class QuoteRepositoryTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private lateinit var database: QuoteDatabase
    private lateinit var scope: CoroutineScope
    private lateinit var prefsFile: File
    private var statusCode = 200
    private var body = "[]"
    private var requests = 0
    private var conditionalHeader: String? = null
    private var now = 10 * 24 * 60 * 60 * 1000L
    private lateinit var repo: QuotesRepoImpl
    private lateinit var preferences: androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences>

    @Before fun setup() {
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        database = Room.inMemoryDatabaseBuilder(context, QuoteDatabase::class.java).build()
        prefsFile = File(context.cacheDir, "quotes-${UUID.randomUUID()}.preferences_pb")
        preferences = PreferenceDataStoreFactory.create(scope = scope, produceFile = { prefsFile })
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            requests++
            conditionalHeader = chain.request().header("If-None-Match")
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1)
                .code(statusCode).message("Test response").header("ETag", "test-version")
                .body(body.toResponseBody("application/json".toMediaType())).build()
        }.build()
        repo = QuotesRepoImpl(context, database, client, scope, preferences, listOf("https://quotes.invalid/quotes.json"), { now }, false)
    }
    @After fun close() { scope.cancel(); database.close(); prefsFile.delete() }

    @Test fun bundleSeedsThousandsWithoutNetworkAndIsIdempotent() = runBlocking {
        repo.initialize()
        assertEquals(3079, database.quotes().count())
        assertEquals(0, requests)
        val first = repo.allQuotes.first().first()
        repo.setSaved(first.id, true)
        repo.initialize()
        assertEquals(3079, database.quotes().count())
        assertTrue(first.id in repo.savedIds.first())
        assertNotNull(repo.randomQuote())
    }

    @Test fun refreshAddsQuotesPreservesSavedAndUsesConditionalDailyCache() = runBlocking {
        repo.initialize()
        val quote = repo.allQuotes.first().first { it.tags.any { tag -> tag != "general" } }
        repo.setSaved(quote.id, true)
        val escaped = org.json.JSONObject().put("quote", quote.quote).put("author", quote.author)
        body = org.json.JSONArray().put(escaped).put(org.json.JSONObject().put("quote", "A new tested quote for the collection.").put("author", "Test Author")).toString()
        repo.refresh()
        assertEquals(3080, database.quotes().count())
        assertEquals(quote.tags, repo.allQuotes.first().first { it.id == quote.id }.tags)
        assertTrue(quote.id in repo.savedIds.first())
        repo.refresh()
        assertEquals(1, requests)
        now += 24 * 60 * 60 * 1000L
        statusCode = 304
        repo.refresh()
        assertEquals("test-version", conditionalHeader)
        assertEquals(3080, database.quotes().count())
    }

    @Test fun rateLimitAndOutageKeepLocalQuotesAndBackOff() = runBlocking {
        repo.initialize()
        for (code in listOf(429,503)) {
            statusCode = code
            repo.refresh(force = true)
            assertEquals(3079, database.quotes().count())
            val attempts = requests
            repo.refresh()
            assertEquals(attempts, requests)
            assertTrue(repo.status.value.contains("refresh unavailable"))
        }
    }

    @Test fun malformedEmptyAndOversizedResponsesKeepLocalCollection() = runBlocking {
        repo.initialize()
        for (invalid in listOf("[]", "not JSON", "[{\"quote\":\"Valid quote\",\"author\":null}]", "x".repeat(4 * 1024 * 1024 + 1))) {
            body = invalid
            repo.refresh(force = true)
            assertEquals(3079, database.quotes().count())
            assertNull(preferences.data.first()[stringPreferencesKey("QUOTE_ETAG_0")])
            assertNull(preferences.data.first()[longPreferencesKey("QUOTE_REFRESH_SUCCESS")])
        }
    }

    @Test fun parserMergesDuplicateTopicsAndRejectsMissingAuthors() {
        val parsed = QuoteParser.parse("""[
          {"quote":"  A thoughtful   quote. ","author":"Test Author","tags":["life"]},
          {"quote":"A thoughtful quote.","author":"Test Author","tags":["wisdom"]},
          {"quote":"Another thoughtful quote.","author":null},
          {"quote":"Another thoughtful quote."}
        ]""")
        assertEquals(1, parsed.size)
        assertEquals(listOf("life", "wisdom"), parsed.single().tags)
        assertEquals(parsed.single(), QuoteParser.domain(QuoteParser.store(parsed.single())))
    }
}
