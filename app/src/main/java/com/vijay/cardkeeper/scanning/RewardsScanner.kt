package com.vijay.cardkeeper.scanning

import android.graphics.Bitmap
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

data class RewardsScanResult(
        val barcode: String? = null,
        val barcodeFormat: Int? = null,
        val shopName: String? = null
)

class RewardsScanner {

    private val barcodeScanner = BarcodeScanning.getClient()
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun scan(bitmap: Bitmap): RewardsScanResult {
        val image = InputImage.fromBitmap(bitmap, 0)

        var barcode: String? = null
        var format: Int? = null
        var shopName: String? = null

        // 1. Scan Barcode
        try {
            val barcodes = barcodeScanner.process(image).await()
            barcodes.firstOrNull()?.let {
                barcode = it.rawValue
                format = it.format
            }
        } catch (e: Exception) {
            // Ignore error
        }

        // 2. Scan Text for Shop Name
        try {
            val text = textRecognizer.process(image).await()
            // Heuristic: First line of first block
            shopName = text.textBlocks.firstOrNull()?.text?.lines()?.firstOrNull()
        } catch (e: Exception) {
            // Ignore error
        }

        return RewardsScanResult(barcode, format, shopName)
    }
}
