package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import com.example.alarm.AlarmScheduler
import com.example.alarm.service.AlarmAudioService
import com.example.data.database.AppDatabase
import com.example.data.model.AlarmEntity
import com.example.data.model.ChallengeType
import com.example.data.model.Difficulty
import com.example.data.model.SoundType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WakeQuestApplication : Application() {

  override fun onCreate() {
    super.onCreate()
    createNotificationChannels()
    seedDefaultAlarmsIfEmpty()
  }

  private fun createNotificationChannels() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        ?: return

      // Urgent Alarm Channel
      val alarmChannel = NotificationChannel(
        AlarmAudioService.CHANNEL_ID,
        "UnikKlock Alarms",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "High-priority wake-up alarms with interactive mental challenges"
        enableVibration(true)
        lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
        setBypassDnd(true)
      }
      notificationManager.createNotificationChannel(alarmChannel)
    }
  }

  private fun seedDefaultAlarmsIfEmpty() {
    CoroutineScope(Dispatchers.IO).launch {
      val db = AppDatabase.getInstance(applicationContext)
      val existing = db.alarmDao().getAllEnabledAlarms()
      if (existing.isEmpty()) {
        // Seed 2 initial polished alarms
        val default1 = AlarmEntity(
          hour = 7,
          minute = 0,
          name = "Morning Wake Up",
          isEnabled = true,
          daysOfWeek = "2,3,4,5,6", // Mon-Fri
          soundType = SoundType.ENERGETIC.id,
          volume = 85,
          isVibrate = true,
          isGradualVolume = false,
          snoozeMinutes = 5,
          maxSnoozeCount = 3,
          difficulty = Difficulty.MEDIUM.id,
          challengeType = ChallengeType.MATH.id,
          challengeCount = 1
        )
        val default2 = AlarmEntity(
          hour = 8,
          minute = 30,
          name = "Weekend Quest",
          isEnabled = false,
          daysOfWeek = "1,7", // Sat-Sun
          soundType = SoundType.DIGITAL.id,
          volume = 90,
          isVibrate = true,
          isGradualVolume = true,
          snoozeMinutes = 10,
          maxSnoozeCount = 2,
          difficulty = Difficulty.EASY.id,
          challengeType = ChallengeType.MEMORY_SEQUENCE.id,
          challengeCount = 1
        )

        val id1 = db.alarmDao().insertAlarm(default1)
        db.alarmDao().insertAlarm(default2)
        AlarmScheduler.scheduleAlarm(applicationContext, default1.copy(id = id1))
      }
    }
  }
}
