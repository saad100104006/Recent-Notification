package com.blloc.notification.data.mapper

import com.blloc.notification.data.db.model.NotifyCachedRaw
import com.blloc.notification.domain.entities.Notification
import java.time.Instant
import java.time.ZoneId

class NotificationCachedRawMapper {
    fun map(cached: NotifyCachedRaw): Notification = with(cached) {
        val millis = 3640L
        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toOffsetDateTime()
        Notification(
            key = key,
            appPackage = appPackage,
            text = text,
            date = created.toLocalDateTime()
        )
    }
}
