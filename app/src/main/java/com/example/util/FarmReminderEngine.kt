package com.example.util

import com.example.data.FarmTask
import com.example.data.FarmUnit
import com.example.data.TaskCategory
import com.example.data.TaskPriority
import com.example.ui.screens.AnimalDetailData
import com.example.ui.screens.CattleEventItem
import com.example.utils.PoultryAgeAndVaccinationUtils
import com.example.utils.VaccineDueStatus
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class ReminderType(val displayName: String, val emoji: String) {
    VACCINATION("Vaccination", "💉"),
    DEWORMING("Deworming", "💊"),
    CALVING("Expected Calving", "🍼"),
    DRY_OFF("Dry-Off Milestone", "🍂"),
    PREGNANCY_CHECK("Pregnancy Check (PD)", "🤰"),
    TASK("Scheduled Farm Task", "📋")
}

enum class ReminderUrgency(val label: String, val colorHex: Long) {
    OVERDUE("🚨 Overdue", 0xFFDC2626),
    TODAY("⚠️ Due Today", 0xFFEA580C),
    DUE_SOON("⏳ Due Soon", 0xFFD97706),
    UPCOMING("📅 Upcoming", 0xFF2563EB)
}

data class FarmReminder(
    val id: String,
    val type: ReminderType,
    val title: String,
    val targetName: String,
    val targetTag: String,
    val dueDateStr: String,
    val daysRemaining: Int,
    val urgency: ReminderUrgency,
    val details: String,
    val recommendation: String,
    val actionLabel: String = "Log Action",
    val unitId: Long? = null,
    val isCompleted: Boolean = false
)

object FarmReminderEngine {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
    private val altDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

