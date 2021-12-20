package com.blloc.notification.data.repository

import com.blloc.notification.data.db.dao.NotifyDao
import com.blloc.notification.data.db.model.NotifyCachedRaw
import com.blloc.notification.data.mapper.NotifationCachedRawMapper
import com.blloc.notification.domain.entities.Notification
import com.blloc.notification.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.ZoneId

class NotificationRepositoryImpl constructor(
    private val notifyDao: NotifyDao,
    private val notifationMapper: NotifationCachedRawMapper
) : NotificationRepository {
    override fun observeNotification(): Flow<List<Notification>> {
        return notifyDao.observeAll()
            .map { list -> list.map(notifationMapper::map) }
    }

    override suspend fun storeNotify(key: String, appPackage: String, text: String, date: Long) {
        notifyDao.insert(
            NotifyCachedRaw(
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
