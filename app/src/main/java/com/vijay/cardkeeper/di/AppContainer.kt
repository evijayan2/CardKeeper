package com.vijay.cardkeeper.di

import android.content.Context
import com.vijay.cardkeeper.data.AppDatabase
import com.vijay.cardkeeper.data.repository.AadharCardRepository
import com.vijay.cardkeeper.data.repository.FinancialRepository
import com.vijay.cardkeeper.data.repository.GreenCardRepository
import com.vijay.cardkeeper.data.repository.GiftCardRepository
import com.vijay.cardkeeper.data.repository.IdentityRepository
import com.vijay.cardkeeper.data.repository.SearchRepository
import com.vijay.cardkeeper.data.repository.PanCardRepository
import androidx.datastore.preferences.preferencesDataStoreFile

/** AppContainer for manual dependency injection. */
interface AppContainer {
    val financialRepository: FinancialRepository
    val identityRepository: IdentityRepository
    val passportRepository: com.vijay.cardkeeper.data.repository.PassportRepository
    val greenCardRepository: GreenCardRepository
    val giftCardRepository: GiftCardRepository
    val aadharCardRepository: AadharCardRepository
    val panCardRepository: PanCardRepository
    val searchRepository: SearchRepository
    val userPreferencesRepository: com.vijay.cardkeeper.data.repository.UserPreferencesRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    // We assume that by the time this is accessed, the secure key is unlocked and cached.
    // If not, it means we bypassed AuthActivity, which is a security violation, so crashing is
    // appropriate.
    private val database: AppDatabase by lazy {
        val key =
                com.vijay.cardkeeper.util.KeyManager.cachedPassphrase
                        ?: throw IllegalStateException("Database accessed before authentication!")
        val factory = net.zetetic.database.sqlcipher.SupportOpenHelperFactory(key)
        androidx.room.Room.databaseBuilder(
            context.applicationContext,
            com.vijay.cardkeeper.data.AppDatabase::class.java,
            "cardkeeper_database"
        )
        .openHelperFactory(factory)
        .addMigrations(
            com.vijay.cardkeeper.data.MIGRATION_11_12,
            com.vijay.cardkeeper.data.MIGRATION_12_13,
            com.vijay.cardkeeper.data.MIGRATION_13_14,
            com.vijay.cardkeeper.data.MIGRATION_14_15,
            com.vijay.cardkeeper.data.MIGRATION_15_16,
            com.vijay.cardkeeper.data.MIGRATION_16_17
        )
        .fallbackToDestructiveMigration(false)
        .build()
    }

    override val financialRepository: FinancialRepository by lazy {
        FinancialRepository(database.financialAccountDao())
    }

    override val identityRepository: IdentityRepository by lazy {
        IdentityRepository(database.identityDocumentDao())
    }

    override val passportRepository:
            com.vijay.cardkeeper.data.repository.PassportRepository by lazy {
        com.vijay.cardkeeper.data.repository.PassportRepository(database.passportDao())
    }
    override val greenCardRepository: GreenCardRepository by lazy {
        GreenCardRepository(database.greenCardDao())
    }

    override val giftCardRepository: GiftCardRepository by lazy {
        com.vijay.cardkeeper.data.repository.GiftCardRepositoryImpl(database.giftCardDao())
    }

    override val aadharCardRepository: AadharCardRepository by lazy {
        AadharCardRepository(database.aadharCardDao())
    }

    override val panCardRepository: PanCardRepository by lazy {
        PanCardRepository(database.panCardDao())
    }

    override val searchRepository: SearchRepository by lazy {
        SearchRepository(
                financialRepository,
                identityRepository,
                passportRepository,
                greenCardRepository,
                aadharCardRepository
        )
    }

    override val userPreferencesRepository: com.vijay.cardkeeper.data.repository.UserPreferencesRepository by lazy {
        val dataStore = androidx.datastore.preferences.core.PreferenceDataStoreFactory.create {
            context.preferencesDataStoreFile("user_preferences")
        }
        com.vijay.cardkeeper.data.repository.UserPreferencesRepository(dataStore)
    }
}
