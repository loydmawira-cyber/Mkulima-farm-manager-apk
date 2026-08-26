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

    fun cowKey(cowName: String): String {
        val trimmed = cowName.trim()
        val tag = Regex("\\((?:#\\s*)?([A-Za-z0-9-]+)\\)").find(trimmed)?.groupValues?.getOrNull(1)
        val normalized = (tag ?: trimmed.substringBefore(" (").substringBefore(" -").substringBefore("#"))
            .trim().lowercase(Locale.US)
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
        return normalized.ifBlank { "unassigned-cow" }
    }

    fun entrySyncId(farmId: String, cowName: String, dateText: String, session: String): String? {
        val dateKey = canonicalDateKey(dateText) ?: return null
        return "milk-${farmId}-${cowKey(cowName)}-$dateKey-${normalizedSession(session).lowercase(Locale.US)}"
    }

    fun isSameSlot(first: MilkLog, secondCowName: String, secondDate: String, secondSession: String): Boolean {
        val firstDate = canonicalDateKey(first.date) ?: return false
        val secondDateKey = canonicalDateKey(secondDate) ?: return false
        return cowKey(first.cowName) == cowKey(secondCowName) &&
            firstDate == secondDateKey &&
            normalizedSession(first.session) == normalizedSession(secondSession)
    }
}
