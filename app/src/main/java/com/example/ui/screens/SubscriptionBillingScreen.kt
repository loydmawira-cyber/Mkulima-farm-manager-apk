package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.FarmSubscriptionAccess
import com.example.data.SubscriptionStatus
import com.example.data.SubscriptionTier
import com.example.data.UserSession
import com.example.payments.PaystackCheckoutClient
import com.example.payments.PaystackCheckoutTier
import kotlinx.coroutines.launch

/**
 * Direct-APK annual subscription screen for Smart Farm App.
 *
 * The client never contains a Paystack secret key and never unlocks access from
 * a button click. It only opens a server-created hosted checkout. The existing
 * Firestore subscription listener updates subscriptionAccess after the signed
 * webhook and server verification complete.
 */
@Composable
fun SubscriptionBillingScreen(
    userSession: UserSession,
    subscriptionAccess: FarmSubscriptionAccess,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    checkoutClient: PaystackCheckoutClient? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val client = remember(checkoutClient) { checkoutClient ?: PaystackCheckoutClient() }
    var startingTier by remember { mutableStateOf<PaystackCheckoutTier?>(null) }
    var pendingCheckoutTier by remember { mutableStateOf<PaystackCheckoutTier?>(null) }
    var pendingReference by remember { mutableStateOf<String?>(null) }
    var isVerifyingPayment by remember { mutableStateOf(false) }
    var message by remember {
        mutableStateOf(
            if (subscriptionAccess.status == SubscriptionStatus.EXPIRED) {
                "Your paid plan has expired. Your saved farm data remains available in read-only mode until renewal is confirmed."
            } else {
                "Choose an annual plan. Payment is completed securely on Paystack."
            }
        )
    }

    fun beginCheckout(tier: PaystackCheckoutTier) {
        if (!userSession.role.equals("OWNER", ignoreCase = true)) {
            message = "Only the farm owner can start a subscription checkout."
            return
        }
        scope.launch {
            startingTier = tier
            message = "Preparing secure Paystack checkout..."
            client.initializeCheckout(userSession.farmId, tier)
                .onSuccess { checkout ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(checkout.authorizationUrl))
                    runCatching { context.startActivity(intent) }
                        .onSuccess {
                            pendingCheckoutTier = checkout.tier
                            pendingReference = checkout.reference
                            message = "Checkout opened. Complete payment, return here, and tap Verify payment."
                        }
                        .onFailure {
                            message = "Android could not open the secure Paystack checkout page."
                        }
                }
                .onFailure { error ->
                    message = error.message ?: "Unable to prepare Paystack checkout."
                }
            startingTier = null
        }
    }

    fun verifyPendingPayment() {
        val tier = pendingCheckoutTier ?: return
        val reference = pendingReference ?: return
        scope.launch {
            isVerifyingPayment = true
            message = "Checking the payment securely with Paystack..."
            client.verifyCheckout(userSession.farmId, tier, reference)
                .onSuccess {
                    message = "Payment verified. Your subscription will unlock when the Firestore entitlement sync completes."
                    pendingCheckoutTier = null
                    pendingReference = null
                }
                .onFailure { error ->
                    message = error.message ?: "Payment is not confirmed yet. If you just paid, wait briefly and try Verify payment again."
                }
            isVerifyingPayment = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Subscription & Billing",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Smart Farm App annual plans",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Prices are fixed in KES so M-Pesa works reliably. International card providers convert the KES charge into the customer’s local currency."
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = if (subscriptionAccess.isReadOnly) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            }
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Current plan: ${subscriptionAccess.tier.name}",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (subscriptionAccess.isReadOnly) {
                        "Expired — read-only until a payment is verified."
                    } else {
                        "Active — verified farm access is controlled by the server."
                    }
                )
            }
        }

        Text(text = message, color = MaterialTheme.colorScheme.onSurfaceVariant)

        if (pendingReference != null) {
            Button(
                onClick = ::verifyPendingPayment,
                enabled = !isVerifyingPayment,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isVerifyingPayment) {
                    CircularProgressIndicator(strokeWidth = 2.dp)
                } else {
                    Text("Verify payment")
                }
            }
        }

        PlanCard(
            title = "Free",
            details = "2 cattle only. No poultry, Finance, Reports, or Worker Management.",
            price = "No cost",
            buttonText = null,
            onChoose = {}
        )
        PlanCard(
            title = "Premium",
            details = "Up to 15 cattle and 2 poultry flocks, with all features.",
            price = "US$10/year • KES 1,300 charged",
            buttonText = if (startingTier == PaystackCheckoutTier.PREMIUM) null else "Choose Premium",
            onChoose = { beginCheckout(PaystackCheckoutTier.PREMIUM) },
            isLoading = startingTier == PaystackCheckoutTier.PREMIUM
        )
        PlanCard(
            title = "Pro",
            details = "Unlimited cattle and poultry flocks, with all features.",
            price = "US$30/year • KES 3,900 charged",
            buttonText = if (startingTier == PaystackCheckoutTier.PRO) null else "Choose Pro",
            onChoose = { beginCheckout(PaystackCheckoutTier.PRO) },
            isLoading = startingTier == PaystackCheckoutTier.PRO
        )

        Spacer(Modifier.height(4.dp))
        Button(onClick = onClose, modifier = Modifier.fillMaxWidth()) {
            Text("Close")
        }
    }
}

@Composable
private fun PlanCard(
    title: String,
    details: String,
    price: String,
    buttonText: String?,
    onChoose: () -> Unit,
    isLoading: Boolean = false
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(details)
            Text(
                text = price,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = onChoose,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(strokeWidth = 2.dp)
                } else {
                    Text(buttonText ?: "Start checkout")
                }
            }
        }
    }
}
