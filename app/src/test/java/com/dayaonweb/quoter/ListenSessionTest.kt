package com.dayaonweb.quoter

import com.dayaonweb.quoter.presentation.compose.ListenSession
import org.junit.Assert.*
import org.junit.Test

class ListenSessionTest {
    @Test fun stoppingBeforeStartIgnoresLateCallbacks() {
        val session = ListenSession()
        val id = session.begin()
        session.cancel()
        assertFalse(session.start(id))
        assertFalse(session.complete(id))
    }
    @Test fun replayingTheSameQuoteDoesNotAcceptOldCompletion() {
        val session = ListenSession()
        val first = session.begin()
        assertTrue(session.start(first))
        session.cancel()
        val second = session.begin()
        assertNotEquals(first, second)
        assertFalse(session.complete(first))
        assertTrue(session.start(second))
        assertTrue(session.complete(second))
        assertFalse(session.complete(second))
    }
    @Test fun duplicatesAndCompletionWithoutAudioDoNotCount() {
        val session = ListenSession()
        assertFalse(session.complete(session.begin()))
        val id = session.begin()
        assertTrue(session.start(id))
        assertFalse(session.start(id))
        assertTrue(session.complete(id))
        assertFalse(session.start(id))
        assertFalse(session.complete(id))
    }
}