    private fun parseDate(str: String?): Date? {
        if (str.isNullOrBlank()) return null
        return try {
            dateFormat.parse(str.trim())
        } catch (_: Exception) {
            try {
                altDateFormat.parse(str.trim())
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun getDaysDifference(targetDate: Date, fromDate: Date = Date()): Int {
        val diffMs = targetDate.time - fromDate.time
        return (diffMs / (1000 * 60 * 60 * 24)).toInt()
    }

    private fun calculateUrgency(daysRemaining: Int): ReminderUrgency {
        return when {
            daysRemaining < 0 -> ReminderUrgency.OVERDUE
            daysRemaining == 0 -> ReminderUrgency.TODAY
            daysRemaining in 1..7 -> ReminderUrgency.DUE_SOON
            else -> ReminderUrgency.UPCOMING
        }
    }

    fun computeAllReminders(
        units: List<FarmUnit>,
        cattleEventsMap: Map<Long, List<CattleEventItem>> = emptyMap(),
        tasks: List<FarmTask> = emptyList()
    ): List<FarmReminder> {
        val reminders = mutableListOf<FarmReminder>()
        val today = Date()

        // 1. EVALUATE LIVESTOCK UNITS (CATTLE & POULTRY)
        units.forEach { unit ->
            val isPoultry = unit.type.equals("Poultry", ignoreCase = true) ||
                    unit.name.contains("Flock", ignoreCase = true) ||
                    unit.name.contains("Broiler", ignoreCase = true) ||
                    unit.name.contains("Layer", ignoreCase = true) ||
                    unit.name.contains("Kienyeji", ignoreCase = true)

            if (isPoultry) {
                // POULTRY VACCINATION & DEWORMING REMINDERS
                val dateAddedStr = unit.dateAdded.ifBlank { unit.dob }
                val ageInfo = PoultryAgeAndVaccinationUtils.calculateFlockAge(dateAddedStr)
                val schedule = PoultryAgeAndVaccinationUtils.calculateVaccinationSchedule(dateAddedStr, emptySet())

                schedule.forEach { item ->
                    if (!item.isCompleted) {
                        val urgency = when (item.status) {
                            VaccineDueStatus.OVERDUE -> ReminderUrgency.OVERDUE
                            VaccineDueStatus.DUE_TODAY -> ReminderUrgency.TODAY
                            VaccineDueStatus.DUE_SOON -> ReminderUrgency.DUE_SOON
                            VaccineDueStatus.UPCOMING -> ReminderUrgency.UPCOMING
                            VaccineDueStatus.COMPLETED -> ReminderUrgency.UPCOMING
                        }

                        reminders.add(
                            FarmReminder(
                                id = "poultry_vac_${unit.id}_${item.ruleId}",
                                type = ReminderType.VACCINATION,
                                title = item.vaccineName,
                                targetName = unit.name,
                                targetTag = if (unit.tagNumber.isNotBlank()) "#${unit.tagNumber}" else "${unit.headCount} birds",
                                dueDateStr = item.scheduledDueDateStr,
                                daysRemaining = item.dueDaysMin,
                                urgency = urgency,
                                details = "${item.targetStageLabel} • Method: ${item.administrationMethod}",
                                recommendation = item.notes.ifBlank { "Ensure clean, chlorinated-free drinking water or eye drops." },
                                actionLabel = "Log Vaccination",
                                unitId = unit.id
                            )
                        )
                    }
                }

                // Routine Poultry Deworming Alert (every 8 weeks / 56 days)
                if (ageInfo.totalDays >= 42) {
                    val dewormDueDays = (56 - (ageInfo.totalDays % 56)).coerceIn(-10, 56)
                    val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, dewormDueDays) }
                    val dewormDateStr = dateFormat.format(cal.time)
                    reminders.add(
                        FarmReminder(
                            id = "poultry_deworm_${unit.id}",
                            type = ReminderType.DEWORMING,
                            title = "Routine Flock Deworming",
                            targetName = unit.name,
                            targetTag = if (unit.tagNumber.isNotBlank()) "#${unit.tagNumber}" else "${unit.headCount} birds",
                            dueDateStr = dewormDateStr,
                            daysRemaining = dewormDueDays,
                            urgency = calculateUrgency(dewormDueDays),
                            details = "Periodic internal parasite & worm control for ${unit.name} (${ageInfo.formattedAge}).",
                            recommendation = "Administer Piperazine or Levamisole in drinking water for 1-2 days.",
                            actionLabel = "Log Deworming",
                            unitId = unit.id
                        )
                    )
                }

            } else {
                // CATTLE REMINDERS
                val animalData = AnimalDetailData(
                    id = unit.id.toString(),
                    name = unit.name,
                    tagNumber = unit.tagNumber,
                    breed = unit.breed,
                    category = "Cattle",
                    status = unit.healthStatus,
                    age = unit.dob,
                    weight = unit.currentWeight,
                    lastMilk = "12 L/day",
                    breedingStatus = unit.healthStatus,
                    dateOfBirth = unit.dob,
                    weightAtBirth = unit.weightAtBirth,
                    sire = unit.sire,
                    dam = unit.dam,
                    headCountInt = unit.headCount,
                    photoUri = unit.photoUri
                )
                val events = cattleEventsMap[unit.id] ?: emptyList()
                val eval = CattleLifecycleEngine.evaluateCattleStage(animalData, events)

                // A. Expected Calving Reminder
                if (eval.isInCalf && eval.expectedCalvingDate != null) {
                    val calvingDate = parseDate(eval.expectedCalvingDate)
                    val daysLeft = if (calvingDate != null) getDaysDifference(calvingDate, today) else (283 - (eval.gestationDays ?: 200))
                    val urgency = calculateUrgency(daysLeft)

                    val detailsMsg = when {
                        daysLeft <= 0 -> "Calving due date reached! Monitor closely for signs of labor (water bag, restlessness, udder filling)."
                        daysLeft <= 14 -> "Imminent calving window (${daysLeft} days remaining). Move to clean maternity pen & provide steam-up ration."
                        daysLeft <= 30 -> "Late gestation (Day ${eval.gestationDays ?: 253} of 283). Prepare maternity pen and mineral lick."
                        else -> "Gestation in progress (Day ${eval.gestationDays ?: 150} of 283). Expected delivery on ${eval.expectedCalvingDate}."
                    }

                    reminders.add(
                        FarmReminder(
                            id = "calving_${unit.id}",
                            type = ReminderType.CALVING,
                            title = "Expected Calving — ${unit.name}",
                            targetName = unit.name,
                            targetTag = if (unit.tagNumber.isNotBlank()) "#${unit.tagNumber}" else unit.breed,
                            dueDateStr = eval.expectedCalvingDate,
                            daysRemaining = daysLeft,
                            urgency = urgency,
                            details = detailsMsg,
                            recommendation = "Ensure clean maternity pen, iodine for navel dip, and veterinary assistance on standby.",
                            actionLabel = "Record Calving",
                            unitId = unit.id
                        )
                    )

                    // B. Dry-off Milestone (60 days prior to calving)
                    if (eval.isMilking && eval.expectedDryOffDate != null) {
                        val dryDate = parseDate(eval.expectedDryOffDate)
                        val dryDaysLeft = if (dryDate != null) getDaysDifference(dryDate, today) else (daysLeft - 60)
                        reminders.add(
                            FarmReminder(
                                id = "dry_off_${unit.id}",
                                type = ReminderType.DRY_OFF,
                                title = "Dry-Off Therapy Milestone",
                                targetName = unit.name,
                                targetTag = if (unit.tagNumber.isNotBlank()) "#${unit.tagNumber}" else unit.breed,
                                dueDateStr = eval.expectedDryOffDate,
                                daysRemaining = dryDaysLeft,
                                urgency = calculateUrgency(dryDaysLeft),
                                details = "Lactation cessation milestone for udder regeneration before upcoming calving.",
                                recommendation = "Cease daily milking, infuse dry cow intramammary antibiotic tube & teat sealant.",
                                actionLabel = "Log Dry-Off",
                                unitId = unit.id
                            )
                        )
                    }
                }

                // C. Pregnancy Diagnosis (PD) Check
                if (eval.stage == CattleStage.INSEMINATED) {
                    val aiDateStr = eval.lastInseminationDate
                    val aiDate = parseDate(aiDateStr)
                    val daysPostAi = if (aiDate != null) getDaysDifference(today, aiDate) else 30
                    val pdTargetDaysLeft = 60 - daysPostAi
                    val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, pdTargetDaysLeft) }
                    val pdDateStr = dateFormat.format(cal.time)

                    reminders.add(
                        FarmReminder(
                            id = "pd_check_${unit.id}",
                            type = ReminderType.PREGNANCY_CHECK,
                            title = "Pregnancy Diagnosis (PD) Check",
                            targetName = unit.name,
                            targetTag = if (unit.tagNumber.isNotBlank()) "#${unit.tagNumber}" else unit.breed,
                            dueDateStr = pdDateStr,
                            daysRemaining = pdTargetDaysLeft,
                            urgency = calculateUrgency(pdTargetDaysLeft),
                            details = "60-Day post-AI veterinary palpation or ultrasound to confirm conception.",
                            recommendation = "Schedule farm veterinarian for rectal palpation or ultrasound scan.",
                            actionLabel = "Log PD Result",
                            unitId = unit.id
                        )
                    )
                }

                // D. Cattle Vaccination Reminders
                val isVaccinationDue = unit.healthStatus.contains("Vaccin", ignoreCase = true)
                val isDewormingDue = unit.healthStatus.contains("Deworm", ignoreCase = true)

                if (isVaccinationDue) {
                    reminders.add(
                        FarmReminder(
                            id = "cattle_vac_${unit.id}",
                            type = ReminderType.VACCINATION,
                            title = "Foot & Mouth (FMD) / Anthrax Vaccination",
                            targetName = unit.name,
                            targetTag = if (unit.tagNumber.isNotBlank()) "#${unit.tagNumber}" else unit.breed,
                            dueDateStr = unit.lastUpdated.ifBlank { dateFormat.format(today) },
                            daysRemaining = 0,
                            urgency = ReminderUrgency.TODAY,
                            details = "Mandatory preventive herd vaccination scheduled for ${unit.name}.",
                            recommendation = "Administer quadrivalent FMD or Blanthrax booster subcutaneously under vet supervision.",
                            actionLabel = "Log Vaccination",
                            unitId = unit.id
                        )
                    )
                } else {
                    // Periodic Herd Vaccination Schedule
                    val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 14) }
                    val upcomingVacDateStr = dateFormat.format(cal.time)
                    reminders.add(
                        FarmReminder(
                            id = "cattle_vac_routine_${unit.id}",
                            type = ReminderType.VACCINATION,
                            title = "Bi-Annual FMD & Blackquarter Booster",
                            targetName = unit.name,
                            targetTag = if (unit.tagNumber.isNotBlank()) "#${unit.tagNumber}" else unit.breed,
                            dueDateStr = upcomingVacDateStr,
                            daysRemaining = 14,
                            urgency = ReminderUrgency.UPCOMING,
                            details = "Routine herd immunity booster against Foot and Mouth Disease (FMD) & Clostridial infections.",
                            recommendation = "Prepare cold chain storage for vaccines (2°C - 8°C).",
                            actionLabel = "Schedule Vaccination",
                            unitId = unit.id
                        )
                    )
                }

