package com.dayaonweb.quoter.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.settingsDatastore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object DataStoreManager {

    suspend fun saveValue(context: Context, key: String, value: String) {
        val dataKey = stringPreferencesKey(key)
        context.settingsDatastore.edit {
            it[dataKey] = value
        }
    }

    suspend fun saveValue(context: Context, key: String, value: Boolean) {
        val dataKey = booleanPreferencesKey(key)
        context.settingsDatastore.edit {
            it[dataKey] = value
        }
    }

    suspend fun saveValue(context: Context, key: String, value: Float) {
        val dataKey = floatPreferencesKey(key)
        context.settingsDatastore.edit {
            it[dataKey] = value
        }
    }

    suspend fun getStringValue(context: Context, key: String, defaultValue: String): String {
        val dataKey = stringPreferencesKey(key)
        return context.settingsDatastore.data.map {
            it[dataKey] ?: defaultValue
        }.first()
    }

    suspend fun getBooleanValue(context: Context, key: String, defaultValue: Boolean): Boolean {
        val dataKey = booleanPreferencesKey(key)
        return context.settingsDatastore.data.map {
            it[dataKey] ?: defaultValue
        }.first()
    }

    suspend fun getFloatValue(context: Context, key: String, defaultValue: Float): Float {
        val dataKey = floatPreferencesKey(key)
        return context.settingsDatastore.data.map {
            it[dataKey] ?: defaultValue
        }.first()
    }
}