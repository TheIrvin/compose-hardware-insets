package io.github.damson.hardwareinsets

import android.view.Surface
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import io.github.damson.hardwareinsets.domain.ScreenEdge
import io.github.damson.hardwareinsets.domain.onScreenAt

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
        assertThat(ScreenEdge.LEFT.onScreenAt(Surface.ROTATION_90))
            .isEqualTo(ScreenEdge.BOTTOM)
    }

    @Test
    fun `Should put the right edge along the top While turned anticlockwise`() {
        assertThat(ScreenEdge.RIGHT.onScreenAt(Surface.ROTATION_90))
            .isEqualTo(ScreenEdge.TOP)
    }

    @Test
    fun `Should swap the two While turned the other way`() {
        assertThat(ScreenEdge.LEFT.onScreenAt(Surface.ROTATION_270))
            .isEqualTo(ScreenEdge.TOP)
        assertThat(ScreenEdge.RIGHT.onScreenAt(Surface.ROTATION_270))
            .isEqualTo(ScreenEdge.BOTTOM)
    }

    @Test
    fun `Should give a side a horizontal edge While the screen has not turned yet`() {
        // The frame or two before the lock lands; a rail here would flash up.
        for (rotation in listOf(Surface.ROTATION_0, Surface.ROTATION_180)) {
            assertThat(ScreenEdge.LEFT.onScreenAt(rotation).isHorizontalEdge).isTrue()
            assertThat(ScreenEdge.RIGHT.onScreenAt(rotation).isHorizontalEdge).isTrue()
        }
    }

    private companion object {
        val ROTATIONS = listOf(
            Surface.ROTATION_0,
            Surface.ROTATION_90,
            Surface.ROTATION_180,
            Surface.ROTATION_270,
        )
    }
}
