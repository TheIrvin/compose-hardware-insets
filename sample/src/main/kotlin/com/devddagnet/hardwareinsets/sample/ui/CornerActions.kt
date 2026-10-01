package com.devddagnet.hardwareinsets.sample.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.devddagnet.hardwareinsets.lib.domain.ScreenEdge
import com.devddagnet.hardwareinsets.sample.R
import com.devddagnet.hardwareinsets.sample.model.ViewerOptions

/**
 * The corner control: favourite this plate, or send it on.
 *
 * The two things a gallery puts next to a picture, and the reason the corner is
 * worth defending at all. A control that only closed the screen could be moved
 * anywhere, or dropped for the back gesture, which is what closes this one.
 *
 * Moved by only the cutout rectangles it actually overlaps. Padding it by the
 * full-width inset instead would drop it clear of a centred punch-hole it is
 * nowhere near, which is the whole reason this library exists.
 */
@Composable
internal fun CornerActions(
    moved: IntOffset,
    edge: ScreenEdge,
    options: ViewerOptions,
    isFavourite: Boolean,
    onFavourite: (Boolean) -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.plaque,
        contentColor = MaterialTheme.onPlaque,
        border = BorderStroke(1.dp, MaterialTheme.onPlaque.copy(alpha = 0.22f)),
        modifier = modifier
            .padding(CORNER_PADDING)
            .offset { moved }
            .lifted(MaterialTheme.shapes.extraLarge)
            .sizeIn(minWidth = ACTIONS_WIDTH, minHeight = 48.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconToggleButton(checked = isFavourite, onCheckedChange = onFavourite) {
                Icon(
                    imageVector =
                        if (isFavourite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = stringResource(
                        if (isFavourite) R.string.remove_from_favourites
                        else R.string.add_to_favourites
                    ),
                    tint =
                        if (isFavourite) MaterialTheme.accent
                        else MaterialTheme.onPlaque.copy(alpha = 0.78f),
                )
            }
            IconButton(onClick = onShare) {
                Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.share))
            }
        }
    }
}

internal val ACTIONS_WIDTH = 112.dp

internal val CORNER_PADDING = 16.dp

@Preview(name = "Corner row")
@Composable
private fun CornerActionsPreview() = SamplePreview {
    CornerActions(
        moved = IntOffset.Zero,
        edge = ScreenEdge.TOP,
        options = ViewerOptions(),
        isFavourite = false,
        onFavourite = {},
        onShare = {},
    )
}

@Preview(name = "Corner row, favourited and stepped clear of a camera")
@Composable
private fun CornerActionsClearPreview() = SamplePreview {
    CornerActions(
        moved = IntOffset(x = 0, y = 126),
        edge = ScreenEdge.TOP,
        options = ViewerOptions(),
        isFavourite = true,
        onFavourite = {},
        onShare = {},
    )
}
