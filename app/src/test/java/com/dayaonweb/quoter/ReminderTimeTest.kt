package com.dayaonweb.quoter

import com.dayaonweb.quoter.domain.broadcast.ReminderTime
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class ReminderTimeTest {
    @Test fun invalidLegacyTimesUseSafeDefault() {
        for (time in listOf("", "not a time", "25:00", "9:61", "-1:00")) assertEquals(9 to 0, ReminderTime.parse(time))
        assertEquals(23 to 59, ReminderTime.parse("23:59"))
    }
    @Test fun equalAndPastTimesMoveToTomorrowAndInputIsUnchanged() {
        val now = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata")).apply { set(2026,9,8,9,0,0); set(Calendar.MILLISECOND,0) }
        val original = now.timeInMillis
        assertEquals(original + 24 * 60 * 60 * 1000L, ReminderTime.next(now,9,0))
        assertEquals(original + 60 * 60 * 1000L, ReminderTime.next(now,10,0))
        assertEquals(original, now.timeInMillis)
    }
    @Test fun tomorrowFollowsLocalCalendarAcrossDaylightSaving() {
        val now = Calendar.getInstance(TimeZone.getTimeZone("America/New_York")).apply { set(2026,9,31,9,0,0); set(Calendar.MILLISECOND,0) }
        assertEquals(now.timeInMillis + 25 * 60 * 60 * 1000L, ReminderTime.next(now,9,0))
    }
}
