package com.alpware.keymapkit.layout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyboardLayoutCatalogTest {
    @Test fun idsAndReceiversAreUnique() {
        assertEquals(KeyboardLayoutCatalog.all.size, KeyboardLayoutCatalog.all.map { it.id }.distinct().size)
        assertEquals(KeyboardLayoutCatalog.all.size, KeyboardLayoutCatalog.all.map { it.receiverClassName }.distinct().size)
    }

    @Test fun essentialTurkishLayoutsExist() {
        assertTrue(KeyboardLayoutCatalog.byId("turkish_q") != null)
        assertTrue(KeyboardLayoutCatalog.byId("turkish_f") != null)
    }
}
