@file:Suppress("SameParameterValue")

package com.alpware.keymapkit.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.alpware.keymapkit.BuildConfig
import com.alpware.keymapkit.R
import com.alpware.keymapkit.ui.theme.AppThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenStoreReview: () -> Unit,
    onCheckForUpdate: () -> Unit,
    modifier: Modifier = Modifier,
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    onThemeModeChange: (AppThemeMode) -> Unit = {},
    languageTag: String? = null,
    onLanguageChange: (String?) -> Unit = {},
    showPrivacyOptions: Boolean = false,
    onPrivacyOptions: () -> Unit = {},
    appName: String? = null,
    appTagline: String? = null,
    developerName: String? = null,
    developerEmail: String? = null,
    websiteUrl: String? = null,
    githubRepoUrl: String = "https://github.com/mahmutaunal/KeymapKit",
    githubIssuesUrl: String = "https://github.com/mahmutaunal/KeymapKit/issues",
    playStoreAppUrl: String = "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}",
    playStoreDeveloperUrl: String = "https://play.google.com/store/apps/dev?id=5245599652065968716",
) {
    val context = LocalContext.current
    val appNameText = appName ?: stringResource(R.string.settings_app_name)
    val appTaglineText = appTagline ?: stringResource(R.string.settings_app_tagline)
    val developerNameText = developerName ?: stringResource(R.string.settings_developer_name)
    val developerEmailText = developerEmail ?: stringResource(R.string.settings_developer_email)
    val websiteUrlText = websiteUrl ?: stringResource(R.string.settings_website_url)
    val emailSubject = stringResource(R.string.settings_email_subject, appNameText)
    val shareTitle = stringResource(R.string.settings_share_title, appNameText)
    val shareBody = stringResource(R.string.settings_share_text, appNameText, playStoreAppUrl)

    var legalDialog by remember { mutableStateOf<LegalDialog?>(null) }
    var preferenceDialog by remember { mutableStateOf<PreferenceDialog?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title), fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.cd_back))
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
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(if (useTwoColumns) 2 else 1),
                    modifier = Modifier.widthIn(max = contentWidth).fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = if (useTwoColumns) 24.dp else 16.dp,
                        end = if (useTwoColumns) 24.dp else 16.dp,
                        top = 8.dp,
                        bottom = 32.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                SettingsHero(
                    modifier = Modifier.fillMaxWidth(),
                    appName = appNameText,
                    tagline = appTaglineText
                )
            }
            item {
                SettingsSection(
                    title = stringResource(R.string.settings_section_appearance),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SettingsRow(
                        leading = Icons.Outlined.Palette,
                        title = stringResource(R.string.settings_item_theme),
                        subtitle = themeMode.label(),
                        onClick = { preferenceDialog = PreferenceDialog.Theme }
                    )
                    SettingsDivider()
                    SettingsRow(
                        leading = Icons.Outlined.Language,
                        title = stringResource(R.string.settings_item_language),
                        subtitle = languageLabel(languageTag),
                        onClick = { preferenceDialog = PreferenceDialog.Language }
                    )
                }
            }
            item {
                SettingsSection(
                    title = stringResource(R.string.settings_section_support),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SettingsRow(
                        Icons.Outlined.SystemUpdate,
                        stringResource(R.string.settings_item_check_updates),
                        stringResource(R.string.settings_value_check_updates),
                        onClick = onCheckForUpdate
                    )
                    SettingsDivider()
                    SettingsRow(
                        Icons.Outlined.RateReview,
                        stringResource(R.string.settings_item_rate_play_store),
                        stringResource(R.string.settings_value_leave_review),
                        trailing = Icons.AutoMirrored.Outlined.OpenInNew,
                        onClick = onOpenStoreReview
                    )
                    SettingsDivider()
                    SettingsRow(
                        Icons.Outlined.Share,
                        stringResource(R.string.settings_item_share_app),
                        stringResource(R.string.settings_value_send_play_store_link),
                        onClick = { shareText(context, shareTitle, shareBody) }
                    )
                    SettingsDivider()
                    SettingsRow(
                        Icons.Outlined.BugReport,
                        stringResource(R.string.settings_item_report_issue),
                        stringResource(R.string.settings_value_github_issues),
                        trailing = Icons.AutoMirrored.Outlined.OpenInNew,
                        onClick = { openUrl(context, githubIssuesUrl) }
                    )
                }
            }
            item {
                SettingsSection(
                    title = stringResource(R.string.settings_section_about),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SettingsRow(
                        Icons.Outlined.Info,
                        stringResource(R.string.settings_item_version),
                        BuildConfig.VERSION_NAME,
                        onClick = null
                    )
                    SettingsDivider()
                    SettingsRow(
                        Icons.Outlined.Keyboard,
                        stringResource(R.string.settings_item_repository),
                        githubRepoUrl,
                        subtitleMaxLines = 1,
                        trailing = Icons.AutoMirrored.Outlined.OpenInNew,
                        onClick = { openUrl(context, githubRepoUrl) }
                    )
                }
            }
            item {
                SettingsSection(
                    title = stringResource(R.string.settings_section_developer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SettingsRow(
                        Icons.Outlined.Storefront,
                        developerNameText,
                        stringResource(R.string.settings_value_other_apps),
                        trailing = Icons.AutoMirrored.Outlined.OpenInNew,
                        onClick = { openUrl(context, playStoreDeveloperUrl) }
                    )
                    SettingsDivider()
                    SettingsRow(
                        Icons.Outlined.Email,
                        stringResource(R.string.settings_item_email),
                        developerEmailText,
                        trailing = Icons.AutoMirrored.Outlined.OpenInNew,
                        onClick = { composeEmail(context, developerEmailText, emailSubject) }
                    )
                    SettingsDivider()
                    SettingsRow(
                        Icons.Outlined.Language,
                        stringResource(R.string.settings_item_website),
                        websiteUrlText,
                        trailing = Icons.AutoMirrored.Outlined.OpenInNew,
                        onClick = { openUrl(context, websiteUrlText) }
                    )
                }
            }
            item {
                SettingsSection(
                    title = stringResource(R.string.settings_section_legal),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SettingsRow(
                        Icons.Outlined.PrivacyTip,
                        stringResource(R.string.settings_item_privacy_policy),
                        stringResource(R.string.settings_value_privacy_short),
                        onClick = { legalDialog = LegalDialog.PrivacyPolicy }
                    )
                    if (showPrivacyOptions) {
                        SettingsDivider()
                        SettingsRow(
                            Icons.Outlined.PrivacyTip,
                            stringResource(R.string.settings_item_ad_privacy),
                            stringResource(R.string.settings_value_ad_privacy),
                            onClick = onPrivacyOptions
                        )
                    }
                    SettingsDivider()
                    SettingsRow(
                        Icons.Outlined.Description,
                        stringResource(R.string.settings_item_open_source_notices),
                        stringResource(R.string.settings_value_third_party_licenses),
                        onClick = { legalDialog = LegalDialog.OpenSourceNotices }
                    )
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    stringResource(R.string.settings_footer_tip),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
                }
            }
        }
    }

    legalDialog?.let { dialog ->
        AlertDialog(
            onDismissRequest = { legalDialog = null },
            title = { Text(stringResource(dialog.titleRes)) },
            text = { Text(stringResource(dialog.bodyRes), style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = { legalDialog = null }) { Text(stringResource(R.string.action_close)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    legalDialog = null
                    openUrl(context, githubRepoUrl)
                }) { Text(stringResource(R.string.settings_action_view_on_github)) }
            }
        )
    }

    when (preferenceDialog) {
        PreferenceDialog.Theme -> PreferenceSelectionDialog(
            title = stringResource(R.string.settings_dialog_theme_title),
            options = listOf(
                PreferenceOption(AppThemeMode.SYSTEM.storageValue, stringResource(R.string.settings_theme_system)),
                PreferenceOption(AppThemeMode.LIGHT.storageValue, stringResource(R.string.settings_theme_light)),
                PreferenceOption(AppThemeMode.DARK.storageValue, stringResource(R.string.settings_theme_dark))
            ),
            selectedKey = themeMode.storageValue,
            onDismiss = { preferenceDialog = null },
            onSelect = {
                onThemeModeChange(AppThemeMode.fromStorageValue(it))
                preferenceDialog = null
            }
        )
        PreferenceDialog.Language -> PreferenceSelectionDialog(
            title = stringResource(R.string.settings_dialog_language_title),
            options = listOf(
                PreferenceOption(LANGUAGE_SYSTEM, stringResource(R.string.settings_language_system)),
                PreferenceOption(LANGUAGE_TURKISH, stringResource(R.string.settings_language_turkish)),
                PreferenceOption(LANGUAGE_ENGLISH, stringResource(R.string.settings_language_english))
            ),
            selectedKey = languageTag?.substringBefore('-') ?: LANGUAGE_SYSTEM,
            onDismiss = { preferenceDialog = null },
            onSelect = {
                onLanguageChange(it.takeUnless { key -> key == LANGUAGE_SYSTEM })
                preferenceDialog = null
            }
        )
        null -> Unit
    }
}

