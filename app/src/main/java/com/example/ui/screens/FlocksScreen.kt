package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import com.example.ui.components.AppDatePickerField
import com.example.ui.components.EditAnimalDialog
import com.example.ui.components.AnimalOptionsDialog
import com.example.ui.components.DeleteAnimalConfirmDialog
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
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.material3.LinearProgressIndicator
import com.example.util.CattleLifecycleEngine
import com.example.util.CattleStage
import com.example.util.CattleStageEvaluation
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
        id = "6b",
        name = "Bella",
        tagNumber = "#115",
        breed = "Friesian Cow",
        category = "CATTLE",
        status = "DRY",
        age = "5y 1m",
        weight = "560kg",
        lastMilk = "0.0L",
        breedingStatus = "DRY OFF (Pre-Calving Rest)",
        dateOfBirth = "14 Mar 2021",
        weightAtBirth = "35 kg",
        sire = "Thunder #045",
        dam = "Bella #010",
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
    onUpdateUnit: (FarmUnit) -> Unit = { _ -> },
    onDeleteUnit: (Long) -> Unit = { _ -> },
    modifier: Modifier = Modifier
) {
    var selectedAnimal by remember { mutableStateOf<AnimalDetailData?>(null) }
    var animalForOptions by remember { mutableStateOf<AnimalDetailData?>(null) }
    var animalToEdit by remember { mutableStateOf<AnimalDetailData?>(null) }
    var animalToDelete by remember { mutableStateOf<AnimalDetailData?>(null) }
    var animalToDispose by remember { mutableStateOf<AnimalDetailData?>(null) }
    var selectedFilterCategory by remember { mutableStateOf("CATTLE") }
    var selectedCattleStage by remember { mutableStateOf("ALL") }
    var showCategoryGuideDialog by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val deletedPrefs = remember { context.getSharedPreferences("mkulima_deleted_animals", android.content.Context.MODE_PRIVATE) }
    var deletedSet by remember {
        mutableStateOf(deletedPrefs.getStringSet("deleted_ids", emptySet()) ?: emptySet())
    }

    val roomAnimals = remember(units, milkLogs) {
        units.map { unit ->
            val isPoultry = unit.type.equals("POULTRY", ignoreCase = true) || unit.type.contains("Poultry", ignoreCase = true)
            val cowLogs = milkLogs.filter { it.cowName.equals(unit.name, ignoreCase = true) }
            val lastMilkStr = if (isPoultry) {
                "${unit.headCount} Birds"
            } else if (cowLogs.isNotEmpty()) {
                "${"%.1f".format(cowLogs.first().litres)}L"
            } else {
                "No data yet"
            }
            val calculatedAge = if (unit.dob.isNotBlank()) {
                CattleLifecycleEngine.calculateAgeFromDob(unit.dob)
            } else {
                "1y"
            }
            AnimalDetailData(
                id = "unit_${unit.id}",
                name = unit.name,
                tagNumber = if (unit.tagNumber.isNotBlank()) unit.tagNumber else if (isPoultry) "Count: ${unit.headCount}" else "#${unit.id + 100}",
                breed = unit.breed.ifBlank { if (isPoultry) "Poultry Flock" else "Local Breed" },
                category = if (isPoultry) "POULTRY" else "CATTLE",
                status = unit.healthStatus.ifBlank { "ACTIVE" },
                age = calculatedAge,
                weight = unit.currentWeight.ifBlank { if (isPoultry) "1.8kg avg" else "450kg" },
                lastMilk = lastMilkStr,
                breedingStatus = if (isPoultry) "ACTIVE LAYING" else "HEALTHY",
                dateOfBirth = unit.dob.ifBlank { "12 Apr 2023" },
                weightAtBirth = unit.weightAtBirth.ifBlank { "32 kg" },
                sire = unit.sire.ifBlank { "N/A" },
                dam = unit.dam.ifBlank { "N/A" },
                headCountInt = unit.headCount
            )
        }
    }

    val initialAnimals = remember(units, milkLogs, deletedSet) {
        (roomAnimals + mockAnimals)
            .filter { !deletedSet.contains(it.id) && !deletedSet.contains(it.name.lowercase()) }
            .distinctBy { it.name }
    }

    val mutableAnimals = remember { mutableStateListOf<AnimalDetailData>().apply { addAll(initialAnimals) } }

    val allAnimalEventsMap = remember {
        mutableStateMapOf<String, SnapshotStateList<CattleEventItem>>().apply {
            put(
                "1",
                mutableStateListOf(
                    CattleEventItem("e1_1", "INSEMINATION", "Artificial Insemination (AI)", "12 Sep 2023", "Inseminated with Friesian Bull Straw #FRIESIAN-88 (Sire: Thunder #045).", "Technician: Dr. Otieno (Vet)", "Straw #88"),
                    CattleEventItem("e1_2", "CALVING", "Calving Event - Delivered Healthy Calf", "14 Jun 2024", "Delivered healthy male calf (Calf #130). Clean delivery, placenta expelled.", "Calf Tag: #130"),
                    CattleEventItem("e1_3", "HEAT", "Estrus (Heat Period) Observed", "02 Aug 2026", "Standing heat and clear mucus discharge observed.", "Observed by Worker John"),
                    CattleEventItem("e1_4", "INSEMINATION", "Artificial Insemination (AI)", "03 Aug 2026", "Inseminated with Friesian Straw #92 (Sire: Thunder #045).", "Technician: Dr. Otieno (Vet)", "Straw #92")
                )
            )
            put(
                "2",
                mutableStateListOf(
                    CattleEventItem("e2_1", "INSEMINATION", "Artificial Insemination (AI)", "10 Jan 2026", "Inseminated with Jersey Bull Straw #JERSEY-12.", "Technician: Dr. Otieno (Vet)", "Straw #12"),
                    CattleEventItem("e2_2", "PD", "Pregnancy Diagnosis (PD) - Confirmed Positive", "15 Mar 2026", "Rectal palpation confirmed pregnancy ~65 days. Gestation progressing well.", "Technician: Dr. Otieno (Vet)", "Positive (In-Calf)")
                )
            )
            put(
                "3",
                mutableStateListOf(
                    CattleEventItem("e3_1", "HEAT", "First Estrus Observed", "10 Jun 2026", "Heifer showed standing heat for 12 hours.", "Recorded by Tech"),
                    CattleEventItem("e3_2", "WEIGHT", "Weight Check (Heifer Target)", "15 Jul 2026", "Current weight 380kg. Reached breeding target weight.", "Tech: Peter", "380 kg")
                )
            )
            put(
                "4",
                mutableStateListOf(
                    CattleEventItem("e4_1", "CALVING", "Birth Record", "12 May 2026", "Born to Bessie #102. Birth weight 33kg. Colostrum administered.", "Dam: Bessie #102", "33 kg"),
                    CattleEventItem("e4_2", "HEALTH", "Dehorning & Blackquarter Vaccine", "20 Jun 2026", "Disbudded with electric cautery. BQ vaccine administered.", "Vet Clinic")
                )
            )
            put(
                "5",
                mutableStateListOf(
                    CattleEventItem("e5_1", "WEIGHT", "Breeding Bull Weight Assessment", "01 Jul 2026", "Weighed 680kg. Excellent body conformation.", "Record: Tech", "680 kg")
                )
            )
            put(
                "6b",
                mutableStateListOf(
                    CattleEventItem("e6b_1", "PD", "Pregnancy Diagnosis (PD) - Confirmed Positive", "10 Apr 2026", "Confirmed pregnant ~120 days. Expected calving Sep 2026.", "Technician: Dr. Otieno (Vet)", "Positive (In-Calf)"),
                    CattleEventItem("e6b_2", "DRY_OFF", "Dry Off (Milking Cessation)", "15 Jul 2026", "Milking halted for 60-day dry period. Dry cow intramammary therapy applied.", "Vet: Dr. Otieno", "Dry Period")
                )
            )
        }
    }

    LaunchedEffect(units, deletedSet) {
        val existingNames = mutableAnimals.map { it.name.lowercase() }.toSet()
        initialAnimals.forEach { initItem ->
            if (!existingNames.contains(initItem.name.lowercase()) && !deletedSet.contains(initItem.id) && !deletedSet.contains(initItem.name.lowercase())) {
                mutableAnimals.add(initItem)
            }
        }
    }

    fun handleModifyAnimal(
        animalId: String,
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
    ) {
        val existing = mutableAnimals.find { it.id == animalId } ?: return
        val updated = existing.copy(
            name = name,
            tagNumber = tagNumber,
            breed = breed,
            category = category,
            status = status,
            breedingStatus = breedingStatus,
            age = age,
            dateOfBirth = dob,
            weightAtBirth = weightAtBirth,
            weight = currentWeight,
            sire = sire,
            dam = dam,
            headCountInt = headCount,
            lastMilk = if (category.equals("POULTRY", ignoreCase = true)) "$headCount Birds" else existing.lastMilk
        )
        val idx = mutableAnimals.indexOfFirst { it.id == animalId }
        if (idx >= 0) {
            mutableAnimals[idx] = updated
        }
        if (selectedAnimal?.id == animalId) {
            selectedAnimal = updated
        }

        if (animalId.startsWith("unit_")) {
            val uId = animalId.removePrefix("unit_").toLongOrNull()
            if (uId != null) {
                val matching = units.find { it.id == uId }
                if (matching != null) {
                    val nowFormatted = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())
                    val updatedUnit = matching.copy(
                        name = name,
                        type = if (category.equals("POULTRY", ignoreCase = true)) "Poultry" else "Cattle",
                        headCount = headCount,
                        healthStatus = status,
                        lastUpdated = nowFormatted,
                        tagNumber = tagNumber,
                        breed = breed,
                        dob = dob,
                        weightAtBirth = weightAtBirth,
                        currentWeight = currentWeight,
                        sire = sire,
                        dam = dam
                    )
                    onUpdateUnit(updatedUnit)
                }
            }
        } else {
            val matching = units.find { it.name.equals(existing.name, ignoreCase = true) }
            if (matching != null) {
                val nowFormatted = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())
                val updatedUnit = matching.copy(
                    name = name,
                    type = if (category.equals("POULTRY", ignoreCase = true)) "Poultry" else "Cattle",
                    headCount = headCount,
                    healthStatus = status,
                    lastUpdated = nowFormatted,
                    tagNumber = tagNumber,
                    breed = breed,
                    dob = dob,
                    weightAtBirth = weightAtBirth,
                    currentWeight = currentWeight,
                    sire = sire,
                    dam = dam
                )
                onUpdateUnit(updatedUnit)
            }
        }
    }

    fun handleDeleteAnimalCompletely(animal: AnimalDetailData) {
        val newDeleted = deletedSet + animal.id + animal.name.lowercase() + (if (animal.id.startsWith("unit_")) animal.id.removePrefix("unit_") else "")
        deletedSet = newDeleted
        deletedPrefs.edit().putStringSet("deleted_ids", newDeleted).apply()
        mutableAnimals.removeAll { it.id == animal.id || it.name.equals(animal.name, ignoreCase = true) }
        if (selectedAnimal?.id == animal.id || selectedAnimal?.name.equals(animal.name, ignoreCase = true)) {
            selectedAnimal = null
        }
        if (animal.id.startsWith("unit_")) {
            val uId = animal.id.removePrefix("unit_").toLongOrNull()
            if (uId != null) {
                onDeleteUnit(uId)
            }
        } else {
            val matching = units.find { it.name.equals(animal.name, ignoreCase = true) }
            if (matching != null) {
                onDeleteUnit(matching.id)
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

    fun handleUpdateAnimalStage(animalId: String, newStatus: String, newBreedingStatus: String) {
        val idx = mutableAnimals.indexOfFirst { it.id == animalId }
        if (idx >= 0) {
            val existing = mutableAnimals[idx]
            val updated = existing.copy(
                status = newStatus,
                breedingStatus = newBreedingStatus
            )
            mutableAnimals[idx] = updated
            if (selectedAnimal?.id == animalId) {
                selectedAnimal = updated
            }
        }
        val unitId = animalId.removePrefix("unit_").toLongOrNull()
        if (unitId != null) {
            val u = units.find { it.id == unitId }
            if (u != null) {
                onUpdateUnit(u.copy(healthStatus = newStatus))
            }
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

    // Cattle category stage breakdown calculations using automatic CattleLifecycleEngine
    val cattleList = remember(mutableAnimals.toList()) {
        mutableAnimals.filter { it.category.equals("CATTLE", ignoreCase = true) }
    }
    val poultryList = remember(mutableAnimals.toList()) {
        mutableAnimals.filter { it.category.equals("POULTRY", ignoreCase = true) || it.breed.contains("Layer", ignoreCase = true) || it.breed.contains("Flock", ignoreCase = true) }
    }

    val evaluatedCattleMap = remember(cattleList, allAnimalEventsMap.toMap(), milkLogs) {
        cattleList.associate { animal ->
            val evs = allAnimalEventsMap[animal.id] ?: emptyList()
            animal.id to CattleLifecycleEngine.evaluateCattleStage(animal, evs, milkLogs)
        }
    }

    val inCalfMilkingCount = remember(evaluatedCattleMap) { evaluatedCattleMap.values.count { it.stage == CattleStage.INCALF_MILKING } }
    val inCalfCount = remember(evaluatedCattleMap) { evaluatedCattleMap.values.count { it.stage == CattleStage.INCALF } }
    val totalInCalfCount = remember(evaluatedCattleMap) { inCalfMilkingCount + inCalfCount }
    val milkingCount = remember(evaluatedCattleMap) { evaluatedCattleMap.values.count { it.stage == CattleStage.MILKING } }
    val heiferCount = remember(evaluatedCattleMap) { evaluatedCattleMap.values.count { it.stage == CattleStage.HEIFER } }
    val calfCount = remember(evaluatedCattleMap) { evaluatedCattleMap.values.count { it.stage == CattleStage.CALF } }
    val dryCount = remember(evaluatedCattleMap) { evaluatedCattleMap.values.count { it.stage == CattleStage.DRY } }
    val inseminatedCount = remember(evaluatedCattleMap) { evaluatedCattleMap.values.count { it.stage == CattleStage.INSEMINATED } }
    val bullCount = remember(evaluatedCattleMap) { evaluatedCattleMap.values.count { it.stage == CattleStage.BULL } }
    val disposedCount = remember(evaluatedCattleMap) { evaluatedCattleMap.values.count { it.stage == CattleStage.DISPOSED } }

    val poultryFlocksCount = remember(poultryList) { poultryList.size }
    val poultryLayingCount = remember(poultryList) { poultryList.count { it.status.contains("Laying", ignoreCase = true) || it.breedingStatus.contains("Laying", ignoreCase = true) || it.status.equals("ACTIVE", ignoreCase = true) } }
    val poultryTotalBirds = remember(poultryList) { poultryList.sumOf { it.headCountInt } }

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
                                "Automatic Cattle Stages",
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
                        "Cattle stage and production status are calculated automatically from breeding records & log events:",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    listOf(
                        Triple("🍼 CALF", "Young Stock (< 12 Months)", "Newborn to weaning young stock. Automatically determined by birth date or young age (< 12 months)."),
                        Triple("🌾 HEIFER", "Mature Maiden (> 12 Months)", "Young female (> 12 months) that has not yet given birth to her first calf. Automatically transitions upon aging."),
                        Triple("🤰 IN-CALF", "Confirmed Pregnant (Dry / Heifer)", "Confirmed pregnant through a positive Pregnancy Diagnosis (PD) log event, resting or not actively producing milk."),
                        Triple("🥛🤰 IN-CALF / MILKING", "Pregnant & Active Lactation", "Confirmed pregnant via positive PD log event and concurrently active in daily milk production."),
                        Triple("🥛 MILKING", "Active Lactating Cow (Open)", "Adult cow in daily milk production following calving, awaiting or between inseminations."),
                        Triple("🍂 DRY", "Dry Period (Resting)", "Mature cow that has completed lactation and ceased milking (via Dry Off log event or 0 milk logs)."),
                        Triple("🐂 BULL", "Breeding Male / Stud", "Mature male kept for herd breeding or artificial insemination semen production."),
                        Triple("🚫 DISPOSED", "Culled / Sold / Removed", "Cattle disposed from active herd. All historical milk and breeding records remain safely stored.")
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

    if (animalForOptions != null) {
        val target = animalForOptions!!
        AnimalOptionsDialog(
            animal = target,
            onDismiss = { animalForOptions = null },
            onEditClick = {
                animalForOptions = null
                animalToEdit = target
            },
            onDeleteClick = {
                animalForOptions = null
                animalToDelete = target
            },
            onDisposeClick = {
                animalForOptions = null
                val isPoultry = target.category.equals("POULTRY", ignoreCase = true) || target.breed.contains("Layer", ignoreCase = true) || target.breed.contains("Flock", ignoreCase = true)
                if (isPoultry) {
                    selectedAnimal = target
                } else {
                    animalToDispose = target
                }
            },
            onViewDetailsClick = {
                animalForOptions = null
                selectedAnimal = target
            }
        )
    }

    if (animalToEdit != null) {
        EditAnimalDialog(
            animal = animalToEdit!!,
            onDismiss = { animalToEdit = null },
            onSaveAnimal = { name, tagNumber, breed, category, status, breedingStatus, age, dob, weightAtBirth, currentWeight, sire, dam, headCount ->
                handleModifyAnimal(
                    animalId = animalToEdit!!.id,
                    name = name,
                    tagNumber = tagNumber,
                    breed = breed,
                    category = category,
                    status = status,
                    breedingStatus = breedingStatus,
                    age = age,
                    dob = dob,
                    weightAtBirth = weightAtBirth,
                    currentWeight = currentWeight,
                    sire = sire,
                    dam = dam,
                    headCount = headCount
                )
                animalToEdit = null
            }
        )
    }

    if (animalToDelete != null) {
        DeleteAnimalConfirmDialog(
            animal = animalToDelete!!,
            onDismiss = { animalToDelete = null },
            onConfirmDelete = {
                handleDeleteAnimalCompletely(animalToDelete!!)
                animalToDelete = null
            }
        )
    }

    if (animalToDispose != null) {
        DisposeAnimalDialog(
            animalName = animalToDispose!!.name,
            tagNumber = animalToDispose!!.tagNumber,
            onDismiss = { animalToDispose = null },
            onConfirmDispose = { reason, amount, notes, date ->
                handleDisposeAnimal(animalToDispose!!, reason, amount, notes, date)
                animalToDispose = null
            }
        )
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
                onEditFlock = { animalToEdit = selectedAnimal },
                onDeleteFlock = { animalToDelete = selectedAnimal },
                modifier = modifier
            )
        } else {
            val currentEvents = allAnimalEventsMap.getOrPut(selectedAnimal!!.id) {
                mutableStateListOf<CattleEventItem>()
            }
            AnimalDetailsView(
                animal = selectedAnimal!!,
                milkLogs = milkLogs,
                eggLogs = eggLogs,
                animalEvents = currentEvents,
                onUpdateAnimalStage = { newStatus, newBreedingStatus ->
                    handleUpdateAnimalStage(selectedAnimal!!.id, newStatus, newBreedingStatus)
                },
                onBackClick = { selectedAnimal = null },
                onDisposeAnimal = { reason, amount, notes, date ->
                    handleDisposeAnimal(selectedAnimal!!, reason, amount, notes, date)
                },
                onEditAnimal = { animalToEdit = selectedAnimal },
                onDeleteAnimal = { animalToDelete = selectedAnimal },
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

                    // Category Filter Chips [ CATTLE ] [ POULTRY ] (No 'ALL' option)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf("CATTLE", "POULTRY").forEach { cat ->
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

                    // Cattle Herd Breakdown Panel (Visible for CATTLE filter)
                    if (selectedFilterCategory == "CATTLE") {
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
                                    Triple("MILKING", "🥛 Milking", "$milkingCount"),
                                    Triple("INCALF", "🤰 In-Calf", "$totalInCalfCount"),
                                    Triple("HEIFER", "🌾 Heifers", "$heiferCount"),
                                    Triple("CALF", "🍼 Calves", "$calfCount"),
                                    Triple("BULL", "🐂 Bulls", "$bullCount"),
                                    Triple("DRY", "🍂 Dry", "$dryCount"),
                                    Triple("INSEMINATED", "💉 Inseminated", "$inseminatedCount")
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
                                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(count, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else Color(0xFF0F172A))
                                                Text(label, fontSize = 9.sp, fontWeight = FontWeight.Medium, color = if (isSelected) Color.White.copy(alpha = 0.9f) else Color(0xFF64748B), maxLines = 1)
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
                                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(count, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else Color(0xFF0F172A))
                                                Text(label, fontSize = 9.sp, fontWeight = FontWeight.Medium, color = if (isSelected) Color.White.copy(alpha = 0.9f) else Color(0xFF64748B), maxLines = 1)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (selectedFilterCategory == "POULTRY") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Egg, contentDescription = null, tint = ForestGreenPrimary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Poultry Flock Summary", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color.White,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("$poultryFlocksCount", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                            Text("Total Flocks", fontSize = 11.sp, color = Color(0xFF64748B))
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFFFEF3C7),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("$poultryLayingCount", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                                            Text("Currently Laying", fontSize = 11.sp, color = Color(0xFF92400E))
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFFDCFCE7),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("$poultryTotalBirds", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                                            Text("Total Birds", fontSize = 11.sp, color = ForestGreenPrimary)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }

                val filteredList = mutableAnimals.filter { animal ->
                    val matchesCategory = animal.category.equals(selectedFilterCategory, ignoreCase = true)
                    if (!matchesCategory) return@filter false
                    if (!animal.category.equals("CATTLE", ignoreCase = true)) return@filter true

                    val eval = evaluatedCattleMap[animal.id] ?: CattleLifecycleEngine.evaluateCattleStage(animal, allAnimalEventsMap[animal.id] ?: emptyList(), milkLogs)
                    when (selectedCattleStage) {
                        "MILKING" -> eval.stage == CattleStage.MILKING
                        "INCALF" -> eval.stage == CattleStage.INCALF || eval.stage == CattleStage.INCALF_MILKING
                        "HEIFER" -> eval.stage == CattleStage.HEIFER
                        "CALF" -> eval.stage == CattleStage.CALF
                        "DRY" -> eval.stage == CattleStage.DRY
                        "INSEMINATED" -> eval.stage == CattleStage.INSEMINATED
                        "BULL" -> eval.stage == CattleStage.BULL
                        "DISPOSED" -> eval.stage == CattleStage.DISPOSED
                        else -> true
                    }
                }

                items(filteredList, key = { it.id }) { animal ->
                    val isCattleItem = animal.category.equals("CATTLE", ignoreCase = true)
                    val cattleEval = if (isCattleItem) evaluatedCattleMap[animal.id] ?: CattleLifecycleEngine.evaluateCattleStage(animal, allAnimalEventsMap[animal.id] ?: emptyList(), milkLogs) else null

                    @OptIn(ExperimentalFoundationApi::class)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .combinedClickable(
                                onClick = { selectedAnimal = animal },
                                onLongClick = { animalForOptions = animal }
                            )
                            .testTag("animal_card_${animal.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(14.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
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

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = animal.name,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                    Text(
                                        text = if (cattleEval != null) "${cattleEval.stage.emoji} ${animal.breed}   ${animal.tagNumber}" else "Breed: ${animal.breed}   ${animal.tagNumber}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B)
                                    )
                                    if (cattleEval != null) {
                                        Text(
                                            text = cattleEval.breedingStatusText,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = cattleEval.badgeTextColor
                                        )
                                    } else {
                                        Text(
                                            text = "💡 Long press to Edit / Delete",
                                            fontSize = 10.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = cattleEval?.badgeBgColor ?: if (animal.status == "MILKING" || animal.status == "ACTIVE" || animal.status == "Active Laying") TagLivestockBg else TagYieldBg
                                ) {
                                    Text(
                                        text = cattleEval?.stage?.displayName ?: animal.status,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = cattleEval?.badgeTextColor ?: if (animal.status == "MILKING" || animal.status == "ACTIVE" || animal.status == "Active Laying") TagLivestockText else TagYieldText
                                    )
                                }

                                IconButton(
                                    onClick = { animalForOptions = animal },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .testTag("more_options_${animal.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.MoreVert,
                                        contentDescription = "Animal options",
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
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
    onEditAnimal: () -> Unit = {},
    onDeleteAnimal: () -> Unit = {},
    milkLogs: List<MilkLog> = emptyList(),
    eggLogs: List<EggLog> = emptyList(),
    animalEvents: SnapshotStateList<CattleEventItem> = remember { mutableStateListOf() },
    onUpdateAnimalStage: (newStatus: String, newBreedingStatus: String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var showAddCattleEventDialog by remember { mutableStateOf(false) }
    var selectedLogFilter by remember { mutableStateOf("ALL") } // ALL, HEAT, WEIGHT, HEALTH
    var currentStatus by remember(animal.id, animal.status) { mutableStateOf(animal.status) }
    var showUpdateStageDialog by remember { mutableStateOf(false) }
    var showDisposeDialog by remember { mutableStateOf(false) }
    var showStageInfoDialog by remember { mutableStateOf(false) }

    val isCattle = animal.category.equals("CATTLE", ignoreCase = true)
    val isPoultry = animal.category.contains("POULTRY", ignoreCase = true) || animal.breed.contains("Layer", ignoreCase = true) || animal.breed.contains("Poultry", ignoreCase = true) || animal.breed.contains("Flock", ignoreCase = true)

    // Evaluate dynamic cattle stage using CattleLifecycleEngine
    val cattleEval = remember(animal, animalEvents.toList(), milkLogs) {
        if (isCattle) {
            CattleLifecycleEngine.evaluateCattleStage(animal, animalEvents.toList(), milkLogs)
        } else null
    }

    // Keep animal status in sync with calculated stage if cattle
    LaunchedEffect(cattleEval) {
        if (cattleEval != null && !animal.status.startsWith("DISPOSED", ignoreCase = true)) {
            currentStatus = cattleEval.stage.displayName
            onUpdateAnimalStage(cattleEval.stage.displayName, cattleEval.breedingStatusText)
        }
    }

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

    if (showStageInfoDialog && cattleEval != null) {
        Dialog(onDismissRequest = { showStageInfoDialog = false }) {
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
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = cattleEval.badgeBgColor
                            ) {
                                Text(
                                    text = cattleEval.stage.displayName,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = cattleEval.badgeTextColor
                                )
                            }
                        }
                        IconButton(onClick = { showStageInfoDialog = false }) {
                            Icon(Icons.Filled.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "Automatic Stage Calculation",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Current Evaluation:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = cattleEval.explanation,
                                fontSize = 13.sp,
                                color = Color(0xFF1E293B)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "How Cattle Stages Work:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "• CALF: Cattle under 6 months old.\n" +
                        "• HEIFER: Female >= 6 months old with no calves born yet.\n" +
                        "• INCALF: Confirmed pregnant through positive Pregnancy Diagnosis (PD) check.\n" +
                        "• INCALF / MILKING: Confirmed pregnant while currently actively milking.\n" +
                        "• DRY: Non-lactating cow (rest period ~60 days before calving).\n" +
                        "• MILKING: Actively lactating cow.\n" +
                        "• BULL: Male breeding stock.",
                        fontSize = 12.sp,
                        color = Color(0xFF334155),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                showStageInfoDialog = false
                                showUpdateStageDialog = true
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Manual Override", fontSize = 11.sp)
                        }
                        Button(
                            onClick = {
                                showStageInfoDialog = false
                                showAddCattleEventDialog = true
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                        ) {
                            Text("+ Log Event", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
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
                            "Manual Stage Override",
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
                        "Normally stages update automatically via Log Events (AI, PD, Calving, Dry Off). You can also set a manual override:",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    val stages = listOf(
                        "MILKING" to "🐄 MILKING (Active Lactation)",
                        "INCALF_MILKING" to "🤰 INCALF / MILKING (Pregnant + Lactating)",
                        "INCALF" to "🤰 INCALF (Confirmed Pregnant)",
                        "DRY" to "🌾 DRY (Non-Lactating Gestation)",
                        "CALF" to "🍼 CALF (Young Stock)",
                        "HEIFER" to "🌿 HEIFER (Pre-calving Female)",
                        "BULL" to "🐂 BULL (Breeding Male)",
                        "DISPOSED" to "🚫 DISPOSED (Culled / Sold)"
                    )

                    stages.forEach { (stageKey, stageLabel) ->
                        val isSelected = currentStatus.equals(stageKey, ignoreCase = true) ||
                            (cattleEval?.stage?.name.equals(stageKey, ignoreCase = true))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) ForestGreenPrimary.copy(alpha = 0.12f) else Color(0xFFF8FAFC),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) ForestGreenPrimary else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .clickable {
                                    val newStatus = when (stageKey) {
                                        "INCALF_MILKING" -> "INCALF / MILKING"
                                        "INCALF" -> "INCALF"
                                        "DRY" -> "DRY"
                                        "MILKING" -> "MILKING"
                                        "HEIFER" -> "HEIFER"
                                        "CALF" -> "CALF"
                                        "BULL" -> "BULL"
                                        "DISPOSED" -> "DISPOSED"
                                        else -> stageKey
                                    }
                                    currentStatus = newStatus
                                    onUpdateAnimalStage(newStatus, if (stageKey.contains("INCALF")) "Confirmed Pregnant" else newStatus)
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

    // Initialize sample events if empty
    LaunchedEffect(animal.id) {
        if (animalEvents.isEmpty() && isCattle) {
            animalEvents.addAll(
                listOf(
                    CattleEventItem(
                        id = "e1_${animal.id}",
                        category = "HEAT",
                        title = "Estrus (Heat Period) Observed",
                        date = "02 Aug 2026",
                        details = "Clear mucus discharge and standing heat recorded during morning check.",
                        notes = "Observed by Worker John"
                    ),
                    CattleEventItem(
                        id = "e2_${animal.id}",
                        category = "INSEMINATION",
                        title = "Artificial Insemination (AI)",
                        date = "03 Aug 2026",
                        details = "Inseminated with Friesian Bull Straw #FRIESIAN-88 (Sire: Thunder #045).",
                        notes = "Technician: Dr. Otieno (Vet)",
                        metricValue = "Straw #88"
                    ),
                    CattleEventItem(
                        id = "e3_${animal.id}",
                        category = "WEIGHT",
                        title = "Routine Weight Measurement",
                        date = "28 Jul 2026",
                        details = "Gained +15kg over last 30 days. Good Body Condition Score (3.5/5).",
                        notes = "Recorded by Tech",
                        metricValue = animal.weight.ifBlank { "520 kg" }
                    ),
                    CattleEventItem(
                        id = "e4_${animal.id}",
                        category = "HEALTH",
                        title = "Foot & Mouth Vaccination",
                        date = "14 May 2026",
                        details = "Administered 2ml FMD vaccine booster subcutaneously.",
                        notes = "Batch #FMD-2026-X",
                        metricValue = "2 ml"
                    )
                )
            )
        }
    }

    val cattleNotifications = remember(cattleEval, animalEvents.toList()) {
        val list = mutableListOf<UpcomingCattleNotification>()
        if (cattleEval != null) {
            if (cattleEval.expectedCalvingDate != null) {
                list.add(
                    UpcomingCattleNotification(
                        id = "notif_calving",
                        title = "Expected Calving Date",
                        dueDate = cattleEval.expectedCalvingDate,
                        category = "CALVING",
                        badgeColor = Color(0xFFFEF3C7),
                        badgeTextColor = Color(0xFFB45309)
                    )
                )
            }
            if (cattleEval.dryOffTargetDate != null) {
                list.add(
                    UpcomingCattleNotification(
                        id = "notif_dry",
                        title = "Target Dry Off Date (Rest Period)",
                        dueDate = cattleEval.dryOffTargetDate,
                        category = "DRY_OFF",
                        badgeColor = Color(0xFFDCFCE7),
                        badgeTextColor = Color(0xFF15803D)
                    )
                )
            }
            if (cattleEval.stage == CattleStage.INCALF || cattleEval.stage == CattleStage.INCALF_MILKING) {
                list.add(
                    UpcomingCattleNotification(
                        id = "notif_pd",
                        title = "Routine Pregnancy Check / Vet Follow-up",
                        dueDate = "Next Vet Visit",
                        category = "INSEMINATION_PD",
                        badgeColor = Color(0xFFE0F2FE),
                        badgeTextColor = Color(0xFF0369A1)
                    )
                )
            }
        }
        if (list.isEmpty()) {
            list.add(
                UpcomingCattleNotification(
                    id = "notif_default_1",
                    title = "Monthly Weight & Deworming Check",
                    dueDate = "Aug 28, 2026",
                    category = "WEIGHT",
                    badgeColor = Color(0xFFDCFCE7),
                    badgeTextColor = Color(0xFF15803D)
                )
            )
        }
        list
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1E293B))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Animal Details",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onEditAnimal,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFECFDF5))
                            .border(1.dp, ForestGreenPrimary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .testTag("edit_animal_topbar_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit Animal",
                            tint = ForestGreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDeleteAnimal,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFEF2F2))
                            .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(8.dp))
                            .testTag("delete_animal_topbar_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeleteForever,
                            contentDescription = "Delete Animal",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (!currentStatus.contains("DISPOSED", ignoreCase = true)) {
                        Button(
                            onClick = { showDisposeDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("🚫 DISPOSE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
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
                                    color = if (isCattle && cattleEval != null) cattleEval.badgeBgColor else TagLivestockBg,
                                    modifier = Modifier.clickable {
                                        if (isCattle && cattleEval != null) {
                                            showStageInfoDialog = true
                                        } else {
                                            showUpdateStageDialog = true
                                        }
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isCattle && cattleEval != null) cattleEval.stage.displayName else currentStatus,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCattle && cattleEval != null) cattleEval.badgeTextColor else TagLivestockText
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            Icons.Filled.Info,
                                            contentDescription = "Stage Details",
                                            tint = if (isCattle && cattleEval != null) cattleEval.badgeTextColor else TagLivestockText,
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

                        val filteredEvents = animalEvents.filter {
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

        // Breeding Status Summary Card (Dynamic based on records)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isCattle && cattleEval != null) cattleEval.badgeBgColor else Color(0xFFDCFCE7)
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.SentimentSatisfied,
                                contentDescription = null,
                                tint = if (isCattle && cattleEval != null) cattleEval.badgeTextColor else ForestGreenPrimary
                            )
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
                            color = if (isCattle && cattleEval != null) cattleEval.badgeBgColor else Color(0xFFDCFCE7)
                        ) {
                            Text(
                                text = if (isCattle && cattleEval != null) cattleEval.breedingStatusText else animal.breedingStatus,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCattle && cattleEval != null) cattleEval.badgeTextColor else ForestGreenPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (isCattle && cattleEval != null) {
                        // AI / Breeding Date
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Last Insemination / Mating", fontSize = 13.sp, color = Color(0xFF64748B))
                            Text(
                                cattleEval.lastInseminationDate ?: "None Recorded",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (cattleEval.lastInseminationDate != null) Color(0xFF1E293B) else Color(0xFF94A3B8)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Gestation Progress if pregnant
                        if (cattleEval.daysInGestation != null && cattleEval.daysInGestation > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Gestation Progress", fontSize = 13.sp, color = Color(0xFF64748B))
                                Text(
                                    "Day ${cattleEval.daysInGestation} / 283 (${(cattleEval.daysInGestation * 100 / 283).coerceIn(0, 100)}%)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0369A1)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { (cattleEval.daysInGestation / 283f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFF0284C7),
                                trackColor = Color(0xFFE0F2FE)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Expected Calving
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Expected Calving Date", fontSize = 13.sp, color = Color(0xFF64748B))
                            Text(
                                cattleEval.expectedCalvingDate ?: "N/A",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (cattleEval.expectedCalvingDate != null) ForestGreenPrimary else Color(0xFF94A3B8)
                            )
                        }

                        if (cattleEval.dryOffTargetDate != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Recommended Dry-Off Date", fontSize = 13.sp, color = Color(0xFF64748B))
                                Text(
                                    cattleEval.dryOffTargetDate,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick action button
                        OutlinedButton(
                            onClick = { showAddCattleEventDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ForestGreenPrimary)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, tint = ForestGreenPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("+ LOG HEAT / AI / PD CHECK", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                        }
                    } else {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Breeding Status", fontSize = 13.sp, color = Color(0xFF64748B))
                            Text(animal.breedingStatus, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                        }
                    }
                }
            }
        }

        // Yield Productivity 7-Days Bar Chart (Dynamic Data with Real Values)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
                val shortDayFormat = remember { SimpleDateFormat("EEE", Locale.getDefault()) }

                val last7DaysData = remember(animal, milkLogs, eggLogs, isPoultry) {
                    (6 downTo 0).map { dayOffset ->
                        val c = java.util.Calendar.getInstance()
                        c.add(java.util.Calendar.DAY_OF_YEAR, -dayOffset)
                        val fullDate = dateFormat.format(c.time)
                        val dayName = shortDayFormat.format(c.time)

                        val yieldVal = if (isPoultry) {
                            val matched = eggLogs.filter { log ->
                                (log.unitName.equals(animal.name, ignoreCase = true) || log.unitName.contains(animal.name, ignoreCase = true) || animal.name.contains(log.unitName, ignoreCase = true)) &&
                                (log.loggedAt.contains(fullDate, ignoreCase = true) || log.loggedAt.contains(dayName, ignoreCase = true))
                            }
                            matched.sumOf { it.totalEggs }.toFloat()
                        } else {
                            val cleanAnimal = animal.name.lowercase()
                            val cleanTag = animal.tagNumber.lowercase().replace("#", "").trim()
                            val matched = milkLogs.filter { log ->
                                val logName = log.cowName.lowercase()
                                (logName.contains(cleanAnimal) || cleanAnimal.contains(logName) || (cleanTag.isNotEmpty() && logName.contains(cleanTag))) &&
                                (log.date.equals(fullDate, ignoreCase = true) || log.date.contains(fullDate, ignoreCase = true))
                            }
                            matched.sumOf { it.litres }.toFloat()
                        }

                        Triple(dayName, yieldVal, fullDate)
                    }
                }

                val maxYield = (last7DaysData.map { it.second }.maxOrNull() ?: 10f).coerceAtLeast(if (isPoultry) 50f else 10f)
                val total7Days = last7DaysData.sumOf { it.second.toDouble() }
                val lastLoggedVal = last7DaysData.lastOrNull()?.second ?: 0f

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
                                text = if (isPoultry) {
                                    if (lastLoggedVal > 0) "Today: ${lastLoggedVal.toInt()} Eggs (${"%.1f".format(lastLoggedVal / 30.0)} Trays)"
                                    else "Total 7-Day: ${total7Days.toInt()} Eggs"
                                } else {
                                    if (lastLoggedVal > 0) "Today: ${"%.1f".format(lastLoggedVal)}L"
                                    else "Total 7-Day: ${"%.1f".format(total7Days)}L"
                                },
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        last7DaysData.forEachIndexed { idx, (day, valAmt, _) ->
                            val heightRatio = (valAmt / maxYield).coerceIn(if (valAmt > 0) 0.15f else 0.04f, 1f)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (valAmt > 0f) {
                                    Text(
                                        text = if (isPoultry) "${valAmt.toInt()}" else "%.1f".format(valAmt),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPoultry) Color(0xFF92400E) else ForestGreenPrimary
                                    )
                                } else {
                                    Text("-", fontSize = 9.sp, color = Color(0xFF94A3B8))
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Box(
                                    modifier = Modifier
                                        .width(22.dp)
                                        .height((90 * heightRatio).dp)
                                        .background(
                                            if (valAmt == 0f) Color(0xFFE2E8F0)
                                            else if (isPoultry) Color(0xFFD97706)
                                            else ForestGreenPrimary,
                                            RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                        )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = day,
                                    fontSize = 11.sp,
                                    fontWeight = if (idx == 6) FontWeight.Bold else FontWeight.Normal,
                                    color = if (idx == 6) Color(0xFF1E293B) else Color(0xFF64748B)
                                )
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
                val newEvent = CattleEventItem(
                    id = "e_${System.currentTimeMillis()}",
                    category = type,
                    title = title,
                    date = date,
                    details = details,
                    notes = notes,
                    metricValue = metricValue
                )
                animalEvents.add(0, newEvent)

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

                // If event is pregnancy diagnosis positive, dry off, calving, etc., the reactive cattleEval will update automatically
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
    onEditFlock: () -> Unit = {},
    onDeleteFlock: () -> Unit = {},
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
                    Row(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "🐔 Poultry Flock • ${flock.breed}",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onEditFlock,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFECFDF5))
                                .border(1.dp, ForestGreenPrimary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .testTag("edit_flock_topbar_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit Flock",
                                tint = ForestGreenPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onDeleteFlock,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFEF2F2))
                                .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(8.dp))
                                .testTag("delete_flock_topbar_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteForever,
                                contentDescription = "Delete Flock",
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Button(
                            onClick = { showDisposeFlockDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("🏷️ DISPOSE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
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
