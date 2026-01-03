package com.vijay.cardkeeper.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vijay.cardkeeper.ui.common.SnackbarManager
import kotlinx.coroutines.launch

/**
 * Base ViewModel for standardizing event handling (Navigation, Snackbars).
 */
abstract class BaseViewModel : ViewModel() {
    
    fun showSnackbar(message: String, actionLabel: String? = null) {
        viewModelScope.launch {
            SnackbarManager.showMessage(message, actionLabel)
        }
    }
}
