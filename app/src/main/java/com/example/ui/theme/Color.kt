package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// User Dark Palette (#FF204E, #A0153E, #5D0E41, #00224D)
val CrimsonNeon = Color(0xFFFF204E)        // #FF204E - Blazing vibrant crimson/coral red accent
val CrimsonWine = Color(0xFFA0153E)        // #A0153E - Rich wine/ruby red surface elevated & highlights
val CrimsonPlum = Color(0xFF5D0E41)        // #5D0E41 - Deep velvet plum surface/cards
val CrimsonMidnight = Color(0xFF00224D)    // #00224D - Midnight dark navy canvas background

// Dark Theme Palette - Powered by user dark palette
val DeepNavy = CrimsonMidnight             // Background canvas (#00224D)
val DarkSurface = CrimsonPlum              // Main surfaces / Cards (#5D0E41)
val DarkSurfaceElevated = CrimsonWine      // Elevated cards / Chips (#A0153E)
val DarkSurfaceBorder = Color(0xFF7A1440)  // Harmonized card/divider border

// User Pastel Palette (#DEFCF9, #CADEFC, #C3BEF0, #CCA8E9)
val PastelMintCyan = Color(0xFFDEFCF9)     // #DEFCF9 - Soft pale aqua/mint
val PastelPeriwinkle = Color(0xFFCADEFC)   // #CADEFC - Soft periwinkle blue
val PastelLavender = Color(0xFFC3BEF0)     // #C3BEF0 - Pastel lavender purple
val PastelLilac = Color(0xFFCCA8E9)        // #CCA8E9 - Pastel soft violet/orchid

// Deep accessible variants for text and high-contrast controls
val PastelLilacDeep = Color(0xFF7B42B8)     // Deep accessible violet for light theme
val PastelCyanDeep = Color(0xFF007C8A)      // Deep accessible teal/cyan for light theme

// Light Theme Palette - Using user pastel theme
val LightBackground = PastelMintCyan
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceElevated = PastelPeriwinkle
val LightSurfaceBorder = PastelLavender
val LightTextPrimary = Color(0xFF1F1A3A)    // High-contrast deep plum/navy
val LightTextSecondary = Color(0xFF4E476B)  // Elegant slate violet
val LightTextTertiary = Color(0xFF736B94)
val LightTextDisabled = Color(0xFFA099BD)

// Vibrant Accent Colors
val ElectricViolet = Color(0xFF8A53FF)
val ElectricVioletLight = Color(0xFFA77DFF)
val ElectricVioletDim = PastelLilacDeep
val NeonCyan = Color(0xFF00E5FF)
val NeonCyanDim = PastelCyanDeep
val EnergyAmber = Color(0xFFFFB300)
val EnergyOrange = Color(0xFFFF6D00)
val DangerRed = Color(0xFFFF3366)
val SuccessGreen = Color(0xFF00E676)

// Neon Clock Colors (Neon Red & Neon Purple)
val NeonRed = Color(0xFFFF204E)            // Electric vibrant neon red
val NeonPurple = Color(0xFFDF20F0)         // Electric vibrant neon purple / fuchsia
val NeonPurpleLight = Color(0xFFE040FB)    // Bright luminous violet highlight

// Default / Dark text
val TextPrimary = Color(0xFFFFF0F3)    // Clean warm porcelain white
val TextSecondary = Color(0xFFE2B2C2)  // Soft rose mist (accessible on plum & navy)
val TextTertiary = Color(0xFFA66F85)   // Muted dusty rose
val TextDisabled = Color(0xFF6E4057)   // Subdued plum wine
