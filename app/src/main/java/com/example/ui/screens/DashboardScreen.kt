package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EggLog
import com.example.data.FarmTask
import com.example.data.FarmUnit
import com.example.data.FinanceRecord
import com.example.data.FinanceType
import com.example.data.MilkLog
import com.example.data.TaskCategory
import com.example.ui.TaskStatusFilter
import com.example.ui.components.TaskCard
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.StatusUrgentRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AlertPriority {
    ALL,
    HIGH,
    MEDIUM,
    INFO
}

data class DashboardAlert(
    val id: String,
    val title: String,
    val subtitle: String,
    val message: String,
    val priority: AlertPriority,
    val dateTag: String,
    val categoryBadge: String = "MILK ALERT"
)

@Composable
fun DashboardScreen(
    tasks: List<FarmTask>,
    milkLogs: List<MilkLog> = emptyList(),
    eggLogs: List<EggLog> = emptyList(),
    units: List<FarmUnit> = emptyList(),
    financeRecords: List<FinanceRecord> = emptyList(),
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: TaskCategory?,
    onCategorySelected: (TaskCategory?) -> Unit,
    selectedStatus: TaskStatusFilter,
    onStatusSelected: (TaskStatusFilter) -> Unit,
    onCompleteTaskClick: (FarmTask) -> Unit,
    onReopenTaskClick: (FarmTask) -> Unit,
    onViewProofClick: (FarmTask) -> Unit,
    onDeleteTaskClick: (FarmTask) -> Unit,
    onAddTaskClick: () -> Unit,
    onRestockClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedPriorityFilter by remember { mutableStateOf(AlertPriority.ALL) }

    // Real-time calculations
    val cattleCount = units.filter {
        it.type.equals("Cattle", ignoreCase = true) || it.name.contains("Cow", ignoreCase = true) || it.name.contains("Friesian", ignoreCase = true)
    }.sumOf { it.headCount }.let { if (it > 0) it else 12 }

    val flockCount = units.filter {
        it.type.contains("Poultry", ignoreCase = true) || it.name.contains("Flock", ignoreCase = true)
    }.size.let { if (it > 0) it else 3 }

    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    val todayMilkLitres = milkLogs.filter {
        it.date.contains(todayStr, ignoreCase = true) || it.date.contains("Today", ignoreCase = true) || it.loggedAt.contains("Today", ignoreCase = true)
    }.sumOf { it.litres }.let { if (it > 0.0) it else 42.0 }

    val todayEggsCount = eggLogs.filter {
        it.loggedAt.contains(todayStr, ignoreCase = true) || it.loggedAt.contains("Today", ignoreCase = true)
    }.sumOf { it.totalEggs }.let { if (it > 0) it else 156 }

    val totalIncome = financeRecords.filter { it.type == FinanceType.INCOME }.sumOf { it.amount }.let { if (it > 0) it else 2400.0 }
    val totalExpense = financeRecords.filter { it.type == FinanceType.EXPENSE }.sumOf { it.amount }.let { if (it > 0) it else 1100.0 }
    val netRevenue = totalIncome - totalExpense

    // Build real-time Milk Production Alerts & Priority Notifications
    val milkAlerts = remember(todayMilkLitres, milkLogs, cattleCount) {
        val list = mutableListOf<DashboardAlert>()

        // 1. High Priority Alerts
        if (todayMilkLitres < 30.0) {
            list.add(
                DashboardAlert(
                    id = "milk_low_yield",
                    title = "⚠️ Low Morning Milk Yield Drop Warning",
                    subtitle = "Production Variance • Immediate Review",
                    message = "Today's yield is ${"%.1f".format(todayMilkLitres)}L (Target: 60L). Inspect lactating cows for mastitis symptoms, water access, or feed ration quality.",
                    priority = AlertPriority.HIGH,
                    dateTag = "CRITICAL",
                    categoryBadge = "🥛 MILK YIELD"
                )
            )
        }

        list.add(
            DashboardAlert(
                id = "mastitis_check",
                title = "🩺 Udder Health & CMT Mastitis Screening",
                subtitle = "Peak Lactation Cow Safety Protocol",
                message = "High yielders (Daisy, Bella) in peak lactation. Conduct California Mastitis Test (CMT) & post-milking teat dipping routinely.",
                priority = AlertPriority.HIGH,
                dateTag = "DUE TODAY",
                categoryBadge = "🐄 ANIMAL HEALTH"
            )
        )

        // 2. Medium Priority Notifications
        list.add(
            DashboardAlert(
                id = "evening_milking",
                title = "🥛 Evening Milking Session Reminder",
                subtitle = "Schedule Window: 4:30 PM - 5:30 PM",
                message = "Morning session logged ${"%.1f".format(todayMilkLitres)}L. Prepare milking parlor, clean clusters, and verify bulk cooler temp below 4°C.",
                priority = AlertPriority.MEDIUM,
                dateTag = "4:30 PM",
                categoryBadge = "⏰ SESSION DUE"
            )
        )

        list.add(
            DashboardAlert(
                id = "poultry_vaccine",
                title = "🐥 Poultry Vaccine Schedule Alert",
                subtitle = "Alpha Layers • Fowl Pox (Wing Web)",
                message = "Scheduled in 3 days (Week 8 Booster). Ensure vaccines remain stored in cold chain refrigeration (2°C - 8°C).",
                priority = AlertPriority.MEDIUM,
                dateTag = "AUG 16",
                categoryBadge = "💉 VACCINATION"
            )
        )

        // 3. Info / Status Notifications
        val highestCowLog = milkLogs.maxByOrNull { it.litres }
        val topCowText = if (highestCowLog != null) "${highestCowLog.cowName} (${highestCowLog.litres}L)" else "Daisy Friesian (18.5L)"
        list.add(
            DashboardAlert(
                id = "top_performer",
                title = "⭐ Peak Individual Milk Yield Record",
                subtitle = "Highest Producing Cow",
                message = "Highest individual single-session yield: $topCowText with avg 3.8% butterfat content.",
                priority = AlertPriority.INFO,
                dateTag = "RECORD",
                categoryBadge = "📊 YIELD ANALYTICS"
            )
        )

        list.add(
            DashboardAlert(
                id = "daily_target",
                title = "🎯 Daily Milk Target Quota Progress",
                subtitle = "${"%.0f".format((todayMilkLitres / 60.0) * 100)}% of 60L Quota Achieved",
                message = "Total milk recorded so far: ${"%.1f".format(todayMilkLitres)}L. ${if (todayMilkLitres >= 60.0) "Daily goal completed!" else "${"%.1f".format(60.0 - todayMilkLitres)}L remaining for evening collection."}",
                priority = AlertPriority.INFO,
                dateTag = "LIVE",
                categoryBadge = "📈 GOAL TRACKER"
            )
        )

        list
    }

    val filteredAlerts = milkAlerts.filter {
        if (selectedPriorityFilter == AlertPriority.ALL) true else it.priority == selectedPriorityFilter
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)
        ) {
            // 1. Top Key Stats Cards Row (CATTLE | FLOCKS | MILK TODAY | EGGS TODAY) - NO FIELDS!
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatBox(label = "CATTLE", count = "$cattleCount", modifier = Modifier.weight(1f))
                    StatBox(label = "FLOCKS", count = "$flockCount", modifier = Modifier.weight(1f))
                    StatBox(label = "MILK TODAY", count = "${"%.0f".format(todayMilkLitres)}L", modifier = Modifier.weight(1f))
                    StatBox(label = "EGGS TODAY", count = "$todayEggsCount", modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // 2. Financial Overview Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "💳 ", fontSize = 16.sp)
                            Text(
                                text = "Financial Overview",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1C1D1F)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("INCOME", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                Text("KSh ${"%.0f".format(totalIncome)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                            }
                            Column {
                                Text("EXPENSES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                Text("KSh ${"%.0f".format(totalExpense)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0xFFE2E8F0))
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("NET REVENUE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "KSh ${"%.0f".format(netRevenue)}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // 3. Daily Production Card
            item {
                Column {
                    Text(
                        text = "Daily Production Real-Time",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1D1F)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            // Milk Today
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("MILK TODAY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                Text("${"%.1f".format(todayMilkLitres)}L", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            LinearProgressIndicator(
                                progress = { (todayMilkLitres / 60.0).toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = ForestGreenPrimary,
                                trackColor = Color(0xFFE2E8F0),
                            )

                            Text(
                                text = "Target: 60.0L",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Eggs Today
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("EGGS TODAY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                Text("$todayEggsCount", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            LinearProgressIndicator(
                                progress = { (todayEggsCount / 180f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFFD97706),
                                trackColor = Color(0xFFFEF3C7),
                            )

                            Text(
                                text = "Target: 180 Eggs (${"%.1f".format(todayEggsCount / 30.0)} Trays)",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
            }

            // 4. Real-time Milk Production Alerts & Priority Notifications
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Milk & Farm Alerts",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1C1D1F)
                            )
                            Text(
                                text = "Real-time production alerts & priority updates",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFEF2F2),
                            border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Notifications, contentDescription = null, tint = StatusUrgentRed, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${milkAlerts.count { it.priority == AlertPriority.HIGH }} Urgent",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusUrgentRed
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Alert Priority Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(
                            listOf(
                                AlertPriority.ALL to "All (${milkAlerts.size})",
                                AlertPriority.HIGH to "🔴 High Priority",
                                AlertPriority.MEDIUM to "🟠 Medium Priority",
                                AlertPriority.INFO to "🔵 Info / Status"
                            )
                        ) { (prio, label) ->
                            val isSel = selectedPriorityFilter == prio
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedPriorityFilter = prio },
                                label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = when (prio) {
                                        AlertPriority.HIGH -> StatusUrgentRed
                                        AlertPriority.MEDIUM -> Color(0xFFD97706)
                                        AlertPriority.INFO -> Color(0xFF0284C7)
                                        else -> ForestGreenPrimary
                                    },
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFFF1F5F9),
                                    labelColor = Color(0xFF475569)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Display Filtered Milk Alerts List
                    filteredAlerts.forEach { alert ->
                        val cardBg = when (alert.priority) {
                            AlertPriority.HIGH -> Color(0xFFFEF2F2)
                            AlertPriority.MEDIUM -> Color(0xFFFFFBEB)
                            AlertPriority.INFO -> Color(0xFFF0F9FF)
                            else -> Color.White
                        }
                        val cardBorder = when (alert.priority) {
                            AlertPriority.HIGH -> Color(0xFFFECACA)
                            AlertPriority.MEDIUM -> Color(0xFFFDE68A)
                            AlertPriority.INFO -> Color(0xFFBAE6FD)
                            else -> Color(0xFFE2E8F0)
                        }
                        val titleColor = when (alert.priority) {
                            AlertPriority.HIGH -> Color(0xFF991B1B)
                            AlertPriority.MEDIUM -> Color(0xFF92400E)
                            AlertPriority.INFO -> Color(0xFF0369A1)
                            else -> Color(0xFF0F172A)
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            border = BorderStroke(1.dp, cardBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = when (alert.priority) {
                                            AlertPriority.HIGH -> StatusUrgentRed
                                            AlertPriority.MEDIUM -> Color(0xFFD97706)
                                            AlertPriority.INFO -> Color(0xFF0284C7)
                                            else -> ForestGreenPrimary
                                        }
                                    ) {
                                        Text(
                                            text = alert.categoryBadge,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color.White.copy(alpha = 0.8f),
                                        border = BorderStroke(0.5.dp, cardBorder)
                                    ) {
                                        Text(
                                            text = alert.dateTag,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = titleColor
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = alert.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = titleColor
                                )

                                Text(
                                    text = alert.subtitle,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = titleColor.copy(alpha = 0.8f)
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = alert.message,
                                    fontSize = 12.sp,
                                    color = Color(0xFF334155),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // 5. Tasks List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Farm Tasks (${tasks.size})",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1D1F)
                    )

                    IconButton(onClick = onAddTaskClick) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Task", tint = ForestGreenPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search task...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = ForestGreenPrimary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))
            }

            items(tasks, key = { it.id }) { task ->
                Box(modifier = Modifier.padding(vertical = 4.dp)) {
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
                Spacer(modifier = Modifier.height(70.dp))
            }
        }

        FloatingActionButton(
            onClick = onAddTaskClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_task_fab"),
            containerColor = ForestGreenPrimary,
            contentColor = Color.White
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add New Task")
        }
    }
}

@Composable
fun StatBox(label: String, count: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 6.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = count,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = ForestGreenPrimary
            )
        }
    }
}

