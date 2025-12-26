package com.vijay.cardkeeper.ui.viewmodel

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vijay.cardkeeper.data.entity.AccountType
import com.vijay.cardkeeper.data.entity.DocumentType
import com.vijay.cardkeeper.data.entity.FinancialAccount
import com.vijay.cardkeeper.data.entity.IdentityDocument
import com.vijay.cardkeeper.data.repository.FinancialRepository
import com.vijay.cardkeeper.data.repository.IdentityRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel
    private val financialRepository: FinancialRepository = mockk()
    private val identityRepository: IdentityRepository = mockk()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { financialRepository.allAccounts } returns flowOf(emptyList())
        every { identityRepository.allDocuments } returns flowOf(emptyList())
        viewModel = HomeViewModel(financialRepository, identityRepository)
    }

    @Test
    fun `financialAccounts should emit the correct data`() = runBlocking {
        val accounts = listOf(FinancialAccount(id = 1, institutionName = "Test Bank", type = AccountType.BANK_ACCOUNT, accountName = "Test Account", holderName = "John Doe", number = "12345"))
        every { financialRepository.allAccounts } returns flowOf(accounts)

        viewModel = HomeViewModel(financialRepository, identityRepository)

        viewModel.financialAccounts.test {
            assertThat(awaitItem()).isEqualTo(accounts)
        }
    }

    @Test
    fun `identityDocuments should emit the correct data`() = runBlocking {
        val documents = listOf(IdentityDocument(id = 1, type = DocumentType.PASSPORT, country = "US", docNumber = "123", holderName = "John Doe"))
        every { identityRepository.allDocuments } returns flowOf(documents)

        viewModel = HomeViewModel(financialRepository, identityRepository)

        viewModel.identityDocuments.test {
            assertThat(awaitItem()).isEqualTo(documents)
        }
    }
}
