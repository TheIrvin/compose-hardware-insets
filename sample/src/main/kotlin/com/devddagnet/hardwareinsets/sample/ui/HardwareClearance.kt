package com.devddagnet.hardwareinsets.sample.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.devddagnet.hardwareinsets.lib.clearOfTheHardware
import com.devddagnet.hardwareinsets.lib.cornerClearance
import com.devddagnet.hardwareinsets.lib.domain.CutoutShape
import com.devddagnet.hardwareinsets.lib.domain.HardwarePolicy
import com.devddagnet.hardwareinsets.lib.domain.ScreenEdge
import kotlin.math.abs

/**
 * A control keeps off the bars by padding, and off the camera by moving.
 *
 * Two different questions, and this is the one the library answers by policy: a
 * bar is software, runs the full width and is always in the way, so it is a
 * margin. The camera is a rectangle somewhere along the edge, so it is an
 * offset, and [cornerClearance] is what works it out. Pad by the cutout as well
 * and the control drops clear of a punch-hole it is nowhere near.
 *
 * The label is not given this, because the label is the thing on trial: which
 * insets it takes is the sheet's first switch.
 */
@Composable
internal fun Modifier.clearOfTheBars(): Modifier = this.clearOfTheHardware(
    policy = HardwarePolicy(
        isCutoutIncluded = false,
        isWaterfallIncluded = false,
        areSystemBarsIncluded = true,
    ),
)

/**
 * Everything the window reports on the edge, for a control that has no corner.
 *
 * See [StepButton] for why this is the blunt instrument and the corner control
 * gets the precise one.
 */
@Composable
internal fun Modifier.clearOfEverything(): Modifier = this.clearOfTheHardware(policy = EverythingPolicy)

/** The policy behind [clearOfEverything], named so the label can ask about it too. */
internal val EverythingPolicy = HardwarePolicy(
    isCutoutIncluded = true,
    isWaterfallIncluded = true,
    areSystemBarsIncluded = true,
)

/**
 * Which end of the screen this edge puts things at.
 *
 * [toAlignment] and the label's own padding both need the answer, and when they
 * disagree the label reserves its gutter at the end it is not sitting at, which
 * runs the corner control straight through it. That is what `LEFT` did.
 */
internal val ScreenEdge.isPlacedAtTheTop: Boolean
    get() = this == ScreenEdge.TOP || this == ScreenEdge.LEFT

/**
 * The mapping every caller of this library writes for itself, because
 * [ScreenEdge] names an edge without being able to place anything at it.
 */
internal fun ScreenEdge.toAlignment(): Alignment =
    if (isPlacedAtTheTop) Alignment.TopStart else Alignment.BottomStart

internal fun ScreenEdge.cornerAlignment(isAtTheEnd: Boolean): Alignment = when (this) {
    ScreenEdge.TOP, ScreenEdge.LEFT ->
        if (isAtTheEnd) Alignment.TopEnd else Alignment.TopStart
    ScreenEdge.BOTTOM, ScreenEdge.RIGHT ->
        if (isAtTheEnd) Alignment.BottomEnd else Alignment.BottomStart
}

/**
 * The height the corner row occupies along its edge, measured from where that
 * row starts rather than from the screen.
 *
 * The tallest thing in it is the trigger: 16dp of padding, a 56dp circle, and
 * 16dp again. The bar inset underneath is added at the point of use, because
 * whether the label has already paid for it depends on the policy.
 */
private val CORNER_ROW = 88.dp

/**
 * How far in the settings button stands, as far as [edge]'s label is concerned.
 *
 * Zero unless the label shares the button's edge. The button sits at the bottom
 * whatever the label does, so a top-anchored label has no room to leave it, and
 * reserving some moved the label for a control that was not on its edge.
 *
 * Pulled out of the layout because the decision is the part that was wrong, and
 * a test of the sum below cannot reach it.
 */
internal fun settingsButtonReachOn(edge: ScreenEdge, atTheBottom: Dp): Dp =
    if (edge.isPlacedAtTheTop) 0.dp else atTheBottom

