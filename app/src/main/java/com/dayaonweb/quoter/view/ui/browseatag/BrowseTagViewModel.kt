package com.dayaonweb.quoter.view.ui.browseatag

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

    private val _quotes = MutableLiveData<Quotes>()
    val quotes: LiveData<Quotes> = _quotes

    fun fetchQuotesByTag(tag: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val response = QuotesRepo.getQuotesByTags(listOf(tag))
                    _quotes.postValue(response)

                } catch (exception: Exception) {
                    Log.e(TAG, "fetchQuotesByTag: ${exception.message}", exception.cause)
                }
            }
        }
    }

    companion object {
        private const val TAG = "BrowseTagViewModel"
    }
}