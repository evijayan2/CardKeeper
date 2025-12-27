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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.vijay.cardkeeper.R
import com.vijay.cardkeeper.data.entity.IdentityDocument
import com.vijay.cardkeeper.ui.viewmodel.AddItemViewModel
import com.vijay.cardkeeper.ui.viewmodel.AppViewModelProvider
import com.vijay.cardkeeper.util.StateUtils

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
    val context = LocalContext.current

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
        document?.let { doc ->
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
                    Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                    text = doc.type.name.replace("_", " "),
                                    style = MaterialTheme.typography.headlineSmall
                            )
                            Text(text = doc.country, style = MaterialTheme.typography.titleMedium)
                            if (!doc.state.isNullOrEmpty()) {
                                Text(
                                        text = doc.state,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                    text = "Doc #: ${doc.docNumber}",
                                    style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        // Images Column (Flag & Emblem)
                        Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Country Flag (FlagCDN)
                            // URL: https://flagcdn.com/w160/{code}.png
                            // Fallback to local resources for USA/IND if download fails or as
                            // placeholder?
                            // Actually, let's prefer online for consistency but we have local
                            // backup for speed?
                            // For simplicity, matching user request "download... from online".
                            val countryCode = doc.country.lowercase()
                            val flagUrl =
                                    when (countryCode) {
                                        "usa", "us" -> "https://flagcdn.com/w160/us.png"
                                        "ind", "in", "india" -> "https://flagcdn.com/w160/in.png"
                                        else ->
                                                if (countryCode.length == 2)
                                                        "https://flagcdn.com/w160/$countryCode.png"
                                                else null
                                    }

                            if (flagUrl != null) {
                                AsyncImage(
                                        model =
                                                ImageRequest.Builder(LocalContext.current)
                                                        .data(flagUrl)
                                                        .crossfade(true)
                                                        .build(),
                                        contentDescription = "Country Flag",
                                        placeholder =
                                                painterResource(
                                                        R.drawable.ic_flag_usa
                                                ), // Just a default placeholder
                                        error = painterResource(R.drawable.ic_flag_usa), // Fallback
                                        modifier = Modifier.size(40.dp)
                                )
                            } else {
                                // Fallback to local logic if NO url generated (unlikely for
                                // standard codes)
                                val localFlag =
                                        when (doc.country.uppercase()) {
                                            "USA", "US" -> R.drawable.ic_flag_usa
                                            "IND", "IN", "INDIA" -> R.drawable.ic_flag_india
                                            else -> null
                                        }
                                if (localFlag != null) {
                                    Image(
                                            painter = painterResource(id = localFlag),
                                            contentDescription = "Country Flag",
                                            modifier = Modifier.size(40.dp)
                                    )
                                }
                            }

                            // State Emblem / Flag (Online)
                            if (!doc.state.isNullOrEmpty()) {
                                val stateCode = StateUtils.getStateCode(doc.state)
                                if (stateCode != null) {
                                    val stateFlagUrl =
                                            "https://flagcdn.com/w160/us-${stateCode.lowercase()}.png"
                                    AsyncImage(
                                            model =
                                                    ImageRequest.Builder(LocalContext.current)
                                                            .data(stateFlagUrl)
                                                            .crossfade(true)
                                                            .build(),
                                            contentDescription = "State Emblem",
                                            modifier = Modifier.size(40.dp)
                                    )
                                } else {
                                    // Try local fallback if state code mapping failed but we have a
                                    // matching resource?
                                    val inputCode = doc.state.lowercase().replace(" ", "_")
                                    val emblemResId =
                                            context.resources.getIdentifier(
                                                    "ic_emblem_$inputCode",
                                                    "drawable",
                                                    context.packageName
                                            )
                                    if (emblemResId != 0) {
                                        Image(
                                                painter = painterResource(id = emblemResId),
                                                contentDescription = "State Emblem",
                                                modifier = Modifier.size(40.dp)
                                        )
                                    }
                                }
                            }
                        }
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
                if (!doc.state.isNullOrEmpty()) {
                    DetailRow("State", doc.state)
                }

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
        }
                ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
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
