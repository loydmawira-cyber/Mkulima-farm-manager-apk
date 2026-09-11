package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.IncubationBatch
import java.text.SimpleDateFormat
import java.util.*

private val ForestGreenPrimary = Color(0xFF166534)

@Composable
fun IncubationBatchCard(
    batch: IncubationBatch,
    onMoveToFlock: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dateSetTime = try { dateFormat.parse(batch.dateSet)?.time ?: System.currentTimeMillis() } catch (_: Exception) { System.currentTimeMillis() }
    val daysElapsed = ((System.currentTimeMillis() - dateSetTime) / (1000L * 60L * 60L * 24L)).toInt().coerceAtLeast(0)

    val reminderText = when {
        batch.status == "HATCHED" -> "Hatched! Moved to: ${batch.movedToFlockName.ifBlank { "Active Flock" }} (${batch.hatchedCount} chicks)"
        daysElapsed < 7 -> "Day $daysElapsed/21 • Candling recommended around Day 7"
        daysElapsed in 7..17 -> "Day $daysElapsed/21 • Regular Incubation & Turning"
        daysElapsed in 18..20 -> "Day $daysElapsed/21 • Lockdown (Stop turning, increase humidity!)"
        else -> "Day $daysElapsed/21 • Hatching expected / Ready for transfer!"
    }

    val badgeColor = when (batch.status) {
        "HATCHED" -> Color(0xFF10B981)
        "FAILED" -> Color(0xFFEF4444)
        else -> Color(0xFFD97706)
    }

    var showMenu by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { showMenu = true }
                    )
                },
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFEF3C7),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Egg, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(22.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(batch.batchName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
                            Text("${batch.breed} • Incubator: ${batch.incubatorName}", fontSize = 12.sp, color = Color(0xFF64748B))
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = badgeColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = batch.status,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeColor
                            )
                        }
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Filled.MoreVert,
                                contentDescription = "More Options",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFF1F5F9))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Eggs Set", fontSize = 11.sp, color = Color(0xFF64748B))
                        Text("${batch.eggsSet}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                    }
                    Column {
                        Text("Hatched Chicks", fontSize = 11.sp, color = Color(0xFF64748B))
                        val hatchedText = if (batch.status == "HATCHED" || batch.hatchedCount > 0) "${batch.hatchedCount}" else "Pending"
                        Text(hatchedText, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (hatchedText != "Pending") ForestGreenPrimary else Color(0xFF0F172A))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Hatch Rate", fontSize = 11.sp, color = Color(0xFF64748B))
                        val hatchRateVal = if (batch.eggsSet > 0 && (batch.status == "HATCHED" || batch.hatchedCount > 0)) {
                            String.format(Locale.getDefault(), "%.1f%%", (batch.hatchedCount.toDouble() / batch.eggsSet.toDouble()) * 100)
                        } else if (batch.eggsSet > 0 && batch.fertileEggsCount > 0) {
                            String.format(Locale.getDefault(), "%.1f%% (Est)", (batch.fertileEggsCount.toDouble() / batch.eggsSet.toDouble()) * 100)
                        } else {
                            "Pending"
                        }
                        val rateColor = when {
                            hatchRateVal.contains("%") -> {
                                val num = hatchRateVal.replace("%", "").replace(" (Est)", "").toDoubleOrNull() ?: 0.0
                                if (num >= 75.0) ForestGreenPrimary else if (num >= 50.0) Color(0xFFD97706) else Color(0xFFDC2626)
                            }
                            else -> Color(0xFF64748B)
                        }
                        Text(hatchRateVal, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = rateColor)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Set: ${batch.dateSet}", fontSize = 11.sp, color = Color(0xFF64748B))
                    Text("Hatch: ${batch.expectedHatchDate}", fontSize = 11.sp, color = Color(0xFF64748B))
                }

                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFFFBEB),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Notifications, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(reminderText, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF92400E))
                    }
                }

                if (batch.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Notes: ${batch.notes}", fontSize = 12.sp, color = Color(0xFF475569))
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Press & hold for Edit / Delete",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        fontStyle = FontStyle.Italic
                    )
                    if (batch.status == "INCUBATING") {
                        Button(
                            onClick = onMoveToFlock,
                            modifier = Modifier.height(36.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                        ) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Move to Flock", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier.background(Color.White)
        ) {
            DropdownMenuItem(
                text = { Text("Edit Batch", fontWeight = FontWeight.Medium, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null, tint = Color(0xFF475569)) },
                onClick = {
                    showMenu = false
                    onEdit()
                }
            )
            HorizontalDivider(color = Color(0xFFF1F5F9))
            DropdownMenuItem(
                text = { Text("Delete Batch", color = Color(0xFFDC2626), fontWeight = FontWeight.Medium, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null, tint = Color(0xFFDC2626)) },
                onClick = {
                    showMenu = false
                    onDelete()
                }
            )
        }
    }
}

