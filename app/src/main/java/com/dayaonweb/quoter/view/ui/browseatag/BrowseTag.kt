package com.dayaonweb.quoter.view.ui.browseatag

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.dayaonweb.quoter.R
import com.dayaonweb.quoter.databinding.FragmentBrowseTagBinding

class BrowseTag : Fragment() {

    private var bi: FragmentBrowseTagBinding? = null
    private val viewModel: BrowseTagViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        bi = DataBindingUtil.inflate(inflater,R.layout.fragment_browse_tag, container, false)
        return bi?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        attachListeners()
        attachObservers()
        viewModel.fetchQuotesByTag("love") // get from args
    }

    private fun attachObservers() {

    }

    private fun attachListeners() {
        bi?.shareImageView?.setOnClickListener {

        }
    }

    private fun initNumberPicker() {
        bi?.quoteScroller?.apply {
            isVisible = true
            typeface = ResourcesCompat.getFont(requireContext(), R.font.main_bold)
            setSelectedTypeface(ResourcesCompat.getFont(requireContext(), R.font.main_bold))
            minValue = 0
            maxValue = quoteCountToName.size - 1
            displayedValues = quoteCountToName.keys.map { it.toString() }.toTypedArray()
        }
        bi?.quoteTagTextView?.text =
            viewModel.getFormattedQuoteNameTag(quoteCountToName.values.toTypedArray()[0])
    }

    override fun onDestroy() {
        bi = null
        super.onDestroy()
    }
}