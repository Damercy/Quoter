package com.dayaonweb.quoter.service.broadcast

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.bumptech.glide.Glide
import com.dayaonweb.quoter.R
import com.dayaonweb.quoter.constants.Constants.CHANNEL_ID
import com.dayaonweb.quoter.constants.Constants.CHANNEL_NAME
import com.dayaonweb.quoter.constants.Constants.IS_IMAGE_NOTIFICATION_STYLE
import com.dayaonweb.quoter.constants.Constants.NOTIFICATION_ID
import com.dayaonweb.quoter.data.local.DataStoreManager
import com.dayaonweb.quoter.service.QuotesClient
import com.dayaonweb.quoter.service.model.Quote
import com.dayaonweb.quoter.view.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class QuoteBroadcast : BroadcastReceiver() {

    private lateinit var authorImageBitmap: Bitmap
    private lateinit var randomQuote: Quote
    private var isImageTypeNotification = true

    override fun onReceive(context: Context, intent: Intent) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                isImageTypeNotification =
                    DataStoreManager.getBooleanValue(context, IS_IMAGE_NOTIFICATION_STYLE, true)
                randomQuote = QuotesClient().api.getRandomQuote()
                if (isImageTypeNotification) {
                    val authorImageResponse =
                        QuotesClient().wikiApi.getAuthorImage(
                            authorName = randomQuote.author,
                            thumbnailSize = 500
                        ).query
                    val authorImage =
                        authorImageResponse?.pages?.entries?.first()?.value?.original?.source
                            ?: ""

                    authorImageBitmap = Glide.with(context)
                        .asBitmap()
                        .load(authorImage)
                        .error(R.mipmap.ic_launcher)
                        .submit()
                        .get()
                }
            } catch (exception: Exception) {
                randomQuote = getOfflineRandomQuote()
                authorImageBitmap = Glide.with(context)
                    .asBitmap()
                    .load(R.mipmap.ic_launcher)
                    .submit()
                    .get()
            } finally {
                val quote = randomQuote.content
                val title = "${randomQuote.author} says"
                createNotificationChannel(context)
                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentTitle(title)
                    .setContentText(quote)
                    .setAutoCancel(true)
                    .setStyle(
                        if (isImageTypeNotification) {
                            NotificationCompat.BigPictureStyle()
                                .setBigContentTitle(title)
                                .setSummaryText(quote)
                                .bigPicture(authorImageBitmap)
                        } else {
                            NotificationCompat.BigTextStyle()
                                .setBigContentTitle(title)
                                .bigText(quote)
                        }
                    )
                    .setContentIntent(getPendingIntent(context))
                    .build()
                val notificationManager = NotificationManagerCompat.from(context)
                notificationManager.notify(NOTIFICATION_ID, notification)
            }
        }
    }
}

private fun getPendingIntent(context: Context): PendingIntent? {
    val intent = Intent(context, MainActivity::class.java)
    return TaskStackBuilder.create(context).run {
        addNextIntentWithParentStack(intent)
        getPendingIntent(NOTIFICATION_ID, PendingIntent.FLAG_UPDATE_CURRENT)
    }
}

private fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            CHANNEL_ID, CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            lightColor = Color.GREEN
            enableLights(true)
            enableVibration(true)
        }
        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}

private fun getOfflineRandomQuote() =
    listOf(
        Quote(
            author = "Aeschylus",
            authorSlug = "aeschylus",
            content = "It is good even for old men to learn wisdom.",
            id = "1YubdPwZ3e",
            length = 44,
            tags = listOf("wisdom")
        ),
        Quote(
            author = "Nathaniel Hawthorne",
            authorSlug = "nathaniel-hawthorne",
            content = "Happiness is as a butterfly which, when pursued, is always beyond our grasp, but which if you will sit down quietly, may alight upon you.",
            id = "9gCEJkS5uaRS",
            length = 137,
            tags = listOf("famous-quotes")
        ),
        Quote(
            author = "Chanakya",
            authorSlug = "chanakya",
            content = "There is some self-interest behind every friendship. There is no friendship without self-interests. This is a bitter truth.",
            id = "oT3J6wfjpS",
            length = 123,
            tags = listOf("friendship")
        )
    ).random()


private fun getRandomTitleEmoji(): String {
    val emojis = listOf(
        "\uD83E\uDDD4\uD83C\uDFFC",
        "\uD83D\uDD8B️",
        "\uD83D\uDD25",
        "\uD83D\uDCDC"
    )
    return emojis.random()
}