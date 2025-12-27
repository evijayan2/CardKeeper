package com.vijay.cardkeeper.ui.item

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vijay.cardkeeper.data.entity.GreenCard
import com.vijay.cardkeeper.ui.viewmodel.AddItemViewModel
import com.vijay.cardkeeper.ui.viewmodel.AppViewModelProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewGreenCardScreen(
        greenCardId: Int,
        navigateBack: () -> Unit,
        onEditClick: (Int) -> Unit,
        viewModel: AddItemViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    var greenCard by remember { mutableStateOf<GreenCard?>(null) }
    var fullScreenImage by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(greenCardId) { viewModel.getGreenCard(greenCardId).collect { greenCard = it } }

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

        greenCard?.let { gc ->
            Column(
                    modifier =
                            Modifier.padding(padding)
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Card
                Card(
                        colors =
                                CardDefaults.cardColors(
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
                                modifier =
                                        Modifier.clickable {
                                            clipboardManager.setText(
                                                    AnnotatedString(gc.uscisNumber)
                                            )
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
                            fullScreenImage = path
                        }
                    }
                    gc.backImagePath?.let { path ->
                        GreenCardImage(path, "Back", Modifier.weight(1f)) { fullScreenImage = path }
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
                                    Modifier.fillMaxSize().background(Color.Black).clickable {
                                        fullScreenImage = null
                                    },
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
    }
            ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
}

@Composable
private fun GreenCardImage(
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
