package com.vijay.cardkeeper.scanning

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.vijay.cardkeeper.util.AadharTextAnalyzer
import com.vijay.cardkeeper.util.IdentityDetails
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class AadharScanner {
    suspend fun scan(bitmap: Bitmap): IdentityDetails =
            suspendCancellableCoroutine { cont ->
                val analyzer = AadharTextAnalyzer { details ->
                    if (cont.isActive) {
                        cont.resume(details)
                    }
                }
                val image = InputImage.fromBitmap(bitmap, 0)
                analyzer.analyze(image) { details ->
                    if (cont.isActive) {
                        cont.resume(details)
                    }
                }
            }
}
