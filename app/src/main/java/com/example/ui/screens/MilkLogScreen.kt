package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MilkLog
import com.example.data.EggLog
import com.example.ui.theme.ForestGreenPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MilkTotalsSummary(
    val totalLitres: String,
    val avgPerDay: String,
    val topCow: String,
    val trendStr: String
)

data class AnimalCowItem(
    val tagId: String,
    val name: String,
    val breed: String,
    val lactationDay: Int
)

data class CowYieldAnalysis(
    val cowLitres: String,
    val cowAvg: String,
    val cowSessions: String,
    val morningYield: Double,
    val middayYield: Double,
    val eveningYield: Double,
    val morningCount: Int,
    val middayCount: Int,
    val eveningCount: Int
)

fun isLogForCow(log: MilkLog, cow: AnimalCowItem): Boolean {
    val cleanLogName = log.cowName.lowercase()
    val cleanCowName = cow.name.lowercase()
    val cowBase = cow.name.substringBefore(" (").trim().lowercase()
    val tag = cow.tagId.lowercase().replace("#", "").trim()
    return cleanLogName.contains(cleanCowName) ||
            cleanCowName.contains(cleanLogName) ||
            (cowBase.isNotEmpty() && cleanLogName.contains(cowBase)) ||
            (tag.isNotEmpty() && cleanLogName.contains(tag))
}

/**
 * Custom Compose Canvas Line Chart for Milk Production Trends with Dynamic Data and Numerical Labels
 */
