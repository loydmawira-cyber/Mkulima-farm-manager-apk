package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.ui.screens.AnimalDetailData
import com.example.ui.theme.ForestGreenPrimary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditAnimalDialog(
    animal: AnimalDetailData,
    onDismiss: () -> Unit,
    onSaveAnimal: (
        name: String,
        tagNumber: String,
        breed: String,
        category: String,
        status: String,
        breedingStatus: String,
        age: String,
        dob: String,
        weightAtBirth: String,
        currentWeight: String,
        sire: String,
        dam: String,
        headCount: Int
    ) -> Unit
) {
    val isInitiallyPoultry = animal.category.equals("POULTRY", ignoreCase = true) ||
            animal.breed.contains("Layer", ignoreCase = true) ||
            animal.breed.contains("Flock", ignoreCase = true)

    var category by remember(animal) { mutableStateOf(if (isInitiallyPoultry) "POULTRY" else "CATTLE") }

    // Cattle & Common State
    var name by remember(animal) { mutableStateOf(animal.name) }
    var tagNumber by remember(animal) { mutableStateOf(animal.tagNumber) }
    var breed by remember(animal) { mutableStateOf(animal.breed) }
    var dob by remember(animal) { mutableStateOf(animal.dateOfBirth) }
    var weightAtBirth by remember(animal) { mutableStateOf(animal.weightAtBirth) }
    var currentWeight by remember(animal) { mutableStateOf(animal.weight) }
    var sire by remember(animal) { mutableStateOf(animal.sire) }
    var dam by remember(animal) { mutableStateOf(animal.dam) }
    var status by remember(animal) { mutableStateOf(animal.status) }
    var breedingStatus by remember(animal) { mutableStateOf(animal.breedingStatus) }
    var ageText by remember(animal) { mutableStateOf(animal.age) }

    // Poultry HeadCount
    var headCount by remember(animal) { mutableIntStateOf(animal.headCountInt.coerceAtLeast(1)) }
    var headCountText by remember(animal) { mutableStateOf(animal.headCountInt.toString()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("edit_animal_dialog"),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = ForestGreenPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (category == "CATTLE") Icons.Filled.Pets else Icons.Filled.Egg,
                                    contentDescription = null,
                                    tint = ForestGreenPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Modify Animal Record",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = "Edit ${animal.name} (${animal.tagNumber})",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Category Toggle
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(9.dp),
                            color = if (category == "CATTLE") ForestGreenPrimary else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { category = "CATTLE" }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 9.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Pets,
                                    contentDescription = null,
                                    tint = if (category == "CATTLE") Color.White else Color(0xFF475569),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Cattle / Dairy Herd",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (category == "CATTLE") Color.White else Color(0xFF475569)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(9.dp),
                            color = if (category == "POULTRY") ForestGreenPrimary else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { category = "POULTRY" }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 9.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Egg,
                                    contentDescription = null,
                                    tint = if (category == "POULTRY") Color.White else Color(0xFF475569),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Poultry / Flock",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (category == "POULTRY") Color.White else Color(0xFF475569)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (category == "CATTLE") {
                    // Cattle Form
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Animal Name") },
                        placeholder = { Text("e.g. Daisy, Bessie") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_cattle_name_input"),
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
                            label = { Text("Ear Tag Number") },
                            placeholder = { Text("e.g. #105") },
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

                    Spacer(modifier = Modifier.height(6.dp))

                    Text("Popular Dairy & Beef Breeds:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Friesian", "Jersey", "Guernsey", "Ayrshire", "Boran", "Sahiwal", "Simmental", "Flecvieh").forEach { b ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (breed.equals(b, ignoreCase = true)) ForestGreenPrimary.copy(alpha = 0.15f) else Color(0xFFF1F5F9),
                                border = BorderStroke(1.dp, if (breed.equals(b, ignoreCase = true)) ForestGreenPrimary else Color(0xFFE2E8F0)),
                                modifier = Modifier.clickable { breed = b }
                            ) {
                                Text(
                                    text = b,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (breed.equals(b, ignoreCase = true)) ForestGreenPrimary else Color(0xFF334155),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Automatic Stage Explanation Banner
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF0FDF4),
                        border = BorderStroke(1.dp, ForestGreenPrimary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.Info,
                                    contentDescription = null,
                                    tint = ForestGreenPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Automatic Cattle Stage Engine",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreenPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Stage is automatically calculated from breeding event logs (Positive PD, AI, Calving, Dry Off), DOB/age, and milk records. You can also select a baseline stage below:",
                                fontSize = 11.sp,
                                color = Color(0xFF166534)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Baseline Stage Preset:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(
                            "MILKING" to "🥛 Milking",
                            "INCALF_MILKING" to "🥛🤰 In-Calf/Milking",
                            "INCALF" to "🤰 In-Calf",
                            "HEIFER" to "🌾 Heifer",
                            "CALF" to "🍼 Calf",
                            "DRY" to "🍂 Dry",
                            "BULL" to "🐂 Bull"
                        ).forEach { (stgKey, label) ->
                            val isStg = status.equals(stgKey, ignoreCase = true) || status.equals(label, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isStg) ForestGreenPrimary else Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, if (isStg) ForestGreenPrimary else Color(0xFFCBD5E1)),
                                modifier = Modifier.clickable { status = stgKey }
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isStg) Color.White else Color(0xFF475569),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = breedingStatus,
                        onValueChange = { breedingStatus = it },
                        label = { Text("Breeding / Health Note") },
                        placeholder = { Text("e.g. HEALTHY, PREGNANT (5 MO), SERVICED") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

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
                            testTag = "edit_cattle_dob_picker"
                        )

                        OutlinedTextField(
                            value = ageText,
                            onValueChange = { ageText = it },
                            label = { Text("Age") },
                            placeholder = { Text("e.g. 3y 4m") },
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
                            value = weightAtBirth,
                            onValueChange = { weightAtBirth = it },
                            label = { Text("Weight at Birth") },
                            placeholder = { Text("32 kg") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = currentWeight,
                            onValueChange = { currentWeight = it },
                            label = { Text("Current Weight") },
                            placeholder = { Text("450 kg") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Pedigree & Genetics:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                    Spacer(modifier = Modifier.height(4.dp))
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
                } else {
                    // Poultry / Flock Form
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Flock Name / Identifier") },
                        placeholder = { Text("e.g. Flock A - Isa Brown Layers") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_flock_name_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = headCountText,
                            onValueChange = {
                                headCountText = it
                                headCount = it.toIntOrNull() ?: headCount
                            },
                            label = { Text("Total Bird Headcount") },
                            placeholder = { Text("450") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = breed,
                            onValueChange = { breed = it },
                            label = { Text("Breed / Strain") },
                            placeholder = { Text("Isa Brown, Kuroiler") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text("Popular Poultry Breeds:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Isa Brown", "Kuroiler", "Kenbro", "Kienyeji", "Broiler Cobb 500", "Sasso", "Lohmann Brown").forEach { b ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (breed.equals(b, ignoreCase = true)) ForestGreenPrimary.copy(alpha = 0.15f) else Color(0xFFF1F5F9),
                                border = BorderStroke(1.dp, if (breed.equals(b, ignoreCase = true)) ForestGreenPrimary else Color(0xFFE2E8F0)),
                                modifier = Modifier.clickable { breed = b }
                            ) {
                                Text(
                                    text = b,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (breed.equals(b, ignoreCase = true)) ForestGreenPrimary else Color(0xFF334155),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = status,
                        onValueChange = { status = it },
                        label = { Text("Health & Production Status") },
                        placeholder = { Text("ACTIVE LAYING, VACCINATION DUE") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = ageText,
                        onValueChange = { ageText = it },
                        label = { Text("Flock Age / Batch Info") },
                        placeholder = { Text("e.g. 28 Weeks (Point of Lay)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

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
                        Text("CANCEL", color = Color(0xFF475569))
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSaveAnimal(
                                    name,
                                    tagNumber,
                                    breed,
                                    category,
                                    status,
                                    breedingStatus,
                                    ageText,
                                    dob,
                                    weightAtBirth,
                                    currentWeight,
                                    sire,
                                    dam,
                                    headCount
                                )
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                        modifier = Modifier.testTag("edit_animal_save_button")
                    ) {
                        Text("SAVE CHANGES", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
