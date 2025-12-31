package com.vijay.cardkeeper.ui.viewmodel

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vijay.cardkeeper.data.entity.AccountType
import com.vijay.cardkeeper.data.entity.DocumentType
import com.vijay.cardkeeper.data.entity.FinancialAccount
import com.vijay.cardkeeper.data.entity.IdentityDocument
import com.vijay.cardkeeper.data.repository.FinancialRepository
import com.vijay.cardkeeper.data.repository.IdentityRepository
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
class AddItemViewModelTest {

    private lateinit var viewModel: AddItemViewModel
    private val financialRepository: FinancialRepository = mockk(relaxed = true)
    private val identityRepository: IdentityRepository = mockk(relaxed = true)

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = AddItemViewModel(
            financialRepository, identityRepository,
            passportRepository = TODO(),
            greenCardRepository = TODO(),
            aadharCardRepository = TODO(),
            giftCardRepository = TODO()
        )
    }

    @Test
    fun `saveFinancialAccount should call insert for new account`() = runBlocking {
        viewModel.saveFinancialAccount(
            id = 0,
            type = AccountType.BANK_ACCOUNT,
            institution = "Test Bank",
            name = "Test Account",
            holder = "John Doe",
            number = "12345",
            routing = "",
            ifsc = "",
            expiryDate = null,
            cvv = null,
            pin = null,
            notes = null,
            contact = null,
            cardNetwork = null,
            frontImagePath = null,
            backImagePath = null
        )

        coVerify { financialRepository.insertAccount(any()) }
    }

    @Test
    fun `saveFinancialAccount should call update for existing account`() = runBlocking {
         viewModel.saveFinancialAccount(
            id = 1,
            type = AccountType.BANK_ACCOUNT,
            institution = "Test Bank",
            name = "Test Account",
            holder = "John Doe",
            number = "12345",
            routing = "",
            ifsc = "",
            expiryDate = null,
            cvv = null,
            pin = null,
            notes = null,
            contact = null,
            cardNetwork = null,
            frontImagePath = null,
            backImagePath = null
        )

        coVerify { financialRepository.updateAccount(any()) }
    }

    @Test
    fun `getFinancialAccount should call repository`() = runBlocking {
        viewModel.getFinancialAccount(1)
        coVerify { financialRepository.getAccountById(1) }
    }

    @Test
    fun `saveIdentityDocument should call insert for new document`() = runBlocking {
        viewModel.saveIdentityDocument(
            id = 0,
            type = DocumentType.PASSPORT,
            country = "US",
            docNumber = "123",
            holder = "John Doe",
            expiryDate = null,
            frontImagePath = null,
            backImagePath = null,
            state = null,
            address = null,
            dob = null,
            sex = null,
            eyeColor = null,
            height = null,
            licenseClass = null,
            restrictions = null,
            endorsements = null,
            issuingAuthority = null
        )

        coVerify { identityRepository.insertDocument(any()) }
    }

    @Test
    fun `saveIdentityDocument should call update for existing document`() = runBlocking {
        viewModel.saveIdentityDocument(
            id = 1,
            type = DocumentType.PASSPORT,
            country = "US",
            docNumber = "123",
            holder = "John Doe",
            expiryDate = null,
            frontImagePath = null,
            backImagePath = null,
            state = null,
            address = null,
            dob = null,
            sex = null,
            eyeColor = null,
            height = null,
            licenseClass = null,
            restrictions = null,
            endorsements = null,
            issuingAuthority = null
        )

        coVerify { identityRepository.updateDocument(any()) }
    }

    @Test
    fun `getIdentityDocument should call repository`() = runBlocking {
        viewModel.getIdentityDocument(1)
        coVerify { identityRepository.getDocumentById(1) }
    }

    @Test
    fun `deleteIdentityDocument should call repository`() = runBlocking {
        val doc = IdentityDocument(id = 1, type = DocumentType.PASSPORT, docNumber = "123", holderName = "John Doe", country = "US")
        viewModel.deleteIdentityDocument(doc)
        coVerify { identityRepository.deleteDocument(doc) }
    }

    @Test
    fun `getItem should return financial account`() = runBlocking {
        val account = FinancialAccount(id = 1, institutionName = "Test Bank", type = AccountType.BANK_ACCOUNT, accountName = "Test Account", holderName = "John Doe", number = "12345")
        coEvery { financialRepository.getAccountById(1) } returns account

        viewModel.getItem(1, "financial").test {
            assertThat(awaitItem()).isEqualTo(account)
            awaitComplete()
        }
    }

    @Test
    fun `getItem should return identity document`() = runBlocking {
        val doc = IdentityDocument(id = 1, type = DocumentType.PASSPORT, country = "US", docNumber = "123", holderName = "John Doe")
        coEvery { identityRepository.getDocumentById(1) } returns doc

        viewModel.getItem(1, "identity").test {
            assertThat(awaitItem()).isEqualTo(doc)
            awaitComplete()
        }
    }

    @Test
    fun `getItem should return null for zero id`() = runBlocking {
        viewModel.getItem(0, "financial").test {
            assertThat(awaitItem()).isNull()
            awaitComplete()
        }
    }
}
