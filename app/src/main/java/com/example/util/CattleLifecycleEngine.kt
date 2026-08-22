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
    val lastCalvingDate: String? = null,
    val hasGivenBirthPreviously: Boolean = false,
    val parityCount: Int = 0,
    val daysInMilk: Int? = null,
    val isDriedOff: Boolean = false,
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
        val d = parseDateOrNull(aiDateStr) ?: return ""
        val c = Calendar.getInstance()
        c.time = d
        c.add(Calendar.DAY_OF_YEAR, 283) // standard bovine gestation is ~283 days
        return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(c.time)
    }

    fun calculateExpectedDryOff(calvingDateStr: String): String {
        val d = parseDateOrNull(calvingDateStr) ?: return ""
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
        if (animal.category.equals("POULTRY", ignoreCase = true) || animal.category.equals("FLOCK", ignoreCase = true)) {
            return CattleStageEvaluation(
                stage = CattleStage.MILKING,
                stageKey = "POULTRY",
                label = animal.status.ifBlank { "Active Flock" },
                summaryReason = "Poultry flock management.",
                breedingStatusText = "ACTIVE FLOCK",
                isInCalf = false,
                isMilking = true,
                badgeBgColor = Color(0xFFFEF3C7),
                badgeTextColor = Color(0xFFB45309)
            )
        }

        // 4. Sort and analyze Reproduction Log Events newest first
        val sortedEvents = events.sortedByDescending { parseDateOrNull(it.date) ?: Date(0) }

        val calvingEvents = sortedEvents.filter {
            it.category.equals("CALVING", ignoreCase = true) ||
            it.title.contains("Calving", ignoreCase = true) ||
            it.title.contains("Gave Birth", ignoreCase = true) ||
            it.title.contains("Delivered", ignoreCase = true) ||
            it.details.contains("Calf delivered", ignoreCase = true) ||
            it.details.contains("Calved", ignoreCase = true)
        }

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
            it.details.contains("Straw", ignoreCase = true) ||
            it.details.contains("Semen", ignoreCase = true) ||
            it.details.contains("Inseminated", ignoreCase = true) ||
            it.title.contains("Mating", ignoreCase = true)
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

        // Check Calving History
        val latestCalving = calvingEvents.firstOrNull()
        val latestCalvingDate = latestCalving?.date?.let { parseDateOrNull(it) }
        val parityCount = calvingEvents.size
        val hasGivenBirthPreviously = calvingEvents.isNotEmpty()

        // Days in Milk (DIM)
        val todayCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val daysInMilk = if (latestCalvingDate != null) {
            val calvCal = Calendar.getInstance().apply {
                time = latestCalvingDate
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val diffMs = todayCal.timeInMillis - calvCal.timeInMillis
            (diffMs / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
        } else null

        // Segregate events belonging to the CURRENT lactation/gestation cycle
        // Any AI / PD event before the latest calving event belongs to the completed pregnancy!
        val currentCycleAiEvents = if (latestCalvingDate != null) {
            aiEvents.filter { ev ->
                val d = parseDateOrNull(ev.date)
                d != null && d.after(latestCalvingDate)
            }
        } else {
            aiEvents
        }

        val currentCyclePdEvents = if (latestCalvingDate != null) {
            pdEvents.filter { ev ->
                val d = parseDateOrNull(ev.date)
                d != null && d.after(latestCalvingDate)
            }
        } else {
            pdEvents
        }

        val currentCycleDryOffEvents = if (latestCalvingDate != null) {
            dryOffEvents.filter { ev ->
                val d = parseDateOrNull(ev.date)
                d != null && d.after(latestCalvingDate)
            }
        } else {
            dryOffEvents
        }

        val currentCycleHeatEvents = if (latestCalvingDate != null) {
            heatEvents.filter { ev ->
                val d = parseDateOrNull(ev.date)
                d != null && d.after(latestCalvingDate)
            }
        } else {
            heatEvents
        }

        // Current cycle latest AI
        val latestCurrentAi = currentCycleAiEvents.firstOrNull()
        val latestCurrentAiDate = latestCurrentAi?.date?.let { parseDateOrNull(it) }

        // Current cycle latest PD
        val latestCurrentPd = currentCyclePdEvents.firstOrNull()
        val isPositivePd = latestCurrentPd != null && (
            latestCurrentPd.title.contains("Positive", ignoreCase = true) ||
            latestCurrentPd.details.contains("Positive", ignoreCase = true) ||
            latestCurrentPd.metricValue.contains("Positive", ignoreCase = true) ||
            latestCurrentPd.details.contains("Pregnant", ignoreCase = true) ||
            latestCurrentPd.details.contains("In-Calf", ignoreCase = true)
        )

        val isNegativePd = latestCurrentPd != null && (
            latestCurrentPd.title.contains("Negative", ignoreCase = true) ||
            latestCurrentPd.details.contains("Negative", ignoreCase = true) ||
            latestCurrentPd.metricValue.contains("Negative", ignoreCase = true) ||
            latestCurrentPd.details.contains("Not Pregnant", ignoreCase = true) ||
            latestCurrentPd.details.contains("Open", ignoreCase = true)
        )

        // Check Dry-off status
        val latestCurrentDryOff = currentCycleDryOffEvents.firstOrNull()
        val isExplicitlyDriedOff = latestCurrentDryOff != null ||
            cleanStatus == "DRY" ||
            cleanStatus.contains("DRY OFF") ||
            cleanStatus.contains("DRY COW")

        // In-Calf check in current cycle
        var isInCalf = false
        var gestationEst = 60
        var calvingDateEst: String? = null
        var dryOffTargetDateEst: String? = null

        if (isPositivePd) {
            isInCalf = true
            if (latestCurrentAiDate != null && latestCurrentAi != null) {
                val aiCal = Calendar.getInstance().apply {
                    time = latestCurrentAiDate
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val diff = todayCal.timeInMillis - aiCal.timeInMillis
                gestationEst = (diff / (1000 * 60 * 60 * 24)).toInt().coerceIn(21, 283)
                calvingDateEst = calculateExpectedCalving(latestCurrentAi.date)
                dryOffTargetDateEst = calculateExpectedDryOff(calvingDateEst)
            } else {
                gestationEst = 90
                val pdCal = Calendar.getInstance().apply {
                    latestCurrentPd?.date?.let { parseDateOrNull(it) }?.let { time = it }
                    add(Calendar.DAY_OF_YEAR, 220)
                }
                calvingDateEst = dateFormat.format(pdCal.time)
                dryOffTargetDateEst = calculateExpectedDryOff(calvingDateEst)
            }
        } else if (!isNegativePd && latestCalving == null && (cleanStatus.contains("PREGNANT") || cleanStatus.contains("INCALF") || cleanStatus.contains("IN-CALF") || cleanBreeding.contains("PREGNANT") || cleanBreeding.contains("IN-CALF"))) {
            isInCalf = true
            if (latestCurrentAiDate != null && latestCurrentAi != null) {
                val aiCal = Calendar.getInstance().apply {
                    time = latestCurrentAiDate
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val diff = todayCal.timeInMillis - aiCal.timeInMillis
                gestationEst = (diff / (1000 * 60 * 60 * 24)).toInt().coerceIn(21, 283)
                calvingDateEst = calculateExpectedCalving(latestCurrentAi.date)
                dryOffTargetDateEst = calculateExpectedDryOff(calvingDateEst)
            } else {
                calvingDateEst = animal.expectedCalving.ifBlank {
                    val c = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 120) }
                    dateFormat.format(c.time)
                }
                dryOffTargetDateEst = calculateExpectedDryOff(calvingDateEst)
            }
        }

        // Determine milk activity
        val cleanAnimal = animal.name.lowercase().trim()
        val cleanTag = animal.tagNumber.lowercase().replace("#", "").trim()
        val recentCowMilkLogs = milkLogs.filter { log ->
            val logName = log.cowName.lowercase().trim()
            (logName.contains(cleanAnimal) || cleanAnimal.contains(logName) || (cleanTag.isNotEmpty() && logName.contains(cleanTag)))
        }
        val hasRecentMilkYield = recentCowMilkLogs.any { it.litres > 0.0 }

        val isCurrentlyMilking = !isExplicitlyDriedOff && (
            hasGivenBirthPreviously ||
            hasRecentMilkYield ||
            cleanStatus == "MILKING" ||
            cleanStatus == "LACTATING" ||
            cleanStatus.contains("MILKING")
        )

        // ============================================================
        // LIFECYCLE STAGE EVALUATION (PRIORITIZED)
        // ============================================================

        // CASE 1: IN-CALF & DRIED OFF (Dry In-Calf Cow)
        if (isInCalf && isExplicitlyDriedOff) {
            val calvingStr = calvingDateEst ?: "Expected in ~60 days"
            return CattleStageEvaluation(
                stage = CattleStage.DRY,
                stageKey = CattleStage.DRY.key,
                label = "Dry (In-Calf)",
                summaryReason = "Dried off for pre-calving udder rest • Confirmed pregnant (Expected calving: $calvingStr).",
                breedingStatusText = "IN-CALF (Dry / Day $gestationEst of 283)",
                isInCalf = true,
                isMilking = false,
                badgeBgColor = Color(0xFFFEF3C7),
                badgeTextColor = Color(0xFFB45309),
                gestationDays = gestationEst,
                expectedCalvingDate = calvingDateEst,
                expectedDryOffDate = latestCurrentDryOff?.date ?: dryOffTargetDateEst,
                lastEventSummary = latestCurrentDryOff?.details ?: "Dried off for pre-calving rest",
                lastInseminationDate = latestCurrentAi?.date,
                lastCalvingDate = latestCalving?.date,
                hasGivenBirthPreviously = hasGivenBirthPreviously,
                parityCount = parityCount,
                daysInMilk = daysInMilk,
                isDriedOff = true
            )
        }

        // CASE 2: IN-CALF & MILKING (Pregnant + Lactating)
        if (isInCalf && isCurrentlyMilking) {
            val parityText = if (parityCount > 0) "Parity $parityCount" else "Heifer Calved"
            val dimText = if (daysInMilk != null) " • Day $daysInMilk In Milk" else ""
            return CattleStageEvaluation(
                stage = CattleStage.INCALF_MILKING,
                stageKey = CattleStage.INCALF_MILKING.key,
                label = "In-Calf / Milking",
                summaryReason = "$parityText$dimText • Confirmed pregnant (Day $gestationEst of 283) & actively milking.",
                breedingStatusText = "IN-CALF & MILKING (Day $gestationEst of 283)",
                isInCalf = true,
                isMilking = true,
                badgeBgColor = Color(0xFFDCFCE7),
                badgeTextColor = Color(0xFF15803D),
                gestationDays = gestationEst,
                expectedCalvingDate = calvingDateEst,
                expectedDryOffDate = dryOffTargetDateEst,
                lastEventSummary = latestCurrentPd?.details ?: "Positive PD Confirmed",
                lastInseminationDate = latestCurrentAi?.date,
                lastCalvingDate = latestCalving?.date,
                hasGivenBirthPreviously = hasGivenBirthPreviously,
                parityCount = parityCount,
                daysInMilk = daysInMilk,
                isDriedOff = false
            )
        }

        // CASE 3: IN-CALF HEIFER (Pregnant Maiden Heifer)
        if (isInCalf && !hasGivenBirthPreviously) {
            return CattleStageEvaluation(
                stage = CattleStage.INCALF,
                stageKey = CattleStage.INCALF.key,
                label = "In-Calf Heifer",
                summaryReason = "Confirmed pregnant maiden heifer (Day $gestationEst of 283) • Expected calving: ${calvingDateEst ?: "N/A"}.",
                breedingStatusText = "IN-CALF HEIFER (Day $gestationEst of 283)",
                isInCalf = true,
                isMilking = false,
                badgeBgColor = Color(0xFFFEF3C7),
                badgeTextColor = Color(0xFFB45309),
                gestationDays = gestationEst,
                expectedCalvingDate = calvingDateEst,
                expectedDryOffDate = dryOffTargetDateEst,
                lastEventSummary = latestCurrentPd?.details ?: "Positive PD Confirmed",
                lastInseminationDate = latestCurrentAi?.date,
                lastCalvingDate = null,
                hasGivenBirthPreviously = false,
                parityCount = 0,
                daysInMilk = null,
                isDriedOff = false
            )
        }

        // CASE 4: SERVED AI IN CURRENT CYCLE (Pending PD)
        if (latestCurrentAi != null && !isNegativePd) {
            val serviceDate = latestCurrentAi.date
            val estCalvingIfConceived = calculateExpectedCalving(serviceDate)
            return if (isCurrentlyMilking) {
                CattleStageEvaluation(
                    stage = CattleStage.MILKING,
                    stageKey = CattleStage.MILKING.key,
                    label = "Milking (Served AI)",
                    summaryReason = "Served on $serviceDate (${latestCurrentAi.details.ifBlank { "Straw / AI" }}) • Pending 60-day Pregnancy Diagnosis.",
                    breedingStatusText = "SERVED AI (Pending PD)",
                    isInCalf = false,
                    isMilking = true,
                    badgeBgColor = Color(0xFFEDE9FE),
                    badgeTextColor = Color(0xFF6D28D9),
                    gestationDays = null,
                    expectedCalvingDate = estCalvingIfConceived.ifBlank { null },
                    expectedDryOffDate = null,
                    lastEventSummary = latestCurrentAi.title.ifBlank { "Artificial Insemination Logged" },
                    lastInseminationDate = serviceDate,
                    lastCalvingDate = latestCalving?.date,
                    hasGivenBirthPreviously = hasGivenBirthPreviously,
                    parityCount = parityCount,
                    daysInMilk = daysInMilk,
                    isDriedOff = false
                )
            } else {
                CattleStageEvaluation(
                    stage = CattleStage.INSEMINATED,
                    stageKey = CattleStage.INSEMINATED.key,
                    label = "Inseminated",
                    summaryReason = "Served on $serviceDate • Pending 60-day Pregnancy Diagnosis.",
                    breedingStatusText = "SERVED AI (Pending PD)",
                    isInCalf = false,
                    isMilking = false,
                    badgeBgColor = Color(0xFFEDE9FE),
                    badgeTextColor = Color(0xFF6D28D9),
                    gestationDays = null,
                    expectedCalvingDate = estCalvingIfConceived.ifBlank { null },
                    expectedDryOffDate = null,
                    lastEventSummary = latestCurrentAi.title.ifBlank { "Artificial Insemination Logged" },
                    lastInseminationDate = serviceDate,
                    lastCalvingDate = latestCalving?.date,
                    hasGivenBirthPreviously = hasGivenBirthPreviously,
                    parityCount = parityCount,
                    daysInMilk = null,
                    isDriedOff = isExplicitlyDriedOff
                )
            }
        }

        // CASE 5: CALVED & ACTIVE LACTATION (Open in Milk)
        // A cow that has given birth, is currently milking, and has not yet been served in this cycle
        if (hasGivenBirthPreviously && isCurrentlyMilking) {
            val calvDateStr = latestCalving?.date ?: "recent date"
            val dimStr = if (daysInMilk != null) " • Day $daysInMilk In Milk" else ""
            val parityStr = "Parity $parityCount"
            return CattleStageEvaluation(
                stage = CattleStage.MILKING,
                stageKey = CattleStage.MILKING.key,
                label = "Milking",
                summaryReason = "Calved on $calvDateStr ($parityStr$dimStr) • Open / Active Lactation.",
                breedingStatusText = "OPEN (In Milk / Lactating)",
                isInCalf = false,
                isMilking = true,
                badgeBgColor = Color(0xFFE0F2FE),
                badgeTextColor = Color(0xFF0369A1),
                gestationDays = null,
                expectedCalvingDate = null,
                expectedDryOffDate = null,
                lastEventSummary = latestCalving?.title ?: "Calving & Calf Delivery Logged",
                lastInseminationDate = null, // No AI in current lactation cycle
                lastCalvingDate = latestCalving?.date,
                hasGivenBirthPreviously = true,
                parityCount = parityCount,
                daysInMilk = daysInMilk,
                isDriedOff = false
            )
        }

        // CASE 6: DRY COW (Open / Resting)
        // Completed lactation, dried off, not pregnant
        if (isExplicitlyDriedOff) {
            val dryDateStr = latestCurrentDryOff?.date ?: "Record"
            return CattleStageEvaluation(
                stage = CattleStage.DRY,
                stageKey = CattleStage.DRY.key,
                label = "Dry",
                summaryReason = "Completed lactation • Resting period before next breeding cycle.",
                breedingStatusText = "DRY COW (Open / Resting)",
                isInCalf = false,
                isMilking = false,
                badgeBgColor = Color(0xFFF1F5F9),
                badgeTextColor = Color(0xFF475569),
                gestationDays = null,
                expectedCalvingDate = null,
                expectedDryOffDate = latestCurrentDryOff?.date,
                lastEventSummary = latestCurrentDryOff?.details ?: "Dry Off Logged on $dryDateStr",
                lastInseminationDate = null,
                lastCalvingDate = latestCalving?.date,
                hasGivenBirthPreviously = hasGivenBirthPreviously,
                parityCount = parityCount,
                daysInMilk = null,
                isDriedOff = true
            )
        }

        // CASE 7: EXPLICIT MILKING STATUS (Open cow)
        if (cleanStatus == "MILKING" || cleanStatus == "LACTATING") {
            return CattleStageEvaluation(
                stage = CattleStage.MILKING,
                stageKey = CattleStage.MILKING.key,
                label = "Milking",
                summaryReason = "Active lactation • Open/In Milk.",
                breedingStatusText = "OPEN (In Milk / Lactating)",
                isInCalf = false,
                isMilking = true,
                badgeBgColor = Color(0xFFE0F2FE),
                badgeTextColor = Color(0xFF0369A1),
                gestationDays = null,
                expectedCalvingDate = null,
                expectedDryOffDate = null,
                lastEventSummary = latestCalving?.title ?: "Active Daily Milking",
                lastInseminationDate = null,
                lastCalvingDate = latestCalving?.date,
                hasGivenBirthPreviously = hasGivenBirthPreviously,
                parityCount = parityCount,
                daysInMilk = daysInMilk,
                isDriedOff = false
            )
        }

        // CASE 8: YOUNG CALF
        val isYoungCalf = cleanStatus == "CALF" ||
            cleanBreed.contains("CALF") ||
            animal.age.contains("month", ignoreCase = true) ||
            animal.age.contains("week", ignoreCase = true) ||
            animal.age.contains("day", ignoreCase = true)

        if (isYoungCalf && !hasGivenBirthPreviously && !cleanStatus.contains("MILKING")) {
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
                gestationDays = null,
                expectedCalvingDate = null,
                expectedDryOffDate = null,
                lastEventSummary = "Young stock on starter feed",
                lastInseminationDate = null,
                lastCalvingDate = null,
                hasGivenBirthPreviously = false,
                parityCount = 0,
                daysInMilk = null,
                isDriedOff = false
            )
        }

        // CASE 9: HEIFER (Open Maiden)
        val lastHeat = currentCycleHeatEvents.firstOrNull()
        val heiferBreedingMsg = if (lastHeat != null) "HEAT OBSERVED" else "OPEN HEIFER"
        return CattleStageEvaluation(
            stage = CattleStage.HEIFER,
            stageKey = CattleStage.HEIFER.key,
            label = "Heifer",
            summaryReason = "Mature breeding maiden (${animal.age}) • Pre-calving stock.",
            breedingStatusText = heiferBreedingMsg,
            isInCalf = false,
            isMilking = false,
            badgeBgColor = Color(0xFFFEF9C3),
            badgeTextColor = Color(0xFF854D0E),
            gestationDays = null,
            expectedCalvingDate = null,
            expectedDryOffDate = null,
            lastEventSummary = lastHeat?.title ?: "Ready for Breeding / AI",
            lastInseminationDate = null,
            lastCalvingDate = null,
            hasGivenBirthPreviously = false,
            parityCount = 0,
            daysInMilk = null,
            isDriedOff = false
        )
    }
}
