package com.example.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.EmployeeRequest
import com.example.data.FarmTask
import com.example.data.TaskCategory
import com.example.data.FinanceRecord
import com.example.data.FinanceType
import com.example.data.MilkUsageLog
import com.example.data.RequestStatus
import com.example.data.SyncStatus
import com.example.ui.components.SyncStatusBadge
import com.example.ui.components.AddEggLogDialog
import com.example.ui.components.AddEmployeeRequestDialog
import com.example.ui.components.AddFinanceRecordDialog
import com.example.ui.components.AddMilkLogDialog
import com.example.ui.components.AddTaskDialog
import com.example.ui.components.AddUnitDialog
import com.example.ui.components.ProofImageModal
import com.example.ui.components.ProofUploadDialog
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import com.example.ui.components.FarmRemindersDialog
import com.example.ui.components.SlidingSettingsPanel
import com.example.util.FarmReminderEngine
import com.example.ui.screens.WorkerDashboardScreen
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.History
import com.example.ui.screens.ApprovalRequestsScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.WorkerManagementScreen
import com.example.ui.screens.FinanceScreen
import com.example.ui.screens.AssetsScreen
import com.example.ui.screens.SubscriptionBillingScreen
import com.example.ui.screens.FlocksScreen
import com.example.ui.screens.MilkLogScreen
import com.example.notifications.RequestMkulimaNotifications
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.TagLivestockBg
import com.example.ui.theme.TagLivestockText
import com.example.ui.theme.MkulimaTheme
import com.example.ui.theme.mkulimaColors

@Composable
fun MkulimaApp(
    viewModel: FarmViewModel = viewModel(
        factory = FarmViewModelFactory(LocalContext.current)
    )
) {
    MkulimaThemeWrapper(viewModel) {
        RequestMkulimaNotifications()
        MkulimaAppContent(viewModel)
    }
}

