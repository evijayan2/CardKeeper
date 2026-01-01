package com.vijay.cardkeeper.util

import com.vijay.cardkeeper.ui.common.DateFormatType
import kotlinx.datetime.*
import kotlinx.datetime.format.*

object DateNormalizer {

    private val strictFormats = listOf(
        LocalDate.Format {
            dayOfMonth(); char(' '); monthName(MonthNames.ENGLISH_ABBREVIATED); char(' '); year()
        }, // dd MMM yyyy
        LocalDate.Format {
            year(); char('/'); dayOfMonth(); char('/'); monthNumber()
        }, // yyyy/dd/MM
        LocalDate.Format {
            year(); char('/'); monthNumber(); char('/'); dayOfMonth()
        }, // yyyy/MM/dd
        LocalDate.Format {
            monthNumber(); char('/'); dayOfMonth(); char('/'); year()
        }, // MM/dd/yyyy
        LocalDate.Format {
            dayOfMonth(); char('/'); monthNumber(); char('/'); year()
        }, // dd/MM/yyyy
        LocalDate.Format {
            year(); char('-'); dayOfMonth(); char('-'); monthNumber()
        }, // yyyy-dd-MM
        LocalDate.Format {
           year(); char('-'); monthNumber(); char('-'); dayOfMonth()
        }, // yyyy-MM-dd
        LocalDate.Format {
            monthNumber(); char('-'); dayOfMonth(); char('-'); year()
        }, // MM-dd-yyyy
        LocalDate.Format {
            dayOfMonth(); char('-'); monthNumber(); char('-'); year()
        }  // dd-MM-yyyy
    )

    fun normalize(rawInput: String, dateFormatType: DateFormatType): String {
        val cleaned = cleanOcrNoise(rawInput)
        val parsed = parseStrict(cleaned)
        if (parsed == null) {
            // Instead of crashing, return the original input so the user can edit it.
            return rawInput
        }
        return formatLocalDate(parsed, dateFormatType.formatPattern)
    }

    private fun formatLocalDate(date: LocalDate, pattern: String): String {
        val d = date.dayOfMonth.toString().padStart(2, '0')
        val m = date.monthNumber.toString().padStart(2, '0')
        val y = date.year.toString()
        
        return pattern
            .replace("dd", d)
            .replace("MM", m)
            .replace("yyyy", y)
            .replace("uuuu", y)
    }

    fun isAmbiguous(input: String): Boolean {
        val cleaned = cleanOcrNoise(input)
        val regex = Regex("""\b(\d{2})/(\d{2})/\d{4}\b""")
        val match = regex.find(cleaned) ?: return false

        val (a, b) = match.destructured
        return a.toInt() <= 12 && b.toInt() <= 12
    }

    fun parseStrict(input: String): LocalDate? {
        for (format in strictFormats) {
            try {
                return LocalDate.parse(input, format)
            } catch (_: Exception) { }
        }
        return null
    }

    private fun cleanOcrNoise(input: String): String {
        return input
            .trim()
            .replace('I', '1')
            .replace('l', '1')
            .replace('O', '0')
            .replace('S', '5')
            .replace(Regex("""\s+"""), " ")
    }
}
