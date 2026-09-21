package com.example.alarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.alarm.AlarmActivity
import com.example.alarm.AlarmScheduler
import com.example.alarm.service.AlarmAudioService

class AlarmReceiver : BroadcastReceiver() {
  companion object {
    const val ACTION_TRIGGER_ALARM = "com.aistudio.wakequest.ACTION_ALARM_TRIGGER"
    private const val TAG = "AlarmReceiver"
  }

  override fun onReceive(context: Context, intent: Intent) {
    Log.d(TAG, "Alarm triggered! action=${intent.action}")

    val alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1L)
    val isSnooze = intent.getBooleanExtra(AlarmScheduler.EXTRA_IS_SNOOZE, false)
    val snoozeCount = intent.getIntExtra(AlarmScheduler.EXTRA_SNOOZE_COUNT, 0)

    // Acquire partial wake lock to guarantee CPU stays active until Activity and Service launch
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    val wakeLock = powerManager?.newWakeLock(
      PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
      "wakequest:AlarmWakeLock"
    )
    wakeLock?.acquire(3 * 60 * 1000L) // 3 minutes timeout safety

    // 1. Start ongoing Foreground Alarm Service for continuous sound and vibration
    val serviceIntent = Intent(context, AlarmAudioService::class.java).apply {
      action = AlarmAudioService.ACTION_START_ALARM
      putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
      putExtra(AlarmScheduler.EXTRA_IS_SNOOZE, isSnooze)
      putExtra(AlarmScheduler.EXTRA_SNOOZE_COUNT, snoozeCount)
    }

    try {
      ContextCompat.startForegroundService(context, serviceIntent)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to start foreground alarm service: ${e.message}")
    }

    // 2. Launch Full-Screen Alarm Activity
    val activityIntent = Intent(context, AlarmActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or
          Intent.FLAG_ACTIVITY_CLEAR_TOP or
          Intent.FLAG_ACTIVITY_SINGLE_TOP or
          Intent.FLAG_ACTIVITY_NO_USER_ACTION
      putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
      putExtra(AlarmScheduler.EXTRA_IS_SNOOZE, isSnooze)
      putExtra(AlarmScheduler.EXTRA_SNOOZE_COUNT, snoozeCount)
    }

    try {
      context.startActivity(activityIntent)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to launch AlarmActivity directly: ${e.message}")
    }
  }
}
