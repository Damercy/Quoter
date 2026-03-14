package com.dayaonweb.quoter.data.repository

import com.dayaonweb.quoter.domain.models.Preferences
import com.dayaonweb.quoter.domain.models.UiQuote
import kotlinx.coroutines.flow.Flow

interface PreferencesRepo {
    fun getPreferences(): Flow<Preferences>
    suspend fun togglePreference(key: String, value: Any): Boolean
}