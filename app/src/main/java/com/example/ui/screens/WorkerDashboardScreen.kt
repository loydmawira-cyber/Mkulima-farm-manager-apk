package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EggLog
import com.example.data.EmployeeRequest
import com.example.data.FarmSettings
import com.example.data.FarmUnit
import com.example.data.MilkLog
import com.example.data.RequestStatus
import com.example.data.UserSession
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerDashboardScreen(
    userSession: UserSession?,
    units: List<FarmUnit>,
    milkLogs: List<MilkLog>,
    eggLogs: List<EggLog>,
    employeeRequests: List<EmployeeRequest>,
    farmSettings: FarmSettings,
    onAddMilkLog: (cowName: String, unitName: String, litres: Double, session: String, fatContent: Double, date: String, notes: String) -> Unit,
    onAddEggLog: (flockName: String, totalEggs: Int, damagedEggs: Int, grade: String, notes: String) -> Unit,
    onAddRequestClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeWorkerTab by remember { mutableIntStateOf(0) } // 0: Quick Entry, 1: Log History, 2: Requests
    val todayDate = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Worker Header Banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = ForestGreenDark,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = Color(0xFF86EFAC),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = userSession?.name ?: "Farm Worker",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "${userSession?.farmName ?: "Farm"} • Operator Portal",
                            fontSize = 12.sp,
                            color = Color(0xFFDCFCE7)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = null,
                                tint = Color(0xFFFEF08A),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Entry Mode",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Sub-tabs: Quick Entry | Log History | Requests
        TabRow(
            selectedTabIndex = activeWorkerTab,
            containerColor = Color.White,
            contentColor = ForestGreenPrimary,
            indicator = { tabPositions ->
                if (activeWorkerTab < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[activeWorkerTab]),
                        color = ForestGreenPrimary,
                        height = 3.dp
                    )
                }
            }
        ) {
            Tab(
                selected = activeWorkerTab == 0,
                onClick = { activeWorkerTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Quick Entry", fontWeight = if (activeWorkerTab == 0) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp)
                    }
                },
                modifier = Modifier.testTag("worker_tab_quick_entry")
            )
            Tab(
                selected = activeWorkerTab == 1,
                onClick = { activeWorkerTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.History, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Log History", fontWeight = if (activeWorkerTab == 1) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp)
                    }
                },
                modifier = Modifier.testTag("worker_tab_log_history")
            )
            Tab(
                selected = activeWorkerTab == 2,
                onClick = { activeWorkerTab = 2 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.FactCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Requests", fontWeight = if (activeWorkerTab == 2) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp)
                    }
                },
                modifier = Modifier.testTag("worker_tab_requests")
            )
        }

        when (activeWorkerTab) {
            0 -> WorkerQuickEntryContent(
                units = units,
                todayDate = todayDate,
                onAddMilkLog = onAddMilkLog,
                onAddEggLog = onAddEggLog
            )
            1 -> WorkerLogHistoryContent(
                milkLogs = milkLogs,
                eggLogs = eggLogs
            )
            2 -> WorkerRequestsContent(
                requests = employeeRequests,
                currency = farmSettings.currency,
                onAddRequestClick = onAddRequestClick
            )
        }
    }
}

