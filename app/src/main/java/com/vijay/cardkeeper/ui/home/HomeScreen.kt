package com.vijay.cardkeeper.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vijay.cardkeeper.data.entity.FinancialAccount
import com.vijay.cardkeeper.data.entity.IdentityDocument
import com.vijay.cardkeeper.ui.viewmodel.AppViewModelProvider
import com.vijay.cardkeeper.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
        navigateToItemEntry: (Int, String?) -> Unit,
        navigateToItemView: (Int) -> Unit,
        navigateToIdentityView: (Int) -> Unit,
        viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val financialState by viewModel.financialAccounts.collectAsState(initial = emptyList())
    val identityState by viewModel.identityDocuments.collectAsState(initial = emptyList())
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Finance", "Identity")

    // FAB Menu State
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
            topBar = {
                TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                        painter =
                                                painterResource(
                                                        id =
                                                                com.vijay
                                                                        .cardkeeper
                                                                        .R
                                                                        .mipmap
                                                                        .ic_app_logo_1
                                                ),
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp).padding(end = 8.dp)
                                )
                                Text("Kards")
                            }
                        }
                )
            },
            floatingActionButton = {
                Box {
                    FloatingActionButton(onClick = { showMenu = true }) {
                        Icon(Icons.Filled.Add, "Add Item")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                                text = { Text("Add Credit/Debit Card") },
                                onClick = {
                                    showMenu = false
                                    navigateToItemEntry(0, "CREDIT_CARD")
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.CreditCard, contentDescription = null)
                                }
                        )
                        DropdownMenuItem(
                                text = { Text("Bank Account") },
                                onClick = {
                                    showMenu = false
                                    navigateToItemEntry(0, "BANK_ACCOUNT")
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.AccountBalance, contentDescription = null)
                                }
                        )
                        DropdownMenuItem(
                                text = { Text("Rewards Card") },
                                onClick = {
                                    showMenu = false
                                    navigateToItemEntry(0, "REWARDS_CARD")
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.CardGiftcard, contentDescription = null)
                                }
                        )
                        Divider()
                        DropdownMenuItem(
                                text = { Text("Driver License") },
                                onClick = {
                                    showMenu = false
                                    navigateToItemEntry(1, "DRIVER_LICENSE")
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.DirectionsCar, contentDescription = null)
                                }
                        )
                        DropdownMenuItem(
                                text = { Text("Passport") },
                                onClick = {
                                    showMenu = false
                                    navigateToItemEntry(1, "PASSPORT")
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.AccountBox, contentDescription = null)
                                }
                        )
                        DropdownMenuItem(
                                text = { Text("Other Identity") },
                                onClick = {
                                    showMenu = false
                                    navigateToItemEntry(1, null)
                                },
                                leadingIcon = { Icon(Icons.Filled.Face, contentDescription = null) }
                        )
                    }
                }
            }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) },
                            icon = {
                                Icon(
                                        if (index == 0) Icons.Default.Home else Icons.Default.Face,
                                        contentDescription = null
                                )
                            }
                    )
                }
            }

            when (selectedTab) {
                0 -> FinancialList(financialState, navigateToItemView)
                1 -> IdentityList(identityState, navigateToIdentityView)
            }
        }
    }
}

