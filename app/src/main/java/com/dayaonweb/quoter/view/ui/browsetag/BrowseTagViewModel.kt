package com.dayaonweb.quoter.view.ui.browsetag

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dayaonweb.quoter.service.model.Quotes
import com.dayaonweb.quoter.service.repository.QuotesRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BrowseTagViewModel : ViewModel() {

    var isFetchingQuotes = false

    private val _quotes = MutableLiveData<Quotes>()
    val quotes: LiveData<Quotes> = _quotes

    fun fetchQuotesByTag(tag: String, pageNo: Int) {
        viewModelScope.launch {
            isFetchingQuotes = true
            withContext(Dispatchers.IO) {
                try {
                    val response = QuotesRepo.getQuotesByTags(listOf(tag),pageNo)
                    Log.d(TAG, "fetchQuotesByTag: response: $response")
                    _quotes.postValue(response)
                    isFetchingQuotes = false

                } catch (exception: Exception) {
                    isFetchingQuotes = false
                    Log.e(TAG, "fetchQuotesByTag: ${exception.message}", exception.cause)
                }
            }
        }
    }

    companion object {
        private const val TAG = "BrowseTagViewModel"
    }
}