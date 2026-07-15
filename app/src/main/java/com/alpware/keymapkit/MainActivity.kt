package com.alpware.keymapkit

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alpware.keymapkit.layout.LayoutSelectionRepository
import com.alpware.keymapkit.ui.*
import com.alpware.keymapkit.ui.theme.KeymapKitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        val repository = LayoutSelectionRepository(this).also { it.prepareFreshInstall() }
        setContent {
            KeymapKitTheme {
                val navController = rememberNavController()
                var configured by remember { mutableStateOf(repository.isConfigured) }
                Surface(Modifier.fillMaxSize()) {
                    if (!configured) {
                        OnboardingScreen(repository) { configured = true }
                    } else {
                        NavHost(
                            navController = navController,
                            startDestination = "home"
                        ) {
                            composable("home") {
                                HomeScreen(
                                    repository = repository,
                                    onManageLayouts = {
                                        navController.navigate("layouts")
                                    },
                                    onOpenKeyboardSettings = {
                                        openKeyboardSettings()
                                    },
                                    onOpenSettings = {
                                        navController.navigate("settings")
                                    }
                                )
                            }

                            composable("layouts") {
                                LayoutManagerScreen(
                                    repository = repository,
                                    onBack = {
                                        navController.popBackStack()
                                    }
                                )
                            }

                            composable("settings") {
                                SettingsScreen(
                                    onBack = {
                                        navController.popBackStack()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun openKeyboardSettings() {
        runCatching { startActivity(Intent(Settings.ACTION_HARD_KEYBOARD_SETTINGS)) }
            .getOrElse { startActivity(Intent(Settings.ACTION_SETTINGS)) }
    }
}
