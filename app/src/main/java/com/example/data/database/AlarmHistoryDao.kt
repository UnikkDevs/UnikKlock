package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.AlarmHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmHistoryDao {
  @Query("SELECT * FROM alarm_history ORDER BY completedTimeMillis DESC")
  fun getAllHistoryFlow(): Flow<List<AlarmHistoryEntity>>

  @Query("SELECT * FROM alarm_history ORDER BY completedTimeMillis DESC LIMIT 100")
  suspend fun getAllHistoryList(): List<AlarmHistoryEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertHistory(history: AlarmHistoryEntity): Long

  @Query("DELETE FROM alarm_history")
  suspend fun clearHistory()
}
