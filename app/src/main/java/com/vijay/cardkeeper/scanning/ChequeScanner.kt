package com.vijay.cardkeeper.scanning

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

/** Data class to hold extracted cheque information */
data class ChequeDetails(
        val bankName: String = "",
        val accountNumber: String = "",
        val routingNumber: String = "",
        val chequeNumber: String = "",
        val ifscCode: String = "",
        val holderName: String = "",
        val holderAddress: String = "",
        val rawText: String = "",
        val extractionSummary: String = "" // For user feedback
)

/**
 * Scanner for bank cheques using ML Kit Text Recognition. Supports US (E-13B), European (CMC-7),
 * and Indian cheque formats. Extracts MICR line data (routing number, account number) from cheque
 * images.
 */
class ChequeScanner {

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    companion object {
        private const val TAG = "ChequeScanner"
    }

    /**
     * Scans a cheque image and extracts banking information. US MICR format: ⑆ROUTING⑆ ⑈ACCOUNT⑈
     * CHEQUE# Indian MICR format: City(3) + Bank(3) + Branch(3) digits
     */
    suspend fun scan(bitmap: Bitmap): ChequeDetails {
        val image = InputImage.fromBitmap(bitmap, 0)

        return try {
            val result = textRecognizer.process(image).await()
            val fullText = result.text
            Log.d(TAG, "=== Full OCR Text ===\n$fullText\n=====================")

            // Collect all number sequences from the text
            val allNumbers = extractAllNumbers(fullText)
            Log.d(TAG, "All numbers found: $allNumbers")

            // Try US format first (9-digit routing)
            var micrData = extractUSFormat(fullText, allNumbers)
            var format = "US"

            // If US format failed, try Indian format
            if (micrData.routingNumber.isEmpty() && micrData.accountNumber.isEmpty()) {
                micrData = extractIndianFormat(fullText, allNumbers)
                format = "Indian"
            }

            // Extract IFSC code if present (Indian banks)
            val ifscCode = extractIFSCCode(fullText)

            // Extract bank name
            val bankName = extractBankName(result.textBlocks)

            // Extract holder info (usually top-left)
            val holderInfo = extractHolderInfo(fullText)

            // Build extraction summary for user feedback
            val summary = buildSummary(micrData, ifscCode, bankName, format)
            Log.d(TAG, summary)

            ChequeDetails(
                    bankName = bankName,
                    accountNumber = micrData.accountNumber,
                    routingNumber = micrData.routingNumber,
                    chequeNumber = micrData.chequeNumber,
                    ifscCode = ifscCode,
                    holderName = holderInfo.first,
                    holderAddress = holderInfo.second,
                    rawText = fullText,
                    extractionSummary = summary
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning cheque", e)
            ChequeDetails(extractionSummary = "Scan failed: ${e.message}")
        }
    }

    /**
     * Extracts holder name and address from cheque text. Heuristic: Usually top-left, first few
     * lines that aren't the bank name.
     */
    private fun extractHolderInfo(text: String): Pair<String, String> {
        val lines = text.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) return Pair("", "")

        // Simple heuristic: First line is name, next 1-2 lines are address
        // Skip lines that look like bank names or are very long
        val potentialName =
                lines.firstOrNull { line ->
                    line.length > 3 &&
                            !line.lowercase().contains("bank") &&
                            !line.any { it.isDigit() }
                }
                        ?: ""

        val nameIndex = lines.indexOf(potentialName)
        val addressBuilder = StringBuilder()
        if (nameIndex != -1) {
            for (i in nameIndex + 1 until (nameIndex + 4).coerceAtMost(lines.size)) {
                val line = lines[i]
                if (line.any { it.isDigit() } || line.contains(",")) {
                    addressBuilder.append(line).append(" ")
                }
            }
        }

        return Pair(potentialName, addressBuilder.toString().trim())
    }
    /** Extracts all number sequences from text, useful for finding MICR data. */
    private fun extractAllNumbers(text: String): List<String> {
        return Regex("\\d{3,}")
                .findAll(text)
                .map { it.value }
                .toList()
    }

    /**
     * US Format: 9-digit routing number (with ABA checksum), account number, cheque number MICR
     * Line order (left to right): ⑆ROUTING⑆ ⑈ACCOUNT⑈ CHEQUE# Routing is ALWAYS exactly 9 digits
     * Account is typically 10-17 digits Cheque number is shorter (4-6 digits typically)
     */
    private fun extractUSFormat(text: String, numbers: List<String>): MICRData {
        Log.d(TAG, "Attempting US format extraction from numbers: $numbers")

        val micrLine = text.lines().find { it.contains("⑆") || it.contains("⑈") }
        val numbersToUse =
                if (micrLine != null) {
                    Log.d(TAG, "MICR line detected for US format: $micrLine")
                    Regex("\\d+").findAll(micrLine).map { it.value }.toList()
                } else {
                    numbers
                }
        Log.d(TAG, "Numbers being used for US format extraction: $numbersToUse")

        // Step 1: Find the 9-digit routing number.
        // We prioritize those that pass the checksum.
        var routingNumber = ""
        val nineDigitNumbers = numbersToUse.filter { it.length == 9 }

        for (candidate in nineDigitNumbers) {
            if (isValidUSRoutingNumber(candidate)) {
                routingNumber = candidate
                break
            }
        }

        // If no checksum match, take the first 9-digit number.
        if (routingNumber.isEmpty()) {
            routingNumber = nineDigitNumbers.firstOrNull() ?: ""
        }

        // Step 2: Account number is the longest number remaining (8-17 digits)
        val accountCandidates = numbersToUse.filter { it != routingNumber && it.length in 8..17 }
        val accountNumber = accountCandidates.maxByOrNull { it.length } ?: ""

        // Step 3: Cheque number is typically the shortest remaining number
        val chequeNumber =
                numbersToUse
                        .filter { it != routingNumber && it != accountNumber && it.length in 3..10 }
                        .minByOrNull { it.length }
                        ?: ""

        return MICRData(routingNumber, accountNumber, chequeNumber)
    }

