package com.devddagnet.hardwareinsets.sample.model

import androidx.compose.runtime.Immutable
import com.devddagnet.hardwareinsets.lib.domain.HardwarePolicy
import com.devddagnet.hardwareinsets.lib.domain.ScreenEdge
import com.devddagnet.hardwareinsets.lib.domain.onScreenAt
import com.devddagnet.hardwareinsets.lib.domain.CutoutMode

/**
 * Every decision the library leaves to its caller, in one place.
 *
 * The sample exists to make each of them visible, so each one is here rather
 * than fixed in a call site: a demo that hard-codes a parameter is not
 * demonstrating it.
 *
 * @param anchor the edge the caption and the corner control are anchored to.
 *   `LEFT` and `RIGHT` are edges of the device, so what the layout uses is
 *   `anchor.onScreenAt(rotation)`, not this.
 * @param isCornerAtTheEnd which corner of that edge the control sits in. It has
 *   to match what `cornerClearance` is asked, and nothing but this enforces it.
 * @param policy which of the window's insets count as hardware.
 * @param isFarEdgeIgnored whether the edge opposite [anchor] is forced to zero.
 * @param cutoutMode how far into the cutout the window may extend.
 * @param areBarsHidden whether the system bars are off screen.
 * @param areMarkersShown whether the cutout rectangles and inset depths are
 *   drawn over the picture.
 */
@Immutable
data class ViewerOptions(
    val anchor: ScreenEdge = ScreenEdge.TOP,
    val isCornerAtTheEnd: Boolean = false,
    val policy: HardwarePolicy = HardwarePolicy(),
    val isFarEdgeIgnored: Boolean = false,
    val cutoutMode: CutoutMode = CutoutMode.ALWAYS,
    val areBarsHidden: Boolean = false,
    val areMarkersShown: Boolean = true,
)
