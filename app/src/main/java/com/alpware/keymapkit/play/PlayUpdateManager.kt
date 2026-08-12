package com.alpware.keymapkit.play

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

/** Coordinates Google Play flexible/immediate in-app updates without leaking Play Core into UI code. */
class PlayUpdateManager(
    activity: Activity,
    private val updateLauncher: ActivityResultLauncher<IntentSenderRequest>,
    private val listener: Listener
) {
    private val manager: AppUpdateManager = AppUpdateManagerFactory.create(activity)
    private var listenerRegistered = false

    private val installStateListener = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADED -> listener.onUpdateDownloaded()
            InstallStatus.FAILED -> listener.onUpdateError()
            else -> Unit
        }
    }

    fun checkForUpdate(userInitiated: Boolean) {
        manager.appUpdateInfo
            .addOnSuccessListener { info -> handleUpdateInfo(info, userInitiated) }
            .addOnFailureListener { listener.onUpdateCheckFailed(userInitiated) }
    }

    fun resumeInterruptedUpdate() {
        manager.appUpdateInfo.addOnSuccessListener { info ->
            when {
                info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                    launch(info, AppUpdateType.IMMEDIATE)
                }
                info.installStatus() == InstallStatus.DOWNLOADED -> listener.onUpdateDownloaded()
                info.installStatus() == InstallStatus.PENDING ||
                    info.installStatus() == InstallStatus.DOWNLOADING -> registerInstallListener()
            }
        }
    }

    fun completeUpdate() {
        manager.completeUpdate().addOnFailureListener { listener.onUpdateError() }
    }

    fun close() {
        if (listenerRegistered) {
            manager.unregisterListener(installStateListener)
            listenerRegistered = false
        }
    }

    private fun handleUpdateInfo(info: AppUpdateInfo, userInitiated: Boolean) {
        if (info.updateAvailability() != UpdateAvailability.UPDATE_AVAILABLE) {
            if (userInitiated) listener.onNoUpdateAvailable()
            return
        }

        val immediateRecommended =
            info.updatePriority() >= IMMEDIATE_UPDATE_PRIORITY ||
                (info.clientVersionStalenessDays() ?: 0) >= IMMEDIATE_UPDATE_STALENESS_DAYS

        val type = when {
            immediateRecommended && info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) -> AppUpdateType.IMMEDIATE
            info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) -> AppUpdateType.FLEXIBLE
            info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) -> AppUpdateType.IMMEDIATE
            else -> null
        }

        if (type == null) {
            listener.onUpdateNotAllowed()
            return
        }

        if (type == AppUpdateType.FLEXIBLE) registerInstallListener()
        launch(info, type)
    }

    private fun launch(info: AppUpdateInfo, updateType: Int) {
        runCatching {
            manager.startUpdateFlowForResult(
                info,
                updateLauncher,
                AppUpdateOptions.newBuilder(updateType).build()
            )
        }.onFailure { listener.onUpdateError() }
    }

    private fun registerInstallListener() {
        if (!listenerRegistered) {
            manager.registerListener(installStateListener)
            listenerRegistered = true
        }
    }

    interface Listener {
        fun onNoUpdateAvailable()
        fun onUpdateDownloaded()
        fun onUpdateCheckFailed(userInitiated: Boolean)
        fun onUpdateNotAllowed()
        fun onUpdateError()
    }

    private companion object {
        const val IMMEDIATE_UPDATE_PRIORITY = 4
        const val IMMEDIATE_UPDATE_STALENESS_DAYS = 7
    }
}
