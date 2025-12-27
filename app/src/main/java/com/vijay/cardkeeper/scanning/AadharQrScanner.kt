package com.vijay.cardkeeper.scanning

import android.content.Context
import android.util.Base64
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.security.PublicKey
import java.security.Signature
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.zip.GZIPInputStream

/**
 * Aadhar Secure QR Code Scanner with UIDAI signature verification.
 *
 * Secure QR format:
 * 1. Base10-encoded BigInteger
 * 2. GZip compressed byte array
 * 3. Fields separated by delimiter byte 255
 * 4. Last 256 bytes are SHA256withRSA signature
 */
class AadharQrScanner(private val context: Context) {

    data class AadharQrResult(
            val referenceId: String = "",
            val name: String = "",
            val dob: String = "",
            val gender: String = "",
            val careOf: String = "",
            val district: String = "",
            val landmark: String = "",
            val house: String = "",
            val location: String = "",
            val pincode: String = "",
            val postOffice: String = "",
            val state: String = "",
            val street: String = "",
            val subDistrict: String = "",
            val vtc: String = "",
            val photoBase64: String? = null,
            val maskedAadhaar: String = "",
            val mobileHash: ByteArray? = null,
            val emailHash: ByteArray? = null,
            val email: String? = null,
            val mobile: String? = null,
            val signatureValid: Boolean = false,
            val signatureVerificationAttempted: Boolean = false,
            val rawQrData: String = "",
            val fullAddress: String = ""
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as AadharQrResult
            return referenceId == other.referenceId && maskedAadhaar == other.maskedAadhaar
        }

