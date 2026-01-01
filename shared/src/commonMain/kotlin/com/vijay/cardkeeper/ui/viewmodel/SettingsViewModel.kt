package com.vijay.cardkeeper.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vijay.cardkeeper.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val themeMode: StateFlow<String> = userPreferencesRepository.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "SYSTEM"
        )

    val dateFormat: StateFlow<String> = userPreferencesRepository.dateFormat
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "DEFAULT"
        )
    
    val notificationsEnabled: StateFlow<Boolean> = userPreferencesRepository.notificationsEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = true

        )

    val reminder1Days: StateFlow<Int> = userPreferencesRepository.reminder1Days
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 21
        )

    val reminder2Days: StateFlow<Int> = userPreferencesRepository.reminder2Days
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 11
        )

    val reminder3Days: StateFlow<Int> = userPreferencesRepository.reminder3Days
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 1
        )

    fun selectTheme(theme: String) {
        viewModelScope.launch {
            userPreferencesRepository.saveThemeMode(theme)
        }
    }

    fun selectDateFormat(format: String) {
        viewModelScope.launch {
            userPreferencesRepository.saveDateFormat(format)
        }
    }
    
    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.saveNotificationsEnabled(enabled)
        }
    }

    fun updateReminder1Days(days: String) {
        val daysInt = days.toIntOrNull() ?: return
        viewModelScope.launch {
            userPreferencesRepository.saveReminder1Days(daysInt)
        }
    }

    fun updateReminder2Days(days: String) {
        val daysInt = days.toIntOrNull() ?: return
        viewModelScope.launch {
            userPreferencesRepository.saveReminder2Days(daysInt)
        }
    }

    fun updateReminder3Days(days: String) {
        val daysInt = days.toIntOrNull() ?: return
        viewModelScope.launch {
            userPreferencesRepository.saveReminder3Days(daysInt)
        }
    }
}
