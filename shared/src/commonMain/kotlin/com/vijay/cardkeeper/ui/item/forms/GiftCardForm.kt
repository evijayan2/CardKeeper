package com.vijay.cardkeeper.ui.item.forms

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.vijay.cardkeeper.data.entity.GiftCard

class GiftCardFormState(initialItem: GiftCard?) {
    var providerName by mutableStateOf(initialItem?.providerName ?: "")
    var cardNumber by mutableStateOf(initialItem?.cardNumber ?: "")
    var pin by mutableStateOf(initialItem?.pin ?: "")
    var notes by mutableStateOf(initialItem?.notes ?: "")
    
    // Images
    var frontPath by mutableStateOf(initialItem?.frontImagePath)
    var backPath by mutableStateOf(initialItem?.backImagePath)
    var logoPath by mutableStateOf(initialItem?.logoImagePath)
    var hasFrontImage by mutableStateOf(initialItem?.frontImagePath != null)
    var hasBackImage by mutableStateOf(initialItem?.backImagePath != null)
    var hasLogoImage by mutableStateOf(initialItem?.logoImagePath != null)

    // Barcode
    var barcode by mutableStateOf(initialItem?.barcode)
    var barcodeFormat by mutableStateOf(initialItem?.barcodeFormat)
    var qrCode by mutableStateOf(initialItem?.qrCode)

    var providerNameError by mutableStateOf(false)
    var cardNumberError by mutableStateOf(false)

    fun validate(): Boolean {
        providerNameError = providerName.isBlank()
        // cardNumberError = cardNumber.isBlank() // Removed validation as per user request
        return !providerNameError
    }
}

@Composable
fun rememberGiftCardFormState(initialItem: GiftCard?): GiftCardFormState {
    return remember(initialItem) { GiftCardFormState(initialItem) }
}

@Composable
fun GiftCardForm(
    state: GiftCardFormState,
    onScanFront: () -> Unit,
    onScanBack: () -> Unit,
    onScanBarcode: () -> Unit,
    onSave: () -> Unit,
    onNavigateBack: () -> Unit
) {

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        
        // 1. Top - Image Scan Buttons (Matching FinancialForm)
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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.PhotoCamera, "Front")
                    Text(if (state.hasFrontImage) "Front Captured" else "Scan Front")
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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.PhotoCamera, "Back")
                    Text(if (state.hasBackImage) "Back Captured" else "Scan Back")
                }
            }
        }

        // 2. Provider Name
        OutlinedTextField(
            value = state.providerName,
            onValueChange = { 
                state.providerName = it 
                state.providerNameError = false
            },
            label = { Text("Provider Name (e.g., Amazon)") },
            modifier = Modifier.fillMaxWidth(),
            isError = state.providerNameError,
            supportingText = { if (state.providerNameError) Text("Required") }
        )

        // 3. Card Number (Manual Entry, No Scanner)
        OutlinedTextField(
            value = state.cardNumber,
            onValueChange = { 
                state.cardNumber = it
                state.cardNumberError = false
            },
            label = { Text("Gift Card Code (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            isError = state.cardNumberError,
            // supportingText = { if (state.cardNumberError) Text("Required") } // Optional now
        )

        // 4. PIN
        OutlinedTextField(
            value = state.pin,
            onValueChange = { state.pin = it },
            label = { Text("PIN (Optional)") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        // 5. Scan Data (Barcode/QR)
        Text("Scan Data", style = MaterialTheme.typography.titleMedium)
        
        OutlinedTextField(
            value = state.barcode ?: state.qrCode ?: "",
            onValueChange = { 
                // Mostly read-only from scan, but allowing edit if specifically barcode
                state.barcode = it 
            },
            label = { Text(if (state.qrCode != null) "QR Code Content" else "Barcode Content") },
            placeholder = { Text("Scan to populate") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(
                    onClick = onScanBarcode
                ) { Icon(Icons.Filled.PhotoCamera, "Scan Code") }
            }
        )
        
        if (state.qrCode != null) {
            Text(
                text = "QR Code Format Detected", 
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        // 6. Notes
        OutlinedTextField(
            value = state.notes,
            onValueChange = { state.notes = it },
            label = { Text("Notes (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                onSave()
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Save Gift Card") }
    }
}
