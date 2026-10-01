package io.github.damson.hardwareinsets.sample.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.github.damson.hardwareinsets.domain.CutoutShape
import io.github.damson.hardwareinsets.domain.ScreenEdge
import io.github.damson.hardwareinsets.domain.ScreenRotation
import io.github.damson.hardwareinsets.domain.onScreenAt
import io.github.damson.hardwareinsets.sample.R
import io.github.damson.hardwareinsets.sample.model.Plates
import io.github.damson.hardwareinsets.sample.model.ViewerOptions
import kotlinx.coroutines.launch

/**
 * A gallery: one plate at a time, running to every edge, swipe for the next.
 *
 * The use case rather than a demonstration of it. A viewer is where full bleed
 * is the point and not a style, so it is also where the hardware is a real
 * problem: the plate is meant to be under the camera, and the wall label is
 * meant not to be.
 *
 * @param cutout the live shape from the activity's sibling view. Passed in
 *   rather than read here, so this stays a function of its input.
 * @param rotation where the display is turned to now, watched by the activity
 *   for the same reason. A landscape flip fires no configuration change, so a
 *   screen that read this once would go on laying out for the old one.
 * @param onBarsOver called with how pale the plate now showing is at the top
 *   and at the bottom. The system bars draw their icons over it, and with a
 *   transparent bar the platform has no idea what is underneath.
 * @param onShare called with the line that introduces the library. Sending it
 *   anywhere is an `Intent`, which is the activity's business and not this
 *   function's.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewerScreen(
    cutout: CutoutShape,
    options: ViewerOptions,
    rotation: ScreenRotation,
    onOptions: (ViewerOptions) -> Unit,
    onBarsOver: (isPaleAtTheTop: Boolean, isPaleAtTheBottom: Boolean) -> Unit,
    onShare: (String) -> Unit,
) {
    var isSheetOpen by remember { mutableStateOf(false) }
    // The label, the step buttons, the corner row and the trigger, which go
    // together: one tap on the plate leaves the painting and nothing else.
    //
    // Saved, not remembered: rotation recreates the activity, and rotation is
    // half of what this screen demonstrates. The sheet is left out on purpose.
    var areControlsShown by rememberSaveable { mutableStateOf(true) }
    var favourites by rememberSaveable(stateSaver = FavouritesSaver) {
        mutableStateOf(emptySet<Int>())
    }
    // LEFT and RIGHT are edges of the device, not of the screen, so a layout
    // cannot use one directly: it has to ask which screen edge the device has
    // turned that one into. Skipping this is what makes a side-anchored control
    // land on top of the status bar.
    val edge = options.anchor.onScreenAt(rotation)
    val pager = rememberPagerState(pageCount = { Plates.size })
    val plate = Plates[pager.currentPage]
    val repoPitch = stringResource(R.string.share_repo)
    var shakes by remember { mutableIntStateOf(0) }
    val steps = rememberCoroutineScope()
    val shake = rememberLabelShake(shakes)
    val corner = rememberCornerMovement(cutout, edge, options.isCornerAtTheEnd)

    LaunchedEffect(plate) { onBarsOver(plate.isPaleAtTheTop, plate.isPaleAtTheBottom) }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .markers(cutout, options.areMarkersShown)
    ) {
        HorizontalPager(state = pager, modifier = Modifier.fillMaxSize()) { page ->
            PlateArtwork(
                plate = Plates[page],
                modifier = Modifier.togglingTheControls(areControlsShown) {
                    areControlsShown = !areControlsShown
                },
            )
        }

        AnimatedVisibility(
            visible = areControlsShown,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(edge.toAlignment()).fillMaxWidth(),
        ) {
            WallLabel(
                plate = plate,
                number = pager.currentPage + 1,
                total = Plates.size,
                edge = edge,
                options = options,
                shake = shake,
                rowMoved = with(LocalDensity.current) { corner.reach.toDp() },
            )
        }
        // A half-height sheet's top edge lands exactly where these sit, so
        // leaving them up gets you two buttons sawn in half.
        AnimatedVisibility(
            visible = areControlsShown && !isSheetOpen,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.CenterStart).clearOfEverything(),
        ) {
            StepButton(
                icon = Icons.Filled.KeyboardArrowLeft,
                label = R.string.previous_plate,
                isEnabled = pager.currentPage > 0,
                onStep = { steps.launch { pager.animateScrollToPage(pager.currentPage - 1) } },
            )
        }
        AnimatedVisibility(
            visible = areControlsShown && !isSheetOpen,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.CenterEnd).clearOfEverything(),
        ) {
            StepButton(
                icon = Icons.Filled.KeyboardArrowRight,
                label = R.string.next_plate,
                isEnabled = pager.currentPage < Plates.size - 1,
                onStep = { steps.launch { pager.animateScrollToPage(pager.currentPage + 1) } },
            )
        }
        AnimatedVisibility(
            visible = areControlsShown,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(edge.cornerAlignment(options.isCornerAtTheEnd))
                .clearOfTheBars(),
        ) {
            CornerActions(
                moved = corner.sliding,
                edge = edge,
                options = options,
                isFavourite = pager.currentPage in favourites,
                onFavourite = { isOn ->
                    favourites =
                        if (isOn) favourites + pager.currentPage
                        else favourites - pager.currentPage
                    // A counter rather than a flag: pressed twice quickly, it
                    // has to shake twice.
                    shakes++
                },
                onShare = { onShare(repoPitch) },
            )
        }
        AnimatedVisibility(
            // Gone while the sheet is open: it is the thing that opened it,
            // and a see-through sheet would show it sitting behind itself.
            visible = areControlsShown && !isSheetOpen,
            enter = fadeIn(),
            exit = fadeOut(),
            // The demo puts its own control in a corner of whichever edge you
            // pick, so a fixed trigger collides in exactly one configuration.
            //
            // Clear of everything rather than of the bars alone: this is the
            // way back to the controls, and a corner camera or a curved edge
            // under it is a button the visitor cannot reliably press. The
            // corner row is the one demonstrating a rectangle-precise offset;
            // this one only has to be reachable.
            modifier = Modifier
                .align(
                    if (edge == ScreenEdge.BOTTOM && options.isCornerAtTheEnd) Alignment.BottomStart
                    else Alignment.BottomEnd
                )
                .clearOfEverything(),
        ) {
            OpenControls(onOpen = { isSheetOpen = true })
        }
    }

    if (isSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { isSheetOpen = false },
            // Half height first, so a change to a top-anchored control is visible
            // while it is being made. Drag up for the rest.
            sheetState = rememberModalBottomSheetState(),
            // No scrim: every control on this sheet changes the screen behind
            // it, so dimming the plate to ask about the plate hides the answer.
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = SHEET_ALPHA),
            // `contentColorFor` answers `Unspecified` for a colour that is not
            // a scheme role, and the text would then take whatever is ambient.
            contentColor = MaterialTheme.colorScheme.onSurface,
            scrimColor = Color.Transparent,
        ) {
            ControlSheetContent(cutout, options, onOptions)
        }
    }
}

/** How solid the sheet's frame is. It can be glass because no text sits on it. */
internal const val SHEET_ALPHA = 0.55f

