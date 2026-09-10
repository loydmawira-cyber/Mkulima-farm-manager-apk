package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.FieldPlan
import com.example.data.FeedPlan
import com.example.data.FinanceRecord
import com.example.data.FinanceType
import com.example.data.FarmUnit
import com.example.data.InventoryItem
import com.example.data.InventoryMovement
import com.example.ui.components.AddFinanceRecordDialog
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
    onHarvest: (field: FieldPlan, outcome: String, quantityKg: Double, saleAmount: Double, harvestDate: String, targetPitId: Long?, targetPitName: String?) -> Unit,
    onSaveFeedPlan: (FeedPlan) -> Unit,
    onDeleteFeedPlan: (Long) -> Unit,
    onAutomaticFeedDeductionChanged: (Boolean) -> Unit,
    onLogCropActivity: ((activityType: String, fieldName: String) -> Unit)? = null,
    onAddFinanceRecord: ((type: FinanceType, category: String, amount: Double, description: String, date: String, targetUnit: String) -> Unit)? = null,
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
    var restockTemplate by remember { mutableStateOf<InventoryItem?>(null) }
    var showRecordFinanceDialog by remember { mutableStateOf(false) }
    var pendingFinanceCategory by remember { mutableStateOf("Feeds & Supplies") }
    var pendingFinanceAmount by remember { mutableStateOf(0.0) }
    var pendingFinanceDescription by remember { mutableStateOf("") }
    var pendingFinanceDate by remember { mutableStateOf("") }
    var pendingFinanceTargetUnit by remember { mutableStateOf("General Farm") }

    if (showNewInventory) {
        InventoryEntryDialog(
            existing = null,
            prefillTemplate = null,
            units = units,
            onDismiss = { showNewInventory = false },
            onSave = { onAddInventory(it); showNewInventory = false },
            onRequestRecordExpense = if (onAddFinanceRecord != null) { cat, amount, desc, date, targetUnit ->
                pendingFinanceCategory = cat
                pendingFinanceAmount = amount
                pendingFinanceDescription = desc
                pendingFinanceDate = date
                pendingFinanceTargetUnit = targetUnit.ifBlank { "General Farm" }
                showRecordFinanceDialog = true
            } else null
        )
    }
    restockTemplate?.let { template ->
        InventoryEntryDialog(
            existing = null,
            prefillTemplate = template,
            units = units,
            onDismiss = { restockTemplate = null },
            onSave = { onUpdateInventory(it); restockTemplate = null },
            onRequestRecordExpense = if (onAddFinanceRecord != null) { cat, amount, desc, date, targetUnit ->
                pendingFinanceCategory = cat
                pendingFinanceAmount = amount
                pendingFinanceDescription = desc
                pendingFinanceDate = date
                pendingFinanceTargetUnit = targetUnit.ifBlank { "General Farm" }
                showRecordFinanceDialog = true
            } else null
        )
    }
    inventoryEditor?.let { existing ->
        InventoryEntryDialog(
            existing = existing,
            prefillTemplate = null,
            units = units,
            onDismiss = { inventoryEditor = null },
            onSave = { onUpdateInventory(it); inventoryEditor = null },
            onRequestRecordExpense = null
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
        HarvestDialog(
            field = field,
            inventoryItems = inventoryItems,
            onDismiss = { fieldToHarvest = null }
        ) { outcome, quantityKg, sale, date, targetPitId, targetPitName ->
            onHarvest(field, outcome, quantityKg, sale, date, targetPitId, targetPitName)
            fieldToHarvest = null
        }
    }

    if (showRecordFinanceDialog && onAddFinanceRecord != null) {
        AddFinanceRecordDialog(
            initialType = FinanceType.EXPENSE,
            initialCategory = pendingFinanceCategory,
            initialAmount = pendingFinanceAmount,
            initialDescription = pendingFinanceDescription,
            initialDate = pendingFinanceDate,
            initialTargetUnit = pendingFinanceTargetUnit,
            units = units,
            fieldPlans = fieldPlans,
            userRole = userRole,
            canEditPastDaysLogs = true,
            onDismiss = { showRecordFinanceDialog = false },
            onSaveRecordWithDate = { type, category, amount, description, date ->
                onAddFinanceRecord(type, category, amount, description, date, pendingFinanceTargetUnit)
                showRecordFinanceDialog = false
            },
            onSaveRecordFull = { type, category, amount, description, date, targetUnit ->
                onAddFinanceRecord(type, category, amount, description, date, targetUnit)
                showRecordFinanceDialog = false
            },
            onSaveRecord = { type, category, amount, description ->
                onAddFinanceRecord(type, category, amount, description, pendingFinanceDate, pendingFinanceTargetUnit)
                showRecordFinanceDialog = false
            }
        )
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
                    onAddNewItem = if (isOwner) ({ showNewInventory = true }) else null,
                    onRestock = { restockTemplate = it },
                    onEdit = if (isOwner) ({ inventoryEditor = it }) else null,
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
private fun InventoryContent(
    items: List<InventoryItem>,
    onAddNewItem: (() -> Unit)? = null,
    onRestock: (InventoryItem) -> Unit,
    onEdit: ((InventoryItem) -> Unit)? = null,
    onLongPress: ((InventoryItem) -> Unit)? = null
) {
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    val lowStockItems = items.filter { it.minimumThreshold > 0 && it.quantityAvailable <= it.minimumThreshold }

    // Predefined category ordering priority
    val categoryPriority = listOf(
        "Feed", "Silage", "Seeds", "Fertilizers", "Pesticides", "Harvested Crops", "Tools", "Other"
    )

    val distinctCategories = remember(items) {
        items.map { it.category.ifBlank { "Other" } }.distinct().sortedWith(
            Comparator { a, b ->
                val idxA = categoryPriority.indexOfFirst { it.equals(a, ignoreCase = true) }.let { if (it == -1) 999 else it }
                val idxB = categoryPriority.indexOfFirst { it.equals(b, ignoreCase = true) }.let { if (it == -1) 999 else it }
                if (idxA != idxB) idxA.compareTo(idxB) else a.compareTo(b, ignoreCase = true)
            }
        )
    }

    val groupedItems = remember(items) {
        items.groupBy { it.category.ifBlank { "Other" } }
            .toList()
            .sortedWith(
                Comparator { a, b ->
                    val idxA = categoryPriority.indexOfFirst { it.equals(a.first, ignoreCase = true) }.let { if (it == -1) 999 else it }
                    val idxB = categoryPriority.indexOfFirst { it.equals(b.first, ignoreCase = true) }.let { if (it == -1) 999 else it }
                    if (idxA != idxB) idxA.compareTo(idxB) else a.first.compareTo(b.first, ignoreCase = true)
                }
            )
    }

    val filteredGroups = remember(groupedItems, selectedCategoryFilter) {
        if (selectedCategoryFilter == "ALL") {
            groupedItems
        } else {
            groupedItems.filter { it.first.equals(selectedCategoryFilter, ignoreCase = true) }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Farm Inventory", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("Stock, inputs, tools and harvested feed by category", color = Color.Gray, fontSize = 13.sp)
                }
                if (onAddNewItem != null) {
                    Button(
                        onClick = onAddNewItem,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add Item", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
            Text("Long-press an item to edit or delete it.", color = Color(0xFF64748B), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))

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
                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
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

            // Category Filter Chips
            if (distinctCategories.size > 1) {
                Spacer(Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        val isSel = selectedCategoryFilter == "ALL"
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) ForestGreenPrimary else Color(0xFFF1F5F9),
                            border = if (isSel) null else BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            modifier = Modifier.clickable { selectedCategoryFilter = "ALL" }
                        ) {
                            Text(
                                text = "All Categories (${items.size})",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else Color(0xFF334155)
                            )
                        }
                    }
                    items(distinctCategories) { cat ->
                        val isSel = selectedCategoryFilter.equals(cat, ignoreCase = true)
                        val count = groupedItems.firstOrNull { it.first.equals(cat, ignoreCase = true) }?.second?.size ?: 0
                        val (emoji, _, _) = getCategoryVisual(cat)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) ForestGreenPrimary else Color(0xFFF1F5F9),
                            border = if (isSel) null else BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            modifier = Modifier.clickable { selectedCategoryFilter = cat }
                        ) {
                            Text(
                                text = "$emoji $cat ($count)",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else Color(0xFF334155)
                            )
                        }
                    }
                }
            }
        }

        if (items.isEmpty()) {
            item {
                EmptyState("No inventory yet", "Use + or Add Item to record seed, fertiliser, tools, feed, harvest or silage.")
            }
        } else if (filteredGroups.isEmpty()) {
            item {
                EmptyState("No items in $selectedCategoryFilter", "There are no inventory records for this category.")
            }
        }

        // Render grouped items by Category
        filteredGroups.forEach { (catName, catItems) ->
            val (emoji, iconVector, catColor) = getCategoryVisual(catName)

            // Category Section Header
            item(key = "header_$catName") {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = catColor.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, catColor.copy(alpha = 0.22f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(emoji, fontSize = 17.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = catName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF1E293B)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = catColor.copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = "${catItems.size} ${if (catItems.size == 1) "item" else "items"}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = catColor,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // Items under this category
            items(catItems, key = { it.syncId }) { item ->
                val lowStock = item.minimumThreshold > 0 && item.quantityAvailable <= item.minimumThreshold
                val isDepleted = item.minimumThreshold > 0 && item.quantityAvailable <= 0.0
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (onLongPress != null || onEdit != null) {
                                Modifier.pointerInput(item.syncId) {
                                    detectTapGestures(
                                        onTap = { onEdit?.invoke(item) },
                                        onLongPress = { onLongPress?.invoke(item) }
                                    )
                                }
                            } else Modifier
                        ),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDepleted) Color(0xFFFEF2F2) else if (lowStock) Color(0xFFFFFDF5) else Color.White
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isDepleted) Color(0xFFFCA5A5) else if (lowStock) Color(0xFFFCD34D) else Color(0xFFE2E8F0)
                    )
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isDepleted) Color(0xFFFEE2E2) else if (lowStock) Color(0xFFFEF3C7) else catColor.copy(alpha = 0.12f),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isDepleted || lowStock) Icons.Filled.Inventory2 else iconVector,
                                        contentDescription = null,
                                        tint = if (isDepleted) Color(0xFFDC2626) else if (lowStock) Color(0xFFD97706) else catColor,
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

                        Spacer(Modifier.height(10.dp))
                        HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (item.batchOrLotNumber.isNotBlank()) "Batch: ${item.batchOrLotNumber}" else if (item.purchaseDate.isNotBlank()) "Logged: ${item.purchaseDate}" else "",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )

                            Button(
                                onClick = { onRestock(item) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ForestGreenPrimary,
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .height(36.dp)
                                    .testTag("restock_inventory_${item.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AddShoppingCart,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Restock",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getCategoryVisual(category: String): Triple<String, ImageVector, Color> {
    val cat = category.lowercase()
    return when {
        cat.contains("feed") -> Triple("🌾", Icons.Filled.Inventory2, Color(0xFFD97706))
        cat.contains("silage") -> Triple("🌿", Icons.Filled.Agriculture, Color(0xFF059669))
        cat.contains("seed") -> Triple("🌱", Icons.Filled.Inventory2, Color(0xFF16A34A))
        cat.contains("fertilizer") -> Triple("🧪", Icons.Filled.Inventory2, Color(0xFF0284C7))
        cat.contains("pesticide") -> Triple("🛡️", Icons.Filled.Inventory2, Color(0xFFEA580C))
        cat.contains("harvest") || cat.contains("crop") -> Triple("🌽", Icons.Filled.Inventory2, Color(0xFF65A30D))
        cat.contains("tool") || cat.contains("equip") -> Triple("🛠️", Icons.Filled.Build, Color(0xFF64748B))
        else -> Triple("📦", Icons.Filled.Inventory2, Color(0xFF1B5E20))
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
private fun InventoryEntryDialog(
    existing: InventoryItem?,
    prefillTemplate: InventoryItem? = null,
    units: List<FarmUnit> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (InventoryItem) -> Unit,
    onRequestRecordExpense: ((category: String, amount: Double, description: String, date: String, targetUnit: String) -> Unit)? = null
) {
    val isEdit = existing != null
    val isRestock = !isEdit && prefillTemplate != null
    val source = existing ?: prefillTemplate
    val stateKey = existing?.syncId ?: prefillTemplate?.syncId ?: "new"
    val activeFlocks = remember(units) {
        units.filter { !it.isDeleted && it.type.equals("Poultry", ignoreCase = true) }
            .map { it.name }.distinct()
    }
    var itemName by remember(stateKey) { mutableStateOf(source?.itemName.orEmpty()) }
    var category by remember(stateKey) { mutableStateOf(source?.category ?: "Seeds") }
    var targetUnit by remember(stateKey) {
        mutableStateOf(
            source?.storageLocation.orEmpty().ifBlank {
                if (source?.category.equals("Feed", ignoreCase = true)) activeFlocks.firstOrNull() ?: "Poultry" else "General Farm"
            }
        )
    }
    var description by remember(stateKey) { mutableStateOf(source?.description.orEmpty()) }
    var quantity by remember(stateKey) { mutableStateOf(if (isRestock) "" else existing?.quantityAvailable?.toString().orEmpty()) }
    var unit by remember(stateKey) { mutableStateOf(source?.unitOfMeasurement ?: "kg") }
    var minimum by remember(stateKey) { mutableStateOf(source?.minimumThreshold?.toString() ?: "0") }
    var batch by remember(stateKey) { mutableStateOf(if (isRestock) "" else existing?.batchOrLotNumber.orEmpty()) }
    var purchaseDate by remember(stateKey) { mutableStateOf(if (isRestock) today() else (existing?.purchaseDate?.ifBlank { today() } ?: today())) }
    var expiryDate by remember(stateKey) { mutableStateOf(if (isRestock) "" else existing?.expirationDate.orEmpty()) }
    var categoryMenu by remember { mutableStateOf(false) }
    var targetUnitMenu by remember { mutableStateOf(false) }
    var pendingItemToSave by remember { mutableStateOf<InventoryItem?>(null) }
    var pendingRestockQty by remember { mutableStateOf(0.0) }
    var showExpensePrompt by remember { mutableStateOf(false) }

    if (showExpensePrompt && pendingItemToSave != null) {
        val item = pendingItemToSave!!
        AlertDialog(
            onDismissRequest = {
                onSave(item)
                showExpensePrompt = false
            },
            title = { Text("Record as Expense?", fontWeight = FontWeight.Bold) },
            text = { Text("Do you want to add this as expense on income and expense?") },
            confirmButton = {
                Button(
                    onClick = {
                        val financeCat = when (item.category.lowercase()) {
                            "feed" -> "Poultry Feed & Nutrition"
                            "silage" -> "Feeds & Supplies"
                            "seeds", "harvested crops" -> "Seeds & Planting"
                            "fertilizers", "pesticides" -> "Fertilizer & Chemicals"
                            "tools" -> "Equipment & Maintenance"
                            else -> "Feeds & Supplies"
                        }
                        val expDesc = if (isRestock) {
                            "Inventory Restock: ${item.itemName} (+${pendingRestockQty} ${item.unitOfMeasurement})"
                        } else {
                            "Inventory: ${item.itemName} (${item.quantityAvailable} ${item.unitOfMeasurement})"
                        }
                        onSave(item)
                        val effTarget = item.storageLocation.ifBlank { targetUnit }.ifBlank { "General Farm" }
                        onRequestRecordExpense?.invoke(financeCat, 0.0, expDesc, item.purchaseDate.ifBlank { today() }, effTarget)
                        showExpensePrompt = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onSave(item)
                        showExpensePrompt = false
                    }
                ) {
                    Text("No")
                }
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(18.dp).fillMaxWidth()) {
                Text(if (isEdit) "Edit Inventory Item" else if (isRestock) "Restock: ${source?.itemName}" else "Add Inventory Item", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                if (isRestock && prefillTemplate != null) {
                    Text(
                        "Current in-stock: ${prefillTemplate.quantityAvailable} ${prefillTemplate.unitOfMeasurement}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ForestGreenPrimary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Text("Enter the new quantity to add to this item.", fontSize = 12.sp, color = Color.Gray)
                } else {
                    Text("Record quantity in stock, target flock/unit, threshold, and batch details.", fontSize = 12.sp, color = Color.Gray)
                }
                Input(itemName, { itemName = it }, "Item Name *")
                Box {
                    OutlinedButton(onClick = { if (!isRestock) categoryMenu = true }, modifier = Modifier.fillMaxWidth()) { Text("Category: $category") }
                    DropdownMenu(expanded = categoryMenu, onDismissRequest = { categoryMenu = false }) {
                        listOf("Seeds", "Fertilizers", "Pesticides", "Tools", "Feed", "Harvested Crops", "Silage", "Other").forEach { choice ->
                            DropdownMenuItem(text = { Text(choice) }, onClick = {
                                category = choice
                                if (choice.equals("Silage", ignoreCase = true)) {
                                    unit = "kgs"
                                } else if (choice.equals("Feed", ignoreCase = true) && (targetUnit == "General Farm" || targetUnit.isBlank())) {
                                    targetUnit = activeFlocks.firstOrNull() ?: "Poultry"
                                }
                                categoryMenu = false
                            })
                        }
                    }
                }
                Box(Modifier.padding(top = 4.dp)) {
                    OutlinedButton(onClick = { targetUnitMenu = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Target Enterprise / Flock: $targetUnit")
                    }
                    DropdownMenu(expanded = targetUnitMenu, onDismissRequest = { targetUnitMenu = false }) {
                        DropdownMenuItem(text = { Text("🏠 General Farm") }, onClick = { targetUnit = "General Farm"; targetUnitMenu = false })
                        DropdownMenuItem(text = { Text("🐔 Poultry (All Flocks)") }, onClick = { targetUnit = "Poultry"; targetUnitMenu = false })
                        activeFlocks.forEach { flockName ->
                            DropdownMenuItem(text = { Text("🐔 $flockName") }, onClick = { targetUnit = flockName; targetUnitMenu = false })
                        }
                        DropdownMenuItem(text = { Text("🐄 Cattle") }, onClick = { targetUnit = "Cattle"; targetUnitMenu = false })
                        DropdownMenuItem(text = { Text("🌾 Crops / Fields") }, onClick = { targetUnit = "Crops / Fields"; targetUnitMenu = false })
                    }
                }
                Input(description, { description = it }, "Description")
                Row {
                    Input(quantity, { quantity = it }, if (isRestock) "Quantity to Add *" else "Quantity *", Modifier.weight(1f), KeyboardType.Decimal)
                    Spacer(Modifier.width(8.dp))
                    Input(unit, { unit = it }, "Unit", Modifier.weight(1f))
                }
                Input(minimum, { minimum = it }, "Minimum Threshold", keyboard = KeyboardType.Decimal)
                Input(batch, { batch = it }, "Batch or Lot Number")
                Input(purchaseDate, { purchaseDate = it }, "Purchase / Received Date")
                Input(expiryDate, { expiryDate = it }, "Expiration Date (optional)")
                Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(
                        onClick = {
                            val parsedQuantity = quantity.toDoubleOrNull() ?: 0.0
                            if (itemName.isNotBlank() && parsedQuantity > 0.0) {
                                val silage = category.equals("Silage", ignoreCase = true)
                                val finalQuantity = if (isRestock) {
                                    (prefillTemplate?.quantityAvailable ?: 0.0) + parsedQuantity
                                } else {
                                    parsedQuantity
                                }
                                val base = existing ?: prefillTemplate ?: InventoryItem(itemName = itemName, category = category)
                                val itemToSave = base.copy(
                                    itemName = itemName.trim(),
                                    category = category,
                                    skuOrBarcode = "",
                                    description = description.trim(),
                                    quantityAvailable = finalQuantity,
                                    unitOfMeasurement = if (silage) "kgs" else unit.ifBlank { "kg" },
                                    minimumThreshold = minimum.toDoubleOrNull() ?: 0.0,
                                    storageLocation = targetUnit.ifBlank { "General Farm" },
                                    batchOrLotNumber = batch.trim(),
                                    purchaseDate = purchaseDate,
                                    expirationDate = expiryDate,
                                    unitCost = 0.0,
                                    isSilage = silage
                                )
                                if (!isEdit && onRequestRecordExpense != null) {
                                    pendingItemToSave = itemToSave
                                    pendingRestockQty = parsedQuantity
                                    showExpensePrompt = true
                                } else {
                                    onSave(itemToSave)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                    ) { Text(if (isEdit) "Save Changes" else if (isRestock) "Confirm Restock" else "Add Item") }
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
private fun HarvestDialog(
    field: FieldPlan,
    inventoryItems: List<InventoryItem>,
    onDismiss: () -> Unit,
    onSave: (outcome: String, quantityKg: Double, sale: Double, date: String, targetPitId: Long?, targetPitName: String?) -> Unit
) {
    var outcome by remember { mutableStateOf("SILAGE") }
    var quantityKg by remember { mutableStateOf("") }
    var sale by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(today()) }

    val availableSilagePits = remember(inventoryItems) {
        inventoryItems.filter {
            it.isSilage ||
                it.category.equals("Silage", ignoreCase = true) ||
                it.itemName.contains("silage", ignoreCase = true) ||
                it.itemName.contains("pit", ignoreCase = true)
        }
    }

    var selectedPit by remember(availableSilagePits) {
        mutableStateOf(availableSilagePits.firstOrNull())
    }
    var isCreatingNewPit by remember(availableSilagePits) {
        mutableStateOf(availableSilagePits.isEmpty())
    }
    var newPitName by remember {
        mutableStateOf(if (availableSilagePits.isEmpty()) "Silage Pit 1" else "")
    }
    var pitMenuExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(18.dp).fillMaxWidth()) {
                Text("Harvest ${field.fieldName}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Choose Silage to store chopped fodder into a silage pit in inventory, or Sold to record crop-sale income.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                )

                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
                    FilterChip(
                        selected = outcome == "SILAGE",
                        onClick = { outcome = "SILAGE" },
                        label = { Text("Chop as Silage (kgs)") },
                        leadingIcon = {
                            Icon(Icons.Filled.Agriculture, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                    FilterChip(
                        selected = outcome == "SOLD",
                        onClick = { outcome = "SOLD" },
                        label = { Text("Sold") }
                    )
                }

                if (outcome == "SILAGE") {
                    Text(
                        "Silage Pit / Bunker *",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155),
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                    )

                    if (availableSilagePits.isNotEmpty() && !isCreatingNewPit) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { pitMenuExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = selectedPit?.itemName ?: "Select Silage Pit",
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = "Available: ${(selectedPit?.quantityAvailable ?: 0.0).toInt()} ${selectedPit?.unitOfMeasurement?.ifBlank { "kgs" } ?: "kgs"}",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Dropdown")
                                }
                            }

                            DropdownMenu(
                                expanded = pitMenuExpanded,
                                onDismissRequest = { pitMenuExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.85f)
                            ) {
                                availableSilagePits.forEach { pit ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(pit.itemName, fontWeight = FontWeight.SemiBold)
                                                Text(
                                                    "${pit.quantityAvailable.toInt()} ${pit.unitOfMeasurement.ifBlank { "kgs" }} in stock",
                                                    fontSize = 11.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedPit = pit
                                            isCreatingNewPit = false
                                            pitMenuExpanded = false
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Filled.Inventory2, contentDescription = null, tint = Color(0xFF059669))
                                        }
                                    )
                                }
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = {
                                        Text("+ Create New Silage Pit...", color = Color(0xFF059669), fontWeight = FontWeight.Bold)
                                    },
                                    onClick = {
                                        isCreatingNewPit = true
                                        if (newPitName.isBlank()) {
                                            newPitName = "Silage Pit ${availableSilagePits.size + 1}"
                                        }
                                        pitMenuExpanded = false
                                    }
                                )
                            }
                        }
                    } else {
                        // Creating new pit or no existing pits found
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Input(
                                value = newPitName,
                                onValueChange = { newPitName = it },
                                label = "New Silage Pit Name *"
                            )
                            if (availableSilagePits.isNotEmpty()) {
                                TextButton(
                                    onClick = {
                                        isCreatingNewPit = false
                                        if (selectedPit == null) selectedPit = availableSilagePits.firstOrNull()
                                    },
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text("← Choose existing pit from inventory", fontSize = 12.sp)
                                }
                            } else {
                                Text(
                                    "No silage pit in inventory yet. A new pit item will be created automatically.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF059669),
                                    modifier = Modifier.padding(top = 2.dp, start = 4.dp)
                                )
                            }
                        }
                    }

                    // Show stock preview if pit selected
                    if (!isCreatingNewPit && selectedPit != null) {
                        val curStock = selectedPit?.quantityAvailable ?: 0.0
                        val harvestQty = quantityKg.toDoubleOrNull() ?: 0.0
                        if (harvestQty > 0.0) {
                            Surface(
                                color = Color(0xFFF0FDF4),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "${selectedPit?.itemName}: ${curStock.toInt()} kgs + ${harvestQty.toInt()} kgs = ${(curStock + harvestQty).toInt()} kgs total",
                                        fontSize = 12.sp,
                                        color = Color(0xFF166534),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                Input(quantityKg, { quantityKg = it }, if (outcome == "SILAGE") "Harvested Silage (kgs) *" else "Harvested Quantity (kgs) *", keyboard = KeyboardType.Decimal)
                if (outcome == "SOLD") Input(sale, { sale = it }, "Total Sale Amount", keyboard = KeyboardType.Decimal)
                Input(date, { date = it }, "Harvest Date")

                Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    val canConfirm = (quantityKg.toDoubleOrNull() ?: 0.0) > 0.0 &&
                        (outcome != "SILAGE" || (!isCreatingNewPit && selectedPit != null) || (isCreatingNewPit && newPitName.isNotBlank()))
                    Button(
                        onClick = {
                            val harvested = quantityKg.toDoubleOrNull() ?: 0.0
                            if (harvested > 0.0) {
                                val targetPitId = if (outcome == "SILAGE" && !isCreatingNewPit) selectedPit?.id else null
                                val targetPitName = if (outcome == "SILAGE" && isCreatingNewPit) newPitName.trim() else null
                                onSave(outcome, harvested, if (outcome == "SOLD") sale.toDoubleOrNull() ?: 0.0 else 0.0, date, targetPitId, targetPitName)
                            }
                        },
                        enabled = canConfirm,
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
