package com.alpware.keymapkit.ads

/** Pure frequency rules for layout-change interstitials. */
object InterstitialFrequencyPolicy {
    fun nextActionCount(currentCount: Int, actionsRequired: Int): Int =
        (currentCount + 1).coerceAtMost(actionsRequired)

    fun isEligible(actionCount: Int, actionsRequired: Int): Boolean =
        actionCount >= actionsRequired

    fun isCooldownComplete(nowMs: Long, lastShownMs: Long, cooldownMs: Long): Boolean =
        lastShownMs <= 0L || nowMs < lastShownMs || nowMs - lastShownMs >= cooldownMs
}
