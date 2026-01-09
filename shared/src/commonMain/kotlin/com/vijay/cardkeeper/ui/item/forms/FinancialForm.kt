package com.vijay.cardkeeper.ui.item.forms

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.vijay.cardkeeper.data.entity.AccountType
import com.vijay.cardkeeper.data.entity.BankAccountSubType
import com.vijay.cardkeeper.data.entity.FinancialAccount
import com.vijay.cardkeeper.data.entity.RewardCard
import com.vijay.cardkeeper.ui.common.DateVisualTransformation
import com.vijay.cardkeeper.ui.common.MonthYearVisualTransformation
import com.vijay.cardkeeper.ui.common.CardKeeperTextField
import com.vijay.cardkeeper.ui.common.CardKeeperScanButtons
import com.vijay.cardkeeper.ui.common.CardKeeperSaveButton

class FinancialFormState(
    initialAccount: FinancialAccount? = null,
    initialType: AccountType? = null
) {
    var type by mutableStateOf(initialAccount?.type ?: initialType ?: AccountType.CREDIT_CARD)
    var institution by mutableStateOf(initialAccount?.institutionName ?: "")
    var accName by mutableStateOf(initialAccount?.accountName ?: "")
    var holder by mutableStateOf(initialAccount?.holderName ?: "")
    var number by mutableStateOf(initialAccount?.number ?: "")
    var routing by mutableStateOf(initialAccount?.routingNumber ?: "")
    var ifsc by mutableStateOf(initialAccount?.ifscCode ?: "")
    var swift by mutableStateOf(initialAccount?.swiftCode ?: "")
    private var _expiry = mutableStateOf(initialAccount?.expiryDate?.filter { it.isDigit() }?.take(4) ?: "")
    var expiry: String
        get() = _expiry.value
        set(value) {
            val digits = value.filter { it.isDigit() }.take(4)
            _expiry.value = digits
            if (digits.length >= 2) {
                val month = digits.take(2).toIntOrNull() ?: 0
                expiryError = month < 1 || month > 12
            } else {
                expiryError = false
            }
        }
    val formattedExpiry: String
        get() = if (_expiry.value.length >= 2) {
            _expiry.value.substring(0, 2) + "/" + _expiry.value.substring(2)
        } else {
            _expiry.value
        }

    var expiryError by mutableStateOf(false)
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

    // Images using Path check or boolean flag
    var frontPath by mutableStateOf(initialAccount?.frontImagePath)
    var backPath by mutableStateOf(initialAccount?.backImagePath)
    var logoPath by mutableStateOf(initialAccount?.logoImagePath)

    // Bitmaps removed, using flags for UI state
    var hasFrontImage by mutableStateOf(initialAccount?.frontImagePath != null)
    var hasBackImage by mutableStateOf(initialAccount?.backImagePath != null)
    var hasLogoImage by mutableStateOf(initialAccount?.logoImagePath != null)

    // UI State
    var cvvVisible by mutableStateOf(false)
    var pinVisible by mutableStateOf(false)
}

