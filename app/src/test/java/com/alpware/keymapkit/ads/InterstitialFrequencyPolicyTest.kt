package com.alpware.keymapkit.ads

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InterstitialFrequencyPolicyTest {
    @Test
    fun becomesEligibleAtConfiguredThreshold() {
        assertFalse(InterstitialFrequencyPolicy.isEligible(6, 7))
        assertTrue(InterstitialFrequencyPolicy.isEligible(7, 7))
    }

    @Test
    fun counterStopsAtEligibilityThreshold() {
        assertEquals(1, InterstitialFrequencyPolicy.nextActionCount(0, 7))
        assertEquals(7, InterstitialFrequencyPolicy.nextActionCount(6, 7))
        assertEquals(7, InterstitialFrequencyPolicy.nextActionCount(7, 7))
    }

    @Test
    fun cooldownHandlesClockRollbackSafely() {
        assertFalse(InterstitialFrequencyPolicy.isCooldownComplete(1_500, 1_000, 1_000))
        assertTrue(InterstitialFrequencyPolicy.isCooldownComplete(2_000, 1_000, 1_000))
        assertTrue(InterstitialFrequencyPolicy.isCooldownComplete(500, 1_000, 1_000))
    }
}
