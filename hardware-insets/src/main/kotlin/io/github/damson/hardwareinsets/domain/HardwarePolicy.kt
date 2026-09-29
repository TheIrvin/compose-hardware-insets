package io.github.damson.hardwareinsets.domain

import androidx.compose.runtime.Immutable

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
