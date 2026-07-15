package com.alpware.keymapkit.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alpware.keymapkit.R
import com.alpware.keymapkit.layout.KeyboardLayoutCatalog
import com.alpware.keymapkit.layout.LayoutSelectionRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(repository: LayoutSelectionRepository, onDone: () -> Unit) {
    val recommended = remember { repository.recommendedIds() }
    var selected by remember { mutableStateOf(recommended) }
    val items = remember { KeyboardLayoutCatalog.all.filter { it.id in recommended || it.popular }.distinctBy { it.id } }

    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.onboarding_title)) }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {
            Icon(Icons.Outlined.Keyboard, null, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.onboarding_heading), style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.onboarding_body), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.recommended_layouts), style = MaterialTheme.typography.titleMedium)
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(items, key = { it.id }) { item ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f)) {
                                Text(stringResource(item.labelRes), style = MaterialTheme.typography.bodyLarge)
                                Text(item.category.name.lowercase().replaceFirstChar { it.titlecase() }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Checkbox(checked = item.id in selected, onCheckedChange = { checked -> selected = selected.toMutableSet().apply { if (checked) add(item.id) else remove(item.id) } })
                        }
                    }
                }
            }
            Button(onClick = { repository.applySelection(selected); onDone() }, enabled = selected.isNotEmpty(), modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.continue_with_count, selected.size))
            }
            TextButton(onClick = { repository.applySelection(recommended.ifEmpty { setOf(KeyboardLayoutCatalog.all.first().id) }); onDone() }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.use_recommended))
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}
