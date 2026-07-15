package com.alpware.keymapkit.layout

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.alpware.keymapkit.receiver.KeyboardLayoutsQueryReceiver
import java.util.Locale
import androidx.core.content.edit

class LayoutSelectionRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences("layout_selection", Context.MODE_PRIVATE)
    private val pm = context.packageManager

    val isConfigured: Boolean get() = prefs.getBoolean(KEY_CONFIGURED, false)

    fun selectedIds(): Set<String> = prefs.getStringSet(KEY_SELECTED, emptySet())?.toSet().orEmpty()

    fun recommendedIds(): Set<String> {
        val language = if (Build.VERSION.SDK_INT >= 24) {
            context.resources.configuration.locales[0]?.language
        } else {
            @Suppress("DEPRECATION") context.resources.configuration.locale?.language
        } ?: Locale.getDefault().language
        return when (language.lowercase(Locale.ROOT)) {
            "tr" -> setOf("turkish_q", "turkish_f")
            "de" -> setOf("german_ibm")
            "ru" -> setOf("russian_phonetic_linux", "russian_typewriter")
            "uk" -> setOf("ukrainian_enhanced")
            "ar" -> setOf("arabic_102_azerty")
            "fa" -> setOf("persian_standard")
            "ko" -> setOf("korean")
            "th" -> setOf("thai_kedmanee")
            "hi" -> setOf("hindi_traditional", "devanagari_inscript")
            else -> setOf("united_kingdom_extended")
        }.filterTo(mutableSetOf()) { KeyboardLayoutCatalog.byId(it) != null }
    }

    fun prepareFreshInstall() {
        if (isConfigured) return
        val info = pm.getPackageInfo(context.packageName, 0)
        val freshInstall = info.firstInstallTime == info.lastUpdateTime
        if (freshInstall) setLegacyProviderEnabled(false)
    }

    fun applySelection(ids: Set<String>) {
        val valid = ids.filterTo(linkedSetOf()) { KeyboardLayoutCatalog.byId(it) != null }
        KeyboardLayoutCatalog.all.forEach { item ->
            setReceiverEnabled(item.receiverClassName, item.id in valid)
        }
        setLegacyProviderEnabled(false)
        prefs.edit { putStringSet(KEY_SELECTED, valid).putBoolean(KEY_CONFIGURED, true) }
    }

    fun setSelected(id: String, selected: Boolean) {
        val item = KeyboardLayoutCatalog.byId(id) ?: return
        val next = selectedIds().toMutableSet().apply { if (selected) add(id) else remove(id) }
        setReceiverEnabled(item.receiverClassName, selected)
        setLegacyProviderEnabled(false)
        prefs.edit { putStringSet(KEY_SELECTED, next).putBoolean(KEY_CONFIGURED, true) }
    }

    private fun setReceiverEnabled(className: String, enabled: Boolean) {
        pm.setComponentEnabledSetting(
            ComponentName(context.packageName, className),
            if (enabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    private fun setLegacyProviderEnabled(enabled: Boolean) {
        pm.setComponentEnabledSetting(
            ComponentName(context, KeyboardLayoutsQueryReceiver::class.java),
            if (enabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    private companion object {
        const val KEY_CONFIGURED = "configured"
        const val KEY_SELECTED = "selected_layout_ids"
    }
}
