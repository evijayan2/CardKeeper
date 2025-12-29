package com.vijay.cardkeeper.ui.item

import android.app.Activity
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.vijay.cardkeeper.data.entity.AccountType
import com.vijay.cardkeeper.data.entity.DocumentType
import com.vijay.cardkeeper.data.entity.FinancialAccount
import com.vijay.cardkeeper.data.entity.IdentityDocument
import com.vijay.cardkeeper.data.entity.Passport
import com.vijay.cardkeeper.scanning.AadharQrScanner
import com.vijay.cardkeeper.scanning.ChequeScanner
import com.vijay.cardkeeper.scanning.DriverLicenseScanner
import com.vijay.cardkeeper.scanning.GreenCardScanner
import com.vijay.cardkeeper.scanning.IdentityScanner
import com.vijay.cardkeeper.scanning.PassportScanner
import com.vijay.cardkeeper.scanning.PaymentCardScanner
import com.vijay.cardkeeper.scanning.RewardsScanner
import com.vijay.cardkeeper.ui.item.forms.AadharCardForm
import com.vijay.cardkeeper.ui.item.forms.FinancialForm
import com.vijay.cardkeeper.ui.item.forms.GreenCardForm
import com.vijay.cardkeeper.ui.item.forms.IdentityForm
import com.vijay.cardkeeper.ui.item.forms.PassportForm
import com.vijay.cardkeeper.ui.item.forms.rememberAadharCardFormState
import com.vijay.cardkeeper.ui.item.forms.rememberFinancialFormState
import com.vijay.cardkeeper.ui.item.forms.rememberGreenCardFormState
import com.vijay.cardkeeper.ui.item.forms.rememberIdentityFormState
import com.vijay.cardkeeper.ui.item.forms.rememberPassportFormState
import com.vijay.cardkeeper.ui.scanner.BarcodeScannerScreen
import com.vijay.cardkeeper.ui.viewmodel.AddItemViewModel
import com.vijay.cardkeeper.ui.viewmodel.AppViewModelProvider
import com.vijay.cardkeeper.util.saveImageToInternalStorage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(
        documentId: Int?,
        documentType: String?, // "financial" or "identity"
        initialCategory: Int = 0,
        navigateBack: () -> Unit,
        viewModel: AddItemViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        // Determine type for lookup: explicit type OR fallback to category
        val typeForLookup =
                documentType.takeIf { !it.isNullOrEmpty() }
                        ?: when (initialCategory) {
                                0 -> "financial"
                                1 -> "identity"
                                2 -> "passport"
                                4 -> "greencard"
                                5 -> "aadhar"
                                else -> "financial"
                        }
        // Load existing item if editing
        val item by viewModel.getItem(documentId, typeForLookup).collectAsState(initial = null)

        // Category / Tabs
        // If we are editing, we lock the category to the item type.
        // If adding new, we start at 0 (Financial) but user can switch.
        // Index mapping: 0 -> Financial, 1 -> Identity, 2 -> Passport, 3 -> Rewards, 4 -> Green
        // Card
        var selectedCategory by remember { mutableIntStateOf(initialCategory) }

        // Sync selectedCategory with loaded item
        val currentItem = item
        LaunchedEffect(currentItem) {
                if (currentItem is IdentityDocument) selectedCategory = 1
                else if (currentItem is FinancialAccount) {
                        if (currentItem.type == AccountType.REWARDS_CARD ||
                                        currentItem.type == AccountType.LIBRARY_CARD
                        )
                                selectedCategory = 3
                        else selectedCategory = 0
                } else if (currentItem is Passport) selectedCategory = 2
                else if (currentItem is com.vijay.cardkeeper.data.entity.GreenCard)
                        selectedCategory = 4
                else if (currentItem is com.vijay.cardkeeper.data.entity.AadharCard)
                        selectedCategory = 5
        }

        // Parse initialType from documentType string for financial accounts
        val initialAccountType: AccountType? =
                remember(documentType) {
                        when (documentType?.uppercase()) {
                                "CREDIT_CARD" -> AccountType.CREDIT_CARD
                                "DEBIT_CARD" -> AccountType.DEBIT_CARD
                                "BANK_ACCOUNT" -> AccountType.BANK_ACCOUNT
                                "REWARDS_CARD" -> AccountType.REWARDS_CARD
                                else -> null
                        }
                }

        // Parse initialType from documentType string for identity documents
        val initialDocumentType: DocumentType? =
                remember(documentType) {
                        when (documentType?.uppercase()) {
                                "DRIVER_LICENSE" -> DocumentType.DRIVER_LICENSE
                                // "PASSPORT" -> DocumentType.PASSPORT // Passport is now separate
                                "GREEN_CARD" -> DocumentType.GREEN_CARD
                                else -> null
                        }
                }

        // Form States
        val financialState =
                rememberFinancialFormState(item as? FinancialAccount, initialAccountType)
        val identityState =
                rememberIdentityFormState(item as? IdentityDocument, initialDocumentType)
        val passportState = rememberPassportFormState(item as? Passport)
        val greenCardState =
                rememberGreenCardFormState(item as? com.vijay.cardkeeper.data.entity.GreenCard)
        val aadharCardState =
                rememberAadharCardFormState(item as? com.vijay.cardkeeper.data.entity.AadharCard)

        // Scanners
        val paymentScanner = remember { PaymentCardScanner() }
        val rewardsScanner = remember { RewardsScanner() }
        val identityScanner = remember { IdentityScanner() }
        val driverLicenseScanner = remember { DriverLicenseScanner() }
        val chequeScanner = remember { ChequeScanner() }
        val passportScanner = remember { PassportScanner() }
        val greenCardScanner = remember { GreenCardScanner() }
        val aadharQrScanner = remember { AadharQrScanner(context) }

        // State for Aadhar QR scanning
        var showAadharQrScanner by remember { mutableStateOf(false) }

        // Constants for Camera Logic
        var scanningBack by remember { mutableStateOf(false) }
        var showBarcodeScanner by remember { mutableStateOf(false) }

        // GmsDocumentScanner Options
        val scannerOptions = remember {
                GmsDocumentScannerOptions.Builder()
                        .setGalleryImportAllowed(true)
                        .setPageLimit(1)
                        .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
                        .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
                        .build()
        }
        val scanner: GmsDocumentScanner = remember { GmsDocumentScanning.getClient(scannerOptions) }
        val activity = context as Activity

        // Document Scanner Launcher
        val scannerLauncher =
                rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.StartIntentSenderForResult()
                ) { result ->
                        if (result.resultCode == Activity.RESULT_OK) {
                                val scanningResult =
                                        GmsDocumentScanningResult.fromActivityResultIntent(
                                                result.data
                                        )
                                scanningResult?.pages?.firstOrNull()?.imageUri?.let { uri ->
                                        val inputStream =
                                                context.contentResolver.openInputStream(uri)
                                        val bitmap = BitmapFactory.decodeStream(inputStream)
                                        inputStream?.close()

                                        if (bitmap != null) {
                                                val savedPath =
                                                        saveImageToInternalStorage(
                                                                context,
                                                                bitmap,
                                                                "scan_${System.currentTimeMillis()}"
                                                        )

                                                scope.launch {
                                                        when (selectedCategory) {
                                                                0, 3 -> {
                                                                        // Financial (0) or Rewards
                                                                        // (3)
                                                                        if (scanningBack) {
                                                                                financialState
                                                                                        .backBitmap =
                                                                                        bitmap
                                                                                financialState
                                                                                        .backPath =
                                                                                        savedPath
                                                                                if (financialState
                                                                                                .type ==
                                                                                                AccountType
                                                                                                        .REWARDS_CARD ||
                                                                                                selectedCategory ==
                                                                                                        3
                                                                                ) {
                                                                                        val res =
                                                                                                rewardsScanner
                                                                                                        .scan(
                                                                                                                bitmap
                                                                                                        )
                                                                                        res.barcode
                                                                                                ?.let {
                                                                                                        financialState
                                                                                                                .barcode =
                                                                                                                it
                                                                                                }
                                                                                        res.barcodeFormat
                                                                                                ?.let {
                                                                                                        financialState
                                                                                                                .barcodeFormat =
                                                                                                                it
                                                                                                }
                                                                                        res.shopName
                                                                                                ?.let {
                                                                                                        financialState
                                                                                                                .institution =
                                                                                                                it
                                                                                                }
                                                                                } else {
                                                                                        val details =
                                                                                                paymentScanner
                                                                                                        .scan(
                                                                                                                bitmap
                                                                                                        )
                                                                                        if (financialState
                                                                                                        .number
                                                                                                        .isEmpty() &&
                                                                                                        details.number
                                                                                                                .isNotEmpty()
                                                                                        )
                                                                                                financialState
                                                                                                        .number =
                                                                                                        details.number
                                                                                        if (financialState
                                                                                                        .expiry
                                                                                                        .isEmpty() &&
                                                                                                        details.expiryDate
                                                                                                                .isNotEmpty()
                                                                                        )
                                                                                                financialState
                                                                                                        .expiry =
                                                                                                        details.expiryDate
                                                                                }
                                                                        } else {
                                                                                financialState
                                                                                        .frontBitmap =
                                                                                        bitmap
                                                                                financialState
                                                                                        .frontPath =
                                                                                        savedPath
                                                                                if (financialState
                                                                                                .type ==
                                                                                                AccountType
                                                                                                        .REWARDS_CARD
                                                                                ) {
                                                                                        val res =
                                                                                                rewardsScanner
                                                                                                        .scan(
                                                                                                                bitmap
                                                                                                        )
                                                                                        res.barcode
                                                                                                ?.let {
                                                                                                        financialState
                                                                                                                .barcode =
                                                                                                                it
                                                                                                }
                                                                                        res.barcodeFormat
                                                                                                ?.let {
                                                                                                        financialState
                                                                                                                .barcodeFormat =
                                                                                                                it
                                                                                                }
                                                                                        res.shopName
                                                                                                ?.let {
                                                                                                        financialState
                                                                                                                .institution =
                                                                                                                it
                                                                                                }
                                                                                        if (financialState
                                                                                                        .logoPath ==
                                                                                                        null
                                                                                        ) {
                                                                                                financialState
                                                                                                        .logoPath =
                                                                                                        savedPath
                                                                                                financialState
                                                                                                        .logoBitmap =
                                                                                                        bitmap
                                                                                        }
                                                                                } else if (financialState
                                                                                                .type ==
                                                                                                AccountType
                                                                                                        .BANK_ACCOUNT
                                                                                ) {
                                                                                        val chequeDetails =
                                                                                                chequeScanner
                                                                                                        .scan(
                                                                                                                bitmap
                                                                                                        )
                                                                                        if (chequeDetails
                                                                                                        .accountNumber
                                                                                                        .isNotEmpty()
                                                                                        )
                                                                                                financialState
                                                                                                        .number =
                                                                                                        chequeDetails
                                                                                                                .accountNumber
                                                                                        if (chequeDetails
                                                                                                        .routingNumber
                                                                                                        .isNotEmpty()
                                                                                        )
                                                                                                financialState
                                                                                                        .routing =
                                                                                                        chequeDetails
                                                                                                                .routingNumber
                                                                                        if (chequeDetails
                                                                                                        .bankName
                                                                                                        .isNotEmpty()
                                                                                        )
                                                                                                financialState
                                                                                                        .institution =
                                                                                                        chequeDetails
                                                                                                                .bankName
                                                                                        if (chequeDetails
                                                                                                        .ifscCode
                                                                                                        .isNotEmpty()
                                                                                        )
                                                                                                financialState
                                                                                                        .ifsc =
                                                                                                        chequeDetails
                                                                                                                .ifscCode
                                                                                        if (chequeDetails
                                                                                                        .holderName
                                                                                                        .isNotEmpty()
                                                                                        )
                                                                                                financialState
                                                                                                        .holder =
                                                                                                        chequeDetails
                                                                                                                .holderName
                                                                                        if (chequeDetails
                                                                                                        .holderAddress
                                                                                                        .isNotEmpty()
                                                                                        )
                                                                                                financialState
                                                                                                        .holderAddress =
                                                                                                        chequeDetails
                                                                                                                .holderAddress
                                                                                        Toast.makeText(
                                                                                                        context,
                                                                                                        chequeDetails
                                                                                                                .extractionSummary,
                                                                                                        Toast.LENGTH_LONG
                                                                                                )
                                                                                                .show()
                                                                                } else {
                                                                                        val details =
                                                                                                paymentScanner
                                                                                                        .scan(
                                                                                                                bitmap
                                                                                                        )
                                                                                        if (financialState
                                                                                                        .number
                                                                                                        .isEmpty()
                                                                                        )
                                                                                                financialState
                                                                                                        .number =
                                                                                                        details.number
                                                                                        if (financialState
                                                                                                        .expiry
                                                                                                        .isEmpty()
                                                                                        )
                                                                                                financialState
                                                                                                        .expiry =
                                                                                                        details.expiryDate
                                                                                        if (financialState
                                                                                                        .holder
                                                                                                        .isEmpty()
                                                                                        )
                                                                                                financialState
                                                                                                        .holder =
                                                                                                        details.ownerName
                                                                                        if (financialState
                                                                                                        .institution
                                                                                                        .isEmpty()
                                                                                        )
                                                                                                financialState
                                                                                                        .institution =
                                                                                                        details.bankName
                                                                                        if (financialState
                                                                                                        .network
                                                                                                        .isEmpty() &&
                                                                                                        details.scheme
                                                                                                                .isNotEmpty() &&
                                                                                                        details.scheme !=
                                                                                                                "Unknown"
                                                                                        ) {
                                                                                                financialState
                                                                                                        .network =
                                                                                                        details.scheme
                                                                                        }
                                                                                        if (details.cardType ==
                                                                                                        "Debit"
                                                                                        ) {
                                                                                                if (financialState
                                                                                                                .type ==
                                                                                                                AccountType
                                                                                                                        .CREDIT_CARD
                                                                                                )
                                                                                                        financialState
                                                                                                                .type =
                                                                                                                AccountType
                                                                                                                        .DEBIT_CARD
                                                                                        }
                                                                                }
                                                                        }
                                                                }
                                                                2 -> {
                                                                        // Passport
                                                                        if (scanningBack) {
                                                                                passportState
                                                                                        .backBitmap =
                                                                                        bitmap
                                                                                passportState
                                                                                        .backPath =
                                                                                        savedPath
                                                                                val details =
                                                                                        passportScanner
                                                                                                .scanBack(
                                                                                                        bitmap
                                                                                                )
                                                                                if (details.fatherName
                                                                                                ?.isNotEmpty() ==
                                                                                                true
                                                                                )
                                                                                        passportState
                                                                                                .fatherName =
                                                                                                details.fatherName
                                                                                if (details.motherName
                                                                                                ?.isNotEmpty() ==
                                                                                                true
                                                                                )
                                                                                        passportState
                                                                                                .motherName =
                                                                                                details.motherName
                                                                                if (details.spouseName
                                                                                                ?.isNotEmpty() ==
                                                                                                true
                                                                                )
                                                                                        passportState
                                                                                                .spouseName =
                                                                                                details.spouseName
                                                                                if (details.address
                                                                                                ?.isNotEmpty() ==
                                                                                                true
                                                                                )
                                                                                        passportState
                                                                                                .address =
                                                                                                details.address
                                                                                if (details.fileNumber
                                                                                                ?.isNotEmpty() ==
                                                                                                true
                                                                                )
                                                                                        passportState
                                                                                                .fileNumber =
                                                                                                details.fileNumber
                                                                        } else {
                                                                                passportState
                                                                                        .frontBitmap =
                                                                                        bitmap
                                                                                passportState
                                                                                        .frontPath =
                                                                                        savedPath
                                                                                val details =
                                                                                        passportScanner
                                                                                                .scanFront(
                                                                                                        bitmap
                                                                                                )
                                                                                fun setIfNotNull(
                                                                                        value:
                                                                                                String?,
                                                                                        setter:
                                                                                                (
                                                                                                        String) -> Unit
                                                                                ) {
                                                                                        if (!value.isNullOrEmpty()
                                                                                        ) {
                                                                                                setter(
                                                                                                        value
                                                                                                )
                                                                                        }
                                                                                }
                                                                                setIfNotNull(
                                                                                        details.passportNumber
                                                                                ) {
                                                                                        passportState
                                                                                                .passportNumber =
                                                                                                it
                                                                                }
                                                                                setIfNotNull(
                                                                                        details.countryCode
                                                                                ) {
                                                                                        passportState
                                                                                                .countryCode =
                                                                                                it
                                                                                }
                                                                                setIfNotNull(
                                                                                        details.surname
                                                                                ) {
                                                                                        passportState
                                                                                                .surname =
                                                                                                it
                                                                                }
                                                                                setIfNotNull(
                                                                                        details.givenNames
                                                                                ) {
                                                                                        passportState
                                                                                                .givenNames =
                                                                                                it
                                                                                }
                                                                                setIfNotNull(
                                                                                        details.nationality
                                                                                ) {
                                                                                        passportState
                                                                                                .nationality =
                                                                                                it
                                                                                }
                                                                                setIfNotNull(
                                                                                        details.dob
                                                                                ) {
                                                                                        passportState
                                                                                                .rawDob =
                                                                                                it.filter { c -> c.isDigit() }
                                                                                }
                                                                                setIfNotNull(
                                                                                        details.sex
                                                                                ) {
                                                                                        passportState
                                                                                                .sex =
                                                                                                it
                                                                                }
                                                                                setIfNotNull(
                                                                                        details.dateOfExpiry
                                                                                ) {
                                                                                        passportState
                                                                                                .rawDateOfExpiry =
                                                                                                it.filter { c -> c.isDigit() }
                                                                                }
                                                                                setIfNotNull(
                                                                                        details.dateOfIssue
                                                                                ) {
                                                                                        passportState
                                                                                                .rawDateOfIssue =
                                                                                                it.filter { c -> c.isDigit() }
                                                                                }
                                                                                setIfNotNull(
                                                                                        details.placeOfBirth
                                                                                ) {
                                                                                        passportState
                                                                                                .placeOfBirth =
                                                                                                it
                                                                                }
                                                                                setIfNotNull(
                                                                                        details.placeOfIssue
                                                                                ) {
                                                                                        passportState
                                                                                                .placeOfIssue =
                                                                                                it
                                                                                }
                                                                                setIfNotNull(
                                                                                        details.authority
                                                                                ) {
                                                                                        passportState
                                                                                                .authority =
                                                                                                it
                                                                                }
                                                                        }
                                                                }
                                                                1 -> {
                                                                        // Identity
                                                                        if (scanningBack) {
                                                                                identityState
                                                                                        .backBitmap =
                                                                                        bitmap
                                                                                identityState
                                                                                        .backPath =
                                                                                        savedPath
                                                                                if (identityState
                                                                                                .type ==
                                                                                                DocumentType
                                                                                                        .DRIVER_LICENSE
                                                                                ) {
                                                                                        val details =
                                                                                                driverLicenseScanner
                                                                                                        .scan(
                                                                                                                bitmap
                                                                                                        )
                                                                                        if (details.docNumber
                                                                                                        .isNotEmpty()
                                                                                        )
                                                                                                identityState
                                                                                                        .number =
                                                                                                        details.docNumber
                                                                                        if (details.name
                                                                                                        .isNotEmpty()
                                                                                        ) {
                                                                                                identityState
                                                                                                        .firstName =
                                                                                                        details.name
                                                                                                                .substringBefore(
                                                                                                                        " "
                                                                                                                )
                                                                                                identityState
                                                                                                        .lastName =
                                                                                                        details.name
                                                                                                                .substringAfterLast(
                                                                                                                        " ",
                                                                                                                        ""
                                                                                                                )
                                                                                        }
                                                                                        if (details.dob
                                                                                                        .isNotEmpty()
                                                                                        )
                                                                                                identityState
                                                                                                        .rawDob =
                                                                                                        details.dob.filter { it.isDigit() }
                                                                                        if (details.expiryDate
                                                                                                        .isNotEmpty()
                                                                                        )
                                                                                                identityState
                                                                                                        .rawExpiry =
                                                                                                        details.expiryDate.filter { it.isDigit() }
                                                                                        if (details.address
                                                                                                        .isNotEmpty()
                                                                                        )
                                                                                                identityState
                                                                                                        .address =
                                                                                                        details.address
                                                                                        if (details.sex
                                                                                                        .isNotEmpty()
                                                                                        )
                                                                                                identityState
                                                                                                        .sex =
                                                                                                        details.sex
                                                                                        if (details.eyeColor
                                                                                                        .isNotEmpty()
                                                                                        )
                                                                                                identityState
                                                                                                        .eyeColor =
                                                                                                        details.eyeColor
                                                                                        if (details.height
                                                                                                        .isNotEmpty()
                                                                                        )
                                                                                                identityState
                                                                                                        .height =
                                                                                                        details.height
                                                                                        if (details.licenseClass
                                                                                                        .isNotEmpty()
                                                                                        )
                                                                                                identityState
                                                                                                        .licenseClass =
                                                                                                        details.licenseClass
                                                                                        if (details.restrictions
                                                                                                        .isNotEmpty()
                                                                                        )
                                                                                                identityState
                                                                                                        .restrictions =
                                                                                                        details.restrictions
                                                                                        if (details.endorsements
                                                                                                        .isNotEmpty()
                                                                                        )
                                                                                                identityState
                                                                                                        .endorsements =
                                                                                                        details.endorsements
                                                                                        if (details.state
                                                                                                        .isNotEmpty()
                                                                                        )
                                                                                                identityState
                                                                                                        .region =
                                                                                                        details.state
                                                                                        if (details.issuingAuthority
                                                                                                        .isNotEmpty()
                                                                                        )
                                                                                                identityState
                                                                                                        .issuingAuthority =
                                                                                                        details.issuingAuthority
                                                                                        if (details.country
                                                                                                        .isNotEmpty()
                                                                                        )
                                                                                                identityState
                                                                                                        .country =
                                                                                                        details.country
                                                                                } else {
                                                                                        val details =
                                                                                                identityScanner
                                                                                                        .scan(
                                                                                                                bitmap,
                                                                                                                scanningBack
                                                                                                        )
                                                                                        if (identityState
                                                                                                        .dob
                                                                                                        .isEmpty()
                                                                                        )
                                                                                                identityState
                                                                                                        .rawDob =
                                                                                                        details.dob.filter { it.isDigit() }
                                                                                        if (identityState
                                                                                                        .sex
                                                                                                        .isEmpty()
                                                                                        )
                                                                                                identityState
                                                                                                        .sex =
                                                                                                        details.sex
                                                                                }
                                                                        } else {
                                                                                identityState
                                                                                        .frontBitmap =
                                                                                        bitmap
                                                                                identityState
                                                                                        .frontPath =
                                                                                        savedPath
                                                                                val details =
                                                                                        identityScanner
                                                                                                .scan(
                                                                                                        bitmap,
                                                                                                        scanningBack
                                                                                                )
                                                                                if (identityState
                                                                                                .number
                                                                                                .isEmpty()
                                                                                )
                                                                                        identityState
                                                                                                .number =
                                                                                                details.docNumber
                                                                                if (identityState
                                                                                                .firstName
                                                                                                .isEmpty()
                                                                                )
                                                                                        identityState
                                                                                                .firstName =
                                                                                                details.name
                                                                                                        .substringBefore(
                                                                                                                " "
                                                                                                        )
                                                                                if (identityState
                                                                                                .lastName
                                                                                                .isEmpty()
                                                                                )
                                                                                        identityState
                                                                                                .lastName =
                                                                                                details.name
                                                                                                        .substringAfter(
                                                                                                                " ",
                                                                                                                ""
                                                                                                        )
                                                                                if (identityState
                                                                                                .dob
                                                                                                .isEmpty()
                                                                                )
                                                                                        identityState
                                                                                                 .rawDob =
                                                                                                 details.dob.filter { it.isDigit() }
                                                                                if (identityState
                                                                                                .address
                                                                                                .isEmpty()
                                                                                )
                                                                                        identityState
                                                                                                .address =
                                                                                                details.address
                                                                        }
                                                                }
                                                                4 -> {
                                                                        // Save the image
                                                                        if (scanningBack) {
                                                                                greenCardState
                                                                                        .backBitmap =
                                                                                        bitmap
                                                                                greenCardState
                                                                                        .backPath =
                                                                                        savedPath
                                                                        } else {
                                                                                greenCardState
                                                                                        .frontBitmap =
                                                                                        bitmap
                                                                                greenCardState
                                                                                        .frontPath =
                                                                                        savedPath
                                                                        }

                                                                        // Parse BOTH front and back
                                                                        // scans
                                                                        val details =
                                                                                greenCardScanner
                                                                                        .scan(
                                                                                                bitmap
                                                                                        )

                                                                        // For back scan (MRZ):
                                                                        // always overwrite
                                                                        // For front scan: only fill
                                                                        // empty fields
                                                                        val shouldOverwrite =
                                                                                scanningBack &&
                                                                                        details.isMrzData

                                                                        if (details.firstName
                                                                                        .isNotEmpty() &&
                                                                                        (shouldOverwrite ||
                                                                                                greenCardState
                                                                                                        .givenName
                                                                                                        .isEmpty())
                                                                        ) {
                                                                                greenCardState
                                                                                        .givenName =
                                                                                        details.firstName
                                                                        }
                                                                        if (details.lastName
                                                                                        .isNotEmpty() &&
                                                                                        (shouldOverwrite ||
                                                                                                greenCardState
                                                                                                        .surname
                                                                                                        .isEmpty())
                                                                        ) {
                                                                                greenCardState
                                                                                        .surname =
                                                                                        details.lastName
                                                                        }
                                                                        if (details.uscisNumber
                                                                                        .isNotEmpty() &&
                                                                                        (shouldOverwrite ||
                                                                                                greenCardState
                                                                                                        .uscisNumber
                                                                                                        .isEmpty())
                                                                        ) {
                                                                                greenCardState
                                                                                        .uscisNumber =
                                                                                        details.uscisNumber
                                                                        }
                                                                        if (details.dob
                                                                                        .isNotEmpty() &&
                                                                                        (shouldOverwrite ||
                                                                                                greenCardState
                                                                                                        .dob
                                                                                                        .isEmpty())
                                                                        ) {
                                                                                greenCardState.rawDob =
                                                                                        details.dob.filter { it.isDigit() }
                                                                        }
                                                                        if (details.expiryDate
                                                                                        .isNotEmpty() &&
                                                                                        (shouldOverwrite ||
                                                                                                greenCardState
                                                                                                        .expiryDate
                                                                                                        .isEmpty())
                                                                        ) {
                                                                                greenCardState
                                                                                        .rawExpiryDate =
                                                                                        details.expiryDate.filter { it.isDigit() }
                                                                        }
                                                                        if (details.sex
                                                                                        .isNotEmpty() &&
                                                                                        (shouldOverwrite ||
                                                                                                greenCardState
                                                                                                        .sex
                                                                                                        .isEmpty())
                                                                        ) {
                                                                                greenCardState.sex =
                                                                                        details.sex
                                                                        }
                                                                        if (details.countryOfBirth
                                                                                        .isNotEmpty() &&
                                                                                        (shouldOverwrite ||
                                                                                                greenCardState
                                                                                                        .countryOfBirth
                                                                                                        .isEmpty())
                                                                        ) {
                                                                                greenCardState
                                                                                        .countryOfBirth =
                                                                                        details.countryOfBirth
                                                                        }
                                                                        if (details.residentSince
                                                                                        .isNotEmpty() &&
                                                                                        (shouldOverwrite ||
                                                                                                greenCardState
                                                                                                        .residentSince
                                                                                                        .isEmpty())
                                                                        ) {
                                                                                greenCardState
                                                                                        .rawResidentSince =
                                                                                        details.residentSince.filter { it.isDigit() }
                                                                        }
                                                                        if (details.category
                                                                                        .isNotEmpty() &&
                                                                                        (shouldOverwrite ||
                                                                                                greenCardState
                                                                                                        .category
                                                                                                        .isEmpty())
                                                                        ) {
                                                                                greenCardState
                                                                                        .category =
                                                                                        details.category
                                                                        }
                                                                }
                                                                5 -> {
                                                                        // Aadhar
                                                                        if (scanningBack) {
                                                                                aadharCardState
                                                                                        .backBitmap =
                                                                                        bitmap
                                                                                aadharCardState
                                                                                        .backPath =
                                                                                        savedPath
                                                                        } else {
                                                                                aadharCardState
                                                                                        .frontBitmap =
                                                                                        bitmap
                                                                                aadharCardState
                                                                                        .frontPath =
                                                                                        savedPath
                                                                        }
                                                                }
                                                        }
                                                }
                                        }
                                }
                        }
                }

        val scanDocument = { isBack: Boolean ->
                scanningBack = isBack

                // Use document scanner for everything
                scanner.getStartScanIntent(activity)
                        .addOnSuccessListener { intentSender ->
                                scannerLauncher.launch(
                                        IntentSenderRequest.Builder(intentSender).build()
                                )
                        }
                        .addOnFailureListener { e ->
                                Toast.makeText(
                                                context,
                                                "Scanner error: ${e.message}",
                                                Toast.LENGTH_SHORT
                                        )
                                        .show()
                        }
        }

        // Logo Picker for Financial Form (Specific)
        val logoLauncher =
                rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()
                ) { uri ->
                        uri?.let {
                                val source =
                                        android.graphics.ImageDecoder.createSource(
                                                context.contentResolver,
                                                it
                                        )
                                val bitmap = android.graphics.ImageDecoder.decodeBitmap(source)
                                val savedPath =
                                        saveImageToInternalStorage(
                                                context,
                                                bitmap,
                                                "logo_${System.currentTimeMillis()}"
                                        )
                                financialState.logoPath = savedPath
                                financialState.logoBitmap = bitmap
                        }
                }

        // Dynamic Title Logic
        val screenTitle =
                if (documentId != null && documentId != 0) {
                        // Editing existing item
                        when (selectedCategory) {
                                0 -> {
                                        when (financialState.type) {
                                                AccountType.CREDIT_CARD, AccountType.DEBIT_CARD ->
                                                        "Update Credit/Debit Card"
                                                AccountType.BANK_ACCOUNT -> "Update Bank Account"
                                                else -> "Update Financial Account"
                                        }
                                }
                                3 -> {
                                        when (financialState.type) {
                                                AccountType.LIBRARY_CARD -> "Update Library Card"
                                                else -> "Update Rewards Card"
                                        }
                                }
                                2 -> "Update Passport"
                                4 -> "Update Green Card"
                                5 -> "Update Aadhar"
                                else -> {
                                        when (identityState.type) {
                                                DocumentType.DRIVER_LICENSE ->
                                                        "Update Driver License"
                                                DocumentType.SSN -> "Update SSN"
                                                DocumentType.PAN -> "Update PAN"
                                                DocumentType.ADHAAR -> "Update Aadhar"
                                                else -> "Update Identity Document"
                                        }
                                }
                        }
                } else {
                        // Adding new item
                        when (selectedCategory) {
                                0 -> {
                                        when (financialState.type) {
                                                AccountType.CREDIT_CARD, AccountType.DEBIT_CARD ->
                                                        "Add Credit/Debit Card"
                                                AccountType.BANK_ACCOUNT -> "Add Bank Account"
                                                else -> "Add Financial Account"
                                        }
                                }
                                3 -> {
                                        when (financialState.type) {
                                                AccountType.LIBRARY_CARD -> "Add Library Card"
                                                else -> "Add Rewards Card"
                                        }
                                }
                                2 -> "Add Passport"
                                4 -> "Add Green Card"
                                5 -> "Add Aadhar"
                                else -> {
                                        when (identityState.type) {
                                                DocumentType.DRIVER_LICENSE -> "Add Driver License"
                                                DocumentType.SSN -> "Add SSN"
                                                DocumentType.PAN -> "Add PAN"
                                                DocumentType.ADHAAR -> "Add Aadhar"
                                                else -> "Add Identity Document"
                                        }
                                }
                        }
                }

        val typeIcon =
                when (selectedCategory) {
                        0 -> {
                                when (financialState.type) {
                                        AccountType.CREDIT_CARD, AccountType.DEBIT_CARD ->
                                                Icons.Filled.CreditCard
                                        AccountType.BANK_ACCOUNT -> Icons.Filled.AccountBalance
                                        AccountType.REWARDS_CARD -> Icons.Filled.CardGiftcard
                                        else -> Icons.Filled.AccountBalance
                                }
                        }
                        2, 4 -> Icons.Filled.AccountBox
                        else -> {
                                when (identityState.type) {
                                        DocumentType.DRIVER_LICENSE -> Icons.Filled.DirectionsCar
                                        else -> Icons.Filled.Face
                                }
                        }
                }

        // Barcode result handler - captures data only (image is handled by Scan Back)
        val handleBarcodeResult: (String, android.graphics.Bitmap?) -> Unit = { rawData, _ ->
                showBarcodeScanner = false

                scope.launch {
                        val details = driverLicenseScanner.parseAAMVAData(rawData)

                        // Populate form fields from barcode data
                        if (details.docNumber.isNotEmpty()) identityState.number = details.docNumber
                        if (details.name.isNotEmpty()) {
                                identityState.firstName = details.name.substringBefore(" ")
                                identityState.lastName = details.name.substringAfterLast(" ", "")
                        }
                        if (details.dob.isNotEmpty()) identityState.rawDob = details.dob.filter { it.isDigit() }
                        if (details.expiryDate.isNotEmpty())
                                identityState.rawExpiry = details.expiryDate.filter { it.isDigit() }
                        if (details.address.isNotEmpty()) identityState.address = details.address
                        if (details.sex.isNotEmpty()) identityState.sex = details.sex
                        if (details.eyeColor.isNotEmpty()) identityState.eyeColor = details.eyeColor
                        if (details.height.isNotEmpty()) identityState.height = details.height
                        if (details.licenseClass.isNotEmpty())
                                identityState.licenseClass = details.licenseClass
                        if (details.restrictions.isNotEmpty())
                                identityState.restrictions = details.restrictions
                        if (details.endorsements.isNotEmpty())
                                identityState.endorsements = details.endorsements
                        if (details.state.isNotEmpty()) identityState.region = details.state
                        if (details.issuingAuthority.isNotEmpty())
                                identityState.issuingAuthority = details.issuingAuthority
                        if (details.country.isNotEmpty()) identityState.country = details.country
                }
        }

        Box(modifier = Modifier.fillMaxSize()) {
                Scaffold(
                        topBar = {
                                TopAppBar(
                                        title = {
                                                Row(
                                                        verticalAlignment =
                                                                androidx.compose.ui.Alignment
                                                                        .CenterVertically
                                                ) {
                                                        Icon(
                                                                imageVector = typeIcon,
                                                                contentDescription = null,
                                                                modifier =
                                                                        Modifier.padding(end = 8.dp)
                                                        )
                                                        Text(screenTitle)
                                                }
                                        },
                                        navigationIcon = {
                                                IconButton(onClick = navigateBack) {
                                                        Icon(
                                                                Icons.AutoMirrored.Filled.ArrowBack,
                                                                "Back"
                                                        )
                                                }
                                        }
                                )
                        }
                ) { innerPadding ->
                        Column(
                                modifier =
                                        Modifier.padding(innerPadding)
                                                .fillMaxSize()
                                                .padding(16.dp)
                                                .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                                // Category Switcher Removed per user request

                                when (selectedCategory) {
                                        0, 3 -> {
                                                // If Rewards category is selected, ensure type is
                                                // REWARDS_CARD or LIBRARY_CARD
                                                if (selectedCategory == 3 &&
                                                                financialState.type !=
                                                                        AccountType.REWARDS_CARD &&
                                                                financialState.type !=
                                                                        AccountType.LIBRARY_CARD
                                                ) {
                                                        financialState.type =
                                                                AccountType.REWARDS_CARD
                                                }

                                                FinancialForm(
                                                        state = financialState,
                                                        onScanFront = { scanDocument(false) },
                                                        onScanBack = { scanDocument(true) },
                                                        onPickLogo = {
                                                                logoLauncher.launch("image/*")
                                                        },
                                                        onSave = {
                                                                viewModel.saveFinancialAccount(
                                                                        id = documentId ?: 0,
                                                                        type = financialState.type,
                                                                        institution =
                                                                                financialState
                                                                                        .institution,
                                                                        name =
                                                                                financialState
                                                                                        .accName,
                                                                        holder =
                                                                                financialState
                                                                                        .holder,
                                                                        number =
                                                                                financialState
                                                                                        .number,
                                                                        routing =
                                                                                financialState
                                                                                        .routing,
                                                                        ifsc = financialState.ifsc,
                                                                        swift =
                                                                                financialState
                                                                                        .swift,
                                                                        expiryDate =
                                                                                financialState
                                                                                        .formattedExpiry,
                                                                        cvv = financialState.cvv,
                                                                        pin = financialState.pin,
                                                                        notes =
                                                                                financialState
                                                                                        .notes,
                                                                        contact =
                                                                                financialState
                                                                                        .contact,
                                                                        cardNetwork =
                                                                                financialState
                                                                                        .network,
                                                                        frontImagePath =
                                                                                financialState
                                                                                        .frontPath,
                                                                        backImagePath =
                                                                                financialState
                                                                                        .backPath,
                                                                        barcode =
                                                                                if (financialState
                                                                                                .type ==
                                                                                                AccountType
                                                                                                        .REWARDS_CARD
                                                                                )
                                                                                        financialState
                                                                                                .barcode
                                                                                else null,
                                                                        barcodeFormat =
                                                                                if (financialState
                                                                                                .type ==
                                                                                                AccountType
                                                                                                        .REWARDS_CARD
                                                                                )
                                                                                        financialState
                                                                                                .barcodeFormat
                                                                                else null,
                                                                        linkedPhoneNumber =
                                                                                financialState
                                                                                        .linkedPhone,
                                                                        logoImagePath =
                                                                                financialState
                                                                                        .logoPath,
                                                                        accountSubType =
                                                                                financialState
                                                                                        .accountSubType,
                                                                        wireNumber =
                                                                                financialState
                                                                                        .wireNumber,
                                                                        branchAddress =
                                                                                financialState
                                                                                        .branchAddress,
                                                                        branchContactNumber =
                                                                                financialState
                                                                                        .branchContact,
                                                                        bankWebUrl =
                                                                                financialState
                                                                                        .bankWebUrl,
                                                                        bankBrandColor =
                                                                                financialState
                                                                                        .bankBrandColor,
                                                                        holderAddress =
                                                                                financialState
                                                                                        .holderAddress
                                                                )
                                                        },
                                                        onNavigateBack = navigateBack
                                                )
                                        }
                                        2 -> {
                                                // Passport Form
                                                PassportForm(
                                                        state = passportState,
                                                        onScanFront = { scanDocument(false) },
                                                        onScanBack = { scanDocument(true) },
                                                        onSave = {
                                                                viewModel.savePassport(
                                                                        Passport(
                                                                                id =
                                                                                        if (item is
                                                                                                        Passport
                                                                                        )
                                                                                                (item as
                                                                                                                Passport)
                                                                                                        .id
                                                                                        else 0,
                                                                                passportNumber =
                                                                                        passportState
                                                                                                .passportNumber,
                                                                                countryCode =
                                                                                        passportState
                                                                                                .countryCode,
                                                                                surname =
                                                                                        passportState
                                                                                                .surname,
                                                                                givenNames =
                                                                                        passportState
                                                                                                .givenNames,
                                                                                nationality =
                                                                                        passportState
                                                                                                .nationality,
                                                                                dob =
                                                                                        passportState
                                                                                                .dob,
                                                                                sex =
                                                                                        passportState
                                                                                                .sex,
                                                                                placeOfBirth =
                                                                                        passportState
                                                                                                .placeOfBirth,
                                                                                dateOfIssue =
                                                                                        passportState
                                                                                                .dateOfIssue,
                                                                                placeOfIssue =
                                                                                        passportState
                                                                                                .placeOfIssue,
                                                                                dateOfExpiry =
                                                                                        passportState
                                                                                                .dateOfExpiry,
                                                                                authority =
                                                                                        passportState
                                                                                                .authority,
                                                                                fatherName =
                                                                                        passportState
                                                                                                .fatherName,
                                                                                motherName =
                                                                                        passportState
                                                                                                .motherName,
                                                                                spouseName =
                                                                                        passportState
                                                                                                .spouseName,
                                                                                address =
                                                                                        passportState
                                                                                                .address,
                                                                                fileNumber =
                                                                                        passportState
                                                                                                .fileNumber,
                                                                                frontImagePath =
                                                                                        passportState
                                                                                                .frontPath,
                                                                                backImagePath =
                                                                                        passportState
                                                                                                .backPath
                                                                        )
                                                                )
                                                        },
                                                        onNavigateBack = navigateBack
                                                )
                                        }
                                        1 -> {
                                                // Identity
                                                IdentityForm(
                                                        state = identityState,
                                                        onScanFront = { scanDocument(false) },
                                                        onScanBack = { scanDocument(true) },
                                                        onScanBarcode = {
                                                                showBarcodeScanner = true
                                                        },
                                                        onSave = {
                                                                viewModel.saveIdentityDocument(
                                                                        id = documentId ?: 0,
                                                                        type = identityState.type,
                                                                        country =
                                                                                identityState
                                                                                        .country,
                                                                        docNumber =
                                                                                identityState
                                                                                        .number,
                                                                        holder =
                                                                                "${identityState.firstName} ${identityState.lastName}".trim(),
                                                                        expiryDate =
                                                                                identityState
                                                                                        .expiry,
                                                                        frontImagePath =
                                                                                identityState
                                                                                        .frontPath,
                                                                        backImagePath =
                                                                                identityState
                                                                                        .backPath,
                                                                        state =
                                                                                identityState
                                                                                        .region,
                                                                        address =
                                                                                identityState
                                                                                        .address,
                                                                        dob = identityState.dob,
                                                                        sex = identityState.sex,
                                                                        eyeColor =
                                                                                identityState
                                                                                        .eyeColor,
                                                                        height =
                                                                                identityState
                                                                                        .height,
                                                                        licenseClass =
                                                                                identityState
                                                                                        .licenseClass,
                                                                        restrictions =
                                                                                identityState
                                                                                        .restrictions,
                                                                        endorsements =
                                                                                identityState
                                                                                        .endorsements,
                                                                        issuingAuthority =
                                                                                identityState
                                                                                        .issuingAuthority
                                                                )
                                                        },
                                                        onNavigateBack = navigateBack
                                                )
                                        }
                                        4 -> {
                                                GreenCardForm(
                                                        state = greenCardState,
                                                        onScanFront = { scanDocument(false) },
                                                        onScanBack = { scanDocument(true) },
                                                        onSave = {
                                                                viewModel.saveGreenCard(
                                                                        com.vijay.cardkeeper.data
                                                                                .entity.GreenCard(
                                                                                id = documentId
                                                                                                ?: 0,
                                                                                surname =
                                                                                        greenCardState
                                                                                                .surname,
                                                                                givenName =
                                                                                        greenCardState
                                                                                                .givenName,
                                                                                uscisNumber =
                                                                                        greenCardState
                                                                                                .uscisNumber,
                                                                                category =
                                                                                        greenCardState
                                                                                                .category,
                                                                                countryOfBirth =
                                                                                        greenCardState
                                                                                                .countryOfBirth,
                                                                                dob =
                                                                                        greenCardState
                                                                                                .dob,
                                                                                sex =
                                                                                        greenCardState
                                                                                                .sex,
                                                                                expiryDate =
                                                                                        greenCardState
                                                                                                .expiryDate,
                                                                                residentSince =
                                                                                        greenCardState
                                                                                                .residentSince,
                                                                                frontImagePath =
                                                                                        greenCardState
                                                                                                .frontPath,
                                                                                backImagePath =
                                                                                        greenCardState
                                                                                                .backPath
                                                                        )
                                                                )
                                                        },
                                                        onNavigateBack = navigateBack
                                                )
                                        }
                                        5 -> {
                                                AadharCardForm(
                                                        state = aadharCardState,
                                                        onScanFront = { scanDocument(false) },
                                                        onScanBack = { scanDocument(true) },
                                                        onScanQr = { showAadharQrScanner = true },
                                                        onSave = {
                                                                viewModel.saveAadharCard(
                                                                        com.vijay.cardkeeper.data
                                                                                .entity.AadharCard(
                                                                                id = documentId
                                                                                                ?: 0,
                                                                                referenceId =
                                                                                        aadharCardState
                                                                                                .referenceId,
                                                                                holderName =
                                                                                        aadharCardState
                                                                                                .holderName,
                                                                                dob =
                                                                                        aadharCardState
                                                                                                .dob,
                                                                                gender =
                                                                                        aadharCardState
                                                                                                .gender,
                                                                                address =
                                                                                        aadharCardState
                                                                                                .address,
                                                                                pincode =
                                                                                        aadharCardState
                                                                                                .pincode
                                                                                                .ifEmpty {
                                                                                                        null
                                                                                                },
                                                                                maskedAadhaarNumber =
                                                                                        aadharCardState
                                                                                                .maskedAadhaarNumber,
                                                                                uid =
                                                                                        aadharCardState
                                                                                                .uid
                                                                                                .ifEmpty {
                                                                                                        null
                                                                                                },
                                                                                vid =
                                                                                        aadharCardState
                                                                                                .vid
                                                                                                .ifEmpty {
                                                                                                        null
                                                                                                },
                                                                                photoBase64 =
                                                                                        aadharCardState
                                                                                                .photoBase64,
                                                                                timestamp =
                                                                                        aadharCardState
                                                                                                .timestamp
                                                                                                .ifEmpty {
                                                                                                        null
                                                                                                },
                                                                                digitalSignature =
                                                                                        aadharCardState
                                                                                                .digitalSignature,
                                                                                certificateId =
                                                                                        aadharCardState
                                                                                                .certificateId,
                                                                                enrollmentNumber =
                                                                                        aadharCardState
                                                                                                .enrollmentNumber
                                                                                                .ifEmpty {
                                                                                                        null
                                                                                                },
                                                                                email =
                                                                                        aadharCardState
                                                                                                .email,
                                                                                mobile =
                                                                                        aadharCardState
                                                                                                .mobile,
                                                                                frontImagePath =
                                                                                        aadharCardState
                                                                                                .frontPath,
                                                                                backImagePath =
                                                                                        aadharCardState
                                                                                                .backPath,
                                                                                qrData =
                                                                                        aadharCardState
                                                                                                .qrData
                                                                        )
                                                                )
                                                        },
                                                        onNavigateBack = navigateBack
                                                )
                                        }
                                        else -> {
                                                Text("Unsupported category: $selectedCategory")
                                        }
                                }
                        }
                }

                // Barcode Scanner Overlay
                if (showBarcodeScanner) {
                        BarcodeScannerScreen(
                                onBarcodeScanned = handleBarcodeResult,
                                onDismiss = { showBarcodeScanner = false }
                        )
                }

                // Aadhar QR Scanner Overlay
                if (showAadharQrScanner) {
                        BarcodeScannerScreen(
                                onBarcodeScanned = { qrData, _ ->
                                        showAadharQrScanner = false
                                        scope.launch {
                                                val result = aadharQrScanner.parse(qrData)

                                                // Populate form fields from QR
                                                if (result.referenceId.isNotEmpty()) {
                                                        aadharCardState.referenceId =
                                                                result.referenceId
                                                }
                                                if (result.name.isNotEmpty()) {
                                                        aadharCardState.holderName = result.name
                                                }
                                                if (result.dob.isNotEmpty()) {
                                                        aadharCardState.rawDob = result.dob.filter { it.isDigit() }
                                                }
                                                if (result.gender.isNotEmpty()) {
                                                        aadharCardState.gender = result.gender
                                                }
                                                if (result.fullAddress.isNotEmpty()) {
                                                        aadharCardState.address = result.fullAddress
                                                }
                                                if (result.pincode.isNotEmpty()) {
                                                        aadharCardState.pincode = result.pincode
                                                }
                                                // Handle Full UID from Legacy XML (stored in
                                                // referenceId usually)
                                                // If referenceId is exactly 12 digits, treat it as
                                                // UID
                                                if (result.referenceId.length == 12 &&
                                                                result.referenceId.all {
                                                                        it.isDigit()
                                                                }
                                                ) {
                                                        aadharCardState.uid = result.referenceId
                                                        // Also update masked if not already set
                                                        // (though scanner usually sets it)
                                                        if (aadharCardState.maskedAadhaarNumber
                                                                        .isEmpty()
                                                        ) {
                                                                aadharCardState
                                                                        .maskedAadhaarNumber =
                                                                        "XXXX XXXX " +
                                                                                result.referenceId
                                                                                        .takeLast(4)
                                                        }
                                                }

                                                if (result.email != null) {
                                                        aadharCardState.email = result.email
                                                }
                                                if (result.mobile != null) {
                                                        aadharCardState.mobile = result.mobile
                                                }

                                                if (result.maskedAadhaar.isNotEmpty()) {
                                                        aadharCardState.maskedAadhaarNumber =
                                                                "XXXX XXXX ${result.maskedAadhaar}"
                                                }
                                                if (result.photoBase64 != null) {
                                                        aadharCardState.photoBase64 =
                                                                result.photoBase64
                                                }
                                                // Store raw QR data for reproduction
                                                aadharCardState.qrData = qrData

                                                // Store signature verification status
                                                aadharCardState.signatureValid =
                                                        result.signatureValid

                                                // Show verification result
                                                val verifyMsg =
                                                        if (result.signatureVerificationAttempted) {
                                                                if (result.signatureValid)
                                                                        "✓ Signature Verified"
                                                                else "✗ Signature Invalid"
                                                        } else {
                                                                "⚠ Signature not verified (no certificate)"
                                                        }
                                                Toast.makeText(
                                                                context,
                                                                "QR Scanned: ${result.name}\n$verifyMsg",
                                                                Toast.LENGTH_LONG
                                                        )
                                                        .show()
                                        }
                                },
                                onDismiss = { showAadharQrScanner = false }
                        )
                }
        } // Close Box
} // Close AddItemScreen
