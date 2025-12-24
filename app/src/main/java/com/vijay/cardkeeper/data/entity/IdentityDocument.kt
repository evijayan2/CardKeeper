package com.vijay.cardkeeper.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DocumentType {
    PASSPORT, DRIVER_LICENSE, GREEN_CARD, ADHAAR, PAN, SSN, VOTER_ID, OTHER
}

@Entity(tableName = "identity_documents")
data class IdentityDocument(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: DocumentType,
    val country: String,         // USA, IND, etc.
    val docNumber: String,       // Passport No, DL No
    val holderName: String,
    
    // Dates
    val issueDate: Long? = null,
    val expiryDate: Long? = null,
    
    // Metadata
    val issuingAuthority: String? = null, // "Dept of State", "RTO Bangalore"
    val metadata: String? = null,         // JSON or arbitrary text for extra fields (Visa Class, etc.)
    
    // Images (Paths to encrypted files)
    val frontImagePath: String? = null,
    val backImagePath: String? = null
)
