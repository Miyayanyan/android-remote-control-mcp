@file:Suppress("FunctionNaming", "MagicNumber")

package com.danielealbano.androidremotecontrolmcp.ui.glass

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.RuntimeShader
import android.graphics.Shader
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import kotlin.math.abs
import kotlin.random.Random

/** How a glass surface is tinted. */
internal enum class GlassTone {
    /** Transparent centre: the default for cards, rows and secondary buttons. */
    Clear,

    /** Lilac-to-pink tinted glass for primary actions. */
    Tinted,

    /** Deep-red glass for actions that stop something. */
    Danger,

    /** The bright raised "lens" bubble used for the current selection. */
    Lens,
}

internal object GlassDefaults {
    val CardCornerRadius: Dp = 26.dp
    val CardShadowElevation: Dp = 10.dp
    val ButtonShadowElevation: Dp = 6.dp
}

/**
 * Provided by [GlassBackdrop]. When absent (e.g. a component rendered outside the app theme), glass
 * surfaces skip the refracted backdrop and only draw their fill and highlights.
 */
internal val LocalGlassPalette = staticCompositionLocalOf<GlassPalette?> { null }

/**
 * Draws the pink/lilac wallpaper behind [content] and enables glass surfaces inside it. Must be placed
 * at the root of the window: glass surfaces align their copy of the wallpaper using root coordinates.
 */
@Composable
internal fun GlassBackdrop(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    val palette = if (darkTheme) NightGlassPalette else DayGlassPalette
    Box(Modifier.fillMaxSize().drawBehind { drawGlassWallpaper(palette, size) }) {
        CompositionLocalProvider(LocalGlassPalette provides palette, content = content)
    }
}

/**
 * A liquid-glass surface: the wallpaper behind it, refracted at the edges and blurred, under a light
 * fill, grain, six inner shadows, two directional highlight strokes and (for [GlassTone.Tinted] and
 * [GlassTone.Lens]) a subtle pink/lilac colour fringe.
 *
 * @param cornerRadius corner radius, or `null` for a pill (fully rounded ends).
 */
@Composable
internal fun GlassSurface(
    modifier: Modifier = Modifier,
    tone: GlassTone = GlassTone.Clear,
    cornerRadius: Dp? = GlassDefaults.CardCornerRadius,
    shadowElevation: Dp = GlassDefaults.CardShadowElevation,
    content: @Composable BoxScope.() -> Unit,
) {
    val providedPalette = LocalGlassPalette.current
    val palette = providedPalette ?: DayGlassPalette
    val shape = if (cornerRadius == null) RoundedCornerShape(percent = 50) else RoundedCornerShape(cornerRadius)
    val placement = remember { GlassPlacement() }
    val contentColor =
        when (tone) {
            GlassTone.Clear, GlassTone.Lens -> MaterialTheme.colorScheme.onSurface
            GlassTone.Tinted, GlassTone.Danger -> Color.White
        }

    Box(
        modifier =
            modifier
                .shadow(
                    elevation = shadowElevation,
                    shape = shape,
                    clip = false,
                    ambientColor = palette.dropShadow,
                    spotColor = palette.dropShadow,
                ).clip(shape)
                .onGloballyPositioned { coordinates ->
                    placement.offset = coordinates.positionInRoot()
                    placement.rootSize = coordinates.findRootCoordinates().size.toSize()
                },
        propagateMinConstraints = true,
    ) {
        if (providedPalette != null) {
            Box(Modifier.matchParentSize().glassBackdropLayer(palette, placement, cornerRadius))
        }
        Box(Modifier.matchParentSize().glassOverlay(palette, tone, shape, cornerRadius))
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            Box(propagateMinConstraints = true, content = content)
        }
    }
}

/** Where a surface sits in the window; read only while drawing, so scrolling just redraws. */
@Stable
private class GlassPlacement {
    var offset by mutableStateOf(Offset.Zero)
    var rootSize by mutableStateOf(Size.Zero)
}

