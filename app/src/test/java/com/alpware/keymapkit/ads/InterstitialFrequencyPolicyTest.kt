package com.alpware.keymapkit.ads

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InterstitialFrequencyPolicyTest {
    @Test
    fun becomesEligibleAtConfiguredThreshold() {
        assertFalse(InterstitialFrequencyPolicy.isEligible(2, 3))
        assertTrue(InterstitialFrequencyPolicy.isEligible(3, 3))
    }

    @Test
    fun counterStopsAtEligibilityThreshold() {
        assertEquals(1, InterstitialFrequencyPolicy.nextActionCount(0, 3))
        assertEquals(3, InterstitialFrequencyPolicy.nextActionCount(2, 3))
        assertEquals(3, InterstitialFrequencyPolicy.nextActionCount(3, 3))
    }
}
