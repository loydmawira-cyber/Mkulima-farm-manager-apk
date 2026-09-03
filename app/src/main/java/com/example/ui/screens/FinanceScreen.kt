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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FinanceRecord
import com.example.data.FinanceType
import com.example.data.MonthlyReport
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.theme.ForestGreenPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun FinanceScreen(
    records: List<FinanceRecord>,
    reports: List<MonthlyReport> = emptyList(),
    onAddTransactionClick: () -> Unit,
    onEditTransaction: (FinanceRecord) -> Unit = {},
    onDeleteTransaction: (FinanceRecord) -> Unit = {},
    onOpenReport: (MonthlyReport) -> Unit = {},
    currency: String = "KES",
    canEditFinance: Boolean = true,
    userRole: String = "OWNER",
    canEditPastDaysLogs: Boolean = true,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var recordToDelete by remember { mutableStateOf<FinanceRecord?>(null) }
    var activeMenuRecordId by remember { mutableStateOf<Long?>(null) }

    recordToDelete?.let { record ->
        val isIncome = record.type == FinanceType.INCOME
        ConfirmDeleteDialog(
            title = "Delete Transaction?",
            message = "Are you sure you want to delete this ${if (isIncome) "income" else "expense"} transaction of ${formatMoney(currency, record.amount)} (${record.category})? This will update your financial balances.",
            confirmButtonText = "Delete Transaction",
            confirmButtonColor = Color(0xFFDC2626),
            onConfirm = {
                onDeleteTransaction(record)
                recordToDelete = null
            },
            onDismiss = { recordToDelete = null }
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Finance", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("finance_tab")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Reports", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("reports_tab")
            )
        }

        if (selectedTab == 0) {
            FinanceTab(
                records = records,
                currency = currency,
                activeMenuRecordId = activeMenuRecordId,
                onMenuExpanded = { activeMenuRecordId = it },
                onEditTransaction = onEditTransaction,
                onDeleteTransactionRequested = { recordToDelete = it },
                onAddTransactionClick = onAddTransactionClick,
                canEditFinance = canEditFinance,
                userRole = userRole,
                canEditPastDaysLogs = canEditPastDaysLogs
            )
        } else {
            ReportsTab(reports = reports, currency = currency, onOpenReport = onOpenReport)
        }
    }
}

