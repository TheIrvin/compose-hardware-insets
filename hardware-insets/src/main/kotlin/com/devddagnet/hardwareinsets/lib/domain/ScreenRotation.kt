package com.devddagnet.hardwareinsets.lib.domain

/**
 * How far the device is turned from its natural orientation.
 *
 * A named quarter turn rather than the `Surface.ROTATION_*` constant it comes
 * from, because that constant is an `Int` and so is everything else a caller
 * has to hand. `Configuration.ORIENTATION_LANDSCAPE` is 2, and so is
 * `Surface.ROTATION_180`: passing the wrong one compiles, runs, and answers
 * with the wrong edge. Naming the rotation is what makes that a typing error
 * instead of a silent one.
 *
 * Read one off a `Display` with `screenRotation`.
 */
enum class ScreenRotation {
    /** The natural orientation, whichever way round that is for the device. */
    NONE,

    /** A quarter turn anticlockwise, `Surface.ROTATION_90`. */
    QUARTER,

    /** Upside down, `Surface.ROTATION_180`. */
    HALF,

    /** A quarter turn clockwise, `Surface.ROTATION_270`. */
    THREE_QUARTERS,
}
