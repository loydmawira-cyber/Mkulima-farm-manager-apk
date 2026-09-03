package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EggLog
import com.example.data.MilkLog
import com.example.data.MilkLogEntryRules
import com.example.data.MilkUsageLog
import com.example.ui.components.AppDatePickerField
import com.example.ui.theme.ForestGreenPrimary
import com.example.util.CattleLifecycleEngine
import com.example.util.CattleStage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

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

data class CowDayMilkBreakdown(
    val dateKey: String,
    val displayDate: String,
    val dayOfWeek: String,
    val dayOfMonth: Int,
    val morningYield: Double,
    val morningCount: Int,
    val afternoonYield: Double,
    val afternoonCount: Int,
    val eveningYield: Double,
    val eveningCount: Int,
    val dailyTotal: Double
)

fun isMilkingCow(
    name: String,
    breed: String = "",
    status: String = "",
    tag: String = "",
    lastMilk: String = "",
    breedingStatus: String = ""
): Boolean {
    val s = status.uppercase()
    val b = breed.uppercase()
    val n = name.uppercase()
    val bs = breedingStatus.uppercase()
    val lm = lastMilk.uppercase().trim()

    // 1. Disqualify disposed, sold, dead, culled
    if (s.contains("DISPOSED") || s.contains("SOLD") || s.contains("DEAD") || s.contains("CULLED") || bs.contains("DISPOSED")) return false

    // 2. Disqualify bulls, sires, steers, studs
    if (s.contains("BULL") || b.contains("BULL") || n.contains("BULL") || s.contains("SIRE") || b.contains("SIRE") || bs.contains("BULL") || bs.contains("SIRE") || s.contains("STEER")) return false

    // 3. Disqualify young calves and weaners
    if (s.contains("CALF") || b.contains("CALF") || n.contains("CALF") || bs.contains("CALF") || s.contains("WEAN") || bs.contains("WEAN")) return false

    // 4. Disqualify heifers
    if (s.contains("HEIFER") || b.contains("HEIFER") || n.contains("HEIFER") || bs.contains("HEIFER")) return false

    // 5. Disqualify dry cows
    if (s.contains("DRY") || bs.contains("DRY")) return false

    // 6. Disqualify poultry, crops, greenhouses
    if (b.contains("POULTRY") || b.contains("LAYER") || b.contains("BROILER") || n.contains("FLOCK") || b.contains("KIENYEJI") ||
        b.contains("GREENHOUSE") || b.contains("FIELD") || b.contains("PLOT") ||
        s.contains("POULTRY") || s.contains("FLOCK")) return false

    // 7. Explicitly qualify milking & incalf&milking stages
    if (s.contains("MILKING") || bs.contains("MILKING") || s == "INCALF_MILKING" || bs.contains("IN-CALF & MILKING") || bs.contains("INCALF & MILKING") || bs.contains("INCALF_MILKING")) {
        return true
    }

    // 8. If status is PREGNANT / INCALF, qualify only if lactating/milking (e.g. lastMilk has positive yield)
    if (s.contains("PREGNANT") || s.contains("INCALF") || bs.contains("PREGNANT") || bs.contains("IN-CALF")) {
        if (lm == "0.0L" || lm == "0L" || lm == "N/A" || lm == "0") return false
        return true
    }

    // 9. If status is ACTIVE or general cow with positive milk
    if (s == "ACTIVE" || s.isEmpty()) {
        if (lm == "0.0L" || lm == "0L" || lm == "N/A" || lm == "0") return false
        return true
    }

    return false
}

fun isLogForCow(log: MilkLog, cow: AnimalCowItem): Boolean {
    val cleanLog = log.cowName.trim().lowercase()
    val cleanCow = cow.name.trim().lowercase()

    if (cleanLog == cleanCow) return true

    val logBase = cleanLog.substringBefore(" (").substringBefore(" -").substringBefore("#").trim()
    val cowBase = cleanCow.substringBefore(" (").substringBefore(" -").substringBefore("#").trim()

    if (logBase.isNotEmpty() && cowBase.isNotEmpty() && (logBase == cowBase || cleanLog.contains(cowBase) || cleanCow.contains(logBase))) {
        return true
    }

    val tag = cow.tagId.lowercase().replace("#", "").trim()
    if (tag.isNotEmpty() && (cleanLog.contains(tag) || cleanLog.contains("#$tag"))) {
        return true
    }

    return false
}

fun parseMilkLogCalendar(dateStr: String): java.util.Calendar? {
    val clean = dateStr.trim()
    val formats = arrayOf(
        "dd MMM yyyy",
        "d MMM yyyy",
        "yyyy-MM-dd",
        "dd/MM/yyyy",
        "dd-MM-yyyy",
        "dd MMM, hh:mm a",
        "dd MMM"
    )
    for (fmt in formats) {
        try {
            val sdf = SimpleDateFormat(fmt, Locale.getDefault())
            val parsed = sdf.parse(clean)
            if (parsed != null) {
                val cal = java.util.Calendar.getInstance().apply { time = parsed }
                if (cal.get(java.util.Calendar.YEAR) < 2000) {
                    cal.set(java.util.Calendar.YEAR, java.util.Calendar.getInstance().get(java.util.Calendar.YEAR))
                }
                return cal
            }
        } catch (_: Exception) {}
    }
    return null
}

