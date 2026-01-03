package com.vijay.cardkeeper.ui.item.logic

import android.graphics.Bitmap
import com.vijay.cardkeeper.data.entity.AccountType
import com.vijay.cardkeeper.data.entity.DocumentType
import com.vijay.cardkeeper.scanning.*
import com.vijay.cardkeeper.ui.item.forms.*

/**
 * Handles the business logic of processing a scanned bitmap and updating the relevant form state.
 * This extracts logic from the UI layer (AddItemRoute).
 */
class ScanResultProcessor(
    private val paymentScanner: PaymentCardScanner,
    private val rewardsScanner: RewardsScanner,
    private val identityScanner: IdentityScanner,
    private val driverLicenseScanner: DriverLicenseScanner,
    private val chequeScanner: ChequeScanner,
    private val passportScanner: PassportScanner,
    private val greenCardScanner: GreenCardScanner,
    private val aadharScanner: AadharScanner,
    private val panCardScanner: PanCardScanner
) {

    suspend fun process(
        bitmap: Bitmap,
        path: String,
        category: Int,
        isBack: Boolean,
        financialState: FinancialFormState,
        identityState: IdentityFormState,
        passportState: PassportFormState,
        greenCardState: GreenCardFormState,
        aadharCardState: AadharCardFormState,
        giftCardState: GiftCardFormState,
        panCardState: PanCardFormState
    ) {
        if (isBack) {
            processBackSide(
                bitmap, path, category,
                financialState, identityState, passportState, greenCardState, 
                aadharCardState, giftCardState, panCardState
            )
        } else {
            processFrontSide(
                bitmap, path, category,
                financialState, identityState, passportState, greenCardState, 
                aadharCardState, giftCardState, panCardState
            )
        }
    }

    private suspend fun processBackSide(
        bitmap: Bitmap,
        path: String,
        category: Int,
        financialState: FinancialFormState,
        identityState: IdentityFormState,
        passportState: PassportFormState,
        greenCardState: GreenCardFormState,
        aadharCardState: AadharCardFormState,
        giftCardState: GiftCardFormState,
        panCardState: PanCardFormState
    ) {
        when (category) {
            0, 3 -> {
                financialState.backPath = path
                financialState.hasBackImage = true
                if (financialState.type == AccountType.REWARDS_CARD || category == 3) {
                    val res = rewardsScanner.scan(bitmap)
                    
                    // Prioritize actual barcode for the barcode field, use OCR number as fallback if barcode is null or better
                    val newBarcode = res.barcode ?: res.cardNumber
                    if (newBarcode != null && (financialState.barcode.isEmpty() || newBarcode.length > financialState.barcode.length)) {
                        financialState.barcode = newBarcode
                    }
                    res.barcodeFormat?.let { financialState.barcodeFormat = it }
                    res.shopName?.let { financialState.institution = it }
                } else {
                    val details = paymentScanner.scan(bitmap)
                    if (financialState.number.isEmpty()) financialState.number = details.number
                    if (financialState.expiry.isEmpty()) financialState.expiry = details.expiryDate
                }
            }
            1 -> {
                identityState.backPath = path
                identityState.hasBackImage = true
                if (identityState.type == DocumentType.DRIVER_LICENSE) {
                    val details = driverLicenseScanner.scan(bitmap)
                    if (details.docNumber.isNotEmpty()) identityState.number = details.docNumber
                    if (details.name.isNotEmpty()) {
                        identityState.firstName = details.name.substringBefore(" ")
                        identityState.lastName = details.name.substringAfterLast(" ", "")
                    }
                    if (details.dob.isNotEmpty()) identityState.rawDob = details.dob.filter { it.isDigit() }
                    if (details.expiryDate.isNotEmpty()) identityState.rawExpiry = details.expiryDate.filter { it.isDigit() }
                    if (details.address.isNotEmpty()) identityState.address = details.address
                    
                    // Map additional fields
                    if (details.sex.isNotEmpty()) identityState.sex = details.sex
                    if (details.eyeColor.isNotEmpty()) identityState.eyeColor = details.eyeColor
                    if (details.height.isNotEmpty()) identityState.height = details.height
                    if (details.licenseClass.isNotEmpty()) identityState.licenseClass = details.licenseClass
                    if (details.restrictions.isNotEmpty()) identityState.restrictions = details.restrictions
                    if (details.endorsements.isNotEmpty()) identityState.endorsements = details.endorsements
                    if (details.state.isNotEmpty()) identityState.region = details.state
                    if (details.issuingAuthority.isNotEmpty()) identityState.issuingAuthority = details.issuingAuthority
                } else {
                    val details = identityScanner.scan(bitmap, true)
                    if (identityState.rawDob.isEmpty()) identityState.rawDob = details.dob.filter { it.isDigit() }
                    if (identityState.sex.isEmpty()) identityState.sex = details.sex
                }
            }
            2 -> {
                passportState.backPath = path
                passportState.hasBackImage = true
                val details = passportScanner.scanBack(bitmap)
                details.fatherName?.let { passportState.fatherName = it }
                details.motherName?.let { passportState.motherName = it }
                details.spouseName?.let { passportState.spouseName = it }
                details.address?.let { passportState.address = it }
                details.fileNumber?.let { passportState.fileNumber = it }
            }
            4 -> {
                greenCardState.backPath = path
                greenCardState.hasBackImage = true
                val details = greenCardScanner.scan(bitmap)
                if (details.isMrzData) {
                    if (details.firstName.isNotEmpty()) greenCardState.givenName = details.firstName
                    if (details.lastName.isNotEmpty()) greenCardState.surname = details.lastName
                    if (details.uscisNumber.isNotEmpty()) greenCardState.uscisNumber = details.uscisNumber
                    if (details.dob.isNotEmpty()) greenCardState.rawDob = details.dob.filter { it.isDigit() }
                    if (details.expiryDate.isNotEmpty()) greenCardState.rawExpiryDate = details.expiryDate.filter { it.isDigit() }
                    if (details.sex.isNotEmpty()) greenCardState.sex = details.sex
                    if (details.countryOfBirth.isNotEmpty()) greenCardState.countryOfBirth = details.countryOfBirth
                    if (details.category.isNotEmpty()) greenCardState.category = details.category
                }
            }
            5 -> {
                aadharCardState.backPath = path
                aadharCardState.hasBackImage = true
                val details = aadharScanner.scan(bitmap)
                if (details.docNumber.isNotEmpty() && aadharCardState.uid.isEmpty()) {
                    aadharCardState.uid = details.docNumber
                }
            }
            6 -> {
                giftCardState.backPath = path
                giftCardState.hasBackImage = true
                val res = rewardsScanner.scan(bitmap)
                
                res.barcodeFormat?.let { giftCardState.barcodeFormat = it }
            }
            7 -> {
                panCardState.backPath = path
                panCardState.hasBackImage = true
            }
        }
    }

    private suspend fun processFrontSide(
        bitmap: Bitmap,
        path: String,
        category: Int,
        financialState: FinancialFormState,
        identityState: IdentityFormState,
        passportState: PassportFormState,
        greenCardState: GreenCardFormState,
        aadharCardState: AadharCardFormState,
        giftCardState: GiftCardFormState,
        panCardState: PanCardFormState
    ) {
        when (category) {
            0, 3 -> {
                financialState.frontPath = path
                financialState.hasFrontImage = true
                if (financialState.type == AccountType.REWARDS_CARD || category == 3) {
                    val res = rewardsScanner.scan(bitmap)
                    
                    val newBarcode = res.barcode ?: res.cardNumber
                    if (newBarcode != null && (financialState.barcode.isEmpty() || newBarcode.length > financialState.barcode.length)) {
                        financialState.barcode = newBarcode
                    }
                    res.barcodeFormat?.let { financialState.barcodeFormat = it }
                    res.shopName?.let { financialState.institution = it }
                    if (financialState.logoPath == null) financialState.logoPath = path
                } else if (financialState.type == AccountType.BANK_ACCOUNT) {
                    val details = chequeScanner.scan(bitmap)
                    if (details.accountNumber.isNotEmpty()) financialState.number = details.accountNumber
                    if (details.routingNumber.isNotEmpty()) financialState.routing = details.routingNumber
                    if (details.bankName.isNotEmpty()) financialState.institution = details.bankName
                    if (details.ifscCode.isNotEmpty()) financialState.ifsc = details.ifscCode
                    if (details.holderName.isNotEmpty()) financialState.holder = details.holderName
                } else {
                    val details = paymentScanner.scan(bitmap)
                    if (financialState.number.isEmpty()) financialState.number = details.number
                    if (financialState.expiry.isEmpty()) financialState.expiry = details.expiryDate
                    if (financialState.holder.isEmpty()) financialState.holder = details.ownerName
                    if (financialState.institution.isEmpty()) financialState.institution = details.bankName
                }
            }
            1 -> {
                identityState.frontPath = path
                identityState.hasFrontImage = true
                val details = identityScanner.scan(bitmap, false)
                if (identityState.number.isEmpty()) identityState.number = details.docNumber
                if (identityState.firstName.isEmpty()) identityState.firstName = details.name.substringBefore(" ")
                if (identityState.lastName.isEmpty()) identityState.lastName = details.name.substringAfter(" ", "")
                if (identityState.rawDob.isEmpty()) identityState.rawDob = details.dob.filter { it.isDigit() }
            }
            2 -> {
                passportState.frontPath = path
                passportState.hasFrontImage = true
                val details = passportScanner.scanFront(bitmap)
                details.passportNumber?.let { passportState.passportNumber = it }
                details.surname?.let { passportState.surname = it }
                details.givenNames?.let { passportState.givenNames = it }
                details.dob?.let { passportState.rawDob = it.filter { c -> c.isDigit() } }
                details.sex?.let { passportState.sex = it }
                details.dateOfExpiry?.let { passportState.rawDateOfExpiry = it.filter { c -> c.isDigit() } }
                details.dateOfIssue?.let { passportState.rawDateOfIssue = it.filter { c -> c.isDigit() } }
                details.nationality?.let { passportState.nationality = it }
                details.placeOfBirth?.let { passportState.placeOfBirth = it }
                details.placeOfIssue?.let { passportState.placeOfIssue = it }
                details.authority?.let { passportState.authority = it }
                if (details.countryCode.isNotBlank()) passportState.countryCode = details.countryCode
            }
            4 -> {
                greenCardState.frontPath = path
                greenCardState.hasFrontImage = true
                val details = greenCardScanner.scan(bitmap)
                if (details.firstName.isNotEmpty()) greenCardState.givenName = details.firstName
                if (details.lastName.isNotEmpty()) greenCardState.surname = details.lastName
                if (details.uscisNumber.isNotEmpty()) greenCardState.uscisNumber = details.uscisNumber
                if (details.sex.isNotEmpty()) greenCardState.sex = details.sex
                if (details.countryOfBirth.isNotEmpty()) greenCardState.countryOfBirth = details.countryOfBirth
                if (details.category.isNotEmpty()) greenCardState.category = details.category
            }
            5 -> {
                aadharCardState.frontPath = path
                aadharCardState.hasFrontImage = true
                val details = aadharScanner.scan(bitmap)
                if (details.docNumber.isNotEmpty() && aadharCardState.uid.isEmpty()) {
                    aadharCardState.uid = details.docNumber
                }
                if (details.name.isNotEmpty() && aadharCardState.holderName.isEmpty()) {
                    aadharCardState.holderName = details.name
                }
                if (details.dob.isNotEmpty() && aadharCardState.rawDob.isEmpty()) {
                    aadharCardState.rawDob = details.dob.filter { it.isDigit() }
                }
                if (details.sex.isNotEmpty() && aadharCardState.gender.isEmpty()) {
                    aadharCardState.gender = details.sex
                }
            }
            6 -> {
                giftCardState.frontPath = path
                giftCardState.hasFrontImage = true
                val res = rewardsScanner.scan(bitmap)
                
                res.barcode?.let { if (giftCardState.barcode.orEmpty().isEmpty() || it.length > giftCardState.barcode.orEmpty().length) giftCardState.barcode = it }
                res.cardNumber?.let { if (giftCardState.cardNumber.isEmpty() || it.length > giftCardState.cardNumber.length) giftCardState.cardNumber = it }
                res.barcodeFormat?.let { giftCardState.barcodeFormat = it }
                res.shopName?.let { if (giftCardState.providerName.isEmpty()) giftCardState.providerName = it }
            }
            7 -> {
                panCardState.frontPath = path
                panCardState.hasFrontImage = true
                val result = panCardScanner.scan(bitmap)
                if (result.panNumber.isNotEmpty()) panCardState.panNumber = result.panNumber
                if (result.holderName.isNotEmpty()) panCardState.holderName = result.holderName
                if (result.fatherName.isNotEmpty()) panCardState.fatherName = result.fatherName
                if (result.dob.isNotEmpty()) panCardState.rawDob = result.dob.filter { it.isDigit() }
            }
        }
    }
}
