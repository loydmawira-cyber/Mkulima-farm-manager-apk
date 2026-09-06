package com.example.ui.components

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.FarmSettings
import com.example.data.UserSession
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenPrimary
import com.example.util.NotificationHelper

/**
 * A cleanly organized, modular, full-height Settings surface that slides in from the left.
 * Structured into modern M3 cards with clear category divisions, crisp typography, and zero clutter.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlidingSettingsPanel(
    visible: Boolean,
    settings: FarmSettings,
    userSession: UserSession?,
    onDismiss: () -> Unit,
    onSaveSettings: (FarmSettings) -> Unit,
    onSaveFarmName: (String) -> Unit,
    onSaveRecoveryEmail: (String) -> Unit,
    onOpenWorkerManagement: () -> Unit,
    onOpenSubscriptionBilling: () -> Unit,
    onLogout: () -> Unit
) {
    val panelTransition = remember { MutableTransitionState(false) }
    panelTransition.targetState = visible
    if (!panelTransition.currentState && !panelTransition.targetState) return

    // Draft states
    var farmNameDraft by remember(userSession?.farmName) { mutableStateOf(userSession?.farmName.orEmpty()) }
    var farmTypeDraft by remember(settings.farmType) { mutableStateOf(settings.farmType) }
    var themeModeDraft by remember(settings.themeMode) {
        mutableStateOf(settings.themeMode.ifBlank { "CLASSIC" }.uppercase())
    }
    var monthlyReportsEnabledDraft by remember(settings.monthlyReportsEnabled) { mutableStateOf(settings.monthlyReportsEnabled) }
    var notificationsEnabledDraft by remember(settings.notificationsEnabled) { mutableStateOf(settings.notificationsEnabled) }
    var notifyMilkLogsDraft by remember(settings.notifyMilkLogs) { mutableStateOf(settings.notifyMilkLogs) }
    var notifyNewEntriesDraft by remember(settings.notifyNewEntries) { mutableStateOf(settings.notifyNewEntries) }
    var notifyAccountChangesDraft by remember(settings.notifyAccountChanges) { mutableStateOf(settings.notifyAccountChanges) }
    var notifyDeletionsDraft by remember(settings.notifyDeletions) { mutableStateOf(settings.notifyDeletions) }
    var notifyRemindersDraft by remember(settings.notifyReminders) { mutableStateOf(settings.notifyReminders) }

    // Collapsible section state for recovery email
    var showRecoveryEmailEditor by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var hasSystemPermission by remember {
        mutableStateOf(NotificationHelper.isSystemPermissionGranted(context))
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasSystemPermission = granted
        if (granted) {
            NotificationHelper.createChannels(context)
            Toast.makeText(context, "System notification permission granted!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "System notification permission was not granted.", Toast.LENGTH_SHORT).show()
        }
    }

    val savedRecoveryEmail = userSession?.recoveryEmail.orEmpty().ifBlank {
        if (userSession?.emailOrPhone?.contains("@") == true) userSession.emailOrPhone else ""
    }
    var recoveryEmailDraft by remember(savedRecoveryEmail) { mutableStateOf(savedRecoveryEmail) }
    val isOwner = userSession?.isOwner == true
    val canSaveFarmName = isOwner && farmNameDraft.trim().isNotBlank() && farmNameDraft.trim() != userSession?.farmName.orEmpty()
    val canSaveRecoveryEmail = isOwner && recoveryEmailDraft.trim().contains("@") && recoveryEmailDraft.trim().contains(".") && recoveryEmailDraft.trim() != savedRecoveryEmail

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.40f))
        ) {
            // Dismiss background tap
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = onDismiss)
            )

            AnimatedVisibility(
                visibleState = panelTransition,
                enter = slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(durationMillis = 280)
                ) + fadeIn(),
                exit = slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(durationMillis = 240)
                ) + fadeOut(),
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.90f)
                        .widthIn(max = 440.dp),
                    shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                    color = Color(0xFFF8FAFC),
                    shadowElevation = 20.dp
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // 1. Elegant Header Bar
                        SettingsHeader(
                            onDismiss = onDismiss,
                            farmName = userSession?.farmName.orEmpty().ifBlank { "Mkulima Farm" }
                        )

                        // 2. Scrollable Body with Clean Categorized Cards
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                                .imePadding()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .navigationBarsPadding(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Section 1: User Profile & Account
                            SettingsSectionCard(
                                title = "ACCOUNT & TEAM",
                                icon = Icons.Filled.Person
                            ) {
                                UserProfileBlock(
                                    userSession = userSession,
                                    isOwner = isOwner,
                                    savedRecoveryEmail = savedRecoveryEmail
                                )

                                if (isOwner) {
                                    Spacer(Modifier.height(12.dp))
                                    Divider(color = Color(0xFFE2E8F0))
                                    Spacer(Modifier.height(12.dp))

                                    // Quick Owner Action Buttons
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                onDismiss()
                                                onOpenWorkerManagement()
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFE8F5E9),
                                                contentColor = Color(0xFF166534)
                                            ),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
                                        ) {
                                            Icon(Icons.Filled.Groups, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("Farm Workers", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = {
                                                onDismiss()
                                                onOpenSubscriptionBilling()
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFDDF5E7),
                                                contentColor = Color(0xFF14532D)
                                            ),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
                                        ) {
                                            Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("Subscription", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(Modifier.height(10.dp))

                                    // Collapsible / Clean Recovery Email Card
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFF1F5F9),
                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        Icons.Filled.Shield,
                                                        contentDescription = null,
                                                        tint = ForestGreenPrimary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(Modifier.width(8.dp))
                                                    Column {
                                                        Text(
                                                            "Account Recovery Email",
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFF1E293B)
                                                        )
                                                        Text(
                                                            if (savedRecoveryEmail.isNotBlank()) savedRecoveryEmail else "Not set (recommended)",
                                                            fontSize = 11.sp,
                                                            color = if (savedRecoveryEmail.isNotBlank()) ForestGreenDark else Color(0xFF94A3B8),
                                                            fontWeight = if (savedRecoveryEmail.isNotBlank()) FontWeight.SemiBold else FontWeight.Normal
                                                        )
                                                    }
                                                }

                                                IconButton(
                                                    onClick = { showRecoveryEmailEditor = !showRecoveryEmailEditor },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (showRecoveryEmailEditor) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                                        contentDescription = "Toggle Recovery Email",
                                                        tint = Color(0xFF64748B)
                                                    )
                                                }
                                            }

                                            AnimatedVisibility(
                                                visible = showRecoveryEmailEditor,
                                                enter = expandVertically() + fadeIn(),
                                                exit = shrinkVertically() + fadeOut()
                                            ) {
                                                Column(modifier = Modifier.padding(top = 10.dp)) {
                                                    OutlinedTextField(
                                                        value = recoveryEmailDraft,
                                                        onValueChange = { recoveryEmailDraft = it },
                                                        label = { Text("Enter recovery email", fontSize = 12.sp) },
                                                        placeholder = { Text("user@example.com") },
                                                        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                                        singleLine = true,
                                                        modifier = Modifier.fillMaxWidth(),
                                                        shape = RoundedCornerShape(10.dp)
                                                    )
                                                    Spacer(Modifier.height(8.dp))
                                                    Button(
                                                        onClick = {
                                                            onSaveRecoveryEmail(recoveryEmailDraft.trim())
                                                            showRecoveryEmailEditor = false
                                                        },
                                                        enabled = canSaveRecoveryEmail,
                                                        modifier = Modifier.fillMaxWidth(),
                                                        shape = RoundedCornerShape(10.dp),
                                                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                                                    ) {
                                                        Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(15.dp))
                                                        Spacer(Modifier.width(6.dp))
                                                        Text("Save Recovery Email", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Section 2: Farm Profile & Configuration
                            SettingsSectionCard(
                                title = "FARM CONFIGURATION",
                                icon = Icons.Filled.Home
                            ) {
                                // Farm Name
                                Text(
                                    "Farm Name",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                                Spacer(Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = farmNameDraft,
                                    onValueChange = { farmNameDraft = it },
                                    enabled = isOwner,
                                    label = { Text("Farm name") },
                                    leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    supportingText = {
                                        Text(
                                            if (isOwner) "Shown on dashboard and shared across all workers." else "Only the farm owner can rename the farm.",
                                            fontSize = 11.sp
                                        )
                                    },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                if (canSaveFarmName) {
                                    Spacer(Modifier.height(6.dp))
                                    Button(
                                        onClick = { onSaveFarmName(farmNameDraft.trim()) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                                    ) {
                                        Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Update Farm Name", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }

                                Spacer(Modifier.height(14.dp))
                                Divider(color = Color(0xFFE2E8F0))
                                Spacer(Modifier.height(14.dp))

                                // Farm Type
                                Text(
                                    "Farm Livestock Type",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    "Filters dashboards and feed plans to your livestock specialty.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    listOf("Cattle Only", "Poultry Only", "Both").forEach { type ->
                                        FilterChip(
                                            selected = farmTypeDraft == type,
                                            onClick = {
                                                if (isOwner) {
                                                    farmTypeDraft = type
                                                    onSaveSettings(settings.copy(farmType = type))
                                                }
                                            },
                                            enabled = isOwner,
                                            label = { Text(type, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Color(0xFFDDF5E7),
                                                selectedLabelColor = Color(0xFF14532D)
                                            ),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }
                                }
                            }

                            // Section 3: App Theme & Automation
                            SettingsSectionCard(
                                title = "PREFERENCES & REPORTS",
                                icon = Icons.Filled.Tune
                            ) {
                                // Theme Selector
                                Text(
                                    "App Theme",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    "Choose visual style for your device",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    listOf(
                                        Triple("CLASSIC", "Classic Light", Icons.Filled.LightMode),
                                        Triple("DARK", "Dark Theme", Icons.Filled.DarkMode),
                                        Triple("SYSTEM", "System Auto", Icons.Filled.PhoneAndroid)
                                    ).forEach { (value, label, icon) ->
                                        FilterChip(
                                            selected = themeModeDraft == value,
                                            onClick = {
                                                if (isOwner) {
                                                    themeModeDraft = value
                                                    onSaveSettings(settings.copy(themeMode = value))
                                                }
                                            },
                                            leadingIcon = {
                                                Icon(icon, contentDescription = null, modifier = Modifier.size(15.dp))
                                            },
                                            enabled = isOwner,
                                            label = { Text(label, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Color(0xFFDDF5E7),
                                                selectedLabelColor = Color(0xFF14532D)
                                            ),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }
                                }

                                Spacer(Modifier.height(14.dp))
                                Divider(color = Color(0xFFE2E8F0))
                                Spacer(Modifier.height(14.dp))

                                // Monthly Reports Toggle
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFFE0F2FE),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Filled.Assessment,
                                                contentDescription = null,
                                                tint = Color(0xFF0284C7),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Automatic Monthly Reports",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E293B)
                                        )
                                        Text(
                                            "Generates end-of-month financial summaries under Finance → Reports.",
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                    Switch(
                                        checked = monthlyReportsEnabledDraft,
                                        onCheckedChange = { enabled ->
                                            if (isOwner) {
                                                monthlyReportsEnabledDraft = enabled
                                                onSaveSettings(settings.copy(monthlyReportsEnabled = enabled))
                                            }
                                        },
                                        enabled = isOwner,
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = ForestGreenPrimary
                                        )
                                    )
                                }
                            }

                            // Section 4: Notifications & Alerts
                            SettingsSectionCard(
                                title = "PUSH NOTIFICATIONS & ALERTS",
                                icon = Icons.Filled.Notifications
                            ) {
                                // Master Toggle
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (notificationsEnabledDraft) Color(0xFFDDF5E7) else Color(0xFFF1F5F9),
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = if (notificationsEnabledDraft) Icons.Filled.NotificationsActive else Icons.Filled.NotificationsOff,
                                                contentDescription = null,
                                                tint = if (notificationsEnabledDraft) ForestGreenPrimary else Color(0xFF64748B),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Push Notifications",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF153E2D)
                                        )
                                        Text(
                                            "Get live alerts for logs, reminders, and farm tasks",
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                    Switch(
                                        checked = notificationsEnabledDraft,
                                        onCheckedChange = { enabled ->
                                            notificationsEnabledDraft = enabled
                                            val updated = settings.copy(
                                                notificationsEnabled = enabled,
                                                notifyMilkLogs = notifyMilkLogsDraft,
                                                notifyNewEntries = notifyNewEntriesDraft,
                                                notifyAccountChanges = notifyAccountChangesDraft,
                                                notifyDeletions = notifyDeletionsDraft,
                                                notifyReminders = notifyRemindersDraft
                                            )
                                            onSaveSettings(updated)
                                            NotificationHelper.cacheNotificationPreferences(context, updated)

                                            if (enabled && !hasSystemPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                            }
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = ForestGreenPrimary
                                        )
                                    )
                                }

                                // System Permission Notice
                                if (!hasSystemPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    Spacer(Modifier.height(10.dp))
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFFFFFBEB),
                                        border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Filled.Warning,
                                                contentDescription = null,
                                                tint = Color(0xFFD97706),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    "System Permission Required",
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF92400E)
                                                )
                                                Text(
                                                    "Notifications are blocked by Android system settings.",
                                                    fontSize = 10.5.sp,
                                                    color = Color(0xFFB45309)
                                                )
                                            }
                                            Spacer(Modifier.width(6.dp))
                                            Button(
                                                onClick = {
                                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                                },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                                            ) {
                                                Text("ALLOW", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }

                                // Granular Categories (Visible when master toggle is on)
                                AnimatedVisibility(
                                    visible = notificationsEnabledDraft,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(top = 12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Divider(color = Color(0xFFE2E8F0))
                                        Spacer(Modifier.height(4.dp))

                                        Text(
                                            "ALERT CATEGORIES",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF475569)
                                        )

                                        NotificationCategoryRow(
                                            title = "Milk & Production Logs",
                                            description = "Milk entries, usage dispatch, and egg yields",
                                            checked = notifyMilkLogsDraft,
                                            onCheckedChange = { checked ->
                                                notifyMilkLogsDraft = checked
                                                val updated = settings.copy(
                                                    notificationsEnabled = notificationsEnabledDraft,
                                                    notifyMilkLogs = checked,
                                                    notifyNewEntries = notifyNewEntriesDraft,
                                                    notifyAccountChanges = notifyAccountChangesDraft,
                                                    notifyDeletions = notifyDeletionsDraft,
                                                    notifyReminders = notifyRemindersDraft
                                                )
                                                onSaveSettings(updated)
                                                NotificationHelper.cacheNotificationPreferences(context, updated)
                                            }
                                        )

                                        NotificationCategoryRow(
                                            title = "New Entries & Farm Activity",
                                            description = "New livestock, flocks, tasks, finance records, and fields",
                                            checked = notifyNewEntriesDraft,
                                            onCheckedChange = { checked ->
                                                notifyNewEntriesDraft = checked
                                                val updated = settings.copy(
                                                    notificationsEnabled = notificationsEnabledDraft,
                                                    notifyMilkLogs = notifyMilkLogsDraft,
                                                    notifyNewEntries = checked,
                                                    notifyAccountChanges = notifyAccountChangesDraft,
                                                    notifyDeletions = notifyDeletionsDraft,
                                                    notifyReminders = notifyRemindersDraft
                                                )
                                                onSaveSettings(updated)
                                                NotificationHelper.cacheNotificationPreferences(context, updated)
                                            }
                                        )

                                        NotificationCategoryRow(
                                            title = "Edits & Account Changes",
                                            description = "Farm profile, recovery email, animal profiles, and workers",
                                            checked = notifyAccountChangesDraft,
                                            onCheckedChange = { checked ->
                                                notifyAccountChangesDraft = checked
                                                val updated = settings.copy(
                                                    notificationsEnabled = notificationsEnabledDraft,
                                                    notifyMilkLogs = notifyMilkLogsDraft,
                                                    notifyNewEntries = notifyNewEntriesDraft,
                                                    notifyAccountChanges = checked,
                                                    notifyDeletions = notifyDeletionsDraft,
                                                    notifyReminders = notifyRemindersDraft
                                                )
                                                onSaveSettings(updated)
                                                NotificationHelper.cacheNotificationPreferences(context, updated)
                                            }
                                        )

                                        NotificationCategoryRow(
                                            title = "Deletions & Removals",
                                            description = "High-priority alerts when animals, logs, or records are deleted",
                                            checked = notifyDeletionsDraft,
                                            onCheckedChange = { checked ->
                                                notifyDeletionsDraft = checked
                                                val updated = settings.copy(
                                                    notificationsEnabled = notificationsEnabledDraft,
                                                    notifyMilkLogs = notifyMilkLogsDraft,
                                                    notifyNewEntries = notifyNewEntriesDraft,
                                                    notifyAccountChanges = notifyAccountChangesDraft,
                                                    notifyDeletions = checked,
                                                    notifyReminders = notifyRemindersDraft
                                                )
                                                onSaveSettings(updated)
                                                NotificationHelper.cacheNotificationPreferences(context, updated)
                                            }
                                        )

                                        NotificationCategoryRow(
                                            title = "Task & Health Reminders",
                                            description = "Upcoming tasks, vaccinations, breeding dates, and low feed alerts",
                                            checked = notifyRemindersDraft,
                                            onCheckedChange = { checked ->
                                                notifyRemindersDraft = checked
                                                val updated = settings.copy(
                                                    notificationsEnabled = notificationsEnabledDraft,
                                                    notifyMilkLogs = notifyMilkLogsDraft,
                                                    notifyNewEntries = notifyNewEntriesDraft,
                                                    notifyAccountChanges = notifyAccountChangesDraft,
                                                    notifyDeletions = notifyDeletionsDraft,
                                                    notifyReminders = checked
                                                )
                                                onSaveSettings(updated)
                                                NotificationHelper.cacheNotificationPreferences(context, updated)
                                            }
                                        )

                                        Spacer(Modifier.height(6.dp))

                                        // Action buttons for notifications
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    if (!hasSystemPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                                    } else {
                                                        NotificationHelper.sendTestNotification(
                                                            context = context,
                                                            settings = settings.copy(
                                                                notificationsEnabled = true,
                                                                notifyMilkLogs = notifyMilkLogsDraft,
                                                                notifyNewEntries = notifyNewEntriesDraft,
                                                                notifyAccountChanges = notifyAccountChangesDraft,
                                                                notifyDeletions = notifyDeletionsDraft,
                                                                notifyReminders = notifyRemindersDraft
                                                            )
                                                        )
                                                        Toast.makeText(context, "Test notification sent! Check your notification bar.", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.weight(1f),
                                                contentPadding = PaddingValues(vertical = 10.dp)
                                            ) {
                                                Icon(Icons.Filled.Notifications, contentDescription = null, modifier = Modifier.size(15.dp))
                                                Spacer(Modifier.width(6.dp))
                                                Text("Test Alert", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                            }

                                            Button(
                                                onClick = {
                                                    val updated = settings.copy(
                                                        notificationsEnabled = notificationsEnabledDraft,
                                                        notifyMilkLogs = notifyMilkLogsDraft,
                                                        notifyNewEntries = notifyNewEntriesDraft,
                                                        notifyAccountChanges = notifyAccountChangesDraft,
                                                        notifyDeletions = notifyDeletionsDraft,
                                                        notifyReminders = notifyRemindersDraft
                                                    )
                                                    onSaveSettings(updated)
                                                    NotificationHelper.cacheNotificationPreferences(context, updated)
                                                    Toast.makeText(context, "Notification preferences saved!", Toast.LENGTH_SHORT).show()
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                                                modifier = Modifier.weight(1f),
                                                contentPadding = PaddingValues(vertical = 10.dp)
                                            ) {
                                                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(15.dp))
                                                Spacer(Modifier.width(6.dp))
                                                Text("Save Prefs", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }

                            // Footer Section: Log Out & App Version
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Button(
                                        onClick = onLogout,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFFEE2E2),
                                            contentColor = Color(0xFFB91C1C)
                                        ),
                                        contentPadding = PaddingValues(vertical = 12.dp)
                                    ) {
                                        Icon(Icons.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("LOG OUT OF FARM", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                                    }

                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Mkulima Dairy & Poultry Farm Manager • v2.4",
                                        fontSize = 10.5.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

/** Header component for the sliding settings drawer */
@Composable
private fun SettingsHeader(
    onDismiss: () -> Unit,
    farmName: String
) {
    Surface(
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFE8F5E9),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.Tune,
                        contentDescription = null,
                        tint = ForestGreenPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    farmName,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFFF1F5F9), shape = CircleShape)
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Close settings",
                    tint = Color(0xFF334155),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/** Reusable modern M3 Card section with consistent header styling */
