package com.vijay.cardkeeper.domain.utils

import com.vijay.cardkeeper.domain.model.IdentityDetails

class AamvaParser {

    /**
     * Parses AAMVA format data from driver license barcode.
     */
    fun parse(rawData: String): IdentityDetails {
        val fields = mutableMapOf<String, String>()

        // AAMVA format: Each field starts with a 3-letter code followed by value
        val lines = rawData.split("\n", "\r\n", "\r", "\\n")

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.length >= 3) {
                val possibleCode = trimmed.take(3)
                if (possibleCode.all { it.isUpperCase() }) {
                     val value = trimmed.drop(3).trim()
                     if (value.isNotEmpty()) {
                        fields[possibleCode] = value
                     }
                }
            }
        }

        // Regex parsing as fallback
        val regex = Regex("([A-Z]{3})(.+?)(?=$|[A-Z]{3})")
        regex.findAll(rawData).forEach { match ->
            val code = match.groupValues[1]
            val value = match.groupValues[2].trim()
            
            if (value.isNotEmpty() && !fields.containsKey(code)) {
                fields[code] = value
            }
        }

        // Extract standard AAMVA fields
        val firstName = fields["DAC"] ?: fields["DCT"] ?: "" // First name
        val lastName = fields["DCS"] ?: fields["DAB"] ?: "" // Last name
        val middleName = fields["DAD"] ?: ""
        val fullName =
                buildString {
                            if (firstName.isNotEmpty()) append(firstName)
                            if (middleName.isNotEmpty()) append(" $middleName")
                            if (lastName.isNotEmpty()) append(" $lastName")
                        }
                        .trim()

        val dob = fields["DBB"] ?: "" // Date of birth (MMDDYYYY or YYYYMMDD)
        val expiryDate = fields["DBA"] ?: "" // Expiration date
        
        var docNumber = fields["DAQ"] ?: ""
        if (docNumber.isEmpty()) {
            val ans = fields["ANS"] ?: ""
            if (ans.contains("DAQ")) {
                docNumber = ans.substringAfter("DAQ")
                    .split(Regex("[\\s\\n\\r]"))
                    .first()
                    .split(Regex("(?=[A-Z]{3})"))
                    .first()
                    .trim()
            }
        }
        
        val address =
                buildString {
                            val street = fields["DAG"] ?: ""
                            val city = fields["DAI"] ?: ""
                            val state = fields["DAJ"] ?: ""
                            val zip = fields["DAK"] ?: ""
                            if (street.isNotEmpty()) append(street)
                            if (city.isNotEmpty()) append(", $city")
                            if (state.isNotEmpty()) append(", $state")
                            if (zip.isNotEmpty()) append(" $zip")
                        }
                        .trim()
                        .trimStart(',')
                        .trim()

        val sex =
                when (fields["DBC"]) {
                    "1" -> "M"
                    "2" -> "F"
                    else -> fields["DBC"] ?: ""
                }

        val eyeColor = fields["DAY"] ?: ""
        val height = fields["DAU"] ?: ""
        val licenseClass = fields["DCA"] ?: ""
        val restrictions = fields["DCB"] ?: ""
        val endorsements = fields["DCD"] ?: ""
        val state = fields["DAJ"] ?: ""
        val country = fields["DCG"] ?: "USA"
        val issuingAuthority = state
        val issueDate = fields["DBD"] ?: ""

        // Format dates
        val formattedDob = formatDate(dob)
        val formattedExpiry = formatDate(expiryDate)
        val formattedIssueDate = formatDate(issueDate)

        return IdentityDetails(
                docNumber = docNumber,
                name = fullName,
                dob = formattedDob,
                expiryDate = formattedExpiry,
                address = address,
                sex = sex,
                eyeColor = eyeColor,
                height = height,
                licenseClass = licenseClass,
                restrictions = restrictions,
                endorsements = endorsements,
                state = state,
                issuingAuthority = issuingAuthority,
                issueDate = formattedIssueDate,
                country = country
        )
    }

    private fun formatDate(date: String): String {
        if (date.isEmpty()) return ""

        // Try MMDDYYYY format
        if (date.length == 8 && date.all { it.isDigit() }) {
            return try {
                val month = date.substring(0, 2)
                val day = date.substring(2, 4)
                val year = date.substring(4, 8)
                "$month/$day/$year"
            } catch (e: Exception) {
                date
            }
        }
        return date
    }
}
