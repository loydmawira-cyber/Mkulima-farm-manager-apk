package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Safe pre-approval subscription page.
 *
 * Premium and Pro controls are intentionally interactive now, but they do not
 * launch a payment or change access until the Play Console test setup and
 * secure server verification are complete.
 */
@Composable
fun SubscriptionBillingScreen(
    onClose: () -> Unit,
    @Suppress("UNUSED_PARAMETER")
    onPurchaseForVerification: (purchaseToken: String, productIds: List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

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
            text = "Choose a plan now to see that it is ready. Google Play checkout will be connected after Play Console approval and test setup."
        )

        PlanCard(
            title = "Free",
            details = "2 cattle only. Poultry, Finance, Reports, and Worker Management are unavailable.",
            price = "No cost"
        )
        PlanCard(
            title = "Premium",
            details = "Up to 15 cattle and 2 poultry flocks, with all features.",
            price = "US$10 / year",
            buttonText = "Choose Premium",
            onChoose = {
                Toast.makeText(
                    context,
                    "Premium is ready. Google Play checkout will be enabled after your Play Console approval and test setup.",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
        PlanCard(
            title = "Pro",
            details = "Unlimited cattle and poultry flocks, with all features.",
            price = "US$30 / year",
            buttonText = "Choose Pro",
            onChoose = {
                Toast.makeText(
                    context,
                    "Pro is ready. Google Play checkout will be enabled after your Play Console approval and test setup.",
                    Toast.LENGTH_LONG
                ).show()
            }
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
    buttonText: String? = null,
    onChoose: (() -> Unit)? = null
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
            if (buttonText != null && onChoose != null) {
                Button(onClick = onChoose, modifier = Modifier.fillMaxWidth()) {
                    Text(buttonText)
                }
            }
        }
    }
}
