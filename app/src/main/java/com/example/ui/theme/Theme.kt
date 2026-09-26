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
  NEON_RED("Radiant Gold", "Bright golden numbers (#FFCC00)"),
  NEON_PURPLE("Electric Violet", "Vibrant neon violet digits (#B13BFF)"),
  NEON_DUO("Gold & Violet Duo", "Dual-tone gold hours & neon violet minutes")
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
  val clockNeonRed: Color = NeonGold,
  val clockNeonPurple: Color = NeonViolet,
  val darkNavy: Color = MidnightObsidian,
  val darkPlum: Color = VelvetViolet,
  val darkWine: Color = DarkSurfaceElevated,
  val darkCrimson: Color = NeonViolet,
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
    ClockColorMode.NEON_DUO -> Color.White
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
    accentCyan = RadiantGold,
    accentViolet = NeonViolet,
    accentAmber = EnergyAmber,
    isDark = true
  )
}

object AppTheme {
  val colors: AppThemeColors
    @Composable
    get() = LocalAppThemeColors.current
}

private val UnikKlockDarkColorScheme = darkColorScheme(
  primary = RadiantGold,
  onPrimary = MidnightObsidian,
  primaryContainer = VelvetViolet,
  onPrimaryContainer = Color.White,
  secondary = NeonViolet,
  onSecondary = Color.White,
  secondaryContainer = DarkSurfaceElevated,
  onSecondaryContainer = Color.White,
  tertiary = RadiantGold,
  onTertiary = MidnightObsidian,
  background = MidnightObsidian,
  onBackground = Color.White,
  surface = VelvetViolet,
  onSurface = Color.White,
  surfaceVariant = DarkSurfaceElevated,
  onSurfaceVariant = Color.White,
  outline = DarkSurfaceBorder,
  error = DangerRed,
  onError = Color.White
)

private val UnikKlockLightColorScheme = lightColorScheme(
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
  val colorScheme = if (darkTheme) UnikKlockDarkColorScheme else UnikKlockLightColorScheme

  val appThemeColors = if (darkTheme) {
    AppThemeColors(
      background = MidnightObsidian,
      surface = VelvetViolet,
      surfaceElevated = DarkSurfaceElevated,
      surfaceBorder = DarkSurfaceBorder,
      textPrimary = Color.White,
      textSecondary = Color.White,
      textTertiary = Color(0xFFF3E8FF),
      textDisabled = Color(0xFFD4BFFF),
      accentCyan = RadiantGold,
      accentViolet = NeonViolet,
      accentAmber = RadiantGold,
      clockNeonRed = RadiantGold,
      clockNeonPurple = NeonViolet,
      darkNavy = MidnightObsidian,
      darkPlum = VelvetViolet,
      darkWine = DarkSurfaceElevated,
      darkCrimson = NeonViolet,
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
        val barColor = (if (darkTheme) MidnightObsidian else LightBackground).toArgb()
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
