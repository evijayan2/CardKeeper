package com.vijay.cardkeeper.ui.item

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vijay.cardkeeper.ui.item.forms.*
import com.vijay.cardkeeper.ui.common.CardKeeperTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(
    financialState: FinancialFormState,
    rewardsState: RewardsFormState,
    identityState: IdentityFormState,
    passportState: PassportFormState,
    greenCardState: GreenCardFormState,
    aadharCardState: AadharCardFormState,
    giftCardState: GiftCardFormState,
    panCardState: PanCardFormState,
    selectedCategory: Int,
    onCategorySelected: (Int) -> Unit,
    onScanRequest: (category: Int, requestType: ScanRequestType) -> Unit,
    onSave: () -> Unit,
    onNavigateBack: () -> Unit,
    isEditing: Boolean,
    showCategoryTabs: Boolean = !isEditing,
    title: String = if (isEditing) "Edit Item" else "Add Item",
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        topBar = {
            CardKeeperTopBar(
                title = title,
                onNavigateBack = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            
            // Category Tabs
            if (showCategoryTabs) {
                ScrollableTabRow(selectedTabIndex = selectedCategory, modifier = Modifier.fillMaxWidth()) {
                    Tab(selected = selectedCategory == 0, onClick = { onCategorySelected(0) }, text = { Text("Financial") })
                    Tab(selected = selectedCategory == 1, onClick = { onCategorySelected(1) }, text = { Text("Identity") })
                    Tab(selected = selectedCategory == 2, onClick = { onCategorySelected(2) }, text = { Text("Passport") })
                    Tab(selected = selectedCategory == 3, onClick = { onCategorySelected(3) }, text = { Text("Rewards") })
                    Tab(selected = selectedCategory == 4, onClick = { onCategorySelected(4) }, text = { Text("Green Card") })
                    Tab(selected = selectedCategory == 5, onClick = { onCategorySelected(5) }, text = { Text("Aadhaar") })
                    Tab(selected = selectedCategory == 6, onClick = { onCategorySelected(6) }, text = { Text("Gift Card") })
                    Tab(selected = selectedCategory == 7, onClick = { onCategorySelected(7) }, text = { Text("PAN Card") })
                }
            } else if (isEditing) {
                 // If editing, show the category title as a locked header or just rely on TopBar
                 val title = when(selectedCategory) {
                     0 -> "Financial Account"
                     1 -> "Identity Document"
                     2 -> "Passport"
                     3 -> "Rewards Card"
                     4 -> "Green Card"
                     5 -> "Aadhaar Card"
                     6 -> "Gift Card"
                     7 -> "PAN Card"
                     else -> "Item"
                 }
                 Text(title, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(vertical = 8.dp))
            }

            // Forms
            when (selectedCategory) {
                0 -> { // Financial
                     FinancialForm(
                         state = financialState,
                         onScanFront = { onScanRequest(selectedCategory, ScanRequestType.FRONT) },
                         onScanBack = { onScanRequest(selectedCategory, ScanRequestType.BACK) },
                         onSave = onSave,
                         onNavigateBack = onNavigateBack
                     )
                }
                3 -> { // Rewards
                     RewardsForm(
                         state = rewardsState,
                         onScanFront = { onScanRequest(selectedCategory, ScanRequestType.FRONT) },
                         onScanBack = { onScanRequest(selectedCategory, ScanRequestType.BACK) },
                         onPickLogo = { onScanRequest(selectedCategory, ScanRequestType.PICK_LOGO) },
                         onScanBarcode = { onScanRequest(selectedCategory, ScanRequestType.BARCODE) },
                         onSave = onSave,
                         onNavigateBack = onNavigateBack
                     )
                }
                1 -> { // Identity
                     IdentityForm(
                         state = identityState,
                         onScanFront = { onScanRequest(1, ScanRequestType.FRONT) },
                         onScanBack = { onScanRequest(1, ScanRequestType.BACK) },
                         onScanBarcode = { onScanRequest(1, ScanRequestType.BARCODE) }, // Driver License Barcode
                         onSave = onSave,
                         onNavigateBack = onNavigateBack
                     )
                }
                2 -> { // Passport
                     PassportForm(
                         state = passportState,
                         onScanFront = { onScanRequest(2, ScanRequestType.FRONT) },
                         onScanBack = { onScanRequest(2, ScanRequestType.BACK) },
                         onSave = onSave,
                         onNavigateBack = onNavigateBack
                     )
                }
                4 -> { // Green Card
                     GreenCardForm(
                         state = greenCardState,
                         onScanFront = { onScanRequest(4, ScanRequestType.FRONT) },
                         onScanBack = { onScanRequest(4, ScanRequestType.BACK) },
                         onSave = onSave,
                         onNavigateBack = onNavigateBack
                     )
                }
                5 -> { // Aadhaar
                     AadharCardForm(
                         state = aadharCardState,
                         onScanFront = { onScanRequest(5, ScanRequestType.FRONT) },
                         onScanBack = { onScanRequest(5, ScanRequestType.BACK) },
                         onScanQr = { onScanRequest(5, ScanRequestType.QR) },
                         onSave = onSave,
                         onNavigateBack = onNavigateBack
                     )
                }
                6 -> { // Gift Card
                     GiftCardForm(
                         state = giftCardState,
                         onScanFront = { onScanRequest(6, ScanRequestType.FRONT) },
                         onScanBack = { onScanRequest(6, ScanRequestType.BACK) },
                         onScanBarcode = { onScanRequest(6, ScanRequestType.BARCODE) }, // Code scan
                         onSave = onSave,
                         onNavigateBack = onNavigateBack
                     )
                }
                7 -> { // PAN Card
                     PanCardForm(
                         state = panCardState,
                         onScanOcr = { onScanRequest(7, ScanRequestType.FRONT) }, // Use FRONT for OCR scan
                         onScanFront = { onScanRequest(7, ScanRequestType.FRONT) },
                         onScanBack = { onScanRequest(7, ScanRequestType.BACK) },
                         onSave = onSave,
                         onNavigateBack = onNavigateBack
                     )
                }
            }
        }
    }
}
