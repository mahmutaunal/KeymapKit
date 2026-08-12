package com.alpware.keymapkit.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.alpware.keymapkit.R
import com.alpware.keymapkit.layout.LayoutCategory

internal val KeymapContentMaxWidth = 720.dp
internal val KeymapWideContentMaxWidth = 1200.dp
internal val KeymapTwoPaneBreakpoint = 600.dp

@Composable
internal fun TonalIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    size: Dp = 48.dp,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(size * 0.48f)
            )
        }
    }
}

@Composable
internal fun SectionHeading(
    title: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null
) {
    Column(modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        if (supportingText != null) {
            Spacer(Modifier.size(4.dp))
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

internal fun Modifier.keymapContentWidth(): Modifier =
    this.widthIn(max = KeymapContentMaxWidth)

@Composable
internal fun LayoutCategory.localizedName(): String = when (this) {
    LayoutCategory.LATIN -> stringResource(R.string.layout_category_latin)
    LayoutCategory.CYRILLIC -> stringResource(R.string.layout_category_cyrillic)
    LayoutCategory.ARABIC -> stringResource(R.string.layout_category_arabic)
    LayoutCategory.INDIC -> stringResource(R.string.layout_category_indic)
    LayoutCategory.ASIAN -> stringResource(R.string.layout_category_asian)
    LayoutCategory.SEMITIC -> stringResource(R.string.layout_category_semitic)
    LayoutCategory.AFRICAN -> stringResource(R.string.layout_category_african)
}
