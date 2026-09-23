package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AppTheme
import com.example.alarm.audio.AlarmSoundPlayer
import com.example.data.model.AlarmEntity
import com.example.data.model.ChallengeType
import com.example.data.model.Difficulty
import com.example.data.model.SoundType
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EnergyAmber
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextPrimary
import java.util.Calendar

// Dynamic theme color getters for AlarmEditSheet
private val DeepNavy: Color @Composable get() = AppTheme.colors.background
private val DarkSurface: Color @Composable get() = AppTheme.colors.surface
private val DarkSurfaceElevated: Color @Composable get() = AppTheme.colors.surfaceElevated
private val DarkSurfaceBorder: Color @Composable get() = AppTheme.colors.surfaceBorder
private val TextPrimary: Color @Composable get() = AppTheme.colors.textPrimary
private val TextSecondary: Color @Composable get() = AppTheme.colors.textSecondary
private val TextDisabled: Color @Composable get() = AppTheme.colors.textDisabled
private val NeonCyan: Color @Composable get() = AppTheme.colors.accentCyan
private val ElectricViolet: Color @Composable get() = AppTheme.colors.accentViolet


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AlarmEditSheet(
  alarm: AlarmEntity?,
  onDismiss: () -> Unit,
  onSave: (AlarmEntity) -> Unit,
  onPreviewChallenge: (ChallengeType, Difficulty) -> Unit
) {
  val context = LocalContext.current
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  var hour by remember { mutableIntStateOf(alarm?.hour ?: 7) }
  var minute by remember { mutableIntStateOf(alarm?.minute ?: 0) }
  var name by remember { mutableStateOf(alarm?.name ?: "Morning Wake Up") }
  var showMinutePickerDirect by remember { mutableStateOf(false) }
  val selectedDays = remember {
    val initial = alarm?.getDaysList() ?: listOf(
      Calendar.MONDAY,
      Calendar.TUESDAY,
      Calendar.WEDNESDAY,
      Calendar.THURSDAY,
      Calendar.FRIDAY
    )
    mutableStateListOf(*initial.toTypedArray())
  }

  var selectedSound by remember { mutableStateOf(alarm?.soundType ?: SoundType.ENERGETIC.id) }
  var volume by remember { mutableFloatStateOf((alarm?.volume ?: 85).toFloat()) }
  var isVibrate by remember { mutableStateOf(alarm?.isVibrate ?: true) }
  var isGradualVolume by remember { mutableStateOf(alarm?.isGradualVolume ?: false) }

  var difficulty by remember { mutableStateOf(alarm?.difficulty ?: Difficulty.MEDIUM.id) }
  var challengeType by remember { mutableStateOf(alarm?.challengeType ?: ChallengeType.MATH.id) }
  var challengeCount by remember { mutableIntStateOf(alarm?.challengeCount ?: 1) }

  var snoozeMinutes by remember { mutableIntStateOf(alarm?.snoozeMinutes ?: 5) }
  var maxSnoozes by remember { mutableIntStateOf(alarm?.maxSnoozeCount ?: 3) }
  var snoozeChallengeEnabled by remember { mutableStateOf(alarm?.snoozeChallengeEnabled ?: true) }

  // Sound Preview Player
  var isPreviewPlaying by remember { mutableStateOf(false) }
  val soundPlayer = remember { AlarmSoundPlayer(context) }

  DisposableEffect(Unit) {
    onDispose {
      soundPlayer.stop()
    }
  }

  ModalBottomSheet(
    onDismissRequest = {
      soundPlayer.stop()
      onDismiss()
    },
    sheetState = sheetState,
    containerColor = DarkSurfaceElevated,
    dragHandle = {
      Box(
        modifier = Modifier
          .padding(vertical = 12.dp)
          .size(width = 44.dp, height = 4.dp)
          .clip(CircleShape)
          .background(DarkSurfaceBorder)
      )
    }
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (alarm == null) "New Alarm" else "Edit Alarm",
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
        IconButton(
          onClick = {
            soundPlayer.stop()
            onDismiss()
          }
        ) {
          Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Time Selector Card
      Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkSurfaceBorder)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          val isPm = hour >= 12
          val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            // Hour controls
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              IconButton(onClick = { hour = (hour + 1) % 24 }) {
                Icon(Icons.Default.Add, contentDescription = "Hour Up", tint = NeonCyan)
              }
              Text(
                text = String.format("%02d", displayHour),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = TextPrimary
              )
              IconButton(onClick = { hour = if (hour == 0) 23 else hour - 1 }) {
                Icon(Icons.Default.Remove, contentDescription = "Hour Down", tint = NeonCyan)
              }
            }

            Text(
              text = ":",
              style = MaterialTheme.typography.displayMedium,
              fontWeight = FontWeight.Black,
              color = TextPrimary,
              modifier = Modifier.padding(horizontal = 12.dp)
            )

            // Minute controls
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              IconButton(onClick = { minute = (minute + 1) % 60 }) {
                Icon(Icons.Default.Add, contentDescription = "Minute Up", tint = NeonCyan)
              }
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(DarkSurfaceElevated.copy(alpha = 0.6f))
                  .clickable { showMinutePickerDirect = true }
                  .padding(horizontal = 8.dp, vertical = 2.dp)
              ) {
                Text(
                  text = String.format("%02d", minute),
                  style = MaterialTheme.typography.displayMedium,
                  fontWeight = FontWeight.Black,
                  color = TextPrimary
                )
              }
              IconButton(onClick = { minute = if (minute == 0) 59 else minute - 1 }) {
                Icon(Icons.Default.Remove, contentDescription = "Minute Down", tint = NeonCyan)
              }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // AM / PM Switcher
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (!isPm) ElectricViolet else DarkSurfaceElevated)
                  .clickable {
                    if (isPm) hour -= 12
                  }
                  .padding(horizontal = 12.dp, vertical = 6.dp)
              ) {
                Text(
                  "AM",
                  color = if (!isPm) TextPrimary else TextSecondary,
                  fontWeight = FontWeight.Bold
                )
              }
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (isPm) ElectricViolet else DarkSurfaceElevated)
                  .clickable {
                    if (!isPm) hour += 12
                  }
                  .padding(horizontal = 12.dp, vertical = 6.dp)
              ) {
                Text(
                  "PM",
                  color = if (isPm) TextPrimary else TextSecondary,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Minute slider for fast scrubbing across all 0..59 minutes
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 4.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                "Minute: ${String.format("%02d", minute)}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = NeonCyan
              )
              Text(
                "Drag to set any minute (0-59)",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
              )
            }
            Slider(
              value = minute.toFloat(),
              onValueChange = { minute = it.toInt().coerceIn(0, 59) },
              valueRange = 0f..59f,
              steps = 58,
              colors = SliderDefaults.colors(
                thumbColor = NeonCyan,
                activeTrackColor = NeonCyan,
                inactiveTrackColor = DarkSurfaceElevated
              ),
              modifier = Modifier.fillMaxWidth()
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          // Quick Minute Adjustment / Step row
          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              "Step:",
              style = MaterialTheme.typography.labelSmall,
              color = TextSecondary
            )
            AssistChip(
              onClick = { minute = if (minute == 0) 59 else minute - 1 },
              label = { Text("-1m") },
              colors = AssistChipDefaults.assistChipColors(
                containerColor = DarkSurfaceElevated,
                labelColor = NeonCyan
              )
            )
            AssistChip(
              onClick = { minute = (minute + 1) % 60 },
              label = { Text("+1m") },
              colors = AssistChipDefaults.assistChipColors(
                containerColor = DarkSurfaceElevated,
                labelColor = NeonCyan
              )
            )
            AssistChip(
              onClick = { minute = (minute + 5) % 60 },
              label = { Text("+5m") },
              colors = AssistChipDefaults.assistChipColors(
                containerColor = DarkSurfaceElevated,
                labelColor = TextSecondary
              )
            )
            AssistChip(
              onClick = { minute = 0 },
              label = { Text(":00") },
              colors = AssistChipDefaults.assistChipColors(
                containerColor = DarkSurfaceElevated,
                labelColor = TextSecondary
              )
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Alarm Name Input
      OutlinedTextField(
        value = name,
        onValueChange = { name = it },
        label = { Text("Alarm Label") },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = NeonCyan,
          unfocusedBorderColor = DarkSurfaceBorder,
          focusedTextColor = TextPrimary,
          unfocusedTextColor = TextPrimary,
          focusedLabelColor = NeonCyan
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(20.dp))

      // Repeat Days Section
      Text("REPEAT", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
      Spacer(modifier = Modifier.height(8.dp))

      // Quick repeat presets
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        FilterChip(
          selected = selectedDays.isEmpty(),
          onClick = { selectedDays.clear() },
          label = { Text("Once") },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = ElectricViolet,
            selectedLabelColor = TextPrimary
          )
        )
        FilterChip(
          selected = selectedDays.size == 7,
          onClick = {
            selectedDays.clear()
            selectedDays.addAll(1..7)
          },
          label = { Text("Every day") },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = ElectricViolet,
            selectedLabelColor = TextPrimary
          )
        )
        FilterChip(
          selected = selectedDays.size == 5 &&
              selectedDays.containsAll(listOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY)),
          onClick = {
            selectedDays.clear()
            selectedDays.addAll(listOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY))
          },
          label = { Text("Mon–Fri") },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = ElectricViolet,
            selectedLabelColor = TextPrimary
          )
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Individual Day Chips
      val dayLabels = listOf(
        Pair(Calendar.SUNDAY, "S"),
        Pair(Calendar.MONDAY, "M"),
        Pair(Calendar.TUESDAY, "T"),
        Pair(Calendar.WEDNESDAY, "W"),
        Pair(Calendar.THURSDAY, "T"),
        Pair(Calendar.FRIDAY, "F"),
        Pair(Calendar.SATURDAY, "S")
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        dayLabels.forEach { (calDay, label) ->
          val isSelected = selectedDays.contains(calDay)
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(CircleShape)
              .background(if (isSelected) NeonCyan else DarkSurface)
              .border(
                1.5.dp,
                if (isSelected) NeonCyan else DarkSurfaceBorder,
                CircleShape
              )
              .clickable {
                if (isSelected) selectedDays.remove(calDay) else selectedDays.add(calDay)
              },
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = label,
              fontWeight = FontWeight.Bold,
              color = if (isSelected) DeepNavy else TextSecondary
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Challenge Section Header
      Text(
        text = "WAKE CHALLENGE SETTINGS",
        style = MaterialTheme.typography.labelLarge,
        color = NeonCyan,
        fontWeight = FontWeight.Black
      )
      Spacer(modifier = Modifier.height(8.dp))

      // Challenge Type selector
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        ChallengeType.entries.forEach { type ->
          FilterChip(
            selected = challengeType == type.id,
            onClick = { challengeType = type.id },
            label = { Text(type.title) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = ElectricViolet,
              selectedLabelColor = TextPrimary,
              containerColor = DarkSurface
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Difficulty & Count row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text("Difficulty", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
          Spacer(modifier = Modifier.height(4.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Difficulty.entries.forEach { diff ->
              FilterChip(
                selected = difficulty == diff.id,
                onClick = { difficulty = diff.id },
                label = { Text(diff.label) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = NeonCyan,
                  selectedLabelColor = DeepNavy
                )
              )
            }
          }
        }

        Column(horizontalAlignment = Alignment.End) {
          Text("Challenges", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
          Spacer(modifier = Modifier.height(4.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(1, 2, 3).forEach { count ->
              FilterChip(
                selected = challengeCount == count,
                onClick = { challengeCount = count },
                label = { Text("$count") },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = ElectricViolet,
                  selectedLabelColor = TextPrimary
                )
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Preview Challenge Button
      OutlinedButton(
        onClick = {
          soundPlayer.stop()
          onPreviewChallenge(
            ChallengeType.fromId(challengeType),
            Difficulty.fromId(difficulty)
          )
        },
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(NeonCyan)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Test Challenge (${ChallengeType.fromId(challengeType).title})", fontWeight = FontWeight.Bold)
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Sound & Vibration
      Text("SOUND & HAPTICS", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
      Spacer(modifier = Modifier.height(8.dp))

      // Sound Selector & Preview
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.weight(1f)
        ) {
          SoundType.entries.forEach { st ->
            FilterChip(
              selected = selectedSound == st.id,
              onClick = {
                selectedSound = st.id
                if (isPreviewPlaying) {
                  soundPlayer.start(st.id, volume.toInt(), isVibrate = false, isGradualVolume = false)
                }
              },
              label = { Text(st.label) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = ElectricViolet,
                selectedLabelColor = TextPrimary
              )
            )
          }
        }

        IconButton(
          onClick = {
            if (isPreviewPlaying) {
              soundPlayer.stop()
              isPreviewPlaying = false
            } else {
              soundPlayer.start(selectedSound, volume.toInt(), isVibrate = false, isGradualVolume = false)
              isPreviewPlaying = true
            }
          },
          modifier = Modifier
            .clip(CircleShape)
            .background(if (isPreviewPlaying) DangerRed else ElectricViolet)
        ) {
          Icon(
            imageVector = if (isPreviewPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
            contentDescription = "Preview Sound",
            tint = TextPrimary
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Volume Slider
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = TextSecondary)
        Spacer(modifier = Modifier.width(12.dp))
        Slider(
          value = volume,
          onValueChange = { volume = it },
          valueRange = 10f..100f,
          colors = SliderDefaults.colors(
            thumbColor = NeonCyan,
            activeTrackColor = NeonCyan,
            inactiveTrackColor = DarkSurfaceBorder
          ),
          modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("${volume.toInt()}%", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
      }

      // Vibration & Gradual volume switches
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Vibration", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
        Switch(
          checked = isVibrate,
          onCheckedChange = { isVibrate = it },
          colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan, checkedTrackColor = ElectricViolet)
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Gradual Volume Increase", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
        Switch(
          checked = isGradualVolume,
          onCheckedChange = { isGradualVolume = it },
          colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan, checkedTrackColor = ElectricViolet)
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Snooze Config
      Text("SNOOZE CONFIGURATION", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text("Snooze Duration", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
          Spacer(modifier = Modifier.height(4.dp))
          Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            listOf(1, 3, 5, 10, 15).forEach { mins ->
              FilterChip(
                selected = snoozeMinutes == mins,
                onClick = { snoozeMinutes = mins },
                label = { Text("$mins m") },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = EnergyAmber,
                  selectedLabelColor = DeepNavy
                )
              )
            }
          }
        }

        Column(horizontalAlignment = Alignment.End) {
          Text("Max Snoozes", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
          Spacer(modifier = Modifier.height(4.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(1, 3, 5).forEach { max ->
              FilterChip(
                selected = maxSnoozes == max,
                onClick = { maxSnoozes = max },
                label = { Text("$max") },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = EnergyAmber,
                  selectedLabelColor = DeepNavy
                )
              )
            }
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Require Challenge to Snooze", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
        Switch(
          checked = snoozeChallengeEnabled,
          onCheckedChange = { snoozeChallengeEnabled = it },
          colors = SwitchDefaults.colors(checkedThumbColor = EnergyAmber, checkedTrackColor = ElectricViolet)
        )
      }

      Spacer(modifier = Modifier.height(28.dp))

      // Save Button
      Button(
        onClick = {
          soundPlayer.stop()
          val updatedAlarm = (alarm ?: AlarmEntity(hour = hour, minute = minute)).copy(
            hour = hour,
            minute = minute,
            name = name.ifBlank { "UnikKlock Alarm" },
            daysOfWeek = selectedDays.joinToString(","),
            soundType = selectedSound,
            volume = volume.toInt(),
            isVibrate = isVibrate,
            isGradualVolume = isGradualVolume,
            difficulty = difficulty,
            challengeType = challengeType,
            challengeCount = challengeCount,
            snoozeMinutes = snoozeMinutes,
            maxSnoozeCount = maxSnoozes,
            snoozeChallengeEnabled = snoozeChallengeEnabled,
            isEnabled = true
          )
          onSave(updatedAlarm)
        },
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DeepNavy),
        modifier = Modifier
          .fillMaxWidth()
          .height(54.dp)
          .testTag("save_alarm_button")
      ) {
        Icon(Icons.Default.Check, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Save Alarm", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }

  if (showMinutePickerDirect) {
    var textInput by remember { mutableStateOf(minute.toString()) }
    AlertDialog(
      onDismissRequest = { showMinutePickerDirect = false },
      containerColor = DarkSurfaceElevated,
      titleContentColor = TextPrimary,
      title = { Text("Set Minute (0 - 59)", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text("Enter exact minute from 0 to 59:", color = TextSecondary)
          OutlinedTextField(
            value = textInput,
            onValueChange = { input ->
              if (input.all { it.isDigit() } && input.length <= 2) {
                textInput = input
              }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = NeonCyan,
              unfocusedBorderColor = DarkSurfaceBorder,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            )
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val parsed = textInput.toIntOrNull()
            if (parsed != null && parsed in 0..59) {
              minute = parsed
            }
            showMinutePickerDirect = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DeepNavy)
        ) {
          Text("Set", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showMinutePickerDirect = false }) {
          Text("Cancel", color = TextSecondary)
        }
      }
    )
  }
}
