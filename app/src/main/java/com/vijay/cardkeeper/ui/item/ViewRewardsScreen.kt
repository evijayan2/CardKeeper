package com.vijay.cardkeeper.ui.item

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.vijay.cardkeeper.ui.viewmodel.AppViewModelProvider
import com.vijay.cardkeeper.ui.viewmodel.ViewItemViewModel
import com.vijay.cardkeeper.util.LogoUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewRewardsScreen(
        itemId: Int,
        navigateBack: () -> Unit,
        onEditClick: (Int) -> Unit,
        viewModel: ViewItemViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    LaunchedEffect(itemId) { viewModel.loadAccount(itemId) }

    val account by viewModel.selectedAccount.collectAsState()
    val context = LocalContext.current
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text(account?.institutionName ?: "Reward Details") },
                        navigationIcon = {
                            IconButton(onClick = navigateBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                            }
                        },
                        actions = {
                            IconButton(onClick = { account?.let { onEditClick(it.id) } }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = { showDeleteConfirmation = true }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete")
                            }
                        }
                )
            }
    ) { innerPadding ->
        if (showDeleteConfirmation) {
            AlertDialog(
                    onDismissRequest = { showDeleteConfirmation = false },
                    title = { Text("Delete Card") },
                    text = { Text("Are you sure you want to delete this card?") },
                    confirmButton = {
                        TextButton(
                                onClick = {
                                    account?.let { viewModel.deleteAccount(it) }
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
                modifier =
                        Modifier.padding(innerPadding)
                                .fillMaxSize()
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            account?.let { acc ->
                // Banner / Card Header
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Shop Logo
                        if (acc.logoImagePath != null) {
                            val bitmap =
                                    remember(acc.logoImagePath) {
                                        try {
                                            BitmapFactory.decodeFile(acc.logoImagePath)
                                        } catch (e: Exception) {
                                            null
                                        }
                                    }
                            if (bitmap != null) {
                                androidx.compose.foundation.Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "Logo",
                                        modifier = Modifier.height(80.dp).fillMaxWidth(),
                                        contentScale = ContentScale.Fit
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        } else {
                            // Dynamic Logo Fallback
                            val dynamicLogoUrl =
                                    LogoUtils.getInstitutionLogoUrl(acc.institutionName)
                            if (dynamicLogoUrl != null) {
                                AsyncImage(
                                        model =
                                                ImageRequest.Builder(LocalContext.current)
                                                        .data(dynamicLogoUrl)
                                                        .crossfade(true)
                                                        .build(),
                                        contentDescription = "Logo",
                                        modifier = Modifier.height(80.dp).fillMaxWidth(),
                                        contentScale = ContentScale.Fit
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }

                        Text(
                                text = acc.institutionName,
                                style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                                text = acc.type.name.replace("_", " "),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                // Barcode Section - Prominent
                if (!acc.barcode.isNullOrEmpty()) {
                    Card(
                            colors =
                                    CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                    ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier =
                                    Modifier.fillMaxWidth().clickable {
                                        viewModel.setFullScreenImage("BARCODE:${acc.barcode}")
                                    }
                    ) {
                        Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Scan Barcode", style = MaterialTheme.typography.labelSmall)
                            Spacer(modifier = Modifier.height(8.dp))

                            val barcodeBitmap =
                                    remember(acc.barcode, acc.barcodeFormat) {
                                        generateBarcodeBitmap(acc.barcode, acc.barcodeFormat)
                                    }

                            if (barcodeBitmap != null) {
                                androidx.compose.foundation.Image(
                                        bitmap = barcodeBitmap.asImageBitmap(),
                                        contentDescription = "Barcode",
                                        modifier = Modifier.fillMaxWidth().height(120.dp),
                                        contentScale = ContentScale.Fit
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                    text = acc.barcode,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Linked Phone
                if (!acc.linkedPhoneNumber.isNullOrEmpty()) {
                    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Linked Phone Number", style = MaterialTheme.typography.labelSmall)
                            Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                        text = acc.linkedPhoneNumber,
                                        style = MaterialTheme.typography.titleMedium
                                )
                                IconButton(
                                        onClick = {
                                            val intent =
                                                    Intent(Intent.ACTION_DIAL).apply {
                                                        data =
                                                                Uri.parse(
                                                                        "tel:${acc.linkedPhoneNumber}"
                                                                )
                                                    }
                                            context.startActivity(intent)
                                        }
                                ) { Icon(Icons.Filled.Call, "Call") }
                            }
                        }
                    }
                }

                // Notes
                if (!acc.notes.isNullOrEmpty()) {
                    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Notes", style = MaterialTheme.typography.labelSmall)
                            Text(text = acc.notes, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                // Images
                if (acc.frontImagePath != null || acc.backImagePath != null) {
                    Text("Card Images", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        acc.frontImagePath?.let { path ->
                            Box(
                                    modifier =
                                            Modifier.weight(1f).clickable {
                                                viewModel.setFullScreenImage(path)
                                            }
                            ) {
                                com.vijay.cardkeeper.ui.home.DashboardImageThumbnail(
                                        path = path,
                                        label = "Front"
                                )
                            }
                        }
                        acc.backImagePath?.let { path ->
                            Box(
                                    modifier =
                                            Modifier.weight(1f).clickable {
                                                viewModel.setFullScreenImage(path)
                                            }
                            ) {
                                com.vijay.cardkeeper.ui.home.DashboardImageThumbnail(
                                        path = path,
                                        label = "Back"
                                )
                            }
                        }
                    }
                }

                // Full Screen Image Dialog (Reuse logic)
                val fullScreenImage by viewModel.fullScreenImage.collectAsState()
                if (fullScreenImage != null) {
                    Dialog(
                            onDismissRequest = { viewModel.setFullScreenImage(null) },
                            properties = DialogProperties(usePlatformDefaultWidth = false)
                    ) {
                        Box(
                                modifier =
                                        Modifier.fillMaxSize().background(Color.Black).clickable {
                                            viewModel.setFullScreenImage(null)
                                        },
                                contentAlignment = Alignment.Center
                        ) {
                            val bitmap =
                                    remember(fullScreenImage) {
                                        if (fullScreenImage?.startsWith("BARCODE:") == true) {
                                            val code = fullScreenImage!!.substring(8)
                                            generateBarcodeBitmap(code, acc.barcodeFormat)
                                        } else {
                                            try {
                                                BitmapFactory.decodeFile(fullScreenImage)
                                            } catch (e: Exception) {
                                                null
                                            }
                                        }
                                    }

                            // Max Brightness for Barcode
                            if (fullScreenImage?.startsWith("BARCODE:") == true) {
                                DisposableEffect(Unit) {
                                    val activity = context as? android.app.Activity
                                    val window = activity?.window
                                    val originalBrightness = window?.attributes?.screenBrightness
                                    val lp = window?.attributes
                                    lp?.screenBrightness = 1f
                                    window?.attributes = lp
                                    onDispose {
                                        lp?.screenBrightness = originalBrightness ?: -1f
                                        window?.attributes = lp
                                    }
                                }
                            }

                            if (bitmap != null) {
                                androidx.compose.foundation.Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "Full Screen",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
