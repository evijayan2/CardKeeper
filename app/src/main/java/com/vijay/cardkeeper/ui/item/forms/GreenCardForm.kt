package com.vijay.cardkeeper.ui.item.forms

import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vijay.cardkeeper.data.entity.GreenCard

class GreenCardFormState(initialDoc: GreenCard?) {
    var surname by mutableStateOf(initialDoc?.surname ?: "")
    var givenName by mutableStateOf(initialDoc?.givenName ?: "")
    var uscisNumber by mutableStateOf(initialDoc?.uscisNumber ?: "")
    var category by mutableStateOf(initialDoc?.category ?: "")
    var countryOfBirth by mutableStateOf(initialDoc?.countryOfBirth ?: "")
    var dob by mutableStateOf(initialDoc?.dob ?: "")
    var sex by mutableStateOf(initialDoc?.sex ?: "")
    var expiryDate by mutableStateOf(initialDoc?.expiryDate ?: "")
    var residentSince by mutableStateOf(initialDoc?.residentSince ?: "")

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
                value = state.dob,
                onValueChange = { state.dob = it },
                label = { Text("Date of Birth") },
                modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
                value = state.expiryDate,
                onValueChange = { state.expiryDate = it },
                label = { Text("Card Expires") },
                modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
                value = state.residentSince,
                onValueChange = { state.residentSince = it },
                label = { Text("Resident Since") },
                modifier = Modifier.fillMaxWidth()
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
