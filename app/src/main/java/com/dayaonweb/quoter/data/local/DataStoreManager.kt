package com.dayaonweb.quoter.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

// Retain the v3 file name so existing reminder, theme and speech choices survive updates.
val Context.settingsDatastore: DataStore<Preferences> by preferencesDataStore(name = "settings")