/**
 * A [Set] is not one of the types a `Bundle` carries, so the favourites travel
 * as the list they came from.
 */
private val FavouritesSaver = listSaver<Set<Int>, Int>(
    save = { it.toList() },
    restore = { it.toSet() },
)

/**
 * The whole screen against hardware the preview does not have.
 *
 * A preview window reports no cutout, so the marker overlay would be empty and
 * `cornerClearance` would have nothing to move for. Handing the shape in as a
 * value is what makes the three below different from each other, and it is the
 * same move a test makes: see `CornerOffsetTest`.
 */
@Preview(name = "A camera in the corner", showSystemUi = true)
@Composable
private fun ViewerScreenPreview() = SampleTheme {
    ViewerScreen(
        cutout = PreviewHardware.cameraInTheCorner,
        options = ViewerOptions(),
        rotation = ScreenRotation.NONE,
        onOptions = {},
        onBarsOver = { _, _ -> },
        onShare = {},
    )
}

@Preview(name = "A punch-hole in the middle", showSystemUi = true)
@Composable
private fun ViewerScreenCentredHolePreview() = SampleTheme {
    ViewerScreen(
        cutout = PreviewHardware.centredPunchHole,
        options = ViewerOptions(),
        rotation = ScreenRotation.NONE,
        onOptions = {},
        onBarsOver = { _, _ -> },
        onShare = {},
    )
}

@Preview(name = "Curved edges, no camera", showSystemUi = true)
@Composable
private fun ViewerScreenCurvedPreview() = SampleTheme {
    ViewerScreen(
        cutout = PreviewHardware.curvedEdges,
        options = ViewerOptions(anchor = ScreenEdge.BOTTOM),
        rotation = ScreenRotation.NONE,
        onOptions = {},
        onBarsOver = { _, _ -> },
        onShare = {},
    )
}

@Preview(name = "Nothing in the way", showSystemUi = true)
@Composable
private fun ViewerScreenWithoutHardwarePreview() = SampleTheme {
    ViewerScreen(
        cutout = PreviewHardware.none,
        options = ViewerOptions(areMarkersShown = false),
        rotation = ScreenRotation.NONE,
        onOptions = {},
        onBarsOver = { _, _ -> },
        onShare = {},
    )
}
