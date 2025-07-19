package com.dayaonweb.quoter.view.ui.browse

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dayaonweb.quoter.domain.repository.Repository
import kotlinx.coroutines.launch

class AllQuotesByTagViewModel(
    private val repository: Repository
) : ViewModel() {

    private val _allQuotesByTags = MutableLiveData<List<String>>()
    val allQuotesByTag: LiveData<List<String>> = _allQuotesByTags


    init {
        getAllQuotes()
    }

    private fun getAllQuotes() {
        viewModelScope.launch {
            _allQuotesByTags.postValue(repository.fetchQuotesTag())
        }
    }
}