package com.alpware.keymapkit.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.alpware.keymapkit.R
import com.alpware.keymapkit.layout.KeyboardLayoutCatalog
import com.alpware.keymapkit.layout.KeyboardLayoutItem
import com.alpware.keymapkit.layout.LayoutSelectionRepository

@Composable
fun OnboardingScreen(repository: LayoutSelectionRepository, onDone: () -> Unit) {
    val recommended = remember { repository.recommendedIds() }
    var selected by remember { mutableStateOf(recommended) }
    val items = remember {
        KeyboardLayoutCatalog.all
            .filter { it.id in recommended || it.popular }
            .distinctBy { it.id }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val useTwoColumns = maxWidth >= KeymapTwoPaneBreakpoint
        val contentWidth = if (useTwoColumns) KeymapWideContentMaxWidth else KeymapContentMaxWidth
        Scaffold(
            containerColor = MaterialTheme.colorScheme.surface,
            bottomBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 3.dp
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(
                                horizontal = if (useTwoColumns) 24.dp else 16.dp,
                                vertical = 12.dp
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        val actionsModifier = Modifier.widthIn(max = contentWidth).fillMaxWidth()
                        if (useTwoColumns) Row(
                            modifier = actionsModifier,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    repository.applySelection(
                                        recommended.ifEmpty { setOf(KeyboardLayoutCatalog.all.first().id) }
                                    )
                                    onDone()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(stringResource(R.string.use_recommended))
                            }
                            Button(
                                onClick = {
                                    repository.applySelection(selected)
                                    onDone()
                                },
                                enabled = selected.isNotEmpty(),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 14.dp)
                            ) {
                                Text(stringResource(R.string.continue_with_count, selected.size))
                            }
                        } else Column(
                            modifier = actionsModifier,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Button(
                                onClick = {
                                    repository.applySelection(selected)
                                    onDone()
                                },
                                enabled = selected.isNotEmpty(),
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = 14.dp)
                            ) {
                                Text(stringResource(R.string.continue_with_count, selected.size))
                            }
                            TextButton(
                                onClick = {
                                    repository.applySelection(
                                        recommended.ifEmpty { setOf(KeyboardLayoutCatalog.all.first().id) }
                                    )
                                    onDone()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.use_recommended))
                            }
                        }
                    }
                }
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(if (useTwoColumns) 2 else 1),
                    modifier = Modifier.widthIn(max = contentWidth).fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = if (useTwoColumns) 24.dp else 16.dp,
                        end = if (useTwoColumns) 24.dp else 16.dp,
                        top = if (useTwoColumns) 16.dp else 24.dp,
                        bottom = 24.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        OnboardingHero(Modifier.fillMaxWidth())
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    stringResource(R.string.recommended_layouts),
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text(
                                    stringResource(R.string.onboarding_selection_hint),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    selected.size.toString(),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                    items(items, key = { it.id }) { item ->
                        LayoutSelectionCard(
                            modifier = Modifier.fillMaxWidth(),
                            item = item,
                            selected = item.id in selected,
                            onToggle = {
                                selected = selected.toMutableSet().apply {
                                    if (item.id in this) remove(item.id) else add(item.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingHero(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Image(
                painter = painterResource(R.mipmap.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.padding(14.dp).size(84.dp).clip(MaterialTheme.shapes.large)
            )
        }
        Text(
            stringResource(R.string.onboarding_heading),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            stringResource(R.string.onboarding_body),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LayoutSelectionCard(
    item: KeyboardLayoutItem,
    selected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onToggle,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
        else MaterialTheme.colorScheme.onSurface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(item.labelRes),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.size(3.dp))
                Text(
                    item.category.localizedName(),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f)
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Checkbox(checked = selected, onCheckedChange = null)
        }
    }
}
