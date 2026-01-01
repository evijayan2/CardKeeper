package com.vijay.cardkeeper.ui.item

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vijay.cardkeeper.ui.viewmodel.AppViewModelProvider
import com.vijay.cardkeeper.ui.viewmodel.ViewItemViewModel

@Composable
fun ViewIdentityRoute(
    documentId: Int,
    navigateBack: () -> Unit,
    onEditClick: (Int) -> Unit,
    viewModel: ViewItemViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    com.vijay.cardkeeper.ui.item.ViewIdentityScreen(
        documentId = documentId,
        navigateBack = navigateBack,
        onEditClick = onEditClick,
        viewModel = viewModel
    )
}
