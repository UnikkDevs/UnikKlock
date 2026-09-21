package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar

enum class ChallengeType(val id: String, val title: String, val description: String) {
  RANDOM("Random", "Random Challenge", "Picks any challenge at random"),
  MATH("Math", "Math Challenge", "Solve mental arithmetic equations"),
  MEMORY_SEQUENCE("MemorySequence", "Memory Sequence", "Memorize and recall digit sequences"),
  PATTERN("PatternRecognition", "Pattern Recognition", "Find the next item in visual patterns"),
  REACTION("Reaction", "Reaction Challenge", "Test your reflex speed when signaled"),
  QUICK_TAP("QuickTap", "Quick Tap", "Tap moving energized target spheres"),
  MEMORY_CARDS("MemoryCards", "Memory Cards", "Flip and match matching pairs"),
  WORD_SCRAMBLE("WordScramble", "Word Scramble", "Unscramble awakening brain words");

  companion object {
    fun fromId(id: String): ChallengeType =
      entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: MATH
  }
}

enum class Difficulty(val id: String, val label: String) {
  EASY("Easy", "Easy"),
  MEDIUM("Medium", "Medium"),
  HARD("Hard", "Hard");

  companion object {
    fun fromId(id: String): Difficulty =
      entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: MEDIUM
  }
}

enum class SoundType(val id: String, val label: String) {
  DEFAULT("Default", "System Alarm"),
  ENERGETIC("Energetic", "Energetic Pulse"),
  DIGITAL("Digital", "Digital Cyber"),
  GENTLE("Gentle", "Gentle Chime"),
  LOUD("Loud", "High Alert");

  companion object {
    fun fromId(id: String): SoundType =
      entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: ENERGETIC
  }
}

@Entity(tableName = "alarms")
data class AlarmEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val hour: Int,
  val minute: Int,
  val name: String = "Morning Wake Up",
  val isEnabled: Boolean = true,
  // Comma-separated day indices (Calendar.SUNDAY=1 .. Calendar.SATURDAY=7)
  // Empty string indicates a non-repeating alarm (fires once)
  val daysOfWeek: String = "2,3,4,5,6", // Mon-Fri by default
  val soundType: String = SoundType.ENERGETIC.id,
  val volume: Int = 85,
  val isVibrate: Boolean = true,
  val isGradualVolume: Boolean = false,
  val snoozeMinutes: Int = 5,
  val maxSnoozeCount: Int = 3,
  val currentSnoozeCount: Int = 0,
  val snoozeChallengeEnabled: Boolean = true,
  val difficulty: String = Difficulty.MEDIUM.id,
  val challengeType: String = ChallengeType.MATH.id,
  val challengeCount: Int = 1
) {
  val isRepeating: Boolean
    get() = daysOfWeek.isNotBlank()

  fun getDaysList(): List<Int> {
    if (daysOfWeek.isBlank()) return emptyList()
    return daysOfWeek.split(",")
      .mapNotNull { it.trim().toIntOrNull() }
  }

  fun formattedTime(): String {
    val h = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
    val m = String.format("%02d", minute)
    return "$h:$m"
  }

  fun amPm(): String {
    return if (hour >= 12) "PM" else "AM"
  }

  fun repeatSummary(): String {
    val days = getDaysList()
    if (days.isEmpty()) return "Once"
    if (days.size == 7) return "Every day"
    if (days.size == 5 &&
      days.containsAll(listOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY))) {
      return "Mon – Fri"
    }
    if (days.size == 2 &&
      days.containsAll(listOf(Calendar.SATURDAY, Calendar.SUNDAY))) {
      return "Weekends"
    }
    val names = mapOf(
      Calendar.SUNDAY to "Sun",
      Calendar.MONDAY to "Mon",
      Calendar.TUESDAY to "Tue",
      Calendar.WEDNESDAY to "Wed",
      Calendar.THURSDAY to "Thu",
      Calendar.FRIDAY to "Fri",
      Calendar.SATURDAY to "Sat"
    )
    return days.sorted().mapNotNull { names[it] }.joinToString(" ")
  }
}

@Entity(tableName = "alarm_history")
data class AlarmHistoryEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val alarmId: Long,
  val alarmName: String,
  val triggerTimeMillis: Long,
  val completedTimeMillis: Long,
  val durationSeconds: Int,
  val challengeType: String,
  val difficulty: String,
  val snoozeCount: Int = 0,
  val wasDismissedWithChallenge: Boolean = true
)
