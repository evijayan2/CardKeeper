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
            val app = cardKeeperApplication()
            val workManager = androidx.work.WorkManager.getInstance(app)
            val scheduler = object : com.vijay.cardkeeper.domain.ExpirationScheduler {
                override fun scheduleExpirationCheck() {
                    val request = androidx.work.OneTimeWorkRequestBuilder<com.vijay.cardkeeper.worker.ExpirationCheckWorker>()
                            .build()
                    workManager.enqueue(request)
                }
            }
            AddItemViewModel(
                    app.container.financialRepository,
                    app.container.identityRepository,
                    app.container.passportRepository,
                    app.container.greenCardRepository,
                    app.container.aadharCardRepository,
                    app.container.giftCardRepository,
                    scheduler
            )
        }
        initializer { 
            val app = cardKeeperApplication()
            ViewItemViewModel(
                app.container.financialRepository,
                app.container.giftCardRepository,
                app.container.identityRepository,
                app.container.passportRepository,
                app.container.greenCardRepository,
                app.container.aadharCardRepository
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
