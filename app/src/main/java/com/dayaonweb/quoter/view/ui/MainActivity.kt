package com.dayaonweb.quoter.view.ui

import android.Manifest
import android.animation.ObjectAnimator
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.animation.doOnEnd
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.dayaonweb.quoter.constants.Constants
import com.dayaonweb.quoter.data.local.DataStoreManager
import com.dayaonweb.quoter.databinding.ActivityMainBinding
import com.dayaonweb.quoter.domain.broadcast.QuoteBroadcast
import com.dayaonweb.quoter.domain.tts.Speaker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.get
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val speaker: Speaker = get()

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
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            try {
                val iconView = splashScreenView.iconView
                val slideUp = ObjectAnimator.ofFloat(
                    iconView,
                    View.TRANSLATION_Y,
                    0f,
                    -iconView.height.toFloat()
                )
                slideUp.apply {
                    interpolator = AnticipateInterpolator()
                    duration = 200L
                }
                slideUp.doOnEnd { splashScreenView.remove() }
                slideUp.start()
            }catch (e: Exception){
                // Do nothing
            }
        }
        initNotifications()
        initAppTheme()
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
        lifecycleScope.launch(Dispatchers.IO){
            val isDarkMode = DataStoreManager.getBooleanValue(
                this@MainActivity,
                Constants.IS_DARK_MODE,
                AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
            )
            withContext(Dispatchers.Main){
                AppCompatDelegate.setDefaultNightMode(if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

    private fun initNotifications() {
        lifecycleScope.launch(Dispatchers.IO){
            val isNotificationOn = DataStoreManager.getBooleanValue(
                this@MainActivity,
                Constants.IS_NOTIFICATION_ON,
                true
            )
            if (isNotificationOn) {
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
        val broadcastIntent = Intent(this, QuoteBroadcast::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            Constants.PENDING_INTENT_REQ_CODE,
            broadcastIntent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        if (calendar.before(Calendar.getInstance()))
            calendar.add(Calendar.DATE, 1)
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }


}