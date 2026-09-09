package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.FinanceRecord
import com.example.data.FinanceType
import com.example.data.FarmUnit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddFinanceRecordDialog(
    onDismiss: () -> Unit,
    onSaveRecord: (type: FinanceType, category: String, amount: Double, description: String) -> Unit,
    onUpdateRecord: ((FinanceRecord) -> Unit)? = null,
    existing: FinanceRecord? = null,
    initialType: FinanceType = FinanceType.INCOME,
    initialCategory: String? = null,
    initialAmount: Double? = null,
    initialDescription: String? = null,
    initialDate: String? = null,
    initialTargetUnit: String? = null,
    units: List<FarmUnit> = emptyList(),
    userRole: String = "OWNER",
    canEditPastDaysLogs: Boolean = true,
    onSaveRecordWithDate: ((type: FinanceType, category: String, amount: Double, description: String, date: String) -> Unit)? = null,
    onSaveRecordFull: ((type: FinanceType, category: String, amount: Double, description: String, date: String, targetUnit: String) -> Unit)? = null
) {
    val isOwner = userRole.equals("OWNER", ignoreCase = true)
    val cannotEditPast = !isOwner && !canEditPastDaysLogs
    val effectiveType = existing?.type ?: initialType
    var selectedType by remember { mutableStateOf(effectiveType) }
    val incomeCategories = listOf(
        "Egg Sales",
        "Poultry Meat Sales",
        "Milk Sales",
        "Cattle Sales",
        "Crop Harvest Sales",
        "Manure / Fertilizer Sales",
        "Other Income"
    )
    val expenseCategories = listOf(
        "Poultry Feed & Nutrition",
        "Cattle Feed & Nutrition",
        "Vaccines & Vet",
        "Chick & Poultry Restock",
        "Cattle Restock",
        "Bedding & Disinfection",
        "Equipment & Repairs",
        "Labor & Wages",
        "Utilities & Transport",
        "Other Expense"
    )
    var categoryOptions by remember { mutableStateOf(if (selectedType == FinanceType.INCOME) incomeCategories else expenseCategories) }

    var expandedCategory by remember { mutableStateOf(false) }
    var selectedCategory by remember {
        mutableStateOf(
            existing?.category
                ?: initialCategory?.takeIf { categoryOptions.contains(it) }
                ?: categoryOptions.first()
        )
    }

    // Target Enterprise / Unit resolution
    var selectedTargetUnit by remember {
        mutableStateOf(
            existing?.targetUnit
                ?: initialTargetUnit?.takeIf { it.isNotBlank() }
                ?: "General Farm"
        )
    }
    var expandedTargetMenu by remember { mutableStateOf(false) }

    val activeFlocks = remember(units) {
        units.filter { it.type.equals("Poultry", ignoreCase = true) }.map { it.name }
    }
    val activeCattleUnits = remember(units) {
        units.filter { it.type.equals("Cattle", ignoreCase = true) }.map { it.name }
    }
    val otherUnits = remember(units) {
        units.filter { !it.type.equals("Poultry", ignoreCase = true) && !it.type.equals("Cattle", ignoreCase = true) }.map { it.name }
    }

    var amountText by remember {
        mutableStateOf(
            existing?.amount?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() }
                ?: initialAmount?.takeIf { it > 0.0 }?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() }
                ?: ""
        )
    }
    var descriptionText by remember { mutableStateOf(existing?.description ?: initialDescription ?: "") }
    var transactionDate by remember {
        mutableStateOf(
            existing?.date
                ?: initialDate?.takeIf { it.isNotBlank() }
                ?: SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        )
    }
    val transactionDateIsPastRestricted = cannotEditPast && com.example.util.DateValidationUtils.isPastDate(transactionDate)

    // Update category options when type toggles
    androidx.compose.runtime.LaunchedEffect(selectedType) {
        categoryOptions = if (selectedType == FinanceType.INCOME) incomeCategories else expenseCategories
        if (!categoryOptions.contains(selectedCategory)) selectedCategory = categoryOptions.first()
    }

    // Smart default suggestion for target unit based on category
    fun onCategoryChosen(cat: String) {
        selectedCategory = cat
        val lowerCat = cat.lowercase()
        when {
            lowerCat.contains("milk") || lowerCat.contains("cattle") || lowerCat.contains("dairy") -> {
                if (selectedTargetUnit == "General Farm" || selectedTargetUnit.isBlank()) {
                    selectedTargetUnit = "Cattle"
                }
            }
            lowerCat.contains("egg") || lowerCat.contains("poultry") || lowerCat.contains("chick") || lowerCat.contains("layer") || lowerCat.contains("broiler") || lowerCat.contains("flock") -> {
                if (selectedTargetUnit == "General Farm" || selectedTargetUnit.isBlank()) {
                    selectedTargetUnit = activeFlocks.firstOrNull() ?: "Poultry"
                }
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("add_finance_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = if (existing != null) "✏️ Edit Transaction" else "💵 Record Income or Expense",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1B1F)
                )
                Text(
                    text = "Log farm financial transactions categorized by enterprise or flock",
                    fontSize = 12.sp,
                    color = Color(0xFF49454F)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Date Picker Field
                AppDatePickerField(
                    value = transactionDate,
                    onValueChange = { transactionDate = it },
                    label = "Transaction Date",
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "finance_transaction_date_picker"
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Type Toggle
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedType == FinanceType.INCOME,
                        onClick = {
                            selectedType = FinanceType.INCOME
                            onCategoryChosen(incomeCategories.first())
                        },
                        label = { Text("🟢 Income (Revenue)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFDCFCE7),
                            selectedLabelColor = Color(0xFF166534)
                        )
                    )
                    FilterChip(
                        selected = selectedType == FinanceType.EXPENSE,
                        onClick = {
                            selectedType = FinanceType.EXPENSE
                            onCategoryChosen(expenseCategories.first())
                        },
                        label = { Text("🔴 Expense (Cost)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFEE2E2),
                            selectedLabelColor = Color(0xFF991B1B)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Enterprise / Flock / Cattle Target Section
                Text(
                    text = "Spent On / Earned From (Enterprise / Unit)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF49454F)
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Quick selector chips for Target Enterprise
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedTargetUnit.equals("General Farm", ignoreCase = true),
                            onClick = { selectedTargetUnit = "General Farm" },
                            label = { Text("🚜 General Farm", fontSize = 11.5.sp) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedTargetUnit.equals("Cattle", ignoreCase = true) || selectedTargetUnit.equals("All Cattle", ignoreCase = true),
                            onClick = { selectedTargetUnit = "Cattle" },
                            label = { Text("🐄 Cattle", fontSize = 11.5.sp) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedTargetUnit.equals("Poultry", ignoreCase = true) || selectedTargetUnit.equals("All Poultry", ignoreCase = true),
                            onClick = { selectedTargetUnit = "Poultry" },
                            label = { Text("🐔 Poultry", fontSize = 11.5.sp) }
                        )
                    }
                    if (activeFlocks.isNotEmpty()) {
                        items(activeFlocks) { flockName ->
                            FilterChip(
                                selected = selectedTargetUnit.equals(flockName, ignoreCase = true),
                                onClick = { selectedTargetUnit = flockName },
                                label = { Text("🐔 $flockName", fontSize = 11.5.sp) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Dropdown / Custom field for target unit
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedTargetUnit,
                        onValueChange = { selectedTargetUnit = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Target Enterprise / Unit") },
                        placeholder = { Text("e.g. Flock A, Cattle, Field 1") },
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            IconButton(onClick = { expandedTargetMenu = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Enterprise")
                            }
                        }
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { expandedTargetMenu = true }
                    )
                    DropdownMenu(
                        expanded = expandedTargetMenu,
                        onDismissRequest = { expandedTargetMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("🚜 General Farm") },
                            onClick = {
                                selectedTargetUnit = "General Farm"
                                expandedTargetMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("🐄 Cattle (Dairy & Beef)") },
                            onClick = {
                                selectedTargetUnit = "Cattle"
                                expandedTargetMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("🐔 Poultry (All Flocks)") },
                            onClick = {
                                selectedTargetUnit = "Poultry"
                                expandedTargetMenu = false
                            }
                        )
                        if (activeFlocks.isNotEmpty()) {
                            activeFlocks.forEach { flock ->
                                DropdownMenuItem(
                                    text = { Text("🐔 $flock") },
                                    onClick = {
                                        selectedTargetUnit = flock
                                        expandedTargetMenu = false
                                    }
                                )
                            }
                        }
                        if (activeCattleUnits.isNotEmpty()) {
                            activeCattleUnits.forEach { cow ->
                                DropdownMenuItem(
                                    text = { Text("🐄 $cow") },
                                    onClick = {
                                        selectedTargetUnit = cow
                                        expandedTargetMenu = false
                                    }
                                )
                            }
                        }
                        if (otherUnits.isNotEmpty()) {
                            otherUnits.forEach { u ->
                                DropdownMenuItem(
                                    text = { Text("🌾 $u") },
                                    onClick = {
                                        selectedTargetUnit = u
                                        expandedTargetMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Category",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF49454F)
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Dropdown style category selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        label = { Text("Category") },
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            IconButton(onClick = { expandedCategory = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Category")
                            }
                        }
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { expandedCategory = true }
                    )
                    DropdownMenu(expanded = expandedCategory, onDismissRequest = { expandedCategory = false }) {
                        categoryOptions.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A)) },
                                onClick = {
                                    onCategoryChosen(cat)
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (KSh / USD)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = descriptionText,
                    onValueChange = { descriptionText = it },
                    label = { Text("Description / Receipt Notes") },
                    placeholder = { Text("e.g. 50kg layer mash from Agro-vet") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (transactionDateIsPastRestricted) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Recording or modifying transactions for previous days is disabled for worker accounts. Please select today's date.",
                        color = Color(0xFFB91C1C),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(100.dp)) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            val finalDescription = descriptionText.ifBlank { "Recorded on $transactionDate" }
                            val finalTarget = selectedTargetUnit.ifBlank { "General Farm" }
                            if (amt <= 0.0 || transactionDateIsPastRestricted) return@Button
                            if (existing != null && onUpdateRecord != null) {
                                onUpdateRecord(existing.copy(
                                    type = selectedType,
                                    category = selectedCategory,
                                    amount = amt,
                                    description = finalDescription,
                                    date = transactionDate,
                                    targetUnit = finalTarget
                                ))
                            } else if (onSaveRecordFull != null) {
                                onSaveRecordFull(selectedType, selectedCategory, amt, finalDescription, transactionDate, finalTarget)
                            } else if (onSaveRecordWithDate != null) {
                                onSaveRecordWithDate(selectedType, selectedCategory, amt, finalDescription, transactionDate)
                            } else {
                                onSaveRecord(selectedType, selectedCategory, amt, finalDescription)
                            }
                            onDismiss()
                        },
                        shape = RoundedCornerShape(100.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedType == FinanceType.INCOME) Color(0xFF166534) else Color(0xFFB3261E),
                            disabledContainerColor = Color(0xFF94A3B8)
                        ),
                        enabled = !transactionDateIsPastRestricted && (amountText.toDoubleOrNull() ?: 0.0) > 0.0
                    ) {
                        Text(
                            text = when {
                                transactionDateIsPastRestricted -> "Previous Days Locked"
                                existing != null -> "Update Transaction"
                                else -> "Save Transaction"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
