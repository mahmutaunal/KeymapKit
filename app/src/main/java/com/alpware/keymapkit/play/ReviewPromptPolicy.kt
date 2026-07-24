package com.alpware.keymapkit.play

data class ReviewPromptSnapshot(
    val firstUseEpochMillis: Long,
    val meaningfulActionCount: Int,
    val layoutChangeCount: Int,
    val keyboardSettingsOpenCount: Int,
    val sessionCount: Int,
    val promptAttemptCount: Int,
    val lastPromptAttemptEpochMillis: Long?
)

/** Conservative, deterministic policy for automatic review requests. */
object ReviewPromptPolicy {
    const val MIN_MEANINGFUL_ACTIONS = 4
    const val MIN_LAYOUT_CHANGES = 2
    const val MIN_SESSIONS = 2
    const val MIN_DAYS_SINCE_FIRST_USE = 2
    const val MAX_AUTOMATIC_ATTEMPTS = 2
    const val COOLDOWN_DAYS = 120
    private const val DAY_MILLIS = 24L * 60L * 60L * 1_000L

    fun isEligible(snapshot: ReviewPromptSnapshot, nowEpochMillis: Long): Boolean {
        if (snapshot.promptAttemptCount >= MAX_AUTOMATIC_ATTEMPTS) return false
        if (snapshot.meaningfulActionCount < MIN_MEANINGFUL_ACTIONS) return false
        if (snapshot.layoutChangeCount < MIN_LAYOUT_CHANGES) return false
        if (snapshot.keyboardSettingsOpenCount < 1) return false
        if (snapshot.sessionCount < MIN_SESSIONS) return false
        if (nowEpochMillis - snapshot.firstUseEpochMillis < MIN_DAYS_SINCE_FIRST_USE * DAY_MILLIS) return false
        val lastAttempt = snapshot.lastPromptAttemptEpochMillis ?: return true
        return nowEpochMillis - lastAttempt >= COOLDOWN_DAYS * DAY_MILLIS
    }
}
