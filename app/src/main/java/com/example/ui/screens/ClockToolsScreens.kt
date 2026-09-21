package com.example.ui.screens

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppTheme
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EnergyAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SuccessGreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.sin

enum class ClockSubTab(val title: String, val icon: ImageVector) {
  STOPWATCH("Stopwatch", Icons.Default.Timer),
  TIMER("Timer", Icons.Default.HourglassBottom),
  WORLD_CLOCK("World Clock", Icons.Default.Public),
  SLEEP_CYCLES("Sleep Cycles", Icons.Default.Bedtime)
}

@Composable
fun ClockToolsScreen(
  onNavigateToAlarmEdit: (hour: Int, minute: Int) -> Unit,
  onToggleTheme: (() -> Unit)? = null,
  isDarkTheme: Boolean = true
) {
  var selectedTab by remember { mutableStateOf(ClockSubTab.STOPWATCH) }
  val colors = AppTheme.colors

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(colors.background)
  ) {
    // Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "Clock & Tools",
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Black,
          color = colors.textPrimary
        )
        Text(
          text = "Real-time utilities for your routine",
          style = MaterialTheme.typography.bodySmall,
          color = colors.textSecondary
        )
      }

      if (onToggleTheme != null) {
        IconButton(
          onClick = onToggleTheme,
          modifier = Modifier
            .clip(CircleShape)
            .background(colors.surfaceElevated)
        ) {
          Icon(
            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
            contentDescription = "Toggle Theme",
            tint = colors.accentCyan
          )
        }
      }
    }

    // Sub-Tabs
    ScrollableTabRow(
      selectedTabIndex = selectedTab.ordinal,
      containerColor = colors.surface,
      contentColor = colors.accentCyan,
      edgePadding = 16.dp,
      indicator = { tabPositions ->
        TabRowDefaults.SecondaryIndicator(
          modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
          color = colors.accentCyan,
          height = 3.dp
        )
      }
    ) {
      ClockSubTab.entries.forEach { tab ->
        val isSelected = selectedTab == tab
        Tab(
          selected = isSelected,
          onClick = { selectedTab = tab },
          text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = tab.icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isSelected) colors.accentCyan else colors.textSecondary
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = tab.title,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) colors.accentCyan else colors.textSecondary
              )
            }
          }
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Content Pane
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp)
    ) {
      when (selectedTab) {
        ClockSubTab.STOPWATCH -> StopwatchPane()
        ClockSubTab.TIMER -> CountdownTimerPane()
        ClockSubTab.WORLD_CLOCK -> WorldClockPane()
        ClockSubTab.SLEEP_CYCLES -> SleepCyclesPane(onNavigateToAlarmEdit = onNavigateToAlarmEdit)
      }
    }
  }
}

// -----------------------------------------------------------------------------
// 1. STOPWATCH PANE
// -----------------------------------------------------------------------------

data class LapRecord(
  val lapNumber: Int,
  val lapTimeMillis: Long,
  val totalTimeMillis: Long
)

