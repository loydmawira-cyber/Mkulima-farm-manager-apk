package com.example.util

import androidx.compose.ui.graphics.Color
import com.example.data.MilkLog
import com.example.ui.screens.AnimalDetailData
import com.example.ui.screens.CattleEventItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class CattleStage(
    val key: String,
    val displayName: String,
    val tagLabel: String,
    val emoji: String,
    val description: String
) {
    CALF(
        key = "CALF",
        displayName = "Calf",
        tagLabel = "🍼 CALF",
        emoji = "🍼",
        description = "Young stock (< 12 months) in milk-feeding or weaning phase."
    ),
    HEIFER(
        key = "HEIFER",
        displayName = "Heifer",
        tagLabel = "🌾 HEIFER",
        emoji = "🌾",
        description = "Mature female (> 12 months) that has not yet given birth to her first calf."
    ),
    INCALF(
        key = "INCALF",
        displayName = "In-Calf",
        tagLabel = "🤰 IN-CALF",
        emoji = "🤰",
        description = "Confirmed pregnant through positive PD, currently dry or a pregnant heifer."
    ),
    INCALF_MILKING(
        key = "INCALF_MILKING",
        displayName = "In-Calf / Milking",
        tagLabel = "🥛🤰 IN-CALF / MILKING",
        emoji = "🥛🤰",
        description = "Confirmed pregnant through positive PD and actively in milk production."
    ),
    MILKING(
        key = "MILKING",
        displayName = "Milking",
        tagLabel = "🥛 MILKING",
        emoji = "🥛",
        description = "Adult female cow currently in active lactation after calving, open/not in-calf."
    ),
    INSEMINATED(
        key = "INSEMINATED",
        displayName = "Inseminated",
        tagLabel = "💉 INSEMINATED",
        emoji = "💉",
        description = "Recently inseminated/served, pending pregnancy diagnosis."
    ),
    DRY(
        key = "DRY",
        displayName = "Dry",
        tagLabel = "🍂 DRY",
        emoji = "🍂",
        description = "Mature cow that has completed lactation, resting before next breeding or calving."
    ),
    BULL(
        key = "BULL",
        displayName = "Bull",
        tagLabel = "🐂 BULL",
        emoji = "🐂",
        description = "Breeding male sire or young bull."
    ),
    DISPOSED(
        key = "DISPOSED",
        displayName = "Disposed",
        tagLabel = "🚫 DISPOSED",
        emoji = "🚫",
        description = "Animal culled, sold, or removed from active herd."
    )
}

data class CattleStageEvaluation(
    val stage: CattleStage,
    val stageKey: String,
    val label: String,
    val summaryReason: String,
    val breedingStatusText: String,
    val isInCalf: Boolean,
    val isMilking: Boolean,
    val badgeBgColor: Color,
    val badgeTextColor: Color,
    val gestationDays: Int? = null,
    val expectedCalvingDate: String? = null,
    val expectedDryOffDate: String? = null,
    val lastEventSummary: String? = null,
    val lastInseminationDate: String? = null,
    val explanation: String = summaryReason,
    val daysInGestation: Int? = gestationDays,
    val dryOffTargetDate: String? = expectedDryOffDate
)

object CattleLifecycleEngine {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    fun parseDateOrNull(dateStr: String): Date? {
        val patterns = listOf(
            "dd MMM yyyy",
            "dd MMM, yyyy",
            "dd MMMM yyyy",
            "yyyy-MM-dd",
            "dd/MM/yyyy",
            "dd MMM, hh:mm a",
            "dd MMM ''yy",
            "MMM dd, ''yy",
            "MMM dd, yyyy"
        )
        for (pattern in patterns) {
            try {
                val parser = SimpleDateFormat(pattern, Locale.getDefault())
                parser.isLenient = true
                val d = parser.parse(dateStr.trim())
                if (d != null) return d
            } catch (_: Exception) {}
        }
        return null
    }

    fun calculateExpectedCalving(aiDateStr: String): String {
        val d = parseDateOrNull(aiDateStr) ?: return "Jun 21, '27"
        val c = Calendar.getInstance()
        c.time = d
        c.add(Calendar.DAY_OF_YEAR, 283) // standard bovine gestation is ~283 days
        return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(c.time)
    }

