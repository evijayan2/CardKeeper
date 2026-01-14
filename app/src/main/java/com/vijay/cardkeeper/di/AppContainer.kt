package com.vijay.cardkeeper.di

import android.content.Context

import com.vijay.cardkeeper.data.repository.AadharCardRepository
import com.vijay.cardkeeper.data.repository.FinancialRepository
import com.vijay.cardkeeper.data.repository.GreenCardRepository
import com.vijay.cardkeeper.data.repository.GiftCardRepository
import com.vijay.cardkeeper.data.repository.IdentityRepository
import com.vijay.cardkeeper.data.repository.SearchRepository
import com.vijay.cardkeeper.data.repository.PanCardRepository
import androidx.datastore.preferences.preferencesDataStoreFile
import com.vijay.cardkeeper.SqlDelightDatabase
import app.cash.sqldelight.driver.android.AndroidSqliteDriver


import com.vijay.cardkeeper.data.repository.RewardCardRepository
import com.vijay.cardkeeper.data.repository.RewardCardRepositoryImpl
import com.vijay.cardkeeper.data.repository.InsuranceCardRepository

/** AppContainer for manual dependency injection. */
interface AppContainer {
    val financialRepository: FinancialRepository
    val identityRepository: IdentityRepository
    val passportRepository: com.vijay.cardkeeper.data.repository.PassportRepository
    val greenCardRepository: GreenCardRepository
    val giftCardRepository: GiftCardRepository
    val aadharCardRepository: AadharCardRepository
    val rewardCardRepository: RewardCardRepository
    val panCardRepository: PanCardRepository
    val insuranceCardRepository: InsuranceCardRepository
    val searchRepository: SearchRepository
    val userPreferencesRepository: com.vijay.cardkeeper.data.repository.UserPreferencesRepository
    val imageMigrationManager: com.vijay.cardkeeper.util.ImageMigrationManager
    val backupManager: com.vijay.cardkeeper.data.backup.AndroidBackupManager
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    // We assume that by the time this is accessed, the secure key is unlocked and cached.
    // If not, it means we bypassed AuthActivity, which is a security violation, so crashing is
    // appropriate.


    private val sqlDelightDatabase: SqlDelightDatabase by lazy {
        val key =
                com.vijay.cardkeeper.util.KeyManager.cachedPassphrase
                        ?: throw IllegalStateException("Database accessed before authentication!")
        val factory = net.zetetic.database.sqlcipher.SupportOpenHelperFactory(key)
        val driver = AndroidSqliteDriver(
            schema = SqlDelightDatabase.Schema,
            context = context,
            name = "cardkeeper_database",
            factory = factory
        )
        SqlDelightDatabase(driver)
    }

    override val financialRepository: FinancialRepository by lazy {
        FinancialRepository(sqlDelightDatabase)
    }

    override val identityRepository: IdentityRepository by lazy {
        IdentityRepository(sqlDelightDatabase)
    }

    override val passportRepository:
            com.vijay.cardkeeper.data.repository.PassportRepository by lazy {
        com.vijay.cardkeeper.data.repository.PassportRepository(sqlDelightDatabase)
    }
    override val greenCardRepository: GreenCardRepository by lazy {
        GreenCardRepository(sqlDelightDatabase)
    }

    override val giftCardRepository: GiftCardRepository by lazy {
        com.vijay.cardkeeper.data.repository.GiftCardRepositoryImpl(sqlDelightDatabase)
    }

    override val aadharCardRepository: AadharCardRepository by lazy {
        AadharCardRepository(sqlDelightDatabase)
    }

    override val rewardCardRepository: RewardCardRepository by lazy {
        RewardCardRepositoryImpl(sqlDelightDatabase)
    }

    override val panCardRepository: PanCardRepository by lazy {
        PanCardRepository(sqlDelightDatabase)
    }

    override val insuranceCardRepository: InsuranceCardRepository by lazy {
        InsuranceCardRepository(sqlDelightDatabase)
    }

    override val searchRepository: SearchRepository by lazy {
        SearchRepository(
                financialRepository,
                identityRepository,
                passportRepository,
                greenCardRepository,
                aadharCardRepository,
                rewardCardRepository,
                giftCardRepository,
                panCardRepository
        )
    }

    override val userPreferencesRepository: com.vijay.cardkeeper.data.repository.UserPreferencesRepository by lazy {
        val dataStore = androidx.datastore.preferences.core.PreferenceDataStoreFactory.create {
            context.preferencesDataStoreFile("user_preferences")
        }
        com.vijay.cardkeeper.data.repository.UserPreferencesRepository(dataStore)
    }

    override val imageMigrationManager: com.vijay.cardkeeper.util.ImageMigrationManager by lazy {
        com.vijay.cardkeeper.util.ImageMigrationManager(
            context = context,
            financialRepository = financialRepository,
            identityRepository = identityRepository,
            passportRepository = passportRepository,
            greenCardRepository = greenCardRepository,
            aadharCardRepository = aadharCardRepository,
            giftCardRepository = giftCardRepository,
            panCardRepository = panCardRepository,
            rewardCardRepository = rewardCardRepository
        )
    }

    override val backupManager: com.vijay.cardkeeper.data.backup.AndroidBackupManager by lazy {
        com.vijay.cardkeeper.data.backup.AndroidBackupManager(context, this)
    }
}
