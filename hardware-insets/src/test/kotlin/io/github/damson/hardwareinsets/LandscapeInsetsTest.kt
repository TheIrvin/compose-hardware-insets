package io.github.damson.hardwareinsets

import android.graphics.Rect
import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.graphics.Insets
import androidx.core.view.DisplayCutoutCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** A phone on its side. The cutout is now on a long edge, which is the case
 *  `LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS` exists for. */
private const val LANDSCAPE = "w891dp-h411dp-land"

/**
 * Landscape is where the cutout stops being a top inset and becomes a side one,
 * and where `SHORT_EDGES` would have letterboxed the window. The letterboxing
 * itself belongs to the window manager and cannot be reproduced here. What
 * these cover is everything downstream of it: that a side cutout insets the
 * panel from that side, and that it moves a corner control to the same side.
 *
 * Driven through a real layout rather than through the pure function, because
 * the half that goes wrong is which edge a value lands on, not the arithmetic.
 */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = LANDSCAPE)
class LandscapeInsetsTest {

    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `Should hold the panel inside a cutout on the leading edge`() {
        showPanel()

        applyInsets(
            WindowInsetsCompat.Builder()
                .setInsets(WindowInsetsCompat.Type.displayCutout(), Insets.of(INSET_PX, 0, 0, 0))
                .build()
        )

        assertThat(boundsOf(PANEL).left).isEqualTo(INSET_PX)
        assertThat(boundsOf(PANEL).top).isZero()
    }

    @Test
    fun `Should leave the background across the whole of a landscape screen`() {
        showPanel()

        applyInsets(
            WindowInsetsCompat.Builder()
                .setInsets(WindowInsetsCompat.Type.displayCutout(), Insets.of(INSET_PX, 0, 0, 0))
                .build()
        )

        // Wider screen, same rule: the slab is not a control and does not move.
        assertThat(boundsOf(BACKGROUND).left).isZero()
        assertThat(boundsOf(BACKGROUND).right).isEqualTo(boundsOf(ROOT).right)
    }

    @Test
    fun `Should move a corner control off a cutout that is beside it`() {
        // Rotated, the camera runs down the left edge rather than across the top,
        // so it overlaps the handle for its whole height rather than its width.
        val camera = Rect(0, 380, INSET_PX, 510)
        lateinit var clearance: (Int) -> IntOffset
        compose.setContent { clearance = cornerClearance(listOf(camera)) }

        applyInsets(
            WindowInsetsCompat.Builder()
                .setInsets(WindowInsetsCompat.Type.displayCutout(), Insets.of(INSET_PX, 0, 0, 0))
                .build()
        )

        // The side inset carries it clear; the rectangle does not reach the top
        // corner, so nothing pushes it down.
        assertThat(clearance(HANDLE_WIDTH_PX)).isEqualTo(IntOffset(INSET_PX, 0))
    }

    @Test
    fun `Should hold the panel inside a waterfall edge in landscape`() {
        showPanel()

        val curve = Insets.of(INSET_PX, 0, INSET_PX, 0)
        applyInsets(
            WindowInsetsCompat.Builder()
                .setDisplayCutout(DisplayCutoutCompat(curve, null, null, null, null, curve))
                .build()
        )

        assertThat(boundsOf(PANEL).left).isEqualTo(INSET_PX)
    }

    private fun showPanel() {
        compose.setContent {
            Box(
                Modifier
                    .testTag(BACKGROUND)
                    .fillMaxWidth()
                    .background(Color.Red)
                    .clearOfTheHardware(isFarEdgeIgnored = true),
            ) {
                Box(Modifier.size(PANEL_SIZE).testTag(PANEL))
            }
        }
    }

    private fun applyInsets(insets: WindowInsetsCompat) {
        val content = compose.activity.findViewById<View>(android.R.id.content)
        compose.activity.runOnUiThread { ViewCompat.dispatchApplyWindowInsets(content, insets) }
        compose.waitForIdle()
    }

    private fun boundsOf(tag: String) = with(compose.density) {
        val bounds = if (tag == ROOT) {
            compose.onRoot().getBoundsInRoot()
        } else {
            compose.onNodeWithTag(tag).getBoundsInRoot()
        }
        Bounds(bounds.left.roundToPx(), bounds.top.roundToPx(), bounds.right.roundToPx())
    }

    private data class Bounds(val left: Int, val top: Int, val right: Int)

    private companion object {
        const val PANEL = "panel"
        const val BACKGROUND = "background"
        const val ROOT = "root"
        val PANEL_SIZE = 10.dp

        const val INSET_PX = 96
        const val HANDLE_WIDTH_PX = 160
    }
}
