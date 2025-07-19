package com.dayaonweb.quoter.domain.tts

import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

interface Speaker {
    val onSpeaking: StateFlow<Boolean>
    val onError: StateFlow<Boolean>
    fun speak(quote: String)
    fun stop()
    fun updateLanguage(language: Locale)
    fun updateSpeechRate(speechRate: Float)
    fun getSupportedLanguages(): Set<Locale>
}