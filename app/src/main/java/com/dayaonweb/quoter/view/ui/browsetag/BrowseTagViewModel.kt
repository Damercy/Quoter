package com.dayaonweb.quoter.view.ui.browsetag

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dayaonweb.quoter.service.model.Quotes
import com.dayaonweb.quoter.service.repository.QuotesRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream


class BrowseTagViewModel : ViewModel() {

    var isFetchingQuotes = false

    private val _quotes = MutableLiveData<Quotes>()
    val quotes: LiveData<Quotes> = _quotes

    private val _ssFile = MutableLiveData<File>()
    val ssFile: LiveData<File> = _ssFile

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

}