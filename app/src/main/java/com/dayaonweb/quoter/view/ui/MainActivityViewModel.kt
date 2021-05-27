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


private const val TAG = "MainActivityViewModel"

class MainActivityViewModel(context: Context) : ViewModel() {


    fun initUser(context: Context) {
        context.dataStore
        viewModelScope.launch {
            if (context.readFromPreferences(Constants.USER_ID).isNullOrEmpty()) {
                val userId = FirebaseInstallations.getInstance().id
                userId.addOnCompleteListener {
                    if (it.isSuccessful && it.result != null) {
                        viewModelScope.launch {
                            context.writeToPreferences(Constants.USER_ID, it.result)
                        }
                        initUserData(it.result)
                    }
                }
            }
        }


    }

    private fun initUserData(userId: String?) {
        if (userId.isNullOrEmpty())
            return
        val data = hashMapOf("name" to userId.subSequence(0, 10).toString())
        Firebase.firestore.collection("users")
            .document(userId)
            .set(data, SetOptions.merge())
    }



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