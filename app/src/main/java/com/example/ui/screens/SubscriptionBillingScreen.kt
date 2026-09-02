package com.example.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billing.BillingPlan
import com.example.billing.BillingUiState
import com.example.billing.PlayBillingManager
import com.example.billing.SmartFarmBillingProducts
import com.example.data.FarmSubscriptionAccess
import com.example.data.SubscriptionStatus
import com.example.data.SubscriptionTier
import com.example.data.UserSession
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionBillingScreen(
    userSession: UserSession,
    subscriptionAccess: FarmSubscriptionAccess,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    onPurchaseSuccess: ((tier: String) -> Unit)? = null
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()

    var statusMessage by remember {
        mutableStateOf(
            if (subscriptionAccess.status == SubscriptionStatus.EXPIRED) {
                "Your subscription has expired. Select a Google Play plan below to restore full farm management features."
            } else {
                "Manage your farm subscription securely powered by Google Play Billing."
            }
        )
    }
    var isProcessingPurchase by remember { mutableStateOf(false) }
    var selectedTierProcessing by remember { mutableStateOf<String?>(null) }

    val billingManagerRef = remember { arrayOfNulls<PlayBillingManager>(1) }
    val billingManager = remember {
        PlayBillingManager(context) { purchaseToken, productIds ->
            scope.launch {
                isProcessingPurchase = true
                statusMessage = "Verifying purchase with Google Play..."
                val tier = when {
                    productIds.contains(SmartFarmBillingProducts.PRO_ANNUAL) -> "PRO"
                    productIds.contains(SmartFarmBillingProducts.PREMIUM_ANNUAL) -> "PREMIUM"
                    else -> "PREMIUM"
                }
                billingManagerRef[0]?.acknowledgeVerifiedPurchase(purchaseToken) { success: Boolean ->
                    isProcessingPurchase = false
                    selectedTierProcessing = null
                    if (success) {
                        statusMessage = "Congratulations! Your $tier subscription is now active."
                        onPurchaseSuccess?.invoke(tier)
                    } else {
                        statusMessage = "Purchase recorded. Acknowledgment pending verification."
                    }
                }
            }
        }.also { billingManagerRef[0] = it }
    }

    DisposableEffect(billingManager) {
        billingManager.connect()
        onDispose {
            billingManager.close()
        }
    }

    val billingState by billingManager.uiState.collectAsState()
    val hasActivePremium = subscriptionAccess.status == SubscriptionStatus.ACTIVE &&
        subscriptionAccess.tier == SubscriptionTier.PREMIUM
    val hasActivePro = subscriptionAccess.status == SubscriptionStatus.ACTIVE &&
        subscriptionAccess.tier == SubscriptionTier.PRO

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Google Play Subscriptions", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onClose, modifier = Modifier.testTag("billing_close_btn")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { billingManager.queryAnnualPlans() }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh Google Play Plans")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = if (subscriptionAccess.isReadOnly) MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (subscriptionAccess.isReadOnly) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (subscriptionAccess.isReadOnly) Icons.Default.Lock else Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Current Tier: ${subscriptionAccess.tier.name}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (subscriptionAccess.isReadOnly) "Expired — Read-only mode until renewed."
                            else if (subscriptionAccess.tier == SubscriptionTier.FREE) "Free Starter Plan (Max 2 Cattle)"
                            else "Active Paid Plan — Full Farm Access",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Billed securely via Google Play. Cancel or manage anytime in Play Store.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (statusMessage.isNotEmpty()) {
                Text(
                    text = statusMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val readyPlans = (billingState as? BillingUiState.Ready)?.plans ?: emptyList()
            val premiumPlayPlan = readyPlans.find { it.productId == SmartFarmBillingProducts.PREMIUM_ANNUAL }
            val proPlayPlan = readyPlans.find { it.productId == SmartFarmBillingProducts.PRO_ANNUAL }

            // 1. Free Starter Plan
            PlayPlanCard(
                title = "Free Starter",
                badge = "BASIC",
                price = "Free forever",
                period = "",
                features = listOf("Up to 2 Cattle records", "Basic health & milk logging", "Offline-first local database"),
                buttonText = if (subscriptionAccess.tier == SubscriptionTier.FREE && !subscriptionAccess.isReadOnly) "Current Plan" else null,
                isCurrent = subscriptionAccess.tier == SubscriptionTier.FREE && !subscriptionAccess.isReadOnly,
                isLoading = false,
                isActionEnabled = false,
                onChoose = {}
            )

            // 2. Farm Premium Plan
            PlayPlanCard(
                title = "Farm Premium",
                badge = "POPULAR",
                price = premiumPlayPlan?.price?.ifBlank { "US$10.00" } ?: "US$10.00",
                period = "/ year",
                features = listOf(
                    "Up to 15 Cattle & 2 Poultry Flocks",
                    "Full Milk & Egg Production Tracking",
                    "Farm Reminders & Health Alerts",
                    "Automated Feed Deduction & Reports",
                    "Complete Finance & Expense Records"
                ),
                buttonText = when {
                    hasActivePremium -> "Current Plan"
                    hasActivePro -> null
                    else -> "Subscribe via Google Play"
                },
                isCurrent = hasActivePremium,
                isLoading = isProcessingPurchase && selectedTierProcessing == "PREMIUM",
                isActionEnabled = !hasActivePremium && !isProcessingPurchase,
                onChoose = {
                    if (!userSession.role.equals("OWNER", ignoreCase = true)) {
                        statusMessage = "Only the farm owner can purchase or renew subscriptions."
                        return@PlayPlanCard
                    }
                    if (activity != null && premiumPlayPlan != null) {
                        selectedTierProcessing = "PREMIUM"
                        billingManager.launchAnnualPlanPurchase(activity, premiumPlayPlan)
                    } else {
                        statusMessage = "Google Play Billing is not connected or products are not active yet on Play Console."
                    }
                }
            )

            // 3. Farm Pro Plan
            PlayPlanCard(
                title = "Farm Pro",
                badge = "UNLIMITED",
                price = proPlayPlan?.price?.ifBlank { "US$30.00" } ?: "US$30.00",
                period = "/ year",
                features = listOf(
                    "Unlimited Cattle & Livestock",
                    "Unlimited Poultry Flocks & Batches",
                    "Full Multi-Worker Management & Permissions",
                    "Advanced Farm Analytics & Export",
                    "Priority Cloud Sync & Multi-Device Access"
                ),
                buttonText = if (hasActivePro) "Current Plan" else "Upgrade to Pro",
                isCurrent = hasActivePro,
                isLoading = isProcessingPurchase && selectedTierProcessing == "PRO",
                isActionEnabled = !hasActivePro && !isProcessingPurchase,
                onChoose = {
                    if (!userSession.role.equals("OWNER", ignoreCase = true)) {
                        statusMessage = "Only the farm owner can purchase or renew subscriptions."
                        return@PlayPlanCard
                    }
                    if (activity != null && proPlayPlan != null) {
                        selectedTierProcessing = "PRO"
                        billingManager.launchAnnualPlanPurchase(activity, proPlayPlan)
                    } else {
                        statusMessage = "Google Play Billing is not connected or products are not active yet on Play Console."
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onClose,
                modifier = Modifier.fillMaxWidth().testTag("billing_bottom_close_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Return to Farm")
            }
        }
    }
}

@Composable
private fun PlayPlanCard(
    title: String,
    badge: String,
    price: String,
    period: String,
    features: List<String>,
    buttonText: String?,
    isCurrent: Boolean,
    isLoading: Boolean,
    isActionEnabled: Boolean,
    onChoose: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) MaterialTheme.colorScheme.surfaceVariant
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrent) 1.dp else 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = badge,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = price, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                if (period.isNotEmpty()) {
                    Text(text = period, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 3.dp, start = 4.dp))
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                features.forEach { feat ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = feat, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            if (buttonText != null || isLoading) {
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onChoose,
                    enabled = !isLoading && isActionEnabled,
                    modifier = Modifier.fillMaxWidth().testTag("plan_action_${title.lowercase().replace(" ", "_")}"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(20.dp), color = Color.White)
                    } else {
                        Text(text = buttonText.orEmpty(), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