// =========================================================================
// 1. QUICK ENTRY TAB
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkerQuickEntryContent(
    units: List<FarmUnit>,
    todayDate: String,
    onAddMilkLog: (cowName: String, unitName: String, litres: Double, session: String, fatContent: Double, date: String, notes: String) -> Unit,
    onAddEggLog: (flockName: String, totalEggs: Int, damagedEggs: Int, grade: String, notes: String) -> Unit
) {
    var selectedEntryType by remember { mutableIntStateOf(0) } // 0: Milk, 1: Eggs
    var successNotification by remember { mutableStateOf<String?>(null) }

    // Milk Entry State
    val cattleUnits = units.filter { it.type.equals("Cattle", ignoreCase = true) }
    var milkCowName by remember { mutableStateOf(if (cattleUnits.isNotEmpty()) cattleUnits.first().name else "Dairy Herd - Friesians") }
    var milkSession by remember { mutableStateOf("Morning") }
    var milkLitresInput by remember { mutableStateOf("") }
    var milkFatInput by remember { mutableStateOf("3.8") }
    var milkNotes by remember { mutableStateOf("") }

    // Egg Entry State
    val poultryUnits = units.filter { it.type.equals("Poultry", ignoreCase = true) }
    var eggFlockName by remember { mutableStateOf(if (poultryUnits.isNotEmpty()) poultryUnits.first().name else "Flock B - Kienyeji Layers") }
    var eggTotalInput by remember { mutableStateOf("") }
    var eggDamagedInput by remember { mutableStateOf("0") }
    var eggGrade by remember { mutableStateOf("Grade A") }
    var eggNotes by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Entry Type Selector (Milk / Eggs)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE2E8F0))
                .padding(4.dp)
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { selectedEntryType = 0 }
                    .testTag("worker_select_milk_form"),
                color = if (selectedEntryType == 0) ForestGreenPrimary else Color.Transparent
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.WaterDrop,
                        contentDescription = null,
                        tint = if (selectedEntryType == 0) Color.White else Color(0xFF475569),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Log Milk Collection",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (selectedEntryType == 0) Color.White else Color(0xFF475569)
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { selectedEntryType = 1 }
                    .testTag("worker_select_egg_form"),
                color = if (selectedEntryType == 1) ForestGreenPrimary else Color.Transparent
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Egg,
                        contentDescription = null,
                        tint = if (selectedEntryType == 1) Color.White else Color(0xFF475569),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Log Egg Harvest",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (selectedEntryType == 1) Color.White else Color(0xFF475569)
                    )
                }
            }
        }

        // Success message banner
        AnimatedVisibility(visible = successNotification != null) {
            successNotification?.let { msg ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFDCFCE7),
                    border = BorderStroke(1.dp, Color(0xFF86EFAC))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = ForestGreenDark, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = msg, color = ForestGreenDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (selectedEntryType == 0) {
            // ================= MILK QUICK ENTRY FORM =================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.WaterDrop, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Milk Quick Entry", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    }

                    // Cow / Unit Selector
                    OutlinedTextField(
                        value = milkCowName,
                        onValueChange = { milkCowName = it },
                        label = { Text("Cow / Dairy Herd Name") },
                        placeholder = { Text("e.g. Cow #102 - Friesian") },
                        leadingIcon = { Icon(Icons.Filled.Pets, contentDescription = null, tint = ForestGreenPrimary) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("worker_milk_cow_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = getAuthFieldColors()
                    )

                    // Session Chips
                    Text("Milking Session", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("Morning", Icons.Filled.WbSunny, Color(0xFFF59E0B)),
                            Triple("Midday", Icons.Filled.WbCloudy, Color(0xFF0EA5E9)),
                            Triple("Evening", Icons.Filled.NightsStay, Color(0xFF6366F1))
                        ).forEach { (session, icon, iconColor) ->
                            val isSelected = milkSession == session
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { milkSession = session },
                                color = if (isSelected) Color(0xFFDCFCE7) else Color(0xFFF1F5F9),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) ForestGreenPrimary else Color(0xFFCBD5E1)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        session,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) ForestGreenDark else Color(0xFF475569)
                                    )
                                }
                            }
                        }
                    }

                    // Litres Input + Quick Increment Buttons
                    Text("Quantity (Litres)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569))
                    OutlinedTextField(
                        value = milkLitresInput,
                        onValueChange = { milkLitresInput = it },
                        placeholder = { Text("e.g. 18.5") },
                        leadingIcon = { Icon(Icons.Filled.WaterDrop, contentDescription = null, tint = ForestGreenPrimary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("worker_milk_litres_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = getAuthFieldColors()
                    )

                    // Quick buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(5.0, 10.0, 15.0, 20.0).forEach { qty ->
                            OutlinedButton(
                                onClick = {
                                    val current = milkLitresInput.toDoubleOrNull() ?: 0.0
                                    milkLitresInput = String.format(Locale.US, "%.1f", current + qty)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Text("+$qty L", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                            }
                        }
                    }

                    // Notes / Health Observations
                    OutlinedTextField(
                        value = milkNotes,
                        onValueChange = { milkNotes = it },
                        label = { Text("Observations / Notes (Optional)") },
                        placeholder = { Text("e.g. Normal yield, udder cleaned, healthy") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = getAuthFieldColors()
                    )

                    // Submit Button
                    Button(
                        onClick = {
                            val litres = milkLitresInput.toDoubleOrNull() ?: 0.0
                            if (litres <= 0.0) return@Button
                            val fat = milkFatInput.toDoubleOrNull() ?: 3.8

                            onAddMilkLog(
                                milkCowName.ifBlank { "Dairy Cow" },
                                "Dairy Unit",
                                litres,
                                milkSession,
                                fat,
                                todayDate,
                                milkNotes.ifBlank { "Logged by Operator on $todayDate" }
                            )

                            successNotification = "Successfully recorded ${litres}L from $milkCowName ($milkSession session)!"
                            milkLitresInput = ""
                            milkNotes = ""
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("worker_submit_milk_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                        enabled = (milkLitresInput.toDoubleOrNull() ?: 0.0) > 0.0
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Milk Entry", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        } else {
            // ================= EGG QUICK ENTRY FORM =================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Egg, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Egg Harvest Quick Entry", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    }

                    // Flock Selector
                    OutlinedTextField(
                        value = eggFlockName,
                        onValueChange = { eggFlockName = it },
                        label = { Text("Flock / Layer Pen Name") },
                        placeholder = { Text("e.g. Flock B - Kienyeji Layers") },
                        leadingIcon = { Icon(Icons.Filled.Pets, contentDescription = null, tint = ForestGreenPrimary) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("worker_egg_flock_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = getAuthFieldColors()
                    )

                    // Total Eggs Input
                    Text("Total Eggs Collected", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569))
                    OutlinedTextField(
                        value = eggTotalInput,
                        onValueChange = { eggTotalInput = it },
                        placeholder = { Text("e.g. 180 (6 trays)") },
                        leadingIcon = { Icon(Icons.Filled.Egg, contentDescription = null, tint = ForestGreenPrimary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("worker_egg_total_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = getAuthFieldColors()
                    )

                    // Quick tray buttons (+30, +60, +90, +150)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(30, 60, 90, 150).forEach { trayEggs ->
                            OutlinedButton(
                                onClick = {
                                    val current = eggTotalInput.toIntOrNull() ?: 0
                                    eggTotalInput = "${current + trayEggs}"
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Text("+${trayEggs / 30} Trays", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                            }
                        }
                    }

                    // Damaged Eggs Input
                    OutlinedTextField(
                        value = eggDamagedInput,
                        onValueChange = { eggDamagedInput = it },
                        label = { Text("Cracked / Damaged Eggs") },
                        placeholder = { Text("0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = getAuthFieldColors()
                    )

                    // Grade Chips
                    Text("Egg Quality Grade", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Grade A (Large)", "Grade B (Medium)", "Grade C (Small)").forEach { grade ->
                            val isSelected = eggGrade == grade
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { eggGrade = grade },
                                color = if (isSelected) Color(0xFFDCFCE7) else Color(0xFFF1F5F9),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) ForestGreenPrimary else Color(0xFFCBD5E1)
                                )
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        grade.take(7),
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) ForestGreenDark else Color(0xFF475569)
                                    )
                                }
                            }
                        }
                    }

                    // Notes
                    OutlinedTextField(
                        value = eggNotes,
                        onValueChange = { eggNotes = it },
                        label = { Text("Notes / Feeder observations (Optional)") },
                        placeholder = { Text("Sorted into trays, high laying vigor") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = getAuthFieldColors()
                    )

                    // Submit Button
                    Button(
                        onClick = {
                            val total = eggTotalInput.toIntOrNull() ?: 0
                            if (total <= 0) return@Button
                            val damaged = eggDamagedInput.toIntOrNull() ?: 0

                            onAddEggLog(
                                eggFlockName.ifBlank { "Layers Pen" },
                                total,
                                damaged,
                                eggGrade,
                                eggNotes.ifBlank { "Logged by Operator on $todayDate" }
                            )

                            successNotification = "Successfully recorded $total eggs (${total / 30} trays) from $eggFlockName!"
                            eggTotalInput = ""
                            eggDamagedInput = "0"
                            eggNotes = ""
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("worker_submit_egg_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                        enabled = (eggTotalInput.toIntOrNull() ?: 0) > 0
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Egg Harvest", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

// =========================================================================
// 2. LOG HISTORY TAB (READ ONLY - NO EDIT OR DELETE ALLOWED)
// =========================================================================
@Composable
private fun WorkerLogHistoryContent(
    milkLogs: List<MilkLog>,
    eggLogs: List<EggLog>
) {
    var selectedHistoryType by remember { mutableIntStateOf(0) } // 0: Milk, 1: Eggs
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Read-only info banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFEFF6FF),
            border = BorderStroke(1.dp, Color(0xFFBFDBFE))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Lock, contentDescription = null, tint = Color(0xFF1D4ED8), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Log history is secure and verified. Entries cannot be modified or deleted by operators.",
                    fontSize = 11.sp,
                    color = Color(0xFF1E40AF)
                )
            }
        }

        // Sub-filter: Milk vs Eggs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFE2E8F0))
                .padding(3.dp)
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { selectedHistoryType = 0 },
                color = if (selectedHistoryType == 0) ForestGreenPrimary else Color.Transparent
            ) {
                Text(
                    "Milk History (${milkLogs.size})",
                    modifier = Modifier.padding(vertical = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (selectedHistoryType == 0) Color.White else Color(0xFF475569)
                )
            }
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { selectedHistoryType = 1 },
                color = if (selectedHistoryType == 1) ForestGreenPrimary else Color.Transparent
            ) {
                Text(
                    "Egg History (${eggLogs.size})",
                    modifier = Modifier.padding(vertical = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (selectedHistoryType == 1) Color.White else Color(0xFF475569)
                )
            }
        }

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(if (selectedHistoryType == 0) "Filter by cow, session, or date..." else "Filter by flock, grade, or date...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = ForestGreenPrimary) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = getAuthFieldColors()
        )

        if (selectedHistoryType == 0) {
            val filteredMilk = milkLogs.filter {
                searchQuery.isBlank() ||
                        it.cowName.contains(searchQuery, ignoreCase = true) ||
                        it.session.contains(searchQuery, ignoreCase = true) ||
                        it.date.contains(searchQuery, ignoreCase = true)
            }

            if (filteredMilk.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No milk logs recorded yet.", color = Color(0xFF94A3B8), fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredMilk, key = { it.id }) { log ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        modifier = Modifier.size(44.dp),
                                        shape = CircleShape,
                                        color = Color(0xFFE0F2FE)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Filled.WaterDrop, contentDescription = null, tint = Color(0xFF0284C7))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(log.cowName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                                        Text("${log.session} • ${log.date}", fontSize = 12.sp, color = Color(0xFF64748B))
                                        if (!log.notes.isNullOrBlank()) {
                                            Text(log.notes, fontSize = 11.sp, color = Color(0xFF94A3B8))
                                        }
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFDCFCE7)
                                ) {
                                    Text(
                                        text = "${log.litres} L",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = ForestGreenDark,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            val filteredEggs = eggLogs.filter {
                searchQuery.isBlank() ||
                        it.unitName.contains(searchQuery, ignoreCase = true) ||
                        it.grade.contains(searchQuery, ignoreCase = true) ||
                        it.loggedAt.contains(searchQuery, ignoreCase = true)
            }

            if (filteredEggs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No egg logs recorded yet.", color = Color(0xFF94A3B8), fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredEggs, key = { it.id }) { log ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        modifier = Modifier.size(44.dp),
                                        shape = CircleShape,
                                        color = Color(0xFFFEF3C7)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Filled.Egg, contentDescription = null, tint = Color(0xFFD97706))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(log.unitName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                                        Text("${log.grade} • ${log.loggedAt}", fontSize = 12.sp, color = Color(0xFF64748B))
                                        if (log.damagedEggs > 0) {
                                            Text("${log.damagedEggs} cracked", fontSize = 11.sp, color = Color(0xFFEF4444))
                                        }
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFFEF9C3)
                                    ) {
                                        Text(
                                            text = "${log.totalEggs} Eggs",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF854D0E),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Text(
                                        text = "${log.totalEggs / 30} trays",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B),
                                        modifier = Modifier.padding(top = 2.dp)
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

// =========================================================================
// 3. WORKER REQUESTS TAB
// =========================================================================
@Composable
private fun WorkerRequestsContent(
    requests: List<EmployeeRequest>,
    currency: String,
    onAddRequestClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Action Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
            border = BorderStroke(1.dp, Color(0xFFBBF7D0))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Employee Requests", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ForestGreenDark)
                    Text(
                        "Submit advance salary, leave, feed or medical requests to the Farm Owner.",
                        fontSize = 12.sp,
                        color = Color(0xFF166534)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onAddRequestClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                    modifier = Modifier.testTag("worker_btn_submit_request")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Request", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Text("My Submitted Requests (${requests.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))

        if (requests.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Assignment, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No requests submitted yet.", color = Color(0xFF94A3B8), fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(requests, key = { it.id }) { req ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        modifier = Modifier.size(36.dp),
                                        shape = CircleShape,
                                        color = when (req.requestType) {
                                            "SALARY_ADVANCE" -> Color(0xFFDCFCE7)
                                            "LEAVE" -> Color(0xFFE0E7FF)
                                            else -> Color(0xFFFEF3C7)
                                        }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = when (req.requestType) {
                                                    "SALARY_ADVANCE" -> Icons.Filled.MonetizationOn
                                                    else -> Icons.Filled.FactCheck
                                                },
                                                contentDescription = null,
                                                tint = ForestGreenDark,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            req.requestType.replace("_", " "),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text("Submitted: ${req.submittedAt}", fontSize = 11.sp, color = Color(0xFF64748B))
                                    }
                                }

                                // Status Badge
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = when (req.status) {
                                        RequestStatus.APPROVED -> Color(0xFFDCFCE7)
                                        RequestStatus.REJECTED -> Color(0xFFFEE2E2)
                                        RequestStatus.PENDING -> Color(0xFFFEF3C7)
                                    }
                                ) {
                                    Text(
                                        text = req.status.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = when (req.status) {
                                            RequestStatus.APPROVED -> ForestGreenDark
                                            RequestStatus.REJECTED -> Color(0xFFDC2626)
                                            RequestStatus.PENDING -> Color(0xFFB45309)
                                        },
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            if (req.amount > 0.0) {
                                Text(
                                    text = "Amount: $currency ${String.format(Locale.US, "%,.2f", req.amount)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ForestGreenPrimary
                                )
                            }

                            if (req.startDate.isNotBlank()) {
                                Text(
                                    text = "Duration: ${req.startDate} to ${req.endDate}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF475569)
                                )
                            }

                            Text(
                                text = "Reason: ${req.reason}",
                                fontSize = 12.sp,
                                color = Color(0xFF334155)
                            )
                        }
                    }
                }
            }
        }
    }
}
