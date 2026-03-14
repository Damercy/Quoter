package com.dayaonweb.quoter.domain.tts

import android.content.Context
import android.media.AudioAttributes
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.*
import javax.inject.Inject

class Quoter @Inject constructor(context: Context, onInit: (status: Int) -> Unit) {

    private var isReady = false

    private var tts: TextToSpeech? = TextToSpeech(context, { status ->
        if (status == TextToSpeech.SUCCESS) {
            isReady = true
            setupAudioAttributes()
            setEngineLocale(Locale.ENGLISH)
        }
    }, DEFAULT_ENGINE)

    fun speakText(text: String, utteranceId: String) {
        if (isReady) {
            stopSpeaking()
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        }
    }

    fun stopSpeaking() {
        tts?.stop()
    }

    fun setUtteranceListener(listener: UtteranceProgressListener? = null) {
        listener?.let {
            tts?.setOnUtteranceProgressListener(it)
        }
    }
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

    fun setSpeechRateSpeed(speechRate: Float) {
        tts?.setSpeechRate(speechRate)
    }

    fun getSupportedLanguages() = tts?.availableLanguages

    fun getCurrentVoice() = tts?.voice

    companion object {
        private const val DEFAULT_ENGINE = "com.google.android.tts"
    }
}