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
                id: Int = 0,
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
                cardNetwork: String?,
                frontImagePath: String?,
                backImagePath: String?
        ) {
                viewModelScope.launch {
                        val account =
                                FinancialAccount(
                                        id = id,
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
                                        cardNetwork = cardNetwork?.ifBlank { null },
                                        frontImagePath = frontImagePath,
                                        backImagePath = backImagePath
                                )
                        if (id > 0) {
                                financialRepository.updateAccount(account)
                        } else {
                                financialRepository.insertAccount(account)
                        }
                }
        }

        fun saveFinancialAccount(account: FinancialAccount) {
                viewModelScope.launch {
                        if (account.id > 0) {
                                financialRepository.updateAccount(account)
                        } else {
                                financialRepository.insertAccount(account)
                        }
                }
        }

        suspend fun getFinancialAccount(id: Int) = financialRepository.getAccountById(id)

        suspend fun getIdentityDocument(id: Int) = identityRepository.getDocumentById(id)

        fun saveIdentityDocument(
                id: Int = 0,
                type: DocumentType,
                country: String,
                docNumber: String,
                holder: String,
                expiryDate: Long?,
                frontImagePath: String?,
                backImagePath: String?,
                state: String?,
                address: String?,
                dob: String?,
                sex: String?,
                eyeColor: String?,
                height: String?,
                licenseClass: String?,
                restrictions: String?,
                endorsements: String?,
                issuingAuthority: String?
        ) {
                viewModelScope.launch {
                        val doc =
                                IdentityDocument(
                                        id = id,
                                        type = type,
                                        country = country,
                                        docNumber = docNumber,
                                        holderName = holder,
                                        expiryDate = expiryDate,
                                        issuingAuthority = issuingAuthority,
                                        frontImagePath = frontImagePath,
                                        backImagePath = backImagePath,
                                        state = state,
                                        address = address,
                                        dob = dob,
                                        sex = sex,
                                        eyeColor = eyeColor,
                                        height = height,
                                        licenseClass = licenseClass,
                                        restrictions = restrictions,
                                        endorsements = endorsements
                                )
                        if (id > 0) {
                                identityRepository.updateDocument(doc)
                        } else {
                                identityRepository.insertDocument(doc)
                        }
                }
        }

        fun deleteIdentityDocument(document: IdentityDocument) {
                viewModelScope.launch { identityRepository.deleteDocument(document) }
        }
}
