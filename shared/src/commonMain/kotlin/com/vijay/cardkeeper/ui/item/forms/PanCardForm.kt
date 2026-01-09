package com.vijay.cardkeeper.ui.item.forms

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.vijay.cardkeeper.data.entity.PanCard
import com.vijay.cardkeeper.ui.common.CardKeeperTextField
import com.vijay.cardkeeper.ui.common.CardKeeperScanButtons
import com.vijay.cardkeeper.ui.common.CardKeeperScanActionButton
import com.vijay.cardkeeper.ui.common.CardKeeperSaveButton
import com.vijay.cardkeeper.ui.common.DateFormatType
import com.vijay.cardkeeper.ui.common.DateVisualTransformation
import com.vijay.cardkeeper.util.DateNormalizer
import com.vijay.cardkeeper.util.DateUtils

class PanCardFormState(initialCard: PanCard?) {
    var panNumber by mutableStateOf(initialCard?.panNumber ?: "")
    var holderName by mutableStateOf(initialCard?.holderName ?: "")
    var fatherName by mutableStateOf(initialCard?.fatherName ?: "")
    
    // Date: Internal raw storage for editing
    var rawDob by mutableStateOf(initialCard?.dob?.filter { it.isDigit() } ?: "")
    
    // Exposed property for AddItemScreen to read during onSave
    val dob: String
        get() = DateNormalizer.normalize(DateUtils.formatRawDate(rawDob, DateFormatType.INDIA), DateFormatType.INDIA)
    
    var dobError by mutableStateOf(false)
    var panError by mutableStateOf(false)

    var frontPath by mutableStateOf(initialCard?.frontImagePath)
    var backPath by mutableStateOf(initialCard?.backImagePath)

    var hasFrontImage by mutableStateOf(initialCard?.frontImagePath != null)
    var hasBackImage by mutableStateOf(initialCard?.backImagePath != null)

    // Validate PAN format: ABCDE1234F
    fun validatePan(): Boolean {
        val panRegex = Regex("^[A-Z]{5}[0-9]{4}[A-Z]$")
        val isValid = panNumber.matches(panRegex)
        panError = !isValid && panNumber.isNotEmpty()
        return isValid
    }
}

@Composable
fun rememberPanCardFormState(card: PanCard?): PanCardFormState {
    return remember(card) { PanCardFormState(card) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanCardForm(
    state: PanCardFormState,
    onScanOcr: () -> Unit,
    onScanFront: () -> Unit,
    onScanBack: () -> Unit,
    onSave: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val dateVisualTransformation = remember { DateVisualTransformation() }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(bottom = 16.dp)
    ) {


        // Image Scan Buttons
        CardKeeperScanButtons(
            hasFrontImage = state.hasFrontImage,
            onScanFront = onScanFront,
            hasBackImage = state.hasBackImage,
            onScanBack = onScanBack
        )

        // PAN Number
        CardKeeperTextField(
            value = state.panNumber,
            onValueChange = { 
                // Convert to uppercase and limit to 10 characters
                val formatted = it.uppercase().take(10)
                state.panNumber = formatted
                state.validatePan()
            },
            label = "PAN Number",
            placeholder = { Text("ABCDE1234F") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Characters
            ),
            isError = state.panError,
            supportingText = { 
                if (state.panError) {
                    Text("Invalid PAN format. Expected: ABCDE1234F")
                } else {
                    Text("Format: 5 letters + 4 digits + 1 letter")
                }
            }
        )

        // Holder Name
        CardKeeperTextField(
            value = state.holderName,
            onValueChange = { state.holderName = it },
            label = "Name (as on PAN card)"
        )

        // Father's Name
        CardKeeperTextField(
            value = state.fatherName,
            onValueChange = { state.fatherName = it },
            label = "Father's Name"
        )

        // Date of Birth (India format: DD/MM/YYYY)
        CardKeeperTextField(
            value = state.rawDob,
            onValueChange = { 
                if (it.length <= 8 && it.all { char -> char.isDigit() }) {
                    state.rawDob = it
                    state.dobError = !DateUtils.isValidDate(it, DateFormatType.INDIA) && it.length == 8
                }
            },
            label = "Date of Birth (DD/MM/YYYY)",
            visualTransformation = dateVisualTransformation,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = state.dobError,
            supportingText = { if (state.dobError) Text("Invalid Date") }
        )

        // Save Button
        CardKeeperSaveButton(
            onClick = {
                if (state.validatePan() || state.panNumber.isEmpty()) {
                    onSave()
                }
            },
            text = "Save PAN Card",
            enabled = state.panNumber.isNotEmpty() && state.holderName.isNotEmpty()
        )
    }
}
