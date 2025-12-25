package com.vijay.cardkeeper.util

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
    private val classPattern = Regex("\\b(?:CLASS|C)[:\\s]*([A-Z0-9]+)\\b")
    private val endPattern = Regex("\\b(?:END|E)[:\\s]*([A-Z0-9]+)\\b") // Endorsements
    private val restrPattern = Regex("\\b(?:RESTR|R)[:\\s]*([A-Z0-9]+)\\b") // Restrictions
    private val issPattern =
            Regex(
                    "\\b(?:ISS|ISSUED\\s+BY|AUTH)[:\\.]?\\s*([A-Za-z0-9\\s,]+)\\b",
                    RegexOption.IGNORE_CASE
            )

    // States (Simplified list of 2-letter codes for heuristic)
    private val stateCodes =
            setOf(
                    "AL",
                    "AK",
                    "AZ",
                    "AR",
                    "CA",
                    "CO",
                    "CT",
                    "DE",
                    "FL",
                    "GA",
                    "HI",
                    "ID",
                    "IL",
                    "IN",
                    "IA",
                    "KS",
                    "KY",
                    "LA",
                    "ME",
                    "MD",
                    "MA",
                    "MI",
                    "MN",
                    "MS",
                    "MO",
                    "MT",
                    "NE",
                    "NV",
                    "NH",
                    "NJ",
                    "NM",
                    "NY",
                    "NC",
                    "ND",
                    "OH",
                    "OK",
                    "OR",
                    "PA",
                    "RI",
                    "SC",
                    "SD",
                    "TN",
                    "TX",
                    "UT",
                    "VT",
                    "VA",
                    "WA",
                    "WV",
                    "WI",
                    "WY"
            )

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            val inputImage = InputImage.fromMediaImage(mediaImage, rotationDegrees)

            // For identity, we also sometimes need the bitmap itself for "capturedImage"
            // But if we are using document scanner, we already have the bitmap.
            // When using CameraX ImageAnalysis, we convert proxy to bitmap.

            analyze(inputImage) { details ->
                // Re-attach the bitmap from proxy if the analysis didn't provide it (it won't,
                // since InputImage doesn't carry it easily)
                // Wait, existing logic: `val bitmap = imageProxy.toBitmap()`
                // It does that inside onSuccess.
                // Refactoring to keep it clean: pass a bitmap provider or just the bitmap?
                // ImageProxy.toBitmap() needs the proxy open.

                // If I pass InputImage, I lose the ability to call imageProxy.toBitmap().
                // However, the new flow (Doc Scanner) provides a Bitmap directly.
                // The old flow (Analyzer) provides an ImageProxy.

                // Solution: The internal analyze method shouldn't worry about the bitmap for the
                // Result *unless* it's needed for analysis (it's not).
                // It should return the text details. The caller can attach the bitmap.

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
                                    // Very loose check: Line is just "CA" or "CA USA" or
                                    // matches "CALIFORNIA"
                                    if (lineText == code || lineText == "$code USA") {
                                        foundState = code
                                    }
                                }
                            }

                            // Doc ID
                            if (foundDocId.isEmpty()) {
                                // 1. Try keyword match first (stronger signal)
                                val keywordMatch = dlKeywordPattern.find(line.text)
                                if (keywordMatch != null) {
                                    foundDocId = keywordMatch.groupValues[1].trim()
                                } else {
                                    // 2. Fallback to generic pattern
                                    val match = idNumberPattern.find(line.text)
                                    if (match != null &&
                                                    !lineText.contains("DL") &&
                                                    !lineText.contains("NO") &&
                                                    !lineText.contains("LIC")
                                    ) {
                                        foundDocId = match.value.trim()
                                    }
                                }
                            }

                            // Dates (DOB vs Exp)
                            val dateMatches = datePattern.findAll(line.text)
                            for (match in dateMatches) {
                                val valDate = match.value
                                // context?
                                if (lineText.contains("DOB") || lineText.contains("BIRTH")) {
                                    foundDob = valDate
                                } else if (lineText.contains("EXP") || valDate.startsWith("202")) {
                                    foundExp = valDate
                                } else if (foundDob.isEmpty()) {
                                    foundDob = valDate // Fallback
                                }
                            }

                            // Specific Fields
                            sexPattern.find(lineText)?.let { foundSex = it.groupValues[1] }
                            heightPattern.find(lineText)?.let { foundHeight = it.groupValues[1] }
                            eyesPattern.find(lineText)?.let { foundEyes = it.groupValues[1] }
                            classPattern.find(lineText)?.let { foundClass = it.groupValues[1] }
                            endPattern.find(lineText)?.let { foundEnd = it.groupValues[1] }
                            restrPattern.find(lineText)?.let { foundRestr = it.groupValues[1] }
                            // ISS might be mixed case on some cards, but we UpperCased
                            // lineText.
                            // Re-run find on original text if needed, or just rely on Upper.
                            issPattern.find(line.text)?.let {
                                // Clean up common noise
                                var v = it.groupValues[1].trim()
                                if (v.length > 20) v = v.take(20)
                                foundIss = v
                            }
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
                            }
                        }
                    }

                    if (foundDocId.isNotEmpty() || (foundName.isNotEmpty() && foundDob.isNotEmpty())
                    ) {
                        onResult(
                                IdentityDetails(
                                        docNumber = foundDocId,
                                        name = foundName,
                                        dob = foundDob,
                                        expiryDate = foundExp,
                                        state = foundState,
                                        sex = foundSex,
                                        height = foundHeight,
                                        eyeColor = foundEyes,
                                        licenseClass = foundClass,
                                        restrictions = foundRestr,
                                        endorsements = foundEnd,
                                        issuingAuthority = foundIss
                                        // caller attaches capturedImage
                                        )
                        )
                    }
                }
                .addOnFailureListener {
                    // handle failure
                }
    }
}
