package com.vijay.cardkeeper.ui.item.forms

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.vijay.cardkeeper.data.entity.GreenCard
import com.vijay.cardkeeper.ui.common.CardKeeperTextField
import com.vijay.cardkeeper.ui.common.CardKeeperScanButtons
import com.vijay.cardkeeper.ui.common.CardKeeperSaveButton
import com.vijay.cardkeeper.ui.common.DateFormatType
import com.vijay.cardkeeper.util.DateUtils
import com.vijay.cardkeeper.ui.common.DateVisualTransformation
import com.vijay.cardkeeper.util.DateNormalizer

class GreenCardFormState(initialDoc: GreenCard?) {
    var surname by mutableStateOf(initialDoc?.surname ?: "")
    var givenName by mutableStateOf(initialDoc?.givenName ?: "")
    var uscisNumber by mutableStateOf(initialDoc?.uscisNumber ?: "")
    var category by mutableStateOf(initialDoc?.category ?: "")
    var countryOfBirth by mutableStateOf(initialDoc?.countryOfBirth ?: "")
    
    // Date fields - Internal raw storage
    var rawDob by mutableStateOf(initialDoc?.dob?.filter { it.isDigit() } ?: "")
    var rawExpiryDate by mutableStateOf(initialDoc?.expiryDate?.filter { it.isDigit() } ?: "")
    var rawResidentSince by mutableStateOf(initialDoc?.residentSince?.filter { it.isDigit() } ?: "")
    
    // Computed formatted values for saving (exposed matching AddItemScreen expectations)
    val dob: String
        get() = DateUtils.formatRawDate(rawDob)
    val expiryDate: String
        get() = DateUtils.formatRawDate(rawExpiryDate)
    val residentSince: String
        get() = DateUtils.formatRawDate(rawResidentSince)
        
    // Error states
    var dobError by mutableStateOf(false)
    var expiryError by mutableStateOf(false)
    var residentSinceError by mutableStateOf(false)

    var sex by mutableStateOf(initialDoc?.sex ?: "")

    var frontPath by mutableStateOf(initialDoc?.frontImagePath)
    var backPath by mutableStateOf(initialDoc?.backImagePath)

    var hasFrontImage by mutableStateOf(initialDoc?.frontImagePath != null)
    var hasBackImage by mutableStateOf(initialDoc?.backImagePath != null)
}

@Composable
fun rememberGreenCardFormState(doc: GreenCard?): GreenCardFormState {
    return remember(doc) { GreenCardFormState(doc) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GreenCardForm(
        state: GreenCardFormState,
        onScanFront: () -> Unit,
        onScanBack: () -> Unit,
        onSave: () -> Unit,
        onNavigateBack: () -> Unit
) {
    val dateVisualTransformation = remember { DateVisualTransformation() }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Scan Buttons
        CardKeeperScanButtons(
            hasFrontImage = state.hasFrontImage,
            onScanFront = onScanFront,
            hasBackImage = state.hasBackImage,
            onScanBack = onScanBack
        )

        CardKeeperTextField(
                value = state.surname,
                onValueChange = { state.surname = it },
                label = "Surname"
        )
        CardKeeperTextField(
                value = state.givenName,
                onValueChange = { state.givenName = it },
                label = "Given Name"
        )
        CardKeeperTextField(
                value = state.uscisNumber,
                onValueChange = { state.uscisNumber = it },
                label = "USCIS#"
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CardKeeperTextField(
                    value = state.category,
                    onValueChange = { state.category = it },
                    label = "Category",
                    modifier = Modifier.weight(1f)
            )
            CardKeeperTextField(
                    value = state.sex,
                    onValueChange = { state.sex = it },
                    label = "Sex",
                    modifier = Modifier.weight(1f)
            )
        }
        CardKeeperTextField(
                value = state.countryOfBirth,
                onValueChange = { state.countryOfBirth = it },
                label = "Country of Birth"
        )
        CardKeeperTextField(
                value = state.rawDob,
                onValueChange = { 
                    if (it.length <= 8 && it.all { char -> char.isDigit() }) {
                        state.rawDob = it
                        state.dobError = !DateUtils.isValidDate(it, DateFormatType.USA) && it.length == 8
                    }
                },
                label = "Date of Birth (MM/DD/YYYY)",
                visualTransformation = dateVisualTransformation,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = state.dobError,
                supportingText = { if (state.dobError) Text("Invalid Date") }
        )
        CardKeeperTextField(
                value = state.rawExpiryDate,
                onValueChange = { 
                    if (it.length <= 8 && it.all { char -> char.isDigit() }) {
                        state.rawExpiryDate = it
                        state.expiryError = !DateUtils.isValidDate(it, DateFormatType.USA) && it.length == 8
                    }
                },
                label = "Card Expires (MM/DD/YYYY)",
                visualTransformation = dateVisualTransformation,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = state.expiryError,
                supportingText = { if (state.expiryError) Text("Invalid Date") }
        )
        CardKeeperTextField(
                value = state.rawResidentSince,
                onValueChange = { 
                    if (it.length <= 8 && it.all { char -> char.isDigit() }) {
                        state.rawResidentSince = it
                        state.residentSinceError = !DateUtils.isValidDate(it, DateFormatType.USA) && it.length == 8
                    }
                },
                label = "Resident Since (MM/DD/YYYY)",
                visualTransformation = dateVisualTransformation,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = state.residentSinceError,
                supportingText = { if (state.residentSinceError) Text("Invalid Date") }
        )

        CardKeeperSaveButton(
                onClick = { onSave() },
                text = "Save Green Card"
        )
    }
}