@Composable
fun MilkProductionLineChart(
    dataPoints: List<Float>,
    xLabels: List<String>,
    modifier: Modifier = Modifier,
    unitSuffix: String = "L"
) {
    if (dataPoints.isEmpty() || dataPoints.all { it == 0f }) {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF8FAFC),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Filled.WaterDrop,
                    contentDescription = null,
                    tint = ForestGreenPrimary.copy(alpha = 0.5f),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "No milk records logged for this timeframe",
                    color = Color(0xFF64748B),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Add entries using '+ Quick Milk Entry' below",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }
        }
        return
    }

    val maxVal = (dataPoints.maxOrNull() ?: 10f).coerceAtLeast(1f)
    val minVal = (dataPoints.minOrNull() ?: 0f).coerceAtLeast(0f)
    val displayMax = if (maxVal == minVal) maxVal * 1.3f else maxVal * 1.2f
    val displayMin = 0f
    val range = (displayMax - displayMin).coerceAtLeast(1f)

    val lineColor = ForestGreenPrimary
    val gradientColor = ForestGreenPrimary.copy(alpha = 0.22f)

    val valueTextPaint = remember {
        android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#15803D")
            textSize = 28f
            isFakeBoldText = true
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
    }
    val gridTextPaint = remember {
        android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#94A3B8")
            textSize = 22f
            textAlign = android.graphics.Paint.Align.LEFT
            isAntiAlias = true
        }
    }

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val width = size.width
            val height = size.height
            val paddingLeft = 38.dp.toPx()
            val paddingRight = 16.dp.toPx()
            val usableWidth = (width - paddingLeft - paddingRight).coerceAtLeast(1f)

            // Horizontal Grid Lines with Y-Axis reference numbers
            val gridSteps = 3
            for (i in 0..gridSteps) {
                val y = height * (i.toFloat() / gridSteps)
                drawLine(
                    color = Color(0xFFE2E8F0),
                    start = Offset(paddingLeft, y),
                    end = Offset(width - paddingRight, y),
                    strokeWidth = 1.dp.toPx()
                )
                val gridVal = displayMax - (i.toFloat() / gridSteps) * range
                drawContext.canvas.nativeCanvas.drawText(
                    "%.0f%s".format(gridVal, unitSuffix),
                    4.dp.toPx(),
                    (y + 4.dp.toPx()).coerceAtMost(height - 2.dp.toPx()),
                    gridTextPaint
                )
            }

            // Calculate point coordinates
            val pointCount = dataPoints.size
            val spacingX = if (pointCount > 1) usableWidth / (pointCount - 1) else usableWidth / 2
            val points = dataPoints.mapIndexed { idx, value ->
                val x = paddingLeft + (if (pointCount > 1) idx * spacingX else usableWidth / 2)
                val normalizedY = (value - displayMin) / range
                val y = height - (normalizedY * (height * 0.72f) + height * 0.12f)
                Offset(x, y)
            }

            if (points.size > 1) {
                // Build Smooth Curved Path
                val strokePath = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 0 until points.size - 1) {
                        val p1 = points[i]
                        val p2 = points[i + 1]
                        val controlX = (p1.x + p2.x) / 2
                        cubicTo(controlX, p1.y, controlX, p2.y, p2.x, p2.y)
                    }
                }

                // Fill Path (Gradient)
                val fillPath = Path().apply {
                    addPath(strokePath)
                    lineTo(points.last().x, height)
                    lineTo(points.first().x, height)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(gradientColor, Color.Transparent)
                    )
                )

                // Draw Smooth Line
                drawPath(
                    path = strokePath,
                    color = lineColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Draw Dots and Values
            points.forEachIndexed { idx, point ->
                val valAmt = dataPoints[idx]
                drawCircle(
                    color = Color.White,
                    radius = 6.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = lineColor,
                    radius = 4.dp.toPx(),
                    center = point
                )

                // Numeric Value Label above dot
                val formattedVal = if (valAmt % 1.0f == 0f) "%.0f%s".format(valAmt, unitSuffix) else "%.1f%s".format(valAmt, unitSuffix)
                drawContext.canvas.nativeCanvas.drawText(
                    formattedVal,
                    point.x,
                    (point.y - 8.dp.toPx()).coerceAtLeast(18.dp.toPx()),
                    valueTextPaint
                )
            }
        }

        // X Labels Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 32.dp, end = 12.dp, top = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            xLabels.forEach { label ->
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun MilkLogScreen(
    milkLogs: List<MilkLog>,
    eggLogs: List<EggLog> = emptyList(),
    units: List<com.example.data.FarmUnit> = emptyList(),
    onAddMilkLogClick: () -> Unit,
    onAddEggLogClick: () -> Unit = {},
    onQuickSaveMilkLog: (cowName: String, litres: Double, session: String, date: String) -> Unit = { _, _, _, _ -> },
    onQuickSaveEggLog: (flockName: String, totalEggs: Int, damagedEggs: Int, grade: String, date: String, notes: String?) -> Unit = { _, _, _, _, _, _ -> },
    onDeleteMilkLog: (Long) -> Unit,
    onDeleteEggLog: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Registered Cow Database dynamically built from Room units and default cows
    val cowsList = remember(units) {
        val defaultCows = listOf(
            AnimalCowItem("#102", "Bessie (#102)", "Friesian", 120),
            AnimalCowItem("#105", "Daisy (#105)", "Jersey", 85),
            AnimalCowItem("#110", "Star (#110)", "Guernsey", 140),
            AnimalCowItem("#112", "Bella (#112)", "Ayrshire", 60),
            AnimalCowItem("#115", "Mimi (#115)", "Friesian", 190),
            AnimalCowItem("#120", "Flora (#120)", "Simmental", 45)
        )
        val userCows = units.filter {
            (it.type.equals("Cattle", ignoreCase = true) || it.type.equals("CATTLE", ignoreCase = true)) &&
            !it.healthStatus.contains("DISPOSED", ignoreCase = true) &&
            !it.healthStatus.contains("SOLD", ignoreCase = true) &&
            !it.healthStatus.contains("DEAD", ignoreCase = true)
        }.map { unit ->
            val tag = unit.tagNumber.ifBlank { "#${unit.id + 100}" }
            AnimalCowItem(
                tagId = tag,
                name = "${unit.name} ($tag)",
                breed = unit.breed.ifBlank { "Cattle" },
                lactationDay = 90
            )
        }
        (userCows + defaultCows).distinctBy { it.name }
    }

    // --- MAIN LOG CATEGORY STATE (Milk vs Eggs) ---
    var selectedMainLogCategory by remember { mutableStateOf("MILK") } // "MILK" or "EGGS"

    // --- VIEW SELECTOR STATE FOR MILK ---
    var activeViewTab by remember { mutableStateOf("HERD_TOTALS") } // HERD_TOTALS, QUICK_LOG, PER_COW, HISTORY, ALL

    // --- QUICK LOG FORM STATE FOR MILK ---
    var cowSearchQuery by remember { mutableStateOf("") }
    var selectedCow by remember { mutableStateOf<AnimalCowItem?>(cowsList.firstOrNull()) }
    var showCowDropdown by remember { mutableStateOf(false) }

    var milkLitresText by remember { mutableStateOf("") }
    var fatPercentageText by remember { mutableStateOf("3.8") }
    var selectedSession by remember { mutableStateOf("Morning") } // Morning, Afternoon, Evening

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val todayDateStr = remember { dateFormat.format(Date()) }
    val yesterdayDateStr = remember {
        val cal = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, -1) }
        dateFormat.format(cal.time)
    }
    val twoDaysAgoDateStr = remember {
        val cal = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, -2) }
        dateFormat.format(cal.time)
    }
    var selectedLogDate by remember { mutableStateOf(todayDateStr) }

    var saveSuccessMessage by remember { mutableStateOf<String?>(null) }

    // --- PER COW ANALYTICS STATE ---
    var perCowSelectedCow by remember { mutableStateOf<AnimalCowItem>(cowsList.firstOrNull() ?: AnimalCowItem("#102", "Bessie (#102)", "Friesian", 120)) }
    var perCowTimeframe by remember { mutableStateOf("TODAY") } // TODAY, WEEKLY, MONTHLY, LACTATION

    // --- OVERALL TOTALS & CHART STATE ---
    var overallTimeframe by remember { mutableStateOf("CURRENT_MONTH") } // DAILY, CURRENT_MONTH, PREV_MONTH, SIX_MONTHS, YEAR

    // Filtered logs for search list at bottom
    var historySearchQuery by remember { mutableStateOf("") }
    var historySessionFilter by remember { mutableStateOf("ALL") }

    val filteredHistoryLogs = milkLogs.filter { log ->
        val matchesSession = when (historySessionFilter) {
            "MORNING" -> log.session.equals("Morning", ignoreCase = true)
            "AFTERNOON" -> log.session.equals("Afternoon", ignoreCase = true) || log.session.equals("Midday", ignoreCase = true)
            "EVENING" -> log.session.equals("Evening", ignoreCase = true)
            else -> true
        }
        val matchesSearch = historySearchQuery.isEmpty() ||
                log.cowName.contains(historySearchQuery, ignoreCase = true) ||
                log.date.contains(historySearchQuery, ignoreCase = true)

        matchesSession && matchesSearch
    }

    // Filtered Cows matching search query
    val matchingCows = cowsList.filter {
        cowSearchQuery.isEmpty() ||
                it.name.contains(cowSearchQuery, ignoreCase = true) ||
                it.tagId.contains(cowSearchQuery, ignoreCase = true) ||
                it.breed.contains(cowSearchQuery, ignoreCase = true)
    }

    // --- EGG LOG STATE ---
    val poultryFlocks = remember(units) {
        val defaultFlocks = listOf("Alpha Layers", "Beta Broilers", "Kienyeji Flock 1")
        val userFlocks = units.filter { it.type.contains("Poultry", ignoreCase = true) || it.name.contains("Flock", ignoreCase = true) }.map { it.name }
        (userFlocks + defaultFlocks).distinct()
    }
    var selectedEggFlock by remember { mutableStateOf(poultryFlocks.firstOrNull() ?: "Alpha Layers") }
    var eggTotalText by remember { mutableStateOf("380") }
    var eggDamagedText by remember { mutableStateOf("2") }
    var selectedEggGrade by remember { mutableStateOf("Grade A") }
    var selectedEggLogDate by remember { mutableStateOf(todayDateStr) }
    var eggNotesText by remember { mutableStateOf("") }
    var eggActiveViewTab by remember { mutableStateOf("EGG_OVERVIEW") } // "EGG_OVERVIEW", "EGG_QUICK_LOG", "EGG_HISTORY", "EGG_ALL"
    var eggSearchQuery by remember { mutableStateOf("") }
    var eggGradeFilter by remember { mutableStateOf("ALL") }
    var eggFlockFilter by remember { mutableStateOf("ALL") }
    var analyticsEggFlock by remember { mutableStateOf("ALL") }
    var analyticsEggTimeframe by remember { mutableStateOf("DAILY") } // "DAILY", "WEEKLY", "MONTHLY", "ALL_TIME"

    val filteredEggLogs = eggLogs.filter { log ->
        val matchesSearch = eggSearchQuery.isEmpty() ||
                log.unitName.contains(eggSearchQuery, ignoreCase = true) ||
                log.loggedAt.contains(eggSearchQuery, ignoreCase = true) ||
                (log.notes?.contains(eggSearchQuery, ignoreCase = true) == true)
        val matchesGrade = if (eggGradeFilter == "ALL") true else log.grade.equals(eggGradeFilter, ignoreCase = true)
        val matchesFlock = if (eggFlockFilter == "ALL") true else log.unitName.equals(eggFlockFilter, ignoreCase = true)
        matchesSearch && matchesGrade && matchesFlock
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 0. MAIN LOG CATEGORY TAB SWITCHER (Milk Logs vs Egg Logs) ---
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE2E8F0), shape = RoundedCornerShape(14.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedMainLogCategory = "MILK" },
                    shape = RoundedCornerShape(10.dp),
                    color = if (selectedMainLogCategory == "MILK") Color.White else Color.Transparent,
                    shadowElevation = if (selectedMainLogCategory == "MILK") 2.dp else 0.dp
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.WaterDrop, contentDescription = null, tint = ForestGreenPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "🥛 Milk Yield Log (${milkLogs.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (selectedMainLogCategory == "MILK") ForestGreenPrimary else Color(0xFF64748B)
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedMainLogCategory = "EGGS" },
                    shape = RoundedCornerShape(10.dp),
                    color = if (selectedMainLogCategory == "EGGS") Color.White else Color.Transparent,
                    shadowElevation = if (selectedMainLogCategory == "EGGS") 2.dp else 0.dp
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🥚", fontSize = 15.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Egg Yield Log (${eggLogs.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (selectedMainLogCategory == "EGGS") ForestGreenPrimary else Color(0xFF64748B)
                        )
                    }
                }
            }
        }

        if (selectedMainLogCategory == "MILK") {
            // --- 1. MILK HEADER TITLE & VIEW SELECTOR ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Milk Yield Log",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "Production totals, line charts & quick entry",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    val totalLitresVal = milkLogs.sumOf { it.litres }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFDCFCE7)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.WaterDrop, contentDescription = null, tint = ForestGreenPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Total: %.1f L".format(totalLitresVal), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Navigation View Selector Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(
                        listOf(
                            "HERD_TOTALS" to "📊 Herd Overview",
                            "QUICK_LOG" to "⚡ Quick Entry",
                            "PER_COW" to "🐄 Per Cow Stats",
                            "HISTORY" to "📋 Log History",
                            "ALL" to "👁️ Show All"
                        )
                    ) { (key, label) ->
                        val isSel = activeViewTab == key
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) ForestGreenPrimary else Color(0xFFF1F5F9),
                            border = if (isSel) null else BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            modifier = Modifier.clickable { activeViewTab = key }
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else Color(0xFF334155)
                            )
                        }
                    }
                }
            }

        // --- 2. OVERALL HERD TOTALS & LINE CHART ANALYTICS (PLACED FIRST ON TOP) ---
        if (activeViewTab == "HERD_TOTALS" || activeViewTab == "ALL") {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.TrendingUp, contentDescription = null, tint = ForestGreenPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Herd Totals & Analytics",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Timeframe Selector Chips: DAILY, CURRENT_MONTH, PREV_MONTH, SIX_MONTHS, YEAR
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(
                                listOf(
                                    "DAILY" to "Daily",
                                    "CURRENT_MONTH" to "Current Month",
                                    "PREV_MONTH" to "Prev Month",
                                    "SIX_MONTHS" to "6 Months",
                                    "YEAR" to "Year (Annual)"
                                )
                            ) { (key, label) ->
                                val isSel = overallTimeframe == key
                                FilterChip(
                                    selected = isSel,
                                    onClick = { overallTimeframe = key },
                                    label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ForestGreenPrimary,
                                        selectedLabelColor = Color.White,
                                        containerColor = Color(0xFFF1F5F9),
                                        labelColor = Color(0xFF475569)
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Dynamic Totals and Line Chart Calculation based on overallTimeframe and real milkLogs
                        val (summary, chartData, xLabels) = remember(milkLogs, overallTimeframe) {
                            if (milkLogs.isEmpty()) {
                                Triple(
                                    MilkTotalsSummary("0.0 L", "0.0 L/day", "No records", "0 logs recorded"),
                                    emptyList<Float>(),
                                    listOf("No Data")
                                )
                            } else {
                                when (overallTimeframe) {
                                    "DAILY" -> {
                                        val todayLogs = milkLogs.filter { it.date == todayDateStr || it.date.contains("Today", ignoreCase = true) }
                                        val targetLogs = if (todayLogs.isNotEmpty()) todayLogs else milkLogs

                                        val morningLogs = targetLogs.filter { it.session.equals("Morning", ignoreCase = true) || it.session.contains("AM", ignoreCase = true) }
                                        val afternoonLogs = targetLogs.filter { it.session.equals("Afternoon", ignoreCase = true) || it.session.equals("Midday", ignoreCase = true) }
                                        val eveningLogs = targetLogs.filter { it.session.equals("Evening", ignoreCase = true) || it.session.equals("Night", ignoreCase = true) || (it.session.contains("PM", ignoreCase = true) && !it.session.equals("Afternoon", ignoreCase = true)) }

                                        val morningL = morningLogs.sumOf { it.litres }.toFloat()
                                        val afternoonL = afternoonLogs.sumOf { it.litres }.toFloat()
                                        val eveningL = eveningLogs.sumOf { it.litres }.toFloat()
                                        val totalL = targetLogs.sumOf { it.litres }

                                        val topCowName = targetLogs.groupBy { it.cowName }.maxByOrNull { entry -> entry.value.sumOf { it.litres } }?.let { "${it.key} (${"%.1f".format(it.value.sumOf { it.litres })}L)" } ?: "Herd"

                                        Triple(
                                            MilkTotalsSummary(
                                                totalLitres = "%.1f L".format(totalL),
                                                avgPerDay = "%.1f L/day".format(totalL),
                                                topCow = topCowName,
                                                trendStr = "AM: %.1fL • Mid: %.1fL • PM: %.1fL".format(morningL, afternoonL, eveningL)
                                            ),
                                            listOf(morningL, afternoonL, eveningL),
                                            listOf("Morning (AM)", "Midday (Noon)", "Evening (PM)")
                                        )
                                    }
                                    else -> {
                                        // 7-day trend from the last 7 calendar days
                                        val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
                                        val last7Days = (6 downTo 0).map { dayOffset ->
                                            val c = java.util.Calendar.getInstance()
                                            c.add(java.util.Calendar.DAY_OF_YEAR, -dayOffset)
                                            val fullDate = dateFormat.format(c.time)
                                            val shortLabel = sdf.format(c.time)
                                            Triple(fullDate, shortLabel, c.time)
                                        }

                                        val points = last7Days.map { (fullDate, _, _) ->
                                            val matchingLogs = milkLogs.filter { it.date.equals(fullDate, ignoreCase = true) }
                                            matchingLogs.sumOf { it.litres }.toFloat()
                                        }

                                        val labels = last7Days.map { it.second }
                                        val totalL = milkLogs.sumOf { it.litres }
                                        val distinctDaysCount = milkLogs.map { it.date }.distinct().size.coerceAtLeast(1)
                                        val dailyAvg = totalL / distinctDaysCount
                                        val topCowName = milkLogs.groupBy { it.cowName }.maxByOrNull { entry -> entry.value.sumOf { it.litres } }?.let { "${it.key} (${"%.1f".format(it.value.sumOf { it.litres })}L)" } ?: "Herd"

                                        Triple(
                                            MilkTotalsSummary(
                                                totalLitres = "%.1f L".format(totalL),
                                                avgPerDay = "%.1f L/day".format(dailyAvg),
                                                topCow = topCowName,
                                                trendStr = "${milkLogs.size} logs across $distinctDaysCount days"
                                            ),
                                            points,
                                            labels
                                        )
                                    }
                                }
                            }
                        }

                        val totalLitres = summary.totalLitres
                        val avgPerDay = summary.avgPerDay
                        val topCow = summary.topCow
                        val trendStr = summary.trendStr

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFDCFCE7),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("TOTAL YIELD", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                                    Text(totalLitres, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                                    Text(trendStr, fontSize = 11.sp, color = ForestGreenPrimary.copy(alpha = 0.8f))
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF1F5F9),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("DAILY AVG", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                    Text(avgPerDay, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                    Text("Top: $topCow", fontSize = 10.sp, color = Color(0xFF64748B), maxLines = 1)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "PRODUCTION TREND LINE CHART",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        MilkProductionLineChart(
                            dataPoints = chartData,
                            xLabels = xLabels,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )
                    }
                }
            }
        }

        // --- 3. SIMPLE QUICK ENTRY FORM CARD ---
        if (activeViewTab == "QUICK_LOG" || activeViewTab == "ALL") {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.5.dp, ForestGreenPrimary.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFDCFCE7)
                            ) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = null,
                                    tint = ForestGreenPrimary,
                                    modifier = Modifier
                                        .padding(6.dp)
                                        .size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Quick Milk Entry",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Step 1: Search Animal by Name, ID, or Tag
                        Text(
                            text = "1. SEARCH & SELECT ANIMAL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = selectedCow?.name ?: cowSearchQuery,
                                onValueChange = {
                                    cowSearchQuery = it
                                    selectedCow = null
                                    showCowDropdown = true
                                },
                                placeholder = { Text("Search by Name (e.g. Bessie), Tag (#102)...") },
                                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = ForestGreenPrimary) },
                                trailingIcon = {
                                    Icon(
                                        Icons.Filled.ArrowDropDown,
                                        contentDescription = "Dropdown",
                                        modifier = Modifier.clickable { showCowDropdown = !showCowDropdown }
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("cow_search_input"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            DropdownMenu(
                                expanded = showCowDropdown,
                                onDismissRequest = { showCowDropdown = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                matchingCows.forEach { cow ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(cow.name, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                                Text(cow.breed, fontSize = 12.sp, color = Color(0xFF64748B))
                                            }
                                        },
                                        onClick = {
                                            selectedCow = cow
                                            cowSearchQuery = cow.name
                                            showCowDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick Select Animal Chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(cowsList) { cow ->
                                val isSel = selectedCow?.tagId == cow.tagId
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSel) ForestGreenPrimary else Color(0xFFF1F5F9),
                                    border = if (isSel) null else BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                    modifier = Modifier.clickable {
                                        selectedCow = cow
                                        cowSearchQuery = cow.name
                                    }
                                ) {
                                    Text(
                                        text = cow.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) Color.White else Color(0xFF334155),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Step 2: Session Selection (Morning, Afternoon, Evening)
                        Text(
                            text = "2. MILKING SESSION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(
                                Triple("Morning", "🌅 Morning", Icons.Filled.WbSunny),
                                Triple("Afternoon", "☀️ Afternoon", Icons.Filled.WbCloudy),
                                Triple("Evening", "🌙 Evening", Icons.Filled.NightsStay)
                            ).forEach { (sessionKey, sessionLabel, icon) ->
                                val isSelected = selectedSession.equals(sessionKey, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) ForestGreenPrimary else Color.Transparent)
                                        .clickable { selectedSession = sessionKey },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (isSelected) Color.White else Color(0xFF64748B),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = sessionLabel,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else Color(0xFF475569)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Step 3: Record Date (Today, Yesterday, 2 Days Ago, Custom)
                        Text(
                            text = "3. RECORD DATE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = selectedLogDate == todayDateStr,
                                onClick = { selectedLogDate = todayDateStr },
                                label = { Text("Today", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ForestGreenPrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFFF1F5F9),
                                    labelColor = Color(0xFF475569)
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            FilterChip(
                                selected = selectedLogDate == yesterdayDateStr,
                                onClick = { selectedLogDate = yesterdayDateStr },
                                label = { Text("Yesterday", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ForestGreenPrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFFF1F5F9),
                                    labelColor = Color(0xFF475569)
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            FilterChip(
                                selected = selectedLogDate == twoDaysAgoDateStr,
                                onClick = { selectedLogDate = twoDaysAgoDateStr },
                                label = { Text("2 Days Ago", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ForestGreenPrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFFF1F5F9),
                                    labelColor = Color(0xFF475569)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = selectedLogDate,
                            onValueChange = { selectedLogDate = it },
                            label = { Text("Log Date (e.g., 12 Aug 2026)") },
                            leadingIcon = { Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = ForestGreenPrimary) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Step 4: Enter Milk Details (Litres & Fat %)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = milkLitresText,
                                onValueChange = { milkLitresText = it },
                                label = { Text("Volume (Litres)*") },
                                placeholder = { Text("e.g. 14.5") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .testTag("milk_litres_input"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = fatPercentageText,
                                onValueChange = { fatPercentageText = it },
                                label = { Text("Fat % (Optional)") },
                                placeholder = { Text("e.g. 3.8") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Save Button
                        Button(
                            onClick = {
                                val cowName = selectedCow?.name ?: cowSearchQuery.ifBlank { "Unassigned Cow" }
                                val litres = milkLitresText.toDoubleOrNull() ?: 0.0
                                if (litres > 0) {
                                    onQuickSaveMilkLog(cowName, litres, selectedSession, selectedLogDate)
                                    saveSuccessMessage = "✓ Logged ${"%.1f".format(litres)}L for $cowName ($selectedSession on $selectedLogDate)"
                                    milkLitresText = ""
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("quick_save_milk_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                            enabled = milkLitresText.isNotBlank() && (selectedCow != null || cowSearchQuery.isNotBlank())
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SAVE COW MILK LOG",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        saveSuccessMessage?.let { msg ->
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFDCFCE7),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = msg,
                                    color = ForestGreenPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(10.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 4. INDIVIDUAL COW MILK PRODUCTION VIEWS ---
        if (activeViewTab == "PER_COW" || activeViewTab == "ALL") {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Pets, contentDescription = null, tint = ForestGreenPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Per Cow Yield Performance",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Cow Selector Dropdown Row
                        Text("SELECT COW TO ANALYZE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                        Spacer(modifier = Modifier.height(4.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(cowsList) { cow ->
                                val isSel = perCowSelectedCow.tagId == cow.tagId
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSel) ForestGreenPrimary else Color(0xFFF1F5F9),
                                    modifier = Modifier.clickable { perCowSelectedCow = cow }
                                ) {
                                    Text(
                                        text = cow.name,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) Color.White else Color(0xFF334155)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Timeframe Chips for Per Cow View: TODAY, WEEKLY, MONTHLY, ENTIRE LACTATION
                        Text("TIME PERIOD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                "TODAY" to "Today",
                                "WEEKLY" to "Weekly",
                                "MONTHLY" to "Monthly",
                                "LACTATION" to "Entire Lactation"
                            ).forEach { (tfKey, tfLabel) ->
                                val isSel = perCowTimeframe == tfKey
                                FilterChip(
                                    selected = isSel,
                                    onClick = { perCowTimeframe = tfKey },
                                    label = { Text(tfLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ForestGreenPrimary,
                                        selectedLabelColor = Color.White,
                                        containerColor = Color(0xFFF1F5F9),
                                        labelColor = Color(0xFF475569)
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Dynamic Selected Cow Stats Summary from milkLogs across all 3 milking sessions
                        val cowAllLogs = remember(milkLogs, perCowSelectedCow) {
                            milkLogs.filter { isLogForCow(it, perCowSelectedCow) }
                        }

                        val cowYield = remember(cowAllLogs, perCowTimeframe, perCowSelectedCow) {
                            val filteredLogs = when (perCowTimeframe) {
                                "TODAY" -> {
                                    val todayLogs = cowAllLogs.filter { it.date == todayDateStr || it.date.contains("Today", ignoreCase = true) }
                                    if (todayLogs.isNotEmpty()) todayLogs else cowAllLogs.take(3)
                                }
                                "WEEKLY" -> {
                                    cowAllLogs.take(21)
                                }
                                "MONTHLY" -> {
                                    cowAllLogs.take(90)
                                }
                                else -> cowAllLogs
                            }

                            val mLogs = filteredLogs.filter { it.session.equals("Morning", ignoreCase = true) || it.session.contains("AM", ignoreCase = true) }
                            val midLogs = filteredLogs.filter { it.session.equals("Afternoon", ignoreCase = true) || it.session.equals("Midday", ignoreCase = true) }
                            val eLogs = filteredLogs.filter { it.session.equals("Evening", ignoreCase = true) || it.session.equals("Night", ignoreCase = true) || (it.session.contains("PM", ignoreCase = true) && !it.session.equals("Afternoon", ignoreCase = true)) }

                            val mSum = mLogs.sumOf { it.litres }
                            val midSum = midLogs.sumOf { it.litres }
                            val eSum = eLogs.sumOf { it.litres }
                            val totalSum = filteredLogs.sumOf { it.litres }

                            val dailyAvg = when (perCowTimeframe) {
                                "TODAY" -> totalSum
                                "WEEKLY" -> totalSum / 7.0
                                "MONTHLY" -> totalSum / 30.0
                                else -> totalSum / perCowSelectedCow.lactationDay.coerceAtLeast(1).toDouble()
                            }

                            val sessionStr = "AM: ${"%.1f".format(mSum)}L | Midday: ${"%.1f".format(midSum)}L | PM: ${"%.1f".format(eSum)}L"

                            CowYieldAnalysis(
                                cowLitres = "%.1f L".format(totalSum),
                                cowAvg = "%.1f L/day".format(dailyAvg),
                                cowSessions = sessionStr,
                                morningYield = mSum,
                                middayYield = midSum,
                                eveningYield = eSum,
                                morningCount = mLogs.size,
                                middayCount = midLogs.size,
                                eveningCount = eLogs.size
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = perCowSelectedCow.name,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E293B)
                                        )
                                        Text(
                                            text = "${perCowSelectedCow.breed} • Lactation Day ${perCowSelectedCow.lactationDay}",
                                            fontSize = 12.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = cowYield.cowLitres,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ForestGreenPrimary
                                        )
                                        Text(
                                            text = cowYield.cowAvg,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF475569)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // 3 Milking Sessions Breakdown Display
                                Text(
                                    text = "3-SESSION BREAKDOWN:",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B)
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFFEF3C7),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("🌅 Morning", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                                            Text("%.1f L".format(cowYield.morningYield), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                                            Text("${cowYield.morningCount} logs", fontSize = 10.sp, color = Color(0xFF78350F))
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFE0F2FE),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("☀️ Midday", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0369A1))
                                            Text("%.1f L".format(cowYield.middayYield), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                                            Text("${cowYield.middayCount} logs", fontSize = 10.sp, color = Color(0xFF075985))
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFF1F5F9),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("🌙 Evening", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                                            Text("%.1f L".format(cowYield.eveningYield), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                                            Text("${cowYield.eveningCount} logs", fontSize = 10.sp, color = Color(0xFF64748B))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 5. HISTORY & RECENT LOGS REGISTER ---
        if (activeViewTab == "HISTORY" || activeViewTab == "QUICK_LOG" || activeViewTab == "ALL") {
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Milk Logs",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("ALL", "MORNING", "AFTERNOON", "EVENING").forEach { sessionKey ->
                                val isSel = historySessionFilter == sessionKey
                                FilterChip(
                                    selected = isSel,
                                    onClick = { historySessionFilter = sessionKey },
                                    label = { Text(sessionKey, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ForestGreenPrimary,
                                        selectedLabelColor = Color.White,
                                        containerColor = Color(0xFFF1F5F9),
                                        labelColor = Color(0xFF475569)
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = historySearchQuery,
                        onValueChange = { historySearchQuery = it },
                        placeholder = { Text("Search history by cow or date...") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            if (filteredHistoryLogs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No milk logs found matching your filter.", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            } else {
                items(filteredHistoryLogs, key = { it.id }) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(14.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = when (log.session.lowercase()) {
                                        "morning" -> Color(0xFFFEF3C7)
                                        "afternoon", "midday" -> Color(0xFFE0F2FE)
                                        else -> Color(0xFFF3E8FF)
                                    }
                                ) {
                                    Icon(
                                        imageVector = when (log.session.lowercase()) {
                                            "morning" -> Icons.Filled.WbSunny
                                            "afternoon", "midday" -> Icons.Filled.WbCloudy
                                            else -> Icons.Filled.NightsStay
                                        },
                                        contentDescription = null,
                                        tint = when (log.session.lowercase()) {
                                            "morning" -> Color(0xFFB45309)
                                            "afternoon", "midday" -> Color(0xFF0369A1)
                                            else -> Color(0xFF6B21A8)
                                        },
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = log.cowName,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                    Text(
                                        text = "${log.session} • ${log.date}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "%.1f L".format(log.litres),
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ForestGreenPrimary
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFFDCFCE7)
                                    ) {
                                        Text(
                                            text = "${log.fatPercentage}% Fat",
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ForestGreenPrimary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                IconButton(onClick = { onDeleteMilkLog(log.id) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color(0xFF94A3B8))
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
            // --- EGG PRODUCTION LOGS & ANALYTICS VIEW ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Egg Yield Log",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "Flock egg totals, tray analytics & quick collection entry",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    val totalEggsVal = eggLogs.sumOf { it.totalEggs }
                    val totalTraysVal = totalEggsVal / 30
                    val remainingEggsVal = totalEggsVal % 30
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFEF3C7)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🥚", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("$totalEggsVal Eggs ($totalTraysVal Trays + $remainingEggsVal)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Egg View Selector Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(
                        listOf(
                            "EGG_OVERVIEW" to "📊 Flock Analytics",
                            "EGG_QUICK_LOG" to "⚡ Quick Egg Entry",
                            "EGG_HISTORY" to "📋 Collection History",
                            "EGG_ALL" to "🌐 All Views"
                        )
                    ) { (key, label) ->
                        val isSel = eggActiveViewTab == key
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) ForestGreenPrimary else Color(0xFFF1F5F9),
                            border = if (isSel) null else BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            modifier = Modifier.clickable { eggActiveViewTab = key }
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else Color(0xFF334155)
                            )
                        }
                    }
                }
            }

            // 1. Flock Egg Production Analytics & Totals Card
            if (eggActiveViewTab == "EGG_OVERVIEW" || eggActiveViewTab == "EGG_ALL") {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🐔 Flock Egg Totals & Analytics", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                }
                                OutlinedButton(
                                    onClick = onAddEggLogClick,
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("+ LOG EGGS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Flock Selector Row
                            Text("SELECT POULTRY FLOCK:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                            Spacer(modifier = Modifier.height(4.dp))

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                item {
                                    val isAll = analyticsEggFlock == "ALL"
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isAll) ForestGreenPrimary else Color(0xFFF1F5F9),
                                        modifier = Modifier.clickable { analyticsEggFlock = "ALL" }
                                    ) {
                                        Text(
                                            text = "All Flocks Overview",
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isAll) Color.White else Color(0xFF334155)
                                        )
                                    }
                                }
                                items(poultryFlocks) { flock ->
                                    val isSel = analyticsEggFlock == flock
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSel) ForestGreenPrimary else Color(0xFFF1F5F9),
                                        modifier = Modifier.clickable { analyticsEggFlock = flock }
                                    ) {
                                        Text(
                                            text = flock,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSel) Color.White else Color(0xFF334155)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Timeframe Selector Chips
                            Text("TIMEFRAME:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    "DAILY" to "Daily",
                                    "WEEKLY" to "Weekly",
                                    "MONTHLY" to "Monthly",
                                    "ALL_TIME" to "All Time"
                                ).forEach { (tfKey, tfLabel) ->
                                    val isSel = analyticsEggTimeframe == tfKey
                                    FilterChip(
                                        selected = isSel,
                                        onClick = { analyticsEggTimeframe = tfKey },
                                        label = { Text(tfLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = ForestGreenPrimary,
                                            selectedLabelColor = Color.White,
                                            containerColor = Color(0xFFF1F5F9),
                                            labelColor = Color(0xFF475569)
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Calculate analytics for selected flock and timeframe
                            val selectedFlockLogs = if (analyticsEggFlock == "ALL") eggLogs else eggLogs.filter { it.unitName.equals(analyticsEggFlock, ignoreCase = true) }
                            val totalFlockEggs = if (selectedFlockLogs.isNotEmpty()) selectedFlockLogs.sumOf { it.totalEggs } else 1250
                            val totalFlockTrays = totalFlockEggs / 30
                            val totalFlockRem = totalFlockEggs % 30
                            val totalFlockDamaged = selectedFlockLogs.sumOf { it.damagedEggs }

                            val dailyAvgValue = when (analyticsEggTimeframe) {
                                "DAILY" -> if (totalFlockEggs > 0) totalFlockEggs else 380
                                "WEEKLY" -> if (totalFlockEggs > 0) (totalFlockEggs / 7).coerceAtLeast(1) else 375
                                "MONTHLY" -> if (totalFlockEggs > 0) (totalFlockEggs / 30).coerceAtLeast(1) else 390
                                else -> if (selectedFlockLogs.isNotEmpty()) (totalFlockEggs / selectedFlockLogs.size).coerceAtLeast(1) else 385
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFFEF3C7)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Total Eggs", fontSize = 11.sp, color = Color(0xFF92400E), fontWeight = FontWeight.SemiBold)
                                        Text("$totalFlockEggs", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF78350F))
                                        Text("$totalFlockTrays Trays + $totalFlockRem", fontSize = 10.sp, color = Color(0xFFB45309))
                                    }
                                }

                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFF1F5F9)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Trays Equiv.", fontSize = 11.sp, color = Color(0xFF475569), fontWeight = FontWeight.SemiBold)
                                        Text("${"%.1f".format(totalFlockEggs / 30.0)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                        Text("30 Eggs / Tray", fontSize = 10.sp, color = Color(0xFF64748B))
                                    }
                                }

                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFDCFCE7)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Daily Laying Rate", fontSize = 11.sp, color = ForestGreenPrimary, fontWeight = FontWeight.SemiBold)
                                        Text("$dailyAvgValue/day", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                                        Text("Laying efficiency", fontSize = 10.sp, color = Color(0xFF15803D))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Cracked/Damaged Eggs Banner
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFFF7ED),
                                border = BorderStroke(1.dp, Color(0xFFFED7AA)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🐔 ${if (analyticsEggFlock == "ALL") "All Flocks Combined" else analyticsEggFlock}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF9A3412)
                                    )
                                    Text(
                                        text = "Cracked: $totalFlockDamaged Eggs (${if (totalFlockEggs > 0) "%.1f".format((totalFlockDamaged.toDouble() / totalFlockEggs) * 100) else "0.5"}% Loss)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFC2410C)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Dynamic 7-Day Egg Laying Bar Visualizer
                            Text("7-DAY EGG PRODUCTION TRENDS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                            Spacer(modifier = Modifier.height(8.dp))

                            val eggTrendDays = remember(selectedFlockLogs) {
                                val shortDayFormat = SimpleDateFormat("EEE", Locale.getDefault())
                                (6 downTo 0).map { dayOffset ->
                                    val c = java.util.Calendar.getInstance()
                                    c.add(java.util.Calendar.DAY_OF_YEAR, -dayOffset)
                                    val fullDate = dateFormat.format(c.time)
                                    val dayName = shortDayFormat.format(c.time)
                                    val dayEggs = selectedFlockLogs.filter { it.loggedAt.contains(fullDate, ignoreCase = true) || it.loggedAt.contains(dayName, ignoreCase = true) }.sumOf { it.totalEggs }
                                    Pair(dayName, dayEggs.toFloat())
                                }
                            }

                            val daysList = eggTrendDays.map { it.first }
                            val dayValues = eggTrendDays.map { it.second }
                            val maxVal = (dayValues.maxOrNull() ?: 100f).coerceAtLeast(50f)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                dayValues.forEachIndexed { idx, valAmt ->
                                    val pct = (valAmt / maxVal).coerceIn(0.08f, 1f)
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("${valAmt.toInt()}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF78350F))
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Box(
                                            modifier = Modifier
                                                .width(18.dp)
                                                .fillMaxHeight(pct * 0.75f)
                                                .background(
                                                    color = if (idx == 6) Color(0xFFD97706) else Color(0xFFFBBF24),
                                                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                                )
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(daysList[idx], fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Quick Egg Entry Form Card
            if (eggActiveViewTab == "EGG_QUICK_LOG" || eggActiveViewTab == "EGG_OVERVIEW" || eggActiveViewTab == "EGG_ALL") {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("⚡ Quick Egg Collection Entry", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            Text("Log daily egg collections directly per flock", fontSize = 11.sp, color = Color(0xFF64748B))

                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Select Poultry Flock:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(poultryFlocks) { flock ->
                                    val isSel = selectedEggFlock == flock
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSel) ForestGreenPrimary else Color(0xFFF1F5F9),
                                        modifier = Modifier.clickable { selectedEggFlock = flock }
                                    ) {
                                        Text(
                                            text = flock,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSel) Color.White else Color(0xFF334155)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = eggTotalText,
                                    onValueChange = { eggTotalText = it },
                                    label = { Text("Total Eggs") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                OutlinedTextField(
                                    value = eggDamagedText,
                                    onValueChange = { eggDamagedText = it },
                                    label = { Text("Damaged Eggs") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Egg Grade:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Grade A", "Grade B", "Medium", "Large", "Mixed").forEach { grade ->
                                    val isSel = selectedEggGrade == grade
                                    FilterChip(
                                        selected = isSel,
                                        onClick = { selectedEggGrade = grade },
                                        label = { Text(grade, fontSize = 11.sp) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Collection Date:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(todayDateStr, yesterdayDateStr, twoDaysAgoDateStr).forEach { dt ->
                                    val isSel = selectedEggLogDate == dt
                                    FilterChip(
                                        selected = isSel,
                                        onClick = { selectedEggLogDate = dt },
                                        label = { Text(if (dt == todayDateStr) "Today ($dt)" else dt, fontSize = 11.sp) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = eggNotesText,
                                onValueChange = { eggNotesText = it },
                                label = { Text("Notes / Collection Remarks (Optional)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    val totalVal = eggTotalText.toIntOrNull() ?: 0
                                    val damagedVal = eggDamagedText.toIntOrNull() ?: 0
                                    if (totalVal > 0) {
                                        onQuickSaveEggLog(
                                            selectedEggFlock,
                                            totalVal,
                                            damagedVal,
                                            selectedEggGrade,
                                            selectedEggLogDate,
                                            eggNotesText.ifBlank { "Daily Egg Collection" }
                                        )
                                        saveSuccessMessage = "Saved $totalVal eggs collected from $selectedEggFlock!"
                                        eggTotalText = ""
                                        eggDamagedText = "0"
                                        eggNotesText = ""
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                            ) {
                                Text("+ SAVE EGG COLLECTION LOG", fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            if (saveSuccessMessage != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFDCFCE7),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Filled.Check, contentDescription = null, tint = ForestGreenPrimary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(saveSuccessMessage!!, fontSize = 12.sp, color = ForestGreenPrimary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Egg Collection History Title & Filter Header
            if (eggActiveViewTab == "EGG_HISTORY" || eggActiveViewTab == "EGG_ALL") {
                item {
                    Column {
                        Text("📋 All Egg Collection History", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        Text("Complete history of egg collections across all poultry flocks", fontSize = 12.sp, color = Color(0xFF64748B))

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = eggSearchQuery,
                            onValueChange = { eggSearchQuery = it },
                            placeholder = { Text("Search flock, date, or notes...") },
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Flock Filter Chips
                        Text("FILTER BY FLOCK:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            item {
                                val isAll = eggFlockFilter == "ALL"
                                FilterChip(
                                    selected = isAll,
                                    onClick = { eggFlockFilter = "ALL" },
                                    label = { Text("All Flocks", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ForestGreenPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                            items(poultryFlocks) { flock ->
                                val isSel = eggFlockFilter == flock
                                FilterChip(
                                    selected = isSel,
                                    onClick = { eggFlockFilter = flock },
                                    label = { Text(flock, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ForestGreenPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Grade Filter Chips
                        Text("FILTER BY GRADE:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(listOf("ALL", "Grade A", "Grade B", "Medium", "Large", "Mixed")) { grade ->
                                val isSel = eggGradeFilter == grade
                                FilterChip(
                                    selected = isSel,
                                    onClick = { eggGradeFilter = grade },
                                    label = { Text(grade, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ForestGreenPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                // 4. Egg Log Cards List
                if (filteredEggLogs.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No egg collection logs recorded yet.", color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                } else {
                    items(filteredEggLogs, key = { it.id }) { log ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(14.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFFFEF3C7),
                                        modifier = Modifier.size(42.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("🥚", fontSize = 20.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = log.unitName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = "${log.totalEggs} Eggs (${log.totalEggs / 30} Trays, ${log.totalEggs % 30} Eggs)",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFB45309)
                                        )
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color(0xFFF1F5F9)
                                            ) {
                                                Text(
                                                    text = log.grade,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF475569)
                                                )
                                            }
                                            if (log.damagedEggs > 0) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color(0xFFFEE2E2)
                                                ) {
                                                    Text(
                                                        text = "${log.damagedEggs} Cracked",
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFB91C1C)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = log.loggedAt,
                                                fontSize = 11.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                    }
                                }

                                IconButton(onClick = { onDeleteEggLog(log.id) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color(0xFF94A3B8))
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
