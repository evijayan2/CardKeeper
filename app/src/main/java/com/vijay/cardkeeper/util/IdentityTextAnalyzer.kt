package com.vijay.cardkeeper.util

import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class IdentityTextAnalyzer(
        private val isBackSide: Boolean,
        private val onResult: (IdentityDetails) -> Unit
) : ImageAnalysis.Analyzer {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    // Regex
    private val datePattern =
            Regex(
                    "\\b(0[1-9]|1[0-2])[/-](0[1-9]|[12][0-9]|3[01])[/-](\\d{4})\\b|\\b(\\d{4})[/-](0[1-9]|1[0-2])[/-](0[1-9]|[12][0-9]|3[01])\\b"
            )

    // Improved DL Regex:
    // 1. Generic: 6-15 alphanumeric chars, allow dashes/spaces, but require at least one uppercase
    // letter and digit interaction.
    private val idNumberPattern = Regex("\\b(?=[A-Z0-9- ]*\\d)[A-Z0-9- ]{6,18}\\b")
    // 2. Specific Keywords: Look for "DL" or "NO" or "LIC" followed by text
    private val dlKeywordPattern =
            Regex("\\b(?:DL|LIC|ID|NO)\\s*[:.]?\\s*([A-Z0-9- ]{5,20})\\b", RegexOption.IGNORE_CASE)

    // New Patterns
    private val sexPattern = Regex("\\b(?:SEX|S)[:\\s]*([MF])\\b")
    private val heightPattern = Regex("\\b(?:HGT|H)[:\\s]*(\\d['-]\\d{1,2})\\b") // 5-08 or 5'08
    private val eyesPattern = Regex("\\b(?:EYES|E)[:\\s]*([A-Z]{3})\\b") // BRO, BLU
    private val classPattern = Regex("\\b(?:CLASS|C)[:\\s]+([A-Z0-9]+)\\b")
    private val endPattern = Regex("\\b(?:END|E)[:\\s]*([A-Z0-9]+)\\b") // Endorsements
    private val restrPattern = Regex("\\b(?:RESTR|R)[:\\s]*([A-Z0-9]+)\\b") // Restrictions

    // States (Simplified list of 2-letter codes for heuristic)
    private val stateCodes =
            setOf(
                    "AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "FL", "GA",
                    "HI", "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD",
                    "MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH", "NJ",
                    "NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "RI", "SC",
                    "SD", "TN", "TX", "UT", "VT", "VA", "WA", "WV", "WI", "WY"
            )

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
                    Log.d("IdentityTextAnalyzer", "Raw OCR Text: $text")

                    if (isBackSide) {
                        if (text.length > 20) {
                            onResult(IdentityDetails(rawText = text))
                        }
                        return@addOnSuccessListener
                    }

                    // Front Side Analysis
                    var foundName = ""
                    var foundDocId = ""
                    var foundDob = ""
                    var foundExp = ""
                    var foundState = ""
                    var foundSex = ""
                    var foundHeight = ""
                    var foundEyes = ""
                    var foundClass = ""
                    var foundEnd = ""
                    var foundRestr = ""
                    var foundIss = ""

                    // 1. Text Block Analysis
                    for (block in visionText.textBlocks) {
                        for (line in block.lines) {
                            val lineText = line.text.uppercase()

                            // State (Top 1/3 usually)
                            if (foundState.isEmpty()) {
                                for (code in stateCodes) {
                                    if (lineText == code || lineText == "$code USA") {
                                        foundState = code
                                        Log.d("IdentityTextAnalyzer", "Found State: $foundState")
                                    }
                                }
                            }

                            // Doc ID
                            if (foundDocId.isEmpty()) {
                                val keywordMatch = dlKeywordPattern.find(line.text)
                                if (keywordMatch != null) {
                                    foundDocId = keywordMatch.groupValues[1].trim()
                                    Log.d("IdentityTextAnalyzer", "Found DocID (Keyword): $foundDocId")
                                } else {
                                    val match = idNumberPattern.find(line.text)
                                    if (match != null &&
                                                    !lineText.contains("DL") &&
                                                    !lineText.contains("NO") &&
                                                    !lineText.contains("LIC")
                                    ) {
                                        foundDocId = match.value.trim()
                                        Log.d("IdentityTextAnalyzer", "Found DocID (Pattern): $foundDocId")
                                    }
                                }
                            }

                            // Dates (DOB vs Exp)
                            val dateMatches = datePattern.findAll(line.text)
                            for (match in dateMatches) {
                                val valDate = match.value
                                if (lineText.contains("DOB") || lineText.contains("BIRTH")) {
                                    foundDob = valDate
                                    Log.d("IdentityTextAnalyzer", "Found DOB: $foundDob")
                                } else if (lineText.contains("EXP") || valDate.startsWith("202")) {
                                    foundExp = valDate
                                    Log.d("IdentityTextAnalyzer", "Found Expiry: $foundExp")
                                } else if (lineText.contains("ISS")) {
                                    foundIss = valDate
                                    Log.d("IdentityTextAnalyzer", "Found IssueDate: $foundIss")
                                } else if (foundDob.isEmpty()) {
                                    foundDob = valDate 
                                }
                            }

                            // Specific Fields
                            sexPattern.find(lineText)?.let { foundSex = it.groupValues[1] }
                            heightPattern.find(lineText)?.let { foundHeight = it.groupValues[1] }
                            eyesPattern.find(lineText)?.let { foundEyes = it.groupValues[1] }
                            classPattern.find(lineText)?.let { foundClass = it.groupValues[1] }
                            endPattern.find(lineText)?.let { foundEnd = it.groupValues[1] }
                            restrPattern.find(lineText)?.let { foundRestr = it.groupValues[1] }
                        }
                    }

                    // 2. Name Heuristic
                    for (block in visionText.textBlocks) {
                        for (blockLine in block.lines) {
                            val t = blockLine.text
                            if (foundName.isEmpty() &&
                                            t.matches(Regex("^[A-Z, ]+$")) &&
                                            t.length > 5 &&
                                            !t.contains("USA") &&
                                            !t.contains("DRIVER")
                            ) {
                                if (foundState.isNotEmpty() && t.contains(foundState)) continue
                                foundName = t
                                Log.d("IdentityTextAnalyzer", "Found Name: $foundName")
                            }
                        }
                    }

                    if (foundDocId.isNotEmpty() || (foundName.isNotEmpty() && foundDob.isNotEmpty())
                    ) {
                        val details = IdentityDetails(
                                        docNumber = foundDocId,
                                        name = foundName,
                                        firstName = foundName.substringBefore(" ").trim(),
                                        lastName = foundName.substringAfterLast(" ", "").trim(),
                                        dob = foundDob,
                                        expiryDate = foundExp,
                                        state = foundState,
                                        sex = foundSex,
                                        height = foundHeight,
                                        eyeColor = foundEyes,
                                        licenseClass = foundClass,
                                        restrictions = foundRestr,
                                        endorsements = foundEnd,
                                        issueDate = foundIss
                                        )
                        Log.d("IdentityTextAnalyzer", "Final Details: $details")
                        onResult(details)
                    }
                }
                .addOnFailureListener {
                    Log.e("IdentityTextAnalyzer", "OCR Processing failed", it)
                }
    }
}
