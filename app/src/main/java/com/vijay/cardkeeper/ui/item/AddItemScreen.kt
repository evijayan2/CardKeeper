package com.vijay.cardkeeper.ui.item

import android.content.Context
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vijay.cardkeeper.data.entity.AccountType
import com.vijay.cardkeeper.data.entity.DocumentType
import com.vijay.cardkeeper.ui.viewmodel.AddItemViewModel
import com.vijay.cardkeeper.ui.viewmodel.AppViewModelProvider
import com.vijay.cardkeeper.util.CardTextAnalyzer
import com.vijay.cardkeeper.util.IdentityTextAnalyzer
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(
        navigateBack: () -> Unit,
        initialCategory: Int = 0, // 0 for financial, 1 for identity
        documentId: Int? = null,
        viewModel: AddItemViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
        val context = LocalContext.current
        var selectedCategory by remember {
                mutableIntStateOf(initialCategory)
        } // 0 = Financial, 1 = Identity
        val categories = listOf("Financial", "Identity")

        // Financial State
        var finType by remember { mutableStateOf(AccountType.CREDIT_CARD) }
        var institution by remember { mutableStateOf("") }
        var accName by remember { mutableStateOf("") }
        var accHolder by remember { mutableStateOf("") }
        var accNumber by remember { mutableStateOf("") }
        var routing by remember { mutableStateOf("") }
        var ifsc by remember { mutableStateOf("") }

        // New Fields
        var expiry by remember { mutableStateOf("") }
        var cvv by remember { mutableStateOf("") }
        var cardPin by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }
        var contactNumber by remember { mutableStateOf("") }
        var cardNetwork by remember { mutableStateOf("") }

        // UI State
        var cvvVisible by remember { mutableStateOf(false) }

        // Identity State
        var docType by remember { mutableStateOf(DocumentType.PASSPORT) }
        var country by remember { mutableStateOf("USA") }
        var docNumber by remember { mutableStateOf("") }
        var docHolder by remember { mutableStateOf("") }
        var expiryStr by remember { mutableStateOf("") } // Simplify date input for now

        // Detailed Identity Fields
        var state by remember { mutableStateOf("") }
        var address by remember { mutableStateOf("") }
        var dob by remember { mutableStateOf("") }
        var sex by remember { mutableStateOf("") }
        var eyeColor by remember { mutableStateOf("") }
        var height by remember { mutableStateOf("") }
        var licenseClass by remember { mutableStateOf("") }
        var restrictions by remember { mutableStateOf("") }
        var endorsements by remember { mutableStateOf("") }
        var issuingAuthority by remember { mutableStateOf("") }

        // Identity Images
        var frontImage by remember { mutableStateOf<Bitmap?>(null) }
        var backImage by remember { mutableStateOf<Bitmap?>(null) }
        var frontPath by remember { mutableStateOf<String?>(null) }
        var backPath by remember { mutableStateOf<String?>(null) }
        var scanningBack by remember { mutableStateOf(false) } // true if scanning back side

        // Load existing document if editing
        LaunchedEffect(documentId) {
                if (documentId != null && documentId > 0) {
                        if (selectedCategory == 1) { // Identity
                                val doc = viewModel.getIdentityDocument(documentId)
                                if (doc != null) {
                                        docType = doc.type
                                        country = doc.country
                                        docNumber = doc.docNumber
                                        docHolder = doc.holderName
                                        expiryStr = doc.expiryDate?.toString() ?: ""
                                        state = doc.state ?: ""
                                        address = doc.address ?: ""
                                        dob = doc.dob ?: ""
                                        sex = doc.sex ?: ""
                                        eyeColor = doc.eyeColor ?: ""
                                        height = doc.height ?: ""
                                        licenseClass = doc.licenseClass ?: ""
                                        restrictions = doc.restrictions ?: ""
                                        endorsements = doc.endorsements ?: ""
                                        issuingAuthority = doc.issuingAuthority ?: ""
                                        frontPath = doc.frontImagePath
                                        backPath = doc.backImagePath
                                }
                        } else { // Financial (Using documentId as accountId here for simplicity)
                                val acc = viewModel.getFinancialAccount(documentId)
                                if (acc != null) {
                                        finType = acc.type
                                        institution = acc.institutionName
                                        accName = acc.accountName
                                        accHolder = acc.holderName
                                        accNumber = acc.number
                                        routing = acc.routingNumber ?: ""
                                        ifsc = acc.ifscCode ?: ""
                                        expiry = acc.expiryDate ?: ""
                                        cvv = acc.cvv ?: ""
                                        cardPin = acc.cardPin ?: ""
                                        notes = acc.notes ?: ""
                                        contactNumber = acc.lostCardContactNumber ?: ""
                                        cardNetwork = acc.cardNetwork ?: ""
                                        frontPath = acc.frontImagePath
                                        backPath = acc.backImagePath
                                }
                        }
                }
        }

        // Scanner callback with OCR
        val cardAnalyzer = remember { CardTextAnalyzer { /* No-op */} }
        val identityAnalyzer = remember { IdentityTextAnalyzer(false) { /* No-op */} }

        val scannerLauncher =
                rememberLauncherForActivityResult(
                        contract =
                                androidx.activity.result.contract.ActivityResultContracts
                                        .StartIntentSenderForResult()
                ) { result ->
                        if (result.resultCode == android.app.Activity.RESULT_OK) {
                                val resultData =
                                        com.google.mlkit.vision.documentscanner
                                                .GmsDocumentScanningResult.fromActivityResultIntent(
                                                result.data
                                        )
                                resultData?.pages?.let { pages ->
                                        if (pages.isNotEmpty()) {
                                                val page = pages[0]
                                                val imageUri = page.imageUri

                                                // Load Bitmap
                                                val bitmap =
                                                        if (android.os.Build.VERSION.SDK_INT < 28) {
                                                                @Suppress("DEPRECATION")
                                                                android.provider.MediaStore.Images
                                                                        .Media.getBitmap(
                                                                        context.contentResolver,
                                                                        imageUri
                                                                )
                                                        } else {
                                                                val source =
                                                                        android.graphics
                                                                                .ImageDecoder
                                                                                .createSource(
                                                                                        context.contentResolver,
                                                                                        imageUri
                                                                                )
                                                                android.graphics.ImageDecoder
                                                                        .decodeBitmap(source)
                                                        }

                                                // Save Image
                                                val savedPath =
                                                        saveImageToInternalStorage(
                                                                context,
                                                                bitmap,
                                                                "scan_${System.currentTimeMillis()}"
                                                        )

                                                if (selectedCategory == 0) { // Financial
                                                        if (scanningBack) {
                                                                backImage = bitmap
                                                                backPath = savedPath
                                                        } else {
                                                                frontImage = bitmap
                                                                frontPath = savedPath

                                                                // Run OCR on Front Image for
                                                                // Financial
                                                                val inputImage =
                                                                        com.google.mlkit.vision
                                                                                .common.InputImage
                                                                                .fromBitmap(
                                                                                        bitmap,
                                                                                        0
                                                                                )
                                                                cardAnalyzer.analyze(inputImage) {
                                                                        details ->
                                                                        // Populate Fields if empty
                                                                        if (accNumber.isEmpty())
                                                                                accNumber =
                                                                                        details.number
                                                                        if (expiry.isEmpty() &&
                                                                                        details.expiryDate
                                                                                                .isNotEmpty()
                                                                        )
                                                                                expiry =
                                                                                        details.expiryDate
                                                                        if (accHolder.isEmpty() &&
                                                                                        details.ownerName
                                                                                                .isNotEmpty()
                                                                        )
                                                                                accHolder =
                                                                                        details.ownerName
                                                                        if (institution.isEmpty() &&
                                                                                        details.bankName
                                                                                                .isNotEmpty()
                                                                        )
                                                                                institution =
                                                                                        details.bankName
                                                                        if (cardNetwork.isEmpty() &&
                                                                                        details.scheme
                                                                                                .isNotEmpty()
                                                                        )
                                                                                cardNetwork =
                                                                                        details.scheme
                                                                        if (details.cardType.equals(
                                                                                        "Debit",
                                                                                        ignoreCase =
                                                                                                true
                                                                                )
                                                                        )
                                                                                finType =
                                                                                        AccountType
                                                                                                .DEBIT_CARD
                                                                }
                                                        }
                                                } else { // Identity
                                                        if (scanningBack) {
                                                                backImage = bitmap
                                                                backPath = savedPath
                                                                // Run Back OCR
                                                                val identityAnalyzerBack =
                                                                        IdentityTextAnalyzer(
                                                                                true
                                                                        ) { /* no-op */}
                                                                val inputImage =
                                                                        com.google.mlkit.vision
                                                                                .common.InputImage
                                                                                .fromBitmap(
                                                                                        bitmap,
                                                                                        0
                                                                                )
                                                                identityAnalyzerBack.analyze(
                                                                        inputImage
                                                                ) { details ->
                                                                        // Append
                                                                        // raw text
                                                                        // or
                                                                        // specific
                                                                        // back-side
                                                                        // fields
                                                                        // if any
                                                                        // (currently raw
                                                                        // text)
                                                                        if (notes.isEmpty() &&
                                                                                        details.rawText
                                                                                                .isNotEmpty()
                                                                        )
                                                                                notes =
                                                                                        details.rawText
                                                                }
                                                        } else {
                                                                frontImage = bitmap
                                                                frontPath = savedPath

                                                                // Run Front OCR
                                                                val inputImage =
                                                                        com.google.mlkit.vision
                                                                                .common.InputImage
                                                                                .fromBitmap(
                                                                                        bitmap,
                                                                                        0
                                                                                )
                                                                identityAnalyzer.analyze(
                                                                        inputImage
                                                                ) { details ->
                                                                        if (docNumber.isEmpty())
                                                                                docNumber =
                                                                                        details.docNumber
                                                                        if (docHolder.isEmpty())
                                                                                docHolder =
                                                                                        details.name
                                                                        if (expiryStr.isEmpty())
                                                                                expiryStr =
                                                                                        details.expiryDate
                                                                        if (dob.isEmpty())
                                                                                dob = details.dob
                                                                        if (state.isEmpty())
                                                                                state =
                                                                                        details.state
                                                                        if (address.isEmpty())
                                                                                address =
                                                                                        details.address
                                                                                                ?: ""
                                                                        if (sex.isEmpty())
                                                                                sex = details.sex
                                                                        if (height.isEmpty())
                                                                                height =
                                                                                        details.height
                                                                        if (licenseClass.isEmpty())
                                                                                licenseClass =
                                                                                        details.licenseClass
                                                                        if (restrictions.isEmpty())
                                                                                restrictions =
                                                                                        details.restrictions
                                                                        if (endorsements.isEmpty())
                                                                                endorsements =
                                                                                        details.endorsements
                                                                        if (issuingAuthority
                                                                                        .isEmpty()
                                                                        )
                                                                                issuingAuthority =
                                                                                        details.issuingAuthority
                                                                }
                                                        }
                                                }
                                        }
                                }
                        }
                }

        fun scanDocument(isBack: Boolean) {
                scanningBack = isBack
                val options =
                        com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.Builder()
                                .setGalleryImportAllowed(true)
                                .setPageLimit(1)
                                .setResultFormats(
                                        com.google.mlkit.vision.documentscanner
                                                .GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
                                )
                                .setScannerMode(
                                        com.google.mlkit.vision.documentscanner
                                                .GmsDocumentScannerOptions.SCANNER_MODE_FULL
                                )
                                .build()

                val scanner =
                        com.google.mlkit.vision.documentscanner.GmsDocumentScanning.getClient(
                                options
                        )
                scanner.getStartScanIntent(context as android.app.Activity)
                        .addOnSuccessListener { intentSender ->
                                scannerLauncher.launch(
                                        androidx.activity.result.IntentSenderRequest.Builder(
                                                        intentSender
                                                )
                                                .build()
                                )
                        }
                        .addOnFailureListener {
                                // Handle error
                        }
        }

        val screenTitle =
                if (documentId != null && documentId > 0) {
                        val typeName =
                                if (selectedCategory == 0) {
                                        finType.name.replace("_", " ")
                                } else {
                                        docType.name.replace("_", " ")
                                }
                        "Update $typeName"
                } else {
                        "Add New Item"
                }

        Scaffold(
                topBar = {
                        TopAppBar(
                                title = { Text(screenTitle) },
                                navigationIcon = {
                                        IconButton(onClick = navigateBack) {
                                                Icon(Icons.Filled.ArrowBack, "Back")
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
                        // Category Switcher
                        TabRow(selectedTabIndex = selectedCategory) {
                                categories.forEachIndexed { index, title ->
                                        Tab(
                                                selected = selectedCategory == index,
                                                onClick = { selectedCategory = index },
                                                text = { Text(title) }
                                        )
                                }
                        }

                        if (selectedCategory == 0) {
                                // FINANCIAL FORM

                                // Scan Buttons
                                Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                ) {
                                        Button(
                                                onClick = { scanDocument(false) },
                                                modifier = Modifier.weight(1f),
                                                colors =
                                                        if (frontImage != null)
                                                                ButtonDefaults.buttonColors(
                                                                        containerColor =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .primaryContainer,
                                                                        contentColor =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .onPrimaryContainer
                                                                )
                                                        else ButtonDefaults.buttonColors()
                                        ) {
                                                Column(
                                                        horizontalAlignment =
                                                                androidx.compose.ui.Alignment
                                                                        .CenterHorizontally
                                                ) {
                                                        Icon(Icons.Filled.PhotoCamera, "Front")
                                                        Text(
                                                                if (frontImage != null)
                                                                        "Front Captured"
                                                                else "Scan Front"
                                                        )
                                                }
                                        }
                                        Button(
                                                onClick = { scanDocument(true) },
                                                modifier = Modifier.weight(1f),
                                                colors =
                                                        if (backImage != null)
                                                                ButtonDefaults.buttonColors(
                                                                        containerColor =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .primaryContainer,
                                                                        contentColor =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .onPrimaryContainer
                                                                )
                                                        else ButtonDefaults.buttonColors()
                                        ) {
                                                Column(
                                                        horizontalAlignment =
                                                                androidx.compose.ui.Alignment
                                                                        .CenterHorizontally
                                                ) {
                                                        Icon(Icons.Filled.PhotoCamera, "Back")
                                                        Text(
                                                                if (backImage != null)
                                                                        "Back Captured"
                                                                else "Scan Back"
                                                        )
                                                }
                                        }
                                }

                                Text("Account Type", style = MaterialTheme.typography.labelLarge)

                                // Card vs Bank Selection
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        // Added DEBIT_CARD to the list
                                        listOf(
                                                        AccountType.CREDIT_CARD,
                                                        AccountType.DEBIT_CARD,
                                                        AccountType.BANK_ACCOUNT
                                                )
                                                .forEach { type ->
                                                        FilterChip(
                                                                selected = finType == type,
                                                                onClick = { finType = type },
                                                                label = {
                                                                        Text(
                                                                                type.name.replace(
                                                                                        "_",
                                                                                        " "
                                                                                )
                                                                        )
                                                                }
                                                        )
                                                }
                                }

                                OutlinedTextField(
                                        value = institution,
                                        onValueChange = { institution = it },
                                        label = { Text("Institution (e.g. Chase)") },
                                        modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                        value = accName,
                                        onValueChange = { accName = it },
                                        label = { Text("Account Name (e.g. Sapphire)") },
                                        modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                        value = accNumber,
                                        onValueChange = { accNumber = it },
                                        label = { Text("Account / Card Number") },
                                        keyboardOptions =
                                                KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth()
                                )

                                // Conditional Fields based on Type
                                val isCard =
                                        finType == AccountType.CREDIT_CARD ||
                                                finType == AccountType.DEBIT_CARD

                                if (isCard) {
                                        // Expiry, CVV, and Network Row
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                OutlinedTextField(
                                                        value = expiry,
                                                        onValueChange = { expiry = it },
                                                        label = { Text("Expiry (MM/YY)") },
                                                        modifier = Modifier.weight(1f),
                                                        keyboardOptions =
                                                                KeyboardOptions(
                                                                        keyboardType =
                                                                                KeyboardType.Number
                                                                )
                                                )
                                                OutlinedTextField(
                                                        value = cvv,
                                                        onValueChange = {
                                                                if (it.length <= 4) cvv = it
                                                        },
                                                        label = { Text("CVV/CVC") },
                                                        modifier = Modifier.weight(1f),
                                                        visualTransformation =
                                                                if (cvvVisible)
                                                                        VisualTransformation.None
                                                                else PasswordVisualTransformation(),
                                                        keyboardOptions =
                                                                KeyboardOptions(
                                                                        keyboardType =
                                                                                KeyboardType.Number
                                                                ),
                                                        trailingIcon = {
                                                                val image =
                                                                        if (cvvVisible)
                                                                                Icons.Filled
                                                                                        .Visibility
                                                                        else
                                                                                Icons.Filled
                                                                                        .VisibilityOff
                                                                IconButton(
                                                                        onClick = {
                                                                                cvvVisible =
                                                                                        !cvvVisible
                                                                        }
                                                                ) {
                                                                        Icon(
                                                                                imageVector = image,
                                                                                contentDescription =
                                                                                        "Toggle CVV"
                                                                        )
                                                                }
                                                        }
                                                )
                                        }

                                        // Card PIN and Network
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                // Network (Auto-filled but editable)
                                                OutlinedTextField(
                                                        value = cardNetwork, // Need to add
                                                        // this
                                                        // state
                                                        onValueChange = { cardNetwork = it },
                                                        label = { Text("Network (e.g. Visa)") },
                                                        modifier = Modifier.weight(1f)
                                                )

                                                // PIN with Toggle
                                                // better hoist
                                                var showPin by remember { mutableStateOf(false) }

                                                OutlinedTextField(
                                                        value = cardPin,
                                                        onValueChange = { cardPin = it },
                                                        label = { Text("Card PIN") },
                                                        modifier = Modifier.weight(1f),
                                                        visualTransformation =
                                                                if (showPin)
                                                                        VisualTransformation.None
                                                                else PasswordVisualTransformation(),
                                                        keyboardOptions =
                                                                KeyboardOptions(
                                                                        keyboardType =
                                                                                KeyboardType
                                                                                        .NumberPassword
                                                                ),
                                                        trailingIcon = {
                                                                val image =
                                                                        if (showPin)
                                                                                Icons.Filled
                                                                                        .Visibility
                                                                        else
                                                                                Icons.Filled
                                                                                        .VisibilityOff
                                                                IconButton(
                                                                        onClick = {
                                                                                showPin = !showPin
                                                                        }
                                                                ) {
                                                                        Icon(
                                                                                imageVector = image,
                                                                                contentDescription =
                                                                                        "Toggle PIN"
                                                                        )
                                                                }
                                                        }
                                                )
                                        }
                                } else {
                                        // Bank Specifics
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                OutlinedTextField(
                                                        value = routing,
                                                        onValueChange = { routing = it },
                                                        label = { Text("Routing (Optional)") },
                                                        modifier = Modifier.weight(1f),
                                                        keyboardOptions =
                                                                KeyboardOptions(
                                                                        keyboardType =
                                                                                KeyboardType.Number
                                                                )
                                                )
                                                OutlinedTextField(
                                                        value = ifsc,
                                                        onValueChange = { ifsc = it },
                                                        label = { Text("IFSC (Optional)") },
                                                        modifier = Modifier.weight(1f)
                                                )
                                        }
                                }

                                OutlinedTextField(
                                        value = accHolder,
                                        onValueChange = { accHolder = it },
                                        label = { Text("Holder Name") },
                                        modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                        value = contactNumber,
                                        onValueChange = { contactNumber = it },
                                        label = { Text("Lost Card Contact Number") },
                                        modifier = Modifier.fillMaxWidth(),
                                        keyboardOptions =
                                                KeyboardOptions(keyboardType = KeyboardType.Phone)
                                )

                                OutlinedTextField(
                                        value = notes,
                                        onValueChange = { notes = it },
                                        label = { Text("Notes") },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 3,
                                        maxLines = 5
                                )

                                // Scanner callback with OCR

                                Button(
                                        onClick = {
                                                viewModel.saveFinancialAccount(
                                                        id = documentId ?: 0,
                                                        type = finType,
                                                        institution = institution,
                                                        name = accName,
                                                        holder = accHolder,
                                                        number = accNumber,
                                                        routing = routing,
                                                        ifsc = ifsc,
                                                        expiryDate = expiry,
                                                        cvv = cvv,
                                                        pin = cardPin,
                                                        notes = notes,
                                                        contact = contactNumber,
                                                        cardNetwork = cardNetwork,
                                                        frontImagePath = frontPath,
                                                        backImagePath = backPath
                                                )
                                                navigateBack()
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                ) { Text("Save Financial Account") }
                        } else {
                                // IDENTITY FORM (Unchanged)
                                Text("Document Type", style = MaterialTheme.typography.labelLarge)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf(DocumentType.PASSPORT, DocumentType.DRIVER_LICENSE)
                                                .forEach { type ->
                                                        FilterChip(
                                                                selected = docType == type,
                                                                onClick = { docType = type },
                                                                label = {
                                                                        Text(
                                                                                type.name.replace(
                                                                                        "_",
                                                                                        " "
                                                                                )
                                                                        )
                                                                }
                                                        )
                                                }
                                }

                                // Scan Buttons
                                Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                ) {
                                        Button(
                                                onClick = { scanDocument(false) },
                                                modifier = Modifier.weight(1f),
                                                colors =
                                                        if (frontImage != null)
                                                                ButtonDefaults.buttonColors(
                                                                        containerColor =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .primaryContainer,
                                                                        contentColor =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .onPrimaryContainer
                                                                )
                                                        else ButtonDefaults.buttonColors()
                                        ) {
                                                Column(
                                                        horizontalAlignment =
                                                                androidx.compose.ui.Alignment
                                                                        .CenterHorizontally
                                                ) {
                                                        Icon(Icons.Filled.PhotoCamera, "Front")
                                                        Text(
                                                                if (frontImage != null)
                                                                        "Front Captured"
                                                                else "Scan Front"
                                                        )
                                                }
                                        }
                                        Button(
                                                onClick = { scanDocument(true) },
                                                modifier = Modifier.weight(1f),
                                                colors =
                                                        if (backImage != null)
                                                                ButtonDefaults.buttonColors(
                                                                        containerColor =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .primaryContainer,
                                                                        contentColor =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .onPrimaryContainer
                                                                )
                                                        else ButtonDefaults.buttonColors()
                                        ) {
                                                Column(
                                                        horizontalAlignment =
                                                                androidx.compose.ui.Alignment
                                                                        .CenterHorizontally
                                                ) {
                                                        Icon(Icons.Filled.PhotoCamera, "Back")
                                                        Text(
                                                                if (backImage != null)
                                                                        "Back Captured"
                                                                else "Scan Back"
                                                        )
                                                }
                                        }
                                }

                                OutlinedTextField(
                                        value = country,
                                        onValueChange = { country = it },
                                        label = { Text("Issuing Country") },
                                        modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                        value = docNumber,
                                        onValueChange = { docNumber = it },
                                        label = { Text("Document Number") },
                                        modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                        value = docHolder,
                                        onValueChange = { docHolder = it },
                                        label = { Text("Holder Name") },
                                        modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                        value = expiryStr,
                                        onValueChange = { expiryStr = it },
                                        label = { Text("Expiry (YYYY-MM-DD)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        supportingText = { Text("Leave empty if none") }
                                )

                                // Detailed Fields
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                                value = state,
                                                onValueChange = { state = it },
                                                label = { Text("State") },
                                                modifier = Modifier.weight(1f)
                                        )
                                        OutlinedTextField(
                                                value = dob,
                                                onValueChange = { dob = it },
                                                label = { Text("DOB") },
                                                modifier = Modifier.weight(1f)
                                        )
                                }
                                OutlinedTextField(
                                        value = address,
                                        onValueChange = { address = it },
                                        label = { Text("Address") },
                                        modifier = Modifier.fillMaxWidth()
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                                value = sex,
                                                onValueChange = { sex = it },
                                                label = { Text("Sex") },
                                                modifier = Modifier.weight(1f)
                                        )
                                        OutlinedTextField(
                                                value = height,
                                                onValueChange = { height = it },
                                                label = { Text("Height") },
                                                modifier = Modifier.weight(1f)
                                        )
                                        OutlinedTextField(
                                                value = eyeColor,
                                                onValueChange = { eyeColor = it },
                                                label = { Text("Eyes") },
                                                modifier = Modifier.weight(1f)
                                        )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                                value = licenseClass,
                                                onValueChange = { licenseClass = it },
                                                label = { Text("Class") },
                                                modifier = Modifier.weight(1f)
                                        )
                                        OutlinedTextField(
                                                value = restrictions,
                                                onValueChange = { restrictions = it },
                                                label = { Text("Restr") },
                                                modifier = Modifier.weight(1f)
                                        )
                                        OutlinedTextField(
                                                value = endorsements,
                                                onValueChange = { endorsements = it },
                                                label = { Text("Endorse") },
                                                modifier = Modifier.weight(1f)
                                        )
                                }
                                OutlinedTextField(
                                        value = issuingAuthority,
                                        onValueChange = { issuingAuthority = it },
                                        label = { Text("Issued By (ISS)") },
                                        modifier = Modifier.fillMaxWidth()
                                )

                                Button(
                                        onClick = {
                                                viewModel.saveIdentityDocument(
                                                        id = documentId ?: 0,
                                                        type = docType,
                                                        country = country,
                                                        docNumber,
                                                        docHolder,
                                                        null,
                                                        frontPath,
                                                        backPath,
                                                        state,
                                                        address,
                                                        dob,
                                                        sex,
                                                        eyeColor,
                                                        height,
                                                        licenseClass,
                                                        restrictions,
                                                        endorsements,
                                                        issuingAuthority
                                                )
                                                navigateBack()
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                ) { Text("Save Identity Document") }
                        }
                }
        }
}

fun saveImageToInternalStorage(context: Context, bitmap: Bitmap, name: String): String {
        val directory = context.getDir("card_images", Context.MODE_PRIVATE)
        val file = File(directory, "$name.jpg")
        FileOutputStream(file).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        }
        return file.absolutePath
}
