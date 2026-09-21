package com.example.alarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.repository.AlarmRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
  companion object {
    private const val TAG = "BootReceiver"
  }

  override fun onReceive(context: Context, intent: Intent) {
    val action = intent.action
    Log.d(TAG, "BootReceiver received action: $action")

    if (action == Intent.ACTION_BOOT_COMPLETED ||
      action == Intent.ACTION_MY_PACKAGE_REPLACED ||
      action == Intent.ACTION_TIME_CHANGED ||
      action == Intent.ACTION_TIMEZONE_CHANGED
    ) {
      val pendingResult = goAsync()
      CoroutineScope(Dispatchers.IO).launch {
        try {
          val repository = AlarmRepository(context.applicationContext)
          repository.rescheduleAllEnabledAlarms()
          Log.d(TAG, "All active alarms successfully rescheduled after $action")
        } catch (e: Exception) {
          Log.e(TAG, "Failed to reschedule alarms on boot/time change: ${e.message}")
        } finally {
          pendingResult.finish()
        }
      }
    }
  }
}
