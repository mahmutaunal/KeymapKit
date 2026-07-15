package com.alpware.keymapkit.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.alpware.keymapkit.R
import com.alpware.keymapkit.layout.KeyboardLayoutCatalog
import com.alpware.keymapkit.layout.LayoutCategory
import com.alpware.keymapkit.layout.LayoutSelectionRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LayoutManagerScreen(repository: LayoutSelectionRepository, onBack: () -> Unit) {
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(repository.selectedIds()) }
    var category by remember { mutableStateOf<LayoutCategory?>(null) }
    val context = LocalContext.current
    val filtered = KeyboardLayoutCatalog.all.filter { item ->
        val label = context.getString(item.labelRes)
        (query.isBlank() || label.contains(query, true) || item.id.contains(query, true)) && (category == null || item.category == category)
    }
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.manage_layouts)) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, null) } }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(value = query, onValueChange = { query = it }, leadingIcon = { Icon(Icons.Outlined.Search, null) }, placeholder = { Text(stringResource(R.string.search_layouts)) }, modifier = Modifier.fillMaxWidth().padding(16.dp), singleLine = true)
            ScrollableTabRow(selectedTabIndex = (LayoutCategory.entries.indexOf(category) + 1).coerceAtLeast(0), edgePadding = 12.dp) {
                Tab(selected = category == null, onClick = { category = null }, text = { Text(stringResource(R.string.all_layouts)) })
                LayoutCategory.entries.forEach { c -> Tab(selected = category == c, onClick = { category = c }, text = { Text(c.name.lowercase().replaceFirstChar { it.titlecase() }) }) }
            }
            Text(stringResource(R.string.selected_count, selected.size), modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), style = MaterialTheme.typography.labelLarge)
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(filtered, key = { it.id }) { item ->
                    val checked = item.id in selected
                    ListItem(
                        headlineContent = { Text(stringResource(item.labelRes), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        supportingContent = { Text(item.category.name.lowercase().replaceFirstChar { it.titlecase() }) },
                        trailingContent = { Switch(checked = checked, onCheckedChange = { value -> repository.setSelected(item.id, value); selected = repository.selectedIds() }) },
                        modifier = Modifier.fillMaxWidth().clickable { repository.setSelected(item.id, !checked); selected = repository.selectedIds() }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
