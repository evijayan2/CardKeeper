package com.vijay.cardkeeper.ui.item

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.vijay.cardkeeper.ui.viewmodel.AppViewModelProvider
import com.vijay.cardkeeper.ui.viewmodel.ViewItemViewModel

@Composable
fun ViewItemRoute(
    itemId: Int,
    navigateBack: () -> Unit,
    onEditClick: (Int) -> Unit,
    viewModel: ViewItemViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current

    com.vijay.cardkeeper.ui.item.ViewItemScreen(
        itemId = itemId,
        navigateBack = navigateBack,
        onEditClick = onEditClick,
        onCopyContent = { content, label ->
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, content)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
        },
        onLaunchUrl = { url ->
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Could not open URL", Toast.LENGTH_SHORT).show()
            }
        },
        onDialNumber = { number ->
            try {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$number")
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Could not dial number", Toast.LENGTH_SHORT).show()
            }
        },
        barcodeContent = { content, format ->
            val barcodeBitmap = remember(content, format) {
                generateBarcodeBitmap(content, format)
            }
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

