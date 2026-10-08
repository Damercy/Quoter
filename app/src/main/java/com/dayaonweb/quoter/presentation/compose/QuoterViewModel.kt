package com.dayaonweb.quoter.presentation.compose

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dayaonweb.quoter.data.local.settingsDatastore
import com.dayaonweb.quoter.data.repository.QuotesRepoImpl
import com.dayaonweb.quoter.domain.broadcast.ReminderScheduler
import com.dayaonweb.quoter.domain.broadcast.ReminderTime
import com.dayaonweb.quoter.domain.constants.Constants
import com.dayaonweb.quoter.domain.models.UiQuote
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppSettings(val theme: String = "System", val reminders: Boolean = true, val time: String = "9:00",
    val imageNotification: Boolean = true, val locale: String = "en_IN", val voice: String = "", val rate: Float = 1f,
    val glassEnabled: Boolean = true, val glassTransparency: Float = 0.6f, val reviewRequests: Boolean = true)
data class QuoterState(val quotes: List<UiQuote> = emptyList(), val saved: Set<String> = emptySet(),
    val settings: AppSettings = AppSettings(), val sourceStatus: String = "Loading local quotes")

@HiltViewModel
class QuoterViewModel @Inject constructor(@ApplicationContext private val context: Context,
    private val repository: QuotesRepoImpl) : ViewModel() {
    private val preferences = context.settingsDatastore.data.map { values ->
        val legacyDark = values[booleanPreferencesKey(Constants.IS_DARK_MODE)]
        val (hour, minute) = ReminderTime.parse(values[stringPreferencesKey(Constants.NOTIFICATION_TIME)] ?: "9:00")
        AppSettings(theme = values[stringPreferencesKey("THEME_MODE")] ?: legacyDark?.let { if (it) "Dark" else "Light" } ?: "System",
            reminders = values[booleanPreferencesKey(Constants.IS_NOTIFICATION_ON)] ?: true,
            time = "$hour:${minute.toString().padStart(2,'0')}",
            imageNotification = values[booleanPreferencesKey(Constants.IS_IMAGE_NOTIFICATION_STYLE)] ?: true,
            locale = values[stringPreferencesKey(Constants.TTS_LANGUAGE)] ?: "en_IN",
            voice = values[stringPreferencesKey("TTS_VOICE")] ?: "",
            rate = (values[floatPreferencesKey(Constants.TTS_SPEECH_RATE)] ?: 1f).takeIf { it.isFinite() }?.coerceIn(0.5f,2f) ?: 1f,
            glassEnabled = values[booleanPreferencesKey("GLASS_ENABLED")] ?: true,
            glassTransparency = (values[floatPreferencesKey("GLASS_TRANSPARENCY")] ?: 0.6f).takeIf { it.isFinite() }?.coerceIn(0f,1f) ?: 0.6f,
            reviewRequests = values[booleanPreferencesKey("REVIEW_REQUESTS")] ?: true)
    }
    val state = combine(repository.allQuotes, repository.savedIds, preferences, repository.status) { quotes, saved, settings, status ->
        QuoterState(quotes, saved, settings, status)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), QuoterState())

    fun save(id: String, saved: Boolean) { viewModelScope.launch { repository.setSaved(id, saved) } }
    fun theme(theme: String) { viewModelScope.launch { context.settingsDatastore.edit { it[stringPreferencesKey("THEME_MODE")] = theme } } }
    fun reminder(enabled: Boolean) { viewModelScope.launch {
        context.settingsDatastore.edit { it[booleanPreferencesKey(Constants.IS_NOTIFICATION_ON)] = enabled }
        ReminderScheduler(context).reconcile()
    } }
    fun time(hour: Int, minute: Int) { viewModelScope.launch {
        context.settingsDatastore.edit { it[stringPreferencesKey(Constants.NOTIFICATION_TIME)] = "$hour:$minute" }
        ReminderScheduler(context).reconcile()
    } }
    fun imageNotification(enabled: Boolean) { viewModelScope.launch { context.settingsDatastore.edit { it[booleanPreferencesKey(Constants.IS_IMAGE_NOTIFICATION_STYLE)] = enabled } } }
    fun glass(enabled: Boolean) { viewModelScope.launch { context.settingsDatastore.edit { it[booleanPreferencesKey("GLASS_ENABLED")] = enabled } } }
    fun reviewRequests(enabled: Boolean) { viewModelScope.launch { context.settingsDatastore.edit { it[booleanPreferencesKey("REVIEW_REQUESTS")] = enabled } } }
    fun glassTransparency(value: Float) { viewModelScope.launch { context.settingsDatastore.edit {
        it[floatPreferencesKey("GLASS_TRANSPARENCY")] = value.takeIf { it.isFinite() }?.coerceIn(0f,1f) ?: 0.6f
    } } }
    fun rate(value: Float) { viewModelScope.launch { context.settingsDatastore.edit { it[floatPreferencesKey(Constants.TTS_SPEECH_RATE)] = value.coerceIn(0.5f,2f) } } }
    fun voice(name: String, locale: String) { viewModelScope.launch { context.settingsDatastore.edit {
        it[stringPreferencesKey("TTS_VOICE")] = name; it[stringPreferencesKey(Constants.TTS_LANGUAGE)] = locale
    } } }
}
