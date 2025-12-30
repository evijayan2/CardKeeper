package com.vijay.cardkeeper.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gift_cards")
data class GiftCard(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val providerName: String, // e.g., Amazon, Costco
    val cardNumber: String, // Gift Card Code
    val pin: String? = null,
    val frontImagePath: String? = null,
    val backImagePath: String? = null,
    val barcode: String? = null,
    val barcodeFormat: Int? = null, // MLKit Format Constant
    val qrCode: String? = null, // Dedicated QR code data
    val logoImagePath: String? = null,
    val notes: String? = null // User notes
)
