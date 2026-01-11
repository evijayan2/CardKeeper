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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import kards.shared.generated.resources.Res
import kards.shared.generated.resources.ic_flag_india
import com.vijay.cardkeeper.data.entity.AadharCard
import com.vijay.cardkeeper.ui.viewmodel.ViewItemViewModel
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewAadharCardScreen(
    aadharCardId: Int,
    navigateBack: () -> Unit,
    onEditClick: (Int) -> Unit,
    onCopyContent: (String, String) -> Unit,
    onDecodeBase64: (String) -> ImageBitmap?,
    onGenerateQrCode: (String) -> ImageBitmap?,
    viewModel: ViewItemViewModel
) {
    val aadharCard by viewModel.selectedAadharCard.collectAsState()
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showQrDialog by remember { mutableStateOf(false) }
    val platformContext = LocalPlatformContext.current

    LaunchedEffect(aadharCardId) { 
        viewModel.loadAadharCard(aadharCardId) 
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
                            val qrBitmap = remember(card.qrData) { onGenerateQrCode(card.qrData!!) }
                            if (qrBitmap != null) {
                                Image(
                                    bitmap = qrBitmap,
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
                    model = ImageRequest.Builder(platformContext)
                        .data("https://flagcdn.com/w320/in.png")
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    alpha = 0.1f,
                    modifier = Modifier.matchParentSize()
                )

                Column(
                    modifier = Modifier.padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Card with Aadhaar styling
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
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
                                Icon(
                                    painter = painterResource(Res.drawable.ic_flag_india),
                                    contentDescription = "India Flag",
                                    modifier = Modifier.size(24.dp).clip(RoundedCornerShape(2.dp)),
                                    tint = Color.Unspecified
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
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Aadhaar Number
                            var isAadhaarVisible by remember { mutableStateOf(false) }
                            val realNumber = card.uid ?: card.maskedAadhaarNumber

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    onCopyContent(realNumber, "Aadhaar Number")
                                }
                            ) {
                                val displayNumber = if (isAadhaarVisible) {
                                    if (!card.uid.isNullOrEmpty()) formatAadhaarNumber(card.uid!!)
                                    else card.maskedAadhaarNumber
                                } else {
                                    val last4 = if (!card.uid.isNullOrEmpty()) {
                                        card.uid!!.takeLast(4)
                                    } else {
                                        card.maskedAadhaarNumber.takeLast(4)
                                    }
                                    "XXXX XXXX $last4"
                                }

                                Text(
                                    text = displayNumber,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))

                                IconButton(onClick = { isAadhaarVisible = !isAadhaarVisible }) {
                                    Icon(
                                        if (isAadhaarVisible) Icons.Filled.Visibility
                                        else Icons.Filled.VisibilityOff,
                                        contentDescription = if (isAadhaarVisible) "Hide" else "Show"
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        onCopyContent(realNumber, "Aadhaar Number")
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
                                    val vidDisplay = if (isVidVisible) {
                                        card.vid!!.chunked(4).joinToString(" ")
                                    } else {
                                        "XXXX XXXX XXXX ${card.vid!!.takeLast(4)}"
                                    }

                                    Text(
                                        text = "VID: $vidDisplay",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )

                                    IconButton(
                                        onClick = { isVidVisible = !isVidVisible },
                                        modifier = Modifier.size(24.dp).padding(start = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isVidVisible) Icons.Filled.Visibility
                                            else Icons.Filled.VisibilityOff,
                                            contentDescription = if (isVidVisible) "Hide VID" else "Show VID",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
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
                            val photoBitmap = remember(card.photoBase64) { onDecodeBase64(card.photoBase64!!) }
                            if (photoBitmap != null) {
                                Image(
                                    bitmap = photoBitmap,
                                    contentDescription = "Aadhaar Photo",
                                    modifier = Modifier.size(100.dp).clip(RoundedCornerShape(8.dp)),
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
                                    viewModel.setFullScreenImage(path)
                                }
                            }
                            card.backImagePath?.let { path ->
                                AadharCardImage(path, "Back", Modifier.weight(1f)) {
                                    viewModel.setFullScreenImage(path)
                                }
                            }
                        }
                    }

                    // Address Section
                    SectionHeader("Address")
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
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
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
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

@Composable
private fun AadharCardImage(
    path: String,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val platformContext = LocalPlatformContext.current
    Column(modifier = modifier) {
        AsyncImage(
            model = ImageRequest.Builder(platformContext)
                .data(path)
                .crossfade(true)
                .build(),
            contentDescription = label,
            modifier = Modifier.fillMaxWidth().aspectRatio(1.5f).clickable { onClick() },
            contentScale = ContentScale.Crop
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

private fun formatAadhaarNumber(number: String): String {
    val cleaned = number.replace(" ", "").replace("-", "")
    return cleaned.chunked(4).joinToString(" ")
}
