package com.example.ui.screens

import com.example.util.DateValidationUtils

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import com.example.ui.components.AppDatePickerField
import com.example.ui.components.EditAnimalDialog
import com.example.ui.components.AnimalOptionsDialog
import com.example.ui.components.DeleteAnimalConfirmDialog
import com.example.utils.PoultryAgeAndVaccinationUtils
import com.example.utils.VaccineDueStatus
import com.example.data.ReminderCompletion
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.material3.LinearProgressIndicator
import com.example.util.CattleLifecycleEngine
import com.example.util.CattleStage
import com.example.util.CattleStageEvaluation
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.LaunchedEffect
import com.example.ui.FarmViewModel
import com.example.ui.components.AddCattleEventDialog
import com.example.ui.components.AddFinanceRecordDialog
import com.example.ui.components.CalvingCalfInfo
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.AlertDialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.CachePolicy
import coil.size.Precision
import com.example.R
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import com.example.ui.util.ImageStorageUtils
import com.example.ui.components.CameraCaptureDialog
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.toMutableStateList
import com.example.data.CattleEvent
import com.example.data.EggLog
import com.example.data.EmployeeRequest
import com.example.data.FarmUnit
import com.example.data.FinanceRecord
import com.example.data.FinanceType
import com.example.data.MilkLog
import com.example.data.FarmTask
import com.example.data.MilkLogEntryRules
import com.example.data.PoultryLog
import com.example.data.RequestStatus
import com.example.ui.theme.ForestGreenPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.example.ui.theme.TagLivestockBg
import com.example.ui.theme.TagLivestockText
import com.example.ui.theme.TagYieldBg
import com.example.ui.theme.TagYieldText

data class CattleEventItem(
    val id: String,
    val category: String, // "HEAT", "INSEMINATION", "WEIGHT", "HEALTH"
    val title: String,
    val date: String,
    val details: String,
    val notes: String = "",
    val metricValue: String = ""
)

fun parseEventDateForSorting(dateStr: String): Long {
    if (dateStr.isBlank()) return 0L
    val clean = dateStr.trim()
    if (clean.startsWith("Today", ignoreCase = true)) {
        return System.currentTimeMillis()
    }
    if (clean.startsWith("Yesterday", ignoreCase = true) || clean.startsWith("Overdue", ignoreCase = true)) {
        return System.currentTimeMillis() - 86400000L
    }
    if (clean.startsWith("Tomorrow", ignoreCase = true)) {
        return System.currentTimeMillis() + 86400000L
    }
    val formats = listOf(
        "dd MMM yyyy, hh:mm a",
        "dd MMM yyyy, HH:mm",
        "dd MMM yyyy",
        "d MMM yyyy",
        "yyyy-MM-dd",
        "dd/MM/yyyy",
        "MMM dd, yyyy",
        "dd MMM, hh:mm a",
        "dd MMM"
    )
    for (pattern in formats) {
        try {
            val sdf = SimpleDateFormat(pattern, Locale.getDefault())
            val parsed = sdf.parse(clean)
            if (parsed != null) {
                return parsed.time
            }
        } catch (_: Exception) {}
    }
    return 0L
}

data class UpcomingCattleNotification(
    val id: String,
    val title: String,
    val dueDate: String,
    val category: String, // "HEAT_CHECK", "INSEMINATION_PD", "WEIGHT", "HEALTH", "CALVING", "DRY_OFF", "POULTRY_VACCINE", "TASK"
    val badgeColor: Color,
    val badgeTextColor: Color,
    val details: String = "",
    val urgencyLabel: String = "SCHEDULED",
    val actionCategory: String? = null,
    val daysRemaining: Int = 999
)

