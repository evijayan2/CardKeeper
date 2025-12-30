package com.vijay.cardkeeper.ui.item

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.vijay.cardkeeper.ui.home.DashboardImageThumbnail
import com.vijay.cardkeeper.ui.viewmodel.AppViewModelProvider
import com.vijay.cardkeeper.ui.viewmodel.ViewItemViewModel
import com.vijay.cardkeeper.util.LogoUtils
import com.vijay.cardkeeper.util.QrCodeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewGiftCardScreen(
    giftCardId: Int,
    navigateBack: () -> Unit,
    onEdit: (Int) -> Unit,
    viewModel: ViewItemViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val giftCard by viewModel.selectedGiftCard.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

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
        }
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
                            val bitmap = remember(card.logoImagePath) {
                                try {
                                    BitmapFactory.decodeFile(card.logoImagePath)
                                } catch (e: Exception) { null }
                            }
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Logo",
                                    modifier = Modifier.height(80.dp).fillMaxWidth(),
                                    contentScale = ContentScale.Fit
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        } else {
                            // Dynamic Logo Fallback
                            val dynamicLogoUrl = LogoUtils.getInstitutionLogoUrl(card.providerName)
                            if (dynamicLogoUrl != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
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
                                horizontalArrangement = Arrangement.Start, // Title centered or left? Usually header is left.
                                // Actually, user said "show s "QR Code" (Title). Left is standard for titles.
                                // Let's keep title on left, but remove copy button from here.
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    if (!card.qrCode.isNullOrEmpty()) "QR Code" else "Barcode", 
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            if (!card.qrCode.isNullOrEmpty()) {
                                 val qrBitmap = remember(card.qrCode) {
                                    QrCodeUtils.generateQrBitmap(card.qrCode!!)
                                }
                                if (qrBitmap != null) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Image(
                                            bitmap = qrBitmap.asImageBitmap(),
                                            contentDescription = "QR Code",
                                            modifier = Modifier.size(200.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // DetailRow("", card.qrCode!!)
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
                                    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(card.qrCode!!.trim()))
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
                                val barcodeBitmap = remember(card.barcode, card.barcodeFormat) {
                                    generateBarcodeBitmap(card.barcode!!, card.barcodeFormat)
                                }
                                if (barcodeBitmap != null) {
                                    Image(
                                        bitmap = barcodeBitmap.asImageBitmap(),
                                        contentDescription = "Barcode",
                                        modifier = Modifier.fillMaxWidth().height(120.dp),
                                        contentScale = ContentScale.Fit
                                    )
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
                                        text = card.barcode!!.trim(),
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontFamily = FontFamily.Monospace,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                         modifier = Modifier.weight(1f)
                                    )
                                    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(card.barcode!!.trim()))
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
                                DetailRow("Gift Card Code", card.cardNumber)
                             }
                             if (!card.pin.isNullOrEmpty()) DetailRow("PIN", card.pin)
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
                                DashboardImageThumbnail(path = path, label = "Front")
                            }
                        }
                         if (card.frontImagePath == null) Spacer(modifier = Modifier.weight(1f)) // Balance if only back exists?

                        card.backImagePath?.let { path ->
                            Box(
                                modifier = Modifier.weight(1f).clickable { viewModel.setFullScreenImage(path) }
                            ) {
                                DashboardImageThumbnail(path = path, label = "Back")
                            }
                        }
                        if (card.backImagePath == null) Spacer(modifier = Modifier.weight(1f))
                    }
                }

                // Full Screen Image Dialog (Reuse logic)
                val fullScreenImage by viewModel.fullScreenImage.collectAsState()
                if (fullScreenImage != null) {
                    Dialog(
                        onDismissRequest = { viewModel.setFullScreenImage(null) },
                        properties = DialogProperties(usePlatformDefaultWidth = false)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color.Black).clickable {
                                viewModel.setFullScreenImage(null)
                            },
                            contentAlignment = Alignment.Center
                        ) {
                            val bitmap = remember(fullScreenImage) {
                                if (fullScreenImage?.startsWith("QR:") == true) {
                                     val code = fullScreenImage!!.substring(3)
                                     QrCodeUtils.generateQrBitmap(code)
                                } else if (fullScreenImage?.startsWith("BARCODE:") == true) {
                                    val code = fullScreenImage!!.substring(8)
                                    generateBarcodeBitmap(code, card.barcodeFormat)
                                } else {
                                    try {
                                        BitmapFactory.decodeFile(fullScreenImage)
                                    } catch (e: Exception) { null }
                                }
                            }
                            
                            // Max Brightness
                            if (fullScreenImage?.startsWith("QR:") == true || fullScreenImage?.startsWith("BARCODE:") == true) {
                                DisposableEffect(Unit) {
                                    val activity = context as? android.app.Activity
                                    val window = activity?.window
                                    val originalBrightness = window?.attributes?.screenBrightness
                                    val lp = window?.attributes
                                    lp?.screenBrightness = 1f
                                    window?.attributes = lp
                                    onDispose {
                                        lp?.screenBrightness = originalBrightness ?: -1f
                                        window?.attributes = lp
                                    }
                                }
                            }

                             Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                 if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "Full Screen",
                                        modifier = Modifier.fillMaxWidth(), // Allow width fill for barcode
                                        contentScale = ContentScale.FillWidth // Better for barcodes
                                    )
                                } else if (fullScreenImage?.startsWith("BARCODE:") == true) {
                                     // Text Fallback 
                                     Text(
                                        text = fullScreenImage!!.substring(8),
                                        style = MaterialTheme.typography.displayMedium,
                                        color = Color.Black,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.background(Color.White).padding(32.dp)
                                     )
                                }

                                // Show Code Text for QR/Barcode in Full Screen
                                if (fullScreenImage?.startsWith("QR:") == true || fullScreenImage?.startsWith("BARCODE:") == true) {
                                    val codeText = if (fullScreenImage!!.startsWith("QR:")) fullScreenImage!!.substring(3) else fullScreenImage!!.substring(8)
                                    Spacer(modifier = Modifier.height(32.dp))
                                    Text(
                                        text = codeText.trim(),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color.White,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(16.dp)
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

@Composable
fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        if (label.isNotEmpty()) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
        }
        Row(
            modifier = Modifier.fillMaxWidth(), 
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
             Text(value, style = MaterialTheme.typography.bodyLarge)
             if (value.isNotBlank()) 
             {
                val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                IconButton(
                    onClick = { clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(value)) },
                    modifier = Modifier.size(24.dp)
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
