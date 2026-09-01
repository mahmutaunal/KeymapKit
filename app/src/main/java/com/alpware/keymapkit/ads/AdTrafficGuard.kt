package com.alpware.keymapkit.ads

import android.content.Context
import androidx.core.content.edit

enum class AdFormat(val storageKey: String) {
    BANNER("banner"),
    INTERSTITIAL("interstitial"),
}

/**
 * A device-local, non-resettable-by-Remote-Config circuit breaker. It caps explicit requests and
 * suspends a format for 24 hours when an abnormal impression/click rate is observed.
 */
class AdTrafficGuard(context: Context) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    @Synchronized
    fun allowLoad(format: AdFormat, nowMs: Long = System.currentTimeMillis()): Boolean {
        if (isSuspended(format, nowMs)) return false
        val limit = when (format) {
            AdFormat.BANNER -> MAX_BANNER_EXPLICIT_LOADS_PER_HOUR
            AdFormat.INTERSTITIAL -> MAX_INTERSTITIAL_EXPLICIT_LOADS_PER_HOUR
        }
        return incrementWindow(format, "load", HOUR_MS, limit, nowMs)
    }

    @Synchronized
    fun recordImpression(format: AdFormat, nowMs: Long = System.currentTimeMillis()): Boolean {
        if (format == AdFormat.INTERSTITIAL) return true
        return if (
            incrementWindow(
                format,
                "impression",
                HOUR_MS,
                MAX_BANNER_IMPRESSIONS_PER_HOUR,
                nowMs,
            )
        ) {
            true
        } else {
            suspend(format, nowMs)
            false
        }
    }

    @Synchronized
    fun recordClick(format: AdFormat, nowMs: Long = System.currentTimeMillis()): Boolean {
        return if (incrementWindow(format, "click", DAY_MS, MAX_CLICKS_PER_DAY, nowMs)) {
            true
        } else {
            suspend(format, nowMs)
            false
        }
    }

    fun isSuspended(format: AdFormat, nowMs: Long = System.currentTimeMillis()): Boolean =
        preferences.getLong("${format.storageKey}_suspended_until", 0L) > nowMs

    @Synchronized
    private fun incrementWindow(
        format: AdFormat,
        event: String,
        windowMs: Long,
        limit: Int,
        nowMs: Long,
    ): Boolean {
        val prefix = "${format.storageKey}_$event"
        val start = preferences.getLong("${prefix}_window_start", 0L)
        val validWindow = start > 0L && nowMs >= start && nowMs - start < windowMs
        val count = if (validWindow) preferences.getInt("${prefix}_count", 0) + 1 else 1
        preferences.edit {
            if (!validWindow) putLong("${prefix}_window_start", nowMs)
            putInt("${prefix}_count", count)
        }
        return count <= limit
    }

    private fun suspend(format: AdFormat, nowMs: Long) {
        preferences.edit {
            putLong("${format.storageKey}_suspended_until", nowMs + SUSPENSION_MS)
        }
    }

    private companion object {
        const val PREFS_NAME = "ad_traffic_guard"
        const val HOUR_MS = 60 * 60 * 1000L
        const val DAY_MS = 24 * HOUR_MS
        const val SUSPENSION_MS = DAY_MS

        const val MAX_BANNER_EXPLICIT_LOADS_PER_HOUR = 6
        const val MAX_INTERSTITIAL_EXPLICIT_LOADS_PER_HOUR = 4
        const val MAX_BANNER_IMPRESSIONS_PER_HOUR = 30
        const val MAX_CLICKS_PER_DAY = 2
    }
}
