package com.vijay.cardkeeper.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import kards.shared.generated.resources.Res
import kards.shared.generated.resources.ic_app_logo_1
import kards.shared.generated.resources.ic_brand_amex
import kards.shared.generated.resources.ic_brand_capitolone
import kards.shared.generated.resources.ic_brand_discover
import kards.shared.generated.resources.ic_brand_mastercard
import kards.shared.generated.resources.ic_brand_rupay
import kards.shared.generated.resources.ic_brand_visa
import kards.shared.generated.resources.placeholder_image
import com.vijay.cardkeeper.data.entity.AccountType
import com.vijay.cardkeeper.data.entity.FinancialAccount
import com.vijay.cardkeeper.data.entity.PanCard
import com.vijay.cardkeeper.data.entity.RewardCard

import com.vijay.cardkeeper.data.entity.IdentityDocument
import com.vijay.cardkeeper.data.entity.Passport
import com.vijay.cardkeeper.data.entity.GiftCard
import com.vijay.cardkeeper.ui.viewmodel.HomeViewModel
import com.vijay.cardkeeper.util.LogoUtils
import com.vijay.cardkeeper.util.StateUtils
import org.jetbrains.compose.resources.painterResource
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
        navigateToItemEntry: (Int, String?) -> Unit,
        navigateToItemView: (Int) -> Unit,
        navigateToIdentityView: (Int) -> Unit,
        navigateToPassportView: (Int) -> Unit = {},
        navigateToGreenCardView: (Int) -> Unit = {},
        navigateToAadharView: (Int) -> Unit = {},
        navigateToPanCardView: (Int) -> Unit = {},
        navigateToGiftCardView: (Int) -> Unit = {},
        navigateToRewardsView: (Int) -> Unit = {},
        navigateToInsuranceView: (Int) -> Unit = {},
        navigateToSearch: () -> Unit = {},
        navigateToSettings: () -> Unit = {},
        viewModel: HomeViewModel,
        onCopyContent: (String) -> Unit,
        initialTab: Int? = null,
        snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val bankAccounts by viewModel.bankAccounts.collectAsState()
    val rewardsCards by viewModel.rewardsCards.collectAsState()
    val identityState by viewModel.identityDocuments.collectAsState()
    val passportState by viewModel.passports.collectAsState()
    val greenCardState by viewModel.greenCards.collectAsState()
    val aadharCardState by viewModel.aadharCards.collectAsState()
    val panCardState by viewModel.panCards.collectAsState()
    val insuranceCards by viewModel.insuranceCards.collectAsState()
    val giftCards by viewModel.giftCards.collectAsState()
    var selectedTab by rememberSaveable { mutableStateOf(initialTab ?: 0) }
    
    // Update tab when initialTab changes (e.g., after saving an item)
    LaunchedEffect(initialTab) {
        if (initialTab != null && initialTab >= 0) {
            selectedTab = initialTab
        }
    }
    
    // Update tabs list
    val tabs = listOf("Finance", "Identity", "Passports", "Rewards")
    
    println("CardKeeperUI: HomeScreen Composing. Tab: $selectedTab, Finance: ${bankAccounts.size}, Identity: ${identityState.size}, Passport: ${passportState.size}, GreenCard: ${greenCardState.size}, Aadhar: ${aadharCardState.size}, GiftCard: ${giftCards.size}")


    // FAB Menu State
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
            topBar = {
                TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                        painter = painterResource(Res.drawable.ic_app_logo_1),
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
            snackbarHost = { SnackbarHost(snackbarHostState) },
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
                                text = { Text("PAN Card") },
                                onClick = {
                                    showMenu = false
                                    navigateToItemEntry(7, "PAN")
                                },
                                leadingIcon = { Icon(Icons.Filled.AccountBox, contentDescription = null) }
                        )
                        DropdownMenuItem(
                                text = { Text("Insurance Card") },
                                onClick = {
                                    showMenu = false
                                    navigateToItemEntry(8, "INSURANCE")
                                },
                                leadingIcon = { Icon(Icons.Filled.CreditCard, contentDescription = null) }
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
                0 -> FinancialList(bankAccounts, navigateToItemView, onCopyContent)
                1 ->
                        IdentityList(
                                identityState,
                                greenCardState,
                                aadharCardState,
                                panCardState,
                                navigateToIdentityView,
                                navigateToGreenCardView,
                                navigateToAadharView,
                                navigateToPanCardView
                        )
                2 -> PassportList(passportState, navigateToPassportView)
                3 -> RewardsList(rewardsCards, giftCards, insuranceCards, navigateToRewardsView, navigateToGiftCardView, navigateToInsuranceView)
            }
        }
    }
}

