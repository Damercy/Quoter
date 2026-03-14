package com.dayaonweb.quoter.presentation.view.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.dayaonweb.quoter.domain.constants.Constants
import com.dayaonweb.quoter.data.repository.PreferencesRepo
import com.dayaonweb.quoter.domain.models.Preferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AllSettingsViewModel @Inject constructor(
    private val preferencesRepo: PreferencesRepo
) : ViewModel() {

    val preferences: LiveData<Preferences> = preferencesRepo.getPreferences()
        .asLiveData()

    fun toggleNotification(isOn: Boolean) {
        viewModelScope.launch {
            preferencesRepo.togglePreference(Constants.IS_NOTIFICATION_ON, isOn)
        }
    }

    fun toggleDarkMode(wantDarkMode: Boolean) {
        viewModelScope.launch {
            preferencesRepo.togglePreference(Constants.IS_DARK_MODE, wantDarkMode)
        }
    }

    fun toggleNotificationStyle(isImage: Boolean) {
        viewModelScope.launch {
            preferencesRepo.togglePreference(Constants.IS_IMAGE_NOTIFICATION_STYLE, isImage)
        }
    }

    fun updateTtsLanguage(locale: Locale) {
        viewModelScope.launch {
            preferencesRepo.togglePreference(
                Constants.TTS_LANGUAGE,
                "${locale.language}_${locale.country}"
            )
        }
    }

    fun updateNotifTime(newTime: String) {
        viewModelScope.launch {
            preferencesRepo.togglePreference(Constants.NOTIFICATION_TIME, newTime)
        }
    }

    fun updateTtsSpeechRate(rate: Float) {
        viewModelScope.launch {
            preferencesRepo.togglePreference(Constants.TTS_SPEECH_RATE, rate)
        }
    }
}