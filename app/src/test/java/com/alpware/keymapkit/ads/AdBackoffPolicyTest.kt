package com.alpware.keymapkit.ads

import org.junit.Assert.assertEquals
import org.junit.Test

class AdBackoffPolicyTest {
    @Test
    fun bannerBackoffGrowsExponentiallyAndIsBounded() {
        assertEquals(30_000, AdBackoffPolicy.bannerDelayMs(0))
        assertEquals(60_000, AdBackoffPolicy.bannerDelayMs(1))
        assertEquals(120_000, AdBackoffPolicy.bannerDelayMs(2))
        assertEquals(1_800_000, AdBackoffPolicy.bannerDelayMs(20))
    }

    @Test
    fun interstitialBackoffStartsAtOneMinute() {
        assertEquals(60_000, AdBackoffPolicy.interstitialDelayMs(0))
        assertEquals(120_000, AdBackoffPolicy.interstitialDelayMs(1))
    }
}
