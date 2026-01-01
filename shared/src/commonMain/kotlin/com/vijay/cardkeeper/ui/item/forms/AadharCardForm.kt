package com.vijay.cardkeeper.ui.item.forms

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
// import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.vijay.cardkeeper.data.entity.AadharCard
import com.vijay.cardkeeper.ui.common.DateFormatType
import com.vijay.cardkeeper.util.DateUtils
import com.vijay.cardkeeper.ui.common.DateVisualTransformation
import com.vijay.cardkeeper.util.DateNormalizer

class AadharCardFormState(initialCard: AadharCard?) {
        // QR data fields
        var referenceId by mutableStateOf(initialCard?.referenceId ?: "")
        var holderName by mutableStateOf(initialCard?.holderName ?: "")
        
        // Date field - Internal raw storage
        // Aadhar might have YOB (4 digits) or DOB (8 digits).
        var rawDob by mutableStateOf(initialCard?.dob?.filter { it.isDigit() } ?: "")
        
        // Exposed formatted property for AddItemScreen
        val dob: String
            get() = DateNormalizer.normalize(DateUtils.formatRawDate(rawDob, DateFormatType.INDIA), DateFormatType.INDIA)
            
        var dobError by mutableStateOf(false)

        var gender by mutableStateOf(initialCard?.gender ?: "")
        var address by mutableStateOf(initialCard?.address ?: "")
        var email by mutableStateOf(initialCard?.email ?: "")
        var mobile by mutableStateOf(initialCard?.mobile ?: "")

        // QR metadata
        var pincode by mutableStateOf(initialCard?.pincode ?: "")
        var maskedAadhaarNumber by mutableStateOf(initialCard?.maskedAadhaarNumber ?: "")
        var uid by mutableStateOf(initialCard?.uid ?: "")
        var vid by mutableStateOf(initialCard?.vid ?: "")

        // QR metadata
        var photoBase64 by mutableStateOf(initialCard?.photoBase64)
        var timestamp by mutableStateOf(initialCard?.timestamp ?: "")
        var digitalSignature by mutableStateOf(initialCard?.digitalSignature)
        var certificateId by mutableStateOf(initialCard?.certificateId)

        // Optional
        var enrollmentNumber by mutableStateOf(initialCard?.enrollmentNumber ?: "")

        // Image paths
        var frontPath by mutableStateOf(initialCard?.frontImagePath)
        var backPath by mutableStateOf(initialCard?.backImagePath)

        // Bitmaps for captured images
        var hasFrontImage by mutableStateOf(initialCard?.frontImagePath != null)
        var hasBackImage by mutableStateOf(initialCard?.backImagePath != null)

        // Original QR data for reproduction
        var qrData by mutableStateOf(initialCard?.qrData)

        // Signature verification status
        var signatureValid by mutableStateOf(false)
}

