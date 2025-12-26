package com.vijay.cardkeeper.scanning

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.vijay.cardkeeper.util.IdentityDetails
import kotlinx.coroutines.tasks.await

/**
 * Scanner for driver licenses using PDF417 barcode scanning. Parses AAMVA-format data from the
 * barcode on the back of US driver licenses.
 */
class DriverLicenseScanner {

    // Configure scanner to specifically look for PDF417 and other common DL formats
    private val options =
            BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(
                            Barcode.FORMAT_PDF417,
                            Barcode.FORMAT_DATA_MATRIX,
                            Barcode.FORMAT_QR_CODE,
                            Barcode.FORMAT_AZTEC
                    )
                    .build()

    private val barcodeScanner = BarcodeScanning.getClient(options)

    /**
     * Scans a bitmap for PDF417 barcode and extracts driver license information. Returns
     * IdentityDetails with parsed fields.
     */
    suspend fun scan(bitmap: Bitmap): IdentityDetails {
        val image = InputImage.fromBitmap(bitmap, 0)
        Log.d("DriverLicenseScanner", "Starting scan of bitmap: ${bitmap.width}x${bitmap.height}")

        return try {
            val barcodes = barcodeScanner.process(image).await()
            Log.d("DriverLicenseScanner", "Found ${barcodes.size} barcodes")

            // Look for PDF417 barcode (standard for driver licenses)
            val dlBarcode =
                    barcodes.firstOrNull {
                        it.format == Barcode.FORMAT_PDF417 ||
                                it.format == Barcode.FORMAT_DATA_MATRIX
                    }
                            ?: barcodes.firstOrNull()

            if (dlBarcode != null) {
                Log.d(
                        "DriverLicenseScanner",
                        "Barcode format: ${dlBarcode.format}, rawValue length: ${dlBarcode.rawValue?.length ?: 0}"
                )
                if (dlBarcode.rawValue != null) {
                    Log.d(
                            "DriverLicenseScanner",
                            "Raw data preview: ${dlBarcode.rawValue!!.take(100)}"
                    )
                    parseAAMVAData(dlBarcode.rawValue!!)
                } else {
                    Log.d("DriverLicenseScanner", "Barcode rawValue is null")
                    IdentityDetails()
                }
            } else {
                Log.d("DriverLicenseScanner", "No barcode found in image")
                IdentityDetails()
            }
        } catch (e: Exception) {
            Log.e("DriverLicenseScanner", "Error scanning barcode", e)
            IdentityDetails()
        }
    }

    /**
     * Parses AAMVA format data from driver license barcode. AAMVA standard uses specific field
     * codes like: DAC = First Name, DCS = Last Name, DBB = DOB, etc. This is public so it can be
     * called directly with raw barcode data from CameraX.
     */
    fun parseAAMVAData(rawData: String): IdentityDetails {
        val fields = mutableMapOf<String, String>()

        // AAMVA format: Each field starts with a 3-letter code followed by value, ending with
        // newline or specific delimiter
        val lines = rawData.split("\n", "\r\n", "\r")

        for (line in lines) {
            if (line.length >= 3) {
                val code = line.take(3).uppercase()
                val value = line.drop(3).trim()
                if (value.isNotEmpty()) {
                    fields[code] = value
                }
            }
        }

        // Also try regex-based parsing for single-line formats
        val regex = Regex("([A-Z]{3})([^A-Z]{2,})")
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
        val docNumber = fields["DAQ"] ?: fields["DCA"] ?: "" // License number
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

        val eyeColor = fields["DAY"] ?: "" // Eye color
        val height = fields["DAU"] ?: "" // Height
        val licenseClass = fields["DCA"] ?: "" // License class
        val restrictions = fields["DCB"] ?: "" // Restrictions
        val endorsements = fields["DCD"] ?: "" // Endorsements
        val state = fields["DAJ"] ?: "" // State/jurisdiction (e.g., PA)
        val country = fields["DCG"] ?: "USA" // Country (DCG = USA for US licenses)
        val issuingAuthority = state // For DL, the state IS the issuing authority
        val issueDate = fields["DBD"] ?: "" // Issue date

        // Format dates if they're in MMDDYYYY or YYYYMMDD format
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
