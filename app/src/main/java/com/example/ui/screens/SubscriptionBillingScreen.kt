package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.billing.BillingPlan
import com.example.billing.BillingUiState
import com.example.billing.PlayBillingManager

@Composable
fun SubscriptionBillingScreen(
    onClose: () -> Unit,
    onPurchaseForVerification: (purchaseToken: String, productIds: List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context.findActivity()
    val manager = remember {
        PlayBillingManager(context.applicationContext, onPurchaseForVerification)
    }
    val state by manager.uiState.collectAsState()

    DisposableEffect(manager) {
        manager.connect()
        onDispose { manager.close() }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Subscription & Billing", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Google Play shows available payment methods, including card and eligible M-Pesa Xpress billing. Access changes only after secure server verification.")

        FreePlanCard()

        when (val current = state) {
            BillingUiState.Loading -> CircularProgressIndicator()
            is BillingUiState.Error -> {
                Text(current.message, color = MaterialTheme.colorScheme.error)
                OutlinedButton(onClick = manager::queryAnnualPlans) { Text("Retry") }
            }
            is BillingUiState.Ready -> current.plans.forEach { plan ->
                AnnualPlanCard(
                    plan = plan,
                    enabled = activity != null,
                    onBuy = { activity?.let { manager.launchAnnualPlanPurchase(it, plan) } }
                )
            }
        }

        Spacer(Modifier.height(4.dp))
        OutlinedButton(onClick = onClose, modifier = Modifier.fillMaxWidth()) { Text("Close") }
    }
}

@Composable
private fun FreePlanCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Free", fontWeight = FontWeight.Bold)
            Text("2 cattle only. Poultry, Finance, Reports, and Worker Management are unavailable.")
        }
    }
}

@Composable
private fun AnnualPlanCard(plan: BillingPlan, enabled: Boolean, onBuy: () -> Unit) {
    val limits = when (plan.productId) {
        "smart_farm_premium_annual" -> "Up to 15 cattle and 2 poultry flocks, with all features."
        "smart_farm_pro_annual" -> "Unlimited cattle and flocks, with all features."
        else -> "Annual Smart Farm subscription."
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(plan.title.ifBlank { plan.productId }, fontWeight = FontWeight.Bold)
            if (plan.description.isNotBlank()) Text(plan.description)
            Text(limits)
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(plan.price, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Button(onClick = onBuy, enabled = enabled) { Text("Choose annual plan") }
            }
        }
    }
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
