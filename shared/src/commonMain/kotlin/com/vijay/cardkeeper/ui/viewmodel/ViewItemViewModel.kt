package com.vijay.cardkeeper.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vijay.cardkeeper.data.entity.*
import com.vijay.cardkeeper.data.repository.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class ViewItemViewModel(
    private val financialRepository: FinancialRepository,
    private val giftCardRepository: GiftCardRepository,
    private val identityRepository: IdentityRepository,
    private val passportRepository: PassportRepository,
    private val greenCardRepository: GreenCardRepository,
    private val aadharCardRepository: AadharCardRepository,
    private val panCardRepository: PanCardRepository,
    private val rewardCardRepository: RewardCardRepository
) : ViewModel() {

    private val _selectedAccount = MutableStateFlow<FinancialAccount?>(null)
    val selectedAccount: StateFlow<FinancialAccount?> = _selectedAccount.asStateFlow()

    private val _selectedGiftCard = MutableStateFlow<GiftCard?>(null)
    val selectedGiftCard: StateFlow<GiftCard?> = _selectedGiftCard.asStateFlow()

    private val _selectedIdentityDocument = MutableStateFlow<IdentityDocument?>(null)
    val selectedIdentityDocument: StateFlow<IdentityDocument?> = _selectedIdentityDocument.asStateFlow()

    private val _selectedPassport = MutableStateFlow<Passport?>(null)
    val selectedPassport: StateFlow<Passport?> = _selectedPassport.asStateFlow()

    private val _selectedGreenCard = MutableStateFlow<GreenCard?>(null)
    val selectedGreenCard: StateFlow<GreenCard?> = _selectedGreenCard.asStateFlow()

    private val _selectedAadharCard = MutableStateFlow<AadharCard?>(null)
    val selectedAadharCard: StateFlow<AadharCard?> = _selectedAadharCard.asStateFlow()

    private val _selectedPanCard = MutableStateFlow<PanCard?>(null)
    val selectedPanCard: StateFlow<PanCard?> = _selectedPanCard.asStateFlow()

    private val _selectedRewardCard = MutableStateFlow<RewardCard?>(null)
    val selectedRewardCard: StateFlow<RewardCard?> = _selectedRewardCard.asStateFlow()

    private val _fullScreenImage = MutableStateFlow<String?>(null)
    val fullScreenImage: StateFlow<String?> = _fullScreenImage.asStateFlow()

    private val _qrCodeBitmap = MutableStateFlow<Any?>(null) // Any? to avoid Android dependency in KMP if strictly shared, but for now we assume shared logic
    val qrCodeBitmap: StateFlow<Any?> = _qrCodeBitmap.asStateFlow()

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
        viewModelScope.launch { 
            val card = giftCardRepository.getGiftCardById(id)
            _selectedGiftCard.value = card
            
            // If card has QR data, generate bitmap
            card?.qrCode?.let { qrData ->
                 // In a real KMP architecture, we'd use a platform-specific generator.
                 // For this hybrid setup, we'll expose the data and let the UI helper do it properly 
                 // OR we move the generator to a shared utility if possible.
                 // Given constraints: We will keep generation in UI *ViewModel* but running on IO dispatcher if we had image libs here.
                 // HOWEVER, `shared` module likely doesn't have Android Bitmap. 
                 // We will stick to state exposure and let the UI COMPONENT handle the heavy lifting via a LaunchedEffect + State,
                 // OR better: we keep the VM pure and just expose the data. 
            }
        }
    }

    fun deleteGiftCard(giftCard: GiftCard) {
        viewModelScope.launch { giftCardRepository.deleteGiftCard(giftCard) }
    }

    fun loadIdentityDocument(id: Int) {
        viewModelScope.launch { _selectedIdentityDocument.value = identityRepository.getDocumentById(id) }
    }

    fun deleteIdentityDocument(document: IdentityDocument) {
        viewModelScope.launch { identityRepository.deleteDocument(document) }
    }

    fun loadPassport(id: Int) {
        viewModelScope.launch { _selectedPassport.value = passportRepository.getPassport(id).firstOrNull() }
    }

    fun deletePassport(passport: Passport) {
        viewModelScope.launch { passportRepository.delete(passport) }
    }

    fun loadGreenCard(id: Int) {
        viewModelScope.launch { _selectedGreenCard.value = greenCardRepository.getGreenCard(id).firstOrNull() }
    }

    fun deleteGreenCard(greenCard: GreenCard) {
        viewModelScope.launch { greenCardRepository.delete(greenCard) }
    }

    fun loadAadharCard(id: Int) {
        viewModelScope.launch { _selectedAadharCard.value = aadharCardRepository.getAadharCard(id).firstOrNull() }
    }

    fun deleteAadharCard(aadharCard: AadharCard) {
        viewModelScope.launch { aadharCardRepository.delete(aadharCard) }
    }

    fun loadPanCard(id: Int) {
        viewModelScope.launch { _selectedPanCard.value = panCardRepository.getPanCard(id).firstOrNull() }
    }

    fun deletePanCard(panCard: PanCard) {
        viewModelScope.launch { panCardRepository.delete(panCard) }
    }

    fun loadRewardCard(id: Int) {
        viewModelScope.launch { _selectedRewardCard.value = rewardCardRepository.getRewardCardById(id) }
    }

    fun deleteRewardCard(rewardCard: RewardCard) {
        viewModelScope.launch { rewardCardRepository.deleteRewardCard(rewardCard) }
    }
}