private const val HIGHLIGHT_STROKE_DP = 1.2f
private const val DISPERSION_STROKE_DP = 2.5f
private const val DISPERSION_BLUR_DP = 1.4f
private const val DISPERSION_ALPHA = 0.55f

/** Converts a Figma/CSS-style blur value to an Android BlurMaskFilter radius (sigma = blur / 2). */
private const val BLUR_TO_MASK_RADIUS = 0.866f

/**
 * AGSL edge refraction: within [band] of the rounded outline, sample the wallpaper closer to the centre,
 * magnifying it like the rim of a lens. The centre stays untouched (transparent glass).
 */
private const val EDGE_SHADER_SOURCE = """
uniform shader content;
uniform float2 size;
uniform float radius;
uniform float band;
uniform float strength;

half4 main(float2 p) {
    float2 c = size * 0.5;
    float2 q = abs(p - c) - (c - float2(radius));
    float d = length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - radius;
    float t = clamp(1.0 + d / band, 0.0, 1.0);
    float k = strength * t * t;
    return content.eval(c + (p - c) * (1.0 - k));
}
"""

private fun edgeRadiusPx(
    size: Size,
    cornerRadiusPx: Float?,
): Float {
    val maxRadius = size.minDimension / 2f
    return cornerRadiusPx?.coerceAtMost(maxRadius) ?: maxRadius
}

private fun Modifier.glassBackdropLayer(
    palette: GlassPalette,
    placement: GlassPlacement,
    cornerRadius: Dp?,
): Modifier =
    graphicsLayer {
        renderEffect =
            if (size.minDimension <= 0f) {
                null
            } else {
                val metrics = GlassPolicy.metricsFor(size.height / density)
                glassRenderEffect(
                    size = size,
                    radiusPx = edgeRadiusPx(size, cornerRadius?.toPx()),
                    bandPx = metrics.edgeBandDp.dp.toPx(),
                    strength = metrics.edgeMagnification - 1f,
                    blurPx = metrics.blurDp.dp.toPx(),
                )
            }
        clip = true
    }.drawBehind {
        translate(-placement.offset.x, -placement.offset.y) {
            drawGlassWallpaper(palette, placement.rootSize)
        }
    }

private fun glassRenderEffect(
    size: Size,
    radiusPx: Float,
    bandPx: Float,
    strength: Float,
    blurPx: Float,
): RenderEffect {
    val shader =
        RuntimeShader(EDGE_SHADER_SOURCE).apply {
            setFloatUniform("size", size.width, size.height)
            setFloatUniform("radius", radiusPx)
            setFloatUniform("band", bandPx.coerceAtLeast(1f))
            setFloatUniform("strength", strength)
        }
    val edge = android.graphics.RenderEffect.createRuntimeShaderEffect(shader, "content")
    val blur = android.graphics.RenderEffect.createBlurEffect(blurPx, blurPx, Shader.TileMode.CLAMP)
    // The inner effect (edge refraction) runs first, then the blur.
    return android.graphics.RenderEffect
        .createChainEffect(blur, edge)
        .asComposeRenderEffect()
}

/** One inner shadow in dp, as in the reference file: offset, blur, spread and colour. */
private data class InnerShadowSpec(
    val dx: Float,
    val dy: Float,
    val blur: Float,
    val spread: Float,
    val color: Color,
)

