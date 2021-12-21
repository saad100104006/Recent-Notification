package com.blloc.notification.di

import android.app.NotificationManager
import android.app.Service
import com.blloc.notification.data.db.NotificationRoomDatabase
import com.blloc.notification.data.mapper.ActiveNotificationCachedRawMapper
import com.blloc.notification.data.mapper.NotificationCachedRawMapper
import com.blloc.notification.data.preferences.Preferences
import com.blloc.notification.data.repository.NotificationRepositoryImpl
import com.blloc.notification.domain.repository.NotificationRepository
import com.blloc.notification.ui.notificationlist.RecentNotificationViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val dataModule = module {
    single { NotificationRoomDatabase.create(get()) }

    factory<NotificationRepository> {
        val notificationDao = get<NotificationRoomDatabase>().notificationDao()
        NotificationRepositoryImpl(notificationDao, NotificationCachedRawMapper(), ActiveNotificationCachedRawMapper())
    }

    factory {
        Preferences(androidContext())
    }

    factory {
        androidContext().getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
    }
}

@ExperimentalCoroutinesApi
@FlowPreview
val notificationTrackerFragmentModule = module {
    viewModel {
        RecentNotificationViewModel(get(), get(), get())
    }
}
