package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.alarm.AlarmScheduler
import com.example.data.model.ChallengeType
import com.example.data.model.Difficulty
import com.example.games.ChallengeGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read app name from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("WakeQuest", appName)
  }

  @Test
  fun `test math challenge generation`() {
    val easyMath = ChallengeGenerator.generateMathProblem(Difficulty.EASY)
    assertNotNull(easyMath.question)
    assertTrue(easyMath.question.contains("?"))

    val hardMath = ChallengeGenerator.generateMathProblem(Difficulty.HARD)
    assertNotNull(hardMath.question)
  }

  @Test
  fun `test sequence generation`() {
    val seqEasy = ChallengeGenerator.generateSequence(Difficulty.EASY)
    assertEquals(4, seqEasy.size)

    val seqHard = ChallengeGenerator.generateSequence(Difficulty.HARD)
    assertEquals(7, seqHard.size)
  }

  @Test
  fun `test pattern generator`() {
    val pattern = ChallengeGenerator.generatePattern(Difficulty.MEDIUM)
    assertTrue(pattern.sequence.size >= 4)
    assertTrue(pattern.options.contains(pattern.correctAnswer))
  }

  @Test
  fun `test alarm scheduler calculateNextTriggerTime`() {
    val nextTrigger = AlarmScheduler.calculateNextTriggerTime(8, 30, emptyList())
    assertTrue(nextTrigger > System.currentTimeMillis())
  }

  @Test
  fun `test minute granularity for alarms`() {
    val alarmOne = com.example.data.model.AlarmEntity(hour = 7, minute = 1)
    val alarmTwo = com.example.data.model.AlarmEntity(hour = 7, minute = 2)
    val alarmFiftyNine = com.example.data.model.AlarmEntity(hour = 7, minute = 59)

    assertEquals("7:01", alarmOne.formattedTime())
    assertEquals("7:02", alarmTwo.formattedTime())
    assertEquals("7:59", alarmFiftyNine.formattedTime())
  }
}
