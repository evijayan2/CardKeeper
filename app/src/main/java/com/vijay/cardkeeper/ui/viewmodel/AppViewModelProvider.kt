package com.vijay.cardkeeper.ui.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.vijay.cardkeeper.CardKeeperApplication

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            HomeViewModel(
                    cardKeeperApplication().container.financialRepository,
                    cardKeeperApplication().container.identityRepository,
                    cardKeeperApplication().container.passportRepository,
                    cardKeeperApplication().container.greenCardRepository,
                    cardKeeperApplication().container.aadharCardRepository,
                    cardKeeperApplication().container.giftCardRepository
            )
        }
        initializer {
            AddItemViewModel(
                    cardKeeperApplication().container.financialRepository,
                    cardKeeperApplication().container.identityRepository,
                    cardKeeperApplication().container.passportRepository,
                    cardKeeperApplication().container.greenCardRepository,
                    cardKeeperApplication().container.aadharCardRepository,
                    cardKeeperApplication().container.giftCardRepository
            )
        }
        initializer { 
            ViewItemViewModel(
                cardKeeperApplication().container.financialRepository,
                cardKeeperApplication().container.giftCardRepository
            ) 
        }

        initializer { SearchViewModel(cardKeeperApplication().container.searchRepository) }
        initializer {
            SettingsViewModel(
                cardKeeperApplication().container.userPreferencesRepository
            )
        }
    }
}

/**
 * Extension function to queries for [Application] object and returns an instance of
 * [CardKeeperApplication].
 */
fun CreationExtras.cardKeeperApplication(): CardKeeperApplication =
        (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CardKeeperApplication)
