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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.vijay.cardkeeper.R
import com.vijay.cardkeeper.data.entity.Passport
import com.vijay.cardkeeper.ui.viewmodel.AddItemViewModel
import com.vijay.cardkeeper.ui.viewmodel.AppViewModelProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewPassportScreen(
        passportId: Int,
        navigateBack: () -> Unit,
        onEditClick: (Int) -> Unit,
        viewModel: AddItemViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    var passport by remember { mutableStateOf<Passport?>(null) }
    var fullScreenImage by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    LaunchedEffect(passportId) { viewModel.getPassport(passportId).collect { passport = it } }

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
        if (passport != null) {
            val pass = passport!!
            Column(
                    modifier =
                            Modifier.padding(padding)
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Card with Flag
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
                        // Flag (Online via FlagCDN)
                        val countryCode = pass.countryCode.lowercase()
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
                                    placeholder = painterResource(R.drawable.ic_flag_usa),
                                    error = painterResource(R.drawable.ic_flag_usa),
                                    modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        } else {
                            // Fallback to local
                            val flagRes =
                                    when (pass.countryCode.uppercase()) {
                                        "USA", "US" -> R.drawable.ic_flag_usa
                                        "IND", "IN", "INDIA" -> R.drawable.ic_flag_india
                                        else -> null
                                    }

                            if (flagRes != null) {
                                Image(
                                        painter = painterResource(id = flagRes),
                                        contentDescription = "Country Flag",
                                        modifier = Modifier.size(48.dp)
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
                                modifier =
                                        Modifier.clickable {
                                            clipboardManager.setText(
                                                    AnnotatedString(pass.passportNumber)
                                            )
                                            // Toast.makeText(context, "Copied to clipboard",
                                            // Toast.LENGTH_SHORT).show()
                                            // avoiding Toast for pure compose if possible or just
                                            // rely on clipboard manager
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
                        PassportImage(path, "Front", Modifier.weight(1f)) { fullScreenImage = path }
                    }
                    pass.backImagePath?.let { path ->
                        PassportImage(path, "Back", Modifier.weight(1f)) { fullScreenImage = path }
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

                // Additional / Indian Specific Details
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
fun SectionHeader(title: String) {
    Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
fun PassportImage(path: String, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
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
