package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import com.example.data.RequestStatus
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.TagFinanceBg
import com.example.ui.theme.TagFinanceText
import com.example.ui.theme.TagHRBg
import com.example.ui.theme.TagHRText
import com.example.ui.theme.TagLivestockBg
import com.example.ui.theme.TagLivestockText
import com.example.ui.theme.TagYieldBg
import com.example.ui.theme.TagYieldText

@Composable
fun ApprovalRequestsScreen(
    requests: List<EmployeeRequest>,
    onUpdateRequestStatus: (EmployeeRequest, String) -> Unit,
    currency: String,
    modifier: Modifier = Modifier,
    userRole: String = "OWNER",
    onAddRequestClick: (() -> Unit)? = null
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val isOwner = userRole.equals("OWNER", ignoreCase = true)

    val tabs = if (isOwner) {
        listOf("PENDING (${requests.count { it.status == RequestStatus.PENDING }})", "APPROVED", "REJECTED")
    } else {
        listOf("ALL (${requests.size})", "PENDING", "APPROVED", "REJECTED")
    }

    val filteredRequests = if (isOwner) {
        when (selectedTabIndex) {
            0 -> requests.filter { it.status == RequestStatus.PENDING }
            1 -> requests.filter { it.status == RequestStatus.APPROVED }
            else -> requests.filter { it.status == RequestStatus.REJECTED }
        }
    } else {
        when (selectedTabIndex) {
            0 -> requests
            1 -> requests.filter { it.status == RequestStatus.PENDING }
            2 -> requests.filter { it.status == RequestStatus.APPROVED }
            else -> requests.filter { it.status == RequestStatus.REJECTED }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isOwner) "Approval Requests" else "My Requests",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1D1F)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isOwner) "Review leave and advance requests from workers." else "Track your leave applications and advance requests.",
                    fontSize = 13.sp,
                    color = Color(0xFF5C6470)
                )
            }

            if (onAddRequestClick != null) {
                Button(
                    onClick = onAddRequestClick,
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("btn_submit_request_top")
                ) {
                    Icon(Icons.Filled.PostAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isOwner) "Add Request" else "Apply Leave/Advance", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.Transparent,
            contentColor = ForestGreenPrimary,
            indicator = { tabPositions ->
                if (selectedTabIndex < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = ForestGreenPrimary,
                        height = 3.dp
                    )
                }
            },
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp,
                            color = if (selectedTabIndex == index) ForestGreenPrimary else Color(0xFF5C6470)
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

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
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    Spacer(modifier = Modifier.height(24.dp))
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
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
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
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
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

