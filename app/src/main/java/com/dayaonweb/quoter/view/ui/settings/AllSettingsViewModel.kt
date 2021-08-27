package com.dayaonweb.quoter.view.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.*

class AllSettingsViewModel: ViewModel() {
    private val _isNotificationSwitchOn = MutableLiveData<Boolean>()
    private val _selectedTtsLanguage = MutableLiveData<Locale>()
    private val _notificationTime = MutableLiveData<String>()

    val isNotificationSwitchOn: LiveData<Boolean> = _isNotificationSwitchOn
    val selectedTtsLanguage: LiveData<Locale> = _selectedTtsLanguage
    val notificationTime: LiveData<String> = _notificationTime
}