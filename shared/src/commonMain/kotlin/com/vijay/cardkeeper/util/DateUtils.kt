package com.vijay.cardkeeper.util

import kotlinx.datetime.*
import kotlinx.datetime.format.*

object DateUtils {
    private val DISPLAY_FORMAT = LocalDate.Format {
        monthNumber(); char('/'); dayOfMonth(); char('/'); year()
    } // MM/dd/yyyy
    
    private val PARSE_FORMATS = listOf(
        LocalDate.Format { monthNumber(); char('/'); dayOfMonth(); char('/'); year() },
        LocalDate.Format { dayOfMonth(); char('/'); monthNumber(); char('/'); year() },
        LocalDate.Format { year(); char('-'); monthNumber(); char('-'); dayOfMonth() },
        LocalDate.Format { monthNumber(); char('-'); dayOfMonth(); char('-'); year() },
        LocalDate.Format { dayOfMonth(); char('-'); monthNumber(); char('-'); year() }
    )

    fun parseDate(dateString: String?): Long? {
        if (dateString.isNullOrBlank()) return null

        for (format in PARSE_FORMATS) {
            try {
                val date = LocalDate.parse(dateString.trim(), format)
                return date.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
            } catch (e: Exception) {
                // Ignore
            }
        }
        return null
    }

    fun formatDate(timestamp: Long?): String {
        if (timestamp == null) return ""
        return try {
            val instant = Instant.fromEpochMilliseconds(timestamp)
            val date = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
            date.format(DISPLAY_FORMAT)
        } catch (e: Exception) {
            ""
        }
    }

    fun isValidDate(input: String, type: com.vijay.cardkeeper.ui.common.DateFormatType): Boolean {
        // Expected input is 8 digits: d1d2m1m2y1y2y3y4
        if (input.length != 8 || !input.all { it.isDigit() }) return false

        val part1 = input.substring(0, 2).toIntOrNull() ?: return false
        val part2 = input.substring(2, 4).toIntOrNull() ?: return false
        val year = input.substring(4, 8).toIntOrNull() ?: return false

        // Basic year validation
        if (year < 1900 || year > 2100) return false

        val (day, month) = when (type) {
            com.vijay.cardkeeper.ui.common.DateFormatType.USA -> Pair(part2, part1)
            com.vijay.cardkeeper.ui.common.DateFormatType.INDIA, com.vijay.cardkeeper.ui.common.DateFormatType.GENERIC -> Pair(part1, part2)
        }

        if (month < 1 || month > 12) return false
        
        val maxDays = when (month) {
            4, 6, 9, 11 -> 30
            2 -> if (isLeapYear(year)) 29 else 28
            else -> 31
        }
        
        return day in 1..maxDays
    }

    fun isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }

    /**
     * Formats raw digits "12345678" to "12/34/5678".
     * Does not validate, just formats.
     */
    fun formatRawDate(raw: String): String {
        if (raw.length != 8) return raw
        return "${raw.substring(0, 2)}/${raw.substring(2, 4)}/${raw.substring(4, 8)}"
    }
}
