package com.dayaonweb.quoter.tts

import android.content.Context
import android.media.AudioAttributes
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.*
import java.util.*

class Quoter(context: Context, onInit: (status: Int) -> Unit) {
    private var tts: TextToSpeech? = null
    private val scope: CoroutineScope = CoroutineScope(context = Dispatchers.IO + Job())

    init {
        scope.launch {
            tts = TextToSpeech(context.applicationContext, { status ->
                onInit(status)
            }, DEFAULT_ENGINE)
        }
    }


    fun init(listener: UtteranceProgressListener? = null): Boolean {
        listener?.let {
            tts?.setOnUtteranceProgressListener(it)
        }
        setupAudioAttributes()
        if (tts?.voice?.locale != Locale("en"))
            setEngineLocale(Locale("en", "IN"))
        return true
    }

    fun deInit() {
        stopSpeaking()
        tts?.shutdown()
    }


    fun speakText(text: String, utteranceId: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    private fun stopSpeaking() = tts?.stop()

    private fun setupAudioAttributes() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()

        tts?.setAudioAttributes(audioAttributes)
    }

    fun setEngineLocale(languageLocale: Locale) {
        if (isLanguageAvailable(languageLocale)) {
            tts?.language = languageLocale
        }
    }

    fun setSpeechRateSpeed(speechRate: Float) = tts?.setSpeechRate(speechRate)

    fun getSupportedLanguages() = tts?.availableLanguages

    fun getCurrentVoice() = tts?.voice


    private fun isLanguageAvailable(languageLocale: Locale): Boolean {
        return when (tts?.isLanguageAvailable(languageLocale)) {
            TextToSpeech.LANG_AVAILABLE -> true
            TextToSpeech.LANG_COUNTRY_AVAILABLE -> true
            TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE -> true
            TextToSpeech.LANG_MISSING_DATA -> false
            TextToSpeech.LANG_NOT_SUPPORTED -> false
            else -> false
        }
    }

    companion object {
        private const val DEFAULT_ENGINE = "com.google.android.tts"
    }

}