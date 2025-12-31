package com.vijay.cardkeeper.util

import com.vijay.cardkeeper.ui.common.DateFormatType
import java.util.Locale

// API 26+ imports
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

object DateNormalizer {

    private val strictFormatters = listOf(
        DateTimeFormatter.ofPattern("dd MMM uuuu", Locale.ENGLISH)
            .withResolverStyle(ResolverStyle.STRICT),
        DateTimeFormatter.ofPattern("uuuu/dd/MM")
            .withResolverStyle(ResolverStyle.STRICT),
        DateTimeFormatter.ofPattern("uuuu/MM/dd")
            .withResolverStyle(ResolverStyle.STRICT),
        DateTimeFormatter.ofPattern("MM/dd/uuuu")
            .withResolverStyle(ResolverStyle.STRICT),
        DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT),
        DateTimeFormatter.ofPattern("uuuu-dd-MM")
            .withResolverStyle(ResolverStyle.STRICT),
        DateTimeFormatter.ofPattern("uuuu-MM-dd")
            .withResolverStyle(ResolverStyle.STRICT),
        DateTimeFormatter.ofPattern("MM-dd-uuuu")
            .withResolverStyle(ResolverStyle.STRICT),
        DateTimeFormatter.ofPattern("dd-MM-uuuu")
            .withResolverStyle(ResolverStyle.STRICT)
    )

    /**
     * Normalizes date strings into MM/dd/yyyy
     */
    fun normalize(rawInput: String, dateFormatType: com.vijay.cardkeeper.ui.common.DateFormatType): String {
        val cleaned = cleanOcrNoise(rawInput)

        val parsed = parseStrict(cleaned)
            ?: throw IllegalArgumentException("Unsupported date format: $rawInput")

        return parsed.format(DateTimeFormatter.ofPattern(dateFormatType.formatPattern))
    }

    /**
     * Detect ambiguous MM/dd vs dd/MM
     */
    fun isAmbiguous(input: String): Boolean {
        val cleaned = cleanOcrNoise(input)
        val regex = Regex("""\b(\d{2})/(\d{2})/\d{4}\b""")
        val match = regex.find(cleaned) ?: return false

        val (a, b) = match.destructured
        return a.toInt() <= 12 && b.toInt() <= 12
    }

    private fun parseStrict(input: String): LocalDate? {
        for (formatter in strictFormatters) {
            try {
                return LocalDate.parse(input, formatter)
            } catch (_: Exception) { }
        }
        return null
    }

    /**
     * OCR cleanup for scanned documents (MRZ, cards, forms)
     */
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
