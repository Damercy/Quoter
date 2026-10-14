package com.dayaonweb.quoter.presentation.compose

/** Main-thread utterance ownership; cancelled or replaced callbacks cannot complete a listen. */
internal class ListenSession {
    private var generation = 0L
    private var active: String? = null
    private var started = false

    fun begin(): String {
        generation++
        return "listen-$generation".also { active = it; started = false }
    }
    fun isActive(id: String?) = id != null && id == active
    fun start(id: String?): Boolean {
        if (!isActive(id) || started) return false
        started = true
        return true
    }
    fun complete(id: String?): Boolean {
        if (!isActive(id)) return false
        val completed = started
        cancel()
        return completed
    }
    fun cancel() { active = null; started = false }
}
