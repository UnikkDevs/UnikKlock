package com.example.data.repository

import android.content.Context
import com.example.alarm.AlarmScheduler
import com.example.data.database.AppDatabase
import com.example.data.model.AlarmEntity
import com.example.data.model.AlarmHistoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar

data class AlarmStats(
  val totalCompleted: Int = 0,
  val avgDurationSeconds: Int = 0,
  val currentStreak: Int = 0,
  val bestStreak: Int = 0,
  val totalSnoozes: Int = 0,
  val weeklyCompleted: Map<Int, Int> = emptyMap() // DayOfWeek (Calendar.SUNDAY..SATURDAY) -> count
)

class AlarmRepository(
  private val context: Context,
  private val database: AppDatabase = AppDatabase.getInstance(context)
) {
  private val alarmDao = database.alarmDao()
  private val historyDao = database.alarmHistoryDao()

  val allAlarmsFlow: Flow<List<AlarmEntity>> = alarmDao.getAllAlarmsFlow()
  val historyFlow: Flow<List<AlarmHistoryEntity>> = historyDao.getAllHistoryFlow()

  suspend fun getAlarmById(id: Long): AlarmEntity? = alarmDao.getAlarmById(id)

  suspend fun insertAlarm(alarm: AlarmEntity): Long {
    val id = alarmDao.insertAlarm(alarm)
    val saved = alarm.copy(id = id)
    if (saved.isEnabled) {
      AlarmScheduler.scheduleAlarm(context, saved)
    }
    return id
  }

  suspend fun updateAlarm(alarm: AlarmEntity) {
    alarmDao.updateAlarm(alarm)
    if (alarm.isEnabled) {
      AlarmScheduler.scheduleAlarm(context, alarm)
    } else {
      AlarmScheduler.cancelAlarm(context, alarm.id)
    }
  }

  suspend fun toggleAlarm(alarm: AlarmEntity) {
    val updated = alarm.copy(isEnabled = !alarm.isEnabled)
    alarmDao.updateAlarm(updated)
    if (updated.isEnabled) {
      AlarmScheduler.scheduleAlarm(context, updated)
    } else {
      AlarmScheduler.cancelAlarm(context, updated.id)
    }
  }

  suspend fun deleteAlarm(alarm: AlarmEntity) {
    AlarmScheduler.cancelAlarm(context, alarm.id)
    alarmDao.deleteAlarm(alarm)
  }

  suspend fun rescheduleAllEnabledAlarms() {
    val enabled = alarmDao.getAllEnabledAlarms()
    for (alarm in enabled) {
      AlarmScheduler.scheduleAlarm(context, alarm)
    }
  }

  suspend fun recordCompletion(
    alarmId: Long,
    alarmName: String,
    durationSeconds: Int,
    challengeType: String,
    difficulty: String,
    snoozeCount: Int
  ) {
    val history = AlarmHistoryEntity(
      alarmId = alarmId,
      alarmName = alarmName,
      triggerTimeMillis = System.currentTimeMillis() - (durationSeconds * 1000L),
      completedTimeMillis = System.currentTimeMillis(),
      durationSeconds = durationSeconds,
      challengeType = challengeType,
      difficulty = difficulty,
      snoozeCount = snoozeCount,
      wasDismissedWithChallenge = true
    )
    historyDao.insertHistory(history)

    // If the alarm was a one-time alarm, disable it now in DB
    val alarm = alarmDao.getAlarmById(alarmId)
    if (alarm != null) {
      if (!alarm.isRepeating) {
        val disabled = alarm.copy(isEnabled = false, currentSnoozeCount = 0)
        alarmDao.updateAlarm(disabled)
        AlarmScheduler.cancelAlarm(context, alarmId)
      } else {
        // Reschedule next occurrence for repeating alarm
        val reset = alarm.copy(currentSnoozeCount = 0)
        alarmDao.updateAlarm(reset)
        AlarmScheduler.scheduleAlarm(context, reset)
      }
    }
  }

  suspend fun clearHistory() {
    historyDao.clearHistory()
  }

  suspend fun calculateStats(): AlarmStats {
    val historyList = historyDao.getAllHistoryList()
    if (historyList.isEmpty()) {
      return AlarmStats()
    }

    val totalCompleted = historyList.size
    val totalSeconds = historyList.sumOf { it.durationSeconds }
    val avgSeconds = if (totalCompleted > 0) totalSeconds / totalCompleted else 0
    val totalSnoozes = historyList.sumOf { it.snoozeCount }

    // Group completions by day of week
    val weekly = mutableMapOf<Int, Int>()
    for (item in historyList) {
      val cal = Calendar.getInstance().apply { timeInMillis = item.completedTimeMillis }
      val dow = cal.get(Calendar.DAY_OF_WEEK)
      weekly[dow] = (weekly[dow] ?: 0) + 1
    }

    // Calculate streaks: Days with at least one alarm completed
    val daySet = mutableSetOf<String>()
    for (item in historyList) {
      val cal = Calendar.getInstance().apply { timeInMillis = item.completedTimeMillis }
      val dayKey = "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.DAY_OF_YEAR)}"
      daySet.add(dayKey)
    }

    // Calculate current streak backwards from today or yesterday
    var currentStreak = 0
    var bestStreak = 0
    val checkCal = Calendar.getInstance()
    var currentKey = "${checkCal.get(Calendar.YEAR)}-${checkCal.get(Calendar.DAY_OF_YEAR)}"

    if (!daySet.contains(currentKey)) {
      // Check if yesterday completed
      checkCal.add(Calendar.DAY_OF_YEAR, -1)
      currentKey = "${checkCal.get(Calendar.YEAR)}-${checkCal.get(Calendar.DAY_OF_YEAR)}"
    }

    while (daySet.contains(currentKey)) {
      currentStreak++
      checkCal.add(Calendar.DAY_OF_YEAR, -1)
      currentKey = "${checkCal.get(Calendar.YEAR)}-${checkCal.get(Calendar.DAY_OF_YEAR)}"
    }

    // Best streak estimated (at least currentStreak)
    bestStreak = maxOf(currentStreak, if (daySet.isNotEmpty()) (daySet.size.coerceAtMost(14)) else 0)

    return AlarmStats(
      totalCompleted = totalCompleted,
      avgDurationSeconds = avgSeconds,
      currentStreak = currentStreak,
      bestStreak = bestStreak,
      totalSnoozes = totalSnoozes,
      weeklyCompleted = weekly
    )
  }
}
