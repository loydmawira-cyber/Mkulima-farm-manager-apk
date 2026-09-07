package com.example.util

import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

data class TaskChecklistItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isChecked: Boolean = false
)

enum class TaskRecurrenceInterval(
    val code: String,
    val label: String,
    val shortLabel: String,
    val calendarField: Int,
    val amount: Int
) {
    ONE_DAY("1day", "1 Day", "Daily", Calendar.DAY_OF_YEAR, 1),
    SEVEN_DAYS("7days", "7 Days", "Weekly", Calendar.DAY_OF_YEAR, 7),
    THIRTY_DAYS("30days", "30 Days", "Monthly", Calendar.DAY_OF_YEAR, 30),
    THREE_MONTHS("3months", "3 Months", "Quarterly", Calendar.MONTH, 3),
    SIX_MONTHS("6months", "6 Months", "Semi-Annual", Calendar.MONTH, 6),
    ONE_YEAR("1year", "1 Year", "Annual", Calendar.YEAR, 1);

    companion object {
        fun fromCode(code: String?): TaskRecurrenceInterval? {
            if (code.isNullOrBlank()) return null
            val clean = code.trim().lowercase().replace(" ", "").replace("_", "")
            return entries.find { it.code.equals(clean, ignoreCase = true) }
        }

        fun getDisplayLabel(code: String?): String {
            val matched = fromCode(code)
            return matched?.label ?: if (code.isNullOrBlank()) "None" else code
        }
    }
}

object TaskChecklistUtils {
    fun parseChecklist(json: String?): List<TaskChecklistItem> {
        if (json.isNullOrBlank()) return emptyList()
        val result = mutableListOf<TaskChecklistItem>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val text = obj.optString("text", "").trim()
                if (text.isNotBlank()) {
                    result.add(
                        TaskChecklistItem(
                            id = obj.optString("id", UUID.randomUUID().toString()),
                            text = text,
                            isChecked = obj.optBoolean("isChecked", false)
                        )
                    )
                }
            }
        } catch (_: Exception) {}
        return result
    }

    fun serializeChecklist(items: List<TaskChecklistItem>): String {
        if (items.isEmpty()) return ""
        val array = JSONArray()
        for (item in items) {
            if (item.text.isNotBlank()) {
                val obj = JSONObject()
                obj.put("id", item.id)
                obj.put("text", item.text)
                obj.put("isChecked", item.isChecked)
                array.put(obj)
            }
        }
        return array.toString()
    }

    fun resetChecklist(json: String?): String {
        val items = parseChecklist(json)
        if (items.isEmpty()) return ""
        return serializeChecklist(items.map { it.copy(isChecked = false) })
    }
}

object TaskRecurrenceUtils {

    /**
     * Calculates the next due date/time string given the current scheduled time and interval.
     * E.g. ("07 Sep 2026 at 09:00 AM", "7days") -> "14 Sep 2026 at 09:00 AM"
     */
    fun calculateNextScheduledTime(currentScheduledTime: String?, intervalCode: String): String {
        val interval = TaskRecurrenceInterval.fromCode(intervalCode) ?: TaskRecurrenceInterval.SEVEN_DAYS
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        
        // Extract time component if present (e.g. "at 09:00 AM" or "09:00 AM")
        var timeSuffix = ""
        val scheduled = currentScheduledTime.orEmpty().trim()
        if (scheduled.contains(" at ", ignoreCase = true)) {
            timeSuffix = " at " + scheduled.substringAfter(" at ").trim()
        }

        val baseDate = parseScheduledDate(scheduled) ?: Date()
        val calendar = Calendar.getInstance().apply {
            time = baseDate
            add(interval.calendarField, interval.amount)
        }

        // If the calculated next date is in the past, advance from today
        val now = Calendar.getInstance()
        if (calendar.before(now)) {
            calendar.time = Date()
            calendar.add(interval.calendarField, interval.amount)
        }

        val nextDateFormatted = dateFormat.format(calendar.time)
        return if (timeSuffix.isNotBlank()) {
            "$nextDateFormatted$timeSuffix"
        } else {
            nextDateFormatted
        }
    }

    private fun parseScheduledDate(scheduledTime: String): Date? {
        val trimmed = scheduledTime.trim()
        val datePart = if (trimmed.contains(" at ", ignoreCase = true)) {
            trimmed.substringBefore(" at ").trim()
        } else {
            trimmed
        }

        if (datePart.equals("Today", ignoreCase = true)) return Date()
        if (datePart.equals("Tomorrow", ignoreCase = true)) {
            return Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }.time
        }

        val patterns = listOf("dd MMM yyyy", "yyyy-MM-dd", "dd/MM/yyyy", "d MMM yyyy", "dd MMM")
        for (pattern in patterns) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.getDefault())
                sdf.isLenient = false
                val parsed = sdf.parse(datePart)
                if (parsed != null) return parsed
            } catch (_: Exception) {}
        }
        return null
    }
}
