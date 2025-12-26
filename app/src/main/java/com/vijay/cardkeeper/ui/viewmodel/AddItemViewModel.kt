package com.vijay.cardkeeper.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vijay.cardkeeper.data.entity.AccountType
import com.vijay.cardkeeper.data.entity.BankAccountSubType
import com.vijay.cardkeeper.data.entity.DocumentType
import com.vijay.cardkeeper.data.entity.FinancialAccount
import com.vijay.cardkeeper.data.entity.IdentityDocument
import com.vijay.cardkeeper.data.repository.FinancialRepository
import com.vijay.cardkeeper.data.repository.IdentityRepository
import kotlinx.coroutines.launch

class AddItemViewModel(
        private val financialRepository: FinancialRepository,
        private val identityRepository: IdentityRepository,
        private val passportRepository: com.vijay.cardkeeper.data.repository.PassportRepository
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
                swift: String = "",
                expiryDate: String?,
                cvv: String?,
                pin: String?,
                notes: String?,
                contact: String?,
                cardNetwork: String?,
                frontImagePath: String?,
                backImagePath: String?,
                barcode: String? = null,
                barcodeFormat: Int? = null,
                linkedPhoneNumber: String? = null,
                logoImagePath: String? = null,
                // New bank account fields
                accountSubType: BankAccountSubType? = null,
                wireNumber: String? = null,
                branchAddress: String? = null,
                branchContactNumber: String? = null,
                bankWebUrl: String? = null,
                bankBrandColor: Long? = null,
                holderAddress: String? = null
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
                                        swiftCode = swift.ifBlank { null },
                                        cvv = cvv?.ifBlank { null },
                                        cardPin = pin?.ifBlank { null },
                                        notes = notes?.ifBlank { null },
                                        lostCardContactNumber = contact?.ifBlank { null },
                                        expiryDate = expiryDate?.ifBlank { null },
                                        cardNetwork = cardNetwork?.ifBlank { null },
                                        frontImagePath = frontImagePath,
                                        backImagePath = backImagePath,
                                        barcode = barcode?.ifBlank { null },
                                        barcodeFormat = barcodeFormat,
                                        linkedPhoneNumber = linkedPhoneNumber?.ifBlank { null },
                                        logoImagePath = logoImagePath,
                                        // New bank account fields
                                        accountSubType = accountSubType,
                                        wireNumber = wireNumber?.ifBlank { null },
                                        branchAddress = branchAddress?.ifBlank { null },
                                        branchContactNumber = branchContactNumber?.ifBlank { null },
                                        bankWebUrl = bankWebUrl?.ifBlank { null },
                                        bankBrandColor = bankBrandColor,
                                        holderAddress = holderAddress?.ifBlank { null }
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

        suspend fun getPassport(id: Int) = passportRepository.getPassport(id)

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

        fun savePassport(passport: com.vijay.cardkeeper.data.entity.Passport) {
                viewModelScope.launch {
                        if (passport.id > 0) {
                                passportRepository.update(passport)
                        } else {
                                passportRepository.insert(passport)
                        }
                }
        }

        fun deleteIdentityDocument(document: IdentityDocument) {
                viewModelScope.launch { identityRepository.deleteDocument(document) }
        }

        fun getItem(id: Int?, type: String?): kotlinx.coroutines.flow.Flow<Any?> =
                kotlinx.coroutines.flow.flow {
                        if (id == null || id == 0) {
                                emit(null)
                        } else {
                                if (type == "financial") {
                                        emit(financialRepository.getAccountById(id))
                                } else if (type == "identity") {
                                        emit(identityRepository.getDocumentById(id))
                                } else if (type == "passport") {
                                        emit(passportRepository.getPassport(id))
                                }
                        }
                }
}
