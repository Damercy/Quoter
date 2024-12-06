package com.dayaonweb.quoter.view.ui.splash

import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView
import com.dayaonweb.quoter.R
import com.dayaonweb.quoter.extensions.hideSystemUI
import com.dayaonweb.quoter.view.ui.MainActivity

class Splash : AppCompatActivity() {

    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        supportActionBar?.hide()
        val view = findViewById<LottieAnimationView>(R.id.lottie_view)
        view.animate().alpha(1.0f).setDuration(400L).setListener(object : Animator.AnimatorListener{
            override fun onAnimationStart(animation: Animator) {
            }

            override fun onAnimationEnd(animation: Animator) {
                view.animate().alpha(0.0f).start()
            }

            override fun onAnimationCancel(animation: Animator) {
            }

            override fun onAnimationRepeat(animation: Animator) {
            }
        })
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        handler = Handler(Looper.getMainLooper())
        hideSystemUI()
        handler.postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finishAfterTransition()
        }, 2000)
    }


    override fun onStop() {
        super.onStop()
        handler.removeCallbacksAndMessages(null)
    }
}