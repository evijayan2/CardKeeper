package com.vijay.cardkeeper.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vijay.cardkeeper.data.entity.AccountType
import com.vijay.cardkeeper.data.entity.DocumentType
import com.vijay.cardkeeper.data.entity.FinancialAccount
import com.vijay.cardkeeper.data.entity.IdentityDocument
import com.vijay.cardkeeper.data.repository.FinancialRepository
import com.vijay.cardkeeper.data.repository.IdentityRepository
import kotlinx.coroutines.launch

class AddItemViewModel(
    private val financialRepository: FinancialRepository,
    private val identityRepository: IdentityRepository
) : ViewModel() {

    fun saveFinancialAccount(
        type: AccountType,
        institution: String,
        name: String,
        holder: String,
        number: String,
        routing: String,
        ifsc: String
    ) {
        viewModelScope.launch {
            val account = FinancialAccount(
                type = type,
                institutionName = institution,
                accountName = name,
                holderName = holder,
                number = number,
                routingNumber = routing.ifBlank { null },
                ifscCode = ifsc.ifBlank { null }
            )
            financialRepository.insertAccount(account)
        }
    }

    fun saveIdentityDocument(
        type: DocumentType,
        country: String,
        docNumber: String,
        holder: String,
        expiryDate: Long?
    ) {
        viewModelScope.launch {
            val doc = IdentityDocument(
                type = type,
                country = country,
                docNumber = docNumber,
                holderName = holder,
                expiryDate = expiryDate
            )
            identityRepository.insertDocument(doc)
        }
    }
}
