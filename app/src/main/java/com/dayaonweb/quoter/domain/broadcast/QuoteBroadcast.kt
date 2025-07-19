package com.dayaonweb.quoter.domain.broadcast

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.bumptech.glide.Glide
import com.dayaonweb.quoter.R
import com.dayaonweb.quoter.constants.Constants
import com.dayaonweb.quoter.constants.Constants.CHANNEL_ID
import com.dayaonweb.quoter.constants.Constants.CHANNEL_NAME
import com.dayaonweb.quoter.constants.Constants.IS_IMAGE_NOTIFICATION_STYLE
import com.dayaonweb.quoter.constants.Constants.NOTIFICATION_ID
import com.dayaonweb.quoter.data.local.DataStoreManager
import com.dayaonweb.quoter.data.remote.QuoteService
import com.dayaonweb.quoter.data.remote.WikiService
import com.dayaonweb.quoter.data.remote.model.Quote
import com.dayaonweb.quoter.view.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.java.KoinJavaComponent.inject
import java.util.Calendar

class QuoteBroadcast : BroadcastReceiver(), KoinComponent {

    private lateinit var authorImageBitmap: Bitmap
    private var randomQuote: Quote? = null
    private var isImageTypeNotification = true

    private val wikiService: WikiService by inject()

    private val quotesService: QuoteService by inject()

    override fun onReceive(context: Context, intent: Intent) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                isImageTypeNotification =
                    DataStoreManager.getBooleanValue(context, IS_IMAGE_NOTIFICATION_STYLE, true)
                randomQuote = quotesService.getQuotes(limit = 10)?.quotes?.random()
                if (isImageTypeNotification) {
                    val authorImageResponse =
                        wikiService.getAuthorImage(
                            authorName = randomQuote?.author?.name.orEmpty(),
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
                randomQuote = getOfflineRandomQuote(context).random()
                authorImageBitmap = Glide.with(context)
                    .asBitmap()
                    .load(R.mipmap.ic_launcher)
                    .submit()
                    .get()
            } finally {
                val quote = randomQuote?.content.orEmpty()
                val title = "${randomQuote?.author?.name ?: "Unknown"} says"
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
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    if (notificationManager.areNotificationsEnabled())
                        notificationManager.notify(NOTIFICATION_ID, notification)
                }
                val time =
                    DataStoreManager.getStringValue(context, Constants.NOTIFICATION_TIME, "9:00")
                val timeHrMin = time.split(":")
                setAlarm(
                    context = context,
                    hour = timeHrMin[0].toInt(),
                    minute = timeHrMin[1].toInt()
                )
            }
        }
    }


}

private fun setAlarm(context: Context, hour: Int, minute: Int) {
    val calendar = Calendar.getInstance()
    calendar.apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val broadcastIntent = Intent(context, QuoteBroadcast::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        Constants.PENDING_INTENT_REQ_CODE,
        broadcastIntent,
        PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
    if (calendar.before(Calendar.getInstance()))
        calendar.add(Calendar.DATE, 1)
    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
}

private fun getPendingIntent(context: Context): PendingIntent? {
    val intent = Intent(context, MainActivity::class.java)
    return TaskStackBuilder.create(context).run {
        addNextIntentWithParentStack(intent)
        getPendingIntent(
            NOTIFICATION_ID,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
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

private suspend fun getOfflineRandomQuote(context: Context) = withContext(Dispatchers.IO){
    val localQuotes = DataStoreManager.getStringValue(context, Constants.LOCAL_QUOTES,"")
    if(localQuotes.isEmpty())
        return@withContext emptyList()
    Json.decodeFromString<List<Quote>>(localQuotes)
}