// --- Helper Functions ---

@Composable
fun FinancialList(list: List<FinancialAccount>, onItemClick: (Int) -> Unit, onCopyContent: (String) -> Unit) {
    LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
    ) { items(list, key = { it.id }) { account -> FinancialAccountItem(account, onItemClick, onCopyContent) } }
}

@Composable
fun RewardsList(
    rewardsCards: List<RewardCard>,
    giftCards: List<GiftCard>,
    insuranceCards: List<com.vijay.cardkeeper.data.entity.InsuranceCard>,
    onRewardClick: (Int) -> Unit,
    onGiftCardClick: (Int) -> Unit,
    onInsuranceCardClick: (Int) -> Unit
) {
    LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Gift Cards
        if (giftCards.isNotEmpty()) {
            item {
                Text("Gift Cards", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            }
            items(giftCards, key = { "gc_${it.id}" }) { card -> GiftCardItem(card, onGiftCardClick) }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // 2. Insurance Cards
        if (insuranceCards.isNotEmpty()) {
            item {
                Text("Insurance Cards", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            }
            items(insuranceCards, key = { "ins_${it.id}" }) { card -> InsuranceCardItem(card, onInsuranceCardClick) }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // 3. Rewards Cards
        if (rewardsCards.isNotEmpty()) {
             // Only show header if there are other items above, or maybe always for consistency?
             // Requested order: Gift -> Insurance -> Rewards.
             // If others exist, definitely show header.
             if (giftCards.isNotEmpty() || insuranceCards.isNotEmpty()) {
                 item {
                     Text("Rewards Cards", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                 }
             }
             items(rewardsCards, key = { "rc_${it.id}" }) { rewardCard -> RewardsCardItem(rewardCard, onRewardClick) }
        } else if (giftCards.isEmpty() && insuranceCards.isEmpty()) {
             // Empty state?
        }
    }
}

@Composable
fun InsuranceCardItem(card: com.vijay.cardkeeper.data.entity.InsuranceCard, onItemClick: (Int) -> Unit) {
    Card(
            modifier = Modifier.fillMaxWidth().clickable { onItemClick(card.id) },
            colors =
                    CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // Document Icon or Image (Front Image)
            val imagePath = card.frontImagePath
            if (imagePath != null) {
                AsyncImage(
                        model = imagePath,
                        contentDescription = null,
                        modifier =
                                Modifier.size(60.dp)
                                        .clip(
                                                androidx.compose.foundation.shape
                                                        .RoundedCornerShape(8.dp)
                                        ),
                        contentScale = ContentScale.Crop,
                        fallback = painterResource(Res.drawable.placeholder_image)
                )
            } else {
                 // Fallback Icon based on type
                val icon = when (card.type) {
                    com.vijay.cardkeeper.data.entity.InsuranceCardType.MEDICAL -> Icons.Default.Face // detailed medical icon?
                    com.vijay.cardkeeper.data.entity.InsuranceCardType.DENTAL -> Icons.Default.Face
                    com.vijay.cardkeeper.data.entity.InsuranceCardType.EYE -> Icons.Default.Face
                }
                Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                        text = card.providerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                )
                Text(
                        text = "${card.type.name} - ${card.policyNumber}",
                        style = MaterialTheme.typography.bodyMedium
                )
                if (!card.policyHolderName.isNullOrBlank()) {
                     Text(
                        text = card.policyHolderName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                     )
                }
            }
        }
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
                        model = imagePath,
                        contentDescription = null,
                        modifier =
                                Modifier.size(60.dp)
                                        .clip(
                                                androidx.compose.foundation.shape
                                                        .RoundedCornerShape(8.dp)
                                        ),
                        contentScale = ContentScale.Crop,
                        fallback = painterResource(Res.drawable.placeholder_image)
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
fun RewardsCardItem(rewardCard: RewardCard, onItemClick: (Int) -> Unit) {
    Card(
            modifier = Modifier.fillMaxWidth().clickable { onItemClick(rewardCard.id) },
            colors =
                    CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // Document Icon or Image (Front Image or Logo)
            val imagePath = rewardCard.logoImagePath ?: rewardCard.frontImagePath
            if (imagePath != null) {
                AsyncImage(
                        model = imagePath,
                        contentDescription = null,
                        modifier =
                                Modifier.size(60.dp)
                                        .clip(
                                                androidx.compose.foundation.shape
                                                        .RoundedCornerShape(8.dp)
                                        ),
                        contentScale = ContentScale.Crop,
                        fallback = painterResource(Res.drawable.placeholder_image) // Placeholder
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
                        text = rewardCard.name, // Store Name
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                )
                rewardCard.barcode?.let {
                    Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
                if (!rewardCard.linkedPhoneNumber.isNullOrBlank()) {
                    Text(
                            text = "Phone: ${rewardCard.linkedPhoneNumber}",
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
fun FinancialAccountItem(account: FinancialAccount, onItemClick: (Int) -> Unit, onCopyContent: (String) -> Unit) {
    val cardColor = getAccountColor(account)
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
                                    painter = painterResource(logoResId),
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
                            onCopyContent(account.number)
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
                        ExpirationBadge(expiryDateStr = account.expiryDate)
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
fun getCardLogoResId(network: String?): org.jetbrains.compose.resources.DrawableResource? {
    if (network == null) return null
    return when {
         network.contains("Visa", ignoreCase = true) -> Res.drawable.ic_brand_visa
         network.contains("Master", ignoreCase = true) -> Res.drawable.ic_brand_mastercard
         network.contains("Amex", ignoreCase = true) -> Res.drawable.ic_brand_amex
         network.contains("Discover", ignoreCase = true) -> Res.drawable.ic_brand_discover
         network.contains("Capital", ignoreCase = true) -> Res.drawable.ic_brand_capitolone
         network.contains("Rupay", ignoreCase = true) -> Res.drawable.ic_brand_rupay
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
        panCardList: List<PanCard>,
        onIdentityClick: (Int) -> Unit,
        onGreenCardClick: (Int) -> Unit,
        onAadharClick: (Int) -> Unit,
        onPanCardClick: (Int) -> Unit
) {
    LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(identityList, key = { "id_${it.id}" }) { doc -> IdentityItem(doc, onIdentityClick) }
        items(greenCardList, key = { "gc_${it.id}" }) { gc -> GreenCardItem(gc, onGreenCardClick) }
        items(aadharCardList, key = { "ad_${it.id}" }) { aadhar -> AadharCardItem(aadhar, onAadharClick) }
        items(panCardList, key = { "pan_${it.id}" }) { pan -> PanCardItem(pan, onPanCardClick) }
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
                    model = backgroundUrl,
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
                            model = "https://flagcdn.com/w160/us.png",
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Expires: ${gc.expiryDate}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        ExpirationBadge(expiryDateStr = gc.expiryDate)
                    }
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
                    model = backgroundUrl,
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
                            model = "https://flagcdn.com/w160/in.png",
                            contentDescription = "India Flag",
                            modifier = Modifier.size(24.dp).clip(RoundedCornerShape(2.dp))
                    )

                    val last4 = if (aadhar.uid?.isNotEmpty() == true) {
                        aadhar.uid.takeLast(4)
                    } else {
                        aadhar.maskedAadhaarNumber.takeLast(4)
                    }
                    if (last4.isNotEmpty()) {
                        Text(
                                text = "XXXX XXXX $last4",
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
                        model = backgroundUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        alpha = 0.1f, // Reduced visibility to prevent text interference
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
                                    model = flagUrl,
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
                                        model = stateFlagUrl,
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                         Text(
                            text = "Expires: ${doc.expiryDate}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        ExpirationBadge(expiryDateStr = doc.expiryDate)
                    }
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
fun ExpirationBadge(expiryDateStr: String?) {
    if (expiryDateStr.isNullOrBlank()) return

    val status = remember(expiryDateStr) {
        val today = kotlinx.datetime.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date
        val date = try {
            if (expiryDateStr.matches(Regex("\\d{2}/\\d{2}"))) {
                // Handle MM/yy
                 val parts = expiryDateStr.split("/")
                 val month = parts[0].toInt()
                 val year = 2000 + parts[1].toInt()
                 // approximation: end of month
                 // minimal KMP logic since YearMonth is java.time
                 val nextMonth = if (month == 12) 1 else month + 1
                 val nextMonthYear = if (month == 12) year + 1 else year
                 val firstDayNextMonth = kotlinx.datetime.LocalDate(nextMonthYear, nextMonth, 1)
                 firstDayNextMonth.minus(kotlinx.datetime.DatePeriod(days = 1))
            } else {
                com.vijay.cardkeeper.util.DateNormalizer.parseStrict(expiryDateStr)
            }
        } catch (e: Exception) {
            null
        }

        if (date != null) {
            when {
                date < today -> "EXPIRED" to androidx.compose.ui.graphics.Color.Red
                date == today -> "EXPIRES TODAY" to androidx.compose.ui.graphics.Color(0xFFFF5722) // Deep Orange
                date < today.plus(kotlinx.datetime.DatePeriod(days = 30)) -> "EXPIRING SOON" to androidx.compose.ui.graphics.Color(0xFFFF9800) // Orange
                else -> null
            }
        } else {
            null
        }
    }

    status?.let { (text, color) ->
        Surface(
            color = color,
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.padding(start = 4.dp)
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = androidx.compose.ui.graphics.Color.White,
                fontWeight = FontWeight.Bold
            )
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
        val platformContext = coil3.compose.LocalPlatformContext.current
        val request = remember(path) {
            println("CardKeeperUI: Loading Thumbnail: $path")
            ImageRequest.Builder(platformContext)
                .data(path)
                .size(300) // Downsample to thumbnail size to prevent OOM
                .crossfade(true)
                .listener(
                    onStart = { println("CardKeeperUI: Image load started for $path") },
                    onSuccess = { _, _ -> println("CardKeeperUI: Image load success for $path") },
                    onError = { _, result -> println("CardKeeperUI: Image load failed for $path: ${result.throwable.message}") }
                )
                .build()
        }

        AsyncImage(
                model = request,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanCardItem(pan: PanCard, onItemClick: (Int) -> Unit) {
    Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            onClick = { onItemClick(pan.id) }
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Background India Flag
            val backgroundUrl = "https://flagcdn.com/w320/in.png"

            AsyncImage(
                    model = backgroundUrl,
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
                                text = "PAN CARD",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                        )
                        Text(
                                text = "GOVT OF INDIA",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Flag Icon
                    AsyncImage(
                            model = "https://flagcdn.com/w160/in.png",
                            contentDescription = "India Flag",
                            modifier = Modifier.size(24.dp).clip(RoundedCornerShape(2.dp))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                // Masked PAN: XXXXX 1234 X
                val maskedPan = if (pan.panNumber.length == 10) {
                     "XXXXX " + pan.panNumber.substring(5, 9) + " " + pan.panNumber.last()
                } else {
                     pan.panNumber
                }

                Text(
                        text = maskedPan,
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                         fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Details
                Text(
                        text = "Name: ${pan.holderName.uppercase()}",
                        style = MaterialTheme.typography.bodyMedium
                )
                
                if (!pan.dob.isNullOrEmpty()) {
                    Text(
                            text = "DOB: ${pan.dob}",
                            style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Images
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pan.frontImagePath?.let { path ->
                        DashboardImageThumbnail(path = path, label = "Front")
                    }
                    pan.backImagePath?.let { path ->
                        DashboardImageThumbnail(path = path, label = "Back")
                    }
                }
            }
        }
    }
}