@Composable
fun FinancialList(list: List<FinancialAccount>, onItemClick: (Int) -> Unit) {
    LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
    ) { items(list) { account -> FinancialAccountItem(account, onItemClick) } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialAccountItem(account: FinancialAccount, onItemClick: (Int) -> Unit) {
    Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            onClick = { onItemClick(account.id) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                                text = account.institutionName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                        ) {
                            val typeText =
                                    if (account.type ==
                                                    com.vijay.cardkeeper.data.entity.AccountType
                                                            .BANK_ACCOUNT &&
                                                    account.accountSubType != null
                                    ) {
                                        account.accountSubType.name
                                    } else {
                                        account.type.name
                                    }

                            Text(
                                    text =
                                            typeText.split("_").joinToString(" ") {
                                                it.lowercase().replaceFirstChar { char ->
                                                    char.uppercase()
                                                }
                                            },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                            text = account.holderName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                            text = account.accountName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Card Network Logo
                val logoResId = getCardLogoResId(account.cardNetwork)
                if (logoResId != null) {
                    androidx.compose.foundation.Image(
                            painter = painterResource(id = logoResId),
                            contentDescription = account.cardNetwork,
                            modifier = Modifier.width(40.dp).height(24.dp),
                            contentScale = ContentScale.Fit
                    )
                } else if (!account.cardNetwork.isNullOrBlank()) {
                    Text(
                            text = account.cardNetwork,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Account Details
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                if (account.type == com.vijay.cardkeeper.data.entity.AccountType.REWARDS_CARD) {
                    Column {
                        if (!account.barcode.isNullOrBlank()) {
                            Text(
                                    text = "Barcode: ${account.barcode}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                        if (!account.linkedPhoneNumber.isNullOrBlank()) {
                            Text(
                                    text = "Phone: ${account.linkedPhoneNumber}",
                                    style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                } else {
                    Text(
                            text = "•••• ${account.number.takeLast(4)}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                    )

                    if (account.expiryDate != null) {
                        Text(
                                text = "Exp: ${account.expiryDate}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Images
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                account.frontImagePath?.let { path ->
                    DashboardImageThumbnail(path = path, label = "Front")
                }
                account.backImagePath?.let { path ->
                    DashboardImageThumbnail(path = path, label = "Back")
                }
            }
        }
    }
}

@Composable
fun getCardLogoResId(network: String?): Int? {
    if (network == null) return null
    return when {
        network.contains("Visa", ignoreCase = true) -> com.vijay.cardkeeper.R.drawable.ic_brand_visa
        network.contains("Master", ignoreCase = true) ->
                com.vijay.cardkeeper.R.drawable.ic_brand_mastercard
        network.contains("Amex", ignoreCase = true) -> com.vijay.cardkeeper.R.drawable.ic_brand_amex
        network.contains("Discover", ignoreCase = true) ->
                com.vijay.cardkeeper.R.drawable.ic_brand_discover
        network.contains("Capital", ignoreCase = true) ->
                com.vijay
                        .cardkeeper
                        .R
                        .drawable
                        .ic_brand_capitolone // Handling typo in filename is risky, but user said
        // they added it. Filename was
        // 'ic_brand_capitolone.jpg' which might be problematic
        // as a drawable resource name if not png? Android
        // resources flatten extensions, so
        // R.drawable.ic_brand_capitolone should work.
        network.contains("Rupay", ignoreCase = true) ->
                com.vijay.cardkeeper.R.drawable.ic_brand_rupay
        else -> null
    }
}

@Composable
fun IdentityList(list: List<IdentityDocument>, onItemClick: (Int) -> Unit) {
    LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
    ) { items(list) { doc -> IdentityItem(doc, onItemClick) } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdentityItem(doc: IdentityDocument, onItemClick: (Int) -> Unit) {
    Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            onClick = { onItemClick(doc.id) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                            text = doc.type.name.replace("_", " "),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                    )
                    Text(
                            text = doc.country,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (doc.docNumber.isNotEmpty()) {
                    Text(
                            text = doc.docNumber,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Details
            if (doc.holderName.isNotEmpty()) {
                Text(text = "Name: ${doc.holderName}", style = MaterialTheme.typography.bodyMedium)
            }
            if (!doc.dob.isNullOrEmpty()) {
                Text(text = "DOB: ${doc.dob}", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Images
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                doc.frontImagePath?.let { path ->
                    DashboardImageThumbnail(path = path, label = "Front")
                }
                doc.backImagePath?.let { path ->
                    DashboardImageThumbnail(path = path, label = "Back")
                }
            }
        }
    }
}

@Composable
fun DashboardImageThumbnail(path: String, label: String) {
    val bitmap =
            remember(path) {
                try {
                    android.graphics.BitmapFactory.decodeFile(path)
                } catch (e: Exception) {
                    null
                }
            }

    if (bitmap != null) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            androidx.compose.foundation.Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = label,
                    modifier =
                            Modifier.height(100.dp)
                                    .width(150.dp) // Aspect ratio approx id card
                                    .clip(
                                            androidx.compose.foundation.shape.RoundedCornerShape(
                                                    8.dp
                                            )
                                    ),
                    contentScale = ContentScale.Crop
            )
            Text(text = label, style = MaterialTheme.typography.labelSmall)
        }
    }
}
