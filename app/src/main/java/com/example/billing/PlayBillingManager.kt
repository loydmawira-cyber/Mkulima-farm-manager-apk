package com.example.billing

import android.app.Activity
import android.content.Context
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

/**
 * Google Play Billing client for Smart Farm App's prepaid annual plans.
 *
 * A successful Play checkout does NOT directly unlock the farm. Every purchase
 * token is handed to [onPurchaseForVerification] so the server can verify it
 * with Google Play and write the authoritative farm subscription document.
 */
class PlayBillingManager(
    context: Context,
    private val onPurchaseForVerification: (purchaseToken: String, productIds: List<String>) -> Unit
) : PurchasesUpdatedListener {

    private val productDetailsById = mutableMapOf<String, ProductDetails>()
    private val purchasesByToken = mutableMapOf<String, Purchase>()

    private val _uiState = MutableStateFlow<BillingUiState>(BillingUiState.Loading)
    val uiState: StateFlow<BillingUiState> = _uiState.asStateFlow()

    private val billingClient: BillingClient = BillingClient.newBuilder(context.applicationContext)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enablePrepaidPlans()
                .build()
        )
        .enableAutoServiceReconnection()
        .build()

    fun connect() {
        _uiState.value = BillingUiState.Loading
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryAnnualPlans()
                } else {
                    _uiState.value = BillingUiState.Error(
                        "Google Play Billing is unavailable: ${billingResult.debugMessage}"
                    )
                }
            }

            override fun onBillingServiceDisconnected() {
                // Billing Library automatic reconnection handles the next request.
            }
        })
    }

    fun queryAnnualPlans() {
        val products = SmartFarmBillingProducts.subscriptionProductIds.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(products)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, result ->
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                _uiState.value = BillingUiState.Error(
                    "Unable to load subscription plans: ${billingResult.debugMessage}"
                )
                return@queryProductDetailsAsync
            }

            productDetailsById.clear()
            val plans = result.productDetailsList.mapNotNull { details ->
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
                    "Annual plans are not available yet. Confirm the Play Console products are active and this account is in the test track."
                )
            } else {
                BillingUiState.Ready(plans)
            }
        }
    }

    fun launchAnnualPlanPurchase(activity: Activity, plan: BillingPlan) {
        val details = productDetailsById[plan.productId]
        if (details == null) {
            _uiState.value = BillingUiState.Error("Plan details expired. Refresh the plans and try again.")
            return
        }
        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .setOfferToken(plan.offerToken)
            .build()
        val result = billingClient.launchBillingFlow(
            activity,
            BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productDetailsParams))
                .build()
        )
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            _uiState.value = BillingUiState.Error("Could not open Google Play checkout: ${result.debugMessage}")
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> purchases.orEmpty().forEach(::sendForServerVerification)
            BillingClient.BillingResponseCode.USER_CANCELED -> Unit
            else -> _uiState.value = BillingUiState.Error(
                "Purchase was not completed: ${billingResult.debugMessage}"
            )
        }
    }

    private fun sendForServerVerification(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return
        purchasesByToken[purchase.purchaseToken] = purchase
        onPurchaseForVerification(purchase.purchaseToken, purchase.products)
    }

    /** Call only after the secure server verifies the token and saves entitlement. */
    fun acknowledgeVerifiedPurchase(purchaseToken: String, onComplete: (Boolean) -> Unit = {}) {
        val purchase = purchasesByToken[purchaseToken] ?: return onComplete(false)
        if (purchase.isAcknowledged) return onComplete(true)
        billingClient.acknowledgePurchase(
            AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchaseToken)
                .build()
        ) { result ->
            onComplete(result.responseCode == BillingClient.BillingResponseCode.OK)
        }
    }

    fun close() {
        billingClient.endConnection()
    }
}
