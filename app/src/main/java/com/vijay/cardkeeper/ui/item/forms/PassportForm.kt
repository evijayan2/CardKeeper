package com.vijay.cardkeeper.ui.item.forms

import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vijay.cardkeeper.data.entity.Passport

class PassportFormState(initialPassport: Passport? = null) {
        var passportNumber by mutableStateOf(initialPassport?.passportNumber ?: "")
        var countryCode by mutableStateOf(initialPassport?.countryCode ?: "USA")

        var surname by mutableStateOf(initialPassport?.surname ?: "")
        var givenNames by mutableStateOf(initialPassport?.givenNames ?: "")
        var nationality by mutableStateOf(initialPassport?.nationality ?: "")
        var dob by mutableStateOf(initialPassport?.dob ?: "")
        var sex by mutableStateOf(initialPassport?.sex ?: "")
        var placeOfBirth by mutableStateOf(initialPassport?.placeOfBirth ?: "")
        var dateOfIssue by mutableStateOf(initialPassport?.dateOfIssue ?: "")
        var dateOfExpiry by mutableStateOf(initialPassport?.dateOfExpiry ?: "")
        var placeOfIssue by mutableStateOf(initialPassport?.placeOfIssue ?: "")
        var authority by mutableStateOf(initialPassport?.authority ?: "")

        // Indian Specific
        var fatherName by mutableStateOf(initialPassport?.fatherName ?: "")
        var motherName by mutableStateOf(initialPassport?.motherName ?: "")
        var spouseName by mutableStateOf(initialPassport?.spouseName ?: "")
        var address by mutableStateOf(initialPassport?.address ?: "")
        var fileNumber by mutableStateOf(initialPassport?.fileNumber ?: "")

        var frontPath by mutableStateOf(initialPassport?.frontImagePath)
        var backPath by mutableStateOf(initialPassport?.backImagePath)

        var frontBitmap by mutableStateOf<Bitmap?>(null)
        var backBitmap by mutableStateOf<Bitmap?>(null)
}

@Composable
fun rememberPassportFormState(passport: Passport?): PassportFormState {
        return remember(passport) { PassportFormState(passport) }
}

@Composable
fun PassportForm(
        state: PassportFormState,
        onScanFront: () -> Unit,
        onScanBack: () -> Unit,
        onSave: () -> Unit,
        onNavigateBack: () -> Unit
) {
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
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Filled.PhotoCamera, "Front")
                                        Text(
                                                if (state.frontBitmap != null) "Front Captured"
                                                else "Scan Front"
                                        )
                                }
                        }
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
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Filled.PhotoCamera, "Back")
                                        Text(
                                                if (state.backBitmap != null) "Back Captured"
                                                else "Scan Back"
                                        )
                                }
                        }
                }

                // Basic Info
                OutlinedTextField(
                        value = state.countryCode,
                        onValueChange = { state.countryCode = it },
                        label = { Text("Country Code (USA, IND)") },
                        modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                        value = state.passportNumber,
                        onValueChange = { state.passportNumber = it },
                        label = { Text("Passport Number") },
                        modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                                value = state.givenNames,
                                onValueChange = { state.givenNames = it },
                                label = { Text("Given Names") },
                                modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                                value = state.surname,
                                onValueChange = { state.surname = it },
                                label = { Text("Surname") },
                                modifier = Modifier.weight(1f)
                        )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                                value = state.dob,
                                onValueChange = { state.dob = it },
                                label = { Text("Date of Birth") },
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
                        value = state.nationality,
                        onValueChange = { state.nationality = it },
                        label = { Text("Nationality") },
                        modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                                value = state.dateOfIssue,
                                onValueChange = { state.dateOfIssue = it },
                                label = { Text("Issue Date") },
                                modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                                value = state.dateOfExpiry,
                                onValueChange = { state.dateOfExpiry = it },
                                label = { Text("Expiry Date") },
                                modifier = Modifier.weight(1f)
                        )
                }

                OutlinedTextField(
                        value = state.placeOfIssue,
                        onValueChange = { state.placeOfIssue = it },
                        label = { Text("Place of Issue") },
                        modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                        value = state.placeOfBirth,
                        onValueChange = { state.placeOfBirth = it },
                        label = { Text("Place of Birth") },
                        modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                        value = state.authority,
                        onValueChange = { state.authority = it },
                        label = { Text("Issuing Authority") },
                        modifier = Modifier.fillMaxWidth()
                )

                // Extended Info (Indian Specific mostly)
                HorizontalDivider()
                Text(
                        "Additional Details (Indian Passport)",
                        style = MaterialTheme.typography.labelMedium
                )

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
                        minLines = 3
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
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                ) { Text("Save Passport") }
        }
}
