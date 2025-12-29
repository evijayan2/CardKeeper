package com.vijay.cardkeeper.ui.item.forms

import android.graphics.Bitmap
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
import com.vijay.cardkeeper.ui.common.DateFormatType
import com.vijay.cardkeeper.ui.common.DateUtils
import com.vijay.cardkeeper.ui.common.DateVisualTransformation

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
        get() = DateUtils.formatDate(rawDob)
    val expiryDate: String
        get() = DateUtils.formatDate(rawExpiryDate)
    val residentSince: String
        get() = DateUtils.formatDate(rawResidentSince)
        
    // Error states
    var dobError by mutableStateOf(false)
    var expiryError by mutableStateOf(false)
    var residentSinceError by mutableStateOf(false)

    var sex by mutableStateOf(initialDoc?.sex ?: "")

    var frontPath by mutableStateOf(initialDoc?.frontImagePath)
    var backPath by mutableStateOf(initialDoc?.backImagePath)

    var frontBitmap by mutableStateOf<Bitmap?>(null)
    var backBitmap by mutableStateOf<Bitmap?>(null)
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

        OutlinedTextField(
                value = state.surname,
                onValueChange = { state.surname = it },
                label = { Text("Surname") },
                modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
                value = state.givenName,
                onValueChange = { state.givenName = it },
                label = { Text("Given Name") },
                modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
                value = state.uscisNumber,
                onValueChange = { state.uscisNumber = it },
                label = { Text("USCIS#") },
                modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                    value = state.category,
                    onValueChange = { state.category = it },
                    label = { Text("Category") },
                    modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                    value = state.sex,
                    onValueChange = { state.sex = it },
                    label = { Text("Sex") },
                    modifier = Modifier.weight(1f)
            )
        }
        OutlinedTextField(
                value = state.countryOfBirth,
                onValueChange = { state.countryOfBirth = it },
                label = { Text("Country of Birth") },
                modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
                value = state.rawDob,
                onValueChange = { 
                    if (it.length <= 8 && it.all { char -> char.isDigit() }) {
                        state.rawDob = it
                        state.dobError = !DateUtils.isValidDate(it, DateFormatType.USA) && it.length == 8
                    }
                },
                label = { Text("Date of Birth (MM/DD/YYYY)") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = dateVisualTransformation,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = state.dobError,
                supportingText = { if (state.dobError) Text("Invalid Date") }
        )
        OutlinedTextField(
                value = state.rawExpiryDate,
                onValueChange = { 
                    if (it.length <= 8 && it.all { char -> char.isDigit() }) {
                        state.rawExpiryDate = it
                        state.expiryError = !DateUtils.isValidDate(it, DateFormatType.USA) && it.length == 8
                    }
                },
                label = { Text("Card Expires (MM/DD/YYYY)") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = dateVisualTransformation,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = state.expiryError,
                supportingText = { if (state.expiryError) Text("Invalid Date") }
        )
        OutlinedTextField(
                value = state.rawResidentSince,
                onValueChange = { 
                    if (it.length <= 8 && it.all { char -> char.isDigit() }) {
                        state.rawResidentSince = it
                        state.residentSinceError = !DateUtils.isValidDate(it, DateFormatType.USA) && it.length == 8
                    }
                },
                label = { Text("Resident Since (MM/DD/YYYY)") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = dateVisualTransformation,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = state.residentSinceError,
                supportingText = { if (state.residentSinceError) Text("Invalid Date") }
        )

        Button(
                onClick = {
                    onSave()
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth()
        ) { Text("Save Green Card") }
    }
}
