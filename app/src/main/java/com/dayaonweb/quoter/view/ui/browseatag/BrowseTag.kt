package com.dayaonweb.quoter.view.ui.browseatag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.dayaonweb.quoter.R
import com.dayaonweb.quoter.databinding.FragmentBrowseTagBinding
import com.dayaonweb.quoter.service.model.Quote

class BrowseTag : Fragment() {

    private var bi: FragmentBrowseTagBinding? = null
    private val viewModel: BrowseTagViewModel by viewModels()
    private var quoteToAuthor = mutableMapOf<Quote, String>()
    private var currentPageCount = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        bi = DataBindingUtil.inflate(inflater, R.layout.fragment_browse_tag, container, false)
        return bi?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        attachListeners()
        attachObservers()
        viewModel.fetchQuotesByTag("love") // get from args
    }

    private fun attachObservers() {
        viewModel.quotes.observe({ lifecycle }) {
            currentPageCount = it.count
            for (result in it.results) {
                quoteToAuthor[result] = result.author
            }
            initNumberPicker()
        }
    }

    private fun attachListeners() {
        bi?.apply {
            shareImageView.setOnClickListener {

            }
            quoteScroller.setOnValueChangedListener { _, _, newVal ->
                quoteTextView.text = quoteToAuthor.keys.toTypedArray()[newVal].content
                authorTextView.text = quoteToAuthor.values.toTypedArray()[newVal]
                serialTextView.text = String.format("%s", "${newVal + 1}/$currentPageCount")
            }

        }

    }

    private fun initNumberPicker() {
        bi?.quoteScroller?.apply {
            isVisible = true
            typeface = ResourcesCompat.getFont(requireContext(), R.font.main_bold)
            setSelectedTypeface(ResourcesCompat.getFont(requireContext(), R.font.main_bold))
            minValue = 0
            maxValue = currentPageCount - 1
            displayedValues = Array(quoteToAuthor.size) { "" }
        }
        bi?.quoteTextView?.text = quoteToAuthor.keys.toTypedArray()[0].content
        bi?.authorTextView?.text = quoteToAuthor.values.toTypedArray()[0]
        bi?.serialTextView?.text = String.format("%s", "1/$currentPageCount")
    }

    override fun onDestroy() {
        bi = null
        super.onDestroy()
    }
}