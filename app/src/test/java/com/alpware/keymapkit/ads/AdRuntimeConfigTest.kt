package com.alpware.keymapkit.ads

import org.junit.Assert.assertEquals
import org.junit.Test

class AdRuntimeConfigTest {
    @Test
    fun requiresExactlyThreeActions() {
        assertEquals(3, AdRuntimeConfig.INTERSTITIAL_ACTIONS_REQUIRED)
    }

    @Test
    fun remoteConfigCanDisableInterstitials() {
        val config = AdRuntimeConfig.safe(
            bannerEnabled = true,
            interstitialEnabled = false,
        )

        assertEquals(false, config.interstitialEnabled)
    }
}
