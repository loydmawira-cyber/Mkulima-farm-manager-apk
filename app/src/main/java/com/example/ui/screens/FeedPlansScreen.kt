package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.FarmUnit
import com.example.data.FeedPlan
import com.example.data.InventoryItem
import com.example.data.InventoryMovement

@Composable
fun FeedPlansScreen(
    userRole: String,
    automationEnabled: Boolean,
    units: List<FarmUnit>,
    inventoryItems: List<InventoryItem>,
    plans: List<FeedPlan>,
    movements: List<InventoryMovement>,
    onAutomationChanged: (Boolean) -> Unit,
    onSavePlan: (FeedPlan) -> Unit,
    onDeletePlan: (Long) -> Unit,
) {
    var showPlanDialog by remember { mutableStateOf(false) }
    val owner = userRole.equals("OWNER", true)
    val lowItems = inventoryItems.filter { it.minimumThreshold > 0 && it.quantityAvailable <= it.minimumThreshold }

    if (showPlanDialog) {
        FeedPlanDialog(
            units = units,
            inventoryItems = inventoryItems,
            onDismiss = { showPlanDialog = false },
            onSave = { onSavePlan(it); showPlanDialog = false },
        )
    }

    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Feed & Silage Plans", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Daily quantities are always measured in kilograms and each deduction is recorded in the inventory ledger.", style = MaterialTheme.typography.bodySmall)
        }
        item {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Automatic daily stock deduction", fontWeight = FontWeight.Bold)
                        Text(if (automationEnabled) "Enabled — plans will deduct once per day." else "Disabled — no stock is deducted automatically.", style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(checked = automationEnabled, onCheckedChange = { if (owner) onAutomationChanged(it) }, enabled = owner)
                }
            }
        }
        if (lowItems.isNotEmpty()) item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Text("Low stock: ${lowItems.joinToString { it.itemName }}", modifier = Modifier.padding(14.dp), color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
        item { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text("Active plans", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); if (owner) Button(onClick = { showPlanDialog = true }) { Text("Add plan") } } }
        if (plans.isEmpty()) item { Text("No daily feed plans yet. Add a cattle, poultry, or silage plan to connect livestock consumption to inventory.") }
        items(plans, key = { it.syncId }) { plan ->
            ElevatedCard(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("${plan.targetUnitName} • ${plan.inventoryItemName}", fontWeight = FontWeight.Bold)
                        Text("${plan.consumptionKind.lowercase().replaceFirstChar { it.uppercase() }} • ${plan.dailyQuantityKg} kg/day • ${if (plan.isEnabled) "Active" else "Paused"}", style = MaterialTheme.typography.bodySmall)
                        if (plan.lastProcessedDate.isNotBlank()) Text("Last processed: ${plan.lastProcessedDate}", style = MaterialTheme.typography.labelSmall)
                    }
                    if (owner) TextButton(onClick = { onDeletePlan(plan.id) }) { Text("Remove") }
                }
            }
        }
        item { Text("Inventory movement ledger", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp)) }
        if (movements.isEmpty()) item { Text("Automatic and manual stock movements will appear here.") }
        items(movements.take(30), key = { it.syncId }) { movement ->
            ListItem(
                headlineContent = { Text("${movement.inventoryItemName} • ${movement.targetUnitName}") },
                supportingContent = { Text("${movement.movementType.replace('_', ' ')} • ${movement.occurredOn} • Balance ${movement.balanceAfterKg} kg") },
                trailingContent = { Text("${movement.quantityDeltaKg} kg", color = if (movement.quantityDeltaKg < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary) }
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun FeedPlanDialog(units: List<FarmUnit>, inventoryItems: List<InventoryItem>, onDismiss: () -> Unit, onSave: (FeedPlan) -> Unit) {
    var livestockType by remember { mutableStateOf("POULTRY") }
    var target by remember { mutableStateOf<FarmUnit?>(null) }
    var stock by remember { mutableStateOf<InventoryItem?>(null) }
    var dailyKg by remember { mutableStateOf("") }
    var unitMenu by remember { mutableStateOf(false) }
    var stockMenu by remember { mutableStateOf(false) }
    val targetUnits = units.filter { if (livestockType == "POULTRY") it.type.contains("Poultry", true) || it.name.contains("Flock", true) else !it.type.contains("Poultry", true) && !it.name.contains("Flock", true) }
    val stockItems = inventoryItems.filter { it.category.equals("Feed", true) || (livestockType == "CATTLE" && it.isSilage) }
    val silage = stock?.isSilage == true || stock?.category.equals("Silage", true) == true

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Daily feed plan") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row { FilterChip(selected = livestockType == "POULTRY", onClick = { livestockType = "POULTRY"; target = null; stock = null }, label = { Text("Poultry") }); Spacer(Modifier.width(8.dp)); FilterChip(selected = livestockType == "CATTLE", onClick = { livestockType = "CATTLE"; target = null; stock = null }, label = { Text("Cattle") }) }
            Box { OutlinedButton(onClick = { unitMenu = true }, modifier = Modifier.fillMaxWidth()) { Text(target?.name ?: "Select herd or flock") }; DropdownMenu(expanded = unitMenu, onDismissRequest = { unitMenu = false }) { targetUnits.forEach { unit -> DropdownMenuItem(text = { Text(unit.name) }, onClick = { target = unit; unitMenu = false }) } } }
            Box { OutlinedButton(onClick = { stockMenu = true }, modifier = Modifier.fillMaxWidth()) { Text(stock?.itemName ?: "Select feed or silage stock") }; DropdownMenu(expanded = stockMenu, onDismissRequest = { stockMenu = false }) { stockItems.forEach { item -> DropdownMenuItem(text = { Text("${item.itemName} • ${item.quantityAvailable} ${item.unitOfMeasurement}") }, onClick = { stock = item; stockMenu = false }) } } }
            OutlinedTextField(value = dailyKg, onValueChange = { dailyKg = it }, label = { Text("Daily quantity (kg)") }, suffix = { Text("kg") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Text(if (silage) "This will record daily silage use in kilograms." else "This will record daily feed use in kilograms.", style = MaterialTheme.typography.bodySmall)
        } },
        confirmButton = { Button(onClick = { val u = target; val i = stock; val kg = dailyKg.toDoubleOrNull() ?: 0.0; if (u != null && i != null && kg > 0) onSave(FeedPlan(targetUnitId = u.id, targetUnitSyncId = u.syncId, targetUnitName = u.name, livestockType = livestockType, inventoryItemId = i.id, inventoryItemSyncId = i.syncId, inventoryItemName = i.itemName, consumptionKind = if (silage) "SILAGE" else "FEED", dailyQuantityKg = kg)) }, enabled = target != null && stock != null && (dailyKg.toDoubleOrNull() ?: 0.0) > 0) { Text("Save plan") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
