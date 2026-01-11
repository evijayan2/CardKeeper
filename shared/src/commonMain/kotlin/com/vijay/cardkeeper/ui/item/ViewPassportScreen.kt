package com.vijay.cardkeeper.ui.item

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import kards.shared.generated.resources.Res
import kards.shared.generated.resources.ic_flag_usa
import kards.shared.generated.resources.ic_flag_india
import com.vijay.cardkeeper.data.entity.Passport
import com.vijay.cardkeeper.ui.viewmodel.ViewItemViewModel
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewPassportScreen(
    passportId: Int,
    navigateBack: () -> Unit,
    onEditClick: (Int) -> Unit,
    onCopyContent: (String, String) -> Unit,
    viewModel: ViewItemViewModel
) {
    val passport by viewModel.selectedPassport.collectAsState()
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val platformContext = LocalPlatformContext.current

    LaunchedEffect(passportId) { 
        viewModel.loadPassport(passportId) 
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Passport Details") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onEditClick(passportId) }) {
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
                title = { Text("Delete Passport") },
                text = { Text("Are you sure you want to delete this passport?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            passport?.let { viewModel.deletePassport(it) }
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
            passport?.let { pass ->
                // Header Card with Flag
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
                        // Flag (Online via FlagCDN)
                        val countryCode = pass.countryCode.lowercase()
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
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        } else {
                            // Fallback to local
                            val flagRes = when (pass.countryCode.uppercase()) {
                                "USA", "US" -> Res.drawable.ic_flag_usa
                                "IND", "IN", "INDIA" -> Res.drawable.ic_flag_india
                                else -> null
                            }

                            if (flagRes != null) {
                                Icon(
                                    painter = painterResource(flagRes),
                                    contentDescription = "Country Flag",
                                    modifier = Modifier.size(48.dp),
                                    tint = Color.Unspecified
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        Text(
                            text = "Passport (${pass.countryCode})",
                            style = MaterialTheme.typography.headlineSmall
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Copyable Passport Number
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                onCopyContent(pass.passportNumber, "Passport Number")
                            }
                        ) {
                            Text(
                                text = pass.passportNumber,
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pass.frontImagePath?.let { path ->
                        PassportImage(path, "Front", Modifier.weight(1f)) { viewModel.setFullScreenImage(path) }
                    }
                    pass.backImagePath?.let { path ->
                        PassportImage(path, "Back", Modifier.weight(1f)) { viewModel.setFullScreenImage(path) }
                    }
                }

                // Personal Details
                SectionHeader("Personal Details")
                DetailRow("Surname", pass.surname)
                DetailRow("Given Names", pass.givenNames)
                DetailRow("Nationality", pass.nationality)
                DetailRow("Date of Birth", pass.dob)
                DetailRow("Sex", pass.sex)
                DetailRow("Place of Birth", pass.placeOfBirth)

                // Passport Details
                SectionHeader("Passport Details")
                DetailRow("Date of Issue", pass.dateOfIssue)
                DetailRow("Date of Expiry", pass.dateOfExpiry)
                DetailRow("Place of Issue", pass.placeOfIssue)
                DetailRow("Authority", pass.authority)

                // Additional Details
                if (!pass.fatherName.isNullOrEmpty() ||
                    !pass.motherName.isNullOrEmpty() ||
                    !pass.spouseName.isNullOrEmpty() ||
                    !pass.address.isNullOrEmpty() ||
                    !pass.fileNumber.isNullOrEmpty()
                ) {
                    HorizontalDivider()
                    SectionHeader("Additional Details")

                    DetailRow("Father's Name", pass.fatherName)
                    DetailRow("Mother's Name", pass.motherName)
                    DetailRow("Spouse's Name", pass.spouseName)
                    DetailRow("File Number", pass.fileNumber)
                    DetailRow("Address", pass.address)
                }
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
fun PassportImage(path: String, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
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
