package io.github.damson.hardwareinsets

import android.graphics.Rect
import androidx.compose.ui.unit.IntOffset
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import androidx.compose.ui.unit.IntSize
import io.github.damson.hardwareinsets.domain.WindowCorner
import io.github.damson.hardwareinsets.domain.cornerClearanceFor

/**
 * A corner control sits in the corner of whichever edge it is anchored to, and
 * should touch the edges of the screen. A cutout's *inset* is the wrong thing to
 * move it by: the inset spans the whole width whatever shape the camera is, so a
 * punch-hole in the middle of the top edge would push a corner tab a centimetre
 * down a screen it never shared with the camera.
 *
 * These drive the geometry directly, because none of it can be produced on the
 * emulator to hand -- its own cutout is a single centred punch-hole, and there
 * is no way to ask it for a corner notch or a right-hand one.
 *
 * Robolectric only for `android.graphics.Rect`, which is a stub off-device.
 */
@RunWith(RobolectricTestRunner::class)
class CornerClearanceTest {

    @Test
    fun `Should leave the control against the corner When the cutout is elsewhere`() {
        val camera = Rect(485, 0, 595, 142)

        val clearance = clearanceFor(listOf(camera))

        // A centred punch-hole is not in a corner tab's way.
        assertThat(clearance).isEqualTo(IntOffset.Zero)
    }

    @Test
    fun `Should push the control below a cutout it overlaps`() {
        val notch = Rect(0, 0, CONTROL_WIDTH + 10, 142)

        val clearance = clearanceFor(listOf(notch))

        assertThat(clearance).isEqualTo(IntOffset(0, 142))
    }

    @Test
    fun `Should clear the deepest of several cutouts it overlaps`() {
        val shallow = Rect(0, 0, 40, 80)
        val deep = Rect(30, 0, 90, 160)
        val far = Rect(900, 0, 1000, 400)

        val clearance = clearanceFor(listOf(shallow, deep, far))

        assertThat(clearance.y).isEqualTo(160)
    }

    @Test
    fun `Should measure from the right edge When the layout is right to left`() {
        // Same camera, mirrored: under RTL the handle is in the other corner, so
        // this one is the one that overlaps and the left-hand one does not.
        val rightCorner = Rect(WINDOW_WIDTH - CONTROL_WIDTH, 0, WINDOW_WIDTH, 142)
        val leftCorner = Rect(0, 0, CONTROL_WIDTH, 300)

        val rtl = clearanceFor(listOf(rightCorner, leftCorner), isRtl = true)
        val ltr = clearanceFor(listOf(rightCorner, leftCorner), isRtl = false)

        assertThat(rtl.y).isEqualTo(142)
        assertThat(ltr.y).isEqualTo(300)
    }

    @Test
    fun `Should hold the control off a curved edge whatever the cutout does`() {
        // A waterfall edge has no bounding rectangle -- it runs the whole side --
        // so it arrives as a side inset and is taken whole.
        val clearance = clearanceFor(cutouts = emptyList(), sideInset = 24)

        assertThat(clearance).isEqualTo(IntOffset(24, 0))
    }

    @Test
    fun `Should count the side inset as part of where the control sits`() {
        // Shifted in by the curve, the control no longer reaches the cutout it
        // would have overlapped flush against the edge.
        val notch = Rect(0, 0, 20, 142)

        assertThat(clearanceFor(listOf(notch), sideInset = 0).y).isEqualTo(142)
        assertThat(clearanceFor(listOf(notch), sideInset = 24).y).isZero()
    }

    @Test
    fun `Should ignore a cutout in the edge the control is not against`() {
        val topCamera = Rect(0, 0, CONTROL_WIDTH, 142)
        val bottomCamera = Rect(0, WINDOW_HEIGHT - 96, CONTROL_WIDTH, WINDOW_HEIGHT)

        val atTop = clearanceFor(listOf(topCamera, bottomCamera), isAtTop = true)
        val atBottom = clearanceFor(listOf(topCamera, bottomCamera), isAtTop = false)

        // Each takes the depth of its own edge's camera and nothing from the
        // other's, which the platform reports on the same window.
        assertThat(atTop.y).isEqualTo(142)
        assertThat(atBottom.y).isEqualTo(96)
    }

    @Test
    fun `Should leave a bottom control against the corner When only the top has a cutout`() {
        val topCamera = Rect(0, 0, CONTROL_WIDTH, 142)

        val clearance = clearanceFor(listOf(topCamera), isAtTop = false)

        assertThat(clearance).isEqualTo(IntOffset.Zero)
    }

    @Test
    fun `Should clear the deepest bottom cutout it overlaps`() {
        val shallow = Rect(0, WINDOW_HEIGHT - 40, 40, WINDOW_HEIGHT)
        val deep = Rect(30, WINDOW_HEIGHT - 160, 90, WINDOW_HEIGHT)

        val clearance = clearanceFor(listOf(shallow, deep), isAtTop = false)

        assertThat(clearance.y).isEqualTo(160)
    }

    private fun clearanceFor(
        cutouts: List<Rect>,
        sideInset: Int = 0,
        isRtl: Boolean = false,
        isAtTop: Boolean = true,
    ) = cornerClearanceFor(
        cutoutBounds = cutouts,
        controlWidth = CONTROL_WIDTH,
        sideInset = sideInset,
        windowSize = IntSize(WINDOW_WIDTH, WINDOW_HEIGHT),
        corner = when {
            isAtTop && isRtl -> WindowCorner.TOP_RIGHT
            isAtTop -> WindowCorner.TOP_LEFT
            isRtl -> WindowCorner.BOTTOM_RIGHT
            else -> WindowCorner.BOTTOM_LEFT
        },
    )

    private companion object {
        const val WINDOW_WIDTH = 1080
        const val WINDOW_HEIGHT = 2400

        /** About what the handle measures on a phone-density screen. */
        const val CONTROL_WIDTH = 160
    }
}
