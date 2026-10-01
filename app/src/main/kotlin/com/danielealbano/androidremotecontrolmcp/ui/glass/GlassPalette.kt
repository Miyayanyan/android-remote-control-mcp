@file:Suppress("MagicNumber")

package com.danielealbano.androidremotecontrolmcp.ui.glass

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

/** One soft colour blob of the wallpaper, positioned in fractions of the wallpaper size. */
@Immutable
internal data class WallpaperBlob(
    val x: Float,
    val y: Float,
    val radiusFraction: Float,
    val color: Color,
)

/**
 * Colours of the pink/lilac liquid-glass look. The wallpaper is the only thing glass ever refracts:
 * each glass surface redraws the same wallpaper at its own position, so nothing outside the app
 * (games, other apps) is ever sampled.
 */
@Immutable
internal data class GlassPalette(
    val isDark: Boolean,
    val wallpaperTop: Color,
    val wallpaperMiddle: Color,
    val wallpaperBottom: Color,
    val wallpaperBlobs: List<WallpaperBlob>,
    val wallpaperStreak: Color,
    val clearFill: Color,
    val tintStart: Color,
    val tintEnd: Color,
    val dangerStart: Color,
    val dangerEnd: Color,
    val lensFill: Color,
    val innerDark: Color,
    val innerLightSoft: Color,
    val innerLightStrong: Color,
    val dropShadow: Color,
    val well: Color,
    val wellLine: Color,
    val grainAlpha: Float,
)

internal val DayGlassPalette =
    GlassPalette(
        isDark = false,
        wallpaperTop = Color(0xFFF2E9FF),
        wallpaperMiddle = Color(0xFFFBE7F2),
        wallpaperBottom = Color(0xFFECE6FF),
        wallpaperBlobs =
            listOf(
                WallpaperBlob(0.10f, 0.14f, 0.45f, Color(0xD9AA82FF)),
                WallpaperBlob(0.90f, 0.16f, 0.40f, Color(0xC7FF84C0)),
                WallpaperBlob(0.74f, 0.94f, 0.48f, Color(0xAD8CA0FF)),
                WallpaperBlob(0.20f, 0.90f, 0.42f, Color(0xD9FFAACE)),
                WallpaperBlob(0.52f, 0.46f, 0.32f, Color(0xF2E2C8FF)),
            ),
        wallpaperStreak = Color(0x6BFFFFFF),
        clearFill = Color(0x29FFFFFF),
        tintStart = Color(0xC77A52C4),
        tintEnd = Color(0xBDCC5696),
        dangerStart = Color(0xDBC42C54),
        dangerEnd = Color(0xD1DE4E6E),
        lensFill = Color(0x4DFFFFFF),
        innerDark = Color(0x1A461E64),
        innerLightSoft = Color(0x38FFFFFF),
        innerLightStrong = Color(0x66FFFFFF),
        dropShadow = Color(0x295A2C78),
        well = Color(0x6BFFFFFF),
        wellLine = Color(0xA6FFFFFF),
        grainAlpha = 0.07f,
    )

internal val NightGlassPalette =
    GlassPalette(
        isDark = true,
        wallpaperTop = Color(0xFF140E1B),
        wallpaperMiddle = Color(0xFF1D1227),
        wallpaperBottom = Color(0xFF110D1A),
        wallpaperBlobs =
            listOf(
                WallpaperBlob(0.10f, 0.14f, 0.45f, Color(0xC77C52D6)),
                WallpaperBlob(0.90f, 0.16f, 0.40f, Color(0x9EC4448C)),
                WallpaperBlob(0.74f, 0.94f, 0.48f, Color(0x94464AC4)),
                WallpaperBlob(0.20f, 0.90f, 0.42f, Color(0x8FAA3C7C)),
                WallpaperBlob(0.52f, 0.46f, 0.32f, Color(0x7A6E48B0)),
            ),
        wallpaperStreak = Color(0x21FFAADC),
        clearFill = Color(0x12FFFFFF),
        tintStart = Color(0xB3845CD6),
        tintEnd = Color(0xA8BE468C),
        dangerStart = Color(0xD6C82C58),
        dangerEnd = Color(0xCCE2466C),
        lensFill = Color(0x29FFFFFF),
        innerDark = Color(0x3D000000),
        innerLightSoft = Color(0x26FFFFFF),
        innerLightStrong = Color(0x3DFFFFFF),
        dropShadow = Color(0x73000000),
        well = Color(0x12FFFFFF),
        wellLine = Color(0x1AFFFFFF),
        grainAlpha = 0.10f,
    )

/** Fraction of the wallpaper diagonal along which the light streak runs, and its half-width. */
private const val STREAK_CENTER = 0.46f
private const val STREAK_HALF_WIDTH = 0.085f

/** Draws the full wallpaper for a window of [wallpaperSize] into the current draw scope. */
internal fun DrawScope.drawGlassWallpaper(
    palette: GlassPalette,
    wallpaperSize: Size,
) {
    val base =
        Brush.linearGradient(
            0f to palette.wallpaperTop,
            0.55f to palette.wallpaperMiddle,
            1f to palette.wallpaperBottom,
            start = Offset.Zero,
            end = Offset(wallpaperSize.width, wallpaperSize.height),
        )
    drawRect(base, size = wallpaperSize)
    val reference = wallpaperSize.maxDimension
    palette.wallpaperBlobs.forEach { blob ->
        val center = Offset(blob.x * wallpaperSize.width, blob.y * wallpaperSize.height)
        val radius = (blob.radiusFraction * reference).coerceAtLeast(1f)
        drawRect(
            Brush.radialGradient(listOf(blob.color, Color.Transparent), center = center, radius = radius),
            size = wallpaperSize,
        )
    }
    drawRect(
        Brush.linearGradient(
            (STREAK_CENTER - STREAK_HALF_WIDTH) to Color.Transparent,
            STREAK_CENTER to palette.wallpaperStreak,
            (STREAK_CENTER + STREAK_HALF_WIDTH) to Color.Transparent,
            start = Offset(0f, wallpaperSize.height),
            end = Offset(wallpaperSize.width, 0f),
        ),
        size = wallpaperSize,
    )
}