@Composable
fun StopwatchPane() {
  val colors = AppTheme.colors
  var isRunning by remember { mutableStateOf(false) }
  var elapsedTimeMillis by remember { mutableLongStateOf(0L) }
  var lastStartTime by remember { mutableLongStateOf(0L) }
  val laps = remember { mutableStateListOf<LapRecord>() }
  var lastLapTimestamp by remember { mutableLongStateOf(0L) }

  // High precision timer loop
  LaunchedEffect(isRunning) {
    if (isRunning) {
      val baseTime = System.currentTimeMillis() - elapsedTimeMillis
      while (isActive && isRunning) {
        elapsedTimeMillis = System.currentTimeMillis() - baseTime
        delay(10) // 100 updates per second (hundredths)
      }
    }
  }

  val minutes = (elapsedTimeMillis / 60000) % 60
  val seconds = (elapsedTimeMillis / 1000) % 60
  val hundredths = (elapsedTimeMillis % 1000) / 10

  val fastestLapMillis by remember {
    derivedStateOf {
      if (laps.size >= 2) laps.minOfOrNull { it.lapTimeMillis } else null
    }
  }
  val slowestLapMillis by remember {
    derivedStateOf {
      if (laps.size >= 2) laps.maxOfOrNull { it.lapTimeMillis } else null
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(vertical = 12.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Stopwatch Visual Dial
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier
        .size(240.dp)
        .padding(16.dp)
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val sweepAngle = ((elapsedTimeMillis % 60000) / 60000f) * 360f
        // Background track
        drawCircle(
          color = colors.surfaceElevated,
          style = Stroke(width = 8.dp.toPx())
        )
        // Progress sweep
        drawArc(
          color = colors.accentCyan,
          startAngle = -90f,
          sweepAngle = sweepAngle,
          useCenter = false,
          style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
        )
      }

      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.Bottom) {
          Text(
            text = String.format("%02d:%02d", minutes, seconds),
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            color = colors.textPrimary
          )
          Text(
            text = String.format(".%02d", hundredths),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = colors.textSecondary,
            modifier = Modifier.padding(bottom = 6.dp)
          )
        }
        Text(
          text = if (isRunning) "RUNNING" else if (elapsedTimeMillis > 0) "PAUSED" else "READY",
          style = MaterialTheme.typography.labelSmall,
          color = if (isRunning) SuccessGreen else colors.textSecondary,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.5.sp
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Primary Control Buttons
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceEvenly,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Lap / Reset button
      if (isRunning) {
        Button(
          onClick = {
            val lapTime = if (laps.isEmpty()) elapsedTimeMillis else elapsedTimeMillis - lastLapTimestamp
            lastLapTimestamp = elapsedTimeMillis
            laps.add(
              0,
              LapRecord(
                lapNumber = laps.size + 1,
                lapTimeMillis = lapTime,
                totalTimeMillis = elapsedTimeMillis
              )
            )
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = colors.surfaceElevated,
            contentColor = colors.textPrimary
          ),
          shape = CircleShape,
          modifier = Modifier.size(72.dp)
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Flag, contentDescription = "Lap", modifier = Modifier.size(20.dp))
            Text("Lap", style = MaterialTheme.typography.labelSmall)
          }
        }
      } else {
        Button(
          onClick = {
            elapsedTimeMillis = 0L
            lastLapTimestamp = 0L
            laps.clear()
          },
          enabled = elapsedTimeMillis > 0,
          colors = ButtonDefaults.buttonColors(
            containerColor = colors.surfaceElevated,
            contentColor = colors.textPrimary,
            disabledContainerColor = colors.surfaceElevated.copy(alpha = 0.5f),
            disabledContentColor = colors.textDisabled
          ),
          shape = CircleShape,
          modifier = Modifier.size(72.dp)
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.size(20.dp))
            Text("Reset", style = MaterialTheme.typography.labelSmall)
          }
        }
      }

      // Start / Pause button
      Button(
        onClick = { isRunning = !isRunning },
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isRunning) DangerRed else colors.accentCyan,
          contentColor = if (isRunning) Color.White else colors.background
        ),
        shape = CircleShape,
        modifier = Modifier.size(80.dp)
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isRunning) "Pause" else "Start",
            modifier = Modifier.size(28.dp)
          )
          Text(
            if (isRunning) "Stop" else if (elapsedTimeMillis > 0) "Resume" else "Start",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Laps List
    if (laps.isNotEmpty()) {
      Text(
        text = "LAPS (${laps.size})",
        style = MaterialTheme.typography.labelMedium,
        color = colors.textSecondary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp)
      )
      Spacer(modifier = Modifier.height(8.dp))

      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .clip(RoundedCornerShape(16.dp))
          .background(colors.surface)
          .border(1.dp, colors.surfaceBorder, RoundedCornerShape(16.dp))
          .padding(8.dp)
      ) {
        itemsIndexed(laps) { _, lap ->
          val isFastest = fastestLapMillis != null && lap.lapTimeMillis == fastestLapMillis
          val isSlowest = slowestLapMillis != null && lap.lapTimeMillis == slowestLapMillis

          val lapMin = (lap.lapTimeMillis / 60000) % 60
          val lapSec = (lap.lapTimeMillis / 1000) % 60
          val lapHun = (lap.lapTimeMillis % 1000) / 10

          val totMin = (lap.totalTimeMillis / 60000) % 60
          val totSec = (lap.totalTimeMillis / 1000) % 60
          val totHun = (lap.totalTimeMillis % 1000) / 10

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "Lap ${lap.lapNumber}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
              )
              if (isFastest) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "FASTEST",
                  style = MaterialTheme.typography.labelSmall,
                  color = SuccessGreen,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(SuccessGreen.copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                )
              } else if (isSlowest) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "SLOWEST",
                  style = MaterialTheme.typography.labelSmall,
                  color = DangerRed,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(DangerRed.copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }

            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = String.format("+%02d:%02d.%02d", lapMin, lapSec, lapHun),
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = if (isFastest) SuccessGreen else if (isSlowest) DangerRed else colors.textPrimary
              )
              Text(
                text = String.format("%02d:%02d.%02d", totMin, totSec, totHun),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = colors.textSecondary
              )
            }
          }
        }
      }
    } else {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "Tap 'Lap' while running to track split times",
          style = MaterialTheme.typography.bodyMedium,
          color = colors.textTertiary
        )
      }
    }
  }
}

