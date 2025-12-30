package com.vijay.cardkeeper.ui.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.vijay.cardkeeper.R
import com.vijay.cardkeeper.data.entity.AccountType
import com.vijay.cardkeeper.data.entity.FinancialAccount
import com.vijay.cardkeeper.data.entity.IdentityDocument
import com.vijay.cardkeeper.data.entity.Passport
import com.vijay.cardkeeper.data.entity.GiftCard
import com.vijay.cardkeeper.ui.viewmodel.AppViewModelProvider
import com.vijay.cardkeeper.ui.viewmodel.HomeViewModel
import com.vijay.cardkeeper.util.LogoUtils
import com.vijay.cardkeeper.util.StateUtils
import java.io.File

import androidx.compose.runtime.saveable.rememberSaveable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
        navigateToItemEntry: (Int, String?) -> Unit,
        navigateToItemView: (Int) -> Unit,
        navigateToIdentityView: (Int) -> Unit,
        navigateToPassportView: (Int) -> Unit = {},
        navigateToGreenCardView: (Int) -> Unit = {},
        navigateToAadharView: (Int) -> Unit = {},
        navigateToGiftCardView: (Int) -> Unit = {},
        navigateToRewardsView: (Int) -> Unit = {},
        navigateToSearch: () -> Unit = {},
        navigateToSettings: () -> Unit = {},
        viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val bankAccounts by viewModel.bankAccounts.collectAsState(initial = emptyList())
    val rewardsCards by viewModel.rewardsCards.collectAsState(initial = emptyList())
    val identityState by viewModel.identityDocuments.collectAsState(initial = emptyList())
    val passportState by viewModel.passports.collectAsState(initial = emptyList())
    val greenCardState by viewModel.greenCards.collectAsState(initial = emptyList())
    val aadharCardState by viewModel.aadharCards.collectAsState(initial = emptyList())
    val giftCards by viewModel.giftCards.collectAsState(initial = emptyList())
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    // Update tabs list
    val tabs = listOf("Finance", "Identity", "Passports", "Rewards")

    // FAB Menu State
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
            topBar = {
                TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                        painter = painterResource(id = R.mipmap.ic_app_logo_1),
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp).padding(end = 8.dp)
                                )
                                Text("Kards")
                            }
                        },
                        actions = {
                            IconButton(onClick = navigateToSearch) {
                                Icon(Icons.Filled.Search, contentDescription = "Search")
                            }
                            IconButton(onClick = navigateToSettings) {
                                Icon(Icons.Filled.Settings, contentDescription = "Settings")
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
                                    // Use category 3 for Rewards if we separate it top-level,
                                    // or keep 0 and just set type.
                                    // Let's use 3 to match the tabs 0,1,2,3
                                    navigateToItemEntry(3, "REWARDS_CARD")
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.CardGiftcard, contentDescription = null)
                                }
                        )
                        DropdownMenuItem(
                                text = { Text("Gift Card") },
                                onClick = {
                                    showMenu = false
                                    navigateToItemEntry(6, "GIFT_CARD")
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
                                text = { Text("Green Card") },
                                onClick = {
                                    showMenu = false
                                    navigateToItemEntry(4, "GREEN_CARD")
                                },
                                leadingIcon = {
                                    Icon(
                                            Icons.Filled.Face,
                                            contentDescription = null
                                    ) // Using Face as placeholder
                                }
                        )
                        DropdownMenuItem(
                                text = { Text("Aadhaar Card") },
                                onClick = {
                                    showMenu = false
                                    navigateToItemEntry(5, "AADHAR")
                                },
                                leadingIcon = { Icon(Icons.Filled.Face, contentDescription = null) }
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
                            text = {
                                Text(
                                        title,
                                        maxLines = 1,
                                        softWrap = false,
                                        style = MaterialTheme.typography.labelSmall
                                )
                            },
                            icon = {
                                Icon(
                                        when (index) {
                                            0 -> Icons.Default.Home
                                            1 -> Icons.Default.Face
                                            2 -> Icons.Default.AccountBox
                                            else -> Icons.Default.CardGiftcard
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                )
                            }
                    )
                }
            }

            when (selectedTab) {
                0 -> FinancialList(bankAccounts, navigateToItemView)
                1 ->
                        IdentityList(
                                identityState,
                                greenCardState,
                                aadharCardState,
                                navigateToIdentityView,
                                navigateToGreenCardView,
                                navigateToAadharView
                        )
                2 -> PassportList(passportState, navigateToPassportView)
                3 -> RewardsList(rewardsCards, giftCards, navigateToRewardsView, navigateToGiftCardView)
            }
        }
    }
}