@Composable
private fun FinanceTab(
    records: List<FinanceRecord>,
    currency: String,
    activeMenuRecordId: Long?,
    onMenuExpanded: (Long?) -> Unit,
    onEditTransaction: (FinanceRecord) -> Unit,
    onDeleteTransactionRequested: (FinanceRecord) -> Unit,
    onAddTransactionClick: () -> Unit,
    canEditFinance: Boolean = true,
    userRole: String = "OWNER",
    canEditPastDaysLogs: Boolean = true
) {
    val currentCal = remember { Calendar.getInstance() }
    val defaultMonthName = remember { SimpleDateFormat("MMMM", Locale.getDefault()).format(currentCal.time) }
    val defaultYearName = remember { SimpleDateFormat("yyyy", Locale.getDefault()).format(currentCal.time) }
    val currentYearNum = remember { currentCal.get(Calendar.YEAR) }
    val todayDateStr = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(currentCal.time) }

    val monthsList = remember {
        listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
    }
    val yearsList = remember { ((currentYearNum - 5)..currentYearNum).map { it.toString() } }

    var financeTimeframe by remember { mutableStateOf("TODAY") } // TODAY, MONTH, YEAR
    var selectedFinanceMonth by remember { mutableStateOf(defaultMonthName) }
    var selectedFinanceYear by remember { mutableStateOf(defaultYearName) }
    var isFinanceMonthMenuExpanded by remember { mutableStateOf(false) }
    var isFinanceYearMenuExpanded by remember { mutableStateOf(false) }

    val targetMonthIdx = monthsList.indexOfFirst { it.equals(selectedFinanceMonth, ignoreCase = true) }
    val targetYearInt = selectedFinanceYear.toIntOrNull() ?: currentYearNum

    val filteredRecords = remember(records, financeTimeframe, selectedFinanceMonth, selectedFinanceYear, targetMonthIdx, targetYearInt) {
        records.filter { record ->
            val cal = parseFinanceCalendar(record.date, record.updatedAt)
            when (financeTimeframe) {
                "TODAY" -> {
                    if (cal != null) {
                        cal.get(Calendar.YEAR) == currentYearNum &&
                                cal.get(Calendar.DAY_OF_YEAR) == currentCal.get(Calendar.DAY_OF_YEAR)
                    } else {
                        record.date.contains("Today", ignoreCase = true) ||
                                record.date.contains(todayDateStr, ignoreCase = true)
                    }
                }
                "MONTH" -> {
                    if (cal != null) {
                        cal.get(Calendar.MONTH) == targetMonthIdx &&
                                cal.get(Calendar.YEAR) == targetYearInt
                    } else {
                        record.date.contains(selectedFinanceMonth, ignoreCase = true) &&
                                record.date.contains(targetYearInt.toString())
                    }
                }
                "YEAR" -> {
                    if (cal != null) {
                        cal.get(Calendar.YEAR) == targetYearInt
                    } else {
                        record.date.contains(targetYearInt.toString())
                    }
                }
                else -> true
            }
        }
    }

    val totalIncome = filteredRecords.filter { it.type == FinanceType.INCOME }.sumOf { it.amount }
    val totalExpenses = filteredRecords.filter { it.type == FinanceType.EXPENSE }.sumOf { it.amount }
    val netBalance = totalIncome - totalExpenses
    val netColor = if (netBalance >= 0) Color(0xFF15803D) else Color(0xFFB91C1C)
    val netBackground = if (netBalance >= 0) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)

    val timeframeLabel = when (financeTimeframe) {
        "TODAY" -> "Today"
        "MONTH" -> "$selectedFinanceMonth $selectedFinanceYear"
        "YEAR" -> selectedFinanceYear
        else -> "All Time"
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Financial Overview", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1C1D1F))
            Text("Income, expenses and farm performance", fontSize = 12.sp, color = Color(0xFF64748B))
            Spacer(Modifier.height(12.dp))

            // Timeframe Tabs for Finance Overview: Today, Month, Year (Identical to Milk Production Overview)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. TODAY TAB
                val isTodaySelected = financeTimeframe == "TODAY"
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isTodaySelected) ForestGreenPrimary else Color(0xFFF1F5F9),
                    border = if (isTodaySelected) null else BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { financeTimeframe = "TODAY" }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isTodaySelected) Color.White else Color(0xFF334155)
                        )
                    }
                }

                // 2. MONTH TAB
                val isMonthSelected = financeTimeframe == "MONTH"
                Box(modifier = Modifier.weight(1.1f)) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isMonthSelected) ForestGreenPrimary else Color(0xFFF1F5F9),
                        border = if (isMonthSelected) null else BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                financeTimeframe = "MONTH"
                                isFinanceMonthMenuExpanded = true
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedFinanceMonth,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isMonthSelected) Color.White else Color(0xFF334155),
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = "Select Month",
                                tint = if (isMonthSelected) Color.White else Color(0xFF64748B),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = isFinanceMonthMenuExpanded,
                        onDismissRequest = { isFinanceMonthMenuExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        monthsList.forEach { month ->
                            val isCurrent = month.equals(selectedFinanceMonth, ignoreCase = true)
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = month,
                                            fontSize = 13.sp,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isCurrent) ForestGreenPrimary else Color(0xFF1E293B)
                                        )
                                        if (isCurrent) {
                                            Text("✓", color = ForestGreenPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp))
                                        }
                                    }
                                },
                                onClick = {
                                    selectedFinanceMonth = month
                                    financeTimeframe = "MONTH"
                                    isFinanceMonthMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // 3. YEAR TAB
                val isYearSelected = financeTimeframe == "YEAR"
                Box(modifier = Modifier.weight(1.1f)) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isYearSelected) ForestGreenPrimary else Color(0xFFF1F5F9),
                        border = if (isYearSelected) null else BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                financeTimeframe = "YEAR"
                                isFinanceYearMenuExpanded = true
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedFinanceYear,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isYearSelected) Color.White else Color(0xFF334155),
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = "Select Year",
                                tint = if (isYearSelected) Color.White else Color(0xFF64748B),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = isFinanceYearMenuExpanded,
                        onDismissRequest = { isFinanceYearMenuExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        yearsList.forEach { yr ->
                            val isCurrent = yr == selectedFinanceYear
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = yr,
                                            fontSize = 13.sp,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isCurrent) ForestGreenPrimary else Color(0xFF1E293B)
                                        )
                                        if (isCurrent) {
                                            Text("✓", color = ForestGreenPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp))
                                        }
                                    }
                                },
                                onClick = {
                                    selectedFinanceYear = yr
                                    financeTimeframe = "YEAR"
                                    isFinanceYearMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
                SummaryMetric("Income", totalIncome, Color(0xFF15803D), Color(0xFFDCFCE7), currency, Modifier.weight(1f))
                SummaryMetric("Expense", totalExpenses, Color(0xFFB91C1C), Color(0xFFFEE2E2), currency, Modifier.weight(1f))
                SummaryMetric("Net", netBalance, netColor, netBackground, currency, Modifier.weight(1f))
            }
        }

        item {
            Spacer(Modifier.height(4.dp))
            Text("RECENT TRANSACTIONS ($timeframeLabel • ${filteredRecords.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
        }

        if (filteredRecords.isEmpty()) {
            item { EmptyState("No transactions for $timeframeLabel", "Tap Log Transaction to record your first income or expense for this period.") }
        }

        val isOwner = userRole.equals("OWNER", ignoreCase = true)
        items(filteredRecords.sortedByDescending { it.updatedAt }, key = { it.id }) { record ->
            val canModifyThisRecord = canEditFinance && (isOwner || canEditPastDaysLogs || com.example.util.DateValidationUtils.isTodayOrFuture(record.date, record.updatedAt))
            TransactionCard(
                record = record,
                currency = currency,
                menuExpanded = activeMenuRecordId == record.id,
                onMenuExpanded = { onMenuExpanded(if (it) record.id else null) },
                onEdit = { onEditTransaction(record) },
                onDelete = { onDeleteTransactionRequested(record) },
                canEdit = canModifyThisRecord
            )
        }

        if (canEditFinance) {
            item {
                Spacer(Modifier.height(6.dp))
                Button(
                    onClick = onAddTransactionClick,
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("log_transaction_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Log Transaction", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Spacer(Modifier.height(20.dp))
            }
        } else {
            item {
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Viewing finance in read-only mode.",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun SummaryMetric(label: String, amount: Double, color: Color, background: Color, currency: String, modifier: Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = background)) {
        Column(Modifier.padding(horizontal = 9.dp, vertical = 9.dp)) {
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
            Text(formatMoney(currency, amount, 0), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = color, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun TransactionCard(
    record: FinanceRecord,
    currency: String,
    menuExpanded: Boolean,
    onMenuExpanded: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    canEdit: Boolean = true
) {
    val isIncome = record.type == FinanceType.INCOME
    val color = if (isIncome) Color(0xFF15803D) else Color(0xFFB91C1C)
    val background = if (isIncome) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
    val details = listOfNotNull(record.date.takeIf { it.isNotBlank() }, record.description.takeIf { it.isNotBlank() }).joinToString(" • ")

    Card(
        modifier = Modifier.fillMaxWidth().testTag("finance_card_${record.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = background)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 9.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(34.dp).background(if (isIncome) Color(0xFFDCFCE7) else Color(0xFFFEE2E2), RoundedCornerShape(10.dp))) {
                Icon(if (isIncome) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward, null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(9.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(record.category, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Rec $details", fontSize = 10.sp, color = Color(0xFF64748B), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${if (isIncome) "+" else "−"}${formatMoney(currency, record.amount)}", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = color)
                if (canEdit) {
                    Box {
                        IconButton(onClick = { onMenuExpanded(true) }, modifier = Modifier.size(30.dp).testTag("finance_item_menu_${record.id}")) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "Transaction Actions", tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
                        }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { onMenuExpanded(false) }, modifier = Modifier.background(Color.White)) {
                            DropdownMenuItem(
                                text = { Text("Edit Transaction", fontSize = 14.sp, fontWeight = FontWeight.Medium) },
                                leadingIcon = { Icon(Icons.Filled.Edit, null, tint = ForestGreenPrimary, modifier = Modifier.size(18.dp)) },
                                onClick = { onMenuExpanded(false); onEdit() }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Transaction", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFFDC2626)) },
                                leadingIcon = { Icon(Icons.Filled.Delete, null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp)) },
                                onClick = { onMenuExpanded(false); onDelete() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportsTab(reports: List<MonthlyReport>, currency: String, onOpenReport: (MonthlyReport) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Monthly Reports", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF153E2D))
            Text("Automatic reports are created on the first day for the completed previous month when enabled in Settings.", fontSize = 12.sp, color = Color(0xFF64748B))
        }
        if (reports.isEmpty()) item { EmptyState("No reports yet", "The next automatic report will appear here after a successful monthly run.") }
        items(reports.sortedByDescending { it.generatedAt }, key = { it.syncId }) { report ->
            val netColor = if (report.netBalance >= 0) Color(0xFF15803D) else Color(0xFFB91C1C)
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Description, null, tint = ForestGreenPrimary, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(9.dp))
                        Column(Modifier.weight(1f)) {
                            Text(report.title.ifBlank { "Monthly Farm Report" }, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF153E2D))
                            Text(reportMonthLabel(report.reportMonth), fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("Income ${formatMoney(currency, report.totalIncome)} • Expenses ${formatMoney(currency, report.totalExpense)}", fontSize = 11.sp, color = Color(0xFF475569))
                    Text("Net ${formatMoney(currency, report.netBalance)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = netColor)
                    Text("Milk ${"%.1f".format(report.totalMilkLitres)} L • Eggs ${report.totalEggs} • ${report.inventoryItemCount} inventory items", fontSize = 11.sp, color = Color(0xFF64748B))
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = { onOpenReport(report) },
                        enabled = report.fileUrl.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                    ) { Text("VIEW REPORT PDF", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(title: String, body: String) {
    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
            Text(body, fontSize = 12.sp, color = Color(0xFF64748B), modifier = Modifier.padding(top = 4.dp))
        }
    }
}

private fun parseFinanceCalendar(dateStr: String, fallbackTimestamp: Long = 0L): Calendar? {
    val clean = dateStr.trim()
    val formats = arrayOf(
        "dd MMM yyyy",
        "d MMM yyyy",
        "dd MMMM yyyy",
        "d MMMM yyyy",
        "yyyy-MM-dd",
        "dd/MM/yyyy",
        "dd-MM-yyyy",
        "MMMM yyyy",
        "MMM yyyy",
        "dd MMM, hh:mm a",
        "dd MMM"
    )
    for (fmt in formats) {
        try {
            val sdf = SimpleDateFormat(fmt, Locale.getDefault())
            val parsed = sdf.parse(clean)
            if (parsed != null) {
                val cal = Calendar.getInstance().apply { time = parsed }
                if (cal.get(Calendar.YEAR) < 2000) {
                    cal.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR))
                }
                return cal
            }
        } catch (_: Exception) {}
    }
    if (fallbackTimestamp > 0L) {
        return Calendar.getInstance().apply { timeInMillis = fallbackTimestamp }
    }
    return null
}

private fun formatMoney(currency: String, amount: Double, fractionDigits: Int = 2): String =
    "$currency ${String.format(Locale.getDefault(), "%,.${fractionDigits}f", amount)}"

private fun reportMonthLabel(value: String): String = runCatching {
    val source = SimpleDateFormat("yyyy-MM", Locale.US)
    SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(source.parse(value) ?: return@runCatching value)
}.getOrDefault(value)

