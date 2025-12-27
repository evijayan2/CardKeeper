package com.vijay.cardkeeper.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.vijay.cardkeeper.data.dao.FinancialAccountDao
import com.vijay.cardkeeper.data.dao.IdentityDocumentDao
import com.vijay.cardkeeper.data.entity.FinancialAccount
import com.vijay.cardkeeper.data.entity.GreenCard
import com.vijay.cardkeeper.data.entity.IdentityDocument

@Database(
        entities =
                [
                        FinancialAccount::class,
                        IdentityDocument::class,
                        com.vijay.cardkeeper.data.entity.Passport::class,
                        GreenCard::class],
        version = 12,
        exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun financialAccountDao(): FinancialAccountDao
    abstract fun identityDocumentDao(): IdentityDocumentDao
    abstract fun passportDao(): com.vijay.cardkeeper.data.dao.PassportDao
    abstract fun greenCardDao(): com.vijay.cardkeeper.data.dao.GreenCardDao

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

        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE
                    ?: synchronized(this) {
                        val instance =
                                Room.databaseBuilder(
                                                context.applicationContext,
                                                AppDatabase::class.java,
                                                "cardkeeper_database"
                                        )
                                        .addMigrations(MIGRATION_11_12)
                                        .fallbackToDestructiveMigration(false)
                                        .build()
                        INSTANCE = instance
                        instance
                    }
        }
    }
}
