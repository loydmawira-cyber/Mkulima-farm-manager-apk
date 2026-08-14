package com.example.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class PoultryVaccineRule(
    val id: String,
    val dayMin: Int,
    val dayMax: Int,
    val targetStageLabel: String,
    val vaccineName: String,
    val administrationMethod: String,
    val description: String
)

enum class VaccineDueStatus {
    COMPLETED,
    OVERDUE,
    DUE_TODAY,
    DUE_SOON,   // 1-2 days before due
    UPCOMING
}

data class FlockVaccineScheduleItem(
    val ruleId: String,
    val vaccineName: String,
    val administrationMethod: String,
    val targetStageLabel: String,
    val scheduledDueDateStr: String,
    val dueDaysMin: Int,
    val dueDaysMax: Int,
    val status: VaccineDueStatus,
    val statusLabel: String,
    val isCompleted: Boolean,
    val notes: String
)

data class PoultryFeedStageInfo(
    val stageName: String,               // "Starter Feed (0 - 3 Weeks)", "Grower Feed (3 - 8 Weeks)", "Layer / Finisher Feed (8+ Weeks)"
    val feedType: String,                // "Chick Starter Mash (20–22% CP)"
    val purpose: String,
    val dailyRationPerBird: String,
    val hasTransitionAlert: Boolean,
    val transitionAlertMessage: String?
)

data class PoultryAgeInfo(
    val totalDays: Int,
    val weeks: Int,
    val remainingDays: Int,
    val formattedAge: String,            // e.g. "24 Days (3 Wks, 3 Days)"
    val shortAgeLabel: String,           // e.g. "Day 24 • Wk 3"
    val dateAddedFormatted: String,
    val feedStage: PoultryFeedStageInfo
)

object PoultryAgeAndVaccinationUtils {

    // Standard Poultry Vaccination Schedule as requested
    val STANDARD_VACCINATION_RULES = listOf(
        PoultryVaccineRule(
            id = "vac_day_0",
            dayMin = 0,
            dayMax = 0,
            targetStageLabel = "Day 0 (Hatch/Arrival)",
            vaccineName = "Marek's Disease Vaccine",
            administrationMethod = "Subcutaneous Injection",
            description = "Administered at hatch or arrival on farm via injection."
        ),
        PoultryVaccineRule(
            id = "vac_day_7",
            dayMin = 7,
            dayMax = 7,
            targetStageLabel = "Day 7 (1 Week)",
            vaccineName = "Newcastle Disease Vaccine (ND1)",
            administrationMethod = "Eye / Nasal drop",
            description = "First Newcastle strain administered via intraocular/nasal drop."
        ),
        PoultryVaccineRule(
            id = "vac_day_14",
            dayMin = 14,
            dayMax = 14,
            targetStageLabel = "Day 14 (2 Weeks)",
            vaccineName = "Gumboro Vaccine (IBD 1)",
            administrationMethod = "Drinking water",
            description = "Gumboro intermediate strain via clean drinking water with skim milk stabilizer."
        ),
        PoultryVaccineRule(
            id = "vac_day_21",
            dayMin = 21,
            dayMax = 21,
            targetStageLabel = "Day 21 (3 Weeks)",
            vaccineName = "Newcastle Disease Vaccine (ND2)",
            administrationMethod = "Drinking water or eye drop",
            description = "Newcastle second booster (LaSota strain)."
        ),
        PoultryVaccineRule(
            id = "vac_day_28",
            dayMin = 28,
            dayMax = 28,
            targetStageLabel = "Day 28 (4 Weeks)",
            vaccineName = "Gumboro Vaccine (IBD 2 Booster)",
            administrationMethod = "Drinking water",
            description = "Second Gumboro booster in drinking water."
        ),
        PoultryVaccineRule(
            id = "vac_day_42",
            dayMin = 42,
            dayMax = 42,
            targetStageLabel = "Day 42 (6 Weeks)",
            vaccineName = "Newcastle Disease Vaccine (ND3)",
            administrationMethod = "Drinking water or eye drop",
            description = "Newcastle 6-week booster (LaSota)."
        ),
        PoultryVaccineRule(
            id = "vac_day_56_70",
            dayMin = 56,
            dayMax = 70,
            targetStageLabel = "Day 56-70 (8-10 Weeks)",
            vaccineName = "Fowl Pox Vaccine",
            administrationMethod = "Wing web stab",
            description = "Administered with double-prong needle via wing web puncture."
        ),
        PoultryVaccineRule(
            id = "vac_day_112_126",
            dayMin = 112,
            dayMax = 126,
            targetStageLabel = "Day 112-126 (16-18 Weeks)",
            vaccineName = "Newcastle Disease Vaccine (ND4)",
            administrationMethod = "Drinking water",
            description = "ND + IB pre-laying booster for layers and breeding stock."
        )
    )

