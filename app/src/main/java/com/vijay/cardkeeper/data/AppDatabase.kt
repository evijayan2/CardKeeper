package com.vijay.cardkeeper.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.vijay.cardkeeper.data.dao.FinancialAccountDao
import com.vijay.cardkeeper.data.dao.IdentityDocumentDao
import com.vijay.cardkeeper.data.entity.FinancialAccount
import com.vijay.cardkeeper.data.entity.IdentityDocument

@Database(
    entities = [FinancialAccount::class, IdentityDocument::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun financialAccountDao(): FinancialAccountDao
    abstract fun identityDocumentDao(): IdentityDocumentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cardkeeper_database"
                )
                // In a real app, we would use proper migrations. 
                // For dev prototype, fallbackToDestructiveMigration is okay, 
                // but since this is for user data, strictly standard logic is better.
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
