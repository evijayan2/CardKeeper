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
        // OCR Scan Button
        Button(
            onClick = onScanOcr,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.QrCodeScanner, "Scan PAN")
                Text("Scan PAN Card (OCR)")
            }
        }

        // Image Scan Buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onScanFront,
                modifier = Modifier.weight(1f),
                colors = if (state.hasFrontImage)
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                else ButtonDefaults.buttonColors()
            ) {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.PhotoCamera, "Front")
                    Text(
                        if (state.hasFrontImage) "Front Captured"
                        else "Scan Front"
                    )
                }
            }
            Button(
                onClick = onScanBack,
                modifier = Modifier.weight(1f),
                colors = if (state.hasBackImage)
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                else ButtonDefaults.buttonColors()
            ) {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.PhotoCamera, "Back")
                    Text(
                        if (state.hasBackImage) "Back Captured"
                        else "Scan Back"
                    )
                }
            }
        }

        // PAN Number
        OutlinedTextField(
            value = state.panNumber,
            onValueChange = { 
                // Convert to uppercase and limit to 10 characters
                val formatted = it.uppercase().take(10)
                state.panNumber = formatted
                state.validatePan()
            },
            label = { Text("PAN Number") },
            placeholder = { Text("ABCDE1234F") },
            modifier = Modifier.fillMaxWidth(),
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
        OutlinedTextField(
            value = state.holderName,
            onValueChange = { state.holderName = it },
            label = { Text("Name (as on PAN card)") },
            modifier = Modifier.fillMaxWidth()
        )

        // Father's Name
        OutlinedTextField(
            value = state.fatherName,
            onValueChange = { state.fatherName = it },
            label = { Text("Father's Name") },
            modifier = Modifier.fillMaxWidth()
        )

        // Date of Birth (India format: DD/MM/YYYY)
        OutlinedTextField(
            value = state.rawDob,
            onValueChange = { 
                if (it.length <= 8 && it.all { char -> char.isDigit() }) {
                    state.rawDob = it
                    state.dobError = !DateUtils.isValidDate(it, DateFormatType.INDIA) && it.length == 8
                }
            },
            label = { Text("Date of Birth (DD/MM/YYYY)") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = dateVisualTransformation,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = state.dobError,
            supportingText = { if (state.dobError) Text("Invalid Date") }
        )

        // Save Button
        Button(
            onClick = {
                if (state.validatePan() || state.panNumber.isEmpty()) {
                    onSave()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = state.panNumber.isNotEmpty() && state.holderName.isNotEmpty()
        ) { 
            Text("Save PAN Card") 
        }
    }
}
