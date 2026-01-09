package com.vijay.cardkeeper.ui.item

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vijay.cardkeeper.ui.viewmodel.AppViewModelProvider
import com.vijay.cardkeeper.ui.viewmodel.ViewItemViewModel
import com.vijay.cardkeeper.ui.item.ViewInsuranceCardScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ViewInsuranceCardRoute(
    cardId: Int,
    navigateBack: () -> Unit,
    onEdit: (Int) -> Unit,
    viewModel: ViewItemViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val card by viewModel.selectedInsuranceCard.collectAsState()

    LaunchedEffect(cardId) {
        viewModel.loadInsuranceCard(cardId)
    }

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

    card?.let { insuranceCard ->
        ViewInsuranceCardScreen(
            card = insuranceCard,
            onBackClick = navigateBack,
            onEditClick = { onEdit(insuranceCard.id) },
            onDeleteClick = {
                viewModel.deleteInsuranceCard(insuranceCard)
                navigateBack()
            },
            onCopyContent = { text, label ->
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(label, text)
                clipboard.setPrimaryClip(clip)

                scope.launch {
                    com.vijay.cardkeeper.ui.common.SnackbarManager.showMessage("$label copied")
                }
            },
            viewModel = viewModel,
            snackbarHostState = snackbarHostState
        )
    }
}
