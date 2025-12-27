package com.vijay.cardkeeper.data.model

data class GreenCardMrz(
        val documentCode: String,
        val issuingCountry: String,
        val documentNumber: String,
        val documentNumberCheckDigit: Char,
        val dateOfBirth: String, // YYMMDD
        val dobCheckDigit: Char,
        val sex: String, // M / F / <
        val expiryDate: String, // YYMMDD
        val expiryCheckDigit: Char,
        val nationality: String,
        val lastName: String,
        val firstName: String
)
