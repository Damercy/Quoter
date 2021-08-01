package com.dayaonweb.quoter.view.ui.browse

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dayaonweb.quoter.service.model.QuotesTagsResponseItem
import com.dayaonweb.quoter.service.repository.QuotesRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AllQuotesByTagViewModel : ViewModel() {

    private val _allQuotesByTags = MutableLiveData<Array<QuotesTagsResponseItem>>()
    val allQuotesByTag: LiveData<Array<QuotesTagsResponseItem>> = _allQuotesByTags

    fun getAllQuotes() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val response = QuotesRepo.getAllQuoteTags()
                    _allQuotesByTags.postValue(response.toTypedArray())
                } catch (exception: Exception) {
                    Log.e(TAG, "getAllQuotes: ${exception.message}", exception.cause)
                }
            }
        }
    }


    companion object {
        private const val TAG = "AllQuotesByTagViewModel"
    }
}