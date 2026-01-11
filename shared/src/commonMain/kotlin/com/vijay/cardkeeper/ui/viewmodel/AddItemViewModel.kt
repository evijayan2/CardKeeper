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
import com.vijay.cardkeeper.data.entity.InsuranceCard
import com.vijay.cardkeeper.data.entity.InsuranceCardType
import com.vijay.cardkeeper.data.entity.PanCard
import com.vijay.cardkeeper.data.entity.Passport
import com.vijay.cardkeeper.data.entity.RewardCard
import com.vijay.cardkeeper.data.repository.AadharCardRepository
import com.vijay.cardkeeper.data.repository.FinancialRepository
import com.vijay.cardkeeper.data.repository.GiftCardRepository
import com.vijay.cardkeeper.data.repository.GreenCardRepository
import com.vijay.cardkeeper.data.repository.IdentityRepository
import com.vijay.cardkeeper.data.repository.PanCardRepository
import com.vijay.cardkeeper.data.repository.RewardCardRepository
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.launch

class AddItemViewModel(
        private val financialRepository: FinancialRepository,
        private val identityRepository: IdentityRepository,
        private val passportRepository: com.vijay.cardkeeper.data.repository.PassportRepository,
        private val greenCardRepository: GreenCardRepository,
        private val aadharCardRepository: AadharCardRepository,

        private val giftCardRepository: GiftCardRepository,
        private val panCardRepository: PanCardRepository,
        private val rewardCardRepository: RewardCardRepository,
        private val insuranceCardRepository: com.vijay.cardkeeper.data.repository.InsuranceCardRepository,
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

        fun savePassport(
                id: Int = 0,
                firstName: String?,
                lastName: String?,
                passportNumber: String,
                nationality: String?,
                dob: String?,
                dateOfIssue: String?,
                dateOfExpiry: String?,
                placeOfIssue: String?,
                placeOfBirth: String?,
                sex: String?,
                authority: String?,
                frontImagePath: String?,
                backImagePath: String?
        ) = viewModelScope.launch {
                val card = Passport(
                        id = id,
                        passportNumber = passportNumber,
                        countryCode = "USA", // Default or derived
                        surname = lastName,
                        givenNames = firstName,
                        nationality = nationality,
                        dob = dob,
                        dateOfIssue = dateOfIssue,
                        dateOfExpiry = dateOfExpiry,
                        placeOfIssue = placeOfIssue,
                        placeOfBirth = placeOfBirth,
                        sex = sex,
                        authority = authority,
                        frontImagePath = frontImagePath,
                        backImagePath = backImagePath
                )
                 savePassport(card)
        }

        fun savePassport(passport: Passport) = viewModelScope.launch {
                        if (passport.id > 0) {
                                passportRepository.update(passport)
                        } else {
                                passportRepository.insert(passport)
                        }
                        scheduleExpirationCheck()
                }

        fun saveGreenCard(
                id: Int = 0,
                firstName: String,
                lastName: String,
                uscisNumber: String,
                category: String,
                countryOfBirth: String,
                dob: String,
                dateOfExpiry: String,
                residentSince: String,
                sex: String,
                frontImagePath: String?,
                backImagePath: String?
        ) = viewModelScope.launch {
                val card = GreenCard(
                        id = id,
                        givenName = firstName,
                        surname = lastName,
                        uscisNumber = uscisNumber,
                        category = category,
                        countryOfBirth = countryOfBirth,
                        dob = dob,
                        sex = sex,
                        expiryDate = dateOfExpiry,
                        residentSince = residentSince,
                        frontImagePath = frontImagePath,
                        backImagePath = backImagePath
                )
                saveGreenCard(card)
        }

        fun saveGreenCard(greenCard: GreenCard) = viewModelScope.launch {
                        if (greenCard.id > 0) {
                                greenCardRepository.update(greenCard)
                        } else {
                                greenCardRepository.insert(greenCard)
                        }
                        scheduleExpirationCheck()
                }

        fun saveAadharCard(
                id: Int = 0,
                name: String,
                aadharNumber: String,
                dob: String,
                gender: String,
                address: String,
                vid: String?,
                frontImagePath: String?,
                backImagePath: String?
        ) = viewModelScope.launch {
                val card = AadharCard(
                        id = id,
                        referenceId = "", // Not available from manual entry
                        holderName = name,
                        dob = dob,
                        gender = gender,
                        address = address,
                        uid = aadharNumber,
                        maskedAadhaarNumber = aadharNumber.takeLast(4).let { "xxxx xxxx $it" },
                        vid = vid,
                        frontImagePath = frontImagePath,
                        backImagePath = backImagePath
                )
                saveAadharCard(card)
        }

        fun saveAadharCard(aadharCard: AadharCard) = viewModelScope.launch {
                        if (aadharCard.id > 0) {
                                aadharCardRepository.update(aadharCard)
                        } else {
                                aadharCardRepository.insert(aadharCard)
                        }
                        scheduleExpirationCheck()
                }

        fun saveGiftCard(
                id: Int = 0,
                merchantName: String,
                cardNumber: String,
                pin: String?,
                balance: String?,
                expiryDate: String?,
                frontImagePath: String?,
                backImagePath: String?
        ) = viewModelScope.launch {
                val card = GiftCard(
                        id = id,
                        providerName = merchantName,
                        cardNumber = cardNumber,
                        pin = pin,
                        frontImagePath = frontImagePath,
                        backImagePath = backImagePath,
                        notes = if (balance != null) "Balance: $balance" else null
                )
                saveGiftCard(card)
        }

        fun saveGiftCard(giftCard: GiftCard) = viewModelScope.launch {
                        if (giftCard.id > 0) {
                                giftCardRepository.updateGiftCard(giftCard)
                        } else {
                                giftCardRepository.insertGiftCard(giftCard)
                        }
                        scheduleExpirationCheck()
                }

        fun savePanCard(
                id: Int = 0,
                name: String,
                fatherName: String?,
                panNumber: String,
                dob: String?,
                category: String?,
                frontImagePath: String?,
                backImagePath: String?
        ) = viewModelScope.launch {
                val card = PanCard(
                        id = id,
                        panNumber = panNumber,
                        holderName = name,
                        fatherName = fatherName,
                        dob = dob,
                        frontImagePath = frontImagePath,
                        backImagePath = backImagePath
                )
                savePanCard(card)
        }

        fun savePanCard(panCard: PanCard) = viewModelScope.launch {
                if (panCard.id > 0) {
                        panCardRepository.update(panCard)
                } else {
                        panCardRepository.insert(panCard)
                }
                scheduleExpirationCheck()
        }

        fun saveRewardCard(
                id: Int = 0,
                merchantName: String,
                cardNumber: String?,
                pin: String?,
                expiryDate: String?,
                points: String?,
                email: String?,
                phone: String?,
                website: String?,
                notes: String?,
                barcodeType: String?,
                barcodeValue: String?,
                frontImagePath: String?,
                backImagePath: String?
        ) = viewModelScope.launch {
                val card = RewardCard(
                        id = id,
                        name = merchantName,
                        type = AccountType.REWARDS_CARD,
                        barcode = barcodeValue ?: cardNumber,
                        linkedPhoneNumber = phone,
                        notes = notes,
                        frontImagePath = frontImagePath,
                        backImagePath = backImagePath
                )
                saveRewardCard(card)
        }

        fun saveRewardCard(rewardCard: RewardCard) = viewModelScope.launch {
                if (rewardCard.id > 0) {
                        rewardCardRepository.updateRewardCard(rewardCard)
                } else {
                        rewardCardRepository.insertRewardCard(rewardCard)
                }
                scheduleExpirationCheck()
        }

        fun saveInsuranceCard(
                id: Int = 0,
                provider: String,
                policyNumber: String,
                groupNumber: String?,
                holderName: String,
                validTill: String?,
                notes: String?,
                frontImagePath: String?,
                backImagePath: String?
        ) = viewModelScope.launch {
                val card = com.vijay.cardkeeper.data.entity.InsuranceCard(
                        id = id,
                        providerName = provider,
                        type = InsuranceCardType.MEDICAL, // Default
                        policyNumber = policyNumber,
                        groupNumber = groupNumber,
                        policyHolderName = holderName,
                        expiryDate = validTill,
                        notes = notes,
                        frontImagePath = frontImagePath,
                        backImagePath = backImagePath
                )
                saveInsuranceCard(card)
        }

        fun saveInsuranceCard(insuranceCard: com.vijay.cardkeeper.data.entity.InsuranceCard) = viewModelScope.launch {
                if (insuranceCard.id > 0) {
                        insuranceCardRepository.update(insuranceCard)
                } else {
                        insuranceCardRepository.insert(insuranceCard)
                }
                scheduleExpirationCheck()
        }

        fun deleteInsuranceCard(insuranceCard: com.vijay.cardkeeper.data.entity.InsuranceCard) {
                viewModelScope.launch { 
                    insuranceCardRepository.delete(insuranceCard)
                    scheduleExpirationCheck()
                }
        }

        fun getItem(id: Int?, type: String?): kotlinx.coroutines.flow.Flow<Any?> =
                kotlinx.coroutines.flow.flow {
                        if (id == null || id == 0) {
                                emit(null)
                        } else {
                                if (type == "financial") {
                                        emit(financialRepository.getAccountById(id))
                                } else if (type == "rewards" || type == "REWARDS_CARD") {
                                        emit(rewardCardRepository.getRewardCardById(id))
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
                                } else if (type == "pancard") {
                                        emitAll(panCardRepository.getPanCard(id))
                                } else if (type == "insurance") {
                                        emitAll(insuranceCardRepository.getInsuranceCard(id))
                                }
                        }
                }
}
