package com.dayaonweb.quoter.view.ui.settings

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dayaonweb.quoter.BuildConfig
import com.dayaonweb.quoter.R
import com.dayaonweb.quoter.constants.Constants.PENDING_INTENT_REQ_CODE
import com.dayaonweb.quoter.databinding.FragmentAllSettingsBinding
import com.dayaonweb.quoter.extensions.showSnack
import com.dayaonweb.quoter.extensions.showSnackWithAction
import com.dayaonweb.quoter.service.broadcast.QuoteBroadcast
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
    private lateinit var calendar: Calendar
    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent

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
        attachObservers()
        setupListeners()
        viewModel.getAllPreferences(requireContext())
    }

    private fun attachObservers() {
        viewModel.preferences.observe(viewLifecycleOwner) {
            Log.d("PREFS", "attachObservers: $it")
            handler.postDelayed({
                initializeQuoterTts(it.ttsLanguage)
            }, 1000)
            bi?.notifSwitch?.isChecked = it.isNotificationOn
            bi?.darkModeSwitch?.isChecked = it.isDarkMode
            bi?.speechRateSlider?.value = it.speechRate
            if (it.isNotificationOn) {
                val time = it.notificationTime.split(":")
                bi?.notifTimeBtn?.text = getTime(time[0].toInt(), time[1].toInt())
                val checkBtnId =
                    if (it.isImageStyleNotification) R.id.image_style_btn else R.id.text_style_btn
                bi?.notifStyleButtonToggleGroup?.check(checkBtnId)
            }
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
                    viewModel.updateTtsLanguage(requireContext(), it)
                    quoterSpeaker?.speakText("This is a sample text in ${it.displayLanguage}", "")
                }
            }
            speechRateSlider.addOnChangeListener { _, value, _ ->
                viewModel.updateTtsSpeechRate(requireContext(), value)
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
                    viewModel.updateNotifTime(
                        requireContext(),
                        "${timePicker.hour}:${timePicker.minute}"
                    )
                    setAlarm(timePicker.hour, timePicker.minute)
                }
                timePicker.show(requireActivity().supportFragmentManager, null)
            }
            notifStyleButtonToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
                val isImageStyled = checkedId == R.id.image_style_btn && isChecked
                viewModel.toggleNotificationStyle(requireContext(), isImageStyled)
                showSnack("You'll receive ${if (isImageStyled) "image" else "text"} styled notification quote")
            }
            notifSwitch.setOnCheckedChangeListener { _, isChecked ->
                notifOption2TextView.isVisible = isChecked
                notifOption3TextView.isVisible = isChecked
                notifTimeBtn.isVisible = isChecked
                notifStyleButtonToggleGroup.isVisible = isChecked
                viewModel.toggleNotification(requireContext(), isChecked)
                if (!isChecked)
                    cancelAlarm()
                else {
                    val timePrefs = viewModel.preferences.value ?: return@setOnCheckedChangeListener
                    val time = timePrefs.notificationTime.split(":")
                    setAlarm(time[0].toInt(), time[1].toInt())
                }
            }
            darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
                viewModel.toggleDarkMode(requireContext(), isChecked)
                AppCompatDelegate.setDefaultNightMode(if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

    private fun setAlarm(hour: Int, minute: Int) {
        calendar = Calendar.getInstance()
        calendar.apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val broadcastIntent = Intent(requireContext(), QuoteBroadcast::class.java)
        pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            PENDING_INTENT_REQ_CODE,
            broadcastIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        if (calendar.before(Calendar.getInstance()))
            calendar.add(Calendar.DATE, 1)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                showSnackWithAction(
                    text = "Please grant scheduling exact alarm permission to get notifications",
                    actionText = "Grant permission",
                    action = {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        intent.data = Uri.parse("package:" + requireActivity().packageName)
                        startActivity(intent)
                    }
                )
            } else
                setAlarmManager(hour = hour, minute = minute)
        }
        setAlarmManager(hour = hour, minute = minute)
    }


    private fun setAlarmManager(hour: Int, minute: Int) {
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
        val scheduledTime = getTime(hour, minute)
        bi?.notifTimeBtn?.text = scheduledTime
        showSnack("Next quote scheduled at $scheduledTime")
    }

    private fun cancelAlarm() {
        val broadcastIntent = Intent(requireContext(), QuoteBroadcast::class.java)
        pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            PENDING_INTENT_REQ_CODE,
            broadcastIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    private fun getTime(hr: Int, min: Int): String? {
        val cal: Calendar = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, hr)
        cal.set(Calendar.MINUTE, min)
        val formatter: Format
        formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
        return formatter.format(cal.time)
    }

    private fun initializeQuoterTts(ttsLanguage: Locale) {
        quoterSpeaker = Quoter(context = requireContext()) { initStatus ->
            if (initStatus == TextToSpeech.SUCCESS)
                quoterSpeaker?.init()
            quoterSpeaker?.setEngineLocale(ttsLanguage)
            quoterSpeaker?.setSpeechRateSpeed(viewModel.preferences.value?.speechRate ?: 1.0f)
            val engineLocale = quoterSpeaker?.getCurrentVoice()?.locale ?: ttsLanguage
            viewModel.updateTtsLanguage(requireContext(), engineLocale)
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
        quoterSpeaker?.deInit()
    }


    override fun onDestroy() {
        super.onDestroy()
        bi = null
    }


}