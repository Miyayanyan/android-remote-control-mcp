package com.danielealbano.androidremotecontrolmcp.ui.glass

import com.danielealbano.androidremotecontrolmcp.data.model.ServerStatus

/**
 * Size-dependent parameters of one glass surface, in dp (or plain factors).
 *
 * The reference recipe (JoyTaung, "iOS 26 Liquid Glass Simulation", Figma Community) is tuned for an
 * 80 px tall button; [GlassPolicy.metricsFor] scales it to the real height of each surface so small
 * buttons stay crisp and large cards do not turn milky.
 */
internal data class GlassMetrics(
    /** Background blur radius applied to the wallpaper behind the surface. */
    val blurDp: Float,
    /** Width of the "liquid" edge band where the wallpaper is magnified. */
    val edgeBandDp: Float,
    /** Magnification at the outer edge of the band (1.0 = none). */
    val edgeMagnification: Float,
    /** Multiplier for inner-shadow offsets, small blurs and spreads. */
    val shadowScale: Float,
    /** Multiplier for the two wide (88 px) inner-shadow blurs. */
    val wideBlurScale: Float,
)

internal object GlassPolicy {
    private const val REFERENCE_HEIGHT_DP = 80f
    private const val BLUR_PER_HEIGHT = 0.07f
    private const val MIN_BLUR_DP = 3f
    private const val MAX_BLUR_DP = 16f
    private const val BAND_PER_HEIGHT = 0.2f
    private const val MIN_BAND_DP = 6f
    private const val MAX_BAND_DP = 22f
    private const val SMALL_SURFACE_MAX_HEIGHT_DP = 120f
    private const val SMALL_SURFACE_MAGNIFICATION = 1.08f
    private const val LARGE_SURFACE_MAGNIFICATION = 1.04f
    private const val MIN_SHADOW_SCALE = 0.6f
    private const val MAX_SHADOW_SCALE = 1.4f
    private const val MIN_WIDE_BLUR_SCALE = 0.35f
    private const val MAX_WIDE_BLUR_SCALE = 1f

    /** Minimum window width, at font scale 1, for the two-column layout. */
    const val TWO_COLUMN_MIN_WIDTH_DP = 700f

    /** Glass parameters for a surface [heightDp] tall. Non-finite or non-positive heights use the minimums. */
    fun metricsFor(heightDp: Float): GlassMetrics {
        val height = if (heightDp.isFinite() && heightDp > 0f) heightDp else 0f
        val ratio = height / REFERENCE_HEIGHT_DP
        return GlassMetrics(
            blurDp = (height * BLUR_PER_HEIGHT).coerceIn(MIN_BLUR_DP, MAX_BLUR_DP),
            edgeBandDp = (height * BAND_PER_HEIGHT).coerceIn(MIN_BAND_DP, MAX_BAND_DP),
            edgeMagnification =
                if (height < SMALL_SURFACE_MAX_HEIGHT_DP) SMALL_SURFACE_MAGNIFICATION else LARGE_SURFACE_MAGNIFICATION,
            shadowScale = ratio.coerceIn(MIN_SHADOW_SCALE, MAX_SHADOW_SCALE),
            wideBlurScale = ratio.coerceIn(MIN_WIDE_BLUR_SCALE, MAX_WIDE_BLUR_SCALE),
        )
    }

    /**
     * Two columns only when the window is wide enough for the CURRENT font scale: larger text needs
     * proportionally more width, so enlarged system text falls back to one readable column.
     */
    fun useTwoColumns(
        widthDp: Float,
        fontScale: Float,
    ): Boolean {
        if (!widthDp.isFinite()) return false
        val scale = if (fontScale.isFinite() && fontScale > 0f) fontScale.coerceAtLeast(1f) else 1f
        return widthDp >= TWO_COLUMN_MIN_WIDTH_DP * scale
    }

    /**
     * The floating "Stop server" shortcut is shown only while the server is running, which is exactly
     * when the server card's own Stop button is enabled. It performs the same ordinary server stop.
     */
    fun showStopServerShortcut(status: ServerStatus): Boolean = status is ServerStatus.Running
}
