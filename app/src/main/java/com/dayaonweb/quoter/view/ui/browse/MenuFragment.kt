package com.dayaonweb.quoter.view.ui.browse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.dayaonweb.quoter.R
import com.dayaonweb.quoter.databinding.FragmentMenuBinding

class MenuFragment : Fragment() {

    private var bi: FragmentMenuBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        bi = DataBindingUtil.inflate(inflater, R.layout.fragment_menu, container, false)
        return bi?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        attachListeners()
    }

    private fun attachListeners() {
        bi?.apply {
            settingsTextView.setOnClickListener {
                Toast.makeText(requireContext(),"Settings",Toast.LENGTH_SHORT).show()
            }
            closeImageView.setOnClickListener {
                requireActivity().onBackPressed()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bi = null
    }

}