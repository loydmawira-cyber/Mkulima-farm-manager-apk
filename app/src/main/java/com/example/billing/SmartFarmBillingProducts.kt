package com.example.billing

object SmartFarmBillingProducts {
    const val PREMIUM_ANNUAL = "smart_farm_premium_annual"
    const val PRO_ANNUAL = "smart_farm_pro_annual"
    const val PREMIUM_SIMPLE = "smart_farm_premium"
    const val PRO_SIMPLE = "smart_farm_pro"
    const val PREMIUM_SUB = "premium_annual"
    const val PRO_SUB = "pro_annual"
    const val PREMIUM_MONTHLY = "smart_farm_premium_monthly"
    const val PRO_MONTHLY = "smart_farm_pro_monthly"

    val subscriptionProductIds = listOf(
        PREMIUM_ANNUAL,
        PRO_ANNUAL,
        PREMIUM_SIMPLE,
        PRO_SIMPLE,
        PREMIUM_SUB,
        PRO_SUB,
        PREMIUM_MONTHLY,
        PRO_MONTHLY
    )

    fun isPremiumProduct(productId: String): Boolean {
        val lower = productId.lowercase()
        return lower.contains("premium")
    }

    fun isProProduct(productId: String): Boolean {
        val lower = productId.lowercase()
        return lower.contains("pro")
    }
}

data class BillingPlan(
    val productId: String,
    val title: String,
    val description: String,
    val price: String,
    internal val offerToken: String = "",
    val productType: String = "subs"
)

sealed interface BillingUiState {
    data object Loading : BillingUiState
    data class Ready(val plans: List<BillingPlan>) : BillingUiState
    data class Error(val message: String, val details: String = "") : BillingUiState
}

