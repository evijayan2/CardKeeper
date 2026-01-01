package com.vijay.cardkeeper.ui.item

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vijay.cardkeeper.ui.viewmodel.AppViewModelProvider
import com.vijay.cardkeeper.ui.viewmodel.ViewItemViewModel

@Composable
fun ViewRewardsRoute(
    itemId: Int,
    navigateBack: () -> Unit,
    onEditClick: (Int) -> Unit,
    viewModel: ViewItemViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    
    com.vijay.cardkeeper.ui.item.ViewRewardsScreen(
        itemId = itemId,
        navigateBack = navigateBack,
        onEditClick = onEditClick,
        onDialNumber = { phone ->
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phone")
            }
            context.startActivity(intent)
        },
        onGenerateBarcode = { content, format ->
            generateBarcodeBitmap(content, format)?.asImageBitmap()
        },
        onSetBrightness = { brightness ->
            val activity = context as? Activity
            val window = activity?.window
            val lp = window?.attributes
            if (lp != null) {
                lp.screenBrightness = brightness
                window?.attributes = lp
            }
        },
        viewModel = viewModel
    )
}
