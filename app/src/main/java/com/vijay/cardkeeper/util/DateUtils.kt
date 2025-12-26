package com.vijay.cardkeeper.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    private const val DISPLAY_FORMAT = "MM/dd/yyyy"
    private val PARSE_FORMATS =
            listOf("MM/dd/yyyy", "dd/MM/yyyy", "yyyy-MM-dd", "MM-dd-yyyy", "dd-MM-yyyy")

    fun parseDate(dateString: String?): Long? {
        if (dateString.isNullOrBlank()) return null

        for (format in PARSE_FORMATS) {
            try {
                val sdf = SimpleDateFormat(format, Locale.US)
                sdf.isLenient = false
                val date = sdf.parse(dateString.trim())
                if (date != null) {
                    return date.time
                }
            } catch (e: Exception) {
                // Ignore and try next format
            }
        }
        return null
    }

    fun formatDate(timestamp: Long?): String {
        if (timestamp == null) return ""
        return try {
            val sdf = SimpleDateFormat(DISPLAY_FORMAT, Locale.US)
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            ""
        }
    }
}
