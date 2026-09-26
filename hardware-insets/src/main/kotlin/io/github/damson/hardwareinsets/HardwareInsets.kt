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
import androidx.compose.ui.unit.LayoutDirection

/**
 * Which of the window's insets count as hardware.
 *
 * The default is the two things physically in the way: the display cutout,
 * which is the camera, and the waterfall, which is the curved edge, drawable
 * but not reliably touchable. The system bars are software and are left out, so
 * an app that hides them is not padded by a bar nobody can see.
 *
 * Rounded corners are not offered. The platform reports them from API 31 as a
 * radius per corner rather than as an inset, so turning them into one is a
 * decision a caller has to make with its own corner shape in hand.
 *
 * @param isCutoutIncluded whether the cutout counts. Zero on hardware that has
 *   none, and below API 28, where the platform reports none at all.
 * @param isWaterfallIncluded whether the curved edge counts. Always zero below
 *   API 30, which is where the platform began reporting it.
 * @param areSystemBarsIncluded whether the status and navigation bars count.
 *   Off by default, because including a bar that is hidden insets content past
 *   an edge nothing occupies.
 */
@Immutable
data class HardwarePolicy(
    val isCutoutIncluded: Boolean = true,
    val isWaterfallIncluded: Boolean = true,
    val areSystemBarsIncluded: Boolean = false,
)

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
    // The end corner under LTR is the visual right, exactly where the start
    // corner is under RTL, so one flag serves the maths for both.
    val isRtl = (direction == LayoutDirection.Rtl) != isAtTheEnd
    val cutout = WindowInsets.displayCutout
    val waterfall = WindowInsets.waterfall

    val side = if (isRtl) {
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
            windowWidth = view.rootView.width,
            windowHeight = view.rootView.height,
            isRtl = isRtl,
            isAtTop = position == ScreenEdge.TOP,
        )
    }
}

/**
 * The geometry [cornerClearance] is built on, kept pure so it can be tested
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
