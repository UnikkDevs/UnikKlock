package com.example.alarm

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alarm.service.AlarmAudioService
import com.example.data.database.AppDatabase
import com.example.data.model.AlarmEntity
import com.example.data.model.ChallengeType
import com.example.data.model.Difficulty
import com.example.data.repository.AlarmRepository
import com.example.games.ChallengeGenerator
import com.example.games.ui.MathChallengeComposable
import com.example.games.ui.MemoryCardsComposable
import com.example.games.ui.MemorySequenceComposable
import com.example.games.ui.PatternChallengeComposable
import com.example.games.ui.QuickTapComposable
import com.example.games.ui.ReactionChallengeComposable
import com.example.games.ui.WordScrambleComposable
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EnergyAmber
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class AlarmActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Unlock and turn screen on
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
      setShowWhenLocked(true)
      setTurnScreenOn(true)
      val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
      keyguardManager?.requestDismissKeyguard(this, null)
    } else {
      @Suppress("DEPRECATION")
      window.addFlags(
        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
      )
    }
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

    val alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1L)
    val isSnooze = intent.getBooleanExtra(AlarmScheduler.EXTRA_IS_SNOOZE, false)
    val currentSnoozeCount = intent.getIntExtra(AlarmScheduler.EXTRA_SNOOZE_COUNT, 0)

    setContent {
      MyApplicationTheme(darkTheme = true) {
        AlarmRingingScreen(
          alarmId = alarmId,
          isSnooze = isSnooze,
          snoozeCount = currentSnoozeCount,
          onAlarmFinished = {
            stopAlarmService()
            finish()
          }
        )
      }
    }
  }

  private fun stopAlarmService() {
    val stopIntent = Intent(this, AlarmAudioService::class.java).apply {
      action = AlarmAudioService.ACTION_STOP_ALARM
    }
    startService(stopIntent)
  }
}

