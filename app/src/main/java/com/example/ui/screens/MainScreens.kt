package com.example.ui.screens

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Schedule
import com.example.ui.theme.AppTheme
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.ClockColorMode
import com.example.ui.theme.NeonClockShadow
import com.example.ui.theme.neonClockHourColor
import com.example.ui.theme.neonClockMinuteColor
import com.example.ui.theme.neonClockSeparatorColor
import com.example.ui.theme.neonClockPrimaryColor
import com.example.alarm.AlarmScheduler
import com.example.data.model.AlarmEntity
import com.example.data.model.AlarmHistoryEntity
import com.example.data.model.ChallengeType
import com.example.data.model.Difficulty
import com.example.data.repository.AlarmStats
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ElectricVioletLight
import com.example.ui.theme.EnergyAmber
import com.example.ui.theme.EnergyOrange
import com.example.ui.theme.SuccessGreen
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Dynamic theme color getters - adapt seamlessly between Dark and Light mode
private val DeepNavy: Color @Composable get() = AppTheme.colors.background
private val DarkSurface: Color @Composable get() = AppTheme.colors.surface
private val DarkSurfaceElevated: Color @Composable get() = AppTheme.colors.surfaceElevated
private val DarkSurfaceBorder: Color @Composable get() = AppTheme.colors.surfaceBorder
private val TextPrimary: Color @Composable get() = AppTheme.colors.textPrimary
private val TextSecondary: Color @Composable get() = AppTheme.colors.textSecondary
private val TextTertiary: Color @Composable get() = AppTheme.colors.textTertiary
private val TextDisabled: Color @Composable get() = AppTheme.colors.textDisabled
private val NeonCyan: Color @Composable get() = AppTheme.colors.accentCyan
private val ElectricViolet: Color @Composable get() = AppTheme.colors.accentViolet

