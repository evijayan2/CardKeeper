package com.vijay.cardkeeper.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.ConstructedBy
import androidx.room.RoomDatabaseConstructor
import com.vijay.cardkeeper.data.dao.AadharCardDao
import com.vijay.cardkeeper.data.dao.FinancialAccountDao
import com.vijay.cardkeeper.data.dao.IdentityDocumentDao
import com.vijay.cardkeeper.data.entity.AadharCard
import com.vijay.cardkeeper.data.entity.FinancialAccount
import com.vijay.cardkeeper.data.entity.GiftCard
import com.vijay.cardkeeper.data.entity.GreenCard
import com.vijay.cardkeeper.data.entity.IdentityDocument

@Database(
    entities = [
        FinancialAccount::class,
        IdentityDocument::class,
        com.vijay.cardkeeper.data.entity.Passport::class,
        GreenCard::class,
        AadharCard::class,
        GiftCard::class
    ],
    version = 16,
    exportSchema = true
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun financialAccountDao(): FinancialAccountDao
    abstract fun identityDocumentDao(): IdentityDocumentDao
    abstract fun passportDao(): com.vijay.cardkeeper.data.dao.PassportDao
    abstract fun greenCardDao(): com.vijay.cardkeeper.data.dao.GreenCardDao
    abstract fun aadharCardDao(): AadharCardDao
    abstract fun giftCardDao(): com.vijay.cardkeeper.data.dao.GiftCardDao
}

expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase>
