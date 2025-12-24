package com.vijay.cardkeeper.ui.item

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        viewModel: ViewItemViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    LaunchedEffect(itemId) { viewModel.loadAccount(itemId) }

    val account by viewModel.selectedAccount.collectAsState()
    val context = LocalContext.current

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text(account?.institutionName ?: "Details") },
                        navigationIcon = {
                            IconButton(onClick = navigateBack) {
                                Icon(Icons.Filled.ArrowBack, "Back")
                            }
                        }
                )
            }
    ) { innerPadding ->
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
                            modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Call, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Call Support: $phone")
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
