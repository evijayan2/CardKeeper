package com.vijay.cardkeeper.util

import com.vijay.cardkeeper.ui.common.DateFormatType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DateNormalizerTest {

    @Test
    fun `text month format`() {
        assertEquals(
            "21/05/1979",
            DateNormalizer.normalize("21 May 1979", DateFormatType.INDIA)
        )
    }

    @Test
    fun `year first format`() {
        assertEquals(
            "21/05/1979",
            DateNormalizer.normalize("1979/21/05", DateFormatType.INDIA)
        )
    }

    @Test
    fun `US format`() {
        assertEquals(
            "05/22/1979",
            DateNormalizer.normalize("05/22/1979", DateFormatType.USA)
        )
    }

    @Test
    fun usFormat_GC() {
        assertEquals(
            "21/05/1979",
            DateNormalizer.normalize("1979-05-21", DateFormatType.GENERIC)
        )
    }

    @Test
    fun `OCR noisy input`() {
        assertEquals(
            "21/05/1979",
            DateNormalizer.normalize("2I May I979", DateFormatType.INDIA)
        )
    }

    @Test
    fun `ambiguous date detection`() {
        assertTrue(DateNormalizer.isAmbiguous("05/06/1979"))
        assertFalse(DateNormalizer.isAmbiguous("13/06/1979"))
    }

    @Test
    fun `invalid date throws exception`() {
        assertThrows<IllegalArgumentException> {
            DateNormalizer.normalize("1979-99-99", DateFormatType.GENERIC)
        }
    }
    @Test
    fun `parseStrict handles 01-01-2026`() {
        val input = "01/01/2026"
        val parsed = DateNormalizer.parseStrict(input)
        assertNotNull(parsed, "Failed to parse 01/01/2026")
        assertEquals(kotlinx.datetime.LocalDate(2026, 1, 1), parsed)
    }
}
