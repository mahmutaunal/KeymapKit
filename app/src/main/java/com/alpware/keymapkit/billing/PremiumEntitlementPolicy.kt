package com.alpware.keymapkit.billing

internal enum class PremiumPurchaseState { PURCHASED, PENDING, OTHER }

internal data class PremiumPurchaseRecord(
    val productIds: Set<String>,
    val state: PremiumPurchaseState,
)

internal object PremiumEntitlementPolicy {
    fun hasPremiumPurchase(
        purchases: List<PremiumPurchaseRecord>,
        premiumProductId: String,
    ): Boolean = purchases.any { purchase ->
        premiumProductId in purchase.productIds &&
            purchase.state == PremiumPurchaseState.PURCHASED
    }

    fun hasPendingPremiumPurchase(
        purchases: List<PremiumPurchaseRecord>,
        premiumProductId: String,
    ): Boolean = purchases.any { purchase ->
        premiumProductId in purchase.productIds &&
            purchase.state == PremiumPurchaseState.PENDING
    }
}
