package com.alpware.keymapkit.ads

/**
 * Runtime monetization policy. Remote values are always clamped to the immutable safety
 * envelope below, so a console mistake cannot make ads more aggressive than the binary allows.
 */
data class AdRuntimeConfig(
    val bannerEnabled: Boolean = true,
    val interstitialEnabled: Boolean = true,
    val interstitialActionsRequired: Int = DEFAULT_ACTIONS_REQUIRED,
    val interstitialCooldownMs: Long = DEFAULT_COOLDOWN_MS,
    val interstitialMinimumSessionAgeMs: Long = DEFAULT_MINIMUM_SESSION_AGE_MS,
    val interstitialMaxPerSession: Int = HARD_MAX_INTERSTITIALS_PER_SESSION,
    val interstitialMaxPerDay: Int = HARD_MAX_INTERSTITIALS_PER_DAY,
) {
    companion object {
        const val HARD_MIN_ACTIONS_REQUIRED = 5
        const val HARD_MAX_ACTIONS_REQUIRED = 20
        const val DEFAULT_ACTIONS_REQUIRED = 7

        const val HARD_MIN_COOLDOWN_MS = 10 * 60 * 1000L
        const val HARD_MAX_COOLDOWN_MS = 24 * 60 * 60 * 1000L
        const val DEFAULT_COOLDOWN_MS = 15 * 60 * 1000L

        const val HARD_MIN_SESSION_AGE_MS = 90 * 1000L
        const val HARD_MAX_SESSION_AGE_MS = 30 * 60 * 1000L
        const val DEFAULT_MINIMUM_SESSION_AGE_MS = 2 * 60 * 1000L

        const val HARD_MAX_INTERSTITIALS_PER_SESSION = 1
        const val HARD_MAX_INTERSTITIALS_PER_DAY = 2

        fun safe(
            bannerEnabled: Boolean,
            interstitialEnabled: Boolean,
            actionsRequired: Long,
            cooldownMs: Long,
            minimumSessionAgeMs: Long,
            maxPerSession: Long,
            maxPerDay: Long,
        ) = AdRuntimeConfig(
            bannerEnabled = bannerEnabled,
            interstitialEnabled = interstitialEnabled,
            interstitialActionsRequired = actionsRequired.toInt().coerceIn(
                HARD_MIN_ACTIONS_REQUIRED,
                HARD_MAX_ACTIONS_REQUIRED,
            ),
            interstitialCooldownMs = cooldownMs.coerceIn(
                HARD_MIN_COOLDOWN_MS,
                HARD_MAX_COOLDOWN_MS,
            ),
            interstitialMinimumSessionAgeMs = minimumSessionAgeMs.coerceIn(
                HARD_MIN_SESSION_AGE_MS,
                HARD_MAX_SESSION_AGE_MS,
            ),
            interstitialMaxPerSession = maxPerSession.toInt().coerceIn(
                0,
                HARD_MAX_INTERSTITIALS_PER_SESSION,
            ),
            interstitialMaxPerDay = maxPerDay.toInt().coerceIn(
                0,
                HARD_MAX_INTERSTITIALS_PER_DAY,
            ),
        )
    }
}
