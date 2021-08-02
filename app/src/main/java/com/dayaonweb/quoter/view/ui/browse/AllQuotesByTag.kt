package com.dayaonweb.quoter.view.ui.browse

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dayaonweb.quoter.R
import com.dayaonweb.quoter.databinding.FragmentAllQuotesByTagBinding

class AllQuotesByTag : Fragment() {

    private var bi: FragmentAllQuotesByTagBinding? = null
    private val viewModel: AllQuotesByTagViewModel by viewModels()
    private var quoteCountToName = mutableMapOf<Int, String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        bi =
            DataBindingUtil.inflate(inflater, R.layout.fragment_all_quotes_by_tag, container, false)
        return bi?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        attachListeners()
        attachObservers()
        viewModel.getAllQuotes()
    }

    private fun attachObservers() {
        viewModel.allQuotesByTag.observe({ lifecycle }) {
            if (it.isNotEmpty()) {
                it.filter { item ->
                    item.quoteCount != 0
                }.forEach { item ->
                    quoteCountToName[item.quoteCount] = item.name
                }
                initNumberPicker()
            }
        }
    }

    private fun initNumberPicker() {
        bi?.numberPicker?.apply {
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

    private fun attachListeners() {
        bi?.apply {
            numberPicker.setOnValueChangedListener { _, _, newVal ->
                quoteTagTextView.text =
                    viewModel.getFormattedQuoteNameTag(quoteCountToName.values.toTypedArray()[newVal])
            }
            numberPicker.setOnClickListener {
                Log.d(
                    TAG,
                    "attachListeners: clicked with itemID/tag:${quoteCountToName.values.toTypedArray()[numberPicker.value]}"
                )
                findNavController().navigate(R.id.action_allQuotesByTag_to_browseTag)
            }
            menuImageView.setOnClickListener {

            }
        }

    }

    override fun onDestroy() {
        bi = null
        super.onDestroy()
    }

    companion object {
        private const val TAG = "AllQuotesByTag"
    }


}