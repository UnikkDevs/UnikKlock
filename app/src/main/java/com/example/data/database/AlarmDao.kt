package com.example.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AlarmEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
  @Query("SELECT * FROM alarms ORDER BY hour ASC, minute ASC")
  fun getAllAlarmsFlow(): Flow<List<AlarmEntity>>

  @Query("SELECT * FROM alarms WHERE isEnabled = 1")
  suspend fun getAllEnabledAlarms(): List<AlarmEntity>

  @Query("SELECT COUNT(*) FROM alarms")
  suspend fun getAlarmCount(): Int

  @Query("SELECT * FROM alarms WHERE id = :id LIMIT 1")
  suspend fun getAlarmById(id: Long): AlarmEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAlarm(alarm: AlarmEntity): Long

  @Update
  suspend fun updateAlarm(alarm: AlarmEntity)

  @Delete
  suspend fun deleteAlarm(alarm: AlarmEntity)

  @Query("DELETE FROM alarms WHERE id = :id")
  suspend fun deleteAlarmById(id: Long)
}
