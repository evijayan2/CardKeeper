package com.vijay.cardkeeper.ui.item

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vijay.cardkeeper.ui.viewmodel.AppViewModelProvider
import com.vijay.cardkeeper.ui.viewmodel.ViewItemViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewItemScreen(
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
                        title = { Text(account?.institutionName ?: "Details") },
                        navigationIcon = {
                            IconButton(onClick = navigateBack) {
                                Icon(Icons.Filled.ArrowBack, "Back")
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
                    title = { Text("Delete Account") },
                    text = { Text("Are you sure you want to delete this account?") },
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
                // Header Card
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                                acc.type.name.replace("_", " "),
                                style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(acc.accountName, style = MaterialTheme.typography.headlineSmall)
                        if (!acc.cardNetwork.isNullOrBlank()) {
                            Text(acc.cardNetwork, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                // Images
                if (acc.frontImagePath != null || acc.backImagePath != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        acc.frontImagePath?.let { path ->
                            Box(
                                    modifier =
                                            Modifier.clickable {
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
                                            Modifier.clickable {
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

                // Details Section
                Text("Account Details", style = MaterialTheme.typography.titleMedium)

                // Number (Copyable)
                DetailRow(
                        label = "Number",
                        value = acc.number,
                        isCopyable = true,
                        context = context,
                        fontFamily = FontFamily.Monospace
                )

                acc.cvv?.let { cvv -> SecureDetailRow(label = "CVV", value = cvv) }

                acc.cardPin?.let { pin -> SecureDetailRow(label = "PIN", value = pin) }

                acc.expiryDate?.let {
                    DetailRow(
                            label = "Expiry",
                            value = it.toString()
                    ) // TODO: proper format if Long
                }

                acc.holderName.takeIf { it.isNotEmpty() }?.let {
                    DetailRow(label = "Holder Name", value = it)
                }

                // Actions
                acc.lostCardContactNumber?.takeIf { it.isNotEmpty() }?.let { phone ->
                    Button(
                            onClick = {
                                val intent =
                                        Intent(Intent.ACTION_DIAL).apply {
                                            data = Uri.parse("tel:$phone")
                                        }
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Icon(Icons.Filled.Call, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Call Support")
                    }
                }

                // Full Screen Image Dialog
                val fullScreenImage by viewModel.fullScreenImage.collectAsState()
                if (fullScreenImage != null) {
                    androidx.compose.ui.window.Dialog(
                            onDismissRequest = { viewModel.setFullScreenImage(null) },
                            properties =
                                    androidx.compose.ui.window.DialogProperties(
                                            usePlatformDefaultWidth = false
                                    )
                    ) {
                        Box(
                                modifier =
                                        Modifier.fillMaxSize()
                                                .background(
                                                        androidx.compose.ui.graphics.Color.Black
                                                )
                                                .clickable {
                                                    viewModel.setFullScreenImage(null)
                                                }, // Click anywhere to close
                                contentAlignment = Alignment.Center
                        ) {
                            val bitmap =
                                    remember(fullScreenImage) {
                                        try {
                                            android.graphics.BitmapFactory.decodeFile(
                                                    fullScreenImage
                                            )
                                        } catch (e: Exception) {
                                            null
                                        }
                                    }
                            if (bitmap != null) {
                                androidx.compose.foundation.Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "Full Screen Image",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                )
                            }
                        }
                    }
                }

                acc.notes?.takeIf { it.isNotEmpty() }?.let {
                    Text("Notes", style = MaterialTheme.typography.titleMedium)
                    Text(it, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun DetailRow(
        label: String,
        value: String,
        isCopyable: Boolean = false,
        context: Context? = null,
        fontFamily: FontFamily? = null
) {
    Column(
            modifier =
                    Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable(
                                    enabled = isCopyable
                            ) {
                        if (isCopyable && context != null) {
                            val clipboard =
                                    context.getSystemService(Context.CLIPBOARD_SERVICE) as
                                            ClipboardManager
                            val clip = ClipData.newPlainText(label, value)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "$label copied", Toast.LENGTH_SHORT).show()
                        }
                    }
    ) {
        Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = fontFamily,
                    modifier = Modifier.weight(1f)
            )
            if (isCopyable) {
                Icon(Icons.Filled.ContentCopy, "Copy", modifier = Modifier.size(16.dp))
            }
        }
        Divider(modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
fun SecureDetailRow(label: String, value: String) {
    var visible by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                    text = if (visible) value else "•".repeat(value.length),
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { visible = !visible }) {
                Icon(if (visible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, "Toggle")
            }
        }
        Divider(modifier = Modifier.padding(top = 8.dp))
    }
}
