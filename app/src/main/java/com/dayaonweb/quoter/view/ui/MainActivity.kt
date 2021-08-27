package com.dayaonweb.quoter.view.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.dayaonweb.quoter.R
import com.dayaonweb.quoter.analytics.Analytics
import com.dayaonweb.quoter.constants.Constants
import com.dayaonweb.quoter.data.local.DataStoreManager
import com.dayaonweb.quoter.data.local.settingsDatastore
import com.dayaonweb.quoter.databinding.ActivityMainBinding
import com.dayaonweb.quoter.extensions.showSnack
import com.dayaonweb.quoter.service.broadcast.QuoteBroadcast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        Analytics.init(this)
        CoroutineScope(Dispatchers.IO).launch {
            val isNotificationOn = DataStoreManager.getBooleanValue(this@MainActivity, Constants.IS_NOTIFICATION_ON, true)
            if(isNotificationOn) {
                val notificationTime = DataStoreManager.getStringValue(
                    this@MainActivity,
                    Constants.NOTIFICATION_TIME,
                    "9:00"
                )
                val time = notificationTime.split(":")
                setAlarm(time[0].toInt(), time[1].toInt())
            }
        }
    }


    private fun setAlarm(hour: Int, minute: Int) {
        val calendar = Calendar.getInstance()
        calendar.apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val broadcastIntent = Intent(this, QuoteBroadcast::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            Constants.PENDING_INTENT_REQ_CODE,
            broadcastIntent,
            0
        )
        if (calendar.before(Calendar.getInstance()))
            calendar.add(Calendar.DATE, 1)
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }


}