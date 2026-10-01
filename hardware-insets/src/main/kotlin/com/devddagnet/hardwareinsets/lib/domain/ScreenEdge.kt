package com.devddagnet.hardwareinsets.lib.domain

/**
 * An edge content can be anchored to.
 *
 * [TOP] and [BOTTOM] name the screen's own edges and never move. [LEFT] and
 * [RIGHT] name the *device's*, held upright: anchoring to one turns the screen
 * so that edge becomes a long one, and [onScreenAt] says which screen edge it
 * has become.
 *
 * **Treat these names as a stored format the moment you persist one.** Writing
 * [name] and resolving it back is the obvious way to remember a choice, and
 * renaming a constant then discards every stored value in silence: the old
 * string stops resolving and reads as nothing having been chosen. Pin the four
 * spellings in a test of your own if you store them.
 */
enum class ScreenEdge {
    TOP,
    BOTTOM,
    LEFT,
    RIGHT,
    ;

    val isHorizontalEdge: Boolean get() = this == TOP || this == BOTTOM

    val opposite: ScreenEdge
        get() = when (this) {
            TOP -> BOTTOM
            BOTTOM -> TOP
            LEFT -> RIGHT
            RIGHT -> LEFT
        }
}

/**
 * Which edge of the screen a chosen edge of the device has become.
 *
 * @param rotation how far the device is turned from its natural orientation,
 *   which a caller reads off its own display with `Display.screenRotation`.
 * @return the screen edge this one has become. Always a horizontal edge, since
 *   a side only lays out along one.
 */
fun ScreenEdge.onScreenAt(rotation: ScreenRotation): ScreenEdge = when {
    isHorizontalEdge -> this
    // Turned anticlockwise: the phone's bottom edge is on the right, so its
    // left edge is along the bottom of what the user sees.
    rotation == ScreenRotation.QUARTER -> if (this == ScreenEdge.LEFT) {
        ScreenEdge.BOTTOM
    } else {
        ScreenEdge.TOP
    }
    rotation == ScreenRotation.THREE_QUARTERS -> if (this == ScreenEdge.LEFT) {
        ScreenEdge.TOP
    } else {
        ScreenEdge.BOTTOM
    }
    // Upright or upside down, where a side edge is still a side. A horizontal
    // edge rather than the side it is, because a caller laying out along this
    // edge has to be given one it can lay out along.
    else -> ScreenEdge.BOTTOM
}
