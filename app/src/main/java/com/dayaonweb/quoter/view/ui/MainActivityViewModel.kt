package com.dayaonweb.quoter.view.ui

import android.content.Context
import android.content.res.Configuration
import android.view.MenuItem
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dayaonweb.quoter.constants.Constants
import com.dayaonweb.quoter.extensions.dataStore
import com.dayaonweb.quoter.extensions.readFromPreferences
import com.dayaonweb.quoter.extensions.writeToPreferences
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlin.random.Random


class MainActivityViewModel : ViewModel() {


    fun themeOnClickListener(context: Context) {
        val isNightTheme =
            context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (isNightTheme) {
            Configuration.UI_MODE_NIGHT_YES -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            Configuration.UI_MODE_NIGHT_NO ->
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    fun tintMenuIcon(context: Context, item: MenuItem, color: Int) {
        val normalDrawable = item.icon
        val wrapDrawable = DrawableCompat.wrap(normalDrawable)
        DrawableCompat.setTint(wrapDrawable, context.resources.getColor(color, context.theme))
        item.icon = wrapDrawable
    }

}