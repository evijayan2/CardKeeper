package com.vijay.cardkeeper.scanning

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.vijay.cardkeeper.util.IdentityDetails
import com.vijay.cardkeeper.util.IdentityTextAnalyzer
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class IdentityScanner {

    suspend fun scan(bitmap: Bitmap, isBackSide: Boolean): IdentityDetails =
            suspendCancellableCoroutine { cont ->
                val analyzer =
                        IdentityTextAnalyzer(isBackSide) { details ->
                            if (cont.isActive) {
                                cont.resume(details)
                            }
                        }
                val image = InputImage.fromBitmap(bitmap, 0)
                // We need to adapt IdentityTextAnalyzer.analyze(ImageProxy) to InputImage?
                // Wait, IdentityTextAnalyzer implements ImageAnalysis.Analyzer (takes ImageProxy).
                // But it also has `analyze(image: InputImage, onResult: ...)`?
                // Let's check IdentityTextAnalyzer.kt again.
                // Step 1012: It HAS `fun analyze(image: InputImage, onResult: ...)` at line 135.
                // But constructor takes `onResult`.
                // The `analyze(image)` method (line 135) takes `onResult` as param?
                // No, line 135: `fun analyze(image: InputImage, onResult: (IdentityDetails) ->
                // Unit)`.
                // It ignores class property `onResult`?
                // Line 110 calls `analyze(inputImage) { details -> ... }`.

                // If I use the overload `analyze(InputImage, callback)`, I don't need to pass
                // callback to constructor?
                // But constructor REQUIRES `onResult`.
                // So I pass dummy or correct callback to constructor.

                analyzer.analyze(image) { details ->
                    if (cont.isActive) {
                        cont.resume(details)
                    }
                }
            }
}
