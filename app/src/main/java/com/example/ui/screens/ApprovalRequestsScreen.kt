package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EmployeeRequest
import com.example.data.FarmTask
import com.example.data.RequestStatus
import com.example.data.TaskCategory
import com.example.data.TaskPriority
import com.example.ui.TaskStatusFilter
import com.example.ui.components.TaskCard
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.LineColor
import com.example.ui.theme.Sage
import com.example.ui.theme.Soil
import com.example.ui.theme.SoilSoft
import com.example.ui.theme.TagFinanceBg
import com.example.ui.theme.TagFinanceText
import com.example.ui.theme.TagHRBg
import com.example.ui.theme.TagHRText
import com.example.ui.theme.TagLivestockBg
import com.example.ui.theme.TagLivestockText
import com.example.ui.theme.TagYieldBg
import com.example.ui.theme.TagYieldText
import com.example.ui.theme.Terracotta

@Composable
fun ApprovalRequestsScreen(
    tasks: List<FarmTask> = emptyList(),
    requests: List<EmployeeRequest>,
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    selectedCategory: TaskCategory? = null,
    onCategorySelected: (TaskCategory?) -> Unit = {},
    selectedStatus: TaskStatusFilter = TaskStatusFilter.ALL,
    onStatusSelected: (TaskStatusFilter) -> Unit = {},
    onCompleteTaskClick: (FarmTask) -> Unit = {},
    onReopenTaskClick: (FarmTask) -> Unit = {},
    onViewProofClick: (FarmTask) -> Unit = {},
    onDeleteTaskClick: (FarmTask) -> Unit = {},
    onAddTaskClick: () -> Unit = {},
    onUpdateRequestStatus: (EmployeeRequest, String) -> Unit,
    currency: String,
    modifier: Modifier = Modifier,
    userRole: String = "OWNER",
    onAddRequestClick: (() -> Unit)? = null
) {
    var primarySectionIndex by remember { mutableIntStateOf(0) } // 0: Daily Tasks, 1: Requests & Approvals
    var selectedRequestTabIndex by remember { mutableIntStateOf(0) }
    val isOwner = userRole.equals("OWNER", ignoreCase = true)

    val pendingTasksCount = remember(tasks) { tasks.count { !it.isCompleted } }
    val pendingRequestsCount = remember(requests) { requests.count { it.status == RequestStatus.PENDING } }

    val requestTabs = if (isOwner) {
        listOf("PENDING ($pendingRequestsCount)", "APPROVED", "REJECTED")
    } else {
        listOf("ALL (${requests.size})", "PENDING", "APPROVED", "REJECTED")
    }

    val filteredRequests = if (isOwner) {
        when (selectedRequestTabIndex) {
            0 -> requests.filter { it.status == RequestStatus.PENDING }
            1 -> requests.filter { it.status == RequestStatus.APPROVED }
            else -> requests.filter { it.status == RequestStatus.REJECTED }
        }
    } else {
        when (selectedRequestTabIndex) {
            0 -> requests
            1 -> requests.filter { it.status == RequestStatus.PENDING }
            2 -> requests.filter { it.status == RequestStatus.APPROVED }
            else -> requests.filter { it.status == RequestStatus.REJECTED }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Top Main Segment Bar (Daily Tasks vs Worker Requests)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (primarySectionIndex == 0) "Farm Tasks & Operations" else if (isOwner) "Worker Requests" else "My Requests",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = if (primarySectionIndex == 0) "Assign, track and complete daily farm routines." else "Review employee leave and advance requests.",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    if (primarySectionIndex == 0) {
                        Button(
                            onClick = onAddTaskClick,
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_add_task_header")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Task", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else if (onAddRequestClick != null) {
                        Button(
                            onClick = onAddRequestClick,
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_submit_request_top")
                        ) {
                            Icon(Icons.Filled.PostAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isOwner) "Add Request" else "Apply", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Primary Segment Selector
                TabRow(
                    selectedTabIndex = primarySectionIndex,
                    containerColor = Color.White,
                    contentColor = ForestGreenPrimary,
                    indicator = { tabPositions ->
                        if (primarySectionIndex < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[primarySectionIndex]),
                                color = ForestGreenPrimary,
                                height = 3.dp
                            )
                        }
                    }
                ) {
                    Tab(
                        selected = primarySectionIndex == 0,
                        onClick = { primarySectionIndex = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Assignment, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Daily Tasks ($pendingTasksCount)", fontWeight = if (primarySectionIndex == 0) FontWeight.Bold else FontWeight.Medium, fontSize = 13.sp)
                            }
                        },
                        modifier = Modifier.testTag("segment_tab_daily_tasks")
                    )
                    Tab(
                        selected = primarySectionIndex == 1,
                        onClick = { primarySectionIndex = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.FactCheck, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Requests ($pendingRequestsCount)", fontWeight = if (primarySectionIndex == 1) FontWeight.Bold else FontWeight.Medium, fontSize = 13.sp)
                            }
                        },
                        modifier = Modifier.testTag("segment_tab_requests")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (primarySectionIndex == 0) {
            // ================= DAILY TASKS SECTION =================
            val displayedTasks = tasks.filter { task ->
                val matchesStatus = when (selectedStatus) {
                    TaskStatusFilter.PENDING -> !task.isCompleted
                    TaskStatusFilter.COMPLETED -> task.isCompleted
                    TaskStatusFilter.HIGH_PRIORITY -> !task.isCompleted && task.priority == TaskPriority.HIGH
                    TaskStatusFilter.ALL -> true
                    else -> true
                }
                val matchesCategory = selectedCategory == null || task.category == selectedCategory
                val matchesSearch = searchQuery.isBlank() ||
                        task.title.contains(searchQuery, ignoreCase = true) ||
                        task.targetUnit.contains(searchQuery, ignoreCase = true) ||
                        (task.assignedWorker?.contains(searchQuery, ignoreCase = true) == true)
                matchesStatus && matchesCategory && matchesSearch
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Search Field
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tasks_search_input"),
                        placeholder = { Text("Search task name, unit, or worker...", fontSize = 13.sp, color = Color(0xFF64748B)) },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Clear", tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1E293B),
                            unfocusedTextColor = Color(0xFF1E293B),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = ForestGreenPrimary,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )
                }

                // Status Filter Chips: Pending, All, Completed, High Priority
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val pendingActive = selectedStatus == TaskStatusFilter.PENDING
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (pendingActive) ForestGreenPrimary else Color(0xFFF1F5F9),
                            border = if (pendingActive) null else BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onStatusSelected(TaskStatusFilter.PENDING) }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 9.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "⏳ Pending (${tasks.count { !it.isCompleted }})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (pendingActive) Color.White else Color(0xFF334155)
                                )
                            }
                        }

                        val allActive = selectedStatus == TaskStatusFilter.ALL
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (allActive) ForestGreenPrimary else Color(0xFFF1F5F9),
                            border = if (allActive) null else BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onStatusSelected(TaskStatusFilter.ALL) }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 9.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📋 All (${tasks.size})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (allActive) Color.White else Color(0xFF334155)
                                )
                            }
                        }

                        val completedActive = selectedStatus == TaskStatusFilter.COMPLETED
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (completedActive) ForestGreenPrimary else Color(0xFFF1F5F9),
                            border = if (completedActive) null else BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onStatusSelected(TaskStatusFilter.COMPLETED) }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 9.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "✅ Done (${tasks.count { it.isCompleted }})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (completedActive) Color.White else Color(0xFF334155)
                                )
                            }
                        }
                    }
                }

                // Category Chips Row
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            val isAllSel = selectedCategory == null
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isAllSel) ForestGreenPrimary else Color(0xFFF1F5F9),
                                border = if (isAllSel) null else BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                modifier = Modifier.clickable { onCategorySelected(null) }
                            ) {
                                Text(
                                    text = "ALL",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAllSel) Color.White else Color(0xFF334155),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        items(TaskCategory.values()) { category ->
                            val isCatSel = selectedCategory == category
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isCatSel) ForestGreenPrimary else Color(0xFFF1F5F9),
                                border = if (isCatSel) null else BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                modifier = Modifier.clickable { onCategorySelected(if (isCatSel) null else category) }
                            ) {
                                Text(
                                    text = category.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCatSel) Color.White else Color(0xFF334155),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Task List Items
                if (displayedTasks.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🌾", fontSize = 36.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = when (selectedStatus) {
                                        TaskStatusFilter.PENDING -> "No pending tasks 🎉"
                                        TaskStatusFilter.COMPLETED -> "No completed tasks yet"
                                        TaskStatusFilter.HIGH_PRIORITY -> "No high priority tasks"
                                        TaskStatusFilter.ALL -> "No tasks registered"
                                        else -> "No tasks found"
                                    },
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tap '+ New Task' above to schedule farm operations.",
                                    fontSize = 13.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                } else {
                    items(displayedTasks, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            onCompleteClick = onCompleteTaskClick,
                            onReopenClick = onReopenTaskClick,
                            onViewProofClick = onViewProofClick,
                            onDeleteClick = onDeleteTaskClick
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        } else {
            // ================= WORKER REQUESTS & APPROVALS SECTION =================
            TabRow(
                selectedTabIndex = selectedRequestTabIndex,
                containerColor = Color.Transparent,
                contentColor = ForestGreenPrimary,
                indicator = { tabPositions ->
                    if (selectedRequestTabIndex < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedRequestTabIndex]),
                            color = ForestGreenPrimary,
                            height = 3.dp
                        )
                    }
                },
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                requestTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedRequestTabIndex == index,
                        onClick = { selectedRequestTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedRequestTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                color = if (selectedRequestTabIndex == index) ForestGreenPrimary else Color(0xFF5C6470)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredRequests.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isOwner) "No requests in this category." else "No requests submitted yet.",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                        if (onAddRequestClick != null && !isOwner) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onAddRequestClick,
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Submit Leave or Advance Request")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredRequests, key = { it.id }) { req ->
                        ApprovalRequestCard(
                            request = req,
                            currency = currency,
                            isOwner = isOwner,
                            onApprove = { onUpdateRequestStatus(req, "APPROVED") },
                            onReject = { onUpdateRequestStatus(req, "REJECTED") }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(28.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ApprovalRequestCard(
    request: EmployeeRequest,
    currency: String,
    isOwner: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val (tagBg, tagText) = when {
        request.requestType.contains("Yield", ignoreCase = true) -> TagYieldBg to TagYieldText
        request.requestType.contains("Livestock", ignoreCase = true) || request.requestType.contains("Medication", ignoreCase = true) -> TagLivestockBg to TagLivestockText
        request.requestType.contains("Salary", ignoreCase = true) || request.requestType.contains("Advance", ignoreCase = true) -> TagFinanceBg to TagFinanceText
        else -> TagHRBg to TagHRText
    }

    val initials = request.employeeName.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("approval_request_card_${request.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row: Avatar, Name, Date, Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = initials.ifBlank { "Worker" },
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF334155),
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = request.employeeName,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "Submitted: ${request.submittedAt}",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = tagBg
                ) {
                    Text(
                        text = request.requestType.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = tagText
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Details based on request type
            Text(
                text = if (request.amount > 0) "Amount Requested: ${currency} ${"%.2f".format(request.amount)}" else "Leave Period: ${request.startDate} to ${request.endDate}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Note quote block
            if (request.reason.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(36.dp)
                            .background(Color(0xFFCBD5E1), RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "REASON:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = "\"${request.reason}\"",
                            fontSize = 13.sp,
                            fontStyle = FontStyle.Italic,
                            color = Color(0xFF334155)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Action Buttons for Owner OR Status Pill for Worker
            if (request.status == RequestStatus.PENDING) {
                if (isOwner) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onReject,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Text("REJECT", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), fontSize = 13.sp)
                        }

                        Button(
                            onClick = onApprove,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                        ) {
                            Text("APPROVE", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "The worker will see your decision immediately.",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFEF3C7),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "⏳ PENDING ADMIN REVIEW",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            } else {
                val isApproved = request.status == RequestStatus.APPROVED
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isApproved) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isApproved) "✓ APPROVED" else "✕ REJECTED",
                            fontWeight = FontWeight.Bold,
                            color = if (isApproved) ForestGreenDark else Color(0xFF991B1B),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
