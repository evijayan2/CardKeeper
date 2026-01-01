package com.vijay.cardkeeper.ui.item

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vijay.cardkeeper.ui.viewmodel.AppViewModelProvider
import com.vijay.cardkeeper.ui.viewmodel.ViewItemViewModel

@Composable
fun ViewGreenCardRoute(
    greenCardId: Int,
    navigateBack: () -> Unit,
    onEditClick: (Int) -> Unit,
    viewModel: ViewItemViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    
    com.vijay.cardkeeper.ui.item.ViewGreenCardScreen(
        greenCardId = greenCardId,
        navigateBack = navigateBack,
        onEditClick = onEditClick,
        onCopyContent = { text, label ->
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "$label copied", Toast.LENGTH_SHORT).show()
        },
        viewModel = viewModel
    )
}
