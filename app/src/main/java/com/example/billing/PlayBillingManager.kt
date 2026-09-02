package com.example.billing

import android.app.Activity
import android.content.Context
import android.util.Log
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG = "PlayBillingManager"

/**
 * Google Play Billing client for Smart Farm App's prepaid annual plans.
 * Resilient against offline states, missing Google Play Services, and unpublished test environments.
 */
class PlayBillingManager(
    context: Context,
    private val onPurchaseForVerification: (purchaseToken: String, productIds: List<String>) -> Unit
) : PurchasesUpdatedListener {

    private val productDetailsById = mutableMapOf<String, ProductDetails>()
    private val purchasesByToken = mutableMapOf<String, Purchase>()

    private val _uiState = MutableStateFlow<BillingUiState>(BillingUiState.Loading)
    val uiState: StateFlow<BillingUiState> = _uiState.asStateFlow()

    private var billingClient: BillingClient? = null

    init {
        try {
            billingClient = BillingClient.newBuilder(context.applicationContext)
                .setListener(this)
                .enablePendingPurchases(
                    PendingPurchasesParams.newBuilder()
                        .enableOneTimeProducts()
                        .enablePrepaidPlans()
                        .build()
                )
                .enableAutoServiceReconnection()
                .build()
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to initialize BillingClient", e)
            _uiState.value = BillingUiState.Error("Google Play services are unavailable on this device.")
        }
    }

    fun connect() {
        val client = billingClient ?: return
        _uiState.value = BillingUiState.Loading
        try {
            client.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        queryAnnualPlans()
                    } else {
                        Log.w(TAG, "Billing setup finished with code: ${billingResult.responseCode}, debugMessage: ${billingResult.debugMessage}")
                        _uiState.value = BillingUiState.Error(
                            "Google Play Billing is not active (${billingResult.debugMessage.ifBlank { "Response code: " + billingResult.responseCode }}). Using local test mode."
                        )
                    }
                }

                override fun onBillingServiceDisconnected() {
                    Log.d(TAG, "Billing service disconnected.")
                }
            })
        } catch (e: Throwable) {
            Log.e(TAG, "Exception during startConnection", e)
            _uiState.value = BillingUiState.Error("Unable to connect to Google Play Store.")
        }
    }

    fun queryAnnualPlans() {
        val client = billingClient
        if (client == null || !client.isReady) {
            _uiState.value = BillingUiState.Error("Google Play Billing client is not ready. Using test pricing.", "Billing client is not connected.")
            return
        }

        try {
            val subProducts = SmartFarmBillingProducts.subscriptionProductIds.map { productId ->
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(productId)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            }
            val subParams = QueryProductDetailsParams.newBuilder()
                .setProductList(subProducts)
                .build()

            client.queryProductDetailsAsync(subParams) { billingResult, subResult ->
                val allPlans = mutableListOf<BillingPlan>()
                productDetailsById.clear()

                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val detailsList = subResult?.productDetailsList.orEmpty()
                    for (details in detailsList) {
                        val offer = details.subscriptionOfferDetails?.firstOrNull()
                        val offerToken = offer?.offerToken ?: ""
                        val formattedPrice = offer?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
                            ?: details.oneTimePurchaseOfferDetails?.formattedPrice
                            ?: ""
                        productDetailsById[details.productId] = details
                        allPlans.add(
                            BillingPlan(
                                productId = details.productId,
                                title = details.name,
                                description = details.description,
                                price = formattedPrice,
                                offerToken = offerToken,
                                productType = "subs"
                            )
                        )
                    }
                }

                // Also query one-time/in-app products in case configured as INAPP on Play Console
                val inAppProducts = SmartFarmBillingProducts.subscriptionProductIds.map { productId ->
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                }
                val inAppParams = QueryProductDetailsParams.newBuilder()
                    .setProductList(inAppProducts)
                    .build()

                client.queryProductDetailsAsync(inAppParams) { inAppResult, inAppDetailsList ->
                    if (inAppResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        for (details in inAppDetailsList?.productDetailsList.orEmpty()) {
                            if (!productDetailsById.containsKey(details.productId)) {
                                val price = details.oneTimePurchaseOfferDetails?.formattedPrice.orEmpty()
                                productDetailsById[details.productId] = details
                                allPlans.add(
                                    BillingPlan(
                                        productId = details.productId,
                                        title = details.name,
                                        description = details.description,
                                        price = price,
                                        offerToken = "",
                                        productType = "inapp"
                                    )
                                )
                            }
                        }
                    }

                    if (allPlans.isNotEmpty()) {
                        _uiState.value = BillingUiState.Ready(allPlans)
                    } else {
                        val debugMsg = if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                            "Play Store response code: ${billingResult.responseCode} (${billingResult.debugMessage})"
                        } else {
                            "Products verified with Google Play, but no active subscription base plans were returned. Ensure the subscription base plan is set to 'Active' on Google Play Console."
                        }
                        _uiState.value = BillingUiState.Error(
                            "Google Play products not active yet.",
                            debugMsg
                        )
                    }
                }
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Exception in queryAnnualPlans", e)
            _uiState.value = BillingUiState.Error("Failed to query Google Play products: ${e.message}", e.localizedMessage ?: "")
        }
    }

    fun launchAnnualPlanPurchase(activity: Activity, plan: BillingPlan) {
        val client = billingClient
        if (client == null || !client.isReady) {
            _uiState.value = BillingUiState.Error("Google Play client is not connected.", "BillingClient.isReady is false.")
            return
        }

        val details = productDetailsById[plan.productId]
        if (details == null) {
            _uiState.value = BillingUiState.Error("Plan details not cached for '${plan.productId}'. Refresh and try again.")
            return
        }

        try {
            val productDetailsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
            if (plan.offerToken.isNotBlank()) {
                productDetailsBuilder.setOfferToken(plan.offerToken)
            }
            val productDetailsParams = productDetailsBuilder.build()

            val result = client.launchBillingFlow(
                activity,
                BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(listOf(productDetailsParams))
                    .build()
            )
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                _uiState.value = BillingUiState.Error(
                    "Could not open Google Play checkout (${result.debugMessage.ifBlank { "Code " + result.responseCode }}).",
                    result.debugMessage
                )
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Exception during launchBillingFlow", e)
            _uiState.value = BillingUiState.Error("Could not open checkout: ${e.message}", e.localizedMessage ?: "")
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        try {
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> purchases.orEmpty().forEach(::sendForServerVerification)
                BillingClient.BillingResponseCode.USER_CANCELED -> Unit
                else -> _uiState.value = BillingUiState.Error(
                    "Purchase was not completed: ${billingResult.debugMessage}"
                )
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Exception in onPurchasesUpdated", e)
        }
    }

    private fun sendForServerVerification(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return
        purchasesByToken[purchase.purchaseToken] = purchase
        onPurchaseForVerification(purchase.purchaseToken, purchase.products)
    }

    /** Call only after the secure server verifies the token and saves entitlement. */
    fun acknowledgeVerifiedPurchase(purchaseToken: String, onComplete: (Boolean) -> Unit = {}) {
        val client = billingClient
        val purchase = purchasesByToken[purchaseToken] ?: return onComplete(false)
        if (purchase.isAcknowledged) return onComplete(true)
        if (client == null || !client.isReady) return onComplete(false)

        try {
            client.acknowledgePurchase(
                AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchaseToken)
                    .build()
            ) { result ->
                onComplete(result.responseCode == BillingClient.BillingResponseCode.OK)
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Exception acknowledging purchase", e)
            onComplete(false)
        }
    }

    fun close() {
        try {
            val client = billingClient
            if (client != null && client.isReady) {
                client.endConnection()
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Exception while closing BillingClient", e)
        }
    }
}
