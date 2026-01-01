package com.vijay.cardkeeper.util

import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class AadharTextAnalyzer(
        private val onResult: (IdentityDetails) -> Unit
) : ImageAnalysis.Analyzer {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    // Aadhar UID Pattern: 12 digits, often spaced as XXXX XXXX XXXX
    private val uidPattern = Regex("\\b\\d{4}\\s\\d{4}\\s\\d{4}\\b|\\b\\d{12}\\b")
    
    // DOB Pattern: DD/MM/YYYY
    private val datePattern = Regex("\\b(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[0-2])/(\\d{4})\\b")
    
    // Gender Pattern: Male/Female
    private val genderPattern = Regex("\\b(MALE|FEMALE)\\b", RegexOption.IGNORE_CASE)

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            val inputImage = InputImage.fromMediaImage(mediaImage, rotationDegrees)

            analyze(inputImage) { details ->
                val finalDetails = details.copy(capturedImage = imageProxy.toBitmap())
                onResult(finalDetails)
                imageProxy.close()
            }
        } else {
            imageProxy.close()
        }
    }

    fun analyze(image: InputImage, onResult: (IdentityDetails) -> Unit) {
        recognizer
                .process(image)
                .addOnSuccessListener { visionText ->
                    val text = visionText.text
                    Log.d("AadharTextAnalyzer", "Raw OCR Text: $text")
                    
                    var foundUid = ""
                    var foundName = ""
                    var foundDob = ""
                    var foundGender = ""
                    
                    for (block in visionText.textBlocks) {
                        for (line in block.lines) {
                            val lineText = line.text.trim()
                            
                            // 1. UID Detection (Priority)
                            val uidMatch = uidPattern.find(lineText)
                            if (uidMatch != null && foundUid.isEmpty()) {
                                foundUid = uidMatch.value.replace(" ", "")
                                Log.d("AadharTextAnalyzer", "Found UID: $foundUid")
                            }
                            
                            // 2. DOB Detection
                            val dateMatch = datePattern.find(lineText)
                            if (dateMatch != null && foundDob.isEmpty()) {
                                foundDob = dateMatch.value
                                Log.d("AadharTextAnalyzer", "Found DOB: $foundDob")
                            }
                            
                            // 3. Gender Detection
                            val genderMatch = genderPattern.find(lineText)
                            if (genderMatch != null && foundGender.isEmpty()) {
                                foundGender = genderMatch.value.uppercase()
                                Log.d("AadharTextAnalyzer", "Found Gender: $foundGender")
                            }
                        }
                    }
                    
                    // 4. Name Heuristic (usually above the the phrase "Government of India" or near photo)
                    // For now, let's just pick a line that doesn't match other patterns and is uppercase words
                    if (foundName.isEmpty()) {
                        for (block in visionText.textBlocks) {
                            for (line in block.lines) {
                                val t = line.text.trim()
                                if (t.matches(Regex("^[A-Z\\s]+$")) && 
                                    t.length > 3 && 
                                    !t.contains("GOVERNMENT") && 
                                    !t.contains("INDIA") &&
                                    !t.contains("AADHAAR") &&
                                    !uidPattern.containsMatchIn(t)) {
                                    foundName = t
                                    Log.d("AadharTextAnalyzer", "Found Name Heuristic: $foundName")
                                    break
                                }
                            }
                            if (foundName.isNotEmpty()) break
                        }
                    }

                    if (foundUid.isNotEmpty() || foundName.isNotEmpty()) {
                        val details = IdentityDetails(
                            docNumber = foundUid,
                            name = foundName,
                            dob = foundDob,
                            sex = foundGender,
                            rawText = text
                        )
                        Log.d("AadharTextAnalyzer", "Final Details: $details")
                        onResult(details)
                    }
                }
                .addOnFailureListener {
                    // Fail silently or log
                }
    }
}
