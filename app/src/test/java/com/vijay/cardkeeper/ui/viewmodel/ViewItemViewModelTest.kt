package com.vijay.cardkeeper.ui.viewmodel

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vijay.cardkeeper.data.entity.AccountType
import com.vijay.cardkeeper.data.entity.FinancialAccount
import com.vijay.cardkeeper.data.repository.FinancialRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class ViewItemViewModelTest {

    private lateinit var viewModel: ViewItemViewModel
    private val repository: FinancialRepository = mockk(relaxed = true)

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = ViewItemViewModel(repository)
    }

    @Test
    fun `loadAccount should update selectedAccount`() = runBlocking {
        val account = FinancialAccount(id = 1, institutionName = "Test Bank", type = AccountType.BANK_ACCOUNT, accountName = "Test Account", holderName = "John Doe", number = "12345")
        coEvery { repository.getAccountById(1) } returns account

        viewModel.loadAccount(1)

        viewModel.selectedAccount.test {
            assertThat(awaitItem()).isEqualTo(account)
        }
    }

    @Test
    fun `setFullScreenImage should update fullScreenImage`() = runBlocking {
        val path = "/path/to/image.jpg"
        viewModel.setFullScreenImage(path)

        viewModel.fullScreenImage.test {
            assertThat(awaitItem()).isEqualTo(path)
        }
    }

    @Test
    fun `deleteAccount should call repository`() = runBlocking {
        val account = FinancialAccount(id = 1, institutionName = "Test Bank", type = AccountType.BANK_ACCOUNT, accountName = "Test Account", holderName = "John Doe", number = "12345")
        viewModel.deleteAccount(account)
        coVerify { repository.deleteAccount(account) }
    }
}
