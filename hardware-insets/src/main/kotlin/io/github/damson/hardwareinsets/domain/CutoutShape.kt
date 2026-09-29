package io.github.damson.hardwareinsets.domain

import android.graphics.Rect

/**
 * What the hardware takes out of the window: where the cameras physically are,
 * and how deep into each edge the platform says content must not go.
 *
 * Neither substitutes for the other: a rectangle is *where* a camera is, which a
 * corner control needs; an inset is *whether* an edge has hardware at all, which
 * choosing an edge needs, and it can be read during composition when the window
 * still measures zero.
 */
data class CutoutShape(
    /**
     * Where the cameras are, in pixels, in the coordinate space of the window
     * rather than of any view inside it. Empty below API 28, where the platform
     * reports no cutout at all, and empty on hardware that has none.
     */
    val bounds: List<Rect> = emptyList(),
    /** How far content must stay off the top edge, in pixels. */
    val topInset: Int = 0,
    /** How far content must stay off the bottom edge, in pixels. */
    val bottomInset: Int = 0,
    /** How far content must stay off the left edge, in pixels. */
    val leftInset: Int = 0,
    /** How far content must stay off the right edge, in pixels. */
    val rightInset: Int = 0,
)
