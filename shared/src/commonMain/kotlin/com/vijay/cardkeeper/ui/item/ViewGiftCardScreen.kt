package com.vijay.cardkeeper.ui.item

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.vijay.cardkeeper.data.entity.GiftCard
import com.vijay.cardkeeper.ui.viewmodel.ViewItemViewModel
import com.vijay.cardkeeper.util.LogoUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewGiftCardScreen(
    giftCardId: Int,
    navigateBack: () -> Unit,
    onEdit: (Int) -> Unit,
    onCopyContent: (String, String) -> Unit, // content, label for message
    qrCodeContent: @Composable (String) -> Unit,
    barcodeContent: @Composable (String, Int?) -> Unit, // content, format
    viewModel: ViewItemViewModel,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val giftCard by viewModel.selectedGiftCard.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val platformContext = LocalPlatformContext.current

    LaunchedEffect(giftCardId) {
        viewModel.loadGiftCard(giftCardId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(giftCard?.providerName ?: "Gift Card") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onEdit(giftCardId) }) {
                        Icon(Icons.Default.Edit, "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, "Delete")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            giftCard?.let { card ->
                
                // 1. Header Card (Logo + Name)
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Shop Logo
                        if (card.logoImagePath != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(platformContext)
                                    .data(card.logoImagePath)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Logo",
                                modifier = Modifier.height(80.dp).fillMaxWidth(),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        } else {
                            // Dynamic Logo Fallback
                            val dynamicLogoUrl = LogoUtils.getInstitutionLogoUrl(card.providerName)
                            if (dynamicLogoUrl != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(platformContext)
                                        .data(dynamicLogoUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Logo",
                                    modifier = Modifier.height(80.dp).fillMaxWidth(),
                                    contentScale = ContentScale.Fit
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }

                        Text(
                            text = card.providerName,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = "Gift Card",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                // 2. Barcode / QR Section - Prominent
                if (!card.barcode.isNullOrEmpty() || !card.qrCode.isNullOrEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth().clickable {
                            if (!card.qrCode.isNullOrEmpty()) {
                                viewModel.setFullScreenImage("QR:${card.qrCode}")
                            } else {
                                viewModel.setFullScreenImage("BARCODE:${card.barcode}")
                            }
                        }
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    if (!card.qrCode.isNullOrEmpty()) "QR Code" else "Barcode", 
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            if (!card.qrCode.isNullOrEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    qrCodeContent(card.qrCode!!)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Data + Copy Row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp)
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically, 
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = card.qrCode!!.trim(),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.weight(1f)
                                    )
                                    val clipboardManager = LocalClipboardManager.current
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(card.qrCode!!.trim()))
                                        },
                                        modifier = Modifier.size(24.dp).padding(start = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.ContentCopy,
                                            contentDescription = "Copy",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                            } else if (!card.barcode.isNullOrEmpty()) {
                                barcodeContent(card.barcode!!, card.barcodeFormat)
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Data + Copy Row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp)
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = card.barcode!!.trim(),
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontFamily = FontFamily.Monospace,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier.weight(1f)
                                    )
                                    val clipboardManager = LocalClipboardManager.current
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(card.barcode!!.trim()))
                                        },
                                        modifier = Modifier.size(24.dp).padding(start = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.ContentCopy,
                                            contentDescription = "Copy",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Details Card
                if (card.cardNumber.isNotBlank() || !card.pin.isNullOrEmpty()) {
                    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (card.cardNumber.isNotBlank()) {
                                DetailRow(
                                    label = "Gift Card Code",
                                    value = card.cardNumber,
                                    isCopyable = true,
                                    onCopy = { onCopyContent(card.cardNumber, "Gift Card Code") },
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            if (!card.pin.isNullOrEmpty()) {
                                DetailRow(
                                    label = "PIN",
                                    value = card.pin,
                                    isCopyable = true,
                                    onCopy = { onCopyContent(card.pin!!, "PIN") },
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                // 4. Notes
                if (!card.notes.isNullOrEmpty()) {
                    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Notes", style = MaterialTheme.typography.labelSmall)
                            Text(text = card.notes, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                // 5. Images Thumbnail Row
                if (card.frontImagePath != null || card.backImagePath != null) {
                    Text("Card Images", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        card.frontImagePath?.let { path ->
                            Box(
                                modifier = Modifier.weight(1f).clickable { viewModel.setFullScreenImage(path) }
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(platformContext)
                                        .data(path)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Front",
                                    modifier = Modifier.fillMaxWidth().height(100.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        if (card.frontImagePath == null) Spacer(modifier = Modifier.weight(1f))

                        card.backImagePath?.let { path ->
                            Box(
                                modifier = Modifier.weight(1f).clickable { viewModel.setFullScreenImage(path) }
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(platformContext)
                                        .data(path)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Back",
                                    modifier = Modifier.fillMaxWidth().height(100.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        if (card.backImagePath == null) Spacer(modifier = Modifier.weight(1f))
                    }
                }

                // Full Screen Image Dialog
                val fullScreenImage by viewModel.fullScreenImage.collectAsState()
                if (fullScreenImage != null) {
                    androidx.compose.ui.window.Dialog(
                        onDismissRequest = { viewModel.setFullScreenImage(null) },
                        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color.Black).clickable {
                                viewModel.setFullScreenImage(null)
                            },
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                fullScreenImage!!.startsWith("QR:") -> {
                                    val code = fullScreenImage!!.substring(3)
                                    Box(modifier = Modifier.fillMaxWidth().height(300.dp).background(Color.White).padding(16.dp)) {
                                        qrCodeContent(code)
                                    }
                                }
                                fullScreenImage!!.startsWith("BARCODE:") -> {
                                    val code = fullScreenImage!!.substring(8)
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(modifier = Modifier.fillMaxWidth().height(300.dp).background(Color.White).padding(16.dp)) {
                                            barcodeContent(code, card.barcodeFormat)
                                        }
                                        Spacer(modifier = Modifier.height(32.dp))
                                        Text(
                                            text = code.trim(),
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = Color.White,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }
                                }
                                else -> {
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
                }

            } ?: run {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Loading...")
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Gift Card?") },
                text = { Text("Are you sure you want to delete this gift card? This action cannot be undone.") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteGiftCard(giftCard!!)
                        showDeleteDialog = false
                        navigateBack()
                    }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
