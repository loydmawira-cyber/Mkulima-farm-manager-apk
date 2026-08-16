package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.FactCheck
import androidx.compose.material.icons.outlined.Grass
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.EmployeeRequest
import com.example.data.FarmTask
import com.example.data.FinanceType
import com.example.data.RequestStatus
import com.example.ui.components.AddEggLogDialog
import com.example.ui.components.AddEmployeeRequestDialog
import com.example.ui.components.AddFinanceRecordDialog
import com.example.ui.components.AddMilkLogDialog
import com.example.ui.components.AddTaskDialog
import com.example.ui.components.AddUnitDialog
import com.example.ui.components.ProofImageModal
import com.example.ui.components.ProofUploadDialog
import androidx.compose.material.icons.filled.Settings
import com.example.ui.components.SettingsDialog
import com.example.ui.screens.ApprovalRequestsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FinanceScreen
import com.example.ui.screens.FlocksScreen
import com.example.ui.screens.MilkLogScreen
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.TagLivestockBg
import com.example.ui.theme.TagLivestockText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MkulimaApp(
    viewModel: FarmViewModel = viewModel(
        factory = FarmViewModelFactory(LocalContext.current)
    )
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var userRole by remember { mutableStateOf("Owner") } // Owner or Worker

    val filteredTasks by viewModel.filteredTasks.collectAsState()
    val allTasks by viewModel.rawTasks.collectAsState()
    val allUnits by viewModel.allUnits.collectAsState()
    val milkLogs by viewModel.allMilkLogs.collectAsState()
    val eggLogs by viewModel.allEggLogs.collectAsState()
    val financeRecords by viewModel.allFinanceRecords.collectAsState()
    val employeeRequests by viewModel.allEmployeeRequests.collectAsState()
    val farmSettings by viewModel.farmSettings.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategoryFilter by viewModel.selectedCategoryFilter.collectAsState()
    val selectedStatusFilter by viewModel.selectedStatusFilter.collectAsState()

    // Dialog state holders
    var proofUploadTaskTarget by remember { mutableStateOf<FarmTask?>(null) }
    var proofModalTaskTarget by remember { mutableStateOf<FarmTask?>(null) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddUnitDialog by remember { mutableStateOf(false) }
    var showAddMilkLogDialog by remember { mutableStateOf(false) }
    var showAddEggLogDialog by remember { mutableStateOf(false) }
    var showAddFinanceDialog by remember { mutableStateOf(false) }
    var showAddEmployeeRequestDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().padding(end = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { }) {
                                Icon(
                                    imageVector = Icons.Filled.Menu,
                                    contentDescription = "Menu",
                                    tint = Color(0xFF1E293B)
                                )
                            }
                            Icon(
                                imageVector = Icons.Filled.Agriculture,
                                contentDescription = null,
                                tint = ForestGreenPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Mkulima",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }
                        IconButton(onClick = { showSettingsDialog = true }) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "Settings",
                                tint = Color(0xFF1E293B)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF8F9FA)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 6.dp
            ) {
                // Tab 0: Home
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            if (selectedTab == 0) Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = "Home"
                        )
                    },
                    label = { Text("Home", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ForestGreenPrimary,
                        selectedTextColor = ForestGreenPrimary,
                        indicatorColor = Color(0xFFDCFCE7),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("nav_home_tab")
                )

                // Tab 1: Livestock
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            if (selectedTab == 1) Icons.Filled.Grass else Icons.Outlined.Grass,
                            contentDescription = "Livestock"
                        )
                    },
                    label = { Text("Livestock", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ForestGreenPrimary,
                        selectedTextColor = ForestGreenPrimary,
                        indicatorColor = Color(0xFFDCFCE7),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("nav_livestock_tab")
                )

                // Tab 2: Log
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            if (selectedTab == 2) Icons.Filled.Assignment else Icons.Outlined.Assignment,
                            contentDescription = "Log"
                        )
                    },
                    label = { Text("Log", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ForestGreenPrimary,
                        selectedTextColor = ForestGreenPrimary,
                        indicatorColor = Color(0xFFDCFCE7),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("nav_daily_log_tab")
                )

                // Tab 3: Finance
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        Icon(
                            if (selectedTab == 3) Icons.Filled.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet,
                            contentDescription = "Finance"
                        )
                    },
                    label = { Text("Finance", fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Medium, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ForestGreenPrimary,
                        selectedTextColor = ForestGreenPrimary,
                        indicatorColor = Color(0xFFDCFCE7),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("nav_finance_tab")
                )

                // Tab 4: Requests / Inventory
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = {
                        Icon(
                            if (selectedTab == 4) Icons.Filled.FactCheck else Icons.Outlined.FactCheck,
                            contentDescription = "Requests"
                        )
                    },
                    label = { Text("Requests", fontWeight = if (selectedTab == 4) FontWeight.Bold else FontWeight.Medium, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ForestGreenPrimary,
                        selectedTextColor = ForestGreenPrimary,
                        indicatorColor = Color(0xFFDCFCE7),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("nav_requests_tab")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8F9FA))
        ) {
            when (selectedTab) {
                0 -> DashboardScreen(
                    tasks = filteredTasks,
                    milkLogs = milkLogs,
                    eggLogs = eggLogs,
                    units = allUnits,
                    financeRecords = financeRecords,
                    employeeRequests = employeeRequests,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.searchQuery.value = it },
                    selectedCategory = selectedCategoryFilter,
                    onCategorySelected = { viewModel.selectedCategoryFilter.value = it },
                    selectedStatus = selectedStatusFilter,
                    onStatusSelected = { viewModel.selectedStatusFilter.value = it },
                    onCompleteTaskClick = { proofUploadTaskTarget = it },
                    onReopenTaskClick = { viewModel.markTaskIncomplete(it.id) },
                    onViewProofClick = { proofModalTaskTarget = it },
                    onDeleteTaskClick = { viewModel.deleteTask(it.id) },
                    onAddTaskClick = { showAddTaskDialog = true },
                    onRestockClick = { showAddFinanceDialog = true },
                    onNavigateToTab = { selectedTab = it },
                    onAddUnitClick = { showAddUnitDialog = true },
                    onAddMilkLogClick = { showAddMilkLogDialog = true },
                    onAddEggLogClick = { showAddEggLogDialog = true },
                    userRole = userRole,
                    farmSettings = farmSettings
                )

                1 -> FlocksScreen(
                    units = allUnits,
                    milkLogs = milkLogs,
                    eggLogs = eggLogs,
                    financeRecords = financeRecords,
                    employeeRequests = employeeRequests,
                    onAddUnitClick = { showAddUnitDialog = true },
                    onAddTaskForUnit = { showAddTaskDialog = true },
                    onAddMilkLogClick = { showAddMilkLogDialog = true },
                    onAddEggLogClick = { showAddEggLogDialog = true },
                    onAddFinanceClick = { showAddFinanceDialog = true },
                    onAddEmployeeRequestClick = { showAddEmployeeRequestDialog = true },
                    onUpdateRequestStatus = { req, status ->
                        viewModel.updateEmployeeRequestStatus(req, status)
                    },
                    onAddFinanceRecord = { type, category, amount, description ->
                        viewModel.addFinanceRecord(type, category, amount, description)
                    },
                    onUpdateUnitHeadCount = { unitId, newCount ->
                        viewModel.updateUnitHeadCount(unitId, newCount)
                    },
                    onUpdateUnit = { unit ->
                        viewModel.updateUnit(unit)
                    },
                    onDeleteUnit = { unitId ->
                        viewModel.deleteUnit(unitId)
                    },
                    farmSettings = farmSettings
                )

                2 -> MilkLogScreen(
                    milkLogs = milkLogs,
                    eggLogs = eggLogs,
                    units = allUnits,
                    onAddMilkLogClick = { showAddMilkLogDialog = true },
                    onAddEggLogClick = { showAddEggLogDialog = true },
                    onQuickSaveMilkLog = { cowName, litres, session, recordDate ->
                        val finalDate = recordDate.ifBlank {
                            java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date())
                        }
                        viewModel.addMilkLog(cowName, "Cattle Unit", litres, session, 3.8, finalDate, "Recorded from Daily Log")
                    },
                    onQuickSaveEggLog = { flockName, totalEggs, damagedEggs, grade, date, notes ->
                        viewModel.addEggLog(flockName, totalEggs, damagedEggs, grade, notes)
                    },
                    onDeleteMilkLog = { viewModel.deleteMilkLog(it) },
                    onDeleteEggLog = { viewModel.deleteEggLog(it) },
                    farmSettings = farmSettings
                )

                3 -> FinanceScreen(
                    records = financeRecords,
                    onAddTransactionClick = { showAddFinanceDialog = true },
                    currency = farmSettings.currency
                )

                4 -> ApprovalRequestsScreen(
                    requests = employeeRequests,
                    onUpdateRequestStatus = { req, statusString ->
                        val requestStatus = when (statusString) {
                            "APPROVED" -> RequestStatus.APPROVED
                            "REJECTED" -> RequestStatus.REJECTED
                            else -> RequestStatus.PENDING
                        }
                        viewModel.updateEmployeeRequestStatus(req, requestStatus)
                    },
                    currency = farmSettings.currency
                )
            }
        }
    }

    // Proof Upload Dialog
    proofUploadTaskTarget?.let { task ->
        ProofUploadDialog(
            task = task,
            onDismiss = { proofUploadTaskTarget = null },
            onSubmitProof = { photoUri, notes ->
                viewModel.completeTaskWithProof(task.id, photoUri, notes)
                proofUploadTaskTarget = null
            },
            onSaveInternalPhoto = { uri ->
                viewModel.savePhotoToInternalStorage(context, uri)
            }
        )
    }

    // Proof Image Detail Modal
    proofModalTaskTarget?.let { task ->
        ProofImageModal(
            task = task,
            onDismiss = { proofModalTaskTarget = null }
        )
    }

    // Add Task Dialog
    if (showAddTaskDialog) {
        AddTaskDialog(
            availableUnits = allUnits,
            onDismiss = { showAddTaskDialog = false },
            onTaskCreated = { title, category, targetUnit, priority, scheduledTime, instructions, worker ->
                viewModel.addNewTask(title, category, targetUnit, priority, scheduledTime, instructions, worker)
                showAddTaskDialog = false
            }
        )
    }

    // Add Unit Dialog
    if (showAddUnitDialog) {
        AddUnitDialog(
            onDismiss = { showAddUnitDialog = false },
            farmSettings = farmSettings,
            onUnitCreated = { name, type, headCount, healthStatus, location, tagNumber, breed, dob, weightAtBirth, currentWeight, sire, dam ->
                viewModel.addNewUnit(
                    name = name,
                    type = type,
                    headCount = headCount,
                    healthStatus = healthStatus,
                    location = location,
                    tagNumber = tagNumber,
                    breed = breed,
                    dob = dob,
                    weightAtBirth = weightAtBirth,
                    currentWeight = currentWeight,
                    sire = sire,
                    dam = dam
                )
                showAddUnitDialog = false
            }
        )
    }

    // Add Milk Log Dialog
    if (showAddMilkLogDialog) {
        AddMilkLogDialog(
            availableUnits = allUnits,
            onDismiss = { showAddMilkLogDialog = false },
            onSaveMilkLog = { cowName, unitName, litres, session, fat, date, notes ->
                viewModel.addMilkLog(cowName, unitName, litres, session, fat, date, notes)
                showAddMilkLogDialog = false
            }
        )
    }

    // Add Egg Log Dialog
    if (showAddEggLogDialog) {
        AddEggLogDialog(
            availableUnits = allUnits,
            onDismiss = { showAddEggLogDialog = false },
            onSaveEggLog = { unitName, totalEggs, damagedEggs, grade, notes ->
                viewModel.addEggLog(unitName, totalEggs, damagedEggs, grade, notes)
                showAddEggLogDialog = false
            }
        )
    }

    // Add Finance Record Dialog
    if (showAddFinanceDialog) {
        AddFinanceRecordDialog(
            onDismiss = { showAddFinanceDialog = false },
            onSaveRecord = { type, category, amount, description ->
                viewModel.addFinanceRecord(type, category, amount, description)
                showAddFinanceDialog = false
            }
        )
    }

    // Add Employee Request Dialog
    if (showAddEmployeeRequestDialog) {
        AddEmployeeRequestDialog(
            onDismiss = { showAddEmployeeRequestDialog = false },
            onSaveRequest = { name, type, amount, start, end, reason ->
                viewModel.addEmployeeRequest(name, type, amount, start, end, reason)
                showAddEmployeeRequestDialog = false
            }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            settings = farmSettings,
            onDismiss = { showSettingsDialog = false },
            onSaveSettings = { newSettings: com.example.data.FarmSettings ->
                viewModel.updateSettings(newSettings)
                showSettingsDialog = false
            }
        )
    }
}
