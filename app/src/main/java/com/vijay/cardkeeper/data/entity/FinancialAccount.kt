package com.vijay.cardkeeper.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AccountType {
    CREDIT_CARD,
    DEBIT_CARD,
    BANK_ACCOUNT,
    INVESTMENT,
    INSURANCE,
    LOAN
}

@Entity(tableName = "financial_accounts")
data class FinancialAccount(
        @PrimaryKey(autoGenerate = true) val id: Int = 0,
        val type: AccountType,
        val institutionName: String, // e.g., Chase, SBI, LIC
        val accountName: String, // e.g., "Sapphire Reserve", "Savings 1234"
        val holderName: String,

        // Core Numbers (Will be encrypted in future)
        val number: String, // Card Number or Account Number
        val cvv: String? = null, // For cards
        val pinHint: String? = null, // Encrypted pin hint

        // Banking specifics (Nullable as they don't apply to all)
        val routingNumber: String? = null, // USA
        val ifscCode: String? = null, // India
        val swiftCode: String? = null, // International
        val branchCode: String? = null, // Specific branch

        // Dates (Stored as hull Epoch Millis)
        val expiryDate: String? = null, // MM/YY
        val statementDate: Int? = null, // Day of month (1-31)

        // Aesthetic
        val colorTheme: Long? = null, // Color hex code
        val cardNetwork: String? = null, // Visa, Mastercard, RuPay (Auto-detected ideally)
        val notes: String? = null,
        val cardPin: String? = null,
        val lostCardContactNumber: String? = null,
        val frontImagePath: String? = null,
        val backImagePath: String? = null
)
