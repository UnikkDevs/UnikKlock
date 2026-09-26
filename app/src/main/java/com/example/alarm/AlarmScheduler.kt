package com.example.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.MainActivity
import com.example.alarm.receiver.AlarmReceiver
import com.example.data.model.AlarmEntity
import java.util.Calendar

object AlarmScheduler {
  private const val TAG = "AlarmScheduler"
  const val EXTRA_ALARM_ID = "extra_alarm_id"
  const val EXTRA_IS_SNOOZE = "extra_is_snooze"
  const val EXTRA_SNOOZE_COUNT = "extra_snooze_count"

  fun canScheduleExactAlarms(context: Context): Boolean {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
      ?: return false
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      alarmManager.canScheduleExactAlarms()
    } else {
      true
    }
  }

  fun scheduleAlarm(context: Context, alarm: AlarmEntity, isSnooze: Boolean = false, snoozeCount: Int = 0) {
    if (!alarm.isEnabled && !isSnooze) {
      cancelAlarm(context, alarm.id)
      return
    }

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    val triggerTime = if (isSnooze) {
      System.currentTimeMillis() + (alarm.snoozeMinutes * 60 * 1000L)
    } else {
      calculateNextTriggerTime(alarm.hour, alarm.minute, alarm.getDaysList())
    }

    val intent = Intent(context, AlarmReceiver::class.java).apply {
      action = AlarmReceiver.ACTION_TRIGGER_ALARM
      putExtra(EXTRA_ALARM_ID, alarm.id)
      putExtra(EXTRA_IS_SNOOZE, isSnooze)
      putExtra(EXTRA_SNOOZE_COUNT, snoozeCount)
    }

    val requestCode = alarm.id.toInt() * 10 + (if (isSnooze) 1 else 0)
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      requestCode,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Intent to open MainActivity when user taps the alarm icon in status bar or lockscreen
    val showIntent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val showPendingIntent = PendingIntent.getActivity(
      context,
      requestCode + 100000,
      showIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        val clockInfo = AlarmManager.AlarmClockInfo(triggerTime, showPendingIntent)
        alarmManager.setAlarmClock(clockInfo, pendingIntent)
        Log.d(TAG, "Scheduled alarm clock for id=${alarm.id} at $triggerTime")
      } else {
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
      }
    } catch (e: SecurityException) {
      Log.e(TAG, "SecurityException scheduling exact alarm: ${e.message}. Falling back.")
      try {
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
      } catch (ex: Exception) {
        Log.e(TAG, "Fallback scheduling failed: ${ex.message}")
      }
    } catch (e: Exception) {
      Log.e(TAG, "Failed to schedule alarm: ${e.message}")
    }
  }

  fun cancelAlarm(context: Context, alarmId: Long) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    val intent = Intent(context, AlarmReceiver::class.java).apply {
      action = AlarmReceiver.ACTION_TRIGGER_ALARM
      putExtra(EXTRA_ALARM_ID, alarmId)
    }

    // Cancel main and snooze requestCodes
    val requestCodeMain = alarmId.toInt() * 10
    val requestCodeSnooze = alarmId.toInt() * 10 + 1

    try {
      val pendingIntentMain = PendingIntent.getBroadcast(
        context,
        requestCodeMain,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )
      alarmManager.cancel(pendingIntentMain)
      pendingIntentMain.cancel()
    } catch (e: Exception) {
      Log.e(TAG, "Error cancelling main alarm pending intent: ${e.message}")
    }

    try {
      val pendingIntentSnooze = PendingIntent.getBroadcast(
        context,
        requestCodeSnooze,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )
      alarmManager.cancel(pendingIntentSnooze)
      pendingIntentSnooze.cancel()
    } catch (e: Exception) {
      Log.e(TAG, "Error cancelling snooze pending intent: ${e.message}")
    }

    try {
      val showIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
      }
      val showPendingIntent = PendingIntent.getActivity(
        context,
        requestCodeMain + 100000,
        showIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )
      alarmManager.cancel(showPendingIntent)
      showPendingIntent.cancel()
    } catch (e: Exception) {
      // Ignore
    }
    Log.d(TAG, "Cancelled alarm for id=$alarmId")
  }

  fun calculateNextTriggerTime(hour: Int, minute: Int, repeatDays: List<Int>): Long {
    val now = Calendar.getInstance()

    if (repeatDays.isEmpty()) {
      // One-time alarm
      val target = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
      }
      if (target.timeInMillis <= now.timeInMillis) {
        target.add(Calendar.DAY_OF_YEAR, 1)
      }
      return target.timeInMillis
    }

    // Repeating alarm across daysOfWeek (Calendar.SUNDAY=1 .. Calendar.SATURDAY=7)
    var minDiffDays = 8
    var targetCalendar: Calendar? = null

    for (day in repeatDays) {
      val candidate = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_WEEK, day)
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
      }

      if (candidate.timeInMillis <= now.timeInMillis) {
        candidate.add(Calendar.WEEK_OF_YEAR, 1)
      }

      val diffMillis = candidate.timeInMillis - now.timeInMillis
      if (targetCalendar == null || diffMillis < (targetCalendar.timeInMillis - now.timeInMillis)) {
        targetCalendar = candidate
      }
    }

    return targetCalendar?.timeInMillis ?: (now.timeInMillis + 60000L)
  }

  fun formatRemainingTime(triggerTimeMillis: Long): String {
    val diff = triggerTimeMillis - System.currentTimeMillis()
    if (diff <= 0) return "Due now"
    val minutes = (diff / (1000 * 60)) % 60
    val hours = (diff / (1000 * 60 * 60)) % 24
    val days = diff / (1000 * 60 * 60 * 24)

    return when {
      days > 0 -> "Rings in $days d $hours hr $minutes min"
      hours > 0 -> "Rings in $hours hr $minutes min"
      minutes > 0 -> "Rings in $minutes min"
      else -> "Rings in less than a minute"
    }
  }

  fun scheduleTestAlarmInSeconds(context: Context, alarm: AlarmEntity, seconds: Int = 5) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    val triggerTime = System.currentTimeMillis() + (seconds * 1000L)

    val intent = Intent(context, AlarmReceiver::class.java).apply {
      action = AlarmReceiver.ACTION_TRIGGER_ALARM
      putExtra(EXTRA_ALARM_ID, alarm.id)
      putExtra(EXTRA_IS_SNOOZE, false)
      putExtra(EXTRA_SNOOZE_COUNT, 0)
    }

    val pendingIntent = PendingIntent.getBroadcast(
      context,
      999999,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val showIntent = Intent(context, AlarmActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
      putExtra(EXTRA_ALARM_ID, alarm.id)
    }
    val showPendingIntent = PendingIntent.getActivity(
      context,
      999999,
      showIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        val clockInfo = AlarmManager.AlarmClockInfo(triggerTime, showPendingIntent)
        alarmManager.setAlarmClock(clockInfo, pendingIntent)
      } else {
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
      }
    } catch (e: Exception) {
      Log.e(TAG, "Test alarm schedule failed: ${e.message}")
    }
  }
}
