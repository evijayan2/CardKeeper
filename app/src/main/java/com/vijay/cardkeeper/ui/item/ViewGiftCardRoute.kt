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

@Composable
fun ViewGiftCardRoute(
    giftCardId: Int,
    navigateBack: () -> Unit,
    onEdit: (Int) -> Unit,
    viewModel: ViewItemViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    
    com.vijay.cardkeeper.ui.item.ViewGiftCardScreen(
        giftCardId = giftCardId,
        navigateBack = navigateBack,
        onEdit = onEdit,
        onCopyContent = { text, label ->
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "$label copied", Toast.LENGTH_SHORT).show()
        },
        qrCodeContent = { data ->
            // Use the QR generator from ViewAadharCardScreen wrapper or similar
            // For now, let's use a helper if available or implement inline
            val qrBitmap = rememberQrCodeBitmap(data)
            if (qrBitmap != null) {
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "QR Code",
                    modifier = Modifier.size(200.dp),
                    contentScale = ContentScale.Fit
                )
            }
        },
        barcodeContent = { content, format ->
            val barcodeBitmap = generateBarcodeBitmap(content, format)
            if (barcodeBitmap != null) {
                Image(
                    bitmap = barcodeBitmap.asImageBitmap(),
                    contentDescription = "Barcode",
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentScale = ContentScale.Fit
                )
            }
        },
        viewModel = viewModel
    )
}

@Composable
fun rememberQrCodeBitmap(data: String): android.graphics.Bitmap? {
    return androidx.compose.runtime.remember(data) {
        try {
            val writer = com.google.zxing.qrcode.QRCodeWriter()
            val size = 512
            val bitMatrix = writer.encode(data, com.google.zxing.BarcodeFormat.QR_CODE, size, size)
            val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.RGB_565)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            null
        }
    }
}
