package com.dayaonweb.quoter.domain.models

import java.util.*

data class Preferences(
    val isNotificationOn: Boolean,
    val isImageStyleNotification: Boolean,
    val notificationTime: String,
    val ttsLanguage: Locale,
    val speechRate: Float,
    val isDarkMode: Boolean
)
