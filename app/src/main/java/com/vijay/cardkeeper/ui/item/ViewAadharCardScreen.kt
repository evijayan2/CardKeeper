package com.vijay.cardkeeper.ui.item

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.vijay.cardkeeper.data.entity.AadharCard
import com.vijay.cardkeeper.ui.viewmodel.AddItemViewModel
import com.vijay.cardkeeper.ui.viewmodel.AppViewModelProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewAadharCardScreen(
        aadharCardId: Int,
        navigateBack: () -> Unit,
        onEditClick: (Int) -> Unit,
        viewModel: AddItemViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    var aadharCard by remember { mutableStateOf<AadharCard?>(null) }
    var fullScreenImage by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showQrDialog by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(aadharCardId) {
        viewModel.getAadharCard(aadharCardId).collect { aadharCard = it }
    }

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("Aadhaar Card Details") },
                        navigationIcon = {
                            IconButton(onClick = navigateBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                            }
                        },
                        actions = {
                            IconButton(onClick = { showQrDialog = true }) {
                                Icon(Icons.Default.QrCode, "Show QR")
                            }
                            IconButton(onClick = { onEditClick(aadharCardId) }) {
                                Icon(Icons.Default.Edit, "Edit")
                            }
                            IconButton(onClick = { showDeleteConfirmation = true }) {
                                Icon(Icons.Default.Delete, "Delete")
                            }
                        }
                )
            }
    ) { padding ->
        if (showDeleteConfirmation) {
            AlertDialog(
                    onDismissRequest = { showDeleteConfirmation = false },
                    title = { Text("Delete Aadhaar Card") },
                    text = { Text("Are you sure you want to delete this Aadhaar card?") },
                    confirmButton = {
                        TextButton(
                                onClick = {
                                    aadharCard?.let { viewModel.deleteAadharCard(it) }
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

        aadharCard?.let { card ->
            // QR Code Dialog
            if (showQrDialog && card.qrData != null) {
                AlertDialog(
                        onDismissRequest = { showQrDialog = false },
                        title = { Text("Aadhaar QR Code") },
                        text = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val qrBitmap =
                                        remember(card.qrData) { generateQrCode(card.qrData!!, 512) }
                                if (qrBitmap != null) {
                                    Image(
                                            bitmap = qrBitmap.asImageBitmap(),
                                            contentDescription = "Aadhaar QR Code",
                                            modifier = Modifier.size(256.dp)
                                    )
                                } else {
                                    Text("Unable to generate QR code")
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                        "Scan this QR for verification",
                                        style = MaterialTheme.typography.bodySmall
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showQrDialog = false }) { Text("Close") }
                        }
                )
            } else if (showQrDialog) {
                AlertDialog(
                        onDismissRequest = { showQrDialog = false },
                        title = { Text("No QR Data") },
                        text = { Text("QR data was not scanned for this Aadhaar card.") },
                        confirmButton = {
                            TextButton(onClick = { showQrDialog = false }) { Text("OK") }
                        }
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                // India Flag Background
                AsyncImage(
                        model =
                                ImageRequest.Builder(
                                                androidx.compose.ui.platform.LocalContext.current
                                        )
                                        .data("https://flagcdn.com/w320/in.png")
                                        .crossfade(true)
                                        .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        alpha = 0.1f,
                        modifier = Modifier.matchParentSize()
                )

                Column(
                        modifier =
                                Modifier.padding(padding)
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                        .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Card with Aadhaar styling
                    Card(
                            colors =
                                    CardDefaults.cardColors(
                                            containerColor =
                                                    MaterialTheme.colorScheme.primaryContainer
                                    ),
                            modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                            ) {
                                // India Flag
                                AsyncImage(
                                        model =
                                                ImageRequest.Builder(
                                                                androidx.compose.ui.platform
                                                                        .LocalContext.current
                                                        )
                                                        .data("https://flagcdn.com/w160/in.png")
                                                        .crossfade(true)
                                                        .build(),
                                        contentDescription = "India Flag",
                                        modifier =
                                                Modifier.size(24.dp).clip(RoundedCornerShape(2.dp))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                        text = "Aadhaar",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                    text = "Unique Identification Authority of India",
                                    style = MaterialTheme.typography.labelSmall,
                                    color =
                                            MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                                    alpha = 0.7f
                                            )
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Aadhaar Number
                            var isAadhaarVisible by remember { mutableStateOf(false) }
                            val realNumber = card.uid ?: card.maskedAadhaarNumber

                            Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier =
                                            Modifier.clickable {
                                                // Copy on click
                                                clipboardManager.setText(
                                                        AnnotatedString(realNumber)
                                                )
                                            }
                            ) {
                                val displayNumber =
                                        if (isAadhaarVisible) {
                                            if (!card.uid.isNullOrEmpty())
                                                    formatAadhaarNumber(card.uid!!)
                                            else card.maskedAadhaarNumber
                                        } else {
                                            // Masked view
                                            if (card.maskedAadhaarNumber.isNotEmpty())
                                                    card.maskedAadhaarNumber
                                            else "XXXX XXXX ${card.uid?.takeLast(4) ?: ""}"
                                        }

                                Text(
                                        text = displayNumber,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))

                                // Toggle Visibility
                                IconButton(onClick = { isAadhaarVisible = !isAadhaarVisible }) {
                                    Icon(
                                            if (isAadhaarVisible) Icons.Filled.Visibility
                                            else Icons.Filled.VisibilityOff,
                                            contentDescription =
                                                    if (isAadhaarVisible) "Hide" else "Show"
                                    )
                                }

                                IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(realNumber))
                                        }
                                ) {
                                    Icon(
                                            Icons.Default.ContentCopy,
                                            "Copy",
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // VID if available
                            if (!card.vid.isNullOrEmpty()) {
                                var isVidVisible by remember { mutableStateOf(false) }
                                Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    val vidDisplay =
                                            if (isVidVisible) {
                                                card.vid!!.chunked(4).joinToString(" ")
                                            } else {
                                                "XXXX XXXX XXXX ${card.vid!!.takeLast(4)}"
                                            }

                                    Text(
                                            text = "VID: $vidDisplay",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontFamily = FontFamily.Monospace,
                                            color =
                                                    MaterialTheme.colorScheme.onPrimaryContainer
                                                            .copy(alpha = 0.8f)
                                    )

                                    IconButton(
                                            onClick = { isVidVisible = !isVidVisible },
                                            modifier = Modifier.size(24.dp).padding(start = 4.dp)
                                    ) {
                                        Icon(
                                                imageVector =
                                                        if (isVidVisible) Icons.Filled.Visibility
                                                        else Icons.Filled.VisibilityOff,
                                                contentDescription =
                                                        if (isVidVisible) "Hide VID"
                                                        else "Show VID",
                                                modifier = Modifier.size(16.dp),
                                                tint =
                                                        MaterialTheme.colorScheme.onPrimaryContainer
                                                                .copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Photo and Details Row
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Photo from QR (Base64)
                        if (card.photoBase64 != null) {
                            val photoBitmap =
                                    remember(card.photoBase64) {
                                        decodeBase64ToBitmap(card.photoBase64!!)
                                    }
                            if (photoBitmap != null) {
                                Image(
                                        bitmap = photoBitmap.asImageBitmap(),
                                        contentDescription = "Aadhaar Photo",
                                        modifier =
                                                Modifier.size(100.dp)
                                                        .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                )
                            }
                        }

                        // Name and basic details
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                    text = card.holderName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                            )
                            Text(
                                    text = "DOB: ${card.dob}",
                                    style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                    text = "Gender: ${card.gender}",
                                    style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    // Scanned Images
                    if (card.frontImagePath != null || card.backImagePath != null) {
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            card.frontImagePath?.let { path ->
                                AadharCardImage(path, "Front", Modifier.weight(1f)) {
                                    fullScreenImage = path
                                }
                            }
                            card.backImagePath?.let { path ->
                                AadharCardImage(path, "Back", Modifier.weight(1f)) {
                                    fullScreenImage = path
                                }
                            }
                        }
                    }

                    // Address Section
                    SectionHeader("Address")
                    Card(
                            colors =
                                    CardDefaults.cardColors(
                                            containerColor =
                                                    MaterialTheme.colorScheme.surfaceVariant
                                    ),
                            modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = card.address, style = MaterialTheme.typography.bodyMedium)
                            if (!card.pincode.isNullOrEmpty()) {
                                Text(
                                        text = "Pincode: ${card.pincode}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Contact Details
                    if (card.email != null || card.mobile != null) {
                        SectionHeader("Contact Details")
                        if (card.mobile != null) {
                            DetailRow("Mobile", card.mobile!!)
                        }
                        if (card.email != null) {
                            DetailRow("Email", card.email!!)
                        }
                    }
                }
            }

            // Full Screen Image Dialog
            if (fullScreenImage != null) {
                androidx.compose.ui.window.Dialog(
                        onDismissRequest = { fullScreenImage = null },
                        properties =
                                androidx.compose.ui.window.DialogProperties(
                                        usePlatformDefaultWidth = false
                                )
                ) {
                    Box(
                            modifier =
                                    Modifier.fillMaxSize()
                                            .background(androidx.compose.ui.graphics.Color.Black)
                                            .clickable { fullScreenImage = null },
                            contentAlignment = Alignment.Center
                    ) {
                        val bitmap =
                                remember(fullScreenImage) {
                                    try {
                                        BitmapFactory.decodeFile(fullScreenImage)
                                    } catch (e: Exception) {
                                        null
                                    }
                                }
                        if (bitmap != null) {
                            Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Full Screen Image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
            }
        }
                ?: Box(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
    }
}

@Composable
private fun AadharCardImage(
        path: String,
        label: String,
        modifier: Modifier = Modifier,
        onClick: () -> Unit
) {
    val bitmap =
            remember(path) {
                try {
                    BitmapFactory.decodeFile(path)
                } catch (e: Exception) {
                    null
                }
            }

    Column(modifier = modifier) {
        if (bitmap != null) {
            Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = label,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1.5f).clickable { onClick() },
                    contentScale = ContentScale.Crop
            )
        } else {
            Box(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .aspectRatio(1.5f)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
            ) { Text("No Image", style = MaterialTheme.typography.bodySmall) }
        }
        Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

/** Format Aadhaar number with spaces: XXXX XXXX XXXX */
private fun formatAadhaarNumber(number: String): String {
    val cleaned = number.replace(" ", "").replace("-", "")
    return cleaned.chunked(4).joinToString(" ")
}

/** Decode Base64 encoded photo to Bitmap */
private fun decodeBase64ToBitmap(base64String: String): Bitmap? {
    return try {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        null
    }
}

/** Generate QR Code from data string using ZXing */
private fun generateQrCode(data: String, size: Int): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, size, size)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        bitmap
    } catch (e: Exception) {
        null
    }
}
