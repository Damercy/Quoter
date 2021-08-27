package com.dayaonweb.quoter.view.ui.browse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dayaonweb.quoter.R
import com.dayaonweb.quoter.analytics.Analytics
import com.dayaonweb.quoter.databinding.FragmentMenuBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.play.core.review.ReviewManagerFactory

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
            statusBarColor = ContextCompat.getColor(requireContext(), R.color.black)
            navigationBarColor = ContextCompat.getColor(requireContext(), R.color.black)
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
                rateApp()
            }
            helpTextView.setOnClickListener {

            }
            aboutTextView.setOnClickListener {
                showAboutDialog()
            }
            closeImageView.setOnClickListener {
                requireActivity().onBackPressed()
            }
        }
    }

    private fun showAboutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Quoter: Minimalistic quotes app")
            .setMessage(getString(R.string.quoter_message))
            .setPositiveButton("Rate app") { dialog, _ ->
                rateApp()
                dialog.dismiss()
            }
            .setOnDismissListener { 
                requireActivity().onBackPressed()
            }
            .show()
    }

    private fun rateApp() {
        val manager = ReviewManagerFactory.create(requireContext())
        //val manager = FakeReviewManager(requireContext())
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { request ->
            if (request.isSuccessful) {
                // We got the ReviewInfo object
                val reviewInfo = request.result
                val flow = manager.launchReviewFlow(requireActivity(), reviewInfo)
                flow.addOnCompleteListener { _ ->
                    Analytics.trackAppReview()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bi = null
    }

}