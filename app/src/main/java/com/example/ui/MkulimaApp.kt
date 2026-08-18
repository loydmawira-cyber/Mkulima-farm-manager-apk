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
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.ProofImageModal
import com.example.ui.components.ProofUploadDialog
import androidx.compose.material.icons.filled.Settings
import com.example.ui.components.SettingsDialog
import com.example.ui.screens.ApprovalRequestsScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.WorkerManagementScreen
import com.example.ui.screens.FinanceScreen
import com.example.ui.screens.FlocksScreen
import com.example.ui.screens.MilkLogScreen
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.TagLivestockBg
import com.example.ui.theme.TagLivestockText
import com.example.ui.theme.MkulimaTheme

@Composable
fun MkulimaApp(
    viewModel: FarmViewModel = viewModel(
        factory = FarmViewModelFactory(LocalContext.current)
    )
) {
    MkulimaThemeWrapper(viewModel) {
        MkulimaAppContent(viewModel)
    }
}

@Composable
fun MkulimaThemeWrapper(viewModel: FarmViewModel, content: @Composable () -> Unit) {
    val farmSettings by viewModel.farmSettings.collectAsState()
    MkulimaTheme(themeMode = farmSettings.themeMode) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MkulimaAppContent(
    viewModel: FarmViewModel
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    val filteredTasks by viewModel.filteredTasks.collectAsState()
    val allTasks by viewModel.rawTasks.collectAsState()
    val allUnits by viewModel.allUnits.collectAsState()
    val milkLogs by viewModel.allMilkLogs.collectAsState()
    val eggLogs by viewModel.allEggLogs.collectAsState()
    val financeRecords by viewModel.allFinanceRecords.collectAsState()
    val employeeRequests by viewModel.allEmployeeRequests.collectAsState()
    val farmSettings by viewModel.farmSettings.collectAsState()
    val farmWorkers by viewModel.farmWorkers.collectAsState()
    val userSession by viewModel.currentSession.collectAsState()
    val userRole = userSession?.role ?: "Worker"

    if (userSession == null) {
        AuthScreen(
            onLogin = { email, pass, onError -> viewModel.login(email, pass, onError) },
            onSignUp = { name, email, pass, farmName, onError -> viewModel.signUpOwner(name, email, pass, farmName, onError) },
            onForgotPassword = { email, onComplete -> viewModel.forgotPassword(email, onComplete) }
        )
        return
    }

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
    var showWorkerManagementScreen by remember { mutableStateOf(false) }

    // New finance edit/delete states
    var editingFinanceRecord by remember { mutableStateOf<com.example.data.FinanceRecord?>(null) }
    var showEditFinanceDialog by remember { mutableStateOf(false) }
    var deletingFinanceRecord by remember { mutableStateOf<com.example.data.FinanceRecord?>(null) }
    var showConfirmDeleteFinance by remember { mutableStateOf(false) }

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
                            imageVector = if (selectedTab == 0) Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = "Home",
                            tint = if (selectedTab == 0) ForestGreenPrimary else Color(0xFF94A3B8)
                        )
                    },
                    label = { Text("Home") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = ForestGreenPrimary)
                )

                // Tab 1: Tasks
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 1) Icons.Filled.Assignment else Icons.Outlined.Assignment,
                            contentDescription = "Tasks",
                            tint = if (selectedTab == 1) ForestGreenPrimary else Color(0xFF94A3B8)
                        )
                    },
                    label = { Text("Tasks") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = ForestGreenPrimary)
                )

                // Tab 2: Flocks
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 2) Icons.Filled.Grass else Icons.Outlined.Grass,
                            contentDescription = "Flocks",
                            tint = if (selectedTab == 2) ForestGreenPrimary else Color(0xFF94A3B8)
                        )
                    },
                    label = { Text("Flocks") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = ForestGreenPrimary)
                )

                // Tab 3: Finance
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 3) Icons.Filled.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet,
                            contentDescription = "Finance",
                            tint = if (selectedTab == 3) ForestGreenPrimary else Color(0xFF94A3B8)
                        )
                    },
                    label = { Text("Finance") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = ForestGreenPrimary)
                )

                // Tab 4: Logs / Reports
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.FactCheck,
                            contentDescription = "Reports",
                            tint = if (selectedTab == 4) ForestGreenPrimary else Color(0xFF94A3B8)
                        )
                    },
                    label = { Text("Reports") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = ForestGreenPrimary)
                )
            }
        }
    ) {
        Box(modifier = Modifier.padding(it)) {
            when (selectedTab) {
                0 -> DashboardScreen(
                    onAddTask = { showAddTaskDialog = true },
                    onOpenWorkerManagement = { showWorkerManagementScreen = true },
                    onOpenSettings = { showSettingsDialog = true }
                )
                1 -> ApprovalRequestsScreen()
                2 -> FlocksScreen()
                3 -> FinanceScreen(
                    records = financeRecords,
                    onAddTransactionClick = { showAddFinanceDialog = true },
                    onEditTransaction = { rec ->
                        editingFinanceRecord = rec
                        showEditFinanceDialog = true
                    },
                    onDeleteTransaction = { rec ->
                        deletingFinanceRecord = rec
                        showConfirmDeleteFinance = true
                    }
                )
                4 -> MilkLogScreen(
                    milkLogs = milkLogs,
                    onAddMilkLog = { showAddMilkLogDialog = true }
                )
            }

            // Dialogs
            if (showAddTaskDialog) AddTaskDialog(
                availableUnits = allUnits,
                onDismiss = { showAddTaskDialog = false },
                onTaskCreated = { title, category, targetUnit, priority, scheduledTime, instructions, assignedWorker ->
                    viewModel.addNewTask(
                        title = title,
                        category = category,
                        targetUnit = targetUnit,
                        priority = priority,
                        scheduledTime = scheduledTime,
                        instructions = instructions,
                        worker = assignedWorker
                    )
                    showAddTaskDialog = false
                }
            )

            if (showAddFinanceDialog) {
                AddFinanceRecordDialog(onDismiss = { showAddFinanceDialog = false }, onSaveRecord = { type, category, amount, description ->
                    viewModel.addFinanceRecord(type = type, category = category, amount = amount, description = description)
                    showAddFinanceDialog = false
                })
            }

            if (showEditFinanceDialog && editingFinanceRecord != null) {
                AddFinanceRecordDialog(
                    onDismiss = { showEditFinanceDialog = false; editingFinanceRecord = null },
                    onSaveRecord = { type, category, amount, description ->
                        // fallback to add
                        viewModel.addFinanceRecord(type = type, category = category, amount = amount, description = description)
                        showEditFinanceDialog = false
                        editingFinanceRecord = null
                    },
                    onUpdateRecord = { updated ->
                        viewModel.updateFinanceRecord(updated)
                        showEditFinanceDialog = false
                        editingFinanceRecord = null
                    },
                    existing = editingFinanceRecord
                )
            }

            if (showConfirmDeleteFinance && deletingFinanceRecord != null) {
                ConfirmDeleteDialog(
                    title = "Delete transaction",
                    message = "This action will permanently delete the transaction. Continue?",
                    onConfirm = {
                        viewModel.deleteFinanceRecord(deletingFinanceRecord!!.id)
                        deletingFinanceRecord = null
                    },
                    onDismiss = {
                        showConfirmDeleteFinance = false
                        deletingFinanceRecord = null
                    }
                )
            }

            if (showAddMilkLogDialog) AddMilkLogDialog(onDismiss = { showAddMilkLogDialog = false }, onSave = { cowName, unitName, litres, sessionStr, fat, date, notes ->
                viewModel.addMilkLog(cowName, unitName, litres, sessionStr, fat, date, notes)
                showAddMilkLogDialog = false
            })

            if (showAddEggLogDialog) AddEggLogDialog(onDismiss = { showAddEggLogDialog = false }, onSave = { unitName, totalEggs, damagedEggs, grade, notes ->
                viewModel.addEggLog(unitName, totalEggs, damagedEggs, grade, notes)
                showAddEggLogDialog = false
            })

            if (showAddUnitDialog) AddUnitDialog(
                onDismiss = { showAddUnitDialog = false },
                farmSettings = farmSettings,
                onUnitCreated = { name, type, headCount, healthStatus, location, tagNumber, breed, dob, weightAtBirth, currentWeight, sire, dam ->
                    viewModel.addNewUnit(
                        name = name,
                        type = type,
                        headCount = headCount,
                        healthStatus = healthStatus,
                        location = location,
                        tagNumber = tagNumber.ifBlank { null },
                        breed = breed.ifBlank { null },
                        dob = dob,
                        weightAtBirth = weightAtBirth.filter { ch -> ch.isDigit() || ch == '.' }.toDoubleOrNull(),
                        currentWeight = currentWeight.filter { ch -> ch.isDigit() || ch == '.' }.toDoubleOrNull()
                    )
                    showAddUnitDialog = false
                }
            )

            if (showAddEmployeeRequestDialog) AddEmployeeRequestDialog(
                onDismiss = { showAddEmployeeRequestDialog = false },
                onSaveRequest = { employeeName, requestType, amount, startDate, endDate, reason ->
                    viewModel.addEmployeeRequest(employeeName, requestType, amount, startDate, endDate, reason)
                    showAddEmployeeRequestDialog = false
                }
            )

            if (showSettingsDialog) SettingsDialog(
                settings = farmSettings,
                userSession = userSession,
                onDismiss = { showSettingsDialog = false },
                onSaveSettings = { settings ->
                    viewModel.updateSettings(settings)
                    showSettingsDialog = false
                },
                onOpenWorkerManagement = { showWorkerManagementScreen = true },
                onLogout = {
                    viewModel.logout()
                    showSettingsDialog = false
                }
            )

            if (showWorkerManagementScreen) WorkerManagementScreen(
                farmId = userSession?.farmId ?: "FARM-DEFAULT",
                farmName = farmSettings.farmName ?: userSession?.name ?: "My Farm",
                workers = farmWorkers,
                onCreateWorker = { name, emailOrPhone, pass, permissions ->
                    viewModel.createWorker(name, emailOrPhone, pass, permissions)
                },
                onUpdateWorker = { updated -> viewModel.updateWorker(updated) },
                onToggleRevoke = { workerId, isRevoked -> viewModel.toggleWorkerRevoked(workerId, isRevoked) },
                onDeleteWorker = { workerId -> viewModel.deleteWorker(workerId) },
                onClose = { showWorkerManagementScreen = false }
            )
        }
    }
}