/**
 * Custom Compose Canvas Line Chart for Milk Production Trends with Dynamic Data,
 * Horizontal Scroll for Multi-day views, Clean Bezier Curves, and Interactive Tap Tooltips.
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
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF8FAFC),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Filled.WaterDrop,
                    contentDescription = null,
                    tint = ForestGreenPrimary.copy(alpha = 0.5f),
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No milk records logged for this timeframe",
                    color = Color(0xFF64748B),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
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

    var selectedIndex by remember(dataPoints, xLabels) { mutableStateOf<Int?>(null) }
    val maxVal = (dataPoints.maxOrNull() ?: 10f).coerceAtLeast(1f)
    val minVal = (dataPoints.minOrNull() ?: 0f).coerceAtLeast(0f)
    val displayMax = if (maxVal == minVal) maxVal * 1.3f else maxVal * 1.25f
    val displayMin = 0f
    val range = (displayMax - displayMin).coerceAtLeast(1f)

    val lineColor = ForestGreenPrimary
    val gradientTop = ForestGreenPrimary.copy(alpha = 0.28f)
    val gradientBottom = ForestGreenPrimary.copy(alpha = 0.02f)

    val isDense = dataPoints.size > 7
    val pointSpacingDp = if (isDense) 52.dp else 0.dp

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFFFFFF))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp, horizontal = 8.dp)
    ) {
        // Selected Item Banner / Summary if tapped
        val activeIndex = selectedIndex
        if (activeIndex != null && activeIndex in dataPoints.indices) {
            val idx = activeIndex
            val label = xLabels.getOrElse(idx) { "Day ${idx + 1}" }
            val yieldVal = dataPoints[idx]
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFECFDF5),
                border = BorderStroke(1.dp, Color(0xFFA7F3D0))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF065F46)
                    )
                    Text(
                        text = "Yield: %.1f%s".format(yieldVal, unitSuffix),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ForestGreenPrimary
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (isDense) {
                // Scrollable container for multi-day views (e.g. 30-day Month view)
                val scrollState = rememberScrollState()
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(scrollState)
                ) {
                    val canvasWidth = (dataPoints.size * 56).dp.coerceAtLeast(320.dp)
                    MilkProductionCanvas(
                        dataPoints = dataPoints,
                        xLabels = xLabels,
                        displayMax = displayMax,
                        displayMin = displayMin,
                        range = range,
                        lineColor = lineColor,
                        gradientTop = gradientTop,
                        gradientBottom = gradientBottom,
                        unitSuffix = unitSuffix,
                        selectedIndex = selectedIndex,
                        onPointSelected = { selectedIndex = it },
                        modifier = Modifier
                            .width(canvasWidth)
                            .fillMaxHeight()
                    )
                }
            } else {
                // Static Responsive Chart for 7-day or 3-session view
                MilkProductionCanvas(
                    dataPoints = dataPoints,
                    xLabels = xLabels,
                    displayMax = displayMax,
                    displayMin = displayMin,
                    range = range,
                    lineColor = lineColor,
                    gradientTop = gradientTop,
                    gradientBottom = gradientBottom,
                    unitSuffix = unitSuffix,
                    selectedIndex = selectedIndex,
                    onPointSelected = { selectedIndex = it },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun MilkProductionCanvas(
    dataPoints: List<Float>,
    xLabels: List<String>,
    displayMax: Float,
    displayMin: Float,
    range: Float,
    lineColor: Color,
    gradientTop: Color,
    gradientBottom: Color,
    unitSuffix: String,
    selectedIndex: Int?,
    onPointSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val valueTextPaint = remember {
        android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#15803D")
            textSize = 26f
            isFakeBoldText = true
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
    }
    val gridTextPaint = remember {
        android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#94A3B8")
            textSize = 20f
            textAlign = android.graphics.Paint.Align.LEFT
            isAntiAlias = true
        }
    }
    val xLabelTextPaint = remember {
        android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#475569")
            textSize = 22f
            isFakeBoldText = true
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
    }

    val selectedGlowColor = ForestGreenPrimary.copy(alpha = 0.25f)

    Canvas(
        modifier = modifier
            .padding(horizontal = 8.dp)
            .pointerInput(dataPoints) {
                detectTapGestures { offset ->
                    val width = size.width.toFloat()
                    val paddingLeft = 36.dp.toPx()
                    val paddingRight = 24.dp.toPx()
                    val plotWidth = width - paddingLeft - paddingRight
                    val pointCount = dataPoints.size
                    if (pointCount > 1) {
                        val spacing = plotWidth / (pointCount - 1)
                        val touchX = offset.x - paddingLeft
                        val rawIndex = (touchX / spacing).toInt()
                        val remainder = (touchX % spacing) / spacing
                        val nearestIndex = if (remainder > 0.5f) rawIndex + 1 else rawIndex
                        val clampedIndex = nearestIndex.coerceIn(0, pointCount - 1)
                        onPointSelected(clampedIndex)
                    } else if (pointCount == 1) {
                        onPointSelected(0)
                    }
                }
            }
    ) {
        val width = size.width
        val height = size.height
        val paddingLeft = 36.dp.toPx()
        val paddingRight = 24.dp.toPx()
        val paddingTop = 28.dp.toPx()
        val paddingBottom = 28.dp.toPx()
        val plotWidth = width - paddingLeft - paddingRight
        val plotHeight = height - paddingTop - paddingBottom

        val pointCount = dataPoints.size
        if (pointCount == 0) return@Canvas

        val spacing = if (pointCount > 1) plotWidth / (pointCount - 1) else 0f

        // Draw 3 horizontal grid lines (Max, Mid, Min)
        val gridSteps = 3
        for (i in 0..gridSteps) {
            val ratio = i.toFloat() / gridSteps
            val y = paddingTop + plotHeight * ratio
            val gridVal = displayMax - (range * ratio)

            drawLine(
                color = Color(0xFFE2E8F0),
                start = Offset(paddingLeft, y),
                end = Offset(width - paddingRight, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
            )

            // Y-Axis Label on the left
            val labelStr = if (gridVal % 1.0f == 0f) "%.0f".format(gridVal) else "%.1f".format(gridVal)
            drawContext.canvas.nativeCanvas.drawText(
                labelStr,
                4.dp.toPx(),
                y + 4.dp.toPx(),
                gridTextPaint
            )
        }

        // Calculate points
        val points = dataPoints.mapIndexed { idx, value ->
            val x = if (pointCount == 1) paddingLeft + plotWidth / 2f else paddingLeft + idx * spacing
            val normalizedY = if (range > 0f) (value - displayMin) / range else 0.5f
            val y = paddingTop + plotHeight * (1f - normalizedY)
            Offset(x, y)
        }

        // Area Gradient under line
        val fillPath = Path().apply {
            moveTo(points.first().x, paddingTop + plotHeight)
            points.forEach { lineTo(it.x, it.y) }
            lineTo(points.last().x, paddingTop + plotHeight)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(gradientTop, gradientBottom),
                startY = paddingTop,
                endY = paddingTop + plotHeight
            )
        )

        // Plot the connecting line
        val strokePath = Path().apply {
            points.forEachIndexed { idx, point ->
                if (idx == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
            }
        }
        drawPath(
            path = strokePath,
            color = lineColor,
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Draw points and values
        points.forEachIndexed { idx, point ->
            val isSelected = selectedIndex == idx
            val valAmt = dataPoints[idx]

            if (isSelected) {
                // Glow ring for selected point
                drawCircle(
                    color = selectedGlowColor,
                    radius = 12.dp.toPx(),
                    center = point
                )
            }

            drawCircle(
                color = Color.White,
                radius = if (isSelected) 7.dp.toPx() else 5.dp.toPx(),
                center = point
            )
            drawCircle(
                color = if (isSelected) Color(0xFF065F46) else lineColor,
                radius = if (isSelected) 5.dp.toPx() else 3.5.dp.toPx(),
                center = point
            )

            // Numeric Value Label above dot (render if selected or not overly crowded)
            if (isSelected || pointCount <= 10 || (pointCount <= 14 && idx % 2 == 0)) {
                val formattedVal = if (valAmt % 1.0f == 0f) "%.0f%s".format(valAmt, unitSuffix) else "%.1f%s".format(valAmt, unitSuffix)
                drawContext.canvas.nativeCanvas.drawText(
                    formattedVal,
                    point.x,
                    (point.y - 8.dp.toPx()).coerceAtLeast(16.dp.toPx()),
                    valueTextPaint
                )
            }

            // X-Axis Day Label below dot
            val label = xLabels.getOrElse(idx) { (idx + 1).toString() }
            drawContext.canvas.nativeCanvas.drawText(
                label,
                point.x,
                height - 4.dp.toPx(),
                xLabelTextPaint
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MilkLogScreen(
    milkLogs: List<MilkLog>,
    milkUsageLogs: List<MilkUsageLog> = emptyList(),
    eggLogs: List<EggLog> = emptyList(),
    units: List<com.example.data.FarmUnit> = emptyList(),
    onAddMilkLogClick: () -> Unit,
    onAddEggLogClick: () -> Unit = {},
    onQuickSaveMilkLog: (cowName: String, litres: Double, session: String, date: String, onResult: (Boolean, String?) -> Unit) -> Unit = { _, _, _, _, _ -> },
    onSaveMilkUsageLog: (date: String, session: String, litresToCooperative: Double, litresHomeUse: Double, litresToCalves: Double, onResult: (Boolean, String?) -> Unit) -> Unit = { _, _, _, _, _, _ -> },
    onEditMilkUsageLog: (id: Long, litresToCooperative: Double, litresHomeUse: Double, litresToCalves: Double, onResult: (Boolean, String?) -> Unit) -> Unit = { _, _, _, _, _ -> },
    onDeleteMilkUsageLog: (Long) -> Unit = {},
    onQuickSaveEggLog: (flockName: String, totalEggs: Int, damagedEggs: Int, grade: String, date: String, notes: String?) -> Unit = { _, _, _, _, _, _ -> },
    onDeleteMilkLog: (Long) -> Unit,
    onDeleteEggLog: (Long) -> Unit,
    farmSettings: com.example.data.FarmSettings,
    userRole: String = "OWNER",
    canEditLogs: Boolean = true,
    canEditPastDaysLogs: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val deletedPrefs = remember { context.getSharedPreferences("mkulima_deleted_animals", android.content.Context.MODE_PRIVATE) }
    val deletedSet = remember {
        try {
            deletedPrefs.getStringSet("deleted_ids", emptySet()) ?: emptySet()
        } catch (e: Exception) {
            val raw = try { deletedPrefs.getString("deleted_ids", "") ?: "" } catch (ex: Exception) { "" }
            if (raw.isNotBlank()) raw.split(",").toSet() else emptySet()
        }
    }

    // Registered Milking Cow Database dynamically built strictly from active farm livestock list (Room units + mockAnimals)
    val cowsList = remember(units, deletedSet) {
        val result = mutableListOf<AnimalCowItem>()

        // 1. From Room units (registered farm livestock)
        units.filter {
            (it.type.equals("Cattle", ignoreCase = true) || it.type.equals("CATTLE", ignoreCase = true)) &&
            !deletedSet.contains("unit_${it.id}") && !deletedSet.contains(it.name.lowercase()) &&
            isMilkingCow(
                name = it.name,
                breed = it.breed,
                status = it.healthStatus,
                tag = it.tagNumber,
                lastMilk = it.currentWeight
            )
        }.forEach { unit ->
            val tag = unit.tagNumber.ifBlank { "#${unit.id + 100}" }
            val displayName = if (unit.name.contains(tag)) unit.name else "${unit.name} ($tag)"
            result.add(
                AnimalCowItem(
                    tagId = tag,
                    name = displayName,
                    breed = unit.breed.ifBlank { "Dairy Cattle" },
                    lactationDay = 90
                )
            )
        }

        // 2. From mockAnimals (registered farm livestock list)
        mockAnimals.filter {
            it.category.equals("CATTLE", ignoreCase = true) &&
            !deletedSet.contains(it.id) && !deletedSet.contains(it.name.lowercase()) &&
            isMilkingCow(
                name = it.name,
                breed = it.breed,
                status = it.status,
                tag = it.tagNumber,
                lastMilk = it.lastMilk,
                breedingStatus = it.breedingStatus
            )
        }.forEach { animal ->
            val tag = animal.tagNumber.ifBlank { "#100" }
            val displayName = if (animal.name.contains(tag)) animal.name else "${animal.name} ($tag)"
            result.add(
                AnimalCowItem(
                    tagId = tag,
                    name = displayName,
                    breed = animal.breed.ifBlank { "Dairy Cattle" },
                    lactationDay = 90
                )
            )
        }

        // Deduplicate by normalized base name
        val distinct = result.distinctBy {
            it.name.substringBefore(" (").substringBefore(" -").substringBefore("#").trim().lowercase()
        }
        distinct
    }

    // --- MAIN LOG CATEGORY STATE (Milk vs Eggs) ---
    val initialMainCategory = if (farmSettings.farmType.equals("Poultry Only", ignoreCase = true)) "EGGS" else "MILK"
    var selectedMainLogCategory by remember(farmSettings.farmType) { mutableStateOf(initialMainCategory) }

    LaunchedEffect(farmSettings.farmType) {
        if (farmSettings.farmType.equals("Poultry Only", ignoreCase = true)) {
            selectedMainLogCategory = "EGGS"
        } else if (farmSettings.farmType.equals("Cattle Only", ignoreCase = true)) {
            selectedMainLogCategory = "MILK"
        }
    }

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
    var milkCalendarDayKey by remember { mutableStateOf(dateFormat.format(Date())) }
    LaunchedEffect(Unit) {
        while (true) {
            val now = java.util.Calendar.getInstance()
            val nextMidnight = (now.clone() as java.util.Calendar).apply {
                add(java.util.Calendar.DAY_OF_YEAR, 1)
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            delay((nextMidnight.timeInMillis - System.currentTimeMillis()).coerceAtLeast(1_000L))
            milkCalendarDayKey = dateFormat.format(Date())
        }
    }
    val todayDateStr = remember(milkCalendarDayKey) { dateFormat.format(Date()) }
    val yesterdayDateStr = remember(milkCalendarDayKey) {
        val cal = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, -1) }
        dateFormat.format(cal.time)
    }
    val twoDaysAgoDateStr = remember(milkCalendarDayKey) {
        val cal = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, -2) }
        dateFormat.format(cal.time)
    }
    var selectedLogDate by remember(milkCalendarDayKey) { mutableStateOf(todayDateStr) }
    var saveSuccessMessage by remember { mutableStateOf<String?>(null) }
    var saveErrorMessage by remember { mutableStateOf<String?>(null) }

    val isOwner = userRole.equals("OWNER", ignoreCase = true)
    val cannotEditPast = !isOwner && !canEditPastDaysLogs
    val selectedDateIsFuture = MilkLogEntryRules.isFutureDate(selectedLogDate)
    val selectedDateIsPastRestricted = cannotEditPast && com.example.util.DateValidationUtils.isPastDate(selectedLogDate)
    val selectedDateIsValid = MilkLogEntryRules.canonicalDateKey(selectedLogDate) != null
    val selectedMilkSlotRecorded = selectedCow?.let { cow ->
        milkLogs.any { log -> MilkLogEntryRules.isSameSlot(log, cow.name, selectedLogDate, selectedSession) }
    } ?: false

    // --- PER COW ANALYTICS STATE ---
    var perCowSelectedCow by remember { mutableStateOf<AnimalCowItem>(cowsList.firstOrNull() ?: AnimalCowItem("#102", "Unknown", "Friesian", 120)) }

    LaunchedEffect(cowsList) {
        if (cowsList.isNotEmpty()) {
            if (selectedCow == null || cowsList.none { it.tagId == selectedCow?.tagId || it.name == selectedCow?.name }) {
                selectedCow = cowsList.first()
            }
            if (perCowSelectedCow.name == "Unknown" || cowsList.none { it.tagId == perCowSelectedCow.tagId || it.name == perCowSelectedCow.name }) {
                perCowSelectedCow = cowsList.first()
            }
        }
    }
    val currentCal = remember { java.util.Calendar.getInstance() }
    val defaultMonthName = remember { SimpleDateFormat("MMMM", Locale.getDefault()).format(currentCal.time) } // e.g. "August"
    val defaultYearName = remember { SimpleDateFormat("yyyy", Locale.getDefault()).format(currentCal.time) } // e.g. "2026"

    val monthsList = remember {
        listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
    }
    val currentYearNum = remember { currentCal.get(java.util.Calendar.YEAR) }
    val yearsList = remember { ((currentYearNum - 5)..(currentYearNum)).map { it.toString() } } // 6 years (e.g. 2021..2026)

    var perCowTimeframe by remember { mutableStateOf("TODAY") } // TODAY, MONTH, YEAR
    var selectedPerCowMonth by remember { mutableStateOf(defaultMonthName) }
    var selectedPerCowYear by remember { mutableStateOf(defaultYearName) }
    var isMonthMenuExpanded by remember { mutableStateOf(false) }
    var isYearMenuExpanded by remember { mutableStateOf(false) }

    // --- OVERALL TOTALS & CHART STATE ---
    var overallTimeframe by remember { mutableStateOf("TODAY") } // TODAY, MONTH, YEAR
    var selectedHerdMonth by remember { mutableStateOf(defaultMonthName) }
    var selectedHerdYear by remember { mutableStateOf(defaultYearName) }
    var isHerdMonthMenuExpanded by remember { mutableStateOf(false) }
    var isHerdYearMenuExpanded by remember { mutableStateOf(false) }

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

    LaunchedEffect(cowsList) {
        if (selectedCow == null || cowsList.none { it.tagId == selectedCow?.tagId || it.name == selectedCow?.name }) {
            selectedCow = cowsList.firstOrNull()
        }
        if (cowsList.isNotEmpty() && cowsList.none { it.tagId == perCowSelectedCow.tagId || it.name == perCowSelectedCow.name }) {
            cowsList.firstOrNull()?.let { perCowSelectedCow = it }
        }
    }

    // --- EGG LOG STATE ---
    val poultryFlocks = remember(units) {
        units.filter { unit ->
            !unit.isDeleted && (
                unit.type.equals("Poultry", ignoreCase = true) ||
                unit.type.equals("Flock", ignoreCase = true) ||
                unit.breed.contains("Layer", ignoreCase = true) ||
                unit.breed.contains("Flock", ignoreCase = true)
            )
        }.map { it.name }.distinct().sorted()
    }
    var selectedEggFlock by remember { mutableStateOf(poultryFlocks.firstOrNull().orEmpty()) }
    LaunchedEffect(poultryFlocks) {
        if (selectedEggFlock !in poultryFlocks) selectedEggFlock = poultryFlocks.firstOrNull().orEmpty()
    }
    var eggTotalText by remember { mutableStateOf("") }
    var eggDamagedText by remember { mutableStateOf("0") }
    var selectedEggGrade by remember { mutableStateOf("Grade A") }
    var selectedEggLogDate by remember { mutableStateOf(todayDateStr) }
    var eggNotesText by remember { mutableStateOf("") }
    var eggActiveViewTab by remember { mutableStateOf("EGG_OVERVIEW") } // "EGG_OVERVIEW", "EGG_QUICK_LOG", "EGG_HISTORY", "EGG_ALL"
    var eggSearchQuery by remember { mutableStateOf("") }
    var eggGradeFilter by remember { mutableStateOf("ALL") }
    var eggFlockFilter by remember { mutableStateOf("ALL") }
    var analyticsEggFlock by remember { mutableStateOf("ALL") }
    var analyticsEggTimeframe by remember { mutableStateOf("TODAY") } // TODAY, MONTH, YEAR
    var analyticsEggMonth by remember { mutableStateOf(defaultMonthName) }
    var analyticsEggYear by remember { mutableStateOf(defaultYearName) }
    var isEggMonthMenuExpanded by remember { mutableStateOf(false) }
    var isEggYearMenuExpanded by remember { mutableStateOf(false) }
    val eggAnalyticsYears = remember(currentYearNum) { (currentYearNum downTo (currentYearNum - 3)).map { it.toString() } }

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
        if (farmSettings.farmType.equals("Both", ignoreCase = true)) {
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
                            text = "Milk Production",
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
                            "HERD_TOTALS" to "📊 Overview",
                            "QUICK_LOG" to "⚡ Quick Entry",
                            "PER_COW" to "🐄 Per Cow Stats",
                            "HISTORY" to "📋 Log History"
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

                        // Timeframe Tabs for Herd Overview: Today, Month, Year
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 1. TODAY TAB
                            val isTodaySelected = overallTimeframe == "TODAY"
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isTodaySelected) ForestGreenPrimary else Color(0xFFF1F5F9),
                                border = if (isTodaySelected) null else BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { overallTimeframe = "TODAY" }
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
                            val isMonthSelected = overallTimeframe == "MONTH"
                            Box(modifier = Modifier.weight(1.1f)) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isMonthSelected) ForestGreenPrimary else Color(0xFFF1F5F9),
                                    border = if (isMonthSelected) null else BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            overallTimeframe = "MONTH"
                                            isHerdMonthMenuExpanded = true
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = selectedHerdMonth,
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
                                    expanded = isHerdMonthMenuExpanded,
                                    onDismissRequest = { isHerdMonthMenuExpanded = false },
                                    modifier = Modifier.background(Color.White)
                                ) {
                                    monthsList.forEach { month ->
                                        val isCurrent = month.equals(selectedHerdMonth, ignoreCase = true)
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
                                                selectedHerdMonth = month
                                                overallTimeframe = "MONTH"
                                                isHerdMonthMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // 3. YEAR TAB
                            val isYearSelected = overallTimeframe == "YEAR"
                            Box(modifier = Modifier.weight(1.1f)) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isYearSelected) ForestGreenPrimary else Color(0xFFF1F5F9),
                                    border = if (isYearSelected) null else BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            overallTimeframe = "YEAR"
                                            isHerdYearMenuExpanded = true
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = selectedHerdYear,
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
                                    expanded = isHerdYearMenuExpanded,
                                    onDismissRequest = { isHerdYearMenuExpanded = false },
                                    modifier = Modifier.background(Color.White)
                                ) {
                                    yearsList.forEach { yr ->
                                        val isCurrent = yr == selectedHerdYear
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
                                                selectedHerdYear = yr
                                                overallTimeframe = "YEAR"
                                                isHerdYearMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Dynamic Totals and Line Chart Calculation based on overallTimeframe, selectedHerdMonth, and selectedHerdYear
                        val (summary, chartData, xLabels) = remember(milkLogs, overallTimeframe, selectedHerdMonth, selectedHerdYear, milkCalendarDayKey) {
                            if (milkLogs.isEmpty()) {
                                Triple(
                                    MilkTotalsSummary("0.0 L", "0.0 L/day", "No records", "0 logs recorded"),
                                    emptyList<Float>(),
                                    listOf("No Data")
                                )
                            } else {
                                val nowCal = java.util.Calendar.getInstance()
                                val cMonth = nowCal.get(java.util.Calendar.MONTH)
                                val cYear = nowCal.get(java.util.Calendar.YEAR)
                                val cDayOfYear = nowCal.get(java.util.Calendar.DAY_OF_YEAR)
                                
                                val targetMonthIdx = monthsList.indexOfFirst { it.equals(selectedHerdMonth, ignoreCase = true) }
                                val targetYearInt = selectedHerdYear.toIntOrNull() ?: cYear
                                val shortMonthLabel = if (targetMonthIdx >= 0) {
                                    val cal = java.util.Calendar.getInstance().apply { set(java.util.Calendar.MONTH, targetMonthIdx) }
                                    java.text.SimpleDateFormat("MMM", java.util.Locale.getDefault()).format(cal.time)
                                } else selectedHerdMonth.take(3)

                                when (overallTimeframe) {
                                    "TODAY" -> {
                                        val todayLogs = milkLogs.filter { log ->
                                            val c = parseMilkLogCalendar(log.date)
                                            (c != null && c.get(java.util.Calendar.YEAR) == cYear && c.get(java.util.Calendar.DAY_OF_YEAR) == cDayOfYear) ||
                                                    log.date.equals(todayDateStr, ignoreCase = true) ||
                                                    log.date.contains("Today", ignoreCase = true)
                                        }
                                        val targetLogs = if (todayLogs.isNotEmpty()) todayLogs else {
                                            val latestDate = milkLogs.firstOrNull()?.date ?: todayDateStr
                                            milkLogs.filter { it.date == latestDate }
                                        }
                                        val morningLogs = targetLogs.filter { it.session.contains("Morning", ignoreCase = true) || it.session.contains("AM", ignoreCase = true) }
                                        val afternoonLogs = targetLogs.filter { it.session.contains("Afternoon", ignoreCase = true) || it.session.contains("Midday", ignoreCase = true) || it.session.contains("Noon", ignoreCase = true) }
                                        val eveningLogs = targetLogs.filter { it.session.contains("Evening", ignoreCase = true) || it.session.contains("Night", ignoreCase = true) || (it.session.contains("PM", ignoreCase = true) && !it.session.contains("Afternoon", ignoreCase = true)) }
                                        
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
                                    "MONTH" -> {
                                        val monthLogs = milkLogs.filter { log ->
                                            val c = parseMilkLogCalendar(log.date)
                                            if (c != null) {
                                                c.get(java.util.Calendar.MONTH) == targetMonthIdx && c.get(java.util.Calendar.YEAR) == targetYearInt
                                            } else {
                                                (log.date.contains(selectedHerdMonth, ignoreCase = true) || log.date.contains(shortMonthLabel, ignoreCase = true)) &&
                                                        (log.date.contains(targetYearInt.toString()) || log.loggedAt.contains(targetYearInt.toString()))
                                            }
                                        }
                                        val daysInMonth = java.util.Calendar.getInstance().apply {
                                            set(java.util.Calendar.YEAR, targetYearInt)
                                            set(java.util.Calendar.MONTH, targetMonthIdx)
                                        }.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)

                                        val dailyData = (1..daysInMonth).map { day ->
                                            val dayLogs = monthLogs.filter { (parseMilkLogCalendar(it.date)?.get(java.util.Calendar.DAY_OF_MONTH) ?: 1) == day }
                                            dayLogs.sumOf { it.litres }.toFloat()
                                        }
                                        
                                        val totalL = monthLogs.sumOf { it.litres }
                                        val distinctDays = monthLogs.map { it.date }.distinct().size.coerceAtLeast(1)
                                        val dailyAvg = if (monthLogs.isNotEmpty()) totalL / distinctDays.toDouble() else 0.0
                                        val topCowName = monthLogs.groupBy { it.cowName }.maxByOrNull { it.value.sumOf { it.litres } }?.let { "${it.key} (${"%.1f".format(it.value.sumOf { it.litres })}L)" } ?: "Herd"
                                        
                                        Triple(
                                            MilkTotalsSummary(
                                                totalLitres = "%.1f L".format(totalL),
                                                avgPerDay = "%.1f L/day".format(dailyAvg),
                                                topCow = topCowName,
                                                trendStr = "$selectedHerdMonth $targetYearInt: ${monthLogs.size} logs across $distinctDays days"
                                            ),
                                            dailyData,
                                            (1..daysInMonth).map { "Day $it" }
                                        )
                                    }
                                    "YEAR" -> {
                                        val yearLogs = milkLogs.filter { log ->
                                            val c = parseMilkLogCalendar(log.date)
                                            if (c != null) {
                                                c.get(java.util.Calendar.YEAR) == targetYearInt
                                            } else {
                                                log.date.contains(targetYearInt.toString()) || log.loggedAt.contains(targetYearInt.toString())
                                            }
                                        }
                                        val allMonthsData = (0..11).map { mIdx ->
                                            val cal = java.util.Calendar.getInstance().apply {
                                                set(java.util.Calendar.YEAR, targetYearInt)
                                                set(java.util.Calendar.MONTH, mIdx)
                                            }
                                            val monthLabel = java.text.SimpleDateFormat("MMM", java.util.Locale.getDefault()).format(cal.time)
                                            val logsForMonth = yearLogs.filter { log ->
                                                val c = parseMilkLogCalendar(log.date)
                                                if (c != null) {
                                                    c.get(java.util.Calendar.MONTH) == mIdx && c.get(java.util.Calendar.YEAR) == targetYearInt
                                                } else {
                                                    log.date.contains(monthLabel, ignoreCase = true)
                                                }
                                            }
                                            val sumL = logsForMonth.sumOf { it.litres }.toFloat()
                                            monthLabel to sumL
                                        }
                                        val labels = allMonthsData.map { it.first }
                                        val points = allMonthsData.map { it.second }
                                        val totalL = yearLogs.sumOf { it.litres }
                                        val distinctDays = yearLogs.map { it.date }.distinct().size.coerceAtLeast(1)
                                        val dailyAvg = if (yearLogs.isNotEmpty()) totalL / distinctDays.toDouble() else 0.0
                                        val topCowName = yearLogs.groupBy { it.cowName }.maxByOrNull { it.value.sumOf { it.litres } }?.let { "${it.key} (${"%.1f".format(it.value.sumOf { it.litres })}L)" } ?: "Herd"
                                        
                                        Triple(
                                            MilkTotalsSummary(
                                                totalLitres = "%.1f L".format(totalL),
                                                avgPerDay = "%.1f L/day".format(dailyAvg),
                                                topCow = topCowName,
                                                trendStr = "Annual Total $targetYearInt: ${yearLogs.size} logs across $distinctDays days"
                                            ),
                                            points,
                                            labels
                                        )
                                    }
                                    else -> {
                                        val points = milkLogs.take(7).map { it.litres.toFloat() }
                                        val labels = milkLogs.take(7).map { it.date }
                                        val totalL = milkLogs.sumOf { it.litres }
                                        Triple(
                                            MilkTotalsSummary("%.1f L".format(totalL), "%.1f L/day".format(totalL / 7), "Herd", "Overview"),
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

            item {
                var usageTimeframe by remember { mutableStateOf("TODAY") } // TODAY, MONTH, YEAR
                var selectedUsageMonth by remember { mutableStateOf(defaultMonthName) }
                var selectedUsageYear by remember { mutableStateOf(defaultYearName) }
                var isUsageMonthMenuExpanded by remember { mutableStateOf(false) }
                var isUsageYearMenuExpanded by remember { mutableStateOf(false) }
                var longPressedUsageLog by remember { mutableStateOf<MilkUsageLog?>(null) }
                var editingUsageLog by remember { mutableStateOf<MilkUsageLog?>(null) }
                var deleteCandidateUsageLog by remember { mutableStateOf<MilkUsageLog?>(null) }

                val filteredUsageLogs = remember(milkUsageLogs, usageTimeframe, selectedUsageMonth, selectedUsageYear) {
                    val nowCal = java.util.Calendar.getInstance()
                    val cYear = nowCal.get(java.util.Calendar.YEAR)
                    val cDayOfYear = nowCal.get(java.util.Calendar.DAY_OF_YEAR)
                    val targetMonthIdx = monthsList.indexOfFirst { it.equals(selectedUsageMonth, ignoreCase = true) }
                    val targetYearInt = selectedUsageYear.toIntOrNull() ?: cYear
                    val shortMonthLabel = if (targetMonthIdx >= 0) {
                        val cal = java.util.Calendar.getInstance().apply { set(java.util.Calendar.MONTH, targetMonthIdx) }
                        SimpleDateFormat("MMM", Locale.getDefault()).format(cal.time)
                    } else selectedUsageMonth.take(3)

                    milkUsageLogs.filter { usage ->
                        val c = parseMilkLogCalendar(usage.date)
                        when (usageTimeframe) {
                            "TODAY" -> (c != null && c.get(java.util.Calendar.YEAR) == cYear && c.get(java.util.Calendar.DAY_OF_YEAR) == cDayOfYear) ||
                                usage.date.equals(todayDateStr, ignoreCase = true) ||
                                usage.date.contains("Today", ignoreCase = true)
                            "MONTH" -> if (c != null) {
                                c.get(java.util.Calendar.MONTH) == targetMonthIdx && c.get(java.util.Calendar.YEAR) == targetYearInt
                            } else {
                                (usage.date.contains(selectedUsageMonth, ignoreCase = true) || usage.date.contains(shortMonthLabel, ignoreCase = true)) &&
                                    usage.date.contains(targetYearInt.toString())
                            }
                            else -> if (c != null) {
                                c.get(java.util.Calendar.YEAR) == targetYearInt
                            } else {
                                usage.date.contains(targetYearInt.toString())
                            }
                        }
                    }.sortedByDescending {
                        parseMilkLogCalendar(it.date)?.timeInMillis ?: 0L
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "🥛 Milk Sales & Consumption Log",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "Daily breakdown of where milk went",
                            fontSize = 11.5.sp,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 1. TODAY TAB
                            val isUsageTodaySelected = usageTimeframe == "TODAY"
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isUsageTodaySelected) ForestGreenPrimary else Color(0xFFF1F5F9),
                                border = if (isUsageTodaySelected) null else BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { usageTimeframe = "TODAY" }
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
                                        color = if (isUsageTodaySelected) Color.White else Color(0xFF334155)
                                    )
                                }
                            }

                            // 2. MONTH TAB
                            val isUsageMonthSelected = usageTimeframe == "MONTH"
                            Box(modifier = Modifier.weight(1.1f)) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isUsageMonthSelected) ForestGreenPrimary else Color(0xFFF1F5F9),
                                    border = if (isUsageMonthSelected) null else BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            usageTimeframe = "MONTH"
                                            isUsageMonthMenuExpanded = true
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = selectedUsageMonth,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isUsageMonthSelected) Color.White else Color(0xFF334155),
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Icon(
                                            imageVector = Icons.Filled.ArrowDropDown,
                                            contentDescription = "Select Month",
                                            tint = if (isUsageMonthSelected) Color.White else Color(0xFF64748B),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                DropdownMenu(
                                    expanded = isUsageMonthMenuExpanded,
                                    onDismissRequest = { isUsageMonthMenuExpanded = false },
                                    modifier = Modifier.background(Color.White)
                                ) {
                                    monthsList.forEach { month ->
                                        val isCurrent = month.equals(selectedUsageMonth, ignoreCase = true)
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
                                                selectedUsageMonth = month
                                                usageTimeframe = "MONTH"
                                                isUsageMonthMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // 3. YEAR TAB
                            val isUsageYearSelected = usageTimeframe == "YEAR"
                            Box(modifier = Modifier.weight(1.1f)) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isUsageYearSelected) ForestGreenPrimary else Color(0xFFF1F5F9),
                                    border = if (isUsageYearSelected) null else BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            usageTimeframe = "YEAR"
                                            isUsageYearMenuExpanded = true
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = selectedUsageYear,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isUsageYearSelected) Color.White else Color(0xFF334155),
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Icon(
                                            imageVector = Icons.Filled.ArrowDropDown,
                                            contentDescription = "Select Year",
                                            tint = if (isUsageYearSelected) Color.White else Color(0xFF64748B),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                DropdownMenu(
                                    expanded = isUsageYearMenuExpanded,
                                    onDismissRequest = { isUsageYearMenuExpanded = false },
                                    modifier = Modifier.background(Color.White)
                                ) {
                                    yearsList.forEach { yr ->
                                        val isCurrent = yr == selectedUsageYear
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
                                                selectedUsageYear = yr
                                                usageTimeframe = "YEAR"
                                                isUsageYearMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (filteredUsageLogs.isEmpty()) {
                            Text(
                                text = "No milk usage records for this period yet.",
                                fontSize = 12.5.sp,
                                color = Color(0xFF94A3B8),
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("DATE", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), modifier = Modifier.weight(1.1f))
                                Text("TOTAL", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), modifier = Modifier.weight(0.9f), textAlign = TextAlign.End)
                                Text("CALVES", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706), modifier = Modifier.weight(0.9f), textAlign = TextAlign.End)
                                Text("HOME", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7), modifier = Modifier.weight(0.9f), textAlign = TextAlign.End)
                                Text("COOP", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary, modifier = Modifier.weight(0.9f), textAlign = TextAlign.End)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 2.dp)

                            filteredUsageLogs.forEach { usage ->
                                val canModifyThisUsage = isOwner || (canEditLogs && (canEditPastDaysLogs || com.example.util.DateValidationUtils.isTodayOrFuture(usage.date, usage.updatedAt)))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .pointerInput(usage.id, canModifyThisUsage) {
                                            detectTapGestures(
                                                onLongPress = {
                                                    if (canModifyThisUsage) {
                                                        longPressedUsageLog = usage
                                                    } else {
                                                        android.widget.Toast.makeText(context, "Editing or deleting records for previous days is disabled for worker accounts.", android.widget.Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            )
                                        }
                                        .padding(vertical = 10.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1.1f)) {
                                        Text(usage.date, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                        Text(
                                            text = MilkLogEntryRules.normalizedSession(usage.session).lowercase().replaceFirstChar { it.uppercase() },
                                            fontSize = 10.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                    Text("%.1fL".format(usage.totalAllocated), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), modifier = Modifier.weight(0.9f), textAlign = TextAlign.End)
                                    Text("%.1fL".format(usage.litresToCalves), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706), modifier = Modifier.weight(0.9f), textAlign = TextAlign.End)
                                    Text("%.1fL".format(usage.litresHomeUse), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7), modifier = Modifier.weight(0.9f), textAlign = TextAlign.End)
                                    Text("%.1fL".format(usage.litresToCooperative), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary, modifier = Modifier.weight(0.9f), textAlign = TextAlign.End)
                                }
                                HorizontalDivider(color = Color(0xFFF8FAFC), thickness = 1.dp)
                            }

                            val totalAll = filteredUsageLogs.sumOf { it.totalAllocated }
                            val totalCalves = filteredUsageLogs.sumOf { it.litresToCalves }
                            val totalHome = filteredUsageLogs.sumOf { it.litresHomeUse }
                            val totalCoop = filteredUsageLogs.sumOf { it.litresToCooperative }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("TOTAL", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), modifier = Modifier.weight(1.1f))
                                Text("%.1fL".format(totalAll), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), modifier = Modifier.weight(0.9f), textAlign = TextAlign.End)
                                Text("%.1fL".format(totalCalves), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706), modifier = Modifier.weight(0.9f), textAlign = TextAlign.End)
                                Text("%.1fL".format(totalHome), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7), modifier = Modifier.weight(0.9f), textAlign = TextAlign.End)
                                Text("%.1fL".format(totalCoop), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary, modifier = Modifier.weight(0.9f), textAlign = TextAlign.End)
                            }
                        }
                    }
                }

                // Long-press action menu: Edit / Delete
                longPressedUsageLog?.let { targetLog ->
                    Box {
                        DropdownMenu(
                            expanded = true,
                            onDismissRequest = { longPressedUsageLog = null },
                            modifier = Modifier.background(Color.White)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B)) },
                                onClick = {
                                    editingUsageLog = targetLog
                                    longPressedUsageLog = null
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete", fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C)) },
                                onClick = {
                                    deleteCandidateUsageLog = targetLog
                                    longPressedUsageLog = null
                                }
                            )
                        }
                    }
                }

                // Edit dialog: Coop / Home / Calves fields only
                editingUsageLog?.let { targetLog ->
                    var editCoopInput by remember(targetLog.id) { mutableStateOf(if (targetLog.litresToCooperative == 0.0) "" else targetLog.litresToCooperative.toString()) }
                    var editHomeInput by remember(targetLog.id) { mutableStateOf(if (targetLog.litresHomeUse == 0.0) "" else targetLog.litresHomeUse.toString()) }
                    var editCalvesInput by remember(targetLog.id) { mutableStateOf(if (targetLog.litresToCalves == 0.0) "" else targetLog.litresToCalves.toString()) }
                    var editErrorMessage by remember(targetLog.id) { mutableStateOf<String?>(null) }

                    AlertDialog(
                        onDismissRequest = { editingUsageLog = null },
                        title = { Text("Edit Milk Usage", fontWeight = FontWeight.Bold) },
                        text = {
                            Column {
                                Text(
                                    text = "${targetLog.date} • ${MilkLogEntryRules.normalizedSession(targetLog.session).lowercase().replaceFirstChar { it.uppercase() }}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                Text("Coop (L)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = editCoopInput,
                                    onValueChange = { editCoopInput = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                Text("Home (L)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = editHomeInput,
                                    onValueChange = { editHomeInput = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                Text("Calves (L)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = editCalvesInput,
                                    onValueChange = { editCalvesInput = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                )

                                editErrorMessage?.let { message ->
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(message, color = Color(0xFFB91C1C), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    val coop = editCoopInput.toDoubleOrNull() ?: 0.0
                                    val home = editHomeInput.toDoubleOrNull() ?: 0.0
                                    val calves = editCalvesInput.toDoubleOrNull() ?: 0.0
                                    if (coop == 0.0 && home == 0.0 && calves == 0.0) {
                                        editErrorMessage = "Enter at least one usage amount."
                                    } else {
                                        onEditMilkUsageLog(targetLog.id, coop, home, calves) { success, message ->
                                            if (success) {
                                                editingUsageLog = null
                                            } else {
                                                editErrorMessage = message ?: "Unable to update milk usage."
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                            ) {
                                Text("Save Changes", fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            OutlinedButton(onClick = { editingUsageLog = null }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                // Delete confirmation dialog
                deleteCandidateUsageLog?.let { targetLog ->
                    AlertDialog(
                        onDismissRequest = { deleteCandidateUsageLog = null },
                        title = { Text("Delete Record?", fontWeight = FontWeight.Bold) },
                        text = { Text("Do you want to permanently delete this record?") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    onDeleteMilkUsageLog(targetLog.id)
                                    deleteCandidateUsageLog = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB91C1C))
                            ) {
                                Text("Delete", fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            OutlinedButton(onClick = { deleteCandidateUsageLog = null }) {
                                Text("Cancel")
                            }
                        }
                    )
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
                            text = "1. SEARCH & SELECT ANIMAL (MILKING COWS ONLY)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = if (selectedCow != null && cowSearchQuery.isBlank()) selectedCow?.name ?: "" else cowSearchQuery,
                                onValueChange = { query ->
                                    cowSearchQuery = query
                                    selectedCow = cowsList.firstOrNull { it.name.equals(query.trim(), ignoreCase = true) }
                                    showCowDropdown = true
                                },
                                placeholder = { Text("Search by Name (e.g. Bessie), Tag (#102)...") },
                                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = ForestGreenPrimary) },
                                trailingIcon = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (selectedCow != null || cowSearchQuery.isNotBlank()) {
                                            IconButton(
                                                onClick = {
                                                    selectedCow = null
                                                    cowSearchQuery = ""
                                                    showCowDropdown = true
                                                }
                                            ) {
                                                Icon(Icons.Filled.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp), tint = Color(0xFF94A3B8))
                                            }
                                        }
                                        IconButton(onClick = { showCowDropdown = !showCowDropdown }) {
                                            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Dropdown", tint = Color(0xFF475569))
                                        }
                                    }
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
                                modifier = Modifier.fillMaxWidth(0.9f).background(Color.White)
                            ) {
                                if (matchingCows.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("No matching milking cows found", color = Color.Gray, fontSize = 13.sp) },
                                        onClick = { showCowDropdown = false }
                                    )
                                } else {
                                    matchingCows.forEach { cow ->
                                        val isCurrent = selectedCow?.tagId == cow.tagId || selectedCow?.name == cow.name
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column {
                                                        Text(
                                                            text = cow.name,
                                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                                            color = if (isCurrent) ForestGreenPrimary else Color(0xFF1E293B)
                                                        )
                                                        Text(
                                                            text = "${cow.breed} • ${cow.tagId}",
                                                            fontSize = 12.sp,
                                                            color = Color(0xFF64748B)
                                                        )
                                                    }
                                                    if (isCurrent) {
                                                        Text("✓", color = ForestGreenPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                                    }
                                                }
                                            },
                                            onClick = {
                                                selectedCow = cow
                                                cowSearchQuery = ""
                                                showCowDropdown = false
                                            }
                                        )
                                    }
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
                                val isSel = selectedCow?.tagId == cow.tagId || selectedCow?.name == cow.name
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSel) ForestGreenPrimary else Color(0xFFF1F5F9),
                                    border = if (isSel) null else BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                    modifier = Modifier.clickable {
                                        selectedCow = cow
                                        cowSearchQuery = ""
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

                            if (!cannotEditPast) {
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
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        AppDatePickerField(
                            value = selectedLogDate,
                            onValueChange = { selectedLogDate = it },
                            label = "Log Date",
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "milk_quick_log_date_picker"
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
                                if (litres > 0 && !selectedMilkSlotRecorded && !selectedDateIsFuture && !selectedDateIsPastRestricted && selectedDateIsValid) {
                                    onQuickSaveMilkLog(cowName, litres, selectedSession, selectedLogDate) { saved, message ->
                                        if (saved) {
                                            saveErrorMessage = null
                                            saveSuccessMessage = "✓ Logged ${"%.1f".format(litres)}L for $cowName ($selectedSession on $selectedLogDate)"
                                            milkLitresText = ""
                                        } else {
                                            saveSuccessMessage = null
                                            saveErrorMessage = message ?: "Milk log was not saved."
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("quick_save_milk_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ForestGreenPrimary,
                                disabledContainerColor = if (selectedMilkSlotRecorded) Color(0xFF475569) else Color(0xFFCBD5E1)
                            ),
                            enabled = milkLitresText.isNotBlank() && (selectedCow != null || cowSearchQuery.isNotBlank()) &&
                                !selectedMilkSlotRecorded && !selectedDateIsFuture && !selectedDateIsPastRestricted && selectedDateIsValid
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when {
                                    selectedMilkSlotRecorded -> "RECORDED"
                                    selectedDateIsFuture -> "FUTURE DATE NOT ALLOWED"
                                    selectedDateIsPastRestricted -> "PREVIOUS DAYS LOCKED"
                                    !selectedDateIsValid -> "SELECT A VALID DATE"
                                    else -> "SAVE COW MILK LOG"
                                },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (selectedMilkSlotRecorded || selectedDateIsFuture || selectedDateIsPastRestricted || !selectedDateIsValid) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = when {
                                    selectedMilkSlotRecorded -> "This cow's ${MilkLogEntryRules.normalizedSession(selectedSession).lowercase()} milk is already recorded for this date. Delete it in Log History before recording again."
                                    selectedDateIsFuture -> "Milk cannot be recorded for a future date."
                                    selectedDateIsPastRestricted -> "Recording logs for previous days is disabled for worker accounts. Please select today's date."
                                    else -> "Choose a valid date on or before today."
                                },
                                color = if (selectedMilkSlotRecorded) Color(0xFF475569) else Color(0xFFB91C1C),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        saveErrorMessage?.let { message ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(message, color = Color(0xFFB91C1C), fontSize = 12.sp, fontWeight = FontWeight.Medium)
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

            item {
                var usageDateText by remember { mutableStateOf(SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())) }
                var usageSession by remember { mutableStateOf("MORNING") }
                var coopInput by remember { mutableStateOf("") }
                var homeInput by remember { mutableStateOf("") }
                var calvesInput by remember { mutableStateOf("") }
                var usageErrorMessage by remember { mutableStateOf<String?>(null) }
                var usageSuccessMessage by remember { mutableStateOf<String?>(null) }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.5.dp, ForestGreenPrimary.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = CircleShape, color = Color(0xFFDCFCE7)) {
                                Text(
                                    text = "🥛",
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Log Milk Usage",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }
                        Text(
                            text = "Record how much went to the cooperative, home use, and calves",
                            fontSize = 11.5.sp,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(start = 42.dp, top = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text("DATE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                        Spacer(modifier = Modifier.height(6.dp))
                        AppDatePickerField(
                            value = usageDateText,
                            onValueChange = { usageDateText = it },
                            label = "Usage Date",
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "milk_usage_date_picker"
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text("SESSION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("MORNING" to "Morning", "AFTERNOON" to "Afternoon", "EVENING" to "Evening").forEach { (value, label) ->
                                val isSel = usageSession == value
                                FilterChip(
                                    selected = isSel,
                                    onClick = { usageSession = value },
                                    label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    modifier = Modifier.weight(1f).testTag("milk_usage_session_$value"),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ForestGreenPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Auto-computed production total for the selected date, from all cows' milk logs
                        val usageDateKey = remember(usageDateText) { MilkLogEntryRules.canonicalDateKey(usageDateText) }
                        val dayMilkLogs = remember(usageDateKey, milkLogs) {
                            if (usageDateKey == null) emptyList()
                            else milkLogs.filter { MilkLogEntryRules.canonicalDateKey(it.date) == usageDateKey }
                        }
                        val morningTotal = remember(dayMilkLogs) {
                            dayMilkLogs.filter { MilkLogEntryRules.normalizedSession(it.session) == "MORNING" }.sumOf { it.litres }
                        }
                        val afternoonTotal = remember(dayMilkLogs) {
                            dayMilkLogs.filter { MilkLogEntryRules.normalizedSession(it.session) == "AFTERNOON" }.sumOf { it.litres }
                        }
                        val eveningTotal = remember(dayMilkLogs) {
                            dayMilkLogs.filter { MilkLogEntryRules.normalizedSession(it.session) == "EVENING" }.sumOf { it.litres }
                        }
                        val dayGrandTotal = morningTotal + afternoonTotal + eveningTotal

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF0FDF4),
                            border = BorderStroke(1.dp, ForestGreenPrimary.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "TOTAL PRODUCED THIS DAY",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF64748B)
                                    )
                                    Text(
                                        text = "%.1fL".format(dayGrandTotal),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ForestGreenPrimary
                                    )
                                }

                                if (usageDateKey == null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Enter a valid date to see the day's totals.",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                } else if (dayMilkLogs.isEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "No milk logs recorded for this date yet.",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Morning", fontSize = 10.sp, fontWeight = if (usageSession == "MORNING") FontWeight.Bold else FontWeight.Normal, color = if (usageSession == "MORNING") ForestGreenPrimary else Color(0xFF64748B))
                                            Text("%.1fL".format(morningTotal), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Afternoon", fontSize = 10.sp, fontWeight = if (usageSession == "AFTERNOON") FontWeight.Bold else FontWeight.Normal, color = if (usageSession == "AFTERNOON") ForestGreenPrimary else Color(0xFF64748B))
                                            Text("%.1fL".format(afternoonTotal), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Evening", fontSize = 10.sp, fontWeight = if (usageSession == "EVENING") FontWeight.Bold else FontWeight.Normal, color = if (usageSession == "EVENING") ForestGreenPrimary else Color(0xFF64748B))
                                            Text("%.1fL".format(eveningTotal), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(shape = CircleShape, color = ForestGreenPrimary, modifier = Modifier.size(8.dp)) {}
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text("Coop (L)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = coopInput,
                                    onValueChange = { coopInput = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(shape = CircleShape, color = Color(0xFF0284C7), modifier = Modifier.size(8.dp)) {}
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text("Home (L)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = homeInput,
                                    onValueChange = { homeInput = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(shape = CircleShape, color = Color(0xFFD97706), modifier = Modifier.size(8.dp)) {}
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text("Calves (L)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = calvesInput,
                                    onValueChange = { calvesInput = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val existingUsageForDate = remember(usageDateKey, usageSession, milkUsageLogs) {
                            if (usageDateKey == null) null
                            else milkUsageLogs.firstOrNull {
                                !it.isDeleted &&
                                    MilkLogEntryRules.canonicalDateKey(it.date) == usageDateKey &&
                                    MilkLogEntryRules.normalizedSession(it.session) == usageSession
                            }
                        }
                        val alreadyRecorded = existingUsageForDate != null
                        val usageDateIsPastRestricted = cannotEditPast && com.example.util.DateValidationUtils.isPastDate(usageDateText)

                        Button(
                            onClick = {
                                usageErrorMessage = null
                                usageSuccessMessage = null
                                val coop = coopInput.toDoubleOrNull() ?: 0.0
                                val home = homeInput.toDoubleOrNull() ?: 0.0
                                val calves = calvesInput.toDoubleOrNull() ?: 0.0
                                if (coop == 0.0 && home == 0.0 && calves == 0.0) {
                                    usageErrorMessage = "Enter at least one usage amount."
                                } else {
                                    onSaveMilkUsageLog(usageDateText, usageSession, coop, home, calves) { success, message ->
                                        if (success) {
                                            usageSuccessMessage = "Milk usage saved for $usageDateText (${usageSession.lowercase().replaceFirstChar { it.uppercase() }})."
                                            coopInput = ""
                                            homeInput = ""
                                            calvesInput = ""
                                        } else {
                                            usageErrorMessage = message ?: "Unable to save milk usage."
                                        }
                                    }
                                }
                            },
                            enabled = !alreadyRecorded && !usageDateIsPastRestricted,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_milk_usage_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ForestGreenPrimary,
                                disabledContainerColor = Color(0xFF94A3B8)
                            )
                        ) {
                            Text(
                                text = when {
                                    alreadyRecorded -> "Recorded"
                                    usageDateIsPastRestricted -> "Previous Days Locked"
                                    else -> "Save Usage Record"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        if (alreadyRecorded) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "A usage record already exists for this date and session. Delete it first to re-enter.",
                                color = Color(0xFF64748B),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (usageDateIsPastRestricted) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Recording milk usage for previous days is disabled for worker accounts. Please select today's date.",
                                color = Color(0xFFB91C1C),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        usageErrorMessage?.let { message ->
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(message, color = Color(0xFFB91C1C), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }

                        usageSuccessMessage?.let { msg ->
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

                        var isPerCowSelectorExpanded by remember { mutableStateOf(false) }

                        // Cow Selector Dropdown Row
                        Text("SELECT COW TO ANALYZE (MILKING COWS ONLY)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                        Spacer(modifier = Modifier.height(6.dp))

                        // 1. Prominent Dropdown Card for Cow Selection
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF1F5F9),
                                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isPerCowSelectorExpanded = true }
                                    .testTag("per_cow_dropdown_selector")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = CircleShape,
                                            color = ForestGreenPrimary.copy(alpha = 0.15f),
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    Icons.Filled.Pets,
                                                    contentDescription = null,
                                                    tint = ForestGreenPrimary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = perCowSelectedCow.name,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1E293B)
                                            )
                                            Text(
                                                text = "${perCowSelectedCow.breed} • Tag: ${perCowSelectedCow.tagId}",
                                                fontSize = 12.sp,
                                                color = ForestGreenPrimary,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = Icons.Filled.ArrowDropDown,
                                        contentDescription = "Select Cow",
                                        tint = Color(0xFF475569)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = isPerCowSelectorExpanded,
                                onDismissRequest = { isPerCowSelectorExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.9f).background(Color.White)
                            ) {
                                if (cowsList.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("No milking cows available", color = Color.Gray, fontSize = 13.sp) },
                                        onClick = { isPerCowSelectorExpanded = false }
                                    )
                                } else {
                                    cowsList.forEach { cow ->
                                        val isCurrent = perCowSelectedCow.tagId == cow.tagId || perCowSelectedCow.name == cow.name
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column {
                                                        Text(
                                                            text = cow.name,
                                                            fontSize = 14.sp,
                                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                                            color = if (isCurrent) ForestGreenPrimary else Color(0xFF1E293B)
                                                        )
                                                        Text(
                                                            text = "${cow.breed} • ${cow.tagId}",
                                                            fontSize = 12.sp,
                                                            color = Color(0xFF64748B)
                                                        )
                                                    }
                                                    if (isCurrent) {
                                                        Text("✓", color = ForestGreenPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                                    }
                                                }
                                            },
                                            onClick = {
                                                perCowSelectedCow = cow
                                                isPerCowSelectorExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 2. Fast 1-tap horizontal selection chips
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(cowsList) { cow ->
                                val isSel = perCowSelectedCow.tagId == cow.tagId || perCowSelectedCow.name == cow.name
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSel) ForestGreenPrimary else Color(0xFFF1F5F9),
                                    border = if (isSel) null else BorderStroke(1.dp, Color(0xFFCBD5E1)),
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

                        // Timeframe Tabs for Per Cow View: Today, Month, Year, All Time
                        Text("TIME PERIOD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // 1. TODAY TAB
                            val isTodaySelected = perCowTimeframe == "TODAY"
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isTodaySelected) ForestGreenPrimary else Color(0xFFF1F5F9),
                                border = if (isTodaySelected) null else BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        perCowTimeframe = "TODAY"
                                    }
                                    .testTag("per_cow_today_tab")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Today",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isTodaySelected) Color.White else Color(0xFF334155)
                                    )
                                }
                            }

                            // 2. MONTH TAB (defaults to current month e.g. "August", clickable with 12 months dropdown)
                            val isMonthSelected = perCowTimeframe == "MONTH"
                            Box(modifier = Modifier.weight(1.2f)) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isMonthSelected) ForestGreenPrimary else Color(0xFFF1F5F9),
                                    border = if (isMonthSelected) null else BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            perCowTimeframe = "MONTH"
                                            isMonthMenuExpanded = true
                                        }
                                        .testTag("per_cow_month_tab")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = selectedPerCowMonth.take(3),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isMonthSelected) Color.White else Color(0xFF334155),
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Icon(
                                            imageVector = Icons.Filled.ArrowDropDown,
                                            contentDescription = "Select Month",
                                            tint = if (isMonthSelected) Color.White else Color(0xFF64748B),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = isMonthMenuExpanded,
                                    onDismissRequest = { isMonthMenuExpanded = false },
                                    modifier = Modifier.background(Color.White)
                                ) {
                                    monthsList.forEach { month ->
                                        val isCurrent = month.equals(selectedPerCowMonth, ignoreCase = true)
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
                                                selectedPerCowMonth = month
                                                perCowTimeframe = "MONTH"
                                                isMonthMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // 3. YEAR TAB (defaults to current year e.g. "2026", clickable with years dropdown)
                            val isYearSelected = perCowTimeframe == "YEAR"
                            Box(modifier = Modifier.weight(1.1f)) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isYearSelected) ForestGreenPrimary else Color(0xFFF1F5F9),
                                    border = if (isYearSelected) null else BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            perCowTimeframe = "YEAR"
                                            isYearMenuExpanded = true
                                        }
                                        .testTag("per_cow_year_tab")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = selectedPerCowYear,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isYearSelected) Color.White else Color(0xFF334155),
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Icon(
                                            imageVector = Icons.Filled.ArrowDropDown,
                                            contentDescription = "Select Year",
                                            tint = if (isYearSelected) Color.White else Color(0xFF64748B),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = isYearMenuExpanded,
                                    onDismissRequest = { isYearMenuExpanded = false },
                                    modifier = Modifier.background(Color.White)
                                ) {
                                    yearsList.forEach { yr ->
                                        val isCurrent = yr == selectedPerCowYear
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
                                                selectedPerCowYear = yr
                                                perCowTimeframe = "YEAR"
                                                isYearMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // 4. ALL TIME TAB
                            val isAllTimeSelected = perCowTimeframe == "ALL_TIME"
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isAllTimeSelected) ForestGreenPrimary else Color(0xFFF1F5F9),
                                border = if (isAllTimeSelected) null else BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier
                                    .weight(1.1f)
                                    .clickable {
                                        perCowTimeframe = "ALL_TIME"
                                    }
                                    .testTag("per_cow_all_time_tab")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Lactation",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isAllTimeSelected) Color.White else Color(0xFF334155)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Filtered logs for selected cow and chosen time period (Month, Year, or Today)
                        val cowAllLogs = remember(milkLogs, perCowSelectedCow) {
                            milkLogs.filter { isLogForCow(it, perCowSelectedCow) }
                        }

                        val targetMonthIdx = remember(selectedPerCowMonth) {
                            monthsList.indexOfFirst { it.equals(selectedPerCowMonth, ignoreCase = true) }
                        }
                        val targetYearInt = remember(selectedPerCowYear) {
                            selectedPerCowYear.toIntOrNull() ?: 2026
                        }
                        val shortMonthLabel = remember(targetMonthIdx, selectedPerCowMonth) {
                            if (targetMonthIdx >= 0) {
                                val cal = java.util.Calendar.getInstance().apply { set(java.util.Calendar.MONTH, targetMonthIdx) }
                                SimpleDateFormat("MMM", Locale.getDefault()).format(cal.time)
                            } else selectedPerCowMonth.take(3)
                        }

                        val periodFilteredLogs = remember(cowAllLogs, perCowTimeframe, selectedPerCowMonth, selectedPerCowYear, targetMonthIdx, targetYearInt) {
                            when (perCowTimeframe) {
                                "TODAY" -> {
                                    val nowCal = java.util.Calendar.getInstance()
                                    val tYear = nowCal.get(java.util.Calendar.YEAR)
                                    val tDayOfYear = nowCal.get(java.util.Calendar.DAY_OF_YEAR)
                                    cowAllLogs.filter { log ->
                                        val c = parseMilkLogCalendar(log.date)
                                        (c != null && c.get(java.util.Calendar.YEAR) == tYear && c.get(java.util.Calendar.DAY_OF_YEAR) == tDayOfYear) ||
                                                log.date.equals(todayDateStr, ignoreCase = true) ||
                                                log.date.contains("Today", ignoreCase = true)
                                    }
                                }
                                "MONTH" -> {
                                    cowAllLogs.filter { log ->
                                        val c = parseMilkLogCalendar(log.date)
                                        if (c != null) {
                                            c.get(java.util.Calendar.MONTH) == targetMonthIdx && c.get(java.util.Calendar.YEAR) == targetYearInt
                                        } else {
                                            (log.date.contains(selectedPerCowMonth, ignoreCase = true) || log.date.contains(shortMonthLabel, ignoreCase = true)) &&
                                                    (log.date.contains(targetYearInt.toString()) || log.loggedAt.contains(targetYearInt.toString()))
                                        }
                                    }
                                }
                                "YEAR" -> {
                                    cowAllLogs.filter { log ->
                                        val c = parseMilkLogCalendar(log.date)
                                        if (c != null) {
                                            c.get(java.util.Calendar.YEAR) == targetYearInt
                                        } else {
                                            log.date.contains(targetYearInt.toString()) || log.loggedAt.contains(targetYearInt.toString())
                                        }
                                    }
                                }
                                else -> cowAllLogs
                            }
                        }

                        // Group the period logs by date/day to analyze morning, afternoon, evening and daily subtotals
                        val dailyBreakdowns = remember(periodFilteredLogs) {
                            val grouped = periodFilteredLogs.groupBy { log ->
                                val cal = parseMilkLogCalendar(log.date)
                                if (cal != null) {
                                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                                } else {
                                    log.date
                                }
                            }

                            grouped.map { (dateKey, logsForDay) ->
                                val cal = logsForDay.mapNotNull { parseMilkLogCalendar(it.date) }.firstOrNull()
                                val displayDate = if (cal != null) {
                                    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(cal.time)
                                } else {
                                    logsForDay.firstOrNull()?.date ?: dateKey
                                }
                                val dayOfWeek = if (cal != null) {
                                    SimpleDateFormat("EEE", Locale.getDefault()).format(cal.time)
                                } else ""
                                val dayOfMonth = cal?.get(java.util.Calendar.DAY_OF_MONTH) ?: 1

                                val mLogs = logsForDay.filter { it.session.contains("Morning", ignoreCase = true) || it.session.contains("AM", ignoreCase = true) }
                                val aLogs = logsForDay.filter { it.session.contains("Afternoon", ignoreCase = true) || it.session.contains("Midday", ignoreCase = true) || it.session.contains("Noon", ignoreCase = true) }
                                val eLogs = logsForDay.filter { it.session.contains("Evening", ignoreCase = true) || it.session.contains("Night", ignoreCase = true) || (it.session.contains("PM", ignoreCase = true) && !it.session.contains("Afternoon", ignoreCase = true)) }

                                val mSum = mLogs.sumOf { it.litres }
                                val aSum = aLogs.sumOf { it.litres }
                                val eSum = eLogs.sumOf { it.litres }
                                val dayTotal = mSum + aSum + eSum

                                CowDayMilkBreakdown(
                                    dateKey = dateKey,
                                    displayDate = displayDate,
                                    dayOfWeek = dayOfWeek,
                                    dayOfMonth = dayOfMonth,
                                    morningYield = mSum,
                                    morningCount = mLogs.size,
                                    afternoonYield = aSum,
                                    afternoonCount = aLogs.size,
                                    eveningYield = eSum,
                                    eveningCount = eLogs.size,
                                    dailyTotal = dayTotal
                                )
                            }.sortedByDescending { it.dateKey }
                        }

                        // Period Totals and Subtotals
                        val periodTotalLitres = remember(dailyBreakdowns) { dailyBreakdowns.sumOf { it.dailyTotal } }
                        val periodMorningLitres = remember(dailyBreakdowns) { dailyBreakdowns.sumOf { it.morningYield } }
                        val periodAfternoonLitres = remember(dailyBreakdowns) { dailyBreakdowns.sumOf { it.afternoonYield } }
                        val periodEveningLitres = remember(dailyBreakdowns) { dailyBreakdowns.sumOf { it.eveningYield } }
                        val periodActiveDays = remember(dailyBreakdowns) { dailyBreakdowns.size }
                        val periodDailyAvg = remember(periodTotalLitres, periodActiveDays) {
                            if (periodActiveDays > 0) periodTotalLitres / periodActiveDays else 0.0
                        }

                        val periodLabel = when (perCowTimeframe) {
                            "TODAY" -> "Today ($todayDateStr)"
                            "MONTH" -> "$selectedPerCowMonth $selectedPerCowYear"
                            "YEAR" -> "Year $selectedPerCowYear"
                            else -> "Lactation"
                        }

                        // Cow Period Overview Card
                        Surface(
                            shape = RoundedCornerShape(14.dp),
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
                                            text = "${perCowSelectedCow.breed} • $periodLabel",
                                            fontSize = 12.sp,
                                            color = ForestGreenPrimary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "%.1f L".format(periodTotalLitres),
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ForestGreenPrimary
                                        )
                                        Text(
                                            text = "Avg: %.1f L/day".format(periodDailyAvg),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF475569)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // 3 Milking Sessions Subtotals for the Entire Period
                                Text(
                                    text = "PERIOD SUB-TOTALS ($periodLabel):",
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
                                            Text("%.1f L".format(periodMorningLitres), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                                            Text("${dailyBreakdowns.sumOf { it.morningCount }} logs", fontSize = 10.sp, color = Color(0xFF78350F))
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFE0F2FE),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("☀️ Afternoon", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0369A1))
                                            Text("%.1f L".format(periodAfternoonLitres), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                                            Text("${dailyBreakdowns.sumOf { it.afternoonCount }} logs", fontSize = 10.sp, color = Color(0xFF075985))
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFF1F5F9),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("🌙 Evening", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                                            Text("%.1f L".format(periodEveningLitres), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                                            Text("${dailyBreakdowns.sumOf { it.eveningCount }} logs", fontSize = 10.sp, color = Color(0xFF64748B))
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // --- DETAILED DAILY SESSIONS & DAILY SUB-TOTALS BREAKDOWN TABLE ---
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Filled.CalendarToday,
                                            contentDescription = null,
                                            tint = ForestGreenPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Daily Milk Breakdown",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E293B)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = ForestGreenPrimary.copy(alpha = 0.1f)
                                    ) {
                                        Text(
                                            text = "$periodActiveDays day${if (periodActiveDays == 1) "" else "s"} recorded",
                                            color = ForestGreenPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                if (dailyBreakdowns.isEmpty()) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFFF8FAFC),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(20.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                Icons.Filled.WaterDrop,
                                                contentDescription = null,
                                                tint = Color(0xFF94A3B8),
                                                modifier = Modifier.size(28.dp)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "No milk records for ${perCowSelectedCow.name} in $periodLabel",
                                                color = Color(0xFF64748B),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Use '+ Quick Milk Entry' above to add records",
                                                color = Color(0xFF94A3B8),
                                                fontSize = 11.sp,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                } else {
                                    // Table Column Headers
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFF1F5F9),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "DATE / DAY",
                                                modifier = Modifier.weight(1.3f),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF475569)
                                            )
                                            Text(
                                                text = "🌅 AM",
                                                modifier = Modifier.weight(0.9f),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF92400E),
                                                textAlign = TextAlign.Center
                                            )
                                            Text(
                                                text = "☀️ NOON",
                                                modifier = Modifier.weight(0.9f),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0369A1),
                                                textAlign = TextAlign.Center
                                            )
                                            Text(
                                                text = "🌙 PM",
                                                modifier = Modifier.weight(0.9f),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF334155),
                                                textAlign = TextAlign.Center
                                            )
                                            Text(
                                                text = "SUBTOTAL",
                                                modifier = Modifier.weight(1.1f),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ForestGreenPrimary,
                                                textAlign = TextAlign.End
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // List of Milk Days for that specific cow with Analyzed Data & Daily Subtotals
                                    dailyBreakdowns.forEachIndexed { idx, dayStat ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (idx % 2 == 0) Color(0xFFFAFAFA) else Color.White,
                                            border = BorderStroke(0.5.dp, Color(0xFFE2E8F0)),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Date & Day of Week
                                                Column(modifier = Modifier.weight(1.3f)) {
                                                    Text(
                                                        text = dayStat.displayDate.substringBefore(" ${targetYearInt}").ifBlank { dayStat.displayDate },
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF1E293B)
                                                    )
                                                    if (dayStat.dayOfWeek.isNotBlank()) {
                                                        Text(
                                                            text = dayStat.dayOfWeek,
                                                            fontSize = 10.sp,
                                                            color = Color(0xFF64748B),
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                }

                                                // Morning yield
                                                Text(
                                                    text = if (dayStat.morningYield > 0) "%.1fL".format(dayStat.morningYield) else "—",
                                                    modifier = Modifier.weight(0.9f),
                                                    fontSize = 11.sp,
                                                    fontWeight = if (dayStat.morningYield > 0) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (dayStat.morningYield > 0) Color(0xFFB45309) else Color(0xFF94A3B8),
                                                    textAlign = TextAlign.Center
                                                )

                                                // Afternoon yield
                                                Text(
                                                    text = if (dayStat.afternoonYield > 0) "%.1fL".format(dayStat.afternoonYield) else "—",
                                                    modifier = Modifier.weight(0.9f),
                                                    fontSize = 11.sp,
                                                    fontWeight = if (dayStat.afternoonYield > 0) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (dayStat.afternoonYield > 0) Color(0xFF0284C7) else Color(0xFF94A3B8),
                                                    textAlign = TextAlign.Center
                                                )

                                                // Evening yield
                                                Text(
                                                    text = if (dayStat.eveningYield > 0) "%.1fL".format(dayStat.eveningYield) else "—",
                                                    modifier = Modifier.weight(0.9f),
                                                    fontSize = 11.sp,
                                                    fontWeight = if (dayStat.eveningYield > 0) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (dayStat.eveningYield > 0) Color(0xFF475569) else Color(0xFF94A3B8),
                                                    textAlign = TextAlign.Center
                                                )

                                                // Daily Subtotal (AM + Afternoon + PM)
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = Color(0xFFDCFCE7),
                                                    modifier = Modifier.weight(1.1f)
                                                ) {
                                                    Text(
                                                        text = "%.1f L".format(dayStat.dailyTotal),
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = ForestGreenPrimary,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Grand Subtotals Footer for the Entire Selected Period
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = ForestGreenPrimary.copy(alpha = 0.08f),
                                        border = BorderStroke(1.dp, ForestGreenPrimary.copy(alpha = 0.3f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "PERIOD TOTAL",
                                                modifier = Modifier.weight(1.3f),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ForestGreenPrimary
                                            )
                                            Text(
                                                text = "%.1fL".format(periodMorningLitres),
                                                modifier = Modifier.weight(0.9f),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFB45309),
                                                textAlign = TextAlign.Center
                                            )
                                            Text(
                                                text = "%.1fL".format(periodAfternoonLitres),
                                                modifier = Modifier.weight(0.9f),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0284C7),
                                                textAlign = TextAlign.Center
                                            )
                                            Text(
                                                text = "%.1fL".format(periodEveningLitres),
                                                modifier = Modifier.weight(0.9f),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF475569),
                                                textAlign = TextAlign.Center
                                            )
                                            Text(
                                                text = "%.1f L".format(periodTotalLitres),
                                                modifier = Modifier.weight(1.1f),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ForestGreenPrimary,
                                                textAlign = TextAlign.End
                                            )
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
        if (activeViewTab == "HISTORY") {
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

                                val canDeleteThisLog = isOwner || (canEditLogs && (canEditPastDaysLogs || com.example.util.DateValidationUtils.isTodayOrFuture(log.date, log.updatedAt)))
                                if (canDeleteThisLog) {
                                    IconButton(onClick = { onDeleteMilkLog(log.id) }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color(0xFF94A3B8))
                                    }
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

                            // Timeframe Tabs for Egg Yield: Today, Month, Year (Identical to Milk Production Overview)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 1. TODAY TAB
                                val isTodaySelected = analyticsEggTimeframe == "TODAY"
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isTodaySelected) ForestGreenPrimary else Color(0xFFF1F5F9),
                                    border = if (isTodaySelected) null else BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { analyticsEggTimeframe = "TODAY" }
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
                                val isMonthSelected = analyticsEggTimeframe == "MONTH"
                                Box(modifier = Modifier.weight(1.1f)) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isMonthSelected) ForestGreenPrimary else Color(0xFFF1F5F9),
                                        border = if (isMonthSelected) null else BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                analyticsEggTimeframe = "MONTH"
                                                isEggMonthMenuExpanded = true
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = analyticsEggMonth,
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
                                        expanded = isEggMonthMenuExpanded,
                                        onDismissRequest = { isEggMonthMenuExpanded = false },
                                        modifier = Modifier.background(Color.White)
                                    ) {
                                        monthsList.forEach { month ->
                                            val isCurrent = month.equals(analyticsEggMonth, ignoreCase = true)
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
                                                    analyticsEggMonth = month
                                                    analyticsEggTimeframe = "MONTH"
                                                    isEggMonthMenuExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                // 3. YEAR TAB
                                val isYearSelected = analyticsEggTimeframe == "YEAR"
                                Box(modifier = Modifier.weight(1.1f)) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isYearSelected) ForestGreenPrimary else Color(0xFFF1F5F9),
                                        border = if (isYearSelected) null else BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                analyticsEggTimeframe = "YEAR"
                                                isEggYearMenuExpanded = true
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = analyticsEggYear,
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
                                        expanded = isEggYearMenuExpanded,
                                        onDismissRequest = { isEggYearMenuExpanded = false },
                                        modifier = Modifier.background(Color.White)
                                    ) {
                                        yearsList.forEach { yr ->
                                            val isCurrent = yr == analyticsEggYear
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
                                                    analyticsEggYear = yr
                                                    analyticsEggTimeframe = "YEAR"
                                                    isEggYearMenuExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Analytics records filtered by real calendar period
                            val selectedFlockLogs = if (analyticsEggFlock == "ALL") eggLogs else eggLogs.filter { it.unitName.equals(analyticsEggFlock, ignoreCase = true) }
                            val targetEggMonthIdx = monthsList.indexOfFirst { it.equals(analyticsEggMonth, ignoreCase = true) }
                            val targetEggYearInt = analyticsEggYear.toIntOrNull() ?: currentYearNum

                            fun eggLogCalendar(log: EggLog): java.util.Calendar? =
                                parseMilkLogCalendar(log.loggedAt) ?: parseMilkLogCalendar(log.notes?.substringAfter("[")?.substringBefore("]") ?: "")

                            val timeframeLogs = selectedFlockLogs.filter { log ->
                                val parsed = eggLogCalendar(log)
                                when (analyticsEggTimeframe) {
                                    "TODAY" -> {
                                        if (parsed != null) {
                                            parsed.get(java.util.Calendar.YEAR) == currentYearNum &&
                                                    parsed.get(java.util.Calendar.DAY_OF_YEAR) == currentCal.get(java.util.Calendar.DAY_OF_YEAR)
                                        } else {
                                            log.loggedAt.contains("Today", ignoreCase = true) ||
                                                    log.loggedAt.contains(todayDateStr, ignoreCase = true)
                                        }
                                    }
                                    "MONTH" -> {
                                        if (parsed != null) {
                                            parsed.get(java.util.Calendar.MONTH) == targetEggMonthIdx &&
                                                    parsed.get(java.util.Calendar.YEAR) == targetEggYearInt
                                        } else {
                                            log.loggedAt.contains(analyticsEggMonth, ignoreCase = true) &&
                                                    log.loggedAt.contains(targetEggYearInt.toString())
                                        }
                                    }
                                    "YEAR" -> {
                                        if (parsed != null) {
                                            parsed.get(java.util.Calendar.YEAR) == targetEggYearInt
                                        } else {
                                            log.loggedAt.contains(targetEggYearInt.toString())
                                        }
                                    }
                                    else -> true
                                }
                            }
                            val totalFlockEggs = timeframeLogs.sumOf { it.totalEggs }
                            val totalFlockTrays = totalFlockEggs / 30
                            val totalFlockRem = totalFlockEggs % 30
                            val totalFlockDamaged = timeframeLogs.sumOf { it.damagedEggs }
                            val dailyAvgValue = if (timeframeLogs.isEmpty()) 0 else (totalFlockEggs.toDouble() / timeframeLogs.mapNotNull { eggLogCalendar(it) }.map { it.get(java.util.Calendar.DAY_OF_YEAR) }.distinct().size.coerceAtLeast(1)).toInt()

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

                            // The chart uses exactly the flock and calendar period selected above.
                            val eggTrendTitle = when (analyticsEggTimeframe) {
                                "TODAY" -> "TODAY'S EGG PRODUCTION"
                                "MONTH" -> "${analyticsEggMonth.uppercase()} $targetEggYearInt EGG PRODUCTION"
                                else -> "$targetEggYearInt EGG PRODUCTION"
                            }
                            Text(eggTrendTitle, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                            Spacer(modifier = Modifier.height(8.dp))

                            val eggTrendPoints = remember(timeframeLogs, selectedFlockLogs, analyticsEggTimeframe, analyticsEggMonth, analyticsEggYear, targetEggMonthIdx, targetEggYearInt) {
                                when (analyticsEggTimeframe) {
                                    "TODAY" -> listOf("Today" to timeframeLogs.sumOf { it.totalEggs }.toFloat())
                                    "MONTH" -> {
                                        val maximumDay = java.util.Calendar.getInstance().apply {
                                            set(java.util.Calendar.YEAR, targetEggYearInt)
                                            set(java.util.Calendar.MONTH, targetEggMonthIdx)
                                        }.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
                                        (1..maximumDay).map { day ->
                                            day.toString() to timeframeLogs.filter { eggLogCalendar(it)?.get(java.util.Calendar.DAY_OF_MONTH) == day }.sumOf { it.totalEggs }.toFloat()
                                        }
                                    }
                                    else -> {
                                        monthsList.mapIndexed { index, month ->
                                            month.take(3) to timeframeLogs.filter { eggLogCalendar(it)?.get(java.util.Calendar.MONTH) == index }.sumOf { it.totalEggs }.toFloat()
                                        }
                                    }
                                }
                            }

                            val daysList = eggTrendPoints.map { it.first }
                            val dayValues = eggTrendPoints.map { it.second }
                            val maxVal = (dayValues.maxOrNull() ?: 1f).coerceAtLeast(1f)

                            if (dayValues.size > 12) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .height(120.dp)
                                            .padding(horizontal = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        dayValues.forEachIndexed { idx, valAmt ->
                                            val pct = if (valAmt <= 0f) 0.03f else (valAmt / maxVal).coerceIn(0.03f, 1f)
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.width(26.dp)
                                            ) {
                                                Text("${valAmt.toInt()}", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF78350F))
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .width(16.dp)
                                                        .fillMaxHeight(pct * 0.75f)
                                                    .background(
                                                        color = if (idx == dayValues.lastIndex) Color(0xFFD97706) else Color(0xFFFBBF24),
                                                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                                    )
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(daysList[idx], fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                                            }
                                        }
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    dayValues.forEachIndexed { idx, valAmt ->
                                        val pct = if (valAmt <= 0f) 0.03f else (valAmt / maxVal).coerceIn(0.03f, 1f)
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
                                                        color = if (idx == dayValues.lastIndex) Color(0xFFD97706) else Color(0xFFFBBF24),
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
            }

            // 2. Quick Egg Entry Form Card
            if (eggActiveViewTab == "EGG_QUICK_LOG" || eggActiveViewTab == "EGG_ALL") {
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
                            val eggDateOptions = if (cannotEditPast) listOf(todayDateStr) else listOf(todayDateStr, yesterdayDateStr, twoDaysAgoDateStr)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                eggDateOptions.forEach { dt ->
                                    val isSel = selectedEggLogDate == dt
                                    FilterChip(
                                        selected = isSel,
                                        onClick = { selectedEggLogDate = dt },
                                        label = { Text(if (dt == todayDateStr) "Today ($dt)" else dt, fontSize = 11.sp) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            AppDatePickerField(
                                value = selectedEggLogDate,
                                onValueChange = { selectedEggLogDate = it },
                                label = "Collection Date",
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "egg_quick_log_date_picker"
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = eggNotesText,
                                onValueChange = { eggNotesText = it },
                                label = { Text("Notes / Collection Remarks (Optional)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            val eggDateIsPastRestricted = cannotEditPast && com.example.util.DateValidationUtils.isPastDate(selectedEggLogDate)
                            val eggDateIsFuture = MilkLogEntryRules.isFutureDate(selectedEggLogDate)

                            Button(
                                onClick = {
                                    val totalVal = eggTotalText.toIntOrNull() ?: 0
                                    val damagedVal = eggDamagedText.toIntOrNull() ?: 0
                                    if (totalVal > 0 && !eggDateIsPastRestricted && !eggDateIsFuture) {
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
                                enabled = !eggDateIsPastRestricted && !eggDateIsFuture && (eggTotalText.toIntOrNull() ?: 0) > 0,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ForestGreenPrimary,
                                    disabledContainerColor = Color(0xFF94A3B8)
                                )
                            ) {
                                Text(
                                    text = when {
                                        eggDateIsPastRestricted -> "PREVIOUS DAYS LOCKED"
                                        eggDateIsFuture -> "FUTURE DATE NOT ALLOWED"
                                        else -> "+ SAVE EGG COLLECTION LOG"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            if (eggDateIsPastRestricted) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Recording egg collections for previous days is disabled for worker accounts. Please select today's date.",
                                    color = Color(0xFFB91C1C),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
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
                                        Text(saveSuccessMessage ?: "", fontSize = 12.sp, color = ForestGreenPrimary, fontWeight = FontWeight.Bold)
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

                                val canDeleteThisEggLog = isOwner || (canEditLogs && (canEditPastDaysLogs || com.example.util.DateValidationUtils.isTodayOrFuture(log.loggedAt, log.updatedAt)))
                                if (canDeleteThisEggLog) {
                                    IconButton(onClick = { onDeleteEggLog(log.id) }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color(0xFF94A3B8))
                                    }
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
