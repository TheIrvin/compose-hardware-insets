package com.devddagnet.hardwareinsets.lib.domain

/**
 * What a hidden system bar does when the user reaches for it.
 *
 * Named rather than passed as the controller's `BEHAVIOR_*` constant, so it
 * cannot be transposed with the inset types beside it: both were `Int`, from
 * two different namespaces, and the compiler had nothing to say about them
 * being the wrong way round.
 */
enum class BarBehavior {
    /** A swipe brings the bars back for a moment, then they leave again. */
    SHOW_TRANSIENT_ON_SWIPE,

    /** Whatever the platform does by default for the window's setup. */
    PLATFORM_DEFAULT,
}
