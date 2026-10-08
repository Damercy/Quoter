package com.dayaonweb.quoter.domain.broadcast

import android.app.PendingIntent
import android.content.*
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.dayaonweb.quoter.R
import com.dayaonweb.quoter.data.local.settingsDatastore
import com.dayaonweb.quoter.data.repository.QuotesRepoImpl
import com.dayaonweb.quoter.domain.constants.Constants
import com.dayaonweb.quoter.presentation.view.ui.MainActivity
import com.dayaonweb.quoter.presentation.compose.QuoteImage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class QuoteBroadcast : BroadcastReceiver() {
    @Inject lateinit var repository: QuotesRepoImpl
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            val scheduler = ReminderScheduler(context)
            try {
                withTimeout(8000) {
                    scheduler.createChannel()
                    val preferences = context.settingsDatastore.data.first()
                    if (preferences[booleanPreferencesKey(Constants.IS_NOTIFICATION_ON)] == false || !scheduler.canNotify()) return@withTimeout
                    val quote = repository.randomQuote()
                    val launch = PendingIntent.getActivity(context, Constants.NOTIFICATION_ID, Intent(context, MainActivity::class.java),
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                    val notification = NotificationCompat.Builder(context, Constants.CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_quote).setContentTitle("Daily quote").setSubText(quote.author).setContentText(quote.quote)
                        .setOnlyAlertOnce(true).setAutoCancel(true).setContentIntent(launch)
                    if (quote.quote.length <= 350 && preferences[booleanPreferencesKey(Constants.IS_IMAGE_NOTIFICATION_STYLE)] != false) {
                        val image = QuoteImage.render(context, quote, false, 720)
                        val style = NotificationCompat.BigPictureStyle().bigPicture(image).setSummaryText(quote.quote)
                        if (android.os.Build.VERSION.SDK_INT >= 31) style.showBigPictureWhenCollapsed(true)
                            .setContentDescription("Quote by ${quote.author}")
                        notification.setLargeIcon(image).setStyle(style)
                    } else notification.setStyle(NotificationCompat.BigTextStyle().bigText(quote.quote))
                    try { NotificationManagerCompat.from(context).notify(Constants.NOTIFICATION_ID, notification.build()) }
                    catch (_: SecurityException) { /* Permission may be revoked while building. */ }
                }
            } catch (_: Exception) { /* Failure must never leave a BroadcastReceiver pending. */ }
            finally {
                try { withTimeout(1500) { scheduler.reconcile() } }
                finally { pending.finish() }
            }
        }
    }
}

class ReminderRecoveryReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in setOf(Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED, Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED)) return
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try { withTimeout(8000) { ReminderScheduler(context).reconcile() } }
            finally { pending.finish() }
        }
    }
}
