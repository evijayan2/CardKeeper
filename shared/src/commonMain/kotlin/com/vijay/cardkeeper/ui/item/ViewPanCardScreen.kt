package com.vijay.cardkeeper.ui.item

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
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.vijay.cardkeeper.data.entity.PanCard
import com.vijay.cardkeeper.ui.viewmodel.ViewItemViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewPanCardScreen(
    panCardId: Int,
    navigateBack: () -> Unit,
    onEditClick: (Int) -> Unit,
    onCopyContent: (String, String) -> Unit,
    viewModel: ViewItemViewModel
) {
    val panCard by viewModel.selectedPanCard.collectAsState()
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val platformContext = LocalPlatformContext.current

    LaunchedEffect(panCardId) {
        viewModel.loadPanCard(panCardId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PAN Card Details") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onEditClick(panCardId) }) {
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
                title = { Text("Delete PAN Card") },
                text = { Text("Are you sure you want to delete this PAN card?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            panCard?.let { viewModel.deletePanCard(it) }
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

        panCard?.let { card ->
            Box(modifier = Modifier.fillMaxSize()) {
                // India Flag Background
                AsyncImage(
                    model = ImageRequest.Builder(platformContext)
                        .data("https://flagcdn.com/w320/in.png")
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    alpha = 0.05f,
                    modifier = Modifier.matchParentSize()
                )

                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Card with PAN styling
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
                            Text(
                                text = "Permanent Account Number",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Income Tax Department",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "Government of India",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // PAN Number with masking
                            var isPanVisible by remember { mutableStateOf(false) }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    onCopyContent(card.panNumber, "PAN Number")
                                }
                            ) {
                                val displayPan = if (isPanVisible) {
                                    // Show full PAN with spaces for readability: ABCDE 1234 F
                                    card.panNumber.take(5) + " " + 
                                    card.panNumber.substring(5, 9) + " " + 
                                    card.panNumber.takeLast(1)
                                } else {
                                    // Mask: show only last 4 characters
                                    "XXXXX " + card.panNumber.takeLast(5)
                                }

                                Text(
                                    text = displayPan,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))

                                // Show/Hide Button
                                IconButton(onClick = { isPanVisible = !isPanVisible }) {
                                    Icon(
                                        if (isPanVisible) Icons.Filled.Visibility
                                        else Icons.Filled.VisibilityOff,
                                        contentDescription = if (isPanVisible) "Hide PAN" else "Show PAN"
                                    )
                                }

                                // Copy Button
                                IconButton(
                                    onClick = {
                                        onCopyContent(card.panNumber, "PAN Number")
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        "Copy PAN",
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Holder Details
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Cardholder Details",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            DetailRow("Name", card.holderName)
                            
                            if (!card.fatherName.isNullOrEmpty()) {
                                DetailRow("Father's Name", card.fatherName!!)
                            }
                            
                            if (!card.dob.isNullOrEmpty()) {
                                DetailRow("Date of Birth", card.dob!!)
                            }
                        }
                    }

                    // Scanned Images
                    if (card.frontImagePath != null || card.backImagePath != null) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Scanned Images",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    card.frontImagePath?.let { path ->
                                        PanCardImage(path, "Front", Modifier.weight(1f)) {
                                            viewModel.setFullScreenImage(path)
                                        }
                                    }
                                    card.backImagePath?.let { path ->
                                        PanCardImage(path, "Back", Modifier.weight(1f)) {
                                            viewModel.setFullScreenImage(path)
                                        }
                                    }
                                }
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
private fun PanCardImage(
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
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.6f)
                .clip(RoundedCornerShape(8.dp))
                .clickable { onClick() },
            contentScale = ContentScale.Crop
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp)
        )
    }
}
