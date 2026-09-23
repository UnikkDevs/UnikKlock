package com.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.example.alarm.AlarmScheduler
import com.example.data.model.AlarmEntity
import com.example.data.model.ChallengeType
import com.example.data.model.Difficulty
import com.example.ui.components.ChallengePreviewDialog
import com.example.ui.components.OnboardingDialog
import com.example.ui.screens.AlarmEditSheet
import com.example.ui.screens.AlarmsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatisticsScreen
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.CompositionLocalProvider
import com.example.ui.screens.ClockToolsScreen
import com.example.ui.theme.AppTheme
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.ClockColorMode
import com.example.ui.theme.LocalClockColorMode
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel

enum class NavigationTab(val label: String, val icon: ImageVector) {
  DASHBOARD("Home", Icons.Default.Home),
  ALARMS("Alarms", Icons.Default.Alarm),
  CLOCK_TOOLS("Clock", Icons.Default.Schedule),
  STATS("Stats", Icons.Default.BarChart),
  SETTINGS("Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {
  private val viewModel: MainViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      val context = LocalContext.current
      val sharedPrefs = remember { context.getSharedPreferences("wakequest_prefs", Context.MODE_PRIVATE) }
      val initialThemeStr = remember {
        sharedPrefs.getString("theme_mode", AppThemeMode.DARK.name) ?: AppThemeMode.DARK.name
      }
      var themeMode by remember {
        mutableStateOf(
          try {
            AppThemeMode.valueOf(initialThemeStr)
          } catch (e: Exception) {
            AppThemeMode.DARK
          }
        )
      }

      val initialClockColorStr = remember {
        sharedPrefs.getString("clock_color_mode", ClockColorMode.NEON_DUO.name) ?: ClockColorMode.NEON_DUO.name
      }
      var clockColorMode by remember {
        mutableStateOf(
          try {
            ClockColorMode.valueOf(initialClockColorStr)
          } catch (e: Exception) {
            ClockColorMode.NEON_DUO
          }
        )
      }

      val systemIsDark = isSystemInDarkTheme()
      val isDarkTheme = when (themeMode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> systemIsDark
      }

      MyApplicationTheme(darkTheme = isDarkTheme) {
        CompositionLocalProvider(LocalClockColorMode provides clockColorMode) {
          WakeQuestApp(
            viewModel = viewModel,
            currentThemeMode = themeMode,
            currentClockColorMode = clockColorMode,
            isDarkTheme = isDarkTheme,
            onThemeModeChanged = { newMode ->
              themeMode = newMode
              sharedPrefs.edit().putString("theme_mode", newMode.name).apply()
            },
            onClockColorModeChanged = { newClockMode ->
              clockColorMode = newClockMode
              sharedPrefs.edit().putString("clock_color_mode", newClockMode.name).apply()
            }
          )
        }
      }
    }
  }
}

