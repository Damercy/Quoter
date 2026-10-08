package com.dayaonweb.quoter.presentation.compose

import android.content.*
import android.graphics.*
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.dayaonweb.quoter.R
import com.dayaonweb.quoter.domain.models.UiQuote
import java.io.File
import java.util.UUID

/** Draw from data, so export includes text below the reader's scroll viewport. */
object QuoteImage {
    fun render(context: Context, quote: UiQuote, dark: Boolean, width: Int = 1080): Bitmap {
        val margin = width / 12
        val ink = if (dark) Color.WHITE else Color.BLACK
        val text = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ink; textSize = width / 20f; typeface = ResourcesCompat.getFont(context, R.font.main_bold)
        }
        val body = StaticLayout.Builder.obtain(quote.quote, 0, quote.quote.length, text, width - margin * 2)
            .setLineSpacing(width / 90f, 1f).setIncludePad(false).build()
        val authorPaint = TextPaint(text).apply { textSize = width / 32f; typeface = ResourcesCompat.getFont(context, R.font.main_regular) }
        val author = StaticLayout.Builder.obtain(quote.author, 0, quote.author.length, authorPaint, width - margin * 2)
            .setAlignment(Layout.Alignment.ALIGN_OPPOSITE).setIncludePad(false).build()
        val height = (body.height + author.height + margin * 4).coerceAtLeast(width)
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { bitmap ->
            val canvas = Canvas(bitmap); canvas.drawColor(if (dark) Color.rgb(18,18,18) else Color.WHITE)
            canvas.save(); canvas.translate(margin.toFloat(), margin * 1.5f); body.draw(canvas); canvas.restore()
            canvas.save(); canvas.translate(margin.toFloat(), margin * 2.5f + body.height); author.draw(canvas); canvas.restore()
        }
    }
    fun export(context: Context, quote: UiQuote, dark: Boolean): Intent {
        val directory = File(context.cacheDir, "shared_quotes").apply { mkdirs() }
        directory.listFiles()?.filter { System.currentTimeMillis() - it.lastModified() > 24 * 60 * 60 * 1000L }?.forEach { it.delete() }
        val file = File(directory, "quote_${UUID.randomUUID()}.png")
        val bitmap = render(context, quote, dark)
        try { file.outputStream().use { check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) } } finally { bitmap.recycle() }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        return Intent(Intent.ACTION_SEND).apply {
            type = "image/png"; putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TITLE, "A quote from ${quote.author}")
            putExtra(Intent.EXTRA_SUBJECT, "A quote from ${quote.author} · Quoter")
            putExtra(Intent.EXTRA_TEXT, "${quote.quote}\n— ${quote.author}\n\nShared with Quoter\nhttps://play.google.com/store/apps/details?id=com.dayaonweb.quoter")
            clipData = ClipData.newUri(context.contentResolver, "Shared with Quoter", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
