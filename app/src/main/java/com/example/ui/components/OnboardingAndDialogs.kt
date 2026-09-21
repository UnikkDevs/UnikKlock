package com.example.ui.components

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.alarm.AlarmScheduler
import com.example.data.model.ChallengeType
import com.example.data.model.Difficulty
import com.example.games.ui.MathChallengeComposable
import com.example.games.ui.MemoryCardsComposable
import com.example.games.ui.MemorySequenceComposable
import com.example.games.ui.PatternChallengeComposable
import com.example.games.ui.QuickTapComposable
import com.example.games.ui.ReactionChallengeComposable
import com.example.games.ui.WordScrambleComposable
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EnergyAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun OnboardingDialog(
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  var hasNotificationPermission by remember { mutableStateOf(true) }

  val notificationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    hasNotificationPermission = isGranted
  }

  AlertDialog(
    onDismissRequest = { /* Force explicit button tap */ },
    containerColor = DarkSurfaceElevated,
    titleContentColor = TextPrimary,
    shape = RoundedCornerShape(24.dp),
    modifier = Modifier.padding(12.dp),
    title = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(ElectricViolet.copy(alpha = 0.2f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.Bolt, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(32.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
          text = "WakeQuest",
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Black,
          color = TextPrimary
        )
        Text(
          text = "“Wake up. Think. Conquer.”",
          style = MaterialTheme.typography.bodySmall,
          color = NeonCyan,
          fontWeight = FontWeight.Bold
        )
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Text(
          text = "Don’t just wake up. Wake your brain.",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = EnergyAmber,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth()
        )

        Text(
          text = "WakeQuest eliminates the temptation of mindless snooze. When an alarm rings, the only way to silence it is to complete your mental challenge.",
          style = MaterialTheme.typography.bodyMedium,
          color = TextSecondary,
          textAlign = TextAlign.Center
        )

        // Features list
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          FeatureItem(icon = "🧠", title = "7 Mini-Games", desc = "Math, Memory, Patterns, Reflex, Words & more")
          FeatureItem(icon = "⏰", title = "Reliable Exact Alarms", desc = "Rings even if phone is locked, idle or restarted")
          FeatureItem(icon = "🛡️", title = "Anti-Cheat Guard", desc = "No accidental dismiss buttons")
        }

        // Exact Alarm permission hint if not enabled
        if (!AlarmScheduler.canScheduleExactAlarms(context) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
          Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp)
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("⚠️", fontSize = 16.sp)
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Exact alarm scheduling requires Android permission.",
                style = MaterialTheme.typography.bodySmall,
                color = EnergyAmber
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
          }
          if (!AlarmScheduler.canScheduleExactAlarms(context) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
              data = Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
          }
          onDismiss()
        },
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DeepNavy),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("onboarding_continue_button")
      ) {
        Text("Get Started", fontWeight = FontWeight.Bold, fontSize = 16.sp)
      }
    }
  )
}

@Composable
private fun FeatureItem(icon: String, title: String, desc: String) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Text(icon, fontSize = 18.sp)
    Spacer(modifier = Modifier.width(10.dp))
    Column {
      Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
      Text(desc, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
    }
  }
}

// -------------------------------------------------------------------------
// CHALLENGE PREVIEW / PRACTICE DIALOG
// -------------------------------------------------------------------------
@Composable
fun ChallengePreviewDialog(
  initialType: ChallengeType = ChallengeType.MATH,
  initialDifficulty: Difficulty = Difficulty.MEDIUM,
  onDismiss: () -> Unit
) {
  var selectedType by remember { mutableStateOf(initialType) }
  var selectedDiff by remember { mutableStateOf(initialDifficulty) }
  var isSolved by remember { mutableStateOf(false) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      colors = CardDefaults.cardColors(containerColor = DeepNavy),
      shape = RoundedCornerShape(24.dp),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ElectricViolet)),
      modifier = Modifier
        .fillMaxWidth(0.94f)
        .padding(16.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "PRACTICE CHALLENGE",
              style = MaterialTheme.typography.labelMedium,
              color = NeonCyan,
              fontWeight = FontWeight.Black
            )
            Text(
              text = selectedType.title,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (isSolved) {
          Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text("🎉", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "CHALLENGE SOLVED!",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Black,
              color = SuccessGreen
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Great job! This is how alarms are silenced.",
              style = MaterialTheme.typography.bodyMedium,
              color = TextSecondary,
              textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(18.dp))
            Button(
              onClick = { isSolved = false },
              colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet)
            ) {
              Text("Try Again")
            }
          }
        } else {
          when (selectedType) {
            ChallengeType.MATH -> {
              MathChallengeComposable(
                difficulty = selectedDiff,
                onCompleted = { isSolved = true }
              )
            }
            ChallengeType.MEMORY_SEQUENCE -> {
              MemorySequenceComposable(
                difficulty = selectedDiff,
                onCompleted = { isSolved = true }
              )
            }
            ChallengeType.PATTERN -> {
              PatternChallengeComposable(
                difficulty = selectedDiff,
                onCompleted = { isSolved = true }
              )
            }
            ChallengeType.REACTION -> {
              ReactionChallengeComposable(
                difficulty = selectedDiff,
                onCompleted = { isSolved = true }
              )
            }
            ChallengeType.QUICK_TAP -> {
              QuickTapComposable(
                difficulty = selectedDiff,
                onCompleted = { isSolved = true }
              )
            }
            ChallengeType.MEMORY_CARDS -> {
              MemoryCardsComposable(
                difficulty = selectedDiff,
                onCompleted = { isSolved = true }
              )
            }
            ChallengeType.WORD_SCRAMBLE -> {
              WordScrambleComposable(
                difficulty = selectedDiff,
                onCompleted = { isSolved = true }
              )
            }
            else -> {
              MathChallengeComposable(
                difficulty = selectedDiff,
                onCompleted = { isSolved = true }
              )
            }
          }
        }
      }
    }
  }
}
