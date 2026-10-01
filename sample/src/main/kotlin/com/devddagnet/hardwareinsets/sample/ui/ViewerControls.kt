package com.devddagnet.hardwareinsets.sample.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.devddagnet.hardwareinsets.lib.cornerClearance
import com.devddagnet.hardwareinsets.sample.R

/** The way into the sheet: the only control here that demonstrates nothing. */
@Composable
internal fun OpenControls(onOpen: () -> Unit, modifier: Modifier = Modifier) {
    FloatingActionButton(
        onClick = onOpen,
        shape = CircleShape,
        containerColor = MaterialTheme.accent,
        contentColor = MaterialTheme.onAccent,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp,
        ),
        modifier = modifier
            .padding(16.dp)
            .lifted(CircleShape)
            .border(1.dp, MaterialTheme.onAccent.copy(alpha = 0.18f), CircleShape),
    ) {
        Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.open_controls))
    }
}

/**
 * Previous and next, halfway down the sides.
 *
 * They take the whole inset rather than a rectangle-precise offset. A corner
 * control can ask [cornerClearance] which rectangles are actually in its way;
 * a button halfway down a side has no corner to be measured from, so it clears
 * everything the window reports on that edge, bars included.
 */
@Composable
internal fun StepButton(
    icon: ImageVector,
    label: Int,
    isEnabled: Boolean,
    onStep: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.plaque,
        contentColor = MaterialTheme.onPlaque,
        border = BorderStroke(1.dp, MaterialTheme.onPlaque.copy(alpha = 0.22f)),
        modifier = modifier
            .padding(16.dp)
            .lifted(CircleShape),
    ) {
        IconButton(onClick = onStep, enabled = isEnabled) {
            Icon(
                imageVector = icon,
                contentDescription = stringResource(label),
                tint =
                    if (isEnabled) MaterialTheme.onPlaque
                    else MaterialTheme.onPlaque.copy(alpha = 0.34f),
            )
        }
    }
}

@Preview(name = "Settings trigger")
@Composable
private fun OpenControlsPreview() = SamplePreview { OpenControls(onOpen = {}) }

@Preview(name = "Step, enabled")
@Composable
private fun StepButtonPreview() = SamplePreview {
    StepButton(Icons.Filled.KeyboardArrowRight, R.string.next_plate, isEnabled = true, onStep = {})
}

@Preview(name = "Step, at the end of the gallery")
@Composable
private fun StepButtonDisabledPreview() = SamplePreview {
    StepButton(Icons.Filled.KeyboardArrowRight, R.string.next_plate, isEnabled = false, onStep = {})
}
