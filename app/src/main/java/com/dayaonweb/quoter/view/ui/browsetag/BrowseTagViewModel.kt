package com.dayaonweb.quoter.view.ui.browsetag

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dayaonweb.quoter.constants.Constants
import com.dayaonweb.quoter.data.local.DataStoreManager
import com.dayaonweb.quoter.data.local.models.Preferences
import com.dayaonweb.quoter.data.remote.model.Quote
import com.dayaonweb.quoter.domain.repository.Repository
import com.dayaonweb.quoter.domain.tts.Speaker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.*


class BrowseTagViewModel(
    private val speaker: Speaker,
    private val repository: Repository
) : ViewModel() {

    var isFetchingQuotes = false

    private val _quotes = MutableLiveData<List<Quote?>>()
    val quotes: LiveData<List<Quote?>> = _quotes

    private val _ssFile = MutableLiveData<File>()
    val ssFile: LiveData<File> = _ssFile

    private val _preferences = MutableLiveData<Preferences>()
    val preferences: LiveData<Preferences> = _preferences

    val isSpeaking = speaker.onSpeaking
    val isSpeakError = speaker.onError

    fun fetchQuotesByTag(tag: String, pageNo: Int) {
        viewModelScope.launch {
            isFetchingQuotes = true
            _quotes.postValue(repository.fetchQuotesByTag(tag, pageNo))
            isFetchingQuotes = false
        }
    }

    fun takeScreenShot(view: View, file: File) {
        viewModelScope.launch(Dispatchers.IO) {
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            view.draw(canvas)
            var fos: FileOutputStream? = null
            try {
                fos = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                _ssFile.postValue(file)
            } catch (exception: Exception) {
                Log.e("SS error", exception.message ?: "")
            } finally {
                fos?.flush()
                fos?.close()
            }
        }

    }

    fun getAllPreferences() {
        viewModelScope.launch {
            _preferences.postValue(repository.getUserPreferences())
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


    fun setSpeakerLanguage(locale: Locale) {
        speaker.updateLanguage(locale)
    }

    fun speak(quote: String) {
        speaker.speak(quote)
    }

    fun stop() {
        speaker.stop()
    }

}