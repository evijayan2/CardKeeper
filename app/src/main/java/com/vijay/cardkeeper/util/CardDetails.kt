package com.vijay.cardkeeper.util

data class CardDetails(
        val number: String = "",
        val expiryDate: String = "", // MM/YY
        val securityCode: String = "",
        val ownerName: String = "",
        val bankName: String = "",
        val cardType: String = "", // Credit/Debit
        val scheme: String = "" // Visa, Mastercard, Amex
)
