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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import com.example.data.FarmUnit
import com.example.data.FinanceRecord
import com.example.data.FinanceType
import com.example.data.MonthlyReport
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.theme.ForestGreenPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

fun FinanceRecord.effectiveTargetUnit(): String {
    if (targetUnit.isNotBlank() && !targetUnit.equals("General Farm", ignoreCase = true)) {
        return targetUnit
    }
    val combined = "$category $description $targetUnit".lowercase()
    return when {
        combined.contains("flock a") -> "Flock A"
        combined.contains("flock b") -> "Flock B"
        combined.contains("flock c") -> "Flock C"
        combined.contains("flock") -> "Poultry Flock"
        combined.contains("cow") || combined.contains("cattle") || combined.contains("milk") || combined.contains("heifer") || combined.contains("calf") || combined.contains("calves") || combined.contains("dairy") || combined.contains("bull") -> "Cattle"
        combined.contains("egg") || combined.contains("poultry") || combined.contains("chick") || combined.contains("layer") || combined.contains("broiler") || combined.contains("kienyeji") || combined.contains("bird") || combined.contains("coop") || combined.contains("mash") || combined.contains("grower") || combined.contains("starter") || combined.contains("finisher") -> "Poultry"
        combined.contains("crop") || combined.contains("field") || combined.contains("maize") || combined.contains("silage") || combined.contains("harvest") -> "Crops / Fields"
        combined.contains("goat") -> "Goats"
        else -> "General Farm"
    }
}

fun FinanceRecord.matchesFilter(filter: String): Boolean {
    if (filter.isBlank() || filter.equals("ALL", ignoreCase = true) || filter.equals("All Categories", ignoreCase = true)) {
        return true
    }
    val target = effectiveTargetUnit().lowercase()
    val cat = category.lowercase()
    val desc = description.lowercase()
    val origTarget = targetUnit.lowercase()
    val f = filter.lowercase()

    return when {
        f == "cattle" || f == "all cattle" -> {
            target.contains("cattle") || target.contains("cow") || cat.contains("cattle") || cat.contains("milk") || desc.contains("cattle") || desc.contains("cow") || origTarget.contains("cattle")
        }
        f == "poultry" || f == "all poultry" -> {
            target.contains("poultry") || target.contains("flock") || cat.contains("egg") || cat.contains("poultry") || desc.contains("flock") || desc.contains("chick") || origTarget.contains("poultry") || origTarget.contains("flock")
        }
        f.startsWith("flock") -> {
            target.contains(f) || desc.contains(f) || cat.contains(f)
        }
        f.contains("feed") -> {
            cat.contains("feed") || desc.contains("feed") || desc.contains("mash") || desc.contains("silage")
        }
        f.contains("vet") || f.contains("vaccin") -> {
            cat.contains("vet") || cat.contains("vaccin") || cat.contains("medic") || desc.contains("vet") || desc.contains("vaccin")
        }
        f.contains("milk") -> {
            cat.contains("milk") || desc.contains("milk")
        }
        f.contains("egg") -> {
            cat.contains("egg") || desc.contains("egg")
        }
        f.contains("labor") || f.contains("wage") -> {
            cat.contains("labor") || cat.contains("wage") || cat.contains("salary")
        }
        f.contains("equipment") || f.contains("repair") -> {
            cat.contains("equipment") || cat.contains("repair") || cat.contains("maintenance")
        }
        else -> {
            target.contains(f) || cat.contains(f) || desc.contains(f)
        }
    }
}

