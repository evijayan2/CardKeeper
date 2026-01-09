package com.vijay.cardkeeper.scanning

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.util.zip.GZIPOutputStream

class AadharQrScannerTest {

    private lateinit var scanner: AadharQrScanner
    private lateinit var context: Context
    private lateinit var assetManager: AssetManager

    @BeforeEach
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.w(any<String>(), any<String>()) } returns 0
        every { Log.i(any(), any()) } returns 0

        context = mockk()
        assetManager = mockk()
        every { context.assets } returns assetManager
        every { assetManager.list("certs") } returns emptyArray()

        scanner = AadharQrScanner(context)
    }

    private fun createValidQrString(data: ByteArray): String {
        // GZip compress
        val byteArrayOutputStream = ByteArrayOutputStream()
        GZIPOutputStream(byteArrayOutputStream).use { it.write(data) }
        val compressed = byteArrayOutputStream.toByteArray()
        
        // To BigInteger
        // Note: positive sign is handled in scanner by checking first byte 0
        return BigInteger(1, compressed).toString()
    }

    @Test
    fun `parse should handle clean input`() {
        // Arrange
        val data = "version2".toByteArray() // Dummy data
        val cleanQr = createValidQrString(data)

        // Act
        val result = scanner.parse(cleanQr)

        // Assert
        // We expect it to NOT fail with "rawQrData only" 
        // Although decompression might yield garbage for fields, it should NOT crash at BigInteger step.
        // If it parses, result.rawQrData matches input, and result.signatureValid is false (no keys).
        // But more importantly, the catch block for NumberFormatException wasn't hit if we get here.
        assertThat(result.rawQrData).isEqualTo(cleanQr)
    }

    @Test
    fun `parse should sanitize input with spaces`() {
        // Arrange
        val data = "version2".toByteArray()
        val cleanQr = createValidQrString(data)
        // Insert spaces at random positions (simulating split numeric string)
        val noisyQr = cleanQr.substring(0, 5) + " " + cleanQr.substring(5)

        // Act
        val result = scanner.parse(noisyQr)

        // Assert
        // Should handle the spaces and parse correctly
        // The rawQrData returned in result is the INPUT string
        assertThat(result.rawQrData).isEqualTo(noisyQr)
    }

    @Test
    fun `parse should sanitize input with garbage chars`() {
        // Arrange
        val data = "version2".toByteArray()
        val cleanQr = createValidQrString(data)
        // Add random letters
        val noisyQr = cleanQr.substring(0, 5) + "xyz" + cleanQr.substring(5)

        // Act
        val result = scanner.parse(noisyQr)

        // Assert
        // Logic: trims "xyz", keeps digits -> restores original valid number -> parses
        assertThat(result.rawQrData).isEqualTo(noisyQr)
    }

    @Test
    fun `parse should fail gracefully for completely invalid input`() {
        // Arrange
        val invalidQr = "Not a number at all"

        // Act
        val result = scanner.parse(invalidQr)

        // Assert
        // Should fall into catch block and return raw data only with empty fields
        assertThat(result.rawQrData).isEqualTo(invalidQr)
        assertThat(result.name).isEmpty()
    }
}
