package com.blloc.notification.data.db.dao

import androidx.room.*
import com.blloc.notification.data.db.model.ActiveNotificationCachedRaw
import com.blloc.notification.data.db.model.NotifyCachedRaw
import kotlinx.coroutines.flow.Flow


@Dao
interface NotificationDao {
    @Query("""SELECT * FROM notifications ORDER BY datetime(created_date) DESC LIMIT 20""")
    fun observeAll(): Flow<List<NotifyCachedRaw>>

    @Query("""SELECT * FROM active_notifications ORDER BY datetime(created_date) DESC LIMIT 20""")
    fun observeActive(): Flow<List<ActiveNotificationCachedRaw>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: NotifyCachedRaw)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActive(entity: ActiveNotificationCachedRaw)

    @Delete
    suspend fun deleteActiveNotification(entity: ActiveNotificationCachedRaw)

}
