package com.alpware.keymapkit.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * The system broadcasts QUERY_KEYBOARD_LAYOUTS to discover keyboard layout providers.
 * We don't need to do anything here; the system reads @xml/keyboard_layouts via meta-data.
 */
class KeyboardLayoutsQueryReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Intentionally empty.
        // Presence of this receiver + meta-data is what matters.
    }
}