    /**
     * Indian Format: MICR is 9 digits (City 3 + Bank 3 + Branch 3) Account numbers are typically
     * 10-18 digits
     */
    private fun extractIndianFormat(text: String, numbers: List<String>): MICRData {
        // Look for IFSC pattern first (4 letters + 0 + 6 alphanumeric)
        val ifscPattern = Regex("[A-Z]{4}0[A-Z0-9]{6}")
        val ifscMatch = ifscPattern.find(text.uppercase())

        // Indian MICR is exactly 9 digits but NOT a valid US routing (different checksum)
        val potentialMicr = numbers.filter { it.length == 9 }
        val potentialAccounts = numbers.filter { it.length in 10..18 }

        val micrCode =
                potentialMicr.firstOrNull { !isValidUSRoutingNumber(it) }
                        ?: potentialMicr.firstOrNull() ?: ""
        val accountNumber = potentialAccounts.maxByOrNull { it.length } ?: ""

        Log.d(
                TAG,
                "Indian Format - MICR: $micrCode, Account: $accountNumber, IFSC: ${ifscMatch?.value}"
        )
        return MICRData(micrCode, accountNumber, "")
    }

    /**
     * Extract IFSC code from text (Indian bank identifier) Format: 4 letters + 0 + 6 alphanumeric
     * (e.g., SBIN0001234)
     */
    private fun extractIFSCCode(text: String): String {
        val ifscPattern = Regex("[A-Z]{4}0[A-Z0-9]{6}")
        return ifscPattern.find(text.uppercase())?.value ?: ""
    }

    /**
     * Validates US routing number using ABA checksum algorithm. Formula: 3*(d1+d4+d7) +
     * 7*(d2+d5+d8) + (d3+d6+d9) mod 10 == 0
     */
    private fun isValidUSRoutingNumber(routing: String): Boolean {
        if (routing.length != 9) return false
        return try {
            val d = routing.map { it.digitToInt() }
            val checksum =
                    3 * (d[0] + d[3] + d[6]) + 7 * (d[1] + d[4] + d[7]) + (d[2] + d[5] + d[8])
            val valid = checksum % 10 == 0
            Log.d(TAG, "Routing $routing checksum: $checksum, valid: $valid")
            valid
        } catch (e: Exception) {
            false
        }
    }

    /** Extracts bank name from text blocks (usually largest text at top). */
    private fun extractBankName(
            textBlocks: List<com.google.mlkit.vision.text.Text.TextBlock>
    ): String {
        val bankKeywords =
                listOf(
                        "bank",
                        "credit union",
                        "savings",
                        "federal",
                        "national",
                        "chase",
                        "wells",
                        "citi",
                        "boa",
                        "usaa",
                        "pnc",
                        "td",
                        // Indian banks
                        "sbi",
                        "hdfc",
                        "icici",
                        "axis",
                        "kotak",
                        "pnb",
                        "canara",
                        "union"
                )

        for (block in textBlocks.take(5)) {
            val text = block.text.lowercase()
            if (bankKeywords.any { text.contains(it) }) {
                return block.text.trim()
            }
        }

        return textBlocks.firstOrNull { it.text.length > 5 }?.text?.trim() ?: ""
    }

    /** Builds a human-readable summary of what was extracted */
    private fun buildSummary(micr: MICRData, ifsc: String, bank: String, format: String): String {
        val parts = mutableListOf<String>()

        if (micr.routingNumber.isNotEmpty()) parts.add("Routing: ${micr.routingNumber}")
        if (micr.accountNumber.isNotEmpty()) parts.add("Account: ${micr.accountNumber}")
        if (ifsc.isNotEmpty()) parts.add("IFSC: $ifsc")
        if (bank.isNotEmpty()) parts.add("Bank: $bank")

        return if (parts.isEmpty()) {
            "No banking info detected"
        } else {
            "[$format] ${parts.joinToString(", ")}"
        }
    }

    /** Masks middle portion of account number for display */
    private fun maskMiddle(number: String): String {
        return number // Removed masking as per user request
    }

    private data class MICRData(
            val routingNumber: String,
            val accountNumber: String,
            val chequeNumber: String
    )
}
