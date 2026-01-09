package com.vijay.cardkeeper.ui.item

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.vijay.cardkeeper.data.entity.InsuranceCard
import com.vijay.cardkeeper.ui.viewmodel.ViewItemViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewInsuranceCardScreen(
    card: InsuranceCard,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCopyContent: (String, String) -> Unit,
    viewModel: ViewItemViewModel,
    snackbarHostState: SnackbarHostState
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val platformContext = LocalPlatformContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Insurance Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteConfirmation = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = false },
                title = { Text("Delete Insurance Card") },
                text = { Text("Are you sure you want to delete this insurance card?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onDeleteClick()
                            showDeleteConfirmation = false
                        }
                    ) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmation = false }) { Text("Cancel") }
                }
            )
        }

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Icon (Using Edit as placeholder if medical icon not available)
                    Icon(
                        imageVector = Icons.Default.Edit, 
                        contentDescription = "Insurance",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = card.providerName,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                    
                    if (!card.planName.isNullOrBlank()) {
                        Text(
                            text = card.planName!!,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Copyable Policy Number
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            onCopyContent(card.policyNumber, "Policy Number")
                        }
                    ) {
                        Text(
                            text = card.policyNumber,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Images Side-by-Side
            if (card.frontImagePath != null || card.backImagePath != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    card.frontImagePath?.let { path ->
                        InsuranceCardImage(path, "Front", Modifier.weight(1f)) { viewModel.setFullScreenImage(path) }
                    }
                    card.backImagePath?.let { path ->
                        InsuranceCardImage(path, "Back", Modifier.weight(1f)) { viewModel.setFullScreenImage(path) }
                    }
                }
            }

            // Card Details
            SectionHeader("Card Details")
            DetailRow(label = "Type", value = card.type.name)
            
            if (!card.groupNumber.isNullOrBlank()) {
                DetailRow(label = "Group Number", value = card.groupNumber!!, isCopyable = true, onCopy = { onCopyContent(card.groupNumber!!, "Group Number") })
            }
            if (!card.memberId.isNullOrBlank()) {
                DetailRow(label = "Member ID", value = card.memberId!!, isCopyable = true, onCopy = { onCopyContent(card.memberId!!, "Member ID") })
            }
            if (!card.policyHolderName.isNullOrBlank()) {
                DetailRow(label = "Policy Holder", value = card.policyHolderName!!, isCopyable = true, onCopy = { onCopyContent(card.policyHolderName!!, "Policy Holder") })
            }
            if (!card.expiryDate.isNullOrBlank()) {
                DetailRow(label = "Expiry Date", value = card.expiryDate!!)
            }

            // Contact Info
            if (!card.customerServiceNumber.isNullOrBlank() || !card.website.isNullOrBlank()) {
                HorizontalDivider()
                SectionHeader("Contact Info")
                if (!card.customerServiceNumber.isNullOrBlank()) {
                    DetailRow(label = "Customer Service", value = card.customerServiceNumber!!, isCopyable = true, onCopy = { onCopyContent(card.customerServiceNumber!!, "Customer Service Number") })
                }
                if (!card.website.isNullOrBlank()) {
                    DetailRow(label = "Website", value = card.website!!, isCopyable = true, onCopy = { onCopyContent(card.website!!, "Website") })
                }
            }

            // Notes
            if (!card.notes.isNullOrBlank()) {
                HorizontalDivider()
                SectionHeader("Notes")
                Text(card.notes!!, style = MaterialTheme.typography.bodyMedium)
            }
        }
        
        // Full Screen Image Dialog
        val fullScreenImage by viewModel.fullScreenImage.collectAsState()
        if (fullScreenImage != null) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { viewModel.setFullScreenImage(null) }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .clickable {
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
fun InsuranceCardImage(path: String, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
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
                .aspectRatio(1.5f)
                .clickable { onClick() },
            contentScale = ContentScale.Crop
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}
