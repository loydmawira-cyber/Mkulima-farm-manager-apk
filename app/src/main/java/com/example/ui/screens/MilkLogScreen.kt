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

/**
 * Custom Compose Canvas Line Chart for Milk Production Trends
 */
@Composable
fun MilkProductionLineChart(
    dataPoints: List<Float>,
    xLabels: List<String>,
    modifier: Modifier = Modifier
) {
    if (dataPoints.isEmpty()) return

    val minVal = dataPoints.minOrNull() ?: 0f
    val maxVal = dataPoints.maxOrNull() ?: 100f
    val range = if (maxVal == minVal) 1f else (maxVal - minVal)

    val lineColor = ForestGreenPrimary
    val gradientColor = ForestGreenPrimary.copy(alpha = 0.15f)

    Column(modifier = modifier) {
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .weight(1f)) {
            val width = size.width
            val height = size.height

            // Calculate coordinates
            val spacingX = width / (dataPoints.size - 1)
            val points = dataPoints.mapIndexed { idx, value ->
                val x = idx * spacingX
                val normalizedY = (value - minVal) / range
                val y = height - (normalizedY * (height * 0.75f) + height * 0.1f)
                Offset(x, y)
            }

            // Grid Lines (Horizontal)
            val gridStep = height / 3
            for (i in 1..3) {
                drawLine(
                    color = Color(0xFFE2E8F0),
                    start = Offset(0f, i * gridStep),
                    end = Offset(width, i * gridStep),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Build Curved Path
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

            // Draw Line
            drawPath(
                path = strokePath,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw Dots
            points.forEach { point ->
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = lineColor,
                    radius = 3.dp.toPx(),
                    center = point
                )
            }
        }

        // X Labels Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            xLabels.forEach { label ->
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B)
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

            // --- 2. OVERALL HERD TOTALS & LINE CHART ANALYTICS ---
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

                            // Totals Calculation based on overallTimeframe
                            val summary = when (overallTimeframe) {
                                "DAILY" -> MilkTotalsSummary("245.8 L", "245.8 L/day", "Bessie (#102) - 28.0L", "+4.2% vs yesterday")
                                "PREV_MONTH" -> MilkTotalsSummary("6,950 L", "231.6 L/day", "Mimi (#115) - 820L", "+2.8% MoM")
                                "SIX_MONTHS" -> MilkTotalsSummary("42,800 L", "237.7 L/day", "Bella (#112) - 4,800L", "+12.4% YoY")
                                "YEAR" -> MilkTotalsSummary("88,400 L", "242.2 L/day", "Bessie (#102) - 9,600L", "Peak Herd Record")
                                else -> MilkTotalsSummary("7,450 L", "248.3 L/day", "Bessie (#102) - 860L", "+7.1% vs prev month")
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

                            // Line Chart Component
                            val chartData = when (overallTimeframe) {
                                "DAILY" -> listOf(30f, 45f, 60f, 85f, 70f, 95f, 110f, 125f)
                                "PREV_MONTH" -> listOf(210f, 220f, 215f, 230f, 240f, 225f, 235f, 245f)
                                "SIX_MONTHS" -> listOf(6200f, 6800f, 7100f, 6900f, 7300f, 7450f)
                                "YEAR" -> listOf(7000f, 7200f, 7100f, 7300f, 7500f, 7400f, 7600f, 7800f, 7900f, 7700f, 8000f, 8200f)
                                else -> listOf(220f, 235f, 228f, 242f, 250f, 245f, 258f, 260f)
                            }

                            val xLabels = when (overallTimeframe) {
                                "DAILY" -> listOf("6am", "8am", "10am", "12pm", "2pm", "4pm", "6pm", "8pm")
                                "SIX_MONTHS" -> listOf("Mar", "Apr", "May", "Jun", "Jul", "Aug")
                                "YEAR" -> listOf("Jan", "Mar", "May", "Jul", "Sep", "Nov")
                                else -> listOf("1st", "5th", "10th", "15th", "20th", "25th", "30th")
                            }

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

                            // Step 2: Session Selection
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

                            // Step 3: Record Date
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

                            // Step 4: Enter Milk Details
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

                            val (cowLitres, cowAvg, cowSessions) = when (perCowTimeframe) {
                                "TODAY" -> Triple("28.0 L", "28.0 L/day", "AM: 14.0L | PM: 14.0L")
                                "WEEKLY" -> Triple("192.5 L", "27.5 L/day", "7 Days Logged")
                                "MONTHLY" -> Triple("825.0 L", "27.5 L/day", "30 Days Logged")
                                else -> Triple("3,360.0 L", "28.0 L/day", "Lactation Day ${perCowSelectedCow.lactationDay}")
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
                                                text = cowLitres,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ForestGreenPrimary
                                            )
                                            Text(
                                                text = cowAvg,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF475569)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFDCFCE7)
                                    ) {
                                        Text(
                                            text = cowSessions,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ForestGreenPrimary
                                        )
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

                            Text("7-DAY EGG PRODUCTION TRENDS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                            Spacer(modifier = Modifier.height(8.dp))

                            val daysList = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                            val dayValues = listOf(380f, 410f, 395f, 420f, 388f, 405f, 390f)
                            val maxVal = dayValues.maxOrNull() ?: 450f

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                dayValues.forEachIndexed { idx, valAmt ->
                                    val pct = valAmt / maxVal
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
                                                    color = if (idx == 3) Color(0xFFD97706) else Color(0xFFFBBF24),
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
