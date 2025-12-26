package com.vijay.cardkeeper.ui.item.forms

import android.graphics.Bitmap
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.vijay.cardkeeper.data.entity.AccountType
import com.vijay.cardkeeper.data.entity.BankAccountSubType
import com.vijay.cardkeeper.data.entity.FinancialAccount

class FinancialFormState(initialAccount: FinancialAccount?, initialType: AccountType? = null) {
        var type by mutableStateOf(initialAccount?.type ?: initialType ?: AccountType.CREDIT_CARD)
        var institution by mutableStateOf(initialAccount?.institutionName ?: "")
        var accName by mutableStateOf(initialAccount?.accountName ?: "")
        var holder by mutableStateOf(initialAccount?.holderName ?: "")
        var number by mutableStateOf(initialAccount?.number ?: "")
        var routing by mutableStateOf(initialAccount?.routingNumber ?: "")
        var ifsc by mutableStateOf(initialAccount?.ifscCode ?: "")
        var swift by mutableStateOf(initialAccount?.swiftCode ?: "")
        var expiry by mutableStateOf(initialAccount?.expiryDate ?: "")
        var cvv by mutableStateOf(initialAccount?.cvv ?: "")
        var pin by mutableStateOf(initialAccount?.cardPin ?: "")
        var network by mutableStateOf(initialAccount?.cardNetwork ?: "")
        var notes by mutableStateOf(initialAccount?.notes ?: "")
        var contact by mutableStateOf(initialAccount?.lostCardContactNumber ?: "")

        // Bank Account Specifics
        var accountSubType by mutableStateOf(initialAccount?.accountSubType)
        var wireNumber by mutableStateOf(initialAccount?.wireNumber ?: "")
        var branchAddress by mutableStateOf(initialAccount?.branchAddress ?: "")
        var branchContact by mutableStateOf(initialAccount?.branchContactNumber ?: "")
        var bankWebUrl by mutableStateOf(initialAccount?.bankWebUrl ?: "")
        var bankBrandColor by mutableStateOf(initialAccount?.bankBrandColor)
        var holderAddress by mutableStateOf(initialAccount?.holderAddress ?: "")

        // Rewards Specifics
        var barcode by mutableStateOf(initialAccount?.barcode ?: "")
        var barcodeFormat by mutableStateOf(initialAccount?.barcodeFormat)
        var linkedPhone by mutableStateOf(initialAccount?.linkedPhoneNumber ?: "")

        // Images
        var frontPath by mutableStateOf(initialAccount?.frontImagePath)
        var backPath by mutableStateOf(initialAccount?.backImagePath)
        var logoPath by mutableStateOf(initialAccount?.logoImagePath)

        // Bitmaps (Transient)
        var frontBitmap by mutableStateOf<Bitmap?>(null)
        var backBitmap by mutableStateOf<Bitmap?>(null)
        var logoBitmap by mutableStateOf<Bitmap?>(null)

        // UI State
        var cvvVisible by mutableStateOf(false)
        var pinVisible by mutableStateOf(false)
}

