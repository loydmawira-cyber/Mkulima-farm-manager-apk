package com.example.billing

object SmartFarmBillingProducts {
    const val PREMIUM_ANNUAL = "smart_farm_premium_annual"
    const val PRO_ANNUAL = "smart_farm_pro_annual"

    val subscriptionProductIds = listOf(PREMIUM_ANNUAL, PRO_ANNUAL)
}

data class BillingPlan(
    val productId: String,
    val title: String,
    val description: String,
    val price: String,
    internal val offerToken: String
)

sealed interface BillingUiState {
    data object Loading : BillingUiState
    data class Ready(val plans: List<BillingPlan>) : BillingUiState
    data class Error(val message: String) : BillingUiState
}
