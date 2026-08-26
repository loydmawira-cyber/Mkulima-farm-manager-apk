package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.LaunchedEffect
import com.example.ui.FarmViewModel
import com.example.ui.components.AddCattleEventDialog
import com.example.ui.components.CalvingCalfInfo
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.AlertDialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.CachePolicy
import coil.request.Precision
import com.example.R
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import com.example.ui.util.ImageStorageUtils
import com.example.ui.components.CameraCaptureDialog
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.toMutableStateList
import com.example.data.CattleEvent
import com.example.data.EggLog
import com.example.data.EmployeeRequest
import com.example.data.FarmUnit
import com.example.data.FinanceRecord
import com.example.data.FinanceType
import com.example.data.MilkLog
import com.example.data.PoultryLog
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

fun parseEventDateForSorting(dateStr: String): Long {
    if (dateStr.isBlank()) return 0L
    val clean = dateStr.trim()
    val formats = listOf(
        "dd MMM yyyy, hh:mm a",
        "dd MMM yyyy, HH:mm",
        "dd MMM yyyy",
        "d MMM yyyy",
        "yyyy-MM-dd",
        "dd/MM/yyyy",
        "MMM dd, yyyy",
        "dd MMM, hh:mm a",
        "dd MMM"
    )
    for (pattern in formats) {
        try {
            val sdf = SimpleDateFormat(pattern, Locale.getDefault())
            val parsed = sdf.parse(clean)
            if (parsed != null) {
                return parsed.time
            }
        } catch (_: Exception) {}
    }
    if (clean.startsWith("Today", ignoreCase = true)) {
        return System.currentTimeMillis()
    }
    if (clean.startsWith("Yesterday", ignoreCase = true)) {
        return System.currentTimeMillis() - 86400000L
    }
    return 0L
}

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
    val headCountInt: Int = 1,
    val manuallySetStatus: String? = null,
    val photoUri: String? = null,
    val notes: String = ""
)

data class FlockDisposalLogItem(
    val id: String,
    val recordId: Long,
    val flockName: String,
    val quantity: Int,
    val reason: String, // "Sold", "Death", "Home Consumption", "Other"
    val amount: Double,
    val date: String,
    val notes: String,
    val linkedMortalityLogId: String? = null
)

