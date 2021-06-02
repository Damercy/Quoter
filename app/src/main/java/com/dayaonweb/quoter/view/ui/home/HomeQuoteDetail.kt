package com.dayaonweb.quoter.view.ui.home

import android.os.Bundle
import android.transition.TransitionInflater
import androidx.fragment.app.Fragment
import com.dayaonweb.quoter.R

class HomeQuoteDetail: Fragment(R.layout.fragment_quote_detail) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val animation = TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
        sharedElementEnterTransition = animation
        sharedElementReturnTransition = animation
    }
}