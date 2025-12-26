package com.vijay.cardkeeper.ui.item.forms

import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vijay.cardkeeper.data.entity.DocumentType
import com.vijay.cardkeeper.data.entity.IdentityDocument

class IdentityFormState(initialDoc: IdentityDocument?, initialType: DocumentType? = null) {
        var type by mutableStateOf(initialDoc?.type ?: initialType ?: DocumentType.DRIVER_LICENSE)
        var number by mutableStateOf(initialDoc?.docNumber ?: "")
        var expiry by mutableStateOf(initialDoc?.expiryDate ?: "")
        var firstName by mutableStateOf(initialDoc?.holderName?.substringBefore(" ") ?: "")
        var lastName by mutableStateOf(initialDoc?.holderName?.substringAfter(" ", "") ?: "")
        var address by mutableStateOf(initialDoc?.address ?: "")
        // Expanded fields
        var dob by mutableStateOf(initialDoc?.dob ?: "")
        var sex by mutableStateOf(initialDoc?.sex ?: "")
        var eyeColor by mutableStateOf(initialDoc?.eyeColor ?: "")
        var height by mutableStateOf(initialDoc?.height ?: "")
        var licenseClass by mutableStateOf(initialDoc?.licenseClass ?: "")
        var restrictions by mutableStateOf(initialDoc?.restrictions ?: "")
        var endorsements by mutableStateOf(initialDoc?.endorsements ?: "")
        var issuingAuthority by mutableStateOf(initialDoc?.issuingAuthority ?: "")
        var country by mutableStateOf(initialDoc?.country ?: "")
        var region by mutableStateOf(initialDoc?.state ?: "")

        var frontPath by mutableStateOf(initialDoc?.frontImagePath)
        var backPath by mutableStateOf(initialDoc?.backImagePath)

        var frontBitmap by mutableStateOf<Bitmap?>(null)
        var backBitmap by mutableStateOf<Bitmap?>(null)
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
        onSave: () -> Unit,
        onNavigateBack: () -> Unit
) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Document Type", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                                        DocumentType.DRIVER_LICENSE,
                                        DocumentType.SSN,
                                        DocumentType.PAN,
                                        DocumentType.ADHAAR,
                                        DocumentType.OTHER
                                )
                                .forEach { type ->
                                        FilterChip(
                                                selected = state.type == type,
                                                onClick = { state.type = type },
                                                label = { Text(type.name.replace("_", " ")) }
                                        )
                                }
                }

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
                                Column(
                                        horizontalAlignment =
                                                androidx.compose.ui.Alignment.CenterHorizontally
                                ) {
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
                                Column(
                                        horizontalAlignment =
                                                androidx.compose.ui.Alignment.CenterHorizontally
                                ) {
                                        Icon(Icons.Filled.PhotoCamera, "Back")
                                        Text(
                                                if (state.backBitmap != null) "Back Captured"
                                                else "Scan Back"
                                        )
                                }
                        }
                }

                OutlinedTextField(
                        value = state.country,
                        onValueChange = { v: String -> state.country = v },
                        label = { Text("Issuing Country") },
                        modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                        value = state.number,
                        onValueChange = { v: String -> state.number = v },
                        label = { Text("Document Number") },
                        modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                        value = state.expiry,
                        onValueChange = { v: String -> state.expiry = v },
                        label = { androidx.compose.material3.Text("Expiry Date") },
                        modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                                value = state.firstName,
                                onValueChange = { v: String -> state.firstName = v },
                                label = { Text("First Name") },
                                modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                                value = state.lastName,
                                onValueChange = { v: String -> state.lastName = v },
                                label = { Text("Last Name") },
                                modifier = Modifier.weight(1f)
                        )
                }

                OutlinedTextField(
                        value = state.dob,
                        onValueChange = { v: String -> state.dob = v },
                        label = { Text("Date of Birth") },
                        modifier = Modifier.fillMaxWidth()
                )

                // Detailed Fields
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                                value = state.region,
                                onValueChange = { v: String -> state.region = v },
                                label = { Text("State") },
                                modifier = Modifier.weight(1f)
                        )
                }

                if (state.type == DocumentType.DRIVER_LICENSE) {
                        OutlinedTextField(
                                value = state.address,
                                onValueChange = { v: String -> state.address = v },
                                label = { Text("Address") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                        value = state.sex,
                                        onValueChange = { v: String -> state.sex = v },
                                        label = { Text("Sex") },
                                        modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                        value = state.eyeColor,
                                        onValueChange = { v: String -> state.eyeColor = v },
                                        label = { Text("Eyes") },
                                        modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                        value = state.height,
                                        onValueChange = { v: String -> state.height = v },
                                        label = { Text("Height") },
                                        modifier = Modifier.weight(1f)
                                )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                        value = state.licenseClass,
                                        onValueChange = { v: String -> state.licenseClass = v },
                                        label = { Text("Class") },
                                        modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                        value = state.endorsements,
                                        onValueChange = { v: String -> state.endorsements = v },
                                        label = { Text("End.") },
                                        modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                        value = state.restrictions,
                                        onValueChange = { v: String -> state.restrictions = v },
                                        label = { Text("Restr.") },
                                        modifier = Modifier.weight(1f)
                                )
                        }
                        OutlinedTextField(
                                value = state.issuingAuthority,
                                onValueChange = { v: String -> state.issuingAuthority = v },
                                label = { Text("Issuing Authority (State)") },
                                modifier = Modifier.fillMaxWidth()
                        )
                }

                Button(
                        onClick = {
                                onSave()
                                onNavigateBack()
                        },
                        modifier = Modifier.fillMaxWidth()
                ) { Text("Save Document") }
        }
}
