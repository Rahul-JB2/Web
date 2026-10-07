package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Android 16 Glass & Cyber Cyan Palette
val GlassBackground = Color(0xFF070B12)
val GlassSurface = Color(0x99111927) // 60% opacity dark glass
val GlassSurfaceHighlight = Color(0x33FFFFFF)
val GlassBorder = Color(0x2EFFFFFF)
val GlassBorderCyan = Color(0x5500E5FF)

val CyanNeon = Color(0xFF00E5FF)
val CyanNeonDark = Color(0xFF00B4D8)
val MintNeon = Color(0xFF00F5D4)
val CobaltNeon = Color(0xFF4361EE)
val PurpleNeon = Color(0xFF7209B7)

// Aliases for Material Theme & legacy references
val CyanPrimary = Color(0xFF00E5FF)
val CyanOnPrimary = Color(0xFF00363D)
val CobaltSecondary = Color(0xFF4361EE)
val MintTertiary = Color(0xFF00F5D4)

val DarkBg = Color(0xFF070B14)
val DarkSurface = Color(0xFF0F172A)
val DarkSurfaceCard = Color(0xFF131D31)
val DarkSurfaceVariant = Color(0xFF1E293B)
val DarkBorder = Color(0xFF334155)

val LightBg = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF1F5F9)
val LightBorder = Color(0xFFE2E8F0)

// Signal Status
val StatusGreen = Color(0xFF10B981)
val StatusAmber = Color(0xFFF59E0B)
val StatusRed = Color(0xFFEF4444)

val SignalExcellent = Color(0xFF10B981)
val SignalGood = Color(0xFF00E5FF)
val SignalFair = Color(0xFFF59E0B)
val SignalPoor = Color(0xFFEF4444)

// Glass Gradients
val GlassCardBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xCC131D31),
        Color(0x990D1524)
    )
)

val GlassBorderBrush = Brush.linearGradient(
    colors = listOf(
        Color(0x66FFFFFF),
        Color(0x1A00E5FF),
        Color(0x0DFFFFFF)
    )
)

val CyanGlassGlowBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFF00E5FF),
        Color(0xFF00F5D4)
    )
)

val DarkMeshBackground = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF070B14),
        Color(0xFF0A101D),
        Color(0xFF05080E)
    )
)
