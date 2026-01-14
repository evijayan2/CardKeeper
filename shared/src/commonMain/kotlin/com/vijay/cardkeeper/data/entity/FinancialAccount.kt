package com.vijay.cardkeeper.data.entity



import kotlinx.serialization.Serializable

@Serializable
enum class AccountType {
    CREDIT_CARD,
    DEBIT_CARD,
    BANK_ACCOUNT,
    INVESTMENT,
    INSURANCE,
    LOAN,
    REWARDS_CARD,
    LIBRARY_CARD
}

@Serializable
enum class BankAccountSubType {
    CHECKING,
    SAVINGS,
    NRE,
    NRO,
    CURRENT,
    MONEY_MARKET,
    OTHER
}


@Serializable
data class FinancialAccount(
        val id: Int = 0,
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
        val wireNumber: String? = null, // Wire transfer routing
        val accountSubType: BankAccountSubType? = null, // Checking/Savings/NRE/NRO etc.

        // Branch/Contact Info
        val branchAddress: String? = null,
        val branchContactNumber: String? = null,
        val bankWebUrl: String? = null,
        val bankBrandColor: Long? = null, // Bank's brand color for UI
        val holderAddress: String? = null, // Account holder's mailing address

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
        val backImagePath: String? = null,
        val barcode: String? = null,
        val barcodeFormat: Int? = null, // MLKit Format Constant
        val linkedPhoneNumber: String? = null,
        val logoImagePath: String? = null
)
