package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.FarmSettings
import com.example.data.UserSession
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenPrimary

/** A full-height Settings surface that slides in from the left instead of appearing as a pop-up. */
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

    var farmNameDraft by remember(userSession?.farmName) { mutableStateOf(userSession?.farmName.orEmpty()) }
    var farmTypeDraft by remember(settings.farmType) { mutableStateOf(settings.farmType) }
    var themeModeDraft by remember(settings.themeMode) {
        mutableStateOf(settings.themeMode.ifBlank { "CLASSIC" }.uppercase())
    }
    var monthlyReportsEnabledDraft by remember(settings.monthlyReportsEnabled) { mutableStateOf(settings.monthlyReportsEnabled) }
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
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.34f))) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = onDismiss)
            )

            AnimatedVisibility(
                visibleState = panelTransition,
                enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(),
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.88f)
                        .widthIn(max = 420.dp),
                    shape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp),
                    color = Color(0xFFFCFDFB),
                    shadowElevation = 16.dp
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 22.dp, vertical = 16.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Settings", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF153E2D))
                                Text("Farm preferences and account", fontSize = 13.sp, color = Color(0xFF64748B))
                            }
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Filled.Close, contentDescription = "Close settings", tint = Color(0xFF475569))
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                                .imePadding()
                                .padding(horizontal = 22.dp)
                                .padding(bottom = 48.dp)
                        ) {
                        Spacer(Modifier.height(8.dp))
                        Text("ACCOUNT & SESSION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                        Spacer(Modifier.height(8.dp))

                        // User profile card
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            modifier = Modifier.size(36.dp),
                                            shape = RoundedCornerShape(18.dp),
                                            color = if (isOwner) Color(0xFFDCFCE7) else Color(0xFFE0F2FE)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    Icons.Filled.Person,
                                                    contentDescription = null,
                                                    tint = if (isOwner) ForestGreenPrimary else Color(0xFF0284C7),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                        Spacer(Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = userSession?.name ?: "Farm User",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color(0xFF1E293B)
                                            )
                                            Text(
                                                text = userSession?.emailOrPhone.orEmpty().ifBlank { "Signed In" },
                                                fontSize = 11.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isOwner) Color(0xFFDCFCE7) else Color(0xFFE0F2FE)
                                    ) {
                                        Text(
                                            text = if (isOwner) "OWNER" else "WORKER",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isOwner) ForestGreenDark else Color(0xFF0369A1),
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                if (savedRecoveryEmail.isNotBlank()) {
                                    Spacer(Modifier.height(10.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color.White,
                                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Filled.Shield,
                                                contentDescription = null,
                                                tint = ForestGreenPrimary,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            Column {
                                                Text(
                                                    "RECOVERY EMAIL",
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF64748B)
                                                )
                                                Text(
                                                    savedRecoveryEmail,
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFF0F172A)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        if (isOwner) {
                            Button(
                                onClick = {
                                    onDismiss()
                                    onOpenWorkerManagement()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE8F5E9),
                                    contentColor = Color(0xFF166534)
                                )
                            ) {
                                Icon(Icons.Filled.Groups, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("MANAGE FARM WORKERS", fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    onDismiss()
                                    onOpenSubscriptionBilling()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFDDF5E7),
                                    contentColor = Color(0xFF14532D)
                                )
                            ) {
                                Text("SUBSCRIPTION & BILLING", fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(12.dp))
                            Surface(shape = RoundedCornerShape(14.dp), color = Color(0xFFF3F8F5), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Email, contentDescription = null, tint = ForestGreenPrimary, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Recovery Email", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF153E2D))
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text("Used for password recovery if you forget your credentials.", fontSize = 11.sp, color = Color(0xFF64748B))
                                    
                                    if (savedRecoveryEmail.isNotBlank()) {
                                        Spacer(Modifier.height(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFDCFCE7),
                                            border = BorderStroke(1.dp, ForestGreenPrimary.copy(alpha = 0.3f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Filled.Check, contentDescription = null, tint = ForestGreenPrimary, modifier = Modifier.size(14.dp))
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    "Saved: $savedRecoveryEmail",
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = ForestGreenDark
                                                )
                                            }
                                        }
                                    }

                                    Spacer(Modifier.height(10.dp))
                                    OutlinedTextField(
                                        value = recoveryEmailDraft,
                                        onValueChange = { recoveryEmailDraft = it },
                                        label = { Text("Update recovery email") },
                                        placeholder = { Text("you@example.com") },
                                        leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    Spacer(Modifier.height(10.dp))
                                    Button(
                                        onClick = { onSaveRecoveryEmail(recoveryEmailDraft.trim()) },
                                        enabled = canSaveRecoveryEmail,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                                    ) { Text("SAVE RECOVERY EMAIL", fontWeight = FontWeight.Bold) }
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                        }
                        Button(
                            onClick = onLogout,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2), contentColor = Color(0xFFB91C1C))
                        ) {
                            Icon(Icons.Filled.Logout, contentDescription = null)
                            Text("  LOG OUT", fontWeight = FontWeight.Bold)
                        }

                        Spacer(Modifier.height(24.dp))
                        Text("FARM PROFILE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = farmNameDraft,
                            onValueChange = { farmNameDraft = it },
                            enabled = isOwner,
                            label = { Text("Farm name") },
                            leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                            supportingText = { Text(if (isOwner) "Shown across the app and shared with your farm." else "Only the farm owner can rename the farm.") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = { onSaveFarmName(farmNameDraft.trim()) },
                            enabled = canSaveFarmName,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                        ) { Text("SAVE FARM NAME", fontWeight = FontWeight.Bold) }

                        Spacer(Modifier.height(24.dp))
                        Text("FARM TYPE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            listOf("Cattle Only", "Poultry Only", "Both").forEach { type ->
                                FilterChip(
                                    selected = farmTypeDraft == type,
                                    onClick = { if (isOwner) farmTypeDraft = type },
                                    enabled = isOwner,
                                    label = { Text(type, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFDDF5E7), selectedLabelColor = Color(0xFF14532D))
                                )
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = { onSaveSettings(settings.copy(farmType = farmTypeDraft)) },
                            enabled = isOwner && farmTypeDraft != settings.farmType,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                        ) { Text("SAVE FARM TYPE", fontWeight = FontWeight.Bold) }

                        Spacer(Modifier.height(24.dp))
                        Text("APP THEME", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFF3F8F5),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Choose appearance", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF153E2D))
                                Text("This preference is saved for this farm and applies after synchronization.", fontSize = 11.sp, color = Color(0xFF64748B))
                                Spacer(Modifier.height(10.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    listOf(
                                        "CLASSIC" to "Classic",
                                        "DARK" to "Dark",
                                        "SYSTEM" to "System"
                                    ).forEach { (value, label) ->
                                        FilterChip(
                                            selected = themeModeDraft == value,
                                            onClick = {
                                                if (isOwner) {
                                                    themeModeDraft = value
                                                    onSaveSettings(settings.copy(themeMode = value))
                                                }
                                            },
                                            enabled = isOwner,
                                            label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Color(0xFFDDF5E7),
                                                selectedLabelColor = Color(0xFF14532D)
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(24.dp))
                        Text("MONTHLY REPORTS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                        Spacer(Modifier.height(8.dp))
                        Surface(shape = RoundedCornerShape(14.dp), color = Color(0xFFF3F8F5), modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Automatic monthly report", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF153E2D))
                                    Text("Creates the previous month’s report on the first day and saves it under Finance → Reports.", fontSize = 11.sp, color = Color(0xFF64748B))
                                }
                                Switch(
                                    checked = monthlyReportsEnabledDraft,
                                    onCheckedChange = { enabled ->
                                        if (isOwner) {
                                            monthlyReportsEnabledDraft = enabled
                                            onSaveSettings(settings.copy(monthlyReportsEnabled = enabled))
                                        }
                                    },
                                    enabled = isOwner
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
