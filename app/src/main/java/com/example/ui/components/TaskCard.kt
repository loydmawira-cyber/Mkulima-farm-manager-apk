package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.CachePolicy
import com.example.R
import com.example.data.FarmTask
import com.example.data.TaskCategory
import com.example.data.TaskPriority
import com.example.ui.theme.FarmGreenLight
import com.example.ui.theme.FarmGreenPrimary
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.HarvestAmber
import com.example.ui.theme.HarvestAmberLight
import com.example.ui.theme.StatusCompleted
import com.example.ui.theme.StatusUrgent
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// task.scheduledTime is stored as free-form text depending on how the task was created
// (FarmViewModel's own parser accepts several formats), so this mirrors that same
// format list. This file is in a different package from FarmViewModel and can't reach
// its private parseFarmDate, hence a local copy here.
private fun parseTaskScheduledDate(rawValue: String): java.util.Date? {
    val raw = rawValue.trim()
    if (raw.isBlank()) return null
    return listOf("dd MMM yyyy", "dd MMM, yyyy", "yyyy-MM-dd", "dd/MM/yyyy")
        .firstNotNullOfOrNull { pattern ->
            runCatching {
                SimpleDateFormat(pattern, Locale.getDefault()).apply { isLenient = false }.parse(raw)
            }.getOrNull()
        }
}

// True only for tasks due strictly after tomorrow — today's and tomorrow's tasks stay
// actionable. Compared at day granularity (time-of-day on scheduledTime is ignored).
// If scheduledTime can't be parsed at all, this fails open (returns false / stays
// actionable) rather than silently locking a task whose date just didn't match one of
// the known formats.
private fun isTaskLockedForFutureDueDate(task: FarmTask): Boolean {
    val scheduled = parseTaskScheduledDate(task.scheduledTime) ?: return false

    fun startOfDay(date: java.util.Date): java.util.Date =
        Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

    val tomorrowStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        add(Calendar.DAY_OF_YEAR, 1)
    }.time

    return startOfDay(scheduled).after(tomorrowStart)
}

