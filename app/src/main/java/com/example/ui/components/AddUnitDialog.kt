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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.ForestGreenPrimary

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.layout.ExperimentalLayoutApi

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddUnitDialog(
    onDismiss: () -> Unit,
    farmSettings: com.example.data.FarmSettings? = null,
    onUnitCreated: (
        name: String,
        type: String,
        headCount: Int,
        healthStatus: String,
        location: String,
        tagNumber: String,
        breed: String,
        dob: String,
        weightAtBirth: String,
        currentWeight: String,
        sire: String,
        dam: String,
        notes: String
    ) -> Unit
) {
    val initialCategory = if (farmSettings?.farmType?.equals("Poultry Only", ignoreCase = true) == true) "POULTRY" else "CATTLE"
    var category by remember(farmSettings?.farmType) { mutableStateOf(initialCategory) }

    val showCategoryToggle = farmSettings == null || farmSettings.farmType.equals("Both", ignoreCase = true)

    val todayFormatted = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
    }

    // Common/Cattle state
    var name by remember { mutableStateOf("") }
    var tagNumber by remember { mutableStateOf("#") }
    var breed by remember { mutableStateOf("Friesian") }
    var dob by remember { mutableStateOf(todayFormatted) }
    var weightAtBirth by remember { mutableStateOf("32 kg") }
    var currentWeight by remember { mutableStateOf("450 kg") }
    var sire by remember { mutableStateOf("Thunder #045") }
    var dam by remember { mutableStateOf("Bessie #102") }
    var status by remember { mutableStateOf("HEIFER") }
    var location by remember { mutableStateOf("Barn A - Pen 2") }

    // Poultry state
    var poultryName by remember { mutableStateOf("") }
    var poultryBreed by remember { mutableStateOf("Isa Brown") }
    var headCountText by remember { mutableStateOf("150") }
    var poultryDateAdded by remember { mutableStateOf(todayFormatted) }
    var poultryStatus by remember { mutableStateOf("Active Laying") }
    var poultryLocation by remember { mutableStateOf("Coop 2 - East Sector") }
    var notes by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("add_unit_dialog"),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Dialog Title Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (category == "CATTLE") Icons.Filled.Pets else Icons.Filled.Egg,
                            contentDescription = null,
                            tint = ForestGreenPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (category == "CATTLE") "Add Cattle Record" else "Add Poultry Flock",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }

                if (showCategoryToggle) {
                    Spacer(modifier = Modifier.height(14.dp))

                    // Category Selection Bar: [ 🐄 CATTLE ]  [ 🐔 POULTRY ]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("CATTLE" to "🐄 Cattle", "POULTRY" to "🐔 Poultry").forEach { (catKey, catLabel) ->
                            val isSelected = category == catKey
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) ForestGreenPrimary else Color.Transparent)
                                    .clickable { category = catKey },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = catLabel,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color(0xFF475569),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (category == "CATTLE") {
                    // CATTLE DETAILED FORM
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Animal Name / Identifier") },
                        placeholder = { Text("e.g. Bella, Daisy II") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cattle_name_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = tagNumber,
                            onValueChange = { tagNumber = it },
                            label = { Text("Tag Number") },
                            placeholder = { Text("e.g. #112") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = breed,
                            onValueChange = { breed = it },
                            label = { Text("Breed") },
                            placeholder = { Text("e.g. Friesian, Jersey") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AppDatePickerField(
                            value = dob,
                            onValueChange = { dob = it },
                            label = "Date of Birth",
                            placeholder = "Select DOB",
                            modifier = Modifier.weight(1f),
                            testTag = "cattle_dob_picker"
                        )

                        OutlinedTextField(
                            value = weightAtBirth,
                            onValueChange = { weightAtBirth = it },
                            label = { Text("Weight at Birth") },
                            placeholder = { Text("e.g. 32 kg") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = sire,
                            onValueChange = { sire = it },
                            label = { Text("Sire (Father)") },
                            placeholder = { Text("e.g. Thunder #045") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = dam,
                            onValueChange = { dam = it },
                            label = { Text("Dam (Mother)") },
                            placeholder = { Text("e.g. Bessie #102") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = currentWeight,
                            onValueChange = { currentWeight = it },
                            label = { Text("Current Weight") },
                            placeholder = { Text("e.g. 450 kg") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Barn / Location") },
                            placeholder = { Text("e.g. Barn A - Stall 2") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Select Cattle Stage / Category:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val cattleStages = listOf(
                            "HEIFER" to "🌾 Heifer",
                            "MILKING" to "🥛 Milking",
                            "INCALF" to "🤰 In-calf",
                            "CALF" to "🍼 Calf",
                            "DRY" to "🍂 Dry",
                            "INSEMINATED" to "💉 Inseminated",
                            "BULL" to "🐂 Bull",
                            "DISPOSED" to "🚫 Disposed"
                        )

                        androidx.compose.foundation.layout.FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            cattleStages.forEach { (stageKey, stageLabel) ->
                                val isSelected = status.equals(stageKey, ignoreCase = true)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) ForestGreenPrimary else Color(0xFFF1F5F9),
                                    border = BorderStroke(1.dp, if (isSelected) ForestGreenPrimary else Color(0xFFCBD5E1)),
                                    modifier = Modifier.clickable { status = stageKey }
                                ) {
                                    Text(
                                        text = stageLabel,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else Color(0xFF334155),
                                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 10.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // POULTRY FORM
                    OutlinedTextField(
                        value = poultryName,
                        onValueChange = { poultryName = it },
                        label = { Text("Flock Name") },
                        placeholder = { Text("e.g. Gamma Layers") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("poultry_name_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = poultryBreed,
                            onValueChange = { poultryBreed = it },
                            label = { Text("Breed / Strain") },
                            placeholder = { Text("e.g. Isa Brown") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = headCountText,
                            onValueChange = { headCountText = it.filter { char -> char.isDigit() } },
                            label = { Text("Head Count / Birds") },
                            placeholder = { Text("e.g. 200") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    AppDatePickerField(
                        value = poultryDateAdded,
                        onValueChange = { poultryDateAdded = it },
                        label = "Date Added (Arrival / Hatch Date on Farm)",
                        placeholder = "Select flock arrival date",
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "poultry_date_added_picker"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = poultryStatus,
                        onValueChange = { poultryStatus = it },
                        label = { Text("Laying / Health Status") },
                        placeholder = { Text("e.g. Active Laying") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = poultryLocation,
                        onValueChange = { poultryLocation = it },
                        label = { Text("Coop Location") },
                        placeholder = { Text("e.g. Coop 2 - East Sector") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Origin (Optional)") },
                    placeholder = { Text("e.g. Bought from ..., Hatched on farm, or health background") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("unit_notes_input"),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(20.dp))

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

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            if (category == "CATTLE") {
                                val finalName = name.ifBlank { "Cattle Animal" }
                                onUnitCreated(
                                    finalName,
                                    "CATTLE",
                                    1,
                                    status,
                                    location,
                                    tagNumber.ifBlank { "#${(100..999).random()}" },
                                    breed,
                                    dob,
                                    weightAtBirth,
                                    currentWeight,
                                    sire,
                                    dam,
                                    notes.trim()
                                )
                            } else {
                                val finalName = poultryName.ifBlank { "Poultry Flock" }
                                onUnitCreated(
                                    finalName,
                                    "POULTRY",
                                    headCountText.toIntOrNull() ?: 100,
                                    poultryStatus,
                                    poultryLocation,
                                    "Count: ${headCountText.toIntOrNull() ?: 100}",
                                    poultryBreed,
                                    poultryDateAdded,
                                    "N/A",
                                    "1.8kg avg",
                                    "N/A",
                                    "N/A",
                                    notes.trim()
                                )
                            }
                        },
                        modifier = Modifier.testTag("save_unit_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                    ) {
                        Text("SAVE ANIMAL", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
