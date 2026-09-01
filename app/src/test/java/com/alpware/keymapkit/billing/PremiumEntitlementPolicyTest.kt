package com.alpware.keymapkit.billing

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PremiumEntitlementPolicyTest {
    @Test
    fun purchasedPremiumProductGrantsEntitlement() {
        val purchases = listOf(
            PremiumPurchaseRecord(setOf(PRODUCT_ID), PremiumPurchaseState.PURCHASED)
        )

        assertTrue(PremiumEntitlementPolicy.hasPremiumPurchase(purchases, PRODUCT_ID))
    }

    @Test
    fun pendingPremiumProductDoesNotGrantEntitlement() {
        val purchases = listOf(
            PremiumPurchaseRecord(setOf(PRODUCT_ID), PremiumPurchaseState.PENDING)
        )

        assertFalse(PremiumEntitlementPolicy.hasPremiumPurchase(purchases, PRODUCT_ID))
        assertTrue(PremiumEntitlementPolicy.hasPendingPremiumPurchase(purchases, PRODUCT_ID))
    }

    @Test
    fun unrelatedPurchaseDoesNotGrantEntitlement() {
        val purchases = listOf(
            PremiumPurchaseRecord(setOf("another_product"), PremiumPurchaseState.PURCHASED)
        )

        assertFalse(PremiumEntitlementPolicy.hasPremiumPurchase(purchases, PRODUCT_ID))
    }

    private companion object {
        const val PRODUCT_ID = "premium_lifetime"
    }
}
