package com.vijay.cardkeeper

import android.app.Application
import com.vijay.cardkeeper.di.AppContainer
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager

import com.vijay.cardkeeper.di.DefaultAppContainer
import com.vijay.cardkeeper.worker.ExpirationCheckWorker
import java.util.concurrent.TimeUnit

import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.util.DebugLogger
import com.vijay.cardkeeper.util.EncryptedFileFetcher
import com.vijay.cardkeeper.util.EncryptedImageDecoder

class CardKeeperApplication : Application(), SingletonImageLoader.Factory {

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

    override fun newImageLoader(context: android.content.Context): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(EncryptedFileFetcher.Factory())
                add(EncryptedImageDecoder.Factory(context))
            }
            .diskCachePolicy(coil3.request.CachePolicy.DISABLED) // Disable disk cache for security
            .logger(DebugLogger())
            .build()
    }
}
