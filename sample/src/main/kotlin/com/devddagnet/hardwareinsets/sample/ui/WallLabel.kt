package com.devddagnet.hardwareinsets.sample.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.devddagnet.hardwareinsets.lib.clearOfTheHardware
import com.devddagnet.hardwareinsets.lib.domain.ScreenEdge
import com.devddagnet.hardwareinsets.lib.hardwareInsets
import com.devddagnet.hardwareinsets.sample.R
import com.devddagnet.hardwareinsets.sample.model.Plate
import com.devddagnet.hardwareinsets.sample.model.Plates
import com.devddagnet.hardwareinsets.sample.model.ViewerOptions
import kotlin.math.roundToInt

/**
 * The wall label: what the plate is, and where the library has put it.
 *
 * The inset goes outside the plaque, so the plaque moves clear of the hardware
 * rather than growing a transparent margin inside itself.
 *
 * @param rowMoved how far the corner row has been driven inward by hardware in
 *   its corner, which is room this label has to leave it.
 * @param shake where in its swing the favourite nudge is, in -1..1. The label
 *   answers the heart because it is the only thing on screen naming the plate
 *   the heart was pressed for.
 */
@Composable
internal fun WallLabel(
    plate: Plate,
    number: Int,
    total: Int,
    edge: ScreenEdge,
    options: ViewerOptions,
    shake: Float,
    rowMoved: Dp,
    modifier: Modifier = Modifier,
) {
    val swing = with(LocalDensity.current) { SHAKE_SWING.toPx() }

    val density = LocalDensity.current
    val bars = WindowInsets.systemBars.asPaddingValues()
    val applied = with(density) {
        hardwareInsets(edge, options.policy, options.isFarEdgeIgnored).let {
            if (edge.isPlacedAtTheTop) it.getTop(density).toDp() else it.getBottom(density).toDp()
        }
    }
    val fromTheEdge =
        if (edge.isPlacedAtTheTop) bars.calculateTopPadding() else bars.calculateBottomPadding()
    // The two corner controls do not sit off the same thing. The row clears the
    // bars; the settings button clears everything, so with the bars hidden and
    // a cutout on this edge it is the one standing further in.
    //
    // The button stays on the bottom edge whatever the label does, so it only
    // competes with a label anchored there. Counting it at the top reserved
    // room for a control that is not on that edge, and the label answered a
    // policy change it had no business answering.
    val settingsButton = settingsButtonReachOn(
        edge,
        with(density) { hardwareInsets(edge, EverythingPolicy).getBottom(density).toDp() },
    )
    val reserved = reservedForTheCornerControls(fromTheEdge, settingsButton, rowMoved, applied)

    Box(
        modifier
            .offset { IntOffset((shake * swing).roundToInt(), 0) }
            .fillMaxWidth()
            .clearOfTheHardware(
                position = edge,
                policy = options.policy,
                isFarEdgeIgnored = options.isFarEdgeIgnored,
            )
            // Inboard of the corner row rather than beside it: both corners of
            // an edge can be occupied, and a gutter for each leaves the label
            // about 90dp wide.
            .padding(
                top = if (edge.isPlacedAtTheTop) reserved else 12.dp,
                bottom = if (edge.isPlacedAtTheTop) 12.dp else reserved,
                start = 16.dp,
                end = 16.dp,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier
                .widthIn(max = 460.dp)
                .plaque(MaterialTheme.shapes.large)
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Text(
                stringResource(plate.title),
                color = MaterialTheme.onPlaque,
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(plate.maker),
                color = MaterialTheme.onPlaqueVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                stringResource(plate.medium),
                color = MaterialTheme.onPlaqueVariant.copy(alpha = 0.82f),
                style = MaterialTheme.typography.bodySmall,
            )
            HorizontalDivider(
                Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.onPlaque.copy(alpha = 0.22f),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                PlateRail(number = number, total = total)
                Text(
                    anchorReadout(options, edge),
                    color = MaterialTheme.onPlaqueVariant,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f).padding(start = 16.dp),
                )
            }
        }
    }
}

/**
 * Where you are in the gallery, and that there is somewhere else to be.
 *
 * The row carries the count for a screen reader, because four shapes do not.
 */
