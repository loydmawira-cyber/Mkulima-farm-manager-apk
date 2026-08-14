package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import com.example.ui.components.AppDatePickerField
import com.example.utils.PoultryAgeAndVaccinationUtils
import com.example.utils.VaccineDueStatus
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.LaunchedEffect
import com.example.ui.components.AddCattleEventDialog
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.data.EggLog
import com.example.data.EmployeeRequest
import com.example.data.FarmUnit
import com.example.data.FinanceRecord
import com.example.data.FinanceType
import com.example.data.MilkLog
import com.example.data.RequestStatus
import com.example.ui.theme.ForestGreenPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.ui.theme.TagLivestockBg
import com.example.ui.theme.TagLivestockText
import com.example.ui.theme.TagYieldBg
import com.example.ui.theme.TagYieldText

data class CattleEventItem(
    val id: String,
    val category: String, // "HEAT", "INSEMINATION", "WEIGHT", "HEALTH"
    val title: String,
    val date: String,
    val details: String,
    val notes: String = "",
    val metricValue: String = ""
)

data class UpcomingCattleNotification(
    val id: String,
    val title: String,
    val dueDate: String,
    val category: String, // "HEAT_CHECK", "INSEMINATION_PD", "WEIGHT", "HEALTH"
    val badgeColor: Color,
    val badgeTextColor: Color
)


data class AnimalDetailData(
    val id: String,
    val name: String,
    val tagNumber: String,
    val breed: String,
    val category: String, // CATTLE, POULTRY
    val status: String, // MILKING, PREGNANT, NOT PREGNANT, DRY, ACTIVE, DISPOSED
    val age: String,
    val weight: String,
    val lastMilk: String,
    val breedingStatus: String,
    val expectedCalving: String = "Jun 21, '24",
    val insemination: String = "AI - Thunder (Sep 12, '23)",
    val dateOfBirth: String = "12 Apr 2021",
    val weightAtBirth: String = "32 kg",
    val sire: String = "Thunder #045",
    val dam: String = "Bessie #102",
    val disposalReason: String = "",
    val disposalAmount: Double = 0.0,
    val disposalDate: String = "",
    val disposalNotes: String = "",
    val headCountInt: Int = 1
)

data class FlockDisposalLogItem(
    val id: String,
    val flockName: String,
    val quantity: Int,
    val reason: String, // "Sold", "Death", "Home Consumption", "Other"
    val amount: Double,
    val date: String,
    val notes: String
)

val mockAnimals = listOf(
    AnimalDetailData(
        id = "1",
        name = "Bessie",
        tagNumber = "#102",
        breed = "Friesian Cow",
        category = "CATTLE",
        status = "MILKING",
        age = "4y 2m",
        weight = "520kg",
        lastMilk = "14.2L",
        breedingStatus = "MILKING",
        expectedCalving = "Jun 21, '24",
        insemination = "AI - Thunder (Sep 12, '23)",
        dateOfBirth = "14 May 2020",
        weightAtBirth = "34 kg",
        sire = "Thunder #045",
        dam = "Bessie #001",
        headCountInt = 1
    ),
    AnimalDetailData(
        id = "2",
        name = "Daisy",
        tagNumber = "#105",
        breed = "Jersey Cow",
        category = "CATTLE",
        status = "PREGNANT",
        age = "3y 6m",
        weight = "480kg",
        lastMilk = "12.8L",
        breedingStatus = "PREGNANT (7 months)",
        expectedCalving = "Oct 15, '26",
        dateOfBirth = "02 Dec 2020",
        weightAtBirth = "28 kg",
        sire = "Bull - Prince #012",
        dam = "Daisy #088",
        headCountInt = 1
    ),
    AnimalDetailData(
        id = "3",
        name = "Star",
        tagNumber = "#110",
        breed = "Guernsey Heifer",
        category = "CATTLE",
        status = "HEIFER",
        age = "1y 8m",
        weight = "380kg",
        lastMilk = "N/A",
        breedingStatus = "OPEN HEIFER",
        dateOfBirth = "18 Dec 2024",
        weightAtBirth = "31 kg",
        sire = "Guernsey King #004",
        dam = "Star #052",
        headCountInt = 1
    ),
    AnimalDetailData(
        id = "4",
        name = "Little Joey",
        tagNumber = "#130",
        breed = "Friesian Calf",
        category = "CATTLE",
        status = "CALF",
        age = "3 months",
        weight = "85kg",
        lastMilk = "N/A",
        breedingStatus = "WEANING CALF",
        dateOfBirth = "12 May 2026",
        weightAtBirth = "33 kg",
        sire = "Thunder #045",
        dam = "Bessie #102",
        headCountInt = 1
    ),
    AnimalDetailData(
        id = "5",
        name = "Thunder",
        tagNumber = "#045",
        breed = "Boran Bull",
        category = "CATTLE",
        status = "BULL",
        age = "4y 8m",
        weight = "680kg",
        lastMilk = "N/A",
        breedingStatus = "BREEDING BULL",
        dateOfBirth = "10 Nov 2019",
        weightAtBirth = "38 kg",
        sire = "Boran Giant #001",
        dam = "Queen Boran #011",
        headCountInt = 1
    ),
    AnimalDetailData(
        id = "6",
        name = "Old Bertha",
        tagNumber = "#090",
        breed = "Ayrshire Cow",
        category = "CATTLE",
        status = "DISPOSED (Sold)",
        age = "9y 4m",
        weight = "510kg",
        lastMilk = "0.0L",
        breedingStatus = "DISPOSED (Sold)",
        dateOfBirth = "10 Apr 2017",
        weightAtBirth = "30 kg",
        sire = "Ayrshire Prime #001",
        dam = "Bertha #005",
        disposalReason = "Sold",
        disposalAmount = 145000.0,
        disposalDate = "02 Jun 2026",
        disposalNotes = "Sold to Nakuru Meat Processors",
        headCountInt = 1
    ),
    AnimalDetailData(
        id = "7",
        name = "Alpha Layers",
        tagNumber = "Count: 450",
        breed = "Isa Brown Layers",
        category = "POULTRY",
        status = "Layer / Finisher Feed (8+ Weeks)",
        age = "168 Days (24 Weeks)",
        weight = "1.8kg avg",
        lastMilk = "380 Eggs/Day",
        breedingStatus = "ACTIVE LAYING",
        dateOfBirth = "25 Feb 2026",
        headCountInt = 450
    ),
    AnimalDetailData(
        id = "8",
        name = "Beta Broilers",
        tagNumber = "Count: 300",
        breed = "Cobb 500 Broilers",
        category = "POULTRY",
        status = "Grower Feed (3 - 8 Weeks)",
        age = "35 Days (5 Weeks)",
        weight = "2.1kg avg",
        lastMilk = "N/A (Meat)",
        breedingStatus = "FINISHER STAGE",
        dateOfBirth = "10 Jul 2026",
        headCountInt = 300
    ),
    AnimalDetailData(
        id = "9",
        name = "Kienyeji Flock 1",
        tagNumber = "Count: 200",
        breed = "Improved Kienyeji",
        category = "POULTRY",
        status = "Starter Feed (0 - 3 Weeks)",
        age = "20 Days (2 Wks, 6 Days)",
        weight = "1.2kg avg",
        lastMilk = "Pre-laying",
        breedingStatus = "GROWING FLOCK",
        dateOfBirth = "25 Jul 2026",
        headCountInt = 200
    )
)