// -------------------------------------------------------------------------
// 1. DASHBOARD SCREEN
// -------------------------------------------------------------------------
@Composable
fun DashboardScreen(
  alarms: List<AlarmEntity>,
  stats: AlarmStats,
  onToggleAlarm: (AlarmEntity) -> Unit,
  onAddAlarmClick: () -> Unit,
  onEditAlarmClick: (AlarmEntity) -> Unit,
  onTestChallengeClick: () -> Unit,
  onQuickTestAlarmClick: () -> Unit,
  onToggleTheme: (() -> Unit)? = null,
  isDarkTheme: Boolean = true
) {
  var liveTime by remember { mutableStateOf("") }
  var liveDate by remember { mutableStateOf("") }
  var greeting by remember { mutableStateOf("GOOD MORNING") }

  LaunchedEffect(Unit) {
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())

    while (true) {
      val now = Date()
      liveTime = timeFormat.format(now)
      liveDate = dateFormat.format(now)

      val cal = Calendar.getInstance()
      val hour = cal.get(Calendar.HOUR_OF_DAY)
      greeting = when (hour) {
        in 4..11 -> "GOOD MORNING"
        in 12..16 -> "GOOD AFTERNOON"
        in 17..21 -> "GOOD EVENING"
        else -> "NIGHT VIGIL"
      }
      delay(1000)
    }
  }

  // Find next upcoming alarm
  val enabledAlarms = alarms.filter { it.isEnabled }
  val nextPair = remember(alarms) {
    if (enabledAlarms.isEmpty()) null
    else {
      var bestAlarm: AlarmEntity? = null
      var bestTime = Long.MAX_VALUE
      for (a in enabledAlarms) {
        val t = AlarmScheduler.calculateNextTriggerTime(a.hour, a.minute, a.getDaysList())
        if (t < bestTime) {
          bestTime = t
          bestAlarm = a
        }
      }
      if (bestAlarm != null) Pair(bestAlarm, bestTime) else null
    }
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 18.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(12.dp))
      // Greeting Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = greeting,
            style = MaterialTheme.typography.labelLarge,
            color = NeonCyan,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
          )
          Text(
            text = liveDate,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
          )
        }
        // Header Action Controls: Theme toggle & Streak indicator
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          if (onToggleTheme != null) {
            IconButton(
              onClick = onToggleTheme,
              modifier = Modifier
                .clip(CircleShape)
                .background(DarkSurfaceElevated)
                .border(1.dp, DarkSurfaceBorder, CircleShape)
                .size(38.dp)
            ) {
              Icon(
                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                contentDescription = "Toggle Theme",
                tint = NeonCyan,
                modifier = Modifier.size(18.dp)
              )
            }
          }

          // Streak indicator
          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(DarkSurfaceElevated)
              .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(12.dp))
              .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("🔥", fontSize = 16.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "${stats.currentStreak}d Streak",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = EnergyAmber
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Big Live Digital Clock
      val parts = liveTime.split(" ")
      val timeDigits = parts.firstOrNull() ?: "--:--"
      val amPm = parts.getOrNull(1) ?: ""

      Row(
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.testTag("dashboard_live_clock")
      ) {
        Text(
          text = timeDigits,
          style = MaterialTheme.typography.displayLarge,
          fontWeight = FontWeight.Black,
          color = TextPrimary,
          letterSpacing = (-1).sp
        )
        if (amPm.isNotBlank()) {
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = amPm,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
          )
        }
      }
    }

    // NEXT ALARM HERO CARD
    item {
      if (nextPair != null) {
        val (alarm, triggerMillis) = nextPair
        Card(
          colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
          shape = RoundedCornerShape(24.dp),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ElectricViolet)),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditAlarmClick(alarm) }
            .testTag("next_alarm_card")
        ) {
          Column(modifier = Modifier.padding(20.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Alarm,
                  contentDescription = null,
                  tint = NeonCyan,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "NEXT ALARM",
                  style = MaterialTheme.typography.labelMedium,
                  color = NeonCyan,
                  fontWeight = FontWeight.Black,
                  letterSpacing = 1.sp
                )
              }
              Switch(
                checked = alarm.isEnabled,
                onCheckedChange = { onToggleAlarm(alarm) },
                colors = SwitchDefaults.colors(
                  checkedThumbColor = NeonCyan,
                  checkedTrackColor = ElectricViolet
                )
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.Bottom) {
              Text(
                text = alarm.formattedTime(),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = TextPrimary
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = alarm.amPm(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 6.dp)
              )
            }

            Text(
              text = alarm.name,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
              color = TextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = AlarmScheduler.formatRemainingTime(triggerMillis),
                style = MaterialTheme.typography.bodySmall,
                color = EnergyAmber,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Challenge: ${ChallengeType.fromId(alarm.challengeType).title}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
              )
            }
          }
        }
      } else {
        // No alarms enabled card
        Card(
          colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
          shape = RoundedCornerShape(20.dp),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkSurfaceBorder)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              imageVector = Icons.Default.Alarm,
              contentDescription = null,
              tint = TextDisabled,
              modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = "No Alarms Scheduled",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Tap + Add Alarm to schedule your wake-up alarm.",
              style = MaterialTheme.typography.bodyMedium,
              color = TextSecondary,
              textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(
              onClick = onAddAlarmClick,
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet)
            ) {
              Icon(Icons.Default.Add, contentDescription = null)
              Spacer(modifier = Modifier.width(6.dp))
              Text("Add Alarm")
            }
          }
        }
      }
    }

    // QUICK TEST ACTIONS (Testing alarms & challenges)
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Test Challenge Card
        Card(
          colors = CardDefaults.cardColors(containerColor = DarkSurface),
          shape = RoundedCornerShape(16.dp),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkSurfaceBorder)),
          modifier = Modifier
            .weight(1f)
            .clickable { onTestChallengeClick() }
            .testTag("dashboard_test_challenge_button")
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Icon(Icons.Default.Bolt, contentDescription = null, tint = EnergyAmber)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Try Challenge",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
            Text(
              text = "Practice mini-games",
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondary
            )
          }
        }

        // Test Ringing Alarm (Rings in 5s)
        Card(
          colors = CardDefaults.cardColors(containerColor = DarkSurface),
          shape = RoundedCornerShape(16.dp),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkSurfaceBorder)),
          modifier = Modifier
            .weight(1f)
            .clickable { onQuickTestAlarmClick() }
            .testTag("dashboard_test_alarm_button")
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = DangerRed)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Test Alarm (5s)",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
            Text(
              text = "Full-screen lock test",
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondary
            )
          }
        }
      }
    }

    // STATS SUMMARY ROW
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(18.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkSurfaceBorder)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceAround
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "${stats.totalCompleted}",
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.Black,
              color = NeonCyan
            )
            Text("Completed", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
          }
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "${stats.avgDurationSeconds}s",
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.Black,
              color = SuccessGreen
            )
            Text("Avg Wake Time", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
          }
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "${stats.currentStreak}d",
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.Black,
              color = EnergyAmber
            )
            Text("Current Streak", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(60.dp))
    }
  }
}

