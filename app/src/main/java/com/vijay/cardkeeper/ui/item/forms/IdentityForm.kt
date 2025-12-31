package com.vijay.cardkeeper.ui.item.forms

import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.vijay.cardkeeper.data.entity.DocumentType
import com.vijay.cardkeeper.data.entity.IdentityDocument
import com.vijay.cardkeeper.ui.common.DateFormatType
import com.vijay.cardkeeper.ui.common.DateUtils
import com.vijay.cardkeeper.ui.common.DateVisualTransformation
import com.vijay.cardkeeper.util.DateNormalizer

class IdentityFormState(initialDoc: IdentityDocument?, initialType: DocumentType? = null) {
    var type by mutableStateOf(initialDoc?.type ?: initialType ?: DocumentType.DRIVER_LICENSE)
    var number by mutableStateOf(initialDoc?.docNumber ?: "")
    
    // Dates: Internal raw storage for editing, exposed formatted getter for Saving
    var rawExpiry by mutableStateOf(initialDoc?.expiryDate?.filter { it.isDigit() } ?: "")
    var rawDob by mutableStateOf(initialDoc?.dob?.filter { it.isDigit() } ?: "")

    // Exposed properties for AddItemScreen to read during onSave
    val expiry: String
        get() = DateUtils.formatDate(rawExpiry)
    val dob: String
        get() = DateUtils.formatDate(rawDob)

    var expiryError by mutableStateOf(false)
    var dobError by mutableStateOf(false)

    var firstName by mutableStateOf(initialDoc?.holderName?.substringBefore(" ") ?: "")
    var lastName by mutableStateOf(initialDoc?.holderName?.substringAfter(" ", "") ?: "")
    var address by mutableStateOf(initialDoc?.address ?: "")

    // Expanded fields
    var sex by mutableStateOf(initialDoc?.sex ?: "")
    var eyeColor by mutableStateOf(initialDoc?.eyeColor ?: "")
    var height by mutableStateOf(initialDoc?.height ?: "")
    var licenseClass by mutableStateOf(initialDoc?.licenseClass ?: "")
    var restrictions by mutableStateOf(initialDoc?.restrictions ?: "")
    var endorsements by mutableStateOf(initialDoc?.endorsements ?: "")
    var issuingAuthority by mutableStateOf(initialDoc?.issuingAuthority ?: "")
    var country by mutableStateOf((initialDoc?.country ?: "USA").ifBlank { "USA" })
    var region by mutableStateOf(initialDoc?.state ?: "") // State/Province

    var frontPath by mutableStateOf(initialDoc?.frontImagePath)
    var backPath by mutableStateOf(initialDoc?.backImagePath)

    var frontBitmap by mutableStateOf<Bitmap?>(null)
    var backBitmap by mutableStateOf<Bitmap?>(null)

    // Helper to determine date format based on country
    val dateFormatType: DateFormatType
        get() = when (country.trim().uppercase()) {
            "USA", "UNITED STATES", "US" -> com.vijay.cardkeeper.ui.common.DateFormatType.USA
            "INDIA", "IN", "BHARAT" -> com.vijay.cardkeeper.ui.common.DateFormatType.INDIA
            else -> if (country.contains("United States", ignoreCase = true)) com.vijay.cardkeeper.ui.common.DateFormatType.USA else com.vijay.cardkeeper.ui.common.DateFormatType.GENERIC
        }
}

