package com.tuitionmanager.app.utils

import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {

    fun getCurrentUtcTimestamp(): Long {
        return System.currentTimeMillis()
    }

    /**
     * Formats an epoch millisecond timestamp using the device's default local timezone.
     */
    fun formatLocalDate(epochMs: Long, pattern: String = "dd MMM yyyy"): String {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(Date(epochMs))
    }

    /**
     * Converts minutes from midnight (0..1439) into human-readable AM/PM string.
     * E.g. 600 -> "10:00 AM", 870 -> "02:30 PM"
     */
    fun formatMinutesToTime(minutesFromMidnight: Int): String {
        val safeMinutes = minutesFromMidnight.coerceIn(0, 1439)
        val hours = safeMinutes / 60
        val minutes = safeMinutes % 60
        val isPm = hours >= 12
        val displayHours = when {
            hours == 0 -> 12
            hours > 12 -> hours - 12
            else -> hours
        }
        return String.format(Locale.getDefault(), "%02d:%02d %s", displayHours, minutes, if (isPm) "PM" else "AM")
    }

    /**
     * Calculates the UTC 00:00:00 millisecond timestamp for a given year, month, day.
     */
    fun toUtcDayEpochMs(year: Int, month: Int, day: Int): Long {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(year, month - 1, day, 0, 0, 0)
        }
        return calendar.timeInMillis
    }
}
