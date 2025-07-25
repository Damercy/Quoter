package com.dayaonweb.quoter.view.ui.browse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dayaonweb.quoter.R
import com.dayaonweb.quoter.databinding.FragmentAllQuotesByTagBinding
import com.dayaonweb.quoter.view.ui.browsetag.BrowseTagArgs
import org.koin.androidx.viewmodel.ext.android.viewModel

class AllQuotesByTag : Fragment() {

    private var bi: FragmentAllQuotesByTagBinding? = null
    private var scrollPositionIndex = -1
    private val viewModel: AllQuotesByTagViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        bi = FragmentAllQuotesByTagBinding.inflate(inflater, container, false)
        return bi?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        attachListeners()
        attachObservers()
        viewModel.getAllQuotes()
    }

    private fun attachObservers() {
        viewModel.allQuotesByTag.observe(viewLifecycleOwner) { apiResponse ->
            bi?.textView?.isVisible = true
            bi?.menuImageView?.isVisible = true
            bi?.loader?.isVisible = false
            initNumberPicker(apiResponse ?: emptyList())
        }
    }


    override fun onResume() {
        super.onResume()
        if (scrollPositionIndex != -1) {
            bi?.numberPicker?.value = scrollPositionIndex
        }
    }

    private fun initNumberPicker(quoteTags: List<String>) {
        bi?.numberPicker?.apply {
            isVisible = quoteTags.isNotEmpty()
            typeface = ResourcesCompat.getFont(requireContext(), R.font.main_bold)
            setSelectedTypeface(ResourcesCompat.getFont(requireContext(), R.font.main_bold))
            minValue = 0
            maxValue = if (quoteTags.isNotEmpty()) quoteTags.size - 1 else 1
            if (quoteTags.isNotEmpty())
                displayedValues = quoteTags.toTypedArray()
        }
        if (quoteTags.isEmpty())
            bi?.quoteTagTextView?.text = getString(R.string.failed_msg)
    }

    private fun attachListeners() {
        bi?.apply {
            numberPicker.setOnValueChangedListener { _, _, newVal ->
                scrollPositionIndex = newVal
            }
            numberPicker.setOnClickListener {
                val displayedValue =
                    numberPicker.displayedValues[if (scrollPositionIndex == -1) 0 else scrollPositionIndex]
                val arg =
                    BrowseTagArgs(displayedValue).toBundle()
                findNavController().navigate(R.id.action_allQuotesByTag_to_browseTag, arg)
            }
            menuImageView.setOnClickListener {
                findNavController().navigate(R.id.action_allQuotesByTag_to_menuFragment)
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        bi = null
    }

}