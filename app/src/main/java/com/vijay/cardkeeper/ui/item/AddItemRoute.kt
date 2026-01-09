package com.vijay.cardkeeper.ui.item

import android.app.Activity
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.vijay.cardkeeper.data.entity.*
import com.vijay.cardkeeper.scanning.*
import com.vijay.cardkeeper.ui.item.forms.*
import com.vijay.cardkeeper.ui.scanner.BarcodeScannerScreen
import com.vijay.cardkeeper.ui.viewmodel.AddItemViewModel
import com.vijay.cardkeeper.ui.viewmodel.AppViewModelProvider
import com.vijay.cardkeeper.util.saveImageToInternalStorage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemRoute(
    documentId: Int?,
    documentType: String?, // "financial" or "identity"
    initialCategory: Int = 0,
    navigateBack: (savedCategory: Int) -> Unit,
    viewModel: AddItemViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val activity = context as Activity
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }

    // Observe global snackbar messages
    LaunchedEffect(Unit) {
        com.vijay.cardkeeper.ui.common.SnackbarManager.messages.collect { message ->
            snackbarHostState.showSnackbar(
                message = message.message,
                actionLabel = message.actionLabel,
                withDismissAction = message.withDismissAction,
                duration = androidx.compose.material3.SnackbarDuration.Short
            )
        }
    }

    // Type lookup logic
    val typeForLookup = documentType.takeIf { !it.isNullOrEmpty() }
        ?: when (initialCategory) {
            0 -> "financial"
            1 -> "identity"
            2 -> "passport"
            3 -> "rewards" 
            4 -> "greencard"
            5 -> "aadhar"
            6 -> "giftcard"
            7 -> "pancard"
            8 -> "insurance"
            else -> "financial"
        }

    val item by viewModel.getItem(documentId, typeForLookup).collectAsState(initial = null)

    var selectedCategory by remember { mutableIntStateOf(initialCategory) }
    
    // Sync selectedCategory with loaded item
    LaunchedEffect(item) {
        item?.let {
            when (it) {
                is IdentityDocument -> selectedCategory = 1
                is FinancialAccount -> {
                    selectedCategory = 0
                }
                is RewardCard -> {
                    selectedCategory = 3
                }
                is Passport -> selectedCategory = 2
                is GreenCard -> selectedCategory = 4
                is AadharCard -> selectedCategory = 5
                is GiftCard -> selectedCategory = 6
                is GiftCard -> selectedCategory = 6
                is PanCard -> selectedCategory = 7
                is InsuranceCard -> selectedCategory = 8
            }
        }
    }

    // Map initialType string to enum if possible
    val initialAccountType = remember(documentType) {
        when (documentType?.uppercase()) {
            "CREDIT_CARD" -> AccountType.CREDIT_CARD
            "DEBIT_CARD" -> AccountType.DEBIT_CARD
            "BANK_ACCOUNT" -> AccountType.BANK_ACCOUNT
            "REWARDS_CARD" -> AccountType.REWARDS_CARD
            else -> null
        }
    }

    // Form States
    val financialState = rememberFinancialFormState(
        account = item as? FinancialAccount,
        initialType = initialAccountType
    )
    val rewardsState = rememberRewardsFormState(
        reward = item as? RewardCard
    )
    val identityState = rememberIdentityFormState(item as? IdentityDocument, null)
    val passportState = rememberPassportFormState(item as? Passport)
    val greenCardState = rememberGreenCardFormState(item as? GreenCard)
    val aadharCardState = rememberAadharCardFormState(item as? AadharCard)
    val giftCardState = rememberGiftCardFormState(item as? GiftCard)
    val panCardState = rememberPanCardFormState(item as? PanCard)
    val insuranceState = rememberInsuranceCardFormState(item as? InsuranceCard)

    // Scanners
    val scannerOptions = remember {
        GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(true)
            .setPageLimit(1)
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .build()
    }
    val scanner = remember { GmsDocumentScanning.getClient(scannerOptions) }
    val paymentScanner = remember { PaymentCardScanner() }
    val rewardsScanner = remember { RewardsScanner() }
    val identityScanner = remember { IdentityScanner() }
    val driverLicenseScanner = remember { DriverLicenseScanner() }
    val chequeScanner = remember { ChequeScanner() }
    val passportScanner = remember { PassportScanner() }
    val greenCardScanner = remember { GreenCardScanner() }
    val aadharScanner = remember { AadharScanner() }
    val aadharQrScanner = remember { AadharQrScanner(context) }
    val panCardScanner = remember { PanCardScanner() }
    val insuranceCardScanner = remember { InsuranceCardScanner() }

    // State for Camera/Barcode logic
    var scanningBack by remember { mutableStateOf(false) }
    var showBarcodeScanner by remember { mutableStateOf(false) }

    // Scan Processor
    val scanProcessor = remember(
        paymentScanner, rewardsScanner, identityScanner, driverLicenseScanner,
        chequeScanner, passportScanner, greenCardScanner, aadharScanner, panCardScanner, insuranceCardScanner
    ) {
        com.vijay.cardkeeper.ui.item.logic.ScanResultProcessor(
            paymentScanner, rewardsScanner, identityScanner, driverLicenseScanner,
            chequeScanner, passportScanner, greenCardScanner, aadharScanner, panCardScanner, insuranceCardScanner
        )
    }

    // Document Scanner Launcher
    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanningResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            scanningResult?.pages?.firstOrNull()?.imageUri?.let { uri ->
                scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()

                    bitmap?.let { b ->
                        val path = saveImageToInternalStorage(context, b, "scan_${System.currentTimeMillis()}")
                        scanProcessor.process(
                            bitmap = b, path = path, category = selectedCategory, isBack = scanningBack,
                            financialState = financialState, rewardsState = rewardsState, identityState = identityState,
                            passportState = passportState, greenCardState = greenCardState,
                            aadharCardState = aadharCardState, giftCardState = giftCardState,
                            panCardState = panCardState, insuranceState = insuranceState
                        )
                    }
                }
            }
        }
    }

    // Logo Picker Launcher
    val logoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                val source = android.graphics.ImageDecoder.createSource(context.contentResolver, it)
                val bitmap = android.graphics.ImageDecoder.decodeBitmap(source)
                val path = saveImageToInternalStorage(context, bitmap, "logo_${System.currentTimeMillis()}")
                
                // Switch back to Main for state updates if needed, though state is thread-safe mostly
               kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                   if (selectedCategory == 0) financialState.logoPath = path
                   else if (selectedCategory == 3) rewardsState.logoPath = path
                   else if (selectedCategory == 6) giftCardState.logoPath = path
               }
            }
        }
    }

    val onScanRequest: (Int, ScanRequestType) -> Unit = { category, type ->
        when (type) {
            ScanRequestType.FRONT -> {
                scanningBack = false
                scanner.getStartScanIntent(activity).addOnSuccessListener { 
                    scannerLauncher.launch(IntentSenderRequest.Builder(it).build())
                }
            }
            ScanRequestType.BACK -> {
                scanningBack = true
                scanner.getStartScanIntent(activity).addOnSuccessListener { 
                    scannerLauncher.launch(IntentSenderRequest.Builder(it).build())
                }
            }
            ScanRequestType.QR, ScanRequestType.BARCODE -> {
                showBarcodeScanner = true
            }
            ScanRequestType.PICK_LOGO -> {
                logoLauncher.launch("image/*")
            }
        }
    }

    val onSave: () -> Unit = {
        scope.launch {
            try {
                println("CardKeeperUI: Starting save operation for category: $selectedCategory")
                when (selectedCategory) {
                    0 -> {
                        println("CardKeeperUI: Saving Financial Account")
                        val acc = FinancialAccount(
                            id = if (documentId != null && (typeForLookup == "financial" || typeForLookup == "rewards")) documentId else 0,
                            type = financialState.type,
                            institutionName = financialState.institution,
                            accountName = financialState.accName,
                            holderName = financialState.holder,
                            number = financialState.number,
                            cvv = financialState.cvv.ifBlank { null },
                            pinHint = financialState.pin.ifBlank { null },
                            routingNumber = financialState.routing.ifBlank { null },
                            ifscCode = financialState.ifsc.ifBlank { null },
                            swiftCode = financialState.swift.ifBlank { null },
                            wireNumber = financialState.wireNumber.ifBlank { null },
                            accountSubType = financialState.accountSubType,
                            expiryDate = financialState.formattedExpiry.ifBlank { null },
                            cardNetwork = financialState.network.ifBlank { null },
                            notes = financialState.notes.ifBlank { null },
                            cardPin = financialState.pin.ifBlank { null },
                            lostCardContactNumber = financialState.contact.ifBlank { null },
                            frontImagePath = financialState.frontPath,
                            backImagePath = financialState.backPath,
                            logoImagePath = financialState.logoPath,
                            barcode = null,
                            barcodeFormat = null,
                            linkedPhoneNumber = null
                        )
                        val job = viewModel.saveFinancialAccount(acc)
                        job.join()
                        println("CardKeeperUI: Financial Account saved successfully")
                    }
                    3 -> {
                        println("CardKeeperUI: Saving Reward Card")
                        val rc = RewardCard(
                            id = if (documentId != null && typeForLookup == "rewards") documentId else 0,
                            name = rewardsState.institution,
                            type = rewardsState.type,
                            barcode = rewardsState.barcode.ifBlank { null },
                            barcodeFormat = rewardsState.barcodeFormat,
                            linkedPhoneNumber = rewardsState.linkedPhone.ifBlank { null },
                            frontImagePath = rewardsState.frontPath,
                            backImagePath = rewardsState.backPath,
                            logoImagePath = rewardsState.logoPath,
                            notes = rewardsState.notes.ifBlank { null }
                        )
                        val job = viewModel.saveRewardCard(rc)
                        job.join()
                        println("CardKeeperUI: Reward Card saved successfully")
                    }
                    1 -> {
                        println("CardKeeperUI: Saving Identity Document")
                        val doc = IdentityDocument(
                            id = if (documentId != null && typeForLookup == "identity") documentId else 0,
                            type = identityState.type,
                            country = identityState.country,
                            docNumber = identityState.number,
                            holderName = "${identityState.firstName} ${identityState.lastName}".trim(),
                            expiryDate = identityState.expiry,
                            frontImagePath = identityState.frontPath,
                            backImagePath = identityState.backPath,
                            state = identityState.region,
                            address = identityState.address,
                            dob = identityState.dob,
                            sex = identityState.sex,
                            eyeColor = identityState.eyeColor,
                            height = identityState.height,
                            licenseClass = identityState.licenseClass,
                            restrictions = identityState.restrictions,
                            endorsements = identityState.endorsements,
                            issuingAuthority = identityState.issuingAuthority
                        )
                        println("CardKeeperUI: Identity doc created: id=${doc.id}, type=${doc.type}, number=${doc.docNumber}")
                        val job = viewModel.saveIdentityDocument(doc)
                        println("CardKeeperUI: Save job launched, waiting...")
                        job.join()
                        println("CardKeeperUI: Identity Document saved successfully")
                    }
                    2 -> {
                        println("CardKeeperUI: Saving Passport")
                        val p = Passport(
                            id = if (documentId != null && typeForLookup == "passport") documentId else 0,
                            passportNumber = passportState.passportNumber,
                            countryCode = passportState.countryCode,
                            surname = passportState.surname,
                            givenNames = passportState.givenNames,
                            dob = passportState.dob,
                            sex = passportState.sex,
                            dateOfExpiry = passportState.dateOfExpiry,
                            dateOfIssue = passportState.dateOfIssue,
                            placeOfIssue = passportState.placeOfIssue,
                            nationality = passportState.nationality,
                            frontImagePath = passportState.frontPath,
                            backImagePath = passportState.backPath,
                            fatherName = passportState.fatherName,
                            motherName = passportState.motherName,
                            spouseName = passportState.spouseName,
                            address = passportState.address,
                            fileNumber = passportState.fileNumber,
                            authority = passportState.authority
                        )
                        val job = viewModel.savePassport(p)
                        job.join()
                        println("CardKeeperUI: Passport saved successfully")
                    }
                    4 -> {
                        println("CardKeeperUI: Saving Green Card")
                        val gc = GreenCard(
                            id = if (documentId != null && typeForLookup == "greencard") documentId else 0,
                            surname = greenCardState.surname,
                            givenName = greenCardState.givenName,
                            uscisNumber = greenCardState.uscisNumber,
                            category = greenCardState.category,
                            countryOfBirth = greenCardState.countryOfBirth,
                            dob = greenCardState.dob,
                            sex = greenCardState.sex,
                            expiryDate = greenCardState.expiryDate,
                            residentSince = greenCardState.residentSince,
                            frontImagePath = greenCardState.frontPath,
                            backImagePath = greenCardState.backPath
                        )
                        println("CardKeeperUI: Green Card created: id=${gc.id}, number=${gc.uscisNumber}")
                        val job = viewModel.saveGreenCard(gc)
                        println("CardKeeperUI: Save job launched, waiting...")
                        job.join()
                        println("CardKeeperUI: Green Card saved successfully")
                    }
                    5 -> {
                        println("CardKeeperUI: Saving Aadhar Card")
                        val ac = AadharCard(
                            id = if (documentId != null && typeForLookup == "aadhar") documentId else 0,
                            uid = aadharCardState.uid.ifBlank { null },
                            holderName = aadharCardState.holderName,
                            dob = aadharCardState.dob,
                            gender = aadharCardState.gender,
                            address = aadharCardState.address,
                            mobile = aadharCardState.mobile.ifBlank { null },
                            email = aadharCardState.email.ifBlank { null },
                            pincode = aadharCardState.pincode.ifBlank { null },
                            referenceId = aadharCardState.referenceId,
                            photoBase64 = aadharCardState.photoBase64,
                            maskedAadhaarNumber = aadharCardState.maskedAadhaarNumber,
                            enrollmentNumber = aadharCardState.enrollmentNumber.ifBlank { null },
                            vid = aadharCardState.vid.ifBlank { null },
                            frontImagePath = aadharCardState.frontPath,
                            backImagePath = aadharCardState.backPath,
                            qrData = aadharCardState.qrData,
                            timestamp = aadharCardState.timestamp,
                            certificateId = aadharCardState.certificateId,
                            digitalSignature = aadharCardState.digitalSignature
                        )
                        val job = viewModel.saveAadharCard(ac)
                        job.join()
                        println("CardKeeperUI: Aadhar Card saved successfully")
                    }
                    6 -> {
                        println("CardKeeperUI: Saving Gift Card")
                        val gc = GiftCard(
                            id = if (documentId != null && typeForLookup == "giftcard") documentId else 0,
                            providerName = giftCardState.providerName,
                            cardNumber = giftCardState.cardNumber,
                            pin = giftCardState.pin,
                            notes = giftCardState.notes,
                            frontImagePath = giftCardState.frontPath,
                            backImagePath = giftCardState.backPath,
                            logoImagePath = giftCardState.logoPath,
                            barcode = giftCardState.barcode,
                            barcodeFormat = giftCardState.barcodeFormat,
                            qrCode = giftCardState.qrCode
                        )
                        val job = viewModel.saveGiftCard(gc)
                        job.join()
                        println("CardKeeperUI: Gift Card saved successfully")
                    }
                    7 -> {
                        println("CardKeeperUI: Saving PAN Card")
                        val pc = PanCard(
                            id = if (documentId != null && typeForLookup == "pancard") documentId else 0,
                            panNumber = panCardState.panNumber,
                            holderName = panCardState.holderName,
                            fatherName = panCardState.fatherName,
                            dob = panCardState.dob,
                            frontImagePath = panCardState.frontPath,
                            backImagePath = panCardState.backPath
                        )
                        val job = viewModel.savePanCard(pc)
                        job.join()
                        println("CardKeeperUI: PAN Card saved successfully")
                    }
                    8 -> {
                        println("CardKeeperUI: Saving Insurance Card")
                        val ic = InsuranceCard(
                            id = if (documentId != null && typeForLookup == "insurance") documentId else 0,
                            providerName = insuranceState.providerName,
                            planName = insuranceState.planName,
                            type = insuranceState.type,
                            policyNumber = insuranceState.policyNumber,
                            groupNumber = insuranceState.groupNumber.ifBlank { null },
                            memberId = insuranceState.memberId.ifBlank { null },
                            policyHolderName = insuranceState.policyHolderName,
                            expiryDate = insuranceState.expiryDate.ifBlank { null },
                            website = insuranceState.website.ifBlank { null },
                            customerServiceNumber = insuranceState.customerServiceNumber.ifBlank { null },
                            frontImagePath = insuranceState.frontPath,
                            backImagePath = insuranceState.backPath,
                            notes = insuranceState.notes.ifBlank { null }
                        )
                        val job = viewModel.saveInsuranceCard(ic)
                        job.join()
                        println("CardKeeperUI: Insurance Card saved successfully")
                    }
                }
                println("CardKeeperUI: Save complete, navigating back")
                navigateBack(selectedCategory) // Pass the category that was saved
            } catch (e: Exception) {
                println("CardKeeperUI: SAVE FAILED - Exception: ${e.message}")
                e.printStackTrace()
                com.vijay.cardkeeper.ui.common.SnackbarManager.showMessage("Save failed: ${e.message}")
                // Still navigate back even on failure to prevent stuck screen
                navigateBack(selectedCategory)
            }
        }
    }

    val dynamicTitle = if (documentId != null) {
        when (selectedCategory) {
            0 -> "Edit Financial Account"
            1 -> "Edit Identity Document"
            2 -> "Edit Passport"
            3 -> "Edit Rewards Card"
            4 -> "Edit Green Card"
            5 -> "Edit Aadhaar Card"
            6 -> "Edit Gift Card"
            7 -> "Edit PAN Card"
            8 -> "Edit Insurance Card"
            else -> "Edit Item"
        }
    } else {
        when (selectedCategory) {
            0 -> "Add Financial Account"
            1 -> if (identityState.type == DocumentType.DRIVER_LICENSE) "Add Driver License" else "Add Identity Document"
            2 -> "Add Passport"
            3 -> "Add Rewards Card"
            4 -> "Add Green Card"
            5 -> "Add Aadhaar Card"
            6 -> "Add Gift Card"
            7 -> "Add PAN Card"
            8 -> "Add Insurance Card"
            else -> "Add Item"
        }
    }

    Box {
        com.vijay.cardkeeper.ui.item.AddItemScreen(
            financialState = financialState,
            rewardsState = rewardsState,
            identityState = identityState,
            passportState = passportState,
            greenCardState = greenCardState,
            aadharCardState = aadharCardState,
            giftCardState = giftCardState,

            panCardState = panCardState,
            insuranceState = insuranceState,
            selectedCategory = selectedCategory,
            onCategorySelected = { selectedCategory = it },
            onScanRequest = onScanRequest,
            onSave = onSave,
            onNavigateBack = { navigateBack(selectedCategory) },
            isEditing = documentId != null,
            showCategoryTabs = documentId == null && documentType.isNullOrEmpty(),
            title = dynamicTitle,
            snackbarHostState = snackbarHostState
        )

        if (showBarcodeScanner) {
            BarcodeScannerScreen(
                onBarcodeScanned = { barcode, format, _ ->
                    showBarcodeScanner = false
                    when (selectedCategory) {
                        1 -> { // Identity (DL)
                            scope.launch {
                                val details = driverLicenseScanner.parseAAMVAData(barcode)
                                if (details.docNumber.isNotEmpty() || details.name.isNotEmpty()) {
                                    snackbarHostState.showSnackbar("Scanned DL: ${details.name.ifEmpty { "Data Found" }}")
                                } else {
                                    snackbarHostState.showSnackbar("Could not extract DL data. Try again.")
                                }

                                if (details.docNumber.isNotEmpty()) identityState.number = details.docNumber
                                if (details.name.isNotEmpty()) {
                                    identityState.firstName = details.name.substringBefore(" ")
                                    identityState.lastName = details.name.substringAfter(" ", "")
                                }
                                if (details.dob.isNotEmpty()) identityState.rawDob = details.dob.filter { it.isDigit() }
                                if (details.expiryDate.isNotEmpty()) identityState.rawExpiry = details.expiryDate.filter { it.isDigit() }
                                if (details.address.isNotEmpty()) identityState.address = details.address
                                if (details.sex.isNotEmpty()) identityState.sex = details.sex
                                if (details.eyeColor.isNotEmpty()) identityState.eyeColor = details.eyeColor
                                if (details.height.isNotEmpty()) identityState.height = details.height
                                if (details.licenseClass.isNotEmpty()) identityState.licenseClass = details.licenseClass
                                if (details.restrictions.isNotEmpty()) identityState.restrictions = details.restrictions
                                if (details.endorsements.isNotEmpty()) identityState.endorsements = details.endorsements
                                if (details.state.isNotEmpty()) identityState.region = details.state
                                if (details.issuingAuthority.isNotEmpty()) identityState.issuingAuthority = details.issuingAuthority
                            }
                        }
                        0 -> { // Financial
                             // Financial Form no longer supports barcode usage
                        }
                        3 -> { // Rewards
                             rewardsState.barcode = barcode
                             rewardsState.barcodeFormat = format
                        }
                        5 -> { // Aadhaar
                            val result = aadharQrScanner.parse(barcode)
                            if (result.name.isNotEmpty()) {
                                aadharCardState.holderName = result.name
                                aadharCardState.rawDob = result.dob.filter { it.isDigit() }
                                aadharCardState.gender = result.gender
                                aadharCardState.address = result.fullAddress
                                aadharCardState.pincode = result.pincode
                                aadharCardState.maskedAadhaarNumber = result.maskedAadhaar
                                aadharCardState.referenceId = result.referenceId
                                aadharCardState.photoBase64 = result.photoBase64
                                aadharCardState.qrData = barcode
                                aadharCardState.signatureValid = result.signatureValid
                                aadharCardState.email = result.email ?: ""
                                aadharCardState.mobile = result.mobile ?: ""
                            }
                        }
                        6 -> { // Gift Card
                            if (format == com.google.mlkit.vision.barcode.common.Barcode.FORMAT_QR_CODE) {
                                giftCardState.qrCode = barcode
                            } else {
                                giftCardState.barcode = barcode
                                giftCardState.barcodeFormat = format
                            }
                        }
                    }
                },
                onDismiss = { showBarcodeScanner = false }
            )
        }
    }
}