@Composable
private fun SettingsHero(
    appName: String,
    tagline: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TonalIcon(
                Icons.Outlined.Keyboard,
                size = 56.dp,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
            Column(Modifier.weight(1f)) {
                Text(appName, style = MaterialTheme.typography.titleLarge)
                Text(
                    tagline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title,
            modifier = Modifier.padding(horizontal = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) { Column(content = content) }
    }
}

@Composable
private fun SettingsRow(
    leading: ImageVector,
    title: String,
    subtitle: String? = null,
    subtitleMaxLines: Int = 2,
    trailing: ImageVector? = null,
    onClick: (() -> Unit)?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TonalIcon(leading, size = 42.dp)
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = subtitleMaxLines,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        val trailingIcon = trailing ?: if (onClick != null) Icons.AutoMirrored.Outlined.KeyboardArrowRight else null
        if (trailingIcon != null) {
            Icon(
                trailingIcon,
                null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 70.dp, end = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    )
}

@Composable
private fun AppThemeMode.label(): String = when (this) {
    AppThemeMode.SYSTEM -> stringResource(R.string.settings_theme_system)
    AppThemeMode.LIGHT -> stringResource(R.string.settings_theme_light)
    AppThemeMode.DARK -> stringResource(R.string.settings_theme_dark)
}

@Composable
private fun languageLabel(languageTag: String?): String = when (languageTag?.substringBefore('-')) {
    LANGUAGE_TURKISH -> stringResource(R.string.settings_language_turkish)
    LANGUAGE_ENGLISH -> stringResource(R.string.settings_language_english)
    else -> stringResource(R.string.settings_language_system)
}

@Composable
private fun PreferenceSelectionDialog(
    title: String,
    options: List<PreferenceOption>,
    selectedKey: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                options.forEach { option ->
                    Surface(
                        onClick = { onSelect(option.key) },
                        shape = MaterialTheme.shapes.medium,
                        color = if (option.key == selectedKey) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(option.label, Modifier.weight(1f))
                            RadioButton(selected = option.key == selectedKey, onClick = null)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}

private data class PreferenceOption(val key: String, val label: String)
private enum class PreferenceDialog { Theme, Language }
private const val LANGUAGE_SYSTEM = "system"
private const val LANGUAGE_TURKISH = "tr"
private const val LANGUAGE_ENGLISH = "en"

@Immutable
private sealed class LegalDialog(val titleRes: Int, val bodyRes: Int) {
    data object PrivacyPolicy : LegalDialog(R.string.settings_dialog_privacy_title, R.string.settings_dialog_privacy_body)
    data object OpenSourceNotices : LegalDialog(R.string.settings_dialog_notices_title, R.string.settings_dialog_notices_body)
}

private fun openUrl(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        // The device has no application capable of opening this URL.
    }
}

private fun composeEmail(context: Context, email: String, subject: String) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = "mailto:$email".toUri()
        putExtra(Intent.EXTRA_SUBJECT, subject)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        // The device has no email application.
    }
}

private fun shareText(context: Context, title: String, text: String) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    val chooser = Intent.createChooser(sendIntent, title).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
    try {
        context.startActivity(chooser)
    } catch (_: ActivityNotFoundException) {
        // The device has no share target.
    }
}