@Composable
fun AlarmRingingScreen(
  alarmId: Long,
  isSnooze: Boolean,
  snoozeCount: Int,
  onAlarmFinished: () -> Unit
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val repository = remember { AlarmRepository(context.applicationContext) }

  var alarm by remember { mutableStateOf<AlarmEntity?>(null) }
  var currentTimeString by remember { mutableStateOf("") }
  var startTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

  var currentChallengeStep by remember { mutableIntStateOf(1) }
  var totalChallengeSteps by remember { mutableIntStateOf(1) }
  var currentChallengeType by remember { mutableStateOf(ChallengeType.MATH) }
  var difficulty by remember { mutableStateOf(Difficulty.MEDIUM) }
  var isCompletedState by remember { mutableStateOf(false) }
  var completionSeconds by remember { mutableIntStateOf(0) }

  var showSnoozeChallengeDialog by remember { mutableStateOf(false) }
  var snoozeProblem by remember { mutableStateOf("3 + 7") }
  var snoozeExpectedAnswer by remember { mutableIntStateOf(10) }
  var snoozeInput by remember { mutableStateOf("") }
  var snoozeError by remember { mutableStateOf(false) }

  // Prevent back button dismissal
  BackHandler {
    Toast.makeText(
      context,
      "No bypass! Complete the challenge to silence alarm.",
      Toast.LENGTH_SHORT
    ).show()
  }

  // Load alarm details from DB
  LaunchedEffect(alarmId) {
    if (alarmId > 0) {
      val found = repository.getAlarmById(alarmId)
      alarm = found
      if (found != null) {
        difficulty = Difficulty.fromId(found.difficulty)
        totalChallengeSteps = found.challengeCount.coerceAtLeast(1)
        val initialType = ChallengeType.fromId(found.challengeType)
        currentChallengeType = ChallengeGenerator.resolveChallengeType(initialType)
      }
    } else {
      // Preview or Test Alarm
      currentChallengeType = ChallengeType.MATH
      totalChallengeSteps = 1
    }
    startTimeMillis = System.currentTimeMillis()
  }

  // Live Clock updater
  LaunchedEffect(Unit) {
    val formatter = SimpleDateFormat("h:mm:ss a", Locale.getDefault())
    while (true) {
      currentTimeString = formatter.format(Date())
      delay(1000)
    }
  }

  // Pulse animation for ringing header
  val infiniteTransition = rememberInfiniteTransition(label = "alarm_pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.95f,
    targetValue = 1.05f,
    animationSpec = infiniteRepeatable(
      animation = tween(600, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "scale"
  )

  fun handleChallengeCompleted() {
    if (currentChallengeStep < totalChallengeSteps) {
      currentChallengeStep++
      // For random or next challenge, resolve another
      val configured = alarm?.let { ChallengeType.fromId(it.challengeType) } ?: ChallengeType.RANDOM
      currentChallengeType = ChallengeGenerator.resolveChallengeType(configured)
    } else {
      // Conquered!
      val duration = ((System.currentTimeMillis() - startTimeMillis) / 1000L).toInt().coerceAtLeast(1)
      completionSeconds = duration
      isCompletedState = true

      coroutineScope.launch {
        repository.recordCompletion(
          alarmId = alarm?.id ?: -1L,
          alarmName = alarm?.name ?: "WakeQuest Alarm",
          durationSeconds = duration,
          challengeType = currentChallengeType.title,
          difficulty = difficulty.label,
          snoozeCount = snoozeCount
        )
        delay(2200)
        onAlarmFinished()
      }
    }
  }

  fun handleSnoozeSuccess() {
    showSnoozeChallengeDialog = false
    alarm?.let { al ->
      val updatedSnoozeCount = snoozeCount + 1
      AlarmScheduler.scheduleAlarm(context, al, isSnooze = true, snoozeCount = updatedSnoozeCount)
      Toast.makeText(context, "Alarm snoozed for ${al.snoozeMinutes} minutes", Toast.LENGTH_SHORT).show()
    }
    onAlarmFinished()
  }

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = DeepNavy
  ) {
    if (isCompletedState) {
      // Victory / Celebration View
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(DeepNavy)
          .padding(24.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Success",
            tint = SuccessGreen,
            modifier = Modifier.size(96.dp)
          )
          Spacer(modifier = Modifier.height(20.dp))
          Text(
            text = "ALARM CONQUERED!",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            color = TextPrimary
          )
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = "Solved in $completionSeconds seconds.",
            style = MaterialTheme.typography.headlineSmall,
            color = NeonCyan,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Your brain is awake. Have an extraordinary day!",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center
          )
        }
      }
    } else {
      // Active Ringing & Challenge Interface
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(top = 40.dp, bottom = 24.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Ringing Header
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
          modifier = Modifier.scale(pulseScale)
        ) {
          Icon(
            imageVector = Icons.Default.NotificationsActive,
            contentDescription = "Alarm Active",
            tint = DangerRed,
            modifier = Modifier.size(32.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "WAKE UP!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            color = DangerRed,
            letterSpacing = 2.sp
          )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = currentTimeString,
          style = MaterialTheme.typography.headlineMedium,
          color = TextPrimary,
          fontWeight = FontWeight.Black
        )

        alarm?.name?.let {
          Text(
            text = it,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Progress badge (Challenge 1 of 2)
        Card(
          colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
          shape = RoundedCornerShape(12.dp),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ElectricViolet)),
          modifier = Modifier.padding(horizontal = 8.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "TASK $currentChallengeStep OF $totalChallengeSteps: ",
              style = MaterialTheme.typography.labelLarge,
              color = NeonCyan,
              fontWeight = FontWeight.Black
            )
            Text(
              text = currentChallengeType.title,
              style = MaterialTheme.typography.labelLarge,
              color = TextPrimary,
              fontWeight = FontWeight.Bold
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dynamic Challenge Area
        AnimatedContent(
          targetState = currentChallengeType,
          label = "challenge_view"
        ) { challenge ->
          when (challenge) {
            ChallengeType.MATH -> {
              MathChallengeComposable(
                difficulty = difficulty,
                onCompleted = { handleChallengeCompleted() }
              )
            }
            ChallengeType.MEMORY_SEQUENCE -> {
              MemorySequenceComposable(
                difficulty = difficulty,
                onCompleted = { handleChallengeCompleted() }
              )
            }
            ChallengeType.PATTERN -> {
              PatternChallengeComposable(
                difficulty = difficulty,
                onCompleted = { handleChallengeCompleted() }
              )
            }
            ChallengeType.REACTION -> {
              ReactionChallengeComposable(
                difficulty = difficulty,
                onCompleted = { handleChallengeCompleted() }
              )
            }
            ChallengeType.QUICK_TAP -> {
              QuickTapComposable(
                difficulty = difficulty,
                onCompleted = { handleChallengeCompleted() }
              )
            }
            ChallengeType.MEMORY_CARDS -> {
              MemoryCardsComposable(
                difficulty = difficulty,
                onCompleted = { handleChallengeCompleted() }
              )
            }
            ChallengeType.WORD_SCRAMBLE -> {
              WordScrambleComposable(
                difficulty = difficulty,
                onCompleted = { handleChallengeCompleted() }
              )
            }
            else -> {
              MathChallengeComposable(
                difficulty = difficulty,
                onCompleted = { handleChallengeCompleted() }
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Snooze Option (Requires challenge first per requirement 8)
        val maxSnoozes = alarm?.maxSnoozeCount ?: 3
        val canSnooze = snoozeCount < maxSnoozes
        val snoozeMins = alarm?.snoozeMinutes ?: 5

        if (canSnooze) {
          OutlinedButton(
            onClick = {
              if (alarm?.snoozeChallengeEnabled == true) {
                // Generate quick 1-equation challenge
                val n1 = Random.nextInt(3, 14)
                val n2 = Random.nextInt(3, 14)
                snoozeProblem = "$n1 + $n2"
                snoozeExpectedAnswer = n1 + n2
                snoozeInput = ""
                snoozeError = false
                showSnoozeChallengeDialog = true
              } else {
                handleSnoozeSuccess()
              }
            },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(EnergyAmber)),
            modifier = Modifier.testTag("snooze_alarm_button")
          ) {
            Icon(Icons.Default.Snooze, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "SNOOZE $snoozeMins MIN (${maxSnoozes - snoozeCount} left)",
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
        } else {
          Text(
            text = "Maximum snoozes reached for this alarm",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
          )
        }
      }
    }
  }

  // Snooze Challenge Dialog
  if (showSnoozeChallengeDialog) {
    AlertDialog(
      onDismissRequest = { showSnoozeChallengeDialog = false },
      containerColor = DarkSurfaceElevated,
      titleContentColor = TextPrimary,
      title = {
        Text("Solve equation to snooze", fontWeight = FontWeight.Bold)
      },
      text = {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "Snooze requires a quick verification:",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
          )
          Spacer(modifier = Modifier.height(14.dp))
          Text(
            text = "$snoozeProblem = ?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = EnergyAmber
          )
          Spacer(modifier = Modifier.height(14.dp))
          OutlinedTextField(
            value = snoozeInput,
            onValueChange = {
              snoozeInput = it
              snoozeError = false
            },
            placeholder = { Text("Answer") },
            isError = snoozeError,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = EnergyAmber,
              unfocusedBorderColor = DarkSurfaceBorder,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            )
          )
          if (snoozeError) {
            Spacer(modifier = Modifier.height(4.dp))
            Text("Incorrect answer", color = DangerRed, style = MaterialTheme.typography.bodySmall)
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (snoozeInput.trim().toIntOrNull() == snoozeExpectedAnswer) {
              handleSnoozeSuccess()
            } else {
              snoozeError = true
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = EnergyAmber, contentColor = DeepNavy)
        ) {
          Text("Snooze Now", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showSnoozeChallengeDialog = false }) {
          Text("Cancel", color = TextSecondary)
        }
      }
    )
  }
}