@Composable
fun WakeQuestApp(
  viewModel: MainViewModel,
  currentThemeMode: AppThemeMode = AppThemeMode.DARK,
  currentClockColorMode: ClockColorMode = ClockColorMode.NEON_DUO,
  isDarkTheme: Boolean = true,
  onThemeModeChanged: (AppThemeMode) -> Unit = {},
  onClockColorModeChanged: (ClockColorMode) -> Unit = {}
) {
  val context = LocalContext.current
  val alarms by viewModel.alarms.collectAsState()
  val stats by viewModel.stats.collectAsState()
  val history by viewModel.history.collectAsState()
  val colors = AppTheme.colors

  var currentTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }

  // First launch onboarding check
  val sharedPrefs = remember { context.getSharedPreferences("wakequest_prefs", Context.MODE_PRIVATE) }
  var showOnboarding by remember {
    mutableStateOf(!sharedPrefs.getBoolean("onboarding_completed", false))
  }

  // Edit / Create alarm sheet state
  var editingAlarm by remember { mutableStateOf<AlarmEntity?>(null) }
  var isEditSheetOpen by remember { mutableStateOf(false) }

  // Practice / Preview challenge dialog state
  var isPracticeDialogOpen by remember { mutableStateOf(false) }
  var practiceChallengeType by remember { mutableStateOf(ChallengeType.MATH) }
  var practiceDifficulty by remember { mutableStateOf(Difficulty.MEDIUM) }

  fun triggerQuickTestAlarm() {
    val testAlarm = AlarmEntity(
      id = 999999L,
      hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY),
      minute = java.util.Calendar.getInstance().get(java.util.Calendar.MINUTE),
      name = "⚡ Quick Test Alarm",
      isEnabled = true,
      difficulty = Difficulty.MEDIUM.id,
      challengeType = ChallengeType.RANDOM.id,
      challengeCount = 1
    )
    AlarmScheduler.scheduleTestAlarmInSeconds(context, testAlarm, seconds = 5)
    Toast.makeText(
      context,
      "Test alarm scheduled in 5 seconds! Lock screen or wait to test.",
      Toast.LENGTH_LONG
    ).show()
  }

  Scaffold(
    containerColor = colors.background,
    contentWindowInsets = WindowInsets.systemBars,
    bottomBar = {
      NavigationBar(
        containerColor = colors.surface,
        modifier = Modifier.testTag("main_bottom_nav")
      ) {
        NavigationTab.entries.forEach { tab ->
          val isSelected = currentTab == tab
          NavigationBarItem(
            selected = isSelected,
            onClick = { currentTab = tab },
            icon = {
              Icon(
                imageVector = tab.icon,
                contentDescription = tab.label
              )
            },
            label = { Text(tab.label) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = Color.White,
              selectedTextColor = Color.White,
              unselectedIconColor = Color.White.copy(alpha = 0.7f),
              unselectedTextColor = Color.White.copy(alpha = 0.7f),
              indicatorColor = colors.accentViolet.copy(alpha = 0.45f)
            ),
            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
          )
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      Crossfade(
        targetState = currentTab,
        label = "tab_crossfade"
      ) { tab ->
        when (tab) {
          NavigationTab.DASHBOARD -> {
            DashboardScreen(
              alarms = alarms,
              stats = stats,
              onToggleAlarm = { viewModel.toggleAlarm(it) },
              onAddAlarmClick = {
                editingAlarm = null
                isEditSheetOpen = true
              },
              onEditAlarmClick = {
                editingAlarm = it
                isEditSheetOpen = true
              },
              onTestChallengeClick = {
                practiceChallengeType = ChallengeType.MATH
                practiceDifficulty = Difficulty.MEDIUM
                isPracticeDialogOpen = true
              },
              onQuickTestAlarmClick = {
                triggerQuickTestAlarm()
              },
              onToggleTheme = {
                val next = if (isDarkTheme) AppThemeMode.LIGHT else AppThemeMode.DARK
                onThemeModeChanged(next)
              },
              isDarkTheme = isDarkTheme
            )
          }

          NavigationTab.ALARMS -> {
            AlarmsScreen(
              alarms = alarms,
              onToggleAlarm = { viewModel.toggleAlarm(it) },
              onEditAlarm = {
                editingAlarm = it
                isEditSheetOpen = true
              },
              onDeleteAlarm = { viewModel.deleteAlarm(it) },
              onAddAlarm = {
                editingAlarm = null
                isEditSheetOpen = true
              }
            )
          }

          NavigationTab.CLOCK_TOOLS -> {
            ClockToolsScreen(
              onNavigateToAlarmEdit = { hour, minute ->
                editingAlarm = AlarmEntity(
                  hour = hour,
                  minute = minute,
                  name = "Optimal Sleep Alarm",
                  isEnabled = true,
                  difficulty = Difficulty.MEDIUM.id,
                  challengeType = ChallengeType.RANDOM.id
                )
                isEditSheetOpen = true
              },
              onToggleTheme = {
                val next = if (isDarkTheme) AppThemeMode.LIGHT else AppThemeMode.DARK
                onThemeModeChanged(next)
              },
              isDarkTheme = isDarkTheme
            )
          }

          NavigationTab.STATS -> {
            StatisticsScreen(
              stats = stats,
              history = history,
              onClearHistory = { viewModel.clearHistory() }
            )
          }

          NavigationTab.SETTINGS -> {
            SettingsScreen(
              currentThemeMode = currentThemeMode,
              onThemeModeChanged = onThemeModeChanged,
              currentClockColorMode = currentClockColorMode,
              onClockColorModeChanged = onClockColorModeChanged
            )
          }
        }
      }
    }
  }

  // Create / Edit Alarm Sheet
  if (isEditSheetOpen) {
    AlarmEditSheet(
      alarm = editingAlarm,
      onDismiss = { isEditSheetOpen = false },
      onSave = { updated ->
        viewModel.saveAlarm(updated)
        isEditSheetOpen = false
        Toast.makeText(context, "Alarm saved successfully", Toast.LENGTH_SHORT).show()
      },
      onPreviewChallenge = { type, diff ->
        practiceChallengeType = type
        practiceDifficulty = diff
        isPracticeDialogOpen = true
      }
    )
  }

  // Practice / Preview Challenge Dialog
  if (isPracticeDialogOpen) {
    ChallengePreviewDialog(
      initialType = practiceChallengeType,
      initialDifficulty = practiceDifficulty,
      onDismiss = { isPracticeDialogOpen = false }
    )
  }

  // First-launch Onboarding
  if (showOnboarding) {
    OnboardingDialog(
      onDismiss = {
        sharedPrefs.edit().putBoolean("onboarding_completed", true).apply()
        showOnboarding = false
      }
    )
  }
}
