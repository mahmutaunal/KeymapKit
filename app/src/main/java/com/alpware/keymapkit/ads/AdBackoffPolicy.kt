package com.alpware.keymapkit.ads

/** Bounded exponential retry delays. Attempt zero is the first retry. */
object AdBackoffPolicy {
    const val MAX_BANNER_RETRIES_PER_FOREGROUND_SESSION = 4
    const val MAX_INTERSTITIAL_RETRIES_PER_FOREGROUND_SESSION = 3

    fun bannerDelayMs(attempt: Int): Long = exponentialDelayMs(
        attempt = attempt,
        baseMs = 30_000L,
        maxMs = 30 * 60_000L,
    )

    fun interstitialDelayMs(attempt: Int): Long = exponentialDelayMs(
        attempt = attempt,
        baseMs = 60_000L,
        maxMs = 30 * 60_000L,
    )

    internal fun exponentialDelayMs(attempt: Int, baseMs: Long, maxMs: Long): Long {
        val safeAttempt = attempt.coerceIn(0, 20)
        val multiplier = 1L shl safeAttempt
        return (baseMs * multiplier).coerceAtMost(maxMs)
    }
}
