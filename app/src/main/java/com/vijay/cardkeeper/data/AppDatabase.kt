package com.vijay.cardkeeper.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.vijay.cardkeeper.data.dao.AadharCardDao
import com.vijay.cardkeeper.data.dao.FinancialAccountDao
import com.vijay.cardkeeper.data.dao.IdentityDocumentDao
import com.vijay.cardkeeper.data.entity.AadharCard
import com.vijay.cardkeeper.data.entity.FinancialAccount
import com.vijay.cardkeeper.data.entity.GreenCard
import com.vijay.cardkeeper.data.entity.IdentityDocument

@Database(
        entities =
                [
                        FinancialAccount::class,
                        IdentityDocument::class,
                        com.vijay.cardkeeper.data.entity.Passport::class,
                        GreenCard::class,
                        AadharCard::class],
        version = 14,
        exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun financialAccountDao(): FinancialAccountDao
    abstract fun identityDocumentDao(): IdentityDocumentDao
    abstract fun passportDao(): com.vijay.cardkeeper.data.dao.PassportDao
    abstract fun greenCardDao(): com.vijay.cardkeeper.data.dao.GreenCardDao
    abstract fun aadharCardDao(): AadharCardDao

    companion object {
        val MIGRATION_11_12 =
                object : androidx.room.migration.Migration(11, 12) {
                    override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
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

        val MIGRATION_12_13 =
                object : androidx.room.migration.Migration(12, 13) {
                    override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
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

        val MIGRATION_13_14 =
                object : androidx.room.migration.Migration(13, 14) {
                    override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                        database.execSQL("ALTER TABLE `aadhar_cards` ADD COLUMN `email` TEXT")
                        database.execSQL("ALTER TABLE `aadhar_cards` ADD COLUMN `mobile` TEXT")
                    }
                }

        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, passphrase: ByteArray): AppDatabase {
            return INSTANCE
                    ?: synchronized(this) {
                        val factory =
                                net.zetetic.database.sqlcipher.SupportOpenHelperFactory(passphrase)
                        val instance =
                                Room.databaseBuilder(
                                                context.applicationContext,
                                                AppDatabase::class.java,
                                                "cardkeeper_database"
                                        )
                                        .openHelperFactory(factory)
                                        .addMigrations(
                                                MIGRATION_11_12,
                                                MIGRATION_12_13,
                                                MIGRATION_13_14
                                        )
                                        .fallbackToDestructiveMigration(false)
                                        .build()
                        INSTANCE = instance
                        instance
                    }
        }
    }
}
