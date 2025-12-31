package com.vijay.cardkeeper.ui.common

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

enum class DateFormatType(val formatPattern: String) {
    USA("MM/dd/yyyy"), // MM/DD/YYYY
    INDIA("dd/MM/yyyy"), // DD/MM/YYYY
    GENERIC("dd/MM/yyyy") // DD/MM/YYYY
}

object DateUtils {
    fun isValidDate(input: String, type: DateFormatType): Boolean {
        // Expected input is 8 digits: d1d2m1m2y1y2y3y4
        if (input.length != 8 || !input.all { it.isDigit() }) return false

        val part1 = input.substring(0, 2).toIntOrNull() ?: return false
        val part2 = input.substring(2, 4).toIntOrNull() ?: return false
        val year = input.substring(4, 8).toIntOrNull() ?: return false

        // Basic year validation
        if (year < 1900 || year > 2100) return false

        val (day, month) = when (type) {
            DateFormatType.USA -> Pair(part2, part1)
            DateFormatType.INDIA, DateFormatType.GENERIC -> Pair(part1, part2)
        }

        if (month < 1 || month > 12) return false
        
        val maxDays = when (month) {
            4, 6, 9, 11 -> 30
            2 -> if (isLeapYear(year)) 29 else 28
            else -> 31
        }
        
        return day in 1..maxDays
    }

    private fun isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }

    fun formatDate(raw: String): String {
        if (raw.length != 8) return raw
        return "${raw.substring(0, 2)}/${raw.substring(2, 4)}/${raw.substring(4, 8)}"
    }
}

class DateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        // Input is expected to be raw digits
        val trim = if (text.text.length >= 8) text.text.substring(0, 8) else text.text
        
        val out = StringBuilder()
        for (i in trim.indices) {
            out.append(trim[i])
            if (i == 1 || i == 3) out.append("/")
        }

        val numberOffsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val result = if (offset <= 1) offset
                else if (offset <= 3) offset + 1
                else if (offset <= 8) offset + 2
                else 10
                return result.coerceAtMost(out.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val result = if (offset <= 2) offset
                else if (offset <= 5) offset - 1
                else if (offset <= 10) offset - 2
                else 8
                return result.coerceAtMost(trim.length)
            }
        }

        return TransformedText(AnnotatedString(out.toString()), numberOffsetTranslator)
    }
}
