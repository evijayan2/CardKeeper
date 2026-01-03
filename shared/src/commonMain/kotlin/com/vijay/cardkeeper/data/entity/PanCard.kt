package com.vijay.cardkeeper.data.entity



/**
 * PAN (Permanent Account Number) Card entity.
 * PAN is a 10-character alphanumeric identifier issued by the Income Tax Department of India.
 * Format: ABCDE1234F (5 letters + 4 digits + 1 letter)
 */

data class PanCard(
    val id: Int = 0,

    // PAN number (10 characters: ABCDE1234F)
    val panNumber: String,

    // Holder details
    val holderName: String,
    val fatherName: String? = null,
    val dob: String? = null, // Date of Birth

    // Scanned images (paths to encrypted files)
    val frontImagePath: String? = null,
    val backImagePath: String? = null,

    // Additional metadata
    val issueDate: String? = null,
    val acknowledgementNumber: String? = null // E-PAN acknowledgement number
)
