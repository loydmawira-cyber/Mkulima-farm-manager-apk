package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateValidationUtils {
    private val dateFormats = listOf(
        "yyyy-MM-dd",
        "dd MMM yyyy",
        "d MMM yyyy",
        "dd MMM, hh:mm a",
        "d MMM, hh:mm a",
        "dd MMM, HH:mm",
        "d MMM, HH:mm",
        "yyyy/MM/dd",
        "dd/MM/yyyy",
        "dd-MM-yyyy",
        "MM/dd/yyyy"
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
}
