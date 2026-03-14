package com.dayaonweb.quoter.presentation.view.ui.browse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dayaonweb.quoter.R
import com.dayaonweb.quoter.databinding.FragmentAllQuotesByTagBinding
import com.dayaonweb.quoter.presentation.view.ui.browsetag.BrowseTagArgs
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AllQuotesByTag : Fragment() {

    private var bi: FragmentAllQuotesByTagBinding? = null
    private var scrollPositionIndex = -1
    private val viewModel: AllQuotesByTagViewModel by viewModels()

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
    }

    private fun attachObservers() {
        viewModel.allQuotesByTag.observe(viewLifecycleOwner) { apiResponse ->
            bi?.textView?.isVisible = true
            bi?.menuImageView?.isVisible = true
            bi?.loader?.isVisible = false
            if (apiResponse.isEmpty()) {
                bi?.quoteTagTextView?.text = getString(R.string.failed_msg)
            }else{
                initNumberPicker(apiResponse)
            }
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
            displayedValues = null
            typeface = ResourcesCompat.getFont(requireContext(), R.font.main_bold)
            setSelectedTypeface(ResourcesCompat.getFont(requireContext(), R.font.main_bold))
            minValue = 0
            maxValue = quoteTags.size - 1
            displayedValues = quoteTags.toTypedArray()
            wrapSelectorWheel = true
            isVisible = true
        }
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
        bi = null
        super.onDestroyView()
    }

}