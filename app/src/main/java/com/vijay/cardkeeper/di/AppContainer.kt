package com.vijay.cardkeeper.di

import android.content.Context
import com.vijay.cardkeeper.data.AppDatabase
import com.vijay.cardkeeper.data.repository.AadharCardRepository
import com.vijay.cardkeeper.data.repository.FinancialRepository
import com.vijay.cardkeeper.data.repository.GreenCardRepository
import com.vijay.cardkeeper.data.repository.IdentityRepository
import com.vijay.cardkeeper.data.repository.SearchRepository

/** AppContainer for manual dependency injection. */
interface AppContainer {
    val financialRepository: FinancialRepository
    val identityRepository: IdentityRepository
    val passportRepository: com.vijay.cardkeeper.data.repository.PassportRepository
    val greenCardRepository: GreenCardRepository
    val aadharCardRepository: AadharCardRepository
    val searchRepository: SearchRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    private val database: AppDatabase by lazy { AppDatabase.getDatabase(context) }

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

    override val aadharCardRepository: AadharCardRepository by lazy {
        AadharCardRepository(database.aadharCardDao())
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
}
