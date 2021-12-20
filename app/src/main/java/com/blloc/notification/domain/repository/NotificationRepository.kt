package com.blloc.notification.domain.repository

import com.blloc.notification.domain.entities.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun observeNotification(): Flow<List<Notification>>
    suspend fun storeNotify(key: String, appPackage: String, text: String, date: Long)
}
