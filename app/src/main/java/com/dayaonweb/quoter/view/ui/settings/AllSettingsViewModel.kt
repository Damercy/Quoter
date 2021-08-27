package com.dayaonweb.quoter.view.ui.settings

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dayaonweb.quoter.constants.Constants
import com.dayaonweb.quoter.data.local.DataStoreManager
import com.dayaonweb.quoter.data.local.models.Preferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class AllSettingsViewModel : ViewModel() {

    private val _preferences = MutableLiveData<Preferences>()
    val preferences: LiveData<Preferences> = _preferences

    fun getAllPreferences(context: Context) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val isImageStyled = DataStoreManager.getBooleanValue(
                    context,
                    Constants.IS_IMAGE_NOTIFICATION_STYLE,
                    true
                )
                val isNotificationOn =
                    DataStoreManager.getBooleanValue(context, Constants.IS_NOTIFICATION_ON, true)
                val selectedTtsLanguage =
                    DataStoreManager.getStringValue(context, Constants.TTS_LANGUAGE, "en_IN")
                        .split("_")
                val notifTime =
                    DataStoreManager.getStringValue(context, Constants.NOTIFICATION_TIME, "9:00")
                _preferences.postValue(
                    Preferences(
                        isNotificationOn = isNotificationOn,
                        isImageStyleNotification = isImageStyled,
                        notificationTime = notifTime,
                        ttsLanguage = Locale(selectedTtsLanguage[0], selectedTtsLanguage[1])
                    )
                )
            }
        }
    }

    fun toggleNotification(context: Context, isOn: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                DataStoreManager.saveValue(context, Constants.IS_NOTIFICATION_ON, isOn)
            }
        }
    }

    fun toggleNotificationStyle(context: Context, isImage: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                DataStoreManager.saveValue(context, Constants.IS_IMAGE_NOTIFICATION_STYLE, isImage)
            }
        }
    }

    fun updateTtsLanguage(context: Context, locale: Locale) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                DataStoreManager.saveValue(
                    context,
                    Constants.TTS_LANGUAGE,
                    "${locale.language}_${locale.country}"
                )
            }
        }
    }

    fun updateNotifTime(context: Context, newTime: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                DataStoreManager.saveValue(context, Constants.NOTIFICATION_TIME, newTime)
            }
        }
    }
}