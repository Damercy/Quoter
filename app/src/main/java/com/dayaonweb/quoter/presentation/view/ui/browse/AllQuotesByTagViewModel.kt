package com.dayaonweb.quoter.presentation.view.ui.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.dayaonweb.quoter.data.repository.QuotesRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AllQuotesByTagViewModel @Inject constructor(
    quotesRepo: QuotesRepo
) : ViewModel() {
    val allQuotesByTag = quotesRepo.getTags().asLiveData()

}