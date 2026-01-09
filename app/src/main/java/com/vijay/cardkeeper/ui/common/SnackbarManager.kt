package com.vijay.cardkeeper.ui.common

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Singleton manager for global Snackbar events.
 * ViewModels can emit messages here without needing a direct reference to Scaffold state.
 */
object SnackbarManager {
    private val _messages = MutableSharedFlow<SnackbarMessage>()
    val messages: SharedFlow<SnackbarMessage> = _messages.asSharedFlow()

    suspend fun showMessage(message: String, actionLabel: String? = null, withDismissAction: Boolean = false) {
        _messages.emit(SnackbarMessage(message, actionLabel, withDismissAction))
    }
}

data class SnackbarMessage(
    val message: String,
    val actionLabel: String? = null,
    val withDismissAction: Boolean = false
)
