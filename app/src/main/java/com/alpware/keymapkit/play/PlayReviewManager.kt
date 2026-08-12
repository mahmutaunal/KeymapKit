package com.alpware.keymapkit.play

import android.app.Activity
import com.google.android.play.core.review.ReviewManagerFactory

/**
 * Owns the Google Play in-app review flow.
 *
 * Google Play controls whether the review card is displayed. A successful callback only means
 * that the flow finished; it does not reveal whether the user submitted a review.
 */
class PlayReviewManager(
    private val activity: Activity
) {
    private val reviewManager = ReviewManagerFactory.create(activity)
    private var requestInFlight = false

    fun requestReview(onComplete: (ReviewResult) -> Unit) {
        if (requestInFlight) return
        requestInFlight = true
        reviewManager.requestReviewFlow()
            .addOnSuccessListener { reviewInfo ->
                reviewManager.launchReviewFlow(activity, reviewInfo)
                    .addOnCompleteListener {
                        requestInFlight = false
                        onComplete(ReviewResult.Completed)
                    }
            }
            .addOnFailureListener { error ->
                requestInFlight = false
                onComplete(ReviewResult.Failed(error))
            }
    }
}

sealed interface ReviewResult {
    data object Completed : ReviewResult
    data class Failed(val cause: Throwable) : ReviewResult
}
