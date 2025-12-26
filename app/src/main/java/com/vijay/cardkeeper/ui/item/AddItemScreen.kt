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
import com.vijay.cardkeeper.scanning.ChequeScanner
import com.vijay.cardkeeper.scanning.DriverLicenseScanner
import com.vijay.cardkeeper.scanning.IdentityScanner
import com.vijay.cardkeeper.scanning.PassportScanner
import com.vijay.cardkeeper.scanning.PaymentCardScanner
import com.vijay.cardkeeper.scanning.RewardsScanner
import com.vijay.cardkeeper.ui.item.forms.FinancialForm
import com.vijay.cardkeeper.ui.item.forms.IdentityForm
import com.vijay.cardkeeper.ui.item.forms.PassportForm
import com.vijay.cardkeeper.ui.item.forms.rememberFinancialFormState
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
                                else -> "financial"
                        }
        // Load existing item if editing
        val item by viewModel.getItem(documentId, typeForLookup).collectAsState(initial = null)

        // Category / Tabs
        // If we are editing, we lock the category to the item type.
        // If adding new, we start at 0 (Financial) but user can switch.
        // Index mapping: 0 -> Financial, 1 -> Identity, 2 -> Passport, 3 -> Rewards
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
                                else -> null
                        }
                }

        // Form States
        val financialState =
                rememberFinancialFormState(item as? FinancialAccount, initialAccountType)
        val identityState =
                rememberIdentityFormState(item as? IdentityDocument, initialDocumentType)
        val passportState = rememberPassportFormState(item as? Passport)

        // Scanners
        val paymentScanner = remember { PaymentCardScanner() }
        val rewardsScanner = remember { RewardsScanner() }
        val identityScanner = remember { IdentityScanner() }
        val driverLicenseScanner = remember { DriverLicenseScanner() }
        val chequeScanner = remember { ChequeScanner() }
        val passportScanner = remember { PassportScanner() }

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
                                                        if (selectedCategory == 0 ||
                                                                        selectedCategory == 3
                                                        ) {
                                                                // Financial (0) or Rewards (3)
                                                                // If category is 3, force type to
                                                                // REWARDS_CARD for logic if needed,
                                                                // though state.type should be set
                                                                // by UI selection or default.

                                                                if (scanningBack) {
                                                                        financialState.backBitmap =
                                                                                bitmap
                                                                        financialState.backPath =
                                                                                savedPath
                                                                        // Back Scan Logic
                                                                        if (financialState.type ==
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
                                                                                res.barcode?.let {
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
                                                                                res.shopName?.let {
                                                                                        financialState
                                                                                                .institution =
                                                                                                it
                                                                                }
                                                                        } else {
                                                                                // Payment Card Back
                                                                                // logic
                                                                                val details =
                                                                                        paymentScanner
                                                                                                .scan(
                                                                                                        bitmap
                                                                                                )
                                                                                // Only update if
                                                                                // empty
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
                                                                                // CVV is rarely
                                                                                // extracted by
                                                                                // MLKit
                                                                                // text but possible
                                                                        }
                                                                } else {
                                                                        financialState.frontBitmap =
                                                                                bitmap
                                                                        financialState.frontPath =
                                                                                savedPath
                                                                        // Front Scan Logic
                                                                        if (financialState.type ==
                                                                                        AccountType
                                                                                                .REWARDS_CARD
                                                                        ) {
                                                                                val res =
                                                                                        rewardsScanner
                                                                                                .scan(
                                                                                                        bitmap
                                                                                                )
                                                                                res.barcode?.let {
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
                                                                                res.shopName?.let {
                                                                                        financialState
                                                                                                .institution =
                                                                                                it
                                                                                }
                                                                                // Logo logic?
                                                                                // (Simplified:
                                                                                // prefer
                                                                                // front scan for
                                                                                // logo if not
                                                                                // explicitly
                                                                                // picked)
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
                                                                                // Bank Account -
                                                                                // Cheque Scan
                                                                                val chequeDetails =
                                                                                        chequeScanner
                                                                                                .scan(
                                                                                                        bitmap
                                                                                                )

                                                                                // Update fields
                                                                                // from scan only if
                                                                                // they were found
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

                                                                                // Show toast with
                                                                                // extraction
                                                                                // summary from
                                                                                // scanner for user
                                                                                // verification
                                                                                android.widget.Toast
                                                                                        .makeText(
                                                                                                context,
                                                                                                chequeDetails
                                                                                                        .extractionSummary,
                                                                                                android.widget
                                                                                                        .Toast
                                                                                                        .LENGTH_LONG
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
                                                                                // Map Scheme to
                                                                                // Network
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
                                                                                        // logic to
                                                                                        // switch
                                                                                        // type?
                                                                                        // User
                                                                                        // picks
                                                                                        // type
                                                                                        // first
                                                                                        // usually.
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
                                                        } else if (selectedCategory == 2) {
                                                                // Passport
                                                                if (scanningBack) {
                                                                        passportState.backBitmap =
                                                                                bitmap
                                                                        passportState.backPath =
                                                                                savedPath
                                                                        val details =
                                                                                passportScanner
                                                                                        .scanBack(
                                                                                                bitmap
                                                                                        )
                                                                        // Update back-side fields
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
                                                                        passportState.frontBitmap =
                                                                                bitmap
                                                                        passportState.frontPath =
                                                                                savedPath
                                                                        val details =
                                                                                passportScanner
                                                                                        .scanFront(
                                                                                                bitmap
                                                                                        )
                                                                        // Update front-side fields
                                                                        // (MRZ)

                                                                        // Helper to safely set
                                                                        // string fields
                                                                        fun setIfNotNull(
                                                                                value: String?,
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
                                                                        setIfNotNull(details.dob) {
                                                                                passportState.dob =
                                                                                        it
                                                                        }
                                                                        setIfNotNull(details.sex) {
                                                                                passportState.sex =
                                                                                        it
                                                                        }
                                                                        setIfNotNull(
                                                                                details.dateOfExpiry
                                                                        ) {
                                                                                passportState
                                                                                        .dateOfExpiry =
                                                                                        it
                                                                        }
                                                                        setIfNotNull(
                                                                                details.dateOfIssue
                                                                        ) {
                                                                                passportState
                                                                                        .dateOfIssue =
                                                                                        it
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
                                                        } else {
                                                                // Identity
                                                                if (scanningBack) {
                                                                        identityState.backBitmap =
                                                                                bitmap
                                                                        identityState.backPath =
                                                                                savedPath

                                                                        // Use barcode scanner for
                                                                        // Driver License back
                                                                        // (PDF417)
                                                                        // Barcode data is MORE
                                                                        // ACCURATE than OCR, so we
                                                                        // overwrite fields
                                                                        if (identityState.type ==
                                                                                        DocumentType
                                                                                                .DRIVER_LICENSE
                                                                        ) {
                                                                                val details =
                                                                                        driverLicenseScanner
                                                                                                .scan(
                                                                                                        bitmap
                                                                                                )

                                                                                // Always use
                                                                                // barcode data if
                                                                                // available (more
                                                                                // reliable than
                                                                                // OCR)
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
                                                                                                .dob =
                                                                                                details.dob
                                                                                if (details.expiryDate
                                                                                                .isNotEmpty()
                                                                                )
                                                                                        identityState
                                                                                                .expiry =
                                                                                                details.expiryDate
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
                                                                                // Use text
                                                                                // recognition for
                                                                                // Passport back
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
                                                                                                .dob =
                                                                                                details.dob
                                                                                if (identityState
                                                                                                .sex
                                                                                                .isEmpty()
                                                                                )
                                                                                        identityState
                                                                                                .sex =
                                                                                                details.sex
                                                                        }
                                                                        // update other
                                                                        // back-specific fields if
                                                                        // needed
                                                                } else {
                                                                        identityState.frontBitmap =
                                                                                bitmap
                                                                        identityState.frontPath =
                                                                                savedPath
                                                                        val details =
                                                                                identityScanner
                                                                                        .scan(
                                                                                                bitmap,
                                                                                                scanningBack
                                                                                        )
                                                                        if (identityState.number
                                                                                        .isEmpty()
                                                                        )
                                                                                identityState
                                                                                        .number =
                                                                                        details.docNumber
                                                                        if (identityState.firstName
                                                                                        .isEmpty()
                                                                        )
                                                                                identityState
                                                                                        .firstName =
                                                                                        details.name
                                                                                                .substringBefore(
                                                                                                        " "
                                                                                                )
                                                                        if (identityState.lastName
                                                                                        .isEmpty()
                                                                        )
                                                                                identityState
                                                                                        .lastName =
                                                                                        details.name
                                                                                                .substringAfter(
                                                                                                        " ",
                                                                                                        ""
                                                                                                )
                                                                        if (identityState.dob
                                                                                        .isEmpty()
                                                                        )
                                                                                identityState.dob =
                                                                                        details.dob
                                                                        if (identityState.address
                                                                                        .isEmpty()
                                                                        )
                                                                                identityState
                                                                                        .address =
                                                                                        details.address
                                                                }
                                                        }
                                                }
                                        } // closes if (bitmap != null)
                                } // closes let
                        } // closes if (result.resultCode == RESULT_OK)
                } // closes rememberLauncherForActivityResult

        val scanDocument = { isBack: Boolean ->
                scanningBack = isBack

                // Use barcode scanner for Driver License back scans
                if (isBack &&
                                selectedCategory == 1 &&
                                identityState.type == DocumentType.DRIVER_LICENSE
                ) {
                        showBarcodeScanner = true
                } else {
                        // Use document scanner for everything else
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
                        if (selectedCategory == 0) {
                                when (financialState.type) {
                                        AccountType.CREDIT_CARD, AccountType.DEBIT_CARD ->
                                                "Update Credit/Debit Card"
                                        AccountType.BANK_ACCOUNT -> "Update Bank Account"
                                        else -> "Update Financial Account"
                                }
                        } else if (selectedCategory == 3) {
                                when (financialState.type) {
                                        AccountType.LIBRARY_CARD -> "Update Library Card"
                                        else -> "Update Rewards Card"
                                }
                        } else if (selectedCategory == 2) {
                                "Update Passport"
                        } else {
                                when (identityState.type) {
                                        DocumentType.DRIVER_LICENSE -> "Update Driver License"
                                        DocumentType.SSN -> "Update SSN"
                                        DocumentType.PAN -> "Update PAN"
                                        DocumentType.ADHAAR -> "Update Aadhar"
                                        else -> "Update Identity Document"
                                }
                        }
                } else {
                        // Adding new item
                        if (selectedCategory == 0) {
                                when (financialState.type) {
                                        AccountType.CREDIT_CARD, AccountType.DEBIT_CARD ->
                                                "Add Credit/Debit Card"
                                        AccountType.BANK_ACCOUNT -> "Add Bank Account"
                                        else -> "Add Financial Account"
                                }
                        } else if (selectedCategory == 3) {
                                when (financialState.type) {
                                        AccountType.LIBRARY_CARD -> "Add Library Card"
                                        else -> "Add Rewards Card"
                                }
                        } else if (selectedCategory == 2) {
                                "Add Passport"
                        } else {
                                when (identityState.type) {
                                        DocumentType.DRIVER_LICENSE -> "Add Driver License"
                                        DocumentType.SSN -> "Add SSN"
                                        DocumentType.PAN -> "Add PAN"
                                        DocumentType.ADHAAR -> "Add Aadhar"
                                        else -> "Add Identity Document"
                                }
                        }
                }

        val typeIcon =
                if (selectedCategory == 0) {
                        when (financialState.type) {
                                AccountType.CREDIT_CARD, AccountType.DEBIT_CARD ->
                                        Icons.Filled.CreditCard
                                AccountType.BANK_ACCOUNT -> Icons.Filled.AccountBalance
                                AccountType.REWARDS_CARD -> Icons.Filled.CardGiftcard
                                else -> Icons.Filled.AccountBalance
                        }
                } else if (selectedCategory == 2) {
                        Icons.Filled.AccountBox
                } else {
                        when (identityState.type) {
                                DocumentType.DRIVER_LICENSE -> Icons.Filled.DirectionsCar
                                else -> Icons.Filled.Face
                        }
                }

        // Barcode result handler - also captures and saves the back image
        val handleBarcodeResult: (String, android.graphics.Bitmap?) -> Unit =
                { rawData, capturedBitmap ->
                        showBarcodeScanner = false

                        // Save the captured back image
                        capturedBitmap?.let { bitmap ->
                                val savedPath =
                                        saveImageToInternalStorage(
                                                context,
                                                bitmap,
                                                "dl_back_${System.currentTimeMillis()}"
                                        )
                                identityState.backBitmap = bitmap
                                identityState.backPath = savedPath
                        }

                        scope.launch {
                                val details = driverLicenseScanner.parseAAMVAData(rawData)

                                // Populate form fields from barcode data
                                if (details.docNumber.isNotEmpty())
                                        identityState.number = details.docNumber
                                if (details.name.isNotEmpty()) {
                                        identityState.firstName = details.name.substringBefore(" ")
                                        identityState.lastName =
                                                details.name.substringAfterLast(" ", "")
                                }
                                if (details.dob.isNotEmpty()) identityState.dob = details.dob
                                if (details.expiryDate.isNotEmpty())
                                        identityState.expiry = details.expiryDate
                                if (details.address.isNotEmpty())
                                        identityState.address = details.address
                                if (details.sex.isNotEmpty()) identityState.sex = details.sex
                                if (details.eyeColor.isNotEmpty())
                                        identityState.eyeColor = details.eyeColor
                                if (details.height.isNotEmpty())
                                        identityState.height = details.height
                                if (details.licenseClass.isNotEmpty())
                                        identityState.licenseClass = details.licenseClass
                                if (details.restrictions.isNotEmpty())
                                        identityState.restrictions = details.restrictions
                                if (details.endorsements.isNotEmpty())
                                        identityState.endorsements = details.endorsements
                                if (details.state.isNotEmpty()) identityState.region = details.state
                                if (details.issuingAuthority.isNotEmpty())
                                        identityState.issuingAuthority = details.issuingAuthority
                                if (details.country.isNotEmpty())
                                        identityState.country = details.country
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

                                if (selectedCategory == 0 || selectedCategory == 3) {
                                        // If Rewards category is selected, ensure type is
                                        // REWARDS_CARD or LIBRARY_CARD
                                        if (selectedCategory == 3 &&
                                                        financialState.type !=
                                                                AccountType.REWARDS_CARD &&
                                                        financialState.type !=
                                                                AccountType.LIBRARY_CARD
                                        ) {
                                                financialState.type = AccountType.REWARDS_CARD
                                        }

                                        FinancialForm(
                                                state = financialState,
                                                onScanFront = { scanDocument(false) },
                                                onScanBack = { scanDocument(true) },
                                                onPickLogo = { logoLauncher.launch("image/*") },
                                                onSave = {
                                                        viewModel.saveFinancialAccount(
                                                                id = documentId ?: 0,
                                                                type = financialState.type,
                                                                institution =
                                                                        financialState.institution,
                                                                name = financialState.accName,
                                                                holder = financialState.holder,
                                                                number = financialState.number,
                                                                routing = financialState.routing,
                                                                ifsc = financialState.ifsc,
                                                                swift = financialState.swift,
                                                                expiryDate = financialState.expiry,
                                                                cvv = financialState.cvv,
                                                                pin = financialState.pin,
                                                                notes = financialState.notes,
                                                                contact = financialState.contact,
                                                                cardNetwork =
                                                                        financialState.network,
                                                                frontImagePath =
                                                                        financialState.frontPath,
                                                                backImagePath =
                                                                        financialState.backPath,
                                                                barcode =
                                                                        if (financialState.type ==
                                                                                        AccountType
                                                                                                .REWARDS_CARD
                                                                        )
                                                                                financialState
                                                                                        .barcode
                                                                        else null,
                                                                barcodeFormat =
                                                                        if (financialState.type ==
                                                                                        AccountType
                                                                                                .REWARDS_CARD
                                                                        )
                                                                                financialState
                                                                                        .barcodeFormat
                                                                        else null,
                                                                linkedPhoneNumber =
                                                                        financialState.linkedPhone,
                                                                logoImagePath =
                                                                        financialState.logoPath,
                                                                accountSubType =
                                                                        financialState
                                                                                .accountSubType,
                                                                wireNumber =
                                                                        financialState.wireNumber,
                                                                branchAddress =
                                                                        financialState
                                                                                .branchAddress,
                                                                branchContactNumber =
                                                                        financialState
                                                                                .branchContact,
                                                                bankWebUrl =
                                                                        financialState.bankWebUrl,
                                                                bankBrandColor =
                                                                        financialState
                                                                                .bankBrandColor,
                                                                holderAddress =
                                                                        financialState.holderAddress
                                                        )
                                                },
                                                onNavigateBack = navigateBack
                                        )
                                } else if (selectedCategory == 2) {
                                        // Passport Form
                                        PassportForm(
                                                state = passportState,
                                                onScanFront = { scanDocument(false) },
                                                onScanBack = { scanDocument(true) },
                                                onSave = {
                                                        viewModel.savePassport(
                                                                Passport(
                                                                        id =
                                                                                if (item is Passport
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
                                                                        dob = passportState.dob,
                                                                        sex = passportState.sex,
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
                                } else {
                                        // Identity
                                        IdentityForm(
                                                state = identityState,
                                                onScanFront = { scanDocument(false) },
                                                onScanBack = { scanDocument(true) },
                                                onSave = {
                                                        viewModel.saveIdentityDocument(
                                                                id = documentId ?: 0,
                                                                type = identityState.type,
                                                                country = identityState.country,
                                                                docNumber = identityState.number,
                                                                holder =
                                                                        "${identityState.firstName} ${identityState.lastName}".trim(),
                                                                expiryDate = identityState.expiry,
                                                                frontImagePath =
                                                                        identityState.frontPath,
                                                                backImagePath =
                                                                        identityState.backPath,
                                                                state = identityState.region,
                                                                address = identityState.address,
                                                                dob = identityState.dob,
                                                                sex = identityState.sex,
                                                                eyeColor = identityState.eyeColor,
                                                                height = identityState.height,
                                                                licenseClass =
                                                                        identityState.licenseClass,
                                                                restrictions =
                                                                        identityState.restrictions,
                                                                endorsements =
                                                                        identityState.endorsements,
                                                                issuingAuthority =
                                                                        identityState
                                                                                .issuingAuthority
                                                        )
                                                },
                                                onNavigateBack = navigateBack
                                        )
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
        } // Close Box
}
