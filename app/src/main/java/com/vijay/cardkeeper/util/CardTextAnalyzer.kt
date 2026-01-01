package com.vijay.cardkeeper.util

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class CardTextAnalyzer(private val onCardFound: (CardDetails) -> Unit) : ImageAnalysis.Analyzer {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    // Regex Patterns
    private val numberPattern = Regex("\\b(?:\\d[ -]*?){13,19}\\b")
    private val expiryPattern = Regex("\\b(0[1-9]|1[0-2])[/-](\\d{2}|\\d{4})\\b")
    private val datePattern = Regex("\\b\\d{2}/\\d{2}\\b") // Backup date pattern
    private val cvvPattern = Regex("\\b\\d{3,4}\\b") // Weak signal, context dependent
    private val debitPattern = Regex("(?i)DEBIT")
    private val creditPattern = Regex("(?i)CREDIT")

    // Known Banks (Simple list for heuristic)
    private val knownBanks =
            listOf(
                    "CHASE",
                    "CITI",
                    "BOFA",
                    "BANK OF AMERICA",
                    "WELLS FARGO",
                    "AMEX",
                    "AMERICAN EXPRESS",
                    "CAPITAL ONE",
                    "DISCOVER",
                    "HDFC",
                    "ICICI",
                    "SBI",
                    "AXIS",
                    "KOTAK",
                    "PNC",
                    "US BANK"
            )

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            analyze(image) { details ->
                onCardFound(details)
                imageProxy.close()
            }
        } else {
            imageProxy.close()
        }
    }

    fun analyze(image: InputImage, onResult: (CardDetails) -> Unit) {
        recognizer
                .process(image)
                .addOnSuccessListener { visionText ->
                    android.util.Log.d("CardTextAnalyzer", "Raw OCR Text: ${visionText.text}")
                    var foundNumber = ""
                    var foundExpiry = ""
                    var foundOwner = ""
                    var foundBank = ""
                    var foundType = "Credit" // Default

                    // 1. Analyze entire text blocks for general keywords
                    val allText = visionText.text
                    if (debitPattern.containsMatchIn(allText)) foundType = "Debit"

                    // 2. Iterate blocks for specific fields
                    for (block in visionText.textBlocks) {
                        for (line in block.lines) {
                            val lineText = line.text

                            // Bank Detection
                            if (foundBank.isEmpty()) {
                                val upperLine = lineText.uppercase()
                                knownBanks.firstOrNull { upperLine.contains(it) }?.let {
                                    foundBank = it
                                }
                            }

                            // Number Detection
                            if (foundNumber.isEmpty()) {
                                val match = numberPattern.find(lineText)
                                if (match != null) {
                                    val clean = match.value.replace(Regex("[^0-9]"), "")
                                    if (clean.length in 13..19) {
                                        foundNumber = clean
                                    }
                                }
                            }

                            // Expiry Detection
                            if (foundExpiry.isEmpty()) {
                                // Look for "VALID THRU" or similar context lines nearby (not
                                // implemented fully here),
                                // but simpler regex match first
                                val match = expiryPattern.find(lineText)
                                if (match != null) {
                                    foundExpiry = match.value
                                }
                            }

                            // Potential Name Detection
                            // Heuristic: Matches name pattern, not a bank, not a keyword.
                            // Removed strict foundNumber dependency to allow name capture even if
                            // number fails/is elsewhere,
                            // though we prioritize lines that look like names.
                            if (foundOwner.isEmpty()) {
                                val upperText = lineText.uppercase()
                                val isKeyword =
                                        listOf(
                                                        "VALID",
                                                        "THRU",
                                                        "FROM",
                                                        "UNTIL",
                                                        "SINCE",
                                                        "MEMBER",
                                                        "AUTHORIZED",
                                                        "SIGNATURE",
                                                        "CARD",
                                                        "DEBIT",
                                                        "CREDIT",
                                                        "ELECTRONIC",
                                                        "USE",
                                                        "ONLY",
                                                        "TELLER",
                                                        "GOOD"
                                                )
                                                .any { upperText.contains(it) }

                                val isBank = knownBanks.any { upperText.contains(it) }

                                // Regex: Allow letters, spaces, dots, hyphens, apostrophes. Min
                                // length 5.
                                if (!isKeyword &&
                                                !isBank &&
                                                lineText.matches(Regex("^[a-zA-Z\\s\\.\\-']+$")) &&
                                                lineText.length > 5 &&
                                                lineText.split(" ").size > 1
                                ) {
                                    // If we found a number, assume name is AFTER number?
                                    // Or just capture the first candidate that fits well.
                                    // For now, capture first valid candidate, but if we found
                                    // number, ensure this line is arguably "below" or
                                    // separate.
                                    // MLKit blocks usually flow top-down.
                                    // Warning: This might capture "PLATINUM" or "REWARDS" if not in
                                    // keywords.
                                    // Let's add "PLATINUM", "BUSINESS", "REWARDS", "WORLD" to
                                    // keywords/exclusions if needed.
                                    // Checking if it seems to be a name (2+ words).
                                    foundOwner = lineText
                                }
                            }
                        }
                    }

                    if (foundNumber.isNotEmpty()) {
                        // Infer Scheme from number
                        val scheme =
                                when {
                                    foundNumber.startsWith("4") -> "Visa"
                                    foundNumber.startsWith("5") -> "MasterCard"
                                    foundNumber.startsWith("34") || foundNumber.startsWith("37") ->
                                            "Amex"
                                    foundNumber.startsWith("6") -> "Discover"
                                    else -> "Unknown"
                                }

                        onResult(
                                CardDetails(
                                        number = foundNumber,
                                        expiryDate = foundExpiry,
                                        ownerName = foundOwner,
                                        bankName = foundBank,
                                        cardType = foundType,
                                        scheme = scheme,
                                        securityCode =
                                                "" // Hard to detect reliability from front scan
                                )
                        )
                    }
                }
                .addOnFailureListener {
                    // Handle failure if needed, mainly for debugging
                }
    }
}
