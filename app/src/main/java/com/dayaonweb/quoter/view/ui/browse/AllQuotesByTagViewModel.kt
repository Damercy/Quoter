package com.dayaonweb.quoter.view.ui.browse

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dayaonweb.quoter.service.model.AllQuotesGenreResponse
import com.dayaonweb.quoter.service.repository.QuotesRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AllQuotesByTagViewModel : ViewModel() {

    private val _allQuotesByTags = MutableLiveData<AllQuotesGenreResponse>()
    val allQuotesByTag: LiveData<AllQuotesGenreResponse> = _allQuotesByTags


    init {
        getAllQuotes()
    }

    private fun getAllQuotes() {
        viewModelScope.launch {
                try {
                    val response = QuotesRepo.getAllQuoteTags()
                    _allQuotesByTags.postValue(response)
                } catch (exception: Exception) {
                    Log.e(TAG, "getAllQuotes: ${exception.message}", exception.cause)
                }
        }
    }


    companion object {
        private const val TAG = "AllQuotesByTagViewModel"
    }
}