@Composable
fun TaskCard(
    task: FarmTask,
    onCompleteClick: (FarmTask) -> Unit,
    onReopenClick: (FarmTask) -> Unit,
    onViewProofClick: (FarmTask) -> Unit,
    onDeleteClick: (FarmTask) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    var taskMenuExpanded by remember { mutableStateOf(false) }

    // Tasks due after tomorrow can't be completed or deleted — only viewed. Today's
    // and tomorrow's tasks remain fully actionable.
    val isLockedForFutureDueDate = remember(task.scheduledTime) { isTaskLockedForFutureDueDate(task) }

    val categoryColor = when (task.category) {
        TaskCategory.LIVESTOCK -> Color(0xFF0284C7)
        TaskCategory.CROPS -> ForestGreenPrimary
        TaskCategory.EQUIPMENT -> Color(0xFFD97706)
        TaskCategory.GENERAL -> Color(0xFF475569)
    }

    val categoryIcon = when (task.category) {
        TaskCategory.LIVESTOCK -> Icons.Filled.Pets
        TaskCategory.CROPS -> Icons.Filled.Agriculture
        TaskCategory.EQUIPMENT -> Icons.Filled.Egg
        TaskCategory.GENERAL -> Icons.Filled.Agriculture
    }

    val priorityColor = when (task.priority) {
        TaskPriority.HIGH -> StatusUrgent
        TaskPriority.MEDIUM -> HarvestAmber
        TaskPriority.LOW -> ForestGreenPrimary
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("task_card_${task.id}")
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) Color(0xFFF8FAFC) else Color.White
        ),
        border = BorderStroke(1.dp, if (task.isCompleted) Color(0xFFE2E8F0) else Color(0xFFCBD5E1)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (task.isCompleted) 0.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Category Badge, Priority Chip, Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = categoryColor.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = categoryIcon,
                                contentDescription = null,
                                tint = categoryColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = task.category.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = categoryColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = priorityColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "${task.priority.name} PRIORITY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = priorityColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                Box {
                    IconButton(
                        onClick = { taskMenuExpanded = true },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("task_menu_${task.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "Task options",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = taskMenuExpanded,
                        onDismissRequest = { taskMenuExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        if (!task.isCompleted) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Complete Task",
                                        fontWeight = FontWeight.Bold,
                                        color = if (isLockedForFutureDueDate) Color(0xFFA1A1AA) else ForestGreenPrimary
                                    )
                                },
                                enabled = !isLockedForFutureDueDate,
                                onClick = {
                                    taskMenuExpanded = false
                                    onCompleteClick(task)
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = if (isLockedForFutureDueDate) Color(0xFFA1A1AA) else ForestGreenPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text("Mark Incomplete", fontWeight = FontWeight.SemiBold, color = Color(0xFF475569)) },
                                onClick = {
                                    taskMenuExpanded = false
                                    onReopenClick(task)
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.CheckCircleOutline,
                                        contentDescription = null,
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }

                        if (task.isCompleted && !task.proofPhotoUri.isNullOrBlank()) {
                            DropdownMenuItem(
                                text = { Text("View Proof", fontWeight = FontWeight.Medium, color = Color(0xFF334155)) },
                                onClick = {
                                    taskMenuExpanded = false
                                    onViewProofClick(task)
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Image,
                                        contentDescription = null,
                                        tint = Color(0xFF334155),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }

                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Delete Task",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLockedForFutureDueDate) Color(0xFFA1A1AA) else Color(0xFFDC2626)
                                )
                            },
                            enabled = !isLockedForFutureDueDate,
                            onClick = {
                                taskMenuExpanded = false
                                onDeleteClick(task)
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = null,
                                    tint = if (isLockedForFutureDueDate) Color(0xFFA1A1AA) else Color(0xFFDC2626),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Task Title & Target Unit
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (task.isCompleted) Color(0xFF64748B) else Color(0xFF1E293B),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Unit: ${task.targetUnit}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ForestGreenPrimary
                    )
                }

                // Status Indicator Button
                if (task.isCompleted) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFDCFCE7),
                        modifier = Modifier
                            .testTag("completed_badge_${task.id}")
                            .clickable { onViewProofClick(task) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Completed",
                                tint = ForestGreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "DONE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreenPrimary
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = { onCompleteClick(task) },
                        enabled = !isLockedForFutureDueDate,
                        modifier = Modifier.testTag("complete_button_${task.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ForestGreenPrimary,
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFFE2E8F0),
                            disabledContentColor = Color(0xFF94A3B8)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isLockedForFutureDueDate) "Not yet due" else "Complete",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Time & Assigned Worker Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (task.isCompleted) "Done: ${task.completedAt ?: "Recorded"}" else "Due: ${task.scheduledTime}",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = "Assigned: ${task.assignedWorker ?: "Farm Hand"}",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )
            }

            // Thumbnail Preview if task has proof photo
            if (task.isCompleted && !task.proofPhotoUri.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F5F9))
                        .clickable { onViewProofClick(task) }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(task.proofPhotoUri)
                            .crossfade(true)
                            .placeholder(R.drawable.ic_livestock_placeholder)
                            .error(R.drawable.ic_livestock_placeholder)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .build(),
                        contentDescription = "Task Proof Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Proof Attached",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenPrimary
                        )
                        Text(
                            text = task.proofNotes ?: "Click to view full photo proof",
                            fontSize = 11.sp,
                            color = Color(0xFF475569),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = "View",
                        tint = ForestGreenPrimary
                    )
                }
            }

            // Expanded Instructions Details
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .background(Color(0xFFF8FAFC), shape = RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Instructions:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = task.instructions ?: "No detailed instructions provided.",
                        fontSize = 12.sp,
                        color = Color(0xFF475569)
                    )

                    if (task.isCompleted) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { onReopenClick(task) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reopen_button_${task.id}"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Mark Incomplete / Reopen", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
