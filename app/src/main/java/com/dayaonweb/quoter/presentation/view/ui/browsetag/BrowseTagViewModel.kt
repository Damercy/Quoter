package com.dayaonweb.quoter.presentation.view.ui.browsetag

import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.dayaonweb.quoter.domain.constants.Constants
import com.dayaonweb.quoter.data.repository.PreferencesRepo
import com.dayaonweb.quoter.data.repository.QuotesRepo
import com.dayaonweb.quoter.domain.models.UiQuote
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.*
import javax.inject.Inject


@HiltViewModel
class BrowseTagViewModel @Inject constructor(
    private val quotesRepo: QuotesRepo,
    private val preferencesRepo: PreferencesRepo
) : ViewModel() {

    var isFetchingQuotes = false

    private val _quotes = MutableLiveData<List<UiQuote>>()
    val quotes: LiveData<List<UiQuote>> = _quotes

    val preferences = preferencesRepo.getPreferences().asLiveData()

    private val _ssFile = MutableLiveData<File>()
    val ssFile: LiveData<File> = _ssFile


    fun fetchQuotesByTag(tag: String, pageNo: Int) {
        viewModelScope.launch {
            isFetchingQuotes = true
            val response =
                quotesRepo.getQuotesByTags(listOf(tag), pageNo).firstOrNull() ?: emptyList()
            _quotes.postValue(response)
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

    fun updateTtsLanguage(locale: Locale) {
        viewModelScope.launch {
            preferencesRepo.togglePreference(
                Constants.TTS_LANGUAGE,
                "${locale.language}_${locale.country}"
            )
        }
    }

}