fun generateAnimalUpcomingEvents(
    animal: AnimalDetailData,
    cattleEval: CattleStageEvaluation?,
    animalEvents: List<CattleEventItem>,
    tasks: List<com.example.data.FarmTask> = emptyList(),
    eggLogs: List<EggLog> = emptyList(),
    isPoultry: Boolean = false,
    poultryLogs: List<PoultryLog> = emptyList(),
    reminderCompletions: List<ReminderCompletion> = emptyList()
): List<UpcomingCattleNotification> {
    val list = mutableListOf<UpcomingCattleNotification>()
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    fun parseDate(dStr: String?): Date? {
        if (dStr.isNullOrBlank()) return null
        return CattleLifecycleEngine.parseDateOrNull(dStr)
    }

    fun getDaysDifference(targetDate: Date): Int {
        val targetCal = Calendar.getInstance().apply {
            time = targetDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val diffMs = targetCal.timeInMillis - today.timeInMillis
        return (diffMs / (1000 * 60 * 60 * 24)).toInt()
    }

    fun addDaysToDate(baseDate: Date, days: Int): Date {
        return Calendar.getInstance().apply {
            time = baseDate
            add(Calendar.DAY_OF_YEAR, days)
        }.time
    }

    val cleanTag = animal.tagNumber.replace("#", "").trim()
    val animalUnitId = animal.id.removePrefix("unit_").toLongOrNull() ?: 0L

    val matchingTasks = tasks.filter { !it.isCompleted &&
        (it.targetUnit.isNotBlank() && (
            it.targetUnit.contains(animal.name, ignoreCase = true) ||
            (cleanTag.isNotBlank() && it.targetUnit.contains(cleanTag, ignoreCase = true))
        ))
    }
    val completedMatchingTasks = tasks.filter { it.isCompleted &&
        (it.targetUnit.isNotBlank() && (
            it.targetUnit.contains(animal.name, ignoreCase = true) ||
            (cleanTag.isNotBlank() && it.targetUnit.contains(cleanTag, ignoreCase = true))
        ))
    }

    val dewormTasks = matchingTasks.filter { 
        it.title.contains("deworm", ignoreCase = true) || 
        it.category.name.contains("DEWORM", ignoreCase = true) ||
        it.instructions?.contains("deworm", ignoreCase = true) == true
    }.sortedBy { parseDate(it.scheduledTime)?.time ?: Long.MAX_VALUE }

    val hasAssignedDewormTask = dewormTasks.isNotEmpty()
    val hasCompletedDewormTask = completedMatchingTasks.any {
        it.title.contains("deworm", ignoreCase = true) ||
        it.category.name.contains("DEWORM", ignoreCase = true) ||
        it.instructions?.contains("deworm", ignoreCase = true) == true
    }
    val hasDewormReminderCompletion = reminderCompletions.any {
        it.unitId == animalUnitId && it.ruleKey.contains("deworm", ignoreCase = true)
    }
    val hasDewormPoultryLog = poultryLogs.any {
        it.unitId == animalUnitId && (it.vaccineName.contains("deworm", ignoreCase = true) || it.notes.contains("deworm", ignoreCase = true))
    }
    val isDewormDoneOrAssigned = hasAssignedDewormTask || hasCompletedDewormTask || hasDewormReminderCompletion || hasDewormPoultryLog

    val vaccineTasks = matchingTasks.filter { 
        it.title.contains("vaccin", ignoreCase = true) || 
        it.title.contains("immuniz", ignoreCase = true) || 
        it.category.name.contains("VACCIN", ignoreCase = true) ||
        it.instructions?.contains("vaccin", ignoreCase = true) == true
    }.sortedBy { parseDate(it.scheduledTime)?.time ?: Long.MAX_VALUE }

    val hasAssignedVaccineTask = vaccineTasks.isNotEmpty()
    val hasCompletedVaccineTask = completedMatchingTasks.any {
        it.title.contains("vaccin", ignoreCase = true) ||
        it.title.contains("immuniz", ignoreCase = true) ||
        it.category.name.contains("VACCIN", ignoreCase = true) ||
        it.instructions?.contains("vaccin", ignoreCase = true) == true
    }
    val hasVaccineReminderCompletion = reminderCompletions.any {
        it.unitId == animalUnitId && it.ruleKey.contains("poultry_vac", ignoreCase = true)
    }
    val hasVaccinePoultryLog = poultryLogs.any {
        it.unitId == animalUnitId && it.logType.equals("VACCINATION", ignoreCase = true)
    }
    val isVaccineDoneOrAssigned = hasAssignedVaccineTask || hasCompletedVaccineTask || hasVaccineReminderCompletion || hasVaccinePoultryLog

    if (isPoultry) {
        val flockAgeInfo = CattleLifecycleEngine.calculateAgeFromDob(animal.dateOfBirth)
        val dobDate = parseDate(animal.dateOfBirth) ?: today.time
        val totalFlockDays = ((today.timeInMillis - dobDate.time) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)

        // Feed Transition Reminders (Week 8 & Week 18)
        if (totalFlockDays in 50..56) {
            val daysUntilGrower = 57 - totalFlockDays
            val transitionDate = addDaysToDate(today.time, daysUntilGrower)
            list.add(
                UpcomingCattleNotification(
                    id = "poultry_feed_grower_${animal.id}",
                    title = "Feed Transition: Introduce Growers",
                    dueDate = sdf.format(transitionDate),
                    category = "FEED_TRANSITION",
                    badgeColor = Color(0xFFFEF3C7),
                    badgeTextColor = Color(0xFFB45309),
                    details = "Start introducing growers feed gradually (Week 8 transition phase for ${animal.name}).",
                    urgencyLabel = if (daysUntilGrower <= 1) "DUE NOW" else "DUE SOON",
                    actionCategory = "FEED",
                    daysRemaining = daysUntilGrower
                )
            )
        } else if (totalFlockDays in 120..126) {
            val daysUntilLayer = 127 - totalFlockDays
            val transitionDate = addDaysToDate(today.time, daysUntilLayer)
            list.add(
                UpcomingCattleNotification(
                    id = "poultry_feed_layer_${animal.id}",
                    title = "Feed Transition: Introduce Layers",
                    dueDate = sdf.format(transitionDate),
                    category = "FEED_TRANSITION",
                    badgeColor = Color(0xFFFEF3C7),
                    badgeTextColor = Color(0xFFB45309),
                    details = "Start introducing layers feed gradually (Week 18 transition phase for ${animal.name}).",
                    urgencyLabel = if (daysUntilLayer <= 1) "DUE NOW" else "DUE SOON",
                    actionCategory = "FEED",
                    daysRemaining = daysUntilLayer
                )
            )
        }

        // Routine Deworming (every 8 weeks / 56 days) - only add if not already done or assigned
        if (!isDewormDoneOrAssigned) {
            val dewormDueDays = (56 - (totalFlockDays % 56)).coerceIn(-10, 56)
            val dewormDate = addDaysToDate(today.time, dewormDueDays)
            list.add(
                UpcomingCattleNotification(
                    id = "poultry_deworm_${animal.id}",
                    title = "Flock Routine Deworming",
                    dueDate = sdf.format(dewormDate),
                    category = "HEALTH",
                    badgeColor = if (dewormDueDays <= 0) Color(0xFFFEE2E2) else Color(0xFFDCFCE7),
                    badgeTextColor = if (dewormDueDays <= 0) Color(0xFFB91C1C) else Color(0xFF15803D),
                    details = "Periodic internal parasite & worm control for ${animal.name} ($flockAgeInfo).",
                    urgencyLabel = if (dewormDueDays <= 0) "DUE NOW" else if (dewormDueDays <= 7) "DUE SOON" else "SCHEDULED",
                    actionCategory = "HEALTH",
                    daysRemaining = dewormDueDays
                )
            )
        }

        // Newcastle / Gumboro Booster - only add if not already done or assigned
        if (!isVaccineDoneOrAssigned) {
            val vacDueDays = (90 - (totalFlockDays % 90)).coerceIn(-10, 90)
            val vacDate = addDaysToDate(today.time, vacDueDays)
            list.add(
                UpcomingCattleNotification(
                    id = "poultry_vac_${animal.id}",
                    title = "Newcastle / Gumboro Booster",
                    dueDate = sdf.format(vacDate),
                    category = "POULTRY_VACCINE",
                    badgeColor = Color(0xFFFEF3C7),
                    badgeTextColor = Color(0xFFB45309),
                    details = "Immunization booster via drinking water or eye drop to sustain flock immunity.",
                    urgencyLabel = if (vacDueDays <= 0) "DUE NOW" else if (vacDueDays <= 7) "DUE SOON" else "SCHEDULED",
                    actionCategory = "HEALTH",
                    daysRemaining = vacDueDays
                )
            )
        }
    } else {
        // Cattle Dynamic Lifecycle Events & Reminders

        // 1. Expected Calving Date
        if (cattleEval?.expectedCalvingDate != null) {
            val calvDate = parseDate(cattleEval.expectedCalvingDate)
            val daysLeft = if (calvDate != null) getDaysDifference(calvDate) else (283 - (cattleEval.gestationDays ?: 200))
            val (urgency, bColor, tColor) = when {
                daysLeft <= 0 -> Triple("DUE / IMMINENT", Color(0xFFFEE2E2), Color(0xFFB91C1C))
                daysLeft <= 14 -> Triple("DUE SOON", Color(0xFFFEF3C7), Color(0xFFB45309))
                else -> Triple("EXPECTED", Color(0xFFFEF3C7), Color(0xFFB45309))
            }
            list.add(
                UpcomingCattleNotification(
                    id = "notif_calving_${animal.id}",
                    title = "Expected Calving — ${animal.name}",
                    dueDate = cattleEval.expectedCalvingDate,
                    category = "CALVING",
                    badgeColor = bColor,
                    badgeTextColor = tColor,
                    details = when {
                        daysLeft <= 0 -> "Calving due date reached! Monitor closely for labor signs (water bag, restlessness, udder distension)."
                        daysLeft <= 14 -> "Imminent calving window (${daysLeft} days remaining). Move to clean maternity pen & provide steam-up ration."
                        daysLeft <= 30 -> "Late gestation (Day ${cattleEval.gestationDays ?: 253} of 283). Prepare maternity pen and mineral lick."
                        else -> "Gestation in progress (Day ${cattleEval.gestationDays ?: 150} of 283). Expected delivery on ${cattleEval.expectedCalvingDate}."
                    },
                    urgencyLabel = urgency,
                    actionCategory = "CALVING",
                    daysRemaining = daysLeft
                )
            )

            // 2. Target Dry-Off Date (Rest Period - 60 days before calving)
            if (cattleEval.isMilking && cattleEval.dryOffTargetDate != null && !cattleEval.isDriedOff) {
                val dryDate = parseDate(cattleEval.dryOffTargetDate)
                val dryDaysLeft = if (dryDate != null) getDaysDifference(dryDate) else (daysLeft - 60)
                list.add(
                    UpcomingCattleNotification(
                        id = "notif_dry_${animal.id}",
                        title = "Target Dry-Off (Udder Rest Period)",
                        dueDate = cattleEval.dryOffTargetDate,
                        category = "DRY_OFF",
                        badgeColor = if (dryDaysLeft <= 7) Color(0xFFFEE2E2) else Color(0xFFDCFCE7),
                        badgeTextColor = if (dryDaysLeft <= 7) Color(0xFFB91C1C) else Color(0xFF15803D),
                        details = "Cease daily milking, infuse dry cow intramammary antibiotic & teat sealant for 60-day involution.",
                        urgencyLabel = if (dryDaysLeft <= 0) "DUE NOW" else if (dryDaysLeft <= 14) "DUE SOON" else "SCHEDULED",
                        actionCategory = "HEALTH",
                        daysRemaining = dryDaysLeft
                    )
                )
            }
        }

        // 3. Insemination Follow-ups (Repeat Heat & PD Check)
        if (cattleEval?.lastInseminationDate != null && !cattleEval.isInCalf) {
            val aiDate = parseDate(cattleEval.lastInseminationDate)
            if (aiDate != null) {
                val daysPostAi = ((today.timeInMillis - aiDate.time) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)

                // Repeat Heat Check (Day 18-24)
                val heatDueDays = 21 - daysPostAi
                val heatDateStr = sdf.format(addDaysToDate(aiDate, 21))
                if (daysPostAi <= 28) {
                    list.add(
                        UpcomingCattleNotification(
                            id = "notif_repeat_heat_${animal.id}",
                            title = "Repeat Heat Observation (18-24d post AI)",
                            dueDate = heatDateStr,
                            category = "HEAT_CHECK",
                            badgeColor = if (heatDueDays in -2..2) Color(0xFFEDE9FE) else Color(0xFFF1F5F9),
                            badgeTextColor = if (heatDueDays in -2..2) Color(0xFF6D28D9) else Color(0xFF475569),
                            details = "Monitor for standing heat signs (mucus, mounting). If in estrus, serve repeat AI immediately.",
                            urgencyLabel = if (heatDueDays in -2..2) "ACTIVE WINDOW" else if (heatDueDays < -2) "WINDOW PASSED" else "UPCOMING",
                            actionCategory = "HEAT",
                            daysRemaining = heatDueDays
                        )
                    )
                }

                // 60-Day Pregnancy Diagnosis (PD) Check
                val pdDueDays = 60 - daysPostAi
                val pdDateStr = sdf.format(addDaysToDate(aiDate, 60))
                list.add(
                    UpcomingCattleNotification(
                        id = "notif_pd_check_${animal.id}",
                        title = "Pregnancy Diagnosis (PD) Check",
                        dueDate = pdDateStr,
                        category = "INSEMINATION_PD",
                        badgeColor = if (pdDueDays <= 7) Color(0xFFE0F2FE) else Color(0xFFF0FDF4),
                        badgeTextColor = if (pdDueDays <= 7) Color(0xFF0369A1) else Color(0xFF15803D),
                        details = "60-Day post-AI veterinary rectal palpation or ultrasound to confirm conception.",
                        urgencyLabel = if (pdDueDays <= 0) "DUE NOW" else if (pdDueDays <= 14) "DUE SOON" else "SCHEDULED",
                        actionCategory = "PD",
                        daysRemaining = pdDueDays
                    )
                )

                // Projected Calving Date (if conceived)
                val estCalvingDate = addDaysToDate(aiDate, 283)
                val estDaysLeft = getDaysDifference(estCalvingDate)
                list.add(
                    UpcomingCattleNotification(
                        id = "notif_projected_calving_${animal.id}",
                        title = "Projected Calving (If Conceived)",
                        dueDate = sdf.format(estCalvingDate),
                        category = "CALVING",
                        badgeColor = Color(0xFFFEF3C7),
                        badgeTextColor = Color(0xFFB45309),
                        details = "283-day gestation projection from AI service on ${cattleEval.lastInseminationDate}.",
                        urgencyLabel = "PROJECTED",
                        actionCategory = "CALVING",
                        daysRemaining = estDaysLeft
                    )
                )
            }
        }

        // 4. Post-Calving Milestones (Uterine Check & Voluntary Waiting Period)
        if (cattleEval?.lastCalvingDate != null && cattleEval.lastInseminationDate == null && !cattleEval.isInCalf) {
            val calvingDate = parseDate(cattleEval.lastCalvingDate)
            if (calvingDate != null) {
                val daysPostCalving = ((today.timeInMillis - calvingDate.time) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)

                // Voluntary Waiting Period / Breeding Window (Day 60)
                val vwpDueDays = 60 - daysPostCalving
                val vwpDateStr = sdf.format(addDaysToDate(calvingDate, 60))
                list.add(
                    UpcomingCattleNotification(
                        id = "notif_vwp_${animal.id}",
                        title = "Breeding Window Opens (60d Post-Calving)",
                        dueDate = vwpDateStr,
                        category = "INSEMINATION_PD",
                        badgeColor = Color(0xFFDCFCE7),
                        badgeTextColor = Color(0xFF15803D),
                        details = "Voluntary waiting period ends. Cow is eligible for heat detection and first AI service.",
                        urgencyLabel = if (vwpDueDays <= 0) "OPEN FOR BREEDING" else "UPCOMING",
                        actionCategory = "INSEMINATION",
                        daysRemaining = vwpDueDays
                    )
                )
            }
        }

        // 5. Health & Maintenance Reminders (Deworming, Vaccination, Weight)
        if (!hasAssignedDewormTask) {
            val sortedEvents = animalEvents.sortedByDescending { parseDate(it.date)?.time ?: 0L }
            val latestDeworm = sortedEvents.firstOrNull { it.category.equals("DEWORMING", ignoreCase = true) || it.title.contains("Deworm", ignoreCase = true) }
            val dewormBaseDate = parseDate(latestDeworm?.date) ?: parseDate(animal.dateOfBirth) ?: today.time
            val daysSinceDeworm = ((today.timeInMillis - dewormBaseDate.time) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
            val nextDewormDueDays = (90 - (daysSinceDeworm % 90)).coerceIn(-15, 90)
            val nextDewormDateStr = sdf.format(addDaysToDate(today.time, nextDewormDueDays))

            list.add(
                UpcomingCattleNotification(
                    id = "notif_deworm_${animal.id}",
                    title = "Routine Quarterly Deworming",
                    dueDate = nextDewormDateStr,
                    category = "HEALTH",
                    badgeColor = if (nextDewormDueDays <= 0) Color(0xFFFEE2E2) else Color(0xFFF1F5F9),
                    badgeTextColor = if (nextDewormDueDays <= 0) Color(0xFFB91C1C) else Color(0xFF334155),
                    details = "Administer Albendazole or Ivermectin for internal parasite and liver fluke control.",
                    urgencyLabel = if (nextDewormDueDays <= 0) "DUE NOW" else if (nextDewormDueDays <= 7) "DUE SOON" else "SCHEDULED",
                    actionCategory = "HEALTH",
                    daysRemaining = nextDewormDueDays
                )
            )
        }

        if (!hasAssignedVaccineTask) {
            val sortedEvents = animalEvents.sortedByDescending { parseDate(it.date)?.time ?: 0L }
            val latestVac = sortedEvents.firstOrNull { it.category.equals("VACCINATION", ignoreCase = true) || it.title.contains("Vaccin", ignoreCase = true) }
            val vacBaseDate = parseDate(latestVac?.date) ?: parseDate(animal.dateOfBirth) ?: today.time
            val daysSinceVac = ((today.timeInMillis - vacBaseDate.time) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
            val nextVacDueDays = (180 - (daysSinceVac % 180)).coerceIn(-15, 180)
            val nextVacDateStr = sdf.format(addDaysToDate(today.time, nextVacDueDays))

            list.add(
                UpcomingCattleNotification(
                    id = "notif_vac_${animal.id}",
                    title = "Livestock Booster Vaccination",
                    dueDate = nextVacDateStr,
                    category = "HEALTH",
                    badgeColor = Color(0xFFFEF3C7),
                    badgeTextColor = Color(0xFFB45309),
                    details = "Scheduled herd immunization booster to maintain protective immunity against endemic diseases.",
                    urgencyLabel = if (nextVacDueDays <= 0) "DUE NOW" else if (nextVacDueDays <= 14) "DUE SOON" else "SCHEDULED",
                    actionCategory = "HEALTH",
                    daysRemaining = nextVacDueDays
                )
            )
        }
    }

    // Pending Farm Tasks matching this unit/animal (deduplicate duplicate pending deworming/vaccine tasks)
    val deduplicatedTasks = mutableListOf<com.example.data.FarmTask>()
    var includedDewormTask = false
    var includedVaccineTask = false
    matchingTasks.sortedBy { parseDate(it.scheduledTime)?.time ?: Long.MAX_VALUE }.forEach { task ->
        val isDeworm = task.title.contains("deworm", ignoreCase = true) ||
            task.category.name.contains("DEWORM", ignoreCase = true) ||
            task.instructions?.contains("deworm", ignoreCase = true) == true
        val isVaccine = task.title.contains("vaccin", ignoreCase = true) ||
            task.title.contains("immuniz", ignoreCase = true) ||
            task.category.name.contains("VACCIN", ignoreCase = true) ||
            task.instructions?.contains("vaccin", ignoreCase = true) == true

        if (isDeworm) {
            if (!includedDewormTask) {
                deduplicatedTasks.add(task)
                includedDewormTask = true
            }
        } else if (isVaccine) {
            if (!includedVaccineTask) {
                deduplicatedTasks.add(task)
                includedVaccineTask = true
            }
        } else {
            deduplicatedTasks.add(task)
        }
    }

    deduplicatedTasks.forEach { task ->
        val taskDate = parseDate(task.scheduledTime)
        val taskDaysLeft = if (taskDate != null) getDaysDifference(taskDate) else 0
        list.add(
            UpcomingCattleNotification(
                id = "task_${task.id}",
                title = "Task: ${task.title}",
                dueDate = task.scheduledTime,
                category = "TASK",
                badgeColor = if (taskDaysLeft <= 0) Color(0xFFFEE2E2) else Color(0xFFE0F2FE),
                badgeTextColor = if (taskDaysLeft <= 0) Color(0xFFB91C1C) else Color(0xFF0369A1),
                details = task.instructions?.ifBlank { "Assigned task for ${animal.name}." } ?: "Assigned task for ${animal.name}.",
                urgencyLabel = if (taskDaysLeft <= 0) "OVERDUE" else if (taskDaysLeft <= 2) "DUE SOON" else "ASSIGNED",
                actionCategory = "HEALTH",
                daysRemaining = taskDaysLeft
            )
        )
    }

    // Ensure strictly 1 routine deworming reminder/task is presented per animal
    var seenDeworm = false
    val singleDewormList = list.filter { notif ->
        val isDeworm = notif.id.contains("deworm", ignoreCase = true) ||
            notif.title.contains("deworm", ignoreCase = true) ||
            notif.details.contains("deworm", ignoreCase = true)
        if (isDeworm) {
            if (seenDeworm) false else { seenDeworm = true; true }
        } else {
            true
        }
    }

    return singleDewormList.distinctBy { it.id }.sortedWith(
        compareBy<UpcomingCattleNotification> {
            when (it.urgencyLabel) {
                "DUE / IMMINENT", "DUE NOW", "OVERDUE" -> 1
                "DUE SOON", "ACTIVE WINDOW" -> 2
                "OPEN FOR BREEDING" -> 3
                "EXPECTED", "PROJECTED" -> 4
                else -> 5
            }
        }.thenBy { it.daysRemaining }
    )
}


data class AnimalDetailData(
    val id: String,
    val name: String,
    val tagNumber: String,
    val breed: String,
    val category: String, // CATTLE, POULTRY
    val status: String, // MILKING, PREGNANT, NOT PREGNANT, DRY, ACTIVE, DISPOSED
    val age: String,
    val weight: String,
    val lastMilk: String,
    val breedingStatus: String,
    val expectedCalving: String = "Jun 21, '24",
    val insemination: String = "AI - Thunder (Sep 12, '23)",
    val dateOfBirth: String = "12 Apr 2021",
    val weightAtBirth: String = "32 kg",
    val sire: String = "Thunder #045",
    val dam: String = "Bessie #102",
    val disposalReason: String = "",
    val disposalAmount: Double = 0.0,
    val disposalDate: String = "",
    val disposalNotes: String = "",
    val headCountInt: Int = 1,
    val manuallySetStatus: String? = null,
    val photoUri: String? = null,
    val notes: String = "",
    val isArchived: Boolean = false
)

data class FlockDisposalLogItem(
    val id: String,
    val recordId: Long,
    val flockName: String,
    val quantity: Int,
    val reason: String, // "Sold", "Death", "Home Consumption", "Other"
    val amount: Double,
    val date: String,
    val notes: String,
    val linkedMortalityLogId: String? = null
)

val mockAnimals: List<AnimalDetailData> = emptyList()

@Composable
fun DisposeAnimalDialog(
    animalName: String,
    tagNumber: String,
    onDismiss: () -> Unit,
    onConfirmDispose: (reason: String, amount: Double, notes: String, date: String) -> Unit
) {
    var reason by remember { mutableStateOf("Sold") } // "Sold", "Dead", "Other"
    var notesText by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf(SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    if (showConfirmDialog) {
        com.example.ui.components.ConfirmDeleteDialog(
            title = "Confirm Animal Disposal",
            message = "Are you sure you want to record the disposal of $animalName ($tagNumber) as '$reason'? This record will be archived.",
            confirmButtonText = "Confirm Disposal",
            confirmButtonColor = Color(0xFFDC2626),
            onConfirm = {
                showConfirmDialog = false
                onConfirmDispose(reason, 0.0, notesText, dateText)
            },
            onDismiss = {
                showConfirmDialog = false
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(20.dp)),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Dispose Animal",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "$animalName ($tagNumber)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ForestGreenPrimary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Disposal Reason:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Sold", "Dead", "Other").forEach { r ->
                        val isSel = reason.equals(r, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) ForestGreenPrimary else Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, if (isSel) ForestGreenPrimary else Color(0xFFCBD5E1)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { reason = r }
                        ) {
                            Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (r == "Sold") "Sold" else if (r == "Dead") " ï¸ Dead" else "Other",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White else Color(0xFF475569)
                                )
                            }
                        }
                    }
                }

                if (reason == "Sold") {
                    // amount field removed
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text("Buyer / Destination / Details:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    placeholder = { Text(if (reason == "Sold") "Buyer name or market location..." else "Cause or reason for disposal...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))
                AppDatePickerField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = "Disposal Date",
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "animal_disposal_date_picker"
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage!!, fontSize = 12.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("CANCEL")
                    }
                    Button(
                        onClick = {
                            showConfirmDialog = true
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                    ) {
                        Text("CONFIRM DISPOSAL", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun DisposeFlockDialog(
    flockName: String,
    currentHeadCount: Int,
    onDismiss: () -> Unit,
    onConfirmDisposeFlock: (quantity: Int, reason: String, amount: Double, notes: String, date: String) -> Unit
) {
    var reason by remember { mutableStateOf("Sold") } // "Sold", "Death", "Home Consumption", "Other"
    var qtyText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf(SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    if (showConfirmDialog) {
        val qty = qtyText.toIntOrNull() ?: 0
        com.example.ui.components.ConfirmDeleteDialog(
            title = "Confirm Flock Disposal",
            message = "Are you sure you want to dispose $qty birds from $flockName as '$reason'? This will update the active flock size.",
            confirmButtonText = "Confirm Disposal",
            confirmButtonColor = Color(0xFFDC2626),
            onConfirm = {
                showConfirmDialog = false
                onConfirmDisposeFlock(qty, reason, 0.0, notesText, dateText)
            },
            onDismiss = {
                showConfirmDialog = false
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(20.dp)),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = " Dispose Birds from Flock",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "$flockName ($currentHeadCount Birds Available)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ForestGreenPrimary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Disposal Reason:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Sold", "Death").forEach { r ->
                            val isSel = reason.equals(r, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) ForestGreenPrimary else Color(0xFFF1F5F9),
                                border = BorderStroke(1.dp, if (isSel) ForestGreenPrimary else Color(0xFFCBD5E1)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { reason = r }
                            ) {
                                Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (r == "Sold") "Sold" else " ï¸ Death / Loss",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) Color.White else Color(0xFF475569)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Home Consumption", "Other").forEach { r ->
                            val isSel = reason.equals(r, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) ForestGreenPrimary else Color(0xFFF1F5F9),
                                border = BorderStroke(1.dp, if (isSel) ForestGreenPrimary else Color(0xFFCBD5E1)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { reason = r }
                            ) {
                                Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (r == "Home Consumption") " Home Consumption" else "Other",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) Color.White else Color(0xFF475569)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text("Quantity of Birds to Dispose:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = qtyText,
                    onValueChange = { qtyText = it; errorMessage = null },
                    placeholder = { Text("e.g. 50") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                if (reason == "Sold" || reason == "Home Consumption") {
                    // amount field removed; will prompt for finance record separately
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text("Buyer / Destination / Notes:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    placeholder = { Text("e.g. Sold 50 broilers to local restaurant") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))
                AppDatePickerField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = "Disposal Date",
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "flock_disposal_date_picker"
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage!!, fontSize = 12.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("CANCEL")
                    }
                    Button(
                        onClick = {
                            val qty = qtyText.toIntOrNull() ?: 0
                            if (qty <= 0) {
                                errorMessage = "Please enter a valid bird quantity."
                                return@Button
                            }
                            if (qty > currentHeadCount) {
                                errorMessage = "Quantity cannot exceed current flock size ($currentHeadCount)."
                                return@Button
                            }
                            showConfirmDialog = true
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                    ) {
                        Text("CONFIRM DISPOSAL", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun FlocksScreen(
    viewModel: FarmViewModel,
    userRole: String, // Add userRole
    units: List<FarmUnit>,
    archivedUnits: List<FarmUnit> = emptyList(),
    milkLogs: List<MilkLog>,
    eggLogs: List<EggLog>,
    financeRecords: List<FinanceRecord>,
    employeeRequests: List<EmployeeRequest>,
    onAddUnitClick: () -> Unit,
    onAddTaskForUnit: (FarmUnit) -> Unit,
    onAddMilkLogClick: () -> Unit,
    onAddEggLogClick: () -> Unit,
    onAddFinanceClick: () -> Unit,
    onAddEmployeeRequestClick: () -> Unit,
    onUpdateRequestStatus: (EmployeeRequest, RequestStatus) -> Unit,
    onAddFinanceRecord: (type: FinanceType, category: String, amount: Double, description: String) -> Unit = { _, _, _, _ -> },
    onUpdateUnitHeadCount: (unitId: Long, newHeadCount: Int) -> Unit = { _, _ -> },
    onUpdateUnit: (FarmUnit) -> Unit = { _ -> },
    onDeleteUnit: (Long) -> Unit,
    farmSettings: com.example.data.FarmSettings,
    canEditLivestock: Boolean = true,
    modifier: Modifier = Modifier
) {
    val isOwner = userRole.equals("OWNER", ignoreCase = true)
    val effectiveCanEditLivestock = isOwner || canEditLivestock
    var selectedAnimal by remember { mutableStateOf<AnimalDetailData?>(null) }
    var animalForOptions by remember { mutableStateOf<AnimalDetailData?>(null) }
    var animalToEdit by remember { mutableStateOf<AnimalDetailData?>(null) }
    var animalToDelete by remember { mutableStateOf<AnimalDetailData?>(null) }
    var animalToDispose by remember { mutableStateOf<AnimalDetailData?>(null) }
    var flockToDispose by remember { mutableStateOf<AnimalDetailData?>(null) }
    var showFinancePromptForDisposal by remember { mutableStateOf<Pair<String, String>?>(null) } // Pair(Description, InitialTargetUnit)
    var showRecordFinanceDialog by remember { mutableStateOf(false) }
    var pendingFinanceCategory by remember { mutableStateOf("Animal Sale") }
    var pendingFinanceDescription by remember { mutableStateOf("") }
    var pendingFinanceDate by remember { mutableStateOf("") }
    var pendingFinanceTarget by remember { mutableStateOf("") }
    val initialCategory = if (farmSettings.farmType.equals("Poultry Only", ignoreCase = true)) "POULTRY" else "CATTLE"
    var selectedFilterCategory by remember(farmSettings.farmType) { mutableStateOf(initialCategory) }
    var selectedStatusFilter by remember { mutableStateOf("ACTIVE") } // "ACTIVE" or "ARCHIVED"
    var selectedCattleStage by remember { mutableStateOf("ALL") }
    var showCategoryGuideDialog by remember { mutableStateOf(false) }

    val hasFlocksOverlay = (selectedAnimal != null) || (animalForOptions != null) ||
            (animalToEdit != null) || (animalToDelete != null) || (animalToDispose != null) ||
            (flockToDispose != null) || showCategoryGuideDialog

    BackHandler(enabled = hasFlocksOverlay) {
        when {
            animalForOptions != null -> animalForOptions = null
            animalToEdit != null -> animalToEdit = null
            animalToDelete != null -> animalToDelete = null
            animalToDispose != null -> animalToDispose = null
            flockToDispose != null -> flockToDispose = null
            showCategoryGuideDialog -> showCategoryGuideDialog = false
            selectedAnimal != null -> selectedAnimal = null
        }
    }

    LaunchedEffect(farmSettings.farmType) {
        if (farmSettings.farmType.equals("Poultry Only", ignoreCase = true)) {
            selectedFilterCategory = "POULTRY"
        } else if (farmSettings.farmType.equals("Cattle Only", ignoreCase = true)) {
            selectedFilterCategory = "CATTLE"
        }
    }

    val allDbCattleEvents by viewModel.allCattleEvents.collectAsStateWithLifecycle(initialValue = viewModel.allCattleEvents.value)
    val allDbPoultryLogs by viewModel.allPoultryLogs.collectAsStateWithLifecycle(initialValue = viewModel.allPoultryLogs.value)
    val reminderCompletions by viewModel.reminderCompletions.collectAsStateWithLifecycle(initialValue = viewModel.reminderCompletions.value)
    val rawTasks by viewModel.rawTasks.collectAsStateWithLifecycle(initialValue = viewModel.rawTasks.value)
    val currentSession by viewModel.currentSession.collectAsStateWithLifecycle()

    val milkLogsByCow = remember(milkLogs) {
        milkLogs.groupBy { log -> log.cowName.trim().lowercase() }
    }
    val cattleEventsByUnit = remember(allDbCattleEvents) {
        allDbCattleEvents.groupBy { it.unitId }.mapValues { (_, events) ->
            events.map {
                CattleEventItem(
                    id = it.id.toString(),
                    category = it.category,
                    title = it.title,
                    date = it.date,
                    details = it.details,
                    notes = it.notes ?: "",
                    metricValue = it.metricValue ?: ""
                )
            }
        }
    }
    val targetUnits = remember(selectedStatusFilter, units, archivedUnits) {
        if (selectedStatusFilter == "ACTIVE") {
            units.filter { !it.isArchived && !it.healthStatus.contains("DISPOSED", ignoreCase = true) && it.headCount > 0 }
        } else {
            (archivedUnits + units.filter { it.isArchived || it.healthStatus.contains("DISPOSED", ignoreCase = true) || it.headCount == 0 }).distinctBy { it.id }
        }
    }
    val roomAnimals = remember(targetUnits, units, archivedUnits, milkLogs, cattleEventsByUnit, eggLogs, allDbPoultryLogs, selectedStatusFilter) {
        if (selectedStatusFilter == "ACTIVE") {
            targetUnits.map { unit ->
                val isPoultry = unit.type.equals("POULTRY", ignoreCase = true) || unit.type.contains("Poultry", ignoreCase = true) || unit.breed.contains("Layer", ignoreCase = true) || unit.breed.contains("Flock", ignoreCase = true)
                val isHeiferOrCalfOrBull = !isPoultry && (
                    unit.healthStatus.contains("Heifer", ignoreCase = true) ||
                    (unit.healthStatus.contains("Calf", ignoreCase = true) && !unit.healthStatus.contains("In-Calf", ignoreCase = true) && !unit.healthStatus.contains("InCalf", ignoreCase = true)) ||
                    unit.healthStatus.contains("Bull", ignoreCase = true) ||
                    unit.healthStatus.contains("Steer", ignoreCase = true)
                )
                val cowLogs = MilkLogEntryRules.findLogsForCow(milkLogs, unit.name, unit.tagNumber)
                val unitDbEvents = cattleEventsByUnit[unit.id].orEmpty()
                val calculatedAge = if (unit.dob.isNotBlank()) {
                    CattleLifecycleEngine.calculateAgeFromDob(unit.dob)
                } else {
                    "1y"
                }

                val baseAnimalDetail = AnimalDetailData(
                    id = "unit_${unit.id}",
                    name = unit.name,
                    tagNumber = if (unit.tagNumber.isNotBlank()) unit.tagNumber else if (isPoultry) "Count: ${unit.headCount}" else "#${unit.id + 100}",
                    breed = unit.breed.ifBlank { if (isPoultry) "Poultry Flock" else "Local Breed" },
                    category = if (isPoultry) "POULTRY" else "CATTLE",
                    status = unit.healthStatus.ifBlank { "ACTIVE" },
                    age = calculatedAge,
                    weight = unitDbEvents
                        .filter { it.category.equals("WEIGHT", ignoreCase = true) }
                        .maxByOrNull { CattleLifecycleEngine.parseDateOrNull(it.date)?.time ?: 0L }
                        ?.metricValue
                        ?.takeIf { it.isNotBlank() }
                        ?: unit.currentWeight.ifBlank { if (isPoultry) "1.8kg avg" else "450kg" },
                    lastMilk = "No data yet",
                    breedingStatus = if (unit.healthStatus.isNotBlank() && !unit.healthStatus.equals("ACTIVE", ignoreCase = true) && !unit.healthStatus.equals("OPTIMAL", ignoreCase = true)) unit.healthStatus else "HEALTHY",
                    dateOfBirth = unit.dob.ifBlank { "12 Apr 2023" },
                    weightAtBirth = unit.weightAtBirth.ifBlank { "32 kg" },
                    sire = unit.sire.ifBlank { "N/A" },
                    dam = unit.dam.ifBlank { "N/A" },
                    disposalReason = "",
                    disposalDate = "",
                    disposalAmount = 0.0,
                    disposalNotes = "",
                    headCountInt = unit.headCount,
                    photoUri = unit.photoUri,
                    notes = unit.notes,
                    isArchived = false
                )

                val eval = CattleLifecycleEngine.evaluateCattleStage(baseAnimalDetail, unitDbEvents, cowLogs)

                val lastMilkStr = if (isPoultry) {
                    val poultryEggLogs = eggLogs.filter { it.unitName.equals(unit.name, ignoreCase = true) || it.unitName.contains(unit.name, ignoreCase = true) }
                        .sortedByDescending { it.id }
                    if (poultryEggLogs.isNotEmpty()) "${poultryEggLogs.first().totalEggs} Eggs" else "${unit.headCount} Birds"
                } else if (eval.stage == CattleStage.HEIFER || eval.stage == CattleStage.CALF || eval.stage == CattleStage.BULL || isHeiferOrCalfOrBull) {
                    "Not Lactating"
                } else if (cowLogs.isNotEmpty()) {
                    "${"%.1f".format(cowLogs.first().litres)}L"
                } else {
                    "No data yet"
                }

                val newStatus = if (unitDbEvents.isNotEmpty() || baseAnimalDetail.status.isBlank() || baseAnimalDetail.status.equals("ACTIVE", ignoreCase = true) || baseAnimalDetail.status.equals("OPTIMAL", ignoreCase = true)) eval.stage.displayName else baseAnimalDetail.status

                baseAnimalDetail.copy(
                    status = newStatus,
                    lastMilk = lastMilkStr,
                    breedingStatus = if (eval.breedingStatusText.isNotBlank()) eval.breedingStatusText else baseAnimalDetail.breedingStatus
                )
            }
        } else {
            val allUnitsCombined = (units + archivedUnits).distinctBy { it.id }

            // 1. Every recorded disposal log for poultry (full or partial)
            val poultryDisposalLogs = allDbPoultryLogs
                .filter { it.logType == "DISPOSAL" }
                .sortedByDescending { it.id }
                .map { log ->
                    val parentUnit = allUnitsCombined.find { it.id == log.unitId }
                    AnimalDetailData(
                        id = "poultry_disposal_log_${log.id}",
                        name = parentUnit?.name ?: (if (log.notes.isNotBlank() && !log.notes.startsWith("Partial disposal")) log.notes else "Poultry Flock"),
                        tagNumber = "Disposed: ${log.birdCount}",
                        breed = parentUnit?.breed?.ifBlank { "Layers" } ?: "Layers",
                        category = "POULTRY",
                        status = "DISPOSED (${log.disposalReason.ifBlank { "Disposed" }})",
                        age = "",
                        weight = "",
                        lastMilk = "${log.birdCount} Birds",
                        breedingStatus = "DISPOSED",
                        dateOfBirth = parentUnit?.dob ?: "",
                        weightAtBirth = "",
                        sire = "",
                        dam = "",
                        disposalReason = log.disposalReason.ifBlank { "Disposed" },
                        disposalDate = log.date,
                        disposalAmount = log.disposalAmount,
                        disposalNotes = log.notes,
                        headCountInt = log.birdCount,
                        photoUri = parentUnit?.photoUri,
                        notes = log.notes,
                        isArchived = true
                    )
                }

            // 2. Any archived poultry unit with no individual disposal logs
            val standaloneArchivedPoultry = targetUnits
                .filter { unit ->
                    val isPoultry = unit.type.equals("POULTRY", ignoreCase = true) || unit.type.contains("Poultry", ignoreCase = true) || unit.breed.contains("Layer", ignoreCase = true) || unit.breed.contains("Flock", ignoreCase = true)
                    isPoultry && allDbPoultryLogs.none { it.unitId == unit.id && it.logType == "DISPOSAL" }
                }
                .map { unit ->
                    val disposalReasonExtracted = if (unit.healthStatus.contains("DISPOSED", ignoreCase = true)) {
                        unit.healthStatus.substringAfter("(", "").substringBefore(")", "").ifBlank { "Disposed" }
                    } else "Disposed"
                    val resolvedDisposalDate = unit.lastUpdated.takeIf { it.isNotBlank() }
                        ?: unit.dateAdded.takeIf { it.isNotBlank() }
                        ?: SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(unit.updatedAt))
                    AnimalDetailData(
                        id = "unit_${unit.id}",
                        name = unit.name,
                        tagNumber = if (unit.tagNumber.isNotBlank()) unit.tagNumber else "Count: ${unit.headCount}",
                        breed = unit.breed.ifBlank { "Poultry Flock" },
                        category = "POULTRY",
                        status = "DISPOSED ($disposalReasonExtracted)",
                        age = "",
                        weight = "",
                        lastMilk = "${unit.headCount} Birds",
                        breedingStatus = "DISPOSED",
                        dateOfBirth = unit.dob.ifBlank { "" },
                        weightAtBirth = "",
                        sire = "",
                        dam = "",
                        disposalReason = disposalReasonExtracted,
                        disposalDate = resolvedDisposalDate,
                        disposalAmount = 0.0,
                        disposalNotes = unit.notes,
                        headCountInt = unit.headCount,
                        photoUri = unit.photoUri,
                        notes = unit.notes,
                        isArchived = true
                    )
                }

            // 3. Disposed / Archived cattle units
            val archivedCattle = targetUnits
                .filter { unit ->
                    val isPoultry = unit.type.equals("POULTRY", ignoreCase = true) || unit.type.contains("Poultry", ignoreCase = true) || unit.breed.contains("Layer", ignoreCase = true) || unit.breed.contains("Flock", ignoreCase = true)
                    !isPoultry
                }
                .map { unit ->
                    val unitDbEvents = cattleEventsByUnit[unit.id].orEmpty()
                    val calculatedAge = if (unit.dob.isNotBlank()) CattleLifecycleEngine.calculateAgeFromDob(unit.dob) else "1y"
                    val disposalReasonExtracted = if (unit.healthStatus.contains("DISPOSED", ignoreCase = true)) {
                        unit.healthStatus.substringAfter("(", "").substringBefore(")", "").ifBlank { "Disposed" }
                    } else ""
                    val cattleDisposalEvent = unitDbEvents.lastOrNull { it.category.equals("DISPOSAL", ignoreCase = true) || it.title.contains("Dispos", ignoreCase = true) }
                    val resolvedDisposalDate = cattleDisposalEvent?.date
                        ?: unit.lastUpdated.takeIf { it.isNotBlank() }
                        ?: unit.dateAdded.takeIf { it.isNotBlank() }
                        ?: SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(unit.updatedAt))
                    AnimalDetailData(
                        id = "unit_${unit.id}",
                        name = unit.name,
                        tagNumber = if (unit.tagNumber.isNotBlank()) unit.tagNumber else "#${unit.id + 100}",
                        breed = unit.breed.ifBlank { "Local Breed" },
                        category = "CATTLE",
                        status = unit.healthStatus.ifBlank { "DISPOSED" },
                        age = calculatedAge,
                        weight = unit.currentWeight.ifBlank { "450kg" },
                        lastMilk = "Not Lactating",
                        breedingStatus = "DISPOSED",
                        dateOfBirth = unit.dob.ifBlank { "12 Apr 2023" },
                        weightAtBirth = unit.weightAtBirth.ifBlank { "32 kg" },
                        sire = unit.sire.ifBlank { "N/A" },
                        dam = unit.dam.ifBlank { "N/A" },
                        disposalReason = disposalReasonExtracted.ifBlank { cattleDisposalEvent?.metricValue ?: "Disposed" },
                        disposalDate = resolvedDisposalDate,
                        disposalAmount = 0.0,
                        disposalNotes = cattleDisposalEvent?.notes ?: unit.notes,
                        headCountInt = unit.headCount,
                        photoUri = unit.photoUri,
                        notes = unit.notes,
                        isArchived = true
                    )
                }

            poultryDisposalLogs + standaloneArchivedPoultry + archivedCattle
        }
    }

    val mutableAnimals = remember { mutableStateListOf<AnimalDetailData>().apply { addAll(roomAnimals.distinctBy { it.id }) } }

    val allAnimalEventsMap = remember {
        mutableStateMapOf<String, SnapshotStateList<CattleEventItem>>()
    }

    LaunchedEffect(farmSettings.farmId, roomAnimals) {
        val currentFarmAnimals = roomAnimals.distinctBy { it.id }
        mutableAnimals.clear()
        mutableAnimals.addAll(currentFarmAnimals)
        if (selectedAnimal != null) {
            val curr = mutableAnimals.find { it.id == selectedAnimal?.id || it.name.equals(selectedAnimal?.name, ignoreCase = true) }
            if (curr != null && curr != selectedAnimal) {
                selectedAnimal = curr
            }
        }
    }

    fun handleModifyAnimal(
        animalId: String,
        name: String,
        tagNumber: String,
        breed: String,
        category: String,
        status: String,
        breedingStatus: String,
        age: String,
        dob: String,
        weightAtBirth: String,
        currentWeight: String,
        sire: String,
        dam: String,
        headCount: Int,
        photoUri: String? = null,
        notes: String? = null
    ) {
        val existing = mutableAnimals.find { it.id == animalId } ?: return
        val updated = existing.copy(
            name = name,
            tagNumber = tagNumber,
            breed = breed,
            category = category,
            status = status,
            breedingStatus = breedingStatus,
            age = age,
            dateOfBirth = dob,
            weightAtBirth = weightAtBirth,
            weight = currentWeight,
            sire = sire,
            dam = dam,
            headCountInt = headCount,
            photoUri = photoUri,
            notes = notes ?: existing.notes,
            lastMilk = if (category.equals("POULTRY", ignoreCase = true)) "$headCount Birds" else existing.lastMilk
        )
        val idx = mutableAnimals.indexOfFirst { it.id == animalId }
        if (idx >= 0) {
            mutableAnimals[idx] = updated
        }
        if (selectedAnimal?.id == animalId) {
            selectedAnimal = updated
        }

        // Keep the filter category matching if animal category was changed
        if (category.equals("POULTRY", ignoreCase = true)) {
            if (selectedFilterCategory == "CATTLE" && farmSettings.farmType.contains("Both", ignoreCase = true)) {
                selectedFilterCategory = "POULTRY"
            }
        } else if (category.equals("CATTLE", ignoreCase = true)) {
            if (selectedFilterCategory == "POULTRY" && farmSettings.farmType.contains("Both", ignoreCase = true)) {
                selectedFilterCategory = "CATTLE"
            }
        }

        if (animalId.startsWith("unit_")) {
            val uId = animalId.removePrefix("unit_").toLongOrNull()
            if (uId != null) {
                val matching = units.find { it.id == uId }
                if (matching != null) {
                    val nowFormatted = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())
                    val updatedUnit = matching.copy(
                        name = name,
                        type = if (category.equals("POULTRY", ignoreCase = true)) "Poultry" else "Cattle",
                        headCount = headCount,
                        healthStatus = status,
                        lastUpdated = nowFormatted,
                        tagNumber = tagNumber,
                        breed = breed,
                        dob = dob,
                        weightAtBirth = weightAtBirth,
                        currentWeight = currentWeight,
                        sire = sire,
                        dam = dam,
                        photoUri = photoUri,
                        notes = notes ?: matching.notes
                    )
                    onUpdateUnit(updatedUnit)
                }
            }
        } else {
            val matching = units.find { it.name.equals(existing.name, ignoreCase = true) }
            if (matching != null) {
                val nowFormatted = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())
                val updatedUnit = matching.copy(
                    name = name,
                    type = if (category.equals("POULTRY", ignoreCase = true)) "Poultry" else "Cattle",
                    headCount = headCount,
                    healthStatus = status,
                    lastUpdated = nowFormatted,
                    tagNumber = tagNumber,
                    breed = breed,
                    dob = dob,
                    weightAtBirth = weightAtBirth,
                    currentWeight = currentWeight,
                    sire = sire,
                    dam = dam,
                    photoUri = photoUri,
                    notes = notes ?: matching.notes
                )
                onUpdateUnit(updatedUnit)
            } else {
                viewModel.addNewUnit(
                    name = name,
                    type = if (category.equals("POULTRY", ignoreCase = true)) "Poultry" else "Cattle",
                    headCount = headCount,
                    healthStatus = status,
                    location = "Main Farm",
                    tagNumber = tagNumber,
                    breed = breed,
                    dob = dob,
                    weightAtBirth = weightAtBirth,
                    currentWeight = currentWeight,
                    sire = sire,
                    dam = dam,
                    photoUri = photoUri,
                    notes = notes ?: ""
                )
            }
        }
    }

    fun handleDeleteAnimalCompletely(animal: AnimalDetailData) {
        mutableAnimals.removeAll { it.id == animal.id || it.name.equals(animal.name, ignoreCase = true) }
        if (selectedAnimal?.id == animal.id || selectedAnimal?.name.equals(animal.name, ignoreCase = true)) {
            selectedAnimal = null
        }
        if (animal.id.startsWith("poultry_disposal_log_")) {
            val logId = animal.id.removePrefix("poultry_disposal_log_").toLongOrNull()
            if (logId != null) {
                viewModel.deletePoultryLog(logId)
            }
        } else if (animal.id.startsWith("unit_")) {
            val uId = animal.id.removePrefix("unit_").toLongOrNull()
            if (uId != null) {
                onDeleteUnit(uId)
            }
        } else {
            val matching = units.find { it.name.equals(animal.name, ignoreCase = true) } ?: archivedUnits.find { it.name.equals(animal.name, ignoreCase = true) }
            if (matching != null) {
                onDeleteUnit(matching.id)
            }
        }
    }

    fun handleDisposeAnimal(animal: AnimalDetailData, reason: String, amount: Double, notes: String, date: String) {
        val updated = animal.copy(
            status = "DISPOSED ($reason)",
            breedingStatus = "DISPOSED ($reason)",
            disposalReason = reason,
            disposalAmount = amount,
            disposalDate = date,
            disposalNotes = notes
        )
        val idx = mutableAnimals.indexOfFirst { it.id == animal.id }
        if (idx >= 0) {
            mutableAnimals[idx] = updated
        }
        if (selectedAnimal?.id == animal.id) {
            selectedAnimal = updated
        }
        if (animal.id.startsWith("unit_")) {
            val uId = animal.id.removePrefix("unit_").toLongOrNull()
            if (uId != null) {
                val matching = units.find { it.id == uId }
                if (matching != null) {
                    onUpdateUnit(matching.copy(healthStatus = "DISPOSED ($reason)", isArchived = true, lastUpdated = date))
                }
                viewModel.addCattleEvent(
                    unitId = uId,
                    category = "DISPOSAL",
                    title = "Disposed ($reason)",
                    date = date,
                    details = if (notes.isNotBlank()) notes else "Disposal recorded ($reason)",
                    notes = notes,
                    metricValue = reason,
                    costAmount = amount
                )
            }
        } else {
            val matching = units.find { it.name.equals(animal.name, ignoreCase = true) }
            if (matching != null) {
                onUpdateUnit(matching.copy(healthStatus = "DISPOSED ($reason)", isArchived = true, lastUpdated = date))
            }
        }
    }

    fun handleUpdateAnimalStage(animalId: String, newStatus: String, newBreedingStatus: String) {
        val idx = mutableAnimals.indexOfFirst { it.id == animalId }
        if (idx >= 0) {
            val existing = mutableAnimals[idx]
            val updated = existing.copy(
                status = newStatus,
                breedingStatus = newBreedingStatus
            )
            mutableAnimals[idx] = updated
            if (selectedAnimal?.id == animalId) {
                selectedAnimal = updated
            }
        }
        val unitId = animalId.removePrefix("unit_").toLongOrNull()
        if (unitId != null) {
            val u = units.find { it.id == unitId }
            if (u != null) {
                onUpdateUnit(u.copy(healthStatus = newStatus))
            }
        } else {
            val existing = mutableAnimals.find { it.id == animalId }
            if (existing != null) {
                val u = units.find { it.name.equals(existing.name, ignoreCase = true) }
                if (u != null) {
                    onUpdateUnit(u.copy(healthStatus = newStatus))
                }
            }
        }
    }

    fun handleDisposeFlock(flock: AnimalDetailData, quantity: Int, reason: String, amount: Double, notes: String, date: String) {
        val newCount = (flock.headCountInt - quantity).coerceAtLeast(0)
        val updatedTag = "Count: $newCount"
        val isFullyDisposed = newCount == 0
        val updated = flock.copy(
            headCountInt = newCount,
            tagNumber = updatedTag,
            lastMilk = "$newCount Birds",
            disposalReason = if (isFullyDisposed) reason else "",
            disposalDate = if (isFullyDisposed) date else "",
            disposalAmount = if (isFullyDisposed) amount else 0.0,
            disposalNotes = if (isFullyDisposed) notes else "",
            status = if (isFullyDisposed) "DISPOSED ($reason)" else flock.status,
            isArchived = isFullyDisposed
        )
        val idx = mutableAnimals.indexOfFirst { it.id == flock.id }
        if (idx >= 0) {
            if (selectedStatusFilter == "ACTIVE" && isFullyDisposed) {
                mutableAnimals.removeAt(idx)
            } else {
                mutableAnimals[idx] = updated
            }
        }
        if (selectedAnimal?.id == flock.id) {
            if (isFullyDisposed) {
                selectedAnimal = null
            } else {
                selectedAnimal = updated
            }
        }

        if (flock.id.startsWith("unit_")) {
            val uId = flock.id.removePrefix("unit_").toLongOrNull()
            if (uId != null) {
                if (isFullyDisposed) {
                    val matching = units.find { it.id == uId }
                    if (matching != null) {
                        onUpdateUnit(matching.copy(headCount = 0, healthStatus = "DISPOSED ($reason)", isArchived = true, lastUpdated = date))
                    }
                } else {
                    onUpdateUnitHeadCount(uId, newCount)
                    val matching = units.find { it.id == uId }
                    if (matching != null) {
                        onUpdateUnit(matching.copy(headCount = newCount, lastUpdated = date))
                    }
                }
            }
        }
    }

    // Cattle category stage breakdown calculations evaluated live from mutableAnimals
    val cattleList = mutableAnimals.filter { it.category.equals("CATTLE", ignoreCase = true) }
    val poultryList = mutableAnimals.filter { it.category.equals("POULTRY", ignoreCase = true) || it.breed.contains("Layer", ignoreCase = true) || it.breed.contains("Flock", ignoreCase = true) }

    val evaluatedCattleMap = remember(cattleList, allDbCattleEvents, milkLogs, allAnimalEventsMap) {
        cattleList.associate { animal ->
            val numericUnitId = animal.id.removePrefix("unit_").toLongOrNull()
            val dbEvs = if (numericUnitId != null) {
                allDbCattleEvents.filter { it.unitId == numericUnitId }.map {
                    CattleEventItem(
                        id = it.id.toString(),
                        category = it.category,
                        title = it.title,
                        date = it.date,
                        details = it.details,
                        notes = it.notes ?: "",
                        metricValue = it.metricValue ?: ""
                    )
                }
            } else emptyList()
            val rawId = animal.id.removePrefix("unit_")
            val mockEvs = allAnimalEventsMap[animal.id] ?: allAnimalEventsMap[rawId] ?: emptyList()
            val combinedEvs = (dbEvs + mockEvs).distinctBy { it.id }
            val isExplicitNonLactating = animal.status.contains("Heifer", ignoreCase = true) ||
                (animal.status.contains("Calf", ignoreCase = true) && !animal.status.contains("In-Calf", ignoreCase = true) && !animal.status.contains("InCalf", ignoreCase = true)) ||
                animal.status.contains("Bull", ignoreCase = true) ||
                animal.breedingStatus.contains("HEIFER", ignoreCase = true)
            val cowMilkLogs = com.example.data.MilkLogEntryRules.findLogsForCow(milkLogs, animal.name, animal.tagNumber)
            animal.id to CattleLifecycleEngine.evaluateCattleStage(animal, combinedEvs, cowMilkLogs)
        }
    }

    val inCalfMilkingCount = evaluatedCattleMap.values.count { it.stage == CattleStage.INCALF_MILKING }
    val inCalfCount = evaluatedCattleMap.values.count { it.stage == CattleStage.INCALF || (it.stage == CattleStage.DRY && it.isInCalf) }
    val totalInCalfCount = evaluatedCattleMap.values.count { it.isInCalf }
    val milkingCount = evaluatedCattleMap.values.count { it.isMilking || it.stage == CattleStage.MILKING || it.stage == CattleStage.INCALF_MILKING }
    val heiferCount = evaluatedCattleMap.values.count { it.stage == CattleStage.HEIFER }
    val calfCount = evaluatedCattleMap.values.count { it.stage == CattleStage.CALF }
    val dryCount = evaluatedCattleMap.values.count { it.stage == CattleStage.DRY || it.isDriedOff }
    val inseminatedCount = evaluatedCattleMap.values.count { it.stage == CattleStage.INSEMINATED || (it.lastInseminationDate != null && !it.isInCalf) }
    val bullCount = evaluatedCattleMap.values.count { it.stage == CattleStage.BULL }
    val disposedCount = evaluatedCattleMap.values.count { it.stage == CattleStage.DISPOSED }

    val poultryFlocksCount = poultryList.size
    val poultryLayingCount = poultryList.count { it.status.contains("Laying", ignoreCase = true) || it.breedingStatus.contains("Laying", ignoreCase = true) || it.status.equals("ACTIVE", ignoreCase = true) }
    val poultryTotalBirds = poultryList.sumOf { it.headCountInt }

    if (showCategoryGuideDialog) {
        Dialog(onDismissRequest = { showCategoryGuideDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .clip(RoundedCornerShape(20.dp)),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Info, contentDescription = null, tint = ForestGreenPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Automatic Cattle Stages",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }
                        IconButton(onClick = { showCategoryGuideDialog = false }) {
                            Icon(Icons.Filled.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Cattle stage and production status are calculated automatically from breeding records & log events:",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    listOf(
                        Triple(" CALF", "Young Stock (< 12 Months)", "Newborn to weaning young stock. Automatically determined by birth date or young age (< 12 months)."),
                        Triple("HEIFER", "Mature Maiden (> 12 Months)", "Young female (> 12 months) that has not yet given birth to her first calf. Automatically transitions upon aging."),
                        Triple("IN-CALF", "Confirmed Pregnant (Dry / Heifer)", "Confirmed pregnant through a positive Pregnancy Diagnosis (PD) log event, resting or not actively producing milk."),
                        Triple(" / MILKING", "Pregnant & Active Lactation", "Confirmed pregnant via positive PD log event and concurrently active in daily milk production."),
                        Triple("MILKING", "Active Lactating Cow (Open)", "Adult cow in daily milk production following calving, awaiting or between inseminations."),
                        Triple(" DRY", "Dry Period (Resting)", "Mature cow that has completed lactation and ceased milking (via Dry Off log event or 0 milk logs)."),
                        Triple(" BULL", "Breeding Male / Stud", "Mature male kept for herd breeding or artificial insemination semen production."),
                        Triple("DISPOSED", "Culled / Sold / Removed", "Cattle disposed from active herd. All historical milk and breeding records remain safely stored.")
                    ).forEach { (title, subtitle, desc) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                                    Text(subtitle, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(desc, fontSize = 12.sp, color = Color(0xFF334155))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { showCategoryGuideDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                    ) {
                        Text("GOT IT", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }

    if (animalForOptions != null) {
        val target = animalForOptions!!
        AnimalOptionsDialog(
            animal = target,
            userRole = userRole,
            onDismiss = { animalForOptions = null },
            onEditClick = {
                animalForOptions = null
                animalToEdit = target
            },
            onDeleteClick = {
                animalForOptions = null
                animalToDelete = target
            },
            onDisposeClick = {
                animalForOptions = null
                val isPoultry = target.category.equals("POULTRY", ignoreCase = true) || target.breed.contains("Layer", ignoreCase = true) || target.breed.contains("Flock", ignoreCase = true)
                if (isPoultry) {
                    flockToDispose = target
                } else {
                    animalToDispose = target
                }
            },
            onRestoreClick = {
                animalForOptions = null
                val uId = target.id.removePrefix("unit_").toLongOrNull()
                if (uId != null) {
                    val matching = archivedUnits.find { it.id == uId }
                    if (matching != null) {
                        onUpdateUnit(matching.copy(healthStatus = "ACTIVE", isArchived = false))
                    }
                }
            },
            onViewDetailsClick = {
                animalForOptions = null
                selectedAnimal = target
            }
        )
    }

    if (animalToEdit != null) {
        EditAnimalDialog(
            animal = animalToEdit!!,
            onDismiss = { animalToEdit = null },
            onSaveAnimal = { name, tagNumber, breed, category, status, breedingStatus, age, dob, weightAtBirth, currentWeight, sire, dam, headCount, photoUri, notes ->
                handleModifyAnimal(
                    animalId = animalToEdit!!.id,
                    name = name,
                    tagNumber = tagNumber,
                    breed = breed,
                    category = category,
                    status = status,
                    breedingStatus = breedingStatus,
                    age = age,
                    dob = dob,
                    weightAtBirth = weightAtBirth,
                    currentWeight = currentWeight,
                    sire = sire,
                    dam = dam,
                    headCount = headCount,
                    photoUri = photoUri,
                    notes = notes
                )
                animalToEdit = null
            }
        )
    }

    if (animalToDelete != null) {
        DeleteAnimalConfirmDialog(
            animal = animalToDelete!!,
            onDismiss = { animalToDelete = null },
            onConfirmDelete = {
                handleDeleteAnimalCompletely(animalToDelete!!)
                animalToDelete = null
            }
        )
    }

    if (animalToDispose != null) {
        DisposeAnimalDialog(
            animalName = animalToDispose!!.name,
            tagNumber = animalToDispose!!.tagNumber,
            onDismiss = { animalToDispose = null },
            onConfirmDispose = { reason, amount, notes, date ->
                handleDisposeAnimal(animalToDispose!!, reason, amount, notes, date)
                if (reason.equals("Sold", ignoreCase = true) || reason.equals("Home Consumption", ignoreCase = true)) {
                    val catName = if (reason.equals("Sold", ignoreCase = true)) "Animal Sale" else "Farm Income"
                    pendingFinanceCategory = catName
                    pendingFinanceDate = date
                    showFinancePromptForDisposal = Pair("Disposed ${animalToDispose!!.name} ($reason) - $notes", animalToDispose!!.name)
                }
                animalToDispose = null
            }
        )
    }

    if (flockToDispose != null) {
        DisposeFlockDialog(
            flockName = flockToDispose!!.name,
            currentHeadCount = flockToDispose!!.headCountInt,
            onDismiss = { flockToDispose = null },
            onConfirmDisposeFlock = { quantity, reason, amount, notes, date ->
                handleDisposeFlock(flockToDispose!!, quantity, reason, amount, notes, date)
                if (reason.equals("Sold", ignoreCase = true) || reason.equals("Home Consumption", ignoreCase = true)) {
                    val catName = "Poultry Sales"
                    pendingFinanceCategory = catName
                    pendingFinanceDate = date
                    showFinancePromptForDisposal = Pair("Disposed $quantity birds from ${flockToDispose!!.name} ($reason) - $notes", flockToDispose!!.name)
                }
                flockToDispose = null
            }
        )
    }

    if (showFinancePromptForDisposal != null) {
        val (desc, targetUnit) = showFinancePromptForDisposal!!
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showFinancePromptForDisposal = null },
            title = { Text("Log to Finance?", fontWeight = FontWeight.Bold) },
            text = { Text("Disposal recorded successfully. Would you like to log this as an Income or Expense in your Finance records?") },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        pendingFinanceDescription = desc
                        pendingFinanceTarget = targetUnit
                        showFinancePromptForDisposal = null
                        showRecordFinanceDialog = true
                    }
                ) {
                    Text("Yes, Log to Finance", color = ForestGreenPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showFinancePromptForDisposal = null }) {
                    Text("No, Skip", color = Color(0xFF64748B))
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showRecordFinanceDialog) {
        com.example.ui.components.AddFinanceRecordDialog(
            initialType = com.example.data.FinanceType.INCOME,
            initialCategory = pendingFinanceCategory,
            initialDescription = pendingFinanceDescription,
            initialDate = pendingFinanceDate,
            initialTargetUnit = pendingFinanceTarget,
            userRole = userRole,
            canEditPastDaysLogs = true,
            onDismiss = { showRecordFinanceDialog = false },
            onSaveRecordFull = { type, category, amount, description, date, targetUnit ->
                viewModel.addFinanceRecord(type, category, amount, description, date, targetUnit)
                showRecordFinanceDialog = false
            },
            onSaveRecordWithDate = { type, category, amount, description, date ->
                viewModel.addFinanceRecord(type, category, amount, description, date, pendingFinanceTarget)
                showRecordFinanceDialog = false
            },
            onSaveRecord = { type, category, amount, description ->
                viewModel.addFinanceRecord(type, category, amount, description, pendingFinanceDate, pendingFinanceTarget)
                showRecordFinanceDialog = false
            }
        )
    }

    if (selectedAnimal != null) {
        val isPoultry = selectedAnimal!!.category.equals("POULTRY", ignoreCase = true) || selectedAnimal!!.category.equals("FLOCK", ignoreCase = true)
        val isDisposedPoultry = isPoultry && (selectedAnimal!!.isArchived || selectedAnimal!!.status.contains("DISPOSED", ignoreCase = true) || selectedStatusFilter == "ARCHIVED")

        if (isDisposedPoultry) {
            DisposedFlockDetailView(
                flock = selectedAnimal!!,
                userRole = userRole,
                onBackClick = { selectedAnimal = null },
                onDeleteFlock = {
                    val toDelete = selectedAnimal
                    selectedAnimal = null
                    animalToDelete = toDelete
                },
                canEdit = effectiveCanEditLivestock,
                modifier = modifier
            )
        } else if (isPoultry) {
            val selectedPoultryUnitId = selectedAnimal!!.id.removePrefix("unit_").toLongOrNull() ?: 0L
            FlockDetailsView(
                flock = selectedAnimal!!,
                userRole = userRole,
                eggLogs = eggLogs,
                financeRecords = financeRecords,
                poultryLogs = allDbPoultryLogs.filter { it.unitId == selectedPoultryUnitId },
                completedVaccineRuleIds = remember(reminderCompletions, allDbPoultryLogs, rawTasks, selectedPoultryUnitId, selectedAnimal) {
                    val completedSet = mutableSetOf<String>()
                    // 1. From reminder_completions
                    reminderCompletions.filter { it.unitId == selectedPoultryUnitId }.forEach { completion ->
                        val prefix = "poultry_vac_${selectedPoultryUnitId}_"
                        if (completion.ruleKey.startsWith(prefix)) {
                            completedSet.add(completion.ruleKey.removePrefix(prefix))
                        }
                        val matched = com.example.utils.PoultryAgeAndVaccinationUtils.matchVaccineRuleId(completion.ruleKey)
                        if (matched != null) completedSet.add(matched)
                    }
                    // 2. From poultry_logs
                    allDbPoultryLogs.filter { it.unitId == selectedPoultryUnitId && (it.logType == "VACCINATION" || it.vaccineStatus == "COMPLETED") }.forEach { log ->
                        val matched = com.example.utils.PoultryAgeAndVaccinationUtils.matchVaccineRuleId(log.vaccineName, log.notes, log.targetStage)
                        if (matched != null) completedSet.add(matched)
                    }
                    // 3. From completed tasks
                    val cleanTag = selectedAnimal?.tagNumber?.replace("#", "")?.trim() ?: ""
                    rawTasks.filter { it.isCompleted && (
                        (selectedAnimal != null && it.targetUnit.contains(selectedAnimal!!.name, ignoreCase = true)) ||
                        (cleanTag.isNotBlank() && it.targetUnit.contains(cleanTag, ignoreCase = true))
                    ) }.forEach { task ->
                        val matched = com.example.utils.PoultryAgeAndVaccinationUtils.matchVaccineRuleId(task.title, task.instructions, task.syncId)
                        if (matched != null) completedSet.add(matched)
                    }
                    completedSet
                },
                onMarkVaccinationComplete = { ruleId ->
                    if (selectedPoultryUnitId > 0) {
                        viewModel.markReminderComplete(
                            ruleKey = "poultry_vac_${selectedPoultryUnitId}_${ruleId}",
                            unitId = selectedPoultryUnitId
                        )
                        val rule = com.example.utils.PoultryAgeAndVaccinationUtils.STANDARD_VACCINATION_RULES.firstOrNull { it.id == ruleId }
                        val vacName = rule?.vaccineName ?: ruleId
                        val todayStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
                        viewModel.addPoultryLog(
                            PoultryLog(
                                farmId = currentSession?.farmId ?: "FARM-DEFAULT",
                                unitId = selectedPoultryUnitId,
                                logType = "VACCINATION",
                                vaccineName = vacName,
                                targetStage = rule?.targetStageLabel ?: "Scheduled Stage",
                                vaccineStatus = "COMPLETED",
                                date = todayStr,
                                notes = "Marked complete from vaccination schedule"
                            )
                        )
                    }
                },
                onClearVaccinationComplete = { ruleId ->
                    if (selectedPoultryUnitId > 0) {
                        viewModel.clearReminderCompletion(
                            ruleKey = "poultry_vac_${selectedPoultryUnitId}_${ruleId}"
                        )
                    }
                },
                onAddPoultryLog = { viewModel.addPoultryLog(it) },
                onUpdatePoultryLog = { viewModel.updatePoultryLog(it) },
                onDeletePoultryLog = { viewModel.deletePoultryLog(it) },
                onUpdateFlockHeadCount = { newHeadCount ->
                    if (selectedPoultryUnitId > 0) onUpdateUnitHeadCount(selectedPoultryUnitId, newHeadCount)
                },
                onBackClick = { selectedAnimal = null },
                onAddEggLogClick = onAddEggLogClick,
                onAddFinanceClick = onAddFinanceClick,
                onDisposeFlock = { qty, reason, amount, notes, date ->
                    handleDisposeFlock(selectedAnimal!!, qty, reason, amount, notes, date)
                    if (reason.equals("Sold", ignoreCase = true) || reason.equals("Home Consumption", ignoreCase = true)) {
                        val catName = if (reason.equals("Sold", ignoreCase = true)) "Poultry Sales" else "Farm Income"
                        pendingFinanceCategory = catName
                        pendingFinanceDate = date
                        showFinancePromptForDisposal = Pair("Disposed $qty birds from ${selectedAnimal!!.name} ($reason) - $notes", selectedAnimal!!.name)
                    }
                },
                onEditFlock = { animalToEdit = selectedAnimal },
                onDeleteFlock = { animalToDelete = selectedAnimal },
                onUpdatePhoto = { newPhoto ->
                    handleModifyAnimal(
                        animalId = selectedAnimal!!.id,
                        name = selectedAnimal!!.name,
                        tagNumber = selectedAnimal!!.tagNumber,
                        breed = selectedAnimal!!.breed,
                        category = selectedAnimal!!.category,
                        status = selectedAnimal!!.status,
                        breedingStatus = selectedAnimal!!.breedingStatus,
                        age = selectedAnimal!!.age,
                        dob = selectedAnimal!!.dateOfBirth,
                        weightAtBirth = selectedAnimal!!.weightAtBirth,
                        currentWeight = selectedAnimal!!.weight,
                        sire = selectedAnimal!!.sire,
                        dam = selectedAnimal!!.dam,
                        headCount = selectedAnimal!!.headCountInt,
                        photoUri = newPhoto
                    )
                },
                canEditLivestock = effectiveCanEditLivestock,
                modifier = modifier
            )
        } else {
            AnimalDetailsView(
                viewModel = viewModel,
                animal = selectedAnimal!!,
                userRole = userRole,
                milkLogs = milkLogs,
                eggLogs = eggLogs,
                allDbCattleEvents = allDbCattleEvents,
                onUpdateAnimalStage = { newStatus, newBreedingStatus ->
                    handleUpdateAnimalStage(selectedAnimal!!.id, newStatus, newBreedingStatus)
                },
                onBackClick = { selectedAnimal = null },
                onDisposeAnimal = { reason, amount, notes, date ->
                    handleDisposeAnimal(selectedAnimal!!, reason, amount, notes, date)
                },
                onEditAnimal = { animalToEdit = selectedAnimal },
                onDeleteAnimal = { animalToDelete = selectedAnimal },
                onUpdatePhoto = { newPhoto ->
                    handleModifyAnimal(
                        animalId = selectedAnimal!!.id,
                        name = selectedAnimal!!.name,
                        tagNumber = selectedAnimal!!.tagNumber,
                        breed = selectedAnimal!!.breed,
                        category = selectedAnimal!!.category,
                        status = selectedAnimal!!.status,
                        breedingStatus = selectedAnimal!!.breedingStatus,
                        age = selectedAnimal!!.age,
                        dob = selectedAnimal!!.dateOfBirth,
                        weightAtBirth = selectedAnimal!!.weightAtBirth,
                        currentWeight = selectedAnimal!!.weight,
                        sire = selectedAnimal!!.sire,
                        dam = selectedAnimal!!.dam,
                        headCount = selectedAnimal!!.headCountInt,
                        photoUri = newPhoto
                    )
                },
                canEditLivestock = effectiveCanEditLivestock,
                modifier = modifier
            )
        }
    } else {
        // This used to run as a plain `.filter { }` directly in the composable
        // body — meaning it re-filtered and re-evaluated cattle stages for
        // EVERY animal on EVERY recomposition of this screen, including ones
        // triggered by unrelated state a few lines up (selectedAnimal,
        // animalForOptions, animalToEdit, animalToDelete, animalToDispose,
        // showCategoryGuideDialog, etc. all live in this same composable
        // scope). Opening a detail sheet or any dialog re-ran this full filter
        // over the whole herd, including the CattleLifecycleEngine fallback
        // evaluation for any animal missing from evaluatedCattleMap.
        //
        // derivedStateOf (not remember(keys)) is correct here specifically
        // because mutableAnimals is a SnapshotStateList: its reference never
        // changes when items are added/removed, so a remember() keyed on the
        // list reference would never invalidate on content changes.
        // derivedStateOf instead tracks the actual state reads inside the
        // block — list contents, farmSettings.farmType,
        // selectedFilterCategory, evaluatedCattleMap, selectedCattleStage —
        // and only recomputes when one of those genuinely changes, regardless
        // of what else in this composable causes a recomposition.
        val filteredList by remember {
            derivedStateOf {
                mutableAnimals.filter { animal ->
                    val matchesMode = when {
                        farmSettings.farmType.equals("Cattle Only", ignoreCase = true) -> animal.category.equals("CATTLE", ignoreCase = true)
                        farmSettings.farmType.equals("Poultry Only", ignoreCase = true) -> animal.category.equals("POULTRY", ignoreCase = true) || animal.breed.contains("Layer", ignoreCase = true) || animal.breed.contains("Flock", ignoreCase = true)
                        else -> true
                    }
                    if (!matchesMode) return@filter false

                    val matchesCategory = animal.category.equals(selectedFilterCategory, ignoreCase = true)
                    if (!matchesCategory) return@filter false
                    if (!animal.category.equals("CATTLE", ignoreCase = true)) return@filter true

                    val eval = evaluatedCattleMap[animal.id]
                        ?: CattleLifecycleEngine.evaluateCattleStage(animal, emptyList(), emptyList())
                    when (selectedCattleStage) {
                        "MILKING" -> eval.isMilking || eval.stage == CattleStage.MILKING || eval.stage == CattleStage.INCALF_MILKING
                        "INCALF" -> eval.isInCalf || eval.stage == CattleStage.INCALF || eval.stage == CattleStage.INCALF_MILKING
                        "HEIFER" -> eval.stage == CattleStage.HEIFER
                        "CALF" -> eval.stage == CattleStage.CALF
                        "DRY" -> eval.stage == CattleStage.DRY || eval.isDriedOff
                        "INSEMINATED" -> eval.stage == CattleStage.INSEMINATED || (eval.lastInseminationDate != null && !eval.isInCalf)
                        "BULL" -> eval.stage == CattleStage.BULL
                        "DISPOSED" -> eval.stage == CattleStage.DISPOSED
                        else -> true
                    }
                }
            }
        }

        Box(modifier = modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Livestock",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1D1F)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Category Filter Chips [ CATTLE ] [ POULTRY ]
                    val availableCategories = when {
                        farmSettings.farmType.equals("Cattle Only", ignoreCase = true) -> listOf("CATTLE")
                        farmSettings.farmType.equals("Poultry Only", ignoreCase = true) -> listOf("POULTRY")
                        else -> listOf("CATTLE", "POULTRY")
                    }

                    if (availableCategories.size > 1) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            availableCategories.forEach { cat ->
                                val isSelected = selectedFilterCategory == cat
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedFilterCategory = cat
                                        if (cat == "POULTRY") selectedCattleStage = "ALL"
                                    },
                                    label = { Text(cat, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ForestGreenPrimary,
                                        selectedLabelColor = Color.White,
                                        containerColor = Color.White
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        borderColor = Color(0xFFE2E8F0)
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Status Filter Chips [ ACTIVE ] [ ARCHIVED ]
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf("ACTIVE", "ARCHIVED").forEach { status ->
                            val isSelected = selectedStatusFilter == status
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedStatusFilter = status },
                                label = { Text(if (status == "ACTIVE") "Active" else "Disposed/Archived", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (status == "ACTIVE") ForestGreenPrimary else Color(0xFF64748B),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.White
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = Color(0xFFE2E8F0)
                                )
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    // Cattle Herd Breakdown Panel (Visible for CATTLE filter)
                    if (selectedFilterCategory == "CATTLE") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Pets, contentDescription = null, tint = ForestGreenPrimary, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Cattle Stage Breakdown", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = Color(0xFFEFF6FF),
                                        modifier = Modifier.clickable { showCategoryGuideDialog = true }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Filled.Info, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Category Guide", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                val stageItems = listOf(
                                    Triple("ALL", "All Herd", "${cattleList.size}"),
                                    Triple("MILKING", "Milking", "$milkingCount"),
                                    Triple("INCALF", "In-Calf", "$totalInCalfCount"),
                                    Triple("HEIFER", "Heifers", "$heiferCount"),
                                    Triple("CALF", " Calves", "$calfCount"),
                                    Triple("BULL", " Bulls", "$bullCount"),
                                    Triple("DRY", " Dry", "$dryCount"),
                                    Triple("INSEMINATED", "Inseminated", "$inseminatedCount")
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    stageItems.take(4).forEach { (stageKey, label, count) ->
                                        val isSelected = selectedCattleStage == stageKey
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) ForestGreenPrimary else Color.White,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) ForestGreenPrimary else Color(0xFFCBD5E1)),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { selectedCattleStage = stageKey }
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(count, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else Color(0xFF0F172A))
                                                Text(label, fontSize = 9.sp, fontWeight = FontWeight.Medium, color = if (isSelected) Color.White.copy(alpha = 0.9f) else Color(0xFF64748B), maxLines = 1)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    stageItems.drop(4).forEach { (stageKey, label, count) ->
                                        val isSelected = selectedCattleStage == stageKey
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) ForestGreenPrimary else Color.White,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) ForestGreenPrimary else Color(0xFFCBD5E1)),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { selectedCattleStage = stageKey }
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(count, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else Color(0xFF0F172A))
                                                Text(label, fontSize = 9.sp, fontWeight = FontWeight.Medium, color = if (isSelected) Color.White.copy(alpha = 0.9f) else Color(0xFF64748B), maxLines = 1)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (selectedFilterCategory == "POULTRY") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Egg, contentDescription = null, tint = ForestGreenPrimary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (selectedStatusFilter == "ARCHIVED") "Disposed Poultry Summary" else "Poultry Flock Summary",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (selectedStatusFilter == "ARCHIVED") {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color.White,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("${poultryList.size}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                                Text("Disposed Flocks", fontSize = 10.sp, color = Color(0xFF64748B))
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFFFEE2E2),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                val totalDisposedBirds = poultryList.sumOf { it.headCountInt }
                                                Text("$totalDisposedBirds", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                                                Text("Disposed Birds", fontSize = 10.sp, color = Color(0xFF7F1D1D))
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFFDCFCE7),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                val totalRevenue = poultryList.sumOf { it.disposalAmount }
                                                Text("KSh %,.0f".format(totalRevenue), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary, maxLines = 1)
                                                Text("Total Revenue", fontSize = 10.sp, color = ForestGreenPrimary)
                                            }
                                        }
                                    } else {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color.White,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("$poultryFlocksCount", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                                Text("Total Flocks", fontSize = 11.sp, color = Color(0xFF64748B))
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFFFEF3C7),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("$poultryLayingCount", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                                                Text("Currently Laying", fontSize = 11.sp, color = Color(0xFF92400E))
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFFDCFCE7),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("$poultryTotalBirds", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                                                Text("Total Birds", fontSize = 11.sp, color = ForestGreenPrimary)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }

                items(filteredList, key = { it.id }) { animal ->
                    val isCattleItem = animal.category.equals("CATTLE", ignoreCase = true)
                    val cattleEval = if (isCattleItem) {
                        evaluatedCattleMap[animal.id]
                            ?: CattleLifecycleEngine.evaluateCattleStage(animal, emptyList(), emptyList())
                    } else null

                    val isPoultryItem = animal.category.equals("POULTRY", ignoreCase = true)
                    val poultryEval = if (isPoultryItem) {
                        val unitId = animal.id.removePrefix("unit_").toLongOrNull() ?: animal.id.toLongOrNull() ?: 0L
                        val flockLogs = allDbPoultryLogs.filter { it.unitId == unitId }
                        val mLogs = flockLogs.filter { it.logType == "MORTALITY" }
                        val eLogs = flockLogs.filter { it.logType == "EGG_SALE" }
                        val totalMort = mLogs.sumOf { it.birdCount }
                        val cutoff7Days = System.currentTimeMillis() - (7L * 24L * 60L * 60L * 1000L)
                        val mort7 = mLogs.filter { log ->
                            val parsed = PoultryAgeAndVaccinationUtils.parseDate(log.date)
                            parsed == null || parsed.time >= cutoff7Days
                        }.sumOf { it.birdCount }
                        val avgEggTrays7 = eLogs.filter { log ->
                            val parsed = PoultryAgeAndVaccinationUtils.parseDate(log.date)
                            parsed == null || parsed.time >= cutoff7Days
                        }.sumOf { it.traysSold }.toDouble() / 7.0

                        val ageInfo = PoultryAgeAndVaccinationUtils.calculateFlockAge(animal.dateOfBirth)
                        val completedVacs = flockLogs.filter { it.logType == "VACCINATION" || it.vaccineStatus == "COMPLETED" }
                            .mapNotNull { log -> PoultryAgeAndVaccinationUtils.matchVaccineRuleId(log.vaccineName, log.notes, log.targetStage) }
                            .toSet()
                        val schedule = PoultryAgeAndVaccinationUtils.calculateVaccinationSchedule(animal.dateOfBirth, completedVacs)
                        val isDisposed = animal.status.contains("DISPOSED", ignoreCase = true) || animal.disposalReason.isNotBlank() || animal.headCountInt <= 0
                        val overdueCount = if (isDisposed) 0 else schedule.count { it.status == VaccineDueStatus.OVERDUE }
                        val dueTodayCount = if (isDisposed) 0 else schedule.count { it.status == VaccineDueStatus.DUE_TODAY }

                        PoultryAgeAndVaccinationUtils.evaluateAutomatedFlockStatus(
                            ageInfo = ageInfo,
                            activeHeadCount = animal.headCountInt,
                            mortalityCountLast7Days = mort7,
                            totalMortalityCount = totalMort,
                            avgDailyEggTraysLast7Days = avgEggTrays7,
                            overdueVaccineCount = overdueCount,
                            dueTodayVaccineCount = dueTodayCount
                        )
                    } else null

                    val isDisposedPoultry = isPoultryItem && (animal.isArchived || animal.status.contains("DISPOSED", ignoreCase = true) || selectedStatusFilter == "ARCHIVED")

                    if (isDisposedPoultry) {
                        DisposedPoultryCard(
                            animal = animal,
                            onClick = { selectedAnimal = animal },
                            onMoreOptions = { animalForOptions = animal },
                            canEdit = effectiveCanEditLivestock
                        )
                    } else if (isPoultryItem && poultryEval != null) {
                        val flockAge = PoultryAgeAndVaccinationUtils.calculateFlockAge(animal.dateOfBirth)
                        @OptIn(ExperimentalFoundationApi::class)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .combinedClickable(
                                    onClick = { selectedAnimal = animal },
                                    onLongClick = { animalForOptions = animal }
                                )
                                .testTag("animal_card_${animal.id}"),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                // Header Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color(0xFFFEF3C7),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                                            modifier = Modifier.size(46.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                if (!animal.photoUri.isNullOrBlank()) {
                                                    AsyncImage(
                                                        model = ImageRequest.Builder(LocalContext.current)
                                                            .data(ImageStorageUtils.resolveImageModel(animal.photoUri))
                                                            .crossfade(true)
                                                            .memoryCachePolicy(CachePolicy.ENABLED)
                                                            .diskCachePolicy(CachePolicy.ENABLED)
                                                            .build(),
                                                        contentDescription = "${animal.name} Photo",
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                                                    )
                                                } else {
                                                    Icon(
                                                        imageVector = Icons.Filled.Egg,
                                                        contentDescription = "Poultry Icon",
                                                        tint = Color(0xFFD97706),
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Text(
                                                text = animal.name,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F172A)
                                            )
                                            Text(
                                                text = "${animal.breed.ifEmpty { "Layers" }} • ${animal.tagNumber.ifEmpty { "Coop Unit" }}",
                                                fontSize = 12.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(100.dp),
                                            color = when (poultryEval.healthLevel) {
                                                com.example.utils.PoultryHealthLevel.HEALTHY -> Color(0xFFDCFCE7)
                                                com.example.utils.PoultryHealthLevel.CAUTION -> Color(0xFFFEF3C7)
                                                com.example.utils.PoultryHealthLevel.CRITICAL -> Color(0xFFFEE2E2)
                                            },
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                when (poultryEval.healthLevel) {
                                                    com.example.utils.PoultryHealthLevel.HEALTHY -> Color(0xFF86EFAC)
                                                    com.example.utils.PoultryHealthLevel.CAUTION -> Color(0xFFFDE68A)
                                                    com.example.utils.PoultryHealthLevel.CRITICAL -> Color(0xFFFCA5A5)
                                                }
                                            )
                                        ) {
                                            Text(
                                                text = when (poultryEval.healthLevel) {
                                                    com.example.utils.PoultryHealthLevel.HEALTHY -> "🟢 Healthy"
                                                    com.example.utils.PoultryHealthLevel.CAUTION -> "🟡 Caution"
                                                    com.example.utils.PoultryHealthLevel.CRITICAL -> "🔴 Critical"
                                                },
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when (poultryEval.healthLevel) {
                                                    com.example.utils.PoultryHealthLevel.HEALTHY -> Color(0xFF166534)
                                                    com.example.utils.PoultryHealthLevel.CAUTION -> Color(0xFF92400E)
                                                    com.example.utils.PoultryHealthLevel.CRITICAL -> Color(0xFF991B1B)
                                                },
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }

                                        if (effectiveCanEditLivestock) {
                                            IconButton(
                                                onClick = { animalForOptions = animal },
                                                modifier = Modifier.size(32.dp).testTag("more_options_${animal.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.MoreVert,
                                                    contentDescription = "Flock options",
                                                    tint = Color(0xFF64748B),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Structured Metric Boxes Grid
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFF8FAFC),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Box 1: Flock Count & Age
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "BIRDS & AGE",
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF64748B)
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${animal.headCountInt} Birds",
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F172A)
                                            )
                                            Text(
                                                text = flockAge.shortAgeLabel,
                                                fontSize = 10.5.sp,
                                                color = Color(0xFF475569)
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .width(1.dp)
                                                .height(32.dp)
                                                .background(Color(0xFFE2E8F0))
                                        )

                                        // Box 2: Production Status & Lay Rate
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "PRODUCTION",
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF64748B)
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = if (poultryEval.layRatePercent > 0.0)
                                                    String.format("%.1f%% Lay Rate", poultryEval.layRatePercent)
                                                else "Pre-Lay",
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when (poultryEval.productionLevel) {
                                                    com.example.utils.PoultryProductionLevel.EXCELLENT -> Color(0xFF1E40AF)
                                                    com.example.utils.PoultryProductionLevel.NORMAL -> Color(0xFF15803D)
                                                    com.example.utils.PoultryProductionLevel.LOW_WARNING -> Color(0xFFB45309)
                                                    com.example.utils.PoultryProductionLevel.PRE_LAY -> Color(0xFF475569)
                                                }
                                            )
                                            Text(
                                                text = poultryEval.productionBadgeLabel,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = when (poultryEval.productionLevel) {
                                                    com.example.utils.PoultryProductionLevel.EXCELLENT -> Color(0xFF1D4ED8)
                                                    com.example.utils.PoultryProductionLevel.NORMAL -> Color(0xFF166534)
                                                    com.example.utils.PoultryProductionLevel.LOW_WARNING -> Color(0xFFD97706)
                                                    com.example.utils.PoultryProductionLevel.PRE_LAY -> Color(0xFF64748B)
                                                },
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .width(1.dp)
                                                .height(32.dp)
                                                .background(Color(0xFFE2E8F0))
                                        )

                                        // Box 3: Feed Stage
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "FEED STAGE",
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF64748B)
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = flockAge.feedStage.stageName.substringBefore(" ("),
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F172A),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = flockAge.feedStage.dailyRationPerBird,
                                                fontSize = 10.5.sp,
                                                color = Color(0xFF475569),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                if (poultryEval.automatedAlerts.isNotEmpty()) {
                                    val topAlert = poultryEval.automatedAlerts.first()
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (topAlert.isCritical) Color(0xFFFEF2F2) else Color(0xFFFFFBEB),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (topAlert.isCritical) Color(0xFFFECACA) else Color(0xFFFDE68A)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (topAlert.isCritical) Icons.Filled.Warning else Icons.Filled.Info,
                                                contentDescription = null,
                                                tint = if (topAlert.isCritical) Color(0xFFDC2626) else Color(0xFFD97706),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "${topAlert.title}: ${topAlert.recommendation}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = if (topAlert.isCritical) Color(0xFF991B1B) else Color(0xFF92400E),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        @OptIn(ExperimentalFoundationApi::class)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .combinedClickable(
                                    onClick = { selectedAnimal = animal },
                                    onLongClick = { animalForOptions = animal }
                                )
                                .testTag("animal_card_${animal.id}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(14.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (animal.category == "POULTRY") Color(0xFFFEF3C7) else Color(0xFFE8F5E9),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (animal.category == "POULTRY") Color(0xFFFDE68A) else Color(0xFFC8E6C9)),
                                        modifier = Modifier.size(50.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            if (!animal.photoUri.isNullOrBlank()) {
                                                AsyncImage(
                                                    model = ImageRequest.Builder(LocalContext.current)
                                                        .data(ImageStorageUtils.resolveImageModel(animal.photoUri))
                                                        .crossfade(true)
                                                        .memoryCachePolicy(CachePolicy.ENABLED)
                                                        .diskCachePolicy(CachePolicy.ENABLED)
                                                        .build(),
                                                    contentDescription = "${animal.name} Photo",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = if (animal.category == "POULTRY") Icons.Filled.Egg else Icons.Filled.Pets,
                                                    contentDescription = if (animal.category == "POULTRY") "Poultry Icon" else "Cattle Icon",
                                                    tint = if (animal.category == "POULTRY") Color(0xFFD97706) else ForestGreenPrimary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = animal.name,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E293B)
                                        )
                                        if (cattleEval != null) {
                                            Text(
                                                text = "${cattleEval.stage.emoji} ${animal.breed}   ${animal.tagNumber}",
                                                fontSize = 12.sp,
                                                color = Color(0xFF64748B)
                                            )
                                            Text(
                                                text = cattleEval.breedingStatusText,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = cattleEval.badgeTextColor
                                            )
                                        } else {
                                            Text(
                                                text = "Breed: ${animal.breed}   ${animal.tagNumber}",
                                                fontSize = 12.sp,
                                                color = Color(0xFF64748B)
                                            )
                                            Text(
                                                text = if (userRole == "OWNER") "Long press to Edit / Delete" else "Tap to view full details",
                                                fontSize = 10.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = cattleEval?.badgeBgColor ?: if (animal.status == "MILKING" || animal.status == "ACTIVE" || animal.status == "Active Laying") TagLivestockBg else TagYieldBg
                                    ) {
                                        Text(
                                            text = cattleEval?.stage?.displayName ?: animal.status,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = cattleEval?.badgeTextColor ?: if (animal.status == "MILKING" || animal.status == "ACTIVE" || animal.status == "Active Laying") TagLivestockText else TagYieldText
                                        )
                                    }

                                    if (effectiveCanEditLivestock) {
                                        IconButton(
                                            onClick = { animalForOptions = animal },
                                            modifier = Modifier
                                                .size(34.dp)
                                                .testTag("more_options_${animal.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.MoreVert,
                                                contentDescription = "Animal options",
                                                tint = Color(0xFF64748B),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }

            if (effectiveCanEditLivestock) {
                val isPoultrySection = selectedFilterCategory.equals("POULTRY", ignoreCase = true)
                val addLabel = if (isPoultrySection) "ADD FLOCK" else "ADD ANIMAL"
                FloatingActionButton(
                    onClick = onAddUnitClick,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(20.dp)
                        .testTag(if (isPoultrySection) "add_flock_fab" else "add_animal_fab"),
                    containerColor = ForestGreenPrimary,
                    contentColor = Color.White
                ) {
                    Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Icon(Icons.Filled.Add, contentDescription = addLabel)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(addLabel, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AnimalDetailsView(
    viewModel: FarmViewModel,
    animal: AnimalDetailData,
    userRole: String,
    onBackClick: () -> Unit,
    onDisposeAnimal: (reason: String, amount: Double, notes: String, date: String) -> Unit = { _, _, _, _ -> },
    onEditAnimal: () -> Unit = {},
    onDeleteAnimal: () -> Unit = {},
    onUpdatePhoto: (String?) -> Unit = {},
    milkLogs: List<MilkLog> = emptyList(),
    eggLogs: List<EggLog> = emptyList(),
    allDbCattleEvents: List<com.example.data.CattleEvent> = emptyList(),
    onUpdateAnimalStage: (newStatus: String, newBreedingStatus: String) -> Unit = { _, _ -> },
    canEditLivestock: Boolean = true,
    modifier: Modifier = Modifier
) {
    val canEdit = userRole.equals("OWNER", ignoreCase = true) || canEditLivestock

    val unitId = remember(animal.id) {
        animal.id.removePrefix("unit_").toLongOrNull() ?: ((animal.id.hashCode().toLong() and 0x7FFFFFFF) + 10000L)
    }
    val initialUnitDbEvents = remember(unitId, allDbCattleEvents) {
        allDbCattleEvents.filter { it.unitId == unitId }
    }
    val dbEvents by viewModel.getCattleEventsFlow(unitId).collectAsStateWithLifecycle(initialValue = initialUnitDbEvents)
    val animalEvents = remember(dbEvents) {
        dbEvents.map {
            CattleEventItem(
                id = it.id.toString(),
                category = it.category,
                title = it.title,
                date = it.date,
                details = it.details,
                notes = it.notes ?: "",
                metricValue = it.metricValue ?: ""
            )
        }.toMutableStateList()
    }

    var showAddCattleEventDialog by remember { mutableStateOf(false) }
    var cattleEventDialogCategory by remember { mutableStateOf("PD") }
    var eventToEdit by remember { mutableStateOf<CattleEventItem?>(null) }
    var eventToDelete by remember { mutableStateOf<CattleEventItem?>(null) }
    var showDeleteEventConfirmDialog by remember { mutableStateOf(false) }
    var selectedLogFilter by remember { mutableStateOf("ALL") } // ALL, CALVING, HEALTH, HEAT, PD, WEIGHT
    var showRecordFinanceDialog by remember { mutableStateOf(false) }
    var pendingFinanceCategory by remember { mutableStateOf("Vaccines & Vet") }
    var pendingFinanceDescription by remember { mutableStateOf("") }
    var pendingFinanceDate by remember { mutableStateOf("") }

    val sortedAnimalEvents = remember(dbEvents) {
        animalEvents.sortedWith(
            compareByDescending<CattleEventItem> { parseEventDateForSorting(it.date) }
                .thenByDescending { it.id.toLongOrNull() ?: 0L }
        )
    }

    val calvingLogs = remember(dbEvents) {
        animalEvents.filter { it.category.equals("CALVING", ignoreCase = true) }
            .sortedWith(
                compareByDescending<CattleEventItem> { parseEventDateForSorting(it.date) }
                    .thenByDescending { it.id.toLongOrNull() ?: 0L }
            )
    }

    val isCattle = animal.category.equals("CATTLE", ignoreCase = true)
    val isPoultry = animal.category.contains("POULTRY", ignoreCase = true) || animal.breed.contains("Layer", ignoreCase = true) || animal.breed.contains("Poultry", ignoreCase = true) || animal.breed.contains("Flock", ignoreCase = true)

    val isExplicitNonLactating = isCattle && (
        animal.status.contains("Heifer", ignoreCase = true) ||
        (animal.status.contains("Calf", ignoreCase = true) && !animal.status.contains("In-Calf", ignoreCase = true) && !animal.status.contains("InCalf", ignoreCase = true)) ||
        animal.status.contains("Bull", ignoreCase = true) ||
        animal.breedingStatus.contains("HEIFER", ignoreCase = true)
    )

    val realCowMilkLogs = remember(animal.name, animal.tagNumber, milkLogs) {
        com.example.data.MilkLogEntryRules.findLogsForCow(milkLogs, animal.name, animal.tagNumber)
    }

    val cowMilkLogs = remember(realCowMilkLogs, isExplicitNonLactating) {
        if (isExplicitNonLactating) emptyList()
        else realCowMilkLogs
    }

    // Evaluate dynamic cattle stage using CattleLifecycleEngine
    val cattleEval = remember(animal, dbEvents, realCowMilkLogs) {
        if (isCattle) {
            CattleLifecycleEngine.evaluateCattleStage(animal, animalEvents, realCowMilkLogs)
        } else null
    }

    val isNonLactatingStage = isCattle && (
        isExplicitNonLactating ||
        cattleEval?.stage == CattleStage.HEIFER ||
        cattleEval?.stage == CattleStage.CALF ||
        cattleEval?.stage == CattleStage.BULL ||
        (cattleEval != null && !cattleEval.hasGivenBirthPreviously && !cattleEval.isMilking)
    )

    val initialDisplayStatus = remember(animal.id, animal.status, cattleEval) {
        if (isCattle && cattleEval != null && !animal.status.startsWith("DISPOSED", ignoreCase = true)) {
            cattleEval.stage.displayName
        } else {
            animal.status
        }
    }

    var currentStatus by remember(animal.id, animal.status, initialDisplayStatus) { mutableStateOf(initialDisplayStatus) }
    var showUpdateStageDialog by remember { mutableStateOf(false) }
    var showDisposeDialog by remember { mutableStateOf(false) }
    var showStageInfoDialog by remember { mutableStateOf(false) }

    // Keep animal status in sync with calculated stage if cattle
    LaunchedEffect(animal.id, cattleEval?.stage) {
        if (cattleEval != null && !animal.status.startsWith("DISPOSED", ignoreCase = true)) {
            if (currentStatus != cattleEval.stage.displayName || animal.status != cattleEval.stage.displayName) {
                currentStatus = cattleEval.stage.displayName
                onUpdateAnimalStage(cattleEval.stage.displayName, cattleEval.breedingStatusText)
            }
        }
    }

    if (showDisposeDialog) {
        DisposeAnimalDialog(
            animalName = animal.name,
            tagNumber = animal.tagNumber,
            onDismiss = { showDisposeDialog = false },
            onConfirmDispose = { reason, amount, notes, date ->
                currentStatus = "DISPOSED ($reason)"
                showDisposeDialog = false
                onDisposeAnimal(reason, amount, notes, date)
            }
        )
    }

    if (showStageInfoDialog && cattleEval != null) {
        Dialog(onDismissRequest = { showStageInfoDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .clip(RoundedCornerShape(20.dp)),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = cattleEval.badgeBgColor
                            ) {
                                Text(
                                    text = cattleEval.stage.displayName,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = cattleEval.badgeTextColor
                                )
                            }
                        }
                        IconButton(onClick = { showStageInfoDialog = false }) {
                            Icon(Icons.Filled.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "Automatic Stage Calculation",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Current Evaluation:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = cattleEval.explanation,
                                fontSize = 13.sp,
                                color = Color(0xFF1E293B)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "How Cattle Stages Work:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "• CALF: Cattle under 6 months old.\n" +
                        "• HEIFER: Female >= 6 months old with no calves born yet.\n" +
                        "• INCALF: Confirmed pregnant through positive Pregnancy Diagnosis (PD) check.\n" +
                        "• INCALF / MILKING: Confirmed pregnant while currently actively milking.\n" +
                        "• DRY: Non-lactating cow (rest period ~60 days before calving).\n" +
                        "• MILKING: Actively lactating cow.\n" +
                        "• BULL: Male breeding stock.",
                        fontSize = 12.sp,
                        color = Color(0xFF334155),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                showStageInfoDialog = false
                                showUpdateStageDialog = true
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Manual Override", fontSize = 11.sp)
                        }
                        Button(
                            onClick = {
                                showStageInfoDialog = false
                                showAddCattleEventDialog = true
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                        ) {
                            Text("+ Log Event", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showUpdateStageDialog) {
        Dialog(onDismissRequest = { showUpdateStageDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(20.dp)),
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Manual Stage Override",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        IconButton(onClick = { showUpdateStageDialog = false }) {
                            Icon(Icons.Filled.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Normally stages update automatically via Log Events (AI, PD, Calving, Dry Off). You can also set a manual override:",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    val isAutoManaged = currentStatus.isBlank() || currentStatus.equals("ACTIVE", ignoreCase = true) || currentStatus.equals("AUTO", ignoreCase = true)

                    // Automatic Option
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isAutoManaged) ForestGreenPrimary.copy(alpha = 0.12f) else Color(0xFFF8FAFC),
                        border = BorderStroke(
                            1.dp,
                            if (isAutoManaged) ForestGreenPrimary else Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clickable {
                                val autoStatus = cattleEval?.stage?.displayName ?: "Milking"
                                val autoBreeding = cattleEval?.breedingStatusText ?: "Healthy"
                                currentStatus = autoStatus
                                onUpdateAnimalStage(autoStatus, autoBreeding)
                                showUpdateStageDialog = false
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isAutoManaged) Icons.Filled.CheckCircle else Icons.Filled.Pets,
                                contentDescription = null,
                                tint = if (isAutoManaged) ForestGreenPrimary else Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "✨ Auto-Manage (Recommended)",
                                    fontSize = 13.sp,
                                    fontWeight = if (isAutoManaged) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isAutoManaged) ForestGreenPrimary else Color(0xFF1E293B)
                                )
                                Text(
                                    "Current Stage: ${cattleEval?.stage?.displayName ?: "Dynamic"} (${cattleEval?.breedingStatusText ?: "Calculated"})",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }

                    val stages = listOf(
                        "MILKING" to "🥛 MILKING (Active Lactation)",
                        "INCALF_MILKING" to "🥛🤰 INCALF / MILKING (Pregnant + Lactating)",
                        "INCALF" to "🤰 INCALF (Confirmed Pregnant)",
                        "DRY" to "🍂 DRY (Non-Lactating Gestation)",
                        "CALF" to "🍼 CALF (Young Stock)",
                        "HEIFER" to "🌾 HEIFER (Pre-calving Female)",
                        "BULL" to "🐂 BULL (Breeding Male)",
                        "DISPOSED" to "🚫 DISPOSED (Culled / Sold)"
                    )

                    stages.forEach { (stageKey, stageLabel) ->
                        val targetStatusName = when (stageKey) {
                            "INCALF_MILKING" -> "INCALF / MILKING"
                            else -> stageKey
                        }
                        val isCurrentStageCalculated = cattleEval?.stage?.name.equals(stageKey, ignoreCase = true)
                        val isSelected = !isAutoManaged && (currentStatus.equals(stageKey, ignoreCase = true) || currentStatus.equals(targetStatusName, ignoreCase = true))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) ForestGreenPrimary.copy(alpha = 0.12f) else Color(0xFFF8FAFC),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) ForestGreenPrimary else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .clickable {
                                    val newStatus = when (stageKey) {
                                        "INCALF_MILKING" -> "INCALF / MILKING"
                                        "INCALF" -> "INCALF"
                                        "DRY" -> "DRY"
                                        "MILKING" -> "MILKING"
                                        "HEIFER" -> "HEIFER"
                                        "CALF" -> "CALF"
                                        "BULL" -> "BULL"
                                        "DISPOSED" -> "DISPOSED"
                                        else -> stageKey
                                    }
                                    currentStatus = newStatus
                                    onUpdateAnimalStage(newStatus, if (stageKey.contains("INCALF")) "Confirmed Pregnant" else newStatus)
                                    showUpdateStageDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Filled.Pets,
                                    contentDescription = null,
                                    tint = if (isSelected) ForestGreenPrimary else Color(0xFF94A3B8),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        stageLabel,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) ForestGreenPrimary else Color(0xFF1E293B)
                                    )
                                    if (isCurrentStageCalculated) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = ForestGreenPrimary.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                "Dynamic Stage",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ForestGreenPrimary,
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
        }
    }

    val tasks by viewModel.rawTasks.collectAsStateWithLifecycle(initialValue = emptyList<com.example.data.FarmTask>())
    val reminderCompletions by viewModel.reminderCompletions.collectAsStateWithLifecycle(initialValue = emptyList())
    val allDbPoultryLogs by viewModel.allPoultryLogs.collectAsStateWithLifecycle(initialValue = emptyList())

    val latestMilkLog = cowMilkLogs.firstOrNull()

    val displayLastMilk = remember(latestMilkLog, eggLogs, animal.lastMilk, isPoultry, isNonLactatingStage) {
        if (isPoultry) {
            val poultryEggLogs = eggLogs.filter { it.unitName.equals(animal.name, ignoreCase = true) || it.unitName.contains(animal.name, ignoreCase = true) }
                .sortedByDescending { it.id }
            if (poultryEggLogs.isNotEmpty()) "${poultryEggLogs.first().totalEggs} Eggs" else if (animal.headCountInt > 0) "${animal.headCountInt} Birds" else animal.lastMilk
        } else if (isNonLactatingStage) {
            "Not Lactating"
        } else if (latestMilkLog != null) {
            "${"%.1f".format(latestMilkLog.litres)}L"
        } else if (animal.lastMilk.isNotBlank() && animal.lastMilk != "No data yet") {
            animal.lastMilk
        } else {
            "No data yet"
        }
    }

    // Initialize events & alerts dynamically
    val cattleNotifications = remember(cattleEval, dbEvents, tasks, eggLogs, isPoultry, reminderCompletions, allDbPoultryLogs) {
        generateAnimalUpcomingEvents(
            animal = animal,
            cattleEval = cattleEval,
            animalEvents = animalEvents.toList(),
            tasks = tasks,
            eggLogs = eggLogs,
            isPoultry = isPoultry,
            poultryLogs = allDbPoultryLogs,
            reminderCompletions = reminderCompletions
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Action Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1E293B))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Animal Details",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                }

                if (canEdit) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Edit Animal Button
                        Surface(
                            onClick = onEditAnimal,
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFECFDF5),
                            border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("edit_animal_topbar_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Edit Animal",
                                    tint = ForestGreenPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Edit",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreenPrimary
                                )
                            }
                        }

                        // Dispose Animal Button
                        if (!currentStatus.contains("DISPOSED", ignoreCase = true)) {
                            Surface(
                                onClick = { showDisposeDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFFFBEB),
                                border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                                modifier = Modifier
                                    .height(36.dp)
                                    .testTag("dispose_animal_topbar_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.RemoveCircleOutline,
                                        contentDescription = "Dispose Animal",
                                        tint = Color(0xFFB45309),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Dispose",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB45309)
                                    )
                                }
                            }
                        }

                        // Delete Animal Button
                        Surface(
                            onClick = onDeleteAnimal,
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEF2F2),
                            border = BorderStroke(1.dp, Color(0xFFFECACA)),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("delete_animal_topbar_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DeleteForever,
                                    contentDescription = "Delete Animal",
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Delete",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDC2626)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Disposed Animal Record Banner Card
        if (currentStatus.contains("DISPOSED", ignoreCase = true) || animal.disposalReason.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Info, contentDescription = null, tint = Color(0xFFDC2626))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ANIMAL DISPOSED RECORD",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• Disposal Reason: ${animal.disposalReason.ifBlank { currentStatus }}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF7F1D1D)
                        )
                        if (animal.disposalDate.isNotBlank()) {
                            Text(
                                text = "• Date Disposed: ${animal.disposalDate}",
                                fontSize = 12.sp,
                                color = Color(0xFF991B1B)
                            )
                        }
                        if (animal.disposalAmount > 0) {
                            Text(
                                text = "• Sale Income Recorded: KSh ${animal.disposalAmount} (Added to Finance Income)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreenPrimary
                            )
                        }
                        if (animal.disposalNotes.isNotBlank()) {
                            Text(
                                text = "• Buyer / Details: ${animal.disposalNotes}",
                                fontSize = 12.sp,
                                color = Color(0xFF7F1D1D)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Note: Animal is removed from active milking list. All historical records remain saved below.",
                            fontSize = 11.sp,
                            color = Color(0xFF991B1B)
                        )
                    }
                }
            }
        }

        // Animal Photo Header / Avatar Card
        item {
            val context = androidx.compose.ui.platform.LocalContext.current
            var showPhotoSourceDialog by remember { mutableStateOf(false) }
            var showCameraCaptureDialog by remember { mutableStateOf(false) }
            val photoGalleryLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri: Uri? ->
                if (uri != null) {
                    val saved = ImageStorageUtils.saveImageToInternalStorage(context, uri) ?: uri.toString()
                    onUpdatePhoto(saved)
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Animal Photo Avatar / Fallback Icon
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isPoultry) Color(0xFFFEF3C7) else Color(0xFFE8F5E9))
                                .border(
                                    1.dp,
                                    if (isPoultry) Color(0xFFFDE68A) else Color(0xFFC8E6C9),
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { showPhotoSourceDialog = true }
                                .testTag("animal_photo_avatar_box"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!animal.photoUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(ImageStorageUtils.resolveImageModel(animal.photoUri))
                                        .crossfade(true)
                                        .memoryCachePolicy(CachePolicy.ENABLED)
                                        .diskCachePolicy(CachePolicy.ENABLED)
                                        .build(),
                                    contentDescription = "${animal.name} Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = if (isPoultry) Icons.Filled.Egg else Icons.Filled.Pets,
                                        contentDescription = "Generic Animal Icon",
                                        tint = if (isPoultry) Color(0xFFD97706) else ForestGreenPrimary,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("No Photo", fontSize = 9.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        // Info & Photo Upload Trigger
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = animal.name,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Tag: ${animal.tagNumber}  •  ${animal.breed}",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { showPhotoSourceDialog = true },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .height(34.dp)
                                        .testTag("upload_animal_photo_button")
                                ) {
                                    Icon(
                                        imageVector = if (animal.photoUri.isNullOrBlank()) Icons.Filled.AddPhotoAlternate else Icons.Filled.CameraAlt,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (animal.photoUri.isNullOrBlank()) "Add Photo" else "Change",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (!animal.photoUri.isNullOrBlank()) {
                                    OutlinedButton(
                                        onClick = { onUpdatePhoto(null) },
                                        shape = RoundedCornerShape(10.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        modifier = Modifier
                                            .height(34.dp)
                                            .testTag("remove_animal_photo_button")
                                    ) {
                                        Text("Remove", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (animal.notes.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF0FDF4),
                    border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Notes / Origin", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(animal.notes, fontSize = 13.sp, color = Color(0xFF334155))
                    }
                }
            }

            if (showPhotoSourceDialog) {
                AlertDialog(
                    onDismissRequest = { showPhotoSourceDialog = false },
                    title = { Text("Update Animal Photo", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Choose a photo from your camera or gallery to identify ${animal.name}.",
                                fontSize = 13.sp,
                                color = Color(0xFF475569)
                            )
                            if (!animal.photoUri.isNullOrBlank()) {
                                OutlinedButton(
                                    onClick = {
                                        showPhotoSourceDialog = false
                                        onUpdatePhoto(null)
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color(0xFFFECACA)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                                    modifier = Modifier.fillMaxWidth().testTag("dialog_remove_animal_photo_button")
                                ) {
                                    Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFDC2626))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Remove Current Photo", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFDC2626))
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showPhotoSourceDialog = false
                                showCameraCaptureDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Use Camera")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = {
                                showPhotoSourceDialog = false
                                photoGalleryLauncher.launch("image/*")
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("From Gallery")
                        }
                    }
                )
            }

            if (showCameraCaptureDialog) {
                CameraCaptureDialog(
                    onDismiss = { showCameraCaptureDialog = false },
                    onPhotoCaptured = { uri ->
                        showCameraCaptureDialog = false
                        val saved = ImageStorageUtils.saveImageToInternalStorage(context, uri) ?: uri.toString()
                        onUpdatePhoto(saved)
                    }
                )
            }
        }

        // Animal Main Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = animal.name,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isCattle && cattleEval != null) cattleEval.badgeBgColor else TagLivestockBg,
                                    modifier = Modifier.clickable {
                                        if (isCattle && cattleEval != null) {
                                            showStageInfoDialog = true
                                        } else {
                                            showUpdateStageDialog = true
                                        }
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isCattle && cattleEval != null) cattleEval.stage.displayName else currentStatus,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCattle && cattleEval != null) cattleEval.badgeTextColor else TagLivestockText
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            Icons.Filled.Info,
                                            contentDescription = "Stage Details",
                                            tint = if (isCattle && cattleEval != null) cattleEval.badgeTextColor else TagLivestockText,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "Tag: ${animal.tagNumber}  •  ${animal.breed}",
                                fontSize = 14.sp,
                                color = Color(0xFF64748B),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Age", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text(animal.age, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        }
                        Column {
                            Text("Weight", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text(animal.weight, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        }
                        Column {
                            Text(if (isPoultry) "Daily Egg Yield" else if (isNonLactatingStage) "Lactation" else "Last Milk", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text(displayLastMilk, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (isNonLactatingStage) Color(0xFF64748B) else ForestGreenPrimary)
                            if (latestMilkLog != null && !isPoultry && !isNonLactatingStage) {
                                Text(
                                    text = "${latestMilkLog.session.lowercase().replaceFirstChar { it.uppercase() }} (${latestMilkLog.date})",
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Lineage & Birth Details Card (Cattle Only)
        if (isCattle) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Pets, contentDescription = null, tint = ForestGreenPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Lineage & Birth Details",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("DATE OF BIRTH", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(animal.dateOfBirth.ifBlank { "N/A" }, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("BIRTH WEIGHT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(animal.weightAtBirth.ifBlank { "N/A" }, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("SIRE (FATHER)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(animal.sire.ifBlank { "N/A" }, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("DAM (MOTHER)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(animal.dam.ifBlank { "N/A" }, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                            }
                        }
                    }
                }
            }

            // Upcoming Events & Notifications Alerts Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFEF3C7))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Notifications, contentDescription = null, tint = Color(0xFFD97706))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Upcoming Events & Alerts",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFEF3C7)
                            ) {
                                Text(
                                    text = "${cattleNotifications.size} Active",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        cattleNotifications.forEach { note ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = note.badgeColor,
                                border = androidx.compose.foundation.BorderStroke(1.dp, note.badgeTextColor.copy(alpha = 0.2f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        if (note.actionCategory != null && isCattle) {
                                            cattleEventDialogCategory = note.actionCategory
                                            showAddCattleEventDialog = true
                                        }
                                    }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = note.badgeTextColor
                                            ) {
                                                Text(
                                                    text = note.urgencyLabel,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = note.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = note.badgeTextColor
                                            )
                                        }
                                        Text(
                                            text = note.dueDate,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = note.badgeTextColor
                                        )
                                    }
                                    if (note.details.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = note.details,
                                            fontSize = 11.sp,
                                            color = note.badgeTextColor.copy(alpha = 0.9f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Quick Action Buttons for Cattle / Livestock
            item {
                if (isCattle) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    cattleEventDialogCategory = "CALVING"
                                    showAddCattleEventDialog = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("log_calving_date_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Log Calving Date", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            Button(
                                onClick = {
                                    cattleEventDialogCategory = "HEALTH"
                                    showAddCattleEventDialog = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("log_health_record_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                            ) {
                                Icon(Icons.Filled.MedicalServices, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Log Health / Meds", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    cattleEventDialogCategory = "PD"
                                    showAddCattleEventDialog = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("log_pd_button"),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, ForestGreenPrimary)
                            ) {
                                Text("Pregnancy Check (PD)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                            }

                            OutlinedButton(
                                onClick = {
                                    cattleEventDialogCategory = "INSEMINATION"
                                    showAddCattleEventDialog = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("log_insemination_button"),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFF64748B))
                            ) {
                                Text("AI / Insemination", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                            }
                        }
                    }
                } else {
                    Button(
                        onClick = { showAddCattleEventDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("+ LOG EVENT", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // Calving History & Parity Log Card (for cows that have given birth)
            if (isCattle) {
                val hasCalved = calvingLogs.isNotEmpty() || (cattleEval != null && cattleEval.lastCalvingDate != null)

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, if (hasCalved) Color(0xFFBBF7D0) else Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (hasCalved) Color(0xFFDCFCE7) else Color(0xFFF1F5F9)
                                    ) {
                                        Icon(
                                            Icons.Filled.Pets,
                                            contentDescription = null,
                                            tint = if (hasCalved) ForestGreenPrimary else Color(0xFF64748B),
                                            modifier = Modifier
                                                .padding(6.dp)
                                                .size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Calving History & Parity",
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E293B)
                                        )
                                        Text(
                                            text = if (calvingLogs.isNotEmpty()) "Parity: ${calvingLogs.size} ${if (calvingLogs.size == 1) "Calving" else "Calvings"} Recorded" else if (hasCalved) "Parity: 1+ Calving (Active Lactation)" else "Heifer (No previous calvings)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (hasCalved) Color(0xFF15803D) else Color(0xFF64748B)
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        cattleEventDialogCategory = "CALVING"
                                        showAddCattleEventDialog = true
                                    }
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = "Add Calving", tint = ForestGreenPrimary)
                                }
                            }

                            if (calvingLogs.isEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF8FAFC),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = if (hasCalved) "Cow has previous lactation history. Click '+ Log Calving Date' to add detailed calf records." else "No calving events logged yet. When this cow calves, log the date here to automatically track lactation and next breeding cycle.",
                                            fontSize = 12.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 240.dp)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        calvingLogs.forEachIndexed { idx, cLog ->
                                            var calvingMenuExpanded by remember { mutableStateOf(false) }

                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = Color(0xFFF0FDF4),
                                                border = BorderStroke(1.dp, Color(0xFFDCFCE7)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.weight(1f)
                                                        ) {
                                                            Surface(
                                                                shape = RoundedCornerShape(6.dp),
                                                                color = ForestGreenPrimary
                                                            ) {
                                                                Text(
                                                                    text = "CALVING #${calvingLogs.size - idx}",
                                                                    fontSize = 10.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = Color.White,
                                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                                )
                                                            }
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text(
                                                                text = cLog.date,
                                                                fontSize = 13.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color(0xFF0F172A)
                                                            )
                                                        }

                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            if (cLog.metricValue.isNotBlank()) {
                                                                Text(
                                                                    text = cLog.metricValue,
                                                                    fontSize = 11.sp,
                                                                    fontWeight = FontWeight.SemiBold,
                                                                    color = Color(0xFF166534)
                                                                )
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                            }

                                                            Box {
                                                                IconButton(
                                                                    onClick = { calvingMenuExpanded = true },
                                                                    modifier = Modifier.size(28.dp)
                                                                ) {
                                                                    Icon(
                                                                        Icons.Filled.MoreVert,
                                                                        contentDescription = "Calving Log Options",
                                                                        tint = Color(0xFF64748B),
                                                                        modifier = Modifier.size(18.dp)
                                                                    )
                                                                }

                                                                DropdownMenu(
                                                                    expanded = calvingMenuExpanded,
                                                                    onDismissRequest = { calvingMenuExpanded = false }
                                                                ) {
                                                                    DropdownMenuItem(
                                                                        text = { Text("Edit Record", fontSize = 13.sp) },
                                                                        leadingIcon = {
                                                                            Icon(
                                                                                Icons.Filled.Edit,
                                                                                contentDescription = "Edit",
                                                                                tint = ForestGreenPrimary,
                                                                                modifier = Modifier.size(18.dp)
                                                                            )
                                                                        },
                                                                        onClick = {
                                                                            calvingMenuExpanded = false
                                                                            eventToEdit = cLog
                                                                        }
                                                                    )
                                                                    DropdownMenuItem(
                                                                        text = {
                                                                            Text(
                                                                                "Delete Record",
                                                                                fontSize = 13.sp,
                                                                                color = Color(0xFFDC2626)
                                                                            )
                                                                        },
                                                                        leadingIcon = {
                                                                            Icon(
                                                                                Icons.Filled.Delete,
                                                                                contentDescription = "Delete",
                                                                                tint = Color(0xFFDC2626),
                                                                                modifier = Modifier.size(18.dp)
                                                                            )
                                                                        },
                                                                        onClick = {
                                                                            calvingMenuExpanded = false
                                                                            eventToDelete = cLog
                                                                            showDeleteEventConfirmDialog = true
                                                                        }
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = cLog.details,
                                                        fontSize = 12.sp,
                                                        color = Color(0xFF334155)
                                                    )

                                                    if (cLog.notes.isNotBlank()) {
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Text(
                                                            text = cLog.notes,
                                                            fontSize = 11.sp,
                                                            color = Color(0xFF64748B)
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
                }
            }

            // Events Log & Records (Heat, Insemination, Calving, Weight, Health)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Event, contentDescription = null, tint = ForestGreenPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (sortedAnimalEvents.isNotEmpty()) "Events & Health Logs (${sortedAnimalEvents.size})" else "Events & Health Logs",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                            }

                            IconButton(
                                onClick = {
                                    cattleEventDialogCategory = "HEALTH"
                                    showAddCattleEventDialog = true
                                }
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "Add Event", tint = ForestGreenPrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (sortedAnimalEvents.isEmpty()) {
                            Text(
                                "No events recorded yet. Click '+ Log Event' to add health, breeding, or weight records.",
                                color = Color.Gray,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            // Scrollable box bounded in height instead of taking over the entire page
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 280.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    sortedAnimalEvents.forEach { ev ->
                                        var eventMenuExpanded by remember { mutableStateOf(false) }

                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFFF8FAFC),
                                            border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Surface(
                                                            shape = RoundedCornerShape(6.dp),
                                                            color = when (ev.category.uppercase()) {
                                                                "CALVING" -> Color(0xFFDCFCE7)
                                                                "PD" -> Color(0xFFFEF3C7)
                                                                "HEAT" -> Color(0xFFFFEDD5)
                                                                "INSEMINATION" -> Color(0xFFE0F2FE)
                                                                "WEIGHT" -> Color(0xFFF3E8FF)
                                                                "DRY_OFF" -> Color(0xFFECFDF5)
                                                                "ABORTED" -> Color(0xFFFEE2E2)
                                                                else -> Color(0xFFFEE2E2)
                                                            }
                                                        ) {
                                                            Text(
                                                                text = when (ev.category.uppercase()) {
                                                                    "CALVING" -> "CALVING"
                                                                    "PD" -> "PD"
                                                                    "HEAT" -> "HEAT"
                                                                    "INSEMINATION" -> "AI"
                                                                    "WEIGHT" -> "WEIGHT"
                                                                    "DRY_OFF" -> "DRY OFF"
                                                                    "ABORTED" -> "ABORTED"
                                                                    else -> "HEALTH"
                                                                },
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = when (ev.category.uppercase()) {
                                                                    "CALVING" -> Color(0xFF15803D)
                                                                    "PD" -> Color(0xFFB45309)
                                                                    "HEAT" -> Color(0xFFC2410C)
                                                                    "INSEMINATION" -> Color(0xFF0369A1)
                                                                    "WEIGHT" -> Color(0xFF7E22CE)
                                                                    "DRY_OFF" -> Color(0xFF047857)
                                                                    "ABORTED" -> Color(0xFFDC2626)
                                                                    else -> Color(0xFF991B1B)
                                                                }
                                                            )
                                                        }

                                                        Spacer(modifier = Modifier.width(8.dp))

                                                        Text(
                                                            text = ev.title,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 13.sp,
                                                            color = Color(0xFF1E293B),
                                                            maxLines = 1,
                                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                        )
                                                    }

                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = ev.date,
                                                            fontSize = 11.sp,
                                                            color = Color(0xFF64748B)
                                                        )

                                                        Box {
                                                            IconButton(
                                                                onClick = { eventMenuExpanded = true },
                                                                modifier = Modifier.size(28.dp)
                                                            ) {
                                                                Icon(
                                                                    Icons.Filled.MoreVert,
                                                                    contentDescription = "Event Options",
                                                                    tint = Color(0xFF64748B),
                                                                    modifier = Modifier.size(18.dp)
                                                                )
                                                            }

                                                            DropdownMenu(
                                                                expanded = eventMenuExpanded,
                                                                onDismissRequest = { eventMenuExpanded = false }
                                                            ) {
                                                                DropdownMenuItem(
                                                                    text = { Text("Edit Event", fontSize = 13.sp) },
                                                                    leadingIcon = {
                                                                        Icon(
                                                                            Icons.Filled.Edit,
                                                                            contentDescription = "Edit",
                                                                            tint = ForestGreenPrimary,
                                                                            modifier = Modifier.size(18.dp)
                                                                        )
                                                                    },
                                                                    onClick = {
                                                                        eventMenuExpanded = false
                                                                        eventToEdit = ev
                                                                    }
                                                                )
                                                                DropdownMenuItem(
                                                                    text = {
                                                                        Text(
                                                                            "Delete Event",
                                                                            fontSize = 13.sp,
                                                                            color = Color(0xFFDC2626)
                                                                        )
                                                                    },
                                                                    leadingIcon = {
                                                                        Icon(
                                                                            Icons.Filled.Delete,
                                                                            contentDescription = "Delete",
                                                                            tint = Color(0xFFDC2626),
                                                                            modifier = Modifier.size(18.dp)
                                                                        )
                                                                    },
                                                                    onClick = {
                                                                        eventMenuExpanded = false
                                                                        eventToDelete = ev
                                                                        showDeleteEventConfirmDialog = true
                                                                    }
                                                                )
                                                            }
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(4.dp))

                                                Text(
                                                    text = ev.details,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF334155)
                                                )

                                                if (ev.notes.isNotBlank() || ev.metricValue.isNotBlank()) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        if (ev.notes.isNotBlank()) {
                                                            Text(
                                                                text = "Notes: ${ev.notes}",
                                                                fontSize = 11.sp,
                                                                color = Color(0xFF64748B),
                                                                modifier = Modifier.weight(1f, fill = false)
                                                            )
                                                        }
                                                        if (ev.metricValue.isNotBlank()) {
                                                            Text(
                                                                text = ev.metricValue,
                                                                fontSize = 12.sp,
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
                            }
                        }
                    }
                }
            }
        }

        // Breeding Status Summary Card (Dynamic based on records)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isCattle && cattleEval != null) cattleEval.badgeBgColor else Color(0xFFDCFCE7)
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.SentimentSatisfied,
                                contentDescription = null,
                                tint = if (isCattle && cattleEval != null) cattleEval.badgeTextColor else ForestGreenPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Breeding Summary",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isCattle && cattleEval != null) cattleEval.badgeBgColor else Color(0xFFDCFCE7)
                        ) {
                            Text(
                                text = if (isCattle && cattleEval != null) cattleEval.breedingStatusText else animal.breedingStatus,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCattle && cattleEval != null) cattleEval.badgeTextColor else ForestGreenPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (isCattle && cattleEval != null) {
                        // Current Stage & Reproduction Summary
                        if (cattleEval.summaryReason.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = cattleEval.badgeBgColor.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = cattleEval.summaryReason,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    fontSize = 12.sp,
                                    color = cattleEval.badgeTextColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Calving History & Parity
                        if (cattleEval.hasGivenBirthPreviously && cattleEval.lastCalvingDate != null) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Calving History / Parity", fontSize = 13.sp, color = Color(0xFF64748B))
                                Text(
                                    "${cattleEval.lastCalvingDate} (Parity ${cattleEval.parityCount})",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1E293B)
                                )
                            }
                            if (cattleEval.daysInMilk != null && cattleEval.isMilking) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Days in Milk (DIM)", fontSize = 13.sp, color = Color(0xFF64748B))
                                    Text(
                                        "${cattleEval.daysInMilk} days lactating",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF0369A1)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // AI / Breeding Date
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Last Insemination / Mating", fontSize = 13.sp, color = Color(0xFF64748B))
                            Text(
                                if (cattleEval.lastInseminationDate != null) cattleEval.lastInseminationDate!! else if (cattleEval.hasGivenBirthPreviously) "None in current lactation" else "None Recorded",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (cattleEval.lastInseminationDate != null) Color(0xFF1E293B) else Color(0xFF94A3B8)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Gestation Progress if pregnant
                        if (cattleEval.daysInGestation != null && cattleEval.daysInGestation > 0 && cattleEval.isInCalf) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Gestation Progress", fontSize = 13.sp, color = Color(0xFF64748B))
                                Text(
                                    "Day ${cattleEval.daysInGestation} / 283 (${(cattleEval.daysInGestation * 100 / 283).coerceIn(0, 100)}%)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0369A1)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { (cattleEval.daysInGestation / 283f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFF0284C7),
                                trackColor = Color(0xFFE0F2FE)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Expected Calving
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Expected Calving Date", fontSize = 13.sp, color = Color(0xFF64748B))
                            Text(
                                if (cattleEval.isInCalf) (cattleEval.expectedCalvingDate ?: "Pending") else if (cattleEval.expectedCalvingDate != null) "${cattleEval.expectedCalvingDate} (If Conceived)" else "Open / Not In-Calf",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (cattleEval.isInCalf && cattleEval.expectedCalvingDate != null) ForestGreenPrimary else if (cattleEval.expectedCalvingDate != null) Color(0xFF7C3AED) else Color(0xFF94A3B8)
                            )
                        }

                        if (cattleEval.isDriedOff) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Dry-Off Status", fontSize = 13.sp, color = Color(0xFF64748B))
                                Text(
                                    "Dried Off (Udder Rest)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFB45309)
                                )
                            }
                        } else if (cattleEval.dryOffTargetDate != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Recommended Dry-Off Date", fontSize = 13.sp, color = Color(0xFF64748B))
                                Text(
                                    cattleEval.dryOffTargetDate!!,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }
                    } else {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Breeding Status", fontSize = 13.sp, color = Color(0xFF64748B))
                            Text(animal.breedingStatus, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                        }
                    }
                }
            }
        }

        // Yield Productivity 7-Days Bar Chart (Dynamic Data with Real Values)
        if (isPoultry || !isNonLactatingStage) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
                    val shortDayFormat = remember { SimpleDateFormat("EEE", Locale.getDefault()) }

                    val last7DaysData = remember(animal, milkLogs, eggLogs, isPoultry) {
                        val calKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                        (6 downTo 0).map { dayOffset ->
                            val c = java.util.Calendar.getInstance()
                            c.add(java.util.Calendar.DAY_OF_YEAR, -dayOffset)
                            val fullDate = dateFormat.format(c.time)
                            val targetKey = calKeyFormat.format(c.time)
                            val dayName = shortDayFormat.format(c.time)

                            val yieldVal = if (isPoultry) {
                                val matched = eggLogs.filter { log ->
                                    val flockMatches = log.unitName.equals(animal.name, ignoreCase = true) ||
                                        log.unitName.contains(animal.name, ignoreCase = true) ||
                                        animal.name.contains(log.unitName, ignoreCase = true)
                                    if (!flockMatches) return@filter false

                                    val parsedDate = DateValidationUtils.parseDate(log.loggedAt)
                                        ?: log.notes?.let { n -> DateValidationUtils.parseDate(n.substringAfter("[", "").substringBefore("]", "")) }

                                    if (parsedDate != null) {
                                        val logCal = java.util.Calendar.getInstance().apply { time = parsedDate }
                                        val sameYear = logCal.get(java.util.Calendar.YEAR) == c.get(java.util.Calendar.YEAR) ||
                                            logCal.get(java.util.Calendar.YEAR) < 2000
                                        sameYear && logCal.get(java.util.Calendar.DAY_OF_YEAR) == c.get(java.util.Calendar.DAY_OF_YEAR)
                                    } else {
                                        val shortDay = SimpleDateFormat("dd MMM", Locale.getDefault()).format(c.time)
                                        log.loggedAt.contains(fullDate, ignoreCase = true) ||
                                        log.loggedAt.contains(targetKey) ||
                                        log.loggedAt.contains(shortDay, ignoreCase = true)
                                    }
                                }
                                matched.sumOf { it.totalEggs }.toFloat()
                            } else {
                                val cowLogs = MilkLogEntryRules.findLogsForCow(milkLogs, animal.name, animal.tagNumber)
                                val matched = cowLogs.filter { log ->
                                    val logKey = MilkLogEntryRules.canonicalDateKey(log.date)
                                    logKey == targetKey || log.date.equals(fullDate, ignoreCase = true)
                                }
                                matched.sumOf { it.litres }.toFloat()
                            }

                            Triple(dayName, yieldVal, fullDate)
                        }
                    }

                    val maxYield = (last7DaysData.map { it.second }.maxOrNull() ?: 10f).coerceAtLeast(if (isPoultry) 50f else 10f)
                    val total7Days = last7DaysData.sumOf { it.second.toDouble() }
                    val lastLoggedVal = last7DaysData.lastOrNull()?.second ?: 0f

                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isPoultry) "Egg Laying Yield (7 Days)" else "Milk Productivity (7 Days)",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = if (isPoultry) {
                                        if (lastLoggedVal > 0) "Today: ${lastLoggedVal.toInt()} Eggs (${"%.1f".format(lastLoggedVal / 30.0)} Trays)"
                                        else "Total 7-Day: ${total7Days.toInt()} Eggs"
                                    } else {
                                        if (lastLoggedVal > 0) "Today: ${"%.1f".format(lastLoggedVal)}L"
                                        else "Total 7-Day: ${"%.1f".format(total7Days)}L"
                                    },
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            last7DaysData.forEachIndexed { idx, (day, valAmt, _) ->
                                val heightRatio = (valAmt / maxYield).coerceIn(if (valAmt > 0) 0.15f else 0.04f, 1f)
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    if (valAmt > 0f) {
                                        Text(
                                            text = if (isPoultry) "${valAmt.toInt()}" else "%.1f".format(valAmt),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isPoultry) Color(0xFF92400E) else ForestGreenPrimary
                                        )
                                    } else {
                                        Text("-", fontSize = 9.sp, color = Color(0xFF94A3B8))
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(22.dp)
                                            .height((90 * heightRatio).dp)
                                            .background(
                                                if (valAmt == 0f) Color(0xFFE2E8F0)
                                                else if (isPoultry) Color(0xFFD97706)
                                                else ForestGreenPrimary,
                                                RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = day,
                                        fontSize = 11.sp,
                                        fontWeight = if (idx == 6) FontWeight.Bold else FontWeight.Normal,
                                        color = if (idx == 6) Color(0xFF1E293B) else Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFFEF3C7), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Info, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Pre-Calving Stock (No Lactation)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "This animal is in a pre-calving stage. Milk production tracking will begin automatically once her first calving event is logged.",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }

    if (showAddCattleEventDialog) {
        AddCattleEventDialog(
            animalName = animal.name,
            unitId = unitId,
            initialCategory = cattleEventDialogCategory,
            onDismiss = { showAddCattleEventDialog = false },
            onRequestRecordExpense = { expCategory, expDesc, expDate ->
                pendingFinanceCategory = expCategory
                pendingFinanceDescription = expDesc
                pendingFinanceDate = expDate
                showRecordFinanceDialog = true
            },
            onSaveEvent = { type: String, title: String, date: String, details: String, notes: String, metricValue: String, reminderText: String, calfInfo: CalvingCalfInfo? ->
                viewModel.addCattleEvent(
                    unitId = unitId,
                    category = type,
                    title = title,
                    date = date,
                    details = details,
                    notes = notes,
                    metricValue = metricValue
                )

                // Automatically create a new animal/unit record for the calf in the farm's animal list
                if (type.equals("CALVING", ignoreCase = true) && calfInfo != null) {
                    val finalCalfName = when {
                        calfInfo.calfName.isNotBlank() -> calfInfo.calfName
                        calfInfo.calfTag.isNotBlank() -> "Calf ${calfInfo.calfTag}"
                        else -> "Calf of ${animal.name}"
                    }
                    val isMale = calfInfo.calfGender.contains("Bull", ignoreCase = true) || calfInfo.calfGender.contains("Male", ignoreCase = true)
                    val isTwins = calfInfo.calfGender.contains("Twins", ignoreCase = true)
                    val calfSubType = when {
                        isMale -> "Bull Calf"
                        isTwins -> "Twin Calves"
                        else -> "Heifer Calf"
                    }
                    val birthWeight = calfInfo.birthWeight.ifBlank { "34 kg" }
                    val headCount = if (isTwins) 2 else 1
                    val tagNumber = calfInfo.calfTag.ifBlank { "#${System.currentTimeMillis() % 1000}" }
                    val damName = animal.name
                    val sireName = animal.sire.takeIf { it.isNotBlank() && it != "N/A" } ?: ""

                    viewModel.addNewUnit(
                        name = finalCalfName,
                        type = "Cattle",
                        headCount = headCount,
                        healthStatus = "Healthy",
                        location = "Calf Pen",
                        tagNumber = tagNumber,
                        breed = calfSubType,
                        dob = date,
                        dateAdded = date,
                        weightAtBirth = birthWeight,
                        currentWeight = birthWeight,
                        sire = sireName,
                        dam = damName
                    )
                }

                val hasCalvedOrMilking = animalEvents.any { it.category.equals("CALVING", ignoreCase = true) } ||
                    currentStatus.contains("Milking", ignoreCase = true) ||
                    currentStatus.contains("Lactating", ignoreCase = true) ||
                    (cattleEval != null && (cattleEval.stage == CattleStage.MILKING || cattleEval.stage == CattleStage.INCALF_MILKING || cattleEval.lastCalvingDate != null))

                val (immediateStage, breedingDesc) = when (type.uppercase()) {
                    "PD" -> {
                        val isPos = title.contains("Positive", ignoreCase = true) || details.contains("Positive", ignoreCase = true) || metricValue.contains("Positive", ignoreCase = true) || metricValue.contains("In-Calf", ignoreCase = true)
                        if (isPos) {
                            (if (hasCalvedOrMilking) "In-Calf / Milking" else "In-Calf") to (if (hasCalvedOrMilking) "IN-CALF & MILKING" else "IN-CALF HEIFER")
                        } else {
                            (if (hasCalvedOrMilking) "Milking" else "Heifer") to (if (hasCalvedOrMilking) "OPEN (In Milk)" else "OPEN HEIFER")
                        }
                    }
                    "INSEMINATION" -> (if (hasCalvedOrMilking) "Milking" else "Inseminated") to "SERVED AI (Pending PD)"
                    "CALVING" -> "Milking" to "OPEN (In Milk)"
                    "DRY_OFF" -> "Dry" to (if (cattleEval?.isInCalf == true) "IN-CALF (Dry)" else "DRY COW (Open)")
                    "ABORTED" -> (if (hasCalvedOrMilking) "Milking" else "Heifer") to (if (hasCalvedOrMilking) "OPEN (In Milk)" else "OPEN HEIFER")
                    else -> null to null
                }
                if (immediateStage != null && breedingDesc != null) {
                    currentStatus = immediateStage
                    onUpdateAnimalStage(immediateStage, breedingDesc)
                }
                showAddCattleEventDialog = false
            }
        )
    }

    if (eventToEdit != null) {
        val ev = eventToEdit!!
        AddCattleEventDialog(
            animalName = animal.name,
            unitId = unitId,
            initialCategory = ev.category,
            isEditing = true,
            initialTitle = ev.title,
            initialDate = ev.date,
            initialDetails = ev.details,
            initialNotes = ev.notes,
            initialMetricValue = ev.metricValue,
            onDismiss = { eventToEdit = null },
            onRequestRecordExpense = { expCategory, expDesc, expDate ->
                pendingFinanceCategory = expCategory
                pendingFinanceDescription = expDesc
                pendingFinanceDate = expDate
                showRecordFinanceDialog = true
            },
            onSaveEvent = { type: String, title: String, date: String, details: String, notes: String, metricValue: String, reminderText: String, _ ->
                val evId = ev.id.toLongOrNull()
                if (evId != null) {
                    viewModel.updateCattleEvent(
                        eventId = evId,
                        unitId = unitId,
                        category = type,
                        title = title,
                        date = date,
                        details = details,
                        notes = notes,
                        metricValue = metricValue
                    )
                }
                val idx = animalEvents.indexOfFirst { it.id == ev.id }
                if (idx >= 0) {
                    animalEvents[idx] = CattleEventItem(
                        id = ev.id,
                        category = type,
                        title = title,
                        date = date,
                        details = details,
                        notes = notes,
                        metricValue = metricValue
                    )
                }
                val hasCalvedOrMilking = animalEvents.any { it.category.equals("CALVING", ignoreCase = true) } ||
                    currentStatus.contains("Milking", ignoreCase = true) ||
                    currentStatus.contains("Lactating", ignoreCase = true) ||
                    (cattleEval != null && (cattleEval.stage == CattleStage.MILKING || cattleEval.stage == CattleStage.INCALF_MILKING || cattleEval.lastCalvingDate != null))

                val (immediateStage, breedingDesc) = when (type.uppercase()) {
                    "PD" -> {
                        val isPos = title.contains("Positive", ignoreCase = true) || details.contains("Positive", ignoreCase = true) || metricValue.contains("Positive", ignoreCase = true) || metricValue.contains("In-Calf", ignoreCase = true)
                        if (isPos) {
                            (if (hasCalvedOrMilking) "In-Calf / Milking" else "In-Calf") to (if (hasCalvedOrMilking) "IN-CALF & MILKING" else "IN-CALF HEIFER")
                        } else {
                            (if (hasCalvedOrMilking) "Milking" else "Heifer") to (if (hasCalvedOrMilking) "OPEN (In Milk)" else "OPEN HEIFER")
                        }
                    }
                    "INSEMINATION" -> (if (hasCalvedOrMilking) "Milking" else "Inseminated") to "SERVED AI (Pending PD)"
                    "CALVING" -> "Milking" to "OPEN (In Milk)"
                    "DRY_OFF" -> "Dry" to (if (cattleEval?.isInCalf == true) "IN-CALF (Dry)" else "DRY COW (Open)")
                    "ABORTED" -> (if (hasCalvedOrMilking) "Milking" else "Heifer") to (if (hasCalvedOrMilking) "OPEN (In Milk)" else "OPEN HEIFER")
                    else -> null to null
                }
                if (immediateStage != null && breedingDesc != null) {
                    currentStatus = immediateStage
                    onUpdateAnimalStage(immediateStage, breedingDesc)
                }
                eventToEdit = null
            }
        )
    }

    if (showDeleteEventConfirmDialog && eventToDelete != null) {
        val ev = eventToDelete!!
        AlertDialog(
            onDismissRequest = {
                showDeleteEventConfirmDialog = false
                eventToDelete = null
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = null,
                        tint = Color(0xFFDC2626)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Delete Event Record?",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Are you sure you want to delete this event record?",
                        fontSize = 13.sp,
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFEF2F2),
                        border = BorderStroke(1.dp, Color(0xFFFECACA)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = ev.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B)
                            )
                            Text(
                                text = "Date: ${ev.date}  •  Category: ${ev.category}",
                                fontSize = 11.sp,
                                color = Color(0xFFB91C1C)
                            )
                            if (ev.details.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = ev.details,
                                    fontSize = 11.sp,
                                    color = Color(0xFF7F1D1D)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This action will update the dynamic gestation and lifecycle summary.",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val evId = ev.id.toLongOrNull()
                        if (evId != null) {
                            viewModel.deleteCattleEvent(evId)
                        }
                        animalEvents.removeAll { it.id == ev.id }
                        showDeleteEventConfirmDialog = false
                        eventToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showDeleteEventConfirmDialog = false
                        eventToDelete = null
                    }
                ) {
                    Text("Cancel", color = Color(0xFF475569))
                }
            }
        )
    }

    if (showRecordFinanceDialog) {
        AddFinanceRecordDialog(
            initialType = FinanceType.EXPENSE,
            initialCategory = pendingFinanceCategory,
            initialDescription = pendingFinanceDescription,
            initialDate = pendingFinanceDate,
            initialTargetUnit = animal.name.ifBlank { "Cattle" },
            userRole = userRole,
            canEditPastDaysLogs = true,
            onDismiss = { showRecordFinanceDialog = false },
            onSaveRecordFull = { type, category, amount, description, date, targetUnit ->
                viewModel.addFinanceRecord(type, category, amount, description, date, targetUnit)
                showRecordFinanceDialog = false
            },
            onSaveRecordWithDate = { type, category, amount, description, date ->
                viewModel.addFinanceRecord(type, category, amount, description, date, animal.name.ifBlank { "Cattle" })
                showRecordFinanceDialog = false
            },
            onSaveRecord = { type, category, amount, description ->
                viewModel.addFinanceRecord(type, category, amount, description, pendingFinanceDate, animal.name.ifBlank { "Cattle" })
                showRecordFinanceDialog = false
            }
        )
    }
}


@Composable
fun HealthLogItem(title: String, date: String, description: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFFF8FAFC),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                Text(date, fontSize = 11.sp, color = Color(0xFF64748B))
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(description, fontSize = 12.sp, color = Color(0xFF475569))
        }
    }
}

data class PoultryFeedLogItem(
    val id: String,
    val recordId: Long,
    val date: String,
    val feedType: String,
    val quantityKg: Double,
    val costAmount: Double,
    val notes: String = ""
)

data class PoultryMortalityLogItem(
    val id: String,
    val recordId: Long,
    val linkedLogSyncId: String = "",
    val date: String,
    val count: Int,
    val cause: String,
    val notes: String = ""
)

data class PoultryEggSaleItem(
    val id: String,
    val recordId: Long,
    val date: String,
    val traysSold: Int,
    val pricePerTray: Double,
    val totalRevenue: Double,
    val buyer: String = ""
)

private sealed class PoultryLogAction {
    data class Feed(val item: PoultryFeedLogItem) : PoultryLogAction()
    data class Mortality(val item: PoultryMortalityLogItem) : PoultryLogAction()
    data class EggSale(val item: PoultryEggSaleItem) : PoultryLogAction()
    data class Disposal(val item: FlockDisposalLogItem) : PoultryLogAction()
}

private fun PoultryFeedLogItem.toPoultryLog(unitId: Long) = PoultryLog(
    id = recordId,
    syncId = id,
    unitId = unitId,
    logType = "FEED",
    date = date,
    feedType = feedType,
    quantityKg = quantityKg,
    costAmount = costAmount,
    notes = notes
)

private fun PoultryMortalityLogItem.toPoultryLog(unitId: Long) = PoultryLog(
    id = recordId,
    syncId = id,
    unitId = unitId,
    logType = "MORTALITY",
    date = date,
    birdCount = count,
    cause = cause,
    notes = notes,
    linkedLogSyncId = linkedLogSyncId
)

private fun PoultryEggSaleItem.toPoultryLog(unitId: Long) = PoultryLog(
    id = recordId,
    syncId = id,
    unitId = unitId,
    logType = "EGG_SALE",
    date = date,
    traysSold = traysSold,
    pricePerTray = pricePerTray,
    totalRevenue = totalRevenue,
    buyer = buyer
)

private fun FlockDisposalLogItem.toPoultryLog(unitId: Long) = PoultryLog(
    id = recordId,
    syncId = id,
    unitId = unitId,
    logType = "DISPOSAL",
    date = date,
    birdCount = quantity,
    disposalReason = reason,
    disposalAmount = amount,
    notes = notes,
    linkedLogSyncId = if (reason.equals("Death", ignoreCase = true)) linkedMortalityLogId.orEmpty() else ""
)


data class PoultryVaccineItem(
    val id: String,
    val vaccineName: String,
    val targetStage: String,
    val dueDate: String,
    val status: String, // "COMPLETED", "DUE_SOON", "UPCOMING"
    val notes: String = ""
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FlockDetailsView(
    flock: AnimalDetailData,
    userRole: String = "OWNER",
    eggLogs: List<EggLog>,
    financeRecords: List<FinanceRecord>,
    poultryLogs: List<PoultryLog>,
    // Persisted rule ids loaded from reminder_completions for this flock.
    completedVaccineRuleIds: Set<String> = emptySet(),
    onMarkVaccinationComplete: (String) -> Unit = {},
    onClearVaccinationComplete: (String) -> Unit = {},
    onAddPoultryLog: (PoultryLog) -> Unit = {},
    onUpdatePoultryLog: (PoultryLog) -> Unit = {},
    onDeletePoultryLog: (Long) -> Unit = {},
    onUpdateFlockHeadCount: (Int) -> Unit = {},
    onBackClick: () -> Unit,
    onAddEggLogClick: () -> Unit,
    onAddFinanceClick: () -> Unit,
    onDisposeFlock: (quantity: Int, reason: String, amount: Double, notes: String, date: String) -> Unit = { _, _, _, _, _ -> },
    onEditFlock: () -> Unit = {},
    onDeleteFlock: () -> Unit = {},
    onUpdatePhoto: (String?) -> Unit = {},
    canEditLivestock: Boolean = true,
    modifier: Modifier = Modifier
) {
    val canEdit = userRole.equals("OWNER", ignoreCase = true) || canEditLivestock
    val unitId = remember(flock.id) { flock.id.removePrefix("unit_").toLongOrNull() ?: 0L }
    var showFeedDialog by remember { mutableStateOf(false) }
    var showMortalityDialog by remember { mutableStateOf(false) }
    var showEggSaleDialog by remember { mutableStateOf(false) }
    var showVaccineDialog by remember { mutableStateOf(false) }
    var showDisposeFlockDialog by remember { mutableStateOf(false) }
    var showEditDateAddedDialog by remember { mutableStateOf(false) }
    var poultryLogForOptions by remember { mutableStateOf<PoultryLogAction?>(null) }
    var poultryLogToEdit by remember { mutableStateOf<PoultryLogAction?>(null) }
    var poultryLogToDelete by remember { mutableStateOf<PoultryLogAction?>(null) }

    var flockDateAdded by remember(flock.id, flock.dateOfBirth) {
        mutableStateOf(if (flock.dateOfBirth.isNotBlank()) flock.dateOfBirth else "01 Jul 2026")
    }

    val initialHeadCount = remember(flock.tagNumber, flock.headCountInt) {
        val digits = flock.tagNumber.filter { it.isDigit() }
        if (flock.headCountInt > 1) flock.headCountInt else digits.toIntOrNull() ?: 450
    }
    var liveHeadCount by remember(flock.id) { mutableIntStateOf(initialHeadCount) }
    LaunchedEffect(flock.headCountInt) { liveHeadCount = flock.headCountInt.coerceAtLeast(0) }

    // Dynamic Flock Age Calculation based on Date Added
    val flockAgeInfo = remember(flockDateAdded) {
        PoultryAgeAndVaccinationUtils.calculateFlockAge(flockDateAdded)
    }

    // Vaccine completion state is supplied by the parent from the persisted
    // reminder_completions table. It is not kept in local compose memory.

    val dismissedVaccineRuleIds = remember(flock.id) {
        mutableStateListOf<String>()
    }

    // Dynamic calculated vaccination schedule
    val calculatedVaccineSchedule = remember(flockDateAdded, completedVaccineRuleIds, dismissedVaccineRuleIds.toList(), flock.status, flock.disposalReason, liveHeadCount) {
        if (flock.status.contains("DISPOSED", ignoreCase = true) || flock.disposalReason.isNotBlank() || liveHeadCount <= 0) {
            emptyList()
        } else {
            PoultryAgeAndVaccinationUtils.calculateVaccinationSchedule(flockDateAdded, completedVaccineRuleIds)
                .filter { !dismissedVaccineRuleIds.contains(it.ruleId) }
        }
    }

    val overdueVaccineCount = remember(calculatedVaccineSchedule, flock.status, flock.disposalReason, liveHeadCount) {
        if (flock.status.contains("DISPOSED", ignoreCase = true) || flock.disposalReason.isNotBlank() || liveHeadCount <= 0) 0
        else calculatedVaccineSchedule.count { it.status == VaccineDueStatus.OVERDUE }
    }
    val dueTodayVaccineCount = remember(calculatedVaccineSchedule, flock.status, flock.disposalReason, liveHeadCount) {
        if (flock.status.contains("DISPOSED", ignoreCase = true) || flock.disposalReason.isNotBlank() || liveHeadCount <= 0) 0
        else calculatedVaccineSchedule.count { it.status == VaccineDueStatus.DUE_TODAY }
    }
    val dueSoonVaccineCount = remember(calculatedVaccineSchedule, flock.status, flock.disposalReason, liveHeadCount) {
        if (flock.status.contains("DISPOSED", ignoreCase = true) || flock.disposalReason.isNotBlank() || liveHeadCount <= 0) 0
        else calculatedVaccineSchedule.count { it.status == VaccineDueStatus.DUE_SOON }
    }

    val customVaccines = remember(flock.id) {
        mutableStateListOf<PoultryVaccineItem>()
    }

    val flockDisposalLogs = poultryLogs
        .filter { it.logType == "DISPOSAL" }
        .map {
            FlockDisposalLogItem(
                id = it.syncId,
                recordId = it.id,
                flockName = flock.name,
                quantity = it.birdCount,
                reason = it.disposalReason,
                amount = it.disposalAmount,
                date = it.date,
                notes = it.notes,
                linkedMortalityLogId = it.linkedLogSyncId.ifBlank { null }
            )
        }

    var selectedStage by remember(flockAgeInfo.feedStage.stageName) {
        mutableStateOf(flockAgeInfo.feedStage.stageName)
    }

    val feedLogs = poultryLogs
        .filter { it.logType == "FEED" }
        .map {
            PoultryFeedLogItem(it.syncId, it.id, it.date, it.feedType, it.quantityKg, it.costAmount, it.notes)
        }

    val mortalityLogs = poultryLogs
        .filter { it.logType == "MORTALITY" }
        .map {
            PoultryMortalityLogItem(it.syncId, it.id, it.linkedLogSyncId, it.date, it.birdCount, it.cause, it.notes)
        }

    val eggSaleLogs = poultryLogs
        .filter { it.logType == "EGG_SALE" }
        .map {
            PoultryEggSaleItem(it.syncId, it.id, it.date, it.traysSold, it.pricePerTray, it.totalRevenue, it.buyer)
        }

    val totalMortalityCount = mortalityLogs.sumOf { it.count }
    val mortalityPercentage = remember(liveHeadCount, totalMortalityCount) {
        val totalBorn = liveHeadCount + totalMortalityCount
        if (totalBorn > 0) String.format("%.1f%%", (totalMortalityCount.toDouble() / totalBorn) * 100) else "0.0%"
    }

    val nowMillis = System.currentTimeMillis()
    val sevenDaysAgoMillis = nowMillis - (7L * 24L * 60L * 60L * 1000L)

    val mortalityLast7Days = remember(mortalityLogs) {
        mortalityLogs.filter { log ->
            val parsed = PoultryAgeAndVaccinationUtils.parseDate(log.date)
            parsed == null || parsed.time >= sevenDaysAgoMillis
        }.sumOf { it.count }
    }

    val avgDailyEggTraysLast7Days = remember(eggSaleLogs) {
        val totalTrays = eggSaleLogs.filter { log ->
            val parsed = PoultryAgeAndVaccinationUtils.parseDate(log.date)
            parsed == null || parsed.time >= sevenDaysAgoMillis
        }.sumOf { it.traysSold }
        (totalTrays.toDouble() / 7.0)
    }

    val automatedStatus = remember(
        flockAgeInfo,
        liveHeadCount,
        mortalityLast7Days,
        totalMortalityCount,
        avgDailyEggTraysLast7Days,
        overdueVaccineCount,
        dueTodayVaccineCount
    ) {
        PoultryAgeAndVaccinationUtils.evaluateAutomatedFlockStatus(
            ageInfo = flockAgeInfo,
            activeHeadCount = liveHeadCount,
            mortalityCountLast7Days = mortalityLast7Days,
            totalMortalityCount = totalMortalityCount,
            avgDailyEggTraysLast7Days = avgDailyEggTraysLast7Days,
            overdueVaccineCount = overdueVaccineCount,
            dueTodayVaccineCount = dueTodayVaccineCount
        )
    }

    if (showDisposeFlockDialog) {
        DisposeFlockDialog(
            flockName = flock.name,
            currentHeadCount = liveHeadCount,
            onDismiss = { showDisposeFlockDialog = false },
            onConfirmDisposeFlock = { quantity, reason, amount, notes, date ->
                if (unitId > 0) {
                    val disposalSyncId = java.util.UUID.randomUUID().toString()
                    val mortalitySyncId = if (reason.equals("Death", ignoreCase = true)) java.util.UUID.randomUUID().toString() else ""
                    if (mortalitySyncId.isNotBlank()) {
                        onAddPoultryLog(
                            PoultryLog(
                                syncId = mortalitySyncId,
                                unitId = unitId,
                                logType = "MORTALITY",
                                date = date,
                                birdCount = quantity,
                                cause = notes.ifBlank { "Mortality" },
                                notes = "Created from flock disposal",
                                linkedLogSyncId = disposalSyncId
                            )
                        )
                    }
                    onAddPoultryLog(
                        PoultryLog(
                            syncId = disposalSyncId,
                            unitId = unitId,
                            logType = "DISPOSAL",
                            date = date,
                            birdCount = quantity,
                            disposalReason = reason,
                            disposalAmount = amount,
                            notes = notes.ifBlank { "$reason disposal" },
                            linkedLogSyncId = mortalitySyncId
                        )
                    )
                }
                liveHeadCount = (liveHeadCount - quantity).coerceAtLeast(0)
                onUpdateFlockHeadCount(liveHeadCount)
                showDisposeFlockDialog = false
                onDisposeFlock(quantity, reason, amount, notes, date)
            }
        )
    }

    // Dialog for changing Date Added using AppDatePicker
    if (showEditDateAddedDialog) {
        var tempDate by remember { mutableStateOf(flockDateAdded) }
        Dialog(onDismissRequest = { showEditDateAddedDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Edit Flock Date Added", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Text("The flock age, feed stage recommendations, and vaccination due dates will recalculate automatically.", fontSize = 12.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(16.dp))
                    AppDatePickerField(
                        value = tempDate,
                        onValueChange = { tempDate = it },
                        label = "Date Added (Arrival on Farm)",
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "edit_flock_date_added_picker"
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = { showEditDateAddedDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                flockDateAdded = tempDate
                                showEditDateAddedDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                        ) {
                            Text("Update Date")
                        }
                    }
                }
            }
        }
    }

    if (poultryLogForOptions != null) {
        val selectedLog = poultryLogForOptions!!
        PoultryLogOptionsDialog(
            log = selectedLog,
            onEdit = {
                poultryLogToEdit = selectedLog
                poultryLogForOptions = null
            },
            onDelete = {
                poultryLogToDelete = selectedLog
                poultryLogForOptions = null
            },
            onDismiss = { poultryLogForOptions = null }
        )
    }

    if (poultryLogToDelete != null) {
        val selectedLog = poultryLogToDelete!!
        PoultryLogDeleteConfirmDialog(
            onDismiss = { poultryLogToDelete = null },
            onConfirmDelete = {
                when (selectedLog) {
                    is PoultryLogAction.Feed -> onDeletePoultryLog(selectedLog.item.recordId)
                    is PoultryLogAction.Mortality -> {
                        onDeletePoultryLog(selectedLog.item.recordId)
                        liveHeadCount += selectedLog.item.count
                        onUpdateFlockHeadCount(liveHeadCount)
                    }
                    is PoultryLogAction.EggSale -> onDeletePoultryLog(selectedLog.item.recordId)
                    is PoultryLogAction.Disposal -> {
                        onDeletePoultryLog(selectedLog.item.recordId)
                        liveHeadCount += selectedLog.item.quantity
                        onUpdateFlockHeadCount(liveHeadCount)
                    }
                }
                poultryLogToDelete = null
            }
        )
    }

    when (val selectedLog = poultryLogToEdit) {
        is PoultryLogAction.Feed -> {
            EditFeedLogDialog(
                log = selectedLog.item,
                onDismiss = { poultryLogToEdit = null },
                onSave = { updatedLog ->
                    onUpdatePoultryLog(updatedLog.toPoultryLog(unitId))
                    poultryLogToEdit = null
                }
            )
        }
        is PoultryLogAction.Mortality -> {
            EditMortalityLogDialog(
                log = selectedLog.item,
                onDismiss = { poultryLogToEdit = null },
                onSave = { updatedLog ->
                    val countDifference = updatedLog.count - selectedLog.item.count
                    liveHeadCount = (liveHeadCount - countDifference).coerceAtLeast(0)
                    onUpdateFlockHeadCount(liveHeadCount)
                    onUpdatePoultryLog(updatedLog.toPoultryLog(unitId))
                    poultryLogToEdit = null
                }
            )
        }
        is PoultryLogAction.EggSale -> {
            EditEggSaleLogDialog(
                log = selectedLog.item,
                onDismiss = { poultryLogToEdit = null },
                onSave = { updatedLog ->
                    onUpdatePoultryLog(updatedLog.toPoultryLog(unitId))
                    poultryLogToEdit = null
                }
            )
        }
        is PoultryLogAction.Disposal -> {
            EditDisposalLogDialog(
                log = selectedLog.item,
                onDismiss = { poultryLogToEdit = null },
                onSave = { updatedLog ->
                    val quantityDifference = updatedLog.quantity - selectedLog.item.quantity
                    liveHeadCount = (liveHeadCount - quantityDifference).coerceAtLeast(0)
                    onUpdateFlockHeadCount(liveHeadCount)
                    onUpdatePoultryLog(updatedLog.toPoultryLog(unitId))
                    poultryLogToEdit = null
                }
            )
        }
        null -> Unit
    }

    Box(modifier = modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Bar
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White)
                                .testTag("flock_detail_back_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1E293B))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = flock.name,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = " Poultry Flock  ${flock.breed}",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                if (canEdit) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Edit Flock Button
                        Surface(
                            onClick = onEditFlock,
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFECFDF5),
                            border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("edit_flock_topbar_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Edit Flock",
                                    tint = ForestGreenPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Edit",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreenPrimary
                                )
                            }
                        }

                        // Dispose Flock Button
                        Surface(
                            onClick = { showDisposeFlockDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFFFBEB),
                            border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("dispose_flock_topbar_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.RemoveCircleOutline,
                                    contentDescription = "Dispose Flock",
                                    tint = Color(0xFFB45309),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Dispose",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }

                        // Delete Flock Button
                        Surface(
                            onClick = onDeleteFlock,
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEF2F2),
                            border = BorderStroke(1.dp, Color(0xFFFECACA)),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("delete_flock_topbar_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DeleteForever,
                                    contentDescription = "Delete Flock",
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Delete",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDC2626)
                                )
                            }
                        }
                    }
                }
                }
            }

            // Flock Photo Header / Avatar Card
            item {
                val context = androidx.compose.ui.platform.LocalContext.current
                var showPhotoSourceDialog by remember { mutableStateOf(false) }
                var showCameraCaptureDialog by remember { mutableStateOf(false) }
                val photoGalleryLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    if (uri != null) {
                        val saved = ImageStorageUtils.saveImageToInternalStorage(context, uri) ?: uri.toString()
                        onUpdatePhoto(saved)
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Flock Photo Avatar / Fallback Icon
                            Box(
                                modifier = Modifier
                                    .size(88.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFFEF3C7))
                                    .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(14.dp))
                                    .clickable { showPhotoSourceDialog = true }
                                    .testTag("flock_photo_avatar_box"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!flock.photoUri.isNullOrBlank()) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(ImageStorageUtils.resolveImageModel(flock.photoUri))
                                            .crossfade(true)
                                            .memoryCachePolicy(CachePolicy.ENABLED)
                                            .diskCachePolicy(CachePolicy.ENABLED)
                                            .build(),
                                        contentDescription = "${flock.name} Photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Egg,
                                            contentDescription = "Generic Poultry Icon",
                                            tint = Color(0xFFD97706),
                                            modifier = Modifier.size(40.dp)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("No Photo", fontSize = 9.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }

                            // Info & Photo Upload Trigger
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = flock.name,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Tag: ${flock.tagNumber}  •  ${flock.breed}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = { showPhotoSourceDialog = true },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier
                                            .height(34.dp)
                                            .testTag("upload_flock_photo_button")
                                    ) {
                                        Icon(
                                            imageVector = if (flock.photoUri.isNullOrBlank()) Icons.Filled.AddPhotoAlternate else Icons.Filled.CameraAlt,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (flock.photoUri.isNullOrBlank()) "Add Photo" else "Change",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    if (!flock.photoUri.isNullOrBlank()) {
                                        OutlinedButton(
                                            onClick = { onUpdatePhoto(null) },
                                            shape = RoundedCornerShape(10.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                            modifier = Modifier
                                                .height(34.dp)
                                                .testTag("remove_flock_photo_button")
                                        ) {
                                            Text("Remove", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (flock.notes.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFFFBEB),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Notes / Origin", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(flock.notes, fontSize = 13.sp, color = Color(0xFF334155))
                        }
                    }
                }

                if (showPhotoSourceDialog) {
                    AlertDialog(
                        onDismissRequest = { showPhotoSourceDialog = false },
                        title = { Text("Update Flock Photo", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "Choose a photo from your camera or gallery to identify ${flock.name}.",
                                    fontSize = 13.sp,
                                    color = Color(0xFF475569)
                                )
                                if (!flock.photoUri.isNullOrBlank()) {
                                    OutlinedButton(
                                        onClick = {
                                            showPhotoSourceDialog = false
                                            onUpdatePhoto(null)
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, Color(0xFFFECACA)),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                                        modifier = Modifier.fillMaxWidth().testTag("dialog_remove_flock_photo_button")
                                    ) {
                                        Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFDC2626))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Remove Current Photo", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFDC2626))
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showPhotoSourceDialog = false
                                    showCameraCaptureDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Use Camera")
                            }
                        },
                        dismissButton = {
                            OutlinedButton(
                                onClick = {
                                    showPhotoSourceDialog = false
                                    photoGalleryLauncher.launch("image/*")
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("From Gallery")
                            }
                        }
                    )
                }

                if (showCameraCaptureDialog) {
                    CameraCaptureDialog(
                        onDismiss = { showCameraCaptureDialog = false },
                        onPhotoCaptured = { uri ->
                            showCameraCaptureDialog = false
                            val saved = ImageStorageUtils.saveImageToInternalStorage(context, uri) ?: uri.toString()
                            onUpdatePhoto(saved)
                        }
                    )
                }
            }

            // 0. Prominent Vaccination & Feed Transition Alert Banners
            if (overdueVaccineCount > 0 || dueTodayVaccineCount > 0 || dueSoonVaccineCount > 0) {
                item {
                    val isUrgent = overdueVaccineCount > 0 || dueTodayVaccineCount > 0
                    val bannerBg = if (isUrgent) Color(0xFFFEF2F2) else Color(0xFFFFFBEB)
                    val bannerBorder = if (isUrgent) Color(0xFFFECACA) else Color(0xFFFDE68A)
                    val bannerText = if (isUrgent) Color(0xFF991B1B) else Color(0xFF92400E)

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = bannerBg,
                        border = BorderStroke(1.dp, bannerBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MedicalServices,
                                contentDescription = null,
                                tint = bannerText,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isUrgent) "VACCINATION ATTENTION REQUIRED" else " ï¸ UPCOMING VACCINATIONS",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = bannerText
                                )
                                Text(
                                    text = buildString {
                                        if (overdueVaccineCount > 0) append("$overdueVaccineCount overdue vaccine(s). ")
                                        if (dueTodayVaccineCount > 0) append("$dueTodayVaccineCount vaccine due today! ")
                                        if (dueSoonVaccineCount > 0) append("$dueSoonVaccineCount vaccine due within 2 days.")
                                    },
                                    fontSize = 12.sp,
                                    color = bannerText
                                )
                            }
                        }
                    }
                }
            }

            if (flockAgeInfo.feedStage.hasTransitionAlert && flockAgeInfo.feedStage.transitionAlertMessage != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFEFF6FF),
                        border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = null,
                                tint = Color(0xFF1E40AF),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "FEED STAGE TRANSITION NOTIFICATION",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E40AF)
                                )
                                Text(
                                    text = flockAgeInfo.feedStage.transitionAlertMessage!!,
                                    fontSize = 12.sp,
                                    color = Color(0xFF1E3A8A),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // 1. Flock Header Overview Card (Date Added & Dynamic Age Tracking)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "LIVE BIRD COUNT",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B)
                                )
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "$liveHeadCount",
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ForestGreenPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Birds active",
                                        fontSize = 13.sp,
                                        color = Color(0xFF64748B),
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                }
                            }

                            // Dynamic Calculated Age Badge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFEFF6FF),
                                border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text("CURRENT FLOCK AGE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E40AF))
                                    Text(
                                        text = flockAgeInfo.formattedAge,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1D4ED8)
                                    )
                                    Text(
                                        text = "Calculated Daily",
                                        fontSize = 10.sp,
                                        color = Color(0xFF60A5FA)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Date Added Info Box with Calendar Picker trigger
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth().clickable { showEditDateAddedDialog = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.DateRange,
                                        contentDescription = null,
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("DATE ADDED / ARRIVAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                        Text(flockAgeInfo.dateAddedFormatted, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFE2E8F0)
                                ) {
                                    Text(
                                        text = "Change Date ▾",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF334155)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Stage Pill Chip Selector (Starter Week 1-8, Grower Week 9-18, Layer/Finisher 18+ Wks)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf(
                                "Starter (Wk 1-8)",
                                "Grower (Wk 9-18)",
                                "Layer/Finisher (18+ Wks)"
                            ).forEach { stage ->
                                val isSelected = selectedStage.contains(stage.take(6), ignoreCase = true)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) ForestGreenPrimary else Color.Transparent,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedStage = stage }
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = stage.split(" ").first(),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else Color(0xFF475569)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats Grid Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF0FDF4),
                                border = BorderStroke(1.dp, Color(0xFFBBF7D0))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("FEED STAGE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                                    Text(flockAgeInfo.feedStage.stageName.split(" (").first(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                                    Text(flockAgeInfo.shortAgeLabel, fontSize = 11.sp, color = Color(0xFF166534))
                                }
                            }

                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFEF2F2),
                                border = BorderStroke(1.dp, Color(0xFFFECACA))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("MORTALITY RATE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                                    Text("$totalMortalityCount lost", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                                    Text("Rate: $mortalityPercentage", fontSize = 11.sp, color = Color(0xFF991B1B))
                                }
                            }
                        }
                    }
                }
            }

            // 1b. Automated Health & Production Status Diagnostic Engine Card
            item {
                AutomatedPoultryStatusCard(status = automatedStatus)
            }

            // 2. Stage-Based Feeding Guide & Logs Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("Feed Stage Formulation", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))

                        Spacer(modifier = Modifier.height(12.dp))

                        val stageFeedInfo = when {
                            selectedStage.contains("Starter", ignoreCase = true) -> PoultryAgeAndVaccinationUtils.getFlockFeedStage(28)
                            selectedStage.contains("Grower", ignoreCase = true) -> PoultryAgeAndVaccinationUtils.getFlockFeedStage(84)
                            else -> PoultryAgeAndVaccinationUtils.getFlockFeedStage(140)
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFFBEB),
                            border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("RECOMMENDED FEED FOR ${selectedStage.uppercase()}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(stageFeedInfo.feedType, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF78350F))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("• Purpose: ${stageFeedInfo.purpose}", fontSize = 12.sp, color = Color(0xFF92400E))
                                Text("• Daily Ration: ${stageFeedInfo.dailyRationPerBird}", fontSize = 12.sp, color = Color(0xFF92400E))
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text("Recent Feed Log History:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                        Spacer(modifier = Modifier.height(8.dp))

                        feedLogs.forEach { feed ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp)
                                    .combinedClickable(
                                        enabled = canEdit,
                                        onClick = {},
                                        onLongClick = {
                                            poultryLogForOptions = PoultryLogAction.Feed(feed)
                                        }
                                    )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(feed.feedType, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                                        Text("Date: ${feed.date} • ${feed.notes}", fontSize = 12.sp, color = Color(0xFF64748B))
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("${feed.quantityKg} kg", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ForestGreenPrimary)
                                        Text("\${feed.costAmount}", fontSize = 12.sp, color = Color(0xFF64748B))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Complete Standard Vaccination Schedule & Alerts Card
            if (!flock.status.contains("DISPOSED", ignoreCase = true) && flock.disposalReason.isBlank() && liveHeadCount > 0) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Age Vaccination Schedule", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                Text("Standard protocol keyed from arrival date", fontSize = 12.sp, color = Color(0xFF64748B))
                            }
                            Button(
                                onClick = { showVaccineDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("+ VACCINE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (calculatedVaccineSchedule.isEmpty() && customVaccines.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF8FAFC),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = "All poultry vaccination tasks completed or cleared!",
                                    fontSize = 13.sp,
                                    color = Color(0xFF64748B),
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }

                        calculatedVaccineSchedule.forEach { vac ->
                            val (bgColor, textColor) = when (vac.status) {
                                VaccineDueStatus.COMPLETED -> Color(0xFFDCFCE7) to Color(0xFF15803D)
                                VaccineDueStatus.OVERDUE -> Color(0xFFFEE2E2) to Color(0xFF991B1B)
                                VaccineDueStatus.DUE_TODAY -> Color(0xFFFEF3C7) to Color(0xFFB45309)
                                VaccineDueStatus.DUE_SOON -> Color(0xFFFFFBEB) to Color(0xFFD97706)
                                VaccineDueStatus.UPCOMING -> Color(0xFFF1F5F9) to Color(0xFF475569)
                            }

                            var vacMenuExpanded by remember { mutableStateOf(false) }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (vac.isCompleted) Color(0xFFF8FAFC) else if (vac.status == VaccineDueStatus.OVERDUE) Color(0xFFFFF1F2) else Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, if (vac.status == VaccineDueStatus.OVERDUE) Color(0xFFFECDD3) else Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = vac.vaccineName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = if (vac.isCompleted) Color(0xFF64748B) else Color(0xFF1E293B)
                                            )
                                        }
                                        Text(
                                            text = "Stage: ${vac.targetStageLabel} • Due: ${vac.scheduledDueDateStr}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF475569)
                                        )
                                        Text(
                                            text = "Method: ${vac.administrationMethod} • ${vac.notes}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = bgColor,
                                            modifier = Modifier.clickable(enabled = canEdit) {
                                                if (vac.isCompleted) {
                                                    onClearVaccinationComplete(vac.ruleId)
                                                } else {
                                                    onMarkVaccinationComplete(vac.ruleId)
                                                }
                                            }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (vac.isCompleted) {
                                                    Icon(
                                                        imageVector = Icons.Filled.CheckCircle,
                                                        contentDescription = null,
                                                        tint = textColor,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                }
                                                Text(
                                                    text = vac.statusLabel,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = textColor
                                                )
                                            }
                                        }

                                        Box {
                                            IconButton(
                                                onClick = { vacMenuExpanded = true },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.MoreVert,
                                                    contentDescription = "Vaccine task options",
                                                    tint = Color(0xFF64748B),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            DropdownMenu(
                                                expanded = vacMenuExpanded,
                                                onDismissRequest = { vacMenuExpanded = false },
                                                modifier = Modifier.background(Color.White)
                                            ) {
                                                if (!vac.isCompleted) {
                                                    DropdownMenuItem(
                                                        text = { Text("Complete Vaccination", fontWeight = FontWeight.Bold, color = Color(0xFF15803D)) },
                                                        onClick = {
                                                            vacMenuExpanded = false
                                                            onMarkVaccinationComplete(vac.ruleId)
                                                        },
                                                        leadingIcon = {
                                                            Icon(
                                                                imageVector = Icons.Filled.CheckCircle,
                                                                contentDescription = null,
                                                                tint = Color(0xFF15803D),
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                        }
                                                    )
                                                } else {
                                                    DropdownMenuItem(
                                                        text = { Text("Mark Pending", fontWeight = FontWeight.SemiBold, color = Color(0xFF475569)) },
                                                        onClick = {
                                                            vacMenuExpanded = false
                                                            onClearVaccinationComplete(vac.ruleId)
                                                        },
                                                        leadingIcon = {
                                                            Icon(
                                                                imageVector = Icons.Filled.CheckCircle,
                                                                contentDescription = null,
                                                                tint = Color(0xFF64748B),
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                        }
                                                    )
                                                }

                                                DropdownMenuItem(
                                                    text = { Text("Delete / Dismiss Task", fontWeight = FontWeight.Bold, color = Color(0xFFDC2626)) },
                                                    onClick = {
                                                        vacMenuExpanded = false
                                                        dismissedVaccineRuleIds.add(vac.ruleId)
                                                    },
                                                    leadingIcon = {
                                                        Icon(
                                                            imageVector = Icons.Filled.Delete,
                                                            contentDescription = null,
                                                            tint = Color(0xFFDC2626),
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Custom added vaccines
                        customVaccines.forEach { customVac ->
                            var customMenuExpanded by remember { mutableStateOf(false) }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                        Text(customVac.vaccineName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                                        Text("Target: ${customVac.targetStage} • Due: ${customVac.dueDate}", fontSize = 12.sp, color = Color(0xFF64748B))
                                        if (customVac.notes.isNotBlank()) {
                                            Text(customVac.notes, fontSize = 11.sp, color = Color(0xFF64748B))
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (customVac.status == "COMPLETED") Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                                            modifier = Modifier.clickable {
                                                val newStatus = if (customVac.status == "COMPLETED") "UPCOMING" else "COMPLETED"
                                                val index = customVaccines.indexOf(customVac)
                                                if (index >= 0) {
                                                    customVaccines[index] = customVac.copy(status = newStatus)
                                                }
                                            }
                                        ) {
                                            Text(
                                                text = customVac.status,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (customVac.status == "COMPLETED") Color(0xFF15803D) else Color(0xFFB45309)
                                            )
                                        }

                                        Box {
                                            IconButton(
                                                onClick = { customMenuExpanded = true },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.MoreVert,
                                                    contentDescription = "Custom vaccine options",
                                                    tint = Color(0xFF64748B),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            DropdownMenu(
                                                expanded = customMenuExpanded,
                                                onDismissRequest = { customMenuExpanded = false },
                                                modifier = Modifier.background(Color.White)
                                            ) {
                                                if (customVac.status != "COMPLETED") {
                                                    DropdownMenuItem(
                                                        text = { Text("Complete Vaccination", fontWeight = FontWeight.Bold, color = Color(0xFF15803D)) },
                                                        onClick = {
                                                            customMenuExpanded = false
                                                            val index = customVaccines.indexOf(customVac)
                                                            if (index >= 0) {
                                                                customVaccines[index] = customVac.copy(status = "COMPLETED")
                                                            }
                                                        },
                                                        leadingIcon = {
                                                            Icon(
                                                                imageVector = Icons.Filled.CheckCircle,
                                                                contentDescription = null,
                                                                tint = Color(0xFF15803D),
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                        }
                                                    )
                                                } else {
                                                    DropdownMenuItem(
                                                        text = { Text("Mark Pending", fontWeight = FontWeight.SemiBold, color = Color(0xFF475569)) },
                                                        onClick = {
                                                            customMenuExpanded = false
                                                            val index = customVaccines.indexOf(customVac)
                                                            if (index >= 0) {
                                                                customVaccines[index] = customVac.copy(status = "UPCOMING")
                                                            }
                                                        },
                                                        leadingIcon = {
                                                            Icon(
                                                                imageVector = Icons.Filled.CheckCircle,
                                                                contentDescription = null,
                                                                tint = Color(0xFF64748B),
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                        }
                                                    )
                                                }

                                                DropdownMenuItem(
                                                    text = { Text("Delete Vaccine", fontWeight = FontWeight.Bold, color = Color(0xFFDC2626)) },
                                                    onClick = {
                                                        customMenuExpanded = false
                                                        customVaccines.remove(customVac)
                                                    },
                                                    leadingIcon = {
                                                        Icon(
                                                            imageVector = Icons.Filled.Delete,
                                                            contentDescription = null,
                                                            tint = Color(0xFFDC2626),
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
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
        }

            // 4. Mortality & Health Log Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(" Mortality & Health Log", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            Button(
                                onClick = { showMortalityDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("+ LOG DEATH", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        mortalityLogs.forEach { log ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFEF2F2),
                                border = BorderStroke(1.dp, Color(0xFFFECACA)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp)
                                    .combinedClickable(
                                        enabled = canEdit,
                                        onClick = {},
                                        onLongClick = {
                                            poultryLogForOptions = PoultryLogAction.Mortality(log)
                                        }
                                    )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("${log.count} Birds Lost • Cause: ${log.cause}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF991B1B))
                                        Text("Date: ${log.date} • ${log.notes}", fontSize = 12.sp, color = Color(0xFFB91C1C))
                                    }
                                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFFCA5A5)) {
                                        Text("-${log.count}", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7F1D1D))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. Flock Sales & Disposals History Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFFED7AA))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(" Flock Sales & Disposals Log", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            Button(
                                onClick = { showDisposeFlockDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("+ DISPOSE / SELL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (flockDisposalLogs.isEmpty()) {
                            Text("No disposal or bird sale records yet for this flock.", fontSize = 13.sp, color = Color(0xFF64748B))
                        } else {
                            flockDisposalLogs.forEach { dLog ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFFFF7ED),
                                    border = BorderStroke(1.dp, Color(0xFFFFE4E6)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 6.dp)
                                        .combinedClickable(
                                            enabled = canEdit,
                                            onClick = {},
                                            onLongClick = {
                                                poultryLogForOptions = PoultryLogAction.Disposal(dLog)
                                            }
                                        )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "${dLog.quantity} Birds • Reason: ${dLog.reason}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color(0xFF9A3412)
                                            )
                                            Text(
                                                text = "Date: ${dLog.date} ${if (dLog.notes.isNotBlank()) "• ${dLog.notes}" else ""}",
                                                fontSize = 12.sp,
                                                color = Color(0xFFC2410C)
                                            )
                                        }
                                        if (dLog.amount > 0) {
                                            Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFDCFCE7)) {
                                                Text(
                                                    text = "+KSh ${dLog.amount.toInt()}",
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF15803D)
                                                )
                                            }
                                        } else {
                                            Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFFEE2E2)) {
                                                Text(
                                                    text = "-${dLog.quantity}",
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF991B1B)
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

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    // Dialog 1: Feed Consumption
    if (showFeedDialog) {
        Dialog(onDismissRequest = { showFeedDialog = false }) {
            var feedDate by remember { mutableStateOf(PoultryAgeAndVaccinationUtils.formatDate(Date())) }
            var feedType by remember { mutableStateOf(flockAgeInfo.feedStage.feedType) }
            var qtyText by remember { mutableStateOf("50") }
            var costText by remember { mutableStateOf("22.50") }

            Card(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Log Feed Consumption", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    AppDatePickerField(
                        label = "Feed Date",
                        value = feedDate,
                        onValueChange = { feedDate = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = feedType, onValueChange = { feedType = it }, label = { Text("Feed Type") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = qtyText, onValueChange = { qtyText = it }, label = { Text("Quantity (Kg)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = costText, onValueChange = { costText = it }, label = { Text("Total Cost ($)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = { showFeedDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val qty = qtyText.toDoubleOrNull() ?: 50.0
                                val cost = costText.toDoubleOrNull() ?: 0.0
                                if (unitId > 0) {
                                    onAddPoultryLog(
                                        PoultryLog(
                                            unitId = unitId,
                                            logType = "FEED",
                                            date = feedDate.ifBlank { PoultryAgeAndVaccinationUtils.formatDate(Date()) },
                                            feedType = feedType.trim(),
                                            quantityKg = qty.coerceAtLeast(0.0),
                                            costAmount = cost.coerceAtLeast(0.0),
                                            notes = "Logged from Flock View"
                                        )
                                    )
                                }
                                showFeedDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                        ) { Text("SAVE FEED LOG") }
                    }
                }
            }
        }
    }

    // Dialog 2: Mortality Record
    if (showMortalityDialog) {
        Dialog(onDismissRequest = { showMortalityDialog = false }) {
            var mortalityDate by remember { mutableStateOf(PoultryAgeAndVaccinationUtils.formatDate(Date())) }
            var deathCountText by remember { mutableStateOf("1") }
            var causeText by remember { mutableStateOf("Heat Stress") }
            var notesText by remember { mutableStateOf("High afternoon humidity") }

            Card(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("⚠️ Record Bird Mortality", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                    Text("Deducts death count automatically from live flock head count.", fontSize = 12.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(12.dp))
                    AppDatePickerField(
                        label = "Date of Incident",
                        value = mortalityDate,
                        onValueChange = { mortalityDate = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = deathCountText, onValueChange = { deathCountText = it }, label = { Text("Number of Bird Deaths") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = causeText, onValueChange = { causeText = it }, label = { Text("Cause / Reason") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = notesText, onValueChange = { notesText = it }, label = { Text("Notes / Observations") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = { showMortalityDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val count = deathCountText.toIntOrNull() ?: 1
                                val safeCount = count.coerceAtLeast(1)
                                if (unitId > 0) {
                                    onAddPoultryLog(
                                        PoultryLog(
                                            unitId = unitId,
                                            logType = "MORTALITY",
                                            date = mortalityDate.ifBlank { PoultryAgeAndVaccinationUtils.formatDate(Date()) },
                                            birdCount = safeCount,
                                            cause = causeText.trim(),
                                            notes = notesText.trim()
                                        )
                                    )
                                }
                                liveHeadCount = (liveHeadCount - safeCount).coerceAtLeast(0)
                                onUpdateFlockHeadCount(liveHeadCount)
                                showMortalityDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                        ) { Text("RECORD DEATH") }
                    }
                }
            }
        }
    }

    // Dialog 3: Egg Sales
    if (showEggSaleDialog) {
        Dialog(onDismissRequest = { showEggSaleDialog = false }) {
            var saleDate by remember { mutableStateOf(PoultryAgeAndVaccinationUtils.formatDate(Date())) }
            var traysText by remember { mutableStateOf("10") }
            var priceText by remember { mutableStateOf("4.50") }
            var buyerText by remember { mutableStateOf("Local Supermarket") }

            Card(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Log Egg Sales Revenue", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    AppDatePickerField(
                        label = "Sale Date",
                        value = saleDate,
                        onValueChange = { saleDate = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = traysText, onValueChange = { traysText = it }, label = { Text("Number of Trays Sold") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = priceText, onValueChange = { priceText = it }, label = { Text("Price per Tray ($)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = buyerText, onValueChange = { buyerText = it }, label = { Text("Buyer Name") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = { showEggSaleDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val trays = traysText.toIntOrNull() ?: 10
                                val price = priceText.toDoubleOrNull() ?: 4.50
                                val total = trays * price
                                if (unitId > 0) {
                                    onAddPoultryLog(
                                        PoultryLog(
                                            unitId = unitId,
                                            logType = "EGG_SALE",
                                            date = saleDate.ifBlank { PoultryAgeAndVaccinationUtils.formatDate(Date()) },
                                            traysSold = trays.coerceAtLeast(0),
                                            pricePerTray = price.coerceAtLeast(0.0),
                                            totalRevenue = total.coerceAtLeast(0.0),
                                            buyer = buyerText.trim()
                                        )
                                    )
                                }
                                onAddFinanceClick()
                                showEggSaleDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                        ) { Text("SAVE SALE") }
                    }
                }
            }
        }
    }

    // Dialog 4: Custom Vaccination Record with AppDatePicker
    if (showVaccineDialog) {
        Dialog(onDismissRequest = { showVaccineDialog = false }) {
            var nameText by remember { mutableStateOf("Fowl Pox Vaccine") }
            var stageText by remember { mutableStateOf("Week 8") }
            var dateText by remember { mutableStateOf(PoultryAgeAndVaccinationUtils.formatDate(Date())) }

            Card(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Add Custom Vaccine Schedule", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = nameText, onValueChange = { nameText = it }, label = { Text("Vaccine Name") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = stageText, onValueChange = { stageText = it }, label = { Text("Growth Stage (e.g. Week 8)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    AppDatePickerField(
                        value = dateText,
                        onValueChange = { dateText = it },
                        label = "Scheduled Date",
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "custom_vaccine_date_picker"
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = { showVaccineDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                customVaccines.add(0, PoultryVaccineItem("v_${System.currentTimeMillis()}", nameText, stageText, dateText, "UPCOMING", "User scheduled"))
                                showVaccineDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                        ) { Text("ADD VACCINE") }
                    }
                }
            }
        }
    }
}


@Composable
private fun PoultryLogOptionsDialog(
    log: PoultryLogAction,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val logLabel = when (log) {
        is PoultryLogAction.Feed -> "feed log"
        is PoultryLogAction.Mortality -> "mortality log"
        is PoultryLogAction.EggSale -> "egg-sale log"
        is PoultryLogAction.Disposal -> "disposal log"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Log Options",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        },
        text = {
            Text(
                text = "Choose an action for this $logLabel.",
                fontSize = 13.sp,
                color = Color(0xFF475569)
            )
        },
        confirmButton = {
            Button(
                onClick = onEdit,
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
            ) {
                Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Edit Log", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}

@Composable
private fun PoultryLogDeleteConfirmDialog(
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = null,
                    tint = Color(0xFFDC2626)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Delete Log?",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            }
        },
        text = {
            Text(
                text = "Are you sure you want to delete this log?",
                fontSize = 13.sp,
                color = Color(0xFF475569)
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
            ) {
                Text("Delete", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF475569))
            }
        }
    )
}

@Composable
private fun EditFeedLogDialog(
    log: PoultryFeedLogItem,
    onDismiss: () -> Unit,
    onSave: (PoultryFeedLogItem) -> Unit
) {
    var dateText by remember(log.id) { mutableStateOf(log.date) }
    var feedType by remember(log.id) { mutableStateOf(log.feedType) }
    var quantityText by remember(log.id) { mutableStateOf(log.quantityKg.toString()) }
    var costText by remember(log.id) { mutableStateOf(log.costAmount.toString()) }
    var notesText by remember(log.id) { mutableStateOf(log.notes) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(" Edit Feed Log", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                AppDatePickerField(value = dateText, onValueChange = { dateText = it }, label = "Date")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = feedType, onValueChange = { feedType = it }, label = { Text("Feed Type") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = quantityText, onValueChange = { quantityText = it }, label = { Text("Quantity (Kg)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = costText, onValueChange = { costText = it }, label = { Text("Total Cost (KSh)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = notesText, onValueChange = { notesText = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(
                                log.copy(
                                    date = dateText,
                                    feedType = feedType.trim(),
                                    quantityKg = quantityText.toDoubleOrNull() ?: log.quantityKg,
                                    costAmount = costText.toDoubleOrNull() ?: log.costAmount,
                                    notes = notesText.trim()
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                    ) { Text("SAVE CHANGES") }
                }
            }
        }
    }
}

@Composable
private fun EditMortalityLogDialog(
    log: PoultryMortalityLogItem,
    onDismiss: () -> Unit,
    onSave: (PoultryMortalityLogItem) -> Unit
) {
    var dateText by remember(log.id) { mutableStateOf(log.date) }
    var countText by remember(log.id) { mutableStateOf(log.count.toString()) }
    var causeText by remember(log.id) { mutableStateOf(log.cause) }
    var notesText by remember(log.id) { mutableStateOf(log.notes) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(" Edit Mortality Log", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                Spacer(modifier = Modifier.height(12.dp))
                AppDatePickerField(value = dateText, onValueChange = { dateText = it }, label = "Date")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = countText, onValueChange = { countText = it }, label = { Text("Number of Bird Deaths") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = causeText, onValueChange = { causeText = it }, label = { Text("Cause / Reason") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = notesText, onValueChange = { notesText = it }, label = { Text("Notes / Observations") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(
                                log.copy(
                                    date = dateText,
                                    count = (countText.toIntOrNull() ?: log.count).coerceAtLeast(1),
                                    cause = causeText.trim(),
                                    notes = notesText.trim()
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                    ) { Text("SAVE CHANGES") }
                }
            }
        }
    }
}

@Composable
private fun EditEggSaleLogDialog(
    log: PoultryEggSaleItem,
    onDismiss: () -> Unit,
    onSave: (PoultryEggSaleItem) -> Unit
) {
    var dateText by remember(log.id) { mutableStateOf(log.date) }
    var traysText by remember(log.id) { mutableStateOf(log.traysSold.toString()) }
    var priceText by remember(log.id) { mutableStateOf(log.pricePerTray.toString()) }
    var buyerText by remember(log.id) { mutableStateOf(log.buyer) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(" Edit Egg Sale", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                Spacer(modifier = Modifier.height(12.dp))
                AppDatePickerField(value = dateText, onValueChange = { dateText = it }, label = "Sale Date")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = traysText, onValueChange = { traysText = it }, label = { Text("Number of Trays Sold") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = priceText, onValueChange = { priceText = it }, label = { Text("Price per Tray (KSh)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = buyerText, onValueChange = { buyerText = it }, label = { Text("Buyer Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val trays = (traysText.toIntOrNull() ?: log.traysSold).coerceAtLeast(0)
                            val price = (priceText.toDoubleOrNull() ?: log.pricePerTray).coerceAtLeast(0.0)
                            onSave(
                                log.copy(
                                    date = dateText,
                                    traysSold = trays,
                                    pricePerTray = price,
                                    totalRevenue = trays * price,
                                    buyer = buyerText.trim()
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                    ) { Text("SAVE CHANGES") }
                }
            }
        }
    }
}

@Composable
private fun EditDisposalLogDialog(
    log: FlockDisposalLogItem,
    onDismiss: () -> Unit,
    onSave: (FlockDisposalLogItem) -> Unit
) {
    var dateText by remember(log.id) { mutableStateOf(log.date) }
    var quantityText by remember(log.id) { mutableStateOf(log.quantity.toString()) }
    var reasonText by remember(log.id) { mutableStateOf(log.reason) }
    var amountText by remember(log.id) { mutableStateOf(log.amount.toString()) }
    var notesText by remember(log.id) { mutableStateOf(log.notes) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(" Edit Disposal Log", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                Spacer(modifier = Modifier.height(12.dp))
                AppDatePickerField(value = dateText, onValueChange = { dateText = it }, label = "Disposal Date")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = quantityText, onValueChange = { quantityText = it }, label = { Text("Number of Birds") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = reasonText, onValueChange = { reasonText = it }, label = { Text("Reason") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = amountText, onValueChange = { amountText = it }, label = { Text("Amount (KSh)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = notesText, onValueChange = { notesText = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(
                                log.copy(
                                    date = dateText,
                                    quantity = (quantityText.toIntOrNull() ?: log.quantity).coerceAtLeast(1),
                                    reason = reasonText.trim(),
                                    amount = (amountText.toDoubleOrNull() ?: log.amount).coerceAtLeast(0.0),
                                    notes = notesText.trim()
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                    ) { Text("SAVE CHANGES") }
                }
            }
        }
    }
}

@Composable
private fun AutomatedPoultryStatusCard(
    status: com.example.utils.PoultryAutomatedStatus,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
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
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFFECFDF5), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = ForestGreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "AUTOMATED DIAGNOSTIC ENGINE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = "Health & Production Status",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = Color(0xFFDCFCE7),
                    border = BorderStroke(1.dp, Color(0xFF86EFAC))
                ) {
                    Text(
                        text = "⚡ Real-Time",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF166534),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2 Main Diagnostic Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Health Status Box
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = when (status.healthLevel) {
                        com.example.utils.PoultryHealthLevel.HEALTHY -> Color(0xFFF0FDF4)
                        com.example.utils.PoultryHealthLevel.CAUTION -> Color(0xFFFFFBEB)
                        com.example.utils.PoultryHealthLevel.CRITICAL -> Color(0xFFFEF2F2)
                    },
                    border = BorderStroke(
                        1.dp,
                        when (status.healthLevel) {
                            com.example.utils.PoultryHealthLevel.HEALTHY -> Color(0xFFBBF7D0)
                            com.example.utils.PoultryHealthLevel.CAUTION -> Color(0xFFFDE68A)
                            com.example.utils.PoultryHealthLevel.CRITICAL -> Color(0xFFFECACA)
                        }
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "FLOCK HEALTH",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = status.healthBadgeLabel,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = when (status.healthLevel) {
                                com.example.utils.PoultryHealthLevel.HEALTHY -> Color(0xFF15803D)
                                com.example.utils.PoultryHealthLevel.CAUTION -> Color(0xFFB45309)
                                com.example.utils.PoultryHealthLevel.CRITICAL -> Color(0xFFB91C1C)
                            }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = status.healthAdvice,
                            fontSize = 10.5.sp,
                            color = Color(0xFF334155),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Production Status Box
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = when (status.productionLevel) {
                        com.example.utils.PoultryProductionLevel.EXCELLENT -> Color(0xFFEFF6FF)
                        com.example.utils.PoultryProductionLevel.NORMAL -> Color(0xFFF0FDF4)
                        com.example.utils.PoultryProductionLevel.LOW_WARNING -> Color(0xFFFFFBEB)
                        com.example.utils.PoultryProductionLevel.PRE_LAY -> Color(0xFFF8FAFC)
                    },
                    border = BorderStroke(
                        1.dp,
                        when (status.productionLevel) {
                            com.example.utils.PoultryProductionLevel.EXCELLENT -> Color(0xFFBFDBFE)
                            com.example.utils.PoultryProductionLevel.NORMAL -> Color(0xFFBBF7D0)
                            com.example.utils.PoultryProductionLevel.LOW_WARNING -> Color(0xFFFDE68A)
                            com.example.utils.PoultryProductionLevel.PRE_LAY -> Color(0xFFE2E8F0)
                        }
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "PRODUCTION STATUS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = status.productionBadgeLabel,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = when (status.productionLevel) {
                                com.example.utils.PoultryProductionLevel.EXCELLENT -> Color(0xFF1E40AF)
                                com.example.utils.PoultryProductionLevel.NORMAL -> Color(0xFF15803D)
                                com.example.utils.PoultryProductionLevel.LOW_WARNING -> Color(0xFFB45309)
                                com.example.utils.PoultryProductionLevel.PRE_LAY -> Color(0xFF475569)
                            }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = status.productionAdvice,
                            fontSize = 10.5.sp,
                            color = Color(0xFF334155),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Automated Alerts if any exist
            if (status.automatedAlerts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "AUTOMATED HEALTH & YIELD ALERTS (${status.automatedAlerts.size})",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    status.automatedAlerts.forEach { alert ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (alert.isCritical) Color(0xFFFEF2F2) else Color(0xFFFFFBEB),
                            border = BorderStroke(1.dp, if (alert.isCritical) Color(0xFFFECACA) else Color(0xFFFDE68A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = if (alert.isCritical) Icons.Filled.Warning else Icons.Filled.Info,
                                    contentDescription = null,
                                    tint = if (alert.isCritical) Color(0xFFDC2626) else Color(0xFFD97706),
                                    modifier = Modifier.size(16.dp).padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = alert.title,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (alert.isCritical) Color(0xFF991B1B) else Color(0xFF92400E)
                                    )
                                    Text(
                                        text = alert.message,
                                        fontSize = 11.sp,
                                        color = Color(0xFF334155)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "💡 Action: ${alert.recommendation}",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (alert.isCritical) Color(0xFFB91C1C) else Color(0xFFB45309)
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

@Composable
fun DisposedPoultryCard(
    animal: AnimalDetailData,
    onClick: () -> Unit,
    onMoreOptions: () -> Unit,
    canEdit: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("disposed_flock_card_${animal.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Egg,
                                contentDescription = "Disposed Flock",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = animal.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        val displayDate = animal.disposalDate.ifBlank { "N/A" }
                        Text(
                            text = "Disposed on $displayDate",
                            fontSize = 12.5.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEE2E2),
                    border = BorderStroke(1.dp, Color(0xFFFECACA))
                ) {
                    val count = if (animal.headCountInt > 0) "${animal.headCountInt} Birds" else "Disposed"
                    Text(
                        text = count,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF991B1B),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                if (canEdit) {
                    IconButton(
                        onClick = onMoreOptions,
                        modifier = Modifier.size(32.dp).testTag("more_options_${animal.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "Options",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (animal.disposalReason.isNotBlank() || animal.disposalAmount > 0.0) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (animal.disposalReason.isNotBlank()) {
                            Text(
                                text = "Reason: ${animal.disposalReason}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF475569)
                            )
                        }
                        if (animal.disposalAmount > 0.0) {
                            Text(
                                text = "KSh %,.0f".format(animal.disposalAmount),
                                fontSize = 11.5.sp,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisposedFlockDetailView(
    flock: AnimalDetailData,
    userRole: String,
    onBackClick: () -> Unit,
    onDeleteFlock: () -> Unit = {},
    canEdit: Boolean = true,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = flock.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF0F172A),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Disposed Poultry Record",
                            fontSize = 12.sp,
                            color = Color(0xFFDC2626),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF0F172A)
                        )
                    }
                },
                actions = {
                    if (canEdit) {
                        IconButton(onClick = onDeleteFlock) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete Record",
                                tint = Color(0xFFDC2626)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8FAFC),
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFFFEE2E2),
                                    border = BorderStroke(1.dp, Color(0xFFFECACA)),
                                    modifier = Modifier.size(56.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.Egg,
                                            contentDescription = null,
                                            tint = Color(0xFFDC2626),
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = flock.name,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "Breed: ${flock.breed.ifEmpty { "Layers" }}",
                                        fontSize = 13.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = Color(0xFFFEE2E2),
                                border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                            ) {
                                Text(
                                    text = "DISPOSED",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF991B1B),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                        Spacer(modifier = Modifier.height(16.dp))

                        // Key Disposed Information
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Birds Disposed Box
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFFEF2F2),
                                border = BorderStroke(1.dp, Color(0xFFFECACA)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "BIRDS DISPOSED",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF991B1B)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${flock.headCountInt} Birds",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF7F1D1D)
                                    )
                                }
                            }

                            // Disposed Date Box
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "DISPOSAL DATE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF64748B)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = flock.disposalDate.ifBlank { "N/A" },
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                }
                            }
                        }

                        if (flock.disposalReason.isNotBlank() || flock.disposalAmount > 0.0) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (flock.disposalReason.isNotBlank()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Disposal Reason", fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                                            Text(flock.disposalReason, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                        }
                                    }
                                    if (flock.disposalAmount > 0.0) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Total Revenue / Value", fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                                            Text("KSh %,.0f".format(flock.disposalAmount), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                                        }
                                    }
                                }
                            }
                        }

                        val displayNotes = flock.disposalNotes.ifBlank { flock.notes }
                        if (displayNotes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFFFFBEB),
                                border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Disposal Notes / Destination", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(displayNotes, fontSize = 13.sp, color = Color(0xFF334155))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
