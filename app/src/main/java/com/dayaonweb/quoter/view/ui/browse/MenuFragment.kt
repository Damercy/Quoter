package com.dayaonweb.quoter.view.ui.browse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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

    override fun onResume() {
        super.onResume()
        requireActivity().window.apply {
            statusBarColor = ContextCompat.getColor(requireContext(),R.color.black)
            navigationBarColor = ContextCompat.getColor(requireContext(),R.color.black)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        attachListeners()
    }

    private fun attachListeners() {
        bi?.apply {
            settingsTextView.setOnClickListener {
                findNavController().navigate(R.id.action_menuFragment_to_allSettingsFragment)
            }
            rateTextView.setOnClickListener {

            }
            helpTextView.setOnClickListener {

            }
            aboutTextView.setOnClickListener {

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