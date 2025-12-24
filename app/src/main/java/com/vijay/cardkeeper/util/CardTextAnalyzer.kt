package com.vijay.cardkeeper.util

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class CardTextAnalyzer(private val onCardNumberFound: (String) -> Unit) : ImageAnalysis.Analyzer {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val numberPattern = Regex("\\b(?:\\d[ -]*?){13,19}\\b")

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            recognizer
                    .process(image)
                    .addOnSuccessListener { visionText ->
                        for (block in visionText.textBlocks) {
                            // Simple heuristic: search for pattern in blocks
                            val text = block.text
                            // Remove spaces/dashes to check constraints
                            val potentialNumbers = numberPattern.findAll(text)

                            for (match in potentialNumbers) {
                                val cleanNum = match.value.replace(Regex("[^0-9]"), "")
                                if (cleanNum.length in 13..19) {
                                    onCardNumberFound(cleanNum)
                                    imageProxy.close() // Close early if found? Or let UI handle it.
                                    // We'll let UI decide to stop analysis, but for now just
                                    // callback.
                                    return@addOnSuccessListener
                                }
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        // Log or ignore
                    }
                    .addOnCompleteListener { imageProxy.close() }
        } else {
            imageProxy.close()
        }
    }
}