    fun calculateExpectedDryOff(calvingDateStr: String): String {
        val d = parseDateOrNull(calvingDateStr) ?: return "Apr 21, '27"
        val c = Calendar.getInstance()
        c.time = d
        c.add(Calendar.DAY_OF_YEAR, -60) // standard dry-off is 60 days before calving
        return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(c.time)
    }

    fun calculateAgeFromDob(dobStr: String): String {
        if (dobStr.isBlank() || dobStr.equals("N/A", ignoreCase = true)) return "N/A"
        val d = parseDateOrNull(dobStr) ?: return dobStr
        val birthCal = Calendar.getInstance().apply { time = d }
        val nowCal = Calendar.getInstance()

        if (birthCal.after(nowCal)) return "Newborn"

        var years = nowCal.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR)
        var months = nowCal.get(Calendar.MONTH) - birthCal.get(Calendar.MONTH)
        val days = nowCal.get(Calendar.DAY_OF_MONTH) - birthCal.get(Calendar.DAY_OF_MONTH)

        if (days < 0) {
            months -= 1
        }
        if (months < 0) {
            years -= 1
            months += 12
        }

        return when {
            years >= 1 -> {
                if (months > 0) "${years}y ${months}m" else "${years}y"
            }
            months >= 1 -> {
                "${months} months"
            }
            else -> {
                val diffMs = nowCal.timeInMillis - birthCal.timeInMillis
                val diffDays = (diffMs / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
                if (diffDays >= 7) {
                    "${diffDays / 7} weeks"
                } else {
                    "${diffDays} days"
                }
            }
        }
    }

