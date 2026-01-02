package com.vijay.cardkeeper.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vijay.cardkeeper.data.entity.AadharCard
import com.vijay.cardkeeper.data.entity.FinancialAccount
import com.vijay.cardkeeper.data.entity.GiftCard
import com.vijay.cardkeeper.data.entity.GreenCard
import com.vijay.cardkeeper.data.entity.IdentityDocument
import com.vijay.cardkeeper.data.entity.Passport
import com.vijay.cardkeeper.data.repository.AadharCardRepository
import com.vijay.cardkeeper.data.repository.FinancialRepository
import com.vijay.cardkeeper.data.repository.GiftCardRepository
import com.vijay.cardkeeper.data.repository.GreenCardRepository
import com.vijay.cardkeeper.data.repository.IdentityRepository
import com.vijay.cardkeeper.data.repository.PassportRepository
import com.vijay.cardkeeper.data.entity.PanCard
import com.vijay.cardkeeper.data.repository.PanCardRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
        financialRepository: FinancialRepository,
        identityRepository: IdentityRepository,
        passportRepository: PassportRepository,
        greenCardRepository: GreenCardRepository,
        aadharCardRepository: AadharCardRepository,
        giftCardRepository: GiftCardRepository,
        panCardRepository: PanCardRepository
) : ViewModel() {

        val bankAccounts: StateFlow<List<FinancialAccount>> =
                financialRepository
                        .allAccounts
                        .map { list ->
                                list.filter {
                                        it.type !=
                                                com.vijay.cardkeeper.data.entity.AccountType
                                                        .REWARDS_CARD &&
                                                it.type !=
                                                        com.vijay.cardkeeper.data.entity.AccountType
                                                                .LIBRARY_CARD
                                }
                        }
                        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

        val rewardsCards: StateFlow<List<FinancialAccount>> =
                financialRepository
                        .allAccounts
                        .map { list ->
                                list.filter {
                                        it.type ==
                                                com.vijay.cardkeeper.data.entity.AccountType
                                                        .REWARDS_CARD ||
                                                it.type ==
                                                        com.vijay.cardkeeper.data.entity.AccountType
                                                                .LIBRARY_CARD
                                }
                        }
                        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

        val identityDocuments: StateFlow<List<IdentityDocument>> =
                identityRepository.allDocuments.stateIn(
                        viewModelScope,
                        SharingStarted.Eagerly,
                        emptyList()
                )

        val passports: StateFlow<List<Passport>> =
                passportRepository.allPassports.stateIn(
                        viewModelScope,
                        SharingStarted.Eagerly,
                        emptyList()
                )

        val greenCards: StateFlow<List<GreenCard>> =
                greenCardRepository.allGreenCards.stateIn(
                        viewModelScope,
                        SharingStarted.Eagerly,
                        emptyList()
                )

        val aadharCards: StateFlow<List<AadharCard>> =
                aadharCardRepository.allAadharCards.stateIn(
                        viewModelScope,
                        SharingStarted.Eagerly,
                        emptyList()
                )

        val giftCards: StateFlow<List<GiftCard>> =
                giftCardRepository.getAllGiftCards().stateIn(
                        viewModelScope,
                        SharingStarted.Eagerly,
                        emptyList()
                )

        val panCards: StateFlow<List<PanCard>> =
                panCardRepository.getAllPanCards().stateIn(
                        viewModelScope,
                        SharingStarted.Eagerly,
                        emptyList()
                )
}
