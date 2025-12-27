package com.vijay.cardkeeper.scanning

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.vijay.cardkeeper.data.model.GreenCardMrz
import com.vijay.cardkeeper.util.IdentityDetails
import kotlinx.coroutines.tasks.await

class GreenCardScanner {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val TAG = "GreenCardScanner"

    suspend fun scan(bitmap: Bitmap): IdentityDetails {
        val image = InputImage.fromBitmap(bitmap, 0)
        return try {
            val result = recognizer.process(image).await()
            Log.d(TAG, "Raw OCR text:\n${result.text}")
            parseText(result.text)
        } catch (e: Exception) {
            Log.e(TAG, "OCR failed: ${e.message}")
            IdentityDetails()
        }
    }

    private fun parseText(text: String): IdentityDetails {
        val lines = text.split("\n").map { it.trim() }.filter { it.isNotEmpty() }

        Log.d(TAG, "Parsing ${lines.size} lines")

        var surname = ""
        var givenName = ""
        var uscisNumber = ""
        var category = ""
        var countryOfBirth = ""
        var dob = ""
        var sex = ""
        var expiryDate = ""
        var residentSince = ""
        var mrzParsedSuccessfully = false

        // 1. Try MRZ Parsing - look for lines with < characters (MRZ separator)
        val mrzLines =
                lines.filter { line ->
                    val cleaned = line.replace(" ", "")
                    cleaned.length >= 20 && cleaned.contains("<")
                }

        Log.d(TAG, "Found ${mrzLines.size} potential MRZ lines")

        if (mrzLines.isNotEmpty()) {
            val mrz = parseGreenCardMrz(mrzLines)
            mrzParsedSuccessfully = true
            surname = mrz.lastName
            givenName = mrz.firstName
            uscisNumber = mrz.documentNumber
            category = mrz.documentCode
            countryOfBirth = mrz.nationality
            dob = mrz.dateOfBirth
            sex = mrz.sex
            expiryDate = mrz.expiryDate
        }

        Log.d(
                TAG,
                "Final parsed: surname=$surname, given=$givenName, uscis=$uscisNumber, dob=$dob, expiry=$expiryDate"
        )

        return IdentityDetails(
                name = if (givenName.isNotEmpty()) "$givenName $surname".trim() else surname,
                firstName = givenName,
                lastName = surname,
                docNumber = uscisNumber,
                dob = formatDate(dob),
                sex = sex,
                expiryDate = formatDate(expiryDate),
                countryOfBirth = countryOfBirth,
                residentSince = formatDate(residentSince),
                category = category,
                uscisNumber = uscisNumber,
                isMrzData = mrzParsedSuccessfully
        )
    }

    private fun formatDate(yymmdd: String): String {
        val clean = yymmdd.replace("O", "0").replace("I", "1").replace("S", "5")
        if (clean.length != 6) return yymmdd
        val yy = clean.substring(0, 2)
        val mm = clean.substring(2, 4)
        val dd = clean.substring(4, 6)
        val yearPrefix =
                try {
                    if (yy.toInt() > 50) "19" else "20"
                } catch (e: Exception) {
                    "20"
                }
        return "${yearPrefix}${yy}-${mm}-${dd}"
    }

    fun parseGreenCardMrz(lines: List<String>): GreenCardMrz {
        require(lines.size == 3) { "MRZ must have exactly 3 lines" }
        require(lines.all { it.length == 30 }) { "Each MRZ line must be exactly 30 characters" }

        val line1 = lines[0]
        val line2 = lines[1]
        val line3 = lines[2]

        // ---- Line 1 ----
        val documentCode = line1.substring(0, 2)
        val issuingCountry = line1.substring(2, 5)
        val documentNumber = line1.substring(5, 14).replace("<", "")
        val documentNumberCheckDigit = line1[14]

        // ---- Line 2 ----
        val dateOfBirth = line2.substring(0, 6)
        val dobCheckDigit = line2[6]
        val sex = line2.substring(7, 8)
        val expiryDate = line2.substring(8, 14)
        val expiryCheckDigit = line2[14]
        val nationality = line2.substring(15, 18)

        // ---- Line 3 ----
        val nameParts = line3.split("<<")
        val lastName = nameParts[0].replace("<", " ").trim()
        val firstName = nameParts.getOrNull(1)?.replace("<", " ")?.trim() ?: ""

        return GreenCardMrz(
                documentCode = documentCode,
                issuingCountry = issuingCountry,
                documentNumber = documentNumber,
                documentNumberCheckDigit = documentNumberCheckDigit,
                dateOfBirth = dateOfBirth,
                dobCheckDigit = dobCheckDigit,
                sex = sex,
                expiryDate = expiryDate,
                expiryCheckDigit = expiryCheckDigit,
                nationality = nationality,
                lastName = lastName,
                firstName = firstName
        )
    }
}