                // E. Cattle Deworming Reminders
                if (isDewormingDue) {
                    reminders.add(
                        FarmReminder(
                            id = "cattle_deworm_${unit.id}",
                            type = ReminderType.DEWORMING,
                            title = "Quarterly Deworming (Liver Fluke & Roundworms)",
                            targetName = unit.name,
                            targetTag = if (unit.tagNumber.isNotBlank()) "#${unit.tagNumber}" else unit.breed,
                            dueDateStr = unit.lastUpdated.ifBlank { dateFormat.format(today) },
                            daysRemaining = 0,
                            urgency = ReminderUrgency.TODAY,
                            details = "Deworming cycle overdue or due today for ${unit.name}.",
                            recommendation = "Administer Albendazole 10% oral drench (10ml per 100kg bodyweight) or Closantel.",
                            actionLabel = "Log Deworming",
                            unitId = unit.id
                        )
                    )
                } else {
                    val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 21) }
                    val upcomingDewormDateStr = dateFormat.format(cal.time)
                    reminders.add(
                        FarmReminder(
                            id = "cattle_deworm_routine_${unit.id}",
                            type = ReminderType.DEWORMING,
                            title = "Routine Seasonal Deworming",
                            targetName = unit.name,
                            targetTag = if (unit.tagNumber.isNotBlank()) "#${unit.tagNumber}" else unit.breed,
                            dueDateStr = upcomingDewormDateStr,
                            daysRemaining = 21,
                            urgency = ReminderUrgency.UPCOMING,
                            details = "Scheduled seasonal anti-parasitic drench for pasture grazing stock.",
                            recommendation = "Alternate between Albendazole and Ivermectin/Levamisole to prevent drug resistance.",
                            actionLabel = "Log Deworming",
                            unitId = unit.id
                        )
                    )
                }
            }
        }

        // 2. EVALUATE SCHEDULED FARM TASKS
        tasks.filter { !it.isCompleted }.forEach { task ->
            val isHigh = task.priority == TaskPriority.HIGH
            val taskType = when {
                task.title.contains("Vaccin", ignoreCase = true) -> ReminderType.VACCINATION
                task.title.contains("Deworm", ignoreCase = true) || task.title.contains("Worm", ignoreCase = true) -> ReminderType.DEWORMING
                task.title.contains("Calv", ignoreCase = true) || task.title.contains("Birth", ignoreCase = true) -> ReminderType.CALVING
                else -> ReminderType.TASK
            }

            reminders.add(
                FarmReminder(
                    id = "task_${task.id}",
                    type = taskType,
                    title = task.title,
                    targetName = task.targetUnit.ifBlank { "General Farm Task" },
                    targetTag = "[${task.category.name}]",
                    dueDateStr = task.scheduledTime,
                    daysRemaining = if (isHigh) 0 else 2,
                    urgency = if (isHigh) ReminderUrgency.TODAY else ReminderUrgency.DUE_SOON,
                    details = "Assigned Worker: ${task.assignedWorker ?: "Unassigned"} • Priority: ${task.priority.name}",
                    recommendation = task.instructions ?: "Complete task checklist and submit operational confirmation.",
                    actionLabel = "Mark Task Done"
                )
            )
        }

        // Sort by urgency: Overdue first, then Due Today, then Due Soon, then Upcoming
        return reminders.sortedWith(
            compareBy<FarmReminder> {
                when (it.urgency) {
                    ReminderUrgency.OVERDUE -> 0
                    ReminderUrgency.TODAY -> 1
                    ReminderUrgency.DUE_SOON -> 2
                    ReminderUrgency.UPCOMING -> 3
                }
            }.thenBy { it.daysRemaining }
        )
    }
}
