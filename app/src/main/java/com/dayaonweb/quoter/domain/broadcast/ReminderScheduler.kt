package com.dayaonweb.quoter.domain.broadcast

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.dayaonweb.quoter.data.local.settingsDatastore
import com.dayaonweb.quoter.domain.constants.Constants
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.first
import java.util.Calendar

object ReminderTime {
    fun parse(value: String): Pair<Int, Int> {
        val parts = value.split(':')
        val hour = parts.getOrNull(0)?.toIntOrNull()
        val minute = parts.getOrNull(1)?.toIntOrNull()
        return if (hour != null && hour in 0..23 && minute != null && minute in 0..59) hour to minute else 9 to 0
    }
    fun next(now: Calendar, hour: Int, minute: Int): Long = (now.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, minute); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        if (timeInMillis <= now.timeInMillis) add(Calendar.DATE, 1)
    }.timeInMillis
}

class ReminderScheduler(private val context: Context) {
    fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(NotificationChannel(Constants.CHANNEL_ID, "Daily quotes", NotificationManager.IMPORTANCE_DEFAULT))
    }
    fun canNotify(): Boolean {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return false
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        return Build.VERSION.SDK_INT < 26 || context.getSystemService(NotificationManager::class.java)
            .getNotificationChannel(Constants.CHANNEL_ID)?.importance != NotificationManager.IMPORTANCE_NONE
    }
    private fun pending() = PendingIntent.getBroadcast(context, Constants.PENDING_INTENT_REQ_CODE,
        Intent(context, QuoteBroadcast::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    suspend fun reconcile() {
        createChannel()
        val alarm = context.getSystemService(AlarmManager::class.java)
        alarm.cancel(pending())
        val settings = context.settingsDatastore.data.first()
        if (settings[booleanPreferencesKey(Constants.IS_NOTIFICATION_ON)] == false || !canNotify()) return
        val (hour, minute) = ReminderTime.parse(settings[stringPreferencesKey(Constants.NOTIFICATION_TIME)] ?: "9:00")
        alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, ReminderTime.next(Calendar.getInstance(), hour, minute), pending())
    }
}
