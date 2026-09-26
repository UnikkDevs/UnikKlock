package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.AlarmEntity
import com.example.data.model.AlarmHistoryEntity

@Database(
  entities = [AlarmEntity::class, AlarmHistoryEntity::class],
  version = 1,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun alarmDao(): AlarmDao
  abstract fun alarmHistoryDao(): AlarmHistoryDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getInstance(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val appContext = context.applicationContext
        val oldDbFile = appContext.getDatabasePath("wakequest_database")
        val newDbFile = appContext.getDatabasePath("unikklock_database")
        if (oldDbFile.exists() && !newDbFile.exists()) {
          try {
            oldDbFile.copyTo(newDbFile, overwrite = false)
          } catch (_: Exception) {}
        }

        val instance = Room.databaseBuilder(
          appContext,
          AppDatabase::class.java,
          "unikklock_database"
        )
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
