Package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EggLog
import com.example.data.EmployeeRequest
import com.example.data.FarmTask
import com.example.data.FarmUnit
import com.example.data.FinanceRecord
import com.example.data.FinanceType
import com.example.data.MilkLog
import com.example.data.RequestStatus
import com.example.data.TaskCategory
import com.example.data.TaskPriority
import com.example.ui.TaskStatusFilter
import com.example.ui.components.TaskCard
import androidx.compose.material3.MaterialTheme
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.StatusUrgentRed
import com.example.ui.theme.mkulimaColors
import com.example.util.CattleLifecycleEngine
import com.example.util.CattleStage
import com.example.util.FarmReminder
import com.example.util.FarmReminderEngine
import com.example.util.ReminderType
import com.example.util.ReminderUrgency
import com.example.ui.components.FarmRemindersDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Custom Mkulima Theme Palette matching active theme mode
private val DawnTop: Color @Composable get() = MaterialTheme.mkulimaColors.heroTop
private val DawnBottom: Color @Composable get() = MaterialTheme.mkulimaColors.heroBottom
private val Soil: Color @Composable get() = MaterialTheme.mkulimaColors.textPrimary
private val SoilSoft: Color @Composable get() = MaterialTheme.mkulimaColors.textSecondary
private val Straw: Color @Composable get() = MaterialTheme.colorScheme.background
private val Terracotta: Color @Composable get() = MaterialTheme.mkulimaColors.primary
private val TerracottaDeep: Color @Composable get() = MaterialTheme.mkulimaColors.primaryVariant
private val Sage: Color @Composable get() = MaterialTheme.mkulimaColors.secondary
private val SageBg: Color @Composable get() = MaterialTheme.mkulimaColors.secondaryContainer
private val RustBg: Color @Composable get() = MaterialTheme.mkulimaColors.primaryContainer
private val LineColor: Color @Composable get() = MaterialTheme.mkulimaColors.cardBorder


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
    tasks: List<FarmTask> = emptyList(),
    milkLogs: List<MilkLog> = emptyList(),
    eggLogs: List<EggLog> = emptyList(),
    units: List<FarmUnit> = emptyList(),
    allCattleEvents: List<com.example.data.CattleEvent> = emptyList(),
    financeRecords: List<FinanceRecord> = emptyList(),
    employeeRequests: List<EmployeeRequest> = emptyList(),
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
    onCompleteReminderClick: (com.example.util.FarmReminder) -> Unit = {},
    onDismissReminderClick: (String) -> Unit = {},
    onRestockClick: () -> Unit = {},
    onNavigateToTab: (Int) -> Unit = {},
    onAddUnitClick: () -> Unit = {},
    onAddMilkLogClick: () -> Unit = {},
    onAddEggLogClick: () -> Unit = {},
    userRole: String,
    farmSettings: com.example.data.FarmSettings,
    modifier: Modifier = Modifier
) {
    val isCattleMode = farmSettings.farmType.equals("Cattle Only", ignoreCase = true)
    val isPoultryMode = farmSettings.farmType.equals("Poultry Only", ignoreCase = true)
    val isBothMode = !isCattleMode && !isPoultryMode

    var selectedProdMetric by remember(farmSettings.farmType) {
        mutableStateOf(if (isPoultryMode) "Eggs" else "Milk")
    }
    var showProdDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(farmSettings.farmType) {
        if (isPoultryMode) {
            selectedProdMetric = "Eggs"
        } else if (isCattleMode) {
            selectedProdMetric = "Milk"
        }
    }

    // Dynamic Time-Aware Greeting
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "Good morning 🌅"
            hour < 17 -> "Good afternoon ☀️"
            else -> "Good evening 🌙"
        }
    }

    val todayDateFormatted = remember {
        SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())
    }

    // Dynamic Aggregations
    val cattleUnits = remember(units) {
        units.filter {
            !it.type.contains("POULTRY", ignoreCase = true) && !it.breed.contains("Layer", ignoreCase = true) && !it.breed.contains("Flock", ignoreCase = true)
        }
    }
    val poultryUnits = remember(units) {
        units.filter {
            it.type.contains("POULTRY", ignoreCase = true) || it.breed.contains("Layer", ignoreCase = true) || it.breed.contains("Flock", ignoreCase = true)
        }
    }

    val totalCattle = remember(cattleUnits) {
        cattleUnits.sumOf { it.headCount }
    }
    val totalBirds = remember(poultryUnits) {
        poultryUnits.sumOf { it.headCount }
    }
    val totalPoultryFlocks = remember(poultryUnits) {
        poultryUnits.size
    }

    // Milk Production Calculations
    val todayFormatted1 = remember { java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date()) }
    val todayFormatted2 = remember { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()) }
    val todayFormatted3 = remember { java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault()).format(java.util.Date()) }

    val todayMilkLitres = remember(milkLogs) {
        val todayLogs = milkLogs.filter { log ->
            log.date.contains(todayFormatted1, ignoreCase = true) ||
            log.date.contains(todayFormatted2, ignoreCase = true) ||
            log.date.contains(todayFormatted3, ignoreCase = true) ||
            log.date.contains("Today", ignoreCase = true) ||
            log.loggedAt.contains(todayFormatted1, ignoreCase = true) ||
            log.loggedAt.contains(todayFormatted3, ignoreCase = true) ||
            log.loggedAt.contains("Today", ignoreCase = true)
        }
        todayLogs.sumOf { it.litres }
    }
    val weeklyMilkLitres = remember(milkLogs) {
        milkLogs.sumOf { it.litres }
    }

    // Egg Production Calculations
    val todayEggsCount = remember(eggLogs) {
        val todayLogs = eggLogs.filter { log ->
            log.loggedAt.contains(todayFormatted1, ignoreCase = true) ||
            log.loggedAt.contains(todayFormatted2, ignoreCase = true) ||
            log.loggedAt.contains(todayFormatted3, ignoreCase = true) ||
            log.loggedAt.contains("Today", ignoreCase = true)
        }
        todayLogs.sumOf { it.totalEggs }
    }
    val weeklyEggsCount = remember(eggLogs) {
        eggLogs.sumOf { it.totalEggs }
    }

    // Cattle Stage Breakdown Counts
    val cattleStages = remember(cattleUnits, milkLogs, allCattleEvents) {
        val stageCounts = mutableMapOf(
            "Milking" to 0,
            "In-calf" to 0,
            "Heifers" to 0,
            "Calves" to 0,
            "Bulls" to 0,
            "Dry" to 0,
            "Inseminated" to 0,
            "Disposed" to 0
        )
        val evaluated = cattleUnits.map { unit ->
            val unitDbEvents = allCattleEvents.filter { it.unitId == unit.id }.map {
                com.example.ui.screens.CattleEventItem(
                    id = it.id.toString(),
                    category = it.category,
                    title = it.title,
                    date = it.date,
                    details = it.details,
                    notes = it.notes ?: "",
                    metricValue = it.metricValue ?: ""
                )
            }
            val mockDetail = com.example.ui.screens.AnimalDetailData(
                id = "unit_${unit.id}",
                name = unit.name,
                tagNumber = "#${unit.id}",
                breed = unit.breed,
                category = "CATTLE",
                status = unit.healthStatus,
                age = unit.dob,
                weight = unit.currentWeight,
                lastMilk = "",
                breedingStatus = "ACTIVE",
                dateOfBirth = unit.dob,
                weightAtBirth = unit.weightAtBirth,
                sire = unit.sire,
                dam = unit.dam
            )
            val cowLogs = milkLogs.filter { it.cowName.equals(unit.name, ignoreCase = true) }
            CattleLifecycleEngine.evaluateCattleStage(mockDetail, unitDbEvents, if (cowLogs.isNotEmpty()) cowLogs else milkLogs)
        }
        stageCounts["Milking"] = evaluated.count { it.stage == CattleStage.MILKING }
        stageCounts["In-calf"] = evaluated.count { it.stage == CattleStage.INCALF || it.stage == CattleStage.INCALF_MILKING }
        stageCounts["Heifers"] = evaluated.count { it.stage == CattleStage.HEIFER }
        stageCounts["Calves"] = evaluated.count { it.stage == CattleStage.CALF }
        stageCounts["Bulls"] = evaluated.count { it.stage == CattleStage.BULL }
        stageCounts["Dry"] = evaluated.count { it.stage == CattleStage.DRY }
        stageCounts["Inseminated"] = evaluated.count { it.stage == CattleStage.INSEMINATED }
        stageCounts["Disposed"] = evaluated.count { it.stage == CattleStage.DISPOSED }
        stageCounts
    }

    // Finance Calculations
    val totalIncome = remember(financeRecords) {
        financeRecords.filter { it.type == FinanceType.INCOME }.sumOf { it.amount }
    }
    val totalExpense = remember(financeRecords) {
        financeRecords.filter { it.type == FinanceType.EXPENSE }.sumOf { it.amount }
    }
    val netRevenue = totalIncome - totalExpense

    val incomeByCategory = remember(financeRecords) {
        financeRecords.filter { it.type == FinanceType.INCOME }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }
    val expenseByCategory = remember(financeRecords) {
        financeRecords.filter { it.type == FinanceType.EXPENSE }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }

    // Today Tasks and Reminders (Scheduled for Current Day)
    var showRemindersDialog by remember { mutableStateOf(false) }
    var selectedReminderFilter by remember { mutableStateOf<ReminderType?>(null) }
    val farmReminders = remember(units, tasks) {
        FarmReminderEngine.computeAllReminders(units = units, tasks = tasks)
    }

    val todayDateVariants = remember {
        val now = Date()
        listOf(
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(now),
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now),
            SimpleDateFormat("dd MMM", Locale.getDefault()).format(now),
            SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(now),
            SimpleDateFormat("d MMM", Locale.getDefault()).format(now),
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(now),
            SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(now),
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(now)
        )
    }

    val todayTasksCount = remember(tasks, farmReminders) {
        val isDateMatchingToday: (String?) -> Boolean = { str ->
            if (str.isNullOrBlank()) false
            else {
                val trimmed = str.trim()
                trimmed.contains("Today", ignoreCase = true) ||
                        todayDateVariants.any { trimmed.contains(it, ignoreCase = true) }
            }
        }

        val todayTasks = tasks.filter { task ->
            !task.isCompleted && (
                isDateMatchingToday(task.scheduledTime) ||
                task.scheduledTime.isBlank() ||
                task.priority == TaskPriority.HIGH
            )
        }

        val todayReminders = farmReminders.filter { reminder ->
            !reminder.id.startsWith("task_") && !reminder.isCompleted && (
                reminder.urgency == ReminderUrgency.TODAY ||
                reminder.daysRemaining == 0 ||
                isDateMatchingToday(reminder.dueDateStr)
            )
        }

        todayTasks.size + todayReminders.size
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Straw)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // 1. HERO GRADIENT HEADER
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(DawnTop, DawnBottom, Terracotta),
                                startY = 0f,
                                endY = 850f
                            )
                        )
                        .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 26.dp)
                ) {
                    // Decorative translucent background circle
                    Canvas(modifier = Modifier.matchParentSize()) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.12f),
                            radius = size.width * 0.35f,
                            center = Offset(size.width * 0.1f, size.height * 0.9f)
                        )
                    }

                    Column {
                        // Greeting
                        Text(
                            text = greeting,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Soil,
                            lineHeight = 32.sp
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        // Subtitle date
                        Text(
                            text = "$todayDateFormatted · Here's how the farm stands",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Soil.copy(alpha = 0.75f)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Translucent Glass Badges Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HeroGlassBadge(
                                number = "$todayTasksCount",
                                label = "Today tasks",
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onNavigateToTab(4) }
                            )
                            if (isCattleMode) {
                                HeroGlassBadge(
                                    number = "${"%.0f".format(todayMilkLitres)}L",
                                    label = "Milk today",
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onNavigateToTab(2) }
                                )
                                HeroGlassBadge(
                                    number = "$totalCattle",
                                    label = "Total cattle",
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onNavigateToTab(1) }
                                )
                            } else if (isPoultryMode) {
                                HeroGlassBadge(
                                    number = "$todayEggsCount",
                                    label = "Eggs today",
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onNavigateToTab(2) }
                                )
                                HeroGlassBadge(
                                    number = "$totalBirds",
                                    label = "Total birds",
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onNavigateToTab(1) }
                                )
                            } else {
                                HeroGlassBadge(
                                    number = "${"%.0f".format(todayMilkLitres)}L",
                                    label = "Milk today",
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onNavigateToTab(2) }
                                )
                                HeroGlassBadge(
                                    number = "$todayEggsCount",
                                    label = "Eggs today",
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onNavigateToTab(2) }
                                )
                            }
                        }
                    }
                }
            }

            // 2. CLIPBOARD STACK CONTAINER
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // CARD 1B: FARM REMINDERS & UPCOMING HEALTH SCHEDULE CARD
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dashboard_reminders_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.mkulimaColors.cardBackground),
                        border = BorderStroke(1.dp, LineColor)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            // Head Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("🔔", fontSize = 18.sp)
                                    Text(
                                        text = "Upcoming Reminders",
                                        fontSize = 15.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Soil
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = RustBg
                                    ) {
                                        Text(
                                            text = "${farmReminders.size} Active",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Terracotta,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "View all ›",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Terracotta,
                                    modifier = Modifier
                                        .clickable { showRemindersDialog = true }
                                        .padding(4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Filter Chips Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = selectedReminderFilter == null,
                                    onClick = { selectedReminderFilter = null },
                                    label = { Text("All", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ForestGreenPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                                FilterChip(
                                    selected = selectedReminderFilter == ReminderType.VACCINATION,
                                    onClick = { selectedReminderFilter = ReminderType.VACCINATION },
                                    label = { Text("💉 Vaccines", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ForestGreenPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                                FilterChip(
                                    selected = selectedReminderFilter == ReminderType.DEWORMING,
                                    onClick = { selectedReminderFilter = ReminderType.DEWORMING },
                                    label = { Text("💊 Deworm", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ForestGreenPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                                FilterChip(
                                    selected = selectedReminderFilter == ReminderType.CALVING,
                                    onClick = { selectedReminderFilter = ReminderType.CALVING },
                                    label = { Text("🍼 Calvings", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ForestGreenPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Display top 3 reminders matching filter
                            val displayReminders = remember(farmReminders, selectedReminderFilter) {
                                (if (selectedReminderFilter == null) farmReminders else farmReminders.filter { it.type == selectedReminderFilter }).take(3)
                            }

                            if (displayReminders.isEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFF8FAFC),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("✨", fontSize = 20.sp)
                                        Text(
                                            text = "No upcoming reminders in this category",
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    displayReminders.forEach { reminder ->
                                        val urgencyBg = when (reminder.urgency) {
                                            ReminderUrgency.OVERDUE -> Color(0xFFFEE2E2)
                                            ReminderUrgency.TODAY -> Color(0xFFFFEDD5)
                                            ReminderUrgency.DUE_SOON -> Color(0xFFFEF3C7)
                                            ReminderUrgency.UPCOMING -> Color(0xFFEFF6FF)
                                        }
                                        val urgencyText = when (reminder.urgency) {
                                            ReminderUrgency.OVERDUE -> Color(0xFF991B1B)
                                            ReminderUrgency.TODAY -> Color(0xFFC2410C)
                                            ReminderUrgency.DUE_SOON -> Color(0xFFB45309)
                                            ReminderUrgency.UPCOMING -> Color(0xFF1D4ED8)
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color(0xFFF8FAFC),
                                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { showRemindersDialog = true }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    modifier = Modifier.weight(1f),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    Text(reminder.type.emoji, fontSize = 20.sp)
                                                    Column {
                                                        Text(
                                                            text = reminder.title,
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFF0F172A),
                                                            maxLines = 1
                                                        )
                                                        Text(
                                                            text = "${reminder.targetName} • Due: ${reminder.dueDateStr}",
                                                            fontSize = 11.5.sp,
                                                            color = Color(0xFF64748B),
                                                            maxLines = 1
                                                        )
                                                    }
                                                }

                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = urgencyBg
                                                ) {
                                                    Text(
                                                        text = reminder.urgency.label,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = urgencyText,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // CARD 2: PRODUCTION THIS WEEK CARD
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.mkulimaColors.cardBackground),
                        border = BorderStroke(1.dp, LineColor)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            // Head
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                                ) {
                                    Text("📈", fontSize = 16.sp)
                                    Text(
                                        text = "Production this week",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Soil
                                    )
                                }

                                if (isBothMode) {
                                    Box {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = RustBg,
                                            modifier = Modifier.clickable { showProdDropdown = true }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "$selectedProdMetric ▾",
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Terracotta
                                                )
                                            }
                                        }

                                        DropdownMenu(
                                            expanded = showProdDropdown,
                                            onDismissRequest = { showProdDropdown = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Milk 🥛", fontWeight = FontWeight.Bold, color = Soil) },
                                                onClick = {
                                                    selectedProdMetric = "Milk"
                                                    showProdDropdown = false
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Eggs 🥚", fontWeight = FontWeight.Bold, color = Soil) },
                                                onClick = {
                                                    selectedProdMetric = "Eggs"
                                                    showProdDropdown = false
                                                }
                                            )
                                        }
                                    }
                                } else {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = RustBg
                                    ) {
                                        Text(
                                            text = if (isCattleMode) "Milk 🥛" else "Eggs 🥚",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Terracotta,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Production Numbers
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Today Block
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (selectedProdMetric == "Milk") "${"%.0f".format(todayMilkLitres)}L" else "$todayEggsCount",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Soil
                                    )
                                    Text("Today", fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, color = SoilSoft)
                                    Text(
                                        text = if (selectedProdMetric == "Milk") "↑ 4L vs yesterday" else "↑ 24 eggs vs yesterday",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Sage
                                    )
                                }

                                // Divider line
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(48.dp)
                                        .background(LineColor)
                                )

                                // This Week Block
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (selectedProdMetric == "Milk") "${"%.0f".format(weeklyMilkLitres)}L" else "$weeklyEggsCount",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Soil
                                    )
                                    Text("This week", fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, color = SoilSoft)
                                    Text(
                                        text = "↑ 12% vs last week",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Sage
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Spline Chart
                            ProductionSparkline(
                                points = if (selectedProdMetric == "Milk") {
                                    listOf(32f, 35f, 30f, 38f, 34f, 40f, 38f)
                                } else {
                                    listOf(280f, 295f, 310f, 290f, 320f, 305f, 312f)
                                },
                                strokeColor = Terracotta
                            )
                        }
                    }
                }
            }

            // 3. QUICK ACTIONS ROW (Soil Border Chips)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isCattleMode) {
                        QuickActionChip(
                            icon = "🥛",
                            label = "Milk Log",
                            modifier = Modifier
                                .weight(1f)
                                .testTag("qa_milk_log"),
                            onClick = {
                                onAddMilkLogClick()
                                onNavigateToTab(2)
                            }
                        )
                        QuickActionChip(
                            icon = "➕",
                            label = "Add Cattle",
                            modifier = Modifier
                                .weight(1f)
                                .testTag("qa_add_animal"),
                            onClick = {
                                onAddUnitClick()
                                onNavigateToTab(1)
                            }
                        )
                        QuickActionChip(
                            icon = "📊",
                            label = "Per Cow",
                            modifier = Modifier
                                .weight(1f)
                                .testTag("qa_per_cow"),
                            onClick = { onNavigateToTab(2) }
                        )
                        QuickActionChip(
                            icon = "✅",
                            label = "Approvals",
                            modifier = Modifier
                                .weight(1f)
                                .testTag("qa_approvals"),
                            onClick = { onNavigateToTab(4) }
                        )
                    } else if (isPoultryMode) {
                        QuickActionChip(
                            icon = "🥚",
                            label = "Egg Log",
                            modifier = Modifier
                                .weight(1f)
                                .testTag("qa_egg_log"),
                            onClick = {
                                onAddEggLogClick()
                                onNavigateToTab(2)
                            }
                        )
                        QuickActionChip(
                            icon = "➕",
                            label = "Add Flock",
                            modifier = Modifier
                                .weight(1f)
                                .testTag("qa_add_animal"),
                            onClick = {
                                onAddUnitClick()
                                onNavigateToTab(1)
                            }
                        )
                        QuickActionChip(
                            icon = "📉",
                            label = "Laying %",
                            modifier = Modifier
                                .weight(1f)
                                .testTag("qa_laying_rate"),
                            onClick = { onNavigateToTab(2) }
                        )
                        QuickActionChip(
                            icon = "✅",
                            label = "Approvals",
                            modifier = Modifier
                                .weight(1f)
                                .testTag("qa_approvals"),
                            onClick = { onNavigateToTab(4) }
                        )
                    } else {
                        QuickActionChip(
                            icon = "🥛",
                            label = "Milk Log",
                            modifier = Modifier
                                .weight(1f)
                                .testTag("qa_milk_log"),
                            onClick = {
                                onAddMilkLogClick()
                                onNavigateToTab(2)
                            }
                        )
                        QuickActionChip(
                            icon = "🥚",
                            label = "Egg Log",
                            modifier = Modifier
                                .weight(1f)
                                .testTag("qa_egg_log"),
                            onClick = {
                                onAddEggLogClick()
                                onNavigateToTab(2)
                            }
                        )
                        QuickActionChip(
                            icon = "➕",
                            label = "Add Unit",
                            modifier = Modifier
                                .weight(1f)
                                .testTag("qa_add_animal"),
                            onClick = {
                                onAddUnitClick()
                                onNavigateToTab(1)
                            }
                        )
                        QuickActionChip(
                            icon = "✅",
                            label = "Tasks",
                            modifier = Modifier
                                .weight(1f)
                                .testTag("qa_approvals"),
                            onClick = { onNavigateToTab(4) }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(28.dp))
            }
        }

        if (showRemindersDialog) {
            FarmRemindersDialog(
                reminders = farmReminders,
                onDismiss = { showRemindersDialog = false },
                onNavigateToAnimal = { unitId ->
                    showRemindersDialog = false
                    onNavigateToTab(1)
                },
                onAddNewTaskClick = {
                    showRemindersDialog = false
                    onAddTaskClick()
                },
                onMarkTaskDone = { reminder ->
                    onCompleteReminderClick(reminder)
                },
                onDismissReminder = { reminderId ->
                    onDismissReminderClick(reminderId)
                }
            )
        }
    }
}

@Composable
private fun HeroGlassBadge(
    number: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.mkulimaColors.cardBackground.copy(alpha = 0.65f),
        border = BorderStroke(1.dp, MaterialTheme.mkulimaColors.cardBorder.copy(alpha = 0.6f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 9.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = number,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Soil,
                fontFamily = FontFamily.Serif
            )
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = SoilSoft,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun UrgentItemRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isLast: Boolean = false
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    fontSize = 10.5.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
            Text(
                text = "›",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
        if (!isLast) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.18f))
            )
        }
    }
}

