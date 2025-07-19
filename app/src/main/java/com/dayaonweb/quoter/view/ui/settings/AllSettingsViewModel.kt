package com.dayaonweb.quoter.view.ui.settings

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dayaonweb.quoter.constants.Constants
import com.dayaonweb.quoter.data.local.DataStoreManager
import com.dayaonweb.quoter.data.local.models.Preferences
import com.dayaonweb.quoter.domain.repository.Repository
import com.dayaonweb.quoter.domain.tts.Speaker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class AllSettingsViewModel(
    private val speaker: Speaker,
    private val repository: Repository
) : ViewModel() {

    private val _preferences = MutableLiveData<Preferences>()
    val preferences: LiveData<Preferences> = _preferences

    fun getAllPreferences() {
        viewModelScope.launch {
            _preferences.postValue(repository.getUserPreferences())
        }
    }

    fun toggleNotification(context: Context, isOn: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                DataStoreManager.saveValue(context, Constants.IS_NOTIFICATION_ON, isOn)
            }
        }
    }

    fun toggleDarkMode(context: Context, wantDarkMode: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                DataStoreManager.saveValue(context, Constants.IS_DARK_MODE, wantDarkMode)
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
                    context, Constants.TTS_LANGUAGE, "${locale.language}_${locale.country}"
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

    fun updateTtsSpeechRate(context: Context, rate: Float) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                DataStoreManager.saveValue(context, Constants.TTS_SPEECH_RATE, rate)
            }
        }
    }

    fun getSpeakerSupportedLanguages() = speaker.getSupportedLanguages()

    fun speak(text: String) = speaker.speak(text)

    fun updateLanguage(locale: Locale) = speaker.updateLanguage(locale)

    fun updateSpeechRate(rate: Float) = speaker.updateSpeechRate(rate)
}