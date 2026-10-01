@file:Suppress("FunctionNaming", "MagicNumber")

package com.danielealbano.androidremotecontrolmcp.ui.glass

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.danielealbano.androidremotecontrolmcp.R

private const val PRESSED_SCALE = 0.965f
private const val DISABLED_ALPHA = 0.45f
private const val ORB_GLOW_ALPHA = 0.35f
private const val ORB_SHADE_ALPHA = 0.55f
private const val ORB_HIGHLIGHT_X = 0.35f
private const val ORB_HIGHLIGHT_Y = 0.3f
private const val ORB_CORE_FRACTION = 0.62f
private const val TAB_BAR_PADDING_DP = 5
private const val TAB_SPACING_DP = 4
private const val TAB_MIN_HEIGHT_DP = 52
private const val TAB_VERTICAL_PADDING_DP = 6
private const val TAB_ICON_LABEL_SPACING_DP = 2
private const val BUTTON_HORIZONTAL_PADDING_DP = 20
private const val BUTTON_VERTICAL_PADDING_DP = 10
private const val BUTTON_ICON_SPACING_DP = 8
private const val STOP_SHORTCUT_MAX_WIDTH_DP = 420

/** Minimum touch target height of glass buttons and tabs. */
internal val GlassMinTouchTarget: Dp = 48.dp

/** Default inner padding of [GlassCard]. */
internal val GlassCardPadding: Dp = 18.dp

/** A glass card: [GlassSurface] with the card radius and a padded column. */
@Composable
internal fun GlassCard(
    modifier: Modifier = Modifier,
    tone: GlassTone = GlassTone.Clear,
    contentPadding: PaddingValues = PaddingValues(GlassCardPadding),
    content: @Composable ColumnScope.() -> Unit,
) {
    GlassSurface(modifier = modifier, tone = tone) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

/**
 * A pill-shaped glass button with a ripple, a gentle press squeeze and a 48 dp minimum height.
 * Disabled buttons are dimmed and not clickable.
 */
@Composable
internal fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tone: GlassTone = GlassTone.Clear,
    contentPadding: PaddingValues =
        PaddingValues(horizontal = BUTTON_HORIZONTAL_PADDING_DP.dp, vertical = BUTTON_VERTICAL_PADDING_DP.dp),
    content: @Composable RowScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed && enabled) PRESSED_SCALE else 1f, label = "glassPress")
    GlassSurface(
        modifier =
            modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    alpha = if (enabled) 1f else DISABLED_ALPHA
                }.defaultMinSize(minHeight = GlassMinTouchTarget),
        tone = tone,
        cornerRadius = null,
        shadowElevation = GlassDefaults.ButtonShadowElevation,
    ) {
        Row(
            modifier =
                Modifier
                    .clickable(
                        interactionSource = interactionSource,
                        indication = ripple(),
                        enabled = enabled,
                        role = Role.Button,
                        onClick = onClick,
                    ).padding(contentPadding),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProvideTextStyle(MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)) {
                content()
            }
        }
    }
}

/** A glass button with an optional leading icon and a single text label. */
@Composable
internal fun GlassTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tone: GlassTone = GlassTone.Clear,
    icon: ImageVector? = null,
) {
    GlassButton(onClick = onClick, modifier = modifier, enabled = enabled, tone = tone) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null)
            Spacer(Modifier.width(BUTTON_ICON_SPACING_DP.dp))
        }
        Text(text = text, textAlign = TextAlign.Center)
    }
}

/**
 * A soft inset area for details inside a glass card (no glass on glass): a light translucent fill
 * with a hairline edge.
 */
@Composable
internal fun GlassWell(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val palette = LocalGlassPalette.current ?: DayGlassPalette
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier.clip(shape).background(palette.well).border(1.dp, palette.wellLine, shape),
        content = content,
    )
}

/**
 * A small glowing status sphere in [color]. Purely visual: the caller provides the semantics (the
 * status text) on [modifier].
 */
@Composable
internal fun StatusOrb(
    color: Color,
    modifier: Modifier = Modifier,
    diameter: Dp = 14.dp,
) {
    Canvas(modifier = modifier.size(diameter)) {
        val radius = size.minDimension / 2f
        drawCircle(color = color.copy(alpha = ORB_GLOW_ALPHA), radius = radius)
        val coreRadius = radius * ORB_CORE_FRACTION
        drawCircle(
            brush =
                Brush.radialGradient(
                    listOf(Color.White, color, color.copy(alpha = ORB_SHADE_ALPHA)),
                    center = Offset(size.width * ORB_HIGHLIGHT_X, size.height * ORB_HIGHLIGHT_Y),
                    radius = coreRadius * 2f,
                ),
            radius = coreRadius,
        )
    }
}

/** One destination of [GlassTabBar]. */
@Immutable
internal data class GlassTabItem(
    val key: String,
    val icon: ImageVector,
    val label: String,
)

/**
 * The floating glass tab bar. The selected destination sits on the bright lens bubble from the
 * reference file; every tab keeps a 48 dp+ target and tab semantics.
 */
@Composable
internal fun GlassTabBar(
    items: List<GlassTabItem>,
    selectedKey: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassSurface(modifier = modifier, cornerRadius = null, shadowElevation = GlassDefaults.CardShadowElevation) {
        Row(
            modifier = Modifier.padding(TAB_BAR_PADDING_DP.dp).selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(TAB_SPACING_DP.dp),
        ) {
            items.forEach { item ->
                val selected = item.key == selectedKey
                val tabModifier = Modifier.weight(1f).defaultMinSize(minHeight = TAB_MIN_HEIGHT_DP.dp)
                if (selected) {
                    GlassSurface(
                        modifier = tabModifier,
                        tone = GlassTone.Lens,
                        cornerRadius = null,
                        shadowElevation = GlassDefaults.ButtonShadowElevation,
                    ) {
                        GlassTabContent(item = item, selected = true, onClick = { onSelect(item.key) })
                    }
                } else {
                    Box(modifier = tabModifier.clip(RoundedCornerShape(percent = 50)), propagateMinConstraints = true) {
                        GlassTabContent(item = item, selected = false, onClick = { onSelect(item.key) })
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassTabContent(
    item: GlassTabItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val color =
        if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier =
            Modifier
                .selectable(selected = selected, role = Role.Tab, onClick = onClick)
                .padding(vertical = TAB_VERTICAL_PADDING_DP.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(TAB_ICON_LABEL_SPACING_DP.dp, Alignment.CenterVertically),
    ) {
        Icon(imageVector = item.icon, contentDescription = null, tint = color)
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * Floating red-glass shortcut for the ordinary server stop. Deliberately labelled "Stop server", not
 * "Emergency Stop": it performs exactly the same graceful stop as the server card's Stop button.
 */
@Composable
internal fun StopServerShortcut(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassTextButton(
        text = stringResource(R.string.server_stop_shortcut),
        onClick = onClick,
        modifier = modifier.widthIn(max = STOP_SHORTCUT_MAX_WIDTH_DP.dp).fillMaxWidth(),
        tone = GlassTone.Danger,
        icon = Icons.Default.StopCircle,
    )
}
