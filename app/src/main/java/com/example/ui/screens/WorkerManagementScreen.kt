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
                    canViewLivestock = permissions.canViewLivestock,
                    canEditLivestock = permissions.canEditLivestock,
                    canViewLogs = permissions.canViewLogs,
                    canEditLogs = permissions.canEditLogs,
                    canViewFinance = permissions.canViewFinance,
                    canEditFinance = permissions.canEditFinance,
                    canViewTasks = permissions.canViewTasks,
                    canCompleteTasks = permissions.canCompleteTasks,
                    canViewRequests = permissions.canViewRequests
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
                        Text(
                            text = "DEBUG PW: ${worker.password}",
                            fontSize = 10.sp,
                            color = Color(0xFFDC2626)
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

            // Permissions preview pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PermissionPill("Livestock", worker.canViewLivestock, worker.canEditLivestock)
                PermissionPill("Logs", worker.canViewLogs, worker.canEditLogs)
                PermissionPill("Finance", worker.canViewFinance, worker.canEditFinance)
                PermissionPill("Tasks", worker.canViewTasks, worker.canCompleteTasks)
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
                    Text("Pass: ••••••", fontSize = 11.sp, color = Color(0xFF64748B))
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
fun PermissionPill(name: String, canView: Boolean, canEdit: Boolean) {
    val bg = if (canView) Color(0xFFE2E8F0) else Color(0xFFF8FAFC)
    val text = if (canView) {
        if (canEdit) "$name (Full)" else "$name (View)"
    } else {
        "$name (Hidden)"
    }
    val textColor = if (canView) Color(0xFF1E293B) else Color(0xFF94A3B8)

    Surface(shape = RoundedCornerShape(4.dp), color = bg) {
        Text(
            text = text,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
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
    // Firebase Auth owns credentials, so existing workers never expose a
    // stored password. A password is required only while creating a new worker.
    var password by remember { mutableStateOf("") }
    var passVisible by remember { mutableStateOf(false) }

    var canViewLivestock by remember { mutableStateOf(worker?.canViewLivestock ?: true) }
    var canEditLivestock by remember { mutableStateOf(worker?.canEditLivestock ?: true) }
    var canViewLogs by remember { mutableStateOf(worker?.canViewLogs ?: true) }
    var canEditLogs by remember { mutableStateOf(worker?.canEditLogs ?: true) }
    var canViewFinance by remember { mutableStateOf(worker?.canViewFinance ?: false) }
    var canEditFinance by remember { mutableStateOf(worker?.canEditFinance ?: false) }
    var canViewTasks by remember { mutableStateOf(worker?.canViewTasks ?: true) }
    var canCompleteTasks by remember { mutableStateOf(worker?.canCompleteTasks ?: true) }
    var canViewRequests by remember { mutableStateOf(worker?.canViewRequests ?: true) }

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
                    placeholder = { Text(if (worker == null) "At least 6 characters" else "Leave blank to keep the current password") },
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

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Feature Permissions",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = "Choose which tabs and actions this worker can access:",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(8.dp))

                PermissionToggleRow("Livestock & Flocks Tab", canViewLivestock, { canViewLivestock = it }, "Can Add / Edit / Delete Animals", canEditLivestock, { canEditLivestock = it })
                PermissionToggleRow("Daily Logs Tab (Milk / Eggs)", canViewLogs, { canViewLogs = it }, "Can Record Milk & Egg Logs", canEditLogs, { canEditLogs = it })
                PermissionToggleRow("Finance & Store Tab", canViewFinance, { canViewFinance = it }, "Can Add Income / Expense Transactions", canEditFinance, { canEditFinance = it })
                PermissionToggleRow("Farm Tasks & Dashboard", canViewTasks, { canViewTasks = it }, "Can Complete Tasks & Upload Proofs", canCompleteTasks, { canCompleteTasks = it })

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Requests & Advance Tab", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF334155))
                    Switch(
                        checked = canViewRequests,
                        onCheckedChange = { canViewRequests = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = ForestGreenPrimary)
                    )
                }

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
                                canViewLivestock = canViewLivestock,
                                canEditLivestock = if (canViewLivestock) canEditLivestock else false,
                                canViewLogs = canViewLogs,
                                canEditLogs = if (canViewLogs) canEditLogs else false,
                                canViewFinance = canViewFinance,
                                canEditFinance = if (canViewFinance) canEditFinance else false,
                                canViewTasks = canViewTasks,
                                canCompleteTasks = if (canViewTasks) canCompleteTasks else false,
                                canViewRequests = canViewRequests
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

@Composable
fun PermissionToggleRow(
    title: String,
    canView: Boolean,
    onViewChange: (Boolean) -> Unit,
    subActionLabel: String,
    canAction: Boolean,
    onActionChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
            Switch(
                checked = canView,
                onCheckedChange = onViewChange,
                colors = SwitchDefaults.colors(checkedThumbColor = ForestGreenPrimary)
            )
        }

        if (canView) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = canAction,
                    onCheckedChange = onActionChange,
                    colors = CheckboxDefaults.colors(checkedColor = ForestGreenPrimary)
                )
                Text(subActionLabel, fontSize = 11.sp, color = Color(0xFF475569))
            }
        }
    }
}
