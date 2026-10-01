package io.github.damson.hardwareinsets.sample.ui

import android.graphics.Rect
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.damson.hardwareinsets.domain.CutoutShape

/**
 * Hardware to lay a preview out against, which is the argument this library
 * makes: [CutoutShape] is a value, so a phone you do not own is four numbers.
 *
 * A preview window reports no cutout of its own whatever device is named in the
 * annotation, so without these every preview would be the empty case. The
 * pixels are what a Pixel-sized window reports, so a preview at that size puts
 * the marker where the hardware would be.
 */
internal object PreviewHardware {

    /** A phone with nothing in the way, which is still a case worth seeing. */
    val none = CutoutShape()

    /** A camera in the top right, the case `cornerClearance` exists for. */
    val cameraInTheCorner = CutoutShape(
        bounds = listOf(Rect(954, 0, 1080, 126)),
        topInset = 126,
    )

    /**
     * A punch-hole in the middle of the top edge.
     *
     * The same 126 pixels of top inset as the corner camera, and nothing in the
     * corner: a control that pads by the inset moves for both, and one that
     * asks about the rectangles moves for one.
     */
    val centredPunchHole = CutoutShape(
        bounds = listOf(Rect(477, 0, 603, 126)),
        topInset = 126,
    )

    /** Curved sides and no camera at all: insets with no rectangle behind them. */
    val curvedEdges = CutoutShape(leftInset = 44, rightInset = 44)
}

/**
 * The frame every preview in this module is drawn in.
 *
 * The theme, and a background, because a plaque is translucent and reads as
 * nothing at all over the tooling's default white.
 */
@Composable
internal fun SamplePreview(
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    content: @Composable () -> Unit,
) {
    SampleTheme {
        Box(
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = alignment,
        ) {
            content()
        }
    }
}
