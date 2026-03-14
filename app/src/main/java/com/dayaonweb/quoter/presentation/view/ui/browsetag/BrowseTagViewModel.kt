package com.dayaonweb.quoter.presentation.view.ui.browsetag

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import javax.inject.Inject
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.Dispatchers
import java.io.FileOutputStream


@HiltViewModel
class BrowseTagViewModel @Inject constructor(
    private val quotesRepo: QuotesRepo,
    private val preferencesRepo: PreferencesRepo
) : ViewModel() {

    private val _quotes = MutableLiveData<List<UiQuote>>()
    val quotes: LiveData<List<UiQuote>> = _quotes

    val preferences = preferencesRepo.getPreferences().asLiveData()

    private val _ssFile = MutableLiveData<File>()
    val ssFile: LiveData<File> = _ssFile


    fun fetchQuotesByTag(tag: String) {
        viewModelScope.launch {
            val response =
                quotesRepo.getQuotesByTags(listOf(tag)).firstOrNull() ?: emptyList()
            _quotes.postValue(response)
        }
    }

    fun takeScreenShot(view: View, outputFile: File) {
       viewModelScope.launch(Dispatchers.IO){
           // 1. Create a bitmap with the same size as the view
           val bitmap = createBitmap(view.width, view.height)

           // 2. Use PixelCopy for Hardware Accelerated views (standard in modern Android)
           val location = IntArray(2)
           view.getLocationInWindow(location)

           try {
               if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                   PixelCopy.request(
                       (view.context as Activity).window,
                       Rect(location[0], location[1], location[2] + view.width, location[1] + view.height),
                       bitmap,
                       { copyResult ->
                           if (copyResult == PixelCopy.SUCCESS) {
                               saveBitmapToFile(bitmap, outputFile)
                           } else {
                               drawCanvasFallback(view, outputFile)
                           }
                       },
                       Handler(Looper.getMainLooper())
                   )
               }else{
                   drawCanvasFallback(view, outputFile)
               }
           } catch (_: Exception) {
               drawCanvasFallback(view, outputFile)
           }
       }
    }

    fun drawCanvasFallback(view: View, file: File) {
        viewModelScope.launch(Dispatchers.IO) {
            val bitmap = createBitmap(view.width, view.height)
            val canvas = Canvas(bitmap)
            view.draw(canvas)
            saveBitmapToFile(bitmap,file)
        }
    }

    private fun saveBitmapToFile(bitmap: Bitmap, file: File) {
        viewModelScope.launch(Dispatchers.IO){
            var fos: FileOutputStream? = null
            try {
                fos = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                _ssFile.postValue(file)
            } catch (_: Exception) {
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