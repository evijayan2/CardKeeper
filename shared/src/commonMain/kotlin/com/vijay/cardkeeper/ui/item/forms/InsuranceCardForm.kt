package com.vijay.cardkeeper.ui.item.forms

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.vijay.cardkeeper.data.entity.InsuranceCard
import com.vijay.cardkeeper.data.entity.InsuranceCardType
import com.vijay.cardkeeper.ui.common.CardKeeperSaveButton
import com.vijay.cardkeeper.ui.common.CardKeeperScanButtons
import com.vijay.cardkeeper.ui.common.CardKeeperTextField

class InsuranceCardFormState(
    initialCard: InsuranceCard? = null
) {
    var type by mutableStateOf(initialCard?.type ?: InsuranceCardType.MEDICAL)
    var providerName by mutableStateOf(initialCard?.providerName ?: "")
    var planName by mutableStateOf(initialCard?.planName ?: "")
    var policyNumber by mutableStateOf(initialCard?.policyNumber ?: "")
    var groupNumber by mutableStateOf(initialCard?.groupNumber ?: "")
    var memberId by mutableStateOf(initialCard?.memberId ?: "")
    var policyHolderName by mutableStateOf(initialCard?.policyHolderName ?: "")
    var expiryDate by mutableStateOf(initialCard?.expiryDate ?: "")
    var website by mutableStateOf(initialCard?.website ?: "")
    var customerServiceNumber by mutableStateOf(initialCard?.customerServiceNumber ?: "")
    var notes by mutableStateOf(initialCard?.notes ?: "")

    // Images
    var frontPath by mutableStateOf(initialCard?.frontImagePath)
    var backPath by mutableStateOf(initialCard?.backImagePath)
    
    var hasFrontImage by mutableStateOf(initialCard?.frontImagePath != null)
    var hasBackImage by mutableStateOf(initialCard?.backImagePath != null)
}

@Composable
fun rememberInsuranceCardFormState(
    card: InsuranceCard? = null
): InsuranceCardFormState {
    return remember(card) { InsuranceCardFormState(card) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsuranceCardForm(
    state: InsuranceCardFormState,
    onScanFront: () -> Unit,
    onScanBack: () -> Unit,
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

        Text("Type", style = MaterialTheme.typography.labelLarge)

        // Type Selection
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            InsuranceCardType.values().forEach { type ->
                FilterChip(
                    selected = state.type == type,
                    onClick = { state.type = type },
                    label = { Text(type.name) }
                )
            }
        }

        CardKeeperTextField(
            value = state.providerName,
            onValueChange = { state.providerName = it },
            label = "Provider Name (e.g. Blue Cross)"
        )

        CardKeeperTextField(
            value = state.policyNumber,
            onValueChange = { state.policyNumber = it },
            label = "Policy Number"
        )
        
        CardKeeperTextField(
            value = state.groupNumber,
            onValueChange = { state.groupNumber = it },
            label = "Group Number",
        )

        CardKeeperTextField(
             value = state.memberId,
             onValueChange = { state.memberId = it },
             label = "Member ID"
        )

        CardKeeperTextField(
            value = state.policyHolderName,
            onValueChange = { state.policyHolderName = it },
            label = "Policy Holder Name"
        )
        
        CardKeeperTextField(
             value = state.planName,
             onValueChange = { state.planName = it },
             label = "Plan Name (e.g. PPO)"
        )

        CardKeeperTextField(
            value = state.expiryDate,
            onValueChange = { state.expiryDate = it },
            label = "Expiry Date"
        )
        
        CardKeeperTextField(
             value = state.customerServiceNumber,
             onValueChange = { state.customerServiceNumber = it },
             label = "Customer Service Number",
             keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )

        CardKeeperTextField(
            value = state.notes,
            onValueChange = { state.notes = it },
            label = "Notes",
            minLines = 3,
            maxLines = 5
        )

        CardKeeperSaveButton(
            onClick = { onSave() },
            text = "Save Insurance Card"
        )
    }
}
