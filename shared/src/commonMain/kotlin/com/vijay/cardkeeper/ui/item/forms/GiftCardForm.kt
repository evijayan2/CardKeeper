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
import com.vijay.cardkeeper.ui.common.CardKeeperTextField
import com.vijay.cardkeeper.ui.common.CardKeeperScanButtons
import com.vijay.cardkeeper.ui.common.CardKeeperSaveButton

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
        CardKeeperScanButtons(
            hasFrontImage = state.hasFrontImage,
            onScanFront = onScanFront,
            hasBackImage = state.hasBackImage,
            onScanBack = onScanBack
        )

        // 2. Provider Name
        CardKeeperTextField(
            value = state.providerName,
            onValueChange = { 
                state.providerName = it 
                state.providerNameError = false
            },
            label = "Provider Name (e.g., Amazon)",
            isError = state.providerNameError,
            supportingText = { if (state.providerNameError) Text("Required") }
        )

        // 3. Card Number (Manual Entry, No Scanner)
        CardKeeperTextField(
            value = state.cardNumber,
            onValueChange = { 
                state.cardNumber = it
                state.cardNumberError = false
            },
            label = "Gift Card Code (Optional)",
            isError = state.cardNumberError,
            // supportingText = { if (state.cardNumberError) Text("Required") } // Optional now
        )

        // 4. PIN
        // 4. PIN
        CardKeeperTextField(
            value = state.pin,
            onValueChange = { state.pin = it },
            label = "PIN (Optional)"
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        // 5. Scan Data (Barcode/QR)
        Text("Scan Data", style = MaterialTheme.typography.titleMedium)
        
        CardKeeperTextField(
            value = state.barcode ?: state.qrCode ?: "",
            onValueChange = { 
                // Mostly read-only from scan, but allowing edit if specifically barcode
                state.barcode = it 
            },
            label = if (state.qrCode != null) "QR Code Content" else "Barcode Content",
            placeholder = { Text("Scan to populate") },
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
        // 6. Notes
        CardKeeperTextField(
            value = state.notes,
            onValueChange = { state.notes = it },
            label = "Notes (Optional)",
            minLines = 2
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        CardKeeperSaveButton(
            onClick = { onSave() },
            text = "Save Gift Card"
        )
    }
}
