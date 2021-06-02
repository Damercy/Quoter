package com.dayaonweb.quoter.view.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.dayaonweb.quoter.service.model.Author
import com.dayaonweb.quoter.service.repository.QuotesRepo
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    val quotes = QuotesRepo.getQuotes().cachedIn(viewModelScope)
    private val _author = MutableLiveData<Author>()
    val author: LiveData<Author> = _author


    fun fetchAuthorDetailsBySlug(slug: String, limit: Int? = null, page: Int? = null) {
        viewModelScope.launch {
            QuotesRepo.getAuthorBySlug(slug, limit,page).results?.let {
                _author.postValue(it[0])
            }
        }
    }
}