package com.vijay.cardkeeper.ui.settings

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vijay.cardkeeper.ui.viewmodel.AppViewModelProvider
import com.vijay.cardkeeper.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsRoute(
    navigateBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    com.vijay.cardkeeper.ui.settings.SettingsScreen(
        navigateBack = navigateBack,
        viewModel = viewModel
    )
}
