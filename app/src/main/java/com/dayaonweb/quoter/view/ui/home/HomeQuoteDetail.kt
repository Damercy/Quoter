package com.dayaonweb.quoter.view.ui.home

import android.os.Bundle
import android.transition.TransitionInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.dayaonweb.quoter.R

class HomeQuoteDetail : Fragment(R.layout.fragment_quote_detail) {

    override fun onCreate(savedInstanceState: Bundle?) {
        (activity as AppCompatActivity).supportActionBar?.hide()
        super.onCreate(savedInstanceState)
//        val animation =
//            TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.fade)
//        sharedElementEnterTransition = animation
//        sharedElementReturnTransition = animation
    }


}