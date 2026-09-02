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
            _uiState.value = BillingUiState.Error("Google Play Billing client is not ready. Using test pricing.")
            return
        }

        try {
            val products = SmartFarmBillingProducts.subscriptionProductIds.map { productId ->
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(productId)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            }
            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(products)
                .build()

            client.queryProductDetailsAsync(params) { billingResult, result ->
                if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                    Log.w(TAG, "Query product details failed: ${billingResult.debugMessage}")
                    _uiState.value = BillingUiState.Error(
                        "Subscriptions not found on Play Console (${billingResult.debugMessage.ifBlank { "Code: " + billingResult.responseCode }})."
                    )
                    return@queryProductDetailsAsync
                }

                productDetailsById.clear()
                val detailsList = result?.productDetailsList.orEmpty()
                val plans = detailsList.mapNotNull { details ->
                    val offer = details.subscriptionOfferDetails?.firstOrNull() ?: return@mapNotNull null
                    productDetailsById[details.productId] = details
                    BillingPlan(
                        productId = details.productId,
                        title = details.name,
                        description = details.description,
                        price = offer.pricingPhases.pricingPhaseList.firstOrNull()?.formattedPrice ?: "",
                        offerToken = offer.offerToken
                    )
                }

                _uiState.value = if (plans.isEmpty()) {
                    BillingUiState.Error(
                        "Google Play Console products not published yet. Test mode active."
                    )
                } else {
                    BillingUiState.Ready(plans)
                }
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Exception in queryAnnualPlans", e)
            _uiState.value = BillingUiState.Error("Failed to query Google Play products: ${e.message}")
        }
    }

    fun launchAnnualPlanPurchase(activity: Activity, plan: BillingPlan) {
        val client = billingClient
        if (client == null || !client.isReady) {
            _uiState.value = BillingUiState.Error("Google Play client is not ready.")
            return
        }

        val details = productDetailsById[plan.productId]
        if (details == null) {
            _uiState.value = BillingUiState.Error("Plan details not cached. Refresh and try again.")
            return
        }

        try {
            val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .setOfferToken(plan.offerToken)
                .build()
            val result = client.launchBillingFlow(
                activity,
                BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(listOf(productDetailsParams))
                    .build()
            )
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                _uiState.value = BillingUiState.Error("Could not open Google Play checkout: ${result.debugMessage}")
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Exception during launchBillingFlow", e)
            _uiState.value = BillingUiState.Error("Could not open checkout: ${e.message}")
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