    private val supportedDateFormats = listOf(
        "dd MMM yyyy",
        "yyyy-MM-dd",
        "dd/MM/yyyy",
        "dd-MM-yyyy",
        "MM/dd/yyyy",
        "d MMM yyyy",
        "yyyy/MM/dd"
    )

    fun parseDate(dateStr: String): Date? {
        if (dateStr.isBlank()) return null
        for (pattern in supportedDateFormats) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.getDefault())
                sdf.isLenient = false
                val parsed = sdf.parse(dateStr.trim())
                if (parsed != null) return parsed
            } catch (_: Exception) {
                // Try next pattern
            }
        }
        return null
    }

    fun formatDate(date: Date): String {
        return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date)
    }

    /**
     * Calculates the flock's age in days and weeks from the Date Added field.
     */
    fun calculateFlockAge(dateAddedStr: String): PoultryAgeInfo {
        val arrivalDate = parseDate(dateAddedStr)
        val now = Calendar.getInstance()
        now.set(Calendar.HOUR_OF_DAY, 23)
        now.set(Calendar.MINUTE, 59)
        now.set(Calendar.SECOND, 59)

        val totalDays = if (arrivalDate != null) {
            val startCal = Calendar.getInstance().apply { time = arrivalDate }
            startCal.set(Calendar.HOUR_OF_DAY, 0)
            startCal.set(Calendar.MINUTE, 0)
            startCal.set(Calendar.SECOND, 0)
            val diffMillis = now.timeInMillis - startCal.timeInMillis
            val diffDays = (diffMillis / (1000L * 60L * 60L * 24L)).toInt()
            diffDays.coerceAtLeast(0)
        } else {
            // Default sample age if unparsed
            24
        }

        val weeks = totalDays / 7
        val remainingDays = totalDays % 7

        val formattedAge = if (weeks == 0) {
            "$totalDays ${if (totalDays == 1) "Day" else "Days"}"
        } else if (remainingDays == 0) {
            "$totalDays Days ($weeks ${if (weeks == 1) "Week" else "Weeks"})"
        } else {
            "$totalDays Days ($weeks ${if (weeks == 1) "Wk" else "Wks"}, $remainingDays ${if (remainingDays == 1) "Day" else "Days"})"
        }

        val shortAgeLabel = "Day $totalDays • Wk $weeks"
        val dateAddedFormatted = if (arrivalDate != null) formatDate(arrivalDate) else dateAddedStr.ifBlank { "Recent" }

        val feedStage = getFlockFeedStage(totalDays)

        return PoultryAgeInfo(
            totalDays = totalDays,
            weeks = weeks,
            remainingDays = remainingDays,
            formattedAge = formattedAge,
            shortAgeLabel = shortAgeLabel,
            dateAddedFormatted = dateAddedFormatted,
            feedStage = feedStage
        )
    }

    /**
     * Determines feed stage:
     * - 0 - 3 weeks (0 - 21 days): Starter Feed
     * - 3 - 8 weeks (22 - 56 days): Grower Feed
     * - After 8 weeks (> 56 days): Layer / Finisher Feed
     */
    fun getFlockFeedStage(totalDays: Int): PoultryFeedStageInfo {
        return when {
            totalDays <= 21 -> {
                val daysUntilTransition = 22 - totalDays
                val hasAlert = daysUntilTransition in 0..2
                val alertMsg = if (hasAlert) {
                    if (daysUntilTransition == 0) "⚠️ Feed Transition Today: Switch flock from Starter to Grower Feed!"
                    else "⚠️ Feed Transition in $daysUntilTransition days: Prepare to switch to Grower Feed at Day 22 (3 Weeks)!"
                } else null

                PoultryFeedStageInfo(
                    stageName = "Starter Feed (0 - 3 Weeks)",
                    feedType = "Chick Starter Mash / Crumbs (20–22% CP)",
                    purpose = "High protein & amino acids for bone, organ, and early immune development",
                    dailyRationPerBird = "~20 – 45g / bird / day",
                    hasTransitionAlert = hasAlert,
                    transitionAlertMessage = alertMsg
                )
            }
            totalDays in 22..56 -> {
                val daysUntilTransition = 57 - totalDays
                val hasAlert = daysUntilTransition in 0..2
                val alertMsg = if (hasAlert) {
                    if (daysUntilTransition == 0) "⚠️ Feed Transition Today: Switch flock from Grower to Layer/Finisher Feed!"
                    else "⚠️ Feed Transition in $daysUntilTransition days: Prepare to switch to Layer/Finisher Feed at Day 57 (8 Weeks)!"
                } else null

                PoultryFeedStageInfo(
                    stageName = "Grower Feed (3 - 8 Weeks)",
                    feedType = "Grower Mash / Pellets (15–17% CP)",
                    purpose = "Balanced steady growth without excessive early fat deposition prior to maturity",
                    dailyRationPerBird = "~60 – 90g / bird / day",
                    hasTransitionAlert = hasAlert,
                    transitionAlertMessage = alertMsg
                )
            }
            else -> {
                PoultryFeedStageInfo(
                    stageName = "Layer / Finisher Feed (8+ Weeks)",
                    feedType = "High-Yield Layer Mash (16–18% CP + 3.8% Ca) / Finisher Pellets",
                    purpose = "High calcium and minerals for peak egg production & maximum shell strength",
                    dailyRationPerBird = "~110 – 130g / bird / day",
                    hasTransitionAlert = false,
                    transitionAlertMessage = null
                )
            }
        }
    }

    /**
     * Calculates the vaccination schedule and status for a flock based on age.
     */
    fun calculateVaccinationSchedule(
        dateAddedStr: String,
        completedRuleIds: Set<String>
    ): List<FlockVaccineScheduleItem> {
        val arrivalDate = parseDate(dateAddedStr) ?: Date()
        val ageInfo = calculateFlockAge(dateAddedStr)
        val currentAgeDays = ageInfo.totalDays

        return STANDARD_VACCINATION_RULES.map { rule ->
            val isCompleted = completedRuleIds.contains(rule.id)

            // Compute calendar due date for this vaccine
            val dueCal = Calendar.getInstance().apply {
                time = arrivalDate
                add(Calendar.DAY_OF_YEAR, rule.dayMin)
            }
            val dueDateStr = formatDate(dueCal.time)

            val (status, statusLabel) = when {
                isCompleted -> {
                    VaccineDueStatus.COMPLETED to "COMPLETED"
                }
                currentAgeDays > rule.dayMax -> {
                    val daysOverdue = currentAgeDays - rule.dayMax
                    VaccineDueStatus.OVERDUE to "OVERDUE (${daysOverdue}d ago)"
                }
                currentAgeDays >= rule.dayMin && currentAgeDays <= rule.dayMax -> {
                    VaccineDueStatus.DUE_TODAY to "VACCINATION DUE"
                }
                currentAgeDays >= rule.dayMin - 2 && currentAgeDays < rule.dayMin -> {
                    val inDays = rule.dayMin - currentAgeDays
                    VaccineDueStatus.DUE_SOON to "DUE IN $inDays ${if (inDays == 1) "DAY" else "DAYS"}"
                }
                else -> {
                    val inDays = rule.dayMin - currentAgeDays
                    VaccineDueStatus.UPCOMING to "UPCOMING (in ${inDays}d)"
                }
            }

            FlockVaccineScheduleItem(
                ruleId = rule.id,
                vaccineName = rule.vaccineName,
                administrationMethod = rule.administrationMethod,
                targetStageLabel = rule.targetStageLabel,
                scheduledDueDateStr = dueDateStr,
                dueDaysMin = rule.dayMin,
                dueDaysMax = rule.dayMax,
                status = status,
                statusLabel = statusLabel,
                isCompleted = isCompleted,
                notes = rule.description
            )
        }
    }
}
