package com.vijay.cardkeeper.ui.search

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vijay.cardkeeper.ui.viewmodel.AppViewModelProvider
import com.vijay.cardkeeper.ui.viewmodel.SearchViewModel

@Composable
fun SearchRoute(
    onNavigateBack: () -> Unit,
    onResultClick: (Int, String) -> Unit,
    viewModel: SearchViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    com.vijay.cardkeeper.ui.search.SearchScreen(
        onNavigateBack = onNavigateBack,
        onResultClick = onResultClick,
        viewModel = viewModel
    )
}
