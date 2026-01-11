package com.vijay.cardkeeper.domain.model

data class CardDetails(
    val number: String = "",
    val expiryDate: String = "", // MM/YY
    val securityCode: String = "",
    val ownerName: String = "",
    val bankName: String = "",
    val cardType: String = "", // Credit/Debit
    val scheme: String = "", // Visa, Mastercard, Amex
    val phoneNumber: String = ""
)

data class IdentityDetails(
    val docNumber: String = "",
    val name: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val dob: String = "",
    val expiryDate: String = "",
    val rawText: String = "",
    // val capturedImage: Bitmap? = null, // Removed Bitmap for KMP
    val state: String = "",
    val address: String = "",
    val sex: String = "",
    val eyeColor: String = "",
    val height: String = "",
    val licenseClass: String = "",
    val restrictions: String = "",
    val endorsements: String = "",
    val issueDate: String = "",
    val issuingAuthority: String = "",
    val country: String = "",
    val countryOfBirth: String = "",
    val residentSince: String = "",
    val category: String = "",
    val uscisNumber: String = "",
    val isMrzData: Boolean = false
)
