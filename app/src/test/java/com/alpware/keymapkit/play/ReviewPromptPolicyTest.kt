package com.alpware.keymapkit.play

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewPromptPolicyTest {
    private val day = 24L * 60L * 60L * 1_000L
    private val now = 200L * day
    private fun eligible() = ReviewPromptSnapshot(now - 3 * day, 4, 2, 1, 2, 0, null)

    @Test fun eligibleUserPasses() = assertTrue(ReviewPromptPolicy.isEligible(eligible(), now))
    @Test fun requiresEnoughActions() = assertFalse(ReviewPromptPolicy.isEligible(eligible().copy(meaningfulActionCount = 3), now))
    @Test fun requiresLayoutChanges() = assertFalse(ReviewPromptPolicy.isEligible(eligible().copy(layoutChangeCount = 1), now))
    @Test fun requiresKeyboardSettingsVisit() = assertFalse(ReviewPromptPolicy.isEligible(eligible().copy(keyboardSettingsOpenCount = 0), now))
    @Test fun respectsCooldown() = assertFalse(ReviewPromptPolicy.isEligible(eligible().copy(lastPromptAttemptEpochMillis = now - 30 * day), now))
    @Test fun capsAttempts() = assertFalse(ReviewPromptPolicy.isEligible(eligible().copy(promptAttemptCount = 2), now))
}
