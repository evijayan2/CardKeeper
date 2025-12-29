package com.vijay.cardkeeper.ui.item.forms

import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.vijay.cardkeeper.data.entity.Passport
import com.vijay.cardkeeper.ui.common.DateFormatType
import com.vijay.cardkeeper.ui.common.DateUtils
import com.vijay.cardkeeper.ui.common.DateVisualTransformation

class PassportFormState(initialDoc: Passport? = null) {
        var passportNumber by mutableStateOf(initialDoc?.passportNumber ?: "")
        var countryCode by mutableStateOf((initialDoc?.countryCode ?: "USA").ifBlank { "USA" })
    
        // Dates - Internal Raw
        var rawDob by mutableStateOf(initialDoc?.dob?.filter { it.isDigit() } ?: "")
        var rawDateOfIssue by mutableStateOf(initialDoc?.dateOfIssue?.filter { it.isDigit() } ?: "")
        var rawDateOfExpiry by mutableStateOf(initialDoc?.dateOfExpiry?.filter { it.isDigit() } ?: "")

        // Exposed Formatted Properties for AddItemScreen
        val dob: String get() = DateUtils.formatDate(rawDob)
        val dateOfIssue: String get() = DateUtils.formatDate(rawDateOfIssue)
        val dateOfExpiry: String get() = DateUtils.formatDate(rawDateOfExpiry)

        // Error States
        var dobError by mutableStateOf(false)
        var issueError by mutableStateOf(false)
        var expiryError by mutableStateOf(false)

        var surname by mutableStateOf(initialDoc?.surname ?: "")
        var givenNames by mutableStateOf(initialDoc?.givenNames ?: "")
        var nationality by mutableStateOf(initialDoc?.nationality ?: "")
        var sex by mutableStateOf(initialDoc?.sex ?: "")
        var placeOfBirth by mutableStateOf(initialDoc?.placeOfBirth ?: "")
        var placeOfIssue by mutableStateOf(initialDoc?.placeOfIssue ?: "")
        var authority by mutableStateOf(initialDoc?.authority ?: "")

        // India Specific (but generalized in entity)
        var fatherName by mutableStateOf(initialDoc?.fatherName ?: "")
        var motherName by mutableStateOf(initialDoc?.motherName ?: "")
        var spouseName by mutableStateOf(initialDoc?.spouseName ?: "")
        var address by mutableStateOf(initialDoc?.address ?: "")
        var fileNumber by mutableStateOf(initialDoc?.fileNumber ?: "")

        var frontPath by mutableStateOf(initialDoc?.frontImagePath)
        var backPath by mutableStateOf(initialDoc?.backImagePath)

        var frontBitmap by mutableStateOf<Bitmap?>(null)
        var backBitmap by mutableStateOf<Bitmap?>(null)
    
        // Helper for date format
        val dateFormatType: DateFormatType
            get() = when (countryCode.trim().uppercase()) {
                "USA", "US" -> DateFormatType.USA
                "IND", "INDIA" -> DateFormatType.INDIA
                else -> DateFormatType.GENERIC
            }
}

@Composable
fun rememberPassportFormState(doc: Passport?): PassportFormState {
        return remember(doc) { PassportFormState(doc) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassportForm(
        state: PassportFormState,
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
                                if (state.backBitmap != null || state.backPath != null)
                                        "Back Captured"
                                else "Scan Back"
                        )
                    }
                }
            }

            OutlinedTextField(
                    value = state.countryCode,
                    onValueChange = { state.countryCode = it },
                    label = { Text("Country Code (e.g. USA, IND)") },
                    modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                    value = state.passportNumber,
                    onValueChange = { state.passportNumber = it },
                    label = { Text("Passport Number") },
                    modifier = Modifier.fillMaxWidth()
            )
        
            OutlinedTextField(
                    value = state.surname,
                    onValueChange = { state.surname = it },
                    label = { Text("Surname") },
                    modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                    value = state.givenNames,
                    onValueChange = { state.givenNames = it },
                    label = { Text("Given Names") },
                    modifier = Modifier.fillMaxWidth()
            )
        
            val dobLabel = if (state.dateFormatType == DateFormatType.USA) "Date of Birth (MM/DD/YYYY)" else "Date of Birth (DD/MM/YYYY)"
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
        
            OutlinedTextField(
                    value = state.nationality,
                    onValueChange = { state.nationality = it },
                    label = { Text("Nationality") },
                    modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                    value = state.sex,
                    onValueChange = { state.sex = it },
                    label = { Text("Sex") },
                    modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                    value = state.placeOfBirth,
                    onValueChange = { state.placeOfBirth = it },
                    label = { Text("Place of Birth") },
                    modifier = Modifier.fillMaxWidth()
            )
        
            val dateLabel = if (state.dateFormatType == DateFormatType.USA) "Date of Issue (MM/DD/YYYY)" else "Date of Issue (DD/MM/YYYY)"
            OutlinedTextField(
                    value = state.rawDateOfIssue,
                    onValueChange = { 
                        if (it.length <= 8 && it.all { char -> char.isDigit() }) {
                            state.rawDateOfIssue = it
                            state.issueError = !DateUtils.isValidDate(it, state.dateFormatType) && it.length == 8
                        }
                    },
                    label = { Text(dateLabel) },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = dateVisualTransformation,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = state.issueError,
                    supportingText = { if (state.issueError) Text("Invalid Date") }
            )
        
            val dateExpiryLabel = if (state.dateFormatType == DateFormatType.USA) "Date of Expiry (MM/DD/YYYY)" else "Date of Expiry (DD/MM/YYYY)"
            OutlinedTextField(
                    value = state.rawDateOfExpiry,
                    onValueChange = { 
                        if (it.length <= 8 && it.all { char -> char.isDigit() }) {
                            state.rawDateOfExpiry = it
                            state.expiryError = !DateUtils.isValidDate(it, state.dateFormatType) && it.length == 8
                        }
                    },
                    label = { Text(dateExpiryLabel) },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = dateVisualTransformation,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = state.expiryError,
                    supportingText = { if (state.expiryError) Text("Invalid Date") }
            )
        
            OutlinedTextField(
                    value = state.placeOfIssue,
                    onValueChange = { state.placeOfIssue = it },
                    label = { Text("Place of Issue") },
                    modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                    value = state.authority,
                    onValueChange = { state.authority = it },
                    label = { Text("Authority") },
                    modifier = Modifier.fillMaxWidth()
            )

        // Expanded/India Fields if code is IND? Or always show? 
        // Showing them always as per existing form, but maybe grouped
        
            Text("Additional Details", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))

            OutlinedTextField(
                    value = state.fatherName,
                    onValueChange = { state.fatherName = it },
                    label = { Text("Father's Name") },
                    modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                    value = state.motherName,
                    onValueChange = { state.motherName = it },
                    label = { Text("Mother's Name") },
                    modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                    value = state.spouseName,
                    onValueChange = { state.spouseName = it },
                    label = { Text("Spouse's Name") },
                    modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                    value = state.address,
                    onValueChange = { state.address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
            )
            OutlinedTextField(
                    value = state.fileNumber,
                    onValueChange = { state.fileNumber = it },
                    label = { Text("File Number") },
                    modifier = Modifier.fillMaxWidth()
            )

            Button(
                    onClick = {
                        onSave()
                        onNavigateBack()
                    },
                    modifier = Modifier.fillMaxWidth()
            ) { Text("Save Passport") }
    }
}
