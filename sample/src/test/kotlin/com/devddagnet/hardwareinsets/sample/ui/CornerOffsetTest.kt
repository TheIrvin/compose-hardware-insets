package com.devddagnet.hardwareinsets.sample.ui

import androidx.compose.ui.unit.IntOffset
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import com.devddagnet.hardwareinsets.lib.domain.ScreenEdge

/**
 * The clearance arrives as distances inward from the control's own corner, and
 * `Modifier.offset` reads them as "towards the end" and "downwards". Three of
 * the four corners therefore need a sign turned, and the one that does not is
 * the only one an emulator can put a cutout in.
 */
class CornerOffsetTest {

    private val inward = IntOffset(x = 40, y = 24)

    @Test
    fun `the top start corner takes the distances as they come`() {
        assertThat(inward.awayFromTheHardware(ScreenEdge.TOP, isAtTheEnd = false))
            .isEqualTo(IntOffset(40, 24))
    }

    @Test
    fun `the top end corner moves back towards the start`() {
        assertThat(inward.awayFromTheHardware(ScreenEdge.TOP, isAtTheEnd = true))
            .isEqualTo(IntOffset(-40, 24))
    }

    @Test
    fun `the bottom start corner moves up rather than further down`() {
        assertThat(inward.awayFromTheHardware(ScreenEdge.BOTTOM, isAtTheEnd = false))
            .isEqualTo(IntOffset(40, -24))
    }

    @Test
    fun `the bottom end corner turns both`() {
        assertThat(inward.awayFromTheHardware(ScreenEdge.BOTTOM, isAtTheEnd = true))
            .isEqualTo(IntOffset(-40, -24))
    }

    @Test
    fun `a side edge is left alone, because the clearance is zero there`() {
        assertThat(IntOffset.Zero.awayFromTheHardware(ScreenEdge.LEFT, isAtTheEnd = false))
            .isEqualTo(IntOffset.Zero)
        assertThat(IntOffset.Zero.awayFromTheHardware(ScreenEdge.RIGHT, isAtTheEnd = true))
            .isEqualTo(IntOffset.Zero)
    }

    @Test
    fun `no hardware in the corner moves nothing, in every corner`() {
        for (edge in listOf(ScreenEdge.TOP, ScreenEdge.BOTTOM)) {
            for (end in listOf(false, true)) {
                assertThat(IntOffset.Zero.awayFromTheHardware(edge, end)).isEqualTo(IntOffset.Zero)
            }
        }
    }
}
