package com.devddagnet.hardwareinsets.sample.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.devddagnet.hardwareinsets.lib.domain.CutoutShape

/**
 * What the platform reported, drawn over the plate: the cutout rectangles, and
 * the depth each edge says content must stay off.
 *
 * The rectangles are the thing Compose will not give you. The depths it will,
 * and they are drawn beside them because the difference is the whole argument:
 * a centred punch-hole reports a depth across the entire width.
 */
internal fun Modifier.markers(cutout: CutoutShape, isShowing: Boolean): Modifier =
    drawWithContent {
        drawContent()
        if (!isShowing) return@drawWithContent

        val depths = listOf(
            Offset(0f, 0f) to Size(size.width, cutout.topInset.toFloat()),
            Offset(0f, size.height - cutout.bottomInset) to Size(size.width, cutout.bottomInset.toFloat()),
            Offset(0f, 0f) to Size(cutout.leftInset.toFloat(), size.height),
            Offset(size.width - cutout.rightInset, 0f) to Size(cutout.rightInset.toFloat(), size.height),
        )
        depths.filter { it.second.width > 0f && it.second.height > 0f }.forEach { (at, of) ->
            drawRect(InsetMarker.copy(alpha = 0.10f), topLeft = at, size = of)
            drawRect(
                InsetMarker.copy(alpha = 0.85f),
                topLeft = at,
                size = of,
                style = Stroke(width = 2.dp.toPx()),
            )
        }

        cutout.bounds.forEach { rect ->
            val topLeft = Offset(rect.left.toFloat(), rect.top.toFloat())
            val of = Size(rect.width().toFloat(), rect.height().toFloat())
            drawRect(CutoutMarker.copy(alpha = 0.45f), topLeft = topLeft, size = of)
            drawRect(CutoutMarker, topLeft = topLeft, size = of, style = Stroke(width = 3.dp.toPx()))
        }
    }
