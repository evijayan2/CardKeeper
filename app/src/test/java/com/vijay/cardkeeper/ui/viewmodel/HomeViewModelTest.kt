package com.vijay.cardkeeper.ui.viewmodel

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.vijay.cardkeeper.data.entity.AccountType
import com.vijay.cardkeeper.data.entity.FinancialAccount
import com.vijay.cardkeeper.data.repository.AadharCardRepository
import com.vijay.cardkeeper.data.repository.FinancialRepository
import com.vijay.cardkeeper.data.repository.GiftCardRepository
import com.vijay.cardkeeper.data.repository.GreenCardRepository
import com.vijay.cardkeeper.data.repository.IdentityRepository
import com.vijay.cardkeeper.data.repository.PassportRepository
import com.vijay.cardkeeper.data.repository.PanCardRepository
import com.vijay.cardkeeper.data.repository.RewardCardRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class HomeViewModelTest {

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @Test
    fun `bankAccounts emits only non-rewards and non-library financial accounts`() = runTest {
        // Given
        val bankAccount = FinancialAccount(id = 1, institutionName = "Test Bank", type = AccountType.BANK_ACCOUNT, accountName = "Test Account", holderName = "John Doe", number = "12345")
        val creditCard = FinancialAccount(id = 2, institutionName = "Test CC", type = AccountType.CREDIT_CARD, accountName = "CC", holderName = "John Doe", number = "54321")
        val rewardsCard = FinancialAccount(id = 3, institutionName = "Test Rewards", type = AccountType.REWARDS_CARD, accountName = "Rewards", holderName = "John Doe", number = "67890")
        
        val financialRepository: FinancialRepository = mockk {
            every { allAccounts } returns flowOf(listOf(bankAccount, creditCard, rewardsCard))
        }

        // When
        val viewModel = HomeViewModel(
            financialRepository = financialRepository,
            identityRepository = mockk(relaxed = true) { every { allDocuments } returns flowOf(emptyList()) },
            passportRepository = mockk(relaxed = true) { every { allPassports } returns flowOf(emptyList()) },
            greenCardRepository = mockk(relaxed = true) { every { allGreenCards } returns flowOf(emptyList()) },
            aadharCardRepository = mockk(relaxed = true) { every { allAadharCards } returns flowOf(emptyList()) },
            giftCardRepository = mockk(relaxed = true) { every { getAllGiftCards() } returns flowOf(emptyList()) },
            panCardRepository = mockk(relaxed = true) { every { allPanCards } returns flowOf(emptyList()) },
            rewardCardRepository = mockk(relaxed = true) { every { getAllRewardCards() } returns flowOf(emptyList()) }
        )

        // Then
        viewModel.bankAccounts.test {
            val emitted = awaitItem()
            assertThat(emitted).containsExactly(bankAccount, creditCard)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `rewardsCards emits only rewards and library cards`() = runTest {
         // Given
        val bankAccount = FinancialAccount(id = 1, institutionName = "Test Bank", type = AccountType.BANK_ACCOUNT, accountName = "Test Account", holderName = "John Doe", number = "12345")
        // Use proper RewardCard entity
        val rewardsCard = com.vijay.cardkeeper.data.entity.RewardCard(id = 2, name = "Rewards", type = AccountType.REWARDS_CARD)
        
        val financialRepository: FinancialRepository = mockk {
            every { allAccounts } returns flowOf(listOf(bankAccount))
        }

        // When
        val viewModel = HomeViewModel(
            financialRepository = financialRepository,
            identityRepository = mockk(relaxed = true) { every { allDocuments } returns flowOf(emptyList()) },
            passportRepository = mockk(relaxed = true) { every { allPassports } returns flowOf(emptyList()) },
            greenCardRepository = mockk(relaxed = true) { every { allGreenCards } returns flowOf(emptyList()) },
            aadharCardRepository = mockk(relaxed = true) { every { allAadharCards } returns flowOf(emptyList()) },
            giftCardRepository = mockk(relaxed = true) { every { getAllGiftCards() } returns flowOf(emptyList()) },
            panCardRepository = mockk(relaxed = true) { every { allPanCards } returns flowOf(emptyList()) },
            rewardCardRepository = mockk(relaxed = true) { every { getAllRewardCards() } returns flowOf(listOf(rewardsCard)) }
        )

        // Then
        viewModel.rewardsCards.test {
            val emitted = awaitItem()
            assertThat(emitted).containsExactly(rewardsCard)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
