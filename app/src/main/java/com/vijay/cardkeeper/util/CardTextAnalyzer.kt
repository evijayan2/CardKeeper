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
    
    // Scheme Patterns (Text based)
    private val rupayPattern = Regex("(?i)RUPAY")
    private val visaPattern = Regex("(?i)VISA")
    private val masterCardPattern = Regex("(?i)MASTER\\s?CARD")
    private val maestroPattern = Regex("(?i)MAESTRO")
    private val amexPattern = Regex("(?i)(AMERICAN\\s?EXPRESS|AMEX)")
    private val discoverPattern = Regex("(?i)DISCOVER")
    private val phonePattern = Regex("(?i)(?:(?:\\+|00)1[-. ]?)?\\(?([2-9][0-8][0-9])\\)?[-. ]?([2-9][0-9]{2})[-. ]?([0-9]{4})")

    // Known Banks (Expanded list)
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
                    "STATE BANK OF INDIA",
                    "AXIS",
                    "KOTAK",
                    "MAHINDRA",
                    "PNC",
                    "US BANK",
                    "IDFC",
                    "BOB",
                    "BANK OF BARODA",
                    "PNB",
                    "PUNJAB NATIONAL BANK",
                    "CANARA",
                    "UNION BANK",
                    "INDUSIND",
                    "YES BANK",
                    "RBL",
                    "HSBC",
                    "STANDARD CHARTERED",
                    "SC",
                    "DBS",
                    "FEDERAL BANK",
                    "IDBI",
                    "INDIAN BANK"
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
                    var foundScheme = ""
                    var foundCvv = ""
                    var potentialOwner = ""
                    var foundPhone = ""

                    // 1. Analyze entire text blocks for general keywords
                    val allText = visionText.text
                    if (debitPattern.containsMatchIn(allText)) foundType = "Debit"

                    // Scheme detection via Text
                    if (rupayPattern.containsMatchIn(allText)) foundScheme = "RuPay"
                    else if (visaPattern.containsMatchIn(allText)) foundScheme = "Visa"
                    else if (masterCardPattern.containsMatchIn(allText)) foundScheme = "MasterCard"
                    else if (maestroPattern.containsMatchIn(allText)) foundScheme = "Maestro"
                    else if (amexPattern.containsMatchIn(allText)) foundScheme = "Amex"
                    else if (discoverPattern.containsMatchIn(allText)) foundScheme = "Discover"

                    // 2. Iterate blocks for specific fields
                    for (block in visionText.textBlocks) {
                        for (line in block.lines) {
                            val lineText = line.text

                            // Bank Detection
                            if (foundBank.isEmpty()) {
                                val upperLine = lineText.uppercase()
                                knownBanks.firstOrNull { upperLine.contains(it) }?.let {
                                    // Expand short names
                                    foundBank = when(it) {
                                        "SBI" -> "State Bank of India"
                                        "BOB" -> "Bank of Baroda"
                                        "PNB" -> "Punjab National Bank"
                                        "SC" -> "Standard Chartered"
                                        "BOFA" -> "Bank of America"
                                        else -> it
                                    }
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
                                val match = expiryPattern.find(lineText)
                                if (match != null) {
                                    foundExpiry = match.value
                                }
                            }

                            // Potential Name Detection
                            if (foundOwner.isEmpty()) {
                                val upperText = lineText.uppercase()
                                val isKeyword =
                                        listOf(
                                                "VALID", "THRU", "FROM", "UNTIL", "SINCE",
                                                "MEMBER", "AUTHORIZED", "SIGNATURE", "CARD",
                                                "DEBIT", "CREDIT", "ELECTRONIC", "USE", "ONLY",
                                                "TELLER", "GOOD", "RuPay", "VISA", "MASTER", "MAESTRO", "AMEX",
                                                "CVV", "CVC", "CID", "SECURITY", "CODE"
                                        ).any { upperText.contains(it.uppercase()) }

                                val isBank = knownBanks.any { upperText.contains(it) }

                                if (!isKeyword && !isBank &&
                                    lineText.matches(Regex("^[a-zA-Z\\s\\.\\-']+$")) &&
                                    lineText.length > 5 &&
                                    lineText.split(" ").size > 1
                                ) {
                                    // Only store as candidate; verify presence of number later
                                    if (potentialOwner.isEmpty()) { 
                                        potentialOwner = lineText 
                                        android.util.Log.d("CardTextAnalyzer", "Potential Owner found: $potentialOwner")
                                    }
                                } else {
                                     android.util.Log.d("CardTextAnalyzer", "Line rejected as owner: $lineText (Keyword=$isKeyword, Bank=$isBank)")
                                }
                            }
                            
                            // CVV Detection (Back of card or Front Amex)
                            if (foundCvv.isEmpty()) {
                                val upperLine = lineText.uppercase()
                                val cvvKeywords = listOf("CVV", "CVC", "CID", "CVV2", "CVC2", "SECURITY CODE")
                                val hasKeyword = cvvKeywords.any { upperLine.contains(it) }
                                
                                if (hasKeyword) {
                                    val match = cvvPattern.find(lineText)
                                    if (match != null) {
                                        foundCvv = match.value
                                    }
                                } else {
                                    // Heuristic for isolated 3-4 digit numbers (risky, but valid for some backs)
                                    val possibleCvv = cvvPattern.find(lineText)?.value
                                    if (possibleCvv != null) {
                                        val isYear = possibleCvv.length == 4 && (possibleCvv.startsWith("20") || possibleCvv.startsWith("19"))
                                        if (!isYear && possibleCvv != foundExpiry.replace("/", "")) {
                                             // Store as candidate if needed, but for now relying on keywords or future improvements
                                            foundCvv = possibleCvv
                                        }
                                    }
                                }
                            }
                            
                            // Phone Number Detection
                            if (foundPhone.isEmpty()) {
                                // Simple regex check
                                val match = phonePattern.find(lineText)
                                if (match != null) {
                                    foundPhone = match.value
                                }
                            }
                        }
                    }

                    // 4. Finalize Owner Name (Only if Number is present, per user request)
                    if (foundNumber.isNotEmpty() && potentialOwner.isNotEmpty()) {
                        foundOwner = potentialOwner
                    }

                    // 3. Consistency checks & Final Scheme Logic
                    var finalScheme = if (foundScheme.isNotEmpty()) foundScheme else "Unknown"
                    
                    if (foundNumber.isNotEmpty()) {
                        val numberScheme = when {
                            foundNumber.startsWith("4") -> "Visa"
                            foundNumber.startsWith("5") -> "MasterCard"
                            foundNumber.startsWith("34") || foundNumber.startsWith("37") -> "Amex"
                            foundNumber.startsWith("6") -> {
                                if (foundNumber.startsWith("60") || foundNumber.startsWith("65")) "RuPay"
                                else "Discover"
                            }
                            // RuPay ranges: 60, 65, 81, 82, 508
                            foundNumber.startsWith("81") || foundNumber.startsWith("82") -> "RuPay"
                            foundNumber.startsWith("508") -> "RuPay"
                            // Maestro check (50, 56-58, 6...) - simplified
                            foundNumber.startsWith("50") || foundNumber.startsWith("56") || foundNumber.startsWith("57") || foundNumber.startsWith("58") -> "Maestro"
                            else -> "Unknown"
                        }
                        
                        // Prioritize Number Scheme if known
                        if (numberScheme != "Unknown") {
                            finalScheme = numberScheme
                        }
                    }

                    // ALWAYS return result to avoid hanging
                    val finalDetails = CardDetails(
                            number = foundNumber,
                            expiryDate = foundExpiry,
                            ownerName = foundOwner,
                            bankName = foundBank,
                            cardType = foundType,
                            scheme = finalScheme,
                            securityCode = foundCvv,
                            phoneNumber = foundPhone
                    )
                    android.util.Log.d("CardTextAnalyzer", "Analysis Complete. Returning: $finalDetails")
                    onResult(finalDetails)
                }
                .addOnFailureListener { e ->
                     // Even on failure, we should probably return empty to unblock?
                     // Or let it be. But usually onSuccess is called even if no text.
                     // Failure is for exceptions.
                     android.util.Log.e("CardTextAnalyzer", "Text analysis failed", e)
                     onResult(CardDetails("", "", "", "", "", "Unknown", "", ""))
                }
    }
}