// -----------------------------------------------------------------------------
// 2. COUNTDOWN TIMER PANE
// -----------------------------------------------------------------------------

@Composable
fun CountdownTimerPane() {
  val context = LocalContext.current
  val colors = AppTheme.colors
  val scope = rememberCoroutineScope()

  var inputHours by remember { mutableIntStateOf(0) }
  var inputMinutes by remember { mutableIntStateOf(5) }
  var inputSeconds by remember { mutableIntStateOf(0) }

  var totalTimeMillis by remember { mutableLongStateOf(5 * 60 * 1000L) }
  var remainingTimeMillis by remember { mutableLongStateOf(5 * 60 * 1000L) }
  var isTimerRunning by remember { mutableStateOf(false) }
  var isTimerFinished by remember { mutableStateOf(false) }

  // Sound and vibration alert on timer completion
  fun triggerTimerCompletionAlert() {
    isTimerFinished = true
    scope.launch(Dispatchers.Default) {
      try {
        val sampleRate = 44100
        val numSamples = (sampleRate * 1.5).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
          val t = i.toDouble() / sampleRate
          val freq = if ((i / (sampleRate / 4)) % 2 == 0) 880.0 else 1320.0
          val sample = (sin(2.0 * Math.PI * freq * t) * Short.MAX_VALUE * 0.8).toInt().toShort()
          buffer[i] = sample
        }
        val audioTrack = AudioTrack(
          AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build(),
          AudioFormat.Builder()
            .setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .build(),
          buffer.size * 2,
          AudioTrack.MODE_STATIC,
          AudioManager.AUDIO_SESSION_ID_GENERATE
        )
        audioTrack.write(buffer, 0, buffer.size)
        audioTrack.play()
      } catch (e: Exception) {
        // Fallback silently if audio cannot play
      }

      try {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
          val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
          manager?.defaultVibrator
        } else {
          context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 300, 200, 300, 200, 500), -1))
        } else {
          vibrator?.vibrate(1000)
        }
      } catch (e: Exception) {
        // Silent catch
      }
    }
  }

  // Timer tick loop
  LaunchedEffect(isTimerRunning) {
    if (isTimerRunning) {
      val endTime = System.currentTimeMillis() + remainingTimeMillis
      while (isActive && isTimerRunning) {
        val diff = endTime - System.currentTimeMillis()
        if (diff <= 0) {
          remainingTimeMillis = 0
          isTimerRunning = false
          triggerTimerCompletionAlert()
          break
        } else {
          remainingTimeMillis = diff
        }
        delay(100)
      }
    }
  }

  val progress = if (totalTimeMillis > 0) {
    (remainingTimeMillis.toFloat() / totalTimeMillis.toFloat()).coerceIn(0f, 1f)
  } else 0f
  val animatedProgress by animateFloatAsState(targetValue = progress, label = "timerProgress")

  val remHours = (remainingTimeMillis / 3600000)
  val remMinutes = (remainingTimeMillis / 60000) % 60
  val remSeconds = (remainingTimeMillis / 1000) % 60

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(vertical = 12.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Circular Progress Dial
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier
        .size(240.dp)
        .padding(16.dp)
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
          color = colors.surfaceElevated,
          style = Stroke(width = 10.dp.toPx())
        )
        drawArc(
          color = if (remainingTimeMillis < 10000 && isTimerRunning) DangerRed else colors.accentCyan,
          startAngle = -90f,
          sweepAngle = animatedProgress * 360f,
          useCenter = false,
          style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
        )
      }

      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          if (remHours > 0) {
            Text(
              text = String.format("%02d:", remHours),
              style = MaterialTheme.typography.displayMedium,
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.Monospace,
              color = colors.textPrimary
            )
          }
          Text(
            text = String.format("%02d:%02d", remMinutes, remSeconds),
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            color = colors.textPrimary
          )
        }
        Text(
          text = if (isTimerRunning) "COUNTING DOWN" else if (remainingTimeMillis != totalTimeMillis && remainingTimeMillis > 0) "PAUSED" else "SET TIMER",
          style = MaterialTheme.typography.labelSmall,
          color = if (isTimerRunning) colors.accentCyan else colors.textSecondary,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Time Pickers when paused/stopped
    if (!isTimerRunning && remainingTimeMillis == totalTimeMillis) {
      Card(
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(colors.surfaceBorder)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Hours
          TimerNumberPicker(
            label = "Hours",
            value = inputHours,
            onValueChange = {
              inputHours = it
              totalTimeMillis = (inputHours * 3600L + inputMinutes * 60L + inputSeconds) * 1000L
              remainingTimeMillis = totalTimeMillis
            },
            max = 23
          )

          Text(":", style = MaterialTheme.typography.headlineMedium, color = colors.textSecondary)

          // Minutes
          TimerNumberPicker(
            label = "Minutes",
            value = inputMinutes,
            onValueChange = {
              inputMinutes = it
              totalTimeMillis = (inputHours * 3600L + inputMinutes * 60L + inputSeconds) * 1000L
              remainingTimeMillis = totalTimeMillis
            },
            max = 59
          )

          Text(":", style = MaterialTheme.typography.headlineMedium, color = colors.textSecondary)

          // Seconds
          TimerNumberPicker(
            label = "Seconds",
            value = inputSeconds,
            onValueChange = {
              inputSeconds = it
              totalTimeMillis = (inputHours * 3600L + inputMinutes * 60L + inputSeconds) * 1000L
              remainingTimeMillis = totalTimeMillis
            },
            max = 59
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Presets
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        listOf(
          "1 min" to 1,
          "3 min" to 3,
          "5 min" to 5,
          "10 min" to 10,
          "25 min" to 25
        ).forEach { (label, mins) ->
          AssistChip(
            onClick = {
              inputHours = 0
              inputMinutes = mins
              inputSeconds = 0
              totalTimeMillis = mins * 60 * 1000L
              remainingTimeMillis = totalTimeMillis
            },
            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
            colors = AssistChipDefaults.assistChipColors(
              containerColor = colors.surfaceElevated,
              labelColor = if (inputMinutes == mins && inputHours == 0) colors.accentCyan else colors.textPrimary
            )
          )
        }
      }
    } else {
      // While running: +1 Min quick addition button
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
      ) {
        AssistChip(
          onClick = {
            totalTimeMillis += 60000L
            remainingTimeMillis += 60000L
          },
          label = { Text("+1 Minute", fontWeight = FontWeight.Bold) },
          leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp)) },
          colors = AssistChipDefaults.assistChipColors(
            containerColor = colors.surfaceElevated,
            labelColor = colors.accentCyan
          )
        )
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Controls
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceEvenly,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Reset button
      Button(
        onClick = {
          isTimerRunning = false
          remainingTimeMillis = totalTimeMillis
        },
        enabled = remainingTimeMillis != totalTimeMillis || isTimerRunning,
        colors = ButtonDefaults.buttonColors(
          containerColor = colors.surfaceElevated,
          contentColor = colors.textPrimary,
          disabledContainerColor = colors.surfaceElevated.copy(alpha = 0.5f),
          disabledContentColor = colors.textDisabled
        ),
        shape = CircleShape,
        modifier = Modifier.size(72.dp)
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.size(20.dp))
          Text("Reset", style = MaterialTheme.typography.labelSmall)
        }
      }

      // Start / Pause button
      Button(
        onClick = {
          if (remainingTimeMillis == 0L) {
            remainingTimeMillis = totalTimeMillis
          }
          isTimerRunning = !isTimerRunning
        },
        enabled = totalTimeMillis > 0,
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isTimerRunning) DangerRed else colors.accentCyan,
          contentColor = if (isTimerRunning) Color.White else colors.background
        ),
        shape = CircleShape,
        modifier = Modifier.size(80.dp)
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isTimerRunning) "Pause" else "Start",
            modifier = Modifier.size(28.dp)
          )
          Text(
            if (isTimerRunning) "Pause" else if (remainingTimeMillis < totalTimeMillis) "Resume" else "Start",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }

  // Timer Completion Alert Dialog
  if (isTimerFinished) {
    AlertDialog(
      onDismissRequest = { isTimerFinished = false },
      containerColor = colors.surfaceElevated,
      titleContentColor = colors.textPrimary,
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = EnergyAmber)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Time's Up!", fontWeight = FontWeight.Black)
        }
      },
      text = {
        Text(
          "Your countdown timer has completed.",
          color = colors.textSecondary
        )
      },
      confirmButton = {
        Button(
          onClick = {
            isTimerFinished = false
            remainingTimeMillis = totalTimeMillis
          },
          colors = ButtonDefaults.buttonColors(containerColor = colors.accentCyan, contentColor = colors.background)
        ) {
          Text("Restart", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = {
          isTimerFinished = false
          remainingTimeMillis = totalTimeMillis
        }) {
          Text("Dismiss", color = colors.textSecondary)
        }
      }
    )
  }
}

