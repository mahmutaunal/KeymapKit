package com.alpware.keymapkit.billing

import android.app.Activity
import android.content.Context
import androidx.core.content.edit
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams

enum class PremiumStoreStatus { CONNECTING, READY, PENDING, UNAVAILABLE }

data class PremiumState(
    val isPremium: Boolean,
    val formattedPrice: String? = null,
    val storeStatus: PremiumStoreStatus = PremiumStoreStatus.CONNECTING,
    val operationInProgress: Boolean = false,
    val isTestMode: Boolean = false,
)

/**
 * Owns the single Play Billing connection and the cached lifetime Premium entitlement.
 *
 * The cache preserves an already observed purchase while offline. It is replaced only after a
 * successful authoritative query to Google Play, allowing refunds and revocations to take effect.
 */
class PremiumBillingManager(
    context: Context,
    private val testMode: Boolean,
    private val onStateChanged: (PremiumState) -> Unit,
) : PurchasesUpdatedListener {
    private val appContext = context.applicationContext
    private val preferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val billingClient by lazy {
        BillingClient.newBuilder(appContext)
            .setListener(this)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .enableAutoServiceReconnection()
            .build()
    }

    private var productDetails: ProductDetails? = null
    private var selectedOffer: ProductDetails.OneTimePurchaseOfferDetails? = null
    private var started = false

    var state: PremiumState = PremiumState(
        isPremium = testMode || preferences.getBoolean(KEY_PREMIUM, false),
        storeStatus = if (testMode) PremiumStoreStatus.READY else PremiumStoreStatus.CONNECTING,
        isTestMode = testMode,
    )
        private set

    fun start() {
        if (testMode || started) return
        started = true
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProductDetails()
                    queryPurchases(authoritative = true)
                } else {
                    updateState(
                        operationInProgress = false,
                        storeStatus = PremiumStoreStatus.UNAVAILABLE,
                    )
                }
            }

            override fun onBillingServiceDisconnected() {
                updateState(
                    operationInProgress = false,
                    storeStatus = PremiumStoreStatus.CONNECTING,
                )
            }
        })
    }

    fun refreshPurchases() {
        if (testMode) return
        if (!started) {
            start()
            return
        }
        if (billingClient.connectionState != BillingClient.ConnectionState.CONNECTING) {
            queryPurchases(authoritative = true)
        }
    }

    fun restorePurchases() {
        if (testMode) return
        updateState(operationInProgress = true)
        if (!started) {
            start()
            return
        }
        if (billingClient.connectionState != BillingClient.ConnectionState.CONNECTING) {
            queryPurchases(authoritative = true)
        }
    }

    fun launchPurchase(activity: Activity) {
        if (testMode || state.isPremium || state.operationInProgress) return
        val details = productDetails
        val offer = selectedOffer
        val offerToken = offer?.offerToken
        if (!billingClient.isReady || details == null || offerToken.isNullOrBlank()) {
            updateState(storeStatus = PremiumStoreStatus.UNAVAILABLE)
            if (started) queryProductDetails()
            return
        }

        updateState(operationInProgress = true)
        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .setOfferToken(offerToken)
            .build()
        val result = billingClient.launchBillingFlow(
            activity,
            BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productParams))
                .build(),
        )
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            updateState(
                operationInProgress = false,
                storeStatus = PremiumStoreStatus.UNAVAILABLE,
            )
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                processPurchases(purchases.orEmpty(), authoritative = false)
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                updateState(operationInProgress = false)
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                queryPurchases(authoritative = true)
            }
            else -> updateState(
                operationInProgress = false,
                storeStatus = PremiumStoreStatus.UNAVAILABLE,
            )
        }
    }

    fun close() {
        if (started) billingClient.endConnection()
        started = false
    }

    private fun queryProductDetails() {
        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(PRODUCT_ID)
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        billingClient.queryProductDetailsAsync(
            QueryProductDetailsParams.newBuilder()
                .setProductList(listOf(product))
                .build()
        ) { result, queryResult ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                updateState(storeStatus = PremiumStoreStatus.UNAVAILABLE)
                return@queryProductDetailsAsync
            }

            val details = queryResult.productDetailsList.firstOrNull {
                it.productId == PRODUCT_ID
            }
            val offer = details?.oneTimePurchaseOfferDetailsList
                ?.filter { it.rentalDetails == null && it.preorderDetails == null }
                ?.minByOrNull { it.priceAmountMicros }
                ?: details?.oneTimePurchaseOfferDetails
            productDetails = details
            selectedOffer = offer
            updateState(
                formattedPrice = offer?.formattedPrice,
                storeStatus = if (details != null && !offer?.offerToken.isNullOrBlank()) {
                    PremiumStoreStatus.READY
                } else {
                    PremiumStoreStatus.UNAVAILABLE
                },
            )
        }
    }

    private fun queryPurchases(authoritative: Boolean) {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                processPurchases(purchases, authoritative)
            } else {
                updateState(
                    operationInProgress = false,
                    storeStatus = PremiumStoreStatus.UNAVAILABLE,
                )
            }
        }
    }

    private fun processPurchases(purchases: List<Purchase>, authoritative: Boolean) {
        val records = purchases.map { purchase ->
            PremiumPurchaseRecord(
                productIds = purchase.products.toSet(),
                state = when (purchase.purchaseState) {
                    Purchase.PurchaseState.PURCHASED -> PremiumPurchaseState.PURCHASED
                    Purchase.PurchaseState.PENDING -> PremiumPurchaseState.PENDING
                    else -> PremiumPurchaseState.OTHER
                },
            )
        }
        val hasPremium = PremiumEntitlementPolicy.hasPremiumPurchase(records, PRODUCT_ID)
        val isPending = PremiumEntitlementPolicy.hasPendingPremiumPurchase(records, PRODUCT_ID)

        if (hasPremium || authoritative) {
            preferences.edit { putBoolean(KEY_PREMIUM, hasPremium) }
        }
        val effectivePremium = hasPremium || (!authoritative && state.isPremium)
        updateState(
            isPremium = effectivePremium,
            operationInProgress = false,
            storeStatus = when {
                effectivePremium -> PremiumStoreStatus.READY
                isPending -> PremiumStoreStatus.PENDING
                productDetails != null -> PremiumStoreStatus.READY
                else -> state.storeStatus
            },
        )

        purchases.asSequence()
            .filter { PRODUCT_ID in it.products }
            .filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
            .filterNot { it.isAcknowledged }
            .forEach(::acknowledge)
    }

    private fun acknowledge(purchase: Purchase) {
        billingClient.acknowledgePurchase(
            AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
        ) { result ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                // A later foreground refresh retries acknowledgement within Play's three-day window.
                updateState(storeStatus = PremiumStoreStatus.UNAVAILABLE)
            }
        }
    }

    private fun updateState(
        isPremium: Boolean = state.isPremium,
        formattedPrice: String? = state.formattedPrice,
        storeStatus: PremiumStoreStatus = state.storeStatus,
        operationInProgress: Boolean = state.operationInProgress,
    ) {
        state = state.copy(
            isPremium = isPremium,
            formattedPrice = formattedPrice,
            storeStatus = storeStatus,
            operationInProgress = operationInProgress,
        )
        onStateChanged(state)
    }

    companion object {
        const val PRODUCT_ID = "premium_lifetime"
        private const val PREFS_NAME = "premium_entitlement"
        private const val KEY_PREMIUM = "is_premium"
    }
}
