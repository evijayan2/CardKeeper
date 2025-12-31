package com.vijay.cardkeeper

import android.app.Application
import com.vijay.cardkeeper.di.AppContainer
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager

import com.vijay.cardkeeper.di.DefaultAppContainer
import com.vijay.cardkeeper.worker.ExpirationCheckWorker
import java.util.concurrent.TimeUnit

class CardKeeperApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        System.loadLibrary("sqlcipher")
        container = DefaultAppContainer(this)
        scheduleWork()
    }

    private fun scheduleWork() {
        val workRequest = PeriodicWorkRequestBuilder<ExpirationCheckWorker>(1, TimeUnit.DAYS)
            .build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "ExpirationCheck",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