@Composable
fun IncubationBatchFormDialog(
    initialBatch: IncubationBatch? = null,
    onDismiss: () -> Unit,
    onSave: (IncubationBatch) -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayStr = dateFormat.format(Date())

    var batchName by remember { mutableStateOf(initialBatch?.batchName ?: "") }
    var breed by remember { mutableStateOf(initialBatch?.breed ?: "Layers / Broilers") }
    var eggsSetStr by remember { mutableStateOf(initialBatch?.eggsSet?.toString() ?: "50") }
    var fertileEggsStr by remember { mutableStateOf(initialBatch?.fertileEggsCount?.takeIf { it > 0 }?.toString() ?: "") }
    var hatchedCountStr by remember { mutableStateOf(initialBatch?.hatchedCount?.takeIf { it > 0 }?.toString() ?: "") }
    var dateSet by remember { mutableStateOf(initialBatch?.dateSet?.ifBlank { todayStr } ?: todayStr) }
    var incubatorName by remember { mutableStateOf(initialBatch?.incubatorName ?: "Main Incubator") }
    var notes by remember { mutableStateOf(initialBatch?.notes ?: "") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialBatch == null) "New Incubation Batch" else "Edit Incubation Batch",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF0F172A)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (showError) {
                    Text("Please enter a valid batch name and egg count.", color = Color(0xFFDC2626), fontSize = 12.sp)
                }
                OutlinedTextField(
                    value = batchName,
                    onValueChange = { batchName = it },
                    label = { Text("Batch Name (e.g. Batch #1)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = breed,
                    onValueChange = { breed = it },
                    label = { Text("Breed / Poultry Type") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = eggsSetStr,
                    onValueChange = { eggsSetStr = it },
                    label = { Text("Number of Eggs Set") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = fertileEggsStr,
                    onValueChange = { fertileEggsStr = it },
                    label = { Text("Fertile Eggs (from Candling - Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = hatchedCountStr,
                    onValueChange = { hatchedCountStr = it },
                    label = { Text("Hatched Chicks Count (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = dateSet,
                    onValueChange = { dateSet = it },
                    label = { Text("Date Set (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = incubatorName,
                    onValueChange = { incubatorName = it },
                    label = { Text("Incubator Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Source") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val count = eggsSetStr.toIntOrNull() ?: 0
                    val fertile = fertileEggsStr.toIntOrNull() ?: 0
                    val hatched = hatchedCountStr.toIntOrNull() ?: 0
                    if (batchName.isBlank() || count <= 0) {
                        showError = true
                    } else {
                        val cal = Calendar.getInstance()
                        try {
                            cal.time = dateFormat.parse(dateSet) ?: Date()
                        } catch (_: Exception) {
                            cal.time = Date()
                        }
                        cal.add(Calendar.DAY_OF_YEAR, 21)
                        val expectedHatch = dateFormat.format(cal.time)

                        val batch = (initialBatch ?: IncubationBatch(
                            batchName = batchName,
                            breed = breed,
                            eggsSet = count,
                            dateSet = dateSet,
                            expectedHatchDate = expectedHatch,
                            incubatorName = incubatorName,
                            notes = notes
                        )).copy(
                            batchName = batchName,
                            breed = breed,
                            eggsSet = count,
                            fertileEggsCount = fertile,
                            hatchedCount = hatched,
                            status = if (hatched > 0) "HATCHED" else initialBatch?.status ?: "INCUBATING",
                            dateSet = dateSet,
                            expectedHatchDate = expectedHatch,
                            incubatorName = incubatorName,
                            notes = notes,
                            updatedAt = System.currentTimeMillis()
                        )
                        onSave(batch)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
            ) {
                Text("Save Batch", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun MoveIncubationBatchDialog(
    batch: IncubationBatch,
    onDismiss: () -> Unit,
    onConfirm: (newFlockName: String, hatchedCount: Int, breed: String, hatchDate: String) -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayStr = dateFormat.format(Date())
    var flockName by remember { mutableStateOf("Flock - ${batch.batchName}") }
    var hatchedStr by remember { mutableStateOf(batch.eggsSet.toString()) }
    var breed by remember { mutableStateOf(batch.breed) }
    var hatchDate by remember { mutableStateOf(if (batch.expectedHatchDate.isNotBlank()) batch.expectedHatchDate else todayStr) }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Move Hatched Chicks to Flock",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF0F172A)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Batch: ${batch.batchName} (${batch.eggsSet} eggs set)", fontSize = 13.sp, color = Color(0xFF475569))
                if (showError) {
                    Text("Please enter a valid flock name and hatched count.", color = Color(0xFFDC2626), fontSize = 12.sp)
                }
                OutlinedTextField(
                    value = flockName,
                    onValueChange = { flockName = it },
                    label = { Text("New Flock Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = hatchedStr,
                    onValueChange = { hatchedStr = it },
                    label = { Text("Hatched Chicks Count") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = breed,
                    onValueChange = { breed = it },
                    label = { Text("Breed") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = hatchDate,
                    onValueChange = { hatchDate = it },
                    label = { Text("Hatch / Arrival Date (YYYY-MM-DD or DD MMM YYYY)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val count = hatchedStr.toIntOrNull() ?: -1
                    if (flockName.isBlank() || count < 0) {
                        showError = true
                    } else {
                        onConfirm(flockName, count, breed, hatchDate)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
            ) {
                Text("Move to Active Flocks", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
