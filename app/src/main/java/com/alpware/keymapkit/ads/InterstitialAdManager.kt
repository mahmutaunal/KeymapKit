package com.alpware.keymapkit.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.core.content.edit
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.alpware.keymapkit.BuildConfig
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/** A lifecycle-aware, rate-limited interstitial owner with bounded exponential retry. */
class InterstitialAdManager(
    private val activity: Activity,
    private val lifecycleOwner: LifecycleOwner,
    private val canRequestAds: () -> Boolean,
    private val runtimeConfig: () -> AdRuntimeConfig,
    private val trafficGuard: AdTrafficGuard,
    private val telemetry: AdTelemetry,
) : DefaultLifecycleObserver {
    private val preferences = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val handler = Handler(Looper.getMainLooper())
    private val retryRunnable = Runnable { preload() }

    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    private var isForeground = false
    private var retryAttempt = 0

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        isForeground = true
        retryAttempt = 0
        preload()
    }

    override fun onStop(owner: LifecycleOwner) {
        isForeground = false
        handler.removeCallbacks(retryRunnable)
    }

    fun refreshPolicy() {
        if (!runtimeConfig().interstitialEnabled) {
            handler.removeCallbacks(retryRunnable)
            interstitialAd = null
        } else {
            preload()
        }
    }

    fun recordLayoutChange() {
        val count = InterstitialFrequencyPolicy.nextActionCount(
            preferences.getInt(KEY_ACTION_COUNT, 0),
            AdRuntimeConfig.INTERSTITIAL_ACTIONS_REQUIRED,
        )
        preferences.edit { putInt(KEY_ACTION_COUNT, count) }
        if (InterstitialFrequencyPolicy.isEligible(
                count,
                AdRuntimeConfig.INTERSTITIAL_ACTIONS_REQUIRED,
            )
        ) {
            preload()
        }
    }

    fun preload() {
        val config = runtimeConfig()
        if (!isForeground || !config.interstitialEnabled || !canRequestAds()) return
        if (isLoading || interstitialAd != null || !isCurrentlyEligible(config)) return
        if (!trafficGuard.allowLoad(AdFormat.INTERSTITIAL)) {
            telemetry.trafficAlert(AdFormat.INTERSTITIAL, "explicit_load_limit")
            return
        }

        handler.removeCallbacks(retryRunnable)
        isLoading = true
        telemetry.event(
            AdFormat.INTERSTITIAL,
            action = "load_requested",
            placement = PLACEMENT,
            attempt = retryAttempt,
        )
        InterstitialAd.load(
            activity,
            BuildConfig.ADMOB_INTERSTITIAL_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    isLoading = false
                    retryAttempt = 0
                    ad.onPaidEventListener = { value ->
                        telemetry.paid(AdFormat.INTERSTITIAL, PLACEMENT, value)
                    }
                    interstitialAd = ad
                    telemetry.event(AdFormat.INTERSTITIAL, "loaded", PLACEMENT)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    isLoading = false
                    interstitialAd = null
                    telemetry.event(
                        AdFormat.INTERSTITIAL,
                        action = "load_failed",
                        placement = PLACEMENT,
                        attempt = retryAttempt,
                        errorCode = error.code,
                    )
                    scheduleRetry()
                }
            },
        )
    }

    /** Shows only at the natural transition from layout management back to the app. */
    fun showIfEligible() {
        val config = runtimeConfig()
        if (!isForeground || !canRequestAds() || !isCurrentlyEligible(config)) return
        val ad = interstitialAd
        if (ad == null || activity.isFinishing || activity.isDestroyed) {
            preload()
            return
        }

        preferences.edit { putInt(KEY_ACTION_COUNT, 0) }
        interstitialAd = null
        handler.removeCallbacks(retryRunnable)
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                telemetry.event(AdFormat.INTERSTITIAL, "shown", PLACEMENT)
            }

            override fun onAdImpression() {
                if (!trafficGuard.recordImpression(AdFormat.INTERSTITIAL)) {
                    telemetry.trafficAlert(AdFormat.INTERSTITIAL, "impression_rate")
                }
                telemetry.event(AdFormat.INTERSTITIAL, "impression", PLACEMENT)
            }

            override fun onAdClicked() {
                if (!trafficGuard.recordClick(AdFormat.INTERSTITIAL)) {
                    telemetry.trafficAlert(AdFormat.INTERSTITIAL, "click_rate")
                }
                telemetry.event(AdFormat.INTERSTITIAL, "clicked", PLACEMENT)
            }

            override fun onAdDismissedFullScreenContent() {
                telemetry.event(AdFormat.INTERSTITIAL, "dismissed", PLACEMENT)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                telemetry.event(
                    AdFormat.INTERSTITIAL,
                    action = "show_failed",
                    placement = PLACEMENT,
                    errorCode = adError.code,
                )
            }
        }
        ad.show(activity)
    }

    fun clear() {
        handler.removeCallbacksAndMessages(null)
        lifecycleOwner.lifecycle.removeObserver(this)
        interstitialAd = null
    }

    private fun isCurrentlyEligible(config: AdRuntimeConfig): Boolean {
        if (!config.interstitialEnabled || trafficGuard.isSuspended(AdFormat.INTERSTITIAL)) {
            return false
        }
        return InterstitialFrequencyPolicy.isEligible(
            preferences.getInt(KEY_ACTION_COUNT, 0),
            AdRuntimeConfig.INTERSTITIAL_ACTIONS_REQUIRED,
        )
    }

    private fun scheduleRetry() {
        if (!isForeground || retryAttempt >=
            AdBackoffPolicy.MAX_INTERSTITIAL_RETRIES_PER_FOREGROUND_SESSION
        ) return
        val delayMs = AdBackoffPolicy.interstitialDelayMs(retryAttempt)
        retryAttempt += 1
        handler.removeCallbacks(retryRunnable)
        handler.postDelayed(retryRunnable, delayMs)
    }

    private companion object {
        const val PREFS_NAME = "ad_frequency"
        const val KEY_ACTION_COUNT = "completed_layout_changes"
        const val PLACEMENT = "layout_manager_exit"
    }
}
