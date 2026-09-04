package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.FieldPlan
import com.example.data.FeedPlan
import com.example.data.FinanceRecord
import com.example.data.FarmUnit
import com.example.data.InventoryItem
import com.example.data.InventoryMovement
import com.example.ui.theme.ForestGreenPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AssetsScreen(
    userRole: String,
    livestock: @Composable () -> Unit,
    inventoryItems: List<InventoryItem>,
    fieldPlans: List<FieldPlan>,
    units: List<FarmUnit>,
    feedPlans: List<FeedPlan>,
    inventoryMovements: List<InventoryMovement>,
    automaticFeedDeductionEnabled: Boolean,
    financeRecords: List<FinanceRecord>,
    onAddInventory: (InventoryItem) -> Unit,
    onUpdateInventory: (InventoryItem) -> Unit,
    onDeleteInventory: (InventoryItem) -> Unit,
    onAddField: (FieldPlan) -> Unit,
    onUpdateField: (FieldPlan) -> Unit,
    onDeleteField: (FieldPlan) -> Unit,
    onHarvest: (FieldPlan, String, Double, Double, String) -> Unit,
    onSaveFeedPlan: (FeedPlan) -> Unit,
    onDeleteFeedPlan: (Long) -> Unit,
    onAutomaticFeedDeductionChanged: (Boolean) -> Unit,
    onLogCropActivity: ((activityType: String, fieldName: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isOwner = userRole.equals("OWNER", ignoreCase = true)
    var tab by remember { mutableIntStateOf(0) }
    var inventoryEditor by remember { mutableStateOf<InventoryItem?>(null) }
    var fieldEditor by remember { mutableStateOf<FieldPlan?>(null) }
    var showNewInventory by remember { mutableStateOf(false) }
    var showNewField by remember { mutableStateOf(false) }
    var inventoryActionTarget by remember { mutableStateOf<InventoryItem?>(null) }
    var fieldActionTarget by remember { mutableStateOf<FieldPlan?>(null) }
    var inventoryDeleteTarget by remember { mutableStateOf<InventoryItem?>(null) }
    var fieldDeleteTarget by remember { mutableStateOf<FieldPlan?>(null) }
    var fieldToHarvest by remember { mutableStateOf<FieldPlan?>(null) }

    if (showNewInventory) {
        InventoryEntryDialog(
            existing = null,
            onDismiss = { showNewInventory = false },
            onSave = { onAddInventory(it); showNewInventory = false }
        )
    }
    inventoryEditor?.let { existing ->
        InventoryEntryDialog(
            existing = existing,
            onDismiss = { inventoryEditor = null },
            onSave = { onUpdateInventory(it); inventoryEditor = null }
        )
    }
    if (showNewField) {
        FieldEntryDialog(
            existing = null,
            onDismiss = { showNewField = false },
            onSave = { onAddField(it); showNewField = false }
        )
    }
    fieldEditor?.let { existing ->
        FieldEntryDialog(
            existing = existing,
            onDismiss = { fieldEditor = null },
            onSave = { onUpdateField(it); fieldEditor = null }
        )
    }
    fieldToHarvest?.let { field ->
        HarvestDialog(field, { fieldToHarvest = null }) { outcome, quantityKg, sale, date ->
            onHarvest(field, outcome, quantityKg, sale, date)
            fieldToHarvest = null
        }
    }

    inventoryActionTarget?.let { item ->
        LongPressActionsDialog(
            title = item.itemName,
            description = "Choose whether to edit this inventory record or remove it from the farm inventory.",
            onDismiss = { inventoryActionTarget = null },
            onEdit = { inventoryActionTarget = null; inventoryEditor = item },
            onDelete = { inventoryActionTarget = null; inventoryDeleteTarget = item }
        )
    }
    fieldActionTarget?.let { field ->
        LongPressActionsDialog(
            title = field.fieldName,
            description = "Choose whether to edit this field plan or remove it from the planting fields list.",
            onDismiss = { fieldActionTarget = null },
            onEdit = { fieldActionTarget = null; fieldEditor = field },
            onDelete = { fieldActionTarget = null; fieldDeleteTarget = field }
        )
    }
    inventoryDeleteTarget?.let { item ->
        DeleteConfirmationDialog(
            recordName = item.itemName,
            recordType = "inventory item",
            onDismiss = { inventoryDeleteTarget = null },
            onConfirm = { onDeleteInventory(item); inventoryDeleteTarget = null }
        )
    }
    fieldDeleteTarget?.let { field ->
        DeleteConfirmationDialog(
            recordName = field.fieldName,
            recordType = "field plan",
            onDismiss = { fieldDeleteTarget = null },
            onConfirm = { onDeleteField(field); fieldDeleteTarget = null }
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = tab) {
            listOf("Livestock", "Inventory", "Fields", "Feed Plans").forEachIndexed { index, label ->
                Tab(selected = tab == index, onClick = { tab = index }, text = { Text(label, fontWeight = FontWeight.Bold) })
            }
        }
        Box(Modifier.weight(1f)) {
            when (tab) {
                0 -> livestock()
                1 -> InventoryContent(
                    items = inventoryItems,
                    onLongPress = if (isOwner) ({ inventoryActionTarget = it }) else null
                )
                2 -> FieldsContent(
                    fields = fieldPlans,
                    onHarvest = { fieldToHarvest = it },
                    onLongPress = if (isOwner) ({ fieldActionTarget = it }) else null,
                    onLogCropActivity = onLogCropActivity
                )
                else -> FeedPlansScreen(
                    userRole,
                    automaticFeedDeductionEnabled,
                    units,
                    inventoryItems,
                    feedPlans,
                    inventoryMovements,
                    onAutomaticFeedDeductionChanged,
                    onSaveFeedPlan,
                    onDeleteFeedPlan
                )
            }
            if (isOwner && tab in 1..2) {
                FloatingActionButton(
                    onClick = { if (tab == 1) showNewInventory = true else showNewField = true },
                    containerColor = ForestGreenPrimary,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp)
                ) {
                    Icon(Icons.Filled.Add, if (tab == 1) "Add inventory item" else "Add field")
                }
            }
        }
    }
}

@Composable
private fun InventoryContent(items: List<InventoryItem>, onLongPress: ((InventoryItem) -> Unit)?) {
    val lowStockItems = items.filter { it.minimumThreshold > 0 && it.quantityAvailable <= it.minimumThreshold }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Farm Inventory", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Stock, inputs, tools and harvested feed", color = Color.Gray)
            Text("Long-press an item to edit or delete it.", color = Color(0xFF64748B), fontSize = 12.sp)
            if (lowStockItems.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = "Low Stock Alert",
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Low Inventory Alert (${lowStockItems.size} ${if (lowStockItems.size == 1) "item" else "items"})",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E),
                                fontSize = 14.sp
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        lowStockItems.forEach { item ->
                            val isDepleted = item.quantityAvailable <= 0.0
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "• ${item.itemName} (${item.category})",
                                    fontSize = 12.5.sp,
                                    color = Color(0xFF78350F),
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    if (isDepleted) "Depleted (0 ${item.unitOfMeasurement})" else "Low: ${item.quantityAvailable}/${item.minimumThreshold} ${item.unitOfMeasurement}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDepleted) Color(0xFFDC2626) else Color(0xFFB45309)
                                )
                            }
                        }
                    }
                }
            }
        }
        if (items.isEmpty()) item { EmptyState("No inventory yet", "Use + to record seed, fertiliser, tools, feed, harvest or silage.") }
        items(items, key = { it.syncId }) { item ->
            val lowStock = item.minimumThreshold > 0 && item.quantityAvailable <= item.minimumThreshold
            val isDepleted = item.minimumThreshold > 0 && item.quantityAvailable <= 0.0
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (onLongPress != null) Modifier.pointerInput(item.syncId) { detectTapGestures(onLongPress = { onLongPress(item) }) } else Modifier),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDepleted) Color(0xFFFEF2F2) else if (lowStock) Color(0xFFFFFDF5) else Color.White
                ),
                border = BorderStroke(
                    1.dp,
                    if (isDepleted) Color(0xFFFCA5A5) else if (lowStock) Color(0xFFFCD34D) else Color(0xFFE2E8F0)
                )
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isDepleted) Color(0xFFFEE2E2) else if (lowStock) Color(0xFFFEF3C7) else Color(0xFFF1F5F9),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Inventory2,
                                contentDescription = null,
                                tint = if (isDepleted) Color(0xFFDC2626) else if (lowStock) Color(0xFFD97706) else ForestGreenPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(item.itemName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Text(item.category, fontSize = 12.sp, color = Color.Gray)
                        if (item.description.isNotBlank()) Text(item.description, fontSize = 11.sp, color = Color(0xFF64748B))
                        if (item.expirationDate.isNotBlank()) Text("Expires: ${item.expirationDate}", fontSize = 11.sp, color = Color.Gray)
                        if (item.minimumThreshold > 0) {
                            Text("Min threshold: ${item.minimumThreshold} ${item.unitOfMeasurement}", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "${item.quantityAvailable} ${item.unitOfMeasurement}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (isDepleted) Color(0xFFDC2626) else if (lowStock) Color(0xFFD97706) else Color(0xFF14532D)
                        )
                        if (isDepleted) {
                            Spacer(Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFFEE2E2)
                            ) {
                                Text(
                                    "OUT OF STOCK",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDC2626),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        } else if (lowStock) {
                            Spacer(Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFFEF3C7)
                            ) {
                                Text(
                                    "LOW",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FieldsContent(
    fields: List<FieldPlan>,
    onHarvest: (FieldPlan) -> Unit,
    onLongPress: ((FieldPlan) -> Unit)?,
    onLogCropActivity: ((activityType: String, fieldName: String) -> Unit)? = null
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Planting Fields", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Plan crops, track harvest windows, then sell or transfer to silage.", color = Color.Gray)
            Text("Long-press a field to edit or delete it.", color = Color(0xFF64748B), fontSize = 12.sp)
        }
        if (fields.isEmpty()) item { EmptyState("No fields planned", "Use + to record a maize or crop field, planting date and expected harvest.") }
        items(fields, key = { it.syncId }) { field ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (onLongPress != null) Modifier.pointerInput(field.syncId) { detectTapGestures(onLongPress = { onLongPress(field) }) } else Modifier)
            ) {
                Column(Modifier.padding(14.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(field.fieldName, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Text("${field.cropName}${if (field.variety.isBlank()) "" else " • ${field.variety}"}", color = ForestGreenPrimary)
                        }
                        AssistChip(onClick = {}, label = { Text(field.status) }, enabled = false)
                    }
                    Text("Planted ${field.plantedDate} • Expected harvest ${field.estimatedHarvestDate}", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 6.dp))
                    Text("${field.sizeAcres} acres${if (field.location.isBlank()) "" else " • ${field.location}"}", fontSize = 12.sp, color = Color.Gray)
                    if (field.status == "HARVESTED") {
                        Text("Harvested ${field.harvestedTonnes} kgs → ${field.harvestOutcome}", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
                    } else {
                        // Crop Activity & Task Action Bar on Field Cards
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (onLogCropActivity != null) {
                                OutlinedButton(
                                    onClick = { onLogCropActivity("", field.fieldName) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Filled.Assignment, contentDescription = null, tint = ForestGreenPrimary, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Add Task", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                                }
                            }

                            Button(
                                onClick = { onHarvest(field) },
                                modifier = if (onLogCropActivity != null) Modifier.weight(1.3f) else Modifier,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                            ) {
                                Icon(Icons.Filled.Agriculture, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Harvest", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LongPressActionsDialog(
    title: String,
    description: String,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(description) },
        confirmButton = { TextButton(onClick = onEdit) { Text("Edit") } },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onDelete) { Text("Delete", color = Color(0xFFB91C1C)) }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}

@Composable
private fun DeleteConfirmationDialog(
    recordName: String,
    recordType: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete $recordType?") },
        text = { Text("Are you sure you want to delete \"$recordName\"? This action removes it from the active farm records.") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Delete", color = Color(0xFFB91C1C)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun EmptyState(title: String, body: String) {
    Column(Modifier.fillMaxWidth().padding(34.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        Text(body, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun InventoryEntryDialog(existing: InventoryItem?, onDismiss: () -> Unit, onSave: (InventoryItem) -> Unit) {
    val isEdit = existing != null
    var itemName by remember(existing?.syncId) { mutableStateOf(existing?.itemName.orEmpty()) }
    var category by remember(existing?.syncId) { mutableStateOf(existing?.category ?: "Seeds") }
    var description by remember(existing?.syncId) { mutableStateOf(existing?.description.orEmpty()) }
    var quantity by remember(existing?.syncId) { mutableStateOf(existing?.quantityAvailable?.toString().orEmpty()) }
    var unit by remember(existing?.syncId) { mutableStateOf(existing?.unitOfMeasurement ?: "kg") }
    var minimum by remember(existing?.syncId) { mutableStateOf(existing?.minimumThreshold?.toString() ?: "0") }
    var batch by remember(existing?.syncId) { mutableStateOf(existing?.batchOrLotNumber.orEmpty()) }
    var purchaseDate by remember(existing?.syncId) { mutableStateOf(existing?.purchaseDate?.ifBlank { today() } ?: today()) }
    var expiryDate by remember(existing?.syncId) { mutableStateOf(existing?.expirationDate.orEmpty()) }
    var cost by remember(existing?.syncId) { mutableStateOf(existing?.unitCost?.toString().orEmpty()) }
    var categoryMenu by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(18.dp).fillMaxWidth()) {
                Text(if (isEdit) "Edit Inventory Item" else "Inventory Entry", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("SKU/barcode and storage location are not required. Silage is recorded in kilograms (kgs) without a cost or finance entry.", fontSize = 12.sp, color = Color.Gray)
                Input(itemName, { itemName = it }, "Item Name *")
                Box {
                    OutlinedButton(onClick = { categoryMenu = true }, modifier = Modifier.fillMaxWidth()) { Text("Category: $category") }
                    DropdownMenu(expanded = categoryMenu, onDismissRequest = { categoryMenu = false }) {
                        listOf("Seeds", "Fertilizers", "Pesticides", "Tools", "Feed", "Harvested Crops", "Silage", "Other").forEach { choice ->
                            DropdownMenuItem(text = { Text(choice) }, onClick = {
                                category = choice
                                if (choice.equals("Silage", ignoreCase = true)) {
                                    unit = "kgs"
                                }
                                categoryMenu = false
                            })
                        }
                    }
                }
                Input(description, { description = it }, "Description")
                Row {
                    Input(quantity, { quantity = it }, "Quantity *", Modifier.weight(1f), KeyboardType.Decimal)
                    Spacer(Modifier.width(8.dp))
                    Input(unit, { unit = it }, "Unit", Modifier.weight(1f))
                }
                Input(minimum, { minimum = it }, "Minimum Threshold", keyboard = KeyboardType.Decimal)
                Input(batch, { batch = it }, "Batch or Lot Number")
                Input(purchaseDate, { purchaseDate = it }, "Purchase / Received Date")
                Input(expiryDate, { expiryDate = it }, "Expiration Date (optional)")
                if (!category.equals("Silage", ignoreCase = true)) Input(cost, { cost = it }, "Unit Cost (finance expense)", keyboard = KeyboardType.Decimal)
                Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(
                        onClick = {
                            val parsedQuantity = quantity.toDoubleOrNull() ?: 0.0
                            if (itemName.isNotBlank() && parsedQuantity > 0.0) {
                                val silage = category.equals("Silage", ignoreCase = true)
                                val base = existing ?: InventoryItem(itemName = itemName, category = category)
                                onSave(base.copy(
                                    itemName = itemName.trim(),
                                    category = category,
                                    skuOrBarcode = "",
                                    description = description.trim(),
                                    quantityAvailable = parsedQuantity,
                                    unitOfMeasurement = if (silage) "kgs" else unit.ifBlank { "kg" },
                                    minimumThreshold = minimum.toDoubleOrNull() ?: 0.0,
                                    storageLocation = "",
                                    batchOrLotNumber = batch.trim(),
                                    purchaseDate = purchaseDate,
                                    expirationDate = expiryDate,
                                    unitCost = if (silage) 0.0 else cost.toDoubleOrNull() ?: 0.0,
                                    isSilage = silage
                                ))
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                    ) { Text(if (isEdit) "Save Changes" else "Save Inventory") }
                }
            }
        }
    }
}

@Composable
private fun FieldEntryDialog(existing: FieldPlan?, onDismiss: () -> Unit, onSave: (FieldPlan) -> Unit) {
    val isEdit = existing != null
    var fieldName by remember(existing?.syncId) { mutableStateOf(existing?.fieldName.orEmpty()) }
    var location by remember(existing?.syncId) { mutableStateOf(existing?.location.orEmpty()) }
    var acres by remember(existing?.syncId) { mutableStateOf(existing?.sizeAcres?.toString().orEmpty()) }
    var crop by remember(existing?.syncId) { mutableStateOf(existing?.cropName ?: "Maize") }
    var variety by remember(existing?.syncId) { mutableStateOf(existing?.variety.orEmpty()) }
    var plantedDate by remember(existing?.syncId) { mutableStateOf(existing?.plantedDate?.ifBlank { today() } ?: today()) }
    var daysToHarvest by remember(existing?.syncId) { mutableStateOf(existing?.daysToHarvest?.toString() ?: "120") }
    var notes by remember(existing?.syncId) { mutableStateOf(existing?.plantingNotes.orEmpty()) }
    val calculatedHarvest = estimate(plantedDate, daysToHarvest.toIntOrNull() ?: 120)

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(18.dp).fillMaxWidth()) {
                Text(if (isEdit) "Edit Planting Field" else "Add Planting Field", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Maize commonly takes about 100–150 days. Adjust the duration for your crop and local conditions.", fontSize = 12.sp, color = Color.Gray)
                Input(fieldName, { fieldName = it }, "Field Name *")
                Input(location, { location = it }, "Location")
                Input(acres, { acres = it }, "Size (acres)", keyboard = KeyboardType.Decimal)
                Input(crop, { crop = it }, "Crop (e.g. Maize)")
                Input(variety, { variety = it }, "Variety")
                Input(plantedDate, { plantedDate = it }, "Planting Date")
                Input(daysToHarvest, { daysToHarvest = it }, "Days to Harvest", keyboard = KeyboardType.Number)
                Input(notes, { notes = it }, "Planting Notes")
                Text("Estimated harvest: $calculatedHarvest", fontWeight = FontWeight.SemiBold)
                Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(
                        onClick = {
                            if (fieldName.isNotBlank()) {
                                val base = existing ?: FieldPlan(fieldName = fieldName, cropName = crop, plantedDate = plantedDate)
                                onSave(base.copy(
                                    fieldName = fieldName.trim(),
                                    location = location.trim(),
                                    sizeAcres = acres.toDoubleOrNull() ?: 0.0,
                                    cropName = crop.trim(),
                                    variety = variety.trim(),
                                    plantedDate = plantedDate,
                                    daysToHarvest = daysToHarvest.toIntOrNull() ?: 120,
                                    estimatedHarvestDate = calculatedHarvest,
                                    plantingNotes = notes.trim()
                                ))
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                    ) { Text(if (isEdit) "Save Changes" else "Save Field") }
                }
            }
        }
    }
}

@Composable
private fun HarvestDialog(field: FieldPlan, onDismiss: () -> Unit, onSave: (String, Double, Double, String) -> Unit) {
    var outcome by remember { mutableStateOf("SILAGE") }
    var quantityKg by remember { mutableStateOf("") }
    var sale by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(today()) }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(18.dp).fillMaxWidth()) {
                Text("Harvest ${field.fieldName}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Choose Silage to receive kilograms (kgs) into inventory with no finance record, or Sold to record crop-sale income.", fontSize = 12.sp, color = Color.Gray)
                Row {
                    FilterChip(selected = outcome == "SILAGE", onClick = { outcome = "SILAGE" }, label = { Text("Chop as Silage (kgs)") })
                    Spacer(Modifier.width(8.dp))
                    FilterChip(selected = outcome == "SOLD", onClick = { outcome = "SOLD" }, label = { Text("Sold") })
                }
                Input(quantityKg, { quantityKg = it }, if (outcome == "SILAGE") "Harvested Silage (kgs) *" else "Harvested Quantity (kgs) *", keyboard = KeyboardType.Decimal)
                if (outcome == "SOLD") Input(sale, { sale = it }, "Total Sale Amount", keyboard = KeyboardType.Decimal)
                Input(date, { date = it }, "Harvest Date")
                Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(
                        onClick = {
                            val harvested = quantityKg.toDoubleOrNull() ?: 0.0
                            if (harvested > 0.0) onSave(outcome, harvested, if (outcome == "SOLD") sale.toDoubleOrNull() ?: 0.0 else 0.0, date)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                    ) { Text("Confirm Harvest") }
                }
            }
        }
    }
}

@Composable
private fun Input(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    keyboard: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboard),
        modifier = modifier.padding(top = 6.dp)
    )
}

private fun today(): String = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())

private fun estimate(date: String, days: Int): String = try {
    val parsed = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).parse(date) ?: Date()
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(
        Calendar.getInstance().apply { time = parsed; add(Calendar.DAY_OF_YEAR, days) }.time
    )
} catch (_: Exception) {
    ""
}
