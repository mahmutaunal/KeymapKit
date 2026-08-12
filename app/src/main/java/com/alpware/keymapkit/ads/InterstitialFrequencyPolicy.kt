package com.alpware.keymapkit.ads

/** Pure frequency rules for layout-change interstitials. */
object InterstitialFrequencyPolicy {
    const val ACTIONS_PER_INTERSTITIAL = 3

    fun nextActionCount(currentCount: Int): Int =
        (currentCount + 1).coerceAtMost(ACTIONS_PER_INTERSTITIAL)

    fun isEligible(actionCount: Int): Boolean =
        actionCount >= ACTIONS_PER_INTERSTITIAL
}
