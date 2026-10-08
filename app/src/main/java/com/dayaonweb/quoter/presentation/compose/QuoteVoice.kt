package com.dayaonweb.quoter.presentation.compose

import android.content.Context
import android.media.AudioAttributes
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/** Offline selection is a quality heuristic; naturalness requires listening. */
class QuoteVoice(context: Context, initialize: Boolean = true) {
    private val main = Handler(Looper.getMainLooper())
    private val mutableStatus = MutableStateFlow("Preparing voice")
    val status = mutableStatus.asStateFlow()
    private var engine: TextToSpeech? = null
    private var ready = false
    private var speaking = false
    private var configuration = AppSettings()
    val availableVoices: List<android.speech.tts.Voice>
        get() = engine?.voices.orEmpty().filter { !it.isNetworkConnectionRequired && TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED !in it.features.orEmpty() }.sortedBy { it.name }
    var selectedVoiceName: String? = null
        private set
    init {
        if (initialize) engine = TextToSpeech(context.applicationContext) { result -> main.post {
            val tts = engine ?: return@post
            if (result != TextToSpeech.SUCCESS) { mutableStatus.value = "Speech unavailable"; return@post }
            tts.setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
            tts.setSpeechRate(1f)
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(id: String?) { main.post { speaking = true; mutableStatus.value = "Stop" } }
                override fun onDone(id: String?) { main.post { speaking = false; mutableStatus.value = "Listen" } }
                @Deprecated("Platform callback")
                override fun onError(id: String?) { main.post { speaking = false; mutableStatus.value = "Speech failed. Tap to retry" } }
            })
            ready = true
            configure(configuration)
        } }
    }
    fun configure(settings: AppSettings) {
        configuration = settings
        val tts = engine ?: return
        if (!ready) return
        val locale = Locale.forLanguageTag(settings.locale.replace('_','-'))
        val offline = availableVoices
        val chosen = offline.firstOrNull { settings.voice.isNotBlank() && it.name == settings.voice }
            ?: offline.filter { it.locale.language == locale.language }.sortedWith(
                compareByDescending<android.speech.tts.Voice> { it.quality }
                    .thenByDescending { it.locale.country == locale.country }.thenBy { it.latency }.thenBy { it.name }).firstOrNull()
            ?: offline.filter { it.locale.language == "en" }.maxByOrNull { it.quality }
        if (chosen != null && tts.setVoice(chosen) == TextToSpeech.SUCCESS) {
            selectedVoiceName = chosen.name
            if (!speaking) mutableStatus.value = "Listen"
        } else {
            selectedVoiceName = null
            mutableStatus.value = "Install a compatible offline voice in Android speech settings"
        }
        tts.setSpeechRate(settings.rate)
    }
    fun toggle(quote: com.dayaonweb.quoter.domain.models.UiQuote) {
        if (speaking) { stop(); return }
        if (!ready || selectedVoiceName == null) return
        if (engine?.speak("${quote.quote}. ${quote.author}", TextToSpeech.QUEUE_FLUSH, null, quote.id) == TextToSpeech.ERROR)
            mutableStatus.value = "Speech failed. Tap to retry"
    }
    fun stop() { engine?.stop(); speaking = false; if (ready && selectedVoiceName != null) mutableStatus.value = "Listen" }
    fun close() { stop(); engine?.shutdown(); engine = null }
}
