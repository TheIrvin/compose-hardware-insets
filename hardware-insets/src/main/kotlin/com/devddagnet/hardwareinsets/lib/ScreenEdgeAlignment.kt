package com.devddagnet.hardwareinsets.lib

import androidx.compose.ui.Alignment
import com.devddagnet.hardwareinsets.lib.domain.ScreenEdge
import com.devddagnet.hardwareinsets.lib.domain.onScreenAt

/**
 * The Compose alignment for a horizontal screen edge.
 *
 * A device edge such as [ScreenEdge.LEFT] or [ScreenEdge.RIGHT] must first be
 * mapped through [onScreenAt]; those edges do not have an unambiguous placement
 * on screen. [isAtTheEnd] uses Compose's logical end, so the alignment follows
 * the current layout direction.
 *
 * @throws IllegalArgumentException when this is a vertical device edge rather
 *   than a horizontal screen edge.
 */
fun ScreenEdge.toAlignment(isAtTheEnd: Boolean = false): Alignment = when (this) {
    ScreenEdge.TOP -> if (isAtTheEnd) Alignment.TopEnd else Alignment.TopStart
    ScreenEdge.BOTTOM -> if (isAtTheEnd) Alignment.BottomEnd else Alignment.BottomStart
    ScreenEdge.LEFT, ScreenEdge.RIGHT -> throw IllegalArgumentException(
        "Map a device edge with onScreenAt() before converting it to an alignment",
    )
}
