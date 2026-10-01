package com.devddagnet.hardwareinsets.lib

import android.graphics.Rect
import androidx.compose.ui.unit.IntOffset
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import androidx.compose.ui.unit.IntSize
import com.devddagnet.hardwareinsets.lib.domain.WindowCorner
import com.devddagnet.hardwareinsets.lib.domain.cornerClearanceFor

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

    @Test
    fun `Should take the deepest cutout whatever order they arrive in`() {
        // The platform hands over a list, and nothing promises an order. Taking
        // the deepest only when it happens to come last would read as correct
        // against every fixture written deepest-last, and put a control under a
        // camera on a real device that reports the other way round.
        val deep = Rect(0, 0, CONTROL_WIDTH, 160)
        val shallow = Rect(0, 0, CONTROL_WIDTH, 40)

        assertThat(clearanceFor(listOf(deep, shallow)).y).isEqualTo(160)
        assertThat(clearanceFor(listOf(shallow, deep)).y).isEqualTo(160)

        val deepBottom = Rect(0, WINDOW_HEIGHT - 160, CONTROL_WIDTH, WINDOW_HEIGHT)
        val shallowBottom = Rect(0, WINDOW_HEIGHT - 40, CONTROL_WIDTH, WINDOW_HEIGHT)

        assertThat(clearanceFor(listOf(deepBottom, shallowBottom), isAtTop = false).y).isEqualTo(160)
        assertThat(clearanceFor(listOf(shallowBottom, deepBottom), isAtTop = false).y).isEqualTo(160)
    }

    @Test
    fun `Should move nothing for a cutout that reaches neither edge`() {
        // Horizontally in the control's column, vertically in the middle of the
        // screen: the platform reports a rectangle like this for a camera under
        // the display, and it is in nobody's corner.
        val floating = Rect(0, 400, CONTROL_WIDTH, 540)

        val atTop = clearanceFor(listOf(floating), isAtTop = true)
        val atBottom = clearanceFor(listOf(floating), isAtTop = false)

        assertThat(atTop).isEqualTo(IntOffset.Zero)
        assertThat(atBottom).isEqualTo(IntOffset.Zero)
    }

    @Test
    fun `Should clear a bottom cutout in the far corner under right to left`() {
        // The fourth corner, which nothing else here computes: three of the four
        // reach `cornerClearanceFor` through the tests above and `BOTTOM_RIGHT`
        // did not, so the one combination where both "measure from the right" and
        // "measure from the bottom" apply at once was never run.
        val farCorner = Rect(
            WINDOW_WIDTH - CONTROL_WIDTH,
            WINDOW_HEIGHT - 120,
            WINDOW_WIDTH,
            WINDOW_HEIGHT,
        )
        val nearCorner = Rect(0, WINDOW_HEIGHT - 200, CONTROL_WIDTH, WINDOW_HEIGHT)

        val clearance = clearanceFor(listOf(farCorner, nearCorner), isRtl = true, isAtTop = false)

        // Its own corner's cutout, not the deeper one at the other end.
        assertThat(clearance.y).isEqualTo(120)
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
