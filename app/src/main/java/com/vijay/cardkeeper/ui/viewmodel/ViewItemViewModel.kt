package com.vijay.cardkeeper.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vijay.cardkeeper.data.entity.FinancialAccount
import com.vijay.cardkeeper.data.entity.GiftCard
import com.vijay.cardkeeper.data.repository.FinancialRepository
import com.vijay.cardkeeper.data.repository.GiftCardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ViewItemViewModel(
    private val financialRepository: FinancialRepository,
    private val giftCardRepository: GiftCardRepository
) : ViewModel() {

    private val _selectedAccount = MutableStateFlow<FinancialAccount?>(null)
    val selectedAccount: StateFlow<FinancialAccount?> = _selectedAccount.asStateFlow()

    private val _selectedGiftCard = MutableStateFlow<GiftCard?>(null)
    val selectedGiftCard: StateFlow<GiftCard?> = _selectedGiftCard.asStateFlow()

    private val _fullScreenImage = MutableStateFlow<String?>(null)
    val fullScreenImage: StateFlow<String?> = _fullScreenImage.asStateFlow()

    fun loadAccount(id: Int) {
        viewModelScope.launch { _selectedAccount.value = financialRepository.getAccountById(id) }
    }

    fun setFullScreenImage(path: String?) {
        _fullScreenImage.value = path
    }

    fun deleteAccount(account: FinancialAccount) {
        viewModelScope.launch { financialRepository.deleteAccount(account) }
    }

    fun loadGiftCard(id: Int) {
        viewModelScope.launch { _selectedGiftCard.value = giftCardRepository.getGiftCardById(id) }
    }

    fun deleteGiftCard(giftCard: GiftCard) {
        viewModelScope.launch { giftCardRepository.deleteGiftCard(giftCard) }
    }
}
