package com.alpware.keymapkit.ads

import android.content.Context
import android.os.Bundle
import com.alpware.keymapkit.BuildConfig
import com.google.android.gms.ads.AdValue
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

object FirebaseBootstrap {
    fun initialize(context: Context): FirebaseApp? =
        FirebaseApp.getApps(context).firstOrNull() ?: FirebaseApp.initializeApp(context)
}

/** Remote controls can disable ads or make the baked-in safety policy stricter, never looser. */
class AdRemoteConfig(private val context: Context) {
    fun fetchOnce(onUpdated: (AdRuntimeConfig) -> Unit) {
        if (FirebaseBootstrap.initialize(context) == null) {
            onUpdated(AdRuntimeConfig())
            return
        }

        val remoteConfig = FirebaseRemoteConfig.getInstance()
        remoteConfig.setConfigSettingsAsync(
            FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(MINIMUM_FETCH_INTERVAL_SECONDS)
                .build()
        )
        remoteConfig.setDefaultsAsync(DEFAULTS).addOnCompleteListener {
            onUpdated(readSafe(remoteConfig))
            remoteConfig.fetchAndActivate().addOnCompleteListener {
                onUpdated(readSafe(remoteConfig))
            }
        }
    }

    private fun readSafe(remote: FirebaseRemoteConfig) = AdRuntimeConfig.safe(
        bannerEnabled = remote.getBoolean(KEY_BANNER_ENABLED),
        interstitialEnabled = remote.getBoolean(KEY_INTERSTITIAL_ENABLED),
    )

    companion object {
        const val KEY_BANNER_ENABLED = "ads_banner_enabled"
        const val KEY_INTERSTITIAL_ENABLED = "ads_interstitial_enabled"

        const val MINIMUM_FETCH_INTERVAL_SECONDS = 12 * 60 * 60L

        val DEFAULTS: Map<String, Any> = mapOf(
            KEY_BANNER_ENABLED to true,
            KEY_INTERSTITIAL_ENABLED to true,
        )
    }
}

/**
 * Privacy-conscious aggregate telemetry. Firebase automatically supplies country, app version,
 * platform and acquisition dimensions; only operational ad metadata is added here.
 */
class AdTelemetry(context: Context) {
    private val analytics = FirebaseBootstrap.initialize(context)?.let {
        FirebaseAnalytics.getInstance(context)
    }

    init {
        analytics?.setDefaultEventParameters(Bundle().apply {
            putString(PARAM_APP_VERSION, BuildConfig.VERSION_NAME)
        })
    }

    fun event(
        format: AdFormat,
        action: String,
        placement: String,
        attempt: Int? = null,
        errorCode: Int? = null,
    ) {
        analytics?.logEvent(EVENT_AD_OPERATION, Bundle().apply {
            putString(PARAM_AD_FORMAT, format.storageKey)
            putString(PARAM_ACTION, action)
            putString(PARAM_PLACEMENT, placement)
            attempt?.let { putLong(PARAM_ATTEMPT, it.toLong()) }
            errorCode?.let { putLong(PARAM_ERROR_CODE, it.toLong()) }
        })
    }

    fun paid(format: AdFormat, placement: String, value: AdValue) {
        analytics?.logEvent(EVENT_AD_REVENUE, Bundle().apply {
            putString(PARAM_AD_FORMAT, format.storageKey)
            putString(PARAM_PLACEMENT, placement)
            putString(FirebaseAnalytics.Param.CURRENCY, value.currencyCode)
            putDouble(FirebaseAnalytics.Param.VALUE, value.valueMicros / 1_000_000.0)
            putLong(PARAM_PRECISION, value.precisionType.toLong())
        })
    }

    fun trafficAlert(format: AdFormat, reason: String) {
        analytics?.logEvent(EVENT_TRAFFIC_ALERT, Bundle().apply {
            putString(PARAM_AD_FORMAT, format.storageKey)
            putString(PARAM_REASON, reason)
        })
    }

    fun consentResult(canRequestAds: Boolean, errorCode: Int?) {
        analytics?.logEvent(EVENT_CONSENT_RESULT, Bundle().apply {
            putLong(PARAM_CAN_REQUEST_ADS, if (canRequestAds) 1L else 0L)
            errorCode?.let { putLong(PARAM_ERROR_CODE, it.toLong()) }
        })
    }

    private companion object {
        const val EVENT_AD_OPERATION = "monetization_ad_event"
        const val EVENT_AD_REVENUE = "monetization_ad_revenue"
        const val EVENT_TRAFFIC_ALERT = "ad_traffic_alert"
        const val EVENT_CONSENT_RESULT = "ad_consent_result"
        const val PARAM_AD_FORMAT = "ad_format"
        const val PARAM_ACTION = "ad_action"
        const val PARAM_PLACEMENT = "ad_placement"
        const val PARAM_ATTEMPT = "retry_attempt"
        const val PARAM_ERROR_CODE = "error_code"
        const val PARAM_REASON = "alert_reason"
        const val PARAM_PRECISION = "value_precision"
        const val PARAM_APP_VERSION = "app_version_name"
        const val PARAM_CAN_REQUEST_ADS = "can_request_ads"
    }
}
