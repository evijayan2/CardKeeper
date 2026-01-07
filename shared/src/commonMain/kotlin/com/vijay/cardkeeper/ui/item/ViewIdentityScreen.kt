package com.vijay.cardkeeper.ui.item

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import kards.shared.generated.resources.Res
import kards.shared.generated.resources.ic_flag_usa
import kards.shared.generated.resources.ic_flag_india
import com.vijay.cardkeeper.data.entity.IdentityDocument
import com.vijay.cardkeeper.ui.viewmodel.ViewItemViewModel
import com.vijay.cardkeeper.util.StateUtils
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewIdentityScreen(
    documentId: Int,
    navigateBack: () -> Unit,
    onEditClick: (Int) -> Unit,
    viewModel: ViewItemViewModel
) {
    val document by viewModel.selectedIdentityDocument.collectAsState()
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val platformContext = LocalPlatformContext.current

    LaunchedEffect(documentId) { 
        viewModel.loadIdentityDocument(documentId) 
    }

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
        
        Column(
            modifier = Modifier.padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            document?.let { doc ->
                // Header Card
                Card(
                    colors = CardDefaults.cardColors(
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
                            val stateName = doc.state
                            if (!stateName.isNullOrEmpty()) {
                                Text(
                                    text = stateName,
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

                        // Flags & Emblems
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val countryCode = doc.country.lowercase()
                            val flagUrl = when (countryCode) {
                                "usa", "us" -> "https://flagcdn.com/w160/us.png"
                                "ind", "in", "india" -> "https://flagcdn.com/w160/in.png"
                                else -> if (countryCode.length == 2) "https://flagcdn.com/w160/$countryCode.png" else null
                            }

                            if (flagUrl != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(platformContext)
                                        .data(flagUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Country Flag",
                                    placeholder = painterResource(Res.drawable.ic_flag_usa),
                                    error = painterResource(Res.drawable.ic_flag_usa),
                                    modifier = Modifier.size(40.dp)
                                )
                            } else {
                                val localFlag = when (doc.country.uppercase()) {
                                    "USA", "US" -> Res.drawable.ic_flag_usa
                                    "IND", "IN", "INDIA" -> Res.drawable.ic_flag_india
                                    else -> null
                                }
                                if (localFlag != null) {
                                    Icon(
                                        painter = painterResource(localFlag),
                                        contentDescription = "Country Flag",
                                        modifier = Modifier.size(40.dp),
                                        tint = Color.Unspecified
                                    )
                                }
                            }

                            // State Emblem (Online)
                            val stateVal = doc.state
                            if (!stateVal.isNullOrEmpty()) {
                                val stateCode = StateUtils.getStateCode(stateVal)
                                if (stateCode != null) {
                                    val stateFlagUrl = "https://flagcdn.com/w160/us-${stateCode.lowercase()}.png"
                                    AsyncImage(
                                        model = ImageRequest.Builder(platformContext)
                                            .data(stateFlagUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "State Emblem",
                                        modifier = Modifier.size(40.dp)
                                    )
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
                        IdentityImage(path, "Front", Modifier.weight(1f)) { 
                            viewModel.setFullScreenImage(path) 
                        }
                    }
                    doc.backImagePath?.let { path ->
                        IdentityImage(path, "Back", Modifier.weight(1f)) { 
                            viewModel.setFullScreenImage(path) 
                        }
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
fun IdentityImage(path: String, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
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
