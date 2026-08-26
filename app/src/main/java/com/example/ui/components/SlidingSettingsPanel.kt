package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Logout
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
    onLogout: () -> Unit
) {
    val panelTransition = remember { MutableTransitionState(false) }
    panelTransition.targetState = visible
    if (!panelTransition.currentState && !panelTransition.targetState) return

    var farmNameDraft by remember(userSession?.farmName) { mutableStateOf(userSession?.farmName.orEmpty()) }
    var farmTypeDraft by remember(settings.farmType) { mutableStateOf(settings.farmType) }
    var monthlyReportsEnabledDraft by remember(settings.monthlyReportsEnabled) { mutableStateOf(settings.monthlyReportsEnabled) }
    var recoveryEmailDraft by remember { mutableStateOf("") }
    val isOwner = userSession?.isOwner == true
    val canSaveFarmName = isOwner && farmNameDraft.trim().isNotBlank() && farmNameDraft.trim() != userSession?.farmName.orEmpty()
    val canSaveRecoveryEmail = isOwner && recoveryEmailDraft.trim().contains("@") && recoveryEmailDraft.trim().contains(".")

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
                                .padding(bottom = 24.dp)
                        ) {
                        Spacer(Modifier.height(8.dp))
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

                        if (isOwner) {
                            Spacer(Modifier.height(24.dp))
                            Text("ACCOUNT RECOVERY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                            Spacer(Modifier.height(8.dp))
                            Surface(shape = RoundedCornerShape(14.dp), color = Color(0xFFF3F8F5), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Recovery email", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF153E2D))
                                    Text("Use a real email address to receive secure password reset links. This is especially important for phone-number accounts.", fontSize = 11.sp, color = Color(0xFF64748B))
                                    Spacer(Modifier.height(10.dp))
                                    OutlinedTextField(
                                        value = recoveryEmailDraft,
                                        onValueChange = { recoveryEmailDraft = it },
                                        label = { Text("Recovery email address") },
                                        placeholder = { Text("you@example.com") },
                                        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
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
                        }

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

                        if (isOwner) {
                            Spacer(Modifier.height(24.dp))
                            Button(
                                onClick = onOpenWorkerManagement,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8F5E9), contentColor = Color(0xFF166534))
                            ) {
                                Icon(Icons.Filled.Groups, contentDescription = null)
                                Spacer(Modifier.height(0.dp))
                                Text("  MANAGE WORKERS", fontWeight = FontWeight.Bold)
                            }
                        }

                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding(),
                            color = Color(0xFFFCFDFB),
                            shadowElevation = 12.dp
                        ) {
                            Button(
                                onClick = onLogout,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 22.dp, vertical = 14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2), contentColor = Color(0xFFB91C1C))
                            ) {
                                Icon(Icons.Filled.Logout, contentDescription = null)
                                Text("  LOG OUT", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
