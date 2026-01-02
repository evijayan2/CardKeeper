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

    // Type lookup logic
    val typeForLookup = documentType.takeIf { !it.isNullOrEmpty() }
        ?: when (initialCategory) {
            0 -> "financial"
            1 -> "identity"
            2 -> "passport"
            3 -> "financial" // Rewards Card is stored as FinancialAccount
            4 -> "greencard"
            5 -> "aadhar"
            6 -> "giftcard"
            7 -> "pancard"
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
                    selectedCategory = if (it.type == AccountType.REWARDS_CARD || it.type == AccountType.LIBRARY_CARD) 3 else 0
                }
                is Passport -> selectedCategory = 2
                is GreenCard -> selectedCategory = 4
                is AadharCard -> selectedCategory = 5
                is GiftCard -> selectedCategory = 6
                is PanCard -> selectedCategory = 7
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
    val financialState = rememberFinancialFormState(item as? FinancialAccount, initialAccountType)
    val identityState = rememberIdentityFormState(item as? IdentityDocument, null)
    val passportState = rememberPassportFormState(item as? Passport)
    val greenCardState = rememberGreenCardFormState(item as? GreenCard)
    val aadharCardState = rememberAadharCardFormState(item as? AadharCard)
    val giftCardState = rememberGiftCardFormState(item as? GiftCard)
    val panCardState = rememberPanCardFormState(item as? PanCard)

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

    // State for Camera/Barcode logic
    var scanningBack by remember { mutableStateOf(false) }
    var showBarcodeScanner by remember { mutableStateOf(false) }

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
                        processScanResult(b, path, selectedCategory, scanningBack, 
                            financialState, identityState, passportState, greenCardState, aadharCardState, giftCardState, panCardState,
                            paymentScanner, rewardsScanner, identityScanner, driverLicenseScanner, chequeScanner, passportScanner, greenCardScanner, aadharScanner, panCardScanner, context)
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
                   if (selectedCategory == 0 || selectedCategory == 3) financialState.logoPath = path
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
                    0, 3 -> {
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
                            barcode = financialState.barcode.ifBlank { null },
                            barcodeFormat = financialState.barcodeFormat,
                            linkedPhoneNumber = financialState.linkedPhone.ifBlank { null }
                        )
                        val job = viewModel.saveFinancialAccount(acc)
                        job.join()
                        println("CardKeeperUI: Financial Account saved successfully")
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
                }
                println("CardKeeperUI: Save complete, navigating back")
                navigateBack(selectedCategory) // Pass the category that was saved
            } catch (e: Exception) {
                println("CardKeeperUI: SAVE FAILED - Exception: ${e.message}")
                e.printStackTrace()
                android.widget.Toast.makeText(context, "Save failed: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
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
            else -> "Add Item"
        }
    }

    Box {
        com.vijay.cardkeeper.ui.item.AddItemScreen(
            financialState = financialState,
            identityState = identityState,
            passportState = passportState,
            greenCardState = greenCardState,
            aadharCardState = aadharCardState,
            giftCardState = giftCardState,
            panCardState = panCardState,
            selectedCategory = selectedCategory,
            onCategorySelected = { selectedCategory = it },
            onScanRequest = onScanRequest,
            onSave = onSave,
            onNavigateBack = { navigateBack(selectedCategory) },
            isEditing = documentId != null,
            showCategoryTabs = documentId == null && documentType.isNullOrEmpty(),
            title = dynamicTitle
        )

        if (showBarcodeScanner) {
            BarcodeScannerScreen(
                onBarcodeScanned = { barcode, format, _ ->
                    showBarcodeScanner = false
                    when (selectedCategory) {
                        1 -> { // Identity (DL)
                            scope.launch {
                                val details = driverLicenseScanner.parseAAMVAData(barcode)
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
                        0, 3 -> { // Financial or Rewards
                            financialState.barcode = barcode
                            financialState.barcodeFormat = format
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

private suspend fun processScanResult(
    bitmap: android.graphics.Bitmap,
    path: String,
    category: Int,
    isBack: Boolean,
    financialState: FinancialFormState,
    identityState: IdentityFormState,
    passportState: PassportFormState,
    greenCardState: GreenCardFormState,
    aadharCardState: AadharCardFormState,
    giftCardState: GiftCardFormState,
    panCardState: PanCardFormState,
    paymentScanner: PaymentCardScanner,
    rewardsScanner: RewardsScanner,
    identityScanner: IdentityScanner,
    driverLicenseScanner: DriverLicenseScanner,
    chequeScanner: ChequeScanner,
    passportScanner: PassportScanner,
    greenCardScanner: GreenCardScanner,
    aadharScanner: AadharScanner,
    panCardScanner: PanCardScanner,
    context: android.content.Context
) {
    if (isBack) {
        when (category) {
            0, 3 -> {
                financialState.backPath = path
                financialState.hasBackImage = true
                if (financialState.type == AccountType.REWARDS_CARD || category == 3) {
                    val res = rewardsScanner.scan(bitmap)
                    android.util.Log.d("AddItemRoute", "Rewards Card Back Scan Result: barcode=${res.barcode}, cardNumber=${res.cardNumber}, format=${res.barcodeFormat}, qrCode=${res.qrCode}, shopName=${res.shopName}")
                    
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
                android.util.Log.d("AddItemRoute", "Gift Card Back Scan Result: barcode=${res.barcode}, cardNumber=${res.cardNumber}, format=${res.barcodeFormat}, qrCode=${res.qrCode}, shopName=${res.shopName}")
                
                res.barcodeFormat?.let { giftCardState.barcodeFormat = it }
            }
            7 -> {
                panCardState.backPath = path
                panCardState.hasBackImage = true
                // Optionally scan back for any details, but usually front has all content.
                // If back has address/QR, we could scan it here.
                // For now, just save the image.
            }
        }
    } else {
        when (category) {
            0, 3 -> {
                financialState.frontPath = path
                financialState.hasFrontImage = true
                if (financialState.type == AccountType.REWARDS_CARD || category == 3) {
                    val res = rewardsScanner.scan(bitmap)
                    android.util.Log.d("AddItemRoute", "Rewards Card Front Scan Result: barcode=${res.barcode}, cardNumber=${res.cardNumber}, format=${res.barcodeFormat}, qrCode=${res.qrCode}, shopName=${res.shopName}")
                    
                    // Prioritize actual barcode for the barcode field, use OCR number as fallback if barcode is null or better
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
                    // Aadhar scanner returns DD/MM/YYYY
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
                android.util.Log.d("AddItemRoute", "Gift Card Front Scan Result: barcode=${res.barcode}, cardNumber=${res.cardNumber}, format=${res.barcodeFormat}, qrCode=${res.qrCode}, shopName=${res.shopName}")
                
                // Separate fields for Gift Cards - allow overwrite if new detection is longer
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
