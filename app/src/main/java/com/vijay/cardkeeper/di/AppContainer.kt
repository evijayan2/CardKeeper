package com.vijay.cardkeeper.di

import android.content.Context
import com.vijay.cardkeeper.data.AppDatabase
import com.vijay.cardkeeper.data.repository.FinancialRepository
import com.vijay.cardkeeper.data.repository.IdentityRepository

/**
 * AppContainer for manual dependency injection.
 */
interface AppContainer {
    val financialRepository: FinancialRepository
    val identityRepository: IdentityRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    
    private val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    override val financialRepository: FinancialRepository by lazy {
        FinancialRepository(database.financialAccountDao())
    }

    override val identityRepository: IdentityRepository by lazy {
        IdentityRepository(database.identityDocumentDao())
    }
}
