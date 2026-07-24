package com.alpware.keymapkit.ui

import android.content.Context
import android.hardware.input.InputManager
import android.view.InputDevice
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alpware.keymapkit.R
import com.alpware.keymapkit.layout.LayoutSelectionRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: LayoutSelectionRepository,
    onManageLayouts: () -> Unit,
    onOpenKeyboardSettings: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val context = LocalContext.current
    var selectedCount by remember { mutableIntStateOf(repository.selectedIds().size) }
    var keyboards by remember { mutableStateOf(detectKeyboards(context)) }
    var previewText by rememberSaveable { mutableStateOf("") }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text(stringResource(R.string.app_title)) },
            actions = {
                IconButton(onClick = onOpenSettings) {
                    Icon(
                        Icons.Outlined.Settings,
                        stringResource(R.string.settings_title)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                scrolledContainerColor = MaterialTheme.colorScheme.background
            )
        )
    }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                stringResource(R.string.home_heading),
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                stringResource(R.string.home_body),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(
                            Icons.Outlined.Tune,
                            null
                        ); Text(
                        stringResource(R.string.active_layouts),
                        style = MaterialTheme.typography.titleMedium
                    )
                    }
                    Text(
                        stringResource(R.string.active_layouts_count, selectedCount),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Button(
                        onClick = { onManageLayouts() },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.manage_layouts)) }
                }
            }

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(
                            Icons.Outlined.Keyboard,
                            null
                        ); Text(
                        stringResource(R.string.diagnostics_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    }
                    DiagnosticRow(
                        stringResource(R.string.provider_status),
                        stringResource(R.string.provider_ready)
                    )
                    DiagnosticRow(
                        stringResource(R.string.physical_keyboards),
                        keyboards.size.toString()
                    )
                    keyboards.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
                    OutlinedButton(
                        onClick = {
                            keyboards = detectKeyboards(context); selectedCount =
                            repository.selectedIds().size
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.action_refresh)) }
                }
            }

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        stringResource(R.string.setup_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        stringResource(R.string.setup_steps_compact),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = onOpenKeyboardSettings,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.open_physical_keyboard_settings)) }
                }
            }

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(R.string.preview_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        stringResource(R.string.preview_body),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = previewText,
                        onValueChange = { previewText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(stringResource(R.string.preview_placeholder))
                        }
                    )
                }
            }

            Text(
                stringResource(R.string.layout_change_warning),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DiagnosticRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) { Text(label); Text(value, style = MaterialTheme.typography.labelLarge) }
}

private fun detectKeyboards(context: Context): List<String> {
    val manager = context.getSystemService(InputManager::class.java)

    return manager.inputDeviceIds
        .asSequence().mapNotNull { deviceId -> manager.getInputDevice(deviceId) }
        .filter { device ->
            !device.isVirtual &&
                    device.keyboardType == InputDevice.KEYBOARD_TYPE_ALPHABETIC
        }
        .map { device -> device.name }
        .distinct()
        .toList()
}
