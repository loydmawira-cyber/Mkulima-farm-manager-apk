package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.FarmSettings
import com.example.data.UserSession
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenPrimary

@Composable
fun SettingsDialog(
    settings: FarmSettings,
    userSession: UserSession?,
    onDismiss: () -> Unit,
    onSaveSettings: (FarmSettings) -> Unit,
    onOpenWorkerManagement: (() -> Unit)? = null,
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedFarmType by remember { mutableStateOf(settings.farmType) }
    var currency by remember { mutableStateOf(settings.currency) }
    var weaningDays by remember { mutableStateOf(settings.weaningReminderDays.toString()) }
    var pdDays by remember { mutableStateOf(settings.pregnancyCheckReminderDays.toString()) }
    var dryOffDays by remember { mutableStateOf(settings.dryingOffReminderDays.toString()) }

    val isOwner = userSession?.role?.equals("OWNER", ignoreCase = true) ?: true

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = null,
                            tint = ForestGreenPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Account & Settings",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // User Session Profile Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(38.dp),
                                    shape = CircleShape,
                                    color = if (isOwner) Color(0xFFDCFCE7) else Color(0xFFE0F2FE)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Filled.Person,
                                            contentDescription = null,
                                            tint = if (isOwner) ForestGreenPrimary else Color(0xFF0284C7),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = userSession?.name ?: "David Kimani",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF1E293B)
                                    )
                                    Text(
                                        text = userSession?.emailOrPhone ?: "owner@mkulima.farm",
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
                                    text = if (isOwner) "FARM OWNER" else "FIELD WORKER",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOwner) ForestGreenDark else Color(0xFF0369A1),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Farm ID Badge
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Key, contentDescription = null, tint = ForestGreenPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("FARM ID", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                    Text(userSession?.farmId ?: "FARM-DEFAULT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                }
                            }

                            TextButton(
                                onClick = {
                                    val farmCode = userSession?.farmId ?: "FARM-DEFAULT"
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Farm ID", farmCode))
                                    Toast.makeText(context, "Farm ID copied to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = "Copy Farm ID", modifier = Modifier.size(14.dp), tint = ForestGreenPrimary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy ID", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                            }
                        }

                        // Owner actions: Manage Workers
                        if (isOwner && onOpenWorkerManagement != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    onDismiss()
                                    onOpenWorkerManagement()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_manage_workers_settings"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                            ) {
                                Icon(Icons.Filled.Group, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Manage Farm Workers & Access", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color(0xFFE2E8F0))
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    "Farm Enterprise Mode",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF475569)
                )
                Spacer(modifier = Modifier.height(6.dp))

                listOf("Both", "Cattle Only", "Poultry Only").forEach { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = isOwner) { selectedFarmType = type }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedFarmType == type,
                            onClick = { if (isOwner) selectedFarmType = type },
                            colors = RadioButtonDefaults.colors(selectedColor = ForestGreenPrimary),
                            enabled = isOwner
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(type, fontSize = 14.sp, color = Color(0xFF1E293B))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = currency,
                    onValueChange = { currency = it },
                    label = { Text("Currency Symbol / Code (e.g. KES, USD)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    enabled = isOwner
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = pdDays,
                    onValueChange = { pdDays = it },
                    label = { Text("PD Pregnancy Check Alert (Days post-AI)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    enabled = isOwner
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = dryOffDays,
                    onValueChange = { dryOffDays = it },
                    label = { Text("Dry-Off Alert Target (Days before calving)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    enabled = isOwner
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = weaningDays,
                    onValueChange = { weaningDays = it },
                    label = { Text("Calf Weaning Target (Days)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    enabled = isOwner
                )

                Spacer(modifier = Modifier.height(18.dp))

                if (isOwner) {
                    Button(
                        onClick = {
                            val updated = settings.copy(
                                farmType = selectedFarmType,
                                currency = currency.ifBlank { "KES" },
                                pregnancyCheckReminderDays = pdDays.toIntOrNull() ?: 30,
                                dryingOffReminderDays = dryOffDays.toIntOrNull() ?: 60,
                                weaningReminderDays = weaningDays.toIntOrNull() ?: 180
                            )
                            onSaveSettings(updated)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                    ) {
                        Text("Save Configuration", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Logout Button
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onLogout()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_logout"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626))
                ) {
                    Icon(Icons.Filled.Logout, contentDescription = "Logout", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out / Switch Account", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

