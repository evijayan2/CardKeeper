package com.vijay.cardkeeper.ui.item

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vijay.cardkeeper.ui.viewmodel.AppViewModelProvider
import com.vijay.cardkeeper.ui.viewmodel.ViewItemViewModel
import androidx.compose.runtime.*
import kotlinx.coroutines.launch

@Composable
fun ViewGiftCardRoute(
    giftCardId: Int,
    navigateBack: () -> Unit,
    onEdit: (Int) -> Unit,
    viewModel: ViewItemViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Observe global snackbar messages
    LaunchedEffect(Unit) {
        com.vijay.cardkeeper.ui.common.SnackbarManager.messages.collect { message ->
            snackbarHostState.showSnackbar(
                message = message.message,
                actionLabel = message.actionLabel,
                withDismissAction = message.withDismissAction,
                duration = androidx.compose.material3.SnackbarDuration.Short
            )
        }
    }
    
    com.vijay.cardkeeper.ui.item.ViewGiftCardScreen(
        giftCardId = giftCardId,
        navigateBack = navigateBack,
        onEdit = onEdit,
        onCopyContent = { text, label ->
            // Copy to clipboard
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboard.setPrimaryClip(clip)
            
            // Show Snackbar instead of Toast
            scope.launch {
                com.vijay.cardkeeper.ui.common.SnackbarManager.showMessage("$label copied")
            }
        },
        qrCodeContent = { data ->
            var qrBitmap by remember(data) { mutableStateOf<android.graphics.Bitmap?>(null) }
            
            // Generate QR asynchronously
            LaunchedEffect(data) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        // Inline generation logic or use helper but ensuring it runs on IO
                        val writer = com.google.zxing.qrcode.QRCodeWriter()
                        val size = 512
                        val bitMatrix = writer.encode(data, com.google.zxing.BarcodeFormat.QR_CODE, size, size)
                        val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.RGB_565)
                        for (x in 0 until size) {
                            for (y in 0 until size) {
                                bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                            }
                        }
                        qrBitmap = bitmap
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            if (qrBitmap != null) {
                Image(
                    bitmap = qrBitmap!!.asImageBitmap(),
                    contentDescription = "QR Code",
                    modifier = Modifier.size(200.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                 androidx.compose.foundation.layout.Box(
                     modifier = Modifier.size(200.dp),
                     contentAlignment = androidx.compose.ui.Alignment.Center
                 ) {
                     androidx.compose.material3.CircularProgressIndicator()
                 }
            }
        },
        barcodeContent = { content, format ->
            var barcodeBitmap by remember(content, format) { mutableStateOf<android.graphics.Bitmap?>(null) }
            
            LaunchedEffect(content, format) {
               kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                   barcodeBitmap = generateBarcodeBitmap(content, format)
               }
            }

            if (barcodeBitmap != null) {
                Image(
                    bitmap = barcodeBitmap!!.asImageBitmap(),
                    contentDescription = "Barcode",
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentScale = ContentScale.Fit
                )
             } else {
                 androidx.compose.foundation.layout.Box(
                     modifier = Modifier.fillMaxWidth().height(100.dp),
                     contentAlignment = androidx.compose.ui.Alignment.Center
                 ) {
                     androidx.compose.material3.CircularProgressIndicator()
                 }
             }
        },
        viewModel = viewModel,
        snackbarHostState = snackbarHostState
    )
}


