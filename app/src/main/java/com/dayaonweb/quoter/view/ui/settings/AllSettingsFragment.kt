package com.dayaonweb.quoter.view.ui.settings

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dayaonweb.quoter.R
import com.dayaonweb.quoter.constants.Constants.PENDING_INTENT_REQ_CODE
import com.dayaonweb.quoter.databinding.FragmentAllSettingsBinding
import com.dayaonweb.quoter.extensions.showSnack
import com.dayaonweb.quoter.domain.broadcast.QuoteBroadcast
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*

class AllSettingsFragment : Fragment() {

    private var bi: FragmentAllSettingsBinding? = null
    private val viewModel: AllSettingsViewModel by viewModel()
    private lateinit var calendar: Calendar
    private lateinit var alarmManager: AlarmManager
    private var pendingIntent: PendingIntent? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        bi = FragmentAllSettingsBinding.inflate(
            inflater, container, false
        )
        alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return bi?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        attachObservers()
        setupListeners()
        setupAvailableLanguages(bi?.languageChipGroup)
        viewModel.getAllPreferences()
    }

    private fun attachObservers() {
        viewModel.preferences.observe(viewLifecycleOwner) {
            viewModel.updateLanguage(it.ttsLanguage)
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
            languageChipGroup.setOnCheckedStateChangeListener { view, checkedId ->
                if(view.isPressed){
                    if (checkedId.isEmpty()) return@setOnCheckedStateChangeListener
                    val availableLanguages = viewModel.getSpeakerSupportedLanguages()
                    val selectedLanguage =
                        availableLanguages.firstOrNull { checkedId.contains(it.hashCode()) }
                    selectedLanguage?.let {
                        viewModel.updateTtsLanguage(requireContext(), it)
                        viewModel.updateLanguage(it)
                        viewModel.speak("Hi. Your quotes voice is set to ${it.displayLanguage}")
                    }
                }
            }
            speechRateSlider.addOnChangeListener { view, value, _ ->
                if(view.isPressed){
                    viewModel.updateTtsSpeechRate(requireContext(), value)
                    viewModel.updateSpeechRate(value)
                    viewModel.speak("This is the current speech rate")
                }
            }
            notifTimeBtn.setOnClickListener {
                val timePicker = MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_12H)
                    .setTitleText("Receive notifications at").build()

                timePicker.addOnPositiveButtonClickListener {
                    // Make cal instance & put in sharedpref
                    viewModel.updateNotifTime(
                        requireContext(), "${timePicker.hour}:${timePicker.minute}"
                    )
                    setAlarm(timePicker.hour, timePicker.minute)
                }
                timePicker.show(requireActivity().supportFragmentManager, null)
            }
            notifStyleButtonToggleGroup.addOnButtonCheckedListener { view, checkedId, isChecked ->
                if(view.isPressed){
                    val isImageStyled = checkedId == R.id.image_style_btn && isChecked
                    viewModel.toggleNotificationStyle(requireContext(), isImageStyled)
                    showSnack("You'll receive ${if (isImageStyled) "image" else "text"} styled notification quote")
                }
            }
            notifSwitch.setOnCheckedChangeListener { view, isChecked ->
               if(view.isPressed){
                   notifOption2TextView.isVisible = isChecked
                   notifOption3TextView.isVisible = isChecked
                   notifTimeBtn.isVisible = isChecked
                   notifStyleButtonToggleGroup.isVisible = isChecked
                   viewModel.toggleNotification(requireContext(), isChecked)
                   if (!isChecked) cancelAlarm()
                   else {
                       val timePrefs = viewModel.preferences.value ?: return@setOnCheckedChangeListener
                       val time = timePrefs.notificationTime.split(":")
                       setAlarm(time[0].toInt(), time[1].toInt())
                   }
               }
            }
            darkModeSwitch.setOnCheckedChangeListener { view, isChecked ->
                if(view.isPressed) {
                    viewModel.toggleDarkMode(requireContext(), isChecked)
                    AppCompatDelegate.setDefaultNightMode(if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
                }
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
        if (calendar.before(Calendar.getInstance())) calendar.add(Calendar.DATE, 1)
        setAlarmManager(hour = hour, minute = minute)
    }

    private fun getOrCreatePendingIntent(): PendingIntent {
        if (pendingIntent == null) {
            val broadcastIntent = Intent(requireContext(), QuoteBroadcast::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            pendingIntent = PendingIntent.getBroadcast(
                requireContext(),
                PENDING_INTENT_REQ_CODE,
                broadcastIntent,
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
        return pendingIntent!!
    }


    private fun setAlarmManager(hour: Int, minute: Int) {
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP, calendar.timeInMillis, getOrCreatePendingIntent()
        )
        val scheduledTime = getTime(hour, minute)
        bi?.notifTimeBtn?.text = scheduledTime
        showSnack("Next quote scheduled at $scheduledTime")
    }

    private fun cancelAlarm() {
        alarmManager.cancel(getOrCreatePendingIntent())
    }

    private fun getTime(hr: Int, min: Int): String? {
        val cal: Calendar = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, hr)
        cal.set(Calendar.MINUTE, min)
        val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
        return formatter.format(cal.time)
    }


    private fun setupAvailableLanguages(rootChipGroup: ChipGroup?) {
        if (rootChipGroup == null) return
        val supportedLanguages = viewModel.getSpeakerSupportedLanguages()
        supportedLanguages.forEach {
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
        bi?.loader?.isVisible = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bi = null
        pendingIntent = null
    }


}