package com.vijay.cardkeeper.data.entity

import kotlinx.serialization.Serializable

@Serializable
enum class InsuranceCardType {
    MEDICAL,
    DENTAL,
    EYE
}

@Serializable
data class InsuranceCard(
    val id: Int = 0,
    val providerName: String, // e.g., Blue Cross, VSP
    val planName: String? = null, // e.g., PPO Plus
    val type: InsuranceCardType,
    val policyNumber: String,
    val groupNumber: String? = null,
    val memberId: String? = null,
    val policyHolderName: String,
    
    // Dates
    val expiryDate: String? = null, // MM/YY or YYYY-MM-DD
    
    // Contact
    val website: String? = null,
    val customerServiceNumber: String? = null,
    
    // Images
    val frontImagePath: String? = null,
    val backImagePath: String? = null,
    
    // Aesthetic
    val colorTheme: Long? = null,
    val notes: String? = null
)
