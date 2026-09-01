package com.alpware.keymapkit.ads

/**
 * Runtime monetization policy. Remote values are always clamped to the immutable safety
 * envelope below, so a console mistake cannot make ads more aggressive than the binary allows.
 */
data class AdRuntimeConfig(
    val bannerEnabled: Boolean = true,
    val interstitialEnabled: Boolean = true,
) {
    companion object {
        const val INTERSTITIAL_ACTIONS_REQUIRED = 3

        fun safe(
            bannerEnabled: Boolean,
            interstitialEnabled: Boolean,
        ) = AdRuntimeConfig(
            bannerEnabled = bannerEnabled,
            interstitialEnabled = interstitialEnabled,
        )
    }
}
