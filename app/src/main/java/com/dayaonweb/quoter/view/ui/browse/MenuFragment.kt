package com.dayaonweb.quoter.view.ui.browse

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dayaonweb.quoter.R
import com.dayaonweb.quoter.analytics.Analytics
import com.dayaonweb.quoter.constants.Constants
import com.dayaonweb.quoter.data.local.DataStoreManager
import com.dayaonweb.quoter.databinding.FragmentMenuBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

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
        updateSystemBars(resetColors = false)
    }

    private fun updateSystemBars(resetColors: Boolean) {
        if (!resetColors) {
            setSystemBarColors(R.color.black)
            return
        }
        setSystemBarColors(R.color.onPrimary)
    }

    private fun setSystemBarColors(color: Int) {
        requireActivity().window.apply {
            statusBarColor = ContextCompat.getColor(requireContext(), color)
            navigationBarColor = ContextCompat.getColor(requireContext(), color)
        }
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
        MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogStyle)
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

    override fun onDestroyView() {
        super.onDestroyView()
        updateSystemBars(resetColors = true)
    }

    private fun rateApp() {
        Analytics.trackAppReview()
        val uri: Uri = Uri.parse("market://details?id=${requireContext().packageName}")
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        goToMarket.addFlags(
            Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        )
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=${requireContext().packageName}")
                )
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bi = null
    }

}