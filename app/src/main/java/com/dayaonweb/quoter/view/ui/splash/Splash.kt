package com.dayaonweb.quoter.view.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.dayaonweb.quoter.R
import com.dayaonweb.quoter.extensions.hideSystemUI
import com.dayaonweb.quoter.view.ui.MainActivity

class Splash : AppCompatActivity() {

    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        supportActionBar?.hide()
        window.statusBarColor = ContextCompat.getColor(this,R.color.black)
        handler = Handler(Looper.getMainLooper())
        hideSystemUI()
        handler.postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finishAfterTransition()
        }, 3000)
    }


    override fun onStop() {
        super.onStop()
        window.statusBarColor = ContextCompat.getColor(this,R.color.design_default_color_primary_variant)
        handler.removeCallbacksAndMessages(null)
    }
}