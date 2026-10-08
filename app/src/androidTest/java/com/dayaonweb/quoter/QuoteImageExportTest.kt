package com.dayaonweb.quoter

import android.content.Intent
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import com.dayaonweb.quoter.domain.models.UiQuote
import com.dayaonweb.quoter.presentation.compose.QuoteImage
import org.junit.Assert.*
import org.junit.Test

class QuoteImageExportTest {
    @Test fun sharesReadablePngAndMatchingAttributedTextWithTemporaryPermission() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val quote = UiQuote("export", "A quiet moment helps us think.", "First Author", emptyList())
        val intent = QuoteImage.export(context, quote, false)
        assertEquals(Intent.ACTION_SEND, intent.action)
        assertEquals("image/png", intent.type)
        assertEquals("${quote.quote}\n— ${quote.author}\n\nShared with Quoter\nhttps://play.google.com/store/apps/details?id=com.dayaonweb.quoter", intent.getStringExtra(Intent.EXTRA_TEXT))
        @Suppress("DEPRECATION") val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)!!
        assertEquals("content", uri.scheme)
        assertEquals(uri, intent.clipData!!.getItemAt(0).uri)
        assertTrue(intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
        context.contentResolver.openInputStream(uri)!!.use {
            val signature = ByteArray(8); assertEquals(8, it.read(signature))
            assertArrayEquals(byteArrayOf(-119, 80, 78, 71, 13, 10, 26, 10), signature)
        }
    }
}
