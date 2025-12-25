package com.vijay.cardkeeper.util

import android.graphics.Bitmap

data class IdentityDetails(
        val docNumber: String = "",
        val name: String = "",
        val dob: String = "",
        val expiryDate: String = "",
        val rawText: String = "",
        val capturedImage: Bitmap? = null,
        val state: String = "",
        val address: String = "",
        val sex: String = "",
        val eyeColor: String = "",
        val height: String = "",
        val licenseClass: String = "",
        val restrictions: String = "",
        val endorsements: String = "",
        val issuingAuthority: String = ""
)
