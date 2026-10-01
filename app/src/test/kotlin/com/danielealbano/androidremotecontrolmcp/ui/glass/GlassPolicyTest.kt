package com.danielealbano.androidremotecontrolmcp.ui.glass

import com.danielealbano.androidremotecontrolmcp.data.model.ServerStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("GlassPolicy")
class GlassPolicyTest {
    private val delta = 1e-4f

    @Nested
    @DisplayName("metricsFor")
    inner class MetricsFor {
        @Test
        fun `reference 80dp button uses the reference shadow scale`() {
            val metrics = GlassPolicy.metricsFor(80f)
            assertEquals(1f, metrics.shadowScale, delta)
            assertEquals(1f, metrics.wideBlurScale, delta)
            assertEquals(5.6f, metrics.blurDp, delta)
            assertEquals(16f, metrics.edgeBandDp, delta)
            assertEquals(1.08f, metrics.edgeMagnification, delta)
        }

        @Test
        fun `48dp button is clamped to the minimum shadow scale and keeps strong magnification`() {
            val metrics = GlassPolicy.metricsFor(48f)
            assertEquals(0.6f, metrics.shadowScale, delta)
            assertEquals(0.6f, metrics.wideBlurScale, delta)
            assertEquals(3.36f, metrics.blurDp, delta)
            assertEquals(9.6f, metrics.edgeBandDp, delta)
            assertEquals(1.08f, metrics.edgeMagnification, delta)
        }

        @Test
        fun `large card is clamped so it does not turn milky`() {
            val metrics = GlassPolicy.metricsFor(300f)
            assertEquals(1.4f, metrics.shadowScale, delta)
            assertEquals(1f, metrics.wideBlurScale, delta)
            assertEquals(16f, metrics.blurDp, delta)
            assertEquals(22f, metrics.edgeBandDp, delta)
            assertEquals(1.04f, metrics.edgeMagnification, delta)
        }

        @Test
        fun `magnification switches at 120dp`() {
            assertEquals(1.08f, GlassPolicy.metricsFor(119.9f).edgeMagnification, delta)
            assertEquals(1.04f, GlassPolicy.metricsFor(120f).edgeMagnification, delta)
        }

        @Test
        fun `zero, negative and non-finite heights fall back to the minimums`() {
            listOf(0f, -10f, Float.NaN, Float.POSITIVE_INFINITY).forEach { height ->
                val metrics = GlassPolicy.metricsFor(height)
                assertEquals(3f, metrics.blurDp, delta, "blur for $height")
                assertEquals(6f, metrics.edgeBandDp, delta, "band for $height")
                assertEquals(0.6f, metrics.shadowScale, delta, "shadow for $height")
                assertEquals(0.35f, metrics.wideBlurScale, delta, "wide blur for $height")
            }
        }
    }

    @Nested
    @DisplayName("useTwoColumns")
    inner class UseTwoColumns {
        @Test
        fun `upper Thor display at density 2_25 uses two columns at default text size`() {
            assertTrue(GlassPolicy.useTwoColumns(widthDp = 853f, fontScale = 1f))
        }

        @Test
        fun `lower Thor display at density 2_25 uses one column`() {
            assertFalse(GlassPolicy.useTwoColumns(widthDp = 551f, fontScale = 1f))
        }

        @Test
        fun `enlarged text needs proportionally more width`() {
            assertTrue(GlassPolicy.useTwoColumns(widthDp = 700f, fontScale = 1f))
            assertFalse(GlassPolicy.useTwoColumns(widthDp = 853f, fontScale = 1.3f))
            assertTrue(GlassPolicy.useTwoColumns(widthDp = 910f, fontScale = 1.3f))
            assertFalse(GlassPolicy.useTwoColumns(widthDp = 1200f, fontScale = 2f))
        }

        @Test
        fun `font scales below one never lower the threshold`() {
            assertFalse(GlassPolicy.useTwoColumns(widthDp = 600f, fontScale = 0.85f))
        }

        @Test
        fun `invalid values fall back to one column or the default scale`() {
            assertFalse(GlassPolicy.useTwoColumns(widthDp = Float.NaN, fontScale = 1f))
            assertFalse(GlassPolicy.useTwoColumns(widthDp = Float.POSITIVE_INFINITY, fontScale = 1f))
            assertTrue(GlassPolicy.useTwoColumns(widthDp = 853f, fontScale = Float.NaN))
            assertTrue(GlassPolicy.useTwoColumns(widthDp = 853f, fontScale = 0f))
        }
    }

    @Nested
    @DisplayName("showStopServerShortcut")
    inner class ShowStopServerShortcut {
        @Test
        fun `shown only while the server is running`() {
            val running = ServerStatus.Running(port = 8080, bindingAddress = "127.0.0.1")
            assertTrue(GlassPolicy.showStopServerShortcut(running))
            assertFalse(GlassPolicy.showStopServerShortcut(ServerStatus.Stopped))
            assertFalse(GlassPolicy.showStopServerShortcut(ServerStatus.Starting))
            assertFalse(GlassPolicy.showStopServerShortcut(ServerStatus.Stopping))
            assertFalse(GlassPolicy.showStopServerShortcut(ServerStatus.Error("boom")))
        }
    }
}
