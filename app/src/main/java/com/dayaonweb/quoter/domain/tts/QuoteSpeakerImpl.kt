package com.dayaonweb.quoter.domain.tts

import android.speech.tts.UtteranceProgressListener
import com.dayaonweb.quoter.domain.models.UiQuote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import javax.inject.Inject

class QuoteSpeakerImpl @Inject constructor(
    private val tts: Quoter
) : QuoteSpeaker {

    private val _isSpeaking = MutableStateFlow(false)
    override fun isSpeaking(): Flow<Boolean> = _isSpeaking.asStateFlow()

    override fun speak(quote: UiQuote) {
        val quoteToSpeak = quote.quote
        val author = quote.author
        val text = "$author said, $quoteToSpeak"
        val id = quote.id
        speakImpl(text, id)
    }

    override fun speak(text: String) {
        speakImpl(text, "")
    }

    private fun speakImpl(text: String, id: String) {
        tts.speakText(text, id)
    }

    override fun stopSpeaking() {
        tts.stopSpeaking()
        _isSpeaking.value = false
    }

    override fun setEngineLocale(locale: Locale) {
        tts.setEngineLocale(locale)
    }

    override fun getEngineLocale(): Locale? {
        return tts.getCurrentVoice()?.locale
    }

    override fun getSupportedLocales(): Set<Locale>? {
        return tts.getSupportedLanguages()
    }

    override fun setSpeechRate(rate: Float) {
        tts.setSpeechRateSpeed(rate)
    }

    override fun initSpeakListener() {
        tts.setUtteranceListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
            }

            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
            }

            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
            }
        })
    }
}