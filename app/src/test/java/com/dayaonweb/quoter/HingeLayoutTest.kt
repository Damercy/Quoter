package com.dayaonweb.quoter

import com.dayaonweb.quoter.presentation.compose.*
import org.junit.Assert.assertEquals
import org.junit.Test

class HingeLayoutTest {
    @Test fun bookKeepsTextInOneContiguousPane() {
        assertEquals(PaneRect(0,0,490,1000), safePane(1000,1000,listOf(WindowHinge(PaneRect(490,0,510,1000),true))))
    }
    @Test fun tabletopUsesUpperPane() {
        assertEquals(PaneRect(0,0,1000,490), safePane(1000,1000,listOf(WindowHinge(PaneRect(0,490,1000,510),false))))
    }
    @Test fun multipleHingesNeverLeaveTextAcrossAFold() {
        assertEquals(PaneRect(0,0,390,900), safePane(1200,900,listOf(
            WindowHinge(PaneRect(390,0,410,900),true),WindowHinge(PaneRect(790,0,810,900),true))))
    }
    @Test fun foldOutsideCurrentWindowDoesNotReduceAvailableSpace() {
        assertEquals(PaneRect(0,0,400,800),safePane(400,800,listOf(WindowHinge(PaneRect(800,0,820,800),true))))
    }
}
