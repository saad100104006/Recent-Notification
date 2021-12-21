package com.blloc.notification.data.repository

import com.blloc.notification.data.db.dao.NotificationDao
import com.blloc.notification.data.db.model.ActiveNotificationCachedRaw
import com.blloc.notification.data.db.model.NotifyCachedRaw
import com.blloc.notification.data.mapper.ActiveNotificationCachedRawMapper
import com.blloc.notification.data.mapper.NotificationCachedRawMapper
import com.blloc.notification.domain.entities.Notification
import com.blloc.notification.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.ZoneId

class NotificationRepositoryImpl constructor(
    private val notificationDao: NotificationDao,
    private val notificationMapper: NotificationCachedRawMapper,
    private val activeNotificationMapper: ActiveNotificationCachedRawMapper
) : NotificationRepository {

    override fun observeNotification(): Flow<List<Notification>> {
        return  notificationDao.observeAll()
        .map { list -> list.map(notificationMapper::map) }
    }

    override fun observeActiveNotification(): Flow<List<Notification>> {
        return  notificationDao.observeActive()
            .map { list -> list.map(activeNotificationMapper::map) }
    }

    override suspend fun storeNotification(key: String, appPackage: String, text: String, date: Long) {
        notificationDao.insert(
            NotifyCachedRaw(
                key = key,
                appPackage = appPackage,
                text = text,
                created = date.toOffsetDateTime()
            )
        )
    }

    override suspend fun storeActiveNotification(
        key: String,
        appPackage: String,
        text: String,
        date: Long
    ) {
        notificationDao.insertActive(
            ActiveNotificationCachedRaw(
                key = key,
                appPackage = appPackage,
                text = text,
                created = date.toOffsetDateTime()
            )
        )
    }

    override suspend fun deleteActiveNotification(key: String, appPackage: String, text: String, date: Long) {
        notificationDao.deleteActiveNotification(
            ActiveNotificationCachedRaw(
                key = key,
                appPackage = appPackage,
                text = text,
                created = date.toOffsetDateTime()
            )
        )
    }

    private fun Long.toOffsetDateTime() =
        Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toOffsetDateTime()
}
