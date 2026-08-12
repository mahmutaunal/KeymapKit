package com.alpware.keymapkit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.alpware.keymapkit.R
import com.alpware.keymapkit.layout.KeyboardLayoutCatalog
import com.alpware.keymapkit.layout.KeyboardLayoutItem
import com.alpware.keymapkit.layout.LayoutCategory
import com.alpware.keymapkit.layout.LayoutSelectionRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LayoutManagerScreen(
    repository: LayoutSelectionRepository,
    onBack: () -> Unit,
    onLayoutChanged: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(repository.selectedIds()) }
    var category by remember { mutableStateOf<LayoutCategory?>(null) }

    val filtered = KeyboardLayoutCatalog.all.filter { item ->
        val label = stringResource(item.labelRes)
        (query.isBlank() || label.contains(query, true) || item.id.contains(query, true)) &&
            (category == null || item.category == category)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.manage_layouts))
                        Text(
                            stringResource(R.string.selected_count, selected.size),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            stringResource(R.string.cd_back)
                        )
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
            val useTwoColumns = maxWidth >= KeymapTwoPaneBreakpoint
            val contentWidth = if (useTwoColumns) KeymapWideContentMaxWidth else KeymapContentMaxWidth
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextField(
                    value = query,
                    onValueChange = { query = it },
                    leadingIcon = { Icon(Icons.Outlined.Search, null) },
                    placeholder = { Text(stringResource(R.string.search_layouts)) },
                    modifier = Modifier
                        .widthIn(max = contentWidth)
                        .fillMaxWidth()
                        .padding(horizontal = if (useTwoColumns) 24.dp else 16.dp, vertical = 8.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                    )
                )
                LazyRow(
                    modifier = Modifier.widthIn(max = contentWidth).fillMaxWidth(),
                    contentPadding = PaddingValues(
                        horizontal = if (useTwoColumns) 24.dp else 16.dp,
                        vertical = 4.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = category == null,
                            onClick = { category = null },
                            label = { Text(stringResource(R.string.all_layouts)) }
                        )
                    }
                    items(LayoutCategory.entries) { itemCategory ->
                        FilterChip(
                            selected = category == itemCategory,
                            onClick = { category = itemCategory },
                            label = { Text(itemCategory.localizedName()) }
                        )
                    }
                }
                if (filtered.isEmpty()) {
                    EmptyLayoutSearch(
                        modifier = Modifier
                            .widthIn(max = contentWidth)
                            .fillMaxWidth()
                            .weight(1f)
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(if (useTwoColumns) 2 else 1),
                            modifier = Modifier.widthIn(max = contentWidth).fillMaxSize(),
                            contentPadding = PaddingValues(
                                horizontal = if (useTwoColumns) 24.dp else 16.dp,
                                vertical = 12.dp
                            ),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filtered, key = { it.id }) { item ->
                                LayoutToggleRow(
                                    item = item,
                                    checked = item.id in selected,
                                    onToggle = {
                                        repository.setSelected(item.id, item.id !in selected)
                                        selected = repository.selectedIds()
                                        onLayoutChanged()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LayoutToggleRow(
    item: KeyboardLayoutItem,
    checked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onToggle,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = if (checked) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = if (checked) MaterialTheme.colorScheme.onPrimaryContainer
        else MaterialTheme.colorScheme.onSurface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TonalIcon(
                icon = Icons.Outlined.Keyboard,
                size = 42.dp,
                containerColor = if (checked) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = if (checked) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(item.labelRes),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    item.category.localizedName(),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (checked) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = checked, onCheckedChange = null)
        }
    }
}

@Composable
private fun EmptyLayoutSearch(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TonalIcon(Icons.Outlined.Search, size = 64.dp)
        Text(
            stringResource(R.string.layout_search_empty),
            modifier = Modifier.padding(top = 16.dp),
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            stringResource(R.string.layout_search_empty_supporting),
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
