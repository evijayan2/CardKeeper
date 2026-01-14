package com.vijay.cardkeeper.data.entity




import kotlinx.serialization.Serializable

@Serializable
data class GreenCard(
        val id: Int = 0,
        val surname: String,
        val givenName: String,
        val uscisNumber: String,
        val category: String,
        val countryOfBirth: String,
        val dob: String,
        val sex: String,
        val expiryDate: String,
        val residentSince: String,
        val frontImagePath: String? = null,
        val backImagePath: String? = null
)
