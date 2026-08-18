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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddFinanceRecordDialog(
    onDismiss: () -> Unit,
    onSaveRecord: (type: FinanceType, category: String, amount: Double, description: String) -> Unit,
    onUpdateRecord: ((FinanceRecord) -> Unit)? = null,
    existing: FinanceRecord? = null
) {
    var selectedType by remember { mutableStateOf(existing?.type ?: FinanceType.INCOME) }
    val incomeCategories = listOf("Milk Sale", "Egg Sale", "Crop Harvest Sale", "Cattle Sale", "Other Income")
    val expenseCategories = listOf("Feeds & Supplies", "Vaccines & Vet", "Equipment & Repairs", "Other Expense")
    var categoryOptions by remember { mutableStateOf(if (selectedType == FinanceType.INCOME) incomeCategories else expenseCategories) }

    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(existing?.category ?: categoryOptions.first()) }
    var amountText by remember { mutableStateOf(existing?.amount?.toString() ?: (existing?.amount?.toString() ?: "5000")) }
    var descriptionText by remember { mutableStateOf(existing?.description ?: "") }
    var transactionDate by remember {
        mutableStateOf(existing?.date ?: SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()))
    }

    // Update category options when type toggles
    androidx.compose.runtime.LaunchedEffect(selectedType) {
        categoryOptions = if (selectedType == FinanceType.INCOME) incomeCategories else expenseCategories
        if (!categoryOptions.contains(selectedCategory)) selectedCategory = categoryOptions.first()
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
                    text = "Log farm financial transactions and ledger records",
                    fontSize = 12.sp,
                    color = Color(0xFF49454F)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Date Picker Field (keeps existing AppDatePickerField usage)
                AppDatePickerField(
                    value = transactionDate,
                    onValueChange = { transactionDate = it },
                    label = "Transaction Date",
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "finance_transaction_date_picker"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Type Toggle
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedType == FinanceType.INCOME,
                        onClick = {
                            selectedType = FinanceType.INCOME
                            selectedCategory = incomeCategories.first()
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
                            selectedCategory = expenseCategories.first()
                        },
                        label = { Text("🔴 Expense (Cost)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFEE2E2),
                            selectedLabelColor = Color(0xFF991B1B)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Category",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF49454F)
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Dropdown style category selector
                Box {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = true },
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) }
                    )
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        (if (selectedType == FinanceType.INCOME) incomeCategories else expenseCategories).forEach { cat ->
                            DropdownMenuItem(text = { Text(cat) }, onClick = {
                                selectedCategory = cat
                                expanded = false
                            })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (KSh / USD)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = descriptionText,
                    onValueChange = { descriptionText = it },
                    label = { Text("Description / Receipt Notes") },
                    placeholder = { Text("e.g. 50kg layer mash from Agro-vet") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

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
                            val finalDescription = if (descriptionText.isNotBlank()) "[$transactionDate] $descriptionText" else "[$transactionDate]"
                            if (existing != null && onUpdateRecord != null) {
                                onUpdateRecord(existing.copy(
                                    type = selectedType,
                                    category = selectedCategory,
                                    amount = amt,
                                    description = finalDescription,
                                    date = transactionDate
                                ))
                            } else {
                                onSaveRecord(selectedType, selectedCategory, amt, finalDescription)
                            }
                            onDismiss()
                        },
                        shape = RoundedCornerShape(100.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedType == FinanceType.INCOME) Color(0xFF166534) else Color(0xFFB3261E)
                        )
                    ) {
                        Text(if (existing != null) "Update Transaction" else "Save Transaction", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
