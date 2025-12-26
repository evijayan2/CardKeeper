package com.vijay.cardkeeper.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "passports")
data class Passport(
        @PrimaryKey(autoGenerate = true) val id: Int = 0,
        val passportNumber: String,
        val countryCode: String, // USA, IND, etc.
        val surname: String? = null,
        val givenNames: String? = null,
        val nationality: String? = null,
        val dob: String? = null,
        val placeOfBirth: String? = null,
        val sex: String? = null,
        val dateOfIssue: String? = null,
        val dateOfExpiry: String? = null,
        val authority: String? = null,
        val endorsements: String? = null, // USA specific usually

        // Images
        val frontImagePath: String? = null,
        val backImagePath: String? = null,

        // Indian Specific
        val fatherName: String? = null,
        val motherName: String? = null,
        val spouseName: String? = null,
        val address: String? = null,
        val placeOfIssue: String? = null,
        val fileNumber: String? = null
)
