package com.vijay.cardkeeper.ui.item

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.vijay.cardkeeper.data.entity.AccountType
import com.vijay.cardkeeper.data.entity.FinancialAccount
import com.vijay.cardkeeper.ui.viewmodel.ViewItemViewModel
import com.vijay.cardkeeper.util.LogoUtils
import org.jetbrains.compose.resources.painterResource
// import com.vijay.cardkeeper.R // Removed Android R class

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewItemScreen(
    itemId: Int,
    navigateBack: () -> Unit,
    onEditClick: (Int) -> Unit,
    onCopyContent: (String, String) -> Unit, // content, label for message
    onLaunchUrl: (String) -> Unit,
    onDialNumber: (String) -> Unit,
    barcodeContent: @Composable (String, Int?) -> Unit, // content, format
    viewModel: ViewItemViewModel
) {
    LaunchedEffect(itemId) { viewModel.loadAccount(itemId) }

    val account by viewModel.selectedAccount.collectAsState()
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val platformContext = LocalPlatformContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(account?.institutionName ?: "Details") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { account?.let { onEditClick(it.id) } }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteConfirmation = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = false },
                title = { Text("Delete Account") },
                text = { Text("Are you sure you want to delete this account?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            account?.let { viewModel.deleteAccount(it) }
                            showDeleteConfirmation = false
                            navigateBack()
                        }
                    ) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmation = false }) { Text("Cancel") }
                }
            )
        }
        Column(
            modifier = Modifier.padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            account?.let { acc ->
                // Header Card
                val cardColor = getAccountColor(acc)
                val isDark = cardColor != MaterialTheme.colorScheme.surface // Simple check
                val contentColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface

                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = cardColor,
                        contentColor = contentColor
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Top Row: Institution + Type
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Institution Logo
                            val instLogoUrl = LogoUtils.getInstitutionLogoUrl(acc.institutionName)
                            if (instLogoUrl != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(platformContext)
                                        .data(instLogoUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = acc.institutionName,
                                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(6.dp)),
                                    contentScale = ContentScale.Fit
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        acc.institutionName,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    // Card Network Logo
                                    val dynamicLogoUrl = LogoUtils.getCardNetworkLogoUrl(acc.cardNetwork)
                                    if (dynamicLogoUrl != null) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(platformContext)
                                                .data(dynamicLogoUrl)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = acc.cardNetwork,
                                            modifier = Modifier.width(40.dp).height(24.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    } 
                                    // TODO: Local resources for network logos (R.drawable...) need KMP resources
                                }
                                Text(
                                    acc.type.name.replace("_", " "),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = contentColor.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Middle: Account Number + Copy Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = acc.number.chunked(4).joinToString(" "),
                                style = MaterialTheme.typography.headlineMedium,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { onCopyContent(acc.number, "Account Number") }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = contentColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Bottom Row: Holder
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    acc.holderName.uppercase(),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                if (!acc.expiryDate.isNullOrBlank()) {
                                    Text(
                                        text = "VALID THRU ${acc.expiryDate}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = contentColor.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Images
                if (acc.frontImagePath != null || acc.backImagePath != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        acc.frontImagePath?.let { path ->
                            Box(
                                modifier = Modifier.clickable { viewModel.setFullScreenImage(path) }
                            ) {
                                // Thumbnail abstraction or just use AsyncImage?
                                // Assuming path is local file path. Coil 3 on Android handles it.
                                // On iOS, it needs file:// prefix probably.
                                AsyncImage(
                                    model = ImageRequest.Builder(platformContext)
                                        .data(path)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Front",
                                    modifier = Modifier.height(100.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                        acc.backImagePath?.let { path ->
                            Box(
                                modifier = Modifier.clickable { viewModel.setFullScreenImage(path) }
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(platformContext)
                                        .data(path)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Back",
                                    modifier = Modifier.height(100.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }
                }
                
                // Shop Logo
                acc.logoImagePath?.let { path ->
                     Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                          AsyncImage(
                              model = ImageRequest.Builder(platformContext).data(path).build(),
                              contentDescription = "Shop Logo",
                              modifier = Modifier.height(100.dp).fillMaxWidth().padding(bottom = 16.dp),
                              contentScale = ContentScale.Fit
                          )
                     }
                }

                // Barcode Section
                acc.barcode?.takeIf { it.isNotEmpty() }?.let { barcodeVal ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            viewModel.setFullScreenImage("BARCODE:$barcodeVal")
                        }
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Barcode", style = MaterialTheme.typography.labelSmall)
                            
                            // Delegates barcode rendering to platform
                            barcodeContent(barcodeVal, acc.barcodeFormat ?: 0)
                            
                            Text( "Tap to view full screen", style = MaterialTheme.typography.bodySmall )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = barcodeVal,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton( onClick = { onCopyContent(barcodeVal, "Barcode") } ) {
                                    Icon(Icons.Filled.ContentCopy, "Copy", modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }

                // Details Section
                if (acc.type != AccountType.REWARDS_CARD) {
                    Text("Account Details", style = MaterialTheme.typography.titleMedium)
                }

                // Number (Copyable)
                if (acc.type != AccountType.REWARDS_CARD) {
                    DetailRow(
                        label = "Number",
                        value = acc.number,
                        isCopyable = true,
                        onCopy = { onCopyContent(acc.number, "Number") }
                    )
                }

                acc.cvv?.let { cvv -> SecureDetailRow(label = "CVV", value = cvv) }
                acc.cardPin?.let { pin -> SecureDetailRow(label = "PIN", value = pin) }
                
                acc.expiryDate?.let { DetailRow(label = "Expiry", value = it.toString()) }
                acc.holderName.takeIf { it.isNotEmpty() }?.let { DetailRow(label = "Holder Name", value = it) }
                acc.linkedPhoneNumber?.takeIf { it.isNotEmpty() }?.let { 
                    DetailRow(label = "Linked Phone", value = it, isCopyable = true, onCopy = { onCopyContent(it, "Phone") }) 
                }

                if (acc.type == AccountType.BANK_ACCOUNT) {
                     acc.routingNumber?.takeIf { it.isNotEmpty() }?.let {
                         DetailRow(label = "Routing Number", value = it, isCopyable = true, onCopy = { onCopyContent(it, "Routing") })
                     }
                     acc.ifscCode?.takeIf { it.isNotEmpty() }?.let {
                         DetailRow(label = "IFSC Code", value = it, isCopyable = true, onCopy = { onCopyContent(it, "IFSC") })
                     }
                     acc.swiftCode?.takeIf { it.isNotEmpty() }?.let {
                         DetailRow(label = "SWIFT Code", value = it, isCopyable = true, onCopy = { onCopyContent(it, "SWIFT") })
                     }
                     acc.wireNumber?.takeIf { it.isNotEmpty() }?.let {
                         DetailRow(label = "Wire Number", value = it, isCopyable = true, onCopy = { onCopyContent(it, "Wire") })
                     }
                     acc.branchAddress?.takeIf { it.isNotEmpty() }?.let {
                         DetailRow(label = "Branch Address", value = it, isCopyable = true, onCopy = { onCopyContent(it, "Address") })
                     }
                }

                // Actions
                acc.lostCardContactNumber?.takeIf { it.isNotEmpty() }?.let { phone ->
                    Button(
                        onClick = { onDialNumber(phone) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Icon(Icons.Filled.Call, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Call Support")
                    }
                }

                if (acc.type == AccountType.BANK_ACCOUNT) {
                    acc.branchContactNumber?.takeIf { it.isNotEmpty() }?.let { phone ->
                         Button(
                            onClick = { onDialNumber(phone) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Icon(Icons.Filled.Call, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Call Bank")
                        }
                    }
                    acc.bankWebUrl?.takeIf { it.isNotEmpty() }?.let { url ->
                        val finalUrl = if (!url.startsWith("http")) "https://$url" else url
                        Button(
                            onClick = { onLaunchUrl(finalUrl) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Filled.Visibility, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Visit Website")
                        }
                    }
                }
                
                // Full Screen Dialog
                val fullScreenImage by viewModel.fullScreenImage.collectAsState()
                if (fullScreenImage != null) {
                     // TODO: Dialog is platform specific? No, androidx.compose.ui.window.Dialog is common in recent Compose versions but might have limitations?
                     // Actually Dialog is available in commonMain for Compose Multiplatform now (usually).
                     // However, properties = DialogProperties(usePlatformDefaultWidth = false) might be customizable.
                     // I will assume basics are working.
                     
                     // Wait, fullScreenImage logic for Barcode needs to generate barcode.
                     // I will reuse barcodeContent for that!
                     // But barcodeContent is a composable.
                     
                     androidx.compose.ui.window.Dialog(
                         onDismissRequest = { viewModel.setFullScreenImage(null) }
                     ) {
                         Box(
                             modifier = Modifier.fillMaxSize().background(Color.Black).clickable { viewModel.setFullScreenImage(null) },
                             contentAlignment = Alignment.Center
                         ) {
                             if (fullScreenImage!!.startsWith("BARCODE:")) {
                                 val code = fullScreenImage!!.substring(8)
                                 // We need to render this full screen. 
                                 // Our barcodeContent lambda might not be styled for full screen.
                                 // But we can wrap it.
                                 Box(modifier = Modifier.fillMaxWidth().height(300.dp).background(Color.White).padding(16.dp)) {
                                     barcodeContent(code, acc.barcodeFormat ?: 0)
                                 }
                             } else {
                                 AsyncImage(
                                     model = ImageRequest.Builder(platformContext).data(fullScreenImage).build(),
                                     contentDescription = "Full Screen",
                                     modifier = Modifier.fillMaxSize(),
                                     contentScale = ContentScale.Fit
                                 )
                             }
                         }
                     }
                }

                acc.notes?.takeIf { it.isNotEmpty() }?.let {
                    Text("Notes", style = MaterialTheme.typography.titleMedium)
                    Text(it, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}


@Composable
fun DetailRow(
    label: String, 
    value: String, 
    isCopyable: Boolean = false, 
    onCopy: (() -> Unit)? = null, 
    fontFamily: FontFamily? = null
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, style = MaterialTheme.typography.bodyLarge, fontFamily = fontFamily, modifier = Modifier.weight(1f))
            if (isCopyable && onCopy != null) {
                IconButton(onClick = onCopy) {
                    Icon(Icons.Filled.ContentCopy, "Copy", modifier = Modifier.size(20.dp))
                }
            }
        }
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
fun SecureDetailRow(label: String, value: String) {
    var visible by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                    text = if (visible) value else "•".repeat(value.length),
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { visible = !visible }) {
                Icon(if (visible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, "Toggle")
            }
        }
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
fun getAccountColor(account: FinancialAccount): Color {
    if (account.colorTheme != null) {
        return Color(account.colorTheme)
    }
    // LogoUtils is shared
    val brandColor = LogoUtils.getBrandColor(account.institutionName, account.cardNetwork)
    if (brandColor != null) {
        return Color(brandColor)
    }
    return when (account.type) {
        AccountType.CREDIT_CARD, AccountType.DEBIT_CARD -> Color(0xFF2C3E50)
        AccountType.BANK_ACCOUNT -> Color(0xFFF8F9FA)
        else -> Color.White // Fallback
    }
}
