package com.example.alarm.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.alarm.AlarmActivity
import com.example.alarm.AlarmScheduler
import com.example.alarm.audio.AlarmSoundPlayer
import com.example.data.database.AppDatabase
import com.example.data.model.AlarmEntity
import com.example.data.model.SoundType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class AlarmAudioService : Service() {
  companion object {
    const val CHANNEL_ID = "unikklock_alarm_channel"
    const val NOTIFICATION_ID = 1001
    const val ACTION_START_ALARM = "com.aistudio.unikklock.ACTION_START_ALARM"
    const val ACTION_STOP_ALARM = "com.aistudio.unikklock.ACTION_STOP_ALARM"
    const val LEGACY_ACTION_STOP_ALARM = "com.aistudio.wakequest.ACTION_STOP_ALARM"
    private const val TAG = "AlarmAudioService"

    var isRinging: Boolean = false
      private set
  }

  private var soundPlayer: AlarmSoundPlayer? = null
  private val scope = CoroutineScope(Dispatchers.IO)

  override fun onCreate() {
    super.onCreate()
    soundPlayer = AlarmSoundPlayer(this)
    createNotificationChannel()
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    val action = intent?.action ?: ACTION_START_ALARM

    if (action == ACTION_STOP_ALARM || action == LEGACY_ACTION_STOP_ALARM) {
      stopAlarm()
      stopSelf()
      return START_NOT_STICKY
    }

    val alarmId = intent?.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1L) ?: -1L
    val isSnooze = intent?.getBooleanExtra(AlarmScheduler.EXTRA_IS_SNOOZE, false) ?: false
    val snoozeCount = intent?.getIntExtra(AlarmScheduler.EXTRA_SNOOZE_COUNT, 0) ?: 0

    scope.launch {
      val db = AppDatabase.getInstance(applicationContext)
      val alarm = if (alarmId > 0) db.alarmDao().getAlarmById(alarmId) else null
      val soundType = alarm?.soundType ?: SoundType.ENERGETIC.id
      val volume = alarm?.volume ?: 90
      val vibrate = alarm?.isVibrate ?: true
      val gradual = alarm?.isGradualVolume ?: false

      val notification = buildAlarmNotification(alarm, isSnooze, snoozeCount)
      startForeground(NOTIFICATION_ID, notification)

      soundPlayer?.start(
        soundType = soundType,
        volumePercent = volume,
        isVibrate = vibrate,
        isGradualVolume = gradual
      )
      isRinging = true
      Log.d(TAG, "Alarm sound & vibration started for alarmId=$alarmId")
    }

    return START_STICKY
  }

  private fun stopAlarm() {
    isRinging = false
    soundPlayer?.stop()
    stopForeground(STOP_FOREGROUND_REMOVE)
    Log.d(TAG, "Alarm stopped")
  }

  override fun onDestroy() {
    stopAlarm()
    soundPlayer?.stop()
    soundPlayer = null
    scope.cancel()
    super.onDestroy()
  }

  override fun onBind(intent: Intent?): IBinder? = null

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val name = "UnikKlock Active Alarms"
      val desc = "Urgent full-screen alarms requiring challenge completion"
      val importance = NotificationManager.IMPORTANCE_HIGH
      val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
        description = desc
        enableVibration(true)
        lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        setBypassDnd(true)
      }
      val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
      manager?.createNotificationChannel(channel)
    }
  }

  private fun buildAlarmNotification(
    alarm: AlarmEntity?,
    isSnooze: Boolean,
    snoozeCount: Int
  ): Notification {
    val fullScreenIntent = Intent(this, AlarmActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or
          Intent.FLAG_ACTIVITY_CLEAR_TOP or
          Intent.FLAG_ACTIVITY_SINGLE_TOP
      putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarm?.id ?: -1L)
      putExtra(AlarmScheduler.EXTRA_IS_SNOOZE, isSnooze)
      putExtra(AlarmScheduler.EXTRA_SNOOZE_COUNT, snoozeCount)
    }

    val fullScreenPendingIntent = PendingIntent.getActivity(
      this,
      0,
      fullScreenIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val title = alarm?.name?.ifBlank { "WAKE UP!" } ?: "WAKE UP!"
    val text = if (isSnooze) "Snooze Alarm Active • Complete challenge to dismiss" else "Active Challenge • Wake your brain to stop alarm"

    return NotificationCompat.Builder(this, CHANNEL_ID)
      .setSmallIcon(R.drawable.ic_unikklock_logo)
      .setContentTitle(title)
      .setContentText(text)
      .setPriority(NotificationCompat.PRIORITY_MAX)
      .setCategory(NotificationCompat.CATEGORY_ALARM)
      .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
      .setOngoing(true)
      .setAutoCancel(false)
      .setFullScreenIntent(fullScreenPendingIntent, true)
      .setContentIntent(fullScreenPendingIntent)
      .build()
  }
}
