package com.alpware.keymapkit.ads

/** Pure frequency rules for layout-change interstitials. */
object InterstitialFrequencyPolicy {
    fun nextActionCount(currentCount: Int, actionsRequired: Int): Int =
        (currentCount + 1).coerceAtMost(actionsRequired)

    fun isEligible(actionCount: Int, actionsRequired: Int): Boolean =
        actionCount >= actionsRequired
}
