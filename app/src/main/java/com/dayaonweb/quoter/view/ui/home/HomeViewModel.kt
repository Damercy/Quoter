package com.dayaonweb.quoter.view.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.dayaonweb.quoter.service.model.Author
import com.dayaonweb.quoter.service.repository.QuotesRepo
import kotlinx.coroutines.launch

private const val TAG = "HomeViewModel"

class HomeViewModel : ViewModel() {
    val quotes = QuotesRepo.getQuotes().cachedIn(viewModelScope)
    private val _author = MutableLiveData<Author>()
    private val _authorImage = MutableLiveData<String>()
    val author: LiveData<Author> = _author
    val authorImage: LiveData<String> = _authorImage


    fun fetchAuthorDetailsBySlug(slug: String, limit: Int? = null, page: Int? = null) {
        if (!_author.value?.id.isNullOrEmpty())
            return
        viewModelScope.launch {
            QuotesRepo.getAuthorBySlug(slug, limit, page).results?.let {
                _author.postValue(it[0])
            }
        }
    }

    fun fetchAuthorImage(authorName: String) {
        if (!_authorImage.value.isNullOrEmpty())
            return
        viewModelScope.launch {
            QuotesRepo.getAuthorImageResponse(authorName).let {
                val pages = it.query?.pages
                val keyValue = pages?.get(pages.keys.toIntArray()[0])
                val authorImageUrl = keyValue?.original?.source
                authorImageUrl?.let { url ->
                    Log.d(TAG, "fetchAuthorImage: Url=$authorImageUrl")
                    _authorImage.postValue(url)
                }
            }
        }
    }
}