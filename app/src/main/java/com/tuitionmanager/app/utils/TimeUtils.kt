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

    /**
     * Normalized start of day (00:00:00.000) for a given epoch ms in local timezone.
     */
    fun getStartOfDayEpochMs(epochMs: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = epochMs
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun getTodayStartOfDayEpochMs(): Long {
        return getStartOfDayEpochMs(System.currentTimeMillis())
    }

    /**
     * Returns 1 (Monday) to 7 (Sunday), matching ISO-8601 and ScheduleEntity.
     */
    fun getDayOfWeek(epochMs: Long): Int {
        val cal = Calendar.getInstance().apply {
            timeInMillis = epochMs
        }
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
    }
}
