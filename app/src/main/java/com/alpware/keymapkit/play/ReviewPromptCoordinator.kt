package com.alpware.keymapkit.play

import android.content.Context
import androidx.core.content.edit

/** Stores only anonymous, on-device engagement counters. No analytics or network transmission. */
class ReviewPromptCoordinator(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    init {
        if (!preferences.contains(KEY_FIRST_USE)) {
            preferences.edit { putLong(KEY_FIRST_USE, System.currentTimeMillis()) }
        }
    }

    fun recordSession(now: Long = System.currentTimeMillis()) {
        val last = preferences.getLong(KEY_LAST_SESSION, 0L)
        if (last == 0L || now - last >= SESSION_GAP_MILLIS) {
            preferences.edit {
                putInt(KEY_SESSIONS, preferences.getInt(KEY_SESSIONS, 0) + 1)
                    .putLong(KEY_LAST_SESSION, now)
            }
        }
    }

    fun recordLayoutChanged() {
        increment(KEY_ACTIONS)
        increment(KEY_LAYOUT_CHANGES)
    }

    fun recordKeyboardSettingsOpened() {
        increment(KEY_ACTIONS)
        increment(KEY_KEYBOARD_SETTINGS)
    }

    fun recordMeaningfulAction() = increment(KEY_ACTIONS)

    fun shouldRequestAutomaticReview(now: Long = System.currentTimeMillis()) =
        ReviewPromptPolicy.isEligible(snapshot(), now)

    fun recordAutomaticPromptAttempt(now: Long = System.currentTimeMillis()) {
        preferences.edit {
            putInt(KEY_ATTEMPTS, preferences.getInt(KEY_ATTEMPTS, 0) + 1)
                .putLong(KEY_LAST_ATTEMPT, now)
        }
    }

    private fun snapshot() = ReviewPromptSnapshot(
        firstUseEpochMillis = preferences.getLong(KEY_FIRST_USE, System.currentTimeMillis()),
        meaningfulActionCount = preferences.getInt(KEY_ACTIONS, 0),
        layoutChangeCount = preferences.getInt(KEY_LAYOUT_CHANGES, 0),
        keyboardSettingsOpenCount = preferences.getInt(KEY_KEYBOARD_SETTINGS, 0),
        sessionCount = preferences.getInt(KEY_SESSIONS, 0),
        promptAttemptCount = preferences.getInt(KEY_ATTEMPTS, 0),
        lastPromptAttemptEpochMillis = preferences.getLong(KEY_LAST_ATTEMPT, 0L).takeIf { it > 0L }
    )

    private fun increment(key: String) {
        preferences.edit { putInt(key, preferences.getInt(key, 0) + 1) }
    }

    private companion object {
        const val PREFS = "review_prompt_state"
        const val KEY_FIRST_USE = "first_use"
        const val KEY_ACTIONS = "meaningful_actions"
        const val KEY_LAYOUT_CHANGES = "layout_changes"
        const val KEY_KEYBOARD_SETTINGS = "keyboard_settings_opened"
        const val KEY_SESSIONS = "sessions"
        const val KEY_LAST_SESSION = "last_session"
        const val KEY_ATTEMPTS = "prompt_attempts"
        const val KEY_LAST_ATTEMPT = "last_prompt_attempt"
        const val SESSION_GAP_MILLIS = 30L * 60L * 1_000L
    }
}
