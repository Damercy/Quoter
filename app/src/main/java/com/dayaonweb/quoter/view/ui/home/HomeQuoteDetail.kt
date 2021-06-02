package com.dayaonweb.quoter.view.ui.home

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.transition.TransitionInflater
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.dayaonweb.quoter.R

class HomeQuoteDetail: Fragment(R.layout.fragment_quote_detail) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val animation = TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.slide_bottom)
        sharedElementEnterTransition = animation
        sharedElementReturnTransition = animation
    }

    override fun onResume() {
        super.onResume()
        val toolbar = (activity as AppCompatActivity).supportActionBar
        toolbar?.apply {
            show()
            title = "Detail"
            subtitle = "Know more about a quote"
            setBackgroundDrawable(
                GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(
                        context?.getColor(R.color.purple_700)!!,
                        context?.getColor(R.color.purple_500)!!
                    )
                )
            )
        }
    }

}