/**
 * How much further in the label has to sit to leave both corner controls alone.
 *
 * The two are counted from different origins, which is the whole difficulty:
 * the row is placed off the system bars whatever the policy says, the settings
 * button off everything the window reports, and the label off whatever the
 * policy does say. So the further of the two corner controls is [barInset] plus
 * [rowMoved], or [everythingInset], plus its own height, and the label has
 * already been moved by [applied].
 *
 * Only the difference is left to pay, and it is never negative: where the
 * policy has already moved the label past the row there is nothing to reserve.
 *
 * Kept pure because the interesting cases are ones no emulator can produce: a
 * [rowMoved] above zero needs a cutout in the corner of the anchored edge.
 */
internal fun reservedForTheCornerControls(
    barInset: Dp,
    everythingInset: Dp,
    rowMoved: Dp,
    applied: Dp,
): Dp = (maxOf(barInset + rowMoved, everythingInset) + CORNER_ROW - applied).coerceAtLeast(0.dp)

/**
 * Turns the inward distances [cornerClearance] reports into the translation
 * `Modifier.offset` wants, for a control in the [isAtTheEnd] corner of [edge].
 *
 * Kept out of the composable because no emulator can prove it: the sign is only
 * wrong where the cutout sits in the corner of the anchored edge, and no cutout
 * emulation puts one in a bottom corner. The same argument the library makes for
 * keeping its own geometry pure.
 *
 * `Modifier.offset` is direction aware, so a positive `x` already means "towards
 * the end" and the horizontal flip is the corner alone, unchanged under RTL. The
 * vertical one is not: a positive `y` is always downwards.
 */
internal fun IntOffset.awayFromTheHardware(edge: ScreenEdge, isAtTheEnd: Boolean): IntOffset =
    IntOffset(
        x = if (isAtTheEnd) -x else x,
        y = if (edge == ScreenEdge.TOP) y else -y,
    )

/**
 * How far the corner row has to move to clear the hardware in its corner, and
 * where it has got to on the way.
 *
 * Both are needed, by two different things: the row moves by [sliding], and the
 * label has to leave free whichever is larger, or it takes its space back before
 * the row has left it, or lets the spring's overshoot carry the row into it.
 */
internal class CornerMovement(val target: IntOffset, val sliding: IntOffset) {

    /** How far along its edge the row reaches, in pixels. */
    val reach: Int get() = maxOf(abs(target.y), abs(sliding.y))
}

/**
 * Where the corner row stands once the hardware in its corner is accounted for.
 *
 * Hardware in the anchored edge's corner drives the row inward on both edges,
 * towards the label either way, so the label pays the size of the move whichever
 * direction it has. That is why this is worked out here and handed to both,
 * rather than inside the row where only the row could see it.
 */
@Composable
internal fun rememberCornerMovement(
    cutout: CutoutShape,
    edge: ScreenEdge,
    isAtTheEnd: Boolean,
): CornerMovement {
    // The padding counts. A control is moved only by the cutout rectangles
    // that fall inside the width asked about, so the row's inset from the edge
    // is part of the space it occupies, not a margin outside it.
    val width = with(LocalDensity.current) { (CORNER_PADDING + ACTIONS_WIDTH).roundToPx() }
    val clearance = cornerClearance(
        cutout.bounds,
        position = edge,
        // The corner asked about and the corner placed in come from one value.
        // Asking about one while placing at the other returns a confident zero,
        // which looks exactly like "no hardware here", and nothing in the API
        // can catch it.
        isAtTheEnd = isAtTheEnd,
    )
    val target = clearance(width).awayFromTheHardware(edge, isAtTheEnd = isAtTheEnd)
    // The one authored movement: the row slides clear rather than teleporting,
    // so it is legible that the hardware is what moved it. Animated here because
    // the label reserves what the row is using now, not what it will be using
    // once the spring settles.
    val sliding by animateIntOffsetAsState(
        targetValue = target,
        animationSpec = spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMediumLow),
        label = "clearance",
    )
    return CornerMovement(target = target, sliding = sliding)
}
