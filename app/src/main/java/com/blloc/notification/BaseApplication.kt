package com.blloc.notification

import android.app.Application
import com.blloc.notification.di.dataModule
import com.blloc.notification.di.notificationTrackerFragmentModule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

@ExperimentalCoroutinesApi
@FlowPreview
class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        initLogger()

        startKoin {
            androidLogger()
            androidContext(applicationContext)
            modules(
                dataModule,
                notificationTrackerFragmentModule
            )
        }
    }

    private fun initLogger() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}