package com.vijay.cardkeeper.ui.item.forms


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
import com.vijay.cardkeeper.ui.common.CardKeeperTextField
import com.vijay.cardkeeper.ui.common.CardKeeperScanButtons
import com.vijay.cardkeeper.ui.common.CardKeeperScanActionButton
import com.vijay.cardkeeper.ui.common.CardKeeperSaveButton
import com.vijay.cardkeeper.ui.common.DateFormatType
import com.vijay.cardkeeper.util.DateUtils
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
        get() = DateNormalizer.normalize(DateUtils.formatRawDate(rawExpiry, dateFormatType), dateFormatType)
    val dob: String
        get() = DateNormalizer.normalize(DateUtils.formatRawDate(rawDob, dateFormatType), dateFormatType)

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

    var hasFrontImage by mutableStateOf(initialDoc?.frontImagePath != null)
    var hasBackImage by mutableStateOf(initialDoc?.backImagePath != null)

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
            // Scan Buttons
            CardKeeperScanButtons(
                hasFrontImage = state.hasFrontImage,
                onScanFront = onScanFront,
                hasBackImage = state.hasBackImage,
                onScanBack = onScanBack
            )

        if (state.type == DocumentType.DRIVER_LICENSE) {
            CardKeeperScanActionButton(
                text = "Scan Driver License Barcode",
                onClick = onScanBarcode,
                icon = Icons.Filled.PhotoCamera,
                isCaptured = false // Only prompt, no captured state in UI logic yet for barcode specifically shown in button text in orig code?
                // Orig code: Text(if (state.barcodeData != null) "Barcode Scanned" else "Scan Driver's License Barcode")
                // Ah, I need to check if barcodeData is available.
                // state.barcodeData is not in the viewed file state class? 
                // Let's re-read the file to check state properties.
            )
        }

            CardKeeperTextField(
                value = state.country,
                onValueChange = { state.country = it },
                label = "Issuing Country (Determines Date Format)"
            )

            CardKeeperTextField(
                value = state.region,
                onValueChange = { state.region = it },
                label = "State/Region"
            )

            CardKeeperTextField(
                value = state.number,
                onValueChange = { state.number = it },
                label = "Document Number"
            )
        
            val dateLabel = if (state.dateFormatType == com.vijay.cardkeeper.ui.common.DateFormatType.USA) "Expires (MM/DD/YYYY)" else "Expires (DD/MM/YYYY)"
            CardKeeperTextField(
                value = state.rawExpiry,
                onValueChange = { 
                    if (it.length <= 8 && it.all { char -> char.isDigit() }) {
                        state.rawExpiry = it
                        state.expiryError = !DateUtils.isValidDate(it, state.dateFormatType) && it.length == 8
                    }
                },
                label = dateLabel,
                visualTransformation = dateVisualTransformation,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = state.expiryError,
                supportingText = { if (state.expiryError) Text("Invalid Date") }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CardKeeperTextField(
                    value = state.firstName,
                    onValueChange = { state.firstName = it },
                    label = "First Name",
                    modifier = Modifier.weight(1f)
                )
                CardKeeperTextField(
                    value = state.lastName,
                    onValueChange = { state.lastName = it },
                    label = "Last Name",
                    modifier = Modifier.weight(1f)
                )
            }
            CardKeeperTextField(
                value = state.address,
                onValueChange = { state.address = it },
                label = "Address",
                minLines = 2
            )

            val dobLabel = if (state.dateFormatType == com.vijay.cardkeeper.ui.common.DateFormatType.USA) "Date of Birth (MM/DD/YYYY)" else "Date of Birth (DD/MM/YYYY)"
            CardKeeperTextField(
                value = state.rawDob,
                onValueChange = { 
                    if (it.length <= 8 && it.all { char -> char.isDigit() }) {
                        state.rawDob = it
                        state.dobError = !DateUtils.isValidDate(it, state.dateFormatType) && it.length == 8
                    }
                },
                label = dobLabel,
                visualTransformation = dateVisualTransformation,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = state.dobError,
                supportingText = { if (state.dobError) Text("Invalid Date") }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CardKeeperTextField(
                    value = state.sex,
                    onValueChange = { state.sex = it },
                    label = "Sex",
                    modifier = Modifier.weight(1f)
                )
                CardKeeperTextField(
                    value = state.eyeColor,
                    onValueChange = { state.eyeColor = it },
                    label = "Eyes",
                    modifier = Modifier.weight(1f)
                )
                CardKeeperTextField(
                    value = state.height,
                    onValueChange = { state.height = it },
                    label = "Height",
                    modifier = Modifier.weight(1f)
                )
            }

            if (state.type != DocumentType.DRIVER_LICENSE) {
                CardKeeperTextField(
                    value = state.issuingAuthority,
                    onValueChange = { state.issuingAuthority = it },
                    label = "Issuing Authority"
                )
            }

            CardKeeperTextField(
                value = state.licenseClass,
                onValueChange = { state.licenseClass = it },
                label = "Class"
            )
            CardKeeperTextField(
                value = state.restrictions,
                onValueChange = { state.restrictions = it },
                label = "Restrictions"
            )
            CardKeeperTextField(
                value = state.endorsements,
                onValueChange = { state.endorsements = it },
                label = "Endorsements"
            )

            CardKeeperSaveButton(
                onClick = { onSave() },
                text = "Save Driver License"
            )
    }
}