// -------------------------------------------------------------------------
// 2. ALARMS SCREEN
// -------------------------------------------------------------------------
@Composable
fun AlarmsScreen(
  alarms: List<AlarmEntity>,
  onToggleAlarm: (AlarmEntity) -> Unit,
  onEditAlarm: (AlarmEntity) -> Unit,
  onDeleteAlarm: (AlarmEntity) -> Unit,
  onAddAlarm: () -> Unit
) {
  Scaffold(
    containerColor = DeepNavy,
    floatingActionButton = {
      FloatingActionButton(
        onClick = onAddAlarm,
        containerColor = NeonCyan,
        contentColor = DeepNavy,
        shape = CircleShape,
        modifier = Modifier.testTag("fab_add_alarm")
      ) {
        Icon(Icons.Default.Add, contentDescription = "Add Alarm", modifier = Modifier.size(28.dp))
      }
    }
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(horizontal = 18.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      item {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
          text = "YOUR ALARMS",
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Black,
          color = TextPrimary
        )
        Text(
          text = "${alarms.count { it.isEnabled }} active • ${alarms.size} total",
          style = MaterialTheme.typography.bodyMedium,
          color = TextSecondary
        )
        Spacer(modifier = Modifier.height(6.dp))
      }

      if (alarms.isEmpty()) {
        item {
          Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 40.dp)
          ) {
            Column(
              modifier = Modifier.padding(32.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(Icons.Default.Alarm, contentDescription = null, tint = TextDisabled, modifier = Modifier.size(54.dp))
              Spacer(modifier = Modifier.height(12.dp))
              Text("No Alarms Created", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
              Text("Tap + to configure your first smart alarm.", color = TextSecondary, textAlign = TextAlign.Center)
            }
          }
        }
      } else {
        items(alarms, key = { it.id }) { alarm ->
          AlarmCard(
            alarm = alarm,
            onToggle = { onToggleAlarm(alarm) },
            onClick = { onEditAlarm(alarm) },
            onDelete = { onDeleteAlarm(alarm) }
          )
        }
      }

      item {
        Spacer(modifier = Modifier.height(80.dp))
      }
    }
  }
}

@Composable
fun AlarmCard(
  alarm: AlarmEntity,
  onToggle: () -> Unit,
  onClick: () -> Unit,
  onDelete: () -> Unit
) {
  Card(
    colors = CardDefaults.cardColors(
      containerColor = if (alarm.isEnabled) DarkSurfaceElevated else DarkSurface
    ),
    shape = RoundedCornerShape(20.dp),
    border = CardDefaults.outlinedCardBorder().copy(
      brush = androidx.compose.ui.graphics.SolidColor(if (alarm.isEnabled) ElectricViolet.copy(alpha = 0.6f) else DarkSurfaceBorder)
    ),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("alarm_item_${alarm.id}")
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.Bottom) {
          Text(
            text = alarm.formattedTime(),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            color = if (alarm.isEnabled) TextPrimary else TextDisabled
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = alarm.amPm(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (alarm.isEnabled) TextSecondary else TextDisabled,
            modifier = Modifier.padding(bottom = 4.dp)
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete Alarm", tint = if (alarm.isEnabled) TextSecondary else TextDisabled)
          }
          Switch(
            checked = alarm.isEnabled,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
              checkedThumbColor = NeonCyan,
              checkedTrackColor = ElectricViolet
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = alarm.name,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = if (alarm.isEnabled) TextSecondary else TextDisabled
      )

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Repeat badges
        Text(
          text = alarm.repeatSummary(),
          style = MaterialTheme.typography.bodySmall,
          color = if (alarm.isEnabled) EnergyAmber else TextDisabled,
          fontWeight = FontWeight.Bold
        )

        // Challenge chip
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurfaceBorder)
            .padding(horizontal = 8.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Psychology,
            contentDescription = null,
            tint = if (alarm.isEnabled) NeonCyan else TextDisabled,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "${ChallengeType.fromId(alarm.challengeType).title} • ${alarm.difficulty}",
            style = MaterialTheme.typography.labelSmall,
            color = if (alarm.isEnabled) TextPrimary else TextDisabled
          )
        }
      }
    }
  }
}

