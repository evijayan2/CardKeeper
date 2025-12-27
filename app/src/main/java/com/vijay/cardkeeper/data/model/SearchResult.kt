package com.vijay.cardkeeper.data.model

data class SearchResult(
        val id: Int,
        val title: String,
        val subtitle: String,
        val type: String, // "Finance", "Identity", "Passport", "Rewards"
        val originalType: String?, // e.g., "CREDIT_CARD", "DRIVER_LICENSE"
        val iconRes: Int? = null,
        val logoUrl: String? = null
)
