package com.alpware.keymapkit.ads

import android.app.Activity
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform
import java.util.concurrent.atomic.AtomicBoolean

data class ConsentResult(
    val canRequestAds: Boolean,
    val privacyOptionsRequired: Boolean,
    val error: FormError? = null,
)

/** Owns the complete UMP flow and guarantees exactly one completion callback per launch. */
class AdConsentManager(private val activity: Activity) {
    private val consentInformation = UserMessagingPlatform.getConsentInformation(activity)

    val canRequestAds: Boolean
        get() = consentInformation.canRequestAds()

    val isPrivacyOptionsRequired: Boolean
        get() = consentInformation.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    fun gatherConsent(onComplete: (ConsentResult) -> Unit) {
        val completed = AtomicBoolean(false)
        fun complete(error: FormError? = null) {
            if (completed.compareAndSet(false, true)) {
                onComplete(
                    ConsentResult(
                        canRequestAds = canRequestAds,
                        privacyOptionsRequired = isPrivacyOptionsRequired,
                        error = error,
                    )
                )
            }
        }

        // Do not assert an age status without an age gate. If the Play target audience includes
        // children, the product must add an age-appropriate flow and set TFUA on both UMP and ads.
        val parameters = ConsentRequestParameters.Builder().build()
        consentInformation.requestConsentInfoUpdate(
            activity,
            parameters,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    complete(formError)
                }
            },
            { requestError ->
                // A valid choice from a previous session can still permit requests during an
                // update failure. canRequestAds is always checked by the caller.
                complete(requestError)
            },
        )
    }

    fun showPrivacyOptions(onDismissed: (ConsentResult) -> Unit = {}) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            onDismissed(
                ConsentResult(
                    canRequestAds = canRequestAds,
                    privacyOptionsRequired = isPrivacyOptionsRequired,
                    error = formError,
                )
            )
        }
    }
}
