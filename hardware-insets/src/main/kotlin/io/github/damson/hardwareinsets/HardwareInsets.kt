package io.github.damson.hardwareinsets

import android.graphics.Rect
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.waterfall
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import io.github.damson.hardwareinsets.domain.CutoutShape
import io.github.damson.hardwareinsets.domain.HardwarePolicy
import io.github.damson.hardwareinsets.domain.ScreenEdge
import io.github.damson.hardwareinsets.domain.WindowCorner
import io.github.damson.hardwareinsets.domain.cornerClearanceFor
import io.github.damson.hardwareinsets.platform.cutoutShape

/**
 * How far content has to stay off each edge to clear the hardware there.
 *
 * @param position the edge the contents are anchored to. Only consulted when
 *   [isFarEdgeIgnored] is set.
 * @param policy which of the window's insets count. See [HardwarePolicy].
 * @param isFarEdgeIgnored whether the edge opposite [position] is forced to
 *   zero. Off by default, so all four edges are reported. Set it where the
 *   composable is hosted in a `wrap_content` `ComposeView`: an inset on the far
 *   edge grows that view, and a `ComposeView` swallows every touch inside its
 *   bounds, so the growth silently steals input from whatever is behind it.
 * @return insets in the units [WindowInsets] uses. Every term is zero on
 *   hardware with nothing to avoid.
 */
@Composable
fun hardwareInsets(
    position: ScreenEdge = ScreenEdge.TOP,
    policy: HardwarePolicy = HardwarePolicy(),
    isFarEdgeIgnored: Boolean = false,
): WindowInsets {
    val density = LocalDensity.current
    val direction = LocalLayoutDirection.current
    // Read unconditionally: these are composable getters, and reading them
    // behind the policy's flags would make the call graph depend on it.
    val cutout = WindowInsets.displayCutout
    val waterfall = WindowInsets.waterfall
    val systemBars = WindowInsets.systemBars

    val sources = buildList {
        if (policy.isCutoutIncluded) add(cutout)
        if (policy.isWaterfallIncluded) add(waterfall)
        if (policy.areSystemBarsIncluded) add(systemBars)
    }

    fun inset(edge: ScreenEdge, read: (WindowInsets) -> Int) =
        if (isFarEdgeIgnored && position == edge.opposite) 0 else sources.maxOfOrNull(read) ?: 0

    return WindowInsets(
        left = inset(ScreenEdge.LEFT) { it.getLeft(density, direction) },
        top = inset(ScreenEdge.TOP) { it.getTop(density) },
        right = inset(ScreenEdge.RIGHT) { it.getRight(density, direction) },
        bottom = inset(ScreenEdge.BOTTOM) { it.getBottom(density) },
    )
}

/**
 * Pads the contents clear of the hardware, per [hardwareInsets].
 *
 * Apply after a background, not before: the background keeps the full width and
 * only what sits inside it moves.
 *
 * @param position the edge the contents are anchored to.
 * @param policy which of the window's insets count. See [HardwarePolicy].
 * @param isFarEdgeIgnored whether the edge opposite [position] is forced to
 *   zero. See [hardwareInsets].
 */
@Composable
fun Modifier.clearOfTheHardware(
    position: ScreenEdge = ScreenEdge.TOP,
    policy: HardwarePolicy = HardwarePolicy(),
    isFarEdgeIgnored: Boolean = false,
): Modifier = windowInsetsPadding(hardwareInsets(position, policy, isFarEdgeIgnored))

/**
 * How far a control tucked into the corner of the anchored edge has to move to
 * clear the hardware *there*, given how wide it is.
 *
 * The full-width inset is the wrong question: a centred punch-hole reports one
 * across the whole width, which would drop a corner tab a centimetre to avoid a
 * camera it is nowhere near. So the cutout's rectangles are read and only the
 * ones the control overlaps count. A waterfall curve has no rectangle and is
 * taken whole -- it runs the length of the side.
 *
 * **Horizontal edges only.** Given [ScreenEdge.LEFT] or [ScreenEdge.RIGHT] this
 * returns a function that always answers [IntOffset.Zero], because the geometry
 * behind it measures a control's width against an edge that runs across the
 * window. A control on a side needs its height measured against a side instead,
 * which is not implemented.
 *
 * @param cutoutBounds where the cameras are: [CutoutShape.bounds], as
 *   [cutoutShape] publishes it.
 * @param position which edge the control is tucked against. A cutout on the
 *   other edge is not in its way and must not move it. A vertical edge yields
 *   the zero function, per above.
 * @param isAtTheEnd whether the control sits in the edge's end corner rather
 *   than its start one, which decides which side's hardware is in its way.
 *
 * @return the offset the control should be placed at, relative to its corner:
 *   `x` in from the control's own end of the edge, `y` in from the anchored edge.
 */
@Composable
fun cornerClearance(
    cutoutBounds: List<Rect>,
    position: ScreenEdge = ScreenEdge.TOP,
    isAtTheEnd: Boolean = false,
): (controlWidth: Int) -> IntOffset {
    if (!position.isHorizontalEdge) return { IntOffset.Zero }

    val density = LocalDensity.current
    val direction = LocalLayoutDirection.current
    val view = LocalView.current
    // Which corner this is takes the layout direction as well as the choice:
    // the end corner is the visual right under LTR and the visual left under
    // RTL, and a cutout is on one side or the other regardless.
    val isAtTheRight = (direction == LayoutDirection.Rtl) != isAtTheEnd
    val corner = when {
        position == ScreenEdge.TOP && isAtTheRight -> WindowCorner.TOP_RIGHT
        position == ScreenEdge.TOP -> WindowCorner.TOP_LEFT
        isAtTheRight -> WindowCorner.BOTTOM_RIGHT
        else -> WindowCorner.BOTTOM_LEFT
    }
    val cutout = WindowInsets.displayCutout
    val waterfall = WindowInsets.waterfall

    val side = if (isAtTheRight) {
        maxOf(cutout.getRight(density, direction), waterfall.getRight(density, direction))
    } else {
        maxOf(cutout.getLeft(density, direction), waterfall.getLeft(density, direction))
    }

    return { controlWidth ->
        cornerClearanceFor(
            cutoutBounds = cutoutBounds,
            controlWidth = controlWidth,
            sideInset = side,
            // The root, not this view: a composable hosted in a wrap_content
            // ComposeView measures the host rather than the window, and the
            // rectangles are in window coordinates. Read here rather than at
            // composition, when it is still zero.
            windowSize = IntSize(view.rootView.width, view.rootView.height),
            corner = corner,
        )
    }
}
