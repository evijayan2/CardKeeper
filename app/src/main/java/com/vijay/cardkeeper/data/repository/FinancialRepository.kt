package com.vijay.cardkeeper.data.repository

import com.vijay.cardkeeper.data.dao.FinancialAccountDao
import com.vijay.cardkeeper.data.entity.FinancialAccount
import kotlinx.coroutines.flow.Flow

class FinancialRepository(private val accountDao: FinancialAccountDao) {
    
    val allAccounts: Flow<List<FinancialAccount>> = accountDao.getAllAccounts()

    suspend fun getAccountById(id: Int): FinancialAccount? {
        return accountDao.getAccountById(id)
    }

    suspend fun insertAccount(account: FinancialAccount) {
        accountDao.insertAccount(account)
    }

    suspend fun updateAccount(account: FinancialAccount) {
        accountDao.updateAccount(account)
    }

    suspend fun deleteAccount(account: FinancialAccount) {
        accountDao.deleteAccount(account)
    }
}
