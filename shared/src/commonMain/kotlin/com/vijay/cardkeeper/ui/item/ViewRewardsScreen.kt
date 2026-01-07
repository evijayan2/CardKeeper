package com.vijay.cardkeeper.ui.item

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
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
        viewModel.loadRewardCard(itemId)
    }

    val rewardCard by viewModel.selectedRewardCard.collectAsState()

    val platformContext = LocalPlatformContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(rewardCard?.name ?: "Loading...") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    rewardCard?.let { rc ->
                        IconButton(onClick = { onEditClick(rc.id) }) {
                            Icon(Icons.Filled.Edit, "Edit")
                        }
                        IconButton(onClick = {
                            viewModel.deleteRewardCard(rc)
                            navigateBack()
                        }) {
                            Icon(Icons.Filled.Delete, "Delete")
                        }
                    }
                }
            )
        }
    ) { padding ->
        rewardCard?.let { rc ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Card with Logo
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (rc.logoImagePath != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(platformContext)
                                    .data(rc.logoImagePath)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Logo",
                                modifier = Modifier.height(80.dp).fillMaxWidth(),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        Text(
                            text = rc.name,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = rc.type.name.replace("_", " "),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                // Barcode Section
                if (!rc.barcode.isNullOrEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth().clickable {
                            viewModel.setFullScreenImage("BARCODE:${rc.barcode}")
                        }
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Scan Barcode", style = MaterialTheme.typography.labelSmall)
                            Spacer(modifier = Modifier.height(8.dp))

                            val barcodeBitmap = remember(rc.barcode, rc.barcodeFormat) {
                                rc.barcode?.let { onGenerateBarcode(it, rc.barcodeFormat) }
                            }

                            if (barcodeBitmap != null) {
                                Image(
                                    bitmap = barcodeBitmap,
                                    contentDescription = "Barcode",
                                    modifier = Modifier.fillMaxWidth().height(120.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            rc.barcode?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                // Details Section
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (!rc.linkedPhoneNumber.isNullOrEmpty()) {
                            DetailRow(
                                label = "Linked Phone",
                                value = rc.linkedPhoneNumber!!,
                                icon = Icons.Filled.Phone,
                                onAction = { onDialNumber(rc.linkedPhoneNumber!!) }
                            )
                        }

                        if (!rc.notes.isNullOrEmpty()) {
                            if (!rc.linkedPhoneNumber.isNullOrEmpty()) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                            }
                            DetailRow(
                                label = "Notes",
                                value = rc.notes!!,
                                icon = Icons.Filled.Notes
                            )
                        }
                    }
                }

                // Card Images
                if (rc.frontImagePath != null || rc.backImagePath != null) {
                    Text("Card Images", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rc.frontImagePath?.let { path ->
                            Box(modifier = Modifier.weight(1f).clickable {
                                viewModel.setFullScreenImage(path)
                            }) {
                                DashboardImageThumbnail(path = path, label = "Front")
                            }
                        }
                        rc.backImagePath?.let { path ->
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
                        },
                        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
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
                                    onGenerateBarcode(code, rc.barcodeFormat)
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
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    icon: ImageVector,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(enabled = onAction != null) { onAction?.invoke() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
            Text(text = value, style = MaterialTheme.typography.bodyLarge)
        }
        if (onAction != null) {
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Filled.Call, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}
