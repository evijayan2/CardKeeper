package com.vijay.cardkeeper.data.entity

import kotlinx.serialization.Serializable

@Serializable
data class RewardCard(
    val id: Int = 0,
    val name: String,
    val type: AccountType, // REWARDS_CARD or LIBRARY_CARD
    val barcode: String? = null,
    val barcodeFormat: Int? = null,
    val linkedPhoneNumber: String? = null,
    val frontImagePath: String? = null,
    val backImagePath: String? = null,
    val logoImagePath: String? = null,
    val notes: String? = null
)
