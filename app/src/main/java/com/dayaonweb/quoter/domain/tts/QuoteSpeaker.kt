package com.dayaonweb.quoter.domain.tts

import com.dayaonweb.quoter.domain.models.UiQuote
import kotlinx.coroutines.flow.Flow
import java.util.Locale

interface QuoteSpeaker {
    fun speak(quote: UiQuote)
    fun speak(text: String)
    fun stopSpeaking()
    fun setEngineLocale(locale: Locale)
    fun getEngineLocale(): Locale?
    fun getSupportedLocales(): Set<Locale>?
    fun setSpeechRate(rate: Float)
    fun isSpeaking(): Flow<Boolean>
}