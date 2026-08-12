package com.alpware.keymapkit.ads

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.alpware.keymapkit.BuildConfig
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import kotlin.math.roundToInt

/** A full-width adaptive banner placed immediately above the system navigation area. */
@Suppress("DEPRECATION") // The newer large adaptive API is intentionally avoided to keep 50 dp height.
@Composable
fun PersistentBanner(
    canRequestAds: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!canRequestAds) return

    val context = LocalContext.current
    val density = LocalDensity.current

    @Suppress("UnusedBoxWithConstraintsScope")
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val availableWidthDp = maxWidth.value.roundToInt().coerceAtLeast(1)
        val adSize = remember(availableWidthDp) {
            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                context,
                availableWidthDp
            )
        }
        val bannerHeight = remember(adSize, density) {
            with(density) { adSize.getHeightInPixels(context).toDp() }
        }
        var loaded by remember(availableWidthDp) { mutableStateOf(false) }
        val adView = remember(availableWidthDp) {
            AdView(context).apply {
                adUnitId = BuildConfig.ADMOB_BANNER_ID
                setAdSize(adSize)
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        loaded = true
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        loaded = false
                    }
                }
                loadAd(AdRequest.Builder().build())
            }
        }

        DisposableEffect(adView) {
            onDispose { adView.destroy() }
        }

        if (loaded) {
            Column(Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(bannerHeight),
                    contentAlignment = Alignment.Center
                ) {
                    AndroidView(
                        factory = { adView },
                        modifier = Modifier.fillMaxWidth().height(bannerHeight)
                    )
                }
                // Keeps Android's gesture bar or navigation buttons outside the ad creative.
                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
        } else {
            AndroidView(
                factory = { adView },
                modifier = Modifier.fillMaxWidth().height(0.dp)
            )
        }
    }
}
