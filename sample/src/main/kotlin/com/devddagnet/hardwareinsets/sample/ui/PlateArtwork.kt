package com.devddagnet.hardwareinsets.sample.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.devddagnet.hardwareinsets.sample.R
import com.devddagnet.hardwareinsets.sample.model.Plate

/**
 * A plate, full bleed, under everything else on the screen.
 *
 * Cropped rather than fitted, which is the whole reason these are exported at
 * one tall size instead of being dropped in as assets at whatever size they
 * came at: a fitted image letterboxes, and the letterboxing would become the
 * margin this library exists to do without.
 *
 * The description replaces the node's children rather than adding to them: a
 * painting has no parts a screen reader can usefully walk.
 */
@Composable
internal fun PlateArtwork(plate: Plate, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(plate.artwork),
        contentDescription = stringResource(plate.description),
        contentScale = ContentScale.Crop,
        modifier = modifier.fillMaxSize(),
    )
}

/**
 * Tap the plate to put the controls away: the gesture every full-screen viewer
 * has, and the one that leaves the plate alone under the camera.
 *
 * A click label rather than a bare handler, because with the controls gone
 * there is nothing left on the screen for a screen reader to describe.
 */
@Composable
internal fun Modifier.togglingTheControls(
    areControlsShown: Boolean,
    onToggle: () -> Unit,
): Modifier {
    val label =
        stringResource(if (areControlsShown) R.string.hide_the_label else R.string.show_the_label)
    return clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClickLabel = label,
        onClick = onToggle,
    )
}