@Composable
fun TimerNumberPicker(
  label: String,
  value: Int,
  onValueChange: (Int) -> Unit,
  max: Int
) {
  val colors = AppTheme.colors

  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    IconButton(
      onClick = { onValueChange((value + 1) % (max + 1)) },
      modifier = Modifier.size(36.dp)
    ) {
      Icon(Icons.Default.Add, contentDescription = "Increase $label", tint = colors.accentCyan)
    }

    Text(
      text = String.format("%02d", value),
      style = MaterialTheme.typography.headlineMedium,
      fontWeight = FontWeight.Black,
      fontFamily = FontFamily.Monospace,
      color = colors.textPrimary
    )

    IconButton(
      onClick = { onValueChange(if (value == 0) max else value - 1) },
      modifier = Modifier.size(36.dp)
    ) {
      Icon(Icons.Default.Remove, contentDescription = "Decrease $label", tint = colors.accentCyan)
    }

    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = colors.textSecondary
    )
  }
}

// -----------------------------------------------------------------------------
// 3. WORLD CLOCK PANE
// -----------------------------------------------------------------------------

data class WorldCity(
  val name: String,
  val country: String,
  val timezoneId: String
)

@Composable
fun WorldClockPane() {
  val colors = AppTheme.colors
  var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

  // Update clock every second
  LaunchedEffect(Unit) {
    while (isActive) {
      currentTime = System.currentTimeMillis()
      delay(1000)
    }
  }

  val localTimeZone = remember { TimeZone.getDefault() }
  val localDateFormat = remember {
    SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).apply {
      timeZone = localTimeZone
    }
  }
  val localTimeFormat = remember {
    SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).apply {
      timeZone = localTimeZone
    }
  }

  val majorCities = remember {
    listOf(
      WorldCity("London", "United Kingdom", "Europe/London"),
      WorldCity("New York", "United States", "America/New_York"),
      WorldCity("Tokyo", "Japan", "Asia/Tokyo"),
      WorldCity("Paris", "France", "Europe/Paris"),
      WorldCity("Sydney", "Australia", "Australia/Sydney"),
      WorldCity("Dubai", "United Arab Emirates", "Asia/Dubai"),
      WorldCity("San Francisco", "United States", "America/Los_Angeles"),
      WorldCity("Singapore", "Singapore", "Asia/Singapore"),
      WorldCity("New Delhi", "India", "Asia/Kolkata")
    )
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(vertical = 10.dp)
  ) {
    // Current Local Time Card
    Card(
      colors = CardDefaults.cardColors(containerColor = colors.surface),
      shape = RoundedCornerShape(18.dp),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(colors.accentCyan.copy(alpha = 0.4f))),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(SuccessGreen)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "LOCAL TIME (${localTimeZone.displayName})",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = colors.textSecondary,
            letterSpacing = 1.sp
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = localTimeFormat.format(Date(currentTime)),
          style = MaterialTheme.typography.headlineLarge,
          fontWeight = FontWeight.Black,
          fontFamily = FontFamily.Monospace,
          color = colors.textPrimary
        )

        Text(
          text = localDateFormat.format(Date(currentTime)),
          style = MaterialTheme.typography.bodyMedium,
          color = colors.accentCyan
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = "INTERNATIONAL TIMEZONES",
      style = MaterialTheme.typography.labelMedium,
      color = colors.textSecondary,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(horizontal = 4.dp)
    )

    Spacer(modifier = Modifier.height(8.dp))

    LazyColumn(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      items(majorCities) { city ->
        val tz = TimeZone.getTimeZone(city.timezoneId)
        val cityTimeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault()).apply {
          timeZone = tz
        }
        val cityCal = Calendar.getInstance(tz).apply { timeInMillis = currentTime }
        val hourInCity = cityCal.get(Calendar.HOUR_OF_DAY)
        val isDaytime = hourInCity in 6..18

        val diffMillis = tz.getOffset(currentTime) - localTimeZone.getOffset(currentTime)
        val diffHours = diffMillis / (1000 * 60 * 60)
        val diffMinutes = (diffMillis % (1000 * 60 * 60)) / (1000 * 60)

        val diffString = when {
          diffHours == 0 && diffMinutes == 0 -> "Same time as local"
          diffHours > 0 -> "+$diffHours hrs ahead"
          else -> "$diffHours hrs behind"
        }

        Card(
          colors = CardDefaults.cardColors(containerColor = colors.surface),
          shape = RoundedCornerShape(14.dp),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(colors.surfaceBorder)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = city.name,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = colors.textPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                  imageVector = if (isDaytime) Icons.Default.LightMode else Icons.Default.DarkMode,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                  tint = if (isDaytime) EnergyAmber else colors.accentViolet
                )
              }
              Text(
                text = "${city.country} • $diffString",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
              )
            }

            Column(horizontalAlignment = Alignment.End) {
              val cityTime = cityTimeFormat.format(Date(currentTime))
              Text(
                text = cityTime,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = colors.textPrimary
              )
              Text(
                text = tz.id.substringAfter('/'),
                style = MaterialTheme.typography.labelSmall,
                color = colors.accentCyan
              )
            }
          }
        }
      }
    }
  }
}