@Composable
fun FinanceScreen(
    records: List<FinanceRecord>,
    reports: List<MonthlyReport> = emptyList(),
    units: List<FarmUnit> = emptyList(),
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
                reports = reports,
                units = units,
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
    reports: List<MonthlyReport> = emptyList(),
    units: List<FarmUnit> = emptyList(),
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

    // Enterprise / Unit / Category & Type Filter States
    var selectedCategoryOrUnitFilter by remember { mutableStateOf("ALL") }
    var selectedTypeFilter by remember { mutableStateOf("ALL") } // "ALL", "INCOME", "EXPENSE"
    var isFilterMenuExpanded by remember { mutableStateOf(false) }

    val activeFlocks = remember(units, records) {
        val fromUnits = units.filter { it.type.equals("Poultry", ignoreCase = true) }.map { it.name }
        val fromRecords = records.map { it.effectiveTargetUnit() }
            .filter { it.contains("flock", ignoreCase = true) }
        (fromUnits + fromRecords).distinct()
    }
    val activeCattleUnits = remember(units, records) {
        val fromUnits = units.filter { it.type.equals("Cattle", ignoreCase = true) }.map { it.name }
        val fromRecords = records.map { it.effectiveTargetUnit() }
            .filter { it.contains("cow", ignoreCase = true) || it.contains("heifer", ignoreCase = true) }
        (fromUnits + fromRecords).distinct()
    }

    val quickFilters = remember(activeFlocks) {
        val list = mutableListOf<Pair<String, String>>()
        list.add("ALL" to "All")
        list.add("Cattle" to "🐄 Cattle")
        list.add("Poultry" to "🐔 Poultry")
        activeFlocks.forEach { flock ->
            val label = if (flock.startsWith("flock", ignoreCase = true)) "🐔 $flock" else "🐔 Flock $flock"
            list.add(flock to label)
        }
        list.add("Feeds" to "🌾 Feeds")
        list.add("Vaccines & Vet" to "💉 Vet & Vaccines")
        list.add("Milk Sales" to "🥛 Milk Sales")
        list.add("Egg Sales" to "🥚 Egg Sales")
        list.add("Labor" to "👷 Labor")
        list.add("Equipment" to "⚙️ Equipment")
        list
    }

    val targetMonthIdx = monthsList.indexOfFirst { it.equals(selectedFinanceMonth, ignoreCase = true) }
    val targetYearInt = selectedFinanceYear.toIntOrNull() ?: currentYearNum

    // Time-based filtering
    val timeFilteredRecords = remember(records, financeTimeframe, selectedFinanceMonth, selectedFinanceYear, targetMonthIdx, targetYearInt) {
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

    // Comprehensive filtering by Category/Enterprise and Type
    val filteredRecords = remember(timeFilteredRecords, selectedTypeFilter, selectedCategoryOrUnitFilter) {
        timeFilteredRecords.filter { record ->
            val matchesType = when (selectedTypeFilter) {
                "INCOME" -> record.type == FinanceType.INCOME
                "EXPENSE" -> record.type == FinanceType.EXPENSE
                else -> true
            }
            val matchesCat = record.matchesFilter(selectedCategoryOrUnitFilter)
            matchesType && matchesCat
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
            Text("Income, expenses and enterprise performance", fontSize = 12.sp, color = Color(0xFF64748B))
            Spacer(Modifier.height(12.dp))

            // Timeframe Tabs for Finance Overview: Today, Month, Year
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
                Box(modifier = Modifier.weight(1f)) {
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

            Spacer(Modifier.height(12.dp))

            // CATEGORY / ENTERPRISE FILTER ROW
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "ENTERPRISE & CATEGORY FILTER",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )

                // More Filter Options Dropdown
                Box {
                    Row(
                        modifier = Modifier
                            .clickable { isFilterMenuExpanded = true }
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FilterList,
                            contentDescription = "Filter menu",
                            tint = ForestGreenPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "More Filters",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenPrimary
                        )
                    }

                    DropdownMenu(
                        expanded = isFilterMenuExpanded,
                        onDismissRequest = { isFilterMenuExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = { Text("🌟 All Categories & Enterprises", fontWeight = FontWeight.Bold) },
                            onClick = {
                                selectedCategoryOrUnitFilter = "ALL"
                                isFilterMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("🐄 Cattle (Dairy & Beef)") },
                            onClick = {
                                selectedCategoryOrUnitFilter = "Cattle"
                                isFilterMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("🐔 Poultry (All Flocks)") },
                            onClick = {
                                selectedCategoryOrUnitFilter = "Poultry"
                                isFilterMenuExpanded = false
                            }
                        )
                        activeFlocks.forEach { flock ->
                            DropdownMenuItem(
                                text = { Text("🐔 $flock") },
                                onClick = {
                                    selectedCategoryOrUnitFilter = flock
                                    isFilterMenuExpanded = false
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("🌾 Feeds & Nutrition") },
                            onClick = {
                                selectedCategoryOrUnitFilter = "Feeds"
                                isFilterMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("💉 Vaccines & Vet Care") },
                            onClick = {
                                selectedCategoryOrUnitFilter = "Vaccines & Vet"
                                isFilterMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("🥛 Milk Sales") },
                            onClick = {
                                selectedCategoryOrUnitFilter = "Milk Sales"
                                isFilterMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("🥚 Egg Sales") },
                            onClick = {
                                selectedCategoryOrUnitFilter = "Egg Sales"
                                isFilterMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("👷 Labor & Wages") },
                            onClick = {
                                selectedCategoryOrUnitFilter = "Labor"
                                isFilterMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("⚙️ Equipment & Maintenance") },
                            onClick = {
                                selectedCategoryOrUnitFilter = "Equipment"
                                isFilterMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("🌾 Crops & Field Harvests") },
                            onClick = {
                                selectedCategoryOrUnitFilter = "Crops / Fields"
                                isFilterMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // Quick Filter Chips Row (All, Cattle, Poultry, Flock A, Flock B, Feeds, etc.)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(quickFilters) { (filterKey, filterLabel) ->
                    val isSelected = selectedCategoryOrUnitFilter.equals(filterKey, ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = if (isSelected) ForestGreenPrimary else Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, if (isSelected) ForestGreenPrimary else Color(0xFFCBD5E1)),
                        modifier = Modifier.clickable {
                            selectedCategoryOrUnitFilter = filterKey
                        }
                    ) {
                        Text(
                            text = filterLabel,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color(0xFF334155),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // TYPE FILTER PILLS (All, Income, Expense)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val types = listOf(
                    "ALL" to "All Types",
                    "INCOME" to "🟢 Income",
                    "EXPENSE" to "🔴 Expense"
                )
                types.forEach { (typeKey, typeLabel) ->
                    val isSelected = selectedTypeFilter == typeKey
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when {
                            isSelected && typeKey == "INCOME" -> Color(0xFFDCFCE7)
                            isSelected && typeKey == "EXPENSE" -> Color(0xFFFEE2E2)
                            isSelected -> ForestGreenPrimary
                            else -> Color(0xFFF8FAFC)
                        },
                        border = BorderStroke(1.dp, if (isSelected) Color.Transparent else Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTypeFilter = typeKey }
                    ) {
                        Text(
                            text = typeLabel,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = when {
                                isSelected && typeKey == "INCOME" -> Color(0xFF15803D)
                                isSelected && typeKey == "EXPENSE" -> Color(0xFFB91C1C)
                                isSelected -> Color.White
                                else -> Color(0xFF64748B)
                            },
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                }
            }

            // Filter Active Indicator Banner
            if (selectedCategoryOrUnitFilter != "ALL" || selectedTypeFilter != "ALL") {
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Filled.FilterList, contentDescription = null, tint = ForestGreenPrimary, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Filtered: ${if (selectedCategoryOrUnitFilter != "ALL") selectedCategoryOrUnitFilter else "All"} (${if (selectedTypeFilter != "ALL") selectedTypeFilter else "All"}) • ${filteredRecords.size} records",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E293B),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                selectedCategoryOrUnitFilter = "ALL"
                                selectedTypeFilter = "ALL"
                            }
                        ) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear", tint = Color(0xFF64748B), modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(2.dp))
                            Text(
                                text = "Reset",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreenPrimary
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Dynamic Summary Metrics based on Active Filters
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
                SummaryMetric("Income", totalIncome, Color(0xFF15803D), Color(0xFFDCFCE7), currency, Modifier.weight(1f))
                SummaryMetric("Expense", totalExpenses, Color(0xFFB91C1C), Color(0xFFFEE2E2), currency, Modifier.weight(1f))
                SummaryMetric("Net", netBalance, netColor, netBackground, currency, Modifier.weight(1f))
            }
        }

        item {
            Spacer(Modifier.height(4.dp))
            val filterNote = if (selectedCategoryOrUnitFilter != "ALL") " • $selectedCategoryOrUnitFilter" else ""
            Text("TRANSACTIONS ($timeframeLabel$filterNote • ${filteredRecords.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
        }

        if (filteredRecords.isEmpty()) {
            item {
                EmptyState(
                    title = "No transactions found",
                    body = if (selectedCategoryOrUnitFilter != "ALL" || selectedTypeFilter != "ALL") {
                        "No transactions match the selected filter ($selectedCategoryOrUnitFilter / $selectedTypeFilter) for $timeframeLabel. Try clearing the filter."
                    } else {
                        "No transactions for $timeframeLabel. Tap Log Transaction to record your first income or expense."
                    }
                )
            }
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
    val effTarget = record.effectiveTargetUnit()
    val details = listOfNotNull(record.date.takeIf { it.isNotBlank() }, record.description.takeIf { it.isNotBlank() }).joinToString(" • ")

    val (badgeBg, badgeText, badgeIcon) = when {
        effTarget.contains("flock", ignoreCase = true) || effTarget.contains("poultry", ignoreCase = true) ->
            Triple(Color(0xFFFEF3C7), Color(0xFF92400E), "🐔")
        effTarget.contains("cattle", ignoreCase = true) || effTarget.contains("cow", ignoreCase = true) ->
            Triple(Color(0xFFE0F2FE), Color(0xFF0369A1), "🐄")
        effTarget.contains("crop", ignoreCase = true) || effTarget.contains("field", ignoreCase = true) ->
            Triple(Color(0xFFDCFCE7), Color(0xFF166534), "🌾")
        effTarget.equals("General Farm", ignoreCase = true) ->
            Triple(Color(0xFFF1F5F9), Color(0xFF475569), "🚜")
        else ->
            Triple(Color(0xFFEDE9FE), Color(0xFF6D28D9), "🏷️")
    }

    Card(
        modifier = Modifier.fillMaxWidth().testTag("finance_card_${record.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = background)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 9.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(34.dp).background(if (isIncome) Color(0xFFDCFCE7) else Color(0xFFFEE2E2), RoundedCornerShape(10.dp))
            ) {
                Icon(if (isIncome) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward, null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(9.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        record.category,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = badgeBg
                    ) {
                        Text(
                            text = "$badgeIcon $effTarget",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeText,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
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

