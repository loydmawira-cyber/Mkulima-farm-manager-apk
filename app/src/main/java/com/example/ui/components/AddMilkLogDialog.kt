package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.FarmUnit
import com.example.ui.screens.isMilkingCow
import com.example.ui.screens.mockAnimals
import com.example.ui.theme.ForestGreenPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddMilkLogDialog(
    availableUnits: List<FarmUnit>,
    onDismiss: () -> Unit,
    onSaveMilkLog: (
        cowName: String,
        unitName: String,
        litres: Double,
        session: String,
        fatPercentage: Double,
        date: String,
        notes: String?
    ) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val deletedPrefs = remember { context.getSharedPreferences("mkulima_deleted_animals", android.content.Context.MODE_PRIVATE) }
    val deletedSet = remember {
        try {
            deletedPrefs.getStringSet("deleted_ids", emptySet()) ?: emptySet()
        } catch (e: Exception) {
            val raw = try { deletedPrefs.getString("deleted_ids", "") ?: "" } catch (ex: Exception) { "" }
            if (raw.isNotBlank()) raw.split(",").toSet() else emptySet()
        }
    }

    val milkingCows = remember(availableUnits, deletedSet) {
        val result = mutableListOf<String>()

        // 1. From Room units (registered farm livestock)
        availableUnits.filter {
            (it.type.equals("Cattle", ignoreCase = true) || it.type.equals("CATTLE", ignoreCase = true)) &&
            !deletedSet.contains("unit_${it.id}") && !deletedSet.contains(it.name.lowercase()) &&
            isMilkingCow(name = it.name, breed = it.breed, status = it.healthStatus, tag = it.tagNumber)
        }.forEach { unit ->
            val tag = unit.tagNumber.ifBlank { "#${unit.id + 100}" }
            val breed = unit.breed.ifBlank { "Dairy Cow" }
            result.add("${unit.name} ($tag - $breed)")
        }

        // 2. From mockAnimals (registered farm livestock list)
        mockAnimals.filter {
            it.category.equals("CATTLE", ignoreCase = true) &&
            !deletedSet.contains(it.id) && !deletedSet.contains(it.name.lowercase()) &&
            isMilkingCow(
                name = it.name,
                breed = it.breed,
                status = it.status,
                tag = it.tagNumber,
                lastMilk = it.lastMilk,
                breedingStatus = it.breedingStatus
            )
        }.forEach { animal ->
            val tag = animal.tagNumber.ifBlank { "#100" }
            val breed = animal.breed.ifBlank { "Dairy Cow" }
            result.add("${animal.name} ($tag - $breed)")
        }

        result.add("Overall Herd Bulk Yield")
        result.distinct()
    }

    var selectedCowName by remember(milkingCows) { mutableStateOf(milkingCows.firstOrNull() ?: "Overall Herd Bulk Yield") }
    var cowDropdownExpanded by remember { mutableStateOf(false) }

    val dairyUnits = availableUnits.filter { it.type.contains("Cattle", ignoreCase = true) || it.name.contains("Dairy", ignoreCase = true) }
    var selectedUnitName by remember { mutableStateOf(dairyUnits.firstOrNull()?.name ?: "Dairy Herd - Friesians") }

    var litresText by remember { mutableStateOf("15.5") }
    var fatText by remember { mutableStateOf("3.8") }
    var selectedSession by remember { mutableStateOf("Morning") }
    var selectedDestination by remember { mutableStateOf("Cooperative Sale") }

    val defaultDate = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
    }
    var dateText by remember { mutableStateOf(defaultDate) }
    var notesText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("add_milk_log_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFDCFCE7)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.WaterDrop,
                                contentDescription = null,
                                tint = ForestGreenPrimary,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Log Milk Production",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = "Record collection volume & session details",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Cow Selector
                Text(
                    text = "SELECT COW OR HERD BULK",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedCowName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                Icons.Filled.ArrowDropDown,
                                contentDescription = "Select Cow",
                                modifier = Modifier.clickable { cowDropdownExpanded = true }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { cowDropdownExpanded = true },
                        shape = RoundedCornerShape(12.dp)
                    )
                    DropdownMenu(
                        expanded = cowDropdownExpanded,
                        onDismissRequest = { cowDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.82f)
                    ) {
                        milkingCows.forEach { cow ->
                            DropdownMenuItem(
                                text = { Text(cow, fontWeight = FontWeight.Medium) },
                                onClick = {
                                    selectedCowName = cow
                                    cowDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Session Selector (Morning, Afternoon, Evening)
                Text(
                    text = "MILKING SESSION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(6.dp))

                val sessions = listOf(
                    Triple("Morning", "🌅 Morning", Icons.Filled.WbSunny),
                    Triple("Afternoon", "☀️ Midday", Icons.Filled.WbCloudy),
                    Triple("Evening", "🌙 Evening", Icons.Filled.NightsStay)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    sessions.forEach { (sessKey, sessLabel, icon) ->
                        val isSelected = selectedSession.equals(sessKey, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) ForestGreenPrimary else Color.Transparent)
                                .clickable { selectedSession = sessKey },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else Color(0xFF64748B),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = sessLabel,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color(0xFF475569)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quantity & Fat % Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = litresText,
                        onValueChange = { litresText = it },
                        label = { Text("Quantity (Litres)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = fatText,
                        onValueChange = { fatText = it },
                        label = { Text("Fat %") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Date & Dairy Unit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AppDatePickerField(
                        value = dateText,
                        onValueChange = { dateText = it },
                        label = "Collection Date",
                        modifier = Modifier.weight(1f),
                        testTag = "milk_log_date_picker"
                    )

                    OutlinedTextField(
                        value = selectedUnitName,
                        onValueChange = { selectedUnitName = it },
                        label = { Text("Dairy Shed / Unit") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Milk Destination / Utilization
                Text(
                    text = "MILK DESTINATION / USE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(6.dp))

                val destinations = listOf("Cooperative Sale", "Calf Feeding", "Home / Staff", "Chilled Tank")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    destinations.forEach { dest ->
                        val isSelected = selectedDestination == dest
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(0xFFDCFCE7) else Color(0xFFF1F5F9),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) ForestGreenPrimary else Color.Transparent
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedDestination = dest }
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dest.split(" ").first(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) ForestGreenPrimary else Color(0xFF475569)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Notes / Quality / Chilling Temp (°C)") },
                    placeholder = { Text("e.g. Chilled to 4°C, Delivered to Co-op") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Text("Cancel", color = Color(0xFF475569))
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            val litres = litresText.toDoubleOrNull() ?: 0.0
                            val fat = fatText.toDoubleOrNull() ?: 3.8
                            val fullNote = buildString {
                                append("Use: $selectedDestination")
                                if (notesText.isNotBlank()) {
                                    append(" • $notesText")
                                }
                            }
                            onSaveMilkLog(
                                selectedCowName,
                                selectedUnitName,
                                litres,
                                selectedSession,
                                fat,
                                dateText,
                                fullNote
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                    ) {
                        Text("SAVE MILK LOG", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
