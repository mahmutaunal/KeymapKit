package com.alpware.keymapkit.ui

import android.view.InputDevice
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alpware.keymapkit.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenKeyboardSettings: () -> Unit,
    onOpenSettings: () -> Unit = {},
) {
    val context = LocalContext.current
    val scroll = rememberScrollState()

    var testInput by remember { mutableStateOf("") }
    var debugExpanded by remember { mutableStateOf(false) }
    var keyLog by remember { mutableStateOf("") }
    var statusState by remember { mutableStateOf(buildKeyboardPresenceState(context)) }

    // Refresh on first composition
    LaunchedEffect(Unit) {
        statusState = buildKeyboardPresenceState(context)
    }

    val maxContentWidth = 680.dp

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.app_title),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.settings_title)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = maxContentWidth)
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .verticalScroll(scroll)
                    .padding(horizontal = 16.dp),
            ) {
                Spacer(Modifier.height(4.dp))

                StatusCard(
                    layoutsInstalledText = statusState.layoutsInstalledText,
                    keyboardsDetectedText = statusState.keyboardsDetectedText,
                    devices = statusState.devices,
                    onRefresh = { statusState = buildKeyboardPresenceState(context) }
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

                Spacer(Modifier.height(16.dp))

                DebugCard(
                    keyLog = keyLog,
                    expanded = debugExpanded,
                    onToggleExpanded = { debugExpanded = !debugExpanded },
                    onClear = {
                        testInput = ""
                        keyLog = ""
                    }
                )

                Spacer(Modifier.height(16.dp))

                TroubleshootingCard()

                Spacer(Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.developer_signature),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun StatusCard(
    layoutsInstalledText: String,
    keyboardsDetectedText: String,
    devices: List<String>,
    onRefresh: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(
                stringResource(R.string.status_title),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(8.dp))

            Text(
                layoutsInstalledText,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(6.dp))

            Text(
                keyboardsDetectedText,
                style = MaterialTheme.typography.bodySmall
            )

            if (devices.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(R.string.status_devices),
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(Modifier.height(2.dp))
                devices.forEach {
                    Text("• $it", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onRefresh,
                modifier = Modifier.fillMaxWidth()
            ) {
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
private fun DebugCard(
    keyLog: String,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onClear: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .animateContentSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.debug_title),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.weight(1f))

                if (expanded) {
                    TextButton(onClick = onClear) {
                        Text(stringResource(R.string.clear))
                    }
                }

                IconButton(onClick = onToggleExpanded) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) stringResource(R.string.cd_collapse) else stringResource(R.string.cd_expand)
                    )
                }
            }

            if (expanded) {
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
}

@Composable
private fun TroubleshootingCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
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
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(R.string.troubleshooting_item_2),
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(R.string.troubleshooting_item_3),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun buildKeyboardPresenceState(
    context: android.content.Context
): KeyboardStatusState {
    val ids = InputDevice.getDeviceIds().sorted()
    var keyboards = 0
    val names = mutableListOf<String>()

    for (id in ids) {
        val d = InputDevice.getDevice(id) ?: continue
        val isKeyboardSource =
            (d.sources and InputDevice.SOURCE_KEYBOARD) == InputDevice.SOURCE_KEYBOARD

        val isRealExternalKeyboard =
            isKeyboardSource &&
                    !d.isVirtual &&
                    d.keyboardType == InputDevice.KEYBOARD_TYPE_ALPHABETIC

        if (isRealExternalKeyboard) {
            keyboards++
            names += d.name
        }
    }

    return KeyboardStatusState(
        layoutsInstalledText = context.getString(R.string.status_layouts_installed),
        keyboardsDetectedText = context.getString(
            R.string.status_keyboards_detected,
            keyboards
        ),
        devices = names.distinct()
    )
}

private data class KeyboardStatusState(
    val layoutsInstalledText: String,
    val keyboardsDetectedText: String,
    val devices: List<String>
)