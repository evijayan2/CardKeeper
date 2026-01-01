package com.vijay.cardkeeper.scanning

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.vijay.cardkeeper.data.entity.Passport
import com.vijay.cardkeeper.ui.common.DateFormatType
import com.vijay.cardkeeper.util.DateNormalizer
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class PassportScanner {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun scanFront(bitmap: Bitmap): Passport = suspendCancellableCoroutine { cont ->
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer
                .process(image)
                .addOnSuccessListener { visionText ->
                    val text = visionText.text
                    Log.d("PassportScanner", "Raw OCR Text: $text")

                    // 1. Try MRZ Parsing
                    var passport =
                            parseMrz(text)
                                    ?: Passport(
                                            passportNumber = "",
                                            countryCode = "",
                                            frontImagePath = null // to be filled by UI logic
                                    )

                    // 2. Try Visual Parsing for additional fields
                    val lines = text.split("\n")
                    val visualData = parseFrontVisualFields(lines)
                    Log.d("PassportScanner", "Visual Data Extracted: $visualData")

                    val countryCode = passport.countryCode
                    val dateFormatType = if (countryCode.uppercase() == "IND") DateFormatType.INDIA else DateFormatType.USA

                    // 3. Merge Data
                    passport =
                            passport.copy(
                                    nationality = visualData["nationality"] ?: passport.nationality,
                                    dateOfIssue = visualData["dateOfIssue"]?.let { 
                                        DateNormalizer.normalize(it, dateFormatType) 
                                    } ?: passport.dateOfIssue,
                                    placeOfBirth = visualData["placeOfBirth"]
                                                    ?: passport.placeOfBirth,
                                    placeOfIssue = visualData["placeOfIssue"]
                                                    ?: passport.placeOfIssue,
                                    authority = visualData["authority"] ?: passport.authority
                            )

                    Log.d("PassportScanner", "Final Passport: $passport")
                    if (cont.isActive) cont.resume(passport)
                }
                .addOnFailureListener {
                    if (cont.isActive) cont.resume(Passport(passportNumber = "", countryCode = ""))
                }
    }

    private fun parseFrontVisualFields(lines: List<String>): Map<String, String> {
        val data = mutableMapOf<String, String>()

        var pendingKey: String? = null

        for (line in lines) {
            val cleanLine = line.trim()
            val upper = cleanLine.uppercase()
            if (cleanLine.isBlank()) continue

            // 1. If we have a pending key, this line is likely the value
            if (pendingKey != null) {
                // Heuristic: values are usually all caps or contain digits, and shouldn't contains label keywords
                if (!isLikelyKeyword(upper)) {
                    // If it was a date field, we expect digits
                    if (pendingKey.contains("date", ignoreCase = true)) {
                        val digits = cleanLine.filter { it.isDigit() }
                        if (digits.length >= 6) {
                            data[pendingKey] = cleanLine
                            pendingKey = null
                            continue
                        }
                    } else {
                        data[pendingKey] = cleanLine
                        pendingKey = null
                        continue
                    }
                }
                
                // If we hit another keyword or noise, cancel current pending
                if (isLikelyKeyword(upper)) {
                    pendingKey = null 
                }
            }

            // 2. Check for keywords and extract value if present on same line
            // Indian labels often contain Hindi translations, so we check for presence of both key English terms
            // Values are often on the NEXT line, so extractValueAfterKeyword returning empty is expected.
            
            if (upper.contains("NATIONALITY")) {
                val value = extractValueAfterKeyword(line, "NATIONALITY")
                if (value.isNotEmpty()) data["nationality"] = value else pendingKey = "nationality"
            }
            // Use specific checks to avoid double-matching
            if (upper.contains("PLACE") && (upper.contains("BIRTH") || upper.contains("BIRTHDATE"))) {
                val value = extractValueAfterKeyword(line, if (upper.contains("BIRTHDATE")) "BIRTHDATE" else "BIRTH")
                if (value.isNotEmpty()) data["placeOfBirth"] = value.substringBefore(" ").trim() else pendingKey = "placeOfBirth"
            }
            if (upper.contains("PLACE") && upper.contains("ISSUE")) {
                val kw = if (upper.indexOf("ISSUE") > upper.indexOf("PLACE")) "ISSUE" else "PLACE"
                val value = extractValueAfterKeyword(line, kw)
                if (value.isNotEmpty()) {
                    // Try to isolate place from potential date on same line
                    val cleanValue = value.substringBefore("DATE").trim()
                    if (cleanValue.isNotEmpty()) data["placeOfIssue"] = cleanValue
                } else {
                    pendingKey = "placeOfIssue"
                }
            }
            if (upper.contains("DATE") && upper.contains("ISSUE")) {
                val kw = if (upper.indexOf("ISSUE") > upper.indexOf("DATE")) "ISSUE" else "DATE"
                val value = extractValueAfterKeyword(line, kw)
                if (value.isNotEmpty()) {
                    val cleanValue = value.substringBefore("PLACE").trim()
                    if (cleanValue.isNotEmpty()) data["dateOfIssue"] = cleanValue
                } else {
                    pendingKey = "dateOfIssue"
                }
            }
            if (upper.contains("AUTHORITY")) {
                val value = extractValueAfterKeyword(line, "AUTHORITY")
                if (value.isNotEmpty()) data["authority"] = value else pendingKey = "authority"
            }
        }
        return data
    }

    private fun isLikelyKeyword(text: String): Boolean {
        val keywords = listOf("PLACE", "DATE", "AUTHORITY", "NATIONALITY", "PASSPORT", "SURNAME", "GIVEN")
        return keywords.any { text.contains(it) }
    }

    private fun extractValueAfterKeyword(line: String, keyword: String): String {
        val upper = line.uppercase()
        val index = upper.indexOf(keyword)
        if (index == -1) return ""

        // Find the end of the label section (looking for : or long space or Hindi suffix)
        // In Indian passports: "Nationality / <Hindi> : INDIAN"
        val remaining = line.substring(index + keyword.length)
        
        // Split by colon or prominent space
        val parts = remaining.split(Regex("[:\\s/\\u0900-\\u097F]+"))
        // Recover the part that looks like actual data (usually last few parts if separated by Hindi)
        // Or just rejoin everything after the colon if present
        if (remaining.contains(":")) {
            return remaining.substringAfter(":").trim()
        }
        
        // Fallback: take the last non-Hindi word if it's long enough or looks like a value
        return parts.filter { it.isNotBlank() && !it.any { c -> c in '\u0900'..'\u097F' } }
                    .joinToString(" ").trim()
    }

    suspend fun scanBack(bitmap: Bitmap): Passport = suspendCancellableCoroutine { cont ->
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer
                .process(image)
                .addOnSuccessListener { visionText ->
                    val lines = visionText.text.split("\n")
                    val details = parseBackSide(lines)
                    if (cont.isActive) cont.resume(details)
                }
                .addOnFailureListener {
                    if (cont.isActive) cont.resume(Passport(passportNumber = "", countryCode = ""))
                }
    }

    private fun parseMrz(text: String): Passport? {
        val lines = text.split("\n").filter { it.length > 30 } // Filter short noise lines
        // Look for 44-char lines (MRZ-TD3)
        // Or 36-char lines (MRZ-TD2) - Passport usually TD3
        // Simple heuristic: Find line starting with 'P<' or 'P[' (OCR error)

        val mrzLines = lines.filter { it.contains("<") }.sortedByDescending { it.length }
        if (mrzLines.size < 2) return null

        // Try to identify the two MRZ lines
        var line1 = ""
        var line2 = ""

        for (i in 0 until mrzLines.size - 1) {
            val l1 = mrzLines[i].replace(" ", "")
            val l2 = mrzLines[i + 1].replace(" ", "")
            if (l1.startsWith("P") && l1.contains("<<") && l2.length == l1.length) {
                line1 = l1
                line2 = l2
                break
            }
        }

        if (line1.isEmpty()) {
            // Fallback: just take bottom 2 long lines
            if (lines.size >= 2) {
                line2 = lines.last().replace(" ", "")
                line1 = lines[lines.size - 2].replace(" ", "")
            } else {
                return null
            }
        }

        try {
            // Line 1 Parsing
            // P<Indices<<Given<Names<<<<<<<<<<<<<<<<<<
            // 0: P
            // 1: Type
            // 2-4: Country
            val countryCode = line1.substring(2, 5).replace("<", "")
            val dateFormatType = if (countryCode.uppercase() == "IND") DateFormatType.INDIA else DateFormatType.USA
            val nameSection = line1.substring(5).split("<<")
            val surname = nameSection.getOrNull(0)?.replace("<", " ")?.trim() ?: ""
            val givenNames = nameSection.getOrNull(1)?.replace("<", " ")?.trim() ?: ""

            // Line 2 Parsing
            // Num<...DobSExp...
            // 0-8: Passport No
            val passportNumber = line2.substring(0, 9).replace("<", "")
            val nationality = line2.substring(10, 13).replace("<", "")
            val dobRaw = line2.substring(13, 19)
            // 19: Check
            val sex = line2.substring(20, 21)
            val expiryRaw = line2.substring(21, 27)

            return Passport(
                    passportNumber = passportNumber,
                    countryCode = countryCode,
                    surname = surname,
                    givenNames = givenNames,
                    nationality = nationality,
                    dob = formatDate(dobRaw, dateFormatType),
                    sex = sex,
                    dateOfExpiry = formatDate(expiryRaw, dateFormatType)
            )
        } catch (e: Exception) {
            return null
        }
    }

    private fun formatDate(yymmdd: String, dateFormatType: DateFormatType): String {
        if (yymmdd.length != 6) return yymmdd
        val yy = yymmdd.substring(0, 2)
        val mm = yymmdd.substring(2, 4)
        val dd = yymmdd.substring(4, 6)
        // Simple pivot for year: if > 50 assume 19xx, else 20xx
        val yearPrefix = if (yy.toInt() > 50) "19" else "20"
        val dmt = "${yearPrefix}${yy}-${mm}-${dd}"
        return DateNormalizer.normalize(dmt, dateFormatType = dateFormatType)
    }

    private fun parseBackSide(lines: List<String>): Passport {
        var fatherName: String? = null
        var motherName: String? = null
        var spouseName: String? = null
        var fileNumber: String? = null
        var addressBuffer = StringBuilder()

        // State tracking for multi-line values
        var expectingFatherName = false
        var expectingMotherName = false
        var expectingSpouseName = false
        var expectingFileNumber = false
        var capturingAddress = false

        for (line in lines) {
            val upper = line.uppercase()
            val cleanLine = line.trim()
            if (cleanLine.isBlank()) continue

            // 1. Check if we are satisfying a pending expectation from the previous line
            if (expectingFatherName) {
                fatherName = cleanLine
                expectingFatherName = false
                continue
            }
            if (expectingMotherName) {
                motherName = cleanLine
                expectingMotherName = false
                continue
            }
            if (expectingSpouseName) {
                spouseName = cleanLine
                expectingSpouseName = false
                continue
            }
            if (expectingFileNumber) {
                fileNumber = cleanLine
                expectingFileNumber = false
                continue
            }

            // 2. Identify Keys
            if (upper.contains("FATHER") || upper.contains("LEGAL GUARDIAN")) {
                val valInLine = extractNameValue(line)
                if (valInLine.isNotEmpty()) {
                    fatherName = valInLine
                } else {
                    expectingFatherName = true
                }
                capturingAddress = false
            } else if (upper.contains("MOTHER")) {
                val valInLine = extractNameValue(line)
                if (valInLine.isNotEmpty()) {
                    motherName = valInLine
                } else {
                    expectingMotherName = true
                }
                capturingAddress = false
            } else if (upper.contains("SPOUSE")) {
                val valInLine = extractNameValue(line)
                if (valInLine.isNotEmpty()) {
                    spouseName = valInLine
                } else {
                    expectingSpouseName = true
                }
                capturingAddress = false
            } else if (upper.contains("FILE") && upper.contains("NO")) {
                val valInLine = extractNameValue(line)
                if (valInLine.isNotEmpty()) {
                    fileNumber = valInLine
                } else {
                    expectingFileNumber = true
                }
                capturingAddress = false
            } else if (upper.contains("ADDRESS")) {
                capturingAddress = true
                val addrStart = extractNameValue(line)
                if (addrStart.isNotEmpty()) addressBuffer.append(addrStart).append(", ")
            } else if (upper.contains("PIN") && upper.filter { it.isDigit() }.length == 6) {
                // Address likely ends here or close
                addressBuffer.append(line)
                capturingAddress = false
            } else if (capturingAddress) {
                // Continuation of address
                if (!upper.contains("OLD PASSPORT") && !upper.contains("FILE")) {
                    addressBuffer.append(line).append(", ")
                } else {
                    capturingAddress = false
                }
            }
        }

        return Passport(
                passportNumber = "", // Not on back usually
                countryCode = "",
                fatherName = fatherName,
                motherName = motherName,
                spouseName = spouseName,
                fileNumber = fileNumber,
                address = addressBuffer.toString().trim().removeSuffix(",")
        )
    }

    private fun extractNameValue(line: String): String {
        // "Name of Father : JOHN DOE" -> "JOHN DOE"
        // "Father's Name ... JOHN DOE"
        val parts = line.split(":", limit = 2)
        if (parts.size > 1) {
            return parts[1].trim()
        }
        // Heuristic: If no colon, maybe just split by known keywords? Hard without structure.
        return ""
    }
}
