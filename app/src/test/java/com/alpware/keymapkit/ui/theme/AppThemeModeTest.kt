package com.alpware.keymapkit.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class AppThemeModeTest {
    @Test
    fun storedValuesAreRestored() {
        assertEquals(AppThemeMode.LIGHT, AppThemeMode.fromStorageValue("light"))
        assertEquals(AppThemeMode.DARK, AppThemeMode.fromStorageValue("dark"))
    }

    @Test
    fun missingOrUnknownValuesUseSystemTheme() {
        assertEquals(AppThemeMode.SYSTEM, AppThemeMode.fromStorageValue(null))
        assertEquals(AppThemeMode.SYSTEM, AppThemeMode.fromStorageValue("unknown"))
    }
}
