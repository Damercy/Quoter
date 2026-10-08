package com.dayaonweb.quoter

import com.dayaonweb.quoter.presentation.compose.ReviewLaunchPolicy
import org.junit.Assert.*
import org.junit.Test

class ReviewLaunchPolicyTest {
    @Test fun requestsExactlyOnPowersOfTwoStartingAtSecondLaunch() {
        assertEquals(listOf(2L,4L,8L,16L,32L,64L), (0L..100L).filter(ReviewLaunchPolicy::isDue))
    }
    @Test fun invalidCountsAndOverflowCannotProduceExtraPrompts() {
        assertFalse(ReviewLaunchPolicy.isDue(-2))
        assertFalse(ReviewLaunchPolicy.isDue(Long.MAX_VALUE))
        assertEquals(1L, ReviewLaunchPolicy.increment(-20))
        assertEquals(Long.MAX_VALUE, ReviewLaunchPolicy.increment(Long.MAX_VALUE))
        assertTrue(ReviewLaunchPolicy.isDue(1L shl 62))
    }
}
