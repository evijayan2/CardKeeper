package com.vijay.cardkeeper.data.repository

import com.vijay.cardkeeper.data.dao.FinancialAccountDao
import com.vijay.cardkeeper.data.entity.AccountType
import com.vijay.cardkeeper.data.entity.FinancialAccount
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FinancialRepositoryTest {

    private lateinit var repository: FinancialRepository
    private val dao: FinancialAccountDao = mockk(relaxed = true)

    @BeforeEach
    fun setup() {
        repository = FinancialRepository(dao)
    }

    @Test
    fun `getAccountById should call dao`() = runBlocking {
        repository.getAccountById(1)
        coVerify { dao.getAccountById(1) }
    }

    @Test
    fun `insertAccount should call dao`() = runBlocking {
        val account = FinancialAccount(id = 1, institutionName = "Test Bank", holderName = "John Doe", number = "12345", type = AccountType.BANK_ACCOUNT, accountName = "Test Account")
        repository.insertAccount(account)
        coVerify { dao.insertAccount(account) }
    }

    @Test
    fun `updateAccount should call dao`() = runBlocking {
        val account = FinancialAccount(id = 1, institutionName = "Test Bank", holderName = "John Doe", number = "12345", type = AccountType.BANK_ACCOUNT, accountName = "Test Account")
        repository.updateAccount(account)
        coVerify { dao.updateAccount(account) }
    }

    @Test
    fun `deleteAccount should call dao`() = runBlocking {
        val account = FinancialAccount(id = 1, institutionName = "Test Bank", holderName = "John Doe", number = "12345", type = AccountType.BANK_ACCOUNT, accountName = "Test Account")
        repository.deleteAccount(account)
        coVerify { dao.deleteAccount(account) }
    }

    @Test
    fun `allAccounts should get from dao`() {
        repository.allAccounts
        coVerify { dao.getAllAccounts() }
    }
}
