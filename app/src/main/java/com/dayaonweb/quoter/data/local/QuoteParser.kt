package com.dayaonweb.quoter.data.local

import com.dayaonweb.quoter.domain.models.UiQuote
import org.json.JSONArray
import java.security.MessageDigest
import java.util.Locale

object QuoteParser {
    fun parse(json: String): List<UiQuote> {
        val rows = JSONArray(json)
        require(rows.length() <= 20_000) { "Quote source is too large" }
        val quotes = linkedMapOf<String, UiQuote>()
        for (i in 0 until rows.length()) {
            val item = rows.optJSONObject(i) ?: continue
            val text = ((item.opt("quote") as? String) ?: (item.opt("content") as? String).orEmpty()).replace(Regex("\\s+"), " ").trim()
            val author = (item.opt("author") as? String).orEmpty().replace(Regex("\\s+"), " ").trim()
            if (text.length !in 5..3000 || author.isBlank() || author.length > 200) continue
            val identity = "${text.lowercase(Locale.ROOT)}|${author.lowercase(Locale.ROOT)}"
            val digest = MessageDigest.getInstance("SHA-256").digest(identity.toByteArray(Charsets.UTF_8))
            val hex = "0123456789abcdef"
            val id = buildString(24) { for (index in 0 until 12) {
                val value = digest[index].toInt() and 255
                append(hex[value ushr 4]); append(hex[value and 15])
            } }
            val tagsJson = item.optJSONArray("tags")
            val tags = if (tagsJson == null) emptyList() else (0 until tagsJson.length()).map { tagsJson.optString(it).trim().lowercase(Locale.ROOT) }
                .filter { it.isNotBlank() && it.length <= 80 }.distinct()
            val previous = quotes[id]
            quotes[id] = UiQuote(id, text, author, ((previous?.tags.orEmpty().filterNot { it == "general" } + tags).distinct()).ifEmpty { listOf("general") })
        }
        return quotes.values.toList()
    }
    fun store(quote: UiQuote) = StoredQuote(quote.id, quote.quote, quote.author, JSONArray(quote.tags).toString())
    fun domain(quote: StoredQuote): UiQuote {
        val tags = JSONArray(quote.tags)
        return UiQuote(quote.id, quote.text, quote.author, List(tags.length()) { tags.getString(it) })
    }
}
