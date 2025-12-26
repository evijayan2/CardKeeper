package com.vijay.cardkeeper.scanning

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.vijay.cardkeeper.util.CardDetails
import com.vijay.cardkeeper.util.CardTextAnalyzer
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class PaymentCardScanner {

    // Helper analyzer. The constructor callback is for CameraX stream,
    // but we use the direct analyze(InputImage) overload which takes a callback.
    private val analyzer = CardTextAnalyzer {}

    suspend fun scan(bitmap: Bitmap): CardDetails = suspendCancellableCoroutine { cont ->
        val image = InputImage.fromBitmap(bitmap, 0)
        analyzer.analyze(image) { details ->
            if (cont.isActive) {
                cont.resume(details)
            }
        }
    }
}