// -----------------------------------------------------------------------------
// 4. SLEEP CYCLES CALCULATOR PANE
// -----------------------------------------------------------------------------

data class SleepCycleOption(
  val cycleCount: Int,
  val totalHours: Double,
  val wakeHour: Int,
  val wakeMinute: Int,
  val description: String,
  val isRecommended: Boolean = false
)

@Composable
fun SleepCyclesPane(
  onNavigateToAlarmEdit: (hour: Int, minute: Int) -> Unit
) {
  val colors = AppTheme.colors
  val now = Calendar.getInstance()
  val currentHour = now.get(Calendar.HOUR_OF_DAY)
  val currentMinute = now.get(Calendar.MINUTE)

  // 14 minutes average time to fall asleep + 90 minutes per sleep cycle
  val cycles = remember(currentHour, currentMinute) {
    listOf(4, 5, 6).map { count ->
      val totalMinutesToAdd = 14 + (count * 90)
      val wakeCal = (now.clone() as Calendar).apply {
        add(Calendar.MINUTE, totalMinutesToAdd)
      }
      val wakeH = wakeCal.get(Calendar.HOUR_OF_DAY)
      val wakeM = wakeCal.get(Calendar.MINUTE)
      val hours = (count * 90) / 60.0
      val desc = when (count) {
        4 -> "Quick recharge • 6.0 hrs sleep"
        5 -> "Optimal sweet spot • 7.5 hrs sleep"
        else -> "Full recovery • 9.0 hrs sleep"
      }
      SleepCycleOption(
        cycleCount = count,
        totalHours = hours,
        wakeHour = wakeH,
        wakeMinute = wakeM,
        description = desc,
        isRecommended = (count == 5)
      )
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(vertical = 10.dp)
  ) {
    Card(
      colors = CardDefaults.cardColors(containerColor = colors.surface),
      shape = RoundedCornerShape(18.dp),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(colors.surfaceBorder)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Bedtime, contentDescription = null, tint = colors.accentViolet)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "REM SLEEP CYCLE CALCULATOR",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = colors.accentViolet
          )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Human sleep occurs in natural 90-minute cycles. Waking up between cycles prevents grogginess and morning sleep inertia.",
          style = MaterialTheme.typography.bodySmall,
          color = colors.textSecondary
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    Text(
      text = "IF YOU GO TO SLEEP NOW:",
      style = MaterialTheme.typography.labelMedium,
      color = colors.textSecondary,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(horizontal = 4.dp)
    )

    Spacer(modifier = Modifier.height(8.dp))

    LazyColumn(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      items(cycles) { option ->
        val timeDisplay = String.format(
          "%d:%02d %s",
          if (option.wakeHour % 12 == 0) 12 else option.wakeHour % 12,
          option.wakeMinute,
          if (option.wakeHour < 12) "AM" else "PM"
        )

        Card(
          colors = CardDefaults.cardColors(
            containerColor = if (option.isRecommended) colors.surfaceElevated else colors.surface
          ),
          shape = RoundedCornerShape(16.dp),
          border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
              if (option.isRecommended) colors.accentCyan else colors.surfaceBorder
            )
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = timeDisplay,
                  style = MaterialTheme.typography.headlineSmall,
                  fontWeight = FontWeight.Black,
                  color = colors.textPrimary
                )
                if (option.isRecommended) {
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "BEST",
                    style = MaterialTheme.typography.labelSmall,
                    color = SuccessGreen,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                      .clip(RoundedCornerShape(4.dp))
                      .background(SuccessGreen.copy(alpha = 0.15f))
                      .padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
              Text(
                text = "${option.cycleCount} Cycles • ${option.description}",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
              )
            }

            Button(
              onClick = {
                onNavigateToAlarmEdit(option.wakeHour, option.wakeMinute)
              },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (option.isRecommended) colors.accentCyan else colors.surfaceElevated,
                contentColor = if (option.isRecommended) colors.background else colors.textPrimary
              ),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("Set Alarm", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}
