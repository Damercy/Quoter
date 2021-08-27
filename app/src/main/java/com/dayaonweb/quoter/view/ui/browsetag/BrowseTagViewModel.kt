package com.dayaonweb.quoter.view.ui.browsetag

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dayaonweb.quoter.constants.Constants
import com.dayaonweb.quoter.data.local.DataStoreManager
import com.dayaonweb.quoter.data.local.models.Preferences
import com.dayaonweb.quoter.service.model.Quotes
import com.dayaonweb.quoter.service.repository.QuotesRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.*


class BrowseTagViewModel : ViewModel() {

    var isFetchingQuotes = false

    private val _quotes = MutableLiveData<Quotes>()
    val quotes: LiveData<Quotes> = _quotes

    private val _ssFile = MutableLiveData<File>()
    val ssFile: LiveData<File> = _ssFile

    private val _preferences = MutableLiveData<Preferences>()
    val preferences: LiveData<Preferences> = _preferences

    fun fetchQuotesByTag(tag: String, pageNo: Int) {
        viewModelScope.launch {
            isFetchingQuotes = true
            withContext(Dispatchers.IO) {
                try {
                    val response = QuotesRepo.getQuotesByTags(listOf(tag), pageNo)
                    _quotes.postValue(response)
                    isFetchingQuotes = false

                } catch (exception: Exception) {
                    isFetchingQuotes = false
                }
            }
        }
    }

    fun takeScreenShot(view: View, file: File) {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            _ssFile.postValue(file)
        } catch (exception: Exception) {
        } finally {
            fos?.flush()
            fos?.close()
        }
    }

    fun getAllPreferences(context: Context) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val isImageStyled = DataStoreManager.getBooleanValue(
                    context,
                    Constants.IS_IMAGE_NOTIFICATION_STYLE,
                    true
                )
                val isNotificationOn =
                    DataStoreManager.getBooleanValue(context, Constants.IS_NOTIFICATION_ON, true)
                val selectedTtsLanguage =
                    DataStoreManager.getStringValue(context, Constants.TTS_LANGUAGE, "en_IN")
                        .split("_")
                val notifTime =
                    DataStoreManager.getStringValue(context, Constants.NOTIFICATION_TIME, "9:00")
                val speechRate =
                    DataStoreManager.getFloatValue(context, Constants.TTS_SPEECH_RATE, 1.0f)
                _preferences.postValue(
                    Preferences(
                        isNotificationOn = isNotificationOn,
                        isImageStyleNotification = isImageStyled,
                        notificationTime = notifTime,
                        ttsLanguage = Locale(selectedTtsLanguage[0], selectedTtsLanguage[1]),
                        speechRate = speechRate
                    )
                )
            }
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

}