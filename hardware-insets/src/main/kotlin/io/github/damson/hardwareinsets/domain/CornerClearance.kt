package io.github.damson.hardwareinsets.domain

import android.graphics.Rect
import androidx.compose.ui.unit.IntOffset

/**
 * The geometry `cornerClearance` is built on, kept pure so it can be tested
 * against rectangles no emulator here has the hardware to produce, and exposed
 * so a caller laying out its own control can ask the same question.
 *
 * A rectangle counts only if it overlaps the control's width **and** touches the
 * edge the control is anchored to. Without that second test a camera in the top
 * edge would push a bottom-anchored handle almost the height of the screen, and
 * a chin at the bottom would push a top-anchored one off it -- the platform
 * reports every cutout on the window, not only the near one.
 *
 * @param cutoutBounds the cutout rectangles, in window coordinates and pixels.
 * @param controlWidth how wide the control is, in pixels.
 * @param sideInset how far in from the near side the control already sits.
 * @param windowWidth the window's width in pixels, not the host view's.
 * @param windowHeight the window's height in pixels, not the host view's.
 * @param isRtl whether the control is measured from the right side rather than
 *   the left.
 * @param isAtTop whether the control is anchored to the top edge rather than
 *   the bottom one.
 * @return the offset to place the control at, relative to its corner.
 */
fun cornerClearanceFor(
    cutoutBounds: List<Rect>,
    controlWidth: Int,
    sideInset: Int,
    windowWidth: Int,
    windowHeight: Int,
    isRtl: Boolean,
    isAtTop: Boolean,
): IntOffset {
    val start = if (isRtl) windowWidth - sideInset - controlWidth else sideInset
    val end = start + controlWidth

    val overlapping = cutoutBounds.filter { it.right > start && it.left < end }
    val depth = if (isAtTop) {
        overlapping.filter { it.top <= 0 }.maxOfOrNull { it.bottom }
    } else {
        overlapping.filter { it.bottom >= windowHeight }.maxOfOrNull { windowHeight - it.top }
    }

    return IntOffset(x = sideInset, y = depth ?: 0)
}
