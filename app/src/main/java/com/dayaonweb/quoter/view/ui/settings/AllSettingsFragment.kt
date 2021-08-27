package com.dayaonweb.quoter.view.ui.settings

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dayaonweb.quoter.R
import com.dayaonweb.quoter.databinding.FragmentAllSettingsBinding
import com.dayaonweb.quoter.tts.Quoter
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.Format
import java.text.SimpleDateFormat
import java.util.*

class AllSettingsFragment : Fragment() {

    private var bi: FragmentAllSettingsBinding? = null
    private val viewModel: AllSettingsViewModel by viewModels()
    private val handler by lazy {
        Handler(Looper.getMainLooper())
    }
    private var quoterSpeaker: Quoter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        bi = DataBindingUtil.inflate(inflater, R.layout.fragment_all_settings, container, false)
        return bi?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handler.postDelayed({
            initializeQuoterTts()
        }, 1000)
        setupListeners()
    }


    override fun onResume() {
        super.onResume()
        requireActivity().window.apply {
            statusBarColor = ContextCompat.getColor(requireContext(), R.color.white)
            navigationBarColor = ContextCompat.getColor(requireContext(), R.color.white)
        }
    }

    private fun setupListeners() {
        bi?.apply {
            backImageView.setOnClickListener {
                findNavController().popBackStack()
            }
            languageChipGroup.setOnCheckedChangeListener { _, checkedId ->
                val availableLanguages = quoterSpeaker?.getSupportedLanguages()
                val selectedLanguage = availableLanguages?.first {
                    it.hashCode() == checkedId
                }
                selectedLanguage?.let {
                    quoterSpeaker?.setEngineLocale(it)
                    quoterSpeaker?.speakText("This is a sample text in ${it.displayLanguage}", "")
                }
            }
            speechRateSlider.addOnChangeListener { slider, value, fromUser ->
                quoterSpeaker?.setSpeechRateSpeed(value)
                quoterSpeaker?.speakText("This is the current speech rate", "")
            }
            notifTimeBtn.setOnClickListener {
                val timePicker = MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setTitleText("Receive notifications at")
                    .build()

                timePicker.addOnPositiveButtonClickListener {
                    // Make cal instance & put in sharedpref
                    notifTimeBtn.text = getTime(timePicker.hour, timePicker.minute)
                }
                timePicker.show(requireActivity().supportFragmentManager, null)
            }
            notifSwitch.setOnCheckedChangeListener { _, isChecked ->
                notifOption2TextView.isVisible = isChecked
                notifTimeBtn.isVisible = isChecked
            }
        }
    }

    private fun getTime(hr: Int, min: Int): String? {
        val cal: Calendar = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, hr)
        cal.set(Calendar.MINUTE, min)
        val formatter: Format
        formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
        return formatter.format(cal.time)
    }

    private fun initializeQuoterTts() {
        quoterSpeaker = Quoter(context = requireContext()) { initStatus ->
            if (initStatus == TextToSpeech.SUCCESS)
                quoterSpeaker?.init(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {}
                    override fun onError(utteranceId: String?) {}
                })
            setupSettingValues()
        }
    }

    private fun setupSettingValues() {
        bi?.run {
            setupAvailableLanguages(languageChipGroup)
        }
    }


    private fun setupAvailableLanguages(rootChipGroup: ChipGroup) {
        val supportedLanguages = quoterSpeaker?.getSupportedLanguages()
        val currentSelectedLanguage = quoterSpeaker?.getCurrentVoice()?.locale
        supportedLanguages?.forEach {
            // setup basic chip style
            val chip = layoutInflater.inflate(R.layout.language_chip, rootChipGroup, false) as Chip
            with(chip) {
                isCheckable = true
                isFocusable = true
                isClickable = true
                text = it.displayName
                id = it.hashCode()
                rootChipGroup.addView(this)
            }
        }
        currentSelectedLanguage?.hashCode()?.let {
            rootChipGroup.check(it)
        }
        bi?.loader?.isVisible = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
    }


    override fun onDestroy() {
        super.onDestroy()
        bi = null
    }


}