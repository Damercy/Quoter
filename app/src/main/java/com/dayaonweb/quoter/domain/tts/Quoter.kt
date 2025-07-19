package com.dayaonweb.quoter.domain.tts

import android.media.AudioAttributes
import android.media.AudioManager
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

class Quoter(private val tts: TextToSpeech) :
    Speaker, LifecycleEventObserver, UtteranceProgressListener() {

    private val speakingFlow = MutableStateFlow(false)
    private val errorFlow = MutableStateFlow(false)
    private var currentUtteranceId = ""

    override val onSpeaking: StateFlow<Boolean>
        get() = speakingFlow.asStateFlow()

    override val onError: StateFlow<Boolean>
        get() = errorFlow.asStateFlow()

    override fun speak(quote: String) {
        currentUtteranceId = UUID.randomUUID().toString()
        tts.speak(
            quote,
            TextToSpeech.QUEUE_FLUSH,
            bundleOf(
                TextToSpeech.Engine.KEY_PARAM_STREAM to AudioManager.STREAM_MUSIC,
                TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID to currentUtteranceId
            ),
            currentUtteranceId
        )
    }

    override fun stop() {
        tts.stop()
    }

    override fun updateLanguage(language: Locale) {
        setEngineLocale(language)
    }

    override fun updateSpeechRate(speechRate: Float) {
        tts.setSpeechRate(speechRate)
    }

    override fun getSupportedLanguages(): Set<Locale> = tts.availableLanguages

    override fun onStateChanged(
        source: LifecycleOwner,
        event: Lifecycle.Event
    ) {
        when {
            event.targetState.isAtLeast(Lifecycle.State.CREATED) -> {
                tts.setOnUtteranceProgressListener(this)
                setupAudioAttributes()
            }

            event.targetState.isAtLeast(Lifecycle.State.DESTROYED) -> {
                currentUtteranceId = ""
                tts.stop()
                tts.shutdown()
            }
        }
    }

    override fun onError(utteranceId: String?, errorCode: Int) {
        if (utteranceId == currentUtteranceId)
            errorFlow.tryEmit(true)
    }

    override fun onError(utteranceId: String?) {
        onError(utteranceId, -1)
    }

    override fun onDone(utteranceId: String?) {
        if (utteranceId == currentUtteranceId) {
            speakingFlow.tryEmit(false)
            errorFlow.tryEmit(false)
        }
    }

    override fun onStart(utteranceId: String?) {
        if (utteranceId == currentUtteranceId)
            speakingFlow.tryEmit(true)
    }

    private fun setupAudioAttributes() {
        val usage = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY
        else
            AudioAttributes.USAGE_ASSISTANT

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(usage)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()

        tts.setAudioAttributes(audioAttributes)
    }

    private fun setEngineLocale(languageLocale: Locale) {
        if (isLanguageAvailable(languageLocale)) {
            tts.language = languageLocale
        }
    }


    private fun isLanguageAvailable(languageLocale: Locale): Boolean =
        TTS_IS_LANG_AVAILABLE.contains(tts.isLanguageAvailable(languageLocale))

    companion object {
        private val TTS_IS_LANG_AVAILABLE = listOf(
            TextToSpeech.LANG_AVAILABLE,
            TextToSpeech.LANG_COUNTRY_AVAILABLE,
            TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE
        )
    }

}