package com.vijay.cardkeeper.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vijay.cardkeeper.data.entity.AadharCard
import com.vijay.cardkeeper.data.entity.AccountType
import com.vijay.cardkeeper.data.entity.BankAccountSubType
import com.vijay.cardkeeper.data.entity.DocumentType
import com.vijay.cardkeeper.data.entity.FinancialAccount
import com.vijay.cardkeeper.data.entity.GreenCard
import com.vijay.cardkeeper.data.entity.GiftCard
import com.vijay.cardkeeper.data.entity.IdentityDocument
import com.vijay.cardkeeper.data.repository.AadharCardRepository
import com.vijay.cardkeeper.data.repository.FinancialRepository
import com.vijay.cardkeeper.data.repository.GiftCardRepository
import com.vijay.cardkeeper.data.repository.GreenCardRepository
import com.vijay.cardkeeper.data.repository.IdentityRepository
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.launch

class AddItemViewModel(
        private val financialRepository: FinancialRepository,
        private val identityRepository: IdentityRepository,
        private val passportRepository: com.vijay.cardkeeper.data.repository.PassportRepository,
        private val greenCardRepository: GreenCardRepository,
        private val aadharCardRepository: AadharCardRepository,
        private val giftCardRepository: GiftCardRepository,
        private val expirationScheduler: com.vijay.cardkeeper.domain.ExpirationScheduler
) : ViewModel() {

        private fun scheduleExpirationCheck() {
                expirationScheduler.scheduleExpirationCheck()
        }

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
        ) = viewModelScope.launch {
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
                        scheduleExpirationCheck()
                }

        fun saveFinancialAccount(account: FinancialAccount) = viewModelScope.launch {
                        if (account.id > 0) {
                                financialRepository.updateAccount(account)
                        } else {
                                financialRepository.insertAccount(account)
                        }
                        scheduleExpirationCheck()
                }

        suspend fun getFinancialAccount(id: Int) = financialRepository.getAccountById(id)

        suspend fun getIdentityDocument(id: Int) = identityRepository.getDocumentById(id)

        suspend fun getPassport(id: Int) = passportRepository.getPassport(id)

        suspend fun getGreenCard(id: Int) = greenCardRepository.getGreenCard(id)

        suspend fun getAadharCard(id: Int) = aadharCardRepository.getAadharCard(id)

        suspend fun getGiftCard(id: Int) = giftCardRepository.getGiftCardById(id)

        fun saveIdentityDocument(
                id: Int = 0,
                type: DocumentType,
                country: String,
                docNumber: String,
                holder: String,
                expiryDate: String?,
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
        ) = viewModelScope.launch {
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
                        scheduleExpirationCheck()
                }

        fun saveIdentityDocument(document: IdentityDocument) = viewModelScope.launch {
                        if (document.id > 0) {
                                identityRepository.updateDocument(document)
                        } else {
                                identityRepository.insertDocument(document)
                        }
                        scheduleExpirationCheck()
                }

        fun savePassport(passport: com.vijay.cardkeeper.data.entity.Passport) = viewModelScope.launch {
                        if (passport.id > 0) {
                                passportRepository.update(passport)
                        } else {
                                passportRepository.insert(passport)
                        }
                        scheduleExpirationCheck()
                }

        fun deleteIdentityDocument(document: IdentityDocument) {
                viewModelScope.launch { 
                    identityRepository.deleteDocument(document)
                    scheduleExpirationCheck()
                }
        }

        fun deletePassport(passport: com.vijay.cardkeeper.data.entity.Passport) {
                viewModelScope.launch { 
                    passportRepository.delete(passport)
                    scheduleExpirationCheck()
                }
        }

        fun saveGreenCard(greenCard: GreenCard) = viewModelScope.launch {
                        if (greenCard.id > 0) {
                                greenCardRepository.update(greenCard)
                        } else {
                                greenCardRepository.insert(greenCard)
                        }
                        scheduleExpirationCheck()
                }

        fun deleteGreenCard(greenCard: GreenCard) {
                viewModelScope.launch { 
                    greenCardRepository.delete(greenCard)
                    scheduleExpirationCheck()
                }
        }

        fun saveAadharCard(aadharCard: AadharCard) = viewModelScope.launch {
                        if (aadharCard.id > 0) {
                                aadharCardRepository.update(aadharCard)
                        } else {
                                aadharCardRepository.insert(aadharCard)
                        }
                        scheduleExpirationCheck()
                }

        fun deleteAadharCard(aadharCard: AadharCard) {
                viewModelScope.launch { 
                    aadharCardRepository.delete(aadharCard)
                    scheduleExpirationCheck()
                }
        }

        fun saveGiftCard(giftCard: GiftCard) = viewModelScope.launch {
                        if (giftCard.id > 0) {
                                giftCardRepository.updateGiftCard(giftCard)
                        } else {
                                giftCardRepository.insertGiftCard(giftCard)
                        }
                        scheduleExpirationCheck()
                }

        fun deleteGiftCard(giftCard: GiftCard) {
                viewModelScope.launch { 
                    giftCardRepository.deleteGiftCard(giftCard)
                    scheduleExpirationCheck()
                }
        }

        fun getItem(id: Int?, type: String?): kotlinx.coroutines.flow.Flow<Any?> =
                kotlinx.coroutines.flow.flow {
                        if (id == null || id == 0) {
                                emit(null)
                        } else {
                                if (type == "financial" || type == "REWARDS_CARD") {
                                        emit(financialRepository.getAccountById(id))
                                } else if (type == "identity" || type == "DRIVER_LICENSE") {
                                        emit(identityRepository.getDocumentById(id))
                                } else if (type == "passport" || type == "PASSPORT") {
                                        emitAll(passportRepository.getPassport(id))
                                } else if (type == "greencard") {
                                        emitAll(greenCardRepository.getGreenCard(id))
                                } else if (type == "aadhar") {
                                        emitAll(aadharCardRepository.getAadharCard(id))
                                } else if (type == "giftcard") {
                                        emit(giftCardRepository.getGiftCardById(id))
                                }
                        }
                }
}
