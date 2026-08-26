package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Safe pre-launch subscription page.
 *
 * It intentionally does not create PlayBillingManager or contact Google Play.
 * This keeps Settings usable while the Play Console app, test release, products,
 * and server verification endpoint are not ready yet.
 */
@Composable
fun SubscriptionBillingScreen(
    onClose: () -> Unit,
    @Suppress("UNUSED_PARAMETER")
    onPurchaseForVerification: (purchaseToken: String, productIds: List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Subscription & Billing",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Google Play subscription setup is not active yet. Your farm remains on its current plan and no payment is taken from this screen."
        )

        PlanCard(
            title = "Free",
            details = "2 cattle only. Poultry, Finance, Reports, and Worker Management are unavailable.",
            price = "No cost"
        )
        PlanCard(
            title = "Premium",
            details = "Up to 15 cattle and 2 poultry flocks, with all features. This annual plan will be available after the Google Play test setup is complete.",
            price = "US$10 / year"
        )
        PlanCard(
            title = "Pro",
            details = "Unlimited cattle and poultry flocks, with all features. This annual plan will be available after the Google Play test setup is complete.",
            price = "US$30 / year"
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
    price: String
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(details)
            Text(
                text = price,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
