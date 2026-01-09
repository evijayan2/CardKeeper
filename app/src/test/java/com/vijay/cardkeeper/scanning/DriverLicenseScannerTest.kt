package com.vijay.cardkeeper.scanning

import android.util.Log
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DriverLicenseScannerTest {

    private lateinit var scanner: DriverLicenseScanner

    @BeforeEach
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        scanner = DriverLicenseScanner()
    }

    @Test
    fun `parseAAMVAData should extract DAQ from ANS field when DAQ is not present at top level`() {
        // Arrange: A raw string where DAQ is inside ANS field
        // AAMVA standard usually has tags like DAC, DCS, etc.
        // Some states might pack data into an ANS (Optional) field.
        val rawData = """
            ANSI 636000080002DL00410288ZS03110008DLDAQD1234567
            DCSDOE
            DACJOHN
            ANSDAQD1234567 ZS123
        """.trimIndent()

        // Act
        val result = scanner.parseAAMVAData(rawData)

        // Assert
        assertThat(result.docNumber).isEqualTo("D1234567")
    }

    @Test
    fun `parseAAMVAData should prefer top-level DAQ over ANS embedded DAQ`() {
        // Arrange
        val rawData = """
            DAQTOP123
            ANSDAQEMBED456
        """.trimIndent()

        // Act
        val result = scanner.parseAAMVAData(rawData)

        // Assert
        assertThat(result.docNumber).isEqualTo("TOP123")
    }

    @Test
    fun `parseAAMVAData should handle DAQ in ANS followed by another tag without spaces`() {
        // Arrange: DAQ followed immediately by another tag (e.g., ZSS)
        val rawData = "ANSDAQD7654321ZSS123"

        // Act
        val result = scanner.parseAAMVAData(rawData)

        // Assert
        assertThat(result.docNumber).isEqualTo("D7654321")
    }

    @Test
    fun `parseAAMVAData should correctly parse basic names and dates`() {
        // Arrange
        val rawData = """
            DCSDOE
            DACJOHN
            DADMIDDLE
            DBB19900101
            DBA20300101
            DAQD1234567
        """.trimIndent()

        // Act
        val result = scanner.parseAAMVAData(rawData)

        // Assert
        assertThat(result.name).isEqualTo("JOHN MIDDLE DOE")
        assertThat(result.dob).isEqualTo("19/90/0101")
    }

    @Test
    fun `parseAAMVAData should extract DAQ from ANS when embedded with various delimiters`() {
        // Case 1: Space delimiter
        val rawSpace = "ANSDAQ12345 SPACE...".trimIndent()
        assertThat(scanner.parseAAMVAData(rawSpace).docNumber).isEqualTo("12345")

        // Case 2: Newline delimiter
        val rawNewline = "ANSDAQ67890\nNEXT...".trimIndent()
        assertThat(scanner.parseAAMVAData(rawNewline).docNumber).isEqualTo("67890")

        // Case 3: Carriage return delimiter
        val rawCR = "ANSDAQ11223\rNEXT...".trimIndent()
        assertThat(scanner.parseAAMVAData(rawCR).docNumber).isEqualTo("11223")

        // Case 4: Immediate tag start (Strict AAMVA)
        val rawTag = "ANSDAQ44556ABC...".trimIndent()
        assertThat(scanner.parseAAMVAData(rawTag).docNumber).isEqualTo("44556")

        // Case 5: End of string
        val rawEnd = "ANSDAQ99887".trimIndent()
        assertThat(scanner.parseAAMVAData(rawEnd).docNumber).isEqualTo("99887")

        // Case 5: puntuation chars
        val rawPunct = "ANSDAQMD-99887 DACJOHN".trimIndent()
        assertThat(scanner.parseAAMVAData(rawPunct).docNumber).isEqualTo("MD-99887")
    }
}