@Composable
private fun PlateRail(number: Int, total: Int, modifier: Modifier = Modifier) {
    val description = stringResource(R.string.plate_rail, number, total)
    Row(
        modifier.clearAndSetSemantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(total) { index ->
            val isCurrent = index == number - 1
            Box(
                Modifier
                    .size(width = if (isCurrent) 20.dp else 8.dp, height = 8.dp)
                    .background(
                        color =
                            if (isCurrent) MaterialTheme.accent
                            else MaterialTheme.onPlaqueVariant.copy(alpha = 0.45f),
                        shape = CircleShape,
                    )
            )
        }
    }
}

/**
 * The half of the label that belongs to the library rather than to the plate.
 *
 * Both names are shown whenever they differ, because "anchored to LEFT, now
 * BOTTOM" is the sentence that explains why a side anchor moves when the device
 * turns, and a reader who only sees the second one reads it as a bug.
 */
@Composable
private fun anchorReadout(options: ViewerOptions, edge: ScreenEdge): String =
    if (options.anchor == edge) stringResource(R.string.anchored_to, options.anchor.name)
    else stringResource(R.string.anchored_to_now, options.anchor.name, edge.name)

private val SHAKE_SWING = 12.dp

/**
 * Where the label is in its swing, in -1..1, driven from outside it.
 *
 * Hoisted out of [WallLabel] because the label leaves composition when the
 * controls hide, and an animation held there restarts on every later reveal.
 *
 * @param shakes how many times the heart has been pressed. A count rather than
 *   a flag: pressed twice quickly, the label has to answer twice.
 */
@Composable
internal fun rememberLabelShake(shakes: Int): Float {
    val nudge = remember { Animatable(0f) }
    LaunchedEffect(shakes) {
        // Not on the first composition, or it shakes itself hello on open.
        if (shakes == 0) return@LaunchedEffect
        nudge.snapTo(0f)
        nudge.animateTo(
            targetValue = 0f,
            animationSpec = keyframes {
                durationMillis = SHAKE_MILLIS
                0f at 0
                -1f at SHAKE_MILLIS * 16 / 100
                0.78f at SHAKE_MILLIS * 34 / 100
                -0.48f at SHAKE_MILLIS * 52 / 100
                0.24f at SHAKE_MILLIS * 72 / 100
                0f at SHAKE_MILLIS
            },
        )
    }
    return nudge.value
}

private const val SHAKE_MILLIS = 420

@Preview(name = "Label, anchored to the top", widthDp = 400)
@Composable
private fun WallLabelPreview() = SamplePreview(alignment = Alignment.TopStart) {
    WallLabel(
        plate = Plates.first(),
        number = 1,
        total = Plates.size,
        edge = ScreenEdge.TOP,
        options = ViewerOptions(),
        shake = 0f,
        rowMoved = 0.dp,
    )
}

@Preview(name = "Label, anchored to the bottom", widthDp = 400)
@Composable
private fun WallLabelAtTheBottomPreview() = SamplePreview(alignment = Alignment.BottomStart) {
    WallLabel(
        plate = Plates.first(),
        number = 1,
        total = Plates.size,
        edge = ScreenEdge.BOTTOM,
        options = ViewerOptions(anchor = ScreenEdge.BOTTOM),
        shake = 0f,
        rowMoved = 0.dp,
    )
}

@Preview(name = "Label, leaving room for a corner row on hardware", widthDp = 400)
@Composable
private fun WallLabelBesideTheCornerPreview() = SamplePreview(alignment = Alignment.TopStart) {
    WallLabel(
        plate = Plates.first(),
        number = 1,
        total = Plates.size,
        edge = ScreenEdge.TOP,
        options = ViewerOptions(),
        shake = 0f,
        rowMoved = 126.dp,
    )
}

@Preview(name = "Label, mid shake", widthDp = 400)
@Composable
private fun WallLabelShakingPreview() = SamplePreview(alignment = Alignment.TopStart) {
    WallLabel(
        plate = Plates.first(),
        number = 1,
        total = Plates.size,
        edge = ScreenEdge.TOP,
        options = ViewerOptions(),
        shake = -1f,
        rowMoved = 0.dp,
    )
}
