package com.alpware.keymapkit.ads

import org.junit.Assert.assertEquals
import org.junit.Test

class AdRuntimeConfigTest {
    @Test
    fun remoteValuesCannotCrossHardSafetyBounds() {
        val config = AdRuntimeConfig.safe(
            bannerEnabled = true,
            interstitialEnabled = true,
            actionsRequired = 1,
            cooldownMs = 1,
            minimumSessionAgeMs = 1,
            maxPerSession = 99,
            maxPerDay = 99,
        )

        assertEquals(AdRuntimeConfig.HARD_MIN_ACTIONS_REQUIRED, config.interstitialActionsRequired)
        assertEquals(AdRuntimeConfig.HARD_MIN_COOLDOWN_MS, config.interstitialCooldownMs)
        assertEquals(
            AdRuntimeConfig.HARD_MIN_SESSION_AGE_MS,
            config.interstitialMinimumSessionAgeMs,
        )
        assertEquals(AdRuntimeConfig.HARD_MAX_INTERSTITIALS_PER_SESSION, config.interstitialMaxPerSession)
        assertEquals(AdRuntimeConfig.HARD_MAX_INTERSTITIALS_PER_DAY, config.interstitialMaxPerDay)
    }

    @Test
    fun remoteConfigCanMakePolicyStricter() {
        val config = AdRuntimeConfig.safe(true, true, 15, 3_600_000, 600_000, 0, 1)

        assertEquals(15, config.interstitialActionsRequired)
        assertEquals(3_600_000, config.interstitialCooldownMs)
        assertEquals(0, config.interstitialMaxPerSession)
        assertEquals(1, config.interstitialMaxPerDay)
    }
}
