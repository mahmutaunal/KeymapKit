package com.alpware.keymapkit.ads

import android.app.Activity
import android.content.Context
import com.alpware.keymapkit.BuildConfig
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import androidx.core.content.edit

/**
 * Preloads one interstitial and shows it only when leaving layout management after a change.
 * Every third layout add/remove action becomes an eligible point.
 */
class InterstitialAdManager(
    private val activity: Activity,
    private val canRequestAds: () -> Boolean,
) {
    private val preferences = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false

    fun preload() {
        if (!canRequestAds() || isLoading || interstitialAd != null) return
        isLoading = true
        InterstitialAd.load(
            activity,
            BuildConfig.ADMOB_INTERSTITIAL_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    isLoading = false
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    isLoading = false
                    interstitialAd = null
                }
            }
        )
    }

    fun recordLayoutChange() {
        val count = InterstitialFrequencyPolicy.nextActionCount(
            preferences.getInt(KEY_ACTION_COUNT, 0)
        )
        preferences.edit {
            putInt(KEY_ACTION_COUNT, count)
        }
        preload()
    }

    /** Shows only at the natural transition from layout management back to the app. */
    fun showIfEligible() {
        if (!InterstitialFrequencyPolicy.isEligible(
                preferences.getInt(KEY_ACTION_COUNT, 0)
            )
        ) return

        val ad = interstitialAd
        if (ad == null || activity.isFinishing || activity.isDestroyed) {
            preload()
            return
        }

        preferences.edit { putInt(KEY_ACTION_COUNT, 0) }
        interstitialAd = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() = preload()
            override fun onAdFailedToShowFullScreenContent(adError: AdError) = preload()
        }
        ad.show(activity)
    }

    fun clear() {
        interstitialAd = null
    }

    private companion object {
        const val PREFS_NAME = "ad_frequency"
        const val KEY_ACTION_COUNT = "completed_layout_changes"
    }
}
