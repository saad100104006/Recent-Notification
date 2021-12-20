package com.blloc.notification.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.blloc.notification.data.db.model.NotifyCachedRaw
import kotlinx.coroutines.flow.Flow


@Dao
interface NotifyDao {
    @Query("""SELECT * FROM notifications ORDER BY datetime(created_date) DESC LIMIT 20""")
    fun observeAll(): Flow<List<NotifyCachedRaw>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: NotifyCachedRaw)
}
