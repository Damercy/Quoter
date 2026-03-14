package com.dayaonweb.quoter.presentation.view.ui

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.dayaonweb.quoter.domain.analytics.Analytics
import com.dayaonweb.quoter.domain.constants.Constants
import com.dayaonweb.quoter.data.local.DataStoreManager
import com.dayaonweb.quoter.databinding.ActivityMainBinding
import com.dayaonweb.quoter.domain.broadcast.QuoteBroadcast
import com.dayaonweb.quoter.domain.tts.QuoteSpeaker
import dagger.Lazy
import dagger.hilt.android.AndroidEntryPoint

import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var dataStoreManager: Lazy<DataStoreManager>

    @Inject
    lateinit var quoteSpeaker: QuoteSpeaker

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (!isGranted) {
                Toast.makeText(
                    this,
                    "Please grant notification permission to get local notifications.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Analytics.init(this)
        initNotifications()
        initAppTheme()
        checkNotificationPermission()
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Notification permission is granted, yay!
                }

                ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) -> {
                    Toast.makeText(
                        this,
                        "Please grant notification permission to get local notifications.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {
                    // Ask permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun initAppTheme() {
        lifecycleScope.launch {
            val isDarkMode = dataStoreManager.get().getBooleanValue(
                this@MainActivity,
                Constants.IS_DARK_MODE,
                AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
            )
            AppCompatDelegate.setDefaultNightMode(if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun initNotifications() {
        lifecycleScope.launch {
            val isNotificationOn = dataStoreManager.get().getBooleanValue(
                this@MainActivity,
                Constants.IS_NOTIFICATION_ON,
                true
            )
            if (isNotificationOn) {
                val notificationTime = dataStoreManager.get().getStringValue(
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
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val broadcastIntent = Intent(this, QuoteBroadcast::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            Constants.PENDING_INTENT_REQ_CODE,
            broadcastIntent,
            PendingIntent.FLAG_MUTABLE
        )
        if (calendar.before(Calendar.getInstance()))
            calendar.add(Calendar.DATE, 1)
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,calendar.timeInMillis,pendingIntent)
    }


}