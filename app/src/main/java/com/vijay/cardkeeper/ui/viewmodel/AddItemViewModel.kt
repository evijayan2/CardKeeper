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
                ifsc: String,
                expiryDate: String?, // UI passes String, we might parse or store
                cvv: String?,
                pin: String?,
                notes: String?,
                contact: String?,
                cardNetwork: String?
        ) {
                viewModelScope.launch {
                        // Simple date parse for Expiry MM/YY to millis if needed, or store
                        // separate.
                        // Entity has `expiryDate: Long?`. Let's store a dummy epoch for now or
                        // prompt user.
                        // For this quick prototype, we made need to update Entity to support String
                        // expiry or
                        // parse it.
                        // FinancialAccount has `expiryDate: String?`.
                        // If the user inputs "12/26", we can ballpark it.
                        // Parsing "MM/yy" to timestamp:

                        val account =
                                FinancialAccount(
                                        type = type,
                                        institutionName = institution,
                                        accountName = name,
                                        holderName = holder,
                                        number = number,
                                        routingNumber = routing.ifBlank { null },
                                        ifscCode = ifsc.ifBlank { null },
                                        cvv = cvv?.ifBlank { null },
                                        cardPin = pin?.ifBlank { null },
                                        notes = notes?.ifBlank { null },
                                        lostCardContactNumber = contact?.ifBlank { null },
                                        expiryDate = expiryDate?.ifBlank { null },
                                        cardNetwork = cardNetwork?.ifBlank { null }
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
                        val doc =
                                IdentityDocument(
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