val mockAnimals: List<AnimalDetailData> = emptyList()

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
    var showConfirmDialog by remember { mutableStateOf(false) }

    if (showConfirmDialog) {
        val amount = amountText.toDoubleOrNull() ?: 0.0
        com.example.ui.components.ConfirmDeleteDialog(
            title = "Confirm Animal Disposal",
            message = "Are you sure you want to record the disposal of $animalName ($tagNumber) as '$reason'${if (reason == "Sold" && amount > 0) " for KSh %,.2f".format(amount) else ""}? This record will be archived.",
            confirmButtonText = "Confirm Disposal",
            confirmButtonColor = Color(0xFFDC2626),
            onConfirm = {
                showConfirmDialog = false
                onConfirmDispose(reason, amount, notesText, dateText)
            },
            onDismiss = {
                showConfirmDialog = false
            }
        )
    }

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
                            showConfirmDialog = true
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
    var showConfirmDialog by remember { mutableStateOf(false) }

    if (showConfirmDialog) {
        val qty = qtyText.toIntOrNull() ?: 0
        val amount = amountText.toDoubleOrNull() ?: 0.0
        com.example.ui.components.ConfirmDeleteDialog(
            title = "Confirm Flock Disposal",
            message = "Are you sure you want to dispose $qty birds from $flockName as '$reason'${if (amount > 0) " for KSh %,.2f".format(amount) else ""}? This will update the active flock size.",
            confirmButtonText = "Confirm Disposal",
            confirmButtonColor = Color(0xFFDC2626),
            onConfirm = {
                showConfirmDialog = false
                onConfirmDisposeFlock(qty, reason, amount, notesText, dateText)
            },
            onDismiss = {
                showConfirmDialog = false
            }
        )
    }

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
                            showConfirmDialog = true
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
    viewModel: FarmViewModel,
    userRole: String, // Add userRole
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
    onDeleteUnit: (Long) -> Unit,
    farmSettings: com.example.data.FarmSettings,
    modifier: Modifier = Modifier
) {
    var selectedAnimal by remember { mutableStateOf<AnimalDetailData?>(null) }
    var animalForOptions by remember { mutableStateOf<AnimalDetailData?>(null) }
    var animalToEdit by remember { mutableStateOf<AnimalDetailData?>(null) }
    var animalToDelete by remember { mutableStateOf<AnimalDetailData?>(null) }
    var animalToDispose by remember { mutableStateOf<AnimalDetailData?>(null) }
    val initialCategory = if (farmSettings.farmType.equals("Poultry Only", ignoreCase = true)) "POULTRY" else "CATTLE"
    var selectedFilterCategory by remember(farmSettings.farmType) { mutableStateOf(initialCategory) }
    var selectedCattleStage by remember { mutableStateOf("ALL") }
    var showCategoryGuideDialog by remember { mutableStateOf(false) }

    val hasFlocksOverlay = (selectedAnimal != null) || (animalForOptions != null) ||
            (animalToEdit != null) || (animalToDelete != null) || (animalToDispose != null) ||
            showCategoryGuideDialog

    BackHandler(enabled = hasFlocksOverlay) {
        when {
            animalForOptions != null -> animalForOptions = null
            animalToEdit != null -> animalToEdit = null
            animalToDelete != null -> animalToDelete = null
            animalToDispose != null -> animalToDispose = null
            showCategoryGuideDialog -> showCategoryGuideDialog = false
            selectedAnimal != null -> selectedAnimal = null
        }
    }

    LaunchedEffect(farmSettings.farmType) {
        if (farmSettings.farmType.equals("Poultry Only", ignoreCase = true)) {
            selectedFilterCategory = "POULTRY"
        } else if (farmSettings.farmType.equals("Cattle Only", ignoreCase = true)) {
            selectedFilterCategory = "CATTLE"
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val deletedPrefs = remember { context.getSharedPreferences("mkulima_deleted_animals", android.content.Context.MODE_PRIVATE) }
    var deletedSet by remember {
        mutableStateOf(
            try {
                deletedPrefs.getStringSet("deleted_ids", emptySet()) ?: emptySet()
            } catch (e: Exception) {
                val raw = try { deletedPrefs.getString("deleted_ids", "") ?: "" } catch (ex: Exception) { "" }
                if (raw.isNotBlank()) raw.split(",").toSet() else emptySet()
            }
        )
    }

    val allDbCattleEvents by viewModel.allCattleEvents.collectAsStateWithLifecycle(initialValue = emptyList())
    val allDbPoultryLogs by viewModel.allPoultryLogs.collectAsStateWithLifecycle(initialValue = emptyList())
    val reminderCompletions by viewModel.reminderCompletions.collectAsStateWithLifecycle(initialValue = emptyList())

    val roomAnimals = remember(units, milkLogs, allDbCattleEvents) {
        units.map { unit ->
            val isPoultry = unit.type.equals("POULTRY", ignoreCase = true) || unit.type.contains("Poultry", ignoreCase = true)
            val cowLogs = milkLogs.filter { it.cowName.equals(unit.name, ignoreCase = true) }
            val unitDbEvents = allDbCattleEvents.filter { it.unitId == unit.id }.map {
                CattleEventItem(
                    id = it.id.toString(),
                    category = it.category,
                    title = it.title,
                    date = it.date,
                    details = it.details,
                    notes = it.notes ?: "",
                    metricValue = it.metricValue ?: ""
                )
            }
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

            val animalDetail = AnimalDetailData(
                id = "unit_${unit.id}",
                name = unit.name,
                tagNumber = if (unit.tagNumber.isNotBlank()) unit.tagNumber else if (isPoultry) "Count: ${unit.headCount}" else "#${unit.id + 100}",
                breed = unit.breed.ifBlank { if (isPoultry) "Poultry Flock" else "Local Breed" },
                category = if (isPoultry) "POULTRY" else "CATTLE",
                status = unit.healthStatus.ifBlank { "ACTIVE" },
                age = calculatedAge,
                weight = unitDbEvents
                    .filter { it.category.equals("WEIGHT", ignoreCase = true) }
                    .maxByOrNull { CattleLifecycleEngine.parseDateOrNull(it.date)?.time ?: 0L }
                    ?.metricValue
                    ?.takeIf { it.isNotBlank() }
                    ?: unit.currentWeight.ifBlank { if (isPoultry) "1.8kg avg" else "450kg" },
                lastMilk = lastMilkStr,
                breedingStatus = "HEALTHY",
                dateOfBirth = unit.dob.ifBlank { "12 Apr 2023" },
                weightAtBirth = unit.weightAtBirth.ifBlank { "32 kg" },
                sire = unit.sire.ifBlank { "N/A" },
                dam = unit.dam.ifBlank { "N/A" },
                headCountInt = unit.headCount,
                photoUri = unit.photoUri,
                notes = unit.notes
            )

            val eval = CattleLifecycleEngine.evaluateCattleStage(animalDetail, unitDbEvents, cowLogs)

            // Dynamic Stage Update
            val newStatus = if (unitDbEvents.isNotEmpty() || animalDetail.status.isBlank() || animalDetail.status.equals("ACTIVE", ignoreCase = true)) eval.stage.displayName else animalDetail.status

            animalDetail.copy(
                status = newStatus,
                breedingStatus = if (unitDbEvents.isNotEmpty() || animalDetail.breedingStatus.isBlank() || animalDetail.breedingStatus.equals("HEALTHY", ignoreCase = true)) eval.breedingStatusText else animalDetail.breedingStatus
            )
        }
    }

    val initialAnimals = remember(units, milkLogs, allDbCattleEvents, deletedSet) {
        (roomAnimals + mockAnimals)
            .filter { !deletedSet.contains(it.id) && !deletedSet.contains(it.name.lowercase()) }
            .distinctBy { it.name }
    }

    val mutableAnimals = remember { mutableStateListOf<AnimalDetailData>().apply { addAll(initialAnimals) } }

    val allAnimalEventsMap = remember {
        mutableStateMapOf<String, SnapshotStateList<CattleEventItem>>()
    }

    LaunchedEffect(units, deletedSet, roomAnimals) {
        roomAnimals.forEach { rAnimal ->
            if (!deletedSet.contains(rAnimal.id) && !deletedSet.contains(rAnimal.name.lowercase())) {
                val idx = mutableAnimals.indexOfFirst { it.id == rAnimal.id }
                if (idx >= 0) {
                    mutableAnimals[idx] = rAnimal
                } else {
                    val nameIdx = mutableAnimals.indexOfFirst { it.name.equals(rAnimal.name, ignoreCase = true) }
                    if (nameIdx >= 0) {
                        mutableAnimals[nameIdx] = rAnimal
                    } else {
                        mutableAnimals.add(rAnimal)
                    }
                }
            }
        }
        mutableAnimals.removeAll { deletedSet.contains(it.id) || deletedSet.contains(it.name.lowercase()) }
        if (selectedAnimal != null) {
            val curr = mutableAnimals.find { it.id == selectedAnimal?.id || it.name.equals(selectedAnimal?.name, ignoreCase = true) }
            if (curr != null) {
                selectedAnimal = curr
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
        headCount: Int,
        photoUri: String? = null,
        notes: String? = null
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
            photoUri = if (photoUri != null) photoUri else existing.photoUri,
            notes = notes ?: existing.notes,
            lastMilk = if (category.equals("POULTRY", ignoreCase = true)) "$headCount Birds" else existing.lastMilk
        )
        val idx = mutableAnimals.indexOfFirst { it.id == animalId }
        if (idx >= 0) {
            mutableAnimals[idx] = updated
        }
        if (selectedAnimal?.id == animalId) {
            selectedAnimal = updated
        }

        // Keep the filter category matching if animal category was changed
        if (category.equals("POULTRY", ignoreCase = true)) {
            if (selectedFilterCategory == "CATTLE" && farmSettings.farmType.contains("Both", ignoreCase = true)) {
                selectedFilterCategory = "POULTRY"
            }
        } else if (category.equals("CATTLE", ignoreCase = true)) {
            if (selectedFilterCategory == "POULTRY" && farmSettings.farmType.contains("Both", ignoreCase = true)) {
                selectedFilterCategory = "CATTLE"
            }
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
                        dam = dam,
                        photoUri = if (photoUri != null) photoUri else matching.photoUri,
                        notes = notes ?: matching.notes
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
                    dam = dam,
                    photoUri = if (photoUri != null) photoUri else matching.photoUri,
                    notes = notes ?: matching.notes
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
        if (animal.id.startsWith("unit_")) {
            val uId = animal.id.removePrefix("unit_").toLongOrNull()
            if (uId != null) {
                val matching = units.find { it.id == uId }
                if (matching != null) {
                    onUpdateUnit(matching.copy(healthStatus = "DISPOSED ($reason)"))
                }
            }
        } else {
            val matching = units.find { it.name.equals(animal.name, ignoreCase = true) }
            if (matching != null) {
                onUpdateUnit(matching.copy(healthStatus = "DISPOSED ($reason)"))
            }
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
        } else {
            val existing = mutableAnimals.find { it.id == animalId }
            if (existing != null) {
                val u = units.find { it.name.equals(existing.name, ignoreCase = true) }
                if (u != null) {
                    onUpdateUnit(u.copy(healthStatus = newStatus))
                }
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

    // Cattle category stage breakdown calculations evaluated live from mutableAnimals
    val cattleList = mutableAnimals.filter { it.category.equals("CATTLE", ignoreCase = true) }
    val poultryList = mutableAnimals.filter { it.category.equals("POULTRY", ignoreCase = true) || it.breed.contains("Layer", ignoreCase = true) || it.breed.contains("Flock", ignoreCase = true) }

    val evaluatedCattleMap = remember(cattleList, allDbCattleEvents, milkLogs, allAnimalEventsMap) {
        cattleList.associate { animal ->
            val numericUnitId = animal.id.removePrefix("unit_").toLongOrNull()
            val dbEvs = if (numericUnitId != null) {
                allDbCattleEvents.filter { it.unitId == numericUnitId }.map {
                    CattleEventItem(
                        id = it.id.toString(),
                        category = it.category,
                        title = it.title,
                        date = it.date,
                        details = it.details,
                        notes = it.notes ?: "",
                        metricValue = it.metricValue ?: ""
                    )
                }
            } else emptyList()
            val rawId = animal.id.removePrefix("unit_")
            val mockEvs = allAnimalEventsMap[animal.id] ?: allAnimalEventsMap[rawId] ?: emptyList()
            val combinedEvs = (dbEvs + mockEvs).distinctBy { it.id }
            val cowMilkLogs = milkLogs.filter { it.cowName.equals(animal.name, ignoreCase = true) }
            animal.id to CattleLifecycleEngine.evaluateCattleStage(animal, combinedEvs, if (cowMilkLogs.isNotEmpty()) cowMilkLogs else milkLogs)
        }
    }

    val inCalfMilkingCount = evaluatedCattleMap.values.count { it.stage == CattleStage.INCALF_MILKING }
    val inCalfCount = evaluatedCattleMap.values.count { it.stage == CattleStage.INCALF || (it.stage == CattleStage.DRY && it.isInCalf) }
    val totalInCalfCount = evaluatedCattleMap.values.count { it.isInCalf }
    val milkingCount = evaluatedCattleMap.values.count { it.isMilking || it.stage == CattleStage.MILKING || it.stage == CattleStage.INCALF_MILKING }
    val heiferCount = evaluatedCattleMap.values.count { it.stage == CattleStage.HEIFER }
    val calfCount = evaluatedCattleMap.values.count { it.stage == CattleStage.CALF }
    val dryCount = evaluatedCattleMap.values.count { it.stage == CattleStage.DRY || it.isDriedOff }
    val inseminatedCount = evaluatedCattleMap.values.count { it.stage == CattleStage.INSEMINATED || (it.lastInseminationDate != null && !it.isInCalf) }
    val bullCount = evaluatedCattleMap.values.count { it.stage == CattleStage.BULL }
    val disposedCount = evaluatedCattleMap.values.count { it.stage == CattleStage.DISPOSED }

    val poultryFlocksCount = poultryList.size
    val poultryLayingCount = poultryList.count { it.status.contains("Laying", ignoreCase = true) || it.breedingStatus.contains("Laying", ignoreCase = true) || it.status.equals("ACTIVE", ignoreCase = true) }
    val poultryTotalBirds = poultryList.sumOf { it.headCountInt }

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
            userRole = userRole,
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
            onSaveAnimal = { name, tagNumber, breed, category, status, breedingStatus, age, dob, weightAtBirth, currentWeight, sire, dam, headCount, photoUri, notes ->
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
                    headCount = headCount,
                    photoUri = photoUri,
                    notes = notes
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
            val selectedPoultryUnitId = selectedAnimal!!.id.removePrefix("unit_").toLongOrNull() ?: 0L
            FlockDetailsView(
                flock = selectedAnimal!!,
                userRole = userRole,
                eggLogs = eggLogs,
                financeRecords = financeRecords,
                poultryLogs = allDbPoultryLogs.filter { it.unitId == selectedPoultryUnitId },
                completedVaccineRuleIds = reminderCompletions
                    .asSequence()
                    .filter { it.unitId == selectedPoultryUnitId }
                    .mapNotNull { completion ->
                        completion.ruleKey
                            .takeIf { it.startsWith("poultry_vac_${selectedPoultryUnitId}_") }
                            ?.removePrefix("poultry_vac_${selectedPoultryUnitId}_")
                    }
                    .toSet(),
                onMarkVaccinationComplete = { ruleId ->
                    if (selectedPoultryUnitId > 0) {
                        viewModel.markReminderComplete(
                            ruleKey = "poultry_vac_${selectedPoultryUnitId}_${ruleId}",
                            unitId = selectedPoultryUnitId
                        )
                    }
                },
                onClearVaccinationComplete = { ruleId ->
                    if (selectedPoultryUnitId > 0) {
                        viewModel.clearReminderCompletion(
                            ruleKey = "poultry_vac_${selectedPoultryUnitId}_${ruleId}"
                        )
                    }
                },
                onAddPoultryLog = { viewModel.addPoultryLog(it) },
                onUpdatePoultryLog = { viewModel.updatePoultryLog(it) },
                onDeletePoultryLog = { viewModel.deletePoultryLog(it) },
                onUpdateFlockHeadCount = { newHeadCount ->
                    if (selectedPoultryUnitId > 0) onUpdateUnitHeadCount(selectedPoultryUnitId, newHeadCount)
                },
                onBackClick = { selectedAnimal = null },
                onAddEggLogClick = onAddEggLogClick,
                onAddFinanceClick = onAddFinanceClick,
                onDisposeFlock = { qty, reason, amount, notes, date ->
                    handleDisposeFlock(selectedAnimal!!, qty, reason, amount, notes, date)
                },
                onEditFlock = { animalToEdit = selectedAnimal },
                onDeleteFlock = { animalToDelete = selectedAnimal },
                onUpdatePhoto = { newPhoto ->
                    handleModifyAnimal(
                        animalId = selectedAnimal!!.id,
                        name = selectedAnimal!!.name,
                        tagNumber = selectedAnimal!!.tagNumber,
                        breed = selectedAnimal!!.breed,
                        category = selectedAnimal!!.category,
                        status = selectedAnimal!!.status,
                        breedingStatus = selectedAnimal!!.breedingStatus,
                        age = selectedAnimal!!.age,
                        dob = selectedAnimal!!.dateOfBirth,
                        weightAtBirth = selectedAnimal!!.weightAtBirth,
                        currentWeight = selectedAnimal!!.weight,
                        sire = selectedAnimal!!.sire,
                        dam = selectedAnimal!!.dam,
                        headCount = selectedAnimal!!.headCountInt,
                        photoUri = newPhoto
                    )
                },
                modifier = modifier
            )
        } else {
            AnimalDetailsView(
                viewModel = viewModel,
                animal = selectedAnimal!!,
                userRole = userRole,
                milkLogs = milkLogs,
                eggLogs = eggLogs,
                onUpdateAnimalStage = { newStatus, newBreedingStatus ->
                    handleUpdateAnimalStage(selectedAnimal!!.id, newStatus, newBreedingStatus)
                },
                onBackClick = { selectedAnimal = null },
                onDisposeAnimal = { reason, amount, notes, date ->
                    handleDisposeAnimal(selectedAnimal!!, reason, amount, notes, date)
                },
                onEditAnimal = { animalToEdit = selectedAnimal },
                onDeleteAnimal = { animalToDelete = selectedAnimal },
                onUpdatePhoto = { newPhoto ->
                    handleModifyAnimal(
                        animalId = selectedAnimal!!.id,
                        name = selectedAnimal!!.name,
                        tagNumber = selectedAnimal!!.tagNumber,
                        breed = selectedAnimal!!.breed,
                        category = selectedAnimal!!.category,
                        status = selectedAnimal!!.status,
                        breedingStatus = selectedAnimal!!.breedingStatus,
                        age = selectedAnimal!!.age,
                        dob = selectedAnimal!!.dateOfBirth,
                        weightAtBirth = selectedAnimal!!.weightAtBirth,
                        currentWeight = selectedAnimal!!.weight,
                        sire = selectedAnimal!!.sire,
                        dam = selectedAnimal!!.dam,
                        headCount = selectedAnimal!!.headCountInt,
                        photoUri = newPhoto
                    )
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

                    // Category Filter Chips [ CATTLE ] [ POULTRY ]
                    val availableCategories = when {
                        farmSettings.farmType.equals("Cattle Only", ignoreCase = true) -> listOf("CATTLE")
                        farmSettings.farmType.equals("Poultry Only", ignoreCase = true) -> listOf("POULTRY")
                        else -> listOf("CATTLE", "POULTRY")
                    }

                    if (availableCategories.size > 1) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            availableCategories.forEach { cat ->
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
                    }

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
                    val matchesMode = when {
                        farmSettings.farmType.equals("Cattle Only", ignoreCase = true) -> animal.category.equals("CATTLE", ignoreCase = true)
                        farmSettings.farmType.equals("Poultry Only", ignoreCase = true) -> animal.category.equals("POULTRY", ignoreCase = true) || animal.breed.contains("Layer", ignoreCase = true) || animal.breed.contains("Flock", ignoreCase = true)
                        else -> true
                    }
                    if (!matchesMode) return@filter false

                    val matchesCategory = animal.category.equals(selectedFilterCategory, ignoreCase = true)
                    if (!matchesCategory) return@filter false
                    if (!animal.category.equals("CATTLE", ignoreCase = true)) return@filter true

                    val eval = evaluatedCattleMap[animal.id] ?: run {
                        val numericUnitId = animal.id.removePrefix("unit_").toLongOrNull()
                        val dbEvs = if (numericUnitId != null) {
                            allDbCattleEvents.filter { it.unitId == numericUnitId }.map {
                                CattleEventItem(
                                    id = it.id.toString(),
                                    category = it.category,
                                    title = it.title,
                                    date = it.date,
                                    details = it.details,
                                    notes = it.notes ?: "",
                                    metricValue = it.metricValue ?: ""
                                )
                            }
                        } else emptyList()
                        val rawId = animal.id.removePrefix("unit_")
                        val mockEvs = allAnimalEventsMap[animal.id] ?: allAnimalEventsMap[rawId] ?: emptyList()
                        val combinedEvs = (dbEvs + mockEvs).distinctBy { it.id }
                        val cowMilkLogs = milkLogs.filter { it.cowName.equals(animal.name, ignoreCase = true) }
                        CattleLifecycleEngine.evaluateCattleStage(animal, combinedEvs, if (cowMilkLogs.isNotEmpty()) cowMilkLogs else milkLogs)
                    }
                    when (selectedCattleStage) {
                        "MILKING" -> eval.isMilking || eval.stage == CattleStage.MILKING || eval.stage == CattleStage.INCALF_MILKING
                        "INCALF" -> eval.isInCalf || eval.stage == CattleStage.INCALF || eval.stage == CattleStage.INCALF_MILKING
                        "HEIFER" -> eval.stage == CattleStage.HEIFER
                        "CALF" -> eval.stage == CattleStage.CALF
                        "DRY" -> eval.stage == CattleStage.DRY || eval.isDriedOff
                        "INSEMINATED" -> eval.stage == CattleStage.INSEMINATED || (eval.lastInseminationDate != null && !eval.isInCalf)
                        "BULL" -> eval.stage == CattleStage.BULL
                        "DISPOSED" -> eval.stage == CattleStage.DISPOSED
                        else -> true
                    }
                }

                items(filteredList, key = { it.id }) { animal ->
                    val isCattleItem = animal.category.equals("CATTLE", ignoreCase = true)
                    val cattleEval = if (isCattleItem) evaluatedCattleMap[animal.id] ?: run {
                        val numericUnitId = animal.id.removePrefix("unit_").toLongOrNull()
                        val dbEvs = if (numericUnitId != null) {
                            allDbCattleEvents.filter { it.unitId == numericUnitId }.map {
                                CattleEventItem(
                                    id = it.id.toString(),
                                    category = it.category,
                                    title = it.title,
                                    date = it.date,
                                    details = it.details,
                                    notes = it.notes ?: "",
                                    metricValue = it.metricValue ?: ""
                                )
                            }
                        } else emptyList()
                        val rawId = animal.id.removePrefix("unit_")
                        val mockEvs = allAnimalEventsMap[animal.id] ?: allAnimalEventsMap[rawId] ?: emptyList()
                        val combinedEvs = (dbEvs + mockEvs).distinctBy { it.id }
                        val cowMilkLogs = milkLogs.filter { it.cowName.equals(animal.name, ignoreCase = true) }
                        CattleLifecycleEngine.evaluateCattleStage(animal, combinedEvs, if (cowMilkLogs.isNotEmpty()) cowMilkLogs else milkLogs)
                    } else null

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
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (animal.category == "POULTRY") Color(0xFFFEF3C7) else Color(0xFFE8F5E9),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (animal.category == "POULTRY") Color(0xFFFDE68A) else Color(0xFFC8E6C9)),
                                    modifier = Modifier.size(50.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (!animal.photoUri.isNullOrBlank()) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(animal.photoUri)
                                                    .memoryCacheKey("animal-thumb-${animal.id}-${animal.photoUri}")
                                                    .diskCacheKey("animal-thumb-${animal.id}-${animal.photoUri}")
                                                    .size(100)
                                                    .precision(Precision.INEXACT)
                                                    .crossfade(false)
                                                    .placeholder(R.drawable.ic_livestock_placeholder)
                                                    .error(R.drawable.ic_livestock_placeholder)
                                                    .memoryCachePolicy(CachePolicy.ENABLED)
                                                    .diskCachePolicy(CachePolicy.ENABLED)
                                                    .networkCachePolicy(CachePolicy.ENABLED)
                                                    .build(),
                                                contentDescription = "${animal.name} Photo",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                                            )
                                        } else {
                                            Icon(
                                                imageVector = if (animal.category == "POULTRY") Icons.Filled.Egg else Icons.Filled.Pets,
                                                contentDescription = if (animal.category == "POULTRY") "Poultry Icon" else "Cattle Icon",
                                                tint = if (animal.category == "POULTRY") Color(0xFFD97706) else ForestGreenPrimary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
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
                                            text = if (userRole == "OWNER") "💡 Long press to Edit / Delete" else "💡 Tap to view full details",
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

                                if (userRole == "OWNER") {
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
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }

            if (userRole == "OWNER") {
                val isPoultrySection = selectedFilterCategory.equals("POULTRY", ignoreCase = true)
                val addLabel = if (isPoultrySection) "ADD FLOCK" else "ADD ANIMAL"
                FloatingActionButton(
                    onClick = onAddUnitClick,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(20.dp)
                        .testTag(if (isPoultrySection) "add_flock_fab" else "add_animal_fab"),
                    containerColor = ForestGreenPrimary,
                    contentColor = Color.White
                ) {
                    Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Icon(Icons.Filled.Add, contentDescription = addLabel)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(addLabel, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AnimalDetailsView(
    viewModel: FarmViewModel,
    animal: AnimalDetailData,
    userRole: String,
    onBackClick: () -> Unit,
    onDisposeAnimal: (reason: String, amount: Double, notes: String, date: String) -> Unit = { _, _, _, _ -> },
    onEditAnimal: () -> Unit = {},
    onDeleteAnimal: () -> Unit = {},
    onUpdatePhoto: (String?) -> Unit = {},
    milkLogs: List<MilkLog> = emptyList(),
    eggLogs: List<EggLog> = emptyList(),
    onUpdateAnimalStage: (newStatus: String, newBreedingStatus: String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val canEdit = userRole == "OWNER"

    val unitId = remember(animal.id) {
        animal.id.removePrefix("unit_").toLongOrNull() ?: ((animal.id.hashCode().toLong() and 0x7FFFFFFF) + 10000L)
    }
    val dbEvents by viewModel.getCattleEventsFlow(unitId).collectAsStateWithLifecycle(initialValue = emptyList())
    val animalEvents = remember(dbEvents) {
        dbEvents.map {
            CattleEventItem(
                id = it.id.toString(),
                category = it.category,
                title = it.title,
                date = it.date,
                details = it.details,
                notes = it.notes ?: "",
                metricValue = it.metricValue ?: ""
            )
        }.toMutableStateList()
    }

    var showAddCattleEventDialog by remember { mutableStateOf(false) }
    var cattleEventDialogCategory by remember { mutableStateOf("PD") }
    var eventToEdit by remember { mutableStateOf<CattleEventItem?>(null) }
    var eventToDelete by remember { mutableStateOf<CattleEventItem?>(null) }
    var showDeleteEventConfirmDialog by remember { mutableStateOf(false) }
    var selectedLogFilter by remember { mutableStateOf("ALL") } // ALL, CALVING, HEALTH, HEAT, PD, WEIGHT

    val sortedAnimalEvents = remember(animalEvents.toList()) {
        animalEvents.sortedWith(
            compareByDescending<CattleEventItem> { parseEventDateForSorting(it.date) }
                .thenByDescending { it.id.toLongOrNull() ?: 0L }
        )
    }

    val calvingLogs = remember(animalEvents.toList()) {
        animalEvents.filter { it.category.equals("CALVING", ignoreCase = true) }
            .sortedWith(
                compareByDescending<CattleEventItem> { parseEventDateForSorting(it.date) }
                    .thenByDescending { it.id.toLongOrNull() ?: 0L }
            )
    }

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

    // Keep animal status in sync with calculated stage if cattle and status is default/empty
    LaunchedEffect(cattleEval) {
        if (cattleEval != null && !animal.status.startsWith("DISPOSED", ignoreCase = true) && (animal.status.isBlank() || animal.status.equals("ACTIVE", ignoreCase = true))) {
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

    // Initialize events
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

                if (canEdit) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Edit Animal Button
                        Surface(
                            onClick = onEditAnimal,
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFECFDF5),
                            border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("edit_animal_topbar_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Edit Animal",
                                    tint = ForestGreenPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Edit",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreenPrimary
                                )
                            }
                        }

                        // Dispose Animal Button
                        if (!currentStatus.contains("DISPOSED", ignoreCase = true)) {
                            Surface(
                                onClick = { showDisposeDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFFFBEB),
                                border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                                modifier = Modifier
                                    .height(36.dp)
                                    .testTag("dispose_animal_topbar_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.RemoveCircleOutline,
                                        contentDescription = "Dispose Animal",
                                        tint = Color(0xFFB45309),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Dispose",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB45309)
                                    )
                                }
                            }
                        }

                        // Delete Animal Button
                        Surface(
                            onClick = onDeleteAnimal,
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEF2F2),
                            border = BorderStroke(1.dp, Color(0xFFFECACA)),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("delete_animal_topbar_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DeleteForever,
                                    contentDescription = "Delete Animal",
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Delete",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDC2626)
                                )
                            }
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

        // Animal Photo Header / Avatar Card
        item {
            val context = androidx.compose.ui.platform.LocalContext.current
            var showPhotoSourceDialog by remember { mutableStateOf(false) }
            var showCameraCaptureDialog by remember { mutableStateOf(false) }
            val photoGalleryLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri: Uri? ->
                if (uri != null) {
                    val saved = ImageStorageUtils.saveImageToInternalStorage(context, uri) ?: uri.toString()
                    onUpdatePhoto(saved)
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Animal Photo Avatar / Fallback Icon
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isPoultry) Color(0xFFFEF3C7) else Color(0xFFE8F5E9))
                                .border(
                                    1.dp,
                                    if (isPoultry) Color(0xFFFDE68A) else Color(0xFFC8E6C9),
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { showPhotoSourceDialog = true }
                                .testTag("animal_photo_avatar_box"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!animal.photoUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(animal.photoUri)
                                        .memoryCacheKey("animal-profile-${animal.id}-${animal.photoUri}")
                                        .diskCacheKey("animal-profile-${animal.id}-${animal.photoUri}")
                                        .size(640)
                                        .precision(Precision.INEXACT)
                                        .crossfade(false)
                                        .placeholder(R.drawable.ic_livestock_placeholder)
                                        .error(R.drawable.ic_livestock_placeholder)
                                        .memoryCachePolicy(CachePolicy.ENABLED)
                                        .diskCachePolicy(CachePolicy.ENABLED)
                                        .networkCachePolicy(CachePolicy.ENABLED)
                                        .build(),
                                    contentDescription = "${animal.name} Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = if (isPoultry) Icons.Filled.Egg else Icons.Filled.Pets,
                                        contentDescription = "Generic Animal Icon",
                                        tint = if (isPoultry) Color(0xFFD97706) else ForestGreenPrimary,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("No Photo", fontSize = 9.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        // Info & Photo Upload Trigger
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = animal.name,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Tag: ${animal.tagNumber}  •  ${animal.breed}",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { showPhotoSourceDialog = true },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .height(34.dp)
                                        .testTag("upload_animal_photo_button")
                                ) {
                                    Icon(
                                        imageVector = if (animal.photoUri.isNullOrBlank()) Icons.Filled.AddPhotoAlternate else Icons.Filled.CameraAlt,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (animal.photoUri.isNullOrBlank()) "Add Photo" else "Change",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (!animal.photoUri.isNullOrBlank()) {
                                    OutlinedButton(
                                        onClick = { onUpdatePhoto(null) },
                                        shape = RoundedCornerShape(10.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        modifier = Modifier
                                            .height(34.dp)
                                            .testTag("remove_animal_photo_button")
                                    ) {
                                        Text("Remove", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (animal.notes.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF0FDF4),
                    border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Notes / Origin", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(animal.notes, fontSize = 13.sp, color = Color(0xFF334155))
                    }
                }
            }

            if (showPhotoSourceDialog) {
                AlertDialog(
                    onDismissRequest = { showPhotoSourceDialog = false },
                    title = { Text("Update Animal Photo", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                    text = { Text("Choose a photo from your camera or gallery to identify ${animal.name}. If no photo is uploaded, a generic icon is displayed.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                showPhotoSourceDialog = false
                                showCameraCaptureDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                        ) {
                            Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Use Camera")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = {
                                showPhotoSourceDialog = false
                                photoGalleryLauncher.launch("image/*")
                            }
                        ) {
                            Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("From Gallery")
                        }
                    }
                )
            }

            if (showCameraCaptureDialog) {
                CameraCaptureDialog(
                    onDismiss = { showCameraCaptureDialog = false },
                    onPhotoCaptured = { uri ->
                        showCameraCaptureDialog = false
                        val saved = ImageStorageUtils.saveImageToInternalStorage(context, uri) ?: uri.toString()
                        onUpdatePhoto(saved)
                    }
                )
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

            // Quick Action Buttons for Cattle / Livestock
            item {
                if (isCattle) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    cattleEventDialogCategory = "CALVING"
                                    showAddCattleEventDialog = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("log_calving_date_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("🍼 Log Calving Date", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            Button(
                                onClick = {
                                    cattleEventDialogCategory = "HEALTH"
                                    showAddCattleEventDialog = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("log_health_record_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                            ) {
                                Icon(Icons.Filled.MedicalServices, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("🩺 Log Health / Meds", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    cattleEventDialogCategory = "PD"
                                    showAddCattleEventDialog = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("log_pd_button"),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, ForestGreenPrimary)
                            ) {
                                Text("🤰 Pregnancy Check (PD)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                            }

                            OutlinedButton(
                                onClick = {
                                    cattleEventDialogCategory = "INSEMINATION"
                                    showAddCattleEventDialog = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("log_insemination_button"),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFF64748B))
                            ) {
                                Text("🧬 AI / Insemination", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                            }
                        }
                    }
                } else {
                    Button(
                        onClick = { showAddCattleEventDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("+ LOG EVENT", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // Calving History & Parity Log Card (for cows that have given birth)
            if (isCattle) {
                val hasCalved = calvingLogs.isNotEmpty() || (cattleEval != null && cattleEval.lastCalvingDate != null)

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, if (hasCalved) Color(0xFFBBF7D0) else Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (hasCalved) Color(0xFFDCFCE7) else Color(0xFFF1F5F9)
                                    ) {
                                        Icon(
                                            Icons.Filled.Pets,
                                            contentDescription = null,
                                            tint = if (hasCalved) ForestGreenPrimary else Color(0xFF64748B),
                                            modifier = Modifier
                                                .padding(6.dp)
                                                .size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Calving History & Parity",
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E293B)
                                        )
                                        Text(
                                            text = if (calvingLogs.isNotEmpty()) "Parity: ${calvingLogs.size} ${if (calvingLogs.size == 1) "Calving" else "Calvings"} Recorded" else if (hasCalved) "Parity: 1+ Calving (Active Lactation)" else "Heifer (No previous calvings)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (hasCalved) Color(0xFF15803D) else Color(0xFF64748B)
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        cattleEventDialogCategory = "CALVING"
                                        showAddCattleEventDialog = true
                                    }
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = "Add Calving", tint = ForestGreenPrimary)
                                }
                            }

                            if (calvingLogs.isEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF8FAFC),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = if (hasCalved) "Cow has previous lactation history. Click '+ Log Calving Date' to add detailed calf records." else "No calving events logged yet. When this cow calves, log the date here to automatically track lactation and next breeding cycle.",
                                            fontSize = 12.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 240.dp)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        calvingLogs.forEachIndexed { idx, cLog ->
                                            var calvingMenuExpanded by remember { mutableStateOf(false) }

                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = Color(0xFFF0FDF4),
                                                border = BorderStroke(1.dp, Color(0xFFDCFCE7)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.weight(1f)
                                                        ) {
                                                            Surface(
                                                                shape = RoundedCornerShape(6.dp),
                                                                color = ForestGreenPrimary
                                                            ) {
                                                                Text(
                                                                    text = "CALVING #${calvingLogs.size - idx}",
                                                                    fontSize = 10.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = Color.White,
                                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                                )
                                                            }
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text(
                                                                text = cLog.date,
                                                                fontSize = 13.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color(0xFF0F172A)
                                                            )
                                                        }

                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            if (cLog.metricValue.isNotBlank()) {
                                                                Text(
                                                                    text = cLog.metricValue,
                                                                    fontSize = 11.sp,
                                                                    fontWeight = FontWeight.SemiBold,
                                                                    color = Color(0xFF166534)
                                                                )
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                            }

                                                            Box {
                                                                IconButton(
                                                                    onClick = { calvingMenuExpanded = true },
                                                                    modifier = Modifier.size(28.dp)
                                                                ) {
                                                                    Icon(
                                                                        Icons.Filled.MoreVert,
                                                                        contentDescription = "Calving Log Options",
                                                                        tint = Color(0xFF64748B),
                                                                        modifier = Modifier.size(18.dp)
                                                                    )
                                                                }

                                                                DropdownMenu(
                                                                    expanded = calvingMenuExpanded,
                                                                    onDismissRequest = { calvingMenuExpanded = false }
                                                                ) {
                                                                    DropdownMenuItem(
                                                                        text = { Text("Edit Record", fontSize = 13.sp) },
                                                                        leadingIcon = {
                                                                            Icon(
                                                                                Icons.Filled.Edit,
                                                                                contentDescription = "Edit",
                                                                                tint = ForestGreenPrimary,
                                                                                modifier = Modifier.size(18.dp)
                                                                            )
                                                                        },
                                                                        onClick = {
                                                                            calvingMenuExpanded = false
                                                                            eventToEdit = cLog
                                                                        }
                                                                    )
                                                                    DropdownMenuItem(
                                                                        text = {
                                                                            Text(
                                                                                "Delete Record",
                                                                                fontSize = 13.sp,
                                                                                color = Color(0xFFDC2626)
                                                                            )
                                                                        },
                                                                        leadingIcon = {
                                                                            Icon(
                                                                                Icons.Filled.Delete,
                                                                                contentDescription = "Delete",
                                                                                tint = Color(0xFFDC2626),
                                                                                modifier = Modifier.size(18.dp)
                                                                            )
                                                                        },
                                                                        onClick = {
                                                                            calvingMenuExpanded = false
                                                                            eventToDelete = cLog
                                                                            showDeleteEventConfirmDialog = true
                                                                        }
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = cLog.details,
                                                        fontSize = 12.sp,
                                                        color = Color(0xFF334155)
                                                    )

                                                    if (cLog.notes.isNotBlank()) {
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Text(
                                                            text = cLog.notes,
                                                            fontSize = 11.sp,
                                                            color = Color(0xFF64748B)
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

            // Events Log & Records (Heat, Insemination, Calving, Weight, Health)
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
                                    text = if (sortedAnimalEvents.isNotEmpty()) "Events & Health Logs (${sortedAnimalEvents.size})" else "Events & Health Logs",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                            }

                            IconButton(
                                onClick = {
                                    cattleEventDialogCategory = "HEALTH"
                                    showAddCattleEventDialog = true
                                }
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "Add Event", tint = ForestGreenPrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (sortedAnimalEvents.isEmpty()) {
                            Text(
                                "No events recorded yet. Click '+ Log Event' to add health, breeding, or weight records.",
                                color = Color.Gray,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            // Scrollable box bounded in height instead of taking over the entire page
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 280.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    sortedAnimalEvents.forEach { ev ->
                                        var eventMenuExpanded by remember { mutableStateOf(false) }

                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFFF8FAFC),
                                            border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Surface(
                                                            shape = RoundedCornerShape(6.dp),
                                                            color = when (ev.category.uppercase()) {
                                                                "CALVING" -> Color(0xFFDCFCE7)
                                                                "PD" -> Color(0xFFFEF3C7)
                                                                "HEAT" -> Color(0xFFFFEDD5)
                                                                "INSEMINATION" -> Color(0xFFE0F2FE)
                                                                "WEIGHT" -> Color(0xFFF3E8FF)
                                                                "DRY_OFF" -> Color(0xFFECFDF5)
                                                                "ABORTED" -> Color(0xFFFEE2E2)
                                                                else -> Color(0xFFFEE2E2)
                                                            }
                                                        ) {
                                                            Text(
                                                                text = when (ev.category.uppercase()) {
                                                                    "CALVING" -> "CALVING"
                                                                    "PD" -> "PD"
                                                                    "HEAT" -> "HEAT"
                                                                    "INSEMINATION" -> "AI"
                                                                    "WEIGHT" -> "WEIGHT"
                                                                    "DRY_OFF" -> "DRY OFF"
                                                                    "ABORTED" -> "ABORTED"
                                                                    else -> "HEALTH"
                                                                },
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = when (ev.category.uppercase()) {
                                                                    "CALVING" -> Color(0xFF15803D)
                                                                    "PD" -> Color(0xFFB45309)
                                                                    "HEAT" -> Color(0xFFC2410C)
                                                                    "INSEMINATION" -> Color(0xFF0369A1)
                                                                    "WEIGHT" -> Color(0xFF7E22CE)
                                                                    "DRY_OFF" -> Color(0xFF047857)
                                                                    "ABORTED" -> Color(0xFFDC2626)
                                                                    else -> Color(0xFF991B1B)
                                                                }
                                                            )
                                                        }

                                                        Spacer(modifier = Modifier.width(8.dp))

                                                        Text(
                                                            text = ev.title,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 13.sp,
                                                            color = Color(0xFF1E293B),
                                                            maxLines = 1,
                                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                        )
                                                    }

                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = ev.date,
                                                            fontSize = 11.sp,
                                                            color = Color(0xFF64748B)
                                                        )

                                                        Box {
                                                            IconButton(
                                                                onClick = { eventMenuExpanded = true },
                                                                modifier = Modifier.size(28.dp)
                                                            ) {
                                                                Icon(
                                                                    Icons.Filled.MoreVert,
                                                                    contentDescription = "Event Options",
                                                                    tint = Color(0xFF64748B),
                                                                    modifier = Modifier.size(18.dp)
                                                                )
                                                            }

                                                            DropdownMenu(
                                                                expanded = eventMenuExpanded,
                                                                onDismissRequest = { eventMenuExpanded = false }
                                                            ) {
                                                                DropdownMenuItem(
                                                                    text = { Text("Edit Event", fontSize = 13.sp) },
                                                                    leadingIcon = {
                                                                        Icon(
                                                                            Icons.Filled.Edit,
                                                                            contentDescription = "Edit",
                                                                            tint = ForestGreenPrimary,
                                                                            modifier = Modifier.size(18.dp)
                                                                        )
                                                                    },
                                                                    onClick = {
                                                                        eventMenuExpanded = false
                                                                        eventToEdit = ev
                                                                    }
                                                                )
                                                                DropdownMenuItem(
                                                                    text = {
                                                                        Text(
                                                                            "Delete Event",
                                                                            fontSize = 13.sp,
                                                                            color = Color(0xFFDC2626)
                                                                        )
                                                                    },
                                                                    leadingIcon = {
                                                                        Icon(
                                                                            Icons.Filled.Delete,
                                                                            contentDescription = "Delete",
                                                                            tint = Color(0xFFDC2626),
                                                                            modifier = Modifier.size(18.dp)
                                                                        )
                                                                    },
                                                                    onClick = {
                                                                        eventMenuExpanded = false
                                                                        eventToDelete = ev
                                                                        showDeleteEventConfirmDialog = true
                                                                    }
                                                                )
                                                            }
                                                        }
                                                    }
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
                                                                color = Color(0xFF64748B),
                                                                modifier = Modifier.weight(1f, fill = false)
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
                        // Current Stage & Reproduction Summary
                        if (cattleEval.summaryReason.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = cattleEval.badgeBgColor.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = cattleEval.summaryReason,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    fontSize = 12.sp,
                                    color = cattleEval.badgeTextColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Calving History & Parity
                        if (cattleEval.hasGivenBirthPreviously && cattleEval.lastCalvingDate != null) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Calving History / Parity", fontSize = 13.sp, color = Color(0xFF64748B))
                                Text(
                                    "${cattleEval.lastCalvingDate} (Parity ${cattleEval.parityCount})",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1E293B)
                                )
                            }
                            if (cattleEval.daysInMilk != null && cattleEval.isMilking) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Days in Milk (DIM)", fontSize = 13.sp, color = Color(0xFF64748B))
                                    Text(
                                        "${cattleEval.daysInMilk} days lactating",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF0369A1)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // AI / Breeding Date
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Last Insemination / Mating", fontSize = 13.sp, color = Color(0xFF64748B))
                            Text(
                                if (cattleEval.lastInseminationDate != null) cattleEval.lastInseminationDate!! else if (cattleEval.hasGivenBirthPreviously) "None in current lactation" else "None Recorded",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (cattleEval.lastInseminationDate != null) Color(0xFF1E293B) else Color(0xFF94A3B8)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Gestation Progress if pregnant
                        if (cattleEval.daysInGestation != null && cattleEval.daysInGestation > 0 && cattleEval.isInCalf) {
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
                                if (cattleEval.isInCalf) (cattleEval.expectedCalvingDate ?: "Pending") else if (cattleEval.expectedCalvingDate != null) "${cattleEval.expectedCalvingDate} (If Conceived)" else "Open / Not In-Calf",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (cattleEval.isInCalf && cattleEval.expectedCalvingDate != null) ForestGreenPrimary else if (cattleEval.expectedCalvingDate != null) Color(0xFF7C3AED) else Color(0xFF94A3B8)
                            )
                        }

                        if (cattleEval.isDriedOff) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Dry-Off Status", fontSize = 13.sp, color = Color(0xFF64748B))
                                Text(
                                    "Dried Off (Udder Rest)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFB45309)
                                )
                            }
                        } else if (cattleEval.dryOffTargetDate != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Recommended Dry-Off Date", fontSize = 13.sp, color = Color(0xFF64748B))
                                Text(
                                    cattleEval.dryOffTargetDate!!,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFB45309)
                                )
                            }
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
            unitId = unitId,
            initialCategory = cattleEventDialogCategory,
            onDismiss = { showAddCattleEventDialog = false },
            onSaveEvent = { type: String, title: String, date: String, details: String, notes: String, metricValue: String, reminderText: String, calfInfo: CalvingCalfInfo? ->
                viewModel.addCattleEvent(
                    unitId = unitId,
                    category = type,
                    title = title,
                    date = date,
                    details = details,
                    notes = notes,
                    metricValue = metricValue
                )

                // Automatically create a new animal/unit record for the calf in the farm's animal list
                if (type.equals("CALVING", ignoreCase = true) && calfInfo != null) {
                    val finalCalfName = when {
                        calfInfo.calfName.isNotBlank() -> calfInfo.calfName
                        calfInfo.calfTag.isNotBlank() -> "Calf ${calfInfo.calfTag}"
                        else -> "Calf of ${animal.name}"
                    }
                    val isMale = calfInfo.calfGender.contains("Bull", ignoreCase = true) || calfInfo.calfGender.contains("Male", ignoreCase = true)
                    val isTwins = calfInfo.calfGender.contains("Twins", ignoreCase = true)
                    val calfSubType = when {
                        isMale -> "Bull Calf"
                        isTwins -> "Twin Calves"
                        else -> "Heifer Calf"
                    }
                    val birthWeight = calfInfo.birthWeight.ifBlank { "34 kg" }
                    val headCount = if (isTwins) 2 else 1
                    val tagNumber = calfInfo.calfTag.ifBlank { "#${System.currentTimeMillis() % 1000}" }
                    val damName = animal.name
                    val sireName = animal.sire.takeIf { it.isNotBlank() && it != "N/A" } ?: ""

                    viewModel.addNewUnit(
                        name = finalCalfName,
                        type = "Cattle",
                        headCount = headCount,
                        healthStatus = "Healthy",
                        location = "Calf Pen",
                        tagNumber = tagNumber,
                        breed = calfSubType,
                        dob = date,
                        dateAdded = date,
                        weightAtBirth = birthWeight,
                        currentWeight = birthWeight,
                        sire = sireName,
                        dam = damName
                    )
                }

                val hasCalvedOrMilking = animalEvents.any { it.category.equals("CALVING", ignoreCase = true) } ||
                    currentStatus.contains("Milking", ignoreCase = true) ||
                    currentStatus.contains("Lactating", ignoreCase = true) ||
                    (cattleEval != null && (cattleEval.stage == CattleStage.MILKING || cattleEval.stage == CattleStage.INCALF_MILKING || cattleEval.lastCalvingDate != null))

                val (immediateStage, breedingDesc) = when (type.uppercase()) {
                    "PD" -> {
                        val isPos = title.contains("Positive", ignoreCase = true) || details.contains("Positive", ignoreCase = true) || metricValue.contains("Positive", ignoreCase = true) || metricValue.contains("In-Calf", ignoreCase = true)
                        if (isPos) {
                            (if (hasCalvedOrMilking) "INCALF / MILKING" else "INCALF") to (if (hasCalvedOrMilking) "IN-CALF & MILKING" else "IN-CALF HEIFER")
                        } else {
                            (if (hasCalvedOrMilking) "MILKING" else "HEIFER") to (if (hasCalvedOrMilking) "OPEN (In Milk)" else "OPEN HEIFER")
                        }
                    }
                    "INSEMINATION" -> (if (hasCalvedOrMilking) "MILKING" else "INSEMINATED") to "SERVED AI (Pending PD)"
                    "CALVING" -> "MILKING" to "OPEN (In Milk)"
                    "DRY_OFF" -> "DRY" to (if (cattleEval?.isInCalf == true) "IN-CALF (Dry)" else "DRY COW (Open)")
                    "ABORTED" -> (if (hasCalvedOrMilking) "MILKING" else "HEIFER") to (if (hasCalvedOrMilking) "OPEN (In Milk)" else "OPEN HEIFER")
                    else -> null to null
                }
                if (immediateStage != null && breedingDesc != null) {
                    currentStatus = immediateStage
                    onUpdateAnimalStage(immediateStage, breedingDesc)
                }
                showAddCattleEventDialog = false
            }
        )
    }

    if (eventToEdit != null) {
        val ev = eventToEdit!!
        AddCattleEventDialog(
            animalName = animal.name,
            unitId = unitId,
            initialCategory = ev.category,
            isEditing = true,
            initialTitle = ev.title,
            initialDate = ev.date,
            initialDetails = ev.details,
            initialNotes = ev.notes,
            initialMetricValue = ev.metricValue,
            onDismiss = { eventToEdit = null },
            onSaveEvent = { type: String, title: String, date: String, details: String, notes: String, metricValue: String, reminderText: String, _ ->
                val evId = ev.id.toLongOrNull()
                if (evId != null) {
                    viewModel.updateCattleEvent(
                        eventId = evId,
                        unitId = unitId,
                        category = type,
                        title = title,
                        date = date,
                        details = details,
                        notes = notes,
                        metricValue = metricValue
                    )
                }
                val idx = animalEvents.indexOfFirst { it.id == ev.id }
                if (idx >= 0) {
                    animalEvents[idx] = CattleEventItem(
                        id = ev.id,
                        category = type,
                        title = title,
                        date = date,
                        details = details,
                        notes = notes,
                        metricValue = metricValue
                    )
                }
                val hasCalvedOrMilking = animalEvents.any { it.category.equals("CALVING", ignoreCase = true) } ||
                    currentStatus.contains("Milking", ignoreCase = true) ||
                    currentStatus.contains("Lactating", ignoreCase = true) ||
                    (cattleEval != null && (cattleEval.stage == CattleStage.MILKING || cattleEval.stage == CattleStage.INCALF_MILKING || cattleEval.lastCalvingDate != null))

                val (immediateStage, breedingDesc) = when (type.uppercase()) {
                    "PD" -> {
                        val isPos = title.contains("Positive", ignoreCase = true) || details.contains("Positive", ignoreCase = true) || metricValue.contains("Positive", ignoreCase = true) || metricValue.contains("In-Calf", ignoreCase = true)
                        if (isPos) {
                            (if (hasCalvedOrMilking) "INCALF / MILKING" else "INCALF") to (if (hasCalvedOrMilking) "IN-CALF & MILKING" else "IN-CALF HEIFER")
                        } else {
                            (if (hasCalvedOrMilking) "MILKING" else "HEIFER") to (if (hasCalvedOrMilking) "OPEN (In Milk)" else "OPEN HEIFER")
                        }
                    }
                    "INSEMINATION" -> (if (hasCalvedOrMilking) "MILKING" else "INSEMINATED") to "SERVED AI (Pending PD)"
                    "CALVING" -> "MILKING" to "OPEN (In Milk)"
                    "DRY_OFF" -> "DRY" to (if (cattleEval?.isInCalf == true) "IN-CALF (Dry)" else "DRY COW (Open)")
                    "ABORTED" -> (if (hasCalvedOrMilking) "MILKING" else "HEIFER") to (if (hasCalvedOrMilking) "OPEN (In Milk)" else "OPEN HEIFER")
                    else -> null to null
                }
                if (immediateStage != null && breedingDesc != null) {
                    currentStatus = immediateStage
                    onUpdateAnimalStage(immediateStage, breedingDesc)
                }
                eventToEdit = null
            }
        )
    }

    if (showDeleteEventConfirmDialog && eventToDelete != null) {
        val ev = eventToDelete!!
        AlertDialog(
            onDismissRequest = {
                showDeleteEventConfirmDialog = false
                eventToDelete = null
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = null,
                        tint = Color(0xFFDC2626)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Delete Event Record?",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Are you sure you want to delete this event record?",
                        fontSize = 13.sp,
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFEF2F2),
                        border = BorderStroke(1.dp, Color(0xFFFECACA)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = ev.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B)
                            )
                            Text(
                                text = "Date: ${ev.date}  •  Category: ${ev.category}",
                                fontSize = 11.sp,
                                color = Color(0xFFB91C1C)
                            )
                            if (ev.details.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = ev.details,
                                    fontSize = 11.sp,
                                    color = Color(0xFF7F1D1D)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This action will update the dynamic gestation and lifecycle summary.",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val evId = ev.id.toLongOrNull()
                        if (evId != null) {
                            viewModel.deleteCattleEvent(evId)
                        }
                        animalEvents.removeAll { it.id == ev.id }
                        showDeleteEventConfirmDialog = false
                        eventToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showDeleteEventConfirmDialog = false
                        eventToDelete = null
                    }
                ) {
                    Text("Cancel", color = Color(0xFF475569))
                }
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
    val recordId: Long,
    val date: String,
    val feedType: String,
    val quantityKg: Double,
    val costAmount: Double,
    val notes: String = ""
)

data class PoultryMortalityLogItem(
    val id: String,
    val recordId: Long,
    val linkedLogSyncId: String = "",
    val date: String,
    val count: Int,
    val cause: String,
    val notes: String = ""
)

data class PoultryEggSaleItem(
    val id: String,
    val recordId: Long,
    val date: String,
    val traysSold: Int,
    val pricePerTray: Double,
    val totalRevenue: Double,
    val buyer: String = ""
)

private sealed class PoultryLogAction {
    data class Feed(val item: PoultryFeedLogItem) : PoultryLogAction()
    data class Mortality(val item: PoultryMortalityLogItem) : PoultryLogAction()
    data class EggSale(val item: PoultryEggSaleItem) : PoultryLogAction()
    data class Disposal(val item: FlockDisposalLogItem) : PoultryLogAction()
}

private fun PoultryFeedLogItem.toPoultryLog(unitId: Long) = PoultryLog(
    id = recordId,
    syncId = id,
    unitId = unitId,
    logType = "FEED",
    date = date,
    feedType = feedType,
    quantityKg = quantityKg,
    costAmount = costAmount,
    notes = notes
)

private fun PoultryMortalityLogItem.toPoultryLog(unitId: Long) = PoultryLog(
    id = recordId,
    syncId = id,
    unitId = unitId,
    logType = "MORTALITY",
    date = date,
    birdCount = count,
    cause = cause,
    notes = notes,
    linkedLogSyncId = linkedLogSyncId
)

private fun PoultryEggSaleItem.toPoultryLog(unitId: Long) = PoultryLog(
    id = recordId,
    syncId = id,
    unitId = unitId,
    logType = "EGG_SALE",
    date = date,
    traysSold = traysSold,
    pricePerTray = pricePerTray,
    totalRevenue = totalRevenue,
    buyer = buyer
)

private fun FlockDisposalLogItem.toPoultryLog(unitId: Long) = PoultryLog(
    id = recordId,
    syncId = id,
    unitId = unitId,
    logType = "DISPOSAL",
    date = date,
    birdCount = quantity,
    disposalReason = reason,
    disposalAmount = amount,
    notes = notes,
    linkedLogSyncId = if (reason.equals("Death", ignoreCase = true)) linkedMortalityLogId.orEmpty() else ""
)


data class PoultryVaccineItem(
    val id: String,
    val vaccineName: String,
    val targetStage: String,
    val dueDate: String,
    val status: String, // "COMPLETED", "DUE_SOON", "UPCOMING"
    val notes: String = ""
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FlockDetailsView(
    flock: AnimalDetailData,
    userRole: String = "OWNER",
    eggLogs: List<EggLog>,
    financeRecords: List<FinanceRecord>,
    poultryLogs: List<PoultryLog>,
    // Persisted rule ids loaded from reminder_completions for this flock.
    completedVaccineRuleIds: Set<String> = emptySet(),
    onMarkVaccinationComplete: (String) -> Unit = {},
    onClearVaccinationComplete: (String) -> Unit = {},
    onAddPoultryLog: (PoultryLog) -> Unit = {},
    onUpdatePoultryLog: (PoultryLog) -> Unit = {},
    onDeletePoultryLog: (Long) -> Unit = {},
    onUpdateFlockHeadCount: (Int) -> Unit = {},
    onBackClick: () -> Unit,
    onAddEggLogClick: () -> Unit,
    onAddFinanceClick: () -> Unit,
    onDisposeFlock: (quantity: Int, reason: String, amount: Double, notes: String, date: String) -> Unit = { _, _, _, _, _ -> },
    onEditFlock: () -> Unit = {},
    onDeleteFlock: () -> Unit = {},
    onUpdatePhoto: (String?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val canEdit = userRole == "OWNER"
    val unitId = remember(flock.id) { flock.id.removePrefix("unit_").toLongOrNull() ?: 0L }
    var showFeedDialog by remember { mutableStateOf(false) }
    var showMortalityDialog by remember { mutableStateOf(false) }
    var showEggSaleDialog by remember { mutableStateOf(false) }
    var showVaccineDialog by remember { mutableStateOf(false) }
    var showDisposeFlockDialog by remember { mutableStateOf(false) }
    var showEditDateAddedDialog by remember { mutableStateOf(false) }
    var poultryLogForOptions by remember { mutableStateOf<PoultryLogAction?>(null) }
    var poultryLogToEdit by remember { mutableStateOf<PoultryLogAction?>(null) }
    var poultryLogToDelete by remember { mutableStateOf<PoultryLogAction?>(null) }

    var flockDateAdded by remember(flock.id, flock.dateOfBirth) {
        mutableStateOf(if (flock.dateOfBirth.isNotBlank()) flock.dateOfBirth else "01 Jul 2026")
    }

    val initialHeadCount = remember(flock.tagNumber, flock.headCountInt) {
        val digits = flock.tagNumber.filter { it.isDigit() }
        if (flock.headCountInt > 1) flock.headCountInt else digits.toIntOrNull() ?: 450
    }
    var liveHeadCount by remember(flock.id) { mutableIntStateOf(initialHeadCount) }
    LaunchedEffect(flock.headCountInt) { liveHeadCount = flock.headCountInt.coerceAtLeast(0) }

    // Dynamic Flock Age Calculation based on Date Added
    val flockAgeInfo = remember(flockDateAdded) {
        PoultryAgeAndVaccinationUtils.calculateFlockAge(flockDateAdded)
    }

    // Vaccine completion state is supplied by the parent from the persisted
    // reminder_completions table. It is not kept in local compose memory.

    val dismissedVaccineRuleIds = remember(flock.id) {
        mutableStateListOf<String>()
    }

    // Dynamic calculated vaccination schedule
    val calculatedVaccineSchedule = remember(flockDateAdded, completedVaccineRuleIds, dismissedVaccineRuleIds.toList()) {
        PoultryAgeAndVaccinationUtils.calculateVaccinationSchedule(flockDateAdded, completedVaccineRuleIds)
            .filter { !dismissedVaccineRuleIds.contains(it.ruleId) }
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

    val flockDisposalLogs = poultryLogs
        .filter { it.logType == "DISPOSAL" }
        .map {
            FlockDisposalLogItem(
                id = it.syncId,
                recordId = it.id,
                flockName = flock.name,
                quantity = it.birdCount,
                reason = it.disposalReason,
                amount = it.disposalAmount,
                date = it.date,
                notes = it.notes,
                linkedMortalityLogId = it.linkedLogSyncId.ifBlank { null }
            )
        }

    var selectedStage by remember(flockAgeInfo.feedStage.stageName) {
        mutableStateOf(flockAgeInfo.feedStage.stageName)
    }

    val feedLogs = poultryLogs
        .filter { it.logType == "FEED" }
        .map {
            PoultryFeedLogItem(it.syncId, it.id, it.date, it.feedType, it.quantityKg, it.costAmount, it.notes)
        }

    val mortalityLogs = poultryLogs
        .filter { it.logType == "MORTALITY" }
        .map {
            PoultryMortalityLogItem(it.syncId, it.id, it.linkedLogSyncId, it.date, it.birdCount, it.cause, it.notes)
        }

    val eggSaleLogs = poultryLogs
        .filter { it.logType == "EGG_SALE" }
        .map {
            PoultryEggSaleItem(it.syncId, it.id, it.date, it.traysSold, it.pricePerTray, it.totalRevenue, it.buyer)
        }

    val totalMortalityCount = mortalityLogs.sumOf { it.count }
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
                if (unitId > 0) {
                    val disposalSyncId = java.util.UUID.randomUUID().toString()
                    val mortalitySyncId = if (reason.equals("Death", ignoreCase = true)) java.util.UUID.randomUUID().toString() else ""
                    if (mortalitySyncId.isNotBlank()) {
                        onAddPoultryLog(
                            PoultryLog(
                                syncId = mortalitySyncId,
                                unitId = unitId,
                                logType = "MORTALITY",
                                date = date,
                                birdCount = quantity,
                                cause = notes.ifBlank { "Mortality" },
                                notes = "Created from flock disposal",
                                linkedLogSyncId = disposalSyncId
                            )
                        )
                    }
                    onAddPoultryLog(
                        PoultryLog(
                            syncId = disposalSyncId,
                            unitId = unitId,
                            logType = "DISPOSAL",
                            date = date,
                            birdCount = quantity,
                            disposalReason = reason,
                            disposalAmount = amount,
                            notes = notes.ifBlank { "$reason disposal" },
                            linkedLogSyncId = mortalitySyncId
                        )
                    )
                }
                liveHeadCount = (liveHeadCount - quantity).coerceAtLeast(0)
                onUpdateFlockHeadCount(liveHeadCount)
                showDisposeFlockDialog = false
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

    if (poultryLogForOptions != null) {
        val selectedLog = poultryLogForOptions!!
        PoultryLogOptionsDialog(
            log = selectedLog,
            onEdit = {
                poultryLogToEdit = selectedLog
                poultryLogForOptions = null
            },
            onDelete = {
                poultryLogToDelete = selectedLog
                poultryLogForOptions = null
            },
            onDismiss = { poultryLogForOptions = null }
        )
    }

    if (poultryLogToDelete != null) {
        val selectedLog = poultryLogToDelete!!
        PoultryLogDeleteConfirmDialog(
            onDismiss = { poultryLogToDelete = null },
            onConfirmDelete = {
                when (selectedLog) {
                    is PoultryLogAction.Feed -> onDeletePoultryLog(selectedLog.item.recordId)
                    is PoultryLogAction.Mortality -> {
                        onDeletePoultryLog(selectedLog.item.recordId)
                        liveHeadCount += selectedLog.item.count
                        onUpdateFlockHeadCount(liveHeadCount)
                    }
                    is PoultryLogAction.EggSale -> onDeletePoultryLog(selectedLog.item.recordId)
                    is PoultryLogAction.Disposal -> {
                        onDeletePoultryLog(selectedLog.item.recordId)
                        liveHeadCount += selectedLog.item.quantity
                        onUpdateFlockHeadCount(liveHeadCount)
                    }
                }
                poultryLogToDelete = null
            }
        )
    }

    when (val selectedLog = poultryLogToEdit) {
        is PoultryLogAction.Feed -> {
            EditFeedLogDialog(
                log = selectedLog.item,
                onDismiss = { poultryLogToEdit = null },
                onSave = { updatedLog ->
                    onUpdatePoultryLog(updatedLog.toPoultryLog(unitId))
                    poultryLogToEdit = null
                }
            )
        }
        is PoultryLogAction.Mortality -> {
            EditMortalityLogDialog(
                log = selectedLog.item,
                onDismiss = { poultryLogToEdit = null },
                onSave = { updatedLog ->
                    val countDifference = updatedLog.count - selectedLog.item.count
                    liveHeadCount = (liveHeadCount - countDifference).coerceAtLeast(0)
                    onUpdateFlockHeadCount(liveHeadCount)
                    onUpdatePoultryLog(updatedLog.toPoultryLog(unitId))
                    poultryLogToEdit = null
                }
            )
        }
        is PoultryLogAction.EggSale -> {
            EditEggSaleLogDialog(
                log = selectedLog.item,
                onDismiss = { poultryLogToEdit = null },
                onSave = { updatedLog ->
                    onUpdatePoultryLog(updatedLog.toPoultryLog(unitId))
                    poultryLogToEdit = null
                }
            )
        }
        is PoultryLogAction.Disposal -> {
            EditDisposalLogDialog(
                log = selectedLog.item,
                onDismiss = { poultryLogToEdit = null },
                onSave = { updatedLog ->
                    val quantityDifference = updatedLog.quantity - selectedLog.item.quantity
                    liveHeadCount = (liveHeadCount - quantityDifference).coerceAtLeast(0)
                    onUpdateFlockHeadCount(liveHeadCount)
                    onUpdatePoultryLog(updatedLog.toPoultryLog(unitId))
                    poultryLogToEdit = null
                }
            )
        }
        null -> Unit
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

                if (canEdit) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Edit Flock Button
                        Surface(
                            onClick = onEditFlock,
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFECFDF5),
                            border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("edit_flock_topbar_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Edit Flock",
                                    tint = ForestGreenPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Edit",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreenPrimary
                                )
                            }
                        }

                        // Dispose Flock Button
                        Surface(
                            onClick = { showDisposeFlockDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFFFBEB),
                            border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("dispose_flock_topbar_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.RemoveCircleOutline,
                                    contentDescription = "Dispose Flock",
                                    tint = Color(0xFFB45309),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Dispose",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }

                        // Delete Flock Button
                        Surface(
                            onClick = onDeleteFlock,
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEF2F2),
                            border = BorderStroke(1.dp, Color(0xFFFECACA)),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("delete_flock_topbar_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DeleteForever,
                                    contentDescription = "Delete Flock",
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Delete",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDC2626)
                                )
                            }
                        }
                    }
                }
                }
            }

            // Flock Photo Header / Avatar Card
            item {
                val context = androidx.compose.ui.platform.LocalContext.current
                var showPhotoSourceDialog by remember { mutableStateOf(false) }
                var showCameraCaptureDialog by remember { mutableStateOf(false) }
                val photoGalleryLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    if (uri != null) {
                        val saved = ImageStorageUtils.saveImageToInternalStorage(context, uri) ?: uri.toString()
                        onUpdatePhoto(saved)
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Flock Photo Avatar / Fallback Icon
                            Box(
                                modifier = Modifier
                                    .size(88.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFFEF3C7))
                                    .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(14.dp))
                                    .clickable { showPhotoSourceDialog = true }
                                    .testTag("flock_photo_avatar_box"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!flock.photoUri.isNullOrBlank()) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(flock.photoUri)
                                            .crossfade(false)
                                            .placeholder(R.drawable.ic_livestock_placeholder)
                                            .error(R.drawable.ic_livestock_placeholder)
                                            .memoryCachePolicy(CachePolicy.ENABLED)
                                            .diskCachePolicy(CachePolicy.ENABLED)
                                            .build(),
                                        contentDescription = "${flock.name} Photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Egg,
                                            contentDescription = "Generic Poultry Icon",
                                            tint = Color(0xFFD97706),
                                            modifier = Modifier.size(40.dp)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("No Photo", fontSize = 9.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }

                            // Info & Photo Upload Trigger
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = flock.name,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Tag: ${flock.tagNumber}  •  ${flock.breed}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = { showPhotoSourceDialog = true },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier
                                            .height(34.dp)
                                            .testTag("upload_flock_photo_button")
                                    ) {
                                        Icon(
                                            imageVector = if (flock.photoUri.isNullOrBlank()) Icons.Filled.AddPhotoAlternate else Icons.Filled.CameraAlt,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (flock.photoUri.isNullOrBlank()) "Add Photo" else "Change",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    if (!flock.photoUri.isNullOrBlank()) {
                                        OutlinedButton(
                                            onClick = { onUpdatePhoto(null) },
                                            shape = RoundedCornerShape(10.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                            modifier = Modifier
                                                .height(34.dp)
                                                .testTag("remove_flock_photo_button")
                                        ) {
                                            Text("Remove", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (flock.notes.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFFFBEB),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Notes / Origin", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(flock.notes, fontSize = 13.sp, color = Color(0xFF334155))
                        }
                    }
                }

                if (showPhotoSourceDialog) {
                    AlertDialog(
                        onDismissRequest = { showPhotoSourceDialog = false },
                        title = { Text("Update Flock Photo", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                        text = { Text("Choose a photo from your camera or gallery to identify ${flock.name}. If no photo is uploaded, a generic chicken/egg icon is displayed.") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showPhotoSourceDialog = false
                                    showCameraCaptureDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                            ) {
                                Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Use Camera")
                            }
                        },
                        dismissButton = {
                            OutlinedButton(
                                onClick = {
                                    showPhotoSourceDialog = false
                                    photoGalleryLauncher.launch("image/*")
                                }
                            ) {
                                Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("From Gallery")
                            }
                        }
                    )
                }

                if (showCameraCaptureDialog) {
                    CameraCaptureDialog(
                        onDismiss = { showCameraCaptureDialog = false },
                        onPhotoCaptured = { uri ->
                            showCameraCaptureDialog = false
                            val saved = ImageStorageUtils.saveImageToInternalStorage(context, uri) ?: uri.toString()
                            onUpdatePhoto(saved)
                        }
                    )
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp)
                                    .combinedClickable(
                                        enabled = canEdit,
                                        onClick = {},
                                        onLongClick = {
                                            poultryLogForOptions = PoultryLogAction.Feed(feed)
                                        }
                                    )
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

                        if (calculatedVaccineSchedule.isEmpty() && customVaccines.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF8FAFC),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = "🎉 All poultry vaccination tasks completed or cleared!",
                                    fontSize = 13.sp,
                                    color = Color(0xFF64748B),
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }

                        calculatedVaccineSchedule.forEach { vac ->
                            val (bgColor, textColor) = when (vac.status) {
                                VaccineDueStatus.COMPLETED -> Color(0xFFDCFCE7) to Color(0xFF15803D)
                                VaccineDueStatus.OVERDUE -> Color(0xFFFEE2E2) to Color(0xFF991B1B)
                                VaccineDueStatus.DUE_TODAY -> Color(0xFFFEF3C7) to Color(0xFFB45309)
                                VaccineDueStatus.DUE_SOON -> Color(0xFFFFFBEB) to Color(0xFFD97706)
                                VaccineDueStatus.UPCOMING -> Color(0xFFF1F5F9) to Color(0xFF475569)
                            }

                            var vacMenuExpanded by remember { mutableStateOf(false) }

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

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = bgColor,
                                            modifier = Modifier.clickable(enabled = canEdit) {
                                                if (vac.isCompleted) {
                                                    onClearVaccinationComplete(vac.ruleId)
                                                } else {
                                                    onMarkVaccinationComplete(vac.ruleId)
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

                                        Box {
                                            IconButton(
                                                onClick = { vacMenuExpanded = true },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.MoreVert,
                                                    contentDescription = "Vaccine task options",
                                                    tint = Color(0xFF64748B),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            DropdownMenu(
                                                expanded = vacMenuExpanded,
                                                onDismissRequest = { vacMenuExpanded = false },
                                                modifier = Modifier.background(Color.White)
                                            ) {
                                                if (!vac.isCompleted) {
                                                    DropdownMenuItem(
                                                        text = { Text("Complete Vaccination", fontWeight = FontWeight.Bold, color = Color(0xFF15803D)) },
                                                        onClick = {
                                                            vacMenuExpanded = false
                                                            onMarkVaccinationComplete(vac.ruleId)
                                                        },
                                                        leadingIcon = {
                                                            Icon(
                                                                imageVector = Icons.Filled.CheckCircle,
                                                                contentDescription = null,
                                                                tint = Color(0xFF15803D),
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                        }
                                                    )
                                                } else {
                                                    DropdownMenuItem(
                                                        text = { Text("Mark Pending", fontWeight = FontWeight.SemiBold, color = Color(0xFF475569)) },
                                                        onClick = {
                                                            vacMenuExpanded = false
                                                            onClearVaccinationComplete(vac.ruleId)
                                                        },
                                                        leadingIcon = {
                                                            Icon(
                                                                imageVector = Icons.Filled.CheckCircle,
                                                                contentDescription = null,
                                                                tint = Color(0xFF64748B),
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                        }
                                                    )
                                                }

                                                DropdownMenuItem(
                                                    text = { Text("Delete / Dismiss Task", fontWeight = FontWeight.Bold, color = Color(0xFFDC2626)) },
                                                    onClick = {
                                                        vacMenuExpanded = false
                                                        dismissedVaccineRuleIds.add(vac.ruleId)
                                                    },
                                                    leadingIcon = {
                                                        Icon(
                                                            imageVector = Icons.Filled.Delete,
                                                            contentDescription = null,
                                                            tint = Color(0xFFDC2626),
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Custom added vaccines
                        customVaccines.forEach { customVac ->
                            var customMenuExpanded by remember { mutableStateOf(false) }

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
                                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                        Text(customVac.vaccineName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                                        Text("Target: ${customVac.targetStage} • Due: ${customVac.dueDate}", fontSize = 12.sp, color = Color(0xFF64748B))
                                        if (customVac.notes.isNotBlank()) {
                                            Text(customVac.notes, fontSize = 11.sp, color = Color(0xFF64748B))
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (customVac.status == "COMPLETED") Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                                            modifier = Modifier.clickable {
                                                val newStatus = if (customVac.status == "COMPLETED") "UPCOMING" else "COMPLETED"
                                                val index = customVaccines.indexOf(customVac)
                                                if (index >= 0) {
                                                    customVaccines[index] = customVac.copy(status = newStatus)
                                                }
                                            }
                                        ) {
                                            Text(
                                                text = customVac.status,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (customVac.status == "COMPLETED") Color(0xFF15803D) else Color(0xFFB45309)
                                            )
                                        }

                                        Box {
                                            IconButton(
                                                onClick = { customMenuExpanded = true },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.MoreVert,
                                                    contentDescription = "Custom vaccine options",
                                                    tint = Color(0xFF64748B),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            DropdownMenu(
                                                expanded = customMenuExpanded,
                                                onDismissRequest = { customMenuExpanded = false },
                                                modifier = Modifier.background(Color.White)
                                            ) {
                                                if (customVac.status != "COMPLETED") {
                                                    DropdownMenuItem(
                                                        text = { Text("Complete Vaccination", fontWeight = FontWeight.Bold, color = Color(0xFF15803D)) },
                                                        onClick = {
                                                            customMenuExpanded = false
                                                            val index = customVaccines.indexOf(customVac)
                                                            if (index >= 0) {
                                                                customVaccines[index] = customVac.copy(status = "COMPLETED")
                                                            }
                                                        },
                                                        leadingIcon = {
                                                            Icon(
                                                                imageVector = Icons.Filled.CheckCircle,
                                                                contentDescription = null,
                                                                tint = Color(0xFF15803D),
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                        }
                                                    )
                                                } else {
                                                    DropdownMenuItem(
                                                        text = { Text("Mark Pending", fontWeight = FontWeight.SemiBold, color = Color(0xFF475569)) },
                                                        onClick = {
                                                            customMenuExpanded = false
                                                            val index = customVaccines.indexOf(customVac)
                                                            if (index >= 0) {
                                                                customVaccines[index] = customVac.copy(status = "UPCOMING")
                                                            }
                                                        },
                                                        leadingIcon = {
                                                            Icon(
                                                                imageVector = Icons.Filled.CheckCircle,
                                                                contentDescription = null,
                                                                tint = Color(0xFF64748B),
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                        }
                                                    )
                                                }

                                                DropdownMenuItem(
                                                    text = { Text("Delete Vaccine", fontWeight = FontWeight.Bold, color = Color(0xFFDC2626)) },
                                                    onClick = {
                                                        customMenuExpanded = false
                                                        customVaccines.remove(customVac)
                                                    },
                                                    leadingIcon = {
                                                        Icon(
                                                            imageVector = Icons.Filled.Delete,
                                                            contentDescription = null,
                                                            tint = Color(0xFFDC2626),
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp)
                                    .combinedClickable(
                                        enabled = canEdit,
                                        onClick = {},
                                        onLongClick = {
                                            poultryLogForOptions = PoultryLogAction.Mortality(log)
                                        }
                                    )
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

            // 5. Egg Sales History Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFBBF7D0))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🥚 Egg Sales Log", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            if (canEdit) {
                                Button(
                                    onClick = { showEggSaleDialog = true },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("+ LOG SALE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (eggSaleLogs.isEmpty()) {
                            Text("No egg-sale records yet for this flock.", fontSize = 13.sp, color = Color(0xFF64748B))
                        } else {
                            eggSaleLogs.forEach { sale ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF0FDF4),
                                    border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 6.dp)
                                        .combinedClickable(
                                            enabled = canEdit,
                                            onClick = {},
                                            onLongClick = {
                                                poultryLogForOptions = PoultryLogAction.EggSale(sale)
                                            }
                                        )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${sale.traysSold} trays • ${sale.buyer.ifBlank { "No buyer recorded" }}",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF166534)
                                            )
                                            Text(
                                                text = "Date: ${sale.date} • KSh ${"%.2f".format(sale.pricePerTray)} per tray",
                                                fontSize = 12.sp,
                                                color = Color(0xFF15803D)
                                            )
                                        }
                                        Text(
                                            text = "KSh ${"%.2f".format(sale.totalRevenue)}",
                                            fontSize = 14.sp,
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

            // 6. Flock Sales & Disposals History Card
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
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 6.dp)
                                        .combinedClickable(
                                            enabled = canEdit,
                                            onClick = {},
                                            onLongClick = {
                                                poultryLogForOptions = PoultryLogAction.Disposal(dLog)
                                            }
                                        )
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
                                if (unitId > 0) {
                                    onAddPoultryLog(
                                        PoultryLog(
                                            unitId = unitId,
                                            logType = "FEED",
                                            date = PoultryAgeAndVaccinationUtils.formatDate(Date()),
                                            feedType = feedType.trim(),
                                            quantityKg = qty.coerceAtLeast(0.0),
                                            costAmount = cost.coerceAtLeast(0.0),
                                            notes = "Logged from Flock View"
                                        )
                                    )
                                }
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
                                val safeCount = count.coerceAtLeast(1)
                                if (unitId > 0) {
                                    onAddPoultryLog(
                                        PoultryLog(
                                            unitId = unitId,
                                            logType = "MORTALITY",
                                            date = PoultryAgeAndVaccinationUtils.formatDate(Date()),
                                            birdCount = safeCount,
                                            cause = causeText.trim(),
                                            notes = notesText.trim()
                                        )
                                    )
                                }
                                liveHeadCount = (liveHeadCount - safeCount).coerceAtLeast(0)
                                onUpdateFlockHeadCount(liveHeadCount)
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
                                if (unitId > 0) {
                                    onAddPoultryLog(
                                        PoultryLog(
                                            unitId = unitId,
                                            logType = "EGG_SALE",
                                            date = PoultryAgeAndVaccinationUtils.formatDate(Date()),
                                            traysSold = trays.coerceAtLeast(0),
                                            pricePerTray = price.coerceAtLeast(0.0),
                                            totalRevenue = total.coerceAtLeast(0.0),
                                            buyer = buyerText.trim()
                                        )
                                    )
                                }
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


@Composable
private fun PoultryLogOptionsDialog(
    log: PoultryLogAction,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val logLabel = when (log) {
        is PoultryLogAction.Feed -> "feed log"
        is PoultryLogAction.Mortality -> "mortality log"
        is PoultryLogAction.EggSale -> "egg-sale log"
        is PoultryLogAction.Disposal -> "disposal log"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Log Options",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        },
        text = {
            Text(
                text = "Choose an action for this $logLabel.",
                fontSize = 13.sp,
                color = Color(0xFF475569)
            )
        },
        confirmButton = {
            Button(
                onClick = onEdit,
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
            ) {
                Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Edit Log", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}

@Composable
private fun PoultryLogDeleteConfirmDialog(
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = null,
                    tint = Color(0xFFDC2626)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Delete Log?",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            }
        },
        text = {
            Text(
                text = "Are you sure you want to delete this log?",
                fontSize = 13.sp,
                color = Color(0xFF475569)
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
            ) {
                Text("Delete", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF475569))
            }
        }
    )
}

@Composable
private fun EditFeedLogDialog(
    log: PoultryFeedLogItem,
    onDismiss: () -> Unit,
    onSave: (PoultryFeedLogItem) -> Unit
) {
    var dateText by remember(log.id) { mutableStateOf(log.date) }
    var feedType by remember(log.id) { mutableStateOf(log.feedType) }
    var quantityText by remember(log.id) { mutableStateOf(log.quantityKg.toString()) }
    var costText by remember(log.id) { mutableStateOf(log.costAmount.toString()) }
    var notesText by remember(log.id) { mutableStateOf(log.notes) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("✏️ Edit Feed Log", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                AppDatePickerField(value = dateText, onValueChange = { dateText = it }, label = "Date")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = feedType, onValueChange = { feedType = it }, label = { Text("Feed Type") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = quantityText, onValueChange = { quantityText = it }, label = { Text("Quantity (Kg)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = costText, onValueChange = { costText = it }, label = { Text("Total Cost (KSh)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = notesText, onValueChange = { notesText = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(
                                log.copy(
                                    date = dateText,
                                    feedType = feedType.trim(),
                                    quantityKg = quantityText.toDoubleOrNull() ?: log.quantityKg,
                                    costAmount = costText.toDoubleOrNull() ?: log.costAmount,
                                    notes = notesText.trim()
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                    ) { Text("SAVE CHANGES") }
                }
            }
        }
    }
}

@Composable
private fun EditMortalityLogDialog(
    log: PoultryMortalityLogItem,
    onDismiss: () -> Unit,
    onSave: (PoultryMortalityLogItem) -> Unit
) {
    var dateText by remember(log.id) { mutableStateOf(log.date) }
    var countText by remember(log.id) { mutableStateOf(log.count.toString()) }
    var causeText by remember(log.id) { mutableStateOf(log.cause) }
    var notesText by remember(log.id) { mutableStateOf(log.notes) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("✏️ Edit Mortality Log", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                Spacer(modifier = Modifier.height(12.dp))
                AppDatePickerField(value = dateText, onValueChange = { dateText = it }, label = "Date")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = countText, onValueChange = { countText = it }, label = { Text("Number of Bird Deaths") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = causeText, onValueChange = { causeText = it }, label = { Text("Cause / Reason") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = notesText, onValueChange = { notesText = it }, label = { Text("Notes / Observations") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(
                                log.copy(
                                    date = dateText,
                                    count = (countText.toIntOrNull() ?: log.count).coerceAtLeast(1),
                                    cause = causeText.trim(),
                                    notes = notesText.trim()
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                    ) { Text("SAVE CHANGES") }
                }
            }
        }
    }
}

@Composable
private fun EditEggSaleLogDialog(
    log: PoultryEggSaleItem,
    onDismiss: () -> Unit,
    onSave: (PoultryEggSaleItem) -> Unit
) {
    var dateText by remember(log.id) { mutableStateOf(log.date) }
    var traysText by remember(log.id) { mutableStateOf(log.traysSold.toString()) }
    var priceText by remember(log.id) { mutableStateOf(log.pricePerTray.toString()) }
    var buyerText by remember(log.id) { mutableStateOf(log.buyer) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("✏️ Edit Egg Sale", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                Spacer(modifier = Modifier.height(12.dp))
                AppDatePickerField(value = dateText, onValueChange = { dateText = it }, label = "Sale Date")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = traysText, onValueChange = { traysText = it }, label = { Text("Number of Trays Sold") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = priceText, onValueChange = { priceText = it }, label = { Text("Price per Tray (KSh)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = buyerText, onValueChange = { buyerText = it }, label = { Text("Buyer Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val trays = (traysText.toIntOrNull() ?: log.traysSold).coerceAtLeast(0)
                            val price = (priceText.toDoubleOrNull() ?: log.pricePerTray).coerceAtLeast(0.0)
                            onSave(
                                log.copy(
                                    date = dateText,
                                    traysSold = trays,
                                    pricePerTray = price,
                                    totalRevenue = trays * price,
                                    buyer = buyerText.trim()
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                    ) { Text("SAVE CHANGES") }
                }
            }
        }
    }
}

@Composable
private fun EditDisposalLogDialog(
    log: FlockDisposalLogItem,
    onDismiss: () -> Unit,
    onSave: (FlockDisposalLogItem) -> Unit
) {
    var dateText by remember(log.id) { mutableStateOf(log.date) }
    var quantityText by remember(log.id) { mutableStateOf(log.quantity.toString()) }
    var reasonText by remember(log.id) { mutableStateOf(log.reason) }
    var amountText by remember(log.id) { mutableStateOf(log.amount.toString()) }
    var notesText by remember(log.id) { mutableStateOf(log.notes) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("✏️ Edit Disposal Log", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                Spacer(modifier = Modifier.height(12.dp))
                AppDatePickerField(value = dateText, onValueChange = { dateText = it }, label = "Disposal Date")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = quantityText, onValueChange = { quantityText = it }, label = { Text("Number of Birds") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = reasonText, onValueChange = { reasonText = it }, label = { Text("Reason") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = amountText, onValueChange = { amountText = it }, label = { Text("Amount (KSh)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = notesText, onValueChange = { notesText = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(
                                log.copy(
                                    date = dateText,
                                    quantity = (quantityText.toIntOrNull() ?: log.quantity).coerceAtLeast(1),
                                    reason = reasonText.trim(),
                                    amount = (amountText.toDoubleOrNull() ?: log.amount).coerceAtLeast(0.0),
                                    notes = notesText.trim()
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                    ) { Text("SAVE CHANGES") }
                }
            }
        }
    }
}
