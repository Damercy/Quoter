package com.dayaonweb.quoter.data.repository

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.dayaonweb.quoter.data.local.DataStoreManager
import com.dayaonweb.quoter.domain.constants.Constants
import com.dayaonweb.quoter.domain.models.Preferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Locale
import javax.inject.Inject

class PreferencesRepoImpl @Inject constructor(
    private val context: Context,
    private val dataStoreManager: DataStoreManager
) : PreferencesRepo {

    override fun getPreferences(): Flow<Preferences> = flow {
        val isImageStyled = dataStoreManager.getBooleanValue(
            context,
            Constants.IS_IMAGE_NOTIFICATION_STYLE,
            true
        )
        val isNotificationOn =
            dataStoreManager.getBooleanValue(context, Constants.IS_NOTIFICATION_ON, true)
        val selectedTtsLanguage =
            dataStoreManager.getStringValue(context, Constants.TTS_LANGUAGE, "en_IN")
                .split("_")
        val notifTime =
            dataStoreManager.getStringValue(context, Constants.NOTIFICATION_TIME, "9:00")
        val speechRate =
            dataStoreManager.getFloatValue(context, Constants.TTS_SPEECH_RATE, 1.0f)
        val isDarkMode =
            dataStoreManager.getBooleanValue(
                context,
                Constants.IS_DARK_MODE,
                AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
            )
        emit(
            Preferences(
                isNotificationOn = isNotificationOn,
                isImageStyleNotification = isImageStyled,
                notificationTime = notifTime,
                ttsLanguage = Locale(selectedTtsLanguage[0], selectedTtsLanguage[1]),
                speechRate = speechRate,
                isDarkMode = isDarkMode
            )
        )
    }

    override suspend fun togglePreference(key: String, value: Any): Boolean {
        return when (value) {
            is Boolean -> {
                dataStoreManager.saveValue(context, key, value)
                true
            }

            is String -> {
                dataStoreManager.saveValue(context, key, value)
                true
            }

            is Float -> {
                dataStoreManager.saveValue(context, key, value)
                true
            }

            else -> {
                false
            }
        }
    }
}