@Composable
fun rememberFinancialFormState(
    account: FinancialAccount? = null,
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
        onSave: () -> Unit,
        onNavigateBack: () -> Unit
) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                val expiryDateVisualTransformation = remember { MonthYearVisualTransformation() }
                
                // Scan Buttons
                val frontLabel = if (state.type == AccountType.BANK_ACCOUNT) "Scan Cheque" else "Scan Front"
                val frontCapturedLabel = if (state.type == AccountType.BANK_ACCOUNT) "Cheque Captured" else "Front Captured"

                CardKeeperScanButtons(
                    hasFrontImage = state.hasFrontImage,
                    onScanFront = onScanFront,
                    frontLabel = frontLabel,
                    frontCapturedLabel = frontCapturedLabel,
                    hasBackImage = state.hasBackImage,
                    onScanBack = onScanBack,
                    showBack = state.type != AccountType.BANK_ACCOUNT
                )

                Text("Account Type", style = MaterialTheme.typography.labelLarge)

                // Type Selection
                Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                        val displayedTypes = listOf(
                                AccountType.CREDIT_CARD,
                                AccountType.DEBIT_CARD,
                                AccountType.BANK_ACCOUNT
                        )

                        displayedTypes.forEach { type ->
                                FilterChip(
                                        selected = state.type == type,
                                        onClick = { state.type = type },
                                        label = { Text(type.name.replace("_", " ")) }
                                )
                        }
                }

                CardKeeperTextField(
                        value = state.institution,
                        onValueChange = { state.institution = it },
                        label = "Institution (e.g. Chase)"
                )

                // Standard Financial Fields
                if (state.type != AccountType.BANK_ACCOUNT) {
                        CardKeeperTextField(
                                value = state.accName,
                                onValueChange = { state.accName = it },
                                label = "Account Name (e.g. Sapphire)"
                        )
                }
                
                CardKeeperTextField(
                        value = state.number,
                        onValueChange = { state.number = it },
                        label = if (state.type == AccountType.BANK_ACCOUNT)
                                "Account Number"
                        else "Card Number",
                        keyboardOptions =
                                KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                CardKeeperTextField(
                        value = state.holder,
                        onValueChange = { state.holder = it },
                        label = "Account Holder Name"
                )

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
                        CardKeeperTextField(
                                value = state.routing,
                                onValueChange = { state.routing = it },
                                label = "Routing Number (USA)",
                                keyboardOptions =
                                        KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        CardKeeperTextField(
                                value = state.wireNumber,
                                onValueChange = { state.wireNumber = it },
                                label = "Wire Transfer Number",
                                keyboardOptions =
                                        KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        // Indian Bank Codes
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                CardKeeperTextField(
                                        value = state.ifsc,
                                        onValueChange = { state.ifsc = it.uppercase() },
                                        label = "IFSC Code",
                                        modifier = Modifier.weight(1f)
                                )
                                CardKeeperTextField(
                                        value = state.swift,
                                        onValueChange = { state.swift = it.uppercase() },
                                        label = "SWIFT Code",
                                        modifier = Modifier.weight(1f)
                                )
                        }

                        // Branch Details
                        CardKeeperTextField(
                                value = state.branchAddress,
                                onValueChange = { state.branchAddress = it },
                                label = "Branch Address",
                                minLines = 2
                        )
                        CardKeeperTextField(
                                value = state.branchContact,
                                onValueChange = { state.branchContact = it },
                                label = "Branch Contact Number",
                                keyboardOptions =
                                        KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                        CardKeeperTextField(
                                value = state.bankWebUrl,
                                onValueChange = { state.bankWebUrl = it },
                                label = "Bank Website URL",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                        )

                        // Account Holder Address (from cheque)
                        CardKeeperTextField(
                                value = state.holderAddress,
                                onValueChange = { state.holderAddress = it },
                                label = "Account Holder Address",
                                minLines = 2
                        )
                }

                val isCard =
                        state.type == AccountType.CREDIT_CARD ||
                                state.type == AccountType.DEBIT_CARD

                if (isCard) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                CardKeeperTextField(
                                        value = state.expiry,
                                        onValueChange = { state.expiry = it },
                                        visualTransformation = expiryDateVisualTransformation,
                                        label = "Expiry (MM/YY)",
                                        modifier = Modifier.weight(1f),
                                        isError = state.expiryError,
                                        keyboardOptions =
                                                KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                                CardKeeperTextField(
                                        value = state.cvv,
                                        onValueChange = { if (it.length <= 4) state.cvv = it },
                                        label = "CVV/CVC",
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
                                CardKeeperTextField(
                                        value = state.network,
                                        onValueChange = { state.network = it },
                                        label = "Network (e.g. Visa)",
                                        modifier = Modifier.weight(1f)
                                )
                                CardKeeperTextField(
                                        value = state.pin,
                                        onValueChange = { state.pin = it },
                                        label = "Card PIN",
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
                        CardKeeperTextField(
                                value = state.contact,
                                onValueChange = { state.contact = it },
                                label = "Lost Card Contact Number",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                }

                CardKeeperTextField(
                        value = state.notes,
                        onValueChange = { state.notes = it },
                        label = "Notes",
                        minLines = 3,
                        maxLines = 5
                )

                val saveButtonText = when (state.type) {
                    AccountType.BANK_ACCOUNT -> "Save Bank Account"
                    else -> "Save Card"
                }

                CardKeeperSaveButton(
                    onClick = { onSave() },
                    text = saveButtonText
                )
        }
}
