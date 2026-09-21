package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class AppThemeMode(val title: String) {
  DARK("Dark"),
  LIGHT("Light"),
  SYSTEM("System")
}

enum class ClockColorMode(val label: String, val description: String) {
  NEON_RED("Neon Red", "Electric neon red digits (#FF204E)"),
  NEON_PURPLE("Neon Purple", "Vibrant neon purple digits (#DF20F0)"),
  NEON_DUO("Neon Duo", "Dual-tone neon red hours & neon purple minutes")
}

data class AppThemeColors(
  val background: Color,
  val surface: Color,
  val surfaceElevated: Color,
  val surfaceBorder: Color,
  val textPrimary: Color,
  val textSecondary: Color,
  val textTertiary: Color,
  val textDisabled: Color,
  val accentCyan: Color,
  val accentViolet: Color,
  val accentAmber: Color,
  val clockNeonRed: Color = NeonRed,
  val clockNeonPurple: Color = NeonPurple,
  val darkNavy: Color = CrimsonMidnight,
  val darkPlum: Color = CrimsonPlum,
  val darkWine: Color = CrimsonWine,
  val darkCrimson: Color = CrimsonNeon,
  val pastelMint: Color = PastelMintCyan,
  val pastelPeriwinkle: Color = PastelPeriwinkle,
  val pastelLavender: Color = PastelLavender,
  val pastelLilac: Color = PastelLilac,
  val isDark: Boolean
)

val LocalClockColorMode = compositionLocalOf { ClockColorMode.NEON_DUO }

val NeonClockShadow: Shadow
  @Composable get() = Shadow(
    color = AppTheme.colors.clockNeonRed.copy(alpha = 0.45f),
    blurRadius = 14f
  )

@Composable
fun neonClockHourColor(): Color {
  val colors = AppTheme.colors
  return when (LocalClockColorMode.current) {
    ClockColorMode.NEON_RED -> colors.clockNeonRed
    ClockColorMode.NEON_PURPLE -> colors.clockNeonPurple
    ClockColorMode.NEON_DUO -> colors.clockNeonRed
  }
}

@Composable
fun neonClockMinuteColor(): Color {
  val colors = AppTheme.colors
  return when (LocalClockColorMode.current) {
    ClockColorMode.NEON_RED -> colors.clockNeonRed
    ClockColorMode.NEON_PURPLE -> colors.clockNeonPurple
    ClockColorMode.NEON_DUO -> colors.clockNeonPurple
  }
}

@Composable
fun neonClockSeparatorColor(): Color {
  val colors = AppTheme.colors
  return when (LocalClockColorMode.current) {
    ClockColorMode.NEON_RED -> colors.clockNeonRed
    ClockColorMode.NEON_PURPLE -> colors.clockNeonPurple
    ClockColorMode.NEON_DUO -> colors.clockNeonPurple
  }
}

@Composable
fun neonClockPrimaryColor(): Color {
  val colors = AppTheme.colors
  return when (LocalClockColorMode.current) {
    ClockColorMode.NEON_RED -> colors.clockNeonRed
    ClockColorMode.NEON_PURPLE -> colors.clockNeonPurple
    ClockColorMode.NEON_DUO -> colors.clockNeonRed
  }
}

val LocalAppThemeColors = staticCompositionLocalOf {
  AppThemeColors(
    background = DeepNavy,
    surface = DarkSurface,
    surfaceElevated = DarkSurfaceElevated,
    surfaceBorder = DarkSurfaceBorder,
    textPrimary = TextPrimary,
    textSecondary = TextSecondary,
    textTertiary = TextTertiary,
    textDisabled = TextDisabled,
    accentCyan = CrimsonNeon,
    accentViolet = CrimsonWine,
    accentAmber = EnergyAmber,
    isDark = true
  )
}

object AppTheme {
  val colors: AppThemeColors
    @Composable
    get() = LocalAppThemeColors.current
}

private val WakeQuestDarkColorScheme = darkColorScheme(
  primary = CrimsonNeon,
  onPrimary = Color.White,
  primaryContainer = CrimsonWine,
  onPrimaryContainer = Color.White,
  secondary = CrimsonNeon,
  onSecondary = CrimsonMidnight,
  secondaryContainer = CrimsonPlum,
  onSecondaryContainer = TextPrimary,
  tertiary = CrimsonWine,
  onTertiary = Color.White,
  background = DeepNavy,
  onBackground = TextPrimary,
  surface = DarkSurface,
  onSurface = TextPrimary,
  surfaceVariant = DarkSurfaceElevated,
  onSurfaceVariant = TextSecondary,
  outline = DarkSurfaceBorder,
  error = DangerRed,
  onError = TextPrimary
)

private val WakeQuestLightColorScheme = lightColorScheme(
  primary = PastelLilacDeep,
  onPrimary = Color.White,
  primaryContainer = PastelPeriwinkle,
  onPrimaryContainer = PastelLilacDeep,
  secondary = PastelCyanDeep,
  onSecondary = Color.White,
  secondaryContainer = PastelMintCyan,
  onSecondaryContainer = LightTextPrimary,
  tertiary = PastelLavender,
  onTertiary = LightTextPrimary,
  background = PastelMintCyan,
  onBackground = LightTextPrimary,
  surface = Color.White,
  onSurface = LightTextPrimary,
  surfaceVariant = PastelPeriwinkle,
  onSurfaceVariant = LightTextSecondary,
  outline = PastelLavender,
  error = DangerRed,
  onError = Color.White
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) WakeQuestDarkColorScheme else WakeQuestLightColorScheme

  val appThemeColors = if (darkTheme) {
    AppThemeColors(
      background = DeepNavy,
      surface = DarkSurface,
      surfaceElevated = DarkSurfaceElevated,
      surfaceBorder = DarkSurfaceBorder,
      textPrimary = TextPrimary,
      textSecondary = TextSecondary,
      textTertiary = TextTertiary,
      textDisabled = TextDisabled,
      accentCyan = CrimsonNeon,
      accentViolet = CrimsonWine,
      accentAmber = EnergyAmber,
      clockNeonRed = NeonRed,
      clockNeonPurple = NeonPurple,
      isDark = true
    )
  } else {
    AppThemeColors(
      background = PastelMintCyan,
      surface = Color.White,
      surfaceElevated = PastelPeriwinkle,
      surfaceBorder = PastelLavender,
      textPrimary = LightTextPrimary,
      textSecondary = LightTextSecondary,
      textTertiary = LightTextTertiary,
      textDisabled = LightTextDisabled,
      accentCyan = PastelCyanDeep,
      accentViolet = PastelLilacDeep,
      accentAmber = EnergyAmber,
      clockNeonRed = Color(0xFFD61A46),
      clockNeonPurple = Color(0xFF9C27B0),
      isDark = false
    )
  }

  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        val barColor = (if (darkTheme) DeepNavy else LightBackground).toArgb()
        window.statusBarColor = barColor
        window.navigationBarColor = barColor
        val controller = WindowCompat.getInsetsController(window, view)
        controller.isAppearanceLightStatusBars = !darkTheme
        controller.isAppearanceLightNavigationBars = !darkTheme
      }
    }
  }

  CompositionLocalProvider(LocalAppThemeColors provides appThemeColors) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = Typography,
      content = content
    )
  }
}

