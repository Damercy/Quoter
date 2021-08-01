package com.dayaonweb.quoter.view.ui.browse

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.dayaonweb.quoter.R
import com.dayaonweb.quoter.databinding.FragmentAllQuotesByTagBinding

class AllQuotesByTag : Fragment() {

    private var bi: FragmentAllQuotesByTagBinding? = null
    val data =
        mapOf("1" to "technology", "1550" to "famous quotes", "20" to "faith", "9" to "friendship")

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
        initNumberPicker()
        attachListeners()
    }

    private fun initNumberPicker() {
        bi?.numberPicker?.apply {
            typeface = ResourcesCompat.getFont(requireContext(), R.font.main_bold)
            setSelectedTypeface(ResourcesCompat.getFont(requireContext(), R.font.main_bold))
            minValue = 0
            maxValue = data.size - 1
            displayedValues = data.keys.toTypedArray()
        }
        bi?.quoteTagTextView?.text = data.values.toTypedArray()[0]
    }

    private fun attachListeners() {
        bi?.apply {
            numberPicker.setOnValueChangedListener { _, _, newVal: Int ->
                quoteTagTextView.text = data.values.toTypedArray()[newVal]
            }
            numberPicker.setOnClickListener {
                Log.d(TAG, "attachListeners: clicked with itemID/tag:${data.values.toTypedArray()[numberPicker.value]}")
            }
            menuImageView.setOnClickListener {

            }
        }

    }

    override fun onDestroy() {
        bi = null
        super.onDestroy()
    }

    companion object{
        private const val TAG = "AllQuotesByTag"
    }


}