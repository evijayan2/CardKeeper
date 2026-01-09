package com.vijay.cardkeeper.ui.item.forms

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.vijay.cardkeeper.data.entity.AccountType
import com.vijay.cardkeeper.data.entity.RewardCard
import com.vijay.cardkeeper.ui.common.CardKeeperSaveButton
import com.vijay.cardkeeper.ui.common.CardKeeperScanButtons
import com.vijay.cardkeeper.ui.common.CardKeeperTextField

class RewardsFormState(
    initialReward: RewardCard? = null,
    initialType: AccountType = AccountType.REWARDS_CARD
) {
    var type by mutableStateOf(initialReward?.type ?: initialType)
    var institution by mutableStateOf(initialReward?.name ?: "")
    var barcode by mutableStateOf(initialReward?.barcode ?: "")
    var barcodeFormat by mutableStateOf(initialReward?.barcodeFormat)
    var linkedPhone by mutableStateOf(initialReward?.linkedPhoneNumber ?: "")
    var notes by mutableStateOf(initialReward?.notes ?: "")

    // Images
    var frontPath by mutableStateOf(initialReward?.frontImagePath)
    var backPath by mutableStateOf(initialReward?.backImagePath)
    var logoPath by mutableStateOf(initialReward?.logoImagePath)

    var hasFrontImage by mutableStateOf(initialReward?.frontImagePath != null)
    var hasBackImage by mutableStateOf(initialReward?.backImagePath != null)
    var hasLogoImage by mutableStateOf(initialReward?.logoImagePath != null)
}

@Composable
fun rememberRewardsFormState(
    reward: RewardCard? = null,
    initialType: AccountType = AccountType.REWARDS_CARD
): RewardsFormState {
    return remember(reward, initialType) { RewardsFormState(reward, initialType) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardsForm(
    state: RewardsFormState,
    onScanFront: () -> Unit,
    onScanBack: () -> Unit,
    onPickLogo: () -> Unit,
    onScanBarcode: () -> Unit,
    onSave: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        
        // Scan Buttons
        CardKeeperScanButtons(
            hasFrontImage = state.hasFrontImage,
            onScanFront = onScanFront,
            hasBackImage = state.hasBackImage,
            onScanBack = onScanBack
        )

        Text("Card Type", style = MaterialTheme.typography.labelLarge)

        // Type Selection
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            val displayedTypes = listOf(AccountType.REWARDS_CARD, AccountType.LIBRARY_CARD)

            displayedTypes.forEach { type ->
                FilterChip(
                    selected = state.type == type,
                    onClick = { state.type = type },
                    label = { Text(type.name.replace("_", " ")) }
                )
            }
        }

        CardKeeperTextField(
            value = state.institution,
            onValueChange = { state.institution = it },
            label = if (state.type == AccountType.REWARDS_CARD) "Shop Name (e.g. Starbucks)" else "Library Name"
        )

        CardKeeperTextField(
            value = state.barcode,
            onValueChange = { state.barcode = it },
            label = "Barcode Number",
            trailingIcon = {
                IconButton(
                    onClick = onScanBarcode
                ) { Icon(Icons.Filled.PhotoCamera, "Scan Barcode") }
            }
        )

        CardKeeperTextField(
            value = state.linkedPhone,
            onValueChange = { state.linkedPhone = it },
            label = "Linked Phone Number",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )

        // Logo Picker
        Button(
            onClick = onPickLogo,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        ) {
            Text(
                if (state.hasLogoImage) "Change Logo"
                else "Pick Logo"
            )
        }

        CardKeeperTextField(
            value = state.notes,
            onValueChange = { state.notes = it },
            label = "Notes",
            minLines = 3,
            maxLines = 5
        )

        val saveButtonText = if (state.type == AccountType.REWARDS_CARD) "Save Rewards Card" else "Save Library Card"

        CardKeeperSaveButton(
            onClick = { onSave() },
            text = saveButtonText
        )
    }
}
