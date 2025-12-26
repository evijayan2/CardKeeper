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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vijay.cardkeeper.data.entity.IdentityDocument
import com.vijay.cardkeeper.ui.viewmodel.AddItemViewModel
import com.vijay.cardkeeper.ui.viewmodel.AppViewModelProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewIdentityScreen(
        documentId: Int,
        navigateBack: () -> Unit,
        onEditClick: (Int) -> Unit,
        viewModel: AddItemViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    var document by remember { mutableStateOf<IdentityDocument?>(null) }
    var fullScreenImage by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(documentId) { document = viewModel.getIdentityDocument(documentId) }

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("Identity Details") },
                        navigationIcon = {
                            IconButton(onClick = navigateBack) {
                                Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { onEditClick(documentId) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = { showDeleteConfirmation = true }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                )
            }
    ) { padding ->
        if (showDeleteConfirmation) {
            AlertDialog(
                    onDismissRequest = { showDeleteConfirmation = false },
                    title = { Text("Delete Document") },
                    text = { Text("Are you sure you want to delete this document?") },
                    confirmButton = {
                        TextButton(
                                onClick = {
                                    document?.let { viewModel.deleteIdentityDocument(it) }
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
        if (document != null) {
            val doc = document!!
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
                                )
                ) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        Text(
                                text = doc.type.name.replace("_", " "),
                                style = MaterialTheme.typography.headlineSmall
                        )
                        Text(text = doc.country, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                                text = "Doc #: ${doc.docNumber}",
                                style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                // Images
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    doc.frontImagePath?.let { path ->
                        IdentityImage(path, "Front", Modifier.weight(1f)) { fullScreenImage = path }
                    }
                    doc.backImagePath?.let { path ->
                        IdentityImage(path, "Back", Modifier.weight(1f)) { fullScreenImage = path }
                    }
                }

                // Details List
                DetailRow("Name", doc.holderName)
                DetailRow("DOB", doc.dob)
                DetailRow("Expiry", doc.expiryDate)

                HorizontalDivider()

                DetailRow("Sex", doc.sex)
                DetailRow("Height", doc.height)
                DetailRow("Eyes", doc.eyeColor)
                DetailRow("Issued By", doc.issuingAuthority)

                if (!doc.address.isNullOrEmpty()) {
                    DetailRow("Address", doc.address)
                }

                HorizontalDivider()

                DetailRow("Class", doc.licenseClass)
                DetailRow("Restrictions", doc.restrictions)
                DetailRow("Endorsements", doc.endorsements)
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
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun IdentityImage(path: String, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
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
                    modifier = Modifier.fillMaxWidth().aspectRatio(1.5f),
                    contentAlignment = Alignment.Center
            ) { Text("Image not found") }
        }
        Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun DetailRow(label: String, value: String?) {
    if (!value.isNullOrEmpty()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
            )
            Text(text = value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
