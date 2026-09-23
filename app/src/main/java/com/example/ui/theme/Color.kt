package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// =========================================================================
// Palette: https://colorhunt.co/palette/090040471396b13bffffcc00
// #090040 - Midnight Obsidian Blue (Canvas Background)
// #471396 - Royal Velvet Violet (Cards, Containers, Surfaces)
// #B13BFF - Electric Neon Violet (Elevated cards, active highlights, badges)
// #FFCC00 - Bright Radiant Gold / Amber (Accents, active pills, indicators, stars)
// =========================================================================

val MidnightObsidian = Color(0xFF090040) // #090040 - Main background canvas
val VelvetViolet = Color(0xFF471396)     // #471396 - Main cards & surface components
val NeonViolet = Color(0xFFB13BFF)       // #B13BFF - Elevated surfaces, outlines & glowing accents
val RadiantGold = Color(0xFFFFCC00)      // #FFCC00 - Radiant energetic gold accent

// Dark Theme Surfaces & Dividers
val DeepNavy = MidnightObsidian
val DarkSurface = VelvetViolet
val DarkSurfaceElevated = Color(0xFF5A1CBA) // Rich luminous violet for elevated surfaces
val DarkSurfaceBorder = Color(0xFF7028E4)   // Harmonized crisp violet border

// User Pastel Palette for Light Theme
val PastelMintCyan = Color(0xFFDEFCF9)     // #DEFCF9
val PastelPeriwinkle = Color(0xFFCADEFC)   // #CADEFC
val PastelLavender = Color(0xFFC3BEF0)     // #C3BEF0
val PastelLilac = Color(0xFFCCA8E9)        // #CCA8E9

// Deep accessible variants for text and high-contrast controls in Light Theme
val PastelLilacDeep = Color(0xFF7B42B8)
val PastelCyanDeep = Color(0xFF007C8A)

// Light Theme Palette
val LightBackground = PastelMintCyan
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceElevated = PastelPeriwinkle
val LightSurfaceBorder = PastelLavender
val LightTextPrimary = Color(0xFF1F1A3A)
val LightTextSecondary = Color(0xFF4E476B)
val LightTextTertiary = Color(0xFF736B94)
val LightTextDisabled = Color(0xFFA099BD)

// Vibrant Accent Colors
val ElectricViolet = NeonViolet
val ElectricVioletLight = Color(0xFFC96BFF)
val ElectricVioletDim = VelvetViolet
val NeonCyan = RadiantGold                 // Highlight accent mapped to Radiant Gold
val NeonCyanDim = Color(0xFFE6B800)
val EnergyAmber = RadiantGold              // #FFCC00
val EnergyOrange = Color(0xFFFF9500)
val DangerRed = Color(0xFFFF3366)
val SuccessGreen = Color(0xFF00E676)

// Clock Neon Colors
val NeonGold = RadiantGold                 // #FFCC00
val NeonVioletGlow = NeonViolet            // #B13BFF
val NeonRed = RadiantGold
val NeonPurple = NeonViolet

// Pure Crisp White for All Text and Icons per user specification
val TextPrimary = Color(0xFFFFFFFF)        // Pure crisp white (#FFFFFF)
val TextSecondary = Color(0xFFF0E6FF)      // Pristine bright soft white (#F0E6FF)
val TextTertiary = Color(0xFFD4BFFF)       // High-legibility lavender white (#D4BFFF)
val TextDisabled = Color(0xFF9E84D4)       // Muted legible violet-tinted white
