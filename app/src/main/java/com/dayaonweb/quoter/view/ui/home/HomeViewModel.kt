package com.dayaonweb.quoter.view.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.dayaonweb.quoter.enums.Status
import com.dayaonweb.quoter.service.model.Author
import com.dayaonweb.quoter.service.repository.QuotesRepo
import kotlinx.coroutines.launch

private const val TAG = "HomeViewModel"

class HomeViewModel : ViewModel() {
    val quotes = QuotesRepo.getQuotes().cachedIn(viewModelScope)
    private val _author = MutableLiveData<Author>()
    private val _authorImage = MutableLiveData<String>()
    private val _status = MutableLiveData<Status>()
    val author: LiveData<Author> = _author
    val authorImage: LiveData<String> = _authorImage
    val status: LiveData<Status> = _status


    fun fetchAuthorDetailsBySlug(slug: String, limit: Int? = null, page: Int? = null) {
        if (!_author.value?.id.isNullOrEmpty())
            return
        try {
            viewModelScope.launch {

                try {
                    QuotesRepo.getAuthorBySlug(slug, limit, page).results?.let {
                        _author.postValue(it[0])
                    }
                } catch (exception: Exception) {
                    Log.e(TAG, "fetchAuthorDetailsBySlug: ${exception.message}")
                    _status.postValue(Status.READ_FAIL)
                }
            }
        } catch (exception: Exception) {
            Log.e(TAG, "fetchAuthorDetailsBySlug: ${exception.message}")
            _status.postValue(Status.READ_FAIL)
        }
    }

    fun fetchAuthorImage(authorName: String) {
        if (!_authorImage.value.isNullOrEmpty())
            return
        try {
            viewModelScope.launch {
                try {
                    QuotesRepo.getAuthorImageResponse(authorName).let {
                        val pages = it.query?.pages
                        val keyValue = pages?.get(pages.keys.toIntArray()[0])
                        val authorImageUrl = keyValue?.original?.source
                        authorImageUrl?.let { url ->
                            Log.d(TAG, "fetchAuthorImage: Url=$authorImageUrl")
                            _authorImage.postValue(url)
                        }
                    }
                } catch (exception: Exception) {
                    Log.e(TAG, "fetchAuthorImage: ${exception.message}")
                    _status.postValue(Status.READ_FAIL)
                }
            }
        } catch (exception: Exception) {
            Log.e(TAG, "fetchAuthorImage: ${exception.message}")
            _status.postValue(Status.READ_FAIL)
        }
    }
}