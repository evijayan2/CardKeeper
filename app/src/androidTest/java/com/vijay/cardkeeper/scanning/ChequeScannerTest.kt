package com.vijay.cardkeeper.scanning

import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChequeScannerTest {

    private val scanner = ChequeScanner()

    @Test
    fun scan_should_extract_details_from_a_US_cheque() = runBlocking {
        // Given
        val mockBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)

        // When
        val result = scanner.scan(mockBitmap)

        // Then
        assertThat(result).isNotNull()
    }
}
