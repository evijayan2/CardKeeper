package com.vijay.cardkeeper.scanning

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

/** Data class to hold extracted insurance card information */
data class InsuranceCardDetails(
    val providerName: String = "",
    val memberId: String = "",
    val groupNumber: String = "",
    val policyNumber: String = "",
    val planName: String = "",
    val policyHolderName: String = "",
    val rawText: String = ""
)

/**
 * Scanner for insurance cards using ML Kit Text Recognition.
 * Extracts Provider, Member ID, Group Number, Policy Number, etc.
 */
class InsuranceCardScanner {

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    companion object {
        private const val TAG = "InsuranceCardScanner"
    }

    suspend fun scan(bitmap: Bitmap): InsuranceCardDetails {
        val image = InputImage.fromBitmap(bitmap, 0)
        Log.d(TAG, "Starting scan of Insurance Card")

        return try {
            val result = textRecognizer.process(image).await()
            val fullText = result.text
            Log.d(TAG, "=== Full OCR Text ===\n$fullText\n=====================")

            val lines = fullText.split("\n").map { it.trim() }.filter { it.isNotEmpty() }

            // 1. Extract Provider Name
            // Heuristic: Usually the largest text or the first few lines that are not labels like "Member ID"
            val providerName = extractProviderName(lines, result.textBlocks)

            // 2. Extract Member ID
            // Look for "ID", "Member ID", "No", "#" followed by alphanumeric
            val memberId = extractField(lines, listOf("Member ID", "ID", "Member No", "Member #", "Identification"), true)

            // 3. Extract Group Number
            val groupNumber = extractField(lines, listOf("Group", "Grp", "Group No", "Group #"), true)

            // 4. Extract Policy Number
            // Sometimes called "Policy", "Pol", "Plan ID"
            val policyNumber = extractField(lines, listOf("Policy", "Pol", "Subscriber ID"), true)

            // 5. Extract Plan Name
            // Harder, look for "Plan", "PPO", "HMO", "EPO"
            val planName = extractPlanName(lines)

            // 6. Extract Policy Holder Name
            // Look for "Name", "Subscriber Name"
            val policyHolderName = extractField(lines, listOf("Name", "Member Name", "Subscriber"), false)

            Log.d(TAG, "Extracted: Provider=$providerName, ID=$memberId, Group=$groupNumber, Policy=$policyNumber, Plan=$planName")

            InsuranceCardDetails(
                providerName = providerName,
                memberId = memberId,
                groupNumber = groupNumber,
                policyNumber = policyNumber,
                planName = planName,
                policyHolderName = policyHolderName,
                rawText = fullText
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning insurance card", e)
            InsuranceCardDetails(rawText = "Error: ${e.message}")
        }
    }

    private fun extractProviderName(lines: List<String>, textBlocks: List<com.google.mlkit.vision.text.Text.TextBlock>): String {
        // Known providers
        val knownProviders = listOf("BlueCross", "BlueShield", "Aetna", "UnitedHealthcare", "Cigna", "Humana", "Kaiser", "Anthem", "Medicare", "Medicaid")
        
        // 1. Check for known providers in first 5 lines
        for (line in lines.take(5)) {
            val normalized = line.replace(" ", "").lowercase()
            for (provider in knownProviders) {
                if (normalized.contains(provider.lowercase())) {
                    return line // Return the original line as it looks better
                }
            }
        }

        // 2. Fallback: Take the first line that isn't a label and has reasonable length
        // Skipping common header words
        val skipWords = listOf("Health", "Insurance", "Plan", "Network", "Member", "ID")
        for (line in lines.take(3)) {
            if (line.length > 3 && !skipWords.any { line.contains(it, ignoreCase = true) }) {
                return line
            }
        }
        
        return lines.firstOrNull() ?: ""
    }

    private fun extractField(lines: List<String>, keywords: List<String>, isAlphanumeric: Boolean): String {
        for (line in lines) {
            for (keyword in keywords) {
                if (line.contains(keyword, ignoreCase = true)) {
                    // Two cases:
                    // 1. "Member ID: 12345" (Same line)
                    // 2. "Member ID" \n "12345" (Next line logic - difficult with simple line iteration, 
                    //    but sometimes the OCR merges them or we can split the line)
                    
                    val parts = line.split(keyword, ignoreCase = true)
                    if (parts.size > 1) {
                        var value = parts[1].trim()
                        // Remove common separators like :, #
                        value = value.trimStart { !it.isLetterOrDigit() }
                        
                        if (value.isNotEmpty()) {
                             if (isAlphanumeric) {
                                // Filter mostly alphanumeric, maybe dashes
                                // value = value.filter { it.isLetterOrDigit() || it == '-' }
                             }
                             return value
                        }
                    }
                }
            }
        }
        // Fallback for multi-line (Keyword on one line, value on next)?
        // Basic scan: if a line equals a keyword, take next line
        for (i in 0 until lines.size - 1) {
            val line = lines[i]
            for (keyword in keywords) {
                 if (line.equals(keyword, ignoreCase = true) || line.trimEnd(':').equals(keyword, ignoreCase = true)) {
                     return lines[i+1]
                 }
            }
        }
        return ""
    }
    
    private fun extractPlanName(lines: List<String>): String {
        // Look for keywords PPO, HMO, EPO, POS
        val planTypes = listOf("PPO", "HMO", "EPO", "POS", "Choice Plus", "Open Access")
        for (line in lines) {
             for (type in planTypes) {
                 if (line.contains(type, ignoreCase = true)) {
                     return line
                 }
             }
        }
        // Look for "Plan:"
        val extractedRules = extractField(lines, listOf("Plan", "Plan Name"), false)
        if (extractedRules.isNotEmpty()) return extractedRules
        
        return ""
    }
}