@Composable
fun MkulimaThemeWrapper(viewModel: FarmViewModel, content: @Composable () -> Unit) {
    val farmSettings by viewModel.farmSettings.collectAsState()
    val cachedThemeMode = remember { viewModel.authManager.getCachedThemeMode() }
    // The farm-scoped setting is authoritative after it loads; cache is only the first-frame fallback.
    val effectiveThemeMode = farmSettings.themeMode.ifBlank { cachedThemeMode ?: "CLASSIC" }
    MkulimaTheme(themeMode = effectiveThemeMode) {
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
    val syncStatus by viewModel.syncStatus.collectAsState()
    val allUnits by viewModel.allUnits.collectAsState()
    val milkLogs by viewModel.allMilkLogs.collectAsState()
    val milkUsageLogs by viewModel.allMilkUsageLogs.collectAsState()
    val eggLogs by viewModel.allEggLogs.collectAsState()
    val financeRecords by viewModel.allFinanceRecords.collectAsState()
    val monthlyReports by viewModel.allMonthlyReports.collectAsState()
    val inventoryItems by viewModel.allInventoryItems.collectAsState()
    val fieldPlans by viewModel.allFieldPlans.collectAsState()
    val feedPlans by viewModel.allFeedPlans.collectAsState()
    val inventoryMovements by viewModel.allInventoryMovements.collectAsState()
    val employeeRequests by viewModel.allEmployeeRequests.collectAsState()
    val allCattleEvents by viewModel.allCattleEvents.collectAsState(initial = emptyList())
    val farmSettings by viewModel.farmSettings.collectAsState()
    val subscriptionAccess by viewModel.subscriptionAccess.collectAsState()
    val farmWorkers by viewModel.farmWorkers.collectAsState()
    val userSession by viewModel.currentSession.collectAsState()
    val userRole = userSession?.role ?: "Worker"

    if (userSession == null) {
        AuthScreen(
            onLogin = { email, pass, onError -> viewModel.login(email, pass, onError) },
            onSignUp = { name, email, pass, farmName, countryCode, phone, onError ->
                viewModel.signUpOwner(name, email, pass, farmName, countryCode, phone, onError)
            },
            onForgotPassword = { email, onComplete -> viewModel.forgotPassword(email, onComplete) },
            onCompletePasswordReset = { email, newPass, onResult ->
                viewModel.completePasswordReset(email, newPass, onResult)
            }
        )
        return
    }

    // StateFlow delegation is not smart-castable across the Compose branch.
    // Capture the non-null session once for screens that require an owner session.
    val activeSession = userSession ?: return

    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategoryFilter by viewModel.selectedCategoryFilter.collectAsState()
    val selectedStatusFilter by viewModel.selectedStatusFilter.collectAsState()

    // Dialog state holders
    var proofUploadTaskTarget by remember { mutableStateOf<FarmTask?>(null) }
    var proofModalTaskTarget by remember { mutableStateOf<FarmTask?>(null) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var addTaskInitialCategory by remember { mutableStateOf<TaskCategory?>(null) }
    var addTaskInitialTargetUnit by remember { mutableStateOf<String?>(null) }
    var showAddUnitDialog by remember { mutableStateOf(false) }
    var showAddMilkLogDialog by remember { mutableStateOf(false) }
    var showAddEggLogDialog by remember { mutableStateOf(false) }
    var showAddFinanceDialog by remember { mutableStateOf(false) }
    var editingFinanceRecord by remember { mutableStateOf<FinanceRecord?>(null) }
    var showAddEmployeeRequestDialog by remember { mutableStateOf(false) }
    var showSettingsPanel by remember { mutableStateOf(false) }
    var showSubscriptionBillingScreen by remember { mutableStateOf(false) }
    var showWorkerManagementScreen by remember { mutableStateOf(false) }
    var showRemindersDialog by remember { mutableStateOf(false) }
    var dismissedReminderIds by remember { mutableStateOf(setOf<String>()) }

    val farmReminders by viewModel.farmReminders.collectAsState()
    val activeReminders by remember {
        derivedStateOf { farmReminders.filterNot { it.id in dismissedReminderIds } }
    }

    // Intercept back button to dismiss open dialogs/screens or return to Home tab without closing the app
    val hasOpenOverlay = showSubscriptionBillingScreen || showWorkerManagementScreen || showSettingsPanel || showRemindersDialog || showAddTaskDialog ||
            showAddUnitDialog || showAddMilkLogDialog || showAddEggLogDialog ||
            showAddFinanceDialog || (editingFinanceRecord != null) || showAddEmployeeRequestDialog ||
            (proofUploadTaskTarget != null) || (proofModalTaskTarget != null)

    BackHandler(enabled = hasOpenOverlay || selectedTab != 0) {
        when {
            showSubscriptionBillingScreen -> showSubscriptionBillingScreen = false
            showWorkerManagementScreen -> showWorkerManagementScreen = false
            showSettingsPanel -> showSettingsPanel = false
            showRemindersDialog -> showRemindersDialog = false
            proofModalTaskTarget != null -> proofModalTaskTarget = null
            proofUploadTaskTarget != null -> proofUploadTaskTarget = null
            showAddTaskDialog -> showAddTaskDialog = false
            showAddUnitDialog -> showAddUnitDialog = false
            showAddMilkLogDialog -> showAddMilkLogDialog = false
            showAddEggLogDialog -> showAddEggLogDialog = false
            showAddFinanceDialog -> showAddFinanceDialog = false
            editingFinanceRecord != null -> editingFinanceRecord = null
            showAddEmployeeRequestDialog -> showAddEmployeeRequestDialog = false
            selectedTab != 0 -> selectedTab = 0
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
        if (userSession?.permissions?.canCreateTasks == false) {
            LaunchedEffect(Unit) {
                Toast.makeText(context, "You don't have access to add tasks.", Toast.LENGTH_LONG).show()
                showAddTaskDialog = false
                addTaskInitialCategory = null
                addTaskInitialTargetUnit = null
            }
        } else {
            AddTaskDialog(
                availableUnits = allUnits,
                initialCategory = addTaskInitialCategory,
                initialTargetUnit = addTaskInitialTargetUnit,
                onDismiss = {
                    showAddTaskDialog = false
                    addTaskInitialCategory = null
                    addTaskInitialTargetUnit = null
                },
                onTaskCreated = { title, category, targetUnit, priority, scheduledTime, instructions, worker ->
                    viewModel.addNewTask(title, category, targetUnit, priority, scheduledTime, instructions, worker)
                    showAddTaskDialog = false
                    addTaskInitialCategory = null
                    addTaskInitialTargetUnit = null
                }
            )
        }
    }

    // Add Unit Dialog
    if (showAddUnitDialog) {
        AddUnitDialog(
            onDismiss = { showAddUnitDialog = false },
            farmSettings = farmSettings,
            onUnitCreated = { name, type, headCount, healthStatus, location, tagNumber, breed, dob, weightAtBirth, currentWeight, sire, dam, notes ->
                val isCattle = type.contains("cattle", ignoreCase = true) || type.contains("cow", ignoreCase = true)
                viewModel.addNewUnit(
                    name = name,
                    type = type,
                    headCount = headCount,
                    healthStatus = healthStatus,
                    location = location,
                    // Cattle tags are assigned permanently by the repository; manual input is ignored.
                    tagNumber = if (isCattle) "" else tagNumber,
                    breed = breed,
                    dob = dob,
                    weightAtBirth = weightAtBirth,
                    currentWeight = currentWeight,
                    sire = sire,
                    dam = dam,
                    notes = notes,
                    onCreated = { savedUnit ->
                        if (isCattle) {
                            Toast.makeText(
                                context,
                                "Cattle tag ${savedUnit.tagNumber} assigned automatically.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        showAddUnitDialog = false
                    },
                    onError = { message ->
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }

    // Add Milk Log Dialog
    if (showAddMilkLogDialog) {
        AddMilkLogDialog(
            availableUnits = allUnits,
            onDismiss = { showAddMilkLogDialog = false },
            onSaveMilkLog = { cowName, unitName, litres, session, fat, date, notes ->
                viewModel.addMilkLog(
                    cowName, unitName, litres, session, fat, date, notes,
                    onRecorded = { showAddMilkLogDialog = false },
                    onError = { message -> Toast.makeText(context, message, Toast.LENGTH_LONG).show() }
                )
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

    // Edit Finance Record Dialog
    if (editingFinanceRecord != null) {
        AddFinanceRecordDialog(
            existing = editingFinanceRecord,
            onDismiss = { editingFinanceRecord = null },
            onSaveRecord = { _, _, _, _ -> },
            onUpdateRecord = { updated ->
                viewModel.updateFinanceRecord(updated)
                editingFinanceRecord = null
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
            },
            loggedInUserName = userSession?.name ?: "",
            isOwner = userSession?.isOwner ?: false
        )
    }

    SlidingSettingsPanel(
        visible = showSettingsPanel,
            settings = farmSettings,
            userSession = userSession,
            onDismiss = { showSettingsPanel = false },
            onSaveSettings = { newSettings: com.example.data.FarmSettings ->
                viewModel.updateSettings(newSettings)
            },
            onSaveFarmName = { farmName ->
                viewModel.updateFarmName(
                    farmName,
                    onSuccess = { showSettingsPanel = false },
                    onError = { message -> Toast.makeText(context, message, Toast.LENGTH_LONG).show() }
                )
            },
            onSaveRecoveryEmail = { email ->
                viewModel.updateRecoveryEmail(
                    email,
                    onSuccess = {
                        Toast.makeText(context, "Recovery email saved successfully: $it", Toast.LENGTH_LONG).show()
                    },
                    onError = { message -> Toast.makeText(context, message, Toast.LENGTH_LONG).show() }
                )
            },
            onOpenWorkerManagement = {
                showSettingsPanel = false
                showWorkerManagementScreen = true
            },
            onOpenSubscriptionBilling = {
                showSettingsPanel = false
                showSubscriptionBillingScreen = true
            },
            onLogout = {
                viewModel.logout()
                showSettingsPanel = false
            }
    )

    if (showRemindersDialog) {
        FarmRemindersDialog(
            reminders = activeReminders,
            onDismiss = { showRemindersDialog = false },
            onNavigateToAnimal = {
                showRemindersDialog = false
                selectedTab = 1
            },
            onAddNewTaskClick = {
                showRemindersDialog = false
                showAddTaskDialog = true
            },
            onMarkTaskDone = { reminder ->
                viewModel.completeReminderAsTask(
                    title = reminder.title,
                    targetUnit = reminder.targetName,
                    dueDateStr = reminder.dueDateStr,
                    details = reminder.details,
                    sourceTaskId = reminder.sourceTaskId,
                    reminderRuleKey = if (reminder.sourceTaskId == null) reminder.id else null,
                    reminderUnitId = reminder.unitId
                )
            },
            onDismissReminder = { reminderId ->
                dismissedReminderIds = dismissedReminderIds + reminderId
            }
        )
    }

    if (showSubscriptionBillingScreen) {
        SubscriptionBillingScreen(
            userSession = activeSession,
            subscriptionAccess = subscriptionAccess,
            onClose = { showSubscriptionBillingScreen = false }
        )
    } else if (showWorkerManagementScreen) {
        WorkerManagementScreen(
            farmId = userSession?.farmId ?: "FARM-DEFAULT",
            farmName = userSession?.farmName ?: "Farm",
            workers = farmWorkers,
            onCreateWorker = { name, email, pass, perms -> viewModel.createWorker(name, email, pass, perms) },
            onUpdateWorker = { viewModel.updateWorker(it) },
            onToggleRevoke = { id, revoked -> viewModel.toggleWorkerRevoked(id, revoked) },
            onDeleteWorker = { id -> viewModel.deleteWorker(id) },
            onClose = { showWorkerManagementScreen = false }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                IconButton(
                                    onClick = { showSettingsPanel = true },
                                    modifier = Modifier.testTag("topbar_menu_settings_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Menu,
                                        contentDescription = "Settings & Menu",
                                        tint = MaterialTheme.mkulimaColors.textPrimary
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Filled.Agriculture,
                                    contentDescription = null,
                                    tint = MaterialTheme.mkulimaColors.primary,
                                    modifier = Modifier.size(26.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = userSession?.farmName?.ifBlank { "My Farm" } ?: "My Farm",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.mkulimaColors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Real-time Cloud Sync Status Indicator
                                SyncStatusBadge(
                                    status = syncStatus,
                                    onClick = { viewModel.triggerManualSync() },
                                    modifier = Modifier.padding(end = 4.dp)
                                )

                                // Farm Reminders & Alerts Notification Bell
                                IconButton(
                                    onClick = { showRemindersDialog = true },
                                    modifier = Modifier.testTag("topbar_reminders_button")
                                ) {
                                    BadgedBox(
                                        badge = {
                                            if (activeReminders.isNotEmpty()) {
                                                Badge(
                                                    containerColor = Color(0xFFDC2626),
                                                    contentColor = Color.White
                                                ) {
                                                    Text(
                                                        text = if (activeReminders.size > 9) "9+" else "${activeReminders.size}",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Notifications,
                                            contentDescription = "Farm Reminders",
                                            tint = MaterialTheme.mkulimaColors.textPrimary
                                        )
                                    }
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.mkulimaColors.cardBackground
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.mkulimaColors.cardBackground,
                    tonalElevation = 6.dp
                ) {
                    val navItemColors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.mkulimaColors.primary,
                        selectedTextColor = MaterialTheme.mkulimaColors.primary,
                        indicatorColor = MaterialTheme.mkulimaColors.primaryContainer,
                        unselectedIconColor = MaterialTheme.mkulimaColors.textSecondary,
                        unselectedTextColor = MaterialTheme.mkulimaColors.textSecondary
                    )

                    // Tab 0: Home
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = {
                            if (userSession?.permissions?.canViewHome != false) {
                                selectedTab = 0
                            } else {
                                Toast.makeText(context, "You don't have access to Home.", Toast.LENGTH_LONG).show()
                            }
                        },
                        icon = {
                            Icon(
                                if (selectedTab == 0) Icons.Filled.Home else Icons.Outlined.Home,
                                contentDescription = "Home"
                            )
                        },
                        label = {
                            Text(
                                "Home",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        },
                        colors = navItemColors,
                        modifier = Modifier.testTag("nav_home_tab")
                    )

                    // Tab 1: Assets
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = {
                            if (userSession?.permissions?.canViewLivestock != false) {
                                selectedTab = 1
                            } else {
                                Toast.makeText(context, "You don't have access to Assets.", Toast.LENGTH_LONG).show()
                            }
                        },
                        icon = {
                            Icon(
                                if (selectedTab == 1) Icons.Filled.Grass else Icons.Outlined.Grass,
                                contentDescription = "Assets"
                            )
                        },
                        label = {
                            Text(
                                "Assets",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        },
                        colors = navItemColors,
                        modifier = Modifier.testTag("nav_assets_tab")
                    )

                    // Tab 2: Log
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = {
                            if (userSession?.permissions?.canViewLogs != false) {
                                selectedTab = 2
                            } else {
                                Toast.makeText(context, "You don't have access to the Log.", Toast.LENGTH_LONG).show()
                            }
                        },
                        icon = {
                            Icon(
                                if (selectedTab == 2) Icons.Filled.Assignment else Icons.Outlined.Assignment,
                                contentDescription = "Log"
                            )
                        },
                        label = {
                            Text(
                                "Log",
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        },
                        colors = navItemColors,
                        modifier = Modifier.testTag("nav_daily_log_tab")
                    )

                    // Tab 3: Finance
                    if (userSession?.permissions?.canViewFinance != false) {
                        NavigationBarItem(
                            selected = selectedTab == 3,
                            onClick = {
                                if (subscriptionAccess.canUseFinance) {
                                    selectedTab = 3
                                } else {
                                    val message = if (subscriptionAccess.isReadOnly) {
                                        "Subscription expired. Finance and Reports are read-only until renewal."
                                    } else {
                                        "Finance and Reports are available on Premium and Pro."
                                    }
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                }
                            },
                            icon = {
                                Icon(
                                    if (selectedTab == 3) Icons.Filled.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet,
                                    contentDescription = "Finance"
                                )
                            },
                            label = {
                                Text(
                                    "Finance",
                                    fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            },
                            colors = navItemColors,
                            modifier = Modifier.testTag("nav_finance_tab")
                        )
                    }

                    // Tab 4: Tasks (Daily Tasks & Requests)
                    NavigationBarItem(
                        selected = selectedTab == 4,
                        onClick = {
                            val perms = userSession?.permissions
                            if (perms == null || perms.canViewTasks || perms.canViewRequests) {
                                selectedTab = 4
                            } else {
                                Toast.makeText(context, "You don't have access to Tasks.", Toast.LENGTH_LONG).show()
                            }
                        },
                        icon = {
                            Icon(
                                if (selectedTab == 4) Icons.Filled.FactCheck else Icons.Outlined.FactCheck,
                                contentDescription = "Tasks"
                            )
                        },
                        label = {
                            Text(
                                "Tasks",
                                fontWeight = if (selectedTab == 4) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        },
                        colors = navItemColors,
                        modifier = Modifier.testTag("nav_tasks_tab")
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                val effectiveTab = if (userRole != "OWNER") {
                    when(selectedTab) {
                        0 -> 2
                        3 -> 2
                        else -> selectedTab
                    }
                } else selectedTab

                when (effectiveTab) {
                    0 -> if (userSession?.permissions?.canViewHome != false) {
                        DashboardScreen(
                        tasks = filteredTasks,
                        milkLogs = milkLogs,
                        eggLogs = eggLogs,
                        units = allUnits,
                        allCattleEvents = allCattleEvents,
                        farmRemindersParam = farmReminders,
                        financeRecords = financeRecords,
                        employeeRequests = employeeRequests,
                        onRestockClick = { showAddFinanceDialog = true },
                        onNavigateToTab = { selectedTab = it },
                        onAddUnitClick = { showAddUnitDialog = true },
                        onAddMilkLogClick = { showAddMilkLogDialog = true },
                        onAddEggLogClick = { showAddEggLogDialog = true },
                        onCompleteReminderClick = { reminder ->
                            viewModel.completeReminderAsTask(
                                title = reminder.title,
                                targetUnit = reminder.targetName,
                                dueDateStr = reminder.dueDateStr,
                                details = reminder.details,
                                sourceTaskId = reminder.sourceTaskId,
                                reminderRuleKey = if (reminder.sourceTaskId == null) reminder.id else null,
                                reminderUnitId = reminder.unitId
                            )
                        },
                        onDismissReminderClick = { reminderId ->
                            dismissedReminderIds = dismissedReminderIds + reminderId
                        },
                        userRole = userRole,
                        farmSettings = farmSettings
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "You don't have access to Home.",
                                color = MaterialTheme.mkulimaColors.textSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }

                    1 -> if (userSession?.permissions?.canViewLivestock != false) {
                        AssetsScreen(
                        userRole = userRole,
                        inventoryItems = inventoryItems,
                        fieldPlans = fieldPlans,
                        units = allUnits,
                        feedPlans = feedPlans,
                        inventoryMovements = inventoryMovements,
                        automaticFeedDeductionEnabled = farmSettings.automaticFeedDeductionEnabled,
                        financeRecords = financeRecords,
                        onAddInventory = { viewModel.addInventoryItem(it) },
                        onUpdateInventory = { viewModel.updateInventoryItem(it) },
                        onDeleteInventory = { viewModel.deleteInventoryItem(it) },
                        onAddField = { viewModel.addFieldPlan(it) },
                        onUpdateField = { viewModel.updateFieldPlan(it) },
                        onDeleteField = { viewModel.deleteFieldPlan(it) },
                        onHarvest = { field, outcome, tonnes, saleAmount, harvestDate ->
                            viewModel.recordFieldHarvest(field, outcome, tonnes, saleAmount, harvestDate)
                        },
                        onSaveFeedPlan = { viewModel.saveFeedPlan(it) },
                        onDeleteFeedPlan = { viewModel.deleteFeedPlan(it) },
                        onAutomaticFeedDeductionChanged = { viewModel.setAutomaticFeedDeductionEnabled(it) },
                        onLogCropActivity = { activityType, fieldName ->
                            addTaskInitialCategory = TaskCategory.CROPS
                            addTaskInitialTargetUnit = fieldName
                            showAddTaskDialog = true
                        },
                        livestock = {
                            FlocksScreen(
                                viewModel = viewModel,
                                userRole = userRole,
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
                                onUpdateRequestStatus = { req, status -> viewModel.updateEmployeeRequestStatus(req, status) },
                                onAddFinanceRecord = { type, category, amount, description -> viewModel.addFinanceRecord(type, category, amount, description) },
                                onUpdateUnitHeadCount = { unitId, newCount -> viewModel.updateUnitHeadCount(unitId, newCount) },
                                onUpdateUnit = { unit -> viewModel.updateUnit(unit) },
                                onDeleteUnit = { unitId -> viewModel.deleteUnit(unitId) },
                                farmSettings = farmSettings
                            )
                        }
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "You don't have access to Assets.",
                                color = MaterialTheme.mkulimaColors.textSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }

                    2 -> if (userSession?.permissions?.canViewLogs != false) {
                        MilkLogScreen(
                        milkLogs = milkLogs,
                        milkUsageLogs = milkUsageLogs,
                        eggLogs = eggLogs,
                        units = allUnits,
                        onAddMilkLogClick = { showAddMilkLogDialog = true },
                        onAddEggLogClick = { showAddEggLogDialog = true },
                        onQuickSaveMilkLog = { cowName, litres, session, recordDate, onResult ->
                            val finalDate = recordDate.ifBlank {
                                java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date())
                            }
                            viewModel.addMilkLog(
                                cowName, "Cattle Unit", litres, session, 3.8, finalDate, "Recorded from Daily Log",
                                onRecorded = { onResult(true, null) },
                                onError = { message -> onResult(false, message) }
                            )
                        },
                        onSaveMilkUsageLog = { date, session, coop, home, calves, onResult ->
                            viewModel.saveMilkUsageLog(
                                date, session, coop, home, calves,
                                onSaved = { onResult(true, null) },
                                onError = { message -> onResult(false, message) }
                            )
                        },
                        onEditMilkUsageLog = { id, coop, home, calves, onResult ->
                            viewModel.editMilkUsageLog(
                                id, coop, home, calves,
                                onSaved = { onResult(true, null) },
                                onError = { message -> onResult(false, message) }
                            )
                        },
                        onDeleteMilkUsageLog = { viewModel.deleteMilkUsageLog(it) },
                        onQuickSaveEggLog = { flockName, totalEggs, damagedEggs, grade, date, notes ->
                            viewModel.addEggLog(flockName, totalEggs, damagedEggs, grade, notes)
                        },
                        onDeleteMilkLog = { viewModel.deleteMilkLog(it) },
                        onDeleteEggLog = { viewModel.deleteEggLog(it) },
                        farmSettings = farmSettings,
                        userRole = userRole,
                        canEditLogs = userSession?.permissions?.canEditLogs ?: true,
                        canEditPastDaysLogs = userSession?.permissions?.canEditPastDaysLogs ?: true
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "You don't have access to the Log.",
                                color = MaterialTheme.mkulimaColors.textSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }

                    3 -> if (userSession?.permissions?.canViewFinance != false && subscriptionAccess.canUseFinance) {
                        FinanceScreen(
                        records = financeRecords,
                        reports = monthlyReports,
                        onAddTransactionClick = { showAddFinanceDialog = true },
                        onEditTransaction = { editingFinanceRecord = it },
                        onDeleteTransaction = { viewModel.deleteFinanceRecord(it) },
                        onOpenReport = { report ->
                            if (report.fileUrl.isNotBlank()) {
                                runCatching {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(report.fileUrl)))
                                }.onFailure {
                                    Toast.makeText(context, "Unable to open this report file.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        currency = farmSettings.currency
                        )
                    } else if (userSession?.permissions?.canViewFinance == false) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "You don't have access to Finance.",
                                color = MaterialTheme.mkulimaColors.textSecondary,
                                fontSize = 14.sp
                            )
                        }
                    } else { /* Subscription doesn't allow Finance */ }

                    4 -> if (userSession?.permissions.let { it == null || it.canViewTasks || it.canViewRequests }) {
                        ApprovalRequestsScreen(
                        tasks = filteredTasks,
                        requests = employeeRequests,
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
                        onUpdateRequestStatus = { req, statusString ->
                            val requestStatus = when (statusString) {
                                "APPROVED" -> RequestStatus.APPROVED
                                "REJECTED" -> RequestStatus.REJECTED
                                else -> RequestStatus.PENDING
                            }
                            viewModel.updateEmployeeRequestStatus(req, requestStatus)
                        },
                        currency = farmSettings.currency,
                        userRole = userRole,
                        onAddRequestClick = { showAddEmployeeRequestDialog = true }
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "You don't have access to Tasks.",
                                color = MaterialTheme.mkulimaColors.textSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
            }
        }
    }
}
