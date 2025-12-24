package com.vijay.cardkeeper.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vijay.cardkeeper.data.entity.FinancialAccount
import com.vijay.cardkeeper.data.repository.FinancialRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ViewItemViewModel(private val financialRepository: FinancialRepository) : ViewModel() {

    private val _selectedAccount = MutableStateFlow<FinancialAccount?>(null)
    val selectedAccount: StateFlow<FinancialAccount?> = _selectedAccount.asStateFlow()

    fun loadAccount(id: Int) {
        viewModelScope.launch { _selectedAccount.value = financialRepository.getAccountById(id) }
    }
}