        override fun hashCode(): Int {
            var result = referenceId.hashCode()
            result = 31 * result + maskedAadhaar.hashCode()
            return result
        }
    }

    private val publicKeys = mutableListOf<PublicKey>()

    init {
        loadPublicKeys()
    }

    private fun loadPublicKeys() {
        try {
            val certFiles = context.assets.list("certs") ?: return
            val certificateFactory = CertificateFactory.getInstance("X.509")

            for (fileName in certFiles) {
                try {
                    context.assets.open("certs/$fileName").use { inputStream ->
                        val certificate =
                                certificateFactory.generateCertificate(inputStream) as
                                        X509Certificate
                        publicKeys.add(certificate.publicKey)
                        android.util.Log.d("AadharQrScanner", "Loaded certificate: $fileName")
                    }
                } catch (e: Exception) {
                    android.util.Log.w(
                            "AadharQrScanner",
                            "Failed to load cert $fileName: ${e.message}"
                    )
                }
            }
            android.util.Log.d("AadharQrScanner", "Total loaded keys: ${publicKeys.size}")
        } catch (e: Exception) {
            android.util.Log.e("AadharQrScanner", "Could not list certificates: ${e.message}")
        }
    }

    /** Parse Aadhar Secure QR code data. Supports both V1 (older) and V2 (current) QR formats. */
    fun parse(qrData: String): AadharQrResult {
        return try {
            android.util.Log.d("AadharQrScanner", "Parsing QR data of length: ${qrData.length}")
            if (qrData.length > 50) {
                android.util.Log.d("AadharQrScanner", "QR Data Start: ${qrData.substring(0, 50)}")
            } else {
                android.util.Log.d("AadharQrScanner", "QR Data: $qrData")
            }

            // Check if it's XML (Legacy QR)
            if (qrData.trim().startsWith("<") || qrData.contains("<?xml")) {
                android.util.Log.w("AadharQrScanner", "Detected Legacy XML QR format")
                return parseXml(qrData)
            }

            // Step 1: Convert Base10 string to BigInteger, then to byte array
            val bigInt = BigInteger(qrData)
            var compressedBytes = bigInt.toByteArray()
            android.util.Log.d("AadharQrScanner", "BigInt bytes length: ${compressedBytes.size}")

            // BigInteger may add a leading 0x00 byte for positive sign
            if (compressedBytes.isNotEmpty() && compressedBytes[0] == 0.toByte()) {
                compressedBytes = compressedBytes.copyOfRange(1, compressedBytes.size)
                android.util.Log.d(
                        "AadharQrScanner",
                        "Trimmed sign byte, new length: ${compressedBytes.size}"
                )
            }

            // Step 2: Decompress using GZip
            val decompressedBytes = decompress(compressedBytes)
            android.util.Log.d(
                    "AadharQrScanner",
                    "Decompressed bytes length: ${decompressedBytes.size}"
            )

            // Check QR version based on first byte after decompression
            val version =
                    if (decompressedBytes.isNotEmpty()) {
                        decompressedBytes[0].toInt() and 0xFF
                    } else 0
            android.util.Log.d("AadharQrScanner", "QR Version byte: $version")

            // For V2: First byte is version (2), rest follows
            // For V1/older: No version byte, starts with indicator
            val isV2 = version == 2

            // Step 3: Extract signature (last 256 bytes)
            val signatureBytes =
                    decompressedBytes.copyOfRange(
                            decompressedBytes.size - 256,
                            decompressedBytes.size
                    )
            val dataBytes = decompressedBytes.copyOfRange(0, decompressedBytes.size - 256)
            android.util.Log.d(
                    "AadharQrScanner",
                    "Data bytes length: ${dataBytes.size}, Signature bytes: ${signatureBytes.size}"
            )

            // Step 4: Verify signature on complete data (excluding signature)
            val (signatureValid, verificationAttempted) = verifySignature(dataBytes, signatureBytes)
            android.util.Log.d(
                    "AadharQrScanner",
                    "Signature valid: $signatureValid, attempted: $verificationAttempted"
            )

            // Step 5: Parse fields by delimiter 255
            // Skip version byte if V2
            val dataForParsing =
                    if (isV2 && dataBytes.isNotEmpty()) {
                        dataBytes.copyOfRange(1, dataBytes.size)
                    } else {
                        dataBytes
                    }
            val fields = splitByDelimiter(dataForParsing, 255.toByte())
            android.util.Log.d("AadharQrScanner", "Parsed ${fields.size} fields")

            // Parse fields based on Aadhar QR structure
            parseFields(fields, signatureValid, verificationAttempted, qrData)
        } catch (e: Exception) {
            android.util.Log.e("AadharQrScanner", "Failed to parse QR: ${e.message}", e)
            AadharQrResult(rawQrData = qrData)
        }
    }

    private fun decompress(compressed: ByteArray): ByteArray {
        // GZip decompress the byte array
        return try {
            GZIPInputStream(ByteArrayInputStream(compressed)).use { gzip ->
                val buffer = ByteArrayOutputStream()
                val temp = ByteArray(1024)
                var len: Int
                while (gzip.read(temp).also { len = it } != -1) {
                    buffer.write(temp, 0, len)
                }
                buffer.toByteArray()
            }
        } catch (e: Exception) {
            android.util.Log.e("AadharQrScanner", "Decompression failed: ${e.message}")
            throw e
        }
    }

    private fun splitByDelimiter(data: ByteArray, delimiter: Byte): List<ByteArray> {
        val result = mutableListOf<ByteArray>()
        var start = 0

        for (i in data.indices) {
            if (data[i] == delimiter) {
                result.add(data.copyOfRange(start, i))
                start = i + 1
            }
        }

        // Add remaining bytes
        if (start < data.size) {
            result.add(data.copyOfRange(start, data.size))
        }

        return result
    }

    private fun verifySignature(data: ByteArray, signature: ByteArray): Pair<Boolean, Boolean> {
        if (publicKeys.isEmpty()) {
            android.util.Log.w("AadharQrScanner", "No public keys available for verification")
            return Pair(false, false)
        }

        // Ensure Bouncy Castle provider is registered
        if (java.security.Security.getProvider("BC") == null) {
            java.security.Security.addProvider(org.bouncycastle.jce.provider.BouncyCastleProvider())
        }

        val algorithms = listOf("SHA256withRSA", "SHA1withRSA")

        for (key in publicKeys) {
            for (algo in algorithms) {
                try {
                    // android.util.Log.d("AadharQrScanner", "Verifying with key: ${key.algorithm},
                    // Algo: $algo")
                    val sig = Signature.getInstance(algo, "BC")
                    sig.initVerify(key)
                    sig.update(data)
                    if (sig.verify(signature)) {
                        android.util.Log.d(
                                "AadharQrScanner",
                                "Signature Verified Successfully with $algo!"
                        )
                        return Pair(true, true)
                    }
                } catch (e: Exception) {
                    android.util.Log.w(
                            "AadharQrScanner",
                            "Verification failed with $algo: ${e.message}"
                    )
                }
            }
        }

        android.util.Log.e(
                "AadharQrScanner",
                "Signature verification failed with all ${publicKeys.size} keys and algorithms"
        )
        return Pair(false, true)
    }

    private fun parseFields(
            fields: List<ByteArray>,
            signatureValid: Boolean,
            verificationAttempted: Boolean,
            rawQrData: String
    ): AadharQrResult {
        // Standard field order in Aadhar Secure QR:
        // 0: Email+Mobile indicator (1 byte)
        // 1: Reference ID
        // 2: Name
        // 3: DOB
        // 4: Gender
        // 5: Care Of (C/O)
        // 6: District
        // 7: Landmark
        // 8: House
        // 9: Location
        // 10: Pincode
        // 11: Post Office
        // 12: State
        // 13: Street
        // 14: Sub District
        // 15: VTC (Village/Town/City)
        // 16: Photo (JPEG bytes)
        // 17+: Mobile hash (32 bytes) / Email hash (32 bytes) based on indicator
        // Last: Masked Aadhaar (last 4 digits)

        if (fields.isEmpty()) return AadharQrResult(rawQrData = rawQrData)

        val indicator = if (fields[0].isNotEmpty()) fields[0][0].toInt() and 0xFF else 0

        fun getString(index: Int): String {
            return if (index < fields.size) {
                String(fields[index], Charsets.ISO_8859_1).trim()
            } else ""
        }

        val referenceId = getString(1)
        val name = getString(2)
        val dob = getString(3)
        val gender = getString(4)
        val careOf = getString(5)
        val district = getString(6)
        val landmark = getString(7)
        val house = getString(8)
        val location = getString(9)
        val pincode = getString(10)
        val postOffice = getString(11)
        val state = getString(12)
        val street = getString(13)
        val subDistrict = getString(14)
        val vtc = getString(15)

        // Photo is at index 16 (JPEG bytes)
        val photoBase64 =
                if (fields.size > 16 && fields[16].isNotEmpty()) {
                    Base64.encodeToString(fields[16], Base64.NO_WRAP)
                } else null

        // Mobile/Email hashes based on indicator
        var mobileHash: ByteArray? = null
        var emailHash: ByteArray? = null
        var maskedAadharIndex = 17

        when (indicator) {
            1 -> { // Only email
                emailHash = if (fields.size > 17) fields[17] else null
                maskedAadharIndex = 18
            }
            2 -> { // Only mobile
                mobileHash = if (fields.size > 17) fields[17] else null
                maskedAadharIndex = 18
            }
            3 -> { // Both email and mobile
                emailHash = if (fields.size > 17) fields[17] else null
                mobileHash = if (fields.size > 18) fields[18] else null
                maskedAadharIndex = 19
            }
        }

        // Last 4 digits of Aadhaar
        val maskedAadhaar =
                if (fields.size > maskedAadharIndex) {
                    getString(maskedAadharIndex)
                } else ""

        // Build full address
        val addressParts =
                listOf(
                                house,
                                street,
                                landmark,
                                location,
                                vtc,
                                subDistrict,
                                district,
                                state,
                                pincode
                        )
                        .filter { it.isNotEmpty() }
        val fullAddress = addressParts.joinToString(", ")

        return AadharQrResult(
                referenceId = referenceId,
                name = name,
                dob = dob,
                gender = gender,
                careOf = careOf,
                district = district,
                landmark = landmark,
                house = house,
                location = location,
                pincode = pincode,
                postOffice = postOffice,
                state = state,
                street = street,
                subDistrict = subDistrict,
                vtc = vtc,
                photoBase64 = photoBase64,
                maskedAadhaar = maskedAadhaar,
                mobileHash = mobileHash,
                emailHash = emailHash,
                signatureValid = signatureValid,
                signatureVerificationAttempted = verificationAttempted,
                rawQrData = rawQrData,
                fullAddress = fullAddress
        )
    }

    /** Check if UIDAI certificate is available. */
    fun isCertificateAvailable(): Boolean = publicKeys.isNotEmpty()

    private fun parseXml(xmlData: String): AadharQrResult {
        android.util.Log.i("AadharQrScanner", "parseXml: $xmlData")
        try {
            // Regex to extract attributes: key="value"
            val uid = getAttribute(xmlData, "uid")
            val name = getAttribute(xmlData, "name")
            val gender = getAttribute(xmlData, "gender")
            val yob = getAttribute(xmlData, "yob")
            val dob = getAttribute(xmlData, "dob") // Some have dob, some yob
            val co = getAttribute(xmlData, "co")
            val house = getAttribute(xmlData, "house")
            val street = getAttribute(xmlData, "street")
            val lm = getAttribute(xmlData, "lm")
            val loc = getAttribute(xmlData, "loc")
            val vtc = getAttribute(xmlData, "vtc")
            val po = getAttribute(xmlData, "po")
            val dist = getAttribute(xmlData, "dist")
            val subdist = getAttribute(xmlData, "subdist")
            val state = getAttribute(xmlData, "state")
            val pc = getAttribute(xmlData, "pc")
            val email = getAttribute(xmlData, "e").ifEmpty { getAttribute(xmlData, "email") }
            val mobile = getAttribute(xmlData, "m").ifEmpty { getAttribute(xmlData, "mobile") }

            // Build full address
            val addressParts =
                    listOf(house, street, lm, loc, vtc, po, subdist, dist, state, pc).filter {
                        it.isNotEmpty()
                    }
            val fullAddress = addressParts.joinToString(", ")

            return AadharQrResult(
                    referenceId = uid,
                    name = name,
                    dob = dob.ifEmpty { yob }, // Use YOB if DOB is missing
                    gender = gender,
                    careOf = co,
                    district = dist,
                    landmark = lm,
                    house = house,
                    location = loc,
                    pincode = pc,
                    postOffice = po,
                    state = state,
                    street = street,
                    subDistrict = subdist,
                    vtc = vtc,
                    photoBase64 = null, // XML QR does not contain photo
                    maskedAadhaar = if (uid.length >= 12) "XXXXXXXX" + uid.takeLast(4) else "",
                    mobileHash = null,
                    emailHash = null,
                    email = email,
                    mobile = mobile,
                    signatureValid = false, // XML signature verification not supported here
                    signatureVerificationAttempted = true,
                    rawQrData = xmlData,
                    fullAddress = fullAddress
            )
        } catch (e: Exception) {
            android.util.Log.e("AadharQrScanner", "XML parsing failed: ${e.message}")
            return AadharQrResult(rawQrData = xmlData)
        }
    }

    private fun getAttribute(xml: String, key: String): String {
        // pattern: key="value"
        val pattern = "$key=\"([^\"]*)\"".toRegex()
        val match = pattern.find(xml)
        return match?.groupValues?.get(1) ?: ""
    }
}
