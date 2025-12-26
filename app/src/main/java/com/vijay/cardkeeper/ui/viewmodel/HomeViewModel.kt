package com.vijay.cardkeeper.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vijay.cardkeeper.data.entity.FinancialAccount
import com.vijay.cardkeeper.data.entity.IdentityDocument
import com.vijay.cardkeeper.data.entity.Passport
import com.vijay.cardkeeper.data.repository.FinancialRepository
import com.vijay.cardkeeper.data.repository.IdentityRepository
import com.vijay.cardkeeper.data.repository.PassportRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
        financialRepository: FinancialRepository,
        identityRepository: IdentityRepository,
        passportRepository: PassportRepository
) : ViewModel() {

        val financialAccounts: StateFlow<List<FinancialAccount>> =
                financialRepository.allAccounts.stateIn(
                        viewModelScope,
                        SharingStarted.WhileSubscribed(5000),
                        emptyList()
                )

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
}
