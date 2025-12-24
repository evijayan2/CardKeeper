package com.vijay.cardkeeper.ui.item

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vijay.cardkeeper.data.entity.AccountType
import com.vijay.cardkeeper.data.entity.DocumentType
import com.vijay.cardkeeper.ui.camera.CameraPreview
import com.vijay.cardkeeper.ui.viewmodel.AddItemViewModel
import com.vijay.cardkeeper.ui.viewmodel.AppViewModelProvider
import com.vijay.cardkeeper.util.CardTextAnalyzer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(
        navigateBack: () -> Unit,
        viewModel: AddItemViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
        var selectedCategory by remember { mutableStateOf(0) } // 0 = Financial, 1 = Identity
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

        // Camera State
        var showCamera by remember { mutableStateOf(false) }
        val permissionLauncher =
                rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
                        isGranted ->
                        if (isGranted) {
                                showCamera = true
                        }
                }

        Scaffold(
                topBar = {
                        TopAppBar(
                                title = { Text("Add New Item") },
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
                                        modifier = Modifier.fillMaxWidth(),
                                        trailingIcon = {
                                                IconButton(
                                                        onClick = {
                                                                permissionLauncher.launch(
                                                                        android.Manifest.permission
                                                                                .CAMERA
                                                                )
                                                        }
                                                ) { Icon(Icons.Filled.Add, "Scan Card") }
                                        }
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
                                                        value = cardNetwork, // Need to add this
                                                        // state
                                                        onValueChange = { cardNetwork = it },
                                                        label = { Text("Network (e.g. Visa)") },
                                                        modifier = Modifier.weight(1f)
                                                )

                                                // PIN with Toggle
                                                val pinVisible by remember {
                                                        mutableStateOf(false)
                                                } // Local state for this block usually tricky,
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

                                Button(
                                        onClick = {
                                                viewModel.saveFinancialAccount(
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
                                                        cardNetwork = cardNetwork
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

                                Button(
                                        onClick = {
                                                viewModel.saveIdentityDocument(
                                                        docType,
                                                        country,
                                                        docNumber,
                                                        docHolder,
                                                        null
                                                )
                                                navigateBack()
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                ) { Text("Save Identity Document") }
                        }
                }
        }

        if (showCamera) {
                Dialog(
                        onDismissRequest = { showCamera = false },
                        properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                                CameraPreview(
                                        modifier = Modifier.fillMaxSize(),
                                        analyzer =
                                                CardTextAnalyzer { details ->
                                                        // Auto-fill from details
                                                        accNumber = details.number
                                                        if (details.expiryDate.isNotEmpty())
                                                                expiry = details.expiryDate
                                                        if (details.ownerName.isNotEmpty())
                                                                accHolder = details.ownerName
                                                        if (details.bankName.isNotEmpty())
                                                                institution = details.bankName
                                                        if (details.scheme.isNotEmpty())
                                                                cardNetwork = details.scheme

                                                        // Infer type
                                                        if (details.cardType.equals(
                                                                        "Debit",
                                                                        ignoreCase = true
                                                                )
                                                        ) {
                                                                finType = AccountType.DEBIT_CARD
                                                        } else {
                                                                finType = AccountType.CREDIT_CARD
                                                        }

                                                        showCamera = false
                                                }
                                )
                                Button(
                                        onClick = { showCamera = false },
                                        modifier =
                                                Modifier.align(
                                                                androidx.compose.ui.Alignment
                                                                        .BottomCenter
                                                        )
                                                        .padding(32.dp)
                                ) { Text("Cancel") }
                        }
                }
        }
}