@Composable
private fun SettingsSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = ForestGreenPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF475569),
                    letterSpacing = 0.5.sp
                )
            }

            content()
        }
    }
}

/** User profile banner displayed in the Account section */
@Composable
private fun UserProfileBlock(
    userSession: UserSession?,
    isOwner: Boolean,
    savedRecoveryEmail: String
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = if (isOwner) Color(0xFFDCFCE7) else Color(0xFFE0F2FE)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = (userSession?.name?.firstOrNull()?.uppercase() ?: "U"),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = if (isOwner) ForestGreenDark else Color(0xFF0369A1)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = userSession?.name ?: "Farm User",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        color = Color(0xFF1E293B)
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isOwner) Color(0xFFDCFCE7) else Color(0xFFE0F2FE)
                    ) {
                        Text(
                            text = if (isOwner) "OWNER" else "WORKER",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isOwner) ForestGreenDark else Color(0xFF0369A1),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = userSession?.emailOrPhone.orEmpty().ifBlank { "Signed In" },
                    fontSize = 11.5.sp,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}

/** Row item for granular notification category toggles */
@Composable
private fun NotificationCategoryRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (checked) Color(0xFFF8FAFC) else Color(0xFFFAFAFA),
        border = BorderStroke(1.dp, if (checked) Color(0xFFDDF5E7) else Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = description,
                    fontSize = 10.5.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 13.sp
                )
            }
            Spacer(Modifier.width(8.dp))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = ForestGreenPrimary
                )
            )
        }
    }
}
