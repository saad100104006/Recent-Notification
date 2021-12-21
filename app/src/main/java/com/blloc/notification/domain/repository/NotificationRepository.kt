package com.blloc.notification.domain.repository

import com.blloc.notification.domain.entities.Notification
import com.blloc.notification.ui.notificationlist.RecentNotificationViewModel
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun observeNotification(): Flow<List<Notification>>
    fun observeActiveNotification(): Flow<List<Notification>>
    suspend fun storeNotification(key: String, appPackage: String, text: String, date: Long)
    suspend fun storeActiveNotification(key: String, appPackage: String, text: String, date: Long)
    suspend fun deleteActiveNotification(key: String, appPackage: String, text: String, date: Long)
}
