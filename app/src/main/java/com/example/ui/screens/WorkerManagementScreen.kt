package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.WorkerAccount
import com.example.data.WorkerPermissions
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenPrimary

@Composable
fun WorkerManagementScreen(
    farmId: String,
    farmName: String,
    workers: List<WorkerAccount>,
    onCreateWorker: (name: String, emailOrPhone: String, password: String, permissions: WorkerPermissions) -> Unit,
    onUpdateWorker: (WorkerAccount) -> Unit,
    onToggleRevoke: (workerId: String, isRevoked: Boolean) -> Unit,
    onDeleteWorker: (workerId: String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingWorker by remember { mutableStateOf<WorkerAccount?>(null) }
    var workerToDelete by remember { mutableStateOf<WorkerAccount?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color(0xFF1E293B))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "Team & Worker Management",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = Color(0xFFDCFCE7),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = farmId,
                                    color = ForestGreenDark,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = farmName,
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }

                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("btn_add_worker_top")
                ) {
                    Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Worker", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Info Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Shield, contentDescription = null, tint = ForestGreenPrimary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Granular Access Control",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = "Workers only see sections enabled in their profile. Revoking access blocks logins immediately.",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }

        // Worker List
        if (workers.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Group, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No workers created yet", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Click 'Add Worker' to create login credentials for your farm hands.", fontSize = 12.sp, color = Color(0xFF94A3B8), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Create First Worker Account")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(workers, key = { it.workerId }) { worker ->
                    WorkerAccountCard(
                        worker = worker,
                        onEdit = { editingWorker = worker },
                        onToggleRevoke = { onToggleRevoke(worker.workerId, !worker.isRevoked) },
                        onDelete = { workerToDelete = worker }
                    )
                }
                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }
    }

    // Dialogs
    if (showAddDialog) {
        AddOrEditWorkerDialog(
            worker = null,
            onDismiss = { showAddDialog = false },
            onSave = { name, emailOrPhone, pass, permissions ->
                onCreateWorker(name, emailOrPhone, pass, permissions)
                showAddDialog = false
            }
        )
    }

    editingWorker?.let { worker ->
        AddOrEditWorkerDialog(
            worker = worker,
            onDismiss = { editingWorker = null },
            onSave = { name, emailOrPhone, pass, permissions ->
                val updated = worker.copy(
                    name = name,
                    emailOrPhone = emailOrPhone,
                    password = pass,
                    canViewHome = permissions.canViewHome,
                    canUseQuickActions = permissions.canUseQuickActions,
                    canViewLivestock = permissions.canViewLivestock,
                    canEditLivestock = permissions.canEditLivestock,
                    canViewLogs = permissions.canViewLogs,
                    canEditLogs = permissions.canEditLogs,
                    canEditPastDaysLogs = permissions.canEditPastDaysLogs,
                    canViewFinance = permissions.canViewFinance,
                    canEditFinance = permissions.canEditFinance,
                    canViewTasks = permissions.canViewTasks,
                    canCompleteTasks = permissions.canCompleteTasks,
                    canCreateTasks = permissions.canCreateTasks,
                    canViewRequests = permissions.canViewRequests,
                    canSubmitRequests = permissions.canSubmitRequests
                )
                onUpdateWorker(updated)
                editingWorker = null
            }
        )
    }

    workerToDelete?.let { worker ->
        Dialog(onDismissRequest = { workerToDelete = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Delete Worker Account", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Are you sure you want to permanently delete ${worker.name}? They will lose all access to $farmName.", fontSize = 13.sp, color = Color(0xFF475569))
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = { workerToDelete = null }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = {
                                onDeleteWorker(worker.workerId)
                                workerToDelete = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                        ) {
                            Text("Delete Worker")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WorkerAccountCard(
    worker: WorkerAccount,
    onEdit: () -> Unit,
    onToggleRevoke: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("worker_card_${worker.workerId}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = if (worker.isRevoked) Color(0xFFFEE2E2) else Color(0xFFDCFCE7)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.Person,
                                contentDescription = null,
                                tint = if (worker.isRevoked) Color(0xFFDC2626) else ForestGreenPrimary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = worker.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = worker.emailOrPhone,
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (worker.isRevoked) Color(0xFFFEE2E2) else Color(0xFFDCFCE7)
                ) {
                    Text(
                        text = if (worker.isRevoked) "REVOKED" else "ACTIVE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (worker.isRevoked) Color(0xFFDC2626) else ForestGreenDark,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Permissions preview pills across all 5 tabs
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PermissionPill("Home", worker.canViewHome, worker.canUseQuickActions, "Quick Entry", Modifier.weight(1f))
                    PermissionPill("Assets", worker.canViewLivestock, worker.canEditLivestock, "Manage", Modifier.weight(1f))
                    PermissionPill("Logs", worker.canViewLogs, worker.canEditLogs, if (worker.canEditPastDaysLogs) "All Days" else "Today Only", Modifier.weight(1f))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PermissionPill("Finance", worker.canViewFinance, worker.canEditFinance, "Transact", Modifier.weight(1f))
                    PermissionPill("Requests", worker.canViewTasks || worker.canViewRequests, worker.canCompleteTasks || worker.canSubmitRequests, "Full", Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Key, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Secure Login Account", fontSize = 11.sp, color = Color(0xFF64748B))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Revoke / Reactivate toggle
                    TextButton(
                        onClick = onToggleRevoke,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (worker.isRevoked) ForestGreenPrimary else Color(0xFFD97706)
                        )
                    ) {
                        Icon(
                            if (worker.isRevoked) Icons.Filled.LockOpen else Icons.Filled.Block,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (worker.isRevoked) "Activate" else "Revoke", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(onClick = onEdit) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit Worker", tint = ForestGreenPrimary, modifier = Modifier.size(18.dp))
                    }

                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete Worker", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionPill(
    name: String,
    canView: Boolean,
    canEdit: Boolean,
    actionLabel: String = "Full",
    modifier: Modifier = Modifier
) {
    val bg = if (canView) {
        if (canEdit) Color(0xFFDCFCE7) else Color(0xFFE2E8F0)
    } else {
        Color(0xFFF8FAFC)
    }
    val text = if (canView) {
        if (canEdit) "$name ($actionLabel)" else "$name (View)"
    } else {
        "$name (Off)"
    }
    val textColor = if (canView) {
        if (canEdit) ForestGreenDark else Color(0xFF1E293B)
    } else {
        Color(0xFF94A3B8)
    }

    Surface(shape = RoundedCornerShape(4.dp), color = bg, modifier = modifier) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun AddOrEditWorkerDialog(
    worker: WorkerAccount?,
    onDismiss: () -> Unit,
    onSave: (name: String, emailOrPhone: String, pass: String, permissions: WorkerPermissions) -> Unit
) {
    var name by remember { mutableStateOf(worker?.name ?: "") }
    var emailOrPhone by remember { mutableStateOf(worker?.emailOrPhone ?: "") }
    var password by remember { mutableStateOf("") }
    var passVisible by remember { mutableStateOf(false) }

    // Granular 5-Tab Permissions State
    var canViewHome by remember { mutableStateOf(worker?.canViewHome ?: true) }
    var canUseQuickActions by remember { mutableStateOf(worker?.canUseQuickActions ?: true) }

    var canViewLivestock by remember { mutableStateOf(worker?.canViewLivestock ?: true) }
    var canEditLivestock by remember { mutableStateOf(worker?.canEditLivestock ?: true) }

    var canViewLogs by remember { mutableStateOf(worker?.canViewLogs ?: true) }
    var canEditLogs by remember { mutableStateOf(worker?.canEditLogs ?: true) }
    var canEditPastDaysLogs by remember { mutableStateOf(worker?.canEditPastDaysLogs ?: true) }

    var canViewFinance by remember { mutableStateOf(worker?.canViewFinance ?: false) }
    var canEditFinance by remember { mutableStateOf(worker?.canEditFinance ?: false) }

    var canViewTasks by remember { mutableStateOf(worker?.canViewTasks ?: true) }
    var canCompleteTasks by remember { mutableStateOf(worker?.canCompleteTasks ?: true) }
    var canCreateTasks by remember { mutableStateOf(worker?.canCreateTasks ?: true) }
    var canViewRequests by remember { mutableStateOf(worker?.canViewRequests ?: true) }
    var canSubmitRequests by remember { mutableStateOf(worker?.canSubmitRequests ?: true) }

    var validationError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .testTag("add_worker_dialog"),
            shape = RoundedCornerShape(20.dp),
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.PersonAdd, contentDescription = null, tint = ForestGreenPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (worker == null) "Create Worker Account" else "Edit Worker Permissions",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; validationError = null },
                    label = { Text("Worker Full Name") },
                    placeholder = { Text("e.g. Samuel Mutua") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = emailOrPhone,
                    onValueChange = { emailOrPhone = it; validationError = null },
                    label = { Text("Worker Email or Phone Number") },
                    placeholder = { Text("e.g. samuel@mkulima.farm or 0712345678") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; validationError = null },
                    label = { Text(if (worker == null) "Initial Worker Password" else "Password (managed separately)") },
                    placeholder = { Text(if (worker == null) "At least 6 characters" else "Leave blank to keep current password") },
                    trailingIcon = {
                        IconButton(onClick = { passVisible = !passVisible }) {
                            Icon(if (passVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = null)
                        }
                    },
                    visualTransformation = if (passVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Permission Presets Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Granular Tab & Feature Controls",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "Control visible tabs and enabled actions:",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            canViewHome = true
                            canUseQuickActions = true
                            canViewLivestock = true
                            canEditLivestock = true
                            canViewLogs = true
                            canEditLogs = true
                            canEditPastDaysLogs = true
                            canViewFinance = true
                            canEditFinance = true
                            canViewTasks = true
                            canCompleteTasks = true
                            canCreateTasks = true
                            canViewRequests = true
                            canSubmitRequests = true
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Full", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            canViewHome = true
                            canUseQuickActions = true
                            canViewLivestock = true
                            canEditLivestock = true
                            canViewLogs = true
                            canEditLogs = true
                            canEditPastDaysLogs = false
                            canViewFinance = false
                            canEditFinance = false
                            canViewTasks = true
                            canCompleteTasks = true
                            canCreateTasks = true
                            canViewRequests = true
                            canSubmitRequests = true
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Standard", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            canViewHome = true
                            canUseQuickActions = true
                            canViewLivestock = false
                            canEditLivestock = false
                            canViewLogs = true
                            canEditLogs = true
                            canEditPastDaysLogs = false
                            canViewFinance = false
                            canEditFinance = false
                            canViewTasks = true
                            canCompleteTasks = true
                            canCreateTasks = false
                            canViewRequests = true
                            canSubmitRequests = true
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Milker", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 1. Home Tab Control
                PermissionToggleCard(
                    title = "1. Home Tab (Dashboard)",
                    canView = canViewHome,
                    onViewChange = { canViewHome = it },
                    features = listOf(
                        PermissionFeatureToggle(
                            label = "Allow Quick 1-Tap Entry on Home (Quick Milk, Eggs, Livestock)",
                            isEnabled = canUseQuickActions,
                            onToggle = { canUseQuickActions = it }
                        )
                    )
                )

                // 2. Assets Tab Control
                PermissionToggleCard(
                    title = "2. Assets Tab (Livestock, Crops & Inventory)",
                    canView = canViewLivestock,
                    onViewChange = { canViewLivestock = it },
                    features = listOf(
                        PermissionFeatureToggle(
                            label = "Allow Adding, Editing & Deleting Livestock, Crops & Inventory",
                            isEnabled = canEditLivestock,
                            onToggle = { canEditLivestock = it }
                        )
                    )
                )

                // 3. Log Tab Control
                PermissionToggleCard(
                    title = "3. Daily Logs Tab (Milk & Egg Production)",
                    canView = canViewLogs,
                    onViewChange = { canViewLogs = it },
                    features = listOf(
                        PermissionFeatureToggle(
                            label = "Allow Recording, Editing & Deleting Milk & Egg Logs",
                            isEnabled = canEditLogs,
                            onToggle = { canEditLogs = it }
                        ),
                        PermissionFeatureToggle(
                            label = "Allow Editing / Deleting Past Days' Entries (If OFF: Locks previous days' logs & records to prevent backdated modifications)",
                            isEnabled = canEditPastDaysLogs,
                            onToggle = { canEditPastDaysLogs = it }
                        )
                    )
                )

                // 4. Finance Tab Control
                PermissionToggleCard(
                    title = "4. Finance Tab (Income, Expenses & Reports)",
                    canView = canViewFinance,
                    onViewChange = { canViewFinance = it },
                    features = listOf(
                        PermissionFeatureToggle(
                            label = "Allow Adding & Modifying Financial Transactions",
                            isEnabled = canEditFinance,
                            onToggle = { canEditFinance = it }
                        )
                    )
                )

                // 5. Requests & Tasks Tab Control
                PermissionToggleCard(
                    title = "5. Requests & Tasks Tab",
                    canView = canViewTasks || canViewRequests,
                    onViewChange = {
                        canViewTasks = it
                        canViewRequests = it
                    },
                    features = listOf(
                        PermissionFeatureToggle(
                            label = "Allow Completing Tasks & Uploading Photo Proofs",
                            isEnabled = canCompleteTasks,
                            onToggle = { canCompleteTasks = it }
                        ),
                        PermissionFeatureToggle(
                            label = "Allow Assigning & Creating New Farm Tasks",
                            isEnabled = canCreateTasks,
                            onToggle = { canCreateTasks = it }
                        ),
                        PermissionFeatureToggle(
                            label = "Allow Submitting Salary Advances & Leave Requests",
                            isEnabled = canSubmitRequests,
                            onToggle = { canSubmitRequests = it }
                        )
                    )
                )

                validationError?.let { err ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(err, color = Color(0xFFDC2626), fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            if (name.isBlank() || emailOrPhone.isBlank()) {
                                validationError = "Please fill in Name and Login identifier."
                                return@Button
                            }
                            if (worker == null && password.length < 6) {
                                validationError = "Initial worker password must be at least 6 characters."
                                return@Button
                            }
                            val perms = WorkerPermissions(
                                canViewHome = canViewHome,
                                canUseQuickActions = if (canViewHome) canUseQuickActions else false,
                                canViewLivestock = canViewLivestock,
                                canEditLivestock = if (canViewLivestock) canEditLivestock else false,
                                canViewLogs = canViewLogs,
                                canEditLogs = if (canViewLogs) canEditLogs else false,
                                canEditPastDaysLogs = if (canViewLogs) canEditPastDaysLogs else false,
                                canViewFinance = canViewFinance,
                                canEditFinance = if (canViewFinance) canEditFinance else false,
                                canViewTasks = canViewTasks,
                                canCompleteTasks = if (canViewTasks) canCompleteTasks else false,
                                canCreateTasks = if (canViewTasks) canCreateTasks else false,
                                canViewRequests = canViewRequests,
                                canSubmitRequests = if (canViewRequests || canViewTasks) canSubmitRequests else false
                            )
                            onSave(name, emailOrPhone, password, perms)
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                        modifier = Modifier.testTag("btn_save_worker")
                    ) {
                        Text(if (worker == null) "Create Worker" else "Save Changes", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

data class PermissionFeatureToggle(
    val label: String,
    val isEnabled: Boolean,
    val onToggle: (Boolean) -> Unit
)

@Composable
fun PermissionToggleCard(
    title: String,
    canView: Boolean,
    onViewChange: (Boolean) -> Unit,
    features: List<PermissionFeatureToggle> = emptyList()
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (canView) Color(0xFFF1FDF4) else Color(0xFFF8FAFC)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = CircleShape,
                        color = if (canView) Color(0xFFDCFCE7) else Color(0xFFE2E8F0),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                if (canView) Icons.Filled.Check else Icons.Filled.Close,
                                contentDescription = null,
                                tint = if (canView) ForestGreenDark else Color(0xFF94A3B8),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (canView) Color(0xFF0F172A) else Color(0xFF64748B)
                    )
                }
                Switch(
                    checked = canView,
                    onCheckedChange = onViewChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = ForestGreenPrimary
                    )
                )
            }

            if (canView && features.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Divider(color = Color(0xFFDCFCE7), thickness = 1.dp)
                Spacer(modifier = Modifier.height(4.dp))
                features.forEach { feature ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { feature.onToggle(!feature.isEnabled) }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = feature.isEnabled,
                            onCheckedChange = feature.onToggle,
                            colors = CheckboxDefaults.colors(checkedColor = ForestGreenPrimary)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = feature.label,
                            fontSize = 11.sp,
                            color = Color(0xFF334155),
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}
