package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.FarmUnit
import com.example.data.TaskCategory
import com.example.data.TaskPriority
import com.example.ui.theme.FarmGreenPrimary
import com.example.ui.theme.ForestGreenPrimary
import com.example.util.TaskChecklistItem
import com.example.util.TaskRecurrenceInterval
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    availableUnits: List<FarmUnit>,
    onDismiss: () -> Unit,
    initialCategory: TaskCategory? = null,
    initialTargetUnit: String? = null,
    onTaskCreated: (
        title: String,
        category: TaskCategory,
        targetUnit: String,
        priority: TaskPriority,
        scheduledTime: String,
        instructions: String,
        assignedWorker: String,
        isRecurring: Boolean,
        recurrenceInterval: String,
        checklistItems: List<TaskChecklistItem>
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(initialCategory ?: TaskCategory.LIVESTOCK) }
    var targetUnit by remember {
        mutableStateOf(
            initialTargetUnit?.ifBlank { null }
                ?: availableUnits.firstOrNull()?.name
                ?: ""
        )
    }
    var priority by remember { mutableStateOf(TaskPriority.HIGH) }
    var scheduledDate by remember {
        mutableStateOf(SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()))
    }
    var scheduledTimeText by remember { mutableStateOf("09:00 AM") }
    var instructions by remember { mutableStateOf("") }
    var assignedWorker by remember { mutableStateOf("Lead Farm Hand") }

    // Recurring & Checklist state
    var isRecurring by remember { mutableStateOf(false) }
    var recurrenceInterval by remember { mutableStateOf("7days") }
    var checklistItems by remember { mutableStateOf<List<TaskChecklistItem>>(emptyList()) }
    var newChecklistText by remember { mutableStateOf("") }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var priorityDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("add_task_dialog"),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.PostAdd,
                            contentDescription = null,
                            tint = FarmGreenPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "New Farm Task",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = FarmGreenPrimary
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_add_task_dialog")
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable content body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Quick Activity Presets for Crops & Farm Management
                    Text(
                        text = "Quick Task Templates",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF64748B)
                    )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val cropPresets = listOf(
                        Triple("➕ Add Task", TaskCategory.GENERAL, "General farm operation task"),
                        Triple("🌾 Harvesting", TaskCategory.CROPS, "Crop harvest & collection"),
                        Triple("🌿 Weeding", TaskCategory.CROPS, "Field weeding & clearing"),
                        Triple("🧪 Fertilizer", TaskCategory.CROPS, "Top dressing / fertilizer application"),
                        Triple("🐄 Health Check", TaskCategory.LIVESTOCK, "Livestock health inspection & treatment")
                    )
                    items(cropPresets) { (presetTitle, presetCat, defaultNotes) ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedCategory == presetCat && title == presetTitle.substring(2).trim()) ForestGreenPrimary else Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            modifier = Modifier.clickable {
                                title = presetTitle.substring(2).trim()
                                selectedCategory = presetCat
                                if (instructions.isBlank()) { instructions = defaultNotes }
                            }
                        ) {
                            Text(
                                text = presetTitle,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedCategory == presetCat && title == presetTitle.substring(2).trim()) Color.White else Color(0xFF334155),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Task Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    placeholder = { Text("e.g. Vaccinate Flock B or Irrigate Plot 2") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_task_title_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category & Priority Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Category Dropdown
                    ExposedDropdownMenuBox(
                        expanded = categoryDropdownExpanded,
                        onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedCategory.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("category_dropdown"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false }
                        ) {
                            TaskCategory.entries.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.name) },
                                    onClick = {
                                        selectedCategory = cat
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Priority Dropdown
                    ExposedDropdownMenuBox(
                        expanded = priorityDropdownExpanded,
                        onExpandedChange = { priorityDropdownExpanded = !priorityDropdownExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = priority.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Priority") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("priority_dropdown"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = priorityDropdownExpanded,
                            onDismissRequest = { priorityDropdownExpanded = false }
                        ) {
                            TaskPriority.entries.forEach { prio ->
                                DropdownMenuItem(
                                    text = { Text(prio.name) },
                                    onClick = {
                                        priority = prio
                                        priorityDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Target Unit Manual Input (Direct typing, no forced unit filter dropdown)
                OutlinedTextField(
                    value = targetUnit,
                    onValueChange = { targetUnit = it },
                    label = { Text("Target Unit / Location") },
                    placeholder = { Text("e.g. Flock B, Plot 1, Pen 3, Barn A") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("target_unit_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Scheduled Date & Time Row with AppDatePickerField
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AppDatePickerField(
                        value = scheduledDate,
                        onValueChange = { scheduledDate = it },
                        label = "Scheduled Date",
                        modifier = Modifier.weight(1.2f),
                        testTag = "task_scheduled_date_picker"
                    )

                    OutlinedTextField(
                        value = scheduledTimeText,
                        onValueChange = { scheduledTimeText = it },
                        label = { Text("Time") },
                        placeholder = { Text("09:00 AM") },
                        modifier = Modifier
                            .weight(0.8f)
                            .testTag("scheduled_time_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Recurring Task Configuration Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = if (isRecurring) Color(0xFFF0FDF4) else Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, if (isRecurring) ForestGreenPrimary else Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Repeat,
                                    contentDescription = null,
                                    tint = if (isRecurring) ForestGreenPrimary else Color(0xFF64748B)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Recurring Task",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isRecurring) ForestGreenPrimary else Color(0xFF1E293B)
                                    )
                                    Text(
                                        text = "Automatically reschedule when completed",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                            Switch(
                                checked = isRecurring,
                                onCheckedChange = { isRecurring = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = ForestGreenPrimary
                                ),
                                modifier = Modifier.testTag("recurring_task_switch")
                            )
                        }

                        if (isRecurring) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Select Recurrence Interval:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF334155)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val intervals = listOf(
                                TaskRecurrenceInterval.ONE_DAY,
                                TaskRecurrenceInterval.SEVEN_DAYS,
                                TaskRecurrenceInterval.THIRTY_DAYS,
                                TaskRecurrenceInterval.THREE_MONTHS,
                                TaskRecurrenceInterval.SIX_MONTHS,
                                TaskRecurrenceInterval.ONE_YEAR
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    intervals.take(3).forEach { interval ->
                                        val isSelected = recurrenceInterval == interval.code
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { recurrenceInterval = interval.code }
                                                .testTag("interval_chip_${interval.code}"),
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) ForestGreenPrimary else Color.White,
                                            border = BorderStroke(1.dp, if (isSelected) ForestGreenPrimary else Color(0xFFCBD5E1))
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = interval.label,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) Color.White else Color(0xFF1E293B)
                                                )
                                                Text(
                                                    text = interval.shortLabel,
                                                    fontSize = 10.sp,
                                                    color = if (isSelected) Color(0xFFDCFCE7) else Color(0xFF64748B)
                                                )
                                            }
                                        }
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    intervals.drop(3).forEach { interval ->
                                        val isSelected = recurrenceInterval == interval.code
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { recurrenceInterval = interval.code }
                                                .testTag("interval_chip_${interval.code}"),
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) ForestGreenPrimary else Color.White,
                                            border = BorderStroke(1.dp, if (isSelected) ForestGreenPrimary else Color(0xFFCBD5E1))
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = interval.label,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) Color.White else Color(0xFF1E293B)
                                                )
                                                Text(
                                                    text = interval.shortLabel,
                                                    fontSize = 10.sp,
                                                    color = if (isSelected) Color(0xFFDCFCE7) else Color(0xFF64748B)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Task Checklist / Subtasks Section
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Checklist,
                                    contentDescription = null,
                                    tint = FarmGreenPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Task Checklist Steps",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                    Text(
                                        text = if (checklistItems.isEmpty()) "Add step-by-step checklist items" else "${checklistItems.size} items added",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Checklist Input Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newChecklistText,
                                onValueChange = { newChecklistText = it },
                                placeholder = { Text("e.g. Inspect water nipples", fontSize = 12.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("checklist_item_input"),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true,
                                trailingIcon = {
                                    if (newChecklistText.isNotBlank()) {
                                        IconButton(onClick = { newChecklistText = "" }) {
                                            Icon(Icons.Filled.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            )

                            Button(
                                onClick = {
                                    if (newChecklistText.isNotBlank()) {
                                        checklistItems = checklistItems + TaskChecklistItem(text = newChecklistText.trim())
                                        newChecklistText = ""
                                    }
                                },
                                enabled = newChecklistText.isNotBlank(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FarmGreenPrimary),
                                modifier = Modifier.testTag("add_checklist_item_button")
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "Add Step", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add", fontSize = 12.sp)
                            }
                        }

                        // Quick checklist templates
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Checklist Templates:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val templates = listOf(
                                "Daily Feeding" to listOf("Inspect feed trough", "Refill fresh feed", "Check clean water supply"),
                                "Vaccination" to listOf("Sanitize needles & syringes", "Check expiry & dosage", "Administer dose", "Record ear tag number"),
                                "Barn Maintenance" to listOf("Clear manure & soiled bedding", "Disinfect floors", "Verify ventilation fans")
                            )
                            items(templates) { (templateTitle, steps) ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFE2E8F0),
                                    modifier = Modifier.clickable {
                                        val newItems = steps.map { TaskChecklistItem(text = it) }
                                        checklistItems = checklistItems + newItems
                                    }
                                ) {
                                    Text(
                                        text = "+ $templateTitle",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF334155),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        // Render added items list
                        if (checklistItems.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                checklistItems.forEachIndexed { index, item ->
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color.White,
                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(
                                                    text = "${index + 1}.",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = FarmGreenPrimary
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = item.text,
                                                    fontSize = 13.sp,
                                                    color = Color(0xFF1E293B)
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    checklistItems = checklistItems.filterNot { it.id == item.id }
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    Icons.Filled.DeleteOutline,
                                                    contentDescription = "Remove",
                                                    tint = Color(0xFFEF4444),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Assigned Worker
                OutlinedTextField(
                    value = assignedWorker,
                    onValueChange = { assignedWorker = it },
                    label = { Text("Assigned Farm Operator") },
                    placeholder = { Text("e.g. Lead Farm Hand") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Detailed Instructions
                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("Instructions & Dosage Notes") },
                    placeholder = { Text("e.g. Mix 100g powder per 200L water tank. Check water flow.") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))
            } // End of scrollable content Column

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons (fixed at dialog bottom)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onTaskCreated(
                                title,
                                selectedCategory,
                                targetUnit,
                                priority,
                                "$scheduledDate at $scheduledTimeText",
                                instructions,
                                assignedWorker,
                                isRecurring,
                                recurrenceInterval,
                                checklistItems
                            )
                        }
                    },
                    modifier = Modifier.testTag("save_task_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                ) {
                    Text("Create Task", fontWeight = FontWeight.Bold)
                }
            }
            }
        }
    }
}
