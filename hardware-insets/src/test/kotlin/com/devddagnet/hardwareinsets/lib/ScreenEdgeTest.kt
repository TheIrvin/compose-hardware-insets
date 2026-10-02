package com.devddagnet.hardwareinsets.lib

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import com.devddagnet.hardwareinsets.lib.domain.ScreenEdge
import com.devddagnet.hardwareinsets.lib.domain.ScreenRotation
import com.devddagnet.hardwareinsets.lib.domain.onScreenAt
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

/**
 * The mapping from the edge the user named to the edge the layout uses.
 *
 * Getting it backwards is invisible to every other test: content still appears
 * on a long edge and still looks right, it is just on the opposite side of the
 * phone from the one that was chosen. Only the pairing below says which.
 */
class ScreenEdgeTest {

    @Test
    fun `Should leave a horizontal edge alone Whatever way round the screen is`() {
        for (rotation in ROTATIONS) {
            assertThat(ScreenEdge.TOP.onScreenAt(rotation)).isEqualTo(ScreenEdge.TOP)
            assertThat(ScreenEdge.BOTTOM.onScreenAt(rotation))
                .isEqualTo(ScreenEdge.BOTTOM)
        }
    }

    @Test
    fun `Should put the left edge along the bottom While turned anticlockwise`() {
        assertThat(ScreenEdge.LEFT.onScreenAt(ScreenRotation.QUARTER))
            .isEqualTo(ScreenEdge.BOTTOM)
    }

    @Test
    fun `Should put the right edge along the top While turned anticlockwise`() {
        assertThat(ScreenEdge.RIGHT.onScreenAt(ScreenRotation.QUARTER))
            .isEqualTo(ScreenEdge.TOP)
    }

    @Test
    fun `Should swap the two While turned the other way`() {
        assertThat(ScreenEdge.LEFT.onScreenAt(ScreenRotation.THREE_QUARTERS))
            .isEqualTo(ScreenEdge.TOP)
        assertThat(ScreenEdge.RIGHT.onScreenAt(ScreenRotation.THREE_QUARTERS))
            .isEqualTo(ScreenEdge.BOTTOM)
    }

    @Test
    fun `Should give a side a horizontal edge While the screen has not turned yet`() {
        // The frame or two before the lock lands; a rail here would flash up.
        for (rotation in listOf(ScreenRotation.NONE, ScreenRotation.HALF)) {
            assertThat(ScreenEdge.LEFT.onScreenAt(rotation).isHorizontalEdge).isTrue()
            assertThat(ScreenEdge.RIGHT.onScreenAt(rotation).isHorizontalEdge).isTrue()
        }
    }

    @Test
    fun `Should place horizontal edges at their start or end`() {
        assertThat(ScreenEdge.TOP.toAlignment()).isEqualTo(Alignment.TopStart)
        assertThat(ScreenEdge.TOP.toAlignment(isAtTheEnd = true)).isEqualTo(Alignment.TopEnd)
        assertThat(ScreenEdge.BOTTOM.toAlignment()).isEqualTo(Alignment.BottomStart)
        assertThat(ScreenEdge.BOTTOM.toAlignment(isAtTheEnd = true)).isEqualTo(Alignment.BottomEnd)
    }

    @Test
    fun `Should map a device edge before placing it`() {
        assertThat(ScreenEdge.LEFT.onScreenAt(ScreenRotation.QUARTER).toAlignment())
            .isEqualTo(Alignment.BottomStart)
        assertThat(ScreenEdge.RIGHT.onScreenAt(ScreenRotation.QUARTER).toAlignment())
            .isEqualTo(Alignment.TopStart)
        assertThat(ScreenEdge.LEFT.onScreenAt(ScreenRotation.THREE_QUARTERS).toAlignment())
            .isEqualTo(Alignment.TopStart)
        assertThat(ScreenEdge.RIGHT.onScreenAt(ScreenRotation.THREE_QUARTERS).toAlignment())
            .isEqualTo(Alignment.BottomStart)
    }

    @Test
    fun `Should resolve start and end in the layout direction`() {
        val size = IntSize(10, 20)
        val space = IntSize(100, 200)

        assertThat(ScreenEdge.TOP.toAlignment().align(size, space, LayoutDirection.Ltr))
            .isEqualTo(IntOffset(0, 0))
        assertThat(ScreenEdge.TOP.toAlignment().align(size, space, LayoutDirection.Rtl))
            .isEqualTo(IntOffset(90, 0))
        assertThat(
            ScreenEdge.TOP.toAlignment(isAtTheEnd = true).align(size, space, LayoutDirection.Ltr),
        )
            .isEqualTo(IntOffset(90, 0))
        assertThat(
            ScreenEdge.TOP.toAlignment(isAtTheEnd = true).align(size, space, LayoutDirection.Rtl),
        )
            .isEqualTo(IntOffset(0, 0))
    }

    @Test
    fun `Should reject device edges that have not been mapped onto the screen`() {
        assertThatThrownBy { ScreenEdge.LEFT.toAlignment() }
            .isInstanceOf(IllegalArgumentException::class.java)
        assertThatThrownBy { ScreenEdge.RIGHT.toAlignment(isAtTheEnd = true) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    private companion object {
        val ROTATIONS = ScreenRotation.entries
    }
}
