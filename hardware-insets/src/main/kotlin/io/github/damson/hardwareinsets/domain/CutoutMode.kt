package io.github.damson.hardwareinsets.domain

/**
 * How far the window is allowed to extend into the display cutout.
 *
 * The platform constant this maps to is only honoured from API 28, and
 * [ALWAYS] only exists from API 30, so each case says what it does below that.
 */
enum class CutoutMode {
    /**
     * Into the cutout on every edge, falling back to [SHORT_EDGES] below API 30.
     *
     * The right answer for a surface that fills the window, and the wrong one
     * for most apps: content lands under the camera unless something insets it.
     */
    ALWAYS,

    SHORT_EDGES,
    DEFAULT,
    NEVER,
}
