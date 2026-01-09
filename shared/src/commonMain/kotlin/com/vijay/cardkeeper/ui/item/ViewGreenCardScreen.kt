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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.vijay.cardkeeper.data.entity.GreenCard
import com.vijay.cardkeeper.ui.viewmodel.ViewItemViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewGreenCardScreen(
    greenCardId: Int,
    navigateBack: () -> Unit,
    onEditClick: (Int) -> Unit,
    onCopyContent: (String, String) -> Unit,
    viewModel: ViewItemViewModel
) {
    val greenCard by viewModel.selectedGreenCard.collectAsState()
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val platformContext = LocalPlatformContext.current

    LaunchedEffect(greenCardId) { 
        viewModel.loadGreenCard(greenCardId) 
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Green Card Details") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onEditClick(greenCardId) }) {
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
                title = { Text("Delete Green Card") },
                text = { Text("Are you sure you want to delete this green card?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            greenCard?.let { viewModel.deleteGreenCard(it) }
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
            modifier = Modifier.padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            greenCard?.let { gc ->
                // Header Card
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
                            text = "Permanent Resident Card",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                onCopyContent(gc.uscisNumber, "USCIS Number")
                            }
                        ) {
                            Text(text = gc.uscisNumber, style = MaterialTheme.typography.titleLarge)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                Icons.Default.ContentCopy,
                                "Copy",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Images
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    gc.frontImagePath?.let { path ->
                        GreenCardImage(path, "Front", Modifier.weight(1f)) {
                            viewModel.setFullScreenImage(path)
                        }
                    }
                    gc.backImagePath?.let { path ->
                        GreenCardImage(path, "Back", Modifier.weight(1f)) { 
                            viewModel.setFullScreenImage(path) 
                        }
                    }
                }

                // Details Sections
                SectionHeader("Holder Information")
                DetailRow("Surname", gc.surname)
                DetailRow("Given Name", gc.givenName)
                DetailRow("Date of Birth", gc.dob)
                DetailRow("Sex", gc.sex)
                DetailRow("Country of Birth", gc.countryOfBirth)

                SectionHeader("Card Information")
                DetailRow("Category", gc.category)
                DetailRow("USCIS#", gc.uscisNumber)
                DetailRow("Resident Since", gc.residentSince)
                DetailRow("Card Expires", gc.expiryDate)
            } ?: run {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
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
private fun GreenCardImage(
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