private fun innerShadowSpecs(
    tone: GlassTone,
    palette: GlassPalette,
    metrics: GlassMetrics,
): List<InnerShadowSpec> {
    val s = metrics.shadowScale
    return if (tone == GlassTone.Lens) {
        listOf(
            InnerShadowSpec(0f, -10f * s, 11.2f * s, 0f, Color.White.copy(alpha = 0.75f)),
            InnerShadowSpec(7f * s, 16f * s, 30f * s, 0f, palette.innerDark.copy(alpha = 0.16f)),
            InnerShadowSpec(-1f * s, -2f * s, 4f * s, 0f, Color.White.copy(alpha = 0.5f)),
        )
    } else {
        val wide = 88f * metrics.wideBlurScale
        listOf(
            InnerShadowSpec(1.7f * s, 3.3f * s, wide, 0f, palette.innerDark),
            InnerShadowSpec(-1.7f * s, -3.3f * s, wide, 0f, palette.innerDark),
            InnerShadowSpec(-5f * s, -7f * s, 20f * s, -7f * s, palette.innerLightSoft),
            InnerShadowSpec(-1f * s, -2f * s, 4f * s, 0f, palette.innerLightStrong),
            InnerShadowSpec(5f * s, 5f * s, 11.3f * s, -14f * s, palette.innerLightSoft),
            InnerShadowSpec(1f * s, 2f * s, 4f * s, 0f, palette.innerLightStrong),
        )
    }
}

/** An inner shadow resolved to pixels: a blurred frame around the (offset, spread) shape. */
private class PreparedInnerShadow(
    private val frame: android.graphics.Path,
    private val paint: android.graphics.Paint,
) {
    fun draw(
        canvas: Canvas,
        clip: android.graphics.Path,
    ) {
        val checkpoint = canvas.save()
        canvas.clipPath(clip)
        canvas.drawPath(frame, paint)
        canvas.restoreToCount(checkpoint)
    }
}

private fun prepareInnerShadow(
    spec: InnerShadowSpec,
    size: Size,
    radiusPx: Float,
    density: Density,
): PreparedInnerShadow {
    val dx = with(density) { spec.dx.dp.toPx() }
    val dy = with(density) { spec.dy.dp.toPx() }
    val blur = with(density) { spec.blur.dp.toPx() }
    val spread = with(density) { spec.spread.dp.toPx() }
    val margin = blur * 3f + abs(dx) + abs(dy) + abs(spread) + 1f
    val frame =
        android.graphics.Path().apply {
            fillType = android.graphics.Path.FillType.EVEN_ODD
            addRect(-margin, -margin, size.width + margin, size.height + margin, android.graphics.Path.Direction.CW)
            val hole = RectF(spread + dx, spread + dy, size.width - spread + dx, size.height - spread + dy)
            if (hole.width() > 0f && hole.height() > 0f) {
                val holeRadius = (radiusPx - spread).coerceIn(0f, minOf(hole.width(), hole.height()) / 2f)
                addRoundRect(hole, holeRadius, holeRadius, android.graphics.Path.Direction.CW)
            }
        }
    val paint =
        android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = spec.color.toArgb()
            if (blur > 0f) maskFilter = BlurMaskFilter(blur * BLUR_TO_MASK_RADIUS, BlurMaskFilter.Blur.NORMAL)
        }
    return PreparedInnerShadow(frame, paint)
}

/** The colour fringe: three soft pink, lilac and peach strokes around the inner edge. */
private fun prepareDispersion(
    size: Size,
    density: Density,
): List<android.graphics.Paint> {
    val width = with(density) { DISPERSION_STROKE_DP.dp.toPx() } * 2f
    val blur = with(density) { DISPERSION_BLUR_DP.dp.toPx() }
    val radius = size.maxDimension * 0.6f
    val alpha = (DISPERSION_ALPHA * 255).toInt()
    return listOf(
        Triple(Offset(0f, size.height * 0.5f), Color(0xFFFF78BE), alpha),
        Triple(Offset(size.width, size.height * 0.4f), Color(0xFFA082FF), alpha),
        Triple(Offset(size.width * 0.6f, 0f), Color(0xFFFFBE96), alpha),
    ).map { (center, color, paintAlpha) ->
        android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = width
            maskFilter = BlurMaskFilter(blur * BLUR_TO_MASK_RADIUS, BlurMaskFilter.Blur.NORMAL)
            shader =
                RadialGradient(
                    center.x,
                    center.y,
                    radius,
                    color.toArgb(),
                    Color.Transparent.toArgb(),
                    Shader.TileMode.CLAMP,
                )
            this.alpha = paintAlpha
        }
    }
}

