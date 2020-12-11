package com.shivam.emotions

import android.app.Application
import com.shivam.emotions.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber

class Emotions : Application() {

    override fun onCreate() {
        super.onCreate()

        initializeTimber()
        initializeKoin()

    }


    private fun initializeTimber() {
        Timber.plant(Timber.DebugTree())
    }

    private fun initializeKoin() {
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@Emotions)
            modules(appModule)
        }
    }
}