package com.devddagnet.hardwareinsets.lib.domain

import android.graphics.Rect
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

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
 * @param windowSize the window's size in pixels, not the host view's. A view
 *   inside a `wrap_content` host measures the host, and the rectangles above
 *   are in window coordinates, so the two would not be in the same space.
 * @param corner which corner of the window the control is tucked into, which
 *   decides both the side the width is measured from and the edge a rectangle
 *   has to touch to count.
 * @return the offset to place the control at, relative to its corner.
 */
fun cornerClearanceFor(
    cutoutBounds: List<Rect>,
    controlWidth: Int,
    sideInset: Int,
    windowSize: IntSize,
    corner: WindowCorner,
): IntOffset {
    val start =
        if (corner.isAtTheRight) windowSize.width - sideInset - controlWidth else sideInset
    val end = start + controlWidth

    val overlapping = cutoutBounds.filter { it.right > start && it.left < end }
    val depth = if (corner.isAtTheTop) {
        overlapping.filter { it.top <= 0 }.maxOfOrNull { it.bottom }
    } else {
        overlapping.filter { it.bottom >= windowSize.height }
            .maxOfOrNull { windowSize.height - it.top }
    }

    return IntOffset(x = sideInset, y = depth ?: 0)
}
