package com.vijay.cardkeeper.ui.item

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.vijay.cardkeeper.ui.home.DashboardImageThumbnail
import com.vijay.cardkeeper.ui.viewmodel.ViewItemViewModel
import com.vijay.cardkeeper.util.LogoUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewRewardsScreen(
    itemId: Int,
    navigateBack: () -> Unit,
    onEditClick: (Int) -> Unit,
    onDialNumber: (String) -> Unit,
    onGenerateBarcode: (String, Int?) -> ImageBitmap?,
    onSetBrightness: (Float) -> Unit,
    viewModel: ViewItemViewModel
) {
    LaunchedEffect(itemId) { 
        viewModel.loadAccount(itemId) 
    }

    val account by viewModel.selectedAccount.collectAsState()
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val platformContext = LocalPlatformContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(account?.institutionName ?: "Reward Details") },
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
                title = { Text("Delete Card") },
                text = { Text("Are you sure you want to delete this card?") },
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
                // Banner / Card Header
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Shop Logo
                        if (acc.logoImagePath != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(platformContext)
                                    .data(acc.logoImagePath)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Logo",
                                modifier = Modifier.height(80.dp).fillMaxWidth(),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        } else {
                            // Dynamic Logo Fallback
                            val dynamicLogoUrl = LogoUtils.getInstitutionLogoUrl(acc.institutionName)
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
                            text = acc.institutionName,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = acc.type.name.replace("_", " "),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                // Barcode Section
                if (!acc.barcode.isNullOrEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth().clickable {
                            val barcodeVal = acc.barcode
                            viewModel.setFullScreenImage("BARCODE:$barcodeVal")
                        }
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Scan Barcode", style = MaterialTheme.typography.labelSmall)
                            Spacer(modifier = Modifier.height(8.dp))

                            val barcodeVal = acc.barcode
                            val barcodeFormat = acc.barcodeFormat
                            val barcodeBitmap = remember(barcodeVal, barcodeFormat) {
                                onGenerateBarcode(barcodeVal, barcodeFormat)
                            }

                            if (barcodeBitmap != null) {
                                Image(
                                    bitmap = barcodeBitmap,
                                    contentDescription = "Barcode",
                                    modifier = Modifier.fillMaxWidth().height(120.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }

                            // Barcode Value Text
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = barcodeVal,
                                style = MaterialTheme.typography.titleLarge,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Linked Phone
                val phone = acc.linkedPhoneNumber
                if (!phone.isNullOrEmpty()) {
                    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Linked Phone Number", style = MaterialTheme.typography.labelSmall)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = phone,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                IconButton(onClick = { onDialNumber(phone) }) {
                                    Icon(Icons.Filled.Call, "Call")
                                }
                            }
                        }
                    }
                }

                // Notes
                val notesVal = acc.notes
                if (!notesVal.isNullOrEmpty()) {
                    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Notes", style = MaterialTheme.typography.labelSmall)
                            Text(text = notesVal, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                // Images
                if (acc.frontImagePath != null || acc.backImagePath != null) {
                    Text("Card Images", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        acc.frontImagePath?.let { path ->
                            Box(modifier = Modifier.weight(1f).clickable {
                                viewModel.setFullScreenImage(path)
                            }) {
                                DashboardImageThumbnail(path = path, label = "Front")
                            }
                        }
                        acc.backImagePath?.let { path ->
                            Box(modifier = Modifier.weight(1f).clickable {
                                viewModel.setFullScreenImage(path)
                            }) {
                                DashboardImageThumbnail(path = path, label = "Back")
                            }
                        }
                    }
                }

                // Full Screen Image Dialog
                val fullScreenImage by viewModel.fullScreenImage.collectAsState()
                if (fullScreenImage != null) {
                    androidx.compose.ui.window.Dialog(
                        onDismissRequest = { 
                            onSetBrightness(-1f)
                            viewModel.setFullScreenImage(null) 
                        }
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color.Black).clickable {
                                onSetBrightness(-1f)
                                viewModel.setFullScreenImage(null)
                            },
                            contentAlignment = Alignment.Center
                        ) {
                            val bitmap = remember(fullScreenImage) {
                                if (fullScreenImage?.startsWith("BARCODE:") == true) {
                                    val code = fullScreenImage!!.substring(8)
                                    onGenerateBarcode(code, acc.barcodeFormat)
                                } else null
                            }

                            if (fullScreenImage?.startsWith("BARCODE:") == true) {
                                DisposableEffect(Unit) {
                                    onSetBrightness(1f)
                                    onDispose { onSetBrightness(-1f) }
                                }
                            }

                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap,
                                    contentDescription = "Full Screen",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
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
            } ?: run {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