@Composable
fun rememberIdentityFormState(
    doc: IdentityDocument?,
    initialType: DocumentType? = null
): IdentityFormState {
    return remember(doc, initialType) { IdentityFormState(doc, initialType) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdentityForm(
    state: IdentityFormState,
    onScanFront: () -> Unit,
    onScanBack: () -> Unit,
    onScanBarcode: () -> Unit = {},
    onSave: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val dateVisualTransformation = remember { DateVisualTransformation() }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
            // Scan Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onScanFront,
                    modifier = Modifier.weight(1f),
                    colors =
                    if (state.frontBitmap != null || state.frontPath != null)
                        ButtonDefaults.buttonColors(
                            containerColor =
                            MaterialTheme.colorScheme.primaryContainer,
                            contentColor =
                            MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    else ButtonDefaults.buttonColors()
                ) {
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.PhotoCamera, "Front")
                        Text(
                            if (state.frontBitmap != null || state.frontPath != null)
                                "Front Captured"
                            else "Scan Front"
                        )
                    }
                }
                Button(
                    onClick = onScanBack,
                    modifier = Modifier.weight(1f),
                    colors =
                    if (state.backBitmap != null || state.backPath != null)
                        ButtonDefaults.buttonColors(
                            containerColor =
                            MaterialTheme.colorScheme.primaryContainer,
                            contentColor =
                            MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    else ButtonDefaults.buttonColors()
                ) {
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.PhotoCamera, "Back")
                        Text(
                            if (state.backBitmap != null || state.backPath != null) "Back Captured"
                            else "Scan Back"
                        )
                    }
                }
            }

        if (state.type == DocumentType.DRIVER_LICENSE) {
                Button(
                    onClick = onScanBarcode,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.PhotoCamera, "Scan Barcode")
                        Text("Scan Driver License Barcode")
                    }
                }
        }

            OutlinedTextField(
                value = state.country,
                onValueChange = { state.country = it },
                label = { Text("Issuing Country (Determines Date Format)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.region,
                onValueChange = { state.region = it },
                label = { Text("State/Region") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.number,
                onValueChange = { state.number = it },
                label = { Text("Document Number") },
                modifier = Modifier.fillMaxWidth()
            )
        
            val dateLabel = if (state.dateFormatType == com.vijay.cardkeeper.ui.common.DateFormatType.USA) "Expires (MM/DD/YYYY)" else "Expires (DD/MM/YYYY)"
            OutlinedTextField(
                value = state.rawExpiry,
                onValueChange = { 
                    if (it.length <= 8 && it.all { char -> char.isDigit() }) {
                        state.rawExpiry = it
                        state.expiryError = !DateUtils.isValidDate(it, state.dateFormatType) && it.length == 8
                    }
                },
                label = { Text(dateLabel) },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = dateVisualTransformation,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = state.expiryError,
                supportingText = { if (state.expiryError) Text("Invalid Date") }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.firstName,
                    onValueChange = { state.firstName = it },
                    label = { Text("First Name") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = state.lastName,
                    onValueChange = { state.lastName = it },
                    label = { Text("Last Name") },
                    modifier = Modifier.weight(1f)
                )
            }
            OutlinedTextField(
                value = state.address,
                onValueChange = { state.address = it },
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            val dobLabel = if (state.dateFormatType == com.vijay.cardkeeper.ui.common.DateFormatType.USA) "Date of Birth (MM/DD/YYYY)" else "Date of Birth (DD/MM/YYYY)"
            OutlinedTextField(
                value = state.rawDob,
                onValueChange = { 
                    if (it.length <= 8 && it.all { char -> char.isDigit() }) {
                        state.rawDob = it
                        state.dobError = !DateUtils.isValidDate(it, state.dateFormatType) && it.length == 8
                    }
                },
                label = { Text(dobLabel) },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = dateVisualTransformation,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = state.dobError,
                supportingText = { if (state.dobError) Text("Invalid Date") }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.sex,
                    onValueChange = { state.sex = it },
                    label = { Text("Sex") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = state.eyeColor,
                    onValueChange = { state.eyeColor = it },
                    label = { Text("Eyes") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = state.height,
                    onValueChange = { state.height = it },
                    label = { Text("Height") },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = state.issuingAuthority,
                onValueChange = { state.issuingAuthority = it },
                label = { Text("Issuing Authority") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.licenseClass,
                onValueChange = { state.licenseClass = it },
                label = { Text("Class") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.restrictions,
                onValueChange = { state.restrictions = it },
                label = { Text("Restrictions") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.endorsements,
                onValueChange = { state.endorsements = it },
                label = { Text("Endorsements") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    onSave()
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save Driver License") }
    }
}
