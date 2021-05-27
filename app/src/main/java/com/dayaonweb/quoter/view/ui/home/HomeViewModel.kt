package com.dayaonweb.quoter.view.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.dayaonweb.quoter.service.repository.QuotesRepo

class HomeViewModel: ViewModel() {
    val quotes =  QuotesRepo.getQuotes().cachedIn(viewModelScope)

}