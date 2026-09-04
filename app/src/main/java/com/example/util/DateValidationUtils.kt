package com.example.util

import com.example.data.EggLog
import com.example.data.FarmTask
import com.example.data.MilkLog
import com.example.data.MilkLogEntryRules
import com.example.data.TaskPriority
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateValidationUtils {
    private val dateFormats = listOf(
        "yyyy-MM-dd",
        "dd MMM yyyy",
        "d MMM yyyy",
        "dd MMM, yyyy",
        "d MMM, yyyy",
        "dd MMM, hh:mm a",
        "d MMM, hh:mm a",
        "dd MMM, HH:mm",
        "d MMM, HH:mm",
        "yyyy/MM/dd",
        "dd/MM/yyyy",
        "dd-MM-yyyy",
        "MM/dd/yyyy",
        "dd MMM",
        "d MMM"
    )

    fun parseDate(str: String?): Date? {
        if (str.isNullOrBlank()) return null
        val clean = str.trim()
        for (format in dateFormats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.getDefault())
                sdf.isLenient = false
                val date = sdf.parse(clean)
                if (date != null) return date
            } catch (_: Exception) {}
        }
        return null
    }

    /**
     * Checks if an entry's date or updatedAt timestamp corresponds to today (or the future).
     * If the entry is strictly before today (00:00:00 AM of today), returns false (i.e. is previous day).
     */
    fun isTodayOrFuture(dateStr: String?, timestamp: Long = 0L): Boolean {
        val todayCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // 1. If timestamp is provided and valid (greater than year 2020)
        if (timestamp > 1577836800000L) {
            val entryCal = Calendar.getInstance().apply { timeInMillis = timestamp }
            if (entryCal.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) &&
                entryCal.get(Calendar.DAY_OF_YEAR) == todayCalendar.get(Calendar.DAY_OF_YEAR)) {
                return true
            }
        }

        // 2. Parse dateStr
        if (!dateStr.isNullOrBlank()) {
            val trimmed = dateStr.trim()
            if (trimmed.startsWith("Today", ignoreCase = true)) {
                return true
            }
            val parsedDate = parseDate(trimmed)
            if (parsedDate != null) {
                val entryCal = Calendar.getInstance().apply {
                    time = parsedDate
                    // If year is default 1970 (e.g. from parsing format without year like "dd MMM, hh:mm a")
                    if (get(Calendar.YEAR) == 1970) {
                        set(Calendar.YEAR, todayCalendar.get(Calendar.YEAR))
                    }
                }
                val entryDay = entryCal.get(Calendar.DAY_OF_YEAR)
                val entryYear = entryCal.get(Calendar.YEAR)
                val todayDay = todayCalendar.get(Calendar.DAY_OF_YEAR)
                val todayYear = todayCalendar.get(Calendar.YEAR)

                if (entryYear > todayYear) return true
                if (entryYear == todayYear && entryDay >= todayDay) return true
                return false
            }
        }

        // Fallback: If unable to parse and timestamp is missing, check if it contains today's formatted strings
        val todayFormat1 = SimpleDateFormat("dd MMM", Locale.getDefault()).format(todayCalendar.time)
        val todayFormat2 = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(todayCalendar.time)
        if (dateStr?.contains(todayFormat1, ignoreCase = true) == true || dateStr?.contains(todayFormat2) == true) {
            return true
        }

        return true
    }

    /**
     * Checks if a date string is in the past (before today's start).
     */
    fun isPastDate(dateStr: String?): Boolean {
        if (dateStr.isNullOrBlank()) return false
        return !isTodayOrFuture(dateStr, 0L)
    }

    private fun applyTimeToCalendar(rawTime: String, cal: Calendar) {
        val clean = rawTime.removePrefix("at").trim()
        if (clean.isBlank()) return
        val timePatterns = listOf("hh:mm a", "h:mm a", "HH:mm", "H:mm", "hh:mma", "h:mma")
        for (pattern in timePatterns) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.getDefault())
                sdf.isLenient = false
                val date = sdf.parse(clean)
                if (date != null) {
                    val timeCal = Calendar.getInstance().apply { time = date }
                    cal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
                    cal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    return
                }
            } catch (_: Exception) {}
        }
    }

    /**
     * Parses the scheduled date and time of a FarmTask into epoch milliseconds.
     * Supports "Today at 09:00 AM", "Tomorrow at 10:00 AM", "12 Aug 2026", "2026-09-04", etc.
     */
    fun parseTaskScheduledTimestamp(scheduledTime: String?, createdAt: Long = 0L): Long {
        if (scheduledTime.isNullOrBlank()) {
            return if (createdAt > 0L) createdAt else (Long.MAX_VALUE / 2)
        }
        val trimmed = scheduledTime.trim()

        val todayCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Handle "Today"
        if (trimmed.startsWith("Today", ignoreCase = true)) {
            val cal = (todayCal.clone() as Calendar)
            applyTimeToCalendar(trimmed.substringAfter("Today").trim(), cal)
            return cal.timeInMillis
        }

        // Handle "Tomorrow"
        if (trimmed.startsWith("Tomorrow", ignoreCase = true)) {
            val cal = (todayCal.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, 1)
            }
            applyTimeToCalendar(trimmed.substringAfter("Tomorrow").trim(), cal)
            return cal.timeInMillis
        }

        // Handle "Yesterday" or "Overdue"
        if (trimmed.startsWith("Yesterday", ignoreCase = true) || trimmed.startsWith("Overdue", ignoreCase = true)) {
            val cal = (todayCal.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, -1)
            }
            return cal.timeInMillis
        }

        // Extract potential date and time parts (e.g. "12 Aug 2026 at 09:00 AM")
        val atIndex = trimmed.indexOf(" at ", ignoreCase = true)
        val datePart = if (atIndex != -1) trimmed.substring(0, atIndex).trim() else trimmed
        val timePart = if (atIndex != -1) trimmed.substring(atIndex + 4).trim() else ""

        val parsed = parseDate(datePart)
        if (parsed != null) {
            val cal = Calendar.getInstance().apply {
                time = parsed
                if (get(Calendar.YEAR) == 1970) {
                    set(Calendar.YEAR, todayCal.get(Calendar.YEAR))
                }
            }
            if (timePart.isNotBlank()) {
                applyTimeToCalendar(timePart, cal)
            }
            return cal.timeInMillis
        }

        return if (createdAt > 0L) createdAt else (Long.MAX_VALUE / 2)
    }

    /**
     * Sorts tasks so that the closest date appears first.
     * 1. Uncompleted tasks appear before completed tasks.
     * 2. Among uncompleted tasks:
     *    Sorted by due date timestamp ascending (earliest/closest due date first: Overdue -> Today -> Tomorrow -> Soonest upcoming dates -> Later dates).
     *    Ties broken by TaskPriority (HIGH > MEDIUM > LOW) then ID / creation time.
     * 3. Among completed tasks:
     *    Sorted by closest/most recent completion or date descending.
     */
    fun sortTasksByClosestDate(tasks: List<FarmTask>): List<FarmTask> {
        val pending = tasks.filter { !it.isCompleted }.sortedWith(
            compareBy<FarmTask> { task ->
                parseTaskScheduledTimestamp(task.scheduledTime, task.createdAt)
            }.thenBy { task ->
                when (task.priority) {
                    TaskPriority.HIGH -> 0
                    TaskPriority.MEDIUM -> 1
                    TaskPriority.LOW -> 2
                }
            }.thenByDescending { it.id }
        )

        val completed = tasks.filter { it.isCompleted }.sortedWith(
            compareByDescending<FarmTask> { task ->
                parseTaskScheduledTimestamp(task.completedAt ?: task.scheduledTime, task.updatedAt)
            }.thenByDescending { it.id }
        )

        return pending + completed
    }

    /**
     * Parses the date, session, and loggedAt of a MilkLog into epoch milliseconds.
     */
    fun parseMilkLogTimestamp(log: MilkLog): Long {
        val dateKey = MilkLogEntryRules.canonicalDateKey(log.date)
        val cal = Calendar.getInstance()
        if (dateKey != null) {
            val parsedDate = runCatching {
                SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateKey)
            }.getOrNull()
            if (parsedDate != null) {
                cal.time = parsedDate
            }
        } else {
            val fallbackDate = parseDate(log.date)
            if (fallbackDate != null) {
                cal.time = fallbackDate
            } else if (log.updatedAt > 0L) {
                return log.updatedAt
            }
        }

        // Adjust session or loggedAt time
        val sessionHour = when (log.session.trim().uppercase(Locale.US)) {
            "MORNING" -> 6
            "AFTERNOON", "MIDDAY" -> 13
            "EVENING", "NIGHT" -> 18
            else -> 8
        }
        cal.set(Calendar.HOUR_OF_DAY, sessionHour)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        if (!log.loggedAt.isNullOrBlank()) {
            val timePart = if (log.loggedAt.contains(",")) log.loggedAt.substringAfter(",").trim() else log.loggedAt.trim()
            applyTimeToCalendar(timePart, cal)
        }

        return cal.timeInMillis
    }

    /**
     * Sorts milk logs so that the closest date (most recent / latest date) appears first.
     */
    fun sortMilkLogsByClosestDate(logs: List<MilkLog>): List<MilkLog> {
        return logs.sortedWith(
            compareByDescending<MilkLog> { parseMilkLogTimestamp(it) }
                .thenByDescending { it.id }
        )
    }

    /**
     * Parses the date and time of an EggLog into epoch milliseconds.
     */
    fun parseEggLogTimestamp(log: EggLog): Long {
        if (!log.loggedAt.isNullOrBlank()) {
            val parsed = parseDate(log.loggedAt)
            if (parsed != null) return parsed.time
            val datePart = log.loggedAt.substringBefore(",").trim()
            val parsedDate = parseDate(datePart)
            if (parsedDate != null) {
                val cal = Calendar.getInstance().apply { time = parsedDate }
                val timePart = log.loggedAt.substringAfter(",").trim()
                applyTimeToCalendar(timePart, cal)
                return cal.timeInMillis
            }
        }
        return if (log.updatedAt > 0L) log.updatedAt else log.id
    }

    /**
     * Sorts egg logs so that the closest date (most recent / latest date) appears first.
     */
    fun sortEggLogsByClosestDate(logs: List<EggLog>): List<EggLog> {
        return logs.sortedWith(
            compareByDescending<EggLog> { parseEggLogTimestamp(it) }
                .thenByDescending { it.id }
        )
    }
}
