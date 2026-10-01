@file:Suppress("MagicNumber")

package com.danielealbano.androidremotecontrolmcp.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Miya pink/lilac palette for the liquid-glass UI. `surface` and `background` are transparent so the
// glass wallpaper (drawn by the theme) shows behind scaffolds, top bars and list items; the
// surfaceContainer* roles stay opaque so dialogs, menus and switch tracks remain fully readable.

// Primary: lilac
val PrimaryLight = Color(0xFF6A47AE)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFEADFFF)
val OnPrimaryContainerLight = Color(0xFF2C1F36)

// Secondary: pink
val SecondaryLight = Color(0xFFB5487F)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFFFE0EE)
val OnSecondaryContainerLight = Color(0xFF3A1028)

// Tertiary: safe green (running / healthy states)
val TertiaryLight = Color(0xFF2C7A58)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFCDEFD9)
val OnTertiaryContainerLight = Color(0xFF002113)

// Error: berry red
val ErrorLight = Color(0xFFB5375C)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFFD9E2)
val OnErrorContainerLight = Color(0xFF3E0018)

// Surface and background
val SurfaceLight = Color.Transparent
val OnSurfaceLight = Color(0xFF2C1F36)
val SurfaceVariantLight = Color(0xFFF1E6F8)
val OnSurfaceVariantLight = Color(0xFF5E4F6B)
val OutlineLight = Color(0xFF8B7A98)
val OutlineVariantLight = Color(0xFFD9CBE3)
val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
val SurfaceContainerLowLight = Color(0xFFFBF6FE)
val SurfaceContainerLight = Color(0xFFF6EEFC)
val SurfaceContainerHighLight = Color(0xFFF2E8FA)
val SurfaceContainerHighestLight = Color(0xFFEEE2F7)

// Dark theme
val PrimaryDark = Color(0xFFD4BFFF)
val OnPrimaryDark = Color(0xFF3A1F6E)
val PrimaryContainerDark = Color(0xFF523A8A)
val OnPrimaryContainerDark = Color(0xFFEFE4FF)

val SecondaryDark = Color(0xFFFF98C4)
val OnSecondaryDark = Color(0xFF5A1137)
val SecondaryContainerDark = Color(0xFF7A2D55)
val OnSecondaryContainerDark = Color(0xFFFFD9E8)

val TertiaryDark = Color(0xFF8FDBB8)
val OnTertiaryDark = Color(0xFF003824)
val TertiaryContainerDark = Color(0xFF155239)
val OnTertiaryContainerDark = Color(0xFFABF7D3)

val ErrorDark = Color(0xFFFF98B4)
val OnErrorDark = Color(0xFF5E1130)
val ErrorContainerDark = Color(0xFF8C2346)
val OnErrorContainerDark = Color(0xFFFFD9E2)

val SurfaceDark = Color.Transparent
val OnSurfaceDark = Color(0xFFF7EFFB)
val SurfaceVariantDark = Color(0xFF3A2E45)
val OnSurfaceVariantDark = Color(0xFFC9B9D2)
val OutlineDark = Color(0xFF9C8AA8)
val OutlineVariantDark = Color(0xFF4A3D55)
val SurfaceContainerLowestDark = Color(0xFF120D18)
val SurfaceContainerLowDark = Color(0xFF1E1626)
val SurfaceContainerDark = Color(0xFF241B2D)
val SurfaceContainerHighDark = Color(0xFF2E2438)
val SurfaceContainerHighestDark = Color(0xFF382D43)

/** Amber used for advisory warnings (yellow triangle). ARGB 0xFFF9A825. */
val WarningAmber = Color(0xFFF9A825)

val LightColorScheme =
    lightColorScheme(
        primary = PrimaryLight,
        onPrimary = OnPrimaryLight,
        primaryContainer = PrimaryContainerLight,
        onPrimaryContainer = OnPrimaryContainerLight,
        secondary = SecondaryLight,
        onSecondary = OnSecondaryLight,
        secondaryContainer = SecondaryContainerLight,
        onSecondaryContainer = OnSecondaryContainerLight,
        tertiary = TertiaryLight,
        onTertiary = OnTertiaryLight,
        tertiaryContainer = TertiaryContainerLight,
        onTertiaryContainer = OnTertiaryContainerLight,
        error = ErrorLight,
        onError = OnErrorLight,
        errorContainer = ErrorContainerLight,
        onErrorContainer = OnErrorContainerLight,
        background = SurfaceLight,
        onBackground = OnSurfaceLight,
        surface = SurfaceLight,
        onSurface = OnSurfaceLight,
        surfaceVariant = SurfaceVariantLight,
        onSurfaceVariant = OnSurfaceVariantLight,
        outline = OutlineLight,
        outlineVariant = OutlineVariantLight,
        surfaceContainerLowest = SurfaceContainerLowestLight,
        surfaceContainerLow = SurfaceContainerLowLight,
        surfaceContainer = SurfaceContainerLight,
        surfaceContainerHigh = SurfaceContainerHighLight,
        surfaceContainerHighest = SurfaceContainerHighestLight,
    )

val DarkColorScheme =
    darkColorScheme(
        primary = PrimaryDark,
        onPrimary = OnPrimaryDark,
        primaryContainer = PrimaryContainerDark,
        onPrimaryContainer = OnPrimaryContainerDark,
        secondary = SecondaryDark,
        onSecondary = OnSecondaryDark,
        secondaryContainer = SecondaryContainerDark,
        onSecondaryContainer = OnSecondaryContainerDark,
        tertiary = TertiaryDark,
        onTertiary = OnTertiaryDark,
        tertiaryContainer = TertiaryContainerDark,
        onTertiaryContainer = OnTertiaryContainerDark,
        error = ErrorDark,
        onError = OnErrorDark,
        errorContainer = ErrorContainerDark,
        onErrorContainer = OnErrorContainerDark,
        background = SurfaceDark,
        onBackground = OnSurfaceDark,
        surface = SurfaceDark,
        onSurface = OnSurfaceDark,
        surfaceVariant = SurfaceVariantDark,
        onSurfaceVariant = OnSurfaceVariantDark,
        outline = OutlineDark,
        outlineVariant = OutlineVariantDark,
        surfaceContainerLowest = SurfaceContainerLowestDark,
        surfaceContainerLow = SurfaceContainerLowDark,
        surfaceContainer = SurfaceContainerDark,
        surfaceContainerHigh = SurfaceContainerHighDark,
        surfaceContainerHighest = SurfaceContainerHighestDark,
    )
