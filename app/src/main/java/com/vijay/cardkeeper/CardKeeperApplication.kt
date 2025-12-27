package com.vijay.cardkeeper

import android.app.Application
import com.vijay.cardkeeper.di.AppContainer
import com.vijay.cardkeeper.di.DefaultAppContainer

class CardKeeperApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        System.loadLibrary("sqlcipher")
        container = DefaultAppContainer(this)
    }
}
