package com.devddagnet.hardwareinsets.lib

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import com.devddagnet.hardwareinsets.lib.domain.ScreenEdge
import com.devddagnet.hardwareinsets.lib.domain.ScreenRotation
import com.devddagnet.hardwareinsets.lib.domain.onScreenAt

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

    private companion object {
        val ROTATIONS = ScreenRotation.entries
    }
}