@Composable
fun rememberFinancialFormState(
        account: FinancialAccount?,
        initialType: AccountType? = null
): FinancialFormState {
        return remember(account, initialType) { FinancialFormState(account, initialType) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialForm(
        state: FinancialFormState,
        onScanFront: () -> Unit,
        onScanBack: () -> Unit,
        onPickLogo: () -> Unit,
        onSave: () -> Unit,
        onNavigateBack: () -> Unit
) {
        val context = LocalContext.current

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Scan Buttons
                Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                ) {
                        Button(
                                onClick = onScanFront,
                                modifier = Modifier.weight(1f),
                                colors =
                                        if (state.frontBitmap != null)
                                                ButtonDefaults.buttonColors(
                                                        containerColor =
                                                                MaterialTheme.colorScheme
                                                                        .primaryContainer,
                                                        contentColor =
                                                                MaterialTheme.colorScheme
                                                                        .onPrimaryContainer
                                                )
                                        else ButtonDefaults.buttonColors()
                        ) {
                                Column(
                                        horizontalAlignment =
                                                androidx.compose.ui.Alignment.CenterHorizontally
                                ) {
                                        Icon(Icons.Filled.PhotoCamera, "Front")
                                        Text(
                                                when {
                                                        state.frontBitmap != null ->
                                                                if (state.type ==
                                                                                AccountType
                                                                                        .BANK_ACCOUNT
                                                                )
                                                                        "Cheque Captured"
                                                                else "Front Captured"
                                                        state.type == AccountType.BANK_ACCOUNT ->
                                                                "Scan Cheque"
                                                        else -> "Scan Front"
                                                }
                                        )
                                }
                        }
                        // Only show Scan Back for cards, not for bank accounts
                        if (state.type != AccountType.BANK_ACCOUNT) {
                                Button(
                                        onClick = onScanBack,
                                        modifier = Modifier.weight(1f),
                                        colors =
                                                if (state.backBitmap != null)
                                                        ButtonDefaults.buttonColors(
                                                                containerColor =
                                                                        MaterialTheme.colorScheme
                                                                                .primaryContainer,
                                                                contentColor =
                                                                        MaterialTheme.colorScheme
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
                                                        if (state.backBitmap != null)
                                                                "Back Captured"
                                                        else "Scan Back"
                                                )
                                        }
                                }
                        }
                }

                Text("Account Type", style = MaterialTheme.typography.labelLarge)

                // Type Selection
                Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                        listOf(
                                        AccountType.CREDIT_CARD,
                                        AccountType.DEBIT_CARD,
                                        AccountType.BANK_ACCOUNT,
                                        AccountType.REWARDS_CARD
                                )
                                .forEach { type ->
                                        FilterChip(
                                                selected = state.type == type,
                                                onClick = { state.type = type },
                                                label = { Text(type.name.replace("_", " ")) }
                                        )
                                }
                }

                OutlinedTextField(
                        value = state.institution,
                        onValueChange = { state.institution = it },
                        label = {
                                Text(
                                        if (state.type == AccountType.REWARDS_CARD)
                                                "Shop / Program Name"
                                        else "Institution (e.g. Chase)"
                                )
                        },
                        modifier = Modifier.fillMaxWidth()
                )

                // Standard Financial Fields
                if (state.type != AccountType.REWARDS_CARD && state.type != AccountType.BANK_ACCOUNT
                ) {
                        OutlinedTextField(
                                value = state.accName,
                                onValueChange = { state.accName = it },
                                label = { Text("Account Name (e.g. Sapphire)") },
                                modifier = Modifier.fillMaxWidth()
                        )
                }
                if (state.type != AccountType.REWARDS_CARD) {
                        OutlinedTextField(
                                value = state.number,
                                onValueChange = { state.number = it },
                                label = {
                                        Text(
                                                if (state.type == AccountType.BANK_ACCOUNT)
                                                        "Account Number"
                                                else "Card Number"
                                        )
                                },
                                keyboardOptions =
                                        KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                                value = state.holder,
                                onValueChange = { state.holder = it },
                                label = { Text("Account Holder Name") },
                                modifier = Modifier.fillMaxWidth()
                        )
                }

                // Bank Account Specific Fields
                if (state.type == AccountType.BANK_ACCOUNT) {
                        // Account Sub-Type Selector
                        Text("Account Type", style = MaterialTheme.typography.labelMedium)
                        Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                                listOf(
                                                BankAccountSubType.CHECKING,
                                                BankAccountSubType.SAVINGS,
                                                BankAccountSubType.NRE,
                                                BankAccountSubType.NRO,
                                                BankAccountSubType.CURRENT,
                                                BankAccountSubType.MONEY_MARKET,
                                                BankAccountSubType.OTHER
                                        )
                                        .forEach { subType ->
                                                FilterChip(
                                                        selected = state.accountSubType == subType,
                                                        onClick = {
                                                                state.accountSubType = subType
                                                        },
                                                        label = {
                                                                Text(subType.name.replace("_", " "))
                                                        }
                                                )
                                        }
                        }

                        // Core Banking Numbers
                        OutlinedTextField(
                                value = state.routing,
                                onValueChange = { state.routing = it },
                                label = { Text("Routing Number (USA)") },
                                keyboardOptions =
                                        KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                                value = state.wireNumber,
                                onValueChange = { state.wireNumber = it },
                                label = { Text("Wire Transfer Number") },
                                keyboardOptions =
                                        KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                        )

                        // Indian Bank Codes
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                        value = state.ifsc,
                                        onValueChange = { state.ifsc = it.uppercase() },
                                        label = { Text("IFSC Code") },
                                        modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                        value = state.swift,
                                        onValueChange = { state.swift = it.uppercase() },
                                        label = { Text("SWIFT Code") },
                                        modifier = Modifier.weight(1f)
                                )
                        }

                        // Branch Details
                        OutlinedTextField(
                                value = state.branchAddress,
                                onValueChange = { state.branchAddress = it },
                                label = { Text("Branch Address") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2
                        )
                        OutlinedTextField(
                                value = state.branchContact,
                                onValueChange = { state.branchContact = it },
                                label = { Text("Branch Contact Number") },
                                keyboardOptions =
                                        KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                                value = state.bankWebUrl,
                                onValueChange = { state.bankWebUrl = it },
                                label = { Text("Bank Website URL") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                                modifier = Modifier.fillMaxWidth()
                        )

                        // Account Holder Address (from cheque)
                        OutlinedTextField(
                                value = state.holderAddress,
                                onValueChange = { state.holderAddress = it },
                                label = { Text("Account Holder Address") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2
                        )
                }

                // Rewards / Shop Card Fields
                if (state.type == AccountType.REWARDS_CARD) {
                        OutlinedTextField(
                                value = state.barcode,
                                onValueChange = { state.barcode = it },
                                label = { Text("Barcode Number") },
                                trailingIcon = {
                                        IconButton(
                                                onClick = {
                                                        val options =
                                                                GmsBarcodeScannerOptions.Builder()
                                                                        .setBarcodeFormats(
                                                                                Barcode.FORMAT_ALL_FORMATS
                                                                        )
                                                                        .build()
                                                        val scanner =
                                                                GmsBarcodeScanning.getClient(
                                                                        context,
                                                                        options
                                                                )
                                                        scanner.startScan().addOnSuccessListener {
                                                                barcodeRaw ->
                                                                barcodeRaw.rawValue?.let {
                                                                        state.barcode = it
                                                                        state.barcodeFormat =
                                                                                barcodeRaw.format
                                                                }
                                                        }
                                                }
                                        ) { Icon(Icons.Filled.PhotoCamera, "Scan Barcode") }
                                },
                                modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                                value = state.linkedPhone,
                                onValueChange = { state.linkedPhone = it },
                                label = { Text("Linked Phone Number") },
                                keyboardOptions =
                                        KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.fillMaxWidth()
                        )

                        // Logo Picker
                        Button(onClick = onPickLogo) {
                                Text(
                                        if (state.logoPath != null) "Change Shop Logo"
                                        else "Pick Shop Logo"
                                )
                        }
                }

                val isCard =
                        state.type == AccountType.CREDIT_CARD ||
                                state.type == AccountType.DEBIT_CARD

                if (isCard) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                        value = state.expiry,
                                        onValueChange = { state.expiry = it },
                                        label = { Text("Expiry (MM/YY)") },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions =
                                                KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                                OutlinedTextField(
                                        value = state.cvv,
                                        onValueChange = { if (it.length <= 4) state.cvv = it },
                                        label = { Text("CVV/CVC") },
                                        modifier = Modifier.weight(1f),
                                        visualTransformation =
                                                if (state.cvvVisible) VisualTransformation.None
                                                else PasswordVisualTransformation(),
                                        keyboardOptions =
                                                KeyboardOptions(keyboardType = KeyboardType.Number),
                                        trailingIcon = {
                                                IconButton(
                                                        onClick = {
                                                                state.cvvVisible = !state.cvvVisible
                                                        }
                                                ) {
                                                        Icon(
                                                                if (state.cvvVisible)
                                                                        Icons.Filled.Visibility
                                                                else Icons.Filled.VisibilityOff,
                                                                "Toggle CVV"
                                                        )
                                                }
                                        }
                                )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                        value = state.network,
                                        onValueChange = { state.network = it },
                                        label = { Text("Network (e.g. Visa)") },
                                        modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                        value = state.pin,
                                        onValueChange = { state.pin = it },
                                        label = { Text("Card PIN") },
                                        modifier = Modifier.weight(1f),
                                        visualTransformation =
                                                if (state.pinVisible) VisualTransformation.None
                                                else PasswordVisualTransformation(),
                                        keyboardOptions =
                                                KeyboardOptions(
                                                        keyboardType = KeyboardType.NumberPassword
                                                ),
                                        trailingIcon = {
                                                IconButton(
                                                        onClick = {
                                                                state.pinVisible = !state.pinVisible
                                                        }
                                                ) {
                                                        Icon(
                                                                if (state.pinVisible)
                                                                        Icons.Filled.Visibility
                                                                else Icons.Filled.VisibilityOff,
                                                                "Toggle PIN"
                                                        )
                                                }
                                        }
                                )
                        }
                }

                // Lost card contact - only for cards, not bank accounts
                if (state.type == AccountType.CREDIT_CARD || state.type == AccountType.DEBIT_CARD) {
                        OutlinedTextField(
                                value = state.contact,
                                onValueChange = { state.contact = it },
                                label = { Text("Lost Card Contact Number") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                }

                OutlinedTextField(
                        value = state.notes,
                        onValueChange = { state.notes = it },
                        label = { Text("Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                )

                Button(
                        onClick = {
                                onSave()
                                onNavigateBack()
                        },
                        modifier = Modifier.fillMaxWidth()
                ) { Text("Save Financial Account") }
        }
}
