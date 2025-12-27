package com.vijay.cardkeeper.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vijay.cardkeeper.data.entity.FinancialAccount
import com.vijay.cardkeeper.data.entity.GreenCard
import com.vijay.cardkeeper.data.entity.IdentityDocument
import com.vijay.cardkeeper.data.entity.Passport
import com.vijay.cardkeeper.data.repository.FinancialRepository
import com.vijay.cardkeeper.data.repository.GreenCardRepository
import com.vijay.cardkeeper.data.repository.IdentityRepository
import com.vijay.cardkeeper.data.repository.PassportRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
        financialRepository: FinancialRepository,
        identityRepository: IdentityRepository,
        passportRepository: PassportRepository,
        greenCardRepository: GreenCardRepository
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
                        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
                        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        val identityDocuments: StateFlow<List<IdentityDocument>> =
                identityRepository.allDocuments.stateIn(
                        viewModelScope,
                        SharingStarted.WhileSubscribed(5000),
                        emptyList()
                )

        val passports: StateFlow<List<Passport>> =
                passportRepository.allPassports.stateIn(
                        viewModelScope,
                        SharingStarted.WhileSubscribed(5000),
                        emptyList()
                )

        val greenCards: StateFlow<List<GreenCard>> =
                greenCardRepository.allGreenCards.stateIn(
                        viewModelScope,
                        SharingStarted.WhileSubscribed(5000),
                        emptyList()
                )
}
