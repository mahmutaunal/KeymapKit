package com.alpware.keymapkit.ads

import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.alpware.keymapkit.BuildConfig
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

private enum class BannerState { IDLE, LOADING, LOADED, FAILED, SUSPENDED }

/** A fixed 320x50 banner slot with lifecycle-aware bounded exponential retry. */
@Composable
fun PersistentBanner(
    canRequestAds: Boolean,
    runtimeConfig: AdRuntimeConfig,
    trafficGuard: AdTrafficGuard,
    telemetry: AdTelemetry,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = context as? LifecycleOwner
    val adsEnabled = canRequestAds && runtimeConfig.bannerEnabled &&
        !trafficGuard.isSuspended(AdFormat.BANNER)

    Box(modifier = modifier.fillMaxWidth()) {
        var state by remember { mutableStateOf(BannerState.IDLE) }
        var retryAttempt by remember { mutableIntStateOf(0) }
        var loadNonce by remember { mutableIntStateOf(0) }
        var foreground by remember(lifecycleOwner) {
            mutableStateOf(
                lifecycleOwner?.lifecycle?.currentState?.isAtLeast(Lifecycle.State.STARTED) == true
            )
        }

        val adView = remember {
            AdView(context).apply {
                adUnitId = BuildConfig.ADMOB_BANNER_ID
                setAdSize(AdSize.BANNER)
                onPaidEventListener = { value ->
                    telemetry.paid(AdFormat.BANNER, PLACEMENT, value)
                }
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        retryAttempt = 0
                        state = BannerState.LOADED
                        visibility = View.VISIBLE
                        telemetry.event(AdFormat.BANNER, "loaded", PLACEMENT)
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        state = BannerState.FAILED
                        telemetry.event(
                            AdFormat.BANNER,
                            action = "load_failed",
                            placement = PLACEMENT,
                            attempt = retryAttempt,
                            errorCode = error.code,
                        )
                        if (retryAttempt < AdBackoffPolicy.MAX_BANNER_RETRIES_PER_FOREGROUND_SESSION) {
                            loadNonce += 1
                        }
                    }

                    override fun onAdImpression() {
                        if (!trafficGuard.recordImpression(AdFormat.BANNER)) {
                            state = BannerState.SUSPENDED
                            visibility = View.GONE
                            pause()
                            telemetry.trafficAlert(AdFormat.BANNER, "impression_rate")
                        }
                        telemetry.event(AdFormat.BANNER, "impression", PLACEMENT)
                    }

                    override fun onAdClicked() {
                        if (!trafficGuard.recordClick(AdFormat.BANNER)) {
                            state = BannerState.SUSPENDED
                            visibility = View.GONE
                            pause()
                            telemetry.trafficAlert(AdFormat.BANNER, "click_rate")
                        }
                        telemetry.event(AdFormat.BANNER, "clicked", PLACEMENT)
                    }
                }
            }
        }

        DisposableEffect(lifecycleOwner, adView) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_START -> {
                        foreground = true
                        adView.resume()
                        if (state == BannerState.IDLE || state == BannerState.FAILED) loadNonce += 1
                    }
                    Lifecycle.Event.ON_STOP -> {
                        foreground = false
                        adView.pause()
                    }
                    else -> Unit
                }
            }
            lifecycleOwner?.lifecycle?.addObserver(observer)
            onDispose {
                lifecycleOwner?.lifecycle?.removeObserver(observer)
                adView.destroy()
            }
        }

        LaunchedEffect(adsEnabled, foreground, adView) {
            if (adsEnabled && foreground && state != BannerState.SUSPENDED) {
                adView.visibility = View.VISIBLE
                adView.resume()
            } else {
                adView.visibility = View.GONE
                adView.pause()
            }
        }

        LaunchedEffect(loadNonce, foreground, adsEnabled, adView) {
            if (!foreground || !adsEnabled || state == BannerState.LOADING ||
                state == BannerState.LOADED || state == BannerState.SUSPENDED
            ) return@LaunchedEffect
            if (state == BannerState.FAILED) {
                delay(AdBackoffPolicy.bannerDelayMs(retryAttempt).milliseconds)
                retryAttempt += 1
            }
            if (!foreground || !adsEnabled) return@LaunchedEffect
            if (!trafficGuard.allowLoad(AdFormat.BANNER)) {
                state = BannerState.SUSPENDED
                telemetry.trafficAlert(AdFormat.BANNER, "explicit_load_limit")
                return@LaunchedEffect
            }
            state = BannerState.LOADING
            telemetry.event(
                AdFormat.BANNER,
                action = "load_requested",
                placement = PLACEMENT,
                attempt = retryAttempt,
            )
            adView.loadAd(AdRequest.Builder().build())
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 1.dp,
        ) {
            Column(Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(BANNER_HEIGHT)
                        .background(MaterialTheme.colorScheme.surfaceContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    if (state == BannerState.LOADED && adsEnabled) {
                        AndroidView(
                            factory = { adView },
                            modifier = Modifier.width(BANNER_WIDTH).height(BANNER_HEIGHT),
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            if (state == BannerState.LOADING) {
                                LinearProgressIndicator(
                                    modifier = Modifier
                                        .width(120.dp)
                                        .height(3.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
        }
    }
}

private const val PLACEMENT = "persistent_bottom"
private val BANNER_WIDTH = 320.dp
private val BANNER_HEIGHT = 50.dp
