@file:Suppress("FunctionNaming")

package com.danielealbano.androidremotecontrolmcp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.danielealbano.androidremotecontrolmcp.ui.glass.GlassBackdrop

/**
 * App theme: the Miya pink/lilac palette over the liquid-glass wallpaper. Material You dynamic colour
 * is off by default because it would replace the pink/lilac palette with the system wallpaper colours.
 */
@Composable
fun AndroidRemoteControlMcpTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor -> {
                val context = LocalContext.current
                if (darkTheme) {
                    dynamicDarkColorScheme(context)
                } else {
                    dynamicLightColorScheme(context)
                }
            }

            darkTheme -> {
                DarkColorScheme
            }

            else -> {
                LightColorScheme
            }
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
    ) {
        GlassBackdrop(darkTheme = darkTheme, content = content)
    }
}