@Composable
fun FinancialList(list: List<FinancialAccount>, onItemClick: (Int) -> Unit) {
    LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
    ) { items(list, key = { it.id }) { account -> FinancialAccountItem(account, onItemClick) } }
}

@Composable
fun RewardsList(
    list: List<FinancialAccount>, 
    giftCards: List<GiftCard>,
    onItemClick: (Int) -> Unit,
    onGiftCardClick: (Int) -> Unit
) {
    LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
    ) { 
        if (giftCards.isNotEmpty()) {
            item {
                Text("Gift Cards", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            }
            items(giftCards, key = { "gc_${it.id}" }) { card -> GiftCardItem(card, onGiftCardClick) }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Rewards Cards", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            }
        }
        items(list, key = { "rc_${it.id}" }) { account -> RewardsCardItem(account, onItemClick) } 
    }
}

@Composable
fun GiftCardItem(giftCard: GiftCard, onItemClick: (Int) -> Unit) {
    Card(
            modifier = Modifier.fillMaxWidth().clickable { onItemClick(giftCard.id) },
            colors =
                    CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // Document Icon or Image (Front Image or Logo)
            val imagePath = giftCard.logoImagePath ?: giftCard.frontImagePath
            if (imagePath != null) {
                AsyncImage(
                        model = File(imagePath),
                        contentDescription = null,
                        modifier =
                                Modifier.size(60.dp)
                                        .clip(
                                                androidx.compose.foundation.shape
                                                        .RoundedCornerShape(8.dp)
                                        ),
                        contentScale = ContentScale.Crop,
                        fallback = painterResource(R.drawable.placeholder_image)
                )
            } else {
                Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            val rawNumber = giftCard.qrCode.takeIf { !it.isNullOrEmpty() } ?: giftCard.cardNumber
            val cardNumber = rawNumber.trim().takeLast(4).padStart(rawNumber.trim().length, '*')

            Column(modifier = Modifier.weight(1f)) {
                Text(
                        text = giftCard.providerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                )
                Text(
                        text = "Card #: " + cardNumber, 
                        style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun RewardsCardItem(account: FinancialAccount, onItemClick: (Int) -> Unit) {
    Card(
            modifier = Modifier.fillMaxWidth().clickable { onItemClick(account.id) },
            colors =
                    CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // Document Icon or Image (Front Image or Logo)
            val imagePath = account.logoImagePath ?: account.frontImagePath
            if (imagePath != null) {
                AsyncImage(
                        model = File(imagePath),
                        contentDescription = null,
                        modifier =
                                Modifier.size(60.dp)
                                        .clip(
                                                androidx.compose.foundation.shape
                                                        .RoundedCornerShape(8.dp)
                                        ),
                        contentScale = ContentScale.Crop,
                        fallback = painterResource(R.drawable.placeholder_image) // Placeholder
                )
            } else {
                Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                        text = account.institutionName, // Store Name
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                )
                if (!account.barcode.isNullOrBlank()) {
                    Text(
                            text = account.barcode,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
                if (!account.linkedPhoneNumber.isNullOrBlank()) {
                    Text(
                            text = "Phone: ${account.linkedPhoneNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialAccountItem(account: FinancialAccount, onItemClick: (Int) -> Unit) {
    val cardColor = getAccountColor(account)
    val contentColor =
            if (cardColor == MaterialTheme.colorScheme.surface) MaterialTheme.colorScheme.onSurface
            else androidx.compose.ui.graphics.Color.White
    val context = LocalContext.current

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
                    verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Institution Logo (logo.dev) - Top Left
                // val instLogoUrl = LogoUtils.getInstitutionLogoUrl(account.institutionName)
                // if (instLogoUrl != null) {
                //     AsyncImage(
                //             model =
                //                     ImageRequest.Builder(LocalContext.current)
                //                             .data(instLogoUrl)
                //                             .crossfade(true)
                //                             .build(),
                //             contentDescription = account.institutionName,
                //             modifier = Modifier.size(32.dp).clip(RoundedCornerShape(4.dp)),
                //             contentScale = ContentScale.Fit
                //     )
                //     Spacer(modifier = Modifier.width(12.dp))
                // }

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                                text = account.institutionName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = contentColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        val logoResId = getCardLogoResId(account.cardNetwork)
                        if (logoResId != null) {
                            Image(
                                    painter = painterResource(id = logoResId),
                                    contentDescription = account.cardNetwork,
                                    modifier = Modifier.width(40.dp).height(24.dp),
                                    contentScale = ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        
                        Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                color =
                                        MaterialTheme.colorScheme.tertiaryContainer.copy(
                                                alpha = 0.8f
                                        ),
                        ) {
                            val typeText =
                                    if (account.type == AccountType.BANK_ACCOUNT &&
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
                            text = account.accountName,
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Middle: Masked Account Number + Copy
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                        text = "•••• •••• •••• ${account.number.takeLast(4)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = contentColor,
                        modifier = Modifier.weight(1f)
                )

                IconButton(
                        onClick = {
                            val clipboard =
                                    context.getSystemService(Context.CLIPBOARD_SERVICE) as
                                            ClipboardManager
                            val clip = ClipData.newPlainText("Account Number", account.number)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = contentColor.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Row: Holder Name
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                            text = account.holderName.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = contentColor.copy(alpha = 0.9f)
                    )
                    if (account.expiryDate != null) {
                        Text(
                                text = "EXP ${account.expiryDate}",
                                style = MaterialTheme.typography.labelSmall,
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
    // 1. Check for manual color override
    if (account.colorTheme != null) {
        return androidx.compose.ui.graphics.Color(account.colorTheme)
    }

    // 2. Check for Bank/Network Brand Color via LogoUtils
    val brandColor = LogoUtils.getBrandColor(account.institutionName, account.cardNetwork)
    if (brandColor != null) {
        return androidx.compose.ui.graphics.Color(brandColor)
    }

    // 3. Default fallback based on type
    return when (account.type) {
        AccountType.CREDIT_CARD, AccountType.DEBIT_CARD ->
                androidx.compose.ui.graphics.Color(0xFF2C3E50) // Default dark for cards
        AccountType.BANK_ACCOUNT ->
                androidx.compose.ui.graphics.Color(0xFFF8F9FA) // Default light for bank accounts
        else -> MaterialTheme.colorScheme.surface
    }
}

@Composable
fun getCardLogoResId(network: String?): Int? {
    if (network == null) return null
    return when {
        network.contains("Visa", ignoreCase = true) -> R.drawable.ic_brand_visa
        network.contains("Master", ignoreCase = true) -> R.drawable.ic_brand_mastercard
        network.contains("Amex", ignoreCase = true) -> R.drawable.ic_brand_amex
        network.contains("Discover", ignoreCase = true) -> R.drawable.ic_brand_discover
        network.contains("Capital", ignoreCase = true) -> R.drawable.ic_brand_capitolone
        network.contains("Rupay", ignoreCase = true) -> R.drawable.ic_brand_rupay
        else -> null
    }
}

@Composable
fun PassportList(list: List<Passport>, onItemClick: (Int) -> Unit) {
    LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
    ) { items(list, key = { it.id }) { passport -> PassportItem(passport, { onItemClick(passport.id) }) } }
}

@Composable
fun IdentityList(
        identityList: List<IdentityDocument>,
        greenCardList: List<com.vijay.cardkeeper.data.entity.GreenCard>,
        aadharCardList: List<com.vijay.cardkeeper.data.entity.AadharCard>,
        onIdentityClick: (Int) -> Unit,
        onGreenCardClick: (Int) -> Unit,
        onAadharClick: (Int) -> Unit
) {
    LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(identityList, key = { "id_${it.id}" }) { doc -> IdentityItem(doc, onIdentityClick) }
        items(greenCardList, key = { "gc_${it.id}" }) { gc -> GreenCardItem(gc, onGreenCardClick) }
        items(aadharCardList, key = { "ad_${it.id}" }) { aadhar -> AadharCardItem(aadhar, onAadharClick) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GreenCardItem(gc: com.vijay.cardkeeper.data.entity.GreenCard, onItemClick: (Int) -> Unit) {
    Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            onClick = { onItemClick(gc.id) }
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Background US Flag
            val backgroundUrl = "https://flagcdn.com/w320/us.png"

            AsyncImage(
                    model =
                            ImageRequest.Builder(LocalContext.current)
                                    .data(backgroundUrl)
                                    .crossfade(true)
                                    .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    alpha = 0.15f,
                    modifier = Modifier.matchParentSize()
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                                text = "GREEN CARD",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                        )
                        Text(
                                text = "UNITED STATES OF AMERICA",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Flag Icon
                    AsyncImage(
                            model =
                                    ImageRequest.Builder(LocalContext.current)
                                            .data("https://flagcdn.com/w160/us.png")
                                            .crossfade(true)
                                            .build(),
                            contentDescription = "US Flag",
                            modifier = Modifier.size(24.dp).clip(RoundedCornerShape(2.dp))
                    )

                    if (gc.uscisNumber.isNotEmpty()) {
                        Text(
                                text = gc.uscisNumber,
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Details
                Text(
                        text = "Name: ${gc.givenName} ${gc.surname}",
                        style = MaterialTheme.typography.bodyMedium
                )
                if (gc.dob.isNotEmpty()) {
                    Text(text = "DOB: ${gc.dob}", style = MaterialTheme.typography.bodyMedium)
                }
                if (gc.expiryDate.isNotEmpty()) {
                    Text(
                            text = "Expires: ${gc.expiryDate}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Images
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    gc.frontImagePath?.let { path ->
                        DashboardImageThumbnail(path = path, label = "Front")
                    }
                    gc.backImagePath?.let { path ->
                        DashboardImageThumbnail(path = path, label = "Back")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AadharCardItem(
        aadhar: com.vijay.cardkeeper.data.entity.AadharCard,
        onItemClick: (Int) -> Unit
) {
    Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            onClick = { onItemClick(aadhar.id) }
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Background India Flag
            val backgroundUrl = "https://flagcdn.com/w320/in.png"

            AsyncImage(
                    model =
                            ImageRequest.Builder(LocalContext.current)
                                    .data(backgroundUrl)
                                    .crossfade(true)
                                    .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    alpha = 0.15f,
                    modifier = Modifier.matchParentSize()
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                                text = "AADHAAR CARD",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                        )
                        Text(
                                text = "आधार",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Flag Icon
                    AsyncImage(
                            model =
                                    ImageRequest.Builder(LocalContext.current)
                                            .data("https://flagcdn.com/w160/in.png")
                                            .crossfade(true)
                                            .build(),
                            contentDescription = "India Flag",
                            modifier = Modifier.size(24.dp).clip(RoundedCornerShape(2.dp))
                    )

                    // Masked Aadhaar number
                    if (aadhar.maskedAadhaarNumber.isNotEmpty()) {
                        Text(
                                text = aadhar.maskedAadhaarNumber,
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Details
                Text(
                        text = "Name: ${aadhar.holderName}",
                        style = MaterialTheme.typography.bodyMedium
                )
                if (aadhar.dob.isNotEmpty()) {
                    Text(text = "DOB: ${aadhar.dob}", style = MaterialTheme.typography.bodyMedium)
                }
                if (aadhar.address.isNotEmpty()) {
                    Text(
                            text =
                                    aadhar.address.take(50) +
                                            if (aadhar.address.length > 50) "..." else "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Images
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    aadhar.frontImagePath?.let { path ->
                        DashboardImageThumbnail(path = path, label = "Front")
                    }
                    aadhar.backImagePath?.let { path ->
                        DashboardImageThumbnail(path = path, label = "Back")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdentityItem(doc: IdentityDocument, onItemClick: (Int) -> Unit) {
    Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            onClick = { onItemClick(doc.id) }
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Background Full Watermark (State or Country)
            val cCode = doc.country.lowercase()
            val sCode = doc.state?.let { StateUtils.getStateCode(it) }

            val backgroundUrl =
                    when {
                        doc.type == com.vijay.cardkeeper.data.entity.DocumentType.DRIVER_LICENSE &&
                                sCode != null ->
                                "https://flagcdn.com/w320/us-${sCode.lowercase()}.png"
                        cCode == "usa" || cCode == "us" -> "https://flagcdn.com/w320/us.png"
                        cCode == "ind" || cCode == "in" || cCode == "india" ->
                                "https://flagcdn.com/w320/in.png"
                        cCode.length == 2 -> "https://flagcdn.com/w320/$cCode.png"
                        else -> null
                    }

            if (backgroundUrl != null) {
                AsyncImage(
                        model =
                                ImageRequest.Builder(LocalContext.current)
                                        .data(backgroundUrl)
                                        .crossfade(true)
                                        .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        alpha = 0.15f, // Increased visibility
                        modifier = Modifier.matchParentSize()
                )
            }

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

                    // Flag / Emblem Row
                    Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Country Flag
                        val cCodeFlag = doc.country.lowercase()
                        val flagUrl =
                                when (cCodeFlag) {
                                    "usa", "us" -> "https://flagcdn.com/w160/us.png"
                                    "ind", "in", "india" -> "https://flagcdn.com/w160/in.png"
                                    else ->
                                            if (cCodeFlag.length == 2)
                                                    "https://flagcdn.com/w160/$cCodeFlag.png"
                                            else null
                                }

                        if (flagUrl != null) {
                            AsyncImage(
                                    model =
                                            ImageRequest.Builder(LocalContext.current)
                                                    .data(flagUrl)
                                                    .crossfade(true)
                                                    .build(),
                                    contentDescription = "Country Flag",
                                    modifier = Modifier.size(24.dp).clip(RoundedCornerShape(2.dp))
                            )
                        }

                        // State Flag (as emblem placeholder)
                        if (!doc.state.isNullOrEmpty()) {
                            val sCodeFlag = StateUtils.getStateCode(doc.state)
                            if (sCodeFlag != null) {
                                val stateFlagUrl =
                                        "https://flagcdn.com/w160/us-${sCodeFlag.lowercase()}.png"
                                AsyncImage(
                                        model =
                                                ImageRequest.Builder(LocalContext.current)
                                                        .data(stateFlagUrl)
                                                        .crossfade(true)
                                                        .build(),
                                        contentDescription = "State Flag",
                                        modifier =
                                                Modifier.size(24.dp).clip(RoundedCornerShape(2.dp))
                                )
                            }
                        }
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
                    Text(
                            text = "Name: ${doc.holderName}",
                            style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (!doc.dob.isNullOrEmpty()) {
                    Text(text = "DOB: ${doc.dob}", style = MaterialTheme.typography.bodyMedium)
                }
                if (!doc.expiryDate.isNullOrEmpty()) {
                    Text(
                            text = "Expires: ${doc.expiryDate}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                    )
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
}

@Composable
fun DashboardImageThumbnail(
        path: String,
        label: String,
        textColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AsyncImage(
                model = File(path),
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
