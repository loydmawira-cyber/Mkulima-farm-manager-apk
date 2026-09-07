package com.example.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Shared milk-entry identity and date rules for both the UI and persistence layers. */
object MilkLogEntryRules {
    private val acceptedDateFormats = listOf(
        "dd MMM yyyy", "d MMM yyyy", "yyyy-MM-dd", "dd/MM/yyyy", "dd-MM-yyyy", "dd MMM"
    )

    fun canonicalDateKey(dateText: String): String? {
        val input = dateText.trim()
        if (input.isBlank()) return null
        for (pattern in acceptedDateFormats) {
            val parser = SimpleDateFormat(pattern, Locale.getDefault()).apply { isLenient = false }
            val parsed = runCatching { parser.parse(input) }.getOrNull() ?: continue
            val calendar = Calendar.getInstance().apply { time = parsed }
            if (!pattern.contains("yyyy")) calendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR))
            return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)
        }
        return null
    }

    fun isFutureDate(dateText: String): Boolean {
        val key = canonicalDateKey(dateText) ?: return false
        val selected = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(key) ?: return false
        val today = Calendar.getInstance().apply { clear(Calendar.HOUR_OF_DAY); clear(Calendar.MINUTE); clear(Calendar.SECOND); clear(Calendar.MILLISECOND) }
        return selected.after(today.time)
    }

    fun normalizedSession(session: String): String = when (session.trim().uppercase(Locale.US)) {
        "MIDDAY", "AFTERNOON" -> "AFTERNOON"
        "EVENING", "NIGHT" -> "EVENING"
        else -> "MORNING"
    }

    fun extractBaseName(cowName: String): String {
        val trimmed = cowName.trim()
        return trimmed
            .substringBefore(" (")
            .substringBefore(" -")
            .replace(Regex("^#\\s*[0-9A-Za-z-]+\\s*"), "")
            .substringBefore("#")
            .trim()
            .lowercase(Locale.US)
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
    }

    fun extractTag(cowName: String): String? {
        val trimmed = cowName.trim()
        val inParens = Regex("\\((?:#\\s*)?([A-Za-z0-9-]+)[^)]*\\)").find(trimmed)?.groupValues?.getOrNull(1)
        if (!inParens.isNullOrBlank()) return inParens.trim().lowercase(Locale.US)
        val standaloneTag = Regex("#\\s*([A-Za-z0-9-]+)").find(trimmed)?.groupValues?.getOrNull(1)
        if (!standaloneTag.isNullOrBlank()) return standaloneTag.trim().lowercase(Locale.US)
        return null
    }

    fun cowKey(cowName: String): String {
        val trimmed = cowName.trim()
        val base = extractBaseName(trimmed)
        if (base.isNotBlank()) return base
        val tag = extractTag(trimmed)
        if (!tag.isNullOrBlank()) return tag
        val normalized = trimmed.lowercase(Locale.US).replace(Regex("[^a-z0-9]+"), "-").trim('-')
        return normalized.ifBlank { "unassigned-cow" }
    }

    fun isSameCow(firstCowName: String, secondCowName: String): Boolean {
        val first = firstCowName.trim()
        val second = secondCowName.trim()
        if (first.equals(second, ignoreCase = true)) return true

        val firstKey = cowKey(first)
        val secondKey = cowKey(second)
        if (firstKey == secondKey && firstKey.isNotBlank() && firstKey != "unassigned-cow") return true

        val firstTag = extractTag(first)
        val secondTag = extractTag(second)
        if (firstTag != null && secondTag != null && firstTag == secondTag) return true

        val firstName = extractBaseName(first)
        val secondName = extractBaseName(second)
        if (firstName.isNotBlank() && secondName.isNotBlank() && firstName == secondName && firstName != "unassigned-cow") return true

        return false
    }

    fun entrySyncId(farmId: String, cowName: String, dateText: String, session: String): String? {
        val dateKey = canonicalDateKey(dateText) ?: return null
        return "milk-${farmId}-${cowKey(cowName)}-$dateKey-${normalizedSession(session).lowercase(Locale.US)}"
    }

    fun isSameSlot(first: MilkLog, secondCowName: String, secondDate: String, secondSession: String): Boolean {
        val firstDate = canonicalDateKey(first.date) ?: return false
        val secondDateKey = canonicalDateKey(secondDate) ?: return false
        if (firstDate != secondDateKey) return false
        if (normalizedSession(first.session) != normalizedSession(secondSession)) return false
        return isSameCow(first.cowName, secondCowName)
    }

    fun matchesCow(logCowName: String, targetCowName: String, targetTag: String = "", logNotes: String? = null): Boolean {
        val cleanLog = logCowName.trim()
        val cleanTarget = targetCowName.trim()
        if (cleanLog.isBlank() && cleanTarget.isBlank()) return true
        if (cleanLog.equals(cleanTarget, ignoreCase = true)) return true
        if (isSameCow(cleanLog, cleanTarget)) return true

        val cleanTag = targetTag.trim().replace("#", "").lowercase(Locale.US)
        if (cleanTag.isNotEmpty()) {
            val logLower = cleanLog.lowercase(Locale.US)
            if (logLower.contains(cleanTag) || logLower.contains("#$cleanTag")) return true
            if (logNotes?.lowercase(Locale.US)?.contains(cleanTag) == true) return true
        }

        val lowerLog = cleanLog.lowercase(Locale.US)
        val lowerTarget = cleanTarget.lowercase(Locale.US)
        if (lowerLog.isNotEmpty() && lowerTarget.isNotEmpty()) {
            if (lowerLog.contains(lowerTarget) || lowerTarget.contains(lowerLog)) return true
            val targetBase = lowerTarget.substringBefore(" (").substringBefore(" -").substringBefore("#").trim()
            val logBase = lowerLog.substringBefore(" (").substringBefore(" -").substringBefore("#").trim()
            if (targetBase.isNotEmpty() && logBase.isNotEmpty() && (targetBase == logBase || lowerLog.contains(targetBase) || lowerTarget.contains(logBase))) {
                return true
            }
        }
        return false
    }

    fun findLogsForCow(logs: List<MilkLog>, cowName: String, tagNumber: String = ""): List<MilkLog> {
        return logs.filter { log -> matchesCow(log.cowName, cowName, tagNumber, log.notes) }
            .sortedWith(
                compareByDescending<MilkLog> { canonicalDateKey(it.date) ?: "1970-01-01" }
                    .thenByDescending {
                        when (it.session.trim().uppercase(Locale.US)) {
                            "EVENING", "NIGHT" -> 3
                            "MIDDAY", "AFTERNOON" -> 2
                            else -> 1
                        }
                    }
                    .thenByDescending { it.id }
            )
    }
}
