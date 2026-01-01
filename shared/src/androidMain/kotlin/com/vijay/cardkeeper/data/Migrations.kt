package com.vijay.cardkeeper.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `green_cards` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `surname` TEXT NOT NULL, 
                `givenName` TEXT NOT NULL, 
                `uscisNumber` TEXT NOT NULL, 
                `category` TEXT NOT NULL, 
                `countryOfBirth` TEXT NOT NULL, 
                `dob` TEXT NOT NULL, 
                `sex` TEXT NOT NULL, 
                `expiryDate` TEXT NOT NULL, 
                `residentSince` TEXT NOT NULL, 
                `frontImagePath` TEXT, 
                `backImagePath` TEXT
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `aadhar_cards` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `referenceId` TEXT NOT NULL,
                `holderName` TEXT NOT NULL,
                `dob` TEXT NOT NULL,
                `gender` TEXT NOT NULL,
                `address` TEXT NOT NULL,
                `pincode` TEXT,
                `maskedAadhaarNumber` TEXT NOT NULL,
                `uid` TEXT,
                `vid` TEXT,
                `photoBase64` TEXT,
                `timestamp` TEXT,
                `digitalSignature` TEXT,
                `certificateId` TEXT,
                `enrollmentNumber` TEXT,
                `frontImagePath` TEXT,
                `backImagePath` TEXT,
                `qrData` TEXT
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE `aadhar_cards` ADD COLUMN `email` TEXT")
        database.execSQL("ALTER TABLE `aadhar_cards` ADD COLUMN `mobile` TEXT")
    }
}

val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `gift_cards` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `providerName` TEXT NOT NULL,
                `cardNumber` TEXT NOT NULL,
                `pin` TEXT,
                `frontImagePath` TEXT,
                `backImagePath` TEXT,
                `barcode` TEXT,
                `barcodeFormat` INTEGER,
                `logoImagePath` TEXT,
                `notes` TEXT
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE `gift_cards` ADD COLUMN `qrCode` TEXT")
    }
}