@Composable
private fun StageGridCell(
    count: String,
    label: String,
    isAttn: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isAttn) RustBg else SageBg,
        border = BorderStroke(1.dp, if (isAttn) Color(0xFFFDE68A) else Color(0xFFE2E8F0)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 9.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isAttn) TerracottaDeep else Soil,
                fontFamily = FontFamily.Serif
            )
            Text(
                text = label,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                color = SoilSoft,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun PoultrySummaryCell(
    number: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Straw,
        border = BorderStroke(1.5.dp, LineColor),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = number,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Soil,
                fontFamily = FontFamily.Serif
            )
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = SoilSoft,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun TimelineItemRow(
    name: String,
    meta: String,
    isWarning: Boolean,
    isLast: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(bottom = if (isLast) 0.dp else 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Dot + Connecting line
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(if (isWarning) Terracotta else Sage, CircleShape)
                    .shadow(1.dp, CircleShape)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(1.5.dp)
                        .height(34.dp)
                        .background(LineColor)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = Soil
            )
            Text(
                text = meta,
                fontSize = 10.5.sp,
                color = SoilSoft
            )
        }
    }
}

@Composable
private fun ReceiptLineItem(
    label: String,
    amount: String,
    isPositive: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.5.sp,
            color = Straw
        )
        Text(
            text = amount,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            color = if (isPositive) Color(0xFFA8C99A) else Color(0xFFF0A98C)
        )
    }
}