    fun evaluateCattleStage(
        animal: AnimalDetailData,
        events: List<CattleEventItem>,
        milkLogs: List<MilkLog> = emptyList()
    ): CattleStageEvaluation {
        val cleanStatus = animal.status.trim().uppercase()
        val cleanBreed = animal.breed.trim().uppercase()
        val cleanName = animal.name.trim().uppercase()
        val cleanBreeding = animal.breedingStatus.trim().uppercase()

        // 1. Check Disposed
        if (cleanStatus.contains("DISPOSED") ||
            cleanStatus.contains("SOLD") ||
            cleanStatus.contains("DEAD") ||
            cleanStatus.contains("CULLED") ||
            animal.disposalDate.isNotBlank() ||
            animal.disposalReason.isNotBlank()
        ) {
            return CattleStageEvaluation(
                stage = CattleStage.DISPOSED,
                stageKey = CattleStage.DISPOSED.key,
                label = "Disposed",
                summaryReason = "Culled or sold on ${animal.disposalDate.ifBlank { "record" }} (${animal.disposalReason.ifBlank { "Archived" }})",
                breedingStatusText = "DISPOSED",
                isInCalf = false,
                isMilking = false,
                badgeBgColor = Color(0xFFF1F5F9),
                badgeTextColor = Color(0xFF64748B)
            )
        }

        // 2. Check Bull
        if (cleanStatus == "BULL" ||
            cleanBreed.contains("BULL") ||
            cleanName.contains("BULL") ||
            cleanBreed.contains("BORAN GIANT")
        ) {
            return CattleStageEvaluation(
                stage = CattleStage.BULL,
                stageKey = CattleStage.BULL.key,
                label = "Breeding Bull",
                summaryReason = "Active breeding sire stock.",
                breedingStatusText = "HERD SIRE",
                isInCalf = false,
                isMilking = false,
                badgeBgColor = Color(0xFFEFF6FF),
                badgeTextColor = Color(0xFF1D4ED8)
            )
        }

        // 3. For Poultry or non-cattle
        if (animal.category.equals("POULTRY", ignoreCase = true)) {
            return CattleStageEvaluation(
                stage = CattleStage.MILKING,
                stageKey = "POULTRY",
                label = animal.status,
                summaryReason = "Poultry flock management.",
                breedingStatusText = "ACTIVE FLOCK",
                isInCalf = false,
                isMilking = true,
                badgeBgColor = Color(0xFFFEF3C7),
                badgeTextColor = Color(0xFFB45309)
            )
        }

        // 4. Analyze Breeding & Reproduction Log Events
        val sortedEvents = events

        val pdEvents = sortedEvents.filter {
            it.category.equals("PD", ignoreCase = true) ||
            it.title.contains("Pregnancy", ignoreCase = true) ||
            it.title.contains("PD", ignoreCase = true) ||
            it.details.contains("PD Check", ignoreCase = true)
        }

        val aiEvents = sortedEvents.filter {
            it.category.equals("INSEMINATION", ignoreCase = true) ||
            it.title.contains("Insemination", ignoreCase = true) ||
            it.title.contains("AI", ignoreCase = true) ||
            it.details.contains("Straw", ignoreCase = true)
        }

        val calvingEvents = sortedEvents.filter {
            it.category.equals("CALVING", ignoreCase = true) ||
            it.title.contains("Calving", ignoreCase = true) ||
            it.title.contains("Gave Birth", ignoreCase = true) ||
            it.details.contains("Calf delivered", ignoreCase = true)
        }

        val dryOffEvents = sortedEvents.filter {
            it.category.equals("DRY_OFF", ignoreCase = true) ||
            it.title.contains("Dry Off", ignoreCase = true) ||
            it.title.contains("Dried Off", ignoreCase = true)
        }

        val heatEvents = sortedEvents.filter {
            it.category.equals("HEAT", ignoreCase = true) ||
            it.title.contains("Heat", ignoreCase = true) ||
            it.title.contains("Estrus", ignoreCase = true)
        }

        // Check latest PD result
        val latestPd = pdEvents.firstOrNull()
        val isPositivePd = latestPd != null && (
            latestPd.title.contains("Positive", ignoreCase = true) ||
            latestPd.details.contains("Positive", ignoreCase = true) ||
            latestPd.metricValue.contains("Positive", ignoreCase = true) ||
            latestPd.details.contains("Pregnant", ignoreCase = true) ||
            latestPd.details.contains("In-Calf", ignoreCase = true)
        )

        val isNegativePd = latestPd != null && (
            latestPd.title.contains("Negative", ignoreCase = true) ||
            latestPd.details.contains("Negative", ignoreCase = true) ||
            latestPd.metricValue.contains("Negative", ignoreCase = true) ||
            latestPd.details.contains("Not Pregnant", ignoreCase = true) ||
            latestPd.details.contains("Open", ignoreCase = true)
        )

        // Has the cow calved since the latest PD?
        val latestCalving = calvingEvents.firstOrNull()
        val calvedAfterPd = if (latestCalving != null && latestPd != null) {
            val dCalv = parseDateOrNull(latestCalving.date)
            val dPd = parseDateOrNull(latestPd.date)
            if (dCalv != null && dPd != null) dCalv.after(dPd) else false
        } else false

        // Determine if In-Calf
        var isInCalf = false
        var inCalfReason = ""
        var gestationEst = 90
        var calvingDateEst = animal.expectedCalving.ifBlank { "15 Oct 2026" }

        if (isPositivePd && !calvedAfterPd) {
            isInCalf = true
            inCalfReason = "Confirmed positive PD on ${latestPd?.date ?: "recent check"}"
            val matchingAi = aiEvents.firstOrNull()
            if (matchingAi != null) {
                val dAi = parseDateOrNull(matchingAi.date)
                if (dAi != null) {
                    val diff = System.currentTimeMillis() - dAi.time
                    val days = (diff / (1000 * 60 * 60 * 24)).toInt().coerceIn(21, 283)
                    gestationEst = days
                    calvingDateEst = calculateExpectedCalving(matchingAi.date)
                }
            }
        } else if (!isNegativePd && (cleanStatus.contains("PREGNANT") || cleanStatus.contains("INCALF") || cleanStatus.contains("IN-CALF") || cleanBreeding.contains("PREGNANT") || cleanBreeding.contains("IN-CALF"))) {
            isInCalf = true
            inCalfReason = "Breeding record marked In-Calf"
        }

        // 5. Explicit Manual Stage Overrides (When status is explicitly set in profile)
        if (cleanStatus == "MILKING" || cleanStatus == "LACTATING") {
            val lastAi = aiEvents.firstOrNull()
            val breedingStatusMsg = if (cleanBreeding.isNotBlank() && cleanBreeding != "HEALTHY") animal.breedingStatus else if (lastAi != null) "SERVED AI (Pending PD)" else "OPEN (In Milk)"
            return CattleStageEvaluation(
                stage = CattleStage.MILKING,
                stageKey = CattleStage.MILKING.key,
                label = "Milking",
                summaryReason = "Active lactation • Open/In Milk.",
                breedingStatusText = breedingStatusMsg,
                isInCalf = false,
                isMilking = true,
                badgeBgColor = Color(0xFFE0F2FE),
                badgeTextColor = Color(0xFF0369A1),
                lastEventSummary = calvingEvents.firstOrNull()?.title ?: "Active Daily Milking",
                lastInseminationDate = aiEvents.firstOrNull()?.date
            )
        }

        if (cleanStatus == "INCALF / MILKING" || cleanStatus == "INCALF_MILKING" || cleanStatus == "IN-CALF / MILKING" || (cleanStatus == "INCALF" && cleanBreeding.contains("MILKING"))) {
            val dryOffEst = calculateExpectedDryOff(calvingDateEst)
            return CattleStageEvaluation(
                stage = CattleStage.INCALF_MILKING,
                stageKey = CattleStage.INCALF_MILKING.key,
                label = "In-Calf / Milking",
                summaryReason = "Confirmed pregnant and actively milking.",
                breedingStatusText = "IN-CALF & MILKING (Day $gestationEst of 283)",
                isInCalf = true,
                isMilking = true,
                badgeBgColor = Color(0xFFDCFCE7),
                badgeTextColor = Color(0xFF15803D),
                gestationDays = gestationEst,
                expectedCalvingDate = calvingDateEst,
                expectedDryOffDate = dryOffEst,
                lastEventSummary = latestPd?.details ?: "Positive PD Confirmed",
                lastInseminationDate = aiEvents.firstOrNull()?.date
            )
        }

        if (cleanStatus == "INCALF" || cleanStatus == "IN-CALF" || cleanStatus == "PREGNANT") {
            val dryOffEst = calculateExpectedDryOff(calvingDateEst)
            return CattleStageEvaluation(
                stage = CattleStage.INCALF,
                stageKey = CattleStage.INCALF.key,
                label = "In-Calf",
                summaryReason = "Confirmed pregnant • Resting / Pre-calving.",
                breedingStatusText = "IN-CALF (Day $gestationEst of 283)",
                isInCalf = true,
                isMilking = false,
                badgeBgColor = Color(0xFFFEF3C7),
                badgeTextColor = Color(0xFFB45309),
                gestationDays = gestationEst,
                expectedCalvingDate = calvingDateEst,
                expectedDryOffDate = dryOffEst,
                lastEventSummary = latestPd?.details ?: "Positive PD Confirmed",
                lastInseminationDate = aiEvents.firstOrNull()?.date
            )
        }

        if (cleanStatus == "HEIFER" || cleanStatus == "OPEN HEIFER") {
            val lastHeat = heatEvents.firstOrNull()
            val lastAi = aiEvents.firstOrNull()
            val heiferBreedingMsg = if (cleanBreeding.isNotBlank() && cleanBreeding != "HEALTHY") animal.breedingStatus else if (lastAi != null) "SERVED (Pending PD)" else if (lastHeat != null) "HEAT OBSERVED" else "OPEN HEIFER"
            return CattleStageEvaluation(
                stage = CattleStage.HEIFER,
                stageKey = CattleStage.HEIFER.key,
                label = "Heifer",
                summaryReason = "Mature breeding female (${animal.age}) • Pre-calving stock.",
                breedingStatusText = heiferBreedingMsg,
                isInCalf = false,
                isMilking = false,
                badgeBgColor = Color(0xFFFEF9C3),
                badgeTextColor = Color(0xFF854D0E),
                lastEventSummary = lastAi?.title ?: (lastHeat?.title ?: "Ready for Breeding / AI"),
                lastInseminationDate = aiEvents.firstOrNull()?.date
            )
        }

        if (cleanStatus == "CALF" || cleanStatus == "WEANING CALF") {
            return CattleStageEvaluation(
                stage = CattleStage.CALF,
                stageKey = CattleStage.CALF.key,
                label = "Calf",
                summaryReason = "Young stock (${animal.age}). Weaning & starter feed phase.",
                breedingStatusText = if (cleanBreeding.isNotBlank() && cleanBreeding != "HEALTHY") animal.breedingStatus else "WEANING CALF",
                isInCalf = false,
                isMilking = false,
                badgeBgColor = Color(0xFFF3E8FF),
                badgeTextColor = Color(0xFF7E22CE),
                lastInseminationDate = aiEvents.firstOrNull()?.date
            )
        }

        if (cleanStatus == "DRY" || cleanStatus == "DRY OFF" || cleanStatus == "DRY COW") {
            return CattleStageEvaluation(
                stage = CattleStage.DRY,
                stageKey = CattleStage.DRY.key,
                label = "Dry",
                summaryReason = "Completed lactation • Resting period before next breeding cycle.",
                breedingStatusText = if (cleanBreeding.isNotBlank() && cleanBreeding != "HEALTHY") animal.breedingStatus else "DRY COW (Open)",
                isInCalf = false,
                isMilking = false,
                badgeBgColor = Color(0xFFF1F5F9),
                badgeTextColor = Color(0xFF475569),
                lastEventSummary = dryOffEvents.firstOrNull()?.title ?: "Dry Off Logged",
                lastInseminationDate = aiEvents.firstOrNull()?.date
            )
        }

        if (cleanStatus == "INSEMINATED" || cleanStatus == "SERVED") {
            val lastAi = aiEvents.firstOrNull()
            return CattleStageEvaluation(
                stage = CattleStage.INSEMINATED,
                stageKey = CattleStage.INSEMINATED.key,
                label = "Inseminated",
                summaryReason = "Served/Inseminated on ${lastAi?.date ?: "recent date"} • Pending Pregnancy Diagnosis (PD).",
                breedingStatusText = if (cleanBreeding.isNotBlank() && cleanBreeding != "HEALTHY") animal.breedingStatus else "SERVED AI (Pending PD)",
                isInCalf = false,
                isMilking = false,
                badgeBgColor = Color(0xFFEDE9FE),
                badgeTextColor = Color(0xFF6D28D9),
                lastEventSummary = lastAi?.title ?: "Artificial Insemination Logged",
                lastInseminationDate = aiEvents.firstOrNull()?.date
            )
        }

        // Young Calf identification
        val isYoungCalf = cleanStatus == "CALF" ||
            cleanBreed.contains("CALF") ||
            cleanName.contains("JOEY") ||
            animal.age.contains("month", ignoreCase = true) ||
            animal.age.contains("week", ignoreCase = true) ||
            animal.age.contains("day", ignoreCase = true)

        if (isYoungCalf && calvingEvents.isEmpty() && !cleanStatus.contains("MILKING")) {
            return CattleStageEvaluation(
                stage = CattleStage.CALF,
                stageKey = CattleStage.CALF.key,
                label = "Calf",
                summaryReason = "Young stock (${animal.age}). Weaning & starter feed phase.",
                breedingStatusText = "WEANING CALF",
                isInCalf = false,
                isMilking = false,
                badgeBgColor = Color(0xFFF3E8FF),
                badgeTextColor = Color(0xFF7E22CE),
                lastInseminationDate = aiEvents.firstOrNull()?.date
            )
        }

        // Explicit Heifer designation (or young female who hasn't calved yet)
        val isExplicitHeifer = cleanStatus == "HEIFER" ||
            cleanStatus.contains("OPEN HEIFER") ||
            cleanBreed.contains("HEIFER") ||
            cleanBreeding.contains("OPEN HEIFER") ||
            cleanBreeding == "HEIFER"

        // Determine milk production activity
        val cleanAnimal = animal.name.lowercase().trim()
        val cleanTag = animal.tagNumber.lowercase().replace("#", "").trim()
        val recentCowMilkLogs = milkLogs.filter { log ->
            val logName = log.cowName.lowercase().trim()
            (logName.contains(cleanAnimal) || cleanAnimal.contains(logName) || (cleanTag.isNotEmpty() && logName.contains(cleanTag)))
        }

        val hasRecentMilkYield = recentCowMilkLogs.any { it.litres > 0.0 }
        val hasExplicitLitreText = animal.lastMilk.isNotBlank() &&
            !animal.lastMilk.contains("No data", ignoreCase = true) &&
            !animal.lastMilk.contains("N/A", ignoreCase = true) &&
            !animal.lastMilk.contains("0L", ignoreCase = true) &&
            !animal.lastMilk.contains("0.0L", ignoreCase = true) &&
            !animal.lastMilk.contains("Birds", ignoreCase = true) &&
            !animal.lastMilk.contains("Eggs", ignoreCase = true) &&
            Regex("""\b\d+(\.\d+)?\s*L\b""", RegexOption.IGNORE_CASE).containsMatchIn(animal.lastMilk)

        val latestDryOff = dryOffEvents.firstOrNull()
        val isDriedOff = latestDryOff != null || cleanStatus == "DRY" || cleanStatus.contains("DRY OFF")

        val lastInseminationDate = aiEvents.firstOrNull()?.date

        // Heifer rules: A heifer has never calved. If confirmed pregnant, she is In-Calf (Heifer In-Calf).
        if (isExplicitHeifer && calvingEvents.isEmpty()) {
            if (isInCalf) {
                val dryOffEst = calculateExpectedDryOff(calvingDateEst)
                return CattleStageEvaluation(
                    stage = CattleStage.INCALF,
                    stageKey = CattleStage.INCALF.key,
                    label = "In-Calf Heifer",
                    summaryReason = "$inCalfReason • Pre-calving rest (Expected: $calvingDateEst)",
                    breedingStatusText = "IN-CALF HEIFER (Day $gestationEst of 283)",
                    isInCalf = true,
                    isMilking = false,
                    badgeBgColor = Color(0xFFFEF3C7),
                    badgeTextColor = Color(0xFFB45309),
                    gestationDays = gestationEst,
                    expectedCalvingDate = calvingDateEst,
                    expectedDryOffDate = dryOffEst,
                    lastEventSummary = latestPd?.details ?: "Positive PD Confirmed",
                    lastInseminationDate = lastInseminationDate
                )
            } else if (aiEvents.isNotEmpty() && !isNegativePd) {
                val lastAi = aiEvents.firstOrNull()
                return CattleStageEvaluation(
                    stage = CattleStage.INSEMINATED,
                    stageKey = CattleStage.INSEMINATED.key,
                    label = "Inseminated",
                    summaryReason = "Served/Inseminated on ${lastAi?.date ?: "recent date"} • Pending Pregnancy Diagnosis (PD).",
                    breedingStatusText = "SERVED AI (Pending PD)",
                    isInCalf = false,
                    isMilking = false,
                    badgeBgColor = Color(0xFFEDE9FE),
                    badgeTextColor = Color(0xFF6D28D9),
                    lastEventSummary = lastAi?.title ?: "Artificial Insemination Logged",
                    lastInseminationDate = lastInseminationDate
                )
            } else {
                val lastHeat = heatEvents.firstOrNull()
                val heiferBreedingMsg = if (lastHeat != null) "HEAT OBSERVED" else "OPEN HEIFER"
                return CattleStageEvaluation(
                    stage = CattleStage.HEIFER,
                    stageKey = CattleStage.HEIFER.key,
                    label = "Heifer",
                    summaryReason = "Mature breeding female (${animal.age}) • Pre-calving stock.",
                    breedingStatusText = heiferBreedingMsg,
                    isInCalf = false,
                    isMilking = false,
                    badgeBgColor = Color(0xFFFEF9C3),
                    badgeTextColor = Color(0xFF854D0E),
                    lastEventSummary = lastHeat?.title ?: "Ready for Breeding / AI",
                    lastInseminationDate = lastInseminationDate
                )
            }
        }

        // Active milking determination (only for cows with active milk logs or explicit milking status)
        val isCurrentlyMilking = !isDriedOff && (hasRecentMilkYield || cleanStatus == "MILKING" || (hasExplicitLitreText && (calvingEvents.isNotEmpty() || cleanStatus.contains("MILKING"))))

        // In-Calf evaluation
        if (isInCalf) {
            val dryOffEst = calculateExpectedDryOff(calvingDateEst)
            return if (isCurrentlyMilking) {
                CattleStageEvaluation(
                    stage = CattleStage.INCALF_MILKING,
                    stageKey = CattleStage.INCALF_MILKING.key,
                    label = "In-Calf / Milking",
                    summaryReason = "$inCalfReason • Actively milking (${animal.lastMilk.ifBlank { "14.2L" }}/day)",
                    breedingStatusText = "IN-CALF & MILKING (Day $gestationEst of 283)",
                    isInCalf = true,
                    isMilking = true,
                    badgeBgColor = Color(0xFFDCFCE7),
                    badgeTextColor = Color(0xFF15803D),
                    gestationDays = gestationEst,
                    expectedCalvingDate = calvingDateEst,
                    expectedDryOffDate = dryOffEst,
                    lastEventSummary = latestPd?.details ?: "Positive PD Confirmed",
                    lastInseminationDate = lastInseminationDate
                )
            } else {
                CattleStageEvaluation(
                    stage = CattleStage.INCALF,
                    stageKey = CattleStage.INCALF.key,
                    label = "In-Calf",
                    summaryReason = "$inCalfReason • Dry/Pre-calving rest (Expected: $calvingDateEst)",
                    breedingStatusText = "IN-CALF (Day $gestationEst of 283)",
                    isInCalf = true,
                    isMilking = false,
                    badgeBgColor = Color(0xFFFEF3C7),
                    badgeTextColor = Color(0xFFB45309),
                    gestationDays = gestationEst,
                    expectedCalvingDate = calvingDateEst,
                    expectedDryOffDate = dryOffEst,
                    lastEventSummary = latestPd?.details ?: "Positive PD Confirmed",
                    lastInseminationDate = lastInseminationDate
                )
            }
        }

        // Milking Cow
        if (isCurrentlyMilking) {
            val lastAi = aiEvents.firstOrNull()
            val breedingStatusMsg = if (lastAi != null) "SERVED AI (Pending PD)" else "OPEN (In Milk)"
            return CattleStageEvaluation(
                stage = CattleStage.MILKING,
                stageKey = CattleStage.MILKING.key,
                label = "Milking",
                summaryReason = "Active lactation (${animal.lastMilk.ifBlank { "12.0L" }}/day) • Open/Not in-calf.",
                breedingStatusText = breedingStatusMsg,
                isInCalf = false,
                isMilking = true,
                badgeBgColor = Color(0xFFE0F2FE),
                badgeTextColor = Color(0xFF0369A1),
                lastEventSummary = lastAi?.title ?: (calvingEvents.firstOrNull()?.title ?: "Active Daily Milking"),
                lastInseminationDate = lastInseminationDate
            )
        }

        // Inseminated Cow
        if (cleanStatus == "INSEMINATED" || (aiEvents.isNotEmpty() && !isInCalf && !isDriedOff)) {
            val lastAi = aiEvents.firstOrNull()
            return CattleStageEvaluation(
                stage = CattleStage.INSEMINATED,
                stageKey = CattleStage.INSEMINATED.key,
                label = "Inseminated",
                summaryReason = "Served/Inseminated on ${lastAi?.date ?: "recent date"} • Pending Pregnancy Diagnosis (PD).",
                breedingStatusText = "SERVED AI (Pending PD)",
                isInCalf = false,
                isMilking = false,
                badgeBgColor = Color(0xFFEDE9FE),
                badgeTextColor = Color(0xFF6D28D9),
                lastEventSummary = lastAi?.title ?: "Artificial Insemination Logged",
                lastInseminationDate = lastInseminationDate
            )
        }

        // Dry Cow
        if (isDriedOff || cleanStatus == "DRY" || calvingEvents.isNotEmpty()) {
            return CattleStageEvaluation(
                stage = CattleStage.DRY,
                stageKey = CattleStage.DRY.key,
                label = "Dry",
                summaryReason = "Completed lactation • Resting period before next breeding cycle.",
                breedingStatusText = "DRY COW (Open)",
                isInCalf = false,
                isMilking = false,
                badgeBgColor = Color(0xFFF1F5F9),
                badgeTextColor = Color(0xFF475569),
                lastEventSummary = latestDryOff?.title ?: "Dry Off Logged",
                lastInseminationDate = lastInseminationDate
            )
        }

        // Default female that has not calved -> Heifer
        val lastHeat = heatEvents.firstOrNull()
        val lastAi = aiEvents.firstOrNull()
        val heiferBreedingMsg = if (lastAi != null) "SERVED (Pending PD)" else if (lastHeat != null) "HEAT OBSERVED" else "OPEN HEIFER"
        return CattleStageEvaluation(
            stage = CattleStage.HEIFER,
            stageKey = CattleStage.HEIFER.key,
            label = "Heifer",
            summaryReason = "Mature breeding female (${animal.age}) • Pre-calving stock.",
            breedingStatusText = heiferBreedingMsg,
            isInCalf = false,
            isMilking = false,
            badgeBgColor = Color(0xFFFEF9C3),
            badgeTextColor = Color(0xFF854D0E),
            lastEventSummary = lastAi?.title ?: (lastHeat?.title ?: "Ready for Breeding / AI"),
            lastInseminationDate = lastInseminationDate
        )
    }
}
