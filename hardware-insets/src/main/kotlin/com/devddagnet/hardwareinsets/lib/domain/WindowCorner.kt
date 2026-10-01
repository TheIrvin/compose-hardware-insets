package com.devddagnet.hardwareinsets.lib.domain

/**
 * Which corner of the window a control is tucked into.
 *
 * Named by the physical edges rather than by start and end, because a camera
 * is at a place on the screen and does not move with the layout direction.
 * Under a right-to-left layout the start corner is `TOP_RIGHT`.
 */
enum class WindowCorner {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT,
    ;

    internal val isAtTheTop: Boolean get() = this == TOP_LEFT || this == TOP_RIGHT

    internal val isAtTheRight: Boolean get() = this == TOP_RIGHT || this == BOTTOM_RIGHT
}