@Composable
fun rememberAadharCardFormState(card: AadharCard?): AadharCardFormState {
        return remember(card) { AadharCardFormState(card) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AadharCardForm(
        state: AadharCardFormState,
        onScanFront: () -> Unit,
        onScanBack: () -> Unit,
        onScanQr: () -> Unit,
        onSave: () -> Unit,
        onNavigateBack: () -> Unit
) {
        val dateVisualTransformation = remember { DateVisualTransformation() }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Scan Buttons Row
                Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                ) {
                        Button(
                                onClick = onScanFront,
                                modifier = Modifier.weight(1f),
                                colors =
                                        if (state.hasFrontImage)
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
                                                if (state.hasFrontImage)
                                                        "Front ✓"
                                                else "Scan Front"
                                        )
                                }
                        }
                        Button(
                                onClick = onScanBack,
                                modifier = Modifier.weight(1f),
                                colors =
                                        if (state.hasBackImage)
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
                                                if (state.hasBackImage)
                                                        "Back ✓"
                                                else "Scan Back"
                                        )
                                }
                        }
                }

                // QR Scan Button
                Button(
                        onClick = onScanQr,
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                                if (state.qrData != null)
                                        ButtonDefaults.buttonColors(
                                                containerColor =
                                                        MaterialTheme.colorScheme.tertiaryContainer,
                                                contentColor =
                                                        MaterialTheme.colorScheme
                                                                .onTertiaryContainer
                                        )
                                else ButtonDefaults.outlinedButtonColors()
                ) {
                        Icon(
                                Icons.Filled.QrCodeScanner,
                                "QR",
                                modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(if (state.qrData != null) "QR Scanned ✓" else "Scan Aadhaar QR Code")
                }

                // Signature Verification Status
                if (state.qrData != null) {
                        androidx.compose.material3.Card(
                                colors =
                                        androidx.compose.material3.CardDefaults.cardColors(
                                                containerColor =
                                                        if (state.signatureValid)
                                                                androidx.compose.ui.graphics.Color(
                                                                                0xFF4CAF50
                                                                        )
                                                                        .copy(alpha = 0.15f)
                                                        else
                                                                androidx.compose.ui.graphics.Color(
                                                                                0xFFF44336
                                                                        )
                                                                        .copy(alpha = 0.15f)
                                        ),
                                modifier = Modifier.fillMaxWidth()
                        ) {
                                Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                ) {
                                        Text(
                                                text =
                                                        if (state.signatureValid)
                                                                "✓ UIDAI Signature Verified"
                                                        else "✗ Signature Unverified",
                                                color =
                                                        if (state.signatureValid)
                                                                androidx.compose.ui.graphics.Color(
                                                                        0xFF4CAF50
                                                                )
                                                        else
                                                                androidx.compose.ui.graphics.Color(
                                                                        0xFFF44336
                                                                ),
                                                style = MaterialTheme.typography.labelLarge
                                        )
                                }
                        }
                }

                HorizontalDivider()

                // QR Photo Display
                if (state.photoBase64 != null) {
                    // TODO: Implement KMP Base64 Image decoding
                    Text("Photo available from QR (Display pending KMP implementation)", style = MaterialTheme.typography.bodyMedium)
                    /*
                        val imageBitmap =
                                remember(state.photoBase64) {
                                        try {
                                                val decodedBytes =
                                                        Base64.decode(
                                                                state.photoBase64,
                                                                Base64.DEFAULT
                                                        )
                                                BitmapFactory.decodeByteArray(
                                                                decodedBytes,
                                                                0,
                                                                decodedBytes.size
                                                        )
                                                        .asImageBitmap()
                                        } catch (e: Exception) {
                                                null
                                        }
                                }

                        if (imageBitmap != null) {
                                Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.fillMaxWidth()
                                ) {
                                        Image(
                                                bitmap = imageBitmap,
                                                contentDescription = "Authorized Photo from QR",
                                                modifier =
                                                        Modifier.size(120.dp)
                                                                .padding(8.dp)
                                                                .border(
                                                                        2.dp,
                                                                        MaterialTheme.colorScheme
                                                                                .primary,
                                                                        RoundedCornerShape(8.dp)
                                                                )
                                                                .clip(RoundedCornerShape(8.dp)),
                                                contentScale = ContentScale.Fit
                                        )
                                        Text(
                                                "Photo from QR",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary
                                        )
                                }
                                HorizontalDivider()
                        }
                    */
                }

                HorizontalDivider()

                var isUidVisible by remember { mutableStateOf(false) }

                // Aadhaar Number Section
                Text("Aadhaar Details", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                        value = state.uid,
                        onValueChange = { state.uid = it },
                        label = { Text("Aadhaar Number") },
                        placeholder = if (state.maskedAadhaarNumber.isNotEmpty()) {
                            { Text(state.maskedAadhaarNumber) }
                        } else null,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation =
                                if (isUidVisible)
                                        androidx.compose.ui.text.input.VisualTransformation.None
                                else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        keyboardOptions =
                                androidx.compose.foundation.text.KeyboardOptions(
                                        keyboardType =
                                                androidx.compose.ui.text.input.KeyboardType
                                                        .NumberPassword
                                ),
                        supportingText = {
                            if (state.uid.isEmpty() && state.maskedAadhaarNumber.isNotEmpty()) {
                                Text("Full UID not found in QR. Please enter 12 digits or scan front/back.")
                            }
                        },
                        trailingIcon = {
                                val image =
                                        if (isUidVisible) Icons.Filled.Visibility
                                        else Icons.Filled.VisibilityOff
                                val description =
                                        if (isUidVisible) "Hide Aadhaar" else "Show Aadhaar"

                                IconButton(onClick = { isUidVisible = !isUidVisible }) {
                                        Icon(imageVector = image, contentDescription = description)
                                }
                        }
                )

                OutlinedTextField(
                        value = state.vid,
                        onValueChange = { state.vid = it },
                        label = { Text("VID (16 digits)") },
                        modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider()

                // Personal Details Section
                Text("Personal Details", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                        value = state.holderName,
                        onValueChange = { state.holderName = it },
                        label = { Text("Name (as on Aadhaar)") },
                        modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                                value = state.rawDob,
                                onValueChange = {
                                    if (it.length <= 8 && it.all { char -> char.isDigit() }) {
                                        state.rawDob = it
                                        state.dobError = !DateUtils.isValidDate(it, DateFormatType.INDIA) && it.length == 8
                                    }
                                },
                                label = { Text("DOB (DD/MM/YYYY)") },
                                modifier = Modifier.weight(1f),
                                visualTransformation = dateVisualTransformation,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = state.dobError
                        )
                        OutlinedTextField(
                                value = state.gender,
                                onValueChange = { state.gender = it },
                                label = { Text("Gender") },
                                modifier = Modifier.weight(1f)
                        )
                }

                HorizontalDivider()

                // Contact Details Section
                Text("Contact Details", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                        value = state.mobile,
                        onValueChange = { state.mobile = it },
                        label = { Text("Mobile Number") },
                        keyboardOptions =
                                androidx.compose.foundation.text.KeyboardOptions(
                                        keyboardType =
                                                androidx.compose.ui.text.input.KeyboardType.Phone
                                ),
                        modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                        value = state.email,
                        onValueChange = { state.email = it },
                        label = { Text("Email Address") },
                        keyboardOptions =
                                androidx.compose.foundation.text.KeyboardOptions(
                                        keyboardType =
                                                androidx.compose.ui.text.input.KeyboardType.Email
                                ),
                        modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider()

                // Address Section
                Text("Address", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                        value = state.address,
                        onValueChange = { state.address = it },
                        label = { Text("Address") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                )

                OutlinedTextField(
                        value = state.pincode,
                        onValueChange = { state.pincode = it },
                        label = { Text("Pincode") },
                        modifier = Modifier.fillMaxWidth()
                )

                // Optional: Enrollment Number
                OutlinedTextField(
                        value = state.enrollmentNumber,
                        onValueChange = { state.enrollmentNumber = it },
                        label = { Text("Enrollment Number (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                        onClick = {
                                onSave()
                        },
                        modifier = Modifier.fillMaxWidth()
                ) { Text("Save Aadhaar Card") }
        }
}
