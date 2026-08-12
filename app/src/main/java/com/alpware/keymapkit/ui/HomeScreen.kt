package com.alpware.keymapkit.ui

import android.content.Context
import android.hardware.input.InputManager
import android.view.InputDevice
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(stringResource(R.string.app_title), fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Outlined.Settings, stringResource(R.string.settings_title))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        BoxWithConstraints(Modifier.fillMaxSize().padding(padding)) {
            val useTwoPanes = maxWidth >= KeymapTwoPaneBreakpoint
            val contentWidth = if (useTwoPanes) KeymapWideContentMaxWidth else KeymapContentMaxWidth
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = if (useTwoPanes) 24.dp else 16.dp,
                    end = if (useTwoPanes) 24.dp else 16.dp,
                    top = 12.dp,
                    bottom = 32.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (useTwoPanes) {
                    item {
                        Row(
                            Modifier.widthIn(max = contentWidth).fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            HomeHero(
                                modifier = Modifier.weight(1f),
                                selectedCount = selectedCount,
                                onManageLayouts = onManageLayouts
                            )
                            SetupCard(
                                modifier = Modifier.weight(1f),
                                onOpenKeyboardSettings = onOpenKeyboardSettings
                            )
                        }
                    }
                } else {
                    item {
                        HomeHero(
                            modifier = Modifier.widthIn(max = contentWidth).fillMaxWidth(),
                            selectedCount = selectedCount,
                            onManageLayouts = onManageLayouts
                        )
                    }
                }
                item {
                    Row(
                        modifier = Modifier.widthIn(max = contentWidth).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatusCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Outlined.Tune,
                            value = selectedCount.toString(),
                            label = stringResource(R.string.active_layouts)
                        )
                        StatusCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Outlined.Keyboard,
                            value = keyboards.size.toString(),
                            label = stringResource(R.string.physical_keyboards)
                        )
                    }
                }
                if (!useTwoPanes) {
                    item {
                        SetupCard(
                            modifier = Modifier.widthIn(max = contentWidth).fillMaxWidth(),
                            onOpenKeyboardSettings = onOpenKeyboardSettings
                        )
                    }
                }
                item {
                    if (useTwoPanes) {
                        Row(
                            Modifier.widthIn(max = contentWidth).fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            TypingTestCard(
                                modifier = Modifier.weight(1f),
                                text = previewText,
                                onTextChange = { previewText = it }
                            )
                            DiagnosticsCard(
                                modifier = Modifier.weight(1f),
                                keyboards = keyboards,
                                onRefresh = {
                                    keyboards = detectKeyboards(context)
                                    selectedCount = repository.selectedIds().size
                                }
                            )
                        }
                    } else {
                        TypingTestCard(
                            modifier = Modifier.widthIn(max = contentWidth).fillMaxWidth(),
                            text = previewText,
                            onTextChange = { previewText = it }
                        )
                    }
                }
                if (!useTwoPanes) {
                    item {
                        DiagnosticsCard(
                            modifier = Modifier.widthIn(max = contentWidth).fillMaxWidth(),
                            keyboards = keyboards,
                            onRefresh = {
                                keyboards = detectKeyboards(context)
                                selectedCount = repository.selectedIds().size
                            }
                        )
                    }
                }
                item {
                    Surface(
                        modifier = Modifier.widthIn(max = contentWidth).fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(Icons.Outlined.CheckCircle, null, modifier = Modifier.size(20.dp))
                            Text(
                                stringResource(R.string.layout_change_warning),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHero(
    selectedCount: Int,
    onManageLayouts: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            TonalIcon(
                Icons.Outlined.Keyboard,
                size = 56.dp,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
            Text(stringResource(R.string.home_heading), style = MaterialTheme.typography.headlineMedium)
            Text(
                stringResource(R.string.home_body),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            Spacer(Modifier.height(2.dp))
            Button(
                onClick = onManageLayouts,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Icon(Icons.Outlined.Tune, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.manage_layouts))
                Spacer(Modifier.weight(1f))
                Text(selectedCount.toString())
            }
        }
    }
}

@Composable
private fun StatusCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            TonalIcon(icon, size = 40.dp)
            Text(value, style = MaterialTheme.typography.headlineSmall)
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SetupCard(
    onOpenKeyboardSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            SectionHeading(
                title = stringResource(R.string.setup_title),
                supportingText = stringResource(R.string.home_setup_supporting)
            )
            SetupStep(1, stringResource(R.string.home_setup_step_connect))
            SetupStep(2, stringResource(R.string.home_setup_step_select))
            SetupStep(3, stringResource(R.string.home_setup_step_test))
            Button(onClick = onOpenKeyboardSettings, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.Settings, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.open_physical_keyboard_settings))
            }
        }
    }
}

@Composable
private fun SetupStep(number: Int, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = androidx.compose.foundation.shape.CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ) {
            androidx.compose.foundation.layout.Box(
                Modifier.size(32.dp),
                contentAlignment = Alignment.Center
            ) { Text(number.toString(), style = MaterialTheme.typography.labelLarge) }
        }
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun TypingTestCard(
    text: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionHeading(
                title = stringResource(R.string.preview_title),
                supportingText = stringResource(R.string.preview_body)
            )
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = MaterialTheme.shapes.medium,
                placeholder = { Text(stringResource(R.string.preview_placeholder)) }
            )
        }
    }
}

@Composable
private fun DiagnosticsCard(
    keyboards: List<String>,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.diagnostics_title), style = MaterialTheme.typography.titleLarge)
                    Text(
                        stringResource(R.string.provider_ready),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                FilledTonalIconButton(onClick = onRefresh) {
                    Icon(Icons.Outlined.Refresh, stringResource(R.string.action_refresh))
                }
            }
            if (keyboards.isEmpty()) {
                Text(
                    stringResource(R.string.home_no_keyboard),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                keyboards.take(3).forEach { keyboard ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Keyboard, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(keyboard, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

private fun detectKeyboards(context: Context): List<String> {
    val manager = context.getSystemService(InputManager::class.java)
    return manager.inputDeviceIds
        .asSequence()
        .mapNotNull(manager::getInputDevice)
        .filter { !it.isVirtual && it.keyboardType == InputDevice.KEYBOARD_TYPE_ALPHABETIC }
        .map { it.name }
        .distinct()
        .toList()
}
