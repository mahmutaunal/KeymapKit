package com.alpware.keymapkit.ui.theme

import android.content.Context
import androidx.core.content.edit

/** The user-visible theme choices supported by KeymapKit. */
enum class AppThemeMode(val storageValue: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    companion object {
        fun fromStorageValue(value: String?): AppThemeMode =
            entries.firstOrNull { it.storageValue == value } ?: SYSTEM
    }
}

/** Persists appearance settings independently from Compose and Activity lifecycles. */
class AppearancePreferences(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    var themeMode: AppThemeMode
        get() = AppThemeMode.fromStorageValue(preferences.getString(KEY_THEME_MODE, null))
        set(value) {
            preferences.edit { putString(KEY_THEME_MODE, value.storageValue) }
        }

    private companion object {
        const val PREFERENCES_NAME = "appearance_preferences"
        const val KEY_THEME_MODE = "theme_mode"
    }
}
