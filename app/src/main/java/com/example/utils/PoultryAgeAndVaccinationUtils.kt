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

enum class PoultryHealthLevel {
    HEALTHY,
    CAUTION,
    CRITICAL
}

enum class PoultryProductionLevel {
    EXCELLENT,
    NORMAL,
    LOW_WARNING,
    PRE_LAY
}

data class PoultryAutomatedAlert(
    val title: String,
    val message: String,
    val isCritical: Boolean,
    val recommendation: String
)

data class PoultryAutomatedStatus(
    val healthLevel: PoultryHealthLevel,
    val healthBadgeLabel: String,
    val healthSummary: String,
    val healthAdvice: String,
    val productionLevel: PoultryProductionLevel,
    val productionBadgeLabel: String,
    val layRatePercent: Double,
    val productionSummary: String,
    val productionAdvice: String,
    val weeklyMortalityPercent: Double,
    val automatedAlerts: List<PoultryAutomatedAlert>
)

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

    /**
     * Resolves a standard vaccine rule ID (e.g. "vac_day_14") from arbitrary text (title, notes, syncId, ruleKey).
     */
    fun matchVaccineRuleId(vararg texts: String?): String? {
        val combined = texts.filterNotNull().joinToString(" ").lowercase(Locale.ROOT)
        if (combined.isBlank()) return null

        if (combined.contains("vac_day_0") || combined.contains("marek")) return "vac_day_0"
        if (combined.contains("vac_day_7") || combined.contains("nd1") ||
            (combined.contains("newcastle") && (combined.contains("day 7") || combined.contains("1 week") || combined.contains("first")))) return "vac_day_7"
        if (combined.contains("vac_day_14") || combined.contains("ibd 1") || combined.contains("ibd1") ||
            (combined.contains("gumboro") && (combined.contains("first") || combined.contains("day 14") || combined.contains("2 week") || (!combined.contains("2") && !combined.contains("booster"))))) return "vac_day_14"
        if (combined.contains("vac_day_21") || combined.contains("nd2") || combined.contains("lasota") ||
            (combined.contains("newcastle") && (combined.contains("day 21") || combined.contains("3 week") || combined.contains("second") || combined.contains("booster")))) return "vac_day_21"
        if (combined.contains("vac_day_28") || combined.contains("ibd 2") || combined.contains("ibd2") ||
            (combined.contains("gumboro") && (combined.contains("2") || combined.contains("booster") || combined.contains("day 28") || combined.contains("4 week")))) return "vac_day_28"
        if (combined.contains("vac_day_42") || combined.contains("nd3") ||
            (combined.contains("newcastle") && (combined.contains("day 42") || combined.contains("6 week") || combined.contains("third")))) return "vac_day_42"
        if (combined.contains("vac_day_56_70") || combined.contains("fowl pox") || combined.contains("fowlpox") || combined.contains("wing web")) return "vac_day_56_70"
        if (combined.contains("vac_day_112_126") || combined.contains("nd4") || combined.contains("pre-laying") || combined.contains("pre laying")) return "vac_day_112_126"

        return null
    }

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
     * - Week 1 - 8 (0 - 56 days): Starter Feed
     * - Week 9 - 18 (57 - 126 days): Grower Feed
     * - 18+ Weeks (> 126 days): Layer / Finisher Feed
     */
    fun getFlockFeedStage(totalDays: Int): PoultryFeedStageInfo {
        return when {
            totalDays <= 56 -> {
                // Last week of Starter feeds is Week 8 (Day 50 to 56)
                val isLastWeekOfStarter = totalDays in 50..56
                val daysUntilTransition = 57 - totalDays
                val alertMsg = if (isLastWeekOfStarter) {
                    if (daysUntilTransition <= 1) "⚠️ Final Starter Day: Start introducing growers feed gradually!"
                    else "⚠️ Week 8 Alert: Start introducing growers feed gradually ($daysUntilTransition days until Week 9 Grower stage)!"
                } else null

                PoultryFeedStageInfo(
                    stageName = "Starter Feed (Week 1 - 8)",
                    feedType = "Chick Starter Mash / Crumbs (20–22% CP)",
                    purpose = "High protein & amino acids for bone, organ, and early immune development",
                    dailyRationPerBird = "~20 – 60g / bird / day",
                    hasTransitionAlert = isLastWeekOfStarter,
                    transitionAlertMessage = alertMsg
                )
            }
            totalDays in 57..126 -> {
                // Last week of Grower feeds is Week 18 (Day 120 to 126)
                val isLastWeekOfGrower = totalDays in 120..126
                val daysUntilTransition = 127 - totalDays
                val alertMsg = if (isLastWeekOfGrower) {
                    if (daysUntilTransition <= 1) "⚠️ Final Grower Day: Start introducing layers feed gradually!"
                    else "⚠️ Week 18 Alert: Start introducing layers feed gradually ($daysUntilTransition days until 18+ Weeks Layer/Finisher stage)!"
                } else null

                PoultryFeedStageInfo(
                    stageName = "Grower Feed (Week 9 - 18)",
                    feedType = "Grower Mash / Pellets (15–17% CP)",
                    purpose = "Balanced steady growth without excessive early fat deposition prior to maturity",
                    dailyRationPerBird = "~65 – 95g / bird / day",
                    hasTransitionAlert = isLastWeekOfGrower,
                    transitionAlertMessage = alertMsg
                )
            }
            else -> {
                PoultryFeedStageInfo(
                    stageName = "Layer / Finisher Feed (18+ Weeks)",
                    feedType = "High-Yield Layer Mash (16–18% CP + 3.8% Ca) / Finisher Pellets",
                    purpose = "High calcium and minerals for peak egg production & maximum shell strength",
                    dailyRationPerBird = "~110 – 135g / bird / day",
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

    /**
     * Automates health & production status diagnosis based on flock age, mortality, egg production, and vaccination compliance.
     */
    fun evaluateAutomatedFlockStatus(
        ageInfo: PoultryAgeInfo,
        activeHeadCount: Int,
        mortalityCountLast7Days: Int,
        totalMortalityCount: Int,
        avgDailyEggTraysLast7Days: Double,
        overdueVaccineCount: Int,
        dueTodayVaccineCount: Int
    ): PoultryAutomatedStatus {
        val totalBirdsStart = (activeHeadCount + totalMortalityCount).coerceAtLeast(1)
        val weeklyMortalityPercent = (mortalityCountLast7Days.toDouble() / totalBirdsStart.toDouble()) * 100.0

        // Calculate Lay Rate % (1 Tray = 30 Eggs)
        val avgDailyEggs = avgDailyEggTraysLast7Days * 30.0
        val layRatePercent = if (activeHeadCount > 0) {
            ((avgDailyEggs / activeHeadCount.toDouble()) * 100.0).coerceIn(0.0, 100.0)
        } else 0.0

        val alerts = mutableListOf<PoultryAutomatedAlert>()

        // 1. Evaluate Health Status
        var healthLevel = PoultryHealthLevel.HEALTHY
        var healthSummary = "Flock health is optimal with low mortality."
        var healthAdvice = "Maintain current sanitation, bio-security, and fresh water supply."

        if (weeklyMortalityPercent > 1.5) {
            healthLevel = PoultryHealthLevel.CRITICAL
            healthSummary = "⚠️ High Mortality Rate Spike (${String.format("%.1f%%", weeklyMortalityPercent)} in past 7 days)"
            healthAdvice = "Isolate sick birds immediately, consult a qualified veterinarian, and review ventilation and feed hygiene."
            alerts.add(
                PoultryAutomatedAlert(
                    title = "Critical Mortality Spike",
                    message = "${mortalityCountLast7Days} deaths recorded in past 7 days (${String.format("%.1f%%", weeklyMortalityPercent)} of flock).",
                    isCritical = true,
                    recommendation = "Contact local vet for necropsy/diagnosis & inspect drinking water supply."
                )
            )
        } else if (weeklyMortalityPercent > 0.5) {
            healthLevel = PoultryHealthLevel.CAUTION
            healthSummary = "⚡ Moderate Mortality Notice (${String.format("%.1f%%", weeklyMortalityPercent)} in past 7 days)"
            healthAdvice = "Monitor flock behavior, check for respiratory symptoms or wet litter, and boost multivitamins in drinking water."
            alerts.add(
                PoultryAutomatedAlert(
                    title = "Elevated Mortality Alert",
                    message = "Weekly mortality is ${String.format("%.1f%%", weeklyMortalityPercent)}.",
                    isCritical = false,
                    recommendation = "Inspect feed freshness, coop temperature, and air ventilation."
                )
            )
        }

        if (overdueVaccineCount > 0) {
            if (healthLevel == PoultryHealthLevel.HEALTHY) {
                healthLevel = PoultryHealthLevel.CAUTION
            }
            alerts.add(
                PoultryAutomatedAlert(
                    title = "Overdue Vaccines Detected",
                    message = "$overdueVaccineCount standard vaccination schedule ${if (overdueVaccineCount == 1) "item is" else "items are"} overdue.",
                    isCritical = overdueVaccineCount > 1,
                    recommendation = "Administer required vaccines immediately to prevent viral outbreak."
                )
            )
        }

        if (dueTodayVaccineCount > 0) {
            alerts.add(
                PoultryAutomatedAlert(
                    title = "Vaccination Scheduled Today",
                    message = "$dueTodayVaccineCount vaccine rule due for administration today.",
                    isCritical = false,
                    recommendation = "Prepare clean drinking water or eye drops according to vaccine guidelines."
                )
            )
        }

        val healthBadgeLabel = when (healthLevel) {
            PoultryHealthLevel.HEALTHY -> "🟢 HEALTHY (${String.format("%.1f%%", weeklyMortalityPercent)} Mort/wk)"
            PoultryHealthLevel.CAUTION -> "🟡 CAUTION (${String.format("%.1f%%", weeklyMortalityPercent)} Mort/wk)"
            PoultryHealthLevel.CRITICAL -> "🔴 CRITICAL ALERT (${String.format("%.1f%%", weeklyMortalityPercent)} Mort/wk)"
        }

        // 2. Evaluate Production Status based on Age Weeks
        val totalWeeks = ageInfo.weeks
        val productionLevel: PoultryProductionLevel
        val productionBadgeLabel: String
        val productionSummary: String
        val productionAdvice: String

        when {
            totalWeeks < 18 -> {
                productionLevel = PoultryProductionLevel.PRE_LAY
                productionBadgeLabel = "🌱 Pre-Lay Growth Stage (Wk $totalWeeks)"
                productionSummary = "Flock is currently in rearing/growth phase before egg onset."
                productionAdvice = "Focus on target weight gains and transition to Grower/Pre-layer mash at Week 17."
            }
            totalWeeks in 18..22 -> {
                productionLevel = if (layRatePercent >= 15.0) PoultryProductionLevel.NORMAL else PoultryProductionLevel.LOW_WARNING
                productionBadgeLabel = "🥚 Point of Lay Onset (${String.format("%.1f%%", layRatePercent)})"
                productionSummary = "Flock is entering point of lay (onset of egg production)."
                productionAdvice = "Ensure Layer Mash with 3.8% calcium is supplied to build strong eggshells."
            }
            totalWeeks in 23..45 -> {
                // Peak Lay Stage (Expected 75% - 92%)
                when {
                    layRatePercent >= 75.0 -> {
                        productionLevel = PoultryProductionLevel.EXCELLENT
                        productionBadgeLabel = "🔥 Peak Lay Yield (${String.format("%.1f%%", layRatePercent)})"
                        productionSummary = "Optimal peak laying performance achieved."
                        productionAdvice = "Maintain consistent feeding times, clean water, and 16 hours of daily lighting."
                    }
                    layRatePercent >= 60.0 -> {
                        productionLevel = PoultryProductionLevel.NORMAL
                        productionBadgeLabel = "🟢 Normal Production (${String.format("%.1f%%", layRatePercent)})"
                        productionSummary = "Laying performance is stable."
                        productionAdvice = "Monitor feed consumption per bird (~120g/day) and egg weight."
                    }
                    else -> {
                        productionLevel = PoultryProductionLevel.LOW_WARNING
                        productionBadgeLabel = "⚠️ Below Target Laying (${String.format("%.1f%%", layRatePercent)})"
                        productionSummary = "Laying percentage is below expected peak benchmark (Target: 75%+)."
                        productionAdvice = "Check for feed nutrient deficiency, external parasites (mites), heat stress, or water disruption."
                        alerts.add(
                            PoultryAutomatedAlert(
                                title = "Low Lay Rate Warning",
                                message = "Current lay rate is ${String.format("%.1f%%", layRatePercent)} (Target for Week $totalWeeks is >75%).",
                                isCritical = false,
                                recommendation = "Inspect feed protein levels, water intake, and light duration."
                            )
                        )
                    }
                }
            }
            else -> {
                // Late Lay Stage (>45 Weeks)
                when {
                    layRatePercent >= 65.0 -> {
                        productionLevel = PoultryProductionLevel.EXCELLENT
                        productionBadgeLabel = "🌟 High Late Lay (${String.format("%.1f%%", layRatePercent)})"
                        productionSummary = "Strong late-cycle egg production."
                        productionAdvice = "Supplement calcium grids or limestone grit for shell thickness."
                    }
                    layRatePercent >= 45.0 -> {
                        productionLevel = PoultryProductionLevel.NORMAL
                        productionBadgeLabel = "🟢 Late Lay Phase (${String.format("%.1f%%", layRatePercent)})"
                        productionSummary = "Gradual post-peak drop in lay rate."
                        productionAdvice = "Plan for eventual culling / spent hen marketing when lay rate drops < 40%."
                    }
                    else -> {
                        productionLevel = PoultryProductionLevel.LOW_WARNING
                        productionBadgeLabel = "📉 Low Production (${String.format("%.1f%%", layRatePercent)})"
                        productionSummary = "Production has dropped below economical threshold."
                        productionAdvice = "Consider molting evaluation or planning spent hen disposal / replacement flock."
                    }
                }
            }
        }

        // Feed Transition Alert check
        if (ageInfo.feedStage.hasTransitionAlert && ageInfo.feedStage.transitionAlertMessage != null) {
            alerts.add(
                PoultryAutomatedAlert(
                    title = "Feed Stage Transition",
                    message = ageInfo.feedStage.transitionAlertMessage,
                    isCritical = false,
                    recommendation = "Gradually mix old and new feed types over 5–7 days to prevent digestive stress."
                )
            )
        }

        return PoultryAutomatedStatus(
            healthLevel = healthLevel,
            healthBadgeLabel = healthBadgeLabel,
            healthSummary = healthSummary,
            healthAdvice = healthAdvice,
            productionLevel = productionLevel,
            productionBadgeLabel = productionBadgeLabel,
            layRatePercent = layRatePercent,
            productionSummary = productionSummary,
            productionAdvice = productionAdvice,
            weeklyMortalityPercent = weeklyMortalityPercent,
            automatedAlerts = alerts
        )
    }
}

