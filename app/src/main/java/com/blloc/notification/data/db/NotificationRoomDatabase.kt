package com.blloc.notification.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.blloc.notification.BuildConfig
import com.blloc.notification.data.db.converter.OffsetDateTimeConverter
import com.blloc.notification.data.db.dao.NotifyDao
import com.blloc.notification.data.db.model.NotifyCachedRaw

@Database(
    entities = [NotifyCachedRaw::class],
    version = 7
)
@TypeConverters(OffsetDateTimeConverter::class)
abstract class NotificationRoomDatabase : RoomDatabase() {

    abstract fun notifyDao(): NotifyDao

    companion object Factory {
        fun create(context: Context): NotificationRoomDatabase {
            return Room.databaseBuilder(context, NotificationRoomDatabase::class.java, "app.db")
                .apply {
                    if (BuildConfig.DEBUG) {
                        fallbackToDestructiveMigration()
                    }
                }
                .build()
        }
    }
}
