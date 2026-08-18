package com.example.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotInterested
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.screens.AnimalDetailData
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.TagLivestockBg
import com.example.ui.theme.TagLivestockText
import com.example.ui.theme.TagYieldBg
import com.example.ui.theme.TagYieldText

@Composable
fun AnimalOptionsDialog(
    animal: AnimalDetailData,
    userRole: String,
    onDismiss: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDisposeClick: () -> Unit,
    onViewDetailsClick: () -> Unit
) {
    val isPoultry = animal.category.equals("POULTRY", ignoreCase = true) ||
            animal.breed.contains("Layer", ignoreCase = true) ||
            animal.breed.contains("Flock", ignoreCase = true)
    
    val canEdit = userRole == "OWNER"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("animal_options_dialog"),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Animal Actions",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Animal Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = ForestGreenPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isPoultry) Icons.Filled.Egg else Icons.Filled.Pets,
                                    contentDescription = null,
                                    tint = ForestGreenPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = animal.name,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = "${animal.breed} • ${animal.tagNumber}",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (animal.status == "MILKING" || animal.status == "ACTIVE" || animal.status == "Active Laying") TagLivestockBg else TagYieldBg
                        ) {
                            Text(
                                text = animal.status,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (animal.status == "MILKING" || animal.status == "ACTIVE" || animal.status == "Active Laying") TagLivestockText else TagYieldText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action 1: Modify / Edit
                if (canEdit) {
                    AnimalActionOptionItem(
                        icon = Icons.Filled.Edit,
                        iconBg = Color(0xFFECFDF5),
                        iconTint = ForestGreenPrimary,
                        title = "Modify / Edit Animal Data",
                        subtitle = "Correct name, tag, breed, weight, parentage or stage info",
                        testTag = "option_modify_animal",
                        onClick = {
                            onDismiss()
                            onEditClick()
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Action 2: Delete Completely
                    AnimalActionOptionItem(
                        icon = Icons.Filled.DeleteForever,
                        iconBg = Color(0xFFFEF2F2),
                        iconTint = Color(0xFFDC2626),
                        title = "Delete Completely",
                        subtitle = "Permanently remove this animal from database & records",
                        testTag = "option_delete_animal_completely",
                        onClick = {
                            onDismiss()
                            onDeleteClick()
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Action 3: Record Disposal / Sale
                if (canEdit) {
                    AnimalActionOptionItem(
                        icon = Icons.Filled.NotInterested,
                        iconBg = Color(0xFFFFFBEB),
                        iconTint = Color(0xFFD97706),
                        title = if (isPoultry) "Dispose / Cull / Sell Flock" else "Record Disposal / Sale",
                        subtitle = "Mark as sold, culled, or deceased (keeps audit history)",
                        testTag = "option_dispose_animal",
                        onClick = {
                            onDismiss()
                            onDisposeClick()
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Action 4: View Details
                AnimalActionOptionItem(
                    icon = Icons.Filled.Visibility,
                    iconBg = Color(0xFFF1F5F9),
                    iconTint = Color(0xFF475569),
                    title = "Open Full Profile",
                    subtitle = "View events, yield charts, pedigree and health history",
                    testTag = "option_view_animal_profile",
                    onClick = {
                        onDismiss()
                        onViewDetailsClick()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close", color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun AnimalActionOptionItem(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = iconBg,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}

@Composable
fun DeleteAnimalConfirmDialog(
    animal: AnimalDetailData,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    val isPoultry = animal.category.equals("POULTRY", ignoreCase = true) ||
            animal.breed.contains("Layer", ignoreCase = true) ||
            animal.breed.contains("Flock", ignoreCase = true)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("delete_animal_confirm_dialog"),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                // Warning Icon Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFEE2E2),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.DeleteForever,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Delete Animal Completely?",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "Permanent Record Deletion",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFDC2626)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    border = BorderStroke(1.dp, Color(0xFFFECACA))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "You are deleting:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF7F1D1D)
                        )
                        Text(
                            text = "${animal.name} (${animal.tagNumber})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF991B1B)
                        )
                        Text(
                            text = "Breed: ${animal.breed} • Category: ${if (isPoultry) "Poultry" else "Cattle"}",
                            fontSize = 12.sp,
                            color = Color(0xFFB91C1C)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "⚠️ Warning: This will permanently remove this animal and all associated unit records from your farm database. This action cannot be undone.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = Color(0xFF475569)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "💡 Tip: If you sold, slaughtered, or lost this animal, use 'Record Disposal' instead to keep audit logs and financial income tracking.",
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", color = Color(0xFF64748B))
                    }

                    Button(
                        onClick = {
                            onConfirmDelete()
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("confirm_delete_animal_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                    ) {
                        Text("🗑️ Delete Completely", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