private fun toneFill(
    tone: GlassTone,
    palette: GlassPalette,
    size: Size,
): Brush =
    when (tone) {
        GlassTone.Clear -> {
            SolidColor(palette.clearFill)
        }

        GlassTone.Lens -> {
            SolidColor(palette.lensFill)
        }

        GlassTone.Tinted -> {
            Brush.linearGradient(
                listOf(palette.tintStart, palette.tintEnd),
                Offset.Zero,
                Offset(size.width, size.height),
            )
        }

        GlassTone.Danger -> {
            Brush.linearGradient(
                listOf(palette.dangerStart, palette.dangerEnd),
                Offset.Zero,
                Offset(size.width, size.height),
            )
        }
    }

private fun Modifier.glassOverlay(
    palette: GlassPalette,
    tone: GlassTone,
    shape: Shape,
    cornerRadius: Dp?,
): Modifier =
    drawWithCache {
        if (size.minDimension <= 0f) return@drawWithCache onDrawBehind { }
        val metrics = GlassPolicy.metricsFor(size.height / density)
        val path = Path().apply { addOutline(shape.createOutline(size, layoutDirection, this@drawWithCache)) }
        val nativePath = path.asAndroidPath()
        val radiusPx = edgeRadiusPx(size, cornerRadius?.toPx())
        val shadows = innerShadowSpecs(tone, palette, metrics).map { prepareInnerShadow(it, size, radiusPx, this) }
        val dispersion =
            if (tone == GlassTone.Tinted || tone == GlassTone.Lens) prepareDispersion(size, this) else emptyList()
        val fill = toneFill(tone, palette, size)
        // The surface is clipped to its shape, so a stroke of twice the width shows exactly its inner half.
        val highlightStroke = Stroke(width = HIGHLIGHT_STROKE_DP.dp.toPx() * 2f)
        val highlightRadius = size.maxDimension * 0.75f
        val topLeftLight =
            Brush.radialGradient(
                listOf(Color.White.copy(alpha = 0.6f), Color.Transparent),
                center = Offset(size.width * 0.06f, 0f),
                radius = highlightRadius,
            )
        val bottomRightLight =
            Brush.radialGradient(
                listOf(Color.White.copy(alpha = 0.43f), Color.Transparent),
                center = Offset(size.width * 0.96f, size.height),
                radius = highlightRadius,
            )
        onDrawBehind {
            drawPath(path, fill)
            drawPath(path, GlassNoise.brush, alpha = palette.grainAlpha, blendMode = BlendMode.Overlay)
            drawIntoCanvas { canvas ->
                shadows.forEach { it.draw(canvas.nativeCanvas, nativePath) }
            }
            drawPath(path, topLeftLight, style = highlightStroke)
            drawPath(path, bottomRightLight, style = highlightStroke)
            if (dispersion.isNotEmpty()) {
                drawIntoCanvas { canvas -> dispersion.forEach { canvas.nativeCanvas.drawPath(nativePath, it) } }
            }
        }
    }

/** A small tile of static monochrome noise, repeated as the glass grain. Deterministic, built once. */
private object GlassNoise {
    private const val TILE_SIZE = 96
    private const val SEED = 5931

    val brush: ShaderBrush by lazy {
        val random = Random(SEED)
        val pixels =
            IntArray(TILE_SIZE * TILE_SIZE) {
                val level = random.nextInt(256)
                android.graphics.Color.argb(255, level, level, level)
            }
        val bitmap = Bitmap.createBitmap(pixels, TILE_SIZE, TILE_SIZE, Bitmap.Config.ARGB_8888)
        ShaderBrush(ImageShader(bitmap.asImageBitmap(), TileMode.Repeated, TileMode.Repeated))
    }
}