@Composable
fun DisposeAnimalDialog(
    animalName: String,
    tagNumber: String,
    onDismiss: () -> Unit,
    onConfirmDispose: (reason: String, amount: Double, notes: String, date: String) -> Unit
) {
    var reason by remember { mutableStateOf("Sold") } // "Sold", "Dead", "Other"
    var amountText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf(SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(20.dp)),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🚫 Dispose Animal",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "$animalName ($tagNumber)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ForestGreenPrimary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Disposal Reason:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Sold", "Dead", "Other").forEach { r ->
                        val isSel = reason.equals(r, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) ForestGreenPrimary else Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, if (isSel) ForestGreenPrimary else Color(0xFFCBD5E1)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { reason = r }
                        ) {
                            Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (r == "Sold") "💰 Sold" else if (r == "Dead") "☠️ Dead" else "📦 Other",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White else Color(0xFF475569)
                                )
                            }
                        }
                    }
                }

                if (reason == "Sold") {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Sale Price / Revenue Amount (KSh):", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it; errorMessage = null },
                        placeholder = { Text("e.g. 145000") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    Text(
                        "* This amount will be automatically recorded as Income in Finance.",
                        fontSize = 11.sp,
                        color = ForestGreenPrimary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text("Buyer / Destination / Details:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    placeholder = { Text(if (reason == "Sold") "Buyer name or market location..." else "Cause or reason for disposal...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))
                AppDatePickerField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = "Disposal Date",
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "animal_disposal_date_picker"
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage!!, fontSize = 12.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("CANCEL")
                    }
                    Button(
                        onClick = {
                            val amount = amountText.toDoubleOrNull() ?: 0.0
                            if (reason == "Sold" && amount <= 0) {
                                errorMessage = "Please enter a valid sale price (> 0)."
                                return@Button
                            }
                            onConfirmDispose(reason, amount, notesText, dateText)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                    ) {
                        Text("CONFIRM DISPOSAL", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun DisposeFlockDialog(
    flockName: String,
    currentHeadCount: Int,
    onDismiss: () -> Unit,
    onConfirmDisposeFlock: (quantity: Int, reason: String, amount: Double, notes: String, date: String) -> Unit
) {
    var reason by remember { mutableStateOf("Sold") } // "Sold", "Death", "Home Consumption", "Other"
    var qtyText by remember { mutableStateOf("10") }
    var amountText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf(SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(20.dp)),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🏷️ Dispose Birds from Flock",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "$flockName ($currentHeadCount Birds Available)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ForestGreenPrimary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Disposal Reason:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Sold", "Death").forEach { r ->
                            val isSel = reason.equals(r, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) ForestGreenPrimary else Color(0xFFF1F5F9),
                                border = BorderStroke(1.dp, if (isSel) ForestGreenPrimary else Color(0xFFCBD5E1)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { reason = r }
                            ) {
                                Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (r == "Sold") "💰 Sold" else "☠️ Death / Loss",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) Color.White else Color(0xFF475569)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Home Consumption", "Other").forEach { r ->
                            val isSel = reason.equals(r, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) ForestGreenPrimary else Color(0xFFF1F5F9),
                                border = BorderStroke(1.dp, if (isSel) ForestGreenPrimary else Color(0xFFCBD5E1)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { reason = r }
                            ) {
                                Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (r == "Home Consumption") "🍲 Home Consumption" else "📦 Other",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) Color.White else Color(0xFF475569)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text("Quantity of Birds to Dispose:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = qtyText,
                    onValueChange = { qtyText = it; errorMessage = null },
                    placeholder = { Text("e.g. 50") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                if (reason == "Sold" || reason == "Home Consumption") {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        if (reason == "Sold") "Sale Revenue / Amount (KSh):" else "Estimated Value / Amount (KSh):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it; errorMessage = null },
                        placeholder = { Text("e.g. 35000") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    Text(
                        "* Recorded as Income on Finance.",
                        fontSize = 11.sp,
                        color = ForestGreenPrimary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text("Buyer / Destination / Notes:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    placeholder = { Text("e.g. Sold 50 broilers to local restaurant") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))
                AppDatePickerField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = "Disposal Date",
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "flock_disposal_date_picker"
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage!!, fontSize = 12.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("CANCEL")
                    }
                    Button(
                        onClick = {
                            val qty = qtyText.toIntOrNull() ?: 0
                            if (qty <= 0) {
                                errorMessage = "Please enter a valid bird quantity."
                                return@Button
                            }
                            if (qty > currentHeadCount) {
                                errorMessage = "Quantity cannot exceed current flock size ($currentHeadCount)."
                                return@Button
                            }
                            val amount = amountText.toDoubleOrNull() ?: 0.0
                            if (reason == "Sold" && amount <= 0) {
                                errorMessage = "Please enter a valid sale price (> 0)."
                                return@Button
                            }
                            onConfirmDisposeFlock(qty, reason, amount, notesText, dateText)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                    ) {
                        Text("CONFIRM DISPOSAL", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun FlocksScreen(
    units: List<FarmUnit>,
    milkLogs: List<MilkLog>,
    eggLogs: List<EggLog>,
    financeRecords: List<FinanceRecord>,
    employeeRequests: List<EmployeeRequest>,
    onAddUnitClick: () -> Unit,
    onAddTaskForUnit: (FarmUnit) -> Unit,
    onAddMilkLogClick: () -> Unit,
    onAddEggLogClick: () -> Unit,
    onAddFinanceClick: () -> Unit,
    onAddEmployeeRequestClick: () -> Unit,
    onUpdateRequestStatus: (EmployeeRequest, RequestStatus) -> Unit,
    onAddFinanceRecord: (type: FinanceType, category: String, amount: Double, description: String) -> Unit = { _, _, _, _ -> },
    onUpdateUnitHeadCount: (unitId: Long, newHeadCount: Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var selectedAnimal by remember { mutableStateOf<AnimalDetailData?>(null) }
    var selectedFilterCategory by remember { mutableStateOf("ALL") }
    var selectedCattleStage by remember { mutableStateOf("ALL") } // "ALL", "MILKING", "PREGNANT", "CALF", "HEIFER", "BULL", "DISPOSED"
    var showCategoryGuideDialog by remember { mutableStateOf(false) }

    val roomAnimals = remember(units) {
        units.map { unit ->
            val isPoultry = unit.type.equals("POULTRY", ignoreCase = true) || unit.type.contains("Poultry", ignoreCase = true)
            AnimalDetailData(
                id = "unit_${unit.id}",
                name = unit.name,
                tagNumber = if (unit.tagNumber.isNotBlank()) unit.tagNumber else if (isPoultry) "Count: ${unit.headCount}" else "#${unit.id + 100}",
                breed = unit.breed.ifBlank { if (isPoultry) "Poultry Flock" else "Local Breed" },
                category = if (isPoultry) "POULTRY" else "CATTLE",
                status = unit.healthStatus.ifBlank { "ACTIVE" },
                age = "1y",
                weight = unit.currentWeight.ifBlank { if (isPoultry) "1.8kg avg" else "450kg" },
                lastMilk = if (isPoultry) "${unit.headCount} Birds" else "14.0L",
                breedingStatus = if (isPoultry) "ACTIVE LAYING" else "HEALTHY",
                dateOfBirth = unit.dob.ifBlank { "12 Apr 2023" },
                weightAtBirth = unit.weightAtBirth.ifBlank { "32 kg" },
                sire = unit.sire.ifBlank { "N/A" },
                dam = unit.dam.ifBlank { "N/A" },
                headCountInt = unit.headCount
            )
        }
    }

    val initialAnimals = remember(units) {
        (roomAnimals + mockAnimals).distinctBy { it.name }
    }

    val mutableAnimals = remember { mutableStateListOf<AnimalDetailData>().apply { addAll(initialAnimals) } }

    LaunchedEffect(units) {
        val existingNames = mutableAnimals.map { it.name }.toSet()
        initialAnimals.forEach { initItem ->
            if (!existingNames.contains(initItem.name)) {
                mutableAnimals.add(initItem)
            }
        }
    }

    fun handleDisposeAnimal(animal: AnimalDetailData, reason: String, amount: Double, notes: String, date: String) {
        val updated = animal.copy(
            status = "DISPOSED ($reason)",
            breedingStatus = "DISPOSED ($reason)",
            disposalReason = reason,
            disposalAmount = amount,
            disposalDate = date,
            disposalNotes = notes
        )
        val idx = mutableAnimals.indexOfFirst { it.id == animal.id }
        if (idx >= 0) {
            mutableAnimals[idx] = updated
        }
        if (selectedAnimal?.id == animal.id) {
            selectedAnimal = updated
        }
        if (reason.equals("Sold", ignoreCase = true) && amount > 0) {
            onAddFinanceRecord(
                FinanceType.INCOME,
                "Animal Sale",
                amount,
                "Sold ${animal.name} (${animal.tagNumber}) - ${notes.ifBlank { "Individual animal disposal by sale" }}"
            )
        }
    }

    fun handleDisposeFlock(flock: AnimalDetailData, quantity: Int, reason: String, amount: Double, notes: String, date: String) {
        val newCount = (flock.headCountInt - quantity).coerceAtLeast(0)
        val updatedTag = "Count: $newCount"
        val updated = flock.copy(
            headCountInt = newCount,
            tagNumber = updatedTag,
            lastMilk = "$newCount Birds"
        )
        val idx = mutableAnimals.indexOfFirst { it.id == flock.id }
        if (idx >= 0) {
            mutableAnimals[idx] = updated
        }
        if (selectedAnimal?.id == flock.id) {
            selectedAnimal = updated
        }

        if (flock.id.startsWith("unit_")) {
            val uId = flock.id.removePrefix("unit_").toLongOrNull()
            if (uId != null) {
                onUpdateUnitHeadCount(uId, newCount)
            }
        }

        if (amount > 0 || reason.equals("Sold", ignoreCase = true) || reason.equals("Home Consumption", ignoreCase = true)) {
            val categoryName = if (reason.equals("Sold", ignoreCase = true)) "Poultry Sale" else "Farm Income"
            if (amount > 0) {
                onAddFinanceRecord(
                    FinanceType.INCOME,
                    categoryName,
                    amount,
                    "Disposed $quantity birds from ${flock.name} ($reason) - ${notes.ifBlank { "Flock disposal sale" }}"
                )
            }
        }
    }

    // Cattle category stage breakdown calculations
    val cattleList = remember(mutableAnimals.toList()) {
        mutableAnimals.filter { it.category.equals("CATTLE", ignoreCase = true) }
    }

    val milkingCount = remember(cattleList) {
        cattleList.count { it.status.equals("MILKING", ignoreCase = true) || it.breedingStatus.contains("MILKING", ignoreCase = true) }
    }

    val pregnantCount = remember(cattleList) {
        cattleList.count { it.status.equals("PREGNANT", ignoreCase = true) || it.breedingStatus.contains("PREGNANT", ignoreCase = true) }
    }

    val calfCount = remember(cattleList) {
        cattleList.count { it.status.equals("CALF", ignoreCase = true) || it.breed.contains("Calf", ignoreCase = true) || it.age.contains("month", ignoreCase = true) }
    }

    val heiferCount = remember(cattleList) {
        cattleList.count { it.status.equals("HEIFER", ignoreCase = true) || it.breedingStatus.contains("HEIFER", ignoreCase = true) }
    }

    val bullCount = remember(cattleList) {
        cattleList.count { it.status.equals("BULL", ignoreCase = true) || it.breed.contains("Bull", ignoreCase = true) || it.breedingStatus.contains("BULL", ignoreCase = true) }
    }

    val disposedCount = remember(cattleList) {
        cattleList.count { it.status.contains("DISPOSED", ignoreCase = true) || it.breedingStatus.contains("CULLED", ignoreCase = true) || it.breedingStatus.contains("SOLD", ignoreCase = true) }
    }

    if (showCategoryGuideDialog) {
        Dialog(onDismissRequest = { showCategoryGuideDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .clip(RoundedCornerShape(20.dp)),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Info, contentDescription = null, tint = ForestGreenPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Cattle Stage Definitions",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }
                        IconButton(onClick = { showCategoryGuideDialog = false }) {
                            Icon(Icons.Filled.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Understanding each stage helps optimize feeding, breeding, milk production, and herd management:",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    listOf(
                        Triple("🐄 MILKING", "Active Lactating Cows", "Adult female cows currently producing daily milk after calving. Requires high-energy ration and daily milk yield tracking."),
                        Triple("🤰 PREGNANT", "Gestating Females", "Cows or heifers confirmed pregnant (~283-day gestation). Monitored for dry period close-up feeding and expected calving date alerts."),
                        Triple("🍼 CALVES", "Young Stock (< 1 Year)", "Newborn to weaning young stock (0-12 months). Focus is on colostrum intake, calf starter, dehorning, and vaccination schedules."),
                        Triple("🌾 HEIFERS", "Young Breeding Females", "Post-weaning young female cattle that have reached maturity but have not yet given birth to their first calf. Monitored for AI insemination."),
                        Triple("🐂 BULLS", "Breeding Males / Studs", "Mature male cattle kept for herd sire breeding, artificial insemination semen production, or beef fattening."),
                        Triple("🚫 DISPOSED", "Culled / Sold / Removed", "Cattle removed from the active productive herd due to age, culling, sale, or mortality. Kept for historical & accounting audits.")
                    ).forEach { (title, subtitle, desc) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                                    Text(subtitle, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(desc, fontSize = 12.sp, color = Color(0xFF334155))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { showCategoryGuideDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                    ) {
                        Text("GOT IT", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }

    if (selectedAnimal != null) {
        val isPoultry = selectedAnimal!!.category.equals("POULTRY", ignoreCase = true) || selectedAnimal!!.category.equals("FLOCK", ignoreCase = true)
        if (isPoultry) {
            FlockDetailsView(
                flock = selectedAnimal!!,
                eggLogs = eggLogs,
                financeRecords = financeRecords,
                onBackClick = { selectedAnimal = null },
                onAddEggLogClick = onAddEggLogClick,
                onAddFinanceClick = onAddFinanceClick,
                onDisposeFlock = { qty, reason, amount, notes, date ->
                    handleDisposeFlock(selectedAnimal!!, qty, reason, amount, notes, date)
                },
                modifier = modifier
            )
        } else {
            AnimalDetailsView(
                animal = selectedAnimal!!,
                onBackClick = { selectedAnimal = null },
                onDisposeAnimal = { reason, amount, notes, date ->
                    handleDisposeAnimal(selectedAnimal!!, reason, amount, notes, date)
                },
                modifier = modifier
            )
        }
    } else {
        Box(modifier = modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Livestock",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1D1F)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Category Filter Chips [ ALL ] [ CATTLE ] [ POULTRY ]
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf("ALL", "CATTLE", "POULTRY").forEach { cat ->
                            val isSelected = selectedFilterCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedFilterCategory = cat
                                    if (cat == "POULTRY") selectedCattleStage = "ALL"
                                },
                                label = { Text(cat, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ForestGreenPrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.White
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = Color(0xFFE2E8F0)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Cattle Herd Breakdown Panel (Visible for ALL or CATTLE filter)
                    if (selectedFilterCategory == "ALL" || selectedFilterCategory == "CATTLE") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Pets, contentDescription = null, tint = ForestGreenPrimary, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Cattle Stage Breakdown", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = Color(0xFFEFF6FF),
                                        modifier = Modifier.clickable { showCategoryGuideDialog = true }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Filled.Info, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Category Guide", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                val stageItems = listOf(
                                    Triple("ALL", "All Herd", "${cattleList.size}"),
                                    Triple("MILKING", "🐄 Milking", "$milkingCount"),
                                    Triple("PREGNANT", "🤰 Pregnant", "$pregnantCount"),
                                    Triple("CALF", "🍼 Calves", "$calfCount"),
                                    Triple("HEIFER", "🌾 Heifers", "$heiferCount"),
                                    Triple("BULL", "🐂 Bulls", "$bullCount"),
                                    Triple("DISPOSED", "🚫 Disposed", "$disposedCount")
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    stageItems.take(4).forEach { (stageKey, label, count) ->
                                        val isSelected = selectedCattleStage == stageKey
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) ForestGreenPrimary else Color.White,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) ForestGreenPrimary else Color(0xFFCBD5E1)),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { selectedCattleStage = stageKey }
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(count, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else Color(0xFF0F172A))
                                                Text(label, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = if (isSelected) Color.White.copy(alpha = 0.9f) else Color(0xFF64748B))
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    stageItems.drop(4).forEach { (stageKey, label, count) ->
                                        val isSelected = selectedCattleStage == stageKey
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) ForestGreenPrimary else Color.White,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) ForestGreenPrimary else Color(0xFFCBD5E1)),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { selectedCattleStage = stageKey }
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(count, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else Color(0xFF0F172A))
                                                Text(label, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = if (isSelected) Color.White.copy(alpha = 0.9f) else Color(0xFF64748B))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }

                val filteredList = mutableAnimals.filter { animal ->
                    val matchesCategory = if (selectedFilterCategory == "ALL") true else animal.category.equals(selectedFilterCategory, ignoreCase = true)

                    val matchesStage = when (selectedCattleStage) {
                        "MILKING" -> (animal.status.equals("MILKING", ignoreCase = true) || animal.breedingStatus.contains("MILKING", ignoreCase = true)) && !animal.status.contains("DISPOSED", ignoreCase = true)
                        "PREGNANT" -> (animal.status.equals("PREGNANT", ignoreCase = true) || animal.breedingStatus.contains("PREGNANT", ignoreCase = true)) && !animal.status.contains("DISPOSED", ignoreCase = true)
                        "CALF" -> (animal.status.equals("CALF", ignoreCase = true) || animal.breed.contains("Calf", ignoreCase = true) || animal.age.contains("month", ignoreCase = true)) && !animal.status.contains("DISPOSED", ignoreCase = true)
                        "HEIFER" -> (animal.status.equals("HEIFER", ignoreCase = true) || animal.breedingStatus.contains("HEIFER", ignoreCase = true)) && !animal.status.contains("DISPOSED", ignoreCase = true)
                        "BULL" -> (animal.status.equals("BULL", ignoreCase = true) || animal.breed.contains("Bull", ignoreCase = true) || animal.breedingStatus.contains("BULL", ignoreCase = true)) && !animal.status.contains("DISPOSED", ignoreCase = true)
                        "DISPOSED" -> animal.status.contains("DISPOSED", ignoreCase = true) || animal.breedingStatus.contains("CULLED", ignoreCase = true) || animal.breedingStatus.contains("SOLD", ignoreCase = true)
                        else -> true
                    }

                    matchesCategory && (if (animal.category.equals("CATTLE", ignoreCase = true)) matchesStage else true)
                }

                items(filteredList, key = { it.id }) { animal ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedAnimal = animal }
                            .testTag("animal_card_${animal.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFF1F5F9),
                                    modifier = Modifier.size(46.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (animal.category == "POULTRY") Icons.Filled.Egg else Icons.Filled.Pets,
                                            contentDescription = null,
                                            tint = ForestGreenPrimary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Text(
                                        text = animal.name,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                    Text(
                                        text = "Breed: ${animal.breed}   ${animal.tagNumber}",
                                        fontSize = 13.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (animal.status == "MILKING" || animal.status == "ACTIVE") TagLivestockBg else TagYieldBg
                            ) {
                                Text(
                                    text = animal.status,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (animal.status == "MILKING" || animal.status == "ACTIVE") TagLivestockText else TagYieldText
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }

            FloatingActionButton(
                onClick = onAddUnitClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("add_animal_fab"),
                containerColor = ForestGreenPrimary,
                contentColor = Color.White
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ADD ANIMAL", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AnimalDetailsView(
    animal: AnimalDetailData,
    onBackClick: () -> Unit,
    onDisposeAnimal: (reason: String, amount: Double, notes: String, date: String) -> Unit = { _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    var showAddCattleEventDialog by remember { mutableStateOf(false) }
    var selectedLogFilter by remember { mutableStateOf("ALL") } // ALL, HEAT, WEIGHT, HEALTH
    var currentStatus by remember(animal.id, animal.status) { mutableStateOf(animal.status) }
    var showUpdateStageDialog by remember { mutableStateOf(false) }
    var showDisposeDialog by remember { mutableStateOf(false) }

    val isCattle = animal.category.equals("CATTLE", ignoreCase = true)
    val isPoultry = animal.category.contains("POULTRY", ignoreCase = true) || animal.breed.contains("Layer", ignoreCase = true) || animal.breed.contains("Poultry", ignoreCase = true) || animal.breed.contains("Flock", ignoreCase = true)

    if (showDisposeDialog) {
        DisposeAnimalDialog(
            animalName = animal.name,
            tagNumber = animal.tagNumber,
            onDismiss = { showDisposeDialog = false },
            onConfirmDispose = { reason, amount, notes, date ->
                currentStatus = "DISPOSED ($reason)"
                showDisposeDialog = false
                onDisposeAnimal(reason, amount, notes, date)
            }
        )
    }

    if (showUpdateStageDialog) {
        Dialog(onDismissRequest = { showUpdateStageDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(20.dp)),
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Modify Cattle Stage / Status",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        IconButton(onClick = { showUpdateStageDialog = false }) {
                            Icon(Icons.Filled.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Select new stage for ${animal.name}:",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    val stages = listOf(
                        "MILKING" to "🐄 MILKING (Active Lactation)",
                        "PREGNANT" to "🤰 PREGNANT (Gestating)",
                        "CALF" to "🍼 CALF (Young Stock)",
                        "HEIFER" to "🌾 HEIFER (Pre-calving Female)",
                        "BULL" to "🐂 BULL (Breeding Male)",
                        "DISPOSED" to "🚫 DISPOSED (Culled / Sold)"
                    )

                    stages.forEach { (stageKey, stageLabel) ->
                        val isSelected = currentStatus.equals(stageKey, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) ForestGreenPrimary.copy(alpha = 0.12f) else Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) ForestGreenPrimary else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .clickable {
                                    currentStatus = stageKey
                                    showUpdateStageDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Filled.Pets,
                                    contentDescription = null,
                                    tint = if (isSelected) ForestGreenPrimary else Color(0xFF94A3B8),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    stageLabel,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) ForestGreenPrimary else Color(0xFF1E293B)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    val cattleEvents = remember {
        mutableStateListOf(
            CattleEventItem(
                id = "e1",
                category = "HEAT",
                title = "Estrus (Heat Period) Observed",
                date = "02 Aug 2026",
                details = "Clear mucus discharge and standing heat recorded during morning check.",
                notes = "Observed by Worker John"
            ),
            CattleEventItem(
                id = "e2",
                category = "INSEMINATION",
                title = "Artificial Insemination (AI)",
                date = "03 Aug 2026",
                details = "Inseminated with Friesian Bull Straw #FRIESIAN-88 (Sire: Thunder #045).",
                notes = "Technician: Dr. Otieno (Vet)",
                metricValue = "Straw #88"
            ),
            CattleEventItem(
                id = "e3",
                category = "WEIGHT",
                title = "Routine Weight Measurement",
                date = "28 Jul 2026",
                details = "Gained +15kg over last 30 days. Good Body Condition Score (3.5/5).",
                notes = "Recorded by Tech",
                metricValue = "520 kg"
            ),
            CattleEventItem(
                id = "e4",
                category = "HEALTH",
                title = "Foot & Mouth Vaccination",
                date = "14 May 2026",
                details = "Administered 2ml FMD vaccine booster subcutaneously.",
                notes = "Batch #FMD-2026-X",
                metricValue = "2 ml"
            )
        )
    }

    val cattleNotifications = remember {
        mutableStateListOf(
            UpcomingCattleNotification(
                id = "n1",
                title = "Repeat Heat Check / Pregnancy Diagnosis (PD)",
                dueDate = "Aug 24, 2026 (In 11 days)",
                category = "INSEMINATION_PD",
                badgeColor = Color(0xFFE0F2FE),
                badgeTextColor = Color(0xFF0369A1)
            ),
            UpcomingCattleNotification(
                id = "n2",
                title = "Monthly Weight & Deworming Check",
                dueDate = "Aug 28, 2026 (In 15 days)",
                category = "WEIGHT",
                badgeColor = Color(0xFFDCFCE7),
                badgeTextColor = Color(0xFF15803D)
            ),
            UpcomingCattleNotification(
                id = "n3",
                title = "Expected Calving Date",
                dueDate = "Jun 21, 2024",
                category = "CALVING",
                badgeColor = Color(0xFFFEF3C7),
                badgeTextColor = Color(0xFFB45309)
            )
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Action Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1E293B))
                }
                Text(
                    text = "Animal Details",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                if (!currentStatus.contains("DISPOSED", ignoreCase = true)) {
                    Button(
                        onClick = { showDisposeDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("🚫 DISPOSE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }
        }

        // Disposed Animal Record Banner Card
        if (currentStatus.contains("DISPOSED", ignoreCase = true) || animal.disposalReason.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Info, contentDescription = null, tint = Color(0xFFDC2626))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ANIMAL DISPOSED RECORD",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• Disposal Reason: ${animal.disposalReason.ifBlank { currentStatus }}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF7F1D1D)
                        )
                        if (animal.disposalDate.isNotBlank()) {
                            Text(
                                text = "• Date Disposed: ${animal.disposalDate}",
                                fontSize = 12.sp,
                                color = Color(0xFF991B1B)
                            )
                        }
                        if (animal.disposalAmount > 0) {
                            Text(
                                text = "• Sale Income Recorded: KSh ${animal.disposalAmount} (Added to Finance Income)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreenPrimary
                            )
                        }
                        if (animal.disposalNotes.isNotBlank()) {
                            Text(
                                text = "• Buyer / Details: ${animal.disposalNotes}",
                                fontSize = 12.sp,
                                color = Color(0xFF7F1D1D)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Note: Animal is removed from active milking list. All historical records remain saved below.",
                            fontSize = 11.sp,
                            color = Color(0xFF991B1B)
                        )
                    }
                }
            }
        }

        // Animal Main Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = animal.name,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = TagLivestockBg,
                                    modifier = Modifier.clickable { showUpdateStageDialog = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = currentStatus,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TagLivestockText
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            Icons.Filled.Edit,
                                            contentDescription = "Edit Stage",
                                            tint = TagLivestockText,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "Tag: ${animal.tagNumber}  •  ${animal.breed}",
                                fontSize = 14.sp,
                                color = Color(0xFF64748B),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Age", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text(animal.age, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        }
                        Column {
                            Text("Weight", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text(animal.weight, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        }
                        Column {
                            Text(if (isPoultry) "Daily Egg Yield" else "Last Milk", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text(animal.lastMilk, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                        }
                    }
                }
            }
        }

        // Lineage & Birth Details Card (Cattle Only)
        if (isCattle) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Pets, contentDescription = null, tint = ForestGreenPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Lineage & Birth Details",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("DATE OF BIRTH", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(animal.dateOfBirth.ifBlank { "N/A" }, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("BIRTH WEIGHT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(animal.weightAtBirth.ifBlank { "N/A" }, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("SIRE (FATHER)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(animal.sire.ifBlank { "N/A" }, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("DAM (MOTHER)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(animal.dam.ifBlank { "N/A" }, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                            }
                        }
                    }
                }
            }

            // Upcoming Events & Notifications Alerts Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFEF3C7))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Notifications, contentDescription = null, tint = Color(0xFFD97706))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Upcoming Events & Alerts",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        cattleNotifications.forEach { note ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = note.badgeColor,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = note.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = note.badgeTextColor
                                        )
                                        Text(
                                            text = "DUE: ${note.dueDate}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = note.badgeTextColor.copy(alpha = 0.8f)
                                        )
                                    }
                                    Icon(
                                        Icons.Filled.Event,
                                        contentDescription = null,
                                        tint = note.badgeTextColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Events Log & Records (Heat, Insemination, Weight, Health)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Event, contentDescription = null, tint = ForestGreenPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Events & Health Logs",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                            }

                            Button(
                                onClick = { showAddCattleEventDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ LOG EVENT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Filter Chips Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                "ALL" to "All Logs",
                                "HEAT" to "🔥 Heat & AI",
                                "WEIGHT" to "⚖️ Weight",
                                "HEALTH" to "🩺 Health"
                            ).forEach { (filterKey, filterLabel) ->
                                val isSelected = selectedLogFilter == filterKey
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedLogFilter = filterKey },
                                    label = { Text(filterLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ForestGreenPrimary,
                                        selectedLabelColor = Color.White,
                                        containerColor = Color(0xFFF1F5F9),
                                        labelColor = Color(0xFF475569)
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val filteredEvents = cattleEvents.filter {
                            when (selectedLogFilter) {
                                "HEAT" -> it.category == "HEAT" || it.category == "INSEMINATION"
                                "WEIGHT" -> it.category == "WEIGHT"
                                "HEALTH" -> it.category == "HEALTH"
                                else -> true
                            }
                        }

                        if (filteredEvents.isEmpty()) {
                            Text(
                                "No events recorded under this category.",
                                color = Color.Gray,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            filteredEvents.forEach { ev ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF8FAFC),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = when (ev.category) {
                                                        "HEAT" -> Color(0xFFFEF3C7)
                                                        "INSEMINATION" -> Color(0xFFE0F2FE)
                                                        "WEIGHT" -> Color(0xFFDCFCE7)
                                                        else -> Color(0xFFFEE2E2)
                                                    }
                                                ) {
                                                    Text(
                                                        text = when (ev.category) {
                                                            "HEAT" -> "HEAT"
                                                            "INSEMINATION" -> "AI"
                                                            "WEIGHT" -> "WEIGHT"
                                                            else -> "HEALTH"
                                                        },
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = when (ev.category) {
                                                            "HEAT" -> Color(0xFFB45309)
                                                            "INSEMINATION" -> Color(0xFF0369A1)
                                                            "WEIGHT" -> Color(0xFF15803D)
                                                            else -> Color(0xFF991B1B)
                                                        }
                                                    )
                                                }

                                                Spacer(modifier = Modifier.width(8.dp))

                                                Text(
                                                    text = ev.title,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = Color(0xFF1E293B)
                                                )
                                            }

                                            Text(
                                                text = ev.date,
                                                fontSize = 11.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = ev.details,
                                            fontSize = 12.sp,
                                            color = Color(0xFF334155)
                                        )

                                        if (ev.notes.isNotBlank() || ev.metricValue.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                if (ev.notes.isNotBlank()) {
                                                    Text(
                                                        text = "Notes: ${ev.notes}",
                                                        fontSize = 11.sp,
                                                        color = Color(0xFF64748B)
                                                    )
                                                }
                                                if (ev.metricValue.isNotBlank()) {
                                                    Text(
                                                        text = ev.metricValue,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = ForestGreenPrimary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Breeding Status Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCFCE7))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.SentimentSatisfied, contentDescription = null, tint = ForestGreenPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Breeding Summary",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFDCFCE7)
                        ) {
                            Text(
                                text = animal.breedingStatus,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreenPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Insemination (AI - Thunder)", fontSize = 13.sp, color = Color(0xFF64748B))
                        Text("Sep 12, '23", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Expected Calving", fontSize = 13.sp, color = Color(0xFF64748B))
                        Text("Jun 21, '24", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                    }
                }
            }
        }

        // Yield Productivity 7-Days Bar Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isPoultry) "Egg Laying Yield (7 Days)" else "Milk Productivity (7 Days)",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = if (isPoultry) "Last Collection: 380 Eggs (12.6 Trays)" else "Last: 15.5L",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        listOf(
                            "Mon" to 0.7f,
                            "Tue" to 0.75f,
                            "Wed" to 0.72f,
                            "Thu" to 0.8f,
                            "Fri" to 0.82f,
                            "Sat" to 0.78f,
                            "Sun" to 0.9f
                        ).forEach { (day, heightRatio) ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .width(22.dp)
                                        .height((100 * heightRatio).dp)
                                        .background(if (isPoultry) Color(0xFFD97706) else ForestGreenPrimary, RoundedCornerShape(4.dp))
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(day, fontSize = 11.sp, color = Color(0xFF64748B))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }

    if (showAddCattleEventDialog) {
        AddCattleEventDialog(
            animalName = animal.name,
            onDismiss = { showAddCattleEventDialog = false },
            onSaveEvent = { type: String, title: String, date: String, details: String, notes: String, metricValue: String, reminderDate: String ->
                cattleEvents.add(
                    0,
                    CattleEventItem(
                        id = "e_${System.currentTimeMillis()}",
                        category = type,
                        title = title,
                        date = date,
                        details = details,
                        notes = notes,
                        metricValue = metricValue
                    )
                )

                if (reminderDate.isNotBlank()) {
                    cattleNotifications.add(
                        0,
                        UpcomingCattleNotification(
                            id = "n_${System.currentTimeMillis()}",
                            title = "Follow-up: $title",
                            dueDate = reminderDate,
                            category = type,
                            badgeColor = when (type) {
                                "HEAT" -> Color(0xFFFEF3C7)
                                "INSEMINATION" -> Color(0xFFE0F2FE)
                                "WEIGHT" -> Color(0xFFDCFCE7)
                                else -> Color(0xFFFEE2E2)
                            },
                            badgeTextColor = when (type) {
                                "HEAT" -> Color(0xFFB45309)
                                "INSEMINATION" -> Color(0xFF0369A1)
                                "WEIGHT" -> Color(0xFF15803D)
                                else -> Color(0xFF991B1B)
                            }
                        )
                    )
                }

                showAddCattleEventDialog = false
            }
        )
    }
}


@Composable
fun HealthLogItem(title: String, date: String, description: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFFF8FAFC),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                Text(date, fontSize = 11.sp, color = Color(0xFF64748B))
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(description, fontSize = 12.sp, color = Color(0xFF475569))
        }
    }
}

data class PoultryFeedLogItem(
    val id: String,
    val date: String,
    val feedType: String,
    val quantityKg: Double,
    val costAmount: Double,
    val notes: String = ""
)

data class PoultryMortalityLogItem(
    val id: String,
    val date: String,
    val count: Int,
    val cause: String,
    val notes: String = ""
)

data class PoultryEggSaleItem(
    val id: String,
    val date: String,
    val traysSold: Int,
    val pricePerTray: Double,
    val totalRevenue: Double,
    val buyer: String = ""
)

data class PoultryVaccineItem(
    val id: String,
    val vaccineName: String,
    val targetStage: String,
    val dueDate: String,
    val status: String, // "COMPLETED", "DUE_SOON", "UPCOMING"
    val notes: String = ""
)

@Composable
fun FlockDetailsView(
    flock: AnimalDetailData,
    eggLogs: List<EggLog>,
    financeRecords: List<FinanceRecord>,
    onBackClick: () -> Unit,
    onAddEggLogClick: () -> Unit,
    onAddFinanceClick: () -> Unit,
    onDisposeFlock: (quantity: Int, reason: String, amount: Double, notes: String, date: String) -> Unit = { _, _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    var showFeedDialog by remember { mutableStateOf(false) }
    var showMortalityDialog by remember { mutableStateOf(false) }
    var showEggSaleDialog by remember { mutableStateOf(false) }
    var showVaccineDialog by remember { mutableStateOf(false) }
    var showDisposeFlockDialog by remember { mutableStateOf(false) }
    var showEditDateAddedDialog by remember { mutableStateOf(false) }

    var flockDateAdded by remember(flock.id, flock.dateOfBirth) {
        mutableStateOf(if (flock.dateOfBirth.isNotBlank()) flock.dateOfBirth else "01 Jul 2026")
    }

    val initialHeadCount = remember(flock.tagNumber, flock.headCountInt) {
        val digits = flock.tagNumber.filter { it.isDigit() }
        if (flock.headCountInt > 1) flock.headCountInt else digits.toIntOrNull() ?: 450
    }
    var liveHeadCount by remember(flock.id) { mutableIntStateOf(initialHeadCount) }

    // Dynamic Flock Age Calculation based on Date Added
    val flockAgeInfo = remember(flockDateAdded) {
        PoultryAgeAndVaccinationUtils.calculateFlockAge(flockDateAdded)
    }

    val completedVaccineRuleIds = remember(flock.id) {
        mutableStateListOf<String>().apply {
            // Pre-complete historical vaccines based on age for existing mock flocks
            if (flockAgeInfo.totalDays >= 7) add("vac_day_0")
            if (flockAgeInfo.totalDays >= 14) add("vac_day_7")
            if (flockAgeInfo.totalDays >= 21) add("vac_day_14")
            if (flockAgeInfo.totalDays >= 28) add("vac_day_21")
            if (flockAgeInfo.totalDays >= 42) add("vac_day_28")
            if (flockAgeInfo.totalDays >= 70) add("vac_day_42")
            if (flockAgeInfo.totalDays >= 126) add("vac_day_56_70")
        }
    }

    // Dynamic calculated vaccination schedule
    val calculatedVaccineSchedule = remember(flockDateAdded, completedVaccineRuleIds.toList()) {
        PoultryAgeAndVaccinationUtils.calculateVaccinationSchedule(flockDateAdded, completedVaccineRuleIds.toSet())
    }

    val overdueVaccineCount = remember(calculatedVaccineSchedule) {
        calculatedVaccineSchedule.count { it.status == VaccineDueStatus.OVERDUE }
    }
    val dueTodayVaccineCount = remember(calculatedVaccineSchedule) {
        calculatedVaccineSchedule.count { it.status == VaccineDueStatus.DUE_TODAY }
    }
    val dueSoonVaccineCount = remember(calculatedVaccineSchedule) {
        calculatedVaccineSchedule.count { it.status == VaccineDueStatus.DUE_SOON }
    }

    val customVaccines = remember(flock.id) {
        mutableStateListOf<PoultryVaccineItem>()
    }

    val flockDisposalLogs = remember(flock.id) {
        mutableStateListOf(
            FlockDisposalLogItem("d1", flock.name, 50, "Sold", 35000.0, "10 Aug 2026", "Sold 50 off-layer birds to local butcher"),
            FlockDisposalLogItem("d2", flock.name, 5, "Death", 0.0, "05 Aug 2026", "Heat stress mortality"),
            FlockDisposalLogItem("d3", flock.name, 2, "Home Consumption", 1400.0, "01 Aug 2026", "Home consumption")
        )
    }

    var selectedStage by remember(flockAgeInfo.feedStage.stageName) {
        mutableStateOf(flockAgeInfo.feedStage.stageName)
    }

    val feedLogs = remember(flock.id) {
        mutableStateListOf(
            PoultryFeedLogItem("f1", "12 Aug 2026", flockAgeInfo.feedStage.feedType, 50.0, 22.50, "Morning ration provided"),
            PoultryFeedLogItem("f2", "10 Aug 2026", flockAgeInfo.feedStage.feedType, 50.0, 22.50, "Full ration provided")
        )
    }

    val mortalityLogs = remember(flock.id) {
        mutableStateListOf(
            PoultryMortalityLogItem("m1", "11 Aug 2026", 2, "Heat Stress", "High temperature peak in midday"),
            PoultryMortalityLogItem("m2", "05 Aug 2026", 1, "Culling", "Weak chick culled")
        )
    }

    val eggSaleLogs = remember(flock.id) {
        mutableStateListOf(
            PoultryEggSaleItem("s1", "12 Aug 2026", 12, 4.50, 54.00, "City Mart Supermarket"),
            PoultryEggSaleItem("s2", "08 Aug 2026", 15, 4.50, 67.50, "Green Grocers Depot")
        )
    }

    val totalMortalityCount = remember(mortalityLogs.size) { mortalityLogs.sumOf { it.count } }
    val mortalityPercentage = remember(liveHeadCount, totalMortalityCount) {
        val totalBorn = liveHeadCount + totalMortalityCount
        if (totalBorn > 0) String.format("%.1f%%", (totalMortalityCount.toDouble() / totalBorn) * 100) else "0.0%"
    }

    if (showDisposeFlockDialog) {
        DisposeFlockDialog(
            flockName = flock.name,
            currentHeadCount = liveHeadCount,
            onDismiss = { showDisposeFlockDialog = false },
            onConfirmDisposeFlock = { quantity, reason, amount, notes, date ->
                showDisposeFlockDialog = false
                liveHeadCount = (liveHeadCount - quantity).coerceAtLeast(0)
                flockDisposalLogs.add(
                    0,
                    FlockDisposalLogItem(
                        id = "d_${System.currentTimeMillis()}",
                        flockName = flock.name,
                        quantity = quantity,
                        reason = reason,
                        amount = amount,
                        date = date,
                        notes = notes.ifBlank { "$reason disposal" }
                    )
                )
                if (reason.equals("Death", ignoreCase = true)) {
                    mortalityLogs.add(
                        0,
                        PoultryMortalityLogItem(
                            id = "m_${System.currentTimeMillis()}",
                            date = date,
                            count = quantity,
                            cause = notes.ifBlank { "Mortality" }
                        )
                    )
                }
                onDisposeFlock(quantity, reason, amount, notes, date)
            }
        )
    }

    // Dialog for changing Date Added using AppDatePicker
    if (showEditDateAddedDialog) {
        var tempDate by remember { mutableStateOf(flockDateAdded) }
        Dialog(onDismissRequest = { showEditDateAddedDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("📅 Edit Flock Date Added", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Text("The flock age, feed stage recommendations, and vaccination due dates will recalculate automatically.", fontSize = 12.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(16.dp))
                    AppDatePickerField(
                        value = tempDate,
                        onValueChange = { tempDate = it },
                        label = "Date Added (Arrival on Farm)",
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "edit_flock_date_added_picker"
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = { showEditDateAddedDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                flockDateAdded = tempDate
                                showEditDateAddedDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                        ) {
                            Text("Update Date")
                        }
                    }
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Bar
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White)
                                .testTag("flock_detail_back_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1E293B))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = flock.name,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "🐔 Poultry Flock Management • ${flock.breed}",
                                fontSize = 13.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    Button(
                        onClick = { showDisposeFlockDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("🏷️ DISPOSE / SELL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // 0. Prominent Vaccination & Feed Transition Alert Banners
            if (overdueVaccineCount > 0 || dueTodayVaccineCount > 0 || dueSoonVaccineCount > 0) {
                item {
                    val isUrgent = overdueVaccineCount > 0 || dueTodayVaccineCount > 0
                    val bannerBg = if (isUrgent) Color(0xFFFEF2F2) else Color(0xFFFFFBEB)
                    val bannerBorder = if (isUrgent) Color(0xFFFECACA) else Color(0xFFFDE68A)
                    val bannerText = if (isUrgent) Color(0xFF991B1B) else Color(0xFF92400E)

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = bannerBg,
                        border = BorderStroke(1.dp, bannerBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MedicalServices,
                                contentDescription = null,
                                tint = bannerText,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isUrgent) "🚨 VACCINATION ATTENTION REQUIRED" else "⚠️ UPCOMING VACCINATIONS",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = bannerText
                                )
                                Text(
                                    text = buildString {
                                        if (overdueVaccineCount > 0) append("$overdueVaccineCount overdue vaccine(s). ")
                                        if (dueTodayVaccineCount > 0) append("$dueTodayVaccineCount vaccine due today! ")
                                        if (dueSoonVaccineCount > 0) append("$dueSoonVaccineCount vaccine due within 2 days.")
                                    },
                                    fontSize = 12.sp,
                                    color = bannerText
                                )
                            }
                        }
                    }
                }
            }

            if (flockAgeInfo.feedStage.hasTransitionAlert && flockAgeInfo.feedStage.transitionAlertMessage != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFEFF6FF),
                        border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🥣", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "FEED STAGE TRANSITION NOTIFICATION",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E40AF)
                                )
                                Text(
                                    text = flockAgeInfo.feedStage.transitionAlertMessage!!,
                                    fontSize = 12.sp,
                                    color = Color(0xFF1E3A8A),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // 1. Flock Header Overview Card (Date Added & Dynamic Age Tracking)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "LIVE BIRD COUNT",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B)
                                )
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "$liveHeadCount",
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ForestGreenPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Birds active",
                                        fontSize = 13.sp,
                                        color = Color(0xFF64748B),
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                }
                            }

                            // Dynamic Calculated Age Badge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFEFF6FF),
                                border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text("CURRENT FLOCK AGE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E40AF))
                                    Text(
                                        text = flockAgeInfo.formattedAge,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1D4ED8)
                                    )
                                    Text(
                                        text = "Calculated Daily",
                                        fontSize = 10.sp,
                                        color = Color(0xFF60A5FA)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Date Added Info Box with Calendar Picker trigger
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth().clickable { showEditDateAddedDialog = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("📅", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("DATE ADDED / ARRIVAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                        Text(flockAgeInfo.dateAddedFormatted, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFE2E8F0)
                                ) {
                                    Text(
                                        text = "Change Date ▾",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF334155)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Stage Pill Chip Selector (Starter 0-3w, Grower 3-8w, Layer/Finisher 8+w)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf(
                                "Starter (0-3 Wks)",
                                "Grower (3-8 Wks)",
                                "Layer/Finisher (8+ Wks)"
                            ).forEach { stage ->
                                val isSelected = selectedStage.contains(stage.take(7), ignoreCase = true)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) ForestGreenPrimary else Color.Transparent,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedStage = stage }
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = stage.split(" ").first(),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else Color(0xFF475569)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats Grid Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF0FDF4),
                                border = BorderStroke(1.dp, Color(0xFFBBF7D0))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("FEED STAGE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                                    Text(flockAgeInfo.feedStage.stageName.split(" (").first(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                                    Text(flockAgeInfo.shortAgeLabel, fontSize = 11.sp, color = Color(0xFF166534))
                                }
                            }

                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFEF2F2),
                                border = BorderStroke(1.dp, Color(0xFFFECACA))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("MORTALITY RATE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                                    Text("$totalMortalityCount lost", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                                    Text("Rate: $mortalityPercentage", fontSize = 11.sp, color = Color(0xFF991B1B))
                                }
                            }
                        }
                    }
                }
            }

            // 2. Stage-Based Feeding Guide & Logs Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🥣 Feed Stage Formulation", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))

                            Button(
                                onClick = { showFeedDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("+ LOG FEED", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val stageFeedInfo = when {
                            selectedStage.contains("Starter", ignoreCase = true) -> PoultryAgeAndVaccinationUtils.getFlockFeedStage(10)
                            selectedStage.contains("Grower", ignoreCase = true) -> PoultryAgeAndVaccinationUtils.getFlockFeedStage(30)
                            else -> PoultryAgeAndVaccinationUtils.getFlockFeedStage(70)
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFFBEB),
                            border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("RECOMMENDED FEED FOR ${selectedStage.uppercase()}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(stageFeedInfo.feedType, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF78350F))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("• Purpose: ${stageFeedInfo.purpose}", fontSize = 12.sp, color = Color(0xFF92400E))
                                Text("• Daily Ration: ${stageFeedInfo.dailyRationPerBird}", fontSize = 12.sp, color = Color(0xFF92400E))
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text("Recent Feed Log History:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                        Spacer(modifier = Modifier.height(8.dp))

                        feedLogs.forEach { feed ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(feed.feedType, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                                        Text("Date: ${feed.date} • ${feed.notes}", fontSize = 12.sp, color = Color(0xFF64748B))
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("${feed.quantityKg} kg", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ForestGreenPrimary)
                                        Text("\${feed.costAmount}", fontSize = 12.sp, color = Color(0xFF64748B))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Complete Standard Vaccination Schedule & Alerts Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("💉 Age Vaccination Schedule", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                Text("Standard protocol keyed from arrival date", fontSize = 12.sp, color = Color(0xFF64748B))
                            }
                            Button(
                                onClick = { showVaccineDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("+ VACCINE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        calculatedVaccineSchedule.forEach { vac ->
                            val (bgColor, textColor) = when (vac.status) {
                                VaccineDueStatus.COMPLETED -> Color(0xFFDCFCE7) to Color(0xFF15803D)
                                VaccineDueStatus.OVERDUE -> Color(0xFFFEE2E2) to Color(0xFF991B1B)
                                VaccineDueStatus.DUE_TODAY -> Color(0xFFFEF3C7) to Color(0xFFB45309)
                                VaccineDueStatus.DUE_SOON -> Color(0xFFFFFBEB) to Color(0xFFD97706)
                                VaccineDueStatus.UPCOMING -> Color(0xFFF1F5F9) to Color(0xFF475569)
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (vac.isCompleted) Color(0xFFF8FAFC) else if (vac.status == VaccineDueStatus.OVERDUE) Color(0xFFFFF1F2) else Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, if (vac.status == VaccineDueStatus.OVERDUE) Color(0xFFFECDD3) else Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = vac.vaccineName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = if (vac.isCompleted) Color(0xFF64748B) else Color(0xFF1E293B)
                                            )
                                        }
                                        Text(
                                            text = "Stage: ${vac.targetStageLabel} • Due: ${vac.scheduledDueDateStr}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF475569)
                                        )
                                        Text(
                                            text = "Method: ${vac.administrationMethod} • ${vac.notes}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = bgColor,
                                        modifier = Modifier.clickable {
                                            if (completedVaccineRuleIds.contains(vac.ruleId)) {
                                                completedVaccineRuleIds.remove(vac.ruleId)
                                            } else {
                                                completedVaccineRuleIds.add(vac.ruleId)
                                            }
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (vac.isCompleted) {
                                                Icon(
                                                    imageVector = Icons.Filled.CheckCircle,
                                                    contentDescription = null,
                                                    tint = textColor,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                            }
                                            Text(
                                                text = vac.statusLabel,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = textColor
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Custom added vaccines
                        customVaccines.forEach { customVac ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(customVac.vaccineName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                                        Text("Target: ${customVac.targetStage} • Due: ${customVac.dueDate}", fontSize = 12.sp, color = Color(0xFF64748B))
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFDCFCE7)
                                    ) {
                                        Text(
                                            text = customVac.status,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF15803D)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Mortality & Health Log Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("☠️ Mortality & Health Log", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            Button(
                                onClick = { showMortalityDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("+ LOG DEATH", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        mortalityLogs.forEach { log ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFEF2F2),
                                border = BorderStroke(1.dp, Color(0xFFFECACA)),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("${log.count} Birds Lost • Cause: ${log.cause}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF991B1B))
                                        Text("Date: ${log.date} • ${log.notes}", fontSize = 12.sp, color = Color(0xFFB91C1C))
                                    }
                                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFFCA5A5)) {
                                        Text("-${log.count}", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7F1D1D))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. Flock Sales & Disposals History Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFFED7AA))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🏷️ Flock Sales & Disposals Log", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            Button(
                                onClick = { showDisposeFlockDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("+ DISPOSE / SELL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (flockDisposalLogs.isEmpty()) {
                            Text("No disposal or bird sale records yet for this flock.", fontSize = 13.sp, color = Color(0xFF64748B))
                        } else {
                            flockDisposalLogs.forEach { dLog ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFFFF7ED),
                                    border = BorderStroke(1.dp, Color(0xFFFFE4E6)),
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "${dLog.quantity} Birds • Reason: ${dLog.reason}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color(0xFF9A3412)
                                            )
                                            Text(
                                                text = "Date: ${dLog.date} ${if (dLog.notes.isNotBlank()) "• ${dLog.notes}" else ""}",
                                                fontSize = 12.sp,
                                                color = Color(0xFFC2410C)
                                            )
                                        }
                                        if (dLog.amount > 0) {
                                            Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFDCFCE7)) {
                                                Text(
                                                    text = "+KSh ${dLog.amount.toInt()}",
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF15803D)
                                                )
                                            }
                                        } else {
                                            Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFFEE2E2)) {
                                                Text(
                                                    text = "-${dLog.quantity}",
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF991B1B)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    // Dialog 1: Feed Consumption
    if (showFeedDialog) {
        Dialog(onDismissRequest = { showFeedDialog = false }) {
            var feedType by remember { mutableStateOf(flockAgeInfo.feedStage.feedType) }
            var qtyText by remember { mutableStateOf("50") }
            var costText by remember { mutableStateOf("22.50") }

            Card(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("🥣 Log Feed Consumption", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = feedType, onValueChange = { feedType = it }, label = { Text("Feed Type") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = qtyText, onValueChange = { qtyText = it }, label = { Text("Quantity (Kg)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = costText, onValueChange = { costText = it }, label = { Text("Total Cost ($)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = { showFeedDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val qty = qtyText.toDoubleOrNull() ?: 50.0
                                val cost = costText.toDoubleOrNull() ?: 0.0
                                feedLogs.add(0, PoultryFeedLogItem("f_${System.currentTimeMillis()}", "Today", feedType, qty, cost, "Logged from Flock View"))
                                showFeedDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                        ) { Text("SAVE FEED LOG") }
                    }
                }
            }
        }
    }

    // Dialog 2: Mortality Record
    if (showMortalityDialog) {
        Dialog(onDismissRequest = { showMortalityDialog = false }) {
            var deathCountText by remember { mutableStateOf("1") }
            var causeText by remember { mutableStateOf("Heat Stress") }
            var notesText by remember { mutableStateOf("High afternoon humidity") }

            Card(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("☠️ Record Bird Mortality", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                    Text("Deducts death count automatically from live flock head count.", fontSize = 12.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = deathCountText, onValueChange = { deathCountText = it }, label = { Text("Number of Bird Deaths") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = causeText, onValueChange = { causeText = it }, label = { Text("Cause / Reason") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = notesText, onValueChange = { notesText = it }, label = { Text("Notes / Observations") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = { showMortalityDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val count = deathCountText.toIntOrNull() ?: 1
                                mortalityLogs.add(0, PoultryMortalityLogItem("m_${System.currentTimeMillis()}", "Today", count, causeText, notesText))
                                liveHeadCount = (liveHeadCount - count).coerceAtLeast(0)
                                showMortalityDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                        ) { Text("RECORD DEATH") }
                    }
                }
            }
        }
    }

    // Dialog 3: Egg Sales
    if (showEggSaleDialog) {
        Dialog(onDismissRequest = { showEggSaleDialog = false }) {
            var traysText by remember { mutableStateOf("10") }
            var priceText by remember { mutableStateOf("4.50") }
            var buyerText by remember { mutableStateOf("Local Supermarket") }

            Card(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("🥚 Log Egg Sales Revenue", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = traysText, onValueChange = { traysText = it }, label = { Text("Number of Trays Sold") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = priceText, onValueChange = { priceText = it }, label = { Text("Price per Tray ($)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = buyerText, onValueChange = { buyerText = it }, label = { Text("Buyer Name") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = { showEggSaleDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val trays = traysText.toIntOrNull() ?: 10
                                val price = priceText.toDoubleOrNull() ?: 4.50
                                val total = trays * price
                                eggSaleLogs.add(0, PoultryEggSaleItem("s_${System.currentTimeMillis()}", "Today", trays, price, total, buyerText))
                                onAddFinanceClick()
                                showEggSaleDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                        ) { Text("SAVE SALE") }
                    }
                }
            }
        }
    }

    // Dialog 4: Custom Vaccination Record with AppDatePicker
    if (showVaccineDialog) {
        Dialog(onDismissRequest = { showVaccineDialog = false }) {
            var nameText by remember { mutableStateOf("Fowl Pox Vaccine") }
            var stageText by remember { mutableStateOf("Week 8") }
            var dateText by remember { mutableStateOf(PoultryAgeAndVaccinationUtils.formatDate(Date())) }

            Card(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("💉 Add Custom Vaccine Schedule", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = nameText, onValueChange = { nameText = it }, label = { Text("Vaccine Name") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = stageText, onValueChange = { stageText = it }, label = { Text("Growth Stage (e.g. Week 8)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    AppDatePickerField(
                        value = dateText,
                        onValueChange = { dateText = it },
                        label = "Scheduled Date",
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "custom_vaccine_date_picker"
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = { showVaccineDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                customVaccines.add(0, PoultryVaccineItem("v_${System.currentTimeMillis()}", nameText, stageText, dateText, "UPCOMING", "User scheduled"))
                                showVaccineDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                        ) { Text("ADD VACCINE") }
                    }
                }
            }
        }
    }
}
