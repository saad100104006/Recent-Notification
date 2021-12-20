package com.blloc.notification.data.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.blloc.notification.data.preferences.Preferences
import com.blloc.notification.domain.repository.NotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber

class RecentNotificationService : NotificationListenerService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private val repository: NotificationRepository by inject()
    private val preferences: Preferences by inject()


    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        handleNotification(sbn)
    }

    private fun handleNotification(sbn: StatusBarNotification?) {
        if (!preferences.notificationTrackerEnable) return

        Timber.d("notification: ${sbn.toString()}")
        scope.launch {
            checkNotNull(sbn)
            if (sbn.notification.tickerText != null) {
                repository.storeNotify(
                    key = sbn.key,
                    appPackage = sbn.opPkg,
                    text = sbn.notification.tickerText?.toString() ?: "unknown",
                    date = sbn.postTime
                )
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