// -------------------------------------------------------------------------
// 3. STATISTICS SCREEN
// -------------------------------------------------------------------------
@Composable
fun StatisticsScreen(
  stats: AlarmStats,
  history: List<AlarmHistoryEntity>,
  onClearHistory: () -> Unit
) {
  val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 18.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = "WAKE STATISTICS",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Black,
        color = TextPrimary
      )
      Text(
        text = "Track your cognitive wakefulness and streaks",
        style = MaterialTheme.typography.bodyMedium,
        color = TextSecondary
      )
    }

    // Streak Hero Banner
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(22.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EnergyAmber)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(20.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(64.dp)
              .clip(CircleShape)
              .background(EnergyAmber.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
          ) {
            Text("🔥", fontSize = 32.sp)
          }
          Spacer(modifier = Modifier.width(16.dp))
          Column {
            Text(
              text = "${stats.currentStreak} Day Wake Streak",
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.Black,
              color = TextPrimary
            )
            Text(
              text = "Personal Best: ${stats.bestStreak} days in a row",
              style = MaterialTheme.typography.bodyMedium,
              color = EnergyAmber
            )
          }
        }
      }
    }

    // Grid Metrics
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        MetricCard(
          title = "Alarms Solved",
          value = "${stats.totalCompleted}",
          subtitle = "All through challenges",
          color = NeonCyan,
          modifier = Modifier.weight(1f)
        )
        MetricCard(
          title = "Avg Wake Time",
          value = "${stats.avgDurationSeconds}s",
          subtitle = "To solve challenge",
          color = SuccessGreen,
          modifier = Modifier.weight(1f)
        )
      }
    }

    // Weekly Chart Visualizer
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkSurfaceBorder)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = "WEEKLY ACTIVITY",
            style = MaterialTheme.typography.labelLarge,
            color = NeonCyan,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(14.dp))

          val daysOfWeek = listOf(
            Calendar.MONDAY to "M",
            Calendar.TUESDAY to "T",
            Calendar.WEDNESDAY to "W",
            Calendar.THURSDAY to "T",
            Calendar.FRIDAY to "F",
            Calendar.SATURDAY to "S",
            Calendar.SUNDAY to "S"
          )

          val maxCount = (stats.weeklyCompleted.values.maxOrNull() ?: 1).coerceAtLeast(1)

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
          ) {
            daysOfWeek.forEach { (calDay, label) ->
              val count = stats.weeklyCompleted[calDay] ?: 0
              val heightFraction = (count.toFloat() / maxCount).coerceIn(0.12f, 1f)

              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
              ) {
                Box(
                  modifier = Modifier
                    .width(28.dp)
                    .height((80 * heightFraction).dp)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(if (count > 0) ElectricViolet else DarkSurfaceBorder)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = label,
                  style = MaterialTheme.typography.labelSmall,
                  color = if (count > 0) TextPrimary else TextSecondary,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }
      }
    }

    // History List Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "ALARM HISTORY",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
        if (history.isNotEmpty()) {
          OutlinedButton(
            onClick = onClearHistory,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
          ) {
            Text("Clear", style = MaterialTheme.typography.bodySmall)
          }
        }
      }
    }

    if (history.isEmpty()) {
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = DarkSurface),
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "No alarm history yet. Complete your first morning challenge!",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(20.dp),
            textAlign = TextAlign.Center
          )
        }
      }
    } else {
      items(history, key = { it.id }) { item ->
        Card(
          colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
          shape = RoundedCornerShape(16.dp),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkSurfaceBorder)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = item.alarmName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
              Text(
                text = dateFormat.format(Date(item.completedTimeMillis)),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
              )
              Text(
                text = "Challenge: ${item.challengeType} • ${item.difficulty}",
                style = MaterialTheme.typography.labelSmall,
                color = NeonCyan
              )
            }
            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = "${item.durationSeconds}s",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = SuccessGreen
              )
              if (item.snoozeCount > 0) {
                Text(
                  text = "${item.snoozeCount} snoozes",
                  style = MaterialTheme.typography.labelSmall,
                  color = EnergyAmber
                )
              }
            }
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(70.dp))
    }
  }
}

@Composable
fun MetricCard(
  title: String,
  value: String,
  subtitle: String,
  color: androidx.compose.ui.graphics.Color,
  modifier: Modifier = Modifier
) {
  Card(
    colors = CardDefaults.cardColors(containerColor = DarkSurface),
    shape = RoundedCornerShape(18.dp),
    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkSurfaceBorder)),
    modifier = modifier
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(title, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Black,
        color = color
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
    }
  }
}

