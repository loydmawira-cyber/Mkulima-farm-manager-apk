package com.example.data

enum class SubscriptionTier {
    FREE,
    PREMIUM,
    PRO
}

enum class SubscriptionStatus {
    ACTIVE,
    EXPIRED
}

data class FarmSubscriptionAccess(
    val tier: SubscriptionTier,
    val status: SubscriptionStatus,
    val expiresAt: Long,
    val isReadOnly: Boolean,
    val canReceiveReminders: Boolean,
    val maxCattle: Int,
    val maxPoultryFlocks: Int,
    val canUseFinance: Boolean,
    val canUseReports: Boolean,
    val canManageWorkers: Boolean
)

object SubscriptionPolicy {
    const val PREMIUM_ANNUAL_PRODUCT_ID = "smart_farm_premium_annual"
    const val PRO_ANNUAL_PRODUCT_ID = "smart_farm_pro_annual"

    fun accessFor(
        tierName: String,
        statusName: String,
        expiresAt: Long,
        now: Long = System.currentTimeMillis()
    ): FarmSubscriptionAccess {
        val tier = runCatching { SubscriptionTier.valueOf(tierName.uppercase()) }.getOrDefault(SubscriptionTier.FREE)
        val storedStatus = runCatching { SubscriptionStatus.valueOf(statusName.uppercase()) }.getOrDefault(SubscriptionStatus.ACTIVE)
        val expiredPaidSubscription = tier != SubscriptionTier.FREE &&
            (storedStatus == SubscriptionStatus.EXPIRED || expiresAt <= now)
        val status = if (expiredPaidSubscription) SubscriptionStatus.EXPIRED else SubscriptionStatus.ACTIVE
        val isReadOnly = status == SubscriptionStatus.EXPIRED

        return when (tier) {
            SubscriptionTier.FREE -> FarmSubscriptionAccess(
                tier = tier,
                status = SubscriptionStatus.ACTIVE,
                expiresAt = 0L,
                isReadOnly = false,
                canReceiveReminders = true,
                maxCattle = 2,
                maxPoultryFlocks = 0,
                canUseFinance = false,
                canUseReports = false,
                canManageWorkers = false
            )
            SubscriptionTier.PREMIUM -> FarmSubscriptionAccess(
                tier = tier,
                status = status,
                expiresAt = expiresAt,
                isReadOnly = isReadOnly,
                canReceiveReminders = !isReadOnly,
                maxCattle = if (isReadOnly) 0 else 15,
                maxPoultryFlocks = if (isReadOnly) 0 else 2,
                canUseFinance = !isReadOnly,
                canUseReports = !isReadOnly,
                canManageWorkers = !isReadOnly
            )
            SubscriptionTier.PRO -> FarmSubscriptionAccess(
                tier = tier,
                status = status,
                expiresAt = expiresAt,
                isReadOnly = isReadOnly,
                canReceiveReminders = !isReadOnly,
                maxCattle = if (isReadOnly) 0 else Int.MAX_VALUE,
                maxPoultryFlocks = if (isReadOnly) 0 else Int.MAX_VALUE,
                canUseFinance = !isReadOnly,
                canUseReports = !isReadOnly,
                canManageWorkers = !isReadOnly
            )
        }
    }
}
