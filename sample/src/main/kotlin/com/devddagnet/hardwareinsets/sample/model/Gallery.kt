package com.devddagnet.hardwareinsets.sample.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.devddagnet.hardwareinsets.sample.R

/**
 * One work in the gallery: the wall label, and the plate it hangs beside.
 *
 * @param artwork the plate itself, exported from the generator that paints it
 *   rather than drawn here. The sample is about insets and nothing else; a
 *   few hundred lines of painting in it would be a second subject competing
 *   with the first.
 * @param isPaleAtTheTop whether the status bar's icons have to be dark to be
 *   seen over this plate.
 * @param isPaleAtTheBottom the same question for the navigation bar. Asked per
 *   edge and not per plate, because a plate can be sky at the top and water at
 *   the bottom, and one answer for both loses one of the two bars.
 */
@Immutable
internal class Plate(
    @StringRes val title: Int,
    @StringRes val maker: Int,
    @StringRes val medium: Int,
    @StringRes val description: Int,
    @DrawableRes val artwork: Int,
    val isPaleAtTheTop: Boolean,
    val isPaleAtTheBottom: Boolean,
)

/**
 * The works, in hanging order.
 *
 * Some are pale and some are dark on purpose: a gallery of one or the other
 * would never show what the system bars do over the plate behind them.
 */
internal val Plates = listOf(
    Plate(
        title = R.string.plate_tide_title,
        maker = R.string.plate_maker,
        medium = R.string.plate_tide_medium,
        description = R.string.plate_tide_description,
        artwork = R.drawable.plate_low_tide,
        isPaleAtTheTop = true,
        isPaleAtTheBottom = false,
    ),
    Plate(
        title = R.string.plate_head_title,
        maker = R.string.plate_maker,
        medium = R.string.plate_head_medium,
        description = R.string.plate_head_description,
        artwork = R.drawable.plate_head_counted,
        isPaleAtTheTop = false,
        isPaleAtTheBottom = false,
    ),
    Plate(
        title = R.string.plate_ledger_title,
        maker = R.string.plate_maker,
        medium = R.string.plate_ledger_medium,
        description = R.string.plate_ledger_description,
        artwork = R.drawable.plate_ledger_of_metals,
        isPaleAtTheTop = true,
        isPaleAtTheBottom = true,
    ),
    Plate(
        title = R.string.plate_arms_title,
        maker = R.string.plate_maker,
        medium = R.string.plate_arms_medium,
        description = R.string.plate_arms_description,
        artwork = R.drawable.plate_arms_raised,
        isPaleAtTheTop = false,
        isPaleAtTheBottom = false,
    ),
    Plate(
        title = R.string.plate_crown_title,
        maker = R.string.plate_maker,
        medium = R.string.plate_crown_medium,
        description = R.string.plate_crown_description,
        artwork = R.drawable.plate_crown_crossed_out,
        isPaleAtTheTop = false,
        isPaleAtTheBottom = false,
    ),
)
