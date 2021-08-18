package com.dayaonweb.quoter.tts

import android.content.Context
import android.speech.tts.TextToSpeech

class Quoter(context: Context, onInit: (status: Int) -> Unit) {
    private var tts: TextToSpeech? =
        TextToSpeech(context.applicationContext, { status -> onInit(status) }, DEFAULT_ENGINE)


    companion object {
        private const val DEFAULT_ENGINE = "com.google.android.tts"
    }

}