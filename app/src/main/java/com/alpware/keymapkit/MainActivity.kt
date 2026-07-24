package com.alpware.keymapkit

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alpware.keymapkit.layout.LayoutSelectionRepository
import com.alpware.keymapkit.play.PlayReviewManager
import com.alpware.keymapkit.play.PlayUpdateManager
import com.alpware.keymapkit.play.ReviewPromptCoordinator
import com.alpware.keymapkit.play.ReviewResult
import com.alpware.keymapkit.ui.*
import com.alpware.keymapkit.ui.theme.KeymapKitTheme

class MainActivity : ComponentActivity(), PlayUpdateManager.Listener {
    private val updateLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK) {
            Toast.makeText(this, R.string.update_cancelled, Toast.LENGTH_SHORT).show()
        }
    }

    private lateinit var playUpdateManager: PlayUpdateManager
    private lateinit var playReviewManager: PlayReviewManager
    private lateinit var reviewPromptCoordinator: ReviewPromptCoordinator
    private var updateDownloadedDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        )

        playUpdateManager = PlayUpdateManager(this, updateLauncher, this)
        playReviewManager = PlayReviewManager(this)
        reviewPromptCoordinator = ReviewPromptCoordinator(this).also { it.recordSession() }

        val repository = LayoutSelectionRepository(this).also { it.prepareFreshInstall() }
        setContent {
            KeymapKitTheme {
                val navController = rememberNavController()
                var configured by remember { mutableStateOf(repository.isConfigured) }
                Surface(Modifier.fillMaxSize()) {
                    if (!configured) {
                        OnboardingScreen(repository) {
                            configured = true
                            reviewPromptCoordinator.recordMeaningfulAction()
                            requestAutomaticReviewIfEligible()
                        }
                    } else {
                        NavHost(navController = navController, startDestination = "home") {
                            composable("home") {
                                HomeScreen(
                                    repository = repository,
                                    onManageLayouts = { navController.navigate("layouts") },
                                    onOpenKeyboardSettings = ::openKeyboardSettings,
                                    onOpenSettings = { navController.navigate("settings") }
                                )
                            }
                            composable("layouts") {
                                LayoutManagerScreen(
                                    repository = repository,
                                    onBack = { navController.popBackStack() },
                                    onLayoutChanged = {
                                        reviewPromptCoordinator.recordLayoutChanged()
                                        requestAutomaticReviewIfEligible()
                                    }
                                )
                            }
                            composable("settings") {
                                SettingsScreen(
                                    onBack = { navController.popBackStack() },
                                    onRequestReview = ::requestInAppReview,
                                    onCheckForUpdate = { playUpdateManager.checkForUpdate(true) }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (savedInstanceState == null) playUpdateManager.checkForUpdate(false)
    }

    override fun onResume() {
        super.onResume()
        if (::playUpdateManager.isInitialized) playUpdateManager.resumeInterruptedUpdate()
    }

    override fun onDestroy() {
        updateDownloadedDialog?.dismiss()
        if (::playUpdateManager.isInitialized) playUpdateManager.close()
        super.onDestroy()
    }

    private fun openKeyboardSettings() {
        reviewPromptCoordinator.recordKeyboardSettingsOpened()
        requestAutomaticReviewIfEligible()
        runCatching { startActivity(Intent(Settings.ACTION_HARD_KEYBOARD_SETTINGS)) }
            .getOrElse { startActivity(Intent(Settings.ACTION_SETTINGS)) }
    }

    private fun requestAutomaticReviewIfEligible() {
        if (!reviewPromptCoordinator.shouldRequestAutomaticReview()) return
        reviewPromptCoordinator.recordAutomaticPromptAttempt()
        playReviewManager.requestReview { }
    }

    private fun requestInAppReview() {
        playReviewManager.requestReview { result ->
            val message = when (result) {
                ReviewResult.Completed -> R.string.review_flow_completed
                is ReviewResult.Failed -> R.string.review_flow_unavailable
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onNoUpdateAvailable() = toast(R.string.update_not_available)
    override fun onUpdateCheckFailed(userInitiated: Boolean) { if (userInitiated) toast(R.string.update_check_failed) }
    override fun onUpdateNotAllowed() = toast(R.string.update_not_supported)
    override fun onUpdateError() = toast(R.string.update_error)

    override fun onUpdateDownloaded() {
        if (isFinishing || isDestroyed || updateDownloadedDialog?.isShowing == true) return
        updateDownloadedDialog = AlertDialog.Builder(this)
            .setTitle(R.string.update_ready_title)
            .setMessage(R.string.update_ready_message)
            .setCancelable(false)
            .setPositiveButton(R.string.update_restart_now) { _, _ -> playUpdateManager.completeUpdate() }
            .setNegativeButton(R.string.update_restart_later, null)
            .show()
    }

    private fun toast(message: Int) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