// -------------------------------------------------------------------------
// 4. SETTINGS SCREEN
// -------------------------------------------------------------------------
@Composable
fun SettingsScreen(
  currentThemeMode: AppThemeMode = AppThemeMode.DARK,
  onThemeModeChanged: (AppThemeMode) -> Unit = {},
  currentClockColorMode: ClockColorMode = ClockColorMode.NEON_DUO,
  onClockColorModeChanged: (ClockColorMode) -> Unit = {}
) {
  val context = LocalContext.current
  val canScheduleExact = remember { AlarmScheduler.canScheduleExactAlarms(context) }
  val themeColors = AppTheme.colors

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 18.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = "SETTINGS",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Black,
        color = TextPrimary
      )
      Text(
        text = "Theme, system reliability, permissions & preferences",
        style = MaterialTheme.typography.bodyMedium,
        color = TextSecondary
      )
    }

    // Appearance & Theme Selection Card
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkSurfaceBorder)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = if (themeColors.isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
              contentDescription = null,
              tint = NeonCyan
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "APPEARANCE & THEME",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Black,
              color = NeonCyan
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            AppThemeMode.entries.forEach { mode ->
              val isSelected = currentThemeMode == mode
              Card(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(12.dp))
                  .clickable { onThemeModeChanged(mode) },
                colors = CardDefaults.cardColors(
                  containerColor = if (isSelected) DarkSurfaceElevated else DarkSurface
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                  brush = androidx.compose.ui.graphics.SolidColor(
                    if (isSelected) NeonCyan else DarkSurfaceBorder
                  )
                )
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 4.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Icon(
                    imageVector = when (mode) {
                      AppThemeMode.DARK -> Icons.Default.DarkMode
                      AppThemeMode.LIGHT -> Icons.Default.LightMode
                      AppThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                    },
                    contentDescription = null,
                    tint = if (isSelected) NeonCyan else TextSecondary,
                    modifier = Modifier.size(24.dp)
                  )
                  Spacer(modifier = Modifier.height(6.dp))
                  Text(
                    text = mode.title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) NeonCyan else TextSecondary,
                    textAlign = TextAlign.Center
                  )
                }
              }
            }
          }
        }
      }
    }

    // Android Permissions Status Card
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ElectricViolet)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Security, contentDescription = null, tint = NeonCyan)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "ALARM SYSTEM PERMISSIONS",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Black,
              color = NeonCyan
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Exact Alarm Status
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Exact Alarm Permission",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
              Text(
                text = if (canScheduleExact) "Granted • Alarms ring at exact second" else "Restricted by system settings",
                style = MaterialTheme.typography.bodySmall,
                color = if (canScheduleExact) SuccessGreen else EnergyAmber
              )
            }
            if (!canScheduleExact && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
              Button(
                onClick = {
                  val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${context.packageName}")
                  }
                  context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EnergyAmber, contentColor = DeepNavy),
                shape = RoundedCornerShape(8.dp)
              ) {
                Text("Enable")
              }
            } else {
              Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // App Details / Battery settings
          OutlinedButton(
            onClick = {
              val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
              }
              context.startActivity(intent)
            },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("Open Android App Settings (Battery / Notifications)")
          }
        }
      }
    }

    // Mini-Games Enabled Info
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(18.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkSurfaceBorder)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = "AVAILABLE MINI-GAMES",
            style = MaterialTheme.typography.labelLarge,
            color = TextSecondary,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(10.dp))

          val games = listOf(
            "Math Challenge" to "Arithmetic equations with progressive difficulty",
            "Memory Sequence" to "Recall flashing sequences of numbers",
            "Pattern Recognition" to "Discover the next symbol in geometric patterns",
            "Reaction Challenge" to "Wait for signal then tap to test reaction speed",
            "Quick Tap" to "Tap energized moving targets across the screen",
            "Memory Cards" to "Match pairs of cards to activate brain focus",
            "Word Scramble" to "Unscramble awakening energy keywords"
          )

          games.forEach { (name, desc) ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(NeonCyan)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(name, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                Text(desc, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
              }
            }
          }
        }
      }
    }

    // Privacy & Storage
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(18.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkSurfaceBorder)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Info, contentDescription = null, tint = TextSecondary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("DATA PRIVACY", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "UnikKlock runs 100% offline. All your alarms, history, and wake statistics are stored locally on your device in an encrypted Room SQLite database.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
          )
        }
      }
    }

    // App Branding Footer
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "UnikKlock",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Black,
          color = TextPrimary
        )
        Text(
          text = "“Smart Time. Bold Awakening.”",
          style = MaterialTheme.typography.bodySmall,
          color = NeonCyan,
          fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Version 1.0 • Built with Kotlin & Jetpack Compose",
          style = MaterialTheme.typography.labelSmall,
          color = TextDisabled
        )
      }
    }

    item {
      Spacer(modifier = Modifier.height(70.dp))
    }
  }
}
