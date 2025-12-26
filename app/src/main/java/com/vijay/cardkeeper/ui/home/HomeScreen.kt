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
import com.vijay.cardkeeper.data.entity.Passport
import com.vijay.cardkeeper.ui.viewmodel.AppViewModelProvider
import com.vijay.cardkeeper.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
        navigateToItemEntry: (Int, String?) -> Unit,
        navigateToItemView: (Int) -> Unit,
        navigateToIdentityView: (Int) -> Unit,
        navigateToPassportView: (Int) -> Unit =
                {}, // Default empty for now to avoid breaking callers immediately
        viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val financialState by viewModel.financialAccounts.collectAsState(initial = emptyList())
    val identityState by viewModel.identityDocuments.collectAsState(initial = emptyList())
    val passportState by viewModel.passports.collectAsState(initial = emptyList())
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Finance", "Identity", "Passports")

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
                        HorizontalDivider()
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
                                    navigateToItemEntry(2, "PASSPORT") // Category 2 for Passport
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
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) },
                            icon = {
                                Icon(
                                        when (index) {
                                            0 -> Icons.Default.Home
                                            1 -> Icons.Default.Face
                                            else -> Icons.Default.AccountBox
                                        },
                                        contentDescription = null
                                )
                            }
                    )
                }
            }

            when (selectedTab) {
                0 -> FinancialList(financialState, navigateToItemView)
                1 -> IdentityList(identityState, navigateToIdentityView)
                2 -> PassportList(passportState, navigateToPassportView)
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
    val cardColor = getAccountColor(account)
    // Determine content color based on card color luminance or hardcoded logic
    // For these specific dark/brand colors, White is usually best.
    val contentColor =
            if (cardColor == MaterialTheme.colorScheme.surface) MaterialTheme.colorScheme.onSurface
            else androidx.compose.ui.graphics.Color.White

    Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors =
                    CardDefaults.cardColors(
                            containerColor = cardColor,
                            contentColor = contentColor
                    ),
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
                                fontWeight = FontWeight.Bold,
                                color = contentColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                color =
                                        MaterialTheme.colorScheme.tertiaryContainer.copy(
                                                alpha = 0.8f
                                        ), // Slight transparency to blend
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
                            color = contentColor.copy(alpha = 0.8f)
                    )
                    Text(
                            text = account.accountName,
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor.copy(alpha = 0.7f)
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
                            color = contentColor
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
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    color = contentColor
                            )
                        }
                        if (!account.linkedPhoneNumber.isNullOrBlank()) {
                            Text(
                                    text = "Phone: ${account.linkedPhoneNumber}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = contentColor.copy(alpha = 0.8f)
                            )
                        }
                    }
                } else {
                    Text(
                            text = "•••• ${account.number.takeLast(4)}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = contentColor
                    )

                    if (account.expiryDate != null) {
                        Text(
                                text = "Exp: ${account.expiryDate}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                color = contentColor.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Images
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                account.frontImagePath?.let { path ->
                    DashboardImageThumbnail(path = path, label = "Front", textColor = contentColor)
                }
                account.backImagePath?.let { path ->
                    DashboardImageThumbnail(path = path, label = "Back", textColor = contentColor)
                }
            }
        }
    }
}

@Composable
fun getAccountColor(account: FinancialAccount): androidx.compose.ui.graphics.Color {
    // 1. Check for manual color override (if we add this feature later, using colorTheme)
    if (account.colorTheme != null) {
        return androidx.compose.ui.graphics.Color(account.colorTheme)
    }

    // 2. Check for Bank Brand Color
    if (account.bankBrandColor != null) {
        return androidx.compose.ui.graphics.Color(account.bankBrandColor)
    }

    // 3. Derive from Network / Institution
    val network = account.cardNetwork
    return when {
        network?.contains("Visa", ignoreCase = true) == true ->
                androidx.compose.ui.graphics.Color(0xFF1A1F71) // Visa Blue
        network?.contains("Master", ignoreCase = true) == true ->
                androidx.compose.ui.graphics.Color(0xFF222222) // Master (Dark)
        network?.contains("Amex", ignoreCase = true) == true ->
                androidx.compose.ui.graphics.Color(0xFF006FCF) // Amex Blue
        network?.contains("Discover", ignoreCase = true) == true ->
                androidx.compose.ui.graphics.Color(0xFFE55C20) // Discover Orange
        network?.contains("Rupay", ignoreCase = true) == true ->
                androidx.compose.ui.graphics.Color(0xFF1B3F6B) // Rupay Dark Blue

        // Brand based colors (Simple heuristics)
        account.institutionName.contains("Chase", ignoreCase = true) ->
                androidx.compose.ui.graphics.Color(0xFF117ACA) // Chase Blue
        account.institutionName.contains("Citi", ignoreCase = true) ->
                androidx.compose.ui.graphics.Color(0xFF003B70) // Citi Blue
        account.institutionName.contains("Wells", ignoreCase = true) ->
                androidx.compose.ui.graphics.Color(0xFFCD1409) // Wells Fargo Red
        account.institutionName.contains("Boa", ignoreCase = true) ||
                account.institutionName.contains("Bank of America", ignoreCase = true) ->
                androidx.compose.ui.graphics.Color(0xFFDC1431) // BoA Red
        else -> MaterialTheme.colorScheme.surface
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
fun PassportList(list: List<Passport>, onItemClick: (Int) -> Unit) {
    LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
    ) { items(list) { passport -> PassportItem(passport, { onItemClick(passport.id) }) } }
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
fun DashboardImageThumbnail(
        path: String,
        label: String,
        textColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
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
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = textColor)
        }
    }
}
