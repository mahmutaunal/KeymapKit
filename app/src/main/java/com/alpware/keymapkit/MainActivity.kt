package com.alpware.keymapkit

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.view.InputDevice
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.alpware.keymapkit.ui.theme.KeymapKitTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT
            )
        )

        setContent {
            KeymapKitTheme {
                Surface(Modifier.fillMaxSize()) {
                    SetupScreen(
                        onOpenKeyboardSettings = {
                            // Best-effort: some ROMs may ignore this and open general Settings.
                            runCatching {
                                startActivity(Intent(Settings.ACTION_HARD_KEYBOARD_SETTINGS))
                            }.getOrElse {
                                startActivity(Intent(Settings.ACTION_SETTINGS))
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SetupScreen(
    onOpenKeyboardSettings: () -> Unit
) {
    val context = LocalContext.current
    val scroll = rememberScrollState()

    var testInput by remember { mutableStateOf("") }
    var showDebug by remember { mutableStateOf(false) }
    var keyLog by remember { mutableStateOf("") }
    var deviceSummary by remember { mutableStateOf(buildKeyboardPresenceSummary(context)) }

    // Refresh on first composition
    LaunchedEffect(Unit) {
        deviceSummary = buildKeyboardPresenceSummary(context)
    }

    Column(
        modifier = Modifier
            .verticalScroll(scroll)
            .padding(16.dp)
            .fillMaxSize()
    ) {
        Spacer(Modifier.height(16.dp))

        Text(
            stringResource(R.string.app_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(4.dp))

        Text(
            stringResource(R.string.app_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        StatusCard(
            deviceSummary = deviceSummary,
            onRefresh = { deviceSummary = buildKeyboardPresenceSummary(context) }
        )

        Spacer(Modifier.height(16.dp))

        SetupStepsCard(onOpenKeyboardSettings = onOpenKeyboardSettings)

        Spacer(Modifier.height(16.dp))

        TestCard(
            value = testInput,
            onValueChange = { testInput = it },
            onKeyLogged = { line ->
                // prepend
                keyLog = line + keyLog
            }
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = { showDebug = !showDebug }) {
                Text(if (showDebug) stringResource(R.string.hide_debug) else stringResource(R.string.show_debug))
            }

            TextButton(onClick = {
                testInput = ""
                keyLog = ""
            }) {
                Text(stringResource(R.string.clear))
            }
        }

        AnimatedVisibility(visible = showDebug) {
            DebugCard(keyLog = keyLog)
        }

        Spacer(Modifier.height(16.dp))

        TroubleshootingCard()
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun StatusCard(
    deviceSummary: String,
    onRefresh: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(
                stringResource(R.string.status_title),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            Text(deviceSummary, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onRefresh) {
                Text(stringResource(R.string.refresh_devices))
            }
        }
    }
}

@Composable
private fun SetupStepsCard(
    onOpenKeyboardSettings: () -> Unit
) {
    Card {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.setup_title), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Text(stringResource(R.string.setup_step_1), style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(R.string.setup_step_2),
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(6.dp))
            Text(stringResource(R.string.setup_step_3), style = MaterialTheme.typography.bodySmall)

            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onOpenKeyboardSettings,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.open_keyboard_settings))
            }
        }
    }
}

@Composable
private fun TestCard(
    value: String,
    onValueChange: (String) -> Unit,
    onKeyLogged: (String) -> Unit
) {
    Card {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.test_title), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .onPreviewKeyEvent { e ->
                        if (e.type == KeyEventType.KeyDown) {
                            val native = e.nativeKeyEvent
                            val line =
                                "keyCode=${native.keyCode}, meta=${native.metaState}, unicode=${native.unicodeChar}\n"
                            onKeyLogged(line)
                        }
                        false
                    },
                label = { Text(stringResource(R.string.test_label)) },
                placeholder = { Text(stringResource(R.string.test_placeholder)) },
                singleLine = false,
                minLines = 3
            )

            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.test_tip),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun DebugCard(keyLog: String) {
    Card {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.debug_title), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            val scroll = rememberScrollState()
            Text(
                text = keyLog.ifBlank { stringResource(R.string.debug_empty) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .verticalScroll(scroll),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun TroubleshootingCard() {
    Card {
        Column(Modifier.padding(16.dp)) {
            Text(
                stringResource(R.string.troubleshooting_title),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))

            Text(
                stringResource(R.string.troubleshooting_item_1),
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.troubleshooting_item_2),
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.troubleshooting_item_3),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun buildKeyboardPresenceSummary(context: android.content.Context): String {
    val ids = InputDevice.getDeviceIds().sorted()
    var keyboards = 0
    val names = mutableListOf<String>()

    for (id in ids) {
        val d = InputDevice.getDevice(id) ?: continue
        val isKeyboard = (d.sources and InputDevice.SOURCE_KEYBOARD) == InputDevice.SOURCE_KEYBOARD
        if (isKeyboard) {
            keyboards++
            names += d.name
        }
    }

    return buildString {
        append(context.getString(R.string.status_layouts_installed))
        append("\n")
        append(
            context.getString(
                R.string.status_keyboards_detected,
                keyboards
            )
        )
        append("\n")
        if (names.isNotEmpty()) {
            append(context.getString(R.string.status_devices))
            append("\n")
            names.distinct().forEach {
                append("• ").append(it).append("\n")
            }
        }
    }
}