@Composable
private fun QuickActionChip(
    icon: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.mkulimaColors.cardBackground,
        border = BorderStroke(1.5.dp, LineColor),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Soil,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ProductionSparkline(
    points: List<Float>,
    strokeColor: Color
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        if (points.size < 2) return@Canvas

        val maxVal = points.maxOrNull() ?: 1f
        val minVal = points.minOrNull() ?: 0f
        val range = (maxVal - minVal).coerceAtLeast(1f)

        val w = size.width
        val h = size.height
        val stepX = w / (points.size - 1)

        val path = Path()
        val fillPath = Path()

        points.forEachIndexed { index, value ->
            val x = index * stepX
            val normalized = (value - minVal) / range
            val y = h - (normalized * (h - 16f)) - 8f

            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, h)
                fillPath.lineTo(x, y)
            } else {
                val prevX = (index - 1) * stepX
                val prevNormalized = (points[index - 1] - minVal) / range
                val prevY = h - (prevNormalized * (h - 16f)) - 8f

                val cx1 = prevX + (x - prevX) / 2f
                val cy1 = prevY
                val cx2 = prevX + (x - prevX) / 2f
                val cy2 = y

                path.cubicTo(cx1, cy1, cx2, cy2, x, y)
                fillPath.cubicTo(cx1, cy1, cx2, cy2, x, y)
            }

            if (index == points.size - 1) {
                fillPath.lineTo(x, h)
                fillPath.close()
            }
        }

        // Draw soft gradient fill underneath
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(strokeColor.copy(alpha = 0.25f), strokeColor.copy(alpha = 0.02f)),
                startY = 0f,
                endY = h
            )
        )

        // Draw smooth spline stroke
        drawPath(
            path = path,
            color = strokeColor,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Draw endpoint circle marker
        val lastNormalized = (points.last() - minVal) / range
        val lastX = w
        val lastY = h - (lastNormalized * (h - 16f)) - 8f
        drawCircle(
            color = strokeColor,
            radius = 4.dp.toPx(),
            center = Offset(lastX, lastY)
        )
    }
}
