package com.vijay.cardkeeper.ui.item

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.vijay.cardkeeper.data.entity.AccountType
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
                val cardColor = getAccountColor(acc)
                val isDark = cardColor != MaterialTheme.colorScheme.surface
                val contentColor =
                        if (isDark) androidx.compose.ui.graphics.Color.White
                        else MaterialTheme.colorScheme.onSurface

                ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                                CardDefaults.elevatedCardColors(
                                        containerColor = cardColor,
                                        contentColor = contentColor
                                )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                    acc.type.name.replace("_", " "),
                                    style = MaterialTheme.typography.labelMedium
                            )
                            val logoResId = getCardLogoResId(acc.cardNetwork)
                            if (logoResId != null) {
                                androidx.compose.foundation.Image(
                                        painter =
                                                androidx.compose.ui.res.painterResource(
                                                        id = logoResId
                                                ),
                                        contentDescription = null,
                                        modifier = Modifier.height(32.dp),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                )
                            }
                        }
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

                // Shop Logo
                acc.logoImagePath?.let {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        val bitmap =
                                remember(it) {
                                    try {
                                        android.graphics.BitmapFactory.decodeFile(it)
                                    } catch (e: Exception) {
                                        null
                                    }
                                }
                        if (bitmap != null) {
                            androidx.compose.foundation.Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Shop Logo",
                                    modifier =
                                            Modifier.height(100.dp)
                                                    .fillMaxWidth()
                                                    .padding(bottom = 16.dp),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                            )
                        }
                    }
                }

                // Barcode Section
                acc.barcode?.takeIf { it.isNotEmpty() }?.let { barcodeVal ->
                    Card(
                            modifier =
                                    Modifier.fillMaxWidth().clickable {
                                        viewModel.setFullScreenImage("BARCODE:$barcodeVal")
                                    }
                    ) {
                        Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Barcode", style = MaterialTheme.typography.labelSmall)
                            val barcodeBitmap =
                                    remember(barcodeVal, acc.barcodeFormat) {
                                        generateBarcodeBitmap(barcodeVal, acc.barcodeFormat)
                                    }
                            if (barcodeBitmap != null) {
                                androidx.compose.foundation.Image(
                                        bitmap = barcodeBitmap.asImageBitmap(),
                                        contentDescription = "Barcode",
                                        modifier = Modifier.fillMaxWidth().height(100.dp),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                )
                                Text(
                                        "Tap to view full screen",
                                        style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier =
                                                Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                            text = barcodeVal,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                            onClick = {
                                                val clipboard =
                                                        context.getSystemService(
                                                                Context.CLIPBOARD_SERVICE
                                                        ) as
                                                                ClipboardManager
                                                val clip =
                                                        ClipData.newPlainText("Barcode", barcodeVal)
                                                clipboard.setPrimaryClip(clip)
                                                Toast.makeText(
                                                                context,
                                                                "Barcode copied",
                                                                Toast.LENGTH_SHORT
                                                        )
                                                        .show()
                                            }
                                    ) {
                                        Icon(
                                                Icons.Filled.ContentCopy,
                                                "Copy",
                                                modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Details Section
                if (acc.type != AccountType.REWARDS_CARD) {
                    Text("Account Details", style = MaterialTheme.typography.titleMedium)
                }

                // Number (Copyable)
                if (acc.type != AccountType.REWARDS_CARD) {
                    DetailRow(
                            label = "Number",
                            value = acc.number,
                            isCopyable = true,
                            context = context,
                            fontFamily = FontFamily.Monospace
                    )
                }

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

                acc.linkedPhoneNumber?.takeIf { it.isNotEmpty() }?.let {
                    DetailRow(
                            label = "Linked Phone",
                            value = it,
                            isCopyable = true,
                            context = context
                    )
                }

                if (acc.type == AccountType.BANK_ACCOUNT) {
                    acc.routingNumber?.takeIf { it.isNotEmpty() }?.let {
                        DetailRow(
                                label = "Routing Number",
                                value = it,
                                isCopyable = true,
                                context = context,
                                fontFamily = FontFamily.Monospace
                        )
                    }
                    acc.ifscCode?.takeIf { it.isNotEmpty() }?.let {
                        DetailRow(
                                label = "IFSC Code",
                                value = it,
                                isCopyable = true,
                                context = context,
                                fontFamily = FontFamily.Monospace
                        )
                    }
                    acc.swiftCode?.takeIf { it.isNotEmpty() }?.let {
                        DetailRow(
                                label = "SWIFT Code",
                                value = it,
                                isCopyable = true,
                                context = context,
                                fontFamily = FontFamily.Monospace
                        )
                    }
                    acc.wireNumber?.takeIf { it.isNotEmpty() }?.let {
                        DetailRow(
                                label = "Wire Number",
                                value = it,
                                isCopyable = true,
                                context = context,
                                fontFamily = FontFamily.Monospace
                        )
                    }
                    acc.branchAddress?.takeIf { it.isNotEmpty() }?.let {
                        DetailRow(
                                label = "Branch Address",
                                value = it,
                                isCopyable = true,
                                context = context
                        )
                    }
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

                if (acc.type == AccountType.BANK_ACCOUNT) {
                    acc.branchContactNumber?.takeIf { it.isNotEmpty() }?.let { phone ->
                        Button(
                                onClick = {
                                    val intent =
                                            Intent(Intent.ACTION_DIAL).apply {
                                                data = Uri.parse("tel:$phone")
                                            }
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Icon(Icons.Filled.Call, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Call Bank")
                        }
                    }
                    acc.bankWebUrl?.takeIf { it.isNotEmpty() }?.let { url ->
                        val finalUrl = if (!url.startsWith("http")) "https://$url" else url
                        Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors =
                                        ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.secondary
                                        )
                        ) {
                            // Using a generic world icon or similar if available, else just text or
                            // existing icon
                            Icon(
                                    Icons.Filled.Visibility,
                                    null
                            ) // Using Visibility as placeholder or just Text
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Visit Website")
                        }
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
                                        if (fullScreenImage?.startsWith("BARCODE:") == true) {
                                            val code = fullScreenImage!!.substring(8)
                                            generateBarcodeBitmap(code, acc.barcodeFormat)
                                        } else {
                                            try {
                                                android.graphics.BitmapFactory.decodeFile(
                                                        fullScreenImage
                                                )
                                            } catch (e: Exception) {
                                                null
                                            }
                                        }
                                    }

                            // Brightness Control for Barcode
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
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
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
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}

fun generateBarcodeBitmap(content: String, format: Int?): Bitmap? {
    return try {
        val zxingFormat = if (format != null) mapToZXingFormat(format) else BarcodeFormat.CODE_128
        val writer = MultiFormatWriter()
        val bitMatrix: BitMatrix = writer.encode(content, zxingFormat, 600, 300)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(
                        x,
                        y,
                        if (bitMatrix[x, y]) AndroidColor.BLACK else AndroidColor.WHITE
                )
            }
        }
        bitmap
    } catch (e: Exception) {
        null
    }
}

fun mapToZXingFormat(format: Int): BarcodeFormat {
    return when (format) {
        Barcode.FORMAT_QR_CODE -> BarcodeFormat.QR_CODE
        Barcode.FORMAT_UPC_A -> BarcodeFormat.UPC_A
        Barcode.FORMAT_UPC_E -> BarcodeFormat.UPC_E
        Barcode.FORMAT_EAN_13 -> BarcodeFormat.EAN_13
        Barcode.FORMAT_EAN_8 -> BarcodeFormat.EAN_8
        Barcode.FORMAT_CODE_128 -> BarcodeFormat.CODE_128
        Barcode.FORMAT_CODE_39 -> BarcodeFormat.CODE_39
        Barcode.FORMAT_CODE_93 -> BarcodeFormat.CODE_93
        Barcode.FORMAT_CODABAR -> BarcodeFormat.CODABAR
        Barcode.FORMAT_DATA_MATRIX -> BarcodeFormat.DATA_MATRIX
        Barcode.FORMAT_ITF -> BarcodeFormat.ITF
        Barcode.FORMAT_PDF417 -> BarcodeFormat.PDF_417
        Barcode.FORMAT_AZTEC -> BarcodeFormat.AZTEC
        else -> BarcodeFormat.CODE_128
    }
}

@Composable
fun getCardLogoResId(network: String?): Int? {
    if (network == null) return null
    return when {
        network.contains("Visa", ignoreCase = true) -> com.vijay.cardkeeper.R.drawable.ic_brand_visa
        network.contains("Master", ignoreCase = true) ->
                com.vijay.cardkeeper.R.drawable.ic_brand_mastercard
        network.contains("Amex", ignoreCase = true) -> com.vijay.cardkeeper.R.drawable.ic_brand_amex
        network.contains("Discover", ignoreCase = true) ->
                com.vijay.cardkeeper.R.drawable.ic_brand_discover
        network.contains("Capital", ignoreCase = true) ->
                com.vijay.cardkeeper.R.drawable.ic_brand_capitolone
        network.contains("Rupay", ignoreCase = true) ->
                com.vijay.cardkeeper.R.drawable.ic_brand_rupay
        else -> null
    }
}

@Composable
fun getAccountColor(
        account: com.vijay.cardkeeper.data.entity.FinancialAccount
): androidx.compose.ui.graphics.Color {
    if (account.colorTheme != null) {
        return androidx.compose.ui.graphics.Color(account.colorTheme)
    }
    if (account.bankBrandColor != null) {
        return androidx.compose.ui.graphics.Color(account.bankBrandColor)
    }
    val network = account.cardNetwork
    return when {
        network?.contains("Visa", ignoreCase = true) == true ->
                androidx.compose.ui.graphics.Color(0xFF1A1F71)
        network?.contains("Master", ignoreCase = true) == true ->
                androidx.compose.ui.graphics.Color(0xFF222222)
        network?.contains("Amex", ignoreCase = true) == true ->
                androidx.compose.ui.graphics.Color(0xFF006FCF)
        network?.contains("Discover", ignoreCase = true) == true ->
                androidx.compose.ui.graphics.Color(0xFFE55C20)
        network?.contains("Rupay", ignoreCase = true) == true ->
                androidx.compose.ui.graphics.Color(0xFF1B3F6B)
        account.institutionName.contains("Chase", ignoreCase = true) ->
                androidx.compose.ui.graphics.Color(0xFF117ACA)
        account.institutionName.contains("Citi", ignoreCase = true) ->
                androidx.compose.ui.graphics.Color(0xFF003B70)
        account.institutionName.contains("Wells", ignoreCase = true) ->
                androidx.compose.ui.graphics.Color(0xFFCD1409)
        account.institutionName.contains("Boa", ignoreCase = true) ||
                account.institutionName.contains("Bank of America", ignoreCase = true) ->
                androidx.compose.ui.graphics.Color(0xFFDC1431)
        else -> MaterialTheme.colorScheme.surface
    }
}
