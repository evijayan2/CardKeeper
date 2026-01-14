package com.vijay.cardkeeper.data.entity



import kotlinx.serialization.Serializable

/**
 * Aadhar Card entity with fields from Aadhar QR code. QR code contains: RefID, Name, DOB/YOB,
 * Gender, Address, Masked Aadhaar, Photo, Timestamp, Signature
 */

@Serializable
data class AadharCard(
        val id: Int = 0,

        // Reference ID from QR (unique identifier for this Aadhar issuance)
        val referenceId: String,

        // Holder details
        val holderName: String,
        val dob: String, // Date of Birth or Year of Birth
        val gender: String,

        // Address from QR
        val address: String,
        val pincode: String? = null,

        // Aadhaar number details
        val maskedAadhaarNumber: String, // Last 4 digits visible, rest masked (xxxx xxxx 1234)
        val uid: String? = null, // Full 12-digit UID if manually entered
        val vid: String? = null, // 16-digit Virtual ID (optional)

        // Photo from QR (Base64 encoded JPEG)
        val photoBase64: String? = null,

        // QR metadata
        val timestamp: String? = null, // Timestamp from QR
        val digitalSignature: String? = null, // For authenticity verification
        val certificateId: String? = null, // Certificate Identifier

        // Enrollment details (optional)
        val enrollmentNumber: String? = null,

        // Contact Details
        val email: String? = null,
        val mobile: String? = null,

        // Scanned images (paths to files)
        val frontImagePath: String? = null,
        val backImagePath: String? = null,

        // Original QR data for reproduction
        val qrData: